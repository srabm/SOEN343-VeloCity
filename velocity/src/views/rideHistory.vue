<template>
  <topbar />
  <TierNotification
    v-if="tierChange.oldTier && tierChange.newTier"
    :oldTier="tierChange.oldTier"
    :newTier="tierChange.newTier"
    @close="dismissTierNotification"
  />
  <div class="bg-cover bg-center" style="background-image: url('/src/assets/montreal-architecture.jpg');">
    <div class="min-h-screen bg-black/20">
      <header class="text-center py-8 text-white drop-shadow">
        <h1 class="text-3xl font-extrabold">Ride History</h1>
      </header>

      <section class="grid grid-cols-1 md:grid-cols-2 gap-4 max-w-6xl mx-auto px-4 items-start">
        <!-- Left: Selected Ride Details -->
        <div class="rounded-xl p-5 shadow-lg bg-white/50 backdrop-blur-md min-h-[210px]">
          <h2 class="text-xl font-semibold mb-3">Ride Details</h2>

          <template v-if="selectedRide">
            <div class="space-y-2 text-sm text-slate-800">
              <p><strong>Ride ID:</strong> {{ selectedRide.tripId }}</p>
              <p><strong>Rider Email:</strong> {{ user?.email }}</p>
              <p><strong>Time:</strong> {{ formatDateTime(selectedRide.startTime) }} → {{ formatDateTime(selectedRide.endTime) }}</p>
              <p><strong>Route:</strong> {{ selectedRide.startStationName }} → {{ selectedRide.endStationName }}</p>
              <p><strong>Bike ID:</strong> {{ selectedRide.bikeId }}</p>
              <p><strong>Bike Type:</strong> {{ selectedRide.bikeType }}</p>
            </div>

            <h3 class="text-lg font-medium mt-5 mb-2">Bill Summary</h3>
            <div v-if="selectedRide.bill" class="space-y-1 text-sm">
              <p><strong>Price:</strong> ${{ selectedRide.bill?.baseCost?.toFixed(2) || '0.00' }}</p>
              <p v-if="selectedRide.bill?.discount && selectedRide.bill.discount > 0" class="text-green-800">
                <strong>Tier Discount:</strong> -${{ selectedRide.bill.discount.toFixed(2) }}
              </p>
              <p v-if="selectedRide.bill?.operatorDiscount && selectedRide.bill.operatorDiscount > 0" class="text-green-800">
                <strong>Operator Discount:</strong> -${{ selectedRide.bill.operatorDiscount.toFixed(2) }}
              </p>
              <p v-if="selectedRide.flexRedeemedAmount" class="text-green-800">
                <strong>Flex Discount:</strong> -$0.50
              </p>
              <br v-if="selectedRide.bill?.cost != selectedRide.bill?.baseCost && selectedRide.bill.cost" />
              <p v-if="selectedRide.bill?.cost != selectedRide.bill?.baseCost && selectedRide.bill.cost" ><strong>Price (with discount):</strong> ${{ selectedRide.bill?.cost?.toFixed(2) || '0.00' }}</p>
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
        <div class="rounded-xl p-5 shadow-lg bg-white/50 backdrop-blur-md max-h-[500px] overflow-y-auto">
          <h2 class="text-xl font-semibold mb-3">Past Rides</h2>

          <!-- Filters -->
          <div class="space-y-2 mb-3">
            <div class="flex gap-2">
              <input v-model="filters.searchId" type="text" placeholder="Search by Ride ID"
                class="flex-1 px-3 py-2 rounded bg-white/90 border border-slate-300 text-sm" />
              <select v-model="filters.bikeType" class="px-3 py-2 rounded bg-white/90 border border-slate-300 text-sm">
                <option value="">All bike types</option>
                <option value="standard">standard</option>
                <option value="electric">electric</option>
              </select>
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

          <div class="ride-list-scroll">
            <ul class="divide-y divide-slate-200">
              <li v-for="ride in filteredRides" :key="ride._id"
                  :class="['py-2 cursor-pointer', ride._id === selectedRide?._id ? 'bg-blue-50' : '']"
                  @click="selectRide(ride)">
                <div class="flex justify-between text-sm">
                  <span class="font-semibold">{{ ride.tripId }}</span>
                  <span>{{ formatDate(ride.startTime) }}</span>
                  <span class="font-semibold">${{ ride.bill?.total?.toFixed(2) || '—' }}</span>
                </div>
              </li>
            </ul>
          </div>


          
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import topbar from './topbar.vue'
import TierNotification from '../components/TierNotification.vue'
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
const tierChange = ref({ oldTier: null, newTier: null });

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

function checkForTierChange(userId) {
  // Check for pending tier change notification
  const tierChangeKey = `pending_tier_change_${userId}`;
  const pendingTierChange = localStorage.getItem(tierChangeKey);
  
  if (pendingTierChange) {
    try {
      const tierData = JSON.parse(pendingTierChange);
      tierChange.value = {
        oldTier: tierData.oldTier,
        newTier: tierData.newTier
      };
      // Clear the pending notification so it doesn't show again
      localStorage.removeItem(tierChangeKey);
    } catch (e) {
      console.error('Error parsing tier change data:', e);
      localStorage.removeItem(tierChangeKey);
    }
  }
}

function dismissTierNotification() {
  tierChange.value = { oldTier: null, newTier: null };
}

onMounted(() => {
  const auth = getAuth();
  onAuthStateChanged(auth, async (firebaseUser) => {
    if (firebaseUser) {
      user.value = firebaseUser;
      // Check for tier change notification first
      checkForTierChange(firebaseUser.uid);
      await loadInitialRides();
    } else {
      console.warn("No user logged in");
    };
  });
});
</script>

<style scoped>
.ride-list-scroll {
  max-height: 60vh;      /* Keeps it on one screen */
  overflow-y: auto;      /* Enables vertical scrolling */
  padding-right: 6px;    /* For scrollbar spacing */
}

.ride-list-scroll::-webkit-scrollbar {
  width: 6px;
}

.ride-list-scroll::-webkit-scrollbar-thumb {
  background: #cbd5e1;   /* Slate-300 */
  border-radius: 4px;
}

.ride-list-scroll::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;   /* Slate-400 */
}

</style>
