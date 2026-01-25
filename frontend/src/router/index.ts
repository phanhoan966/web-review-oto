import { createRouter, createWebHistory } from 'vue-router'
import FeedView from '../public/views/FeedView.vue'
import LoginView from '../public/views/LoginView.vue'
import RegisterView from '../public/views/RegisterView.vue'
import ForgotPasswordView from '../public/views/ForgotPasswordView.vue'
import CreateReviewView from '../public/views/CreateReviewView.vue'
import ProfileView from '../public/views/ProfileView.vue'
import UserProfilePublicView from '../public/views/UserProfilePublicView.vue'
import ReviewDetailView from '../public/views/ReviewDetailView.vue'
import AdminLoginView from '../admin/views/AdminLoginView.vue'
import AdminDashboardView from '../admin/views/AdminDashboardView.vue'
import AdminLayout from '../admin/layouts/AdminLayout.vue'
import AdminUsersView from '../admin/views/AdminUsersView.vue'
import AdminDeletedUsersView from '../admin/views/AdminDeletedUsersView.vue'
import AdminPostsView from '../admin/views/AdminPostsView.vue'
import AdminRejectedPostsView from '../admin/views/AdminRejectedPostsView.vue'
import AdminPostDetailView from '../admin/views/AdminPostDetailView.vue'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'feed', component: FeedView },
    { path: '/brand/:brand', name: 'feed-brand', component: FeedView },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/register', name: 'register', component: RegisterView },
    { path: '/forgot-password', name: 'forgot-password', component: ForgotPasswordView },
    { path: '/admin/login', name: 'admin-login', component: AdminLoginView },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: { name: 'admin-dashboard' } },
        { path: 'dashboard', name: 'admin-dashboard', component: AdminDashboardView },
        { path: 'users', name: 'admin-users', component: AdminUsersView },
        { path: 'users/deleted', name: 'admin-deleted-users', component: AdminDeletedUsersView },
        { path: 'posts', name: 'admin-posts', component: AdminPostsView },
        { path: 'posts/rejected', name: 'admin-rejected-posts', component: AdminRejectedPostsView },
        { path: 'posts/:id', name: 'admin-post-detail', component: AdminPostDetailView }
      ]
    },
    { path: '/reviews/new', name: 'review-create', component: CreateReviewView, meta: { requiresAuth: true } },
    { path: '/post/:slug/:id', name: 'review-detail', component: ReviewDetailView },
    { path: '/reviews/:id', name: 'review-detail-legacy', component: ReviewDetailView },
    { path: '/user/:username', name: 'user-profile', component: UserProfilePublicView },
    { path: '/profile', name: 'profile', component: ProfileView, meta: { requiresAuth: true } }
  ]
})

router.beforeEach(async (to, _from, next) => {
  const auth = useAuthStore()
  if (!auth.hydrated) {
    await auth.hydrate()
  }
  const isAdminRoute = to.path.startsWith('/admin')

  if ((to.name === 'login' || to.name === 'register' || to.name === 'forgot-password') && auth.isAuthenticated) {
    next({ name: 'feed' })
    return
  }
  if (to.name === 'admin-login' && auth.isAuthenticated) {
    next({ name: 'admin-dashboard' })
    return
  }
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    next({ name: isAdminRoute ? 'admin-login' : 'login' })
    return
  }
  next()
})

export default router
