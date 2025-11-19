<template>
  <topbar />
  <div class="bg-cover bg-center" style="background-image: url('/src/assets/bike-bg.jpg');">
    <div class="min-h-screen bg-black/40">
      <header class="text-center py-8 text-white drop-shadow">
        <h1 class="text-3xl font-semibold">All Ride History (All Users)</h1>
      </header>

      <section class="grid grid-cols-1 md:grid-cols-2 gap-4 max-w-6xl mx-auto px-4 items-start">
        <!-- Left: Selected Ride Details -->
        <div class="rounded-xl p-5 shadow-lg bg-white/50 backdrop-blur-md min-h-[210px]">
          <h2 class="text-xl font-semibold mb-3">Ride Details</h2>

          <template v-if="selectedRide">
            <div class="space-y-2 text-sm text-slate-800">
              <p><strong>Ride ID:</strong> {{ selectedRide.tripId }}</p>
              <p><strong>Rider Email:</strong> {{ selectedRide.riderEmail || "—" }}</p>
              <p><strong>Time:</strong> {{ formatDateTime(selectedRide.startTime) }} → {{ formatDateTime(selectedRide.endTime) }}</p>
              <p><strong>Route:</strong> {{ selectedRide.startStationName }} → {{ selectedRide.endStationName }}</p>
              <p><strong>Bike ID:</strong> {{ selectedRide.bikeId }}</p>
              <p><strong>Bike Type:</strong> {{ selectedRide.bikeType }}</p>
            </div>

            <h3 class="text-lg font-medium mt-5 mb-2">Bill Summary</h3>
            <div v-if="selectedRide.bill" class="space-y-1 text-sm">
              <p><strong>Price:</strong> ${{ selectedRide.bill?.cost?.toFixed(2) || '0.00' }}</p>
              <p><strong>Tax:</strong> ${{ selectedRide.bill?.tax?.toFixed(2) || '0.00' }}</p>
              <p><strong>Total:</strong> ${{ selectedRide.bill?.total?.toFixed(2) || '0.00' }}</p>
            </div>
            <p v-else class="text-sm text-slate-600">No billing data available.</p>

            <button @click="reportProblem(selectedRide)"
              class="mt-4 bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600 duration-200">
              Report a problem with this bike
            </button>
          </template>

          <template v-else>
            <p class="text-sm text-slate-700">No ride selected.</p>
          </template>
        </div>

        <!-- Right: List + Filters -->
        <div class="rounded-xl p-5 shadow-lg bg-white/50 backdrop-blur-md">
          <h2 class="text-xl font-semibold mb-3">Past Rides</h2>

          <!-- Filters -->
          <div class="space-y-2 mb-3">
            <div class="flex gap-2">
              <input v-model="filters.searchId" type="text" placeholder="Search by Ride ID"
                class="flex-1 px-3 py-2 rounded bg-white/90 border border-slate-300 text-sm" />
              <input v-model="filters.riderEmail" type="text" placeholder="Search by Rider Email"
                class="flex-1 px-3 py-2 rounded bg-white/90 border border-slate-300 text-sm" />
            </div>
            
            <div class="flex gap-2">
              <select v-model="filters.bikeType" class="flex-1 px-3 py-2 rounded bg-white/90 border border-slate-300 text-sm">
                <option value="">All bike types</option>
                <option value="standard">standard</option>
                <option value="electric">electric</option>
              </select>
              <input v-model="filters.station" type="text" placeholder="Search by Station"
                class="flex-1 px-3 py-2 rounded bg-white/90 border border-slate-300 text-sm" />
            </div>

            <div class="flex gap-2 items-center">
              <label class="text-sm text-slate-700">From:
                <input v-model="filters.dateFrom" type="date"
                  class="ml-1 px-2 py-1 rounded bg-white/90 border border-slate-300 text-sm" />
              </label>
              <label class="text-sm text-slate-700">To:
                <input v-model="filters.dateTo" type="date"
                  class="ml-1 px-2 py-1 rounded bg-white/90 border border-slate-300 text-sm" />
              </label>
              <button @click="clearFilters"
                class="ml-auto bg-slate-200 text-black px-3 py-1.5 rounded hover:bg-slate-300 duration-200 text-sm">
                Clear filters
              </button>
            </div>
          </div>

          <!-- List content -->
          <div v-if="filteredRides.length === 0" class="text-sm">
            <p class="text-slate-700">No rides match your filters.</p>
            <p class="text-slate-500">Try clearing filters or searching a different Ride ID.</p>
          </div>

          <ul v-else class="divide-y divide-slate-200">
            <li v-for="ride in filteredRides" :key="ride._id"
              :class="['py-2 cursor-pointer', ride._id === selectedRide?._id ? 'bg-blue-50' : '']"
              @click="selectRide(ride)">
              <div class="flex justify-between text-sm">
                <span class="font-semibold">{{ ride.tripId }}</span>
                <span class="text-purple-600">{{ ride.riderEmail || "—" }}</span>
                <span>{{ formatDate(ride.startTime) }}</span>
                <span class="font-semibold">${{ ride.bill?.total?.toFixed(2) || '—' }}</span>
              </div>
            </li>
          </ul>

          
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import topbar from './topbar.vue'
import { ref, computed, onMounted } from "vue";
import { getAuth, onAuthStateChanged } from "firebase/auth";
import { tripApi } from "../services/api.js"; // updated api.js
import { getFirestore, collection, query, where, orderBy, limit, getDocs, doc, getDoc } from "firebase/firestore";
import app from "../../firebase-config.js";

const db = getFirestore(app);

const user = ref(null);
const allRides = ref([]);
const selectedRide = ref(null);
const canLoadMore = ref(false);

const filters = ref({
  searchId: "",
  riderEmail: "",
  bikeType: "",
  station: "",
  dateFrom: "",
  dateTo: "",
});

async function loadInitialRides() {
  try {
    const tripsCol = collection(db, "trips");
    // Remove the user filter to get all trips
    const tripsQuery = query(
      tripsCol,
      orderBy("startTime", "desc"),
      limit(100) // Increased limit for operator view
    );

    const tripsSnap = await getDocs(tripsQuery);

    // Fetch rider emails for all trips
    const ridesWithEmails = await Promise.all(
      tripsSnap.docs.map(async (tripDoc) => {
        const tripData = tripDoc.data();
        let riderEmail = "—";
        
        // Fetch rider email
        if (tripData.riderId) {
          try {
            const riderRef = doc(db, "riders", tripData.riderId);
            const riderSnap = await getDoc(riderRef);
            if (riderSnap.exists()) {
              riderEmail = riderSnap.data().email || tripData.riderId;
            }
          } catch (error) {
            console.error("Error fetching rider:", error);
            riderEmail = tripData.riderId;
          }
        }

        return {
          _id: tripDoc.id,
          ...tripData,
          riderEmail: riderEmail,
          startTime: tripData.startTime?.toDate ? tripData.startTime.toDate() : new Date(tripData.startTime),
          endTime: tripData.endTime?.toDate ? tripData.endTime.toDate() : new Date(tripData.endTime),
          riderName: tripData.riderName || "Unknown Rider",
          bikeType: tripData.bikeType || "BIKE",
          bill: tripData.bill || null,
        };
      })
    );

    allRides.value = ridesWithEmails;

    if (allRides.value.length > 0) selectedRide.value = allRides.value[0];
    canLoadMore.value = false;

  } catch (error) {
    console.error("Error loading trips:", error);
    allRides.value = [];
  }
}

function selectRide(ride) {
  selectedRide.value = ride;
}

function clearFilters() {
  filters.value = {
    searchId: "",
    riderEmail: "",
    bikeType: "",
    station: "",
    dateFrom: "",
    dateTo: "",
  };
}

function reportProblem(ride) {
  const issue = prompt("Describe the problem with this bike:");
  if (!issue) return;

  tripApi.reportProblem(ride.tripId, issue)
    .then((res) => {
      if (res.success) alert("Problem reported successfully!");
      else alert("Failed to report problem: " + res.error);
    })
    .catch(() => alert("Error reporting problem, check console."));
}

const filteredRides = computed(() => {
  return allRides.value.filter((ride) => {
    // Ride ID filter
    if (filters.value.searchId && !ride.tripId.toLowerCase().includes(filters.value.searchId.toLowerCase())) return false;

    // Rider Email filter
    if (filters.value.riderEmail && !ride.riderEmail?.toLowerCase().includes(filters.value.riderEmail.toLowerCase())) return false;

    // Bike Type filter
    if (filters.value.bikeType && ride.bikeType !== filters.value.bikeType) return false;

    // Station filter (checks both start and end station)
    if (filters.value.station) {
      const searchTerm = filters.value.station.toLowerCase();
      const startMatch = ride.startStationName?.toLowerCase().includes(searchTerm);
      const endMatch = ride.endStationName?.toLowerCase().includes(searchTerm);
      if (!startMatch && !endMatch) return false;
    }

    // Date filters
    if (ride.startTime) {
      const rideDate = new Date(ride.startTime);
      const rideLocalDate = new Date(rideDate.getFullYear(), rideDate.getMonth(), rideDate.getDate());

      // Inclusive start date
      if (filters.value.dateFrom) {
        const fromDate = new Date(filters.value.dateFrom);
        const fromLocalDate = new Date(fromDate.getFullYear(), fromDate.getMonth(), fromDate.getDate());
        if (rideLocalDate < fromLocalDate - 1) return false; // ride is before start date
      }

      // Inclusive end date
      if (filters.value.dateTo) {
        const toDate = new Date(filters.value.dateTo);
        const toLocalDate = new Date(toDate.getFullYear(), toDate.getMonth(), toDate.getDate());
        if (rideLocalDate > toLocalDate) return false; // ride is after end date
      }
    }

    return true;
  });
});

function formatDateTime(date) {
  if (!date) return "Unknown";
  return date.toLocaleString();
}

function formatDate(date) {
  if (!date) return "Unknown";
  return date.toLocaleDateString();
}

onMounted(() => {
  const auth = getAuth();
  onAuthStateChanged(auth, async (firebaseUser) => {
    if (firebaseUser) {
      user.value = firebaseUser;
      await loadInitialRides();
    } else {
      console.warn("No user logged in");
    };
  });
});
</script>

<style scoped></style>