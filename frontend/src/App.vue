<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { RouterLink, RouterView, useRouter, useRoute } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { useUiStore } from './stores/ui'
import client from './api/client'
import brandLogo from './assets/autoreview-logo.png'

type NotificationItem = {
  id: number
  type?: string
  message?: string
  read?: boolean
  createdAt?: string
  reviewId?: number
  reviewSlug?: string
  reviewTitle?: string
  commentId?: number
  actorName?: string
  actorUsername?: string
  actorAvatar?: string
}

const auth = useAuthStore()
const ui = useUiStore()
const router = useRouter()
const route = useRoute()
const initials = computed(() => auth.user?.username?.[0]?.toUpperCase() || 'U')
const menuOpen = ref(false)
const menuRef = ref<HTMLElement | null>(null)
const notifyRef = ref<HTMLElement | null>(null)
const isAdminRoute = computed(() => route.path.startsWith('/admin'))
const showSearchBar = computed(() => !isAdminRoute.value && (route.name === 'feed' || route.name === 'search'))
const searchTerm = ref(route.query.q ? String(route.query.q) : '')
const notifications = ref<NotificationItem[]>([])
const unreadCount = ref(0)
const notifyOpen = ref(false)
const notificationsLoading = ref(false)

if (typeof window !== 'undefined') {
  ui.initTheme()
}

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

function toggleTheme() {
  ui.toggleTheme()
}

function toggleNotify() {
  if (!auth.isAuthenticated) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  notifyOpen.value = !notifyOpen.value
  if (notifyOpen.value) {
    loadNotifications()
  }
}

function handleClickOutside(event: MouseEvent) {
  if (menuRef.value && !menuRef.value.contains(event.target as Node)) {
    menuOpen.value = false
  }
  if (notifyRef.value && !notifyRef.value.contains(event.target as Node)) {
    notifyOpen.value = false
  }
}

watch(
  () => route.query.q,
  (val) => {
    searchTerm.value = val ? String(val) : ''
  }
)

watch(
  () => auth.user,
  (user) => {
    if (user) {
      loadUnreadCount()
      loadNotifications()
    } else {
      notifyOpen.value = false
      notifications.value = []
      unreadCount.value = 0
    }
  },
  { immediate: true }
)

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})

function submitSearch() {
  const term = searchTerm.value.trim()
  if (!term) {
    router.push({ name: 'feed' })
    return
  }
  router.push({ name: 'search', query: { q: term } })
  menuOpen.value = false
}

async function loadUnreadCount() {
  if (!auth.isAuthenticated) {
    unreadCount.value = 0
    return
  }
  try {
    const { data } = await client.get<{ count: number }>('/notifications/unread-count')
    unreadCount.value = data?.count ?? 0
  } catch (error) {
    unreadCount.value = 0
  }
}

async function loadNotifications() {
  if (!auth.isAuthenticated) {
    notifications.value = []
    return
  }
  notificationsLoading.value = true
  try {
    const { data } = await client.get<NotificationItem[]>('/notifications', { params: { limit: 20 } })
    notifications.value = Array.isArray(data) ? data : []
    unreadCount.value = notifications.value.filter((item) => !item.read).length
  } catch (error) {
    notifications.value = []
  } finally {
    notificationsLoading.value = false
  }
}

function formatTime(value?: string) {
  if (!value) return ''
  return new Date(value).toLocaleString('vi-VN')
}

async function markRead(id: number) {
  if (!auth.isAuthenticated) return
  const target = notifications.value.find((item) => item.id === id)
  if (!target || target.read) {
    return
  }
  try {
    await client.post(`/notifications/${id}/read`)
    notifications.value = notifications.value.map((item) => (item.id === id ? { ...item, read: true } : item))
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch (error) {
    return
  }
}

async function handleNotificationClick(item: NotificationItem) {
  await markRead(item.id)
  notifyOpen.value = false
  if (item.reviewSlug) {
    router.push({ name: 'review-detail', params: { slug: item.reviewSlug } })
    return
  }
  if (item.reviewId) {
    router.push({ name: 'review-detail-legacy', params: { id: item.reviewId } })
  }
}

async function logout() {
  await auth.logout()
  router.push({ name: 'feed' })
  menuOpen.value = false
}
</script>

<template>
  <div class="app-shell">
    <header v-if="!isAdminRoute" class="site-header">
      <div class="container header-inner">
        <RouterLink class="brand" to="/">
          <img class="brand-logo" :src="brandLogo" alt="AutoReview" />
        </RouterLink>
        <form v-if="showSearchBar" class="search-bar" @submit.prevent="submitSearch">
          <input v-model="searchTerm" type="search" placeholder="Tìm bài, hãng, mẫu xe" />
          <button type="submit" aria-label="Tìm kiếm">🔍</button>
        </form>
        <div class="actions">
          <button class="icon-btn" aria-label="Chuyển chế độ" @click="toggleTheme">{{ ui.theme === 'dark' ? '☀' : '☾' }}</button>
          <div class="notify" ref="notifyRef">
            <button class="icon-btn" aria-label="Thông báo" @click.stop="toggleNotify">
              🔔
              <span v-if="unreadCount" class="badge">{{ unreadCount }}</span>
            </button>
            <div v-if="notifyOpen" class="notify-dropdown surface">
              <div class="notify-head">Thông báo</div>
              <div v-if="notificationsLoading" class="notify-status">Đang tải...</div>
              <div v-else-if="!notifications.length" class="notify-status">Chưa có thông báo</div>
              <ul v-else class="notify-list">
                <li v-for="item in notifications" :key="item.id" :class="{ unread: !item.read }">
                  <button type="button" class="notify-item" @click="handleNotificationClick(item)">
                    <div class="notify-msg">{{ item.message || 'Thông báo mới' }}</div>
                    <div class="notify-meta">
                      <span>{{ item.reviewTitle || 'Bài viết' }}</span>
                      <span>·</span>
                      <span>{{ formatTime(item.createdAt) }}</span>
                    </div>
                  </button>
                </li>
              </ul>
            </div>
          </div>
          <RouterLink v-if="!auth.isAuthenticated" class="ghost" to="/login">Đăng nhập</RouterLink>
          <RouterLink v-if="!auth.isAuthenticated" class="primary" to="/register">Đăng ký</RouterLink>
          <div v-else class="user-menu" ref="menuRef">
            <button class="author" @click.stop="toggleMenu">
              <div class="avatar" :style="auth.user?.avatarUrl ? `background-image:url(${auth.user.avatarUrl})` : ''">
                {{ auth.user?.avatarUrl ? '' : initials }}
              </div>
            </button>
            <div v-if="menuOpen" class="dropdown surface">
              <RouterLink class="item" to="/profile" @click="menuOpen = false"><span class="ico">👤 </span>Hồ sơ</RouterLink>
              <RouterLink class="item" to="/profile" @click="menuOpen = false"><span class="ico">📧 </span>Tài khoản</RouterLink>
              <RouterLink class="item" to="/reviews/new" @click="menuOpen = false"><span class="ico">📋 </span>Tạo bài</RouterLink>
              <button class="logout" @click="logout">Đăng xuất</button>
            </div>
          </div>
        </div>
      </div>
    </header>
    <RouterView />
  </div>
</template>

<style scoped lang="scss">
.app-shell {
  min-height: 100vh;
  background: var(--bg);
}

.site-header {
  position: sticky;
  top: 0;
  z-index: 10;
  backdrop-filter: blur(10px);
  background: var(--header-bg);
  border-bottom: 1px solid var(--border);
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-top: 5px;
  padding-bottom: 2px;
  flex-wrap: wrap;
}

.brand {
  display: block;
}

.brand-logo {
  height: 60px;
  width: auto;
  display: block;
}

.nav {
  display: flex;
  align-items: center;
  gap: 14px;
  a {
    padding: 8px 10px;
    border-radius: 12px;
    color: var(--text);
    font-weight: 600;
    transition: background-color 0.2s ease;
  }
  a.router-link-active {
    background: var(--chip-bg);
    color: var(--primary);
  }
}

.search-bar {
  height: 50px;
  display: flex;
  align-items: center;
  text-align: center;
  gap: 12px;
  padding: 10px 12px 10px 14px;
  border: 1px solid var(--border);
  // background: linear-gradient(135deg, rgba(14, 165, 233, 0.12), rgba(37, 99, 235, 0.08)), var(--surface);
  border-radius: 16px;
  box-shadow: 0 14px 28px rgba(37, 99, 235, 0.12);
  min-width: 320px;
  max-width: 720px;
}

.search-bar input {
  border: none;
  outline: none;
  background: transparent;
  color: var(--text);
  width: 100%;
  letter-spacing: 0.01em;
}

.search-bar input::placeholder {
  color: var(--muted);
}

.search-bar button {
  border: none;
  // background: linear-gradient(135deg, rgba(14, 165, 233, 0.12), rgba(37, 99, 235, 0.08)), var(--surface);
  color: #fff;
  border-radius: 12px;
  padding: 10px 14px;
  font-weight: 800;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 42px;
}

.search-bar button:active {
  transform: translateY(0);
}

@media (max-width: 920px) {
  .search-bar {
    order: 3;
    width: 100%;
    min-width: 0;
  }
  .actions {
    order: 2;
  }
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notify {
  position: relative;
}

.notify-dropdown {
  position: absolute;
  right: 0;
  top: 110%;
  width: 320px;
  max-height: 420px;
  overflow: auto;
  border: 1px solid var(--border);
  border-radius: 16px;
  box-shadow: var(--shadow);
  background: var(--surface);
  z-index: 20;
}

.notify-head {
  padding: 12px 14px;
  font-weight: 700;
  border-bottom: 1px solid var(--border);
}

.notify-status {
  padding: 14px;
  font-size: 14px;
  color: #6b7280;
}

.notify-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.notify-list li {
  border-bottom: 1px solid var(--border);
}

.notify-list li:last-child {
  border-bottom: none;
}

.notify-item {
  width: 100%;
  text-align: left;
  background: transparent;
  border: none;
  padding: 12px 14px;
  display: grid;
  gap: 4px;
  cursor: pointer;
}

.notify-list li.unread .notify-item {
  background: var(--chip-bg);
}

.notify-msg {
  font-weight: 600;
  color: var(--text);
}

.notify-meta {
  font-size: 12px;
  color: #6b7280;
  display: flex;
  gap: 6px;
  align-items: center;
}

.icon-btn {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  border: 1px solid var(--border);
  background: var(--icon-surface);
  display: grid;
  place-items: center;
  cursor: pointer;
  box-shadow: var(--shadow);
  color: var(--text);
}

.badge {
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  background: var(--accent);
  border-radius: 999px;
  position: absolute;
  top: -4px;
  right: -4px;
  box-shadow: 0 0 0 3px var(--surface);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.ghost,
.primary,
button.ghost {
  border: 1px solid var(--border);
  background: transparent;
  padding: 9px 14px;
  border-radius: 12px;
  font-weight: 700;
  color: var(--text);
  cursor: pointer;
  transition: all 0.2s ease;
}

.primary {
  background: linear-gradient(135deg, var(--accent), var(--primary));
  border: none;
  color: #fff;
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.25);
}

.ghost:hover,
button.ghost:hover {
  background: var(--chip-bg);
}

.user-menu {
  position: relative;
}

.author {
  display: grid;
  grid-template-columns: 35px 1fr;
  align-items: center;
  border: none;
  background: transparent;
  padding: 0;
  cursor: pointer;
}

.avatar {
  width: 35px;
  height: 35px;
  display: flex;
  text-align: center;
  align-items: center;
  font-weight: bold;
  justify-content: center;
  border: 0.5px solid;
  border-radius: 50%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.dropdown {
  position: absolute;
  right: 0;
  top: 110%;
  min-width: 220px;
  padding: 12px;
  border-radius: 16px;
  box-shadow: var(--shadow);
  display: grid;
  gap: 8px;
  background: var(--surface);
  border: 1px solid var(--border);
  z-index: 20;
}

.item {
  padding: 10px 12px;
  border-radius: 12px;
  color: var(--text);
  border: 1px solid transparent;
  display: flex;
  align-items: center;
  gap: 8px;
}

.item:hover {
  background: var(--chip-bg);
  border-color: var(--border);
}

.logout {
  margin-top: 4px;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: var(--chip-bg);
  color: var(--primary);
  font-weight: 800;
  cursor: pointer;
}

@media (max-width: 720px) {
  .nav {
    display: none;
  }
}
</style>
