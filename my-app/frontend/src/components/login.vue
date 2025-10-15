<script setup>
import { ref } from 'vue'
import { login } from '../../firebaseAuth.js'

const email = ref('')
const password = ref('')

const handleLogin = async () => {
  try {
    const userCredential = await login(email.value, password.value)
    const user = userCredential.user
    alert(`Welcome ${user.email}!`)
  } catch (error) {
    alert(`Login failed: ${error.message}`)
  }
}
</script>

<template>
    <div class="login-container">
        <h2>Login</h2>
        <form @submit.prevent="handleLogin">
            <div class="form-group">
                <label for="email">Email:</label>
                <input v-model="email" id="email" required />
            </div>
            <div class="form-group">
                <label for="password">Password:</label>
                <input v-model="password" type="password" id="password" required />
            </div>
            <button type="submit">Login</button>
        </form>
        <h2>WELCOME BACK!</h2>
    </div>
</template>

<style scoped></style>
