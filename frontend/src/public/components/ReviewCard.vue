<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { slugify } from '../utils/slugify'
import { buildAssetUrl } from '../utils/assetUrl'

export interface ReviewCardData {
  id: number
  title: string
  excerpt: string
  heroImageUrl: string
  authorId?: number
  authorName: string
  authorAvatar?: string
  authorUsername?: string
  brand?: string
  vehicleModel?: string
  vehicleYear?: number
  likes?: number
  commentsCount?: number
  views?: number
  publishedAt?: string
}

const props = defineProps<{ review: ReviewCardData }>()

const meta = computed(() => {
  const brand = props.review.brand
  const modelYear = props.review.vehicleYear ? `${props.review.vehicleYear}` : ''
  return [brand, modelYear].filter(Boolean).join(' • ')
})

const detailPath = computed(() => `/post/${slugify(props.review.title) || 'bai-viet'}/${props.review.id}`)

const profilePath = computed(() =>
  props.review.authorUsername ? `/user/${encodeURIComponent(props.review.authorUsername)}` : ''
)

const authorTooltip = computed(() => {
  const username = props.review.authorUsername
  const name = props.review.authorName
  const handle = username ? `@${username}` : ''
  return [name, handle, 'Nhấn để xem hồ sơ'].filter(Boolean).join(' • ')
})

const heroSrc = computed(() => buildAssetUrl(props.review.heroImageUrl))

const relativeTime = computed(() => formatRelativeTime(props.review.publishedAt))

function formatRelativeTime(value?: string) {
  if (!value) return ''
  const now = new Date().getTime()
  const date = new Date(value).getTime()
  const diff = Math.max(0, now - date)
  const hours = Math.floor(diff / 3600000)
  if (hours < 24) {
    return `${hours || 1} giờ trước`
  }
  const days = Math.floor(hours / 24)
  return `${days} ngày trước`
}
</script>

<template>
  <article class="card surface">
    <RouterLink class="hero" :to="detailPath">
      <img :src="heroSrc" :alt="review.title" />
    </RouterLink>
    <div class="content">
      <RouterLink class="title-link" :to="detailPath"><h2>{{ review.title }}</h2></RouterLink>
      <div class="author">
        <RouterLink v-if="profilePath" class="avatar-link" :to="profilePath" :title="authorTooltip">
          <img class="avatar" :src="review.authorAvatar || 'https://as1.ftcdn.net/v2/jpg/16/50/75/40/1000_F_1650754099_NnbV1a2Cgvj26kogaurRePYoipRlFEao.jpg'" alt="avatar" />
        </RouterLink>
        <div v-else class="avatar-link" :title="authorTooltip">
          <img class="avatar" :src="review.authorAvatar || 'https://as1.ftcdn.net/v2/jpg/16/50/75/40/1000_F_1650754099_NnbV1a2Cgvj26kogaurRePYoipRlFEao.jpg'" alt="avatar" />
        </div>
        <div>
          <RouterLink v-if="profilePath" class="name-link" :to="profilePath" :title="authorTooltip">{{ review.authorName }}</RouterLink>
          <div v-else class="name">{{ review.authorName }}</div>
          <div class="sub">{{ meta }} {{ relativeTime ? `• ${relativeTime}` : '' }}</div>
        </div>
      </div>
      <p class="excerpt">{{ review.excerpt }}</p>
      <div class="metrics">
        <div class="pill">❤ {{ review.likes ?? 0 }}</div>
        <div class="pill">💬 {{ review.commentsCount ?? 0 }}</div>
        <div class="pill">👁 {{ review.views ?? 0 }} views</div>
      </div>
    </div>
  </article>
</template>

<style scoped lang="scss">
.card {
  padding: 18px;
  border-radius: 24px;
  display: grid;
  gap: 14px;
  box-shadow: var(--shadow);
}

.hero {
  overflow: hidden;
  border-radius: 18px;
  height: 240px;
  display: block;
}

.hero img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.card:hover .hero img {
  transform: scale(1.02);
}

.title-link {
  text-decoration: none;
  color: inherit;
}

.content h2 {
  margin: 0 0 6px 0;
  font-size: 22px;
  letter-spacing: -0.01em;
}

.author {
  display: grid;
  grid-template-columns: 46px 1fr;
  gap: 10px;
  align-items: center;
}

.avatar-link {
  display: block;
  text-decoration: none;
}

.avatar {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #eef2f7;
}

.name {
  font-weight: 600;
  color: inherit;
  text-decoration: none;
}

.name-link {
  font-weight: 600;
  color: inherit;
  text-decoration: none;
}

.name-link:hover {
  text-decoration: underline;
}

.sub {
  color: var(--muted);
  font-size: 13px;
}

.excerpt {
  color: #2f3c4f;
  font-size: 15px;
  line-height: 1.6;
  margin: 10px 0 6px 0;
}

.metrics {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.pill {
  background: var(--chip-bg);
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 13px;
  color: #1f2a3d;
}
</style>
