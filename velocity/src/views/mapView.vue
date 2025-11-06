<template>
  <div id="map"></div>
</template>

<script>
import L from "leaflet";
import { collection, firestore, getDocs } from '../../firebaseAuth.js';
import { getAuth } from 'firebase/auth';
import { bikeApi } from '../services/api';

let stations = [];
try {
  const stationsRef = collection(firestore, 'stations');
  const stationsSnapshot = await getDocs(stationsRef);
  stations = stationsSnapshot.docs.map(doc => ({
    id: doc.id,
    ...doc.data()
  }));
  console.log('Successfully loaded', stations.length, 'stations');
} catch (error) {
  console.error('Error fetching stations:', error);
}

export default {
  name: "MapView",

  data() {
    return {
      initialCenter: [45.5017, -73.5673], // Montreal coordinates
      initialZoom: 13,
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
    async handleReserveBike(event) {
      const { stationId, stationName } = event.detail;
      
      console.log('=================================');
      console.log('🚴 RESERVE BIKE DEBUG START');
      console.log('=================================');
      console.log('Station ID:', stationId);
      console.log('Station Name:', stationName);

      // Get Firebase auth instance
      const auth = getAuth();
      const currentUser = auth.currentUser;

      console.log('Current User:', currentUser);
      console.log('User ID:', currentUser?.uid);
      console.log('User Email:', currentUser?.email);

      // Check if user is logged in
      if (!currentUser) {
        console.log('❌ User not logged in - redirecting to login');
        alert('Please log in to reserve a bike.');
        this.$router.push({ name: 'Login' });
        return;
      }

      console.log('✅ User is logged in');

      try {
        console.log('---');
        console.log('Step 1: Calling API to get available bikes...');
        console.log('API URL:', import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api');
        console.log('Requesting bikes for stationId:', stationId);
        
        // Get available bikes at this station
        const response = await bikeApi.getAvailableBikes(stationId);
        
        console.log('---');
        console.log('Step 2: API Response received');
        console.log('Full response object:', JSON.stringify(response, null, 2));
        console.log('response.success:', response.success);
        console.log('response.bikes:', response.bikes);
        console.log('response.count:', response.count);
        console.log('response.error:', response.error);

        // Check if response has the expected structure
        if (typeof response !== 'object') {
          console.error('❌ Response is not an object:', typeof response);
          alert('Invalid response from server. Check console for details.');
          return;
        }

        // Check for error in response
        if (response.error) {
          console.error('❌ API returned an error:', response.error);
          alert('Error: ' + response.error);
          return;
        }

        // Check success flag
        if (response.success === false) {
          console.error('❌ API returned success=false');
          alert('Failed to get available bikes. Check console for details.');
          return;
        }

        // Check if bikes array exists
        if (!response.bikes) {
          console.error('❌ response.bikes is undefined or null');
          console.log('Response keys:', Object.keys(response));
          alert('Invalid response structure. Missing bikes array.');
          return;
        }

        // Check if bikes array is empty
        if (!Array.isArray(response.bikes)) {
          console.error('❌ response.bikes is not an array:', typeof response.bikes);
          alert('Invalid bikes data. Expected array.');
          return;
        }

        console.log('Bikes array length:', response.bikes.length);

        if (response.bikes.length === 0) {
          console.log('❌ No available bikes at this station');
          alert('No available bikes at this station. Please try another station.');
          return;
        }

        console.log('✅ Found', response.bikes.length, 'available bikes');
        console.log('Bikes:', response.bikes);

        // Select first available bike
        const selectedBike = response.bikes[0];
        console.log('---');
        console.log('Step 3: Selected bike:', selectedBike);
        console.log('Bike ID:', selectedBike.bikeId);
        console.log('Bike Type:', selectedBike.type);
        console.log('Bike Status:', selectedBike.status);

        console.log('---');
        console.log('Step 4: Calling API to reserve bike...');
        
        // Reserve the bike
        const reservationResponse = await bikeApi.reserveBike(
          selectedBike.bikeId,
          currentUser.uid,
          stationId
        );

        console.log('Reservation response:', JSON.stringify(reservationResponse, null, 2));

        if (reservationResponse.success) {
          console.log('✅ Bike reserved successfully!');
          console.log('Navigating to reservation page...');
          
          // Navigate to reservation page
          this.$router.push({
            name: 'BikeReservation',
            query: {
              bikeId: selectedBike.bikeId,
              stationName: stationName,
              stationId: stationId
            }
          });
        } else {
          console.error('❌ Reservation failed');
          console.error('Error:', reservationResponse.error);
          alert('Failed to reserve bike: ' + (reservationResponse.error || 'Unknown error'));
        }

      } catch (error) {
        console.log('---');
        console.error('❌ EXCEPTION CAUGHT:');
        console.error('Error object:', error);
        console.error('Error message:', error.message);
        console.error('Error stack:', error.stack);
        
        if (error.response) {
          console.error('Error response:', error.response);
          console.error('Error response data:', error.response.data);
          console.error('Error response status:', error.response.status);
        }
        
        if (error.error) {
          console.error('Error.error:', error.error);
        }
        
        alert('Failed to reserve bike. Check browser console for detailed error information.');
      }
      
      console.log('=================================');
      console.log('🚴 RESERVE BIKE DEBUG END');
      console.log('=================================');
    },

    resetViewAnimated() {
      this.map.flyTo(this.initialCenter, this.initialZoom, {
        duration: 1.5,
        easeLinearity: 0.25
      });
    }
  },

  mounted() {
    // Add event listener for reserve button clicks
    window.addEventListener('reserveBike', this.handleReserveBike);

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

    // Add markers for all stations
    for (const station of stations) {
      // icon color based on station status
      let icon = this.icons.green; // default
      if (station.status === 'out_of_service') {
        icon = this.icons.black;
      } else if (station.numDockedBikes === 0) {
        icon = this.icons.red;
      } else if (station.numDockedBikes < 5) {
        icon = this.icons.yellow;
      }

      L.marker([station.latitude, station.longitude], { icon })
        .addTo(this.map)
        .bindPopup(
          station.status === 'out_of_service'
            ? `<b>Station ${station.stationName}</b><br><i>Temporarily Closed</i>`
            : `<b>Station ${station.stationName}</b><br>` +
            `Available Regular Bikes: ${station.nbRegBikes || 0}<br>` +
            `Available Electric Bikes: ${station.nbElectricBikes || 0}<br>` +
            `Available Docks: ${station.capacity - station.numDockedBikes || 0}<br>` +
            (station.numDockedBikes > 0
              ? `<button onclick="window.dispatchEvent(new CustomEvent('reserveBike', 
                      {detail: {stationId: '${station.id}', stationName: '${station.stationName}'}}))">
                      Reserve Bike
                     </button>`
              : '')
        );
    }

    setTimeout(() => {
      this.map.invalidateSize();
    }, 100);
  },

  beforeUnmount() {
    if (this.map) {
      this.map.remove();
    }
    // Remove the event listener
    window.removeEventListener('reserveBike', this.handleReserveBike);
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
</style>