<template>
  <topbar />
  <div class="bg-cover bg-center min-h-screen" style="background-image: url('/src/assets/montreal-architecture.jpg');">
    <div class="min-h-screen bg-black/20">
      <header class="text-center py-8 text-white drop-shadow">
        <h1 class="text-4xl font-bold">Maintenance Dashboard</h1>
        <p class="text-lg mt-2 opacity-90">Operator Tools</p>
      </header>

      <section class="max-w-5xl mx-auto px-4 pb-12">
        <!-- Under Construction Notice -->
        <div class="bg-yellow-50 border-l-4 border-yellow-400 p-6 mb-8 rounded-r-lg shadow-lg">
          <div class="flex items-center mb-2">
            <svg class="w-8 h-8 text-yellow-600 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
            </svg>
            <h2 class="text-2xl font-bold text-yellow-800">Page Under Construction</h2>
          </div>
          <p class="text-yellow-700 text-lg">
            This maintenance dashboard is currently being developed. Check back soon for full functionality!
          </p>
        </div>

        <!-- Feature Cards Grid -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          
          <!-- Stations Management -->
          <div class="bg-white/90 backdrop-blur-md rounded-xl shadow-lg p-6 border-t-4 border-blue-500">
            <div class="flex items-center mb-4">
              <div class="bg-blue-100 p-3 rounded-full">
                <svg class="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path>
                </svg>
              </div>
              <h3 class="text-xl font-bold text-gray-800 ml-3">Station Management</h3>
            </div>
            <p class="text-gray-600 mb-4">
              Manage station operational status and availability.
            </p>
            <ul class="space-y-2 text-sm text-gray-700">
              <li class="flex items-start">
                <span class="text-blue-500 mr-2">•</span>
                <span>Mark stations as <strong>Out of Service</strong></span>
              </li>
              <li class="flex items-start">
                <span class="text-blue-500 mr-2">•</span>
                <span>Bring stations <strong>Back to Service</strong></span>
              </li>
              <li class="flex items-start">
                <span class="text-blue-500 mr-2">•</span>
                <span>View station status history</span>
              </li>
              <li class="flex items-start">
                <span class="text-blue-500 mr-2">•</span>
                <span>Add maintenance notes</span>
              </li>
            </ul>
          </div>

          <!-- Dock Management -->
          <div class="bg-white/90 backdrop-blur-md rounded-xl shadow-lg p-6 border-t-4 border-green-500">
            <div class="flex items-center mb-4">
              <div class="bg-green-100 p-3 rounded-full">
                <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                </svg>
              </div>
              <h3 class="text-xl font-bold text-gray-800 ml-3">Dock Management</h3>
            </div>
            <p class="text-gray-600 mb-4">
              Control individual dock availability at stations.
            </p>
            <ul class="space-y-2 text-sm text-gray-700">
              <li class="flex items-start">
                <span class="text-green-500 mr-2">•</span>
                <span>Mark specific docks as <strong>Out of Service</strong></span>
              </li>
              <li class="flex items-start">
                <span class="text-green-500 mr-2">•</span>
                <span>Restore docks <strong>Back to Service</strong></span>
              </li>
              <li class="flex items-start">
                <span class="text-green-500 mr-2">•</span>
                <span>View available vs. total docks per station</span>
              </li>
              <li class="flex items-start">
                <span class="text-green-500 mr-2">•</span>
                <span>Track dock maintenance issues</span>
              </li>
            </ul>
          </div>

          <!-- Bike Management -->
          <div class="bg-white/90 backdrop-blur-md rounded-xl shadow-lg p-6 border-t-4 border-purple-500">
            <div class="flex items-center mb-4">
              <div class="bg-purple-100 p-3 rounded-full">
                <svg class="w-8 h-8 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                </svg>
              </div>
              <h3 class="text-xl font-bold text-gray-800 ml-3">Bike Maintenance</h3>
            </div>
            <p class="text-gray-600 mb-4">
              Manage bike fleet maintenance and repairs.
            </p>
            <ul class="space-y-2 text-sm text-gray-700">
              <li class="flex items-start">
                <span class="text-purple-500 mr-2">•</span>
                <span>Move bikes <strong>To Maintenance</strong></span>
              </li>
              <li class="flex items-start">
                <span class="text-purple-500 mr-2">•</span>
                <span>Return bikes <strong>From Maintenance</strong></span>
              </li>
              <li class="flex items-start">
                <span class="text-purple-500 mr-2">•</span>
                <span>View bikes currently in maintenance</span>
              </li>
              <li class="flex items-start">
                <span class="text-purple-500 mr-2">•</span>
                <span>Log repair details and issues</span>
              </li>
            </ul>
          </div>

        </div>

        <!-- Additional Info -->
        <div class="mt-8 bg-white/90 backdrop-blur-md rounded-xl shadow-lg p-6">
          <h3 class="text-xl font-bold text-gray-800 mb-4">Coming Soon</h3>
          <div class="grid grid-cols-1 md:grid-cols-1 gap-4 text-gray-700">
            <div>
              <h4 class="font-semibold text-green-600 mb-2">📝 Maintenance Logs</h4>
              <p class="text-sm">Complete history of all maintenance activities with timestamps and operator notes.</p>
            </div>
            <div>
              <h4 class="font-semibold text-purple-600 mb-2">🔔 Automated Alerts</h4>
              <p class="text-sm">Receive notifications when bikes or docks require attention or maintenance.</p>
            </div>
            <div>
              <h4 class="font-semibold text-orange-600 mb-2">📈 Maintenance Analytics</h4>
              <p class="text-sm">Track maintenance trends, common issues, and fleet health metrics.</p>
            </div>
          </div>
        </div>

        <!-- Placeholder Action Section -->
        <div class="mt-8 text-center">
          <p class="text-white text-lg mb-4">
            Need to report an issue or suggest a feature?
          </p>
          <button 
            class="bg-blue-600 text-white px-8 py-3 rounded-lg font-semibold hover:bg-blue-700 transition duration-200 shadow-lg"
            @click="contactSupport"
          >
            Contact Support
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import topbar from './topbar.vue'

function contactSupport() {
  alert('Support contact feature coming soon! For now, please email support@velocity.com');
}
</script>

<style scoped>
/* Any additional custom styles can go here */
</style>