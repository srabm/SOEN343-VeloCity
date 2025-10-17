<script setup>
import { ref } from 'vue'
import { resetPassword } from '../../firebaseAuth.js'

const email = ref('')

const handleResetPassword = async () => {
  const emailEntered = email.value || window.prompt('Enter your email to receive password reset link:')
  if (!emailEntered) {
    alert('No email provided.')
    return
  }
  try {
    await resetPassword(emailEntered)
    alert('Password reset email sent. Check your inbox.')
  } catch (error) {
    alert(`Password reset failed: ${error.message}`)
  }
}
</script>

<template>
    <div class="login-container">
        <h2>Password Reset</h2>
        
        <div>
            Please enter the email address connected with your account to proceed with resetting your password.
        </div>

        <div class="input-group">
            <label for="email">Email:</label>
            <input v-model="email" type="email" id="email" placeholder="Enter your email" />
        </div>
        <button @click="handleResetPassword">Reset My Password</button>

        <div class="back-to-login">
            <a href="/login">Back to Login</a>
        </div>
    </div>
</template>

<style scoped></style>
