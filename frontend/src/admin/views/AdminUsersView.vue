<script setup lang="ts">
import { onMounted, ref } from 'vue'
import client from '../../api/client'

interface AdminUser {
  id: number
  username: string
  email: string
  roles: string[]
  followers?: number
  rating?: number
  reviewCount?: number
  createdAt?: string
}

const users = ref<AdminUser[]>([])
const loading = ref(false)
const errorMsg = ref('')

onMounted(load)

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const { data } = await client.get('/admin/users')
    users.value = data || []
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || 'Không tải được danh sách user'
    users.value = []
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="panel">
    <div class="panel-head">
      <div>
        <p class="eyebrow">Người dùng</p>
        <h2>Quản lý user</h2>
        <p class="muted">Phân quyền và theo dõi hoạt động.</p>
      </div>
      <div class="actions">
        <button class="ghost" @click="load" :disabled="loading">Làm mới</button>
      </div>
    </div>

    <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
    <p v-else-if="loading" class="muted">Đang tải...</p>

    <div v-else class="table">
      <div class="row head">
        <div>Họ tên</div>
        <div>Email</div>
        <div>Roles</div>
        <div>Theo dõi</div>
        <div>Bài viết</div>
        <div>Tham gia</div>
      </div>
      <div v-for="u in users" :key="u.id" class="row">
        <div class="name">{{ u.username }}</div>
        <div class="muted">{{ u.email }}</div>
        <div><span class="pill">{{ (u.roles || []).join(', ') }}</span></div>
        <div>{{ u.followers ?? 0 }}</div>
        <div>{{ u.reviewCount ?? 0 }}</div>
        <div class="muted">{{ u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '-' }}</div>
      </div>
      <div v-if="!users.length" class="row empty">Không có user</div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.panel {
  background: white;
  border-radius: 20px;
  padding: 20px;
  box-shadow: var(--shadow);
  display: grid;
  gap: 16px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
}

.eyebrow {
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-weight: 700;
  font-size: 12px;
  color: var(--muted);
}

h2 {
  margin: 6px 0 2px;
  letter-spacing: -0.02em;
}

.muted {
  color: var(--muted);
}

.error {
  color: #b91c1c;
  font-weight: 700;
}

.table {
  display: grid;
  gap: 10px;
}

.row {
  display: grid;
  grid-template-columns: 2fr 2fr 1.5fr 1fr 1fr 1.2fr;
  align-items: center;
  padding: 12px 10px;
  border-radius: 12px;
  background: #f8fafc;
}

.row.head {
  font-weight: 700;
  color: #111827;
  background: #eef2ff;
}

.row.empty {
  grid-template-columns: 1fr;
  justify-items: center;
  color: var(--muted);
}

.name {
  font-weight: 700;
}

.pill {
  padding: 6px 10px;
  border-radius: 999px;
  background: #eef2ff;
  color: #312e81;
  font-weight: 700;
  font-size: 13px;
}

.actions {
  display: flex;
  gap: 8px;
}

.ghost {
  border-radius: 12px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  padding: 10px 12px;
  background: #f1f5f9;
  color: #111827;
  border: 1px solid #e5e7eb;
}

.ghost:disabled {
  opacity: 0.6;
}

@media (max-width: 960px) {
  .row,
  .row.head {
    grid-template-columns: 1.6fr 1.8fr 1.4fr 1fr 1fr;
  }
}
</style>
