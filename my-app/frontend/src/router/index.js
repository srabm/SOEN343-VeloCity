import { createRouter, createWebHistory } from 'vue-router';
import Login from '../views/login.vue';
import PasswordRecovery from '../views/passwordRecovery.vue';
import Register from '../views/register.vue';

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
    }
  ]
});

export default router;
