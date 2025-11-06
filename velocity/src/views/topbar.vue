<template>
    <header class="bg-white text-black">
        <nav class="grid grid-cols-3 items-stretch h-14 px-4">
            <!-- logo -->
            <div class="flex items-center">
                <img :src="logo" alt="VeloCity" class="h-10 w-auto select-none" />
            </div>

            <!-- tabs -->
            <div class="justify-self-center h-full flex items-stretch">
                <router-link to="/VeloCity/home" class="h-full inline-flex items-center px-4 hover:bg-slate-200 duration-400">
                    Home
                </router-link>
                <router-link to="/VeloCity/pricing" class="h-full inline-flex items-center px-4 hover:bg-slate-200 duration-400">
                    Pricing
                </router-link>
            </div>

            <!-- Login/Logout -->
            <div class="justify-self-end h-full flex items-center">
                <template v-if="isLoggedIn">
                    <Logout />
                </template>
                <template v-else>
                    <router-link
                        to="/login"
                        class="bg-yellow-300 text-black font-medium px-3 py-1 rounded hover:bg-yellow-400 duration-300"
                    >
                        Login
                    </router-link>
                </template>
            </div>
        </nav>
    </header>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAuth, onAuthStateChanged } from 'firebase/auth'
import Logout from './logout.vue'
import logo from '../assets/banner.png'

const isLoggedIn = ref(false)

onMounted(() => {
    const auth = getAuth()
    onAuthStateChanged(auth, (user) => {
        isLoggedIn.value = !!user
    })
})
</script>


<style scoped>

</style>