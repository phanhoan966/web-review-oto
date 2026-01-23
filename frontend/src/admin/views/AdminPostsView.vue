<script setup lang="ts">
const posts = [
  { title: 'SUV điện 2025', author: 'Minh Pham', status: 'APPROVED', views: 18234, submitted: 'Hôm nay' },
  { title: 'Hybrid tiết kiệm', author: 'Bui Tam', status: 'PENDING', views: 4210, submitted: 'Hôm nay' },
  { title: 'Pickup địa hình', author: 'Khoa Le', status: 'REJECTED', views: 8733, submitted: 'Hôm qua' },
  { title: 'Crossover đô thị', author: 'Huy Tran', status: 'PENDING', views: 9510, submitted: '2 ngày trước' }
]
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
        <button class="ghost">Lọc</button>
        <button class="primary">Tạo bài mới</button>
      </div>
    </div>

    <div class="table">
      <div class="row head">
        <div>Tiêu đề</div>
        <div>Tác giả</div>
        <div>Trạng thái</div>
        <div>Lượt xem</div>
        <div>Gửi</div>
        <div></div>
      </div>
      <div v-for="p in posts" :key="p.title" class="row">
        <div class="title">{{ p.title }}</div>
        <div class="muted">{{ p.author }}</div>
        <div><span class="status" :class="p.status.toLowerCase()">{{ p.status }}</span></div>
        <div>{{ p.views }}</div>
        <div class="muted">{{ p.submitted }}</div>
        <div class="row-actions">
          <button class="ghost">Xem</button>
          <button class="primary">Duyệt</button>
          <button class="danger">Từ chối</button>
        </div>
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
