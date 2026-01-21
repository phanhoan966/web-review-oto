import { createRouter, createWebHistory } from 'vue-router'
import FeedView from '../views/FeedView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ForgotPasswordView from '../views/ForgotPasswordView.vue'
import AdminLoginView from '../views/AdminLoginView.vue'
import CreateReviewView from '../views/CreateReviewView.vue'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'feed', component: FeedView },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/register', name: 'register', component: RegisterView },
    { path: '/forgot-password', name: 'forgot-password', component: ForgotPasswordView },
    { path: '/admin/login', name: 'admin-login', component: AdminLoginView },
    { path: '/reviews/new', name: 'review-create', component: CreateReviewView, meta: { requiresAuth: true } }
  ]
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  if ((to.name === 'login' || to.name === 'register' || to.name === 'forgot-password' || to.name === 'admin-login') && auth.isAuthenticated) {
    next({ name: 'feed' })
    return
  }
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    next({ name: 'login' })
    return
  }
  next()
})

export default router
