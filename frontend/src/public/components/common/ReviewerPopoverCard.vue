<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  name: string
  username?: string
  avatarUrl?: string
  bio?: string
  followers?: number
  reviewCount?: number
  rating?: number
}>()

const showStats = computed(
  () => props.followers !== undefined || props.reviewCount !== undefined || props.rating !== undefined
)
</script>

<template>
  <div class="popover-card">
    <div class="pop-header">
      <img class="avatar" :src="props.avatarUrl || 'https://as1.ftcdn.net/v2/jpg/16/50/75/40/1000_F_1650754099_NnbV1a2Cgvj26kogaurRePYoipRlFEao.jpg'" alt="avatar" />
      <div>
        <div class="pop-name">{{ props.name }}</div>
        <div v-if="props.username" class="pop-handle">@{{ props.username }}</div>
      </div>
    </div>
    <p class="pop-bio">{{ props.bio || 'Chưa có giới thiệu.' }}</p>
    <div v-if="showStats" class="pop-stats">
      <div v-if="props.followers !== undefined"><span class="value">{{ props.followers }}</span><span class="label">Followers</span></div>
      <div v-if="props.reviewCount !== undefined"><span class="value">{{ props.reviewCount }}</span><span class="label">Bài viết</span></div>
      <div v-if="props.rating !== undefined"><span class="value">{{ props.rating.toFixed(1) }}</span><span class="label">Rating</span></div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.popover-card {
  display: grid;
  gap: 8px;
}

.pop-header {
  display: grid;
  grid-template-columns: 46px 1fr;
  gap: 10px;
  align-items: center;
}

.avatar {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #eef2f7;
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
