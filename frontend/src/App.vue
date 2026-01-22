<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
const router = useRouter()
const initials = computed(() => auth.user?.username?.[0]?.toUpperCase() || 'U')
const menuOpen = ref(false)
const menuRef = ref<HTMLElement | null>(null)

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

function handleClickOutside(event: MouseEvent) {
  if (menuRef.value && !menuRef.value.contains(event.target as Node)) {
    menuOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})

async function logout() {
  await auth.logout()
  router.push({ name: 'feed' })
  menuOpen.value = false
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
          <button class="icon-btn" aria-label="Chuyển chế độ">☾</button>
          <button class="icon-btn" aria-label="Thông báo">🔔</button>
          <span class="badge"></span>
          <RouterLink v-if="!auth.isAuthenticated" class="ghost" to="/login">Đăng nhập</RouterLink>
          <RouterLink v-if="!auth.isAuthenticated" class="primary" to="/register">Đăng ký</RouterLink>
          <div v-else class="user-menu" ref="menuRef">
            <button class="avatar-btn" @click.stop="toggleMenu">
              <div class="avatar-ring">
                <div class="avatar" :style="auth.user?.avatarUrl ? `background-image:url(${auth.user.avatarUrl})` : ''">{{ auth.user?.avatarUrl ? '' : initials }}</div>
              </div>
            </button>
            <div v-if="menuOpen" class="dropdown surface">
              <RouterLink class="item" to="/profile" @click="menuOpen = false"><span class="ico">👤</span>Hồ sơ</RouterLink>
              <RouterLink class="item" to="/profile" @click="menuOpen = false"><span class="ico">📧</span>Tài khoản</RouterLink>
              <RouterLink class="item" to="/reviews/new" @click="menuOpen = false"><span class="ico">📋</span>Nhiệm vụ</RouterLink>
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

.icon-btn {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  border: 1px solid var(--border);
  background: linear-gradient(135deg, #f8fbff, #eef2ff);
  display: grid;
  place-items: center;
  cursor: pointer;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.05);
}

.badge {
  width: 10px;
  height: 10px;
  background: #7c3aed;
  border-radius: 50%;
  margin-left: -14px;
  margin-right: 6px;
  box-shadow: 0 0 0 4px #f7f9fc;
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

.user-menu {
  position: relative;
}

.avatar-btn {
  border: none;
  background: transparent;
  padding: 0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-ring {
  padding: 2px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fe8fb6, #a173ff);
  display: grid;
  place-items: center;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background-size: cover;
  background-position: center;
  display: grid;
  place-items: center;
  font-weight: 800;
  color: #0f172a;
  background: linear-gradient(135deg, #f3f4f6, #e0f2fe);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  border: 1px solid #e5e7eb;
}

.dropdown {
  position: absolute;
  right: 0;
  top: 120%;
  min-width: 200px;
  padding: 10px;
  border-radius: 14px;
  box-shadow: 0 18px 36px rgba(0, 0, 0, 0.08);
  display: grid;
  gap: 6px;
  background: #fff;
  z-index: 20;
}

.item {
  padding: 10px 12px;
  border-radius: 10px;
  color: #1f2a3d;
  border: 1px solid transparent;
}

.item:hover {
  background: #f7f9fc;
  border-color: var(--border);
}

.logout {
  margin-top: 4px;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #d1d9e6;
  background: #f8f0ff;
  color: #7c3aed;
  font-weight: 800;
  cursor: pointer;
}

@media (max-width: 720px) {
  .nav {
    display: none;
  }
}
</style>
