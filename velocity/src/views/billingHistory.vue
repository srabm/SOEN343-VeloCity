<template>
  <topbar />
  <div class="billing-history">
    <h1>Billing History</h1>

    <!-- Filters -->
    <div class="filters">
      <label>
        From:
        <input v-model="filters.dateFrom" type="date" />
      </label>
      <label>
        To:
        <input v-model="filters.dateTo" type="date" />
      </label>
      <button @click="clearFilters" class="clear-btn">Clear</button>
    </div>

    <!-- Billing Table -->
    <table class="billing-table">
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


    <!-- Empty State -->
    <div v-if="filteredRides.length === 0" class="no-results">
      <p>No billing records found.</p>
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
  orderBy,
  getDocs,
  updateDoc,
  doc,
} from "firebase/firestore";
import app from "../../firebase-config.js";

const auth = getAuth(app);
const db = getFirestore(app);

const user = ref(null);
const rides = ref([]);
const filters = ref({ dateFrom: "", dateTo: "" });

onMounted(() => {
  onAuthStateChanged(auth, async (firebaseUser) => {
    if (firebaseUser) {
      user.value = firebaseUser;
      await loadRides();
    }
  });
});

async function loadRides() {
  const ridesRef = collection(db, "riders", user.value.uid, "rides");
  const q = query(ridesRef, orderBy("startTime", "desc"));
  const snap = await getDocs(q);
  rides.value = snap.docs.map((d) => ({ rideId: d.id, ...d.data() }));
}

function formatDateTime(ts) {
  if (!ts) return "—";
  const date = ts.toDate ? ts.toDate() : new Date(ts);
  return date.toLocaleString();
}

function clearFilters() {
  filters.value = { dateFrom: "", dateTo: "" };
}

// Modal state
const showPaymentModal = ref(false);
const selectedRide = ref(null);
const mockPayment = ref({
  cardNumber: "",
  expiryDate: "",
  cvc: ""
});

// Opens the modal for a specific ride
function payForRide(ride) {
  selectedRide.value = ride;
  showPaymentModal.value = true;
}

// Closes modal
function closeModal() {
  showPaymentModal.value = false;
  selectedRide.value = null;
  mockPayment.value = { cardNumber: "", expiryDate: "", cvc: "" };
}

// Simulate external payment process
async function confirmPayment() {
  try {
    alert("Processing payment...");

    // Simulate delay like a real API call
    await new Promise((resolve) => setTimeout(resolve, 2000));

    // Update Firestore
    const rideRef = doc(db, "riders", user.value.uid, "rides", selectedRide.value.rideId);
    await updateDoc(rideRef, {
      "bill.paymentStatus": "PAID",
      "bill.transactionId": "TXN-" + Date.now(),
    });

    // Update local state
    selectedRide.value.bill.paymentStatus = "PAID";

    alert("Payment successful!");
    closeModal();
  } catch (err) {
    console.error("Payment failed:", err);
    alert("Payment failed. Try again.");
  }
}

const filteredRides = computed(() => {
  return rides.value.filter((ride) => {
    const rideDate = ride.startTime ? new Date(ride.startTime) : null;
    if (!rideDate) return true;

    if (filters.value.dateFrom) {
      const from = new Date(filters.value.dateFrom);
      if (rideDate < from) return false;
    }

    if (filters.value.dateTo) {
      const to = new Date(filters.value.dateTo);
      to.setHours(23, 59, 59, 999);
      if (rideDate > to) return false;
    }

    return true;
  });
});
</script>

<style scoped>
.billing-history {
  max-width: 1000px;
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
  gap: 1rem;
  align-items: center;
  margin-bottom: 1rem;
}

.billing-table {
  width: 100%;
  border-collapse: collapse;
}

.billing-table th,
.billing-table td {
  border: 1px solid #e2e8f0;
  padding: 0.5rem;
  text-align: center;
}

.billing-table th {
  background-color: #f7fafc;
  font-weight: 600;
}

.paid {
  color: #2f855a;
  font-weight: 600;
}

.unpaid {
  color: #c53030;
  font-weight: 600;
}

.pay-btn {
  background-color: #3182ce;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 0.4rem 0.8rem;
  cursor: pointer;
}

.pay-btn:hover {
  background-color: #2b6cb0;
}

.no-results {
  margin-top: 1rem;
  text-align: center;
  color: #718096;
}

/* Modal overlay */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

/* Modal box */
.modal {
  background: white;
  padding: 2rem;
  border-radius: 10px;
  width: 400px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.modal h2 {
  margin-bottom: 1rem;
  font-weight: 700;
  color: #1a202c;
}

.modal input {
  display: block;
  width: 100%;
  margin-bottom: 0.8rem;
  padding: 0.5rem;
  border-radius: 6px;
  border: 1px solid #ccc;
}

.modal-actions {
  display: flex;
  justify-content: space-between;
}

.cancel-btn {
  background-color: #e2e8f0;
  color: #333;
  border: none;
  border-radius: 6px;
  padding: 0.5rem 1rem;
}

.cancel-btn:hover {
  background-color: #cbd5e0;
}

</style>
