<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import client from '../../api/client'
import PaginationBar from '../components/PaginationBar.vue'
import { resolvePageMeta } from '../utils/pageMeta'

interface ReviewRow {
  id: number
  title: string
  authorName?: string
  status?: string
  views?: number
  likes?: number
  commentsCount?: number
  vehicleModel?: string
  publishedAt?: string
}

const pendingFilters = ref({ title: '', author: '', date: '', vehicle: '' })
const approvedFilters = ref({ title: '', author: '', date: '', vehicle: '' })
const rejectedFilters = ref({ title: '', author: '', date: '', vehicle: '' })
const pendingAll = ref<ReviewRow[]>([])
const approvedAll = ref<ReviewRow[]>([])
const rejectedAll = ref<ReviewRow[]>([])
const pending = ref<ReviewRow[]>([])
const approved = ref<ReviewRow[]>([])
const rejected = ref<ReviewRow[]>([])
const pendingBase = ref({ page: 0, size: 10, total: 0 })
const approvedBase = ref({ page: 0, size: 10, total: 0 })
const rejectedBase = ref({ page: 0, size: 10, total: 0 })
const pendingPage = ref({ page: 0, size: 10, total: 0 })
const approvedPage = ref({ page: 0, size: 10, total: 0 })
const rejectedPage = ref({ page: 0, size: 10, total: 0 })

function clampPage(meta: { page: number; size: number; total: number }) {
  const totalPages = meta.size ? Math.max(1, Math.ceil(Math.max(meta.total, 0) / meta.size)) : 1
  const page = Math.min(Math.max(meta.page, 0), totalPages - 1)
  return { ...meta, page }
}

function hasFilters(target: { title: string; author: string; date: string; vehicle: string }) {
  return Object.values(target).some((v) => v.trim())
}

function filterReviews(list: ReviewRow[], target: { title: string; author: string; date: string; vehicle: string }) {
  const title = target.title.trim().toLowerCase()
  const author = target.author.trim().toLowerCase()
  const date = target.date.trim()
  const vehicle = target.vehicle.trim().toLowerCase()
  return list.filter((p) => {
    const matchesTitle = !title || (p.title || '').toLowerCase().includes(title)
    const matchesAuthor = !author || (p.authorName || '').toLowerCase().includes(author)
    const dateText = p.publishedAt ? new Date(p.publishedAt).toISOString().slice(0, 10) : ''
    const matchesDate = !date || dateText === date
    const matchesVehicle = !vehicle || (p.vehicleModel || '').toLowerCase().includes(vehicle)
    return matchesTitle && matchesAuthor && matchesDate && matchesVehicle
  })
}

function resetPendingFilters() {
  pendingFilters.value = { title: '', author: '', date: '', vehicle: '' }
}

function resetApprovedFilters() {
  approvedFilters.value = { title: '', author: '', date: '', vehicle: '' }
}

function resetRejectedFilters() {
  rejectedFilters.value = { title: '', author: '', date: '', vehicle: '' }
}

function applyPending() {
  const filtered = filterReviews(pendingAll.value, pendingFilters.value)
  if (hasFilters(pendingFilters.value)) {
    const meta = clampPage({ ...pendingPage.value, total: filtered.length })
    const start = meta.page * meta.size
    pendingPage.value = meta
    pending.value = filtered.slice(start, start + meta.size)
    return
  }
  pendingPage.value = pendingBase.value
  pending.value = pendingAll.value
}

function applyApproved() {
  const filtered = filterReviews(approvedAll.value, approvedFilters.value)
  if (hasFilters(approvedFilters.value)) {
    const meta = clampPage({ ...approvedPage.value, total: filtered.length })
    const start = meta.page * meta.size
    approvedPage.value = meta
    approved.value = filtered.slice(start, start + meta.size)
    return
  }
  approvedPage.value = approvedBase.value
  approved.value = approvedAll.value
}

function applyRejected() {
  const filtered = filterReviews(rejectedAll.value, rejectedFilters.value)
  if (hasFilters(rejectedFilters.value)) {
    const meta = clampPage({ ...rejectedPage.value, total: filtered.length })
    const start = meta.page * meta.size
    rejectedPage.value = meta
    rejected.value = filtered.slice(start, start + meta.size)
    return
  }
  rejectedPage.value = rejectedBase.value
  rejected.value = rejectedAll.value
}

const loading = ref(false)
const actionLoading = ref<number | null>(null)
const errorMsg = ref('')

watch(
  pendingFilters,
  () => {
    pendingPage.value = { ...pendingPage.value, page: 0 }
    applyPending()
  },
  { deep: true }
)

watch(
  approvedFilters,
  () => {
    approvedPage.value = { ...approvedPage.value, page: 0 }
    applyApproved()
  },
  { deep: true }
)

watch(
  rejectedFilters,
  () => {
    rejectedPage.value = { ...rejectedPage.value, page: 0 }
    applyRejected()
  },
  { deep: true }
)

onMounted(load)

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    await Promise.all([loadPending(), loadApproved(), loadRejected()])
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || 'Không tải được bài viết'
  } finally {
    loading.value = false
  }
}

async function loadPending() {
  const params = { page: pendingPage.value.page, size: pendingPage.value.size }
  const { data } = await client.get('/admin/reviews/pending', { params })
  pendingAll.value = data.reviews || data.content || []
  const meta = resolvePageMeta(data, pendingAll.value.length, { ...pendingPage.value })
  pendingBase.value = clampPage(meta)
  pendingPage.value = pendingBase.value
  applyPending()
}

async function loadApproved() {
  const params = { page: approvedPage.value.page, size: approvedPage.value.size, status: 'APPROVED' }
  const { data } = await client.get('/admin/reviews', { params })
  approvedAll.value = data.reviews || data.content || []
  const meta = resolvePageMeta(data, approvedAll.value.length, { ...approvedPage.value })
  approvedBase.value = clampPage(meta)
  approvedPage.value = approvedBase.value
  applyApproved()
}

async function loadRejected() {
  const params = { page: rejectedPage.value.page, size: rejectedPage.value.size, status: 'REJECTED' }
  const { data } = await client.get('/admin/reviews', { params })
  rejectedAll.value = data.reviews || data.content || []
  const meta = resolvePageMeta(data, rejectedAll.value.length, { ...rejectedPage.value })
  rejectedBase.value = clampPage(meta)
  rejectedPage.value = rejectedBase.value
  applyRejected()
}

async function approve(id: number) {
  actionLoading.value = id
  try {
    await client.post(`/admin/reviews/${id}/approve`)
    await loadPending()
    await loadApproved()
    await loadRejected()
  } finally {
    actionLoading.value = null
  }
}

async function reject(id: number) {
  actionLoading.value = id
  try {
    await client.post(`/admin/reviews/${id}/reject`)
    await loadPending()
    await loadApproved()
    await loadRejected()
  } finally {
    actionLoading.value = null
  }
}

async function hidePost(id: number) {
  actionLoading.value = id
  try {
    await client.post(`/admin/reviews/${id}/reject`)
    await loadApproved()
    await loadRejected()
  } finally {
    actionLoading.value = null
  }
}

function changePendingPage(page: number) {
  pendingPage.value = { ...pendingPage.value, page }
  if (hasFilters(pendingFilters.value)) {
    applyPending()
    return
  }
  loadPending()
}

function changePendingSize(size: number) {
  pendingPage.value = { ...pendingPage.value, size, page: 0 }
  if (hasFilters(pendingFilters.value)) {
    applyPending()
    return
  }
  loadPending()
}

function changeApprovedPage(page: number) {
  approvedPage.value = { ...approvedPage.value, page }
  if (hasFilters(approvedFilters.value)) {
    applyApproved()
    return
  }
  loadApproved()
}

function changeApprovedSize(size: number) {
  approvedPage.value = { ...approvedPage.value, size, page: 0 }
  if (hasFilters(approvedFilters.value)) {
    applyApproved()
    return
  }
  loadApproved()
}

function changeRejectedPage(page: number) {
  rejectedPage.value = { ...rejectedPage.value, page }
  if (hasFilters(rejectedFilters.value)) {
    applyRejected()
    return
  }
  loadRejected()
}

function changeRejectedSize(size: number) {
  rejectedPage.value = { ...rejectedPage.value, size, page: 0 }
  if (hasFilters(rejectedFilters.value)) {
    applyRejected()
    return
  }
  loadRejected()
}
</script>

<template>
  <div class="panel">
    <div class="panel-head">
      <div>
        <p class="eyebrow">Bài viết</p>
        <h2>Quản lý bài viết</h2>
        <p class="muted">Duyệt, ẩn, hoặc gắn nhãn nổi bật cho bài.</p>
      </div>
      <div class="actions">
        <button class="ghost" @click="load" :disabled="loading">Làm mới</button>
      </div>
    </div>

    <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
    <p v-else-if="loading" class="muted">Đang tải...</p>

    <div v-else class="tables">
      <div class="filters">
        <label>
          <span>Tiêu đề</span>
          <input v-model="pendingFilters.title" type="text" placeholder="Nhập tiêu đề" />
        </label>
        <label>
          <span>Tác giả</span>
          <input v-model="pendingFilters.author" type="text" placeholder="Nhập tên tác giả" />
        </label>
        <label>
          <span>Ngày viết</span>
          <input v-model="pendingFilters.date" type="date" />
        </label>
        <label>
          <span>Loại xe</span>
          <input v-model="pendingFilters.vehicle" type="text" placeholder="Nhập mẫu xe" />
        </label>
        <div class="filter-actions">
          <button class="ghost" type="button" @click="resetPendingFilters">Xóa lọc</button>
        </div>
      </div>
      <div class="table">
        <div class="row head">
          <div>Tiêu đề</div>
          <div>Tác giả</div>
          <div>Trạng thái</div>
          <div>Lượt xem</div>
          <div>Thả tim</div>
          <div>Bình luận</div>
          <div>Loại xe</div>
          <div>Gửi</div>
          <div></div>
        </div>
        <div v-for="p in pending" :key="p.id" class="row">
          <div class="title">{{ p.title }}</div>
          <div class="muted">{{ p.authorName || 'Ẩn danh' }}</div>
          <div><span class="status pending">{{ p.status || 'PENDING' }}</span></div>
          <div>{{ p.views ?? 0 }}</div>
          <div>{{ p.likes ?? 0 }}</div>
          <div>{{ p.commentsCount ?? 0 }}</div>
          <div class="muted">{{ p.vehicleModel || '-' }}</div>
          <div class="muted">{{ p.publishedAt ? new Date(p.publishedAt).toLocaleDateString() : '-' }}</div>
          <div class="row-actions">
            <button class="primary" :disabled="actionLoading === p.id" @click="approve(p.id)">Duyệt</button>
            <button class="danger" :disabled="actionLoading === p.id" @click="reject(p.id)">Từ chối</button>
          </div>
        </div>
        <div v-if="!pending.length" class="row empty">Không có bài chờ duyệt</div>
      </div>
      <PaginationBar :page="pendingPage.page" :size="pendingPage.size" :total="pendingPage.total" @update:page="changePendingPage" @update:size="changePendingSize" />

      <div class="filters">
        <label>
          <span>Tiêu đề</span>
          <input v-model="approvedFilters.title" type="text" placeholder="Nhập tiêu đề" />
        </label>
        <label>
          <span>Tác giả</span>
          <input v-model="approvedFilters.author" type="text" placeholder="Nhập tên tác giả" />
        </label>
        <label>
          <span>Ngày viết</span>
          <input v-model="approvedFilters.date" type="date" />
        </label>
        <label>
          <span>Loại xe</span>
          <input v-model="approvedFilters.vehicle" type="text" placeholder="Nhập mẫu xe" />
        </label>
        <div class="filter-actions">
          <button class="ghost" type="button" @click="resetApprovedFilters">Xóa lọc</button>
        </div>
      </div>
      <div class="table">
        <div class="row head">
          <div>Tiêu đề</div>
          <div>Tác giả</div>
          <div>Trạng thái</div>
          <div>Lượt xem</div>
          <div>Thả tim</div>
          <div>Bình luận</div>
          <div>Loại xe</div>
          <div>Xuất bản</div>
          <div></div>
        </div>
        <div v-for="p in approved" :key="p.id" class="row">
          <div class="title">{{ p.title }}</div>
          <div class="muted">{{ p.authorName || 'Ẩn danh' }}</div>
          <div><span class="status approved">{{ p.status || 'APPROVED' }}</span></div>
          <div>{{ p.views ?? 0 }}</div>
          <div>{{ p.likes ?? 0 }}</div>
          <div>{{ p.commentsCount ?? 0 }}</div>
          <div class="muted">{{ p.vehicleModel || '-' }}</div>
          <div class="muted">{{ p.publishedAt ? new Date(p.publishedAt).toLocaleDateString() : '-' }}</div>
          <div class="row-actions">
            <button class="ghost" :disabled="actionLoading === p.id" @click="hidePost(p.id)">Ẩn</button>
          </div>
        </div>
        <div v-if="!approved.length" class="row empty">Không có bài đã duyệt</div>
      </div>
      <PaginationBar :page="approvedPage.page" :size="approvedPage.size" :total="approvedPage.total" @update:page="changeApprovedPage" @update:size="changeApprovedSize" />

      <div class="filters">
        <label>
          <span>Tiêu đề</span>
          <input v-model="rejectedFilters.title" type="text" placeholder="Nhập tiêu đề" />
        </label>
        <label>
          <span>Tác giả</span>
          <input v-model="rejectedFilters.author" type="text" placeholder="Nhập tên tác giả" />
        </label>
        <label>
          <span>Ngày viết</span>
          <input v-model="rejectedFilters.date" type="date" />
        </label>
        <label>
          <span>Loại xe</span>
          <input v-model="rejectedFilters.vehicle" type="text" placeholder="Nhập mẫu xe" />
        </label>
        <div class="filter-actions">
          <button class="ghost" type="button" @click="resetRejectedFilters">Xóa lọc</button>
        </div>
      </div>
      <div class="table">
        <div class="row head">
          <div>Tiêu đề</div>
          <div>Tác giả</div>
          <div>Trạng thái</div>
          <div>Lượt xem</div>
          <div>Thả tim</div>
          <div>Bình luận</div>
          <div>Loại xe</div>
          <div>Gửi</div>
          <div></div>
        </div>
        <div v-for="p in rejected" :key="p.id" class="row">
          <div class="title">{{ p.title }}</div>
          <div class="muted">{{ p.authorName || 'Ẩn danh' }}</div>
          <div><span class="status rejected">{{ p.status || 'REJECTED' }}</span></div>
          <div>{{ p.views ?? 0 }}</div>
          <div>{{ p.likes ?? 0 }}</div>
          <div>{{ p.commentsCount ?? 0 }}</div>
          <div class="muted">{{ p.vehicleModel || '-' }}</div>
          <div class="muted">{{ p.publishedAt ? new Date(p.publishedAt).toLocaleDateString() : '-' }}</div>
          <div class="row-actions">
            <button class="primary" :disabled="actionLoading === p.id" @click="approve(p.id)">Duyệt lại</button>
          </div>
        </div>
        <div v-if="!rejected.length" class="row empty">Không có bài bị từ chối</div>
      </div>
      <PaginationBar :page="rejectedPage.page" :size="rejectedPage.size" :total="rejectedPage.total" @update:page="changeRejectedPage" @update:size="changeRejectedSize" />
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

.tables {
  display: grid;
  gap: 22px;
}

.table {
  display: grid;
  gap: 10px;
  overflow-x: auto;
}

.row,
.row.head {
  grid-template-columns: 2fr 1.2fr 1fr 0.9fr 0.9fr 0.9fr 1fr 1fr minmax(140px, 1fr);
  min-width: 1180px;
}

.row {
  display: grid;
  align-items: center;
  padding: 12px 10px;
  border-radius: 12px;
  background: var(--chip-bg);
  border: 1px solid var(--border);
}

.row > div:nth-child(4),
.row > div:nth-child(5),
.row > div:nth-child(6),
.row > div:nth-child(7),
.row > div:nth-child(8) {
  text-align: center;
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

.title {
  font-weight: 700;
}

.status {
  padding: 6px 10px;
  border-radius: 10px;
  font-weight: 700;
  font-size: 13px;
  color: white;
}

.status.approved {
  background: #22c55e;
}

.status.pending {
  background: #f59e0b;
}

.status.rejected {
  background: #ef4444;
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

.primary:disabled,
.danger:disabled,
.ghost:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 960px) {
  .row,
  .row.head {
    grid-template-columns: 2fr 1.2fr 1fr 0.9fr 0.9fr 0.9fr 1fr 1fr minmax(140px, 1fr);
  }
  .row-actions {
    grid-column: auto;
    justify-content: flex-start;
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
