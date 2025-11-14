import { createRouter, createWebHistory } from 'vue-router';
import { getAuth, onAuthStateChanged } from 'firebase/auth';
import Login from '../views/login.vue';
import PasswordRecovery from '../views/passwordRecovery.vue';
import Register from '../views/register.vue';
import Home from '../views/userHome.vue';
import mapView  from '../views/mapView.vue';
import Settings from '../views/settings.vue';
import pricing from '../views/pricing.vue';
import RideHistory from '../views/rideHistory.vue';
import BillingHistory from '../views/billingHistory.vue';
import AboutUs from '../views/aboutUs.vue';
import BikeReservation from '../views/bikeReservation.vue';
import ActiveTrip from '../views/activeTrip.vue';

// todo : make routing to new operator-only pages conditional on operator status?
// au pire we can do it within the page maybe

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/VeloCity/home' },
    {
      path: '/VeloCity/login',
      name: 'Login',
      component: Login,
      meta: { requiresGuest: true }
    },
    {
      path: '/VeloCity/password-recovery',
      name: 'PasswordRecovery',
      component: PasswordRecovery,
      meta: { requiresGuest: true }
    },
    {
      path: '/VeloCity/register',
      name: 'Register',
      component: Register,
      meta: { requiresGuest: true }
    },
    {
      path: '/VeloCity/home',
      name: 'Home',
      component: Home
    },
    {
      path: '/VeloCity/map',
      name: 'MapView',
      component: mapView
    },
    {
      path: '/VeloCity/reservation',
      name: 'BikeReservation',
      component: BikeReservation,
      meta: { requiresAuth: true }
    },
    {
      path: '/VeloCity/active-trip',
      name: 'ActiveTrip',
      component: ActiveTrip,
      meta: { requiresAuth: true }
    },
    {
      path: '/VeloCity/settings',
      name: 'Settings',
      component: Settings,
      meta: { requiresAuth: true }
    },
    {
      path: '/VeloCity/pricing',
      name: 'Pricing',
      component: pricing
    },
    {
      path: '/VeloCity/rides',
      name: 'RideHistory',
      component: RideHistory,
      meta: { requiresAuth: true }
    },
    {
      path: '/VeloCity/billing',
      name: 'BillingHistory',
      component: BillingHistory,
      meta: { requiresAuth: true }
    },
    {
      path: '/VeloCity/about',
      name: 'AboutUs',
      component: AboutUs
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
  const user = await getCurrentUser();

  // Redirect authenticated users away from guest-only pages
  if (to.matched.some((r) => r.meta?.requiresGuest)) {
    const user = await getCurrentUser();
    if (user) return { path: '/VeloCity/home' };
  }

  // Redirect unauthenticated users to login for protected pages
  if (to.matched.some((r) => r.meta?.requiresAuth)) {
    if (!user) return { path: '/VeloCity/login' };
  }

  return true;
});

export default router;
