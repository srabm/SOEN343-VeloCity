<template>
  <div>
    <button type="submit" @click="handleLogout">Logout</button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { signOutUser } from '../../firebaseAuth.js'

const handleLogout = async () => {
  try {
    await signOutUser()
    alert(`Signed out!`)
  } catch (error) {
    alert(`Signout failed: ${error.message}`)
  }
}
</script>