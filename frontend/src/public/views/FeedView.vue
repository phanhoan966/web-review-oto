<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import FilterChips from '../components/FilterChips.vue'
import ReviewCard, { type ReviewCardData } from '../components/ReviewCard.vue'
import TopReviewers, { type ReviewerItem } from '../components/Sidebar/TopReviewers.vue'
import FeaturedBrands, { type BrandItem } from '../components/Sidebar/FeaturedBrands.vue'
import MostViewed, { type ViewedItem } from '../components/Sidebar/MostViewed.vue'
import client from '../../api/client'

const route = useRoute()
const router = useRouter()

const reviews = ref<ReviewCardData[]>([])
const reviewers = ref<ReviewerItem[]>([])
const brands = ref<BrandItem[]>([])
const mostViewed = ref<ViewedItem[]>([])
const loading = ref(false)
const errorMsg = ref('')
const page = ref(0)
const initialSize = 3
const nextSize = 5
const hasMore = ref(true)
const brandParam = computed(() => (route.params.brand ? String(route.params.brand) : null))
const searchQuery = computed(() => (route.query.q ? String(route.query.q) : ''))
const isSearch = computed(() => route.name === 'search' || !!searchQuery.value)
const selectedBrand = ref<string | null>(brandParam.value)
const total = ref(0)
const heroEyebrow = computed(() => (isSearch.value ? 'Tìm kiếm' : 'Cộng đồng đánh giá xe'))
const heroTitle = computed(() => {
  if (isSearch.value) {
    return searchQuery.value.trim() ? `Kết quả cho “${searchQuery.value.trim()}”` : 'Tìm bài đánh giá'
  }
  return 'Đánh Giá Xe Nổi Bật'
})
const heroNote = computed(() => {
  if (isSearch.value) {
    if (!searchQuery.value.trim()) return 'Nhập từ khóa để bắt đầu tìm kiếm'
    if (loading.value) return 'Đang tìm kiếm...'
    if (!total.value) return 'Không tìm thấy bài viết khớp từ khóa'
    return `${total.value} kết quả sắp xếp theo độ liên quan`
  }
  return 'Khám phá bài viết mới nhất từ cộng đồng'
})

onMounted(() => {
  loadData(true)
  window.addEventListener('scroll', handleScroll)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})

watch(brandParam, (val) => {
  selectedBrand.value = val
  loadData(true)
})

watch(searchQuery, () => {
  selectedBrand.value = null
  loadData(true)
})

async function loadData(reset = false) {
  if (loading.value) return
  if (reset) {
    page.value = 0
    hasMore.value = true
    reviews.value = []
    total.value = 0
  }
  if (isSearch.value && !searchQuery.value.trim()) {
    hasMore.value = false
    errorMsg.value = 'Nhập từ khóa để tìm kiếm'
    return
  }
  if (!hasMore.value) return

  loading.value = true
  errorMsg.value = ''
  try {
    const size = page.value === 0 ? initialSize : nextSize
    const params = isSearch.value
      ? { q: searchQuery.value, page: page.value, size }
      : { page: page.value, size, brand: selectedBrand.value || undefined }
    const [reviewsRes, reviewersRes, brandsRes, viewedRes] = await Promise.all([
      client.get(isSearch.value ? '/reviews/search' : '/reviews', { params }),
      client.get('/reviewers/top', { params: { limit: 3 } }),
      client.get('/brands/featured'),
      client.get('/reviews/most-viewed', { params: { limit: 3 } })
    ])
    const data = reviewsRes.data || {}
    const items = data.reviews || data.content || []
    const totalCount = data.total ?? (isSearch.value ? items.length : reviews.value.length + items.length)
    total.value = totalCount
    reviews.value = reset ? items : [...reviews.value, ...items]
    reviewers.value = reviewersRes.data || []
    brands.value = brandsRes.data || []
    mostViewed.value = viewedRes.data || []
    if (isSearch.value) {
      const loaded = reviews.value.length
      hasMore.value = loaded < totalCount && items.length === size
      if (hasMore.value) {
        page.value += 1
      }
    } else {
      if (items.length < size) {
        hasMore.value = false
      } else {
        page.value += 1
      }
    }
    if (!reviews.value.length) {
      errorMsg.value = isSearch.value ? 'Không tìm thấy kết quả phù hợp' : 'Chưa có bài viết nào.'
    }
  } catch (error) {
    errorMsg.value = 'Tải dữ liệu thất bại, vui lòng thử lại.'
    if (reset) {
      reviews.value = []
    }
  } finally {
    loading.value = false
  }
}

function onFilterChange() {
  loadData(true)
}

function handleScroll() {
  if (loading.value || !hasMore.value) return
  const nearBottom = window.innerHeight + window.scrollY >= document.body.offsetHeight - 200
  if (nearBottom) {
    loadData(false)
  }
}

function onBrandSelect(brand: BrandItem) {
  selectedBrand.value = brand.name
  router.push({ name: 'feed-brand', params: { brand: brand.name } })
}
</script>

<template>
  <div class="container">
    <section class="hero-bar">
      <div>
        <p class="eyebrow">{{ heroEyebrow }}</p>
        <h1>{{ heroTitle }}</h1>
        <p class="subline">{{ heroNote }}</p>
      </div>
      <div v-if="!isSearch" class="filters">
        <FilterChips @change="onFilterChange" />
      </div>
    </section>

    <section class="layout">
      <div class="main-column">
        <div class="stack">
          <ReviewCard v-for="review in reviews" :key="review.id" :review="review" />
          <div v-if="!loading && !reviews.length" class="empty">{{ errorMsg || 'Không có dữ liệu' }}</div>
          <div v-if="loading" class="loading">Đang tải...</div>
          <div v-if="!hasMore && reviews.length" class="end">Đã hết bài viết</div>
        </div>
      </div>
      <aside class="sidebar">
        <TopReviewers :reviewers="reviewers" title="Top Reviewers" />
        <FeaturedBrands :brands="brands" @select="onBrandSelect" />
        <MostViewed :items="mostViewed" />
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

.subline {
  margin: 6px 0 0;
  color: var(--muted);
}

.layout {
  display: grid;
  grid-template-columns: 2.5fr 1fr;
  gap: 18px;
}

.main-column .stack {
  display: grid;
  gap: 16px;
}

.sidebar {
  display: flow;
  gap: 12px;
}

.loading {
  text-align: center;
  color: var(--muted);
  padding: 12px 0;
}

.end {
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
