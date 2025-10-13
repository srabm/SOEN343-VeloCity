<script setup>
import { ref } from 'vue'
import { getAuth, createUserWithEmailAndPassword } from 'firebase/auth'
import app from '../../firebase-config.js'

const auth = getAuth(app)

const email = ref('')
const password = ref('')

const handleSignup = async () => {
  try {
    const userCredential = await createUserWithEmailAndPassword(auth, email.value, password.value)
    console.log('User signed up:', userCredential.user)
  } catch (error) {
    console.error('Error signing up:', error.message)
  }
}

</script>

<template>
  <form @submit.prevent="handleSignup">
    <div class="card">
      <label><b>Email:</b></label>
      <input v-model="email" placeholder="Enter Email" required>

      <label><b>Password:</b></label>
      <input v-model="password" type="password" placeholder="Enter Password" required>

      <button type="submit">Register</button>
    </div>
  </form>
</template>
