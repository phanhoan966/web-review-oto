<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { slugify } from '../../utils/slugify'
import { buildAssetUrl } from '../../utils/assetUrl'

export interface ViewedItem {
  id: number
  title: string
  heroImageUrl: string
  likes?: number
  commentsCount?: number
  views?: number
  rating?: number
}

const props = defineProps<{ items: ViewedItem[] }>()

function heroSrc(item: ViewedItem) {
  return buildAssetUrl(item.heroImageUrl)
}
</script>

<template>
  <section class="widget surface">
    <header class="widget-header">Xem nhiều nhất</header>
    <div class="items">
      <RouterLink v-for="item in props.items" :key="item.id" class="row" :to="`/post/${slugify(item.title) || 'bai-viet'}/${item.id}`">
        <img v-if="heroSrc(item)" class="thumb" :src="heroSrc(item)" :alt="item.title" />
        <div v-else class="thumb placeholder" aria-hidden="true"></div>
        <div class="info">
          <div class="title">{{ item.title }}</div>
          <div class="meta">
            ❤ {{ item.likes || 0 }} • 💬 {{ item.commentsCount || 0 }} • ⭐ {{ item.rating || '4.6' }}
          </div>
        </div>
      </RouterLink>
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
}

.meta {
  color: var(--muted);
  font-size: 13px;
}

.row {
  overflow: hidden;
}
</style>
