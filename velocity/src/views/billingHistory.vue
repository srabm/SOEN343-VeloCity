<template>
  <topbar />
  <div class="bg-cover bg-center" style="background-image: url('/src/assets/bike-bg.jpg');">
    <div class="min-h-screen bg-black/40">
      <header class="text-center py-8 text-white drop-shadow">
        <h1 class="text-3xl font-semibold">Billing History</h1>
      </header>

      <section class="max-w-6xl mx-auto px-4 pb-4">
        <div class="rounded-xl p-5 shadow-lg bg-white/50 backdrop-blur-md">
          <!-- Filters -->
          <div class="flex flex-wrap items-center gap-3 mb-4">
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
              Clear
            </button>
          </div>

          <!-- Billing Table -->
          <div class="overflow-auto rounded-lg bg-white/80">
            <table class="billing-table w-full">
              <thead>
                <tr>
                  <th>Ride ID</th>
                  <th>Start Date</th>
                  <th>End Date</th>
                  <th>Origin Station</th>
                  <th>Arrival Station</th>
                  <th>Bike Type</th>
                  <th>Total ($)</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                <tr v-for="ride in filteredRides" :key="ride.rideId">
                  <td>{{ ride.rideId }}</td>
                  <td>{{ formatDateTime(ride.startTime) }}</td>
                  <td>{{ formatDateTime(ride.endTime) }}</td>
                  <td>{{ ride.originStationName }}</td>
                  <td>{{ ride.arrivalStationName }}</td>
                  <td>{{ ride.bikeType }}</td>
                  <td>{{ ride.bill?.total?.toFixed(2) || "—" }}</td>

                  <!-- Payment Status -->
                  <td>
                    <span
                      :class="{
                        paid: ride.bill?.paymentStatus === 'PAID',
                        unpaid: ride.bill?.paymentStatus === 'UNPAID',
                      }"
                    >
                      {{ ride.bill?.paymentStatus || "UNKNOWN" }}
                    </span>
                  </td>

                  <!-- Action -->
                  <td>
                    <button
                      v-if="ride.bill?.paymentStatus !== 'PAID'"
                      class="pay-btn"
                      @click="payForRide(ride)"
                    >
                      Pay Now
                    </button>
                    <span v-else>—</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Empty State -->
          <div v-if="filteredRides.length === 0" class="no-results">
            <p>No billing records found.</p>
          </div>
        </div>
      </section>

      <!-- Payment Modal -->
      <div v-if="showPaymentModal" class="modal-overlay">
        <div class="modal">
          <h2>External Payment Gateway</h2>
          <p>Paying for ride: <strong>{{ selectedRide?.rideId }}</strong></p>

          <form @submit.prevent="confirmPayment">
            <label>Card Number</label>
            <input v-model="mockPayment.cardNumber" maxlength="19" placeholder="1234 5678 9012 3456" required />

            <label>Expiry Date</label>
            <input v-model="mockPayment.expiryDate" placeholder="MM/YY" required />

            <label>CVC</label>
            <input v-model="mockPayment.cvc" maxlength="4" placeholder="123" required />

            <div class="modal-actions">
              <button type="submit" class="pay-btn">Confirm Payment</button>
              <button type="button" class="cancel-btn" @click="closeModal">Cancel</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">
      <p>Loading billing history...</p>
    </div>
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
  getDocs,
} from "firebase/firestore";
import app from "../../firebase-config.js";
import topbar from './topbar.vue'

const auth = getAuth(app);
const db = getFirestore(app);

const user = ref(null);
const rides = ref([]);
const filters = ref({ dateFrom: "", dateTo: "" });
import topbar from './topbar.vue'
const bills = ref([]);
const loading = ref(false);
const filters = ref({ 
  dateFrom: "", 
  dateTo: "",
  billId: "",
  station: ""
});
const sortBy = ref("date"); // default sort by date (newest first)

onMounted(() => {
  onAuthStateChanged(auth, async (firebaseUser) => {
    if (firebaseUser) {
      user.value = firebaseUser;
      await loadBillingHistory();
    }
  });
});

async function loadBillingHistory() {
  loading.value = true;
  try {
    // Fetch trips from the top-level trips collection for the current user
    // Bills are embedded within trips as a nested object
    const tripsRef = collection(db, "trips");
    const q = query(tripsRef, where("riderId", "==", user.value.uid));
    const tripsSnap = await getDocs(q);
    
    // Extract bill data from trips
    const extractedBills = [];
    
    for (const tripDoc of tripsSnap.docs) {
      const tripData = tripDoc.data();
      
      // Check if trip has bill data embedded
      if (tripData.bill && tripData.bill.billId) {
        // Create enriched bill object combining bill and trip data
        extractedBills.push({
          // Bill data from embedded bill object
          billId: tripData.bill.billId,
          billingDate: tripData.bill.billingDate,
          cost: tripData.bill.cost,
          tax: tripData.bill.tax,
          total: tripData.bill.total,
          status: tripData.bill.status,
          paymentMethodLastFour: tripData.bill.paymentMethodLastFour,
          riderId: tripData.bill.riderId,
          tripId: tripData.bill.tripId,
          
          // Trip data
          bikeId: tripData.bikeId,
          bikeType: tripData.bikeType,
          startStationName: tripData.startStationName,
          endStationName: tripData.endStationName,
          startStationId: tripData.startStationId,
          endStationId: tripData.endStationId,
          startTime: tripData.startTime,
          endTime: tripData.endTime,
          durationMinutes: tripData.durationMinutes,
        });
      }
    }
    
    bills.value = extractedBills;
  } catch (error) {
    console.error("Error loading billing history:", error);
    alert("Failed to load billing history. Please try again.");
  } finally {
    loading.value = false;
  }
}

function formatDateTime(timestamp) {
  if (!timestamp) return "—";
  const date = timestamp.toDate ? timestamp.toDate() : new Date(timestamp);
  return date.toLocaleString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function clearFilters() {
  filters.value = { 
    dateFrom: "", 
    dateTo: "",
    billId: "",
    station: ""
  };
  sortBy.value = "date";
}

// Computed property for filtered bills
const filteredBills = computed(() => {
  return bills.value.filter((bill) => {
    // Date filter
    if (filters.value.dateFrom || filters.value.dateTo) {
      const billDate = bill.billingDate?.toDate ? bill.billingDate.toDate() : new Date(bill.billingDate);
      
      if (filters.value.dateFrom) {
        const fromDate = new Date(filters.value.dateFrom);
        fromDate.setHours(0, 0, 0, 0);
        if (billDate < fromDate) return false;
      }
      
      if (filters.value.dateTo) {
        const toDate = new Date(filters.value.dateTo);
        toDate.setHours(23, 59, 59, 999);
        if (billDate > toDate) return false;
      }
    }
    
    // Bill ID filter
    if (filters.value.billId) {
      const searchTerm = filters.value.billId.toLowerCase();
      if (!bill.billId?.toLowerCase().includes(searchTerm)) {
        return false;
      }
    }
    
    // Station filter (checks both origin and arrival)
    if (filters.value.station) {
      const searchTerm = filters.value.station.toLowerCase();
      const originMatch = bill.startStationName?.toLowerCase().includes(searchTerm);
      const arrivalMatch = bill.endStationName?.toLowerCase().includes(searchTerm);
      if (!originMatch && !arrivalMatch) {
        return false;
      }
    }
    
    return true;
  });
});

// Computed property for sorted and filtered bills
const sortedAndFilteredBills = computed(() => {
  const filtered = [...filteredBills.value];
  
  switch (sortBy.value) {
    case "date":
      // Newest first (descending)
      return filtered.sort((a, b) => {
        const dateA = a.billingDate?.toDate ? a.billingDate.toDate() : new Date(a.billingDate);
        const dateB = b.billingDate?.toDate ? b.billingDate.toDate() : new Date(b.billingDate);
        return dateB - dateA;
      });
    
    case "dateOldest":
      // Oldest first (ascending)
      return filtered.sort((a, b) => {
        const dateA = a.billingDate?.toDate ? a.billingDate.toDate() : new Date(a.billingDate);
        const dateB = b.billingDate?.toDate ? b.billingDate.toDate() : new Date(b.billingDate);
        return dateA - dateB;
      });
    
    case "costHigh":
      // Highest cost first
      return filtered.sort((a, b) => (b.total || 0) - (a.total || 0));
    
    case "costLow":
      // Lowest cost first
      return filtered.sort((a, b) => (a.total || 0) - (b.total || 0));
    
    default:
      return filtered;
  }
});
</script>

<style scoped>
.billing-history {
  max-width: 1400px;
  margin: 2rem auto;
  padding: 1.5rem;
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

.billing-history h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 0.5rem;
  letter-spacing: 0.5px;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  align-items: flex-end;
  margin-bottom: 1.5rem;
  padding: 1rem;
  background-color: #f7fafc;
  border-radius: 8px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.filter-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #4a5568;
}

.filter-group input,
.filter-group select {
  padding: 0.5rem;
  border: 1px solid #cbd5e0;
  border-radius: 6px;
  font-size: 0.9rem;
  min-width: 180px;
}

.filter-group input:focus,
.filter-group select:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.1);
}

.clear-btn {
  background-color: #e2e8f0;
  color: #2d3748;
  border: none;
  border-radius: 6px;
  padding: 0.5rem 1rem;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.2s;
  height: fit-content;
}

.clear-btn:hover {
  background-color: #cbd5e0;
}

.table-container {
  overflow-x: auto;
}

.billing-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 1rem;
}

.billing-table th,
.billing-table td {
  border: 1px solid #e2e8f0;
  padding: 0.75rem;
  text-align: left;
}

.billing-table th {
  background-color: #f7fafc;
  font-weight: 600;
  color: #2d3748;
  text-align: center;
}

.billing-table td {
  color: #4a5568;
}

.bill-id {
  font-family: monospace;
  font-size: 0.85rem;
  color: #3182ce;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.charges-cell {
  background-color: #f7fafc;
}

.charge-breakdown {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.9rem;
}

.charge-line {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
}

.charge-line span:first-child {
  color: #718096;
}

.charge-line span:last-child {
  font-weight: 600;
  color: #2d3748;
}

.total-line {
  border-top: 1px solid #cbd5e0;
  padding-top: 0.25rem;
  margin-top: 0.25rem;
}

.total-line span {
  font-weight: 700;
  color: #1a202c;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-paid {
  background-color: #c6f6d5;
  color: #22543d;
}

.status-unpaid {
  background-color: #fed7d7;
  color: #742a2a;
}

.status-pending {
  background-color: #feebc8;
  color: #7c2d12;
}

.no-results {
  margin-top: 2rem;
  text-align: center;
  color: #718096;
  padding: 2rem;
}

.loading {
  margin-top: 2rem;
  text-align: center;
  color: #4a5568;
  padding: 2rem;
}

/* Responsive design */
@media (max-width: 768px) {
  .filters {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-group input,
  .filter-group select {
    min-width: 100%;
  }

  .billing-table {
    font-size: 0.85rem;
  }

  .billing-table th,
  .billing-table td {
    padding: 0.5rem;
  }
}
</style>