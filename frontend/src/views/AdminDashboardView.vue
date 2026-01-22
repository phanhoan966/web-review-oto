<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()

const kpis = [
  { label: 'Bài chờ duyệt', value: 12, trend: '+3 hôm nay' },
  { label: 'Bài đã đăng', value: 248, trend: '+18 tuần này' },
  { label: 'Người dùng mới', value: 54, trend: '+9 hôm nay' },
  { label: 'Báo cáo vi phạm', value: 4, trend: 'Cần xử lý' }
]

const topReviews = [
  { title: 'SUV điện 2025', author: 'Minh Pham', views: 18234, likes: 1220, status: 'APPROVED' },
  { title: 'Sedan hạng D', author: 'Lan Anh', views: 14220, likes: 980, status: 'APPROVED' },
  { title: 'Crossover đô thị', author: 'Huy Tran', views: 9510, likes: 610, status: 'PENDING' },
  { title: 'Pickup địa hình', author: 'Khoa Le', views: 8733, likes: 540, status: 'APPROVED' }
]

const pending = [
  { title: 'Hybrid tiết kiệm', author: 'Bui Tam', submitted: 'Hôm nay', tags: ['Hybrid', 'C'] },
  { title: 'MPV gia đình', author: 'Nguyen Vy', submitted: 'Hôm qua', tags: ['Xăng', 'B'] },
  { title: 'Sport coupe', author: 'Hoang Vu', submitted: '2 ngày trước', tags: ['Xăng', 'Hiệu năng'] }
]

const traffic = [72, 64, 88, 91, 76, 82, 95]

const welcome = computed(() => auth.user?.username || 'Admin')

function goPending() {
  router.push({ name: 'feed' })
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
              <p class="muted">{{ item.author }}</p>
            </div>
            <div class="stats">
              <span>👁 {{ item.views }}</span>
              <span>❤ {{ item.likes }}</span>
              <span class="pill" :class="item.status.toLowerCase()">{{ item.status }}</span>
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
        <div v-for="item in pending" :key="item.title" class="row pending">
          <div>
            <p class="title">{{ item.title }}</p>
            <p class="muted">{{ item.author }} · {{ item.submitted }}</p>
            <div class="tags">
              <span v-for="t in item.tags" :key="t" class="tag">{{ t }}</span>
            </div>
          </div>
          <div class="actions">
            <button class="approve">Duyệt</button>
            <button class="reject">Từ chối</button>
          </div>
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
  background: #f7f9fc;
  color: #1f2937;
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
}

.kpi .card h2 {
  margin: 0;
  font-size: 30px;
}

.label {
  margin: 0;
  color: #1f2937;
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
  background: #f9fbff;
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
  background: #eef2ff;
  color: #1f2a3d;
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
