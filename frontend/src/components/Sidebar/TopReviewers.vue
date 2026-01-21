<script setup lang="ts">
export interface ReviewerItem {
  id: number
  displayName: string
  avatarUrl?: string
  followers?: number
  rating?: number
  reviewCount?: number
}

defineProps<{ title?: string; reviewers: ReviewerItem[] }>()
</script>

<template>
  <section class="widget surface">
    <header class="widget-header">{{ title || 'Top Reviewers' }}</header>
    <div class="list">
      <div v-for="reviewer in reviewers" :key="reviewer.id" class="row">
        <div class="avatar-wrap">
          <img :src="reviewer.avatarUrl || 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=60'" alt="avatar" />
        </div>
        <div class="info">
          <div class="name">{{ reviewer.displayName }}</div>
          <div class="meta">{{ reviewer.followers || 0 }} followers • ⭐ {{ reviewer.rating?.toFixed(1) || '4.5' }}</div>
        </div>
        <span class="count">{{ reviewer.reviewCount || 0 }}</span>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.widget {
  padding: 16px;
  border-radius: 18px;
  box-shadow: var(--shadow);
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

.row {
  display: grid;
  grid-template-columns: 52px 1fr auto;
  gap: 10px;
  align-items: center;
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
}

.meta {
  color: var(--muted);
  font-size: 13px;
}

.count {
  color: #f4b000;
  font-weight: 700;
}
</style>
