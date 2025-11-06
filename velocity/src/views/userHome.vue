<template>
    <div class="user-home-container">
        <h1>{{ greeting }}, {{ userName }}!</h1>
        <mapView/>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAuth, onAuthStateChanged } from 'firebase/auth'
import { firestore } from '../../firebaseAuth.js'
import { doc, getDoc } from 'firebase/firestore'
import mapView from './mapView.vue'

const userName = ref('Guest')
const greeting = ref('Welcome')

function computeGreeting() {
    const hour = new Date().getHours()
    if (hour < 12) return 'Good morning'
    if (hour < 18) return 'Good afternoon'
    return 'Good evening'
}

onMounted(() => {
    const auth = getAuth()
    onAuthStateChanged(auth, async (user) => {
        const riderRef = doc(firestore, 'riders', user.uid)
        const snap = await getDoc(riderRef)
        if (snap.exists()) {
            const data = snap.data()
            userName.value = (data.firstName).trim()
        }
        greeting.value = computeGreeting()
    })
}
)
</script>

<style scoped></style>