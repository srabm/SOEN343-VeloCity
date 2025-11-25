<template>
  <topbar />
  <div class="bg-cover bg-center" style="background-image: url('/src/assets/montreal-architecture.jpg');">
    <div class="min-h-screen bg-black/20 overflow-auto pt-16 pb-4">
      <div class="billing-history">
        <h1>Billing History</h1>

        <!-- Filters -->
        <div class="filters">
          <div class="filter-group">
            <label>From Date:</label>
            <input v-model="filters.dateFrom" type="date" />
          </div>
          
          <div class="filter-group">
            <label>To Date:</label>
            <input v-model="filters.dateTo" type="date" />
          </div>

          <div class="filter-group">
            <label>Bill ID:</label>
            <input 
              v-model="filters.billId" 
              type="text" 
              placeholder="Enter bill ID"
            />
          </div>

          <div class="filter-group">
            <label>Station:</label>
            <input 
              v-model="filters.station" 
              type="text" 
              placeholder="Origin or arrival station"
            />
          </div>

          <div class="filter-group">
            <label>Sort By:</label>
            <select v-model="sortBy">
              <option value="date">Date (Newest First)</option>
              <option value="dateOldest">Date (Oldest First)</option>
              <option value="costHigh">Cost (High to Low)</option>
              <option value="costLow">Cost (Low to High)</option>
            </select>
          </div>

          <button @click="clearFilters" class="clear-btn">Clear Filters</button>
        </div>

        <!-- Billing Table -->
        <div class="table-container">
          <table class="billing-table">
            <thead>
              <tr>
                <th>Bill ID</th>
                <th>Date & Time</th>
                <th>Bike ID</th>
                <th>Origin Station</th>
                <th>Arrival Station</th>
                <th>Charges</th>
                <th>Status</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="bill in sortedAndFilteredBills" :key="bill.billId">
                <td class="bill-id">{{ bill.billId }}</td>
                <td>{{ formatDateTime(bill.billingDate) }}</td>
                <td>{{ bill.bikeId || "—" }}</td>
                <td>{{ bill.startStationName || "—" }}</td>
                <td>{{ bill.endStationName || "—" }}</td>
                <td class="charges-cell">
                  <div class="charge-breakdown">
                    <div class="charge-line">
                      <span>Cost:</span>
                      <span>${{ bill.cost?.toFixed(2) || "0.00" }}</span>
                    </div>
                    <div class="charge-line">
                      <span>Tax:</span>
                      <span>${{ bill.tax?.toFixed(2) || "0.00" }}</span>
                    </div>
                    <div class="charge-line total-line">
                      <span>Total:</span>
                      <span>${{ bill.total?.toFixed(2) || "0.00" }}</span>
                    </div>
                  </div>
                </td>
                <td>
                  <span :class="'status-badge status-' + bill.status">
                    {{ bill.status?.toUpperCase() || "UNKNOWN" }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Empty State -->
        <div v-if="sortedAndFilteredBills.length === 0" class="no-results">
          <p>No billing records found.</p>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="loading">
          <p>Loading billing history...</p>
        </div>
      </div>
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