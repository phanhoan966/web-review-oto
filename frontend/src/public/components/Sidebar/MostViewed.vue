<script setup lang="ts">
import { RouterLink, useRouter } from 'vue-router'
import { ref, watch } from 'vue'
import { useAuthStore } from '../../../stores/auth'
import client from '../../../api/client'
import { slugify } from '../../utils/slugify'
import { buildAssetUrl } from '../../utils/assetUrl'

export interface ViewedItem {
  id: number
  slug?: string
  title: string
  heroImageUrl: string
  likes?: number
  liked?: boolean
  commentsCount?: number
  views?: number
  rating?: number
}

const props = defineProps<{ items: ViewedItem[] }>()

const auth = useAuthStore()
const router = useRouter()
const likeState = ref<Record<number, { count: number; liked: boolean }>>({})

watch(
  () => props.items,
  (list) => {
    const next: Record<number, { count: number; liked: boolean }> = {}
    list.forEach((item) => {
      next[item.id] = { count: item.likes ?? 0, liked: Boolean(item.liked) }
    })
    likeState.value = next
  },
  { immediate: true }
)

function heroSrc(item: ViewedItem) {
  return buildAssetUrl(item.heroImageUrl)
}

async function toggleLike(id: number) {
  if (!auth.user) {
    router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
    return
  }
  const current = likeState.value[id] || { count: 0, liked: false }
  const liked = !current.liked
  const count = Math.max(0, current.count + (liked ? 1 : -1))
  likeState.value = { ...likeState.value, [id]: { count, liked } }
  try {
    if (liked) {
      await client.post(`/reviews/${id}/like`)
    } else {
      await client.post(`/reviews/${id}/unlike`)
    }
  } catch (error) {
    likeState.value = { ...likeState.value, [id]: current }
  }
}
</script>

<template>
  <section class="widget surface">
    <header class="widget-header">Xem nhiều nhất</header>
    <div class="items">
      <div v-for="item in props.items" :key="item.id" class="row">
        <RouterLink class="thumb-link" :to="`/post/${item.slug || slugify(item.title) || 'bai-viet'}`">
          <img v-if="heroSrc(item)" class="thumb" :src="heroSrc(item)" :alt="item.title" />
          <div v-else class="thumb placeholder" aria-hidden="true"></div>
        </RouterLink>
        <div class="info">
          <RouterLink class="title" :to="`/post/${item.slug || slugify(item.title) || 'bai-viet'}`">{{ item.title }}</RouterLink>
          <div class="meta">
            <button class="pill like-btn" :class="{ liked: likeState[item.id]?.liked }" type="button" @click.stop="toggleLike(item.id)">❤ {{ likeState[item.id]?.count ?? item.likes ?? 0 }}</button>
            <span>• 💬 {{ item.commentsCount || 0 }} • ⭐ {{ item.rating || '4.6' }}</span>
          </div>
        </div>
      </div>
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

.items {
  display: grid;
  gap: 12px;
}

.row {
  display: grid;
  grid-template-columns: 80px 1fr;
  gap: 10px;
  align-items: center;
}

.thumb {
  width: 80px;
  height: 56px;
  object-fit: cover;
  border-radius: 10px;
}

.thumb.placeholder {
  background: linear-gradient(135deg, #f6f8fc, #e5ebf7);
  border: 1px dashed #d5deec;
}

.title {
  font-weight: 600;
  margin-bottom: 4px;
  display: inline-block;
}

.meta {
  color: var(--muted);
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.like-btn {
  border: 1px solid var(--border);
  border-radius: 999px;
  padding: 4px 10px;
  background: var(--chip-bg);
  cursor: pointer;
  color: inherit;
}

.like-btn.liked {
  border-color: var(--primary);
  color: var(--primary);
}

.row {
  overflow: hidden;
}
</style>
