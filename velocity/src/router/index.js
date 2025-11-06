import { createRouter, createWebHistory } from 'vue-router';
import Login from '../views/login.vue';
import PasswordRecovery from '../views/passwordRecovery.vue';
import Register from '../views/register.vue';
import Home from '../views/userHome.vue';
import Settings from '../views/settings.vue';
import pricing from '../views/pricing.vue';


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' },
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/password-recovery',
      name: 'PasswordRecovery',
      component: PasswordRecovery
    },
    {
      path: '/register',
      name: 'Register',
      component: Register
    },
    {
      path: '/home',
      name: 'Home',
      component: Home
    },

    {
      path: '/settings',
      name: 'Settings',
      component: Settings
    {
      path: '/pricing',
      name: 'Pricing',
      component: pricing
    }
  ]
});

export default router;
