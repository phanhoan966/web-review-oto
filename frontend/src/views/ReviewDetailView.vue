<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import client from '../api/client'

interface ReviewDetail {
  id: number
  title: string
  excerpt: string
  content: string
  heroImageUrl: string
  brand?: string
  vehicleModel?: string
  vehicleYear?: number
  fuelType?: string
  priceSegment?: string
  likes?: number
  commentsCount?: number
  views?: number
  publishedAt?: string
  authorName?: string
  authorAvatar?: string
}

const route = useRoute()
const review = ref<ReviewDetail | null>(null)
const loading = ref(false)
const errorMsg = ref('')

onMounted(load)

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const { data } = await client.get(`/reviews/${route.params.id}`)
    review.value = data
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || 'Không tìm thấy bài viết'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="container page">
    <div v-if="loading" class="status">Đang tải...</div>
    <div v-else-if="errorMsg" class="status error">{{ errorMsg }}</div>
    <article v-else-if="review" class="content-card surface">
      <div class="hero" v-if="review.heroImageUrl">
        <img :src="review.heroImageUrl" :alt="review.title" />
      </div>
      <div class="head">
        <p class="eyebrow">{{ review.brand || 'Xe' }}</p>
        <h1>{{ review.title }}</h1>
        <div class="meta">
          <div class="author">
            <img class="avatar" :src="review.authorAvatar || 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=60'" alt="avatar" />
            <span>{{ review.authorName }}</span>
          </div>
          <div class="chips">
            <span v-if="review.vehicleYear" class="chip">{{ review.vehicleYear }}</span>
            <span v-if="review.fuelType" class="chip">{{ review.fuelType }}</span>
            <span v-if="review.priceSegment" class="chip">{{ review.priceSegment }}</span>
          </div>
        </div>
      </div>
      <p class="excerpt">{{ review.excerpt }}</p>
      <div class="body" v-html="review.content" />
      <div class="metrics">
        <span>❤ {{ review.likes ?? 0 }}</span>
        <span>💬 {{ review.commentsCount ?? 0 }}</span>
        <span>👁 {{ review.views ?? 0 }} lượt xem</span>
      </div>
    </article>
  </div>
</template>

<style scoped lang="scss">
.page {
  padding-top: 20px;
}

.content-card {
  padding: 18px;
  border-radius: 20px;
  box-shadow: var(--shadow);
  display: grid;
  gap: 14px;
}

.hero {
  border-radius: 18px;
  overflow: hidden;
  max-height: 420px;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.head {
  display: grid;
  gap: 8px;
}

.eyebrow {
  margin: 0;
  color: var(--muted);
  font-weight: 600;
  letter-spacing: 0.02em;
}

h1 {
  margin: 0;
  font-size: 28px;
  letter-spacing: -0.02em;
}

.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chip {
  padding: 6px 10px;
  border-radius: 12px;
  background: var(--chip-bg);
  color: #1f2a3d;
  font-weight: 600;
}

.excerpt {
  margin: 0;
  font-size: 16px;
  color: #2f3c4f;
}

.body {
  color: #1f2a3d;
  line-height: 1.7;
  white-space: pre-wrap;
}

.metrics {
  display: flex;
  gap: 16px;
  color: var(--muted);
  font-weight: 600;
}

.status {
  text-align: center;
  color: var(--muted);
  padding: 24px 0;
}

.error {
  color: #b91c1c;
}
</style>
