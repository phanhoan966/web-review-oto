<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import client from '../../api/client'
import { useAuthStore } from '../../stores/auth'
import { buildAssetUrl } from '../../public/utils/assetUrl'

interface AdminProfile {
  id: number
  username: string
  email: string
  roles?: string[]
  followers?: number
  rating?: number
  reviewCount?: number
  createdAt?: string
  updatedAt?: string
  avatarUrl?: string
}

const auth = useAuthStore()
const profile = ref<AdminProfile | null>(null)
const loading = ref(false)
const saving = ref(false)
const errorMsg = ref('')
const saveError = ref('')
const form = ref({ username: '', email: '', password: '' })

const avatarSrc = computed(() => (profile.value?.avatarUrl ? buildAssetUrl(profile.value.avatarUrl) : ''))
const avatarInitial = computed(() => (profile.value?.username || 'A').charAt(0).toUpperCase())
const roles = computed(() => profile.value?.roles || [])
const roleText = computed(() => roles.value.join(', ') || 'Quản trị')
const createdText = computed(() => (profile.value?.createdAt ? new Date(profile.value.createdAt).toLocaleString() : '-'))
const updatedText = computed(() => (profile.value?.updatedAt ? new Date(profile.value.updatedAt).toLocaleString() : '-'))

onMounted(load)

async function load() {
  loading.value = true
  errorMsg.value = ''
  await auth.ensureHydrated()
  try {
    if (!auth.user) {
      errorMsg.value = 'Không tìm thấy người dùng'
      return
    }
    const { data } = await client.get('/admin/users')
    const list = Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : data?.users || []
    const current = list.find((u: any) => u.id === auth.user?.id) || { ...auth.user }
    profile.value = current as AdminProfile
    syncForm()
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || 'Không tải được hồ sơ'
  } finally {
    loading.value = false
  }
}

function syncForm() {
  if (!profile.value) return
  form.value.username = profile.value.username || ''
  form.value.email = profile.value.email || ''
  form.value.password = ''
}

async function save() {
  const current = profile.value
  if (!current) return
  if (!form.value.username || !form.value.email) {
    saveError.value = 'Thiếu thông tin bắt buộc'
    return
  }
  saveError.value = ''
  saving.value = true
  try {
    const payload = {
      username: form.value.username,
      email: form.value.email,
      password: form.value.password || undefined,
      roles: roles.value
    }
    const { data } = await client.put(`/admin/users/${current.id}`, payload)
    profile.value = { ...current, ...data }
    const updated = profile.value
    if (updated && auth.user && auth.user.id === updated.id) {
      auth.user = {
        ...auth.user,
        username: data.username || auth.user.username,
        email: data.email || auth.user.email,
        roles: Array.isArray(data.roles) ? data.roles : auth.user.roles,
        followers: data.followers ?? auth.user.followers,
        rating: data.rating ?? auth.user.rating,
        reviewCount: data.reviewCount ?? auth.user.reviewCount
      }
    }
    form.value.password = ''
  } catch (error: any) {
    saveError.value = error.response?.data?.message || 'Không cập nhật được hồ sơ'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="page">
    <div v-if="loading" class="panel empty">
      <p class="muted">Đang tải hồ sơ...</p>
    </div>

    <div v-else-if="errorMsg" class="panel empty">
      <h2>Lỗi</h2>
      <p class="muted">{{ errorMsg }}</p>
    </div>

    <div v-else-if="profile" class="panel">
      <div class="header">
        <div class="avatar" :class="{ hasImage: avatarSrc }">
          <img v-if="avatarSrc" :src="avatarSrc" alt="Avatar" />
          <span v-else>{{ avatarInitial }}</span>
        </div>
        <div class="info">
          <p class="eyebrow">Hồ sơ quản trị</p>
          <h1>{{ profile.username }}</h1>
          <p class="muted">{{ profile.email }}</p>
          <div class="chips">
            <span class="chip">ID: {{ profile.id }}</span>
            <span class="chip">{{ roleText }}</span>
          </div>
        </div>
      </div>

      <div class="grid">
        <div class="card">
          <p class="label">Bài viết</p>
          <h2>{{ profile.reviewCount ?? 0 }}</h2>
          <span class="muted">Tổng số bài đã tạo</span>
        </div>
        <div class="card">
          <p class="label">Đánh giá</p>
          <h2>{{ (profile.rating ?? 0).toFixed(1) }}</h2>
          <span class="muted">Điểm uy tín</span>
        </div>
        <div class="card">
          <p class="label">Người theo dõi</p>
          <h2>{{ profile.followers ?? 0 }}</h2>
          <span class="muted">Tổng lượt theo dõi</span>
        </div>
        <div class="card">
          <p class="label">Tạo lúc</p>
          <h2 class="small">{{ createdText }}</h2>
          <span class="muted">Thời gian tạo hồ sơ</span>
        </div>
        <div class="card">
          <p class="label">Cập nhật</p>
          <h2 class="small">{{ updatedText }}</h2>
          <span class="muted">Lần cập nhật gần nhất</span>
        </div>
      </div>

      <div class="roles">
        <p class="label">Quyền hiện tại</p>
        <div class="role-chips">
          <span v-for="role in roles" :key="role" class="chip">{{ role }}</span>
          <span v-if="!roles.length" class="chip muted">Chưa có quyền</span>
        </div>
      </div>

      <div class="form">
        <div class="form-head">
          <div>
            <p class="eyebrow">Cập nhật</p>
            <h3>Chỉnh sửa thông tin</h3>
            <p class="muted">Nhập thông tin mới và nhấn lưu bên dưới.</p>
          </div>
          <div v-if="saveError" class="error">{{ saveError }}</div>
        </div>
        <div class="fields">
          <label>
            <span>Tên hiển thị</span>
            <input v-model="form.username" type="text" placeholder="Nhập tên" />
          </label>
          <label>
            <span>Email</span>
            <input v-model="form.email" type="email" placeholder="Nhập email" />
          </label>
          <label>
            <span>Mật khẩu mới</span>
            <input v-model="form.password" type="password" placeholder="Để trống nếu giữ nguyên" />
          </label>
        </div>
        <div class="actions">
          <button class="primary" type="button" :disabled="saving" @click="save">
            {{ saving ? 'Đang lưu...' : 'Lưu thông tin' }}
          </button>
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

h2.small {
  font-size: 18px;
}

.roles {
  display: grid;
  gap: 8px;
}

.role-chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.form {
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 16px;
  background: var(--surface);
  box-shadow: var(--shadow);
  display: grid;
  gap: 12px;
}

.form-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.fields {
  display: grid;
  gap: 10px;
}

label {
  display: grid;
  gap: 6px;
  font-weight: 700;
}

input {
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid var(--border);
  background: var(--chip-bg, #f7f9fc);
}

.actions {
  display: flex;
  justify-content: flex-end;
}

.primary {
  padding: 12px 18px;
  border-radius: 12px;
  background: linear-gradient(135deg, #0ea5e9, #2563eb);
  color: #fff;
  border: none;
  font-weight: 800;
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.25);
}

.primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.error {
  color: #b91c1c;
  font-weight: 700;
}

.empty {
  text-align: center;
}
</style>
