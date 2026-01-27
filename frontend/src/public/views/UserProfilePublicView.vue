<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import client from '../../api/client'
import ReviewCard, { type ReviewCardData } from '../components/ReviewCard.vue'
import type { ReviewerItem } from '../components/Sidebar/TopReviewers.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const profile = ref<ReviewerItem | null>(null)
const profileLoading = ref(false)
const profileError = ref('')
const reviews = ref<ReviewCardData[]>([])
const listLoading = ref(false)
const listError = ref('')
const page = ref(0)
const size = 6
const hasMore = ref(true)
const followHover = ref(false)
const followState = ref<{ following: boolean; followers: number }>({ following: false, followers: 0 })

const username = computed(() => String(route.params.username || ''))
const isSelf = computed(() => (auth.user?.id && profile.value?.id ? auth.user.id === profile.value.id : false))
const followLabel = computed(() =>
  followState.value.following ? (followHover.value ? 'Unfollow' : 'Following') : 'Follow'
)
const followClass = computed(() => {
  if (!followState.value.following) return 'primary'
  return followHover.value ? 'danger' : 'following'
})
const displayedFollowers = computed(() => followState.value.followers ?? profile.value?.followers ?? 0)

onMounted(() => {
  loadProfile()
  loadReviews(true)
  window.addEventListener('scroll', handleScroll)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})

watch(
  () => route.params.username,
  () => {
    resetState()
    loadProfile()
    loadReviews(true)
  }
)

function resetState() {
  profile.value = null
  profileError.value = ''
  reviews.value = []
  listError.value = ''
  page.value = 0
  hasMore.value = true
  followHover.value = false
  followState.value = { following: false, followers: 0 }
}

async function loadProfile() {
  profileLoading.value = true
  profileError.value = ''
  try {
    const { data } = await client.get<ReviewerItem>(`/users/username/${username.value}`)
    profile.value = data
    followState.value = { following: Boolean(data.following), followers: data.followers ?? 0 }
  } catch (error: any) {
    profileError.value = error.response?.data?.message || 'Không tải được hồ sơ'
  } finally {
    profileLoading.value = false
  }
}

async function loadReviews(reset = false) {
  if (listLoading.value) return
  if (reset) {
    reviews.value = []
    page.value = 0
    hasMore.value = true
  }
  if (!hasMore.value) return
  listLoading.value = true
  listError.value = ''
  try {
    const { data } = await client.get(`/users/username/${username.value}/reviews`, { params: { page: page.value, size } })
    const items = data.reviews || data.content || []
    reviews.value = reset ? items : [...reviews.value, ...items]
    if (items.length < size) {
      hasMore.value = false
    } else {
      page.value += 1
    }
    if (!reviews.value.length) {
      listError.value = 'Chưa có bài viết nào'
    }
  } catch (error: any) {
    listError.value = error.response?.data?.message || 'Không tải được bài viết'
  } finally {
    listLoading.value = false
  }
}

function handleScroll() {
  if (listLoading.value || !hasMore.value) return
  const nearBottom = window.innerHeight + window.scrollY >= document.body.offsetHeight - 200
  if (nearBottom) {
    loadReviews()
  }
}

async function toggleFollow() {
  if (!profile.value?.id) return
  if (isSelf.value) return
  if (!auth.user) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  const current = { ...followState.value }
  const following = !current.following
  const followers = Math.max(0, current.followers + (following ? 1 : -1))
  followState.value = { following, followers }
  if (profile.value) {
    profile.value.followers = followers
    profile.value.following = following
  }
  try {
    if (following) {
      await client.post(`/users/${profile.value.id}/follow`)
    } else {
      await client.post(`/users/${profile.value.id}/unfollow`)
    }
  } catch (error: any) {
    followState.value = current
    if (profile.value) {
      profile.value.followers = current.followers
      profile.value.following = current.following
    }
  }
}
</script>

<template>
  <div class="container page">
    <section class="profile-card surface">
      <div class="heading">
        <div class="avatar" :style="profile?.avatarUrl ? `background-image:url(${profile.avatarUrl})` : ''">
          {{ profile?.avatarUrl ? '' : profile?.displayName?.[0]?.toUpperCase() || '' }}
        </div>
        <div class="intro">
          <p class="eyebrow">Hồ sơ reviewer</p>
          <h1>{{ profile?.displayName || 'Đang tải...' }}</h1>
          <p class="muted">Khám phá các bài review từ thành viên này.</p>
        </div>
        <button
          v-if="profile && !isSelf"
          class="follow-btn"
          :class="followClass"
          type="button"
          @click="toggleFollow"
          @mouseenter="followHover = true"
          @mouseleave="followHover = false"
        >
          {{ followLabel }}
        </button>
      </div>
      <div v-if="profileLoading" class="status">Đang tải hồ sơ...</div>
      <div v-else-if="profileError" class="status error">{{ profileError }}</div>
      <div v-else class="stats">
        <div>
          <div class="value">{{ displayedFollowers }}</div>
          <div class="label">Người theo dõi</div>
        </div>
        <div>
          <div class="value">{{ profile?.reviewCount ?? 0 }}</div>
          <div class="label">Bài review</div>
        </div>
        <div>
          <div class="value">{{ profile?.rating?.toFixed(1) || '5.0' }}</div>
          <div class="label">Đánh giá</div>
        </div>
      </div>
    </section>

    <section class="reviews surface">
      <header class="reviews-head">
        <div>
          <p class="eyebrow">Bài viết</p>
          <h3>Review từ {{ profile?.displayName || 'thành viên' }}</h3>
        </div>
        <div class="pill total">Tổng: {{ profile ? profile.reviewCount ?? 0 : '-' }}</div>
      </header>
      <div class="list">
        <ReviewCard v-for="item in reviews" :key="item.id" :review="item" />
        <div v-if="listLoading" class="status">Đang tải bài viết...</div>
        <div v-else-if="listError" class="status muted">{{ listError }}</div>
        <div v-else-if="!reviews.length" class="status muted">Chưa có bài viết nào</div>
        <div v-else-if="!hasMore" class="status muted">Đã hết bài viết</div>
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: var(--bg);
  padding: 32px 16px 48px;
  display: grid;
  gap: 18px;
  margin: 0 auto;
}

.profile-card {
  border-radius: 20px;
  box-shadow: var(--shadow);
  padding: 20px;
  display: grid;
  gap: 12px;
}

.heading {
  display: flex;
  gap: 14px;
  align-items: center;
  justify-content: space-between;
}

.intro {
  flex: 1;
}

.avatar {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: linear-gradient(135deg, #dbeafe, #eff6ff);
  background-size: cover;
  background-position: center;
  display: grid;
  place-items: center;
  font-size: 30px;
  font-weight: 800;
  color: #1d4ed8;
}

.eyebrow {
  margin: 0;
  color: var(--muted);
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  font-size: 12px;
}

h1 {
  margin: 4px 0 0 0;
  font-size: 28px;
}

h3 {
  margin: 4px 0 0 0;
}

.muted {
  color: var(--muted);
}

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.value {
  font-size: 24px;
  font-weight: 800;
}

.label {
  color: var(--muted);
}

.reviews {
  border-radius: 20px;
  box-shadow: var(--shadow);
  padding: 18px;
  display: grid;
  gap: 14px;
}

.reviews-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.list {
  display: grid;
  gap: 14px;
}

.status {
  padding: 12px 14px;
  border-radius: 12px;
  background: var(--surface);
  border: 1px solid var(--border);
  font-weight: 700;
}

.status.error {
  color: #b91c1c;
  border-color: #fecdd3;
  background: #fff1f2;
}

.pill.total {
  background: var(--chip-bg);
  border-radius: 999px;
  padding: 8px 12px;
  font-weight: 700;
}

.follow-btn {
  border: 1px solid var(--border);
  border-radius: 999px;
  padding: 8px 16px;
  background: var(--pill-bg);
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.follow-btn.primary {
  color: var(--text);
}

.follow-btn.following {
  border-color: var(--primary);
  color: var(--primary);
}

.follow-btn.danger {
  border-color: #d72638;
  color: #d72638;
  background: #ffecec;
}

@media (max-width: 720px) {
  .heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .avatar {
    width: 72px;
    height: 72px;
  }

  .reviews-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
