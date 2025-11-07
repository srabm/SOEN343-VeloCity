<template>
  <topbar />
  <div class="auth-page">
    <video class="auth-bg" :src="bgVideo" autoplay muted loop playsinline></video>
    <div class="auth-overlay"></div>
    <div class="flip-wrapper">
      <div class="flip-inner" :class="{ 'signup-active': mode === 'signup' }">

        <section class="panel login-panel">
          <div class="panel-content">
            <h2 class="panel-heading">Welcome Back</h2>
            <transition name="slide-h" mode="out-in">
              <!-- Login -->
              <form v-if="mode === 'login'" key="login" @submit.prevent="handleLogin" novalidate class="mt-8">
                <div class="form-group">
                  <label for="loginEmail">Email</label>
                  <input
                    class="w-full rounded-xl border border-slate-300 px-3 py-3 text-base shadow-sm focus:border-blue-600 focus:outline-none focus:ring-4 focus:ring-blue-200"
                    v-model.trim="email" id="loginEmail" type="email" required autocomplete="email" />
                </div>
                <div class="form-group">
                  <label for="loginPassword">Password</label>
                  <input
                    class="w-full rounded-xl border border-slate-300 px-3 py-3 text-base shadow-sm focus:border-blue-600 focus:outline-none focus:ring-4 focus:ring-blue-200"
                    v-model="password" id="loginPassword" type="password" required autocomplete="current-password" />
                </div>
                <div class="form-row between">
                  <button type="button" class="link-btn" @click="toggle('forgot')">Forgot password?</button>
                  <button class="primary" type="submit">Log In</button>
                </div>
                <p class="switch-text">Don't have an account?
                  <button type="button" class="link-btn" @click="toggle('signup')">Sign up</button>
                </p>
                <transition name="fade">
                  <p v-if="error" class="error" role="alert">{{ error }}</p>
                </transition>
              </form>

              <!-- Forgot Password -->
              <form v-else-if="mode === 'forgot'" key="forgot" @submit.prevent="handleReset" novalidate class="mt-8">
                <div class="form-group">
                  <label for="resetEmail">Email</label>
                  <input
                    class="w-full rounded-xl border border-slate-300 px-3 py-3 text-base shadow-sm focus:border-blue-600 focus:outline-none focus:ring-4 focus:ring-blue-200"
                    v-model.trim="email" id="resetEmail" type="email" required autocomplete="email" />
                </div>
                <div class="form-row between">
                  <button class="link-btn" type="button" @click="toggle('login')">Back to Login</button>
                  <button class="primary" type="submit">Send reset link</button>
                </div>
                <transition name="fade">
                  <p v-if="info" class="info" role="status">{{ info }}</p>
                </transition>
                <transition name="fade">
                  <p v-if="error" class="error" role="alert">{{ error }}</p>
                </transition>
              </form>
            </transition>
          </div>
        </section>

        <!-- Signup -->
        <section class="panel signup-panel" aria-labelledby="signupHeading">
          <div class="panel-content">
            <h2 id="signupHeading" class="panel-heading">Create Account</h2>
            <form @submit.prevent="handleSignup" novalidate class="mt-8">
              <div class="grid two-cols">
                <div class="form-group">
                  <label for="firstName">First Name</label>
                  <input
                    class="w-full rounded-xl border border-slate-300 px-3 py-3 text-base shadow-sm focus:border-blue-600 focus:outline-none focus:ring-4 focus:ring-blue-200"
                    v-model.trim="firstName" id="firstName" required autocomplete="given-name" />
                </div>
                <div class="form-group">
                  <label for="lastName">Last Name</label>
                  <input
                    class="w-full rounded-xl border border-slate-300 px-3 py-3 text-base shadow-sm focus:border-blue-600 focus:outline-none focus:ring-4 focus:ring-blue-200"
                    v-model.trim="lastName" id="lastName" required autocomplete="family-name" />
                </div>
              </div>
              <div class="form-group">
                <label for="signupEmail">Email</label>
                <input
                  class="w-full rounded-xl border border-slate-300 px-3 py-3 text-base shadow-sm focus:border-blue-600 focus:outline-none focus:ring-4 focus:ring-blue-200"
                  v-model.trim="email" id="signupEmail" type="email" required autocomplete="email" />
              </div>
              <div class="form-group">
                <label for="phoneNumber">Phone Number</label>
                <input
                  class="w-full rounded-xl border border-slate-300 px-3 py-3 text-base shadow-sm focus:border-blue-600 focus:outline-none focus:ring-4 focus:ring-blue-200"
                  v-model.trim="phoneNumber" id="phoneNumber" type="tel" required autocomplete="tel" />
              </div>
              <div class="form-group">
                <label for="signupPassword">Password</label>
                <input
                  class="w-full rounded-xl border border-slate-300 px-3 py-3 text-base shadow-sm focus:border-blue-600 focus:outline-none focus:ring-4 focus:ring-blue-200"
                  v-model="password" id="signupPassword" type="password" required autocomplete="new-password" />
              </div>
              <div class="form-row between">
                <button class="primary" type="submit">Register</button>
              </div>
              <p class="switch-text">Already have an account?
                <button type="button" class="link-btn" @click="toggle('login')">Log in</button>
              </p>
            </form>
            <transition name="fade">
              <p v-if="error && mode === 'signup'" class="error" role="alert">{{ error }}</p>
            </transition>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login, signUp, resetPassword, firestore, doc, setDoc } from '../../firebaseAuth.js'
import topbar from './topbar.vue'
import bgVideo from '../assets/mont-royal.mp4'

const router = useRouter()

const email = ref('')
const password = ref('')
const firstName = ref('')
const lastName = ref('')
const phoneNumber = ref('')

// UI state
const mode = ref('login')
const error = ref('')
const info = ref('')

function toggle(target) {
  error.value = ''
  info.value = ''
  mode.value = target
}

async function handleLogin() {
  error.value = ''
  try {
    await login(email.value, password.value)
    router.push({ name: 'Home' })
  } catch (e) {
    error.value = e?.message
  }
}

async function handleSignup() {
  error.value = ''
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
      phoneNumber: phoneNumber.value
    })
    router.push({ name: 'Home' })
  } catch (e) {
    error.value = e?.message
  }
}

async function handleReset() {
  error.value = ''
  info.value = ''
  try {
    await resetPassword(email.value)
    info.value = 'Password reset email sent. Check your inbox.'
  } catch (e) {
    error.value = e?.message
  }
}
</script>

<style scoped>
.auth-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
}

.auth-bg {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  object-fit: cover;
  z-index: -2;
}

.auth-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .25);
  z-index: -1;
  backdrop-filter: blur(1px);
}

.flip-wrapper {
  width: 100%;
  max-width: 660px;
  height: 660px;
  perspective: 1400px;
}

.flip-inner {
  position: relative;
  width: 100%;
  height: 100%;
  transform-style: preserve-3d;
  transition: transform .9s cubic-bezier(.22, .61, .36, 1);
}

.flip-inner.signup-active {
  transform: rotateY(180deg);
}

.panel {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2.2rem 3rem;
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
  border-radius: 24px;
  box-shadow: 0 20px 40px -10px rgba(0, 0, 0, .35);
  background: #ffffff;
}

.panel.login-panel {
  transform: rotateY(0deg);
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
}

.panel.signup-panel {
  transform: rotateY(180deg);
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
}

.panel-content {
  width: 100%;
  max-width: 520px;
  max-height: 100%;
  overflow: auto;
}

.panel-content h2,
.panel-heading {
  margin: 0 0 1rem;
  font-size: 2rem;
  letter-spacing: .5px;
}

.mt-8 {
  margin-top: 2rem;
}

form {
  display: block;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: .35rem;
  margin-bottom: 1rem;
}

.form-group label {
  font-weight: 600;
  font-size: .85rem;
  letter-spacing: .4px;
  text-transform: uppercase;
  color: #333;
}

.form-group input {
  width: 100%;
  box-sizing: border-box;
  padding: .75rem .9rem;
  border: 1px solid #d0d4da;
  border-radius: 10px;
  background: #fff;
  font-size: .95rem;
  transition: border-color .25s, box-shadow .25s;
}

.form-group input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, .25);
}

.grid.two-cols {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin: .75rem 0 1.25rem;
}

.form-row.between {
  justify-content: space-between;
}


.primary,
.link-btn {
  cursor: pointer;
  font-weight: 600;
  letter-spacing: .5px;
}

.primary {
  background: #facc15;
  color: #1a1a1a;
  border: none;
  padding: .85rem 1.4rem;
  border-radius: 14px;
  box-shadow: 0 6px 16px -4px rgba(234, 179, 8, .55);
  font-weight: 600;
  transition: background .25s, transform .25s, box-shadow .25s;
}

.primary:hover {
  background: #eab308;
  box-shadow: 0 10px 24px -8px rgba(202, 138, 4, .6);
}

.primary:active {
  transform: translateY(2px);
}

.link-btn {
  background: none;
  border: none;
  padding: 0;
  color: #2563eb;
  font-size: .9rem;
}

.link-btn:hover {
  text-decoration: underline;
}

.switch-text {
  margin: .5rem 0 0;
  font-size: .85rem;
  color: #333;
}

.error {
  margin: .5rem 0 0;
  color: #dc2626;
  font-size: .85rem;
  font-weight: 500;
}

.info {
  margin: .5rem 0 0;
  color: #166534;
  font-size: .85rem;
  font-weight: 500;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity .35s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-h-enter-active {
  animation: slideHIn .45s cubic-bezier(.22, .61, .36, 1);
}

.slide-h-leave-active {
  animation: slideHOut .35s cubic-bezier(.4, .07, .6, .12);
}

@keyframes slideHIn {
  0% {
    opacity: 0;
    transform: translateX(40px);
  }

  100% {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideHOut {
  0% {
    opacity: 1;
    transform: translateX(0);
  }

  100% {
    opacity: 0;
    transform: translateX(-40px);
  }
}

@media (max-width: 880px) {
  .flip-wrapper {
    height: auto;
  }

  .panel {
    position: relative;
    box-shadow: none;
    border-radius: 20px;
  }

  .signup-panel {
    transform: none;
  }

  .flip-inner {
    transform: none !important;
  }
}
</style>
