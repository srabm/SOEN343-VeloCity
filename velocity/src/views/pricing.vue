<template>
  <div class="bg-cover bg-center" style="background-image: url('/src/assets/bike-bg.jpg');">
    <div class="min-h-screen bg-black/30">
      <topbar />

      <header class="text-center py-8 text-white drop-shadow">
        <h1 class="text-3xl font-semibold">Pricing</h1>
      </header>

      <section class="grid grid-cols-1 md:grid-cols-2 gap-4 max-w-4xl mx-auto text-center px-4">
        
        <!-- Regular Membership -->
        <div class="rounded-xl p-5 shadow-lg bg-white/50 backdrop-blur-md">
          <h2 class="text-xl font-semibold mb-3">Regular Membership</h2>
          <p class="text-2xl font-bold mb-2">
            $22.22 <span class="text-sm font-medium text-slate-600">/ month</span>
          </p>
          <p class="text-slate-600">Unlimited access to regular velos.</p>
          <button class="mt-4 w-full bg-yellow-300 text-black px-4 py-2 rounded hover:bg-yellow-500 duration-300">
            Subscribe
          </button>
        </div>

        <!-- Electric Membership -->
        <div class="rounded-xl p-5 shadow-lg bg-white/50 backdrop-blur-md">
          <h2 class="text-xl font-semibold mb-3">Electric Membership</h2>
          <p class="text-2xl font-bold mb-2">
            $33.33 <span class="text-sm font-medium text-slate-600">/ month</span>
          </p>
          <p class="text-slate-600">Unlimited access to both regular and electric velos.</p>
          <button class="mt-4 w-full bg-yellow-300 text-black px-4 py-2 rounded hover:bg-yellow-500 duration-300">
            Subscribe
          </button>
        </div>

        <!-- Pay-as-you-go -->
        <div class="md:col-span-2 rounded-xl p-5 shadow-lg bg-white/50 backdrop-blur-md">
          <h2 class="text-xl font-semibold mb-3">Pay-as-you-go Pricing</h2>
          <ul class="list-none  space-y-1">
            <li>Base (per trip): <strong>$1.11</strong></li>
            <li>Regular velo: <strong>$0.22</strong> / min</li>
            <li>Electric velo: <strong>$0.33</strong> / min</li>
          </ul>
          <p class="text-slate-600 mt-3 text-sm">
            Default pricing for guests or users without a membership. Charges are calculated when you return your bike based on trip duration.
          </p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import topbar from './topbar.vue'
</script>

<style scoped></style>