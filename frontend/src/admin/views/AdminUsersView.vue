<script setup lang="ts">
const users = [
  { name: 'Minh Pham', email: 'minh@example.com', role: 'Editor', status: 'Active', posts: 42 },
  { name: 'Lan Anh', email: 'lan@example.com', role: 'Author', status: 'Active', posts: 18 },
  { name: 'Huy Tran', email: 'huy@example.com', role: 'Reviewer', status: 'Pending', posts: 6 },
  { name: 'Khoa Le', email: 'khoa@example.com', role: 'Admin', status: 'Active', posts: 88 }
]
</script>

<template>
  <div class="panel">
    <div class="panel-head">
      <div>
        <p class="eyebrow">Người dùng</p>
        <h2>Quản lý user</h2>
        <p class="muted">Phân quyền, khóa tài khoản, theo dõi hoạt động.</p>
      </div>
      <div class="actions">
        <button class="ghost">Mời user</button>
        <button class="primary">Tạo admin</button>
      </div>
    </div>

    <div class="table">
      <div class="row head">
        <div>Họ tên</div>
        <div>Email</div>
        <div>Role</div>
        <div>Trạng thái</div>
        <div>Bài viết</div>
        <div></div>
      </div>
      <div v-for="u in users" :key="u.email" class="row">
        <div class="name">{{ u.name }}</div>
        <div class="muted">{{ u.email }}</div>
        <div><span class="pill">{{ u.role }}</span></div>
        <div><span class="status" :class="u.status.toLowerCase()">{{ u.status }}</span></div>
        <div>{{ u.posts }}</div>
        <div class="row-actions">
          <button class="ghost">Sửa</button>
          <button class="danger">Khóa</button>
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
  grid-template-columns: 2fr 2fr 1fr 1fr 1fr auto;
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

.status {
  padding: 6px 10px;
  border-radius: 10px;
  font-weight: 700;
  font-size: 13px;
  color: white;
}

.status.active {
  background: #22c55e;
}

.status.pending {
  background: #f59e0b;
}

.status.blocked {
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
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
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
