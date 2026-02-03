<script setup lang="ts">
const props = defineProps<{
  name: string
  logoUrl?: string
  reviewCount?: number
}>()
</script>

<template>
  <div class="brand-popover">
    <div class="header">
      <div class="logo" :style="props.logoUrl ? `background-image:url(${props.logoUrl})` : ''"></div>
      <div>
        <div class="title">{{ props.name }}</div>
        <div class="meta">{{ props.reviewCount ?? 0 }} bài viết</div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.brand-popover {
  display: grid;
  gap: 10px;
}

.header {
  display: grid;
  grid-template-columns: 52px 1fr;
  gap: 12px;
  align-items: center;
}

.logo {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: var(--chip-bg);
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  border: 1px solid var(--border);
}

.title {
  font-weight: 700;
}

.meta {
  color: var(--muted);
  font-size: 13px;
}
</style>
