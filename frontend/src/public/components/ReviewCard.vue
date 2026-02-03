<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import client from '../../api/client'
import { slugify } from '../utils/slugify'
import { buildAssetUrl } from '../utils/assetUrl'
import HoverPopover from './common/HoverPopover.vue'
import ReviewerPopoverCard from './common/ReviewerPopoverCard.vue'

export interface ReviewCardData {
  id: number
  slug?: string
  title: string
  excerpt: string
  heroImageUrl: string
  authorId?: number
  authorName: string
  authorAvatar?: string
  authorUsername?: string
  authorFollowers?: number
  authorReviewCount?: number
  authorRating?: number
  authorBio?: string
  authorFollowing?: boolean
  brand?: string
  vehicleModel?: string
  vehicleYear?: number
  likes?: number
  liked?: boolean
  commentsCount?: number
  views?: number
  publishedAt?: string
}

const props = defineProps<{ review: ReviewCardData }>()

const auth = useAuthStore()
const router = useRouter()
const likeState = ref({ count: props.review.likes ?? 0, liked: Boolean(props.review.liked) })
const followState = ref({ following: Boolean(props.review.authorFollowing), followers: props.review.authorFollowers ?? 0 })
const followHover = ref(false)

watch(
  () => props.review,
  (val) => {
    likeState.value = { count: val.likes ?? 0, liked: Boolean(val.liked) }
    followState.value = { following: Boolean(val.authorFollowing), followers: val.authorFollowers ?? 0 }
  }
)

const meta = computed(() => {
  const brand = props.review.brand
  const modelYear = props.review.vehicleYear ? `${props.review.vehicleYear}` : ''
  return [brand, modelYear].filter(Boolean).join(' • ')
})

const detailPath = computed(() => {
  const slug = props.review.slug || slugify(props.review.title)
  return `/post/${slug || 'bai-viet'}`
})

const profilePath = computed(() =>
  props.review.authorUsername ? `/user/${encodeURIComponent(props.review.authorUsername)}` : ''
)

const isAuthorSelf = computed(() => {
  if (!auth.user || !props.review.authorId) return false
  return auth.user.id === props.review.authorId
})

const followLabel = computed(() =>
  followState.value.following ? (followHover.value ? 'Unfollow' : 'Following') : 'Follow'
)

const followClass = computed(() => {
  if (!followState.value.following) return 'primary'
  return followHover.value ? 'danger' : 'following'
})

const authorTooltip = computed(() => {
  const username = props.review.authorUsername
  const name = props.review.authorName
  const handle = username ? `@${username}` : ''
  const followers = followState.value.followers ?? props.review.authorFollowers ?? 0
  const posts = props.review.authorReviewCount ?? 0
  const rating = props.review.authorRating ?? 0
  const summary = `${followers} follower${followers === 1 ? '' : 's'} • ${posts} bài • ⭐ ${rating ? rating.toFixed(1) : '5.0'}`
  return [name, handle, summary].filter(Boolean).join(' • ')
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

async function toggleLike() {
  if (!auth.user) {
    router.push({ name: 'login', query: { redirect: detailPath.value } })
    return
  }
  const current = likeState.value
  const liked = !current.liked
  const count = Math.max(0, current.count + (liked ? 1 : -1))
  likeState.value = { count, liked }
  try {
    if (liked) {
      await client.post(`/reviews/${props.review.id}/like`)
    } else {
      await client.post(`/reviews/${props.review.id}/unlike`)
    }
  } catch (error) {
    likeState.value = current
  }
}

async function toggleFollow() {
  if (!props.review.authorId) return
  if (isAuthorSelf.value) return
  if (!auth.user) {
    router.push({ name: 'login', query: { redirect: detailPath.value } })
    return
  }
  const current = { ...followState.value }
  const following = !current.following
  const followers = Math.max(0, current.followers + (following ? 1 : -1))
  followState.value = { following, followers }
  try {
    if (following) {
      await client.post(`/users/${props.review.authorId}/follow`)
    } else {
      await client.post(`/users/${props.review.authorId}/unfollow`)
    }
  } catch (error) {
    followState.value = current
  }
}
</script>

<template>
  <article class="card surface">
    <div class="content">
      <div class="author" :class="{ hoverable: !!profilePath }">
        <HoverPopover v-if="profilePath">
          <template #trigger>
            <div class="author-trigger">
              <RouterLink class="avatar-link" :to="profilePath" :title="authorTooltip">
                <img class="avatar" :src="review.authorAvatar || 'https://as1.ftcdn.net/v2/jpg/16/50/75/40/1000_F_1650754099_NnbV1a2Cgvj26kogaurRePYoipRlFEao.jpg'" alt="avatar" />
              </RouterLink>
              <div class="author-meta">
                <RouterLink class="name-link" :to="profilePath" :title="authorTooltip">{{ review.authorName }}</RouterLink>
                <div class="sub">{{ meta }} {{ relativeTime ? `• ${relativeTime}` : '' }}</div>
              </div>
            </div>
          </template>
          <ReviewerPopoverCard
            :name="review.authorName"
            :username="review.authorUsername"
            :avatar-url="review.authorAvatar"
            :bio="review.authorBio"
            :followers="followState.followers ?? review.authorFollowers"
            :review-count="review.authorReviewCount"
            :rating="review.authorRating"
          />
        </HoverPopover>
        <div v-else class="author-trigger" :title="authorTooltip">
          <div class="avatar-link">
            <img class="avatar" :src="review.authorAvatar || 'https://as1.ftcdn.net/v2/jpg/16/50/75/40/1000_F_1650754099_NnbV1a2Cgvj26kogaurRePYoipRlFEao.jpg'" alt="avatar" />
          </div>
          <div class="author-meta">
            <div class="name">{{ review.authorName }}</div>
            <div class="sub">{{ meta }} {{ relativeTime ? `• ${relativeTime}` : '' }}</div>
          </div>
        </div>
        <button
          v-if="review.authorId && !isAuthorSelf"
          class="follow-btn"
          :class="followClass"
          type="button"
          @click.stop="toggleFollow"
          @mouseenter="followHover = true"
          @mouseleave="followHover = false"
        >
          {{ followLabel }}
        </button>
      </div>
      <RouterLink class="title-link" :to="detailPath">
        <h2>{{ review.title }}</h2>
        <RouterLink class="hero" :to="detailPath">
          <img v-if="heroSrc" :src="heroSrc" :alt="review.title" />
        </RouterLink>
        <i><p class="excerpt">{{ review.excerpt }}</p></i>
      </RouterLink>
      <div class="metrics">
        <button class="pill like-btn" :class="{ liked: likeState.liked }" type="button" @click.stop="toggleLike">❤ {{ likeState.count }}</button>
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
  height: auto;
  display: block;
}

.hero.placeholder {
  background: linear-gradient(135deg, #f6f8fc, #e5ebf7);
  border: 1px dashed #d5deec;
}

.hero img {
  width: 100%;
  height: 300px;
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

.content {
  overflow: hidden;
}

.author {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.author-trigger {
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
  font-size: 13px;
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
  color: var(--text);
}

.like-btn {
  border: 1px solid var(--border);
  cursor: pointer;
}

.like-btn.liked {
  border-color: var(--primary);
  color: var(--primary);
}

.follow-btn {
  border: 1px solid var(--border);
  border-radius: 999px;
  padding: 8px 14px;
  background: var(--pill-bg);
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  min-width: 108px;
  display: inline-flex;
  justify-content: center;
  align-items: center;
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
</style>
