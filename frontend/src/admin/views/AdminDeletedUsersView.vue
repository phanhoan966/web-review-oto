<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import client from '../../api/client'
import PaginationBar from '../components/PaginationBar.vue'
import ConfirmDialog from '../../components/ConfirmDialog.vue'

interface AdminUser {
  id: number
  username: string
  email: string
  roles: string[]
  createdAt?: string
}

const filters = ref({ name: '', email: '' })
const allUsers = ref<AdminUser[]>([])
const users = ref<AdminUser[]>([])
const page = ref({ page: 0, size: 10, total: 0 })

const loading = ref(false)
const actionLoading = ref<number | null>(null)
const errorMsg = ref('')
const confirmVisible = ref(false)
const confirmTarget = ref<AdminUser | null>(null)

const roleLabels: Record<string, string> = {
  ROLE_SYSTEM_ADMIN: 'System Admin',
  ROLE_MANAGER: 'Manager',
  ROLE_ADMIN: 'Admin',
  ROLE_USER: 'User'
}

function clampPage(meta: { page: number; size: number; total: number }) {
  const totalPages = meta.size ? Math.max(1, Math.ceil(Math.max(meta.total, 0) / meta.size)) : 1
  const pageValue = Math.min(Math.max(meta.page, 0), totalPages - 1)
  return { ...meta, page: pageValue }
}

function apply() {
  const name = filters.value.name.trim().toLowerCase()
  const email = filters.value.email.trim().toLowerCase()
  const filtered = allUsers.value.filter((u) => {
    const matchesName = !name || (u.username || '').toLowerCase().includes(name)
    const matchesEmail = !email || (u.email || '').toLowerCase().includes(email)
    return matchesName && matchesEmail
  })
  const meta = clampPage({ ...page.value, total: filtered.length })
  const start = meta.page * meta.size
  page.value = meta
  users.value = filtered.slice(start, start + meta.size)
}

watch(
  filters,
  () => {
    page.value = { ...page.value, page: 0 }
    apply()
  },
  { deep: true }
)

onMounted(load)

function resetFilters() {
  filters.value = { name: '', email: '' }
}

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const { data } = await client.get('/admin/users/deleted')
    const list = Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : data?.users || []
    allUsers.value = list
    page.value = clampPage({ ...page.value, total: allUsers.value.length })
    apply()
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || 'Không tải được danh sách user đã xóa'
    allUsers.value = []
    users.value = []
  } finally {
    loading.value = false
  }
}

function changePage(target: number) {
  page.value = { ...page.value, page: target }
  apply()
}

function changeSize(size: number) {
  page.value = { ...page.value, size, page: 0 }
  apply()
}

function requestRestore(user: AdminUser) {
  confirmTarget.value = user
  confirmVisible.value = true
}

async function confirmRestore() {
  if (!confirmTarget.value) return
  const id = confirmTarget.value.id
  actionLoading.value = id
  confirmVisible.value = false
  try {
    await client.post(`/admin/users/${id}/restore`)
    await load()
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || 'Không khôi phục được user'
  } finally {
    actionLoading.value = null
    confirmTarget.value = null
  }
}
</script>

<template>
  <div class="panel">
    <div class="panel-head">
      <div>
        <p class="eyebrow">Người dùng</p>
        <h2>Danh sách đã xóa</h2>
        <p class="muted">Khôi phục tài khoản đã xóa.</p>
      </div>
      <div class="actions">
        <button class="ghost" @click="load" :disabled="loading">Làm mới</button>
      </div>
    </div>

    <div class="filters">
      <label>
        <span>Tên</span>
        <input v-model="filters.name" type="text" placeholder="Nhập tên" />
      </label>
      <label>
        <span>Email</span>
        <input v-model="filters.email" type="text" placeholder="Nhập email" />
      </label>
      <div class="filter-actions">
        <button class="ghost" type="button" @click="resetFilters">Xóa lọc</button>
      </div>
    </div>

    <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
    <p v-else-if="loading" class="muted">Đang tải...</p>

    <div v-else class="table">
      <div class="row head">
        <div>Họ tên</div>
        <div>Email</div>
        <div>Roles</div>
        <div>Ngày tạo</div>
        <div></div>
      </div>
      <div v-for="u in users" :key="u.id" class="row">
        <div class="name">{{ u.username }}</div>
        <div class="muted">{{ u.email }}</div>
        <div class="role-list">{{ (u.roles || []).map((r) => roleLabels[r] || r).join(', ') }}</div>
        <div class="muted">{{ u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '-' }}</div>
        <div class="row-actions">
          <button class="primary" :disabled="actionLoading === u.id" @click="requestRestore(u)">Khôi phục</button>
        </div>
      </div>
      <div v-if="!users.length" class="row empty">Không có user đã xóa</div>
      <PaginationBar :page="page.page" :size="page.size" :total="page.total" @update:page="changePage" @update:size="changeSize" />
    </div>
  </div>
  <ConfirmDialog
    v-model="confirmVisible"
    :title="'Khôi phục user'"
    :message="confirmTarget ? `Khôi phục ${confirmTarget.username}?` : ''"
    cancel-text="Hủy"
    confirm-text="Khôi phục"
    @confirm="confirmRestore"
  />
</template>

<style scoped lang="scss">
.panel {
  background: var(--surface);
  border-radius: 20px;
  padding: 20px;
  box-shadow: var(--shadow);
  display: grid;
  gap: 16px;
  border: 1px solid var(--border);
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
}

.filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  align-items: end;
  padding: 12px;
  border-radius: 12px;
  background: var(--chip-bg);
  border: 1px solid var(--border);
}

.filters label span {
  display: block;
  font-weight: 700;
  margin-bottom: 6px;
}

.filters input {
  width: 100%;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--text);
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
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
  overflow-x: auto;
}

.row,
.row.head {
  grid-template-columns: 2fr 1.5fr 1.5fr 1fr minmax(140px, 1fr);
  min-width: 1000px;
}

.row {
  display: grid;
  align-items: center;
  padding: 12px 10px;
  border-radius: 12px;
  background: var(--chip-bg);
  border: 1px solid var(--border);
}

.row.head {
  font-weight: 700;
  color: var(--text);
  background: var(--surface);
}

.row.empty {
  grid-template-columns: 1fr;
  justify-items: center;
  color: var(--muted);
}

.role-list {
  color: var(--text);
}

.actions,
.row-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.primary,
.ghost {
  border-radius: 12px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  padding: 10px 12px;
}

.primary {
  background: linear-gradient(135deg, var(--accent), var(--primary));
  color: #fff;
}

.ghost {
  background: var(--chip-bg);
  color: var(--text);
  border: 1px solid var(--border);
}

.primary:disabled,
.ghost:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 960px) {
  .row,
  .row.head {
    grid-template-columns: 2fr 1.5fr 1.5fr 1fr minmax(140px, 1fr);
  }
}

@media (max-width: 720px) {
  .row.head {
    display: none;
  }
  .table {
    overflow-x: visible;
  }
  .row {
    grid-template-columns: 1fr;
    align-items: flex-start;
    gap: 8px;
    min-width: auto;
  }
  .row-actions {
    grid-column: 1 / -1;
    justify-content: flex-start;
  }
}
</style>
