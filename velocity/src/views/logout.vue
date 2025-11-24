<template>
  <div class="flex items-center gap-3">
    <router-link to="/VeloCity/settings" class="inline-flex items-center mt-1">
      <img :src="userIcon" alt="User settings" class="h-8 w-8 rounded-full hover:ring-2 hover:ring-yellow-300 duration-200" />
    </router-link>
    <button
      type="button"
      class="bg-[#0348af] text-white font-medium px-3 py-1 rounded hover:bg-blue-700 duration-300"
      @click="handleLogout"
    >
      Logout
    </button>
  </div>
  
</template>

<script setup>
import { signOutUser } from '../../firebaseAuth.js'
import { useRouter } from 'vue-router'
import userIcon from '../assets/user.png'

const router = useRouter()

const handleLogout = async () => {
  try {
    await signOutUser()
    router.push({ name: 'Login' })
  } catch (error) {
    alert(`Signout failed: ${error.message}`)
  }
}
</script>