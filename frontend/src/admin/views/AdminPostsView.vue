<script setup lang="ts">
import { onMounted, ref } from 'vue'
import client from '../../api/client'

interface ReviewRow {
  id: number
  title: string
  authorName?: string
  status?: string
  views?: number
  publishedAt?: string
}

const pending = ref<ReviewRow[]>([])
const approved = ref<ReviewRow[]>([])
const loading = ref(false)
const actionLoading = ref<number | null>(null)
const errorMsg = ref('')

onMounted(load)

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    await Promise.all([loadPending(), loadApproved()])
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || 'Không tải được bài viết'
  } finally {
    loading.value = false
  }
}

async function loadPending() {
  const { data } = await client.get('/reviews/pending', { params: { page: 0, size: 20 } })
  pending.value = data.reviews || []
}

async function loadApproved() {
  const { data } = await client.get('/reviews', { params: { page: 0, size: 20 } })
  approved.value = data.reviews || []
}

async function approve(id: number) {
  actionLoading.value = id
  try {
    await client.post(`/reviews/${id}/approve`)
    await loadPending()
    await loadApproved()
  } finally {
    actionLoading.value = null
  }
}

async function reject(id: number) {
  actionLoading.value = id
  try {
    await client.post(`/reviews/${id}/reject`)
    await loadPending()
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
      <div class="table">
        <div class="row head">
          <div>Tiêu đề</div>
          <div>Tác giả</div>
          <div>Trạng thái</div>
          <div>Lượt xem</div>
          <div>Gửi</div>
          <div></div>
        </div>
        <div v-for="p in pending" :key="p.id" class="row">
          <div class="title">{{ p.title }}</div>
          <div class="muted">{{ p.authorName || 'Ẩn danh' }}</div>
          <div><span class="status pending">{{ p.status || 'PENDING' }}</span></div>
          <div>{{ p.views ?? 0 }}</div>
          <div class="muted">{{ p.publishedAt ? new Date(p.publishedAt).toLocaleDateString() : '-' }}</div>
          <div class="row-actions">
            <button class="primary" :disabled="actionLoading === p.id" @click="approve(p.id)">Duyệt</button>
            <button class="danger" :disabled="actionLoading === p.id" @click="reject(p.id)">Từ chối</button>
          </div>
        </div>
        <div v-if="!pending.length" class="row empty">Không có bài chờ duyệt</div>
      </div>

      <div class="table">
        <div class="row head">
          <div>Tiêu đề</div>
          <div>Tác giả</div>
          <div>Trạng thái</div>
          <div>Lượt xem</div>
          <div>Xuất bản</div>
          <div></div>
        </div>
        <div v-for="p in approved" :key="p.id" class="row">
          <div class="title">{{ p.title }}</div>
          <div class="muted">{{ p.authorName || 'Ẩn danh' }}</div>
          <div><span class="status approved">{{ p.status || 'APPROVED' }}</span></div>
          <div>{{ p.views ?? 0 }}</div>
          <div class="muted">{{ p.publishedAt ? new Date(p.publishedAt).toLocaleDateString() : '-' }}</div>
          <div class="row-actions">
            <button class="ghost" disabled>Xem</button>
          </div>
        </div>
        <div v-if="!approved.length" class="row empty">Không có bài đã duyệt</div>
      </div>
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

.tables {
  display: grid;
  gap: 22px;
}

.table {
  display: grid;
  gap: 10px;
}

.row {
  display: grid;
  grid-template-columns: 2fr 1.5fr 1fr 1fr 1fr auto;
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
  background: linear-gradient(135deg, #0ea5e9, #2563eb);
  color: white;
}

.ghost {
  background: #f1f5f9;
  color: #111827;
  border: 1px solid #e5e7eb;
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
    grid-template-columns: 1.5fr 1.5fr 1fr 1fr 1fr;
  }
  .row-actions {
    grid-column: 1 / -1;
    justify-content: flex-start;
  }
}
</style>
