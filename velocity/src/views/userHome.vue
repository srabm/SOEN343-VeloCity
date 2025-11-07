<template>
    <topbar />
    <div class="bg-cover bg-center" style="background-image: url('/src/assets/bike-bg.jpg');">
        <div class="min-h-screen bg-black/40">
            <header class="text-center py-6 text-white drop-shadow">
                <h1 class="text-3xl font-semibold">
                    {{ greeting }}<template v-if="userName">, {{ userName }}!</template><template v-else>!</template>
                </h1>
                <p class="mt-2 px-4 max-w-3xl mx-auto text-slate-100/90">
                    Choose a station to start your ride.
                </p>
            </header>

            <section class="px-50 pb-4">
                <div class="rounded-xl p-3 md:p-4 shadow-lg bg-white/50 backdrop-blur-md">
                    <div class="w-full h-[60vh] md:h-[70vh]">
                        <mapView class="w-full h-full rounded-lg overflow-hidden" />
                    </div>
                </div>
            </section>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAuth, onAuthStateChanged } from 'firebase/auth'
import { firestore } from '../../firebaseAuth.js'
import { doc, getDoc } from 'firebase/firestore'
import topbar from './topbar.vue'
import mapView from './mapView.vue'

const userName = ref(null)
const greeting = ref('Welcome')

function computeGreeting() {
    const hour = new Date().getHours()
    if (hour < 12) return 'Good morning'
    if (hour < 18) return 'Good afternoon'
    return 'Good evening'
}

onMounted(() => {
    const auth = getAuth()
    onAuthStateChanged(auth, async (user) => {
        if (user) {
            try {
                const riderRef = doc(firestore, 'riders', user.uid)
                const snap = await getDoc(riderRef)
                if (snap.exists()) {
                    const data = snap.data()
                    userName.value = (data?.firstName || 'Rider').toString().trim()
                } else {
                    userName.value = (user.displayName || 'Rider').toString().trim()
                }
            } catch (e) {
                console.error('Failed to load rider profile', e)
                userName.value = 'Rider'
            }
        } else {
            userName.value = null
        }
        greeting.value = computeGreeting()
    })
})
</script>

<style scoped></style>