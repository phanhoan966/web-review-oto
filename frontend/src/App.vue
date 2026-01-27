<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { RouterLink, RouterView, useRouter, useRoute } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { useUiStore } from './stores/ui'
import brandLogo from './assets/autoreview-logo.png'

const auth = useAuthStore()
const ui = useUiStore()
const router = useRouter()
const route = useRoute()
const initials = computed(() => auth.user?.username?.[0]?.toUpperCase() || 'U')
const menuOpen = ref(false)
const menuRef = ref<HTMLElement | null>(null)
const isAdminRoute = computed(() => route.path.startsWith('/admin'))
const searchTerm = ref(route.query.q ? String(route.query.q) : '')

if (typeof window !== 'undefined') {
  ui.initTheme()
}

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

function toggleTheme() {
  ui.toggleTheme()
}

function handleClickOutside(event: MouseEvent) {
  if (menuRef.value && !menuRef.value.contains(event.target as Node)) {
    menuOpen.value = false
  }
}

watch(
  () => route.query.q,
  (val) => {
    searchTerm.value = val ? String(val) : ''
  }
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
        <nav class="nav">
          <RouterLink v-if="!auth.isAuthenticated" to="/admin/login">Admin</RouterLink>
        </nav>
        <form class="search-bar" @submit.prevent="submitSearch">
          <input v-model="searchTerm" type="search" placeholder="Tìm bài, hãng, mẫu xe" />
          <button type="submit">Tìm</button>
        </form>
        <div class="actions">
          <button class="icon-btn" aria-label="Chuyển chế độ" @click="toggleTheme">{{ ui.theme === 'dark' ? '☀' : '☾' }}</button>
          <div class="notify">
            <button class="icon-btn" aria-label="Thông báo">🔔</button>
            <span class="badge"></span>
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
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px 8px 14px;
  border: 1px solid var(--border);
  background: var(--surface);
  border-radius: 14px;
  box-shadow: var(--shadow);
  min-width: 280px;
}

.search-bar input {
  border: none;
  outline: none;
  background: transparent;
  color: var(--text);
  width: 100%;
}

.search-bar button {
  border: none;
  background: linear-gradient(135deg, var(--accent), var(--primary));
  color: #fff;
  border-radius: 12px;
  padding: 8px 12px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.22);
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
  width: 10px;
  height: 10px;
  background: var(--accent);
  border-radius: 50%;
  position: absolute;
  top: -2px;
  right: -2px;
  box-shadow: 0 0 0 4px var(--surface);
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
