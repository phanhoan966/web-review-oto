<script setup lang="ts">
import { RouterLink } from 'vue-router'
import HoverPopover from '../common/HoverPopover.vue'

export interface ReviewerItem {
  id: number
  displayName: string
  username?: string
  avatarUrl?: string
  followers?: number
  rating?: number
  reviewCount?: number
  bio?: string
}

defineProps<{ title?: string; reviewers: ReviewerItem[] }>()
</script>

<template>
  <section class="widget surface">
    <header class="widget-header">{{ title || 'Top Reviewers' }}</header>
    <div class="list">
      <HoverPopover v-for="reviewer in reviewers" :key="reviewer.id" class="row-wrap">
        <template #trigger>
          <RouterLink
            class="row"
            :to="`/user/${encodeURIComponent(reviewer.username || reviewer.displayName)}`"
          >
            <div class="avatar-wrap">
              <img :src="reviewer.avatarUrl || 'https://as1.ftcdn.net/v2/jpg/16/50/75/40/1000_F_1650754099_NnbV1a2Cgvj26kogaurRePYoipRlFEao.jpg'" alt="avatar" />
            </div>
            <div class="info">
              <div class="name">{{ reviewer.displayName }}</div>
              <div class="meta">{{ reviewer.followers || 0 }} followers • ⭐ {{ reviewer.rating?.toFixed(1) || '4.5' }}</div>
            </div>
            <span class="count">{{ reviewer.reviewCount || 0 }}</span>
          </RouterLink>
        </template>
        <div class="popover-card">
          <div class="pop-header">
            <img class="avatar" :src="reviewer.avatarUrl || 'https://as1.ftcdn.net/v2/jpg/16/50/75/40/1000_F_1650754099_NnbV1a2Cgvj26kogaurRePYoipRlFEao.jpg'" alt="avatar" />
            <div>
              <div class="pop-name">{{ reviewer.displayName }}</div>
              <div class="pop-handle">@{{ reviewer.username || reviewer.displayName }}</div>
            </div>
          </div>
          <p class="pop-bio">{{ reviewer.bio || 'Chưa có giới thiệu.' }}</p>
          <div class="pop-stats">
            <div><span class="value">{{ reviewer.followers || 0 }}</span><span class="label">Followers</span></div>
            <div><span class="value">{{ reviewer.reviewCount || 0 }}</span><span class="label">Bài viết</span></div>
            <div><span class="value">{{ reviewer.rating?.toFixed(1) || '5.0' }}</span><span class="label">Rating</span></div>
          </div>
        </div>
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
  grid-template-columns: 52px 1fr auto;
  gap: 10px;
  align-items: center;
  text-decoration: none;
  color: inherit;
  padding: 6px 4px;
  border-radius: 12px;
  transition: background 0.2s ease;
}

.row:hover {
  background: var(--chip-bg);
}

.row:hover .name {
  text-decoration: underline;
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
