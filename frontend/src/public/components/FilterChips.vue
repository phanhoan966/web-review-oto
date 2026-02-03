<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useUiStore } from '../../stores/ui'

const ui = useUiStore()
const { filters } = storeToRefs(ui)

const emit = defineEmits<{ (e: 'change', value: string): void }>()

function onSelect(value: string) {
  ui.toggleFilter(value)
  emit('change', value)
}
</script>

<template>
  <div class="chips">
    <button v-for="chip in filters" :key="chip.value" class="chip" :class="{ active: chip.active }" @click="onSelect(chip.value)">
      {{ chip.label }}
    </button>
  </div>
</template>

<style scoped lang="scss">
.chips {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.chip {
  border: 1px solid var(--border);
  background: var(--chip-bg);
  border-radius: 999px;
  padding: 10px 14px;
  font-weight: 600;
  color: var(--muted);
  cursor: pointer;
  transition: all 0.2s ease;
}

.chip.active {
  background: linear-gradient(135deg, #0ea5e9, #2563eb);
  color: white;
  border-color: transparent;
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.28);
}

.chip:hover {
  transform: translateY(-1px);
}
</style>
