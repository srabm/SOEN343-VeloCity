<template>
  <div id="map"></div>

  <!-- Reserve modal: the reservation content will be inside this container -->
  <div id="reserveBikeModal" class="modal">
    <div class="modal-content">
      <div class="modal-header">
        <span class="close" id="reserveClose">&times;</span>
        <h2 id="reserveTitle">Reserve Bike</h2>
      </div>
      <div class="modal-body" id="reserveBody">
        <p id="reserveInfo">Reserve details will appear here.</p>
      </div>
    </div>
  </div>

</template>

<script>
import L from "leaflet";
import { collection, firestore, getDocs, doc, updateDoc, onSnapshot } from '../../firebaseAuth.js';
import { getAuth } from 'firebase/auth';
import { bikeApi } from '../services/api';

let stations = [];
let bikes = {};
let stationsUnsubscribe = null;
let bikesUnsubscribe = null;

// Set up real-time listener for stations
const stationsRef = collection(firestore, 'stations');
stationsUnsubscribe = onSnapshot(stationsRef, (snapshot) => {
  stations = snapshot.docs.map(doc => ({
    id: doc.id,
    ...doc.data()
  }));
  console.log('Stations updated:', stations.length);

  // Trigger map refresh if map exists
  if (window.mapInstance) {
    window.dispatchEvent(new Event('dataUpdated'));
  }
}, (error) => {
  console.error('Error listening to stations:', error);
});

// Set up real-time listener for bikes
const bikesRef = collection(firestore, 'bikes');
bikesUnsubscribe = onSnapshot(bikesRef, (snapshot) => {
  bikes = {};
  snapshot.docs.forEach(doc => {
    bikes[doc.id] = {
      id: doc.id,
      ...doc.data()
    };
  });
  console.log('Bikes updated:', Object.keys(bikes).length);

  // Trigger map refresh if map exists
  if (window.mapInstance) {
    window.dispatchEvent(new Event('dataUpdated'));
  }
}, (error) => {
  console.error('Error listening to bikes:', error);
});

export default {
  name: "MapView",

  data() {
    return {
      initialCenter: [45.5017, -73.5673], // Montreal coordinates
      initialZoom: 13,
      markers: [], // Store markers so we can update them
      //custom colored icons
      icons: {
        red: L.icon({
          iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
          shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
          iconSize: [25, 41],
          iconAnchor: [12, 41],
          popupAnchor: [1, -34],
          shadowSize: [41, 41]
        }),
        green: L.icon({
          iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
          shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
          iconSize: [25, 41],
          iconAnchor: [12, 41],
          popupAnchor: [1, -34],
          shadowSize: [41, 41]
        }),
        yellow: L.icon({
          iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-yellow.png',
          shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
          iconSize: [25, 41],
          iconAnchor: [12, 41],
          popupAnchor: [1, -34],
          shadowSize: [41, 41]
        }),
        black: L.icon({
          iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-black.png',
          shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
          iconSize: [25, 41],
          iconAnchor: [12, 41],
          popupAnchor: [1, -34],
          shadowSize: [41, 41]
        })
      }
    };
  },


  methods: {
    handleReserveBike(event) {
      const { stationId, stationName } = event.detail;
      console.log(`Reserving bike at station ${stationName} (ID: ${stationId})`);

      // Find the specific station
      const station = stations.find(s => s.id === stationId);

      if (!station) {
        console.error('Station not found:', stationId);
        return;
      }

      const modal = document.getElementById('reserveBikeModal');
      const title = document.getElementById('reserveTitle');
      const info = document.getElementById('reserveInfo');

      if (!modal || !title || !info) {
        console.warn('Reserve modal elements not found');
        return;
      }

      title.innerHTML = `Bikes available at <strong>${stationName}</strong>`;
      if (station.bikeIds && station.bikeIds.length > 0) {
        info.innerHTML = station.bikeIds.map(bikeId => {
          const bike = bikes[bikeId];
          const bikeType = bike ? ((bike.type === 'e-bike' || bike.type === 'electric') ? 'Electric' : 'Regular') : 'Unknown';
          const bikeStatus = bike ? bike.status : 'unknown';

          // Only show available bikes
          if (bikeStatus !== 'available') {
            return '';
          }

          return `<div class="bike-item">
            <span class="bike-id">${bikeId} <span class="bike-type">(${bikeType})</span></span>
            <button class="reserve-bike-button" data-bike-id="${bikeId}" data-station-id="${stationId}">Reserve</button>
          </div>`;
        }).filter(html => html !== '').join('');

        // Add click handlers to all reserve buttons
        const reserveButtons = info.querySelectorAll('.reserve-bike-button');
        reserveButtons.forEach(button => {
          button.addEventListener('click', async () => {
            const bikeId = button.getAttribute('data-bike-id');
            const stationId = button.getAttribute('data-station-id');
            console.log(`Reserving bike ${bikeId} from station ${stationId}`);

            try {
              // Update bike status to reserved
              const bikeRef = doc(firestore, 'bikes', bikeId);
              await updateDoc(bikeRef, {
                status: 'reserved',
                reservedActive: true,
                reservationExpiry: new Date(Date.now() + 15 * 60 * 1000) // 15 minutes from now
              });

              console.log(`Successfully reserved bike ${bikeId}`);
              alert(`Bike ${bikeId} reserved successfully!`);
              modal.style.display = 'none';
            } catch (error) {
              console.error('Error reserving bike:', error);
              alert('Please login or create an account to reserve a bike.');
            }
          });
        });

        if (info.innerHTML === '') {
          info.innerHTML = 'No bikes currently available';
        }
      } else {
        info.innerHTML = 'No bikes currently available';
      }

      modal.style.display = 'block';

      // close button handler
      const closeBtn = document.getElementById('reserveClose');
      const closeHandler = () => { modal.style.display = 'none'; };
      if (closeBtn) closeBtn.addEventListener('click', closeHandler);

      // click outside to close
      this._modalOutsideClick = (e) => { if (e.target === modal) modal.style.display = 'none'; };
      window.addEventListener('click', this._modalOutsideClick);

      // store handlers so we can remove them on unmount
      this._modalCloseHandler = closeHandler;
    },

    resetViewAnimated() {
      this.map.flyTo(this.initialCenter, this.initialZoom, {
        duration: 1.5,
        easeLinearity: 0.25
      });
    }
  },

  mounted() {
    // Store map instance globally for data update events
    window.mapInstance = true;

    // Add event listener for reserve button clicks (bound so `this` works inside handler)
    this._boundReserveHandler = this.handleReserveBike.bind(this);
    window.addEventListener('reserveBike', this._boundReserveHandler);

    // Add event listener for data updates
    this._boundDataUpdateHandler = this.refreshMarkers.bind(this);
    window.addEventListener('dataUpdated', this._boundDataUpdateHandler);

    //default marker icon fix
    delete L.Icon.Default.prototype._getIconUrl;
    L.Icon.Default.mergeOptions({
      iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
      iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
      shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
    });

    // Create map
    this.map = L.map("map").setView(this.initialCenter, this.initialZoom);

    // Add home button control
    L.Control.HomeButton = L.Control.extend({
      onAdd: (map) => {
        const button = L.DomUtil.create('button', 'home-button');
        button.innerHTML = '🧭';
        button.title = 'Return to Montreal view';
        button.onclick = () => this.resetViewAnimated();
        return button;
      }
    });

    new L.Control.HomeButton({ position: 'topleft' }).addTo(this.map);

    // Use environment variable for API key
    const apiKey = import.meta.env.VITE_MAPTILER_API_KEY;

    L.tileLayer(`https://api.maptiler.com/maps/streets-v2/{z}/{x}/{y}.png?key=${apiKey}`, {
      attribution: '<a href="https://www.maptiler.com/copyright/" target="_blank">&copy; MapTiler</a> <a href="https://www.openstreetmap.org/copyright" target="_blank">&copy; OpenStreetMap contributors</a>',
    }).addTo(this.map);

    // Initial marker rendering
    this.refreshMarkers();

    setTimeout(() => {
      this.map.invalidateSize();
    }, 100);
  },

  beforeUnmount() {
    // Clean up Firestore listeners
    if (stationsUnsubscribe) {
      stationsUnsubscribe();
    }
    if (bikesUnsubscribe) {
      bikesUnsubscribe();
    }

    // Clean up map
    if (this.map) {
      this.map.remove();
    }

    // Clean up global reference
    window.mapInstance = null;

    // Remove event listeners
    if (this._boundReserveHandler) {
      window.removeEventListener('reserveBike', this._boundReserveHandler);
    }
    if (this._boundDataUpdateHandler) {
      window.removeEventListener('dataUpdated', this._boundDataUpdateHandler);
    }
    if (this._modalOutsideClick) {
      window.removeEventListener('click', this._modalOutsideClick);
    }
    const closeBtn = document.getElementById('reserveClose');
    if (closeBtn && this._modalCloseHandler) {
      closeBtn.removeEventListener('click', this._modalCloseHandler);
    }
  }

};
</script>

<style scoped>
#map {
  height: 700px;
  width: 100%;
}

#map :deep(.leaflet-control-attribution) {
  font-size: 9px;
  opacity: 0.7;
}

#map :deep(.leaflet-popup-content button) {
  display: block;
  width: 100%;
  margin-top: 8px;
  padding: 8px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

#map :deep(.leaflet-popup-content button:hover) {
  background-color: #45a049;
}

#map :deep(.home-button) {
  width: 30px;
  height: 30px;
  background: white;
  border: 2px solid rgba(0, 0, 0, 0.2);
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.65);
}

#map :deep(.home-button:hover) {
  background: #f4f4f4;
}

/* Modal styles */
.modal {
  display: none;
  position: fixed;
  z-index: 10050;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  overflow: auto;
  background-color: rgba(0, 0, 0, 0.4);
}

.modal-content {
  background-color: #fefefe;
  margin: 15% auto;
  padding: 20px;
  border: 1px solid #888;
  width: 80%;
  max-width: 500px;
  position: relative;
  z-index: 10051;
  border-radius: 8px;
}

.modal-header {
  margin-bottom: 15px;
}

.close {
  color: #aaa;
  float: right;
  font-size: 28px;
  font-weight: bold;
  line-height: 20px;
}

.close:hover,
.close:focus {
  color: black;
  text-decoration: none;
  cursor: pointer;
}

/* Bike list styles */
.modal-body :deep(.bike-item) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.modal-body :deep(.bike-item:last-child) {
  border-bottom: none;
}

.modal-body :deep(.bike-id) {
  flex: 1;
  font-weight: 500;
}

.modal-body :deep(.bike-type) {
  color: #666;
  font-weight: normal;
  font-size: 0.9em;
}

.modal-body :deep(.reserve-bike-button) {
  padding: 6px 16px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.modal-body :deep(.reserve-bike-button:hover) {
  background-color: #45a049;
}
</style>