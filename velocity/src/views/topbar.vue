<script setup>
import Logout from '../views/logout.vue'
import logo from '../assets/banner.png'
</script>
<template>
    <div class="topbar-container">
        <div class="topbar-left">
            <img :src="logo" alt="VeloCity" class="logo" />
        </div>
        <div class="topbar-right">
            <Logout />
        </div>
    </div>
</template>
<style scoped>
.topbar-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 20px;
}

.topbar-left {
    flex-grow: 1;
}

.topbar-right {
    display: flex;
    align-items: center;
}
</style>