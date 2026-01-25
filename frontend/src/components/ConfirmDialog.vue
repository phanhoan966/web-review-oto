<script setup lang="ts">
import { onBeforeUnmount, onMounted } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: 'Xác nhận' },
  message: { type: String, default: '' },
  confirmText: { type: String, default: 'OK' },
  cancelText: { type: String, default: 'Hủy' },
  showClose: { type: Boolean, default: true },
  stackedActions: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

function close() {
  emit('update:modelValue', false)
  emit('cancel')
}

function confirm() {
  emit('confirm')
}

function onKey(event: KeyboardEvent) {
  if (!props.modelValue) return
  if (event.key === 'Escape') {
    close()
  }
}

onMounted(() => window.addEventListener('keydown', onKey))
onBeforeUnmount(() => window.removeEventListener('keydown', onKey))
</script>

<template>
  <Teleport to="body">
    <div v-if="modelValue" class="confirm-overlay" @click.self="close">
      <div class="confirm-card">
        <button v-if="showClose" class="close" type="button" aria-label="Đóng" @click="close">×</button>
        <p class="eyebrow">Xác nhận</p>
        <h3>{{ title }}</h3>
        <p v-if="message" class="message">{{ message }}</p>
        <div class="slot" v-else>
          <slot />
        </div>
        <div class="actions" :class="{ stacked: stackedActions }">
          <button class="ghost" type="button" @click="close">{{ cancelText }}</button>
          <button class="primary" type="button" @click="confirm">{{ confirmText }}</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped lang="scss">
.confirm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(4px);
  display: grid;
  place-items: center;
  z-index: 50;
  padding: 16px;
}

.confirm-card {
  position: relative;
  background: var(--surface);
  color: var(--text);
  border-radius: 18px;
  padding: 22px;
  max-width: 420px;
  width: min(100%, 420px);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.18);
  border: 1px solid var(--border);
  display: grid;
  gap: 12px;
}

.eyebrow {
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-weight: 700;
  font-size: 12px;
  color: var(--muted);
}

h3 {
  margin: 0;
  font-size: 22px;
}

.message {
  margin: 0;
  color: var(--muted);
  line-height: 1.5;
}

.slot {
  color: var(--muted);
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.actions.stacked {
  flex-direction: column-reverse;
  align-items: stretch;
}

.primary,
.ghost {
  border-radius: 12px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  padding: 10px 14px;
  transition: transform 120ms ease, box-shadow 120ms ease;
}

.primary {
  background: linear-gradient(135deg, var(--accent), var(--primary));
  color: #fff;
  box-shadow: 0 10px 25px rgba(33, 113, 255, 0.25);
}

.ghost {
  background: var(--chip-bg);
  color: var(--text);
  border: 1px solid var(--border);
}

.primary:active,
.ghost:active {
  transform: translateY(1px);
}

.close {
  position: absolute;
  top: 12px;
  right: 12px;
  background: transparent;
  border: none;
  font-size: 18px;
  color: var(--muted);
  cursor: pointer;
}

@media (max-width: 540px) {
  .confirm-card {
    padding: 18px;
  }
  .actions {
    flex-direction: column-reverse;
    align-items: stretch;
  }
}
</style>
