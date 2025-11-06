<template>
  <div class="ride-history-page">
    <!-- Left: Selected Ride Details -->
    <section class="ride-details" v-if="selectedRide">
      <h1>Ride Details</h1>

      <p><strong>Ride ID:</strong> {{ selectedRide.rideId }}</p>
      <p><strong>Rider Name:</strong> {{ selectedRide.riderName }}</p>

      <p>
        <strong>Time:</strong>
        {{ formatDateTime(selectedRide.startTime) }}
        →
        {{ formatDateTime(selectedRide.endTime) }}
      </p>

      <p>
        <strong>Route:</strong>
        {{ selectedRide.originStationName }} →
        {{ selectedRide.arrivalStationName }}
      </p>

      <p><strong>Bike ID:</strong> {{ selectedRide.bikeId }}</p>
      <p><strong>Bike Type:</strong> {{ selectedRide.bikeType }}</p>

    

     <h2>Bill Summary</h2>
     <div v-if="selectedRide.bill">
     <p><strong>Price:</strong> ${{ selectedRide.bill?.price?.toFixed(2) || '0.00' }}</p>
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
            <option value="BIKE">Bike</option>
            <option value="E-BIKE">E-Bike</option>
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
            <span class="ride-id">{{ ride.rideId }}</span>
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
import {
  getFirestore,
  collection,
  query,
  where,
  orderBy,
  limit,
  getDocs,
} from "firebase/firestore";
import app from "../../firebase-config.js";

const auth = getAuth(app);
const db = getFirestore(app);

const user = ref(null);
const allRides = ref([]);        
const selectedRide = ref(null);
const canLoadMore = ref(false);  // placeholder for pagination later

const filters = ref({
  searchId: "",
  bikeType: "",
  dateFrom: "",
  dateTo: "",
});


async function loadInitialRides() {
  if (!user.value?.email) return;

  const ridersCol = collection(db, "riders");
  const riderQuery = query(ridersCol, where("email", "==", user.value.email));
  const riderSnap = await getDocs(riderQuery);

  if (riderSnap.empty) {
    console.warn("No rider found for email:", user.value.email);
    allRides.value = [];
    return;
  }

  // Get the rider's document ID 
  const riderDocId = riderSnap.docs[0].id;
  console.log("Found rider document:", riderDocId);


  const ridesCol = collection(db, "riders", riderDocId, "rides");
  const ridesQuery = query(ridesCol, orderBy("startTime", "desc"), limit(20));

  const ridesSnap = await getDocs(ridesQuery);
  allRides.value = ridesSnap.docs.map((doc) => ({
    _id: doc.id,
    ...doc.data(),
  }));

  console.log("Loaded rides:", allRides.value);

  if (allRides.value.length > 0) {
    selectedRide.value = allRides.value[0]; // most recent
  }

  canLoadMore.value = false;
}


function formatDateTime(ts) {
  if (!ts) return "Unknown";
  const date = ts.toDate ? ts.toDate() : new Date(ts);
  return date.toLocaleString();
}

function formatDate(ts) {
  if (!ts) return "Unknown";
  const date = ts.toDate ? ts.toDate() : new Date(ts);
  return date.toLocaleDateString();
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
  // For now, just log, maybe later we can use it to navigate to a report page, or open a modal with a form
  console.log("Report problem for ride:", ride.rideId);
}

function loadMore() {
  // Placeholder for now
  console.log("Load more clicked");
}


const filteredRides = computed(() => {
  return allRides.value.filter((ride) => {
    // Search by rideId
    if (
      filters.value.searchId &&
      !ride.rideId.toLowerCase().includes(filters.value.searchId.toLowerCase())
    ) {
      return false;
    }

    // Bike type filter
    if (filters.value.bikeType && ride.bikeType !== filters.value.bikeType) {
      return false;
    }

    // Date range
    const rideDate = ride.startTime?.toDate
      ? ride.startTime.toDate()
      : ride.startTime
      ? new Date(ride.startTime)
      : null;

    if (rideDate) {
      if (filters.value.dateFrom) {
        const from = new Date(filters.value.dateFrom);
        from.setHours(0, 0, 0, 0);
        if (rideDate < from) return false;
      }

      if (filters.value.dateTo) {
        const to = new Date(filters.value.dateTo);
        to.setHours(23, 59, 59, 999);
        if (rideDate > to) return false;
      }
    }

    return true;
  });
});

onMounted(() => {
  onAuthStateChanged(auth, async (firebaseUser) => {
    if (firebaseUser) {
      user.value = firebaseUser;
      await loadInitialRides();
    } else {
      console.warn("No user logged in, cannot load ride history.");
    }
  });
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
