<template>
    <div class="login-container">
        <h2>Login</h2>
        <form @submit="">
            <div class="form-group">
                <label for="username">Username:</label>
                <input type="text" id="username" required />
            </div>
            <div class="form-group">
                <label for="password">Password:</label>
                <input type="password" id="password" required />
            </div>
            <button type="submit">Login</button>
        </form>
        <h2>WELCOME BACK!</h2>
    </div>
</template>

<style scoped></style>
