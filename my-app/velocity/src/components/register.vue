<script setup>
import { ref } from 'vue'
import signUp from '../../firebaseAuth.js'

const email = ref('')
const password = ref('')

const handleSignup = async () => {
  await signUp(email.value, password.value)
}
</script>

<template>
    <div class="login-container">
        <h2>Register</h2>
        <form @submit.prevent="handleSignup">
            <div class="form-group">
                <label for="email">Email:</label>
                <input v-model="email" id="email" required />
            </div>
            <div class="form-group">
                <label for="password">Password:</label>
                <input v-model="password" type="password" id="password" required />
            </div>
            <button type="submit">Register</button>
        </form>
        <h2>WELCOME!</h2>
    </div>
</template>