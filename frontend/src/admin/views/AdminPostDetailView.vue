<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import client from '../../api/client'
import { buildAssetUrl } from '../../public/utils/assetUrl'
import { slugify } from '../../public/utils/slugify'

interface ReviewDetail {
  id: number
  title: string
  excerpt?: string
  content?: string
  heroImageUrl?: string
  brand?: string
  vehicleModel?: string
  vehicleYear?: number
  fuelType?: string
  priceSegment?: string
  likes?: number
  commentsCount?: number
  views?: number
  status?: string
  authorName?: string
  authorAvatar?: string
  createdAt?: string
  publishedAt?: string
}

const route = useRoute()
const router = useRouter()
const review = ref<ReviewDetail | null>(null)
const loading = ref(false)
const errorMsg = ref('')
const heroSrc = computed(() => buildAssetUrl(review.value?.heroImageUrl || ''))

onMounted(load)

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const { data } = await client.get(`/admin/reviews/${route.params.id}`)
    review.value = data
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || 'Không tải được bài viết'
  } finally {
    loading.value = false
  }
}

function formatDate(value?: string) {
  if (!value) return '-'
  return new Date(value).toLocaleString('vi-VN')
}

function goBack() {
  router.push({ name: 'admin-posts' })
}

function goPublic() {
  if (!review.value) return
  router.push({ name: 'review-detail', params: { slug: slugify(review.value.title || 'bai-viet'), id: review.value.id } })
}
</script>

<template>
  <div class="detail-page">
    <div class="head">
      <div>
        <p class="eyebrow">Chi tiết bài viết</p>
        <h2>{{ review?.title || 'Đang tải bài viết' }}</h2>
        <p class="muted">Xem nội dung bài viết trong khu vực quản trị.</p>
      </div>
      <div class="head-actions">
        <button class="ghost" type="button" @click="goBack">Quay lại</button>
        <button class="primary" type="button" :disabled="!review" @click="goPublic">Xem trang công khai</button>
      </div>
    </div>

    <div v-if="loading" class="status">Đang tải...</div>
    <div v-else-if="errorMsg" class="status error">{{ errorMsg }}</div>
    <div v-else-if="review" class="detail-layout">
      <article class="card surface main">
        <div class="meta-line">
          <span class="pill" :class="(review.status || 'PENDING').toLowerCase()">{{ review.status || 'PENDING' }}</span>
          <span class="muted">Tác giả: {{ review.authorName || 'Ẩn danh' }}</span>
          <span class="muted">Xuất bản: {{ formatDate(review.publishedAt) }}</span>
        </div>
        <h3 class="title">{{ review.title }}</h3>
        <p v-if="review.excerpt" class="excerpt">{{ review.excerpt }}</p>
        <div class="content" v-html="review.content"></div>
        <div class="hero" v-if="review.heroImageUrl">
          <img :src="heroSrc" :alt="review.title" />
        </div>
      </article>

      <aside class="side">
        <div class="card surface info-card">
          <h4>Thông tin</h4>
          <dl class="info">
            <div><dt>Tác giả</dt><dd>{{ review.authorName || 'Ẩn danh' }}</dd></div>
            <div><dt>Trạng thái</dt><dd><span class="pill" :class="(review.status || 'PENDING').toLowerCase()">{{ review.status || 'PENDING' }}</span></dd></div>
            <div><dt>Tạo lúc</dt><dd>{{ formatDate(review.createdAt) }}</dd></div>
            <div><dt>Xuất bản</dt><dd>{{ formatDate(review.publishedAt) }}</dd></div>
            <div><dt>Lượt xem</dt><dd>{{ review.views ?? 0 }}</dd></div>
            <div><dt>Thả tim</dt><dd>{{ review.likes ?? 0 }}</dd></div>
            <div><dt>Bình luận</dt><dd>{{ review.commentsCount ?? 0 }}</dd></div>
            <div><dt>Thương hiệu</dt><dd>{{ review.brand || '-' }}</dd></div>
            <div><dt>Mẫu xe</dt><dd>{{ review.vehicleModel || '-' }}</dd></div>
            <div><dt>Năm</dt><dd>{{ review.vehicleYear || '-' }}</dd></div>
            <div><dt>Nhiên liệu</dt><dd>{{ review.fuelType || '-' }}</dd></div>
            <div><dt>Phân khúc</dt><dd>{{ review.priceSegment || '-' }}</dd></div>
          </dl>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped lang="scss">
.detail-page {
  background: var(--bg);
  min-height: 100vh;
  padding: 20px 16px 40px;
  color: var(--text);
  display: grid;
  gap: 16px;
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.head h2 {
  margin: 4px 0;
  letter-spacing: -0.02em;
}

.eyebrow {
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-weight: 700;
  font-size: 12px;
  color: var(--muted);
}

.muted {
  color: var(--muted);
}

.head-actions {
  display: flex;
  gap: 8px;
}

.status {
  background: var(--surface);
  border: 1px solid var(--border);
  padding: 12px 16px;
  border-radius: 12px;
  font-weight: 700;
  color: var(--text);
}

.status.error {
  color: #b91c1c;
  border-color: #fecdd3;
  background: #fff1f2;
}

.detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 2fr) 360px;
  gap: 16px;
  align-items: start;
}

.card.surface {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 16px;
  box-shadow: var(--shadow);
}

.meta-line {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 10px;
}

.title {
  margin: 0 0 8px;
  font-size: 24px;
  letter-spacing: -0.01em;
}

.excerpt {
  margin: 0 0 12px;
  color: var(--muted);
}

.content {
  display: grid;
  gap: 10px;
  line-height: 1.6;
  color: var(--text);
}

.content :deep(p) {
  margin: 0;
}

.hero {
  margin-top: 16px;
  overflow: hidden;
  border-radius: 14px;
  border: 1px solid var(--border);
}

.hero img {
  width: 100%;
  display: block;
  object-fit: cover;
}

.side {
  display: grid;
  gap: 12px;
}

.info-card h4 {
  margin: 0 0 12px;
}

.info {
  display: grid;
  gap: 10px;
}

.info div {
  display: grid;
  grid-template-columns: 130px 1fr;
  align-items: center;
  gap: 8px;
}

.info dt {
  margin: 0;
  color: var(--muted);
  font-weight: 700;
}

.info dd {
  margin: 0;
  color: var(--text);
}

.pill {
  padding: 6px 10px;
  border-radius: 10px;
  font-weight: 700;
  font-size: 13px;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.pill.approved {
  background: #22c55e;
}

.pill.pending {
  background: #f59e0b;
}

.pill.rejected {
  background: #ef4444;
}

.primary,
.ghost {
  border-radius: 12px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  padding: 10px 12px;
}

.primary {
  background: linear-gradient(135deg, var(--accent), var(--primary));
  color: #fff;
}

.ghost {
  background: var(--chip-bg);
  color: var(--text);
  border: 1px solid var(--border);
}

.primary:disabled,
.ghost:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 1080px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .head {
    flex-direction: column;
    align-items: flex-start;
  }

  .head-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .info div {
    grid-template-columns: 1fr;
  }
}
</style>
