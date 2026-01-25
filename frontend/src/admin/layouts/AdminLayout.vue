<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useUiStore } from '../../stores/ui'
import { buildAssetUrl } from '../../public/utils/assetUrl'
import brandLogo from '../../assets/autoreview-logo.png'

const auth = useAuthStore()
const ui = useUiStore()
const route = useRoute()
const router = useRouter()
const collapsed = ref(false)
const profileOpen = ref(false)
const sidebarOpen = ref(false)
const isMobileView = ref(false)
const childRefs = ref<Record<string, HTMLElement | null>>({})

function setChildRef(name: string, el: HTMLElement | null) {
  childRefs.value[name] = el
}

function scrollChildren(name: string, delta: number) {
  const el = childRefs.value[name]
  if (el) {
    el.scrollBy({ top: delta, behavior: 'smooth' })
  }
}

const nav = [
  { label: 'Dashboard', name: 'admin-dashboard', icon: '📊', desc: 'Số liệu tổng quan' },
  {
    label: 'Quản lý user',
    name: 'admin-users',
    icon: '👥',
    desc: 'Người dùng & reviewer',
    children: [
      { label: 'Tất cả user', name: 'admin-users', icon: '👥', desc: 'Người dùng & reviewer' },
      { label: 'User đã xóa', name: 'admin-deleted-users', icon: '🗂', desc: 'Khôi phục tài khoản' }
    ]
  },
  {
    label: 'Quản lý bài viết',
    name: 'admin-posts',
    icon: '📰',
    desc: 'Duyệt và xuất bài',
    children: [
      { label: 'Bài pending', name: 'admin-pending-posts', icon: '⏳', desc: 'Chờ duyệt' },
      { label: 'Bài đã duyệt', name: 'admin-approved-posts', icon: '✅', desc: 'Đã xuất bản' },
      { label: 'Bài bị từ chối', name: 'admin-rejected-posts', icon: '🚫', desc: 'Xem và khôi phục' }
    ]
  }
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
  <div class="layout" :class="[ui.theme === 'dark' ? 'admin-dark' : 'admin-light', { collapsed, 'sidebar-open': sidebarOpen }]">
    <aside class="sidebar">
      <div class="sidebar-head">
        <div class="avatar-wrap">
          <img v-if="avatarSrc" :src="avatarSrc" alt="Avatar" />
          <span v-else>{{ avatarInitial }}</span>
          <span class="status-dot"></span>
        </div>
        <div class="sidebar-meta">
          <img class="brand-mark" :src="brandLogo" alt="AutoReview" />
          <h3>{{ displayName }}</h3>
          <p class="muted">{{ auth.user?.email || 'auto@review.com' }}</p>
        </div>
      </div>
      <nav class="nav">
        <div v-for="item in nav" :key="item.name" class="nav-group">
          <RouterLink
            :to="{ name: item.name }"
            class="nav-item"
            :class="{ active: activeName === item.name || (item.children && item.children.some((child) => child.name === activeName)) }"
          >
            <div class="nav-icon">{{ item.icon }}</div>
            <div class="nav-text">
              <p>{{ item.label }}</p>
              <p class="muted small">{{ item.desc }}</p>
            </div>
          </RouterLink>
          <div v-if="item.children" class="nav-children-wrap">
            <div class="scroll-controls">
              <button type="button" aria-label="Cuộn lên" @click.stop="scrollChildren(item.name, -80)">▲</button>
              <button type="button" aria-label="Cuộn xuống" @click.stop="scrollChildren(item.name, 80)">▼</button>
            </div>
            <div class="nav-children" :ref="(el) => setChildRef(item.name, el)">
              <RouterLink v-for="child in item.children" :key="child.name" :to="{ name: child.name }" class="nav-sub" :class="{ active: activeName === child.name }">
                <div class="nav-icon">{{ child.icon }}</div>
                <div class="nav-text">
                  <p>{{ child.label }}</p>
                  <p class="muted small">{{ child.desc }}</p>
                </div>
              </RouterLink>
            </div>
          </div>
        </div>
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
  grid-template-columns: 350px 1fr;
  min-height: 100vh;
  background: var(--bg);
  color: var(--text);
  align-items: start;
}

.layout.admin-light {
  --bg: #f5f7fb;
  --surface: #ffffff;
  --text: #0f172a;
  --muted: #4b5563;
  --border: #e5e7eb;
  --chip-bg: #f7f9fc;
  --shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
  --primary: #2171ff;
  --accent: #3ba9ff;
  --pill-border: #e5e7eb;
  --sidebar-start: rgba(226, 234, 255, 0.9);
  --sidebar-end: rgba(246, 248, 255, 0.95);
}

.layout.admin-dark {
  --bg: #0b1220;
  --surface: #0f172a;
  --text: #e5e7eb;
  --muted: #94a3b8;
  --border: #1f2937;
  --chip-bg: #111827;
  --shadow: 0 12px 30px rgba(0, 0, 0, 0.5);
  --primary: #60a5fa;
  --accent: #38bdf8;
  --pill-border: #1f2937;
  --sidebar-start: rgba(15, 23, 42, 0.95);
  --sidebar-end: rgba(9, 12, 23, 0.95);
}

.sidebar {
  background: linear-gradient(180deg, var(--sidebar-start) 0%, var(--sidebar-end) 100%);
  color: var(--text);
  padding: 18px 16px 24px;
  display: grid;
  grid-template-rows: auto 1fr;
  gap: 16px;
  border-right: 1px solid var(--border);
  position: sticky;
  top: 0;
  min-height: 100vh;
  max-height: 100vh;
  overflow-y: auto;
  z-index: 2;
}

.admin-dark .sidebar {
  background: linear-gradient(180deg, var(--sidebar-start), var(--sidebar-end));
  border-right: 1px solid var(--border);
}

.sidebar-head {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 12px;
  align-items: center;
  padding: 12px;
  border-radius: 16px;
  border: 1px solid var(--border);
  background: var(--surface);
  box-shadow: var(--shadow);
}

.avatar-wrap {
  width: 65px;
  height: 71px;
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

.brand-mark {
  height: 28px;
  width: auto;
  display: block;
  margin-bottom: 6px;
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
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--surface);
  padding: 10px;
  font-size: 16px;
  cursor: pointer;
  display: grid;
  place-items: center;
  color: var(--text);
  box-shadow: var(--shadow);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.stat-card {
  padding: 10px 12px;
  border-radius: 14px;
  border: 1px solid var(--border);
  background: var(--surface);
  display: grid;
  gap: 2px;
  box-shadow: var(--shadow);
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
  display: flow;
  gap: 14px;
  margin-top: 4px;
}

.nav-group {
  display: grid;
  gap: 8px;
  margin-bottom: 10px;
}

.nav-children-wrap {
  position: relative;
  display: grid;
  gap: 8px;
  padding-right: 10px;
}

.scroll-controls {
  position: absolute;
  top: 10px;
  right: 10px;
  display: grid;
  gap: 6px;
}

.scroll-controls button {
  border: 1px solid var(--border);
  background: var(--chip-bg);
  border-radius: 50%;
  padding: 0;
  cursor: pointer;
  font-weight: 800;
  color: var(--muted);
  box-shadow: var(--shadow);
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  line-height: 1;
}

.scroll-controls button:hover {
  border-color: var(--accent);
  color: var(--text);
}

.nav-children {
  display: grid;
  gap: 8px;
  padding-left: 18px;
  max-height: 240px;
  overflow-y: auto;
}

.nav-children::-webkit-scrollbar {
  width: 6px;
}

.nav-children::-webkit-scrollbar-thumb {
  background: var(--border);
  border-radius: 999px;
}

.nav-sub {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 12px;
  border-radius: 14px;
  color: var(--text);
  text-decoration: none;
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  transition: transform 0.12s ease, box-shadow 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.nav-sub:hover {
  transform: translateX(3px);
  border-color: var(--accent);
  box-shadow: var(--shadow);
}

.nav-sub.active {
  background: linear-gradient(135deg, var(--accent), var(--primary));
  color: #fff;
  border-color: transparent;
  box-shadow: 0 18px 32px rgba(29, 122, 255, 0.3);
}

.nav-sub .nav-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(33, 113, 255, 0.16), rgba(59, 169, 255, 0.08));
  display: grid;
  place-items: center;
  font-size: 18px;
  color: var(--text);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.nav-sub.active .nav-icon {
  background: #ffffff;
  color: #1d8aff;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: none;
}

.nav-item {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 16px;
  border-radius: 18px;
  color: var(--text);
  text-decoration: none;
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  transition: transform 0.12s ease, box-shadow 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.nav-item:hover {
  transform: translateX(3px);
  border-color: var(--accent);
  box-shadow: var(--shadow);
}

.nav-item.active {
  background: linear-gradient(135deg, var(--accent), var(--primary));
  color: #fff;
  border-color: transparent;
  box-shadow: 0 18px 32px rgba(29, 122, 255, 0.3);
}

.nav-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(33, 113, 255, 0.16), rgba(59, 169, 255, 0.08));
  display: grid;
  place-items: center;
  font-size: 20px;
  color: var(--text);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
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
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 10px 12px;
  background: var(--surface);
  cursor: pointer;
  font-weight: 700;
  color: var(--text);
  box-shadow: var(--shadow);
}

.theme-icon {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  border: 1px solid var(--border);
  display: grid;
  place-items: center;
  background: var(--chip-bg);
}

.support-card {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 10px;
  align-items: center;
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 12px;
  background: linear-gradient(135deg, rgba(33, 113, 255, 0.12), rgba(59, 169, 255, 0.08));
  box-shadow: var(--shadow);
  color: var(--text);
}

.support-icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  border: 1px solid var(--border);
  background: var(--surface);
  display: grid;
  place-items: center;
  font-weight: 800;
  color: var(--text);
}

.logout-btn {
  width: 100%;
  padding: 12px;
  border-radius: 14px;
  border: 1px solid var(--border);
  background: var(--chip-bg);
  font-weight: 800;
  cursor: pointer;
  color: var(--text);
  box-shadow: var(--shadow);
}

.pill-icon.ghost {
  background: var(--surface);
}

.main {
  display: grid;
  grid-template-rows: auto 1fr;
  min-height: 100vh;
  background: var(--bg);
  color: var(--text);
  padding: 18px 16px 24px;
}

.topbar {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  backdrop-filter: blur(8px);
  background: var(--surface);
  color: var(--text);
  border-bottom: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.breadcrumbs {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  color: var(--text);
}

.breadcrumbs span {
  color: var(--text);
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
  padding-top: 22px;
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
    width: 150px;
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
  grid-template-columns: 130px 1fr;
}

.layout.collapsed .sidebar {
  padding: 18px 16px 24px;
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
