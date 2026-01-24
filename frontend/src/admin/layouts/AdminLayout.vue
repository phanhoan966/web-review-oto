<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useUiStore } from '../../stores/ui'
import { buildAssetUrl } from '../../public/utils/assetUrl'

const auth = useAuthStore()
const ui = useUiStore()
const route = useRoute()
const router = useRouter()
const collapsed = ref(false)
const profileOpen = ref(false)
const sidebarOpen = ref(false)
const isMobileView = ref(false)

const nav = [
  { label: 'Dashboard', name: 'admin-dashboard', icon: '📊' },
  { label: 'Quản lý user', name: 'admin-users', icon: '👥' },
  { label: 'Quản lý bài viết', name: 'admin-posts', icon: '📰' }
]

const activeName = computed(() => route.name)
const avatarSrc = computed(() => (auth.user?.avatarUrl ? buildAssetUrl(auth.user.avatarUrl) : ''))
const avatarInitial = computed(() => (auth.user?.username || 'A').charAt(0).toUpperCase())
const displayName = computed(() => auth.user?.username || 'Admin')
const themeIcon = computed(() => (ui.theme === 'dark' ? '☀' : '☾'))
const themeLabel = computed(() => (ui.theme === 'dark' ? 'Chuyển sang chế độ sáng' : 'Chuyển sang chế độ tối'))

function updateIsMobile() {
  isMobileView.value = typeof window !== 'undefined' && window.matchMedia('(max-width: 720px)').matches
  if (!isMobileView.value) {
    sidebarOpen.value = false
  }
}

onMounted(() => {
  updateIsMobile()
  window.addEventListener('resize', updateIsMobile)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateIsMobile)
})

function toggleSidebar() {
  if (isMobileView.value) {
    sidebarOpen.value = !sidebarOpen.value
    return
  }
  collapsed.value = !collapsed.value
}

function toggleThemeMode() {
  ui.toggleTheme()
}

function closeSidebar() {
  sidebarOpen.value = false
}

function toggleProfileMenu() {
  profileOpen.value = !profileOpen.value
}

function closeProfileMenu() {
  profileOpen.value = false
}

function goProfile() {
  profileOpen.value = false
  router.push({ name: 'profile' })
}

async function logoutAndClose() {
  profileOpen.value = false
  await auth.logout()
  router.push({ name: 'admin-login' })
}
</script>

<template>
  <div class="layout" :class="{ collapsed, 'sidebar-open': sidebarOpen }">
    <aside class="sidebar">
      <div class="brand">AutoReview Admin</div>
      <nav>
        <RouterLink v-for="item in nav" :key="item.name" :to="{ name: item.name }" class="nav-item" :class="{ active: activeName === item.name }">
          <span class="icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
    </aside>
    <div v-if="isMobileView && sidebarOpen" class="sidebar-overlay" @click="closeSidebar"></div>

    <div class="main">
      <header class="topbar">
        <div class="breadcrumbs">
          <button class="icon" aria-label="Toggle sidebar" @click="toggleSidebar">☰</button>
          <span>Khu vực quản trị</span>
        </div>
        <div class="profile-area">
          <div class="quick-actions">
            <button class="icon-btn" :aria-label="themeLabel" :title="themeLabel" @click="toggleThemeMode">
              {{ themeIcon }}
            </button>
            <button class="icon-btn" aria-label="Thông báo" title="Thông báo">
              🔔
              <span class="dot"></span>
            </button>
          </div>
          <button class="avatar-btn" aria-label="Tài khoản" @click="toggleProfileMenu">
            <img v-if="avatarSrc" :src="avatarSrc" alt="Avatar" />
            <span v-else>{{ avatarInitial }}</span>
            <span class="status-dot"></span>
          </button>
          <div v-if="profileOpen" class="dropdown" @click.stop>
            <div class="user-row">
              <div class="menu-avatar">
                <img v-if="avatarSrc" :src="avatarSrc" alt="Avatar" />
                <span v-else>{{ avatarInitial }}</span>
              </div>
              <div>
                <p class="name">{{ displayName }}</p>
                <p class="muted">{{ auth.user?.email || 'Quản trị' }}</p>
              </div>
            </div>
            <button class="menu-item" @click="goProfile">Hồ sơ</button>
            <button class="menu-item" @click="logoutAndClose">Đăng xuất</button>
          </div>
          <div v-if="profileOpen" class="backdrop" @click="closeProfileMenu"></div>
        </div>
      </header>
      <main class="content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped lang="scss">
.layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  min-height: 100vh;
  background: var(--bg);
  color: var(--text);
}

.sidebar {
  background: var(--surface);
  color: var(--text);
  padding: 20px 16px;
  display: grid;
  grid-template-rows: auto 1fr;
  gap: 18px;
  border-right: 1px solid var(--border);
  box-shadow: 8px 0 30px rgba(15, 23, 42, 0.06);
}

.brand {
  font-weight: 800;
  font-size: 20px;
  letter-spacing: -0.02em;
}

nav {
  display: grid;
  gap: 8px;
}

.nav-item {
  display: grid;
  grid-template-columns: 32px 1fr;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 12px;
  color: inherit;
  text-decoration: none;
  background: var(--chip-bg);
  border: 1px solid transparent;
  transition: background 0.2s ease, transform 0.1s ease, border-color 0.2s ease;
}

.nav-item:hover {
  background: var(--surface);
  border-color: var(--border);
  transform: translateX(2px);
}

.nav-item.active {
  background: linear-gradient(135deg, var(--primary), var(--accent));
  color: #fff;
  box-shadow: var(--shadow);
  border-color: transparent;
}

.icon {
  font-size: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.main {
  display: grid;
  grid-template-rows: auto 1fr;
  min-height: 100vh;
  background: var(--bg);
}

.topbar {
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  backdrop-filter: blur(8px);
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
}

.breadcrumbs {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
}

.breadcrumbs .icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: var(--surface);
  cursor: pointer;
  display: grid;
  place-items: center;
  box-shadow: var(--shadow);
  color: var(--text);
}

.profile-area {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 14px;
}

.quick-actions {
  display: inline-flex;
  gap: 10px;
}

.icon-btn {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  border: 1px solid var(--border);
  background: var(--surface);
  box-shadow: var(--shadow);
  display: grid;
  place-items: center;
  cursor: pointer;
  font-size: 18px;
  position: relative;
  color: var(--text);
}

.icon-btn .dot {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 0 4px rgba(43, 138, 239, 0.12);
}

.avatar-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 2px solid var(--border);
  background: var(--surface);
  box-shadow: var(--shadow);
  cursor: pointer;
  display: grid;
  place-items: center;
  padding: 0;
  position: relative;
  overflow: hidden;
  color: var(--text);
}

.avatar-btn img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-btn span {
  font-weight: 800;
  color: var(--text);
}

.status-dot {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 3px var(--surface);
}

.dropdown {
  position: absolute;
  right: 0;
  top: 60px;
  background: var(--surface);
  border-radius: 16px;
  box-shadow: var(--shadow);
  padding: 12px;
  display: grid;
  gap: 10px;
  min-width: 220px;
  z-index: 10;
  border: 1px solid var(--border);
}

.user-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px;
}

.menu-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: 1px solid var(--border);
  background: var(--chip-bg);
  display: grid;
  place-items: center;
  overflow: hidden;
  font-weight: 800;
  color: var(--text);
}

.menu-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.menu-item {
  width: 100%;
  text-align: left;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: var(--chip-bg);
  cursor: pointer;
  font-weight: 700;
  color: var(--text);
}

.menu-item:hover {
  background: var(--surface);
  border-color: var(--border);
}

.backdrop {
  position: fixed;
  inset: 0;
  background: transparent;
}

.actions {
  display: flex;
  gap: 10px;
}

main.content {
  padding: 22px 22px 32px;
}

@media (max-width: 720px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    height: 100vh;
    width: 240px;
    transform: translateX(-100%);
    transition: transform 0.25s ease;
    z-index: 20;
    padding: 14px 12px;
  }
  .layout.sidebar-open .sidebar {
    transform: translateX(0);
  }
  .sidebar-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.28);
    z-index: 15;
  }
  .main {
    grid-column: 1 / -1;
  }
  .topbar {
    padding: 12px 14px;
    gap: 10px;
    position: sticky;
    top: 0;
    z-index: 12;
    background: var(--surface);
  }
  .breadcrumbs span {
    display: none;
  }
  .profile-area {
    gap: 10px;
  }
  .quick-actions {
    gap: 8px;
  }
  .icon-btn {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }
  .avatar-btn {
    width: 42px;
    height: 42px;
  }
  main.content {
    padding: 16px;
  }
}

.primary,
.ghost {
  border-radius: 12px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  padding: 10px 14px;
}

.primary {
  background: linear-gradient(135deg, var(--accent), var(--primary));
  color: white;
  box-shadow: 0 10px 26px rgba(34, 197, 94, 0.18);
}

.ghost {
  background: var(--chip-bg);
  color: var(--text);
  border: 1px solid var(--border);
}

.layout.collapsed {
  grid-template-columns: 72px 1fr;
}

.layout.collapsed .sidebar {
  padding: 16px 10px;
}

.layout.collapsed .brand,
.layout.collapsed .nav-item span:last-child {
  display: none;
}

.layout.collapsed .nav-item {
  grid-template-columns: 1fr;
  justify-items: center;
  padding: 12px;
}

@media (max-width: 1024px) {
  .layout {
    grid-template-columns: 72px 1fr;
  }
  .sidebar {
    padding: 16px 10px;
  }
  .brand,
  .nav-item span:last-child {
    display: none;
  }
  .sidebar-footer {
    display: grid;
  }
  .sidebar-footer .user {
    display: none;
  }
  .nav-item {
    grid-template-columns: 1fr;
    justify-items: center;
    padding: 12px;
  }
}
</style>
