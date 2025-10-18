<script setup>
import { ref } from 'vue'
import { resetPassword } from '../../firebaseAuth.js'

const email = ref('')
const message = ref('')

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const handleResetPassword = async () => {
  message.value = ''

  if (!email.value) {
    message.value = 'No email provided.'
    return
  }
  if (!emailRegex.test(email.value)) {
    message.value = 'Please enter a valid email address.'
    return
  }

  try {
    await resetPassword(email.value)
    message.value = 'If an account under this email exists, a password reset email has been sent.'
  } catch (error) {
    message.value = 'Password reset failed.'
  }
}
</script>

<template>
  <div class="password-recovery-container">
    <h2>Password Reset</h2>

    <div>
      Please enter the email address connected with your account to proceed with resetting your password.
    </div>

    <div class="input-group">
      <label for="email">Email:</label>
      <input v-model="email" type="email" id="email" placeholder="Enter your email" />
    </div>

    <div v-if="message">
      {{ message }}
    </div>

    <button @click="handleResetPassword">Reset My Password</button>

    <div class="back-to-login">
      <router-link to="/login">Back to Login</router-link>
    </div>
  </div>
</template>

<style scoped></style>
