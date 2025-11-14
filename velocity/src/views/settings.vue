<template>
  <topbar />
  <div class="bg-cover bg-center" style="background-image: url('/src/assets/bike-bg.jpg');">
    <div class="min-h-screen bg-black/40 flex flex-col">
      <header class="text-center py-8 text-white drop-shadow">
        <h1 class="text-3xl font-semibold">User Settings</h1>
      </header>

      <section class="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-5xl mx-auto w-full px-4 text-black items-start">
        <!-- User Info Card -->
        <div class="self-start rounded-xl p-6 shadow-lg bg-white/50 backdrop-blur-md">
          <h2 class="text-xl font-semibold mb-4">User Info</h2>
          <ul class="space-y-2 text-sm">
            <li><strong>Full Name:</strong> {{ profile?.firstName ? (profile.firstName + ' ' + profile.lastName) : 'Not set' }}</li>
            <li><strong>Email:</strong> {{ user?.email || 'Not set' }}</li>
            <li><strong>Address:</strong> {{ profile?.address || 'Not set' }}</li>
            <li><strong>Phone Number:</strong> {{ profile?.phoneNumber || 'Not set' }}</li>
            <li><strong>Role:</strong> {{ profile?.isOperator ? 'Operator' : 'Rider' }}</li>
          </ul>

          <!-- Operator View Toggle (only visible for operators) -->
          <div v-if="profile?.isOperator" class="mt-4 p-4 bg-blue-50 rounded-lg border border-blue-200">
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-sm font-semibold text-blue-900">Operator View</h3>
                <p class="text-xs text-blue-700 mt-1">Toggle between operator and user interface</p>
              </div>
              <button 
                @click="toggleOperatorView"
                :class="[
                  'relative inline-flex h-8 w-14 items-center rounded-full transition-colors duration-300',
                  profile?.isOperatorView ? 'bg-blue-600' : 'bg-gray-300'
                ]"
              >
                <span 
                  :class="[
                    'inline-block h-6 w-6 transform rounded-full bg-white transition-transform duration-300',
                    profile?.isOperatorView ? 'translate-x-7' : 'translate-x-1'
                  ]"
                ></span>
              </button>
            </div>
            <p class="text-xs text-blue-600 mt-2">
              Current mode: <strong>{{ profile?.isOperatorView ? 'Operator' : 'User' }}</strong>
            </p>
          </div>

          <div class="mt-4">
            <button @click="startProfileEdit"
              class="bg-yellow-300 text-black px-4 py-2 rounded hover:bg-yellow-400 duration-300">Edit Profile</button>
          </div>
          <div v-if="editProfileMode" class="mt-6 border-t border-white/20 pt-4">
            <h3 class="text-lg font-medium mb-3">Edit Profile</h3>
            <label class="block text-sm mb-1">Address:</label>
            <input v-model="profileEditForm.address" placeholder="Enter your address"
              class="w-full mb-3 px-3 py-2 rounded bg-white/80 text-black focus:outline-none" />
            <label class="block text-sm mb-1">Phone Number:</label>
            <input v-model="profileEditForm.phoneNumber" placeholder="514-123-4567"
              class="w-full mb-4 px-3 py-2 rounded bg-white/80 text-black focus:outline-none" />
            <div class="flex gap-3">
              <button @click="saveProfileInfo"
                class="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600 duration-200">Save</button>
              <button @click="cancelProfileEdit"
                class="bg-slate-300 text-black px-4 py-2 rounded hover:bg-slate-200 duration-200">Cancel</button>
            </div>
          </div>
        </div>

        <!-- Payment Info Card -->
        <div class="self-start rounded-xl p-6 shadow-lg bg-white/50 backdrop-blur-md">
          <h2 class="text-xl font-semibold mb-4">Payment Information</h2>
          <div v-if="!editPaymentMode" class="space-y-2 text-sm">
            <p><strong>Cardholder Name:</strong> {{ profile?.paymentInfo?.cardholderName || 'Not set' }}</p>
            <p><strong>Card Number:</strong> {{ profile?.paymentInfo?.cardNumber || 'Not set' }}</p>
            <p><strong>Expiry Date:</strong> {{ profile?.paymentInfo?.expiryDate || 'Not set' }}</p>
            <p><strong>CVC:</strong> {{ profile?.paymentInfo?.cvc || 'Not set' }}</p>
            <button @click="togglePaymentEdit(true)"
              class="mt-3 bg-yellow-300 text-black px-4 py-2 rounded hover:bg-yellow-400 duration-300">{{ profile?.paymentInfo ? 'Edit Payment Info' : 'Add Payment Info' }}</button>
          </div>
          <!-- Edit payment info -->
          <div v-else>
            <div class="space-y-8 w-full">
              <!-- Front of card -->
              <div
                class="w-full rounded-xl shadow-lg bg-gradient-to-br from-indigo-600 via-purple-600 to-fuchsia-600 text-white overflow-hidden"
                style="aspect-ratio: 85.6 / 54;">
                <div class="relative h-full p-5 flex flex-col">
                  <div class="mt-2 mb-auto">
                    <label class="text-[10px] uppercase tracking-wide opacity-80">Card Number</label>
                    <input :value="formattedCardNumber" @input="handleCardNumberInput" maxlength="19"
                      placeholder="1234 5678 9012 3456"
                      class="w-full bg-white/95 text-black px-3 py-2 rounded mt-1 font-mono text-sm focus:outline-none placeholder:text-black/50" />
                  </div>
                  <div class="flex items-end justify-between">
                    <div class="w-[65%]">
                      <label class="text-[10px] uppercase tracking-wide opacity-80">Cardholder</label>
                      <input v-model="paymentForm.cardholderName" placeholder="Full Name"
                        class="w-full bg-white/95 text-black px-3 py-2 rounded mt-1 text-sm focus:outline-none placeholder:text-black/50" />
                    </div>
                    <div class="w-[28%] text-right">
                      <label class="text-[10px] uppercase tracking-wide opacity-80">Exp</label>
                      <input :value="paymentForm.expiryDate" @input="handleExpiryInput" placeholder="MM/YY"
                        maxlength="5"
                        class="w-full bg-white/95 text-black px-3 py-2 rounded mt-1 text-sm focus:outline-none placeholder:text-black/50 text-center" />
                    </div>
                  </div>
                </div>
              </div>

              <!-- Back of card -->
              <div
                class="relative w-full rounded-xl shadow-lg bg-gradient-to-br from-slate-800 to-slate-900 text-white overflow-hidden"
                style="aspect-ratio: 85.6 / 54;">
                <div class="absolute left-0 right-0 top-8 h-8 bg-black/60"></div>
                <div class="absolute top-[38%] right-6 w-32">
                  <label class="text-[10px] uppercase tracking-wide opacity-80">CVC</label>
                  <input :value="paymentForm.cvc" @input="handleCvcInput" placeholder="123" maxlength="4"
                    class="w-full bg-white/95 text-black px-2 py-2 rounded mt-1 text-sm focus:outline-none placeholder:text-black/50 text-center" />
                </div>
              </div>
            </div>

            <div class="flex gap-3 pt-2 justify-center mt-5">
              <button @click="savePaymentInfo"
                class="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600 duration-200">Save</button>
              <button @click="togglePaymentEdit(false)"
                class="bg-slate-300 text-black px-4 py-2 rounded hover:bg-slate-200 duration-200">Cancel</button>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { getAuth, onAuthStateChanged } from "firebase/auth";
import { getFirestore, doc, getDoc, updateDoc } from "firebase/firestore";
import app from "../../firebase-config.js";
import topbar from './topbar.vue'

const router = useRouter();
const auth = getAuth(app);
const db = getFirestore(app);

const user = ref(null);
const profile = ref(null);

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
  expiryDate: "",
  cvc: "",
});

const formattedCardNumber = computed(() => {
  const raw = (paymentForm.value.cardNumber || "").replace(/[^0-9]/g, "").slice(0, 16);
  return raw.replace(/(.{4})/g, "$1 ").trim();
});

function handleCardNumberInput(e) {
  const digits = (e.target.value || "").replace(/[^0-9]/g, "").slice(0, 16);
  paymentForm.value.cardNumber = digits;
}

function handleExpiryInput(e) {
  let v = (e.target.value || "").replace(/[^0-9]/g, "").slice(0, 4);
  if (v.length >= 3) v = v.slice(0, 2) + "/" + v.slice(2);
  paymentForm.value.expiryDate = v;
}

function handleCvcInput(e) {
  paymentForm.value.cvc = (e.target.value || "").replace(/[^0-9]/g, "").slice(0, 4);
}

onMounted(() => {
  onAuthStateChanged(auth, async (firebaseUser) => {
    if (firebaseUser) {
      user.value = firebaseUser;

      try {
        const docRef = doc(db, "riders", firebaseUser.uid);
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
      router.push("/VeloCity/login");
    }
  });
});

// ----- Operator View Toggle -----
async function toggleOperatorView() {
  if (!user.value || !profile.value?.isOperator) return;

  try {
    const newOperatorViewState = !profile.value.isOperatorView;
    const docRef = doc(db, "riders", user.value.uid);
    
    await updateDoc(docRef, {
      isOperatorView: newOperatorViewState
    });

    profile.value.isOperatorView = newOperatorViewState;
    
    console.log(`Operator view ${newOperatorViewState ? 'enabled' : 'disabled'}`);
    
    // Reload the page to update the topbar navigation
    window.location.reload();
  } catch (err) {
    console.error("Error toggling operator view:", err);
  }
}

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

</script>

<style scoped></style>