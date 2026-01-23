<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import client from '../../api/client'

interface BrandOption {
  id: number
  name: string
}

const router = useRouter()
const loading = ref(false)
const brands = ref<BrandOption[]>([])
const form = ref({
  title: '',
  slug: '',
  excerpt: '',
  content: '',
  heroImageUrl: '',
  vehicleModel: '',
  vehicleYear: '',
  fuelType: '',
  priceSegment: '',
  brandId: ''
})
const heroPreview = ref('')
const uploadError = ref('')
const uploading = ref(false)
const errorMsg = ref('')

watch(
  () => form.value.title,
  (val) => {
    form.value.slug = slugify(val)
  }
)

watch(
  () => form.value.heroImageUrl,
  (val) => {
    if (val && !val.startsWith('data:')) {
      heroPreview.value = val
    }
    if (!val) {
      heroPreview.value = ''
    }
  }
)

onMounted(loadBrands)

async function loadBrands() {
  try {
    const { data } = await client.get('/brands/featured')
    brands.value = data || []
  } catch (error) {
    brands.value = []
  }
}

function slugify(value: string) {
  return value
    .toLowerCase()
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '')
    .trim()
}

async function onHeroFile(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  uploadError.value = ''
  uploading.value = true
  try {
    const payload = new FormData()
    payload.append('file', file)
    const { data } = await client.post<{ url: string }>('/uploads', payload, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    form.value.heroImageUrl = data.url
    heroPreview.value = data.url
  } catch (error: any) {
    uploadError.value = error.response?.data?.message || 'Upload ảnh thất bại'
  } finally {
    uploading.value = false
  }
}

async function submit() {
  const heroUrl = form.value.heroImageUrl?.trim() || ''
  if (uploading.value) {
    errorMsg.value = 'Đang upload ảnh, vui lòng đợi'
    return
  }
  if (heroUrl.startsWith('data:')) {
    errorMsg.value = 'Vui lòng upload ảnh để lấy URL'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    await client.post('/reviews', {
      title: form.value.title,
      excerpt: form.value.excerpt,
      content: form.value.content,
      heroImageUrl: heroUrl || null,
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

        <label>Slug SEO (tự sinh từ tiêu đề)</label>
        <input v-model="form.slug" readonly />
        <p class="muted small">Đường dẫn xem trước: http://localhost:5173/post/{{ form.slug || 'tieu-de' }}</p>

        <label>Tóm tắt</label>
        <textarea v-model="form.excerpt" required maxlength="256" rows="2" />

        <label>Nội dung</label>
        <textarea v-model="form.content" required maxlength="2048" rows="6" />

        <label>Ảnh đại diện (upload hoặc dán URL)</label>
        <div class="hero-row">
          <input v-model="form.heroImageUrl" placeholder="https://..." />
          <label class="upload-btn">
            Upload
            <input type="file" accept="image/*" :disabled="uploading" @change="onHeroFile" />
          </label>
        </div>
        <p v-if="uploadError" class="error">{{ uploadError }}</p>
        <div v-if="form.heroImageUrl || heroPreview" class="hero-preview">
          <img :src="heroPreview || form.heroImageUrl" alt="preview" />
        </div>

        <div class="row">
          <div>
            <label>Hãng xe</label>
            <select v-model="form.brandId" required>
              <option value="" disabled>Chọn hãng</option>
              <option v-for="b in brands" :key="b.id" :value="b.id">{{ b.name }}</option>
            </select>
          </div>
          <div>
            <label>Mẫu xe</label>
            <input v-model="form.vehicleModel" />
          </div>
        </div>

        <div class="row">
          <div>
            <label>Năm</label>
            <input v-model="form.vehicleYear" type="number" />
          </div>
          <div>
            <label>Nhiên liệu</label>
            <input v-model="form.fuelType" />
          </div>
          <div>
            <label>Phân khúc giá</label>
            <input v-model="form.priceSegment" />
          </div>
        </div>

        <button type="submit" :disabled="loading || uploading">{{ loading ? 'Đang gửi...' : uploading ? 'Đang upload ảnh...' : 'Gửi' }}</button>
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
  width: min(780px, 100%);
  padding: 28px;
  border-radius: 20px;
  box-shadow: var(--shadow);
  display: grid;
  gap: 16px;
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
  gap: 12px;
}

.small {
  font-size: 13px;
}

.hero-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  align-items: center;
}

.upload-btn {
  padding: 10px 14px;
  border: 1px dashed var(--border);
  border-radius: 12px;
  cursor: pointer;
  font-weight: 700;
  background: #f9fbff;
  color: #1f2a3d;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  position: relative;
  overflow: hidden;
}

.upload-btn input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.hero-preview {
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 10px;
  background: #f7f9fc;
}

.hero-preview img {
  width: 100%;
  border-radius: 12px;
  object-fit: cover;
  max-height: 260px;
}

.row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
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
