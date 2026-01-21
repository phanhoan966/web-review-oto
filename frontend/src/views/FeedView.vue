<script setup lang="ts">
import { onMounted, ref } from 'vue'
import FilterChips from '../components/FilterChips.vue'
import ReviewCard, { type ReviewCardData } from '../components/ReviewCard.vue'
import TopReviewers, { type ReviewerItem } from '../components/Sidebar/TopReviewers.vue'
import FeaturedBrands, { type BrandItem } from '../components/Sidebar/FeaturedBrands.vue'
import MostViewed, { type ViewedItem } from '../components/Sidebar/MostViewed.vue'
import client from '../api/client'

const reviews = ref<ReviewCardData[]>([])
const reviewers = ref<ReviewerItem[]>([])
const brands = ref<BrandItem[]>([])
const mostViewed = ref<ViewedItem[]>([])
const loading = ref(false)

const fallbackReviews: ReviewCardData[] = [
  {
    id: 1,
    title: 'Honda Civic 2022 Review',
    excerpt: 'Đánh giá Honda Civic 2022 với thiết kế thể thao, tiết kiệm nhiên liệu và tính năng an toàn vượt trội.',
    heroImageUrl: 'https://images.unsplash.com/photo-1503736334956-4c8f8e92946d?auto=format&fit=crop&w=1400&q=80',
    authorName: 'Username',
    authorAvatar: 'https://images.unsplash.com/photo-1502764613149-7f1d229e2305?auto=format&fit=crop&w=200&q=60',
    brand: 'Honda',
    likes: 24,
    commentsCount: 12,
    views: 900,
    publishedAt: new Date().toISOString()
  },
  {
    id: 2,
    title: '2024 Toyota Camry Review',
    excerpt: 'Khám phá Toyota Camry 2024 với nội thất sang trọng, trải nghiệm lái mượt mà và độ tin cậy cao.',
    heroImageUrl: 'https://images.unsplash.com/photo-1503736334956-4c8f8e92946d?auto=format&fit=crop&w=1400&q=80',
    authorName: 'AutoReviewerVN',
    authorAvatar: 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=200&q=60',
    brand: 'Toyota',
    likes: 32,
    commentsCount: 15,
    views: 850,
    publishedAt: new Date().toISOString()
  },
  {
    id: 3,
    title: 'Ford Ranger 2023 Review',
    excerpt: 'Hiệu suất mạnh mẽ và bền bỉ với khả năng địa hình vượt trội.',
    heroImageUrl: 'https://images.unsplash.com/photo-1502877828070-33b167ad6860?auto=format&fit=crop&w=1400&q=80',
    authorName: 'TruckExpert',
    authorAvatar: 'https://images.unsplash.com/photo-1527980965255-d3b416303d12?auto=format&fit=crop&w=200&q=60',
    brand: 'Ford',
    likes: 28,
    commentsCount: 18,
    views: 760,
    publishedAt: new Date().toISOString()
  }
]

const fallbackReviewers: ReviewerItem[] = [
  { id: 1, displayName: 'Enak', avatarUrl: 'https://images.unsplash.com/photo-1527980965255-d3b416303d12?auto=format&fit=crop&w=200&q=60', followers: 85, rating: 4.8, reviewCount: 48 },
  { id: 2, displayName: 'Alone100', avatarUrl: 'https://images.unsplash.com/photo-1502764613149-7f1d229e2305?auto=format&fit=crop&w=200&q=60', followers: 50, rating: 4.7, reviewCount: 50 },
  { id: 3, displayName: 'toetkoihil999', avatarUrl: 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=200&q=60', followers: 32, rating: 4.6, reviewCount: 32 }
]

const fallbackBrands: BrandItem[] = [
  { id: 1, name: 'Ford', logoUrl: 'https://upload.wikimedia.org/wikipedia/commons/3/3e/Ford_logo_flat.svg' },
  { id: 2, name: 'Honda', logoUrl: 'https://upload.wikimedia.org/wikipedia/commons/7/7b/Honda_Logo.svg' },
  { id: 3, name: 'Toyota', logoUrl: 'https://upload.wikimedia.org/wikipedia/commons/9/9d/Toyota_carlogo.svg' }
]

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const [reviewsRes, reviewersRes, brandsRes, viewedRes] = await Promise.all([
      client.get('/reviews', { params: { page: 0, size: 5 } }),
      client.get('/reviewers/top', { params: { limit: 3 } }),
      client.get('/brands/featured'),
      client.get('/reviews/most-viewed', { params: { limit: 3 } })
    ])
    reviews.value = reviewsRes.data.reviews?.length ? reviewsRes.data.reviews : fallbackReviews
    reviewers.value = reviewersRes.data?.length ? reviewersRes.data : fallbackReviewers
    brands.value = brandsRes.data?.length ? brandsRes.data : fallbackBrands
    mostViewed.value = viewedRes.data?.length ? viewedRes.data : fallbackReviews
  } catch (error) {
    reviews.value = fallbackReviews
    reviewers.value = fallbackReviewers
    brands.value = fallbackBrands
    mostViewed.value = fallbackReviews
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="container">
    <section class="hero-bar">
      <div>
        <p class="eyebrow">Cộng đồng đánh giá xe</p>
        <h1>Đánh Giá Xe Nổi Bật</h1>
      </div>
      <div class="filters">
        <FilterChips @change="loadData" />
      </div>
    </section>

    <section class="layout">
      <div class="main-column">
        <div class="stack">
          <ReviewCard v-for="review in reviews" :key="review.id" :review="review" />
          <div v-if="loading" class="loading">Đang tải...</div>
        </div>
      </div>
      <aside class="sidebar">
        <TopReviewers :reviewers="reviewers" title="Top Reviewers" />
        <FeaturedBrands :brands="brands" />
        <MostViewed :items="mostViewed" />
        <FeaturedBrands :brands="brands" />
      </aside>
    </section>
  </div>
</template>

<style scoped lang="scss">
.hero-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.eyebrow {
  margin: 0;
  color: var(--muted);
  font-weight: 600;
  letter-spacing: 0.02em;
}

h1 {
  margin: 4px 0 0 0;
  font-size: 28px;
  letter-spacing: -0.02em;
}

.layout {
  display: grid;
  grid-template-columns: 1.8fr 1fr;
  gap: 18px;
}

.main-column .stack {
  display: grid;
  gap: 16px;
}

.sidebar {
  display: grid;
  gap: 12px;
}

.loading {
  text-align: center;
  color: var(--muted);
  padding: 12px 0;
}

@media (max-width: 1024px) {
  .layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 680px) {
  .hero-bar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
