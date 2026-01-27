<script setup lang="ts">
import { RouterLink, useRouter } from 'vue-router'
import { ref, watch } from 'vue'
import { useAuthStore } from '../../../stores/auth'
import client from '../../../api/client'
import HoverPopover from '../common/HoverPopover.vue'
import ReviewerPopoverCard from '../common/ReviewerPopoverCard.vue'

export interface ReviewerItem {
  id: number
  displayName: string
  username?: string
  avatarUrl?: string
  followers?: number
  rating?: number
  reviewCount?: number
  bio?: string
  following?: boolean
}

const props = defineProps<{ title?: string; reviewers: ReviewerItem[] }>()

const auth = useAuthStore()
const router = useRouter()
const hoverIds = ref<Set<number>>(new Set())
const followState = ref<Record<number, { following: boolean; followers: number }>>({})

watch(
  () => props.reviewers,
  (list) => {
    const next: Record<number, { following: boolean; followers: number }> = {}
    list.forEach((item) => {
      next[item.id] = { following: Boolean(item.following), followers: item.followers ?? 0 }
    })
    followState.value = next
  },
  { immediate: true }
)

function followLabel(id: number) {
  const state = followState.value[id]
  const hovering = hoverIds.value.has(id)
  if (!state?.following) return 'Follow'
  return hovering ? 'Unfollow' : 'Following'
}

function followClass(id: number) {
  const state = followState.value[id]
  if (!state?.following) return 'primary'
  return hoverIds.value.has(id) ? 'danger' : 'following'
}

async function toggleFollow(id: number) {
  if (!auth.user) {
    router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
    return
  }
  if (auth.user.id === id) return
  const current = followState.value[id] || { following: false, followers: 0 }
  const following = !current.following
  const followers = Math.max(0, current.followers + (following ? 1 : -1))
  followState.value = { ...followState.value, [id]: { following, followers } }
  try {
    if (following) {
      await client.post(`/users/${id}/follow`)
    } else {
      await client.post(`/users/${id}/unfollow`)
    }
  } catch (error) {
    followState.value = { ...followState.value, [id]: current }
  }
}
</script>

<template>
  <section class="widget surface">
    <header class="widget-header">{{ title || 'Top Reviewers' }}</header>
    <div class="list">
      <HoverPopover v-for="reviewer in reviewers" :key="reviewer.id" class="row-wrap">
        <template #trigger>
          <div class="row">
            <RouterLink
              class="row-link"
              :to="`/user/${encodeURIComponent(reviewer.username || reviewer.displayName)}`"
            >
              <div class="avatar-wrap">
                <img :src="reviewer.avatarUrl || 'https://as1.ftcdn.net/v2/jpg/16/50/75/40/1000_F_1650754099_NnbV1a2Cgvj26kogaurRePYoipRlFEao.jpg'" alt="avatar" />
              </div>
              <div class="info">
                <div class="name">{{ reviewer.displayName }}</div>
                <div class="meta">{{ followState[reviewer.id]?.followers ?? reviewer.followers ?? 0 }} followers • ⭐ {{ reviewer.rating?.toFixed(1) || '4.5' }}</div>
              </div>
              <span class="count">{{ reviewer.reviewCount || 0 }}</span>
            </RouterLink>
            <button
              v-if="auth.user?.id !== reviewer.id"
              class="follow-btn"
              :class="followClass(reviewer.id)"
              type="button"
              @mouseenter="hoverIds = new Set([...hoverIds, reviewer.id])"
              @mouseleave="hoverIds = new Set([...Array.from(hoverIds)].filter((i) => i !== reviewer.id))"
              @click.stop="toggleFollow(reviewer.id)"
            >
              {{ followLabel(reviewer.id) }}
            </button>
          </div>
        </template>
        <ReviewerPopoverCard
          :name="reviewer.displayName"
          :username="reviewer.username"
          :avatar-url="reviewer.avatarUrl"
          :bio="reviewer.bio"
          :followers="followState[reviewer.id]?.followers ?? reviewer.followers"
          :review-count="reviewer.reviewCount"
          :rating="reviewer.rating"
        />
      </HoverPopover>
    </div>
  </section>
</template>

<style scoped lang="scss">
.widget {
  padding: 16px;
  border-radius: 18px;
  box-shadow: var(--shadow);
  margin-bottom: 15px;
}

.widget-header {
  font-weight: 700;
  font-size: 16px;
  margin-bottom: 14px;
}

.list {
  display: grid;
  gap: 12px;
}

.row-wrap {
  width: 100%;
}

.row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 6px 4px;
  border-radius: 12px;
  transition: background 0.2s ease;
}

.row:hover {
  background: var(--chip-bg);
}

.row-link {
  display: grid;
  grid-template-columns: 52px 1fr auto;
  gap: 10px;
  align-items: center;
  text-decoration: none;
  color: inherit;
}

.row:hover .name {
  text-decoration: underline;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.avatar-wrap {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #eef2f7;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.name {
  font-weight: 600;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meta {
  color: var(--muted);
  font-size: 13px;
}

.count {
  color: #f4b000;
  font-weight: 700;
}

.follow-btn {
  border: 1px solid var(--border);
  border-radius: 999px;
  padding: 6px 12px;
  background: var(--pill-bg);
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
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

.popover-card {
  display: grid;
  gap: 8px;
}

.pop-header {
  display: grid;
  grid-template-columns: 52px 1fr;
  gap: 10px;
  align-items: center;
}

.pop-name {
  font-weight: 700;
}

.pop-handle {
  color: var(--muted);
  font-size: 13px;
}

.pop-bio {
  margin: 8px 0 10px 0;
  color: var(--muted);
  font-size: 13px;
}

.pop-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.pop-stats .value {
  display: block;
  font-weight: 800;
}

.pop-stats .label {
  display: block;
  color: var(--muted);
  font-size: 12px;
}
</style>
