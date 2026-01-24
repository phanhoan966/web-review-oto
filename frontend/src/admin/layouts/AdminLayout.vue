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
  { label: 'Dashboard', name: 'admin-dashboard', icon: '📊', desc: 'Số liệu tổng quan' },
  { label: 'Quản lý user', name: 'admin-users', icon: '👥', desc: 'Người dùng & reviewer' },
  { label: 'Quản lý bài viết', name: 'admin-posts', icon: '📰', desc: 'Duyệt và xuất bài' }
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
      <div class="sidebar-head">
        <div class="avatar-wrap">
          <img v-if="avatarSrc" :src="avatarSrc" alt="Avatar" />
          <span v-else>{{ avatarInitial }}</span>
          <span class="status-dot"></span>
        </div>
        <div class="sidebar-meta">
          <p class="eyebrow">AutoReview</p>
          <h3>{{ displayName }}</h3>
          <p class="muted">{{ auth.user?.email || 'auto@review.com' }}</p>
        </div>
      </div>
      <nav class="nav">
        <RouterLink v-for="item in nav" :key="item.name" :to="{ name: item.name }" class="nav-item" :class="{ active: activeName === item.name }">
          <div class="nav-icon">{{ item.icon }}</div>
          <div class="nav-text">
            <p>{{ item.label }}</p>
            <p class="muted small">{{ item.desc }}</p>
          </div>
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
  grid-template-columns: 300px 1fr;
  min-height: 100vh;
  background: #f5f7fb;
  color: var(--text);
  align-items: start;
}

.sidebar {
  background: linear-gradient(180deg, #eaf1ff 0%, #f6f8ff 100%);
  color: var(--text);
  padding: 18px 16px 24px;
  display: grid;
  grid-template-rows: auto 1fr;
  gap: 16px;
  border-right: 1px solid #dfe8f5;
  position: sticky;
  top: 0;
  min-height: 100vh;
  max-height: 100vh;
  overflow-y: auto;
  z-index: 2;
}

.sidebar-head {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 12px;
  align-items: center;
  padding: 12px;
  border-radius: 16px;
  border: 1px solid #e3eaf7;
  background: #ffffff;
  box-shadow: 0 10px 26px rgba(33, 113, 255, 0.12);
}

.avatar-wrap {
  width: 54px;
  height: 54px;
  border-radius: 16px;
  background: linear-gradient(135deg, #1d8aff, #3bb0ff);
  display: grid;
  place-items: center;
  font-weight: 800;
  color: #fff;
  position: relative;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.6);
}

.avatar-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.sidebar-meta h3 {
  margin: 0;
  font-size: 18px;
}

.sidebar-meta .eyebrow {
  margin: 0 0 4px;
  font-size: 13px;
  letter-spacing: 0.02em;
  color: var(--muted);
}

.sidebar-meta .muted {
  margin: 4px 0 0;
}

.sidebar-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.pill-icon {
  border: 1px solid #dbe4f5;
  border-radius: 12px;
  background: #ffffff;
  padding: 10px;
  font-size: 16px;
  cursor: pointer;
  display: grid;
  place-items: center;
  color: var(--text);
  box-shadow: 0 6px 18px rgba(29, 78, 216, 0.08);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.stat-card {
  padding: 10px 12px;
  border-radius: 14px;
  border: 1px solid #e3eaf7;
  background: #ffffff;
  display: grid;
  gap: 2px;
  box-shadow: 0 12px 28px rgba(29, 78, 216, 0.08);
}

.stat-value {
  margin: 0;
  font-weight: 800;
  font-size: 17px;
}

.small {
  font-size: 13px;
}

.icon {
  font-size: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.nav {
  display: grid;
  gap: 14px;
  margin-top: 12px;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-start;
  gap: 14px;
  min-height: 176px;
  padding: 18px 16px;
  border-radius: 18px;
  color: #0f172a;
  text-decoration: none;
  background: #f7f9fc;
  border: 1px solid #e2e9f6;
  box-shadow: 0 10px 22px rgba(28, 100, 242, 0.08);
  transition: transform 0.12s ease, box-shadow 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.nav-item:hover {
  transform: translateX(3px);
  border-color: #d0ddf6;
  box-shadow: 0 12px 26px rgba(28, 100, 242, 0.12);
}

.nav-item.active {
  background: linear-gradient(135deg, #1d8aff, #327bff);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 18px 32px rgba(29, 122, 255, 0.3);
}

.nav-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #e1ebff, #d3e5ff);
  display: grid;
  place-items: center;
  font-size: 20px;
  color: #1f3a8a;
  border: 1px solid #dbe6fd;
  box-shadow: 0 8px 18px rgba(31, 58, 138, 0.08);
}

.nav-item.active .nav-icon {
  background: #ffffff;
  color: #1d8aff;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: none;
}

.nav-text {
  display: grid;
  gap: 4px;
}

.nav-text p {
  margin: 0;
  font-weight: 700;
}

.nav-text .muted {
  margin: 0;
  color: var(--muted);
}

.nav-item.active .muted {
  color: #e7efff;
}

.sidebar-footer {
  display: grid;
  gap: 10px;
  margin-top: auto;
}

.theme-toggle {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #e3eaf7;
  border-radius: 12px;
  padding: 10px 12px;
  background: #ffffff;
  cursor: pointer;
  font-weight: 700;
  color: var(--text);
  box-shadow: 0 8px 18px rgba(29, 78, 216, 0.08);
}

.theme-icon {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  border: 1px solid #e3eaf7;
  display: grid;
  place-items: center;
  background: #f5f7fb;
}

.support-card {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 10px;
  align-items: center;
  border: 1px solid #d9e6ff;
  border-radius: 14px;
  padding: 12px;
  background: #eef4ff;
  box-shadow: 0 10px 22px rgba(33, 113, 255, 0.12);
  color: #0f172a;
}

.support-icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  border: 1px solid #d9e6ff;
  background: #ffffff;
  display: grid;
  place-items: center;
  font-weight: 800;
  color: var(--text);
}

.logout-btn {
  width: 100%;
  padding: 12px;
  border-radius: 14px;
  border: 1px solid #d9e6ff;
  background: #edf2ff;
  font-weight: 800;
  cursor: pointer;
  color: #1d4ed8;
  box-shadow: 0 10px 22px rgba(33, 113, 255, 0.12);
}

.pill-icon.ghost {
  background: #ffffff;
}

.main {
  display: grid;
  grid-template-rows: auto 1fr;
  min-height: 100vh;
  background: var(--bg);
  padding: 18px 16px 24px;
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

@media (max-width: 1024px) {
  .layout {
    grid-template-columns: 84px 1fr;
  }
  .sidebar {
    padding: 16px 10px;
    grid-template-columns: 1fr;
    grid-template-rows: auto auto 1fr auto;
  }
  .sidebar-head {
    grid-template-columns: 1fr;
    justify-items: center;
    text-align: center;
  }
  .sidebar-meta .muted,
  .sidebar-meta h3 {
    text-align: center;
  }
  .sidebar-actions,
  .nav-text,
  .stats-grid .muted,
  .support-card div,
  .support-card .pill-icon,
  .theme-toggle span:last-child,
  .logout-btn {
    display: none;
  }
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .nav-item {
    grid-template-columns: 1fr;
    justify-items: center;
    padding: 12px;
  }
  .nav-text {
    display: none;
  }
  .theme-icon {
    margin: 0 auto;
  }
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
    width: 260px;
    transform: translateX(-100%);
    transition: transform 0.25s ease;
    z-index: 20;
    padding: 16px 12px;
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
  grid-template-columns: 80px 1fr;
}

.layout.collapsed .sidebar {
  padding: 16px 10px;
}

.layout.collapsed .sidebar-head {
  grid-template-columns: 1fr;
  justify-items: center;
  text-align: center;
}

.layout.collapsed .sidebar-meta,
.layout.collapsed .sidebar-actions,
.layout.collapsed .nav-text,
.layout.collapsed .support-card div,
.layout.collapsed .support-card .pill-icon,
.layout.collapsed .theme-toggle span:last-child,
.layout.collapsed .logout-btn,
.layout.collapsed .stats-grid {
  display: none;
}

.layout.collapsed .nav-item {
  grid-template-columns: 1fr;
  justify-items: center;
  padding: 12px;
}
</style>
