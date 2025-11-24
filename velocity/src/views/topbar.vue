<template>
    <header class="fixed top-0 left-0 right-0 z-50 bg-white text-black shadow">
        <nav class="grid grid-cols-3 items-stretch h-14 px-4">
            <!-- logo -->
            <div class="flex items-center">
            <router-link to="/VeloCity/home">
                <img :src="logo" alt="VeloCity" class="h-14 w-auto select-none cursor-pointer" />
            </router-link>
            </div>

            <!-- tabs -->
            <div class="justify-self-center h-full flex truncate">
                <router-link to="/VeloCity/home" class="h-full inline-flex items-center px-4 hover:bg-slate-200 duration-400">
                    Home
                </router-link>
                
                <!-- User View Navigation -->
                <template v-if="!isOperatorView">
                    <router-link to="/VeloCity/pricing" class="h-full inline-flex items-center px-4 hover:bg-slate-200 duration-400">
                        Pricing
                    </router-link>
                    <router-link v-if="isLoggedIn" to="/VeloCity/rides" class="h-full inline-flex items-center px-4 hover:bg-slate-200 duration-400">
                        Ride History
                    </router-link>
                    <router-link v-if="isLoggedIn" to="/VeloCity/billing" class="h-full inline-flex items-center px-4 hover:bg-slate-200 duration-400">
                        Billing History
                    </router-link>
                    <router-link to="/VeloCity/about" class="h-full inline-flex items-center px-4 hover:bg-slate-200 duration-400">
                        About Us
                    </router-link>
                </template>
                
                <!-- Operator View Navigation -->
                <template v-else>
                    <router-link to="/VeloCity/move-bike" class="h-full inline-flex items-center px-4 hover:bg-slate-200 duration-400">
                        Move Bike
                    </router-link>
                    <router-link to="/VeloCity/all-rides" class="h-full inline-flex items-center px-4 hover:bg-slate-200 duration-400">
                        All Rides
                    </router-link>
                    <router-link to="/VeloCity/all-billing" class="h-full inline-flex items-center px-4 hover:bg-slate-200 duration-400">
                        All Billing
                    </router-link>
                    <router-link to="/VeloCity/maintenance" class="h-full inline-flex items-center px-4 hover:bg-slate-200 duration-400">
                        Maintenance
                    </router-link>
                </template>
            </div>

            <!-- Login/Logout -->
            <div class="justify-self-end h-full flex items-center">
                <template v-if="isLoggedIn">
                    <div class="mr-3 flex items-center text-sm text-slate-700 bg-gray-200 px-2 py-1 rounded">
                        <span class="mr-2">Flex:</span>
                        <div class="text-yellow-500 font-medium">
                            {{ flexDollars }}
                        </div>
                    </div>
                    <Logout />
                </template>
                <template v-else>
                    <router-link
                        to="/velocity/login"
                        class="bg-yellow-300 text-black font-medium px-3 py-1 rounded hover:bg-yellow-400 duration-300"
                    >
                        Login
                    </router-link>
                </template>
            </div>
        </nav>
    </header>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAuth, onAuthStateChanged } from 'firebase/auth'
import { getFirestore, doc, getDoc } from 'firebase/firestore'
import app from '../../firebase-config.js'
import Logout from './logout.vue'
import logo from '../assets/banner.png'

const isLoggedIn = ref(false)
const isOperatorView = ref(false)
const flexDollars = ref(null)

const auth = getAuth(app)
const db = getFirestore(app)

onMounted(() => {
    onAuthStateChanged(auth, async (user) => {
        isLoggedIn.value = !!user

        if (user) {
            try {
                const docRef = doc(db, "riders", user.uid)
                const docSnap = await getDoc(docRef)
                
                if (docSnap.exists()) {
                    const userData = docSnap.data()
                    // Only show operator view if user is an operator AND has operator view enabled
                    isOperatorView.value = userData.isOperator && userData.isOperatorView
                    flexDollars.value = userData?.flexDollars ?? 0
                } else {
                    flexDollars.value = 0
                }
            } catch (err) {
                console.error("Error loading user profile in topbar:", err)
                flexDollars.value = 0
            }
        } else {
            isOperatorView.value = false
            flexDollars.value = null
        }
    })
})
</script>


<style scoped>
header,
header a {
  color: #0348af !important;
  font-weight: 1000 !important;
}

header a {
    font-size: 0.88rem !important; /* = text-sm */
}


</style>