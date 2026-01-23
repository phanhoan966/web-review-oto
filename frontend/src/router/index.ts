import { createRouter, createWebHistory } from 'vue-router'
import FeedView from '../public/views/FeedView.vue'
import LoginView from '../public/views/LoginView.vue'
import RegisterView from '../public/views/RegisterView.vue'
import ForgotPasswordView from '../public/views/ForgotPasswordView.vue'
import CreateReviewView from '../public/views/CreateReviewView.vue'
import ProfileView from '../public/views/ProfileView.vue'
import ReviewDetailView from '../public/views/ReviewDetailView.vue'
import AdminLoginView from '../admin/views/AdminLoginView.vue'
import AdminDashboardView from '../admin/views/AdminDashboardView.vue'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'feed', component: FeedView },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/register', name: 'register', component: RegisterView },
    { path: '/forgot-password', name: 'forgot-password', component: ForgotPasswordView },
    { path: '/admin/login', name: 'admin-login', component: AdminLoginView },
    { path: '/admin/dashboard', name: 'admin-dashboard', component: AdminDashboardView, meta: { requiresAuth: true } },
    { path: '/reviews/new', name: 'review-create', component: CreateReviewView, meta: { requiresAuth: true } },
    { path: '/post/:slug/:id', name: 'review-detail', component: ReviewDetailView },
    { path: '/reviews/:id', name: 'review-detail-legacy', component: ReviewDetailView },
    { path: '/profile', name: 'profile', component: ProfileView, meta: { requiresAuth: true } }
  ]
})

router.beforeEach(async (to, _from, next) => {
  const auth = useAuthStore()
  if (!auth.hydrated) {
    await auth.hydrate()
  }
  if ((to.name === 'login' || to.name === 'register' || to.name === 'forgot-password') && auth.isAuthenticated) {
    next({ name: 'feed' })
    return
  }
  if (to.name === 'admin-login' && auth.isAuthenticated) {
    next({ name: 'admin-dashboard' })
    return
  }
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    next({ name: 'login' })
    return
  }
  next()
})

export default router
