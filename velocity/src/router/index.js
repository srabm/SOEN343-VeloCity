import { createRouter, createWebHistory } from 'vue-router';
import { getAuth, onAuthStateChanged } from 'firebase/auth';
import Login from '../views/login.vue';
import PasswordRecovery from '../views/passwordRecovery.vue';
import Register from '../views/register.vue';
import Home from '../views/userHome.vue';
import pricing from '../views/pricing.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/home' },
    {
      path: '/login',
      name: 'Login',
      component: Login,
      meta: { requiresGuest: true }
    },
    {
      path: '/password-recovery',
      name: 'PasswordRecovery',
      component: PasswordRecovery,
      meta: { requiresGuest: true }
    },
    {
      path: '/register',
      name: 'Register',
      component: Register,
      meta: { requiresGuest: true }
    },
    {
      path: '/home',
      name: 'Home',
      component: Home
    },
    {
      path: '/pricing',
      name: 'Pricing',
      component: pricing
    }
  ]
});

function getCurrentUser() {
  return new Promise((resolve) => {
    const auth = getAuth();
    const removeListener = onAuthStateChanged(auth, (user) => {
      removeListener();
      resolve(user);
    });
  });
}

router.beforeEach(async (to) => {
  if (to.matched.some((r) => r.meta?.requiresGuest)) {
    const user = await getCurrentUser();
    if (user) return { path: '/home' };
  }
  return true;
});

export default router;
