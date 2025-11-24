<template>
    <topbar />
    <TierWelcomeNotification
        v-if="tierWelcome"
        :visible="true"
        :tier="tierWelcome.tier"
        :title="tierWelcome.title"
        :message="tierWelcome.message"
        @close="tierWelcome = null"
    />
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

            <section class="px-50 pb-4 h-full">
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
import TierNotification from '../components/TierNotification.vue'
import TierWelcomeNotification from '../components/TierWelcomeNotification.vue'

const userName = ref(null)
const greeting = ref('Welcome')
const tierChange = ref({ oldTier: null, newTier: null })
const tierWelcome = ref(null)

const tierWelcomeMessages = {
    NoTier: {
        title: 'Zero tier? Zero worries! 🚀',
        message: `Everyone starts here.

Ride more to unlock juicy perks such as discounts and longer holds!`
    },
    Bronze: {
        title: 'Welcome back, Bronze Rider! 🥉🚲',
        message: `You’re officially climbing the ranks.

Perks unlocked: 5% off every trip.

Ride a bit more and Silver might start looking at you funny 👀.`
    },
    Silver: {
        title: 'Silver Rider unlocked! 🥈😎',
        message: `Your superpowers:

• 10% off every trip
• +2 extra minutes added to every reservation hold

Gold is just a few rides away… just saying.`
    },
    Gold: {
        title: 'All rise for the Gold Rider 👑✨',
        message: `Behold your elite perks:

• 15% off every trip
• +5 extra reservation minutes (because royalty shouldn’t rush)

Enjoy the throne, your highness.`
    }
}

function computeGreeting() {
    const hour = new Date().getHours()
    if (hour < 12) return 'Good morning'
    if (hour < 18) return 'Good afternoon'
    return 'Good evening'
}

function normalizeTier(tier) {
    if (!tier) return 'NoTier'
    const trimmed = tier.toString().trim().toLowerCase()
    if (trimmed === 'bronze') return 'Bronze'
    if (trimmed === 'silver') return 'Silver'
    if (trimmed === 'gold') return 'Gold'
    return 'NoTier'
}

function checkTierChange(currentTier, userId) {
    if (!currentTier) return
    
    // Get previous tier from localStorage
    const storageKey = `rider_tier_${userId}`
    const previousTier = localStorage.getItem(storageKey)
    
    // If there's a previous tier and it's different from current, show notification
    if (previousTier && previousTier !== currentTier) {
        tierChange.value = {
            oldTier: previousTier,
            newTier: currentTier
        }
    }
    
    // Update stored tier
    localStorage.setItem(storageKey, currentTier)
}

function showTierWelcome(currentTier) {
    const normalized = normalizeTier(currentTier)
    const content = tierWelcomeMessages[normalized]
    if (content) {
        tierWelcome.value = {
            tier: normalized,
            ...content
        }
    } else {
        tierWelcome.value = null
    }
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

                    // Check for tier changes
                    const currentTier = data?.tier || 'NoTier'
                    checkTierChange(currentTier, user.uid)
                    showTierWelcome(currentTier)
                } else {
                    userName.value = (user.displayName || 'Rider').toString().trim()
                    tierWelcome.value = null
                }
            } catch (e) {
                console.error('Failed to load rider profile', e)
                userName.value = 'Rider'
                tierWelcome.value = null
            }
        } else {
            userName.value = null
            tierWelcome.value = null
        }
        greeting.value = computeGreeting()
    })
})
</script>

<style scoped></style>