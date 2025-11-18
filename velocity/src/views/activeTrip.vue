<template>
  <div class="active-trip-page">
    <div class="container">
      <!-- Loading State -->
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>Loading trip details...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="error-container">
        <div class="error-icon">⚠️</div>
        <h2>Error</h2>
        <p>{{ error }}</p>
        <button @click="goToMap" class="btn btn-primary">Return to Map</button>
      </div>

      <!-- Active Trip -->
      <div v-else class="trip-active">
        <div class="header">
          <h1>🚴 Trip in Progress</h1>
          <div class="trip-duration">
            <span class="label">Duration:</span>
            <span class="duration">{{ formatDuration(tripDuration) }}</span>
          </div>
        </div>

        <div class="bike-info">
          <h2>Bike Information</h2>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">Bike ID</span>
              <span class="info-value">{{ bikeId }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Bike Type</span>
              <span class="info-value">{{ bikeType }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Estimated Cost</span>
              <span class="info-value">${{ estimatedCost.toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <div class="cost-info">
          <p class="cost-note">
            <strong>Note:</strong> 
            {{ bikeType?.toLowerCase().includes('electric') 
              ? 'Electric bikes cost $0.33/minute with a $1.11 unlock fee.' 
              : 'Standard bikes cost $0.22/minute with a $1.11 unlock fee.' }}
          </p>
        </div>

        <div class="end-trip-section">
          <h2>End Your Trip</h2>
          <p>Find an available dock at any station and enter the dock details below:</p>
          
          <form @submit.prevent="endTrip">
            <div class="form-group">
              <label for="dockId">Dock ID</label>
              <input
                id="dockId"
                v-model="endDockId"
                type="text"
                placeholder="Enter dock ID"
                :disabled="ending"
                required
              />
            </div>

            <div class="form-group">
              <label for="endDockCode">Dock Code</label>
              <input
                id="endDockCode"
                v-model="endDockCode"
                type="text"
                placeholder="Enter dock code to lock bike"
                :disabled="ending"
                required
              />
            </div>

            <div v-if="endError" class="error-message">
              {{ endError }}
            </div>

            <button 
              type="submit" 
              class="btn btn-success" 
              :disabled="ending || !endDockId || !endDockCode"
            >
              {{ ending ? 'Ending Trip...' : 'End Trip & Dock Bike' }}
            </button>
          </form>
        </div>

        <div class="help-section">
          <h3>Need Help?</h3>
          <ul>
            <li>Look for a station with available docks (green or yellow markers on map)</li>
            <li>The dock ID is displayed on each dock</li>
            <li>Enter the dock code shown on the dock to lock your bike</li>
            <li>You'll receive a receipt with your trip details after docking</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { getAuth } from 'firebase/auth';
import { tripApi } from '../services/api';


export default {
  name: 'ActiveTrip',

  setup() {
    const router = useRouter();
    const route = useRoute();
    const auth = getAuth();

    // State
    const loading = ref(false);
    const error = ref(null);
    const bikeId = ref('');
    const bikeType = ref('');
    const tripStartTime = ref(new Date());
    const tripDuration = ref(0);
    const endDockId = ref('');
    const endDockCode = ref('');
    const ending = ref(false);
    const endError = ref(null);
    let durationInterval = null;

    // Get current user
    const currentUser = computed(() => auth.currentUser);

    // Calculate estimated cost based on duration and bike type
    const estimatedCost = computed(() => {
      const minutes = Math.ceil(tripDuration.value / 60);
      const isElectric = bikeType.value?.toLowerCase().includes('electric');
      
      if (isElectric) {
        return 1.11 + (minutes * 0.33); // $1.11 unlock + $0.33/min
      } else {
        return 1.11 + (minutes * 0.22); // $1 unlock + $0.22/min
      }
    });

    // Format duration as HH:MM:SS
    const formatDuration = (seconds) => {
      const hours = Math.floor(seconds / 3600);
      const mins = Math.floor((seconds % 3600) / 60);
      const secs = seconds % 60;
      
      if (hours > 0) {
        return `${hours}h ${mins}m ${secs}s`;
      }
      return `${mins}m ${secs}s`;
    };

    // Update trip duration
    const updateDuration = () => {
      const now = new Date();
      const diff = Math.floor((now - tripStartTime.value) / 1000);
      tripDuration.value = diff;
    };

    // Load trip details
    const loadTrip = () => {
      bikeId.value = route.query.bikeId || '';
      bikeType.value = route.query.bikeType || 'Standard';
      
      if (!bikeId.value) {
        error.value = 'No bike information found';
        return;
      }

      // Start duration timer
      updateDuration();
      durationInterval = setInterval(updateDuration, 1000);
    };

    // End trip
    const endTrip = async () => {
      try {
        ending.value = true;
        endError.value = null;

        const response = await tripApi.endTrip(
          bikeId.value,
          currentUser.value.uid,
          endDockId.value,
          endDockCode.value
        );
        
        if (!response.success) {
          throw new Error(response.error || 'Failed to end trip');
        }

        // Check for tier change in response
        if (response.tierChange) {
          // Store tier change info in localStorage to show notification on ride history page
          const tierChangeKey = `pending_tier_change_${currentUser.value.uid}`;
          localStorage.setItem(tierChangeKey, JSON.stringify({
            oldTier: response.tierChange.oldTier,
            newTier: response.tierChange.newTier
          }));
          
          // Update localStorage with new tier
          const storageKey = `rider_tier_${currentUser.value.uid}`;
          localStorage.setItem(storageKey, response.tierChange.newTier);
           }
        
          // Redirect immediately to ride history page
          // The notification will be shown there if there was a tier change
          router.push({ name: 'RideHistory' });

      } catch (err) {
        console.error('Error ending trip:', err);
        endError.value = err.error || err.message || 'Failed to end trip. Please check the dock information and try again.';
      } finally {
        ending.value = false;
      }
    };

    // Navigate to map
    const goToMap = () => {
      router.push({ name: 'MapView' });
    };

    // Lifecycle
    onMounted(() => {
      loadTrip();
    });

    onUnmounted(() => {
      if (durationInterval) {
        clearInterval(durationInterval);
      }
    });

    return {
      loading,
      error,
      bikeId,
      bikeType,
      tripDuration,
      endDockId,
      endDockCode,
      ending,
      endError,
      estimatedCost,
      formatDuration,
      endTrip,
      goToMap,
    };
  },
};
</script>

<style scoped>
.active-trip-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  padding: 2rem;
}

.container {
  max-width: 700px;
  margin: 0 auto;
}

.loading {
  background: white;
  border-radius: 12px;
  padding: 3rem;
  text-align: center;
}

.spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #11998e;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-container {
  background: white;
  border-radius: 12px;
  padding: 3rem;
  text-align: center;
}

.error-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.trip-active {
  background: white;
  border-radius: 12px;
  padding: 2rem;
}

.header {
  text-align: center;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid #e0e0e0;
}

.header h1 {
  color: #11998e;
  margin-bottom: 1rem;
}

.trip-duration {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  font-size: 1.2rem;
}

.trip-duration .label {
  font-weight: 600;
  color: #666;
}

.trip-duration .duration {
  font-size: 2rem;
  font-weight: bold;
  color: #11998e;
}

.bike-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.bike-info h2 {
  margin-top: 0;
  margin-bottom: 1rem;
  color: #333;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  padding: 1rem;
  background: white;
  border-radius: 6px;
}

.info-label {
  font-size: 0.85rem;
  color: #666;
  margin-bottom: 0.5rem;
}

.info-value {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
}

.cost-info {
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1.5rem;
}

.cost-note {
  margin: 0;
  color: #856404;
  font-size: 0.9rem;
}

.end-trip-section {
  background: #e8f5e9;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.end-trip-section h2 {
  margin-top: 0;
  color: #2e7d32;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #333;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #11998e;
}

.error-message {
  background: #ffebee;
  color: #c62828;
  padding: 0.75rem;
  border-radius: 6px;
  margin-bottom: 1rem;
}

.btn {
  width: 100%;
  padding: 1rem;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: #11998e;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #0f8478;
}

.btn-success {
  background: #4CAF50;
  color: white;
}

.btn-success:hover:not(:disabled) {
  background: #45a049;
}

.help-section {
  background: #f0f8ff;
  border-radius: 8px;
  padding: 1.5rem;
}

.help-section h3 {
  margin-top: 0;
  color: #1976d2;
}

.help-section ul {
  margin: 0;
  padding-left: 1.5rem;
}

.help-section li {
  margin-bottom: 0.5rem;
  color: #555;
}
</style>