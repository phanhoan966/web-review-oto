<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  page: number
  size: number
  total: number
  sizes?: number[]
}>()

const emit = defineEmits(['update:page', 'update:size'])

const sizes = computed(() => props.sizes || [5, 10, 20, 50])
const totalPages = computed(() => {
  if (!props.size) return 1
  return Math.max(1, Math.ceil(Math.max(props.total, 0) / props.size))
})
const currentPage = computed(() => Math.min(props.page + 1, totalPages.value))

function changePage(next: number) {
  const clamped = Math.min(Math.max(next, 0), totalPages.value - 1)
  emit('update:page', clamped)
}

function changeSize(next: number) {
  emit('update:size', next)
}
</script>

<template>
  <div class="pager">
    <div class="info">Trang {{ currentPage }} / {{ totalPages }}</div>
    <div class="controls">
      <button class="ghost" :disabled="page <= 0" @click="changePage(page - 1)">Trước</button>
      <button class="ghost" :disabled="page + 1 >= totalPages" @click="changePage(page + 1)">Tiếp</button>
      <select :value="size" @change="changeSize(Number(($event.target as HTMLSelectElement).value))">
        <option v-for="s in sizes" :key="s" :value="s">{{ s }} / trang</option>
      </select>
    </div>
  </div>
</template>

<style scoped lang="scss">
.pager {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  color: var(--text);
}

.info {
  font-weight: 700;
}

.controls {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

button.ghost {
  border-radius: 10px;
  border: 1px solid var(--border);
  background: var(--chip-bg);
  color: var(--text);
  padding: 8px 12px;
  cursor: pointer;
}

button.ghost:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

select {
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--text);
}
</style>
