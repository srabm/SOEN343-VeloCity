import { createRouter, createWebHistory } from 'vue-router';
import { getAuth, onAuthStateChanged } from 'firebase/auth';
import { getFirestore, doc, getDoc } from 'firebase/firestore';
import app from '../../firebase-config.js';
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

// Import operator-specific views (create these as needed)
import MoveBike from '../views/moveBike.vue';
// import AllRides from '../views/operator/allRides.vue';
// import AllBilling from '../views/operator/allBilling.vue';
// import Maintenance from '../views/operator/maintenance.vue';

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
    },
    
    
    {
      path: '/VeloCity/move-bike',
      name: 'MoveBike',
      component: MoveBike,
      meta: { requiresAuth: true, requiresOperator: true }
    },

    // Operator-only routes
    // Uncomment and implement these routes as you create the corresponding components
    /*
    {
      path: '/VeloCity/operator/all-rides',
      name: 'AllRides',
      component: AllRides,
      meta: { requiresAuth: true, requiresOperator: true }
    },
    {
      path: '/VeloCity/operator/all-billing',
      name: 'AllBilling',
      component: AllBilling,
      meta: { requiresAuth: true, requiresOperator: true }
    },
    {
      path: '/VeloCity/operator/maintenance',
      name: 'Maintenance',
      component: Maintenance,
      meta: { requiresAuth: true, requiresOperator: true }
    }
    */
  ]
});

async function getCurrentUser() {
  return new Promise((resolve) => {
    const auth = getAuth();
    const removeListener = onAuthStateChanged(auth, (user) => {
      removeListener();
      resolve(user);
    });
  });
}

async function getUserProfile(uid) {
  const db = getFirestore(app);
  const docRef = doc(db, 'riders', uid);
  const docSnap = await getDoc(docRef);
  
  if (docSnap.exists()) {
    return docSnap.data();
  }
  return null;
}

router.beforeEach(async (to) => {
  const user = await getCurrentUser();

  // Redirect authenticated users away from guest-only pages
  if (to.matched.some((r) => r.meta?.requiresGuest)) {
    if (user) return { path: '/VeloCity/home' };
  }

  // Redirect unauthenticated users to login for protected pages
  if (to.matched.some((r) => r.meta?.requiresAuth)) {
    if (!user) return { path: '/VeloCity/login' };
  }

  // Check operator permissions for operator-only routes
  if (to.matched.some((r) => r.meta?.requiresOperator)) {
    if (!user) return { path: '/VeloCity/login' };
    
    const profile = await getUserProfile(user.uid);
    
    // Redirect non-operators or operators with operator view disabled
    if (!profile?.isOperator || !profile?.isOperatorView) {
      return { path: '/VeloCity/home' };
    }
  }

  return true;
});

export default router;