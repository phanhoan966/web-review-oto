import { createRouter, createWebHistory } from 'vue-router'
import FeedView from '../views/FeedView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ForgotPasswordView from '../views/ForgotPasswordView.vue'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'feed', component: FeedView },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/register', name: 'register', component: RegisterView },
    { path: '/forgot-password', name: 'forgot-password', component: ForgotPasswordView }
  ]
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  if ((to.name === 'login' || to.name === 'register' || to.name === 'forgot-password') && auth.isAuthenticated) {
    next({ name: 'feed' })
    return
  }
  next()
})

export default router
