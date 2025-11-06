<template>
  <div class="settings-page">
    <!-- Tabs header -->
    <div class="tabs-header">
      <button
        class="tab-button"
        :class="{ active: activeTab === 'settings' }"
        @click="activeTab = 'settings'"
      >
        User Settings
      </button>

      <button
        class="tab-button"
        :class="{ active: activeTab === 'billing' }"
        @click="activeTab = 'billing'"
      >
        Billing History
      </button>
    </div>

    <!-- TAB 1: User Settings -->
    <div v-if="activeTab === 'settings'">
      <h1>User Settings</h1>

      <div v-if="user">
        <!-- Basic profile info -->
        <p>
          <strong>Full Name:</strong>
          {{ profile?.firstName ? profile.firstName + " " + profile.lastName : "Not set" }}
        </p>

        <p>
          <strong>Email:</strong>
          {{ user.email }}
        </p>

        <p>
          <strong>Address:</strong>
          {{ profile?.address || "Not set" }}
        </p>

        <p>
          <strong>Phone Number:</strong>
          {{ profile?.phoneNumber || "Not set" }}
        </p>

        <p>
          <strong>Role:</strong>
          {{ profile?.isOperator ? "Operator" : "Rider" }}
        </p>

        <!-- Edit Profile + Log Out buttons -->
        <div class="profile-actions">
          <button @click="startProfileEdit" class="edit-profile-btn">
            Edit Profile
          </button>
          <button @click="logout" class="logout-btn">
            Log Out
          </button>
        </div>

        <!-- Edit Profile (only address and phone can be changed) -->
        <div v-if="editProfileMode" class="profile-edit-form">
          <h2 class="section-title">Edit Profile</h2>

          <label>Address:</label>
          <input v-model="profileEditForm.address" placeholder="Enter your address" />

          <label>Phone Number:</label>
          <input v-model="profileEditForm.phoneNumber" placeholder="514-123-4567" />

          <div class="btn-row">
            <button @click="saveProfileInfo" class="save-btn">Save</button>
            <button @click="cancelProfileEdit" class="cancel-btn">Cancel</button>
          </div>
        </div>

        <!-- Payment Information -->
        <h2 class="section-title">Payment Information</h2>

        <!-- View mode -->
        <div v-if="!editPaymentMode && profile?.paymentInfo">
          <p><strong>Cardholder Name:</strong> {{ profile.paymentInfo.cardholderName || 'Not set' }}</p>
          <p><strong>Card Number:</strong> {{ profile.paymentInfo.cardNumber || 'Not set' }}</p>
          <p><strong>Expiry Date:</strong> {{ profile.paymentInfo.expiryDate || 'Not set' }}</p>
          <p><strong>CVC:</strong> {{ profile.paymentInfo.cvc || 'Not set' }}</p>

          <button @click="togglePaymentEdit(true)" class="edit-btn">
            Edit Payment Info
          </button>
        </div>

        <!-- Edit payment info -->
        <div v-else-if="editPaymentMode" class="payment-edit-form">
          <label>Cardholder Name:</label>
          <input v-model="paymentForm.cardholderName" placeholder="Full name" />

          <label>Card Number:</label>
          <input v-model="paymentForm.cardNumber" maxlength="19" placeholder="1234 5678 9012 3456" />

          <label>Expiry Date:</label>
          <input v-model="paymentForm.expiryDate" placeholder="MM/YY" />

          <label>CVC:</label>
          <input v-model="paymentForm.cvc" maxlength="4" placeholder="123" />

          <div class="btn-row">
            <button @click="savePaymentInfo" class="save-btn">Save</button>
            <button @click="togglePaymentEdit(false)" class="cancel-btn">Cancel</button>
          </div>
        </div>

        <!-- No payment info yet -->
        <div v-else>
          <p>No payment method on file.</p>
          <button @click="togglePaymentEdit(true)" class="add-btn">
            Add Payment Info
          </button>
        </div>
      </div>

      <div v-else>
        <p>Loading user info...</p>
      </div>
    </div>

    <!-- TAB 2: Billing History -->
    <div v-else-if="activeTab === 'billing'">
      <BillingHistory />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getAuth, onAuthStateChanged, signOut } from "firebase/auth";
import { getFirestore, doc, getDoc, updateDoc } from "firebase/firestore";
import app from "../../firebase-config.js";
import BillingHistory from "../views/billingHistory.vue";

const router = useRouter();
const auth = getAuth(app);
const db = getFirestore(app);

const user = ref(null);
const profile = ref(null);

const activeTab = ref("settings"); // 'settings' | 'billing'

// Profile edit (address + phone)
const editProfileMode = ref(false);
const profileEditForm = ref({
  address: "",
  phoneNumber: "",
});

// Payment edit
const editPaymentMode = ref(false);
const paymentForm = ref({
  cardNumber: "",
  cardholderName: "",
  expiryName: "",
  cvc: "",
});

onMounted(() => {
  onAuthStateChanged(auth, async (firebaseUser) => {
    if (firebaseUser) {
      user.value = firebaseUser;

      try {
        const docRef = doc(db, "riders", firebaseUser.uid); // riders/<uid>
        const docSnap = await getDoc(docRef);

        if (docSnap.exists()) {
          profile.value = docSnap.data();
        } else {
          console.warn("No Firestore profile found for this user.");
        }
      } catch (err) {
        console.error("Error loading profile:", err);
      }
    } else {
      router.push("/velocity/login");
    }
  });
});

// ----- Profile (address + phone) editing -----
function startProfileEdit() {
  if (!profile.value) return;
  editProfileMode.value = true;
  profileEditForm.value = {
    address: profile.value.address || "",
    phoneNumber: profile.value.phoneNumber || "",
  };
}

function cancelProfileEdit() {
  editProfileMode.value = false;
}

async function saveProfileInfo() {
  if (!user.value) return;

  try {
    const docRef = doc(db, "riders", user.value.uid);
    await updateDoc(docRef, {
      address: profileEditForm.value.address,
      phoneNumber: profileEditForm.value.phoneNumber,
    });

    if (!profile.value) profile.value = {};
    profile.value.address = profileEditForm.value.address;
    profile.value.phoneNumber = profileEditForm.value.phoneNumber;

    editProfileMode.value = false;
    console.log("Profile info updated successfully");
  } catch (err) {
    console.error("Error updating profile info:", err);
  }
}

// ----- Payment editing -----
function togglePaymentEdit(state) {
  editPaymentMode.value = state;

  if (state) {
    const pInfo = profile.value?.paymentInfo || {};
    paymentForm.value = {
      cardNumber: pInfo.cardNumber || "",
      cardholderName: pInfo.cardholderName || "",
      expiryDate: pInfo.expiryDate || "",
      cvc: pInfo.cvc || ""
    };
  }
}

async function savePaymentInfo() {
  if (!user.value) return;

  try {
    const docRef = doc(db, "riders", user.value.uid);
    await updateDoc(docRef, {
      paymentInfo: paymentForm.value,
    });

    if (!profile.value) profile.value = {};
    profile.value.paymentInfo = { ...paymentForm.value };

    editPaymentMode.value = false;
    console.log("Payment info updated.");
  } catch (err) {
    console.error("Error updating payment info:", err);
  }
}

// ----- Logout -----
async function logout() {
  try {
    await signOut(auth);
    router.push("/velocity/login");
  } catch (err) {
    console.error("Error logging out:", err);
  }
}
</script>

<style scoped>
.settings-page {
  max-width: 700px;
  margin: 2rem auto;
  padding: 2rem;
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

/* Tabs */
.tabs-header {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid #ddd;
}

.tab-button {
  padding: 0.5rem 1rem;
  border: none;
  background: transparent;
  cursor: pointer;
  font-weight: 600;
  border-bottom: 3px solid transparent;
}

.tab-button.active {
  border-bottom-color: #3490dc;
  color: #3490dc;
}

/* Sections */
.section-title {
  margin-top: 1.5rem;
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #ccc;
  padding-bottom: 0.25rem;
}

/* Inputs & buttons */
input {
  display: block;
  width: 100%;
  padding: 0.5rem;
  margin-bottom: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.95rem;
}

button {
  cursor: pointer;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  border: none;
  font-size: 0.9rem;
}

/* Profile buttons */
.profile-actions {
  margin-top: 1rem;
  display: flex;
  gap: 0.75rem;
}

.edit-profile-btn {
  background-color: #4a5568; /* gray */
  color: white;
}

.edit-profile-btn:hover {
  background-color: #2d3748;
}

.logout-btn {
  background-color: #e3342f;
  color: white;
}

.logout-btn:hover {
  background-color: #cc1f1a;
}

/* Save / cancel / edit / add */
.btn-row {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.edit-btn,
.add-btn {
  background-color: #3490dc;
  color: white;
}

.save-btn {
  background-color: #38c172;
  color: white;
}

.cancel-btn {
  background-color: #e2e8f0;
  color: #333;
}

/* Edit sections */
.profile-edit-form,
.payment-edit-form {
  margin-top: 1rem;
}
</style>
