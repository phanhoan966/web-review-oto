<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const collapsed = ref(false)

const nav = [
  { label: 'Dashboard', name: 'admin-dashboard', icon: '📊' },
  { label: 'Quản lý user', name: 'admin-users', icon: '👥' },
  { label: 'Quản lý bài viết', name: 'admin-posts', icon: '📰' }
]

const activeName = computed(() => route.name)

function toggleSidebar() {
  collapsed.value = !collapsed.value
}

async function logout() {
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
      <div class="sidebar-footer">
        <div class="user">
          <div class="avatar">{{ (auth.user?.username || 'A').charAt(0).toUpperCase() }}</div>
          <div class="meta">
            <div class="name">{{ auth.user?.username || 'Admin' }}</div>
            <div class="role">Quản trị</div>
          </div>
        </div>
        <button class="ghost" @click="logout">Đăng xuất</button>
      </div>
    </aside>

    <div class="main">
      <header class="topbar">
        <div class="breadcrumbs">
          <button class="icon" aria-label="Toggle sidebar" @click="toggleSidebar">☰</button>
          <span>Khu vực quản trị</span>
        </div>
        <div class="actions">
          <button class="ghost" @click="router.push({ name: 'review-create' })">Tạo bài mới</button>
          <button class="primary" @click="router.push({ name: 'feed' })">Xem Public</button>
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

.sidebar-footer {
  display: grid;
  gap: 12px;
}

.user {
  display: grid;
  grid-template-columns: 40px 1fr;
  gap: 10px;
  align-items: center;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2563eb, #06b6d4);
  display: grid;
  place-items: center;
  font-weight: 800;
  color: white;
}

.meta .name {
  font-weight: 700;
}

.meta .role {
  color: #9ca3af;
  font-size: 13px;
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
.layout.collapsed .sidebar-footer,
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
  .sidebar-footer,
  .nav-item span:last-child {
    display: none;
  }
  .nav-item {
    grid-template-columns: 1fr;
    justify-items: center;
    padding: 12px;
  }
}
</style>
