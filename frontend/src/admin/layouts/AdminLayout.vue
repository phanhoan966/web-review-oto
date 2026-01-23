<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { buildAssetUrl } from '../../public/utils/assetUrl'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const collapsed = ref(false)
const profileOpen = ref(false)

const nav = [
  { label: 'Dashboard', name: 'admin-dashboard', icon: '📊' },
  { label: 'Quản lý user', name: 'admin-users', icon: '👥' },
  { label: 'Quản lý bài viết', name: 'admin-posts', icon: '📰' }
]

const activeName = computed(() => route.name)
const avatarSrc = computed(() => (auth.user?.avatarUrl ? buildAssetUrl(auth.user.avatarUrl) : ''))
const avatarInitial = computed(() => (auth.user?.username || 'A').charAt(0).toUpperCase())
const displayName = computed(() => auth.user?.username || 'Admin')

function toggleSidebar() {
  collapsed.value = !collapsed.value
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
  <div class="layout" :class="{ collapsed }">
    <aside class="sidebar">
      <div class="brand">AutoReview Admin</div>
      <nav>
        <RouterLink v-for="item in nav" :key="item.name" :to="{ name: item.name }" class="nav-item" :class="{ active: activeName === item.name }">
          <span class="icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
    </aside>

    <div class="main">
      <header class="topbar">
        <div class="breadcrumbs">
          <button class="icon" aria-label="Toggle sidebar" @click="toggleSidebar">☰</button>
          <span>Khu vực quản trị</span>
        </div>
        <div class="profile-area">
          <div class="quick-actions">
            <button class="icon-btn" aria-label="Chế độ" title="Chế độ">
              ☾
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
  background: linear-gradient(135deg, #f6f8fb 0%, #eef2ff 100%);
}

.sidebar {
  background: #0b1437;
  color: #e5e7eb;
  padding: 20px 16px;
  display: grid;
  grid-template-rows: auto 1fr auto;
  gap: 18px;
  box-shadow: 8px 0 30px rgba(0, 0, 0, 0.08);
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
  background: rgba(255, 255, 255, 0.03);
  transition: background 0.2s ease, transform 0.1s ease;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateX(2px);
}

.nav-item.active {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  box-shadow: 0 12px 30px rgba(99, 102, 241, 0.35);
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
}

.topbar {
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  backdrop-filter: blur(8px);
  background: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
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
  border: 1px solid #d1d5db;
  background: white;
  cursor: pointer;
  display: grid;
  place-items: center;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
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
  border: 1px solid #e5e7eb;
  background: white;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  display: grid;
  place-items: center;
  cursor: pointer;
  font-size: 18px;
  position: relative;
}

.icon-btn .dot {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #2563eb;
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.15);
}

.avatar-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 2px solid #e5e7eb;
  background: white;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  display: grid;
  place-items: center;
  padding: 0;
  position: relative;
  overflow: hidden;
}

.avatar-btn img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-btn span {
  font-weight: 800;
  color: #0b1437;
}

.status-dot {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 3px white;
}

.dropdown {
  position: absolute;
  right: 0;
  top: 60px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 14px 38px rgba(0, 0, 0, 0.12);
  padding: 12px;
  display: grid;
  gap: 10px;
  min-width: 220px;
  z-index: 10;
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
  border: 1px solid #e5e7eb;
  background: #eef2ff;
  display: grid;
  place-items: center;
  overflow: hidden;
  font-weight: 800;
  color: #312e81;
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
  border: 1px solid #e5e7eb;
  background: #f8fafc;
  cursor: pointer;
  font-weight: 700;
}

.menu-item:hover {
  background: #eef2ff;
  border-color: #c7d2fe;
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

.primary,
.ghost {
  border-radius: 12px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  padding: 10px 14px;
}

.primary {
  background: linear-gradient(135deg, #22c55e, #16a34a);
  color: white;
  box-shadow: 0 10px 26px rgba(34, 197, 94, 0.25);
}

.ghost {
  background: #eef2ff;
  color: #312e81;
  border: 1px solid #c7d2fe;
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
