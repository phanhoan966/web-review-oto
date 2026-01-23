<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import client from '../../api/client'
import ReviewCard, { type ReviewCardData } from '../components/ReviewCard.vue'

const auth = useAuthStore()
const router = useRouter()
const reviews = ref<(ReviewCardData & { status?: string })[]>([])
const loading = ref(false)
const errorMsg = ref('')

onMounted(async () => {
  await auth.ensureHydrated()
  if (auth.isAuthenticated) {
    loadReviews()
  }
})

function goCreate() {
  router.push({ name: 'review-create' })
}

async function loadReviews() {
  loading.value = true
  errorMsg.value = ''
  try {
    const { data } = await client.get('/reviews/mine', { params: { page: 0, size: 12 } })
    reviews.value = data.reviews || []
    if (!reviews.value.length) {
      errorMsg.value = 'Bạn chưa có bài nào. Bắt đầu viết bài đầu tiên!'
    }
  } catch (error) {
    errorMsg.value = 'Không tải được danh sách bài viết.'
    reviews.value = []
  } finally {
    loading.value = false
  }
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

      <section class="mine">
        <div class="mine-head">
          <h3>Bài viết của bạn</h3>
          <span class="muted">Hiển thị tối đa 12 bài gần nhất</span>
        </div>
        <div v-if="loading" class="loading">Đang tải...</div>
        <div v-else-if="errorMsg" class="empty muted">{{ errorMsg }}</div>
        <div v-else class="list">
          <div v-for="item in reviews" :key="item.id" class="review-wrap">
            <ReviewCard :review="item" />
            <div class="status-pill" :class="item.status?.toLowerCase() || 'pending'">{{ item.status || 'PENDING' }}</div>
          </div>
        </div>
      </section>
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
  width: min(900px, 100%);
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

.mine .muted {
  font-size: 14px;
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

.mine {
  margin-top: 12px;
  display: grid;
  gap: 10px;
}

.mine-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.list {
  display: grid;
  gap: 14px;
}

.review-wrap {
  position: relative;
}

.status-pill {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 6px 10px;
  border-radius: 10px;
  font-weight: 700;
  font-size: 12px;
  text-transform: uppercase;
  background: #f4f4f5;
  color: #111827;
}

.status-pill.approved {
  background: #e0f7e9;
  color: #0f5132;
}

.status-pill.pending {
  background: #fff7ed;
  color: #9a3412;
}

.status-pill.rejected {
  background: #fef2f2;
  color: #b91c1c;
}

.loading {
  color: var(--muted);
}

.empty {
  text-align: center;
}
</style>
