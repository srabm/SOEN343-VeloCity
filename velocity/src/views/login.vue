<script setup>
import { ref } from 'vue'
import { login } from '../../firebaseAuth.js'
import topbar from './topbar.vue'

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
  <topbar />
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

    <div class="forgot-password">
      <router-link to="/password-recovery">Forgot password?</router-link>
    </div>

    <h2>WELCOME BACK!</h2>

    <p>Don't have an account? <router-link to="/register">Sign up</router-link></p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../../firebaseAuth.js'

const email = ref('')
const password = ref('')
const router = useRouter()

const handleLogin = async () => {
  try {
    await login(email.value, password.value)
    router.push({ name: 'Home' })
  } catch (error) {
    alert(`Login failed: ${error.message}`)
  }
}
</script>

<style scoped></style>
