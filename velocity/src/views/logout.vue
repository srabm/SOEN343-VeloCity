<template>
  <div>
    <button
      type="button"
      class="bg-yellow-300 text-black font-medium px-3 py-1 rounded hover:bg-yellow-400 duration-300"
      @click="handleLogout"
    >
      Logout
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { signOutUser } from '../../firebaseAuth.js'
import { useRouter } from 'vue-router'

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