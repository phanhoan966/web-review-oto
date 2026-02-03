<script setup lang="ts">
import { ref } from 'vue'

const open = ref(false)

function show() {
  open.value = true
}

function hide() {
  open.value = false
}
</script>

<template>
  <div class="hover-popover" @mouseenter="show" @mouseleave="hide" @focusin="show" @focusout="hide">
    <div class="trigger">
      <slot name="trigger" />
    </div>
    <div class="panel" :class="{ visible: open }">
      <slot />
    </div>
  </div>
</template>

<style scoped lang="scss">
.hover-popover {
  position: relative;
  width: 100%;
}

.trigger {
  width: 100%;
}

.panel {
  position: absolute;
  left: 0;
  top: 100%;
  margin-top: 6px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 12px;
  box-shadow: 0 18px 38px rgba(0, 0, 0, 0.12);
  padding: 12px;
  min-width: 260px;
  z-index: 5;
  opacity: 0;
  pointer-events: none;
  transform: translateY(0);
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.panel.visible {
  opacity: 1;
  pointer-events: auto;
  transform: translateY(4px);
}
</style>
