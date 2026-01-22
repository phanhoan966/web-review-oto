<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
const router = useRouter()
const initials = computed(() => auth.user?.username?.[0]?.toUpperCase() || 'U')

async function logout() {
  await auth.logout()
  router.push({ name: 'feed' })
}
</script>

<template>
  <div class="app-shell">
    <header class="site-header">
      <div class="container header-inner">
        <RouterLink class="brand" to="/">AutoReview</RouterLink>
        <nav class="nav">
          <RouterLink to="/">Trang chủ</RouterLink>
          <RouterLink v-if="auth.isAuthenticated" to="/reviews/new">Tạo bài</RouterLink>
          <RouterLink v-if="auth.isAuthenticated" to="/profile">Hồ sơ</RouterLink>
          <RouterLink v-if="auth.isAuthenticated" to="/admin/dashboard">Dashboard</RouterLink>
          <RouterLink v-else to="/admin/login">Admin</RouterLink>
        </nav>
        <div class="actions">
          <RouterLink v-if="!auth.isAuthenticated" class="ghost" to="/login">Đăng nhập</RouterLink>
          <RouterLink v-if="!auth.isAuthenticated" class="primary" to="/register">Đăng ký</RouterLink>
          <div v-else class="user-box">
            <div class="user-meta">
              <div class="user-name">{{ auth.user?.username }}</div>
              <div class="user-sub">{{ auth.user?.email }}</div>
              <div class="user-actions">
                <RouterLink class="mini" to="/profile">Hồ sơ</RouterLink>
                <button class="mini ghost" @click="logout">Đăng xuất</button>
              </div>
            </div>
            <div class="avatar" :style="auth.user?.avatarUrl ? `background-image:url(${auth.user.avatarUrl})` : ''">{{ auth.user?.avatarUrl ? '' : initials }}</div>
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
  background: rgba(255, 255, 255, 0.9);
  border-bottom: 1px solid var(--border);
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-top: 16px;
  padding-bottom: 12px;
}

.brand {
  font-weight: 800;
  font-size: 20px;
  letter-spacing: -0.01em;
}

.nav {
  display: flex;
  align-items: center;
  gap: 14px;
  a {
    padding: 8px 10px;
    border-radius: 12px;
    color: #1f2a3d;
    font-weight: 600;
    transition: background-color 0.2s ease;
  }
  a.router-link-active {
    background: #e8f2ff;
    color: #0d6efd;
  }
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ghost,
.primary,
button.ghost {
  border: 1px solid var(--border);
  background: transparent;
  padding: 9px 14px;
  border-radius: 12px;
  font-weight: 700;
  color: #1f2a3d;
  cursor: pointer;
  transition: all 0.2s ease;
}

.primary {
  background: linear-gradient(135deg, #0ea5e9, #2563eb);
  border: none;
  color: #fff;
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.25);
}

.ghost:hover,
button.ghost:hover {
  background: #f3f5f9;
}

.user-box {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 8px 10px;
  border: 1px solid var(--border);
  border-radius: 16px;
  background: #f7f9fc;
}

.user-meta {
  display: grid;
  gap: 4px;
}

.user-name {
  font-weight: 800;
  font-size: 15px;
}

.user-sub {
  color: var(--muted);
  font-size: 13px;
}

.user-actions {
  display: flex;
  gap: 8px;
}

.mini {
  padding: 6px 10px;
  border-radius: 10px;
  font-weight: 700;
  border: 1px solid var(--border);
  background: #fff;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-size: cover;
  background-position: center;
  display: grid;
  place-items: center;
  font-weight: 800;
  color: #0d6efd;
  background: linear-gradient(135deg, #e8f2ff, #f8fbff);
}

@media (max-width: 720px) {
  .nav {
    display: none;
  }
}
</style>
