<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
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
  createdAt?: string
  publishedAt?: string
}

const filters = ref({ title: '', author: '', date: '', vehicle: '' })
const allReviews = ref<ReviewRow[]>([])
const reviews = ref<ReviewRow[]>([])
const page = ref({ page: 0, size: 10, total: 0 })

const loading = ref(false)
const actionLoading = ref<number | null>(null)
const errorMsg = ref('')

function clampPage(meta: { page: number; size: number; total: number }) {
  const totalPages = meta.size ? Math.max(1, Math.ceil(Math.max(meta.total, 0) / meta.size)) : 1
  const pageValue = Math.min(Math.max(meta.page, 0), totalPages - 1)
  return { ...meta, page: pageValue }
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
    const dateText = p.createdAt ? new Date(p.createdAt).toISOString().slice(0, 10) : ''
    const matchesDate = !date || dateText === date
    const matchesVehicle = !vehicle || (p.vehicleModel || '').toLowerCase().includes(vehicle)
    return matchesTitle && matchesAuthor && matchesDate && matchesVehicle
  })
}

function apply() {
  const filtered = filterReviews(allReviews.value, filters.value)
  if (hasFilters(filters.value)) {
    const meta = clampPage({ ...page.value, total: filtered.length })
    const start = meta.page * meta.size
    page.value = meta
    reviews.value = filtered.slice(start, start + meta.size)
    return
  }
  page.value = page.value
  reviews.value = allReviews.value
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
  filters.value = { title: '', author: '', date: '', vehicle: '' }
}

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const params = { page: page.value.page, size: page.value.size, status: 'REJECTED' }
    const { data } = await client.get('/admin/reviews', { params })
    const list = data.reviews || data.content || []
    allReviews.value = list
    const meta = resolvePageMeta(data, allReviews.value.length, { ...page.value })
    page.value = clampPage(meta)
    apply()
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || 'Không tải được bài bị từ chối'
    allReviews.value = []
    reviews.value = []
  } finally {
    loading.value = false
  }
}

function changePage(target: number) {
  page.value = { ...page.value, page: target }
  if (hasFilters(filters.value)) {
    apply()
    return
  }
  load()
}

function changeSize(size: number) {
  page.value = { ...page.value, size, page: 0 }
  if (hasFilters(filters.value)) {
    apply()
    return
  }
  load()
}

async function restore(id: number) {
  actionLoading.value = id
  try {
    await client.post(`/admin/reviews/${id}/restore`)
    await load()
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || 'Không khôi phục được bài viết'
  } finally {
    actionLoading.value = null
  }
}

async function approve(id: number) {
  actionLoading.value = id
  try {
    await client.post(`/admin/reviews/${id}/approve`)
    await load()
  } finally {
    actionLoading.value = null
  }
}
</script>

<template>
  <div class="panel">
    <div class="panel-head">
      <div>
        <p class="eyebrow">Bài viết</p>
        <h2>Bài bị từ chối</h2>
        <p class="muted">Khôi phục hoặc duyệt lại.</p>
      </div>
      <div class="actions">
        <button class="ghost" @click="load" :disabled="loading">Làm mới</button>
      </div>
    </div>

    <div class="filters">
      <label>
        <span>Tiêu đề</span>
        <input v-model="filters.title" type="text" placeholder="Nhập tiêu đề" />
      </label>
      <label>
        <span>Tác giả</span>
        <input v-model="filters.author" type="text" placeholder="Nhập tên tác giả" />
      </label>
      <label>
        <span>Ngày viết</span>
        <input v-model="filters.date" type="date" />
      </label>
      <label>
        <span>Loại xe</span>
        <input v-model="filters.vehicle" type="text" placeholder="Nhập mẫu xe" />
      </label>
      <div class="filter-actions">
        <button class="ghost" type="button" @click="resetFilters">Xóa lọc</button>
      </div>
    </div>

    <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
    <p v-else-if="loading" class="muted">Đang tải...</p>

    <div v-else class="table">
      <div class="row head">
        <div>Tiêu đề</div>
        <div>Tác giả</div>
        <div>Trạng thái</div>
        <div>Lượt xem</div>
        <div>Thả tim</div>
        <div>Bình luận</div>
        <div>Loại xe</div>
        <div>Ngày viết</div>
        <div>Ngày xuất bản</div>
        <div></div>
      </div>
      <div v-for="p in reviews" :key="p.id" class="row">
        <RouterLink class="title link" :to="{ name: 'admin-post-detail', params: { id: p.id } }">{{ p.title }}</RouterLink>
        <div class="muted">{{ p.authorName || 'Ẩn danh' }}</div>
        <div><span class="status rejected">{{ p.status || 'REJECTED' }}</span></div>
        <div>{{ p.views ?? 0 }}</div>
        <div>{{ p.likes ?? 0 }}</div>
        <div>{{ p.commentsCount ?? 0 }}</div>
        <div class="muted">{{ p.vehicleModel || '-' }}</div>
        <div class="muted">{{ p.createdAt ? new Date(p.createdAt).toLocaleDateString() : '-' }}</div>
        <div class="muted">{{ p.publishedAt ? new Date(p.publishedAt).toLocaleDateString() : '-' }}</div>
        <div class="row-actions">
          <button class="ghost" :disabled="actionLoading === p.id" @click="restore(p.id)">Khôi phục</button>
          <button class="primary" :disabled="actionLoading === p.id" @click="approve(p.id)">Duyệt</button>
        </div>
      </div>
      <div v-if="!reviews.length" class="row empty">Không có bài bị từ chối</div>
      <PaginationBar :page="page.page" :size="page.size" :total="page.total" @update:page="changePage" @update:size="changeSize" />
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

.table {
  display: grid;
  gap: 10px;
  overflow-x: auto;
}

.row,
.row.head {
  grid-template-columns: 2fr 1.2fr 1fr 0.9fr 0.9fr 0.9fr 1fr 1fr 1fr minmax(180px, 1fr);
  min-width: 1300px;
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

.title {
  font-weight: 700;
}

.link {
  color: var(--primary);
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.link:hover {
  text-decoration: underline;
}

.status {
  padding: 6px 10px;
  border-radius: 10px;
  font-weight: 700;
  font-size: 13px;
  color: white;
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
    grid-template-columns: 2fr 1.2fr 1fr 0.9fr 0.9fr 0.9fr 1fr 1fr 1fr minmax(180px, 1fr);
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
