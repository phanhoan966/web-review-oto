<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import client from '../../api/client'

interface ReviewItem {
  id: number
  title: string
  authorName?: string
  views?: number
  likes?: number
  status?: string
  fuelType?: string
  priceSegment?: string
  publishedAt?: string
}

const auth = useAuthStore()
const router = useRouter()

const pending = ref<ReviewItem[]>([])
const topReviews = ref<ReviewItem[]>([])
const publishedTotal = ref(0)
const pendingTotal = ref(0)
const userCount = ref(0)
const loading = ref(false)
const errorMsg = ref('')

const kpis = computed(() => [
  { label: 'Bài chờ duyệt', value: pendingTotal.value, trend: 'Đang chờ' },
  { label: 'Bài đã đăng', value: publishedTotal.value, trend: 'Hiện tại' },
  { label: 'Người dùng', value: userCount.value, trend: '' },
  { label: 'Top views', value: topReviews.value[0]?.views || 0, trend: '' }
])

const traffic = [72, 64, 88, 91, 76, 82, 95]

const welcome = computed(() => auth.user?.username || 'Admin')

onMounted(load)

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    await Promise.all([fetchPending(), fetchTop(), fetchPublishedTotal(), fetchUsers()])
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || 'Không tải được dữ liệu admin'
  } finally {
    loading.value = false
  }
}

async function fetchPending() {
  const { data } = await client.get('/admin/reviews/pending', { params: { page: 0, size: 5 } })
  pending.value = (data.reviews || []).map((r: any) => ({
    id: r.id,
    title: r.title,
    authorName: r.authorName,
    fuelType: r.fuelType,
    priceSegment: r.priceSegment,
    status: r.status,
    publishedAt: r.publishedAt
  }))
  pendingTotal.value = data.total || 0
}

async function fetchTop() {
  const { data } = await client.get('/admin/reviews/most-viewed', { params: { limit: 5 } })
  topReviews.value = (data || []).map((r: any) => ({
    id: r.id,
    title: r.title,
    authorName: r.authorName,
    views: r.views,
    likes: r.likes,
    status: r.status
  }))
}

async function fetchPublishedTotal() {
  const { data } = await client.get('/admin/reviews', { params: { page: 0, size: 1, status: 'APPROVED' } })
  publishedTotal.value = data.total || 0
}

async function fetchUsers() {
  const { data } = await client.get('/admin/users')
  userCount.value = data?.length || 0
}

function goPending() {
  router.push({ name: 'admin-posts' })
}
</script>

<template>
  <div class="dashboard">
    <header class="hero">
      <div>
        <p class="eyebrow">Bảng điều khiển</p>
        <h1>Chào, {{ welcome }}</h1>
        <p class="sub">Giám sát bài viết, duyệt nhanh, và theo dõi tương tác.</p>
      </div>
      <div class="hero-actions">
        <button class="primary" @click="goPending">Xem bài chờ duyệt</button>
        <button class="ghost" @click="router.push({ name: 'review-create' })">Tạo bài</button>
      </div>
    </header>

    <section class="grid kpi">
      <div v-for="card in kpis" :key="card.label" class="card surface">
        <p class="label">{{ card.label }}</p>
        <h2>{{ card.value }}</h2>
        <span class="muted">{{ card.trend }}</span>
      </div>
    </section>

    <section class="grid two-cols">
      <div class="card surface">
        <div class="card-head">
          <h3>Hiệu suất 7 ngày</h3>
          <span class="muted">Lượt xem</span>
        </div>
        <div class="bars">
          <div v-for="(v, idx) in traffic" :key="idx" class="bar" :style="{ height: `${v}%` }"></div>
        </div>
      </div>
      <div class="card surface">
        <div class="card-head">
          <h3>Tốp bài nổi bật</h3>
          <span class="muted">Lượt xem & yêu thích</span>
        </div>
        <div class="list">
          <div v-for="item in topReviews" :key="item.title" class="row">
            <div>
              <p class="title">{{ item.title }}</p>
              <p class="muted">{{ item.authorName }}</p>
            </div>
            <div class="stats">
              <span>👁 {{ item.views ?? 0 }}</span>
              <span>❤ {{ item.likes ?? 0 }}</span>
              <span class="pill" :class="(item.status || 'PENDING').toLowerCase()">{{ item.status || 'PENDING' }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="card surface">
      <div class="card-head">
        <h3>Hàng chờ duyệt</h3>
        <button class="ghost" @click="goPending">Xem tất cả</button>
      </div>
      <div class="list">
        <div v-for="item in pending" :key="item.id" class="row pending">
          <div>
            <p class="title">{{ item.title }}</p>
            <p class="muted">{{ item.authorName || 'Ẩn danh' }} · {{ item.status || 'PENDING' }}</p>
            <div class="tags">
              <span v-for="t in [item.fuelType, item.priceSegment].filter(Boolean)" :key="t" class="tag">{{ t }}</span>
            </div>
          </div>
          <div class="pill pending">Chờ duyệt</div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.dashboard {
  display: grid;
  gap: 20px;
  padding: 20px 16px 40px;
  background: var(--bg);
}

.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.eyebrow {
  margin: 0;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-weight: 700;
  font-size: 12px;
}

h1 {
  margin: 4px 0;
  font-size: 28px;
  letter-spacing: -0.02em;
}

.sub {
  margin: 0;
  color: var(--muted);
}

.hero-actions {
  display: flex;
  gap: 10px;
}

.primary,
.ghost,
.approve,
.reject,
button {
  border-radius: 12px;
  font-weight: 700;
}

.primary {
  border: none;
  padding: 12px 16px;
  background: linear-gradient(135deg, #0ea5e9, #2563eb);
  color: #fff;
  box-shadow: 0 12px 28px rgba(37, 99, 235, 0.3);
}

.ghost {
  border: 1px solid var(--border);
  padding: 12px 16px;
  background: var(--chip-bg);
  color: var(--text);
}

.grid.kpi {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
}

.grid.two-cols {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 14px;
}

.card {
  border-radius: 20px;
  box-shadow: var(--shadow);
  padding: 18px;
  display: grid;
  gap: 12px;
  background: var(--surface);
  border: 1px solid var(--border);
  color: var(--text);
}

.kpi .card h2 {
  margin: 0;
  font-size: 30px;
}

.label {
  margin: 0;
  color: var(--text);
  font-weight: 700;
}

.muted {
  color: var(--muted);
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bars {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  align-items: end;
  height: 160px;
}

.bar {
  width: 100%;
  border-radius: 10px;
  background: linear-gradient(180deg, #2563eb, #0ea5e9);
}

.list {
  display: grid;
  gap: 10px;
}

.row {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: var(--chip-bg);
}

.title {
  margin: 0;
  font-weight: 700;
}

.stats {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.pill {
  padding: 6px 10px;
  border-radius: 10px;
  font-weight: 700;
}

.pill.approved {
  background: #e8f8f0;
  color: #0f9d58;
}

.pill.pending {
  background: #fff4e5;
  color: #c47c00;
}

.tags {
  display: flex;
  gap: 6px;
  margin-top: 6px;
  flex-wrap: wrap;
}

.tag {
  background: var(--chip-bg);
  color: var(--text);
  padding: 6px 10px;
  border-radius: 10px;
  font-weight: 600;
}

.pending .actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.approve {
  background: #0ea5e9;
  color: #fff;
  border: none;
  padding: 10px 14px;
}

.reject {
  background: #fee2e2;
  color: #b91c1c;
  border: none;
  padding: 10px 14px;
}

@media (max-width: 840px) {
  .hero {
    flex-direction: column;
    align-items: flex-start;
  }
  .hero-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}
</style>
