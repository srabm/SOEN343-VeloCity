<template>
  <div class="ride-history-page">
    <!-- Left: Selected Ride Details -->
    <section class="ride-details" v-if="selectedRide">
      <h1>Ride Details</h1>

      <p><strong>Ride ID:</strong> {{ selectedRide.tripId }}</p>
      <p><strong>Rider Email:</strong> {{ user.email }}</p>

      <p>
        <strong>Time:</strong>
        {{ formatDateTime(selectedRide.startTime) }}
        →
        {{ formatDateTime(selectedRide.endTime) }}
      </p>

      <p>
        <strong>Route:</strong>
        {{ selectedRide.startStationName }} →
        {{ selectedRide.endStationName }}
      </p>

      <p><strong>Bike ID:</strong> {{ selectedRide.bikeId }}</p>
      <p><strong>Bike Type:</strong> {{ selectedRide.bikeType }}</p>

    

     <h2>Bill Summary</h2>
     <div v-if="selectedRide.bill">
     <p><strong>Price:</strong> ${{ selectedRide.bill?.cost?.toFixed(2) || '0.00' }}</p>
     <p><strong>Tax:</strong> ${{ selectedRide.bill?.tax?.toFixed(2) || '0.00' }}</p>
     <p><strong>Total:</strong> ${{ selectedRide.bill?.total?.toFixed(2) || '0.00' }}</p>
     </div>
     <div v-else>
     <p>No billing data available.</p>
     </div>



      <button class="problem-btn" @click="reportProblem(selectedRide)">
        Report a problem with this bike
      </button>
    </section>

    <!-- If no ride selected (no data yet) -->
    <section class="ride-details" v-else>
      <h1>Ride Details</h1>
      <p>No ride selected.</p>
    </section>

    <!-- Right: List + Filters -->
    <section class="ride-list">
      <h2>Past Rides</h2>

      <!-- Filters -->
      <div class="filters">
        <div class="filter-row">
          <input
            v-model="filters.searchId"
            type="text"
            placeholder="Search by Ride ID"
          />

          <select v-model="filters.bikeType">
            <option value="">All bike types</option>
            <option value="standard">standard</option>
            <option value="electric">electric</option>
          </select>
        </div>

        <div class="filter-row">
          <label>
            From:
            <input v-model="filters.dateFrom" type="date" />
          </label>
          <label>
            To:
            <input v-model="filters.dateTo" type="date" />
          </label>

          <button class="clear-btn" @click="clearFilters">
            Clear filters
          </button>
        </div>
      </div>

      <!-- List content -->
      <div v-if="filteredRides.length === 0">
        <p class="no-results">
          No rides match your filters.
          <span class="hint">Try clearing filters or searching a different Ride ID.</span>
        </p>
      </div>

      <ul v-else class="rides-list">
        <li
          v-for="ride in filteredRides"
          :key="ride._id"
          :class="{ selected: ride._id === selectedRide?._id }"
          @click="selectRide(ride)"
        >
          <div class="ride-line">
            <span class="ride-id">{{ ride.tripId }}</span>
            <span class="ride-date">{{ formatDate(ride.startTime) }}</span>
            <span class="ride-cost">${{ ride.bill?.total?.toFixed(2) || '—' }}</span>
          </div>
        </li>
      </ul>

      <!-- Load more -->
      <div class="load-more">
        <button @click="loadMore" :disabled="!canLoadMore">
          Load more
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { getAuth, onAuthStateChanged } from "firebase/auth";
import { tripApi } from "../services/api.js"; // updated api.js
import { getFirestore, collection, query, where, orderBy, limit, getDocs } from "firebase/firestore";
import app from "../../firebase-config.js";

const db = getFirestore(app);

const user = ref(null);
const allRides = ref([]);
const selectedRide = ref(null);
const canLoadMore = ref(false);

const filters = ref({
  searchId: "",
  bikeType: "",
  dateFrom: "",
  dateTo: "",
});

async function loadInitialRides() {
  if (!user.value?.uid) return;

  try {
    const tripsCol = collection(db, "trips");
    const tripsQuery = query(
      tripsCol,
      where("riderId", "==", user.value.uid),
      orderBy("startTime", "desc"),
      limit(20)
    );


    const tripsSnap = await getDocs(tripsQuery);

    allRides.value = tripsSnap.docs.map(doc => ({
      _id: doc.id,
      ...doc.data(),
      startTime: doc.data().startTime?.toDate ? doc.data().startTime.toDate() : new Date(doc.data().startTime),
      endTime: doc.data().endTime?.toDate ? doc.data().endTime.toDate() : new Date(doc.data().endTime),
      riderName: doc.data().riderName || "Unknown Rider",
      bikeType: doc.data().bikeType || "BIKE",
      bill: doc.data().bill || null,
    }));

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
    bikeType: "",
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
    if (filters.value.searchId && !ride.tripId.toLowerCase().includes(filters.value.searchId.toLowerCase())) return false;

    if (filters.value.bikeType && ride.bikeType !== filters.value.bikeType) return false;

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
},
)
});
</script>


<style scoped>
.ride-history-page {
  max-width: 1100px;
  margin: 2rem auto;
  display: grid;
  grid-template-columns: 2fr 1.5fr;
  gap: 1.5rem;
}

/* Left column */
.ride-details {
  padding: 1.5rem;
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

/* Right column */
.ride-list {
  padding: 1.5rem;
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

/* Filters */
.filters {
  margin-bottom: 1rem;
}

.filter-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.filter-row input[type="text"],
.filter-row input[type="date"],
.filter-row select {
  flex: 1;
  padding: 0.4rem 0.6rem;
  border-radius: 6px;
  border: 1px solid #cbd5e0;
  font-size: 0.9rem;
}

.clear-btn {
  padding: 0.4rem 0.7rem;
  border-radius: 6px;
  border: none;
  background-color: #edf2f7;
  cursor: pointer;
}

/* Rides list */
.rides-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.rides-list li {
  padding: 0.6rem 0.4rem;
  border-bottom: 1px solid #e2e8f0;
  cursor: pointer;
}

.rides-list li.selected {
  background-color: #ebf8ff;
}

.ride-line {
  display: flex;
  justify-content: space-between;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.ride-id {
  font-weight: 600;
}

.ride-cost {
  font-weight: 600;
}

/* No results */
.no-results {
  margin-top: 1rem;
  font-size: 0.9rem;
}

.hint {
  display: block;
  font-size: 0.8rem;
  color: #718096;
}

/* Buttons */
.problem-btn {
  margin-top: 1rem;
  background-color: #e53e3e;
  color: white;
  border: none;
  padding: 0.5rem 0.9rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
}

.problem-btn:hover {
  background-color: #c53030;
}

.load-more {
  margin-top: 1rem;
  text-align: center;
}

.load-more button {
  padding: 0.4rem 0.8rem;
  border-radius: 6px;
  border: none;
  background-color: #4299e1;
  color: white;
  cursor: pointer;
}

.load-more button:disabled {
  background-color: #a0aec0;
  cursor: not-allowed;
}

/* Section headers */
.ride-details h1 {
  font-size: 1.4rem;
  font-weight: 700;
  border-bottom: 2px solid #e2e8f0; /* soft gray divider */
  padding-bottom: 0.4rem;
  margin-bottom: 1rem;
  color: #2d3748;
}

.ride-details h2 {
  font-size: 1.2rem;
  font-weight: 700;
  border-bottom: 2px solid #edf2f7;
  margin-top: 1.5rem;
  padding-bottom: 0.4rem;
  color: #2d3748;
}

/* Add subtle separators between data groups */
.ride-section {
  margin-bottom: 1.2rem;
  padding-bottom: 1rem;
  border-bottom: 1px dashed #e2e8f0;
}

/* Improve text spacing */
.ride-details p {
  margin: 0.3rem 0;
  font-size: 0.95rem;
}

/* Emphasize keys (labels) */
.ride-details strong {
  color: #1a202c;
  font-weight: 600;
}

/* Make left column slightly softer background */
.ride-details {
  padding: 1.5rem;
  background: #ffffff;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  border: 1px solid #edf2f7;
}

</style>
