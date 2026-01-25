<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import client from '../../api/client'
import PaginationBar from '../components/PaginationBar.vue'
import { resolvePageMeta } from '../utils/pageMeta'

interface AdminUser {
  id: number
  username: string
  email: string
  roles: string[]
  followers?: number
  rating?: number
  reviewCount?: number
  birthYear?: number
  createdAt?: string
}

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const allUsers = ref<AdminUser[]>([])
const users = ref<AdminUser[]>([])
const usersPage = ref({ page: 0, size: 10, total: 0 })
const filters = ref({ name: '', email: '', year: '' })

function clampPage(meta: { page: number; size: number; total: number }) {
  const totalPages = meta.size ? Math.max(1, Math.ceil(Math.max(meta.total, 0) / meta.size)) : 1
  const page = Math.min(Math.max(meta.page, 0), totalPages - 1)
  return { ...meta, page }
}

function resetFilters() {
  filters.value = { name: '', email: '', year: '' }
}

function filterUsers() {
  const name = filters.value.name.trim().toLowerCase()
  const email = filters.value.email.trim().toLowerCase()
  const year = filters.value.year.trim()
  return allUsers.value.filter((u) => {
    const matchesName = !name || (u.username || '').toLowerCase().includes(name)
    const matchesEmail = !email || (u.email || '').toLowerCase().includes(email)
    const birthYear = u.birthYear || (u.createdAt ? new Date(u.createdAt).getFullYear() : undefined)
    const matchesYear = !year || (birthYear && birthYear.toString().includes(year))
    return matchesName && matchesEmail && matchesYear
  })
}

function applyPaging() {
  const filtered = filterUsers()
  const meta = clampPage({ ...usersPage.value, total: filtered.length })
  const start = meta.page * meta.size
  const end = start + meta.size
  usersPage.value = meta
  users.value = filtered.slice(start, end)
}

const loading = ref(false)
const saving = ref(false)
const actionLoading = ref<number | null>(null)
const errorMsg = ref('')
const formError = ref('')
const formMode = ref<'create' | 'edit'>('create')
const editingId = ref<number | null>(null)
const form = ref({ username: '', email: '', password: '', roles: ['ROLE_USER'] as string[] })
const roleLabels: Record<string, string> = {
  ROLE_SYSTEM_ADMIN: 'System Admin',
  ROLE_MANAGER: 'Manager',
  ROLE_ADMIN: 'Admin',
  ROLE_USER: 'User'
}

const editingUser = computed(() => users.value.find((u) => u.id === editingId.value) || null)
const isCreateMode = computed(() => route.query.mode === 'create')
const isEditMode = computed(() => route.query.mode === 'edit' && route.query.id)
const isFormView = computed(() => Boolean(isCreateMode.value || isEditMode.value))
const canCreate = computed(() => hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_MANAGER'))

watch(
  filters,
  () => {
    usersPage.value = { ...usersPage.value, page: 0 }
    applyPaging()
  },
  { deep: true }
)

onMounted(load)

watch(
  () => route.query,
  () => {
    syncRouteForm()
  }
)

function hasRole(role: string) {
  return (auth.user?.roles || []).includes(role)
}

function roleOptions(target?: AdminUser | null) {
  if (hasRole('ROLE_SYSTEM_ADMIN')) return ['ROLE_SYSTEM_ADMIN', 'ROLE_MANAGER', 'ROLE_ADMIN', 'ROLE_USER']
  if (hasRole('ROLE_MANAGER')) {
    if (target && target.id === auth.user?.id) {
      return ['ROLE_MANAGER', 'ROLE_USER']
    }
    return ['ROLE_USER']
  }
  if (hasRole('ROLE_ADMIN') && target && target.id === auth.user?.id) {
    return ['ROLE_ADMIN', 'ROLE_USER']
  }
  return []
}

function canEdit(target?: AdminUser | null) {
  if (!auth.user || !target) return false
  const isSelf = target.id === auth.user.id
  const roles = new Set(target.roles || [])
  if (hasRole('ROLE_SYSTEM_ADMIN')) return true
  if (hasRole('ROLE_MANAGER')) {
    if (roles.has('ROLE_SYSTEM_ADMIN') || roles.has('ROLE_ADMIN')) return false
    if (!isSelf && roles.has('ROLE_MANAGER')) return false
    return true
  }
  if (hasRole('ROLE_ADMIN')) {
    return isSelf
  }
  return false
}

function canDeleteUser(target?: AdminUser | null) {
  if (!auth.user || !target) return false
  const roles = new Set(target.roles || [])
  if (hasRole('ROLE_SYSTEM_ADMIN')) return true
  if (hasRole('ROLE_MANAGER')) {
    if (target.id === auth.user.id) return false
    if (roles.has('ROLE_SYSTEM_ADMIN') || roles.has('ROLE_ADMIN') || roles.has('ROLE_MANAGER')) return false
    return true
  }
  return false
}

function resetForm() {
  formMode.value = 'create'
  editingId.value = null
  form.value = { username: '', email: '', password: '', roles: ['ROLE_USER'] }
  formError.value = ''
}

function syncRouteForm() {
  if (route.query.mode === 'create') {
    startCreate()
    return
  }
  if (route.query.mode === 'edit' && route.query.id) {
    const id = Number(route.query.id)
    const target = allUsers.value.find((u) => u.id === id)
    if (target) {
      startEdit(target)
      return
    }
  }
  if (isFormView.value) {
    router.push({ query: {} })
  }
}

function openCreate() {
  if (!canCreate.value) return
  router.push({ query: { mode: 'create' } })
}

function openEdit(user: AdminUser) {
  if (!canEdit(user)) return
  router.push({ query: { mode: 'edit', id: String(user.id) } })
}

function startCreate() {
  if (!canCreate.value) return
  formMode.value = 'create'
  editingId.value = null
  form.value = { username: '', email: '', password: '', roles: ['ROLE_USER'] }
  formError.value = ''
}

function startEdit(user: AdminUser) {
  if (!canEdit(user)) return
  formMode.value = 'edit'
  editingId.value = user.id
  form.value = {
    username: user.username,
    email: user.email,
    password: '',
    roles: (user.roles || []).filter((r) => roleOptions(user).includes(r))
  }
  if (!form.value.roles.length) {
    form.value.roles = roleOptions(user)
  }
  formError.value = ''
}

function toggleRole(role: string) {
  if (!roleOptions(editingUser.value).includes(role)) return
  const set = new Set(form.value.roles)
  if (set.has(role)) set.delete(role)
  else set.add(role)
  if (!set.size) return
  form.value.roles = Array.from(set)
}

async function submit() {
  if (isCreateMode.value && !canCreate.value) return
  if (isEditMode.value && editingUser.value && !canEdit(editingUser.value)) return
  if (!form.value.username || !form.value.email || (formMode.value === 'create' && !form.value.password)) {
    formError.value = 'Thiếu thông tin bắt buộc'
    return
  }
  formError.value = ''
  saving.value = true
  try {
    if (formMode.value === 'create') {
      await client.post('/admin/users', {
        username: form.value.username,
        email: form.value.email,
        password: form.value.password,
        roles: form.value.roles
      })
    } else if (editingId.value) {
      await client.put(`/admin/users/${editingId.value}`, {
        username: form.value.username,
        email: form.value.email,
        password: form.value.password || undefined,
        roles: form.value.roles
      })
    }
    await load()
    resetForm()
    router.push({ query: {} })
  } catch (error: any) {
    formError.value = error.response?.data?.message || 'Không lưu được user'
  } finally {
    saving.value = false
  }
}

async function deleteUser(id: number, target: AdminUser) {
  if (!canDeleteUser(target)) return
  if (!window.confirm('Bạn có chắc chắn muốn xoá user này?')) return
  actionLoading.value = id
  try {
    await client.delete(`/admin/users/${id}`)
    await load()
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || 'Không xoá được user'
  } finally {
    actionLoading.value = null
  }
}

function changeUsersPage(page: number) {
  usersPage.value = { ...usersPage.value, page }
  applyPaging()
}

function changeUsersSize(size: number) {
  usersPage.value = { ...usersPage.value, size, page: 0 }
  applyPaging()
}

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const params = { page: usersPage.value.page, size: usersPage.value.size }
    const { data } = await client.get('/admin/users', { params })
    const list = Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : data?.users || []
    allUsers.value = list
    const meta = resolvePageMeta(data, allUsers.value.length, { ...usersPage.value, total: allUsers.value.length })
    usersPage.value = clampPage(meta)
    applyPaging()
    syncRouteForm()
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || 'Không tải được danh sách user'
    allUsers.value = []
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
        <button v-if="isFormView" class="ghost" type="button" @click="router.push({ query: {} })">Về danh sách</button>
        <button v-else-if="canCreate" class="primary" type="button" @click="openCreate">Thêm user</button>
      </div>
    </div>

    <div v-if="isFormView" class="form">
      <div class="form-head">
        <p class="eyebrow">{{ formMode === 'create' ? 'Thêm user' : 'Chỉnh sửa user' }}</p>
        <h3>{{ formMode === 'create' ? 'Tạo tài khoản mới' : 'Cập nhật tài khoản' }}</h3>
      </div>
      <div class="form-grid">
        <label>
          <span>Họ tên</span>
          <input v-model="form.username" type="text" placeholder="Tên" />
        </label>
        <label>
          <span>Email</span>
          <input v-model="form.email" type="email" placeholder="email@example.com" />
        </label>
        <label>
          <span>Mật khẩu</span>
          <input v-model="form.password" type="password" :placeholder="formMode === 'create' ? 'Tối thiểu 6 ký tự' : 'Để trống nếu giữ nguyên'" />
        </label>
        <div class="roles">
          <span>Roles</span>
          <div class="role-pills">
            <label v-for="role in roleOptions(isCreateMode ? null : editingUser)" :key="role" class="pill-option">
              <input type="checkbox" :value="role" :checked="form.roles.includes(role)" @change="toggleRole(role)" />
              <span>{{ roleLabels[role] || role }}</span>
            </label>
          </div>
        </div>
      </div>
      <div class="form-actions">
        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="gap">
          <button class="ghost" type="button" @click="router.push({ query: {} }); resetForm()" :disabled="saving">Hủy</button>
          <button class="primary" type="button" @click="submit" :disabled="saving">{{ formMode === 'create' ? 'Thêm user' : 'Lưu thay đổi' }}</button>
        </div>
      </div>
    </div>

    <div v-else>
      <div class="filters">
        <label>
          <span>Tên</span>
          <input v-model="filters.name" type="text" placeholder="Nhập tên" />
        </label>
        <label>
          <span>Email</span>
          <input v-model="filters.email" type="text" placeholder="Nhập email" />
        </label>
        <label>
          <span>Năm sinh</span>
          <input v-model="filters.year" type="number" inputmode="numeric" placeholder="Ví dụ 1995" />
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
          <div>Theo dõi</div>
          <div>Bài viết</div>
          <div>Tham gia</div>
          <div></div>
        </div>
        <div v-for="u in users" :key="u.id" class="row">
          <div class="name">{{ u.username }}</div>
          <div class="muted">{{ u.email }}</div>
          <div class="role-list">{{ (u.roles || []).map((r) => roleLabels[r] || r).join(', ') }}</div>
          <div>{{ u.followers ?? 0 }}</div>
          <div>{{ u.reviewCount ?? 0 }}</div>
          <div class="muted">{{ u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '-' }}</div>
          <div class="row-actions">
            <button class="ghost" :disabled="!canEdit(u) || actionLoading === u.id" @click="openEdit(u)">Sửa</button>
            <button class="danger" :disabled="!canDeleteUser(u) || actionLoading === u.id" @click="deleteUser(u.id, u)">Xóa</button>
          </div>
        </div>
        <div v-if="!users.length" class="row empty">Không có user</div>
      </div>
      <PaginationBar :page="usersPage.page" :size="usersPage.size" :total="usersPage.total" @update:page="changeUsersPage" @update:size="changeUsersSize" />
    </div>
  </div>
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

.form {
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 14px;
  display: grid;
  gap: 12px;
  background: var(--chip-bg);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

label span {
  display: block;
  font-weight: 700;
  margin-bottom: 6px;
}

input {
  width: 100%;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--text);
}

.roles {
  display: grid;
  gap: 6px;
}

.role-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pill-option {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: var(--surface);
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.gap {
  display: flex;
  gap: 8px;
}

.table {
  --cols: 1.5fr 1.5fr 1.3fr 0.9fr 0.9fr 1.1fr 140px;
  display: grid;
  gap: 10px;
}

.row {
  display: grid;
  grid-template-columns: var(--cols);
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

.name {
  font-weight: 700;
}

.role-list {
  font-weight: 600;
  color: var(--text);
}

.pill {
  padding: 6px 10px;
  border-radius: 999px;
  background: var(--chip-bg);
  color: var(--text);
  font-weight: 700;
  font-size: 13px;
}

.actions,
.row-actions {
  display: flex;
  gap: 8px;
}

.primary,
.ghost,
.danger {
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

.danger {
  background: #fee2e2;
  color: #b91c1c;
}

.ghost:disabled,
.danger:disabled,
.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 960px) {
  .table {
    --cols: 1.3fr 1.3fr 1.1fr 0.9fr 0.9fr 1fr 120px;
  }
}

@media (max-width: 720px) {
  .row.head {
    display: none;
  }
  .row {
    grid-template-columns: 1fr;
    align-items: flex-start;
    gap: 8px;
  }
  .form-actions {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
