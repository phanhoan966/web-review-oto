<script setup lang="ts">
export interface BrandItem {
  id: number
  name: string
  logoUrl?: string
}

defineProps<{ brands: BrandItem[] }>()
</script>

<template>
  <section class="widget surface">
    <header class="widget-header">Hãng xe nổi bật</header>
    <div class="brands">
      <div v-for="brand in brands" :key="brand.id" class="brand">
        <div class="logo">
          <img :src="brand.logoUrl" :alt="brand.name" />
        </div>
        <div class="name">{{ brand.name }}</div>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.widget {
  padding: 16px;
  border-radius: 18px;
  box-shadow: var(--shadow);
  background: var(--surface);
  border: 1px solid var(--border);
}

.widget-header {
  font-weight: 700;
  font-size: 16px;
  margin-bottom: 14px;
}

.brands {
  display: grid;
  gap: 10px;
}

.brand {
  display: grid;
  grid-template-columns: 56px 1fr;
  gap: 12px;
  align-items: center;
  padding: 8px;
  border-radius: 12px;
  background: var(--chip-bg);
  border: 1px solid var(--border);
}

.logo {
  width: 56px;
  height: 44px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  background: var(--surface);
  border: 1px solid var(--border);
  img {
    max-height: 30px;
    object-fit: contain;
  }
}

.name {
  font-weight: 600;
}
</style>
