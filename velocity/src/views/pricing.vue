<template>
    <topbar />
    <div class="bg-cover bg-center" style="background-image: url('/src/assets/montreal-architecture.jpg');">
        <div class="min-h-screen bg-black/20">
            <!-- HEADER -->
            <header class="text-center py-8 text-white drop-shadow">
                <h1 class="text-4xl font-extrabold tracking-tight">
                    Pricing
                </h1>
                <p class="text-slate-100/90 text-lg mt-2">
                    Memberships, pay-as-you-go, and tier rewards.
                </p>
            </header>

            <!-- PRICING SECTION -->
            <section class="px-4 pb-10 max-w-6xl mx-auto">

                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">

                <!-- PAY-AS-YOU-GO -->
                <div class="mt-6 rounded-xl p-5 shadow-lg bg-white/50 backdrop-blur-md">
                    <h2 class="text-xl font-semibold mb-3">Pay-as-you-go Pricing</h2>

                    <ul class="text-gray-800 space-y-1">
                        <li>Base (per trip): <strong>$1.11</strong></li>
                        <li>Regular velo: <strong>$0.22</strong> / min</li>
                        <li>Electric velo: <strong>$0.33</strong> / min</li>
                    </ul>

                    <p class="text-slate-700 mt-3 text-sm">
                        Default pricing for guests or users without a membership. Charges are calculated when your bike is returned.
                    </p>
                </div>

                <!-- Regular Membership -->
                <div class="rounded-xl p-5 shadow-lg bg-white/50 backdrop-blur-md">
                    <h2 class="text-xl font-semibold mb-3">Regular Membership</h2>
                    <p class="text-2xl font-bold mb-2">
                        $22.22 <span class="text-sm font-medium text-slate-600">/ month</span>
                    </p>
                    <p class="text-slate-600">Unlimited access to regular velos.</p>
                    <button
                        class="mt-4 w-full bg-yellow-300 text-black px-4 py-2 rounded hover:bg-yellow-500 duration-300">
                        Coming soon...
                    </button>
                </div>

                <!-- Electric Membership -->
                <div class="rounded-xl p-5 shadow-lg bg-white/50 backdrop-blur-md">
                    <h2 class="text-xl font-semibold mb-3">Electric Membership</h2>
                    <p class="text-2xl font-bold mb-2">
                        $33.33 <span class="text-sm font-medium text-slate-600">/ month</span>
                    </p>
                    <p class="text-slate-600">Unlimited access to both regular and electric velos.</p>
                    <button
                        class="mt-4 w-full bg-yellow-300 text-black px-4 py-2 rounded hover:bg-yellow-500 duration-300">
                        Coming soon...
                    </button>
                </div>

                <!-- Flex dollars -->
                <div class="md:col-span-2 rounded-xl p-5 shadow-lg bg-white/50 backdrop-blur-md">
                    <h2 class="text-xl font-semibold mb-3">Flex Dollars</h2>
                    
                    <p class="mt-3 px-6">
                        As an incentive for our users to return their bike at a low capacity station, 2 Flex dollars will be rewarded.
                        Each Flex dollar will be automatically redeemed for a $0.50 discount on your next ride.
                    </p>
                </div>

                </div>

                <!-- SEPARATOR -->
                <section class="max-w-4xl mx-auto text-center py-6 relative mt-10">
                <div class="absolute inset-0 bg-black/20 backdrop-blur-[1px] rounded-xl"></div>

                <div class="relative">
                    <h2 class="text-3xl font-extrabold text-white drop-shadow">
                    Rider Tiers & Eligibility
                    </h2>
                    <p class="text-slate-200 mt-1 text-base font-semibold drop-shadow-sm">
                    Click on each card to find out how to be upgraded!
                    </p>
                </div>
                </section>

                <!-- TIER CARDS -->
                <section class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8 max-w-6xl mx-auto px-4 py-12">

                <!-- NO TIER CARD -->
                <div class="flip-card" @click="flipCard('notier')">
                <div class="flip-inner" :class="{ flipped: flipped.notier }">

                    <!-- FRONT -->
                    <div class="flip-front bg-[#0348af]">
                    <h3 class="text-xl font-extrabold text-white">No Tier</h3>
                    <p class="text-sm text-white mt-3">Every journey starts here!</p>
                    </div>

                    <!-- BACK -->
                    <div class="flip-back bg-[#0348af]">
                    <h4 class="font-semibold text-white mb-1">Eligibility</h4>
                    <ul class="text-sm text-white space-y-1 text-left">
                        <li>• No requirements, just start riding!</li>
                    </ul>

                    <h4 class="font-semibold text-white mt-4 mb-1">Perks</h4>
                    <ul class="text-sm text-white space-y-1 text-left">
                        <li>• No discounts (yet!)</li>
                        <li>• No bonus reservation time</li>
                    </ul>
                    </div>

                </div>
                </div>

                <!-- BRONZE CARD -->
                <div class="flip-card" @click="flipCard('bronze')">
                <div class="flip-inner" :class="{ flipped: flipped.bronze }">

                    <!-- FRONT -->
                    <div class="flip-front bg-[#fcd9b6]">
                    <h3 class="text-xl font-extrabold text-[#a1550d]">Bronze</h3>
                    <p class="text-sm text-[#5b3a1d] mt-3">Smooth rider! You're building momentum.</p>
                    </div>

                    <!-- BACK -->
                    <div class="flip-back bg-[#fcd9b6] text-[#5b3a1d]">
                    <h4 class="font-semibold mb-1">Eligibility</h4>
                    <ul class="text-sm space-y-1 text-left">
                        <li>• You kept it clean: No missed reservations in the last year.</li>
                        <li>• Every velo you've taken? Returned like a champ.</li>
                        <li>• Over 10 trips in the last year</li>
                    </ul>

                    <h4 class="font-semibold mt-4 mb-1">Perks</h4>
                    <ul class="text-sm space-y-1 text-left">
                        <li>• Enjoy a sweet <b>5% discount</b> on all trips!</li>
                    </ul>
                    </div>

                </div>
                </div>

                <!-- SILVER CARD -->
                <div class="flip-card" @click="flipCard('silver')">
                <div class="flip-inner" :class="{ flipped: flipped.silver }">

                    <!-- FRONT -->
                    <div class="flip-front bg-[#e5e7eb]">
                    <h3 class="text-xl font-extrabold text-gray-700">Silver</h3>
                    <p class="text-sm text-gray-600 mt-3">Silver shines! You're a true VeloCity regular.</p>
                    </div>

                    <!-- BACK -->
                    <div class="flip-back bg-[#e5e7eb] text-gray-700">
                    <h4 class="font-semibold mb-1">Eligibility</h4>
                    <ul class="text-sm space-y-1 text-left">
                        <li>• All Bronze requirements</li>
                        <li>• Over 5 successful reservations this year</li>
                        <li>• Over 5 trips/month for the last 3 months</li>
                    </ul>

                    <h4 class="font-semibold mt-4 mb-1">Perks</h4>
                    <ul class="text-sm space-y-1 text-left">
                        <li>• <b>10% discount </b>on all trips</li>
                        <li>• Extra <b>2 minute</b> hold time</li>
                    </ul>
                    </div>

                </div>
                </div>

                <!-- GOLD CARD -->
                <div class="flip-card" @click="flipCard('gold')">
                <div class="flip-inner" :class="{ flipped: flipped.gold }">

                    <!-- FRONT -->
                    <div class="flip-front bg-[#fef3c7]">
                    <h3 class="text-xl font-extrabold text-yellow-700">Gold</h3>
                    <p class="text-sm text-yellow-900 mt-3">Elite rider status achieved. You're unstoppable!</p>
                    </div>

                    <!-- BACK -->
                    <div class="flip-back bg-[#fef3c7] text-yellow-900">
                    <h4 class="font-semibold mb-1">Eligibility</h4>
                    <ul class="text-sm space-y-1 text-left">
                        <li>• All Silver requirements met, obviously.</li>
                        <li>• Over 5 trips every week for 3 months</li>
                    </ul>

                    <h4 class="font-semibold mt-4 mb-1">Perks</h4>
                    <ul class="text-sm space-y-1 text-left">
                        <li>• <b>15% discount</b> on all trips</li>
                        <li>• Extra <b>5 minute</b> hold time</li>
                    </ul>
                    </div>
                    </div>
                    </div>
                </section>
            </section>
        </div>
    </div>
</template>

<script setup>
import topbar from './topbar.vue'
import { ref } from 'vue'

const flipped = ref({
  notier: false,
  bronze: false,
  silver: false,
  gold: false
})

function flipCard(card) {
  flipped.value[card] = !flipped.value[card]
}
</script>

<style scoped>
.flip-card {
  perspective: 1200px;
}

.flip-inner {
  width: 100%;
  height: 260px;
  position: relative;
  transform-style: preserve-3d;
  transition: transform 0.9s cubic-bezier(.22, .61, .36, 1);
}

.flip-inner.flipped {
  transform: rotateY(180deg);
}

.flip-front,
.flip-back {
  position: absolute;
  inset: 0;
  padding: 1.25rem;
  border-radius: 1rem;
  box-shadow: 0 15px 35px -10px rgba(0,0,0,0.25);
  backface-visibility: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.flip-back {
  transform: rotateY(180deg);
}



</style>
