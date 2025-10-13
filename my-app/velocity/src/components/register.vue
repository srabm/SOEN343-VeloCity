<template>
    <div class="login-container">
        <h2>Register</h2>
        <form @submit="">
            <div class="form-group">
                <label for="username">Username:</label>
                <input type="text" id="username" required />
            </div>
            <div class="form-group">
                <label for="email">Email:</label>
                <input type="email" id="email" required />
            </div>
            <div class="form-group">
                <label for="password">Password:</label>
                <input type="password" id="password" required />
            </div>
            <button type="submit">Register</button>
        </form>
        <h2>WELCOME!</h2>
    </div>
</template>