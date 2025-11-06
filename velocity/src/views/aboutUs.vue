<template>
    <topbar />
    <div class="bg-cover bg-center" style="background-image: url('/src/assets/bike-bg.jpg');">
        <div class="min-h-screen bg-black/30">
            <header class="text-center py-8 text-white drop-shadow">
                <h1 class="text-3xl font-semibold">About VeloCity</h1>
                <p class="mt-2 px-4 max-w-3xl mx-auto text-slate-100/90">
                    A bike-sharing system that promotes low-cost mobility, exercise, and low-emission transportation.
                </p>
            </header>

            <section class="grid grid-cols-1 md:grid-cols-2 gap-4 max-w-4xl mx-auto text-center px-4">
                <div class="rounded-xl p-5 shadow-lg bg-white/50 backdrop-blur-md">
                    <h2 class="text-xl font-semibold mb-3">Our Mission</h2>
                    <p class="text-slate-600">
                        TBD.
                    </p>
                </div>

                <div class="rounded-xl p-5 shadow-lg bg-white/50 backdrop-blur-md">
                    <h2 class="text-xl font-semibold mb-3">How It Works</h2>
                    <ul class="list-none space-y-1 text-slate-700">
                        <li><strong>1.</strong> T.</li>
                        <li><strong>2.</strong> B.</li>
                        <li><strong>3.</strong> D.</li>
                    </ul>
                </div>

                <div class="md:col-span-2 rounded-xl p-5 shadow-lg bg-white/50 backdrop-blur-md">
                    <h2 class="text-xl font-semibold mb-3">Membership & Pricing</h2>
                    <p class="text-slate-600">
                        Ride pay-as-you-go or subscribe for unlimited access to regular or electric bikes. Transparent pricing, billed per trip or monthly.
                    </p>
                    <div class="mt-4 flex flex-col sm:flex-row gap-3 justify-center">
                        <router-link
                            to="/VeloCity/pricing"
                            class="inline-block bg-yellow-300 text-black px-4 py-2 rounded hover:bg-yellow-500 duration-300"
                        >
                            View Pricing
                        </router-link>
                        <router-link
                            to="/VeloCity/home"
                            class="inline-block bg-white/80 text-black px-4 py-2 rounded hover:bg-white duration-300"
                        >
                            Get Started
                        </router-link>
                    </div>
                    <p class="text-slate-600 mt-3 text-sm">
                        See the pricing page for details and current offers.
                    </p>
                </div>
            </section>

            <footer class="text-center text-white py-8">
                <p class="text-sm opacity-90">Need help? Contact support at support@VeloCity.com</p>
            </footer>
        </div>
    </div>
</template>

<script setup>
import topbar from './topbar.vue'
</script>

<style scoped></style>