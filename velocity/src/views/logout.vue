<template>
  <div>
    <button type="submit" @click="handleLogout">Logout</button>
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