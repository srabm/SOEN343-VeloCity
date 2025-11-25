<template>
  <div class="bg-cover bg-center no-topbar-offset" style="background-image: url('/src/assets/montreal-architecture.jpg');">
    <div class="min-h-screen bg-black/20 overflow-auto pb-4">
      <div class="reservation-page container mx-auto px-4">
      <!-- Loading State -->
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>Loading reservation details...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="error-container">
        <div class="error-icon">⚠️</div>
        <h2>Reservation Error</h2>
        <p>{{ error }}</p>
        <button @click="goToMap" class="btn btn-primary">Return to Map</button>
      </div>

      <!-- Expired Reservation -->
      <div v-else-if="reservationExpired" class="expired-container">
        <div class="expired-icon">⏰</div>
        <h2>Reservation Expired</h2>
        <p>Your reservation has expired. Please return to the map to reserve another bike.</p>
        <button @click="goToMap" class="btn btn-primary">Return to Map</button>
      </div>

      <!-- Active Reservation -->
      <div v-else-if="bike" class="reservation-active">
        <h1>Bike Reserved! ✅</h1>
        
        <div class="bike-details">
          <h2>Reservation Details</h2>
          <div class="detail-row">
            <span class="label">Bike ID:</span>
            <span class="value">{{ bike.bikeId }}</span>
          </div>
          <div class="detail-row">
            <span class="label">Bike Type:</span>
            <span class="value">{{ bike.type }}</span>
          </div>
          <div class="detail-row">
            <span class="label">Station:</span>
            <span class="value">{{ stationName }}</span>
          </div>
          <div class="detail-row">
            <span class="label">Dock ID:</span>
            <span class="value">{{ bike.dockId }}</span>
          </div>
        </div>

        <div class="expiry-section">
          <h3>⏰ Reservation Expires At</h3>
          <div class="expiry-time" :class="{ 'expiry-warning': isExpiringSoon }">
            {{ formatExpiryTime(expiryDate) }}
          </div>
          <p class="expiry-subtext" :class="{ 'warning-text': isExpiringSoon }">
            {{ isExpiringSoon ? '⚠️ Hurry! Your reservation is expiring soon!' : 'Head to the dock to unlock your bike' }}
          </p>
          <p class="expiry-detail">{{ formatTimeRemaining(expiryDate) }}</p>
        </div>

        <div class="unlock-section">
          <h3>🔓 Unlock Your Bike</h3>
          <p>Enter the dock code displayed on the dock to start your trip:</p>
          
          <form @submit.prevent="unlockBike">
            <div class="form-group">
              <label for="dockCode">Dock Code</label>
              <input
                id="dockCode"
                v-model="dockCode"
                type="text"
                placeholder="Enter dock code (e.g., 1234)"
                :disabled="unlocking"
                required
              />
            </div>

            <div v-if="unlockError" class="error-message">
              {{ unlockError }}
            </div>

            <button type="submit" class="btn btn-success" :disabled="unlocking || !dockCode">
              {{ unlocking ? 'Unlocking...' : '🚴 Unlock & Start Trip' }}
            </button>
          </form>
        </div>

        <button @click="cancelReservation" class="btn btn-secondary">
          ❌ Cancel Reservation
        </button>
      </div>
    </div>
  </div>
</div>
</template>

<script>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { getAuth } from 'firebase/auth';
import { bikeApi, tripApi } from '../services/api';

export default {
  name: 'BikeReservation',
  
  setup() {
    const router = useRouter();
    const route = useRoute();
    const auth = getAuth();

    // State
    const loading = ref(true);
    const error = ref(null);
    const bike = ref(null);
    const stationName = ref('');
    const expiryDate = ref(null);
    const reservationExpired = ref(false);
    const dockCode = ref('');
    const unlocking = ref(false);
    const unlockError = ref(null);
    let checkExpiryInterval = null;

    // Get current user
    const currentUser = computed(() => auth.currentUser);

    // Check if expiring soon (less than 5 minutes)
    const isExpiringSoon = computed(() => {
      if (!expiryDate.value) return false;
      const now = new Date();
      const diff = expiryDate.value - now;
      return diff < 5 * 60 * 1000 && diff > 0; // Less than 5 minutes
    });

    // Parse Firestore Timestamp to JavaScript Date
    const parseFirestoreTimestamp = (timestamp) => {
      if (!timestamp) return null;
      
      console.log('Parsing timestamp:', timestamp);
      
      // Handle Firestore Timestamp object {_seconds, _nanoseconds}
      if (timestamp._seconds !== undefined) {
        return new Date(timestamp._seconds * 1000);
      }
      
      // Handle plain object with seconds/nanoseconds
      if (timestamp.seconds !== undefined) {
        return new Date(timestamp.seconds * 1000);
      }
      
      // Handle ISO string
      if (typeof timestamp === 'string') {
        return new Date(timestamp);
      }
      
      // Handle Date object
      if (timestamp instanceof Date) {
        return timestamp;
      }
      
      console.warn('Unknown timestamp format:', timestamp);
      return null;
    };

    // Format expiry time (e.g., "2:45 PM")
    const formatExpiryTime = (date) => {
      if (!date) return 'Unknown';
      
      return date.toLocaleTimeString('en-US', {
        hour: 'numeric',
        minute: '2-digit',
        hour12: true
      });
    };

    // Format time remaining (e.g., "in 12 minutes")
    const formatTimeRemaining = (date) => {
      if (!date) return '';
      
      const now = new Date();
      const diff = Math.floor((date - now) / 1000); // seconds
      
      if (diff <= 0) {
        return 'Expired';
      }
      
      const minutes = Math.floor(diff / 60);
      const seconds = diff % 60;
      
      if (minutes > 0) {
        return `(${minutes} minute${minutes !== 1 ? 's' : ''} remaining)`;
      } else {
        return `(${seconds} second${seconds !== 1 ? 's' : ''} remaining)`;
      }
    };

    // Check if reservation has expired
    const checkExpiry = async () => {
      if (!expiryDate.value) return;
      
      const now = new Date();
      if (now >= expiryDate.value) {
        await bikeApi.updateBikeStatus(bike.value.bikeId, 'available');
        bike.value.reservedByUserId = null;
        reservationExpired.value = true;
        if (checkExpiryInterval) {
          clearInterval(checkExpiryInterval);
        }
      }
    };

    // Load reservation details
    const loadReservation = async () => {
      try {
        loading.value = true;
        error.value = null;

        const bikeId = route.query.bikeId;
        if (!bikeId) {
          throw new Error('No bike ID provided');
        }

        console.log('Loading reservation for bike:', bikeId);

        // Fetch bike details
        const response = await bikeApi.getBikeById(bikeId);
        
        console.log('Bike API response:', response);

        if (!response.success) {
          throw new Error(response.error || 'Failed to load bike details');
        }

        bike.value = response.bike;
        stationName.value = route.query.stationName || 'Unknown Station';
        
        // Parse reservation expiry
        const parsedExpiry = parseFirestoreTimestamp(bike.value.reservationExpiry);
        
        if (!parsedExpiry) {
          throw new Error('Invalid reservation expiry time');
        }
        
        expiryDate.value = parsedExpiry;
        
        console.log('Expiry date:', expiryDate.value);

        // Verify bike is reserved by current user
        if (bike.value.reservedByUserId !== currentUser.value?.uid) {
          throw new Error('This bike is not reserved by you');
        }

        // Check if already expired
        checkExpiry();
        
        // Check expiry every second
        checkExpiryInterval = setInterval(checkExpiry, 1000);

      } catch (err) {
        console.error('Error loading reservation:', err);
        error.value = err.message || 'Failed to load reservation details';
      } finally {
        loading.value = false;
      }
    };

    // Unlock bike and start trip
    const unlockBike = async () => {
      try {
        unlocking.value = true;
        unlockError.value = null;

        console.log('Starting trip with:', {
          bikeId: bike.value.bikeId,
          riderId: currentUser.value.uid,
          dockCode: dockCode.value
        });

        const response = await tripApi.startTripFromReservation(
          bike.value.bikeId,
          currentUser.value.uid,
          dockCode.value
        );

        console.log('Start trip response:', response);

        if (!response.success) {
          throw new Error(response.error || 'Failed to unlock bike');
        }

        // Navigate to active trip page
        router.push({
          name: 'ActiveTrip',
          query: {
            bikeId: bike.value.bikeId,
            bikeType: bike.value.type
          }
        });

      } catch (err) {
        console.error('Error unlocking bike:', err);
        unlockError.value = err.error || err.message || 'Failed to unlock bike. Please check the dock code and try again.';
      } finally {
        unlocking.value = false;
      }
    };

    // Cancel reservation
    const cancelReservation = async () => {
      if (!confirm('Are you sure you want to cancel this reservation?')) {
        return;
      }

      try {
        // Update bike status back to available
        await bikeApi.updateBikeStatus(bike.value.bikeId, 'available');
        router.push({ name: 'Home' });
      } catch (err) {
        console.error('Error canceling reservation:', err);
        alert('Failed to cancel reservation. Please try again.');
      }
    };

    // Navigate to map
    const goToMap = () => {
      router.push({ name: 'Home' });
    };

    // Lifecycle
    onMounted(() => {
      loadReservation();
    });

    onUnmounted(() => {
      if (checkExpiryInterval) {
        clearInterval(checkExpiryInterval);
      }
    });

    return {
      loading,
      error,
      bike,
      stationName,
      expiryDate,
      reservationExpired,
      isExpiringSoon,
      dockCode,
      unlocking,
      unlockError,
      formatExpiryTime,
      formatTimeRemaining,
      unlockBike,
      cancelReservation,
      goToMap,
    };
  },
};
</script>

<style scoped>
.reservation-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem;
}

.container {
  max-width: 600px;
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
  border-top: 4px solid #667eea;
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

.error-container,
.expired-container {
  background: white;
  border-radius: 12px;
  padding: 3rem;
  text-align: center;
}

.error-icon,
.expired-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.reservation-active {
  background: white;
  border-radius: 12px;
  padding: 2rem;
}

.reservation-active h1 {
  color: #4CAF50;
  text-align: center;
  margin-bottom: 2rem;
}

.bike-details {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.bike-details h2 {
  margin-top: 0;
  margin-bottom: 1rem;
  color: #333;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
  border-bottom: 1px solid #e0e0e0;
}

.detail-row:last-child {
  border-bottom: none;
}

.label {
  font-weight: 600;
  color: #666;
}

.value {
  color: #333;
  font-family: monospace;
}

.expiry-section {
  text-align: center;
  margin-bottom: 2rem;
  background: #f0f8ff;
  padding: 1.5rem;
  border-radius: 8px;
}

.expiry-section h3 {
  margin-top: 0;
  color: #333;
}

.expiry-time {
  font-size: 2.5rem;
  font-weight: bold;
  color: #4CAF50;
  margin: 1rem 0;
}

.expiry-warning {
  color: #ff9800;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.expiry-subtext {
  color: #666;
  font-size: 1rem;
  margin: 0.5rem 0;
}

.warning-text {
  color: #ff9800;
  font-weight: 600;
}

.expiry-detail {
  color: #888;
  font-size: 0.9rem;
  font-style: italic;
}

.unlock-section {
  background: #e8f5e9;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.unlock-section h3 {
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
  border-color: #4CAF50;
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
  margin-bottom: 0.5rem;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #5568d3;
}

.btn-success {
  background: #4CAF50;
  color: white;
}

.btn-success:hover:not(:disabled) {
  background: #45a049;
}

.btn-secondary {
  background: #f44336;
  color: white;
}

.btn-secondary:hover {
  background: #da190b;
}
</style>