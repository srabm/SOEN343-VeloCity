// services/api.js
import axios from 'axios';

// Configure base URL - update this to match your backend URL
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add request interceptor to include auth token if available
api.interceptors.request.use(
  (config) => {
    // You can add Firebase auth token here if needed
    // const token = getCurrentUserToken();
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      // Server responded with error status
      console.error('API Error:', error.response.data);
    } else if (error.request) {
      // Request made but no response received
      console.error('Network Error:', error.message);
    } else {
      // Something else happened
      console.error('Error:', error.message);
    }
    return Promise.reject(error);
  }
);

// ==================== Bike API Calls ====================

export const bikeApi = {
  /**
   * Reserve a bike at a station
   * @param {string} bikeId - ID of the bike to reserve
   * @param {string} userId - ID of the user making reservation
   * @param {string} stationId - ID of the station where bike is located
   */
  async reserveBike(bikeId, userId, stationId) {
    try {
      const response = await api.post('/bikes/reserve', {
        bikeId,
        userId,
        stationId,
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Get available bikes at a station
   * @param {string} stationId - ID of the station
   */
  async getAvailableBikes(stationId) {
    try {
      const response = await api.get('/bikes/available', {
        params: { stationId },
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Get bike details by ID
   * @param {string} bikeId - ID of the bike
   */
  async getBikeById(bikeId) {
    try {
      const response = await api.get(`/bikes/${bikeId}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Update bike status
   * @param {string} bikeId - ID of the bike
   * @param {string} status - New status (available, reserved, maintenance, on_trip)
   */
  async updateBikeStatus(bikeId, status) {
    try {
      const response = await api.patch(`/bikes/${bikeId}/status`, { status });
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },
};

// ==================== Trip API Calls ====================

export const tripApi = {
  /**
   * Start a trip from a reserved bike
   * @param {string} bikeId - ID of the reserved bike
   * @param {string} riderId - ID of the rider
   * @param {string} dockCode - Code to unlock the dock
   */
  async startTripFromReservation(bikeId, riderId, dockCode) {
    try {
      const response = await api.post('/trips/start/reserved', {
        bikeId,
        riderId,
        dockCode,
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Start a trip directly without reservation
   * @param {string} dockId - ID of the dock
   * @param {string} dockCode - Code to unlock the dock
   * @param {string} riderId - ID of the rider
   */
  async startTripDirect(dockId, dockCode, riderId) {
    try {
      const response = await api.post('/trips/start/direct', {
        dockId,
        dockCode,
        riderId,
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * End a trip by docking the bike
   * @param {string} bikeId - ID of the bike
   * @param {string} riderId - ID of the rider
   * @param {string} dockId - ID of the dock to return to
   * @param {string} dockCode - Code to lock the dock
   */
  async endTrip(bikeId, riderId, dockId, dockCode) {
    try {
      const response = await api.post('/trips/end', {
        bikeId,
        riderId,
        dockId,
        dockCode,
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Get trip details by ID
   * @param {string} tripId - ID of the trip
   */
  async getTripById(tripId) {
    try {
      const response = await api.get(`/trips/${tripId}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Get bill details by ID
   * @param {string} billId - ID of the bill
   */
  async getBillById(billId) {
    try {
      const response = await api.get(`/trips/bills/${billId}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },
    /**
   * Get all trips for the logged-in user
   * @param {string} userId
   */
  getRiderTrips: async (userId) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/trips/rider/${userId}`);
      return response.data;
    } catch (error) {
      console.error("Error fetching rider trips:", error);
      throw error;
    }
  },

  /**
   * Get a single trip by ID
   */
  getTripById: async (tripId) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/trips/${tripId}`);
      return response.data;
    } catch (error) {
      console.error("Error fetching trip:", error);
      throw error;
    }
  },

  /**
   * Report a problem with a bike (placeholder)
   */
  reportProblem: async (tripId, issue) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/trips/${tripId}/report`, { issue });
      return response.data;
    } catch (error) {
      console.error("Error reporting problem:", error);
      throw error;
    }
  },
};

// ==================== Transfer API Calls ====================

export const transferApi = {
  /**
   * Get all stations
   */
  async getAllStations() {
    try {
      const response = await api.get('/transfer/stations');
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Get available bikes at a specific station
   * @param {string} stationId - ID of the station
   */
  async getAvailableBikesAtStation(stationId) {
    try {
      const response = await api.get(`/transfer/stations/${stationId}/bikes`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Get available docks at a specific station
   * @param {string} stationId - ID of the station
   */
  async getAvailableDocksAtStation(stationId) {
    try {
      const response = await api.get(`/transfer/stations/${stationId}/docks`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  /**
   * Transfer a bike from one dock to another
   * @param {Object} transferRequest - Transfer details
   * @param {string} transferRequest.bikeId - ID of the bike to transfer
   * @param {string} transferRequest.sourceDockId - ID of the source dock
   * @param {string} transferRequest.destinationDockId - ID of the destination dock
   * @param {string} transferRequest.sourceStationId - ID of the source station
   * @param {string} transferRequest.destinationStationId - ID of the destination station
   */
  async transferBike(transferRequest) {
    try {
      const response = await api.post('/transfer/bike', transferRequest);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },
};

export default api;