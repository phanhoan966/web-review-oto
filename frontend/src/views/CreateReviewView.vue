<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import client from '../api/client'
import { useAuthStore } from '../stores/auth'

interface BrandOption {
  id: number
  name: string
}

const auth = useAuthStore()
const router = useRouter()
const loading = ref(false)
const brands = ref<BrandOption[]>([])
const form = ref({
  title: '',
  excerpt: '',
  content: '',
  heroImageUrl: '',
  vehicleModel: '',
  vehicleYear: '',
  fuelType: '',
  priceSegment: '',
  brandId: ''
})
const errorMsg = ref('')

onMounted(loadBrands)

async function loadBrands() {
  try {
    const { data } = await client.get('/brands/featured')
    brands.value = data || []
  } catch (error) {
    brands.value = []
  }
}

async function submit() {
  loading.value = true
  errorMsg.value = ''
  try {
    await client.post('/reviews', {
      title: form.value.title,
      excerpt: form.value.excerpt,
      content: form.value.content,
      heroImageUrl: form.value.heroImageUrl,
      vehicleModel: form.value.vehicleModel,
      vehicleYear: form.value.vehicleYear ? Number(form.value.vehicleYear) : null,
      fuelType: form.value.fuelType,
      priceSegment: form.value.priceSegment,
      brandId: form.value.brandId ? Number(form.value.brandId) : null
    })
    router.push({ name: 'feed' })
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || 'Tạo bài viết thất bại'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page">
    <div class="form-card surface">
      <h1>Tạo bài review</h1>
      <p class="sub">Bài viết sẽ ở trạng thái chờ duyệt.</p>
      <form @submit.prevent="submit">
        <label>Tiêu đề</label>
        <input v-model="form.title" required maxlength="200" />

        <label>Tóm tắt</label>
        <textarea v-model="form.excerpt" required maxlength="256" rows="2" />

        <label>Nội dung</label>
        <textarea v-model="form.content" required maxlength="2048" rows="6" />

        <label>Ảnh hero (URL)</label>
        <input v-model="form.heroImageUrl" placeholder="https://..." />

        <label>Mẫu xe</label>
        <input v-model="form.vehicleModel" />

        <label>Năm</label>
        <input v-model="form.vehicleYear" type="number" />

        <label>Nhiên liệu</label>
        <input v-model="form.fuelType" />

        <label>Phân khúc giá</label>
        <input v-model="form.priceSegment" />

        <label>Hãng xe</label>
        <select v-model="form.brandId" required>
          <option value="" disabled>Chọn hãng</option>
          <option v-for="b in brands" :key="b.id" :value="b.id">{{ b.name }}</option>
        </select>

        <button type="submit" :disabled="loading">{{ loading ? 'Đang gửi...' : 'Gửi' }}</button>
        <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
      </form>
    </div>
  </div>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 40px 16px;
  background: var(--bg);
}

.form-card {
  width: min(720px, 100%);
  padding: 28px;
  border-radius: 20px;
  box-shadow: var(--shadow);
  display: grid;
  gap: 12px;
}

h1 {
  margin: 0;
  font-size: 26px;
}

.sub {
  margin: 6px 0 12px;
  color: var(--muted);
}

form {
  display: grid;
  gap: 10px;
}

label {
  font-weight: 600;
  color: #1f2937;
}

input,
textarea,
select {
  padding: 12px;
  border-radius: 12px;
  border: 1px solid var(--border);
  background: #f7f9fc;
  font-size: 15px;
}

textarea {
  resize: vertical;
}

button {
  margin-top: 6px;
  padding: 12px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #0ea5e9, #2563eb);
  color: white;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 12px 30px rgba(37, 99, 235, 0.35);
}

button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.error {
  color: #b91c1c;
  font-weight: 600;
}
</style>
