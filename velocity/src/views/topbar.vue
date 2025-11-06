<script setup>
import Logout from './logout.vue'
import logo from '../assets/banner.png'
</script>

<template>
    <header class="bg-white text-black">
        <nav class="grid grid-cols-3 items-stretch h-14 px-4">
            <!-- logo -->
            <div class="flex items-center">
                <img :src="logo" alt="VeloCity" class="h-10 w-auto select-none" />
            </div>

            <!-- tabs -->
            <div class="justify-self-center h-full flex items-stretch">
                <router-link to="/home" class="h-full inline-flex items-center px-4 hover:bg-slate-200 duration-400">
                    Home
                </router-link>
                <router-link to="/pricing" class="h-full inline-flex items-center px-4 hover:bg-slate-200 duration-400">
                    Pricing
                </router-link>
            </div>

            <!-- logout -->
            <div class="justify-self-end h-full flex items-center">
                <Logout />
            </div>
        </nav>
    </header>
</template>

<style scoped>

</style>