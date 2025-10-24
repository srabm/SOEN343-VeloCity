<template>
    <div class="login-container">
        <h2>Register</h2>
        <form @submit.prevent="handleSignup">
            <!-- first name -->
            <div class="form-group">
                <label for="firstName">First Name:</label>
                <input v-model="firstName" type="firstName" id="firstName" required />
            </div>

            <!-- last name -->
            <div class="form-group">
                <label for="lastName">Last Name:</label>
                <input v-model="lastName" type="lastName" id="lastName" required />
            </div>

            <!-- email -->
            <div class="form-group">
                <label for="email">Email:</label>
                <input v-model="email" id="email" required />
            </div>

            <!-- phone number -->
            <div class="form-group">
                <label for="phoneNumber">Phone Number:</label>
                <input v-model="phoneNumber" type="phoneNumber" id="phoneNumber" required />
            </div>

            <!-- payment info -->
            <div class="form-group">
                <label for="paymentInfo">Payment Information:</label>
                <input v-model="paymentInfo" type="paymentInfo" id="paymentInfo" required />
            </div>

            <!-- password -->
            <div class="form-group">
                <label for="password">Password:</label>
                <input v-model="password" type="password" id="password" required />
            </div>

            <button type="submit">Register</button>
        </form>
        <h2>WELCOME!</h2>

        <p>Already have an account? <router-link to="/login">Login</router-link></p>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { signUp, firestore, doc, setDoc } from '../../firebaseAuth.js'

const email = ref('')
const password = ref('')
const firstName = ref('')
const lastName = ref('')
const phoneNumber = ref('')
const paymentInfo = ref('')

const handleSignup = async () => {
    try {
        const userCredential = await signUp(email.value, password.value)
        const user = userCredential.user

        await setDoc(doc(firestore, 'riders', user.uid), {
            id: user.uid,
            firstName: firstName.value,
            lastName: lastName.value,
            email: email.value,
            createdAt: new Date(),
            isOperator: false,
            phoneNumber: phoneNumber.value,
            paymentInfo: paymentInfo.value
        })
        alert(`Account created for ${user.email}!`)

    } catch (error) {
        alert(`Signup failed: ${error.message}`)
    }
}
</script>