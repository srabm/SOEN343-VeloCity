<template>
  <topbar />
  <div class="move-bike-container">
    <div class="header">
      <h1>Move Bike</h1>
      <p class="subtitle">Transfer bikes between stations and docks</p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Loading...</p>
    </div>

    <!-- Error/Success Messages -->
    <div v-if="error" class="message error-message">
      <span class="icon">⚠️</span>
      <span>{{ error }}</span>
      <button @click="error = null" class="close-btn">×</button>
    </div>

    <div v-if="success" class="message success-message">
      <span class="icon">✓</span>
      <span>{{ success }}</span>
      <button @click="resetForm" class="close-btn">×</button>
    </div>

    <!-- Main Form - Single Page -->
    <div v-if="!loading" class="transfer-form">
      
      <!-- Source Selection -->
      <div class="form-section">
        <h2>Source</h2>
        <div class="section-content">
          
          <!-- Source Station -->
          <div class="form-group">
            <label>Select Station with Bike</label>
            <input
              v-model="stationSearchTerm"
              type="text"
              placeholder="Search stations..."
              class="search-input"
              @input="filterStations"
            />
            <select v-model="selectedSourceStation" @change="loadSourceBikes" class="select-input">
              <option :value="null">-- Select Station --</option>
              <option
                v-for="station in filteredSourceStations"
                :key="station.stationId"
                :value="station"
                :disabled="station.numDockedBikes === 0"
              >
                {{ station.stationName }} - {{ station.numDockedBikes }} bikes ({{ station.streetAddress }})
              </option>
            </select>
          </div>

          <!-- Source Bike -->
          <div v-if="selectedSourceStation" class="form-group">
            <label>Select Bike to Move</label>
            <select v-model="selectedBike" class="select-input">
              <option :value="null">-- Select Bike --</option>
              <option
                v-for="bike in availableBikes"
                :key="bike.bikeId"
                :value="bike"
              >
                {{ bike.bikeId }} - {{ bike.type === 'electric' ? '⚡ Electric' : '🚲 Standard' }} (Dock: {{ bike.dockId }})
              </option>
            </select>
          </div>

        </div>
      </div>

      <!-- Arrow Indicator -->
      <div v-if="selectedBike" class="transfer-arrow">
        <div class="arrow">→</div>
      </div>

      <!-- Destination Selection -->
      <div v-if="selectedBike" class="form-section">
        <h2>Destination</h2>
        <div class="section-content">
          
          <!-- Destination Station -->
          <div class="form-group">
            <label>Select Destination Station</label>
            <input
              v-model="destStationSearchTerm"
              type="text"
              placeholder="Search stations..."
              class="search-input"
              @input="filterDestStations"
            />
            <select v-model="selectedDestStation" @change="loadDestinationDocks" class="select-input">
              <option :value="null">-- Select Station --</option>
              <option
                v-for="station in filteredDestStations"
                :key="station.stationId"
                :value="station"
                :disabled="station.numDockedBikes >= station.capacity || station.status === 'out_of_service'"
              >
                {{ station.stationName }} - {{ station.capacity - station.numDockedBikes }} empty docks ({{ station.streetAddress }})
              </option>
            </select>
          </div>

          <!-- Destination Dock -->
          <div v-if="selectedDestStation" class="form-group">
            <label>Select Empty Dock</label>
            <select v-model="selectedDestDock" class="select-input">
              <option :value="null">-- Select Dock --</option>
              <option
                v-for="dock in availableDocks"
                :key="dock.dockId"
                :value="dock"
              >
                {{ dock.dockId }}
              </option>
            </select>
          </div>

        </div>
      </div>

      <!-- Transfer Summary & Action -->
      <div v-if="selectedDestDock" class="transfer-summary">
        <h3>Transfer Summary</h3>
        <div class="summary-grid">
          <div class="summary-item">
            <span class="label">Bike:</span>
            <span class="value">{{ selectedBike.bikeId }} ({{ selectedBike.type }})</span>
          </div>
          <div class="summary-item">
            <span class="label">From:</span>
            <span class="value">{{ selectedSourceStation.stationName }} - {{ selectedBike.dockId }}</span>
          </div>
          <div class="summary-item">
            <span class="label">To:</span>
            <span class="value">{{ selectedDestStation.stationName }} - {{ selectedDestDock.dockId }}</span>
          </div>
          <div class="summary-item">
            <span class="label">Type:</span>
            <span class="value">
              {{ isInterStationTransfer ? 'Inter-Station Transfer' : 'Intra-Station Transfer' }}
            </span>
          </div>
        </div>
        
        <div class="button-group">
          <button @click="resetForm" class="btn btn-secondary">Reset</button>
          <button 
            @click="confirmTransfer" 
            :disabled="transferring"
            class="btn btn-primary"
          >
            {{ transferring ? 'Transferring...' : 'Confirm Transfer' }}
          </button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { transferApi } from '../services/api.js';
import topbar from './topbar.vue';

const loading = ref(false);
const error = ref(null);
const success = ref(null);
const transferring = ref(false);

// Data
const allStations = ref([]);
const availableBikes = ref([]);
const availableDocks = ref([]);

// Selected items
const selectedSourceStation = ref(null);
const selectedBike = ref(null);
const selectedDestStation = ref(null);
const selectedDestDock = ref(null);

// Search terms
const stationSearchTerm = ref('');
const destStationSearchTerm = ref('');

// Filtered lists
const filteredSourceStations = ref([]);
const filteredDestStations = ref([]);

// Computed
const isInterStationTransfer = computed(() => {
  if (!selectedSourceStation.value || !selectedDestStation.value) return false;
  return selectedSourceStation.value.stationId !== selectedDestStation.value.stationId;
});

// Methods
const loadStations = async () => {
  loading.value = true;
  error.value = null;
  try {
    allStations.value = await transferApi.getAllStations();
    filteredSourceStations.value = allStations.value.filter(s => s.numDockedBikes > 0);
  } catch (err) {
    error.value = 'Failed to load stations. Please try again.';
    console.error('Error loading stations:', err);
  } finally {
    loading.value = false;
  }
};

const filterStations = () => {
  if (!stationSearchTerm.value) {
    filteredSourceStations.value = allStations.value.filter(s => s.numDockedBikes > 0);
  } else {
    const term = stationSearchTerm.value.toLowerCase();
    filteredSourceStations.value = allStations.value.filter(station =>
      station.numDockedBikes > 0 &&
      (station.stationName.toLowerCase().includes(term) ||
       station.streetAddress.toLowerCase().includes(term))
    );
  }
};

const filterDestStations = () => {
  let stations = allStations.value.filter(
    s => selectedSourceStation.value && s.stationId !== selectedSourceStation.value.stationId
  );

  if (destStationSearchTerm.value) {
    const term = destStationSearchTerm.value.toLowerCase();
    stations = stations.filter(station =>
      station.stationName.toLowerCase().includes(term) ||
      station.streetAddress.toLowerCase().includes(term)
    );
  }

  filteredDestStations.value = stations;
};

const loadSourceBikes = async () => {
  if (!selectedSourceStation.value) return;
  
  // Reset downstream selections
  selectedBike.value = null;
  selectedDestStation.value = null;
  selectedDestDock.value = null;
  availableBikes.value = [];
  availableDocks.value = [];
  
  loading.value = true;
  error.value = null;
  try {
    availableBikes.value = await transferApi.getAvailableBikesAtStation(
      selectedSourceStation.value.stationId
    );
    if (availableBikes.value.length === 0) {
      error.value = 'No available bikes at this station.';
    }
    // Prepare destination stations list
    filterDestStations();
  } catch (err) {
    error.value = 'Failed to load bikes. Please try again.';
    console.error('Error loading bikes:', err);
  } finally {
    loading.value = false;
  }
};

const loadDestinationDocks = async () => {
  if (!selectedDestStation.value) return;
  
  // Reset dock selection
  selectedDestDock.value = null;
  availableDocks.value = [];
  
  loading.value = true;
  error.value = null;
  try {
    availableDocks.value = await transferApi.getAvailableDocksAtStation(
      selectedDestStation.value.stationId
    );
    if (availableDocks.value.length === 0) {
      error.value = 'No empty docks at this station.';
    }
  } catch (err) {
    error.value = 'Failed to load docks. Please try again.';
    console.error('Error loading docks:', err);
  } finally {
    loading.value = false;
  }
};

const confirmTransfer = async () => {
  transferring.value = true;
  error.value = null;
  try {
    const result = await transferApi.transferBike({
      bikeId: selectedBike.value.bikeId,
      sourceDockId: selectedBike.value.dockId,
      destinationDockId: selectedDestDock.value.dockId,
      sourceStationId: selectedSourceStation.value.stationId,
      destinationStationId: selectedDestStation.value.stationId
    });
    
    success.value = `Successfully transferred ${selectedBike.value.bikeId} from ${selectedSourceStation.value.stationName} to ${selectedDestStation.value.stationName}`;
    
    // Reload stations to get updated counts
    setTimeout(() => {
      loadStations();
    }, 2000);
    
  } catch (err) {
    error.value = err.message || 'Failed to transfer bike. Please try again.';
    console.error('Error transferring bike:', err);
  } finally {
    transferring.value = false;
  }
};

const resetForm = () => {
  selectedSourceStation.value = null;
  selectedBike.value = null;
  selectedDestStation.value = null;
  selectedDestDock.value = null;
  availableBikes.value = [];
  availableDocks.value = [];
  stationSearchTerm.value = '';
  destStationSearchTerm.value = '';
  success.value = null;
  error.value = null;
};

// Load stations when component mounts
onMounted(() => {
  loadStations();
});
</script>

<style scoped>
.move-bike-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem;
}

.header {
  text-align: center;
  margin-bottom: 2rem;
}

.header h1 {
  font-size: 2.5rem;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.subtitle {
  color: #7f8c8d;
  font-size: 1.1rem;
}

/* Loading */
.loading-container {
  text-align: center;
  padding: 3rem;
}

.spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
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

/* Messages */
.message {
  padding: 1rem 1.5rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  animation: slideIn 0.3s ease;
}

.error-message {
  background: #fee;
  border: 1px solid #fcc;
  color: #c33;
}

.success-message {
  background: #efe;
  border: 1px solid #cfc;
  color: #3c3;
}

.message .icon {
  font-size: 1.5rem;
}

.close-btn {
  margin-left: auto;
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: inherit;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.close-btn:hover {
  opacity: 1;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Transfer Form */
.transfer-form {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.form-section {
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  padding: 1.5rem;
}

.form-section h2 {
  color: #2c3e50;
  font-size: 1.5rem;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #e0e0e0;
}

.section-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.95rem;
}

.search-input,
.select-input {
  padding: 0.75rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
  background: white;
}

.search-input:focus,
.select-input:focus {
  outline: none;
  border-color: #3498db;
}

.select-input {
  cursor: pointer;
}

.select-input option:disabled {
  color: #ccc;
}

/* Transfer Arrow */
.transfer-arrow {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0.5rem 0;
}

.arrow {
  font-size: 2.5rem;
  color: #3498db;
  font-weight: bold;
}

/* Transfer Summary */
.transfer-summary {
  background: #f8f9fa;
  border: 2px solid #3498db;
  border-radius: 12px;
  padding: 1.5rem;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.transfer-summary h3 {
  color: #2c3e50;
  margin-bottom: 1rem;
  font-size: 1.3rem;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.summary-item .label {
  color: #7f8c8d;
  font-size: 0.85rem;
  font-weight: 500;
}

.summary-item .value {
  color: #2c3e50;
  font-weight: 600;
  font-size: 1rem;
}

/* Buttons */
.button-group {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

.btn {
  padding: 0.875rem 2rem;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: #27ae60;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #229954;
  box-shadow: 0 4px 12px rgba(39, 174, 96, 0.3);
  transform: translateY(-2px);
}

.btn-secondary {
  background: #95a5a6;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background: #7f8c8d;
}

/* Responsive */
@media (max-width: 768px) {
  .move-bike-container {
    padding: 1rem;
  }

  .header h1 {
    font-size: 2rem;
  }

  .form-section {
    padding: 1rem;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .button-group {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }

  .arrow {
    font-size: 2rem;
    transform: rotate(90deg);
  }
}

@media (max-width: 480px) {
  .header h1 {
    font-size: 1.75rem;
  }

  .subtitle {
    font-size: 1rem;
  }
}
</style>