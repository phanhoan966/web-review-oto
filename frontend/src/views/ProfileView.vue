<script setup lang="ts">
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()

function goCreate() {
  router.push({ name: 'review-create' })
}
</script>

<template>
  <div class="page">
    <div class="card surface" v-if="auth.user">
      <div class="header">
        <div class="avatar" :style="auth.user.avatarUrl ? `background-image:url(${auth.user.avatarUrl})` : ''">
          {{ auth.user.avatarUrl ? '' : auth.user.username?.[0]?.toUpperCase() }}
        </div>
        <div>
          <h1>{{ auth.user.username }}</h1>
          <p class="muted">{{ auth.user.email }}</p>
        </div>
      </div>
      <div class="stats">
        <div>
          <div class="value">{{ auth.user.followers ?? 0 }}</div>
          <div class="label">Người theo dõi</div>
        </div>
        <div>
          <div class="value">{{ auth.user.reviewCount ?? 0 }}</div>
          <div class="label">Bài review</div>
        </div>
        <div>
          <div class="value">{{ (auth.user.rating ?? 0).toFixed(1) }}</div>
          <div class="label">Đánh giá</div>
        </div>
      </div>
      <div class="actions">
        <button class="primary" @click="goCreate">Tạo bài review</button>
        <RouterLink class="ghost" to="/reviews/new">Soạn ngay</RouterLink>
      </div>
    </div>

    <div v-else class="card surface empty">
      <h2>Bạn chưa đăng nhập</h2>
      <p class="muted">Vui lòng đăng nhập để xem hồ sơ của bạn.</p>
      <div class="actions">
        <RouterLink class="primary" to="/login">Đăng nhập</RouterLink>
        <RouterLink class="ghost" to="/register">Đăng ký</RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 40px 16px;
  background: var(--bg);
}

.card {
  width: min(720px, 100%);
  padding: 28px;
  border-radius: 20px;
  box-shadow: var(--shadow);
  display: grid;
  gap: 16px;
}

.header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #e8f2ff;
  background-size: cover;
  background-position: center;
  display: grid;
  place-items: center;
  font-weight: 800;
  font-size: 24px;
  color: #0d6efd;
}

h1 {
  margin: 0;
  font-size: 26px;
}

.muted {
  margin: 6px 0 0 0;
  color: var(--muted);
}

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
}

.value {
  font-size: 24px;
  font-weight: 800;
}

.label {
  color: var(--muted);
}

.actions {
  display: flex;
  gap: 10px;
}

.primary,
.ghost {
  padding: 12px 16px;
  border-radius: 12px;
  font-weight: 700;
  border: 1px solid var(--border);
  text-align: center;
}

.primary {
  background: linear-gradient(135deg, #0ea5e9, #2563eb);
  color: #fff;
  border: none;
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.25);
}

.ghost {
  background: #f7f9fc;
  color: #1f2a3d;
}

.empty {
  text-align: center;
}
</style>
