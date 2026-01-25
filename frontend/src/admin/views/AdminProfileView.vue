<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { buildAssetUrl } from '../../public/utils/assetUrl'

const auth = useAuthStore()

const user = computed(() => auth.user)
const avatarSrc = computed(() => (user.value?.avatarUrl ? buildAssetUrl(user.value.avatarUrl) : ''))
const avatarInitial = computed(() => (user.value?.username || 'A').charAt(0).toUpperCase())
const rolesText = computed(() => (user.value?.roles || []).join(', ') || 'Quản trị')

onMounted(() => {
  auth.ensureHydrated()
})
</script>

<template>
  <div class="page">
    <div class="panel" v-if="user">
      <div class="header">
        <div class="avatar" :class="{ hasImage: avatarSrc }">
          <img v-if="avatarSrc" :src="avatarSrc" alt="Avatar" />
          <span v-else>{{ avatarInitial }}</span>
        </div>
        <div class="info">
          <p class="eyebrow">Hồ sơ quản trị</p>
          <h1>{{ user.username }}</h1>
          <p class="muted">{{ user.email }}</p>
          <div class="chips">
            <span class="chip">ID: {{ user.id }}</span>
            <span class="chip">{{ rolesText }}</span>
          </div>
        </div>
      </div>

      <div class="grid">
        <div class="card">
          <p class="label">Bài viết</p>
          <h2>{{ user.reviewCount ?? 0 }}</h2>
          <span class="muted">Tổng số bài đã tạo</span>
        </div>
        <div class="card">
          <p class="label">Đánh giá</p>
          <h2>{{ (user.rating ?? 0).toFixed(1) }}</h2>
          <span class="muted">Điểm uy tín</span>
        </div>
        <div class="card">
          <p class="label">Người theo dõi</p>
          <h2>{{ user.followers ?? 0 }}</h2>
          <span class="muted">Tổng lượt theo dõi</span>
        </div>
      </div>
    </div>

    <div v-else class="panel empty">
      <h2>Không tìm thấy thông tin</h2>
      <p class="muted">Vui lòng đăng nhập lại.</p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.page {
  padding: 20px 16px 40px;
  background: var(--bg);
}

.panel {
  background: var(--surface);
  border-radius: 20px;
  padding: 24px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
  display: grid;
  gap: 20px;
}

.header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #e5e7eb;
  display: grid;
  place-items: center;
  font-size: 28px;
  font-weight: 800;
  color: #1f2937;
  overflow: hidden;
}

.avatar.hasImage {
  background: transparent;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info h1 {
  margin: 4px 0;
  font-size: 28px;
  letter-spacing: -0.02em;
}

.muted {
  color: var(--muted);
  margin: 2px 0 0;
}

.eyebrow {
  margin: 0;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-weight: 700;
  font-size: 12px;
}

.chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 10px;
}

.chip {
  padding: 8px 10px;
  border-radius: 12px;
  border: 1px solid var(--border);
  background: var(--chip-bg, #f7f9fc);
  font-weight: 700;
  font-size: 13px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.card {
  padding: 16px;
  border-radius: 16px;
  border: 1px solid var(--border);
  background: var(--surface);
  box-shadow: var(--shadow);
  display: grid;
  gap: 6px;
}

.label {
  margin: 0;
  color: var(--muted);
  font-weight: 700;
  font-size: 13px;
}

h2 {
  margin: 0;
  font-size: 26px;
}

.empty {
  text-align: center;
}
</style>
