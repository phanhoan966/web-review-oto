<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import ClassicEditor from '@ckeditor/ckeditor5-build-classic'
import client from '../../api/client'
import { buildAssetUrl } from '../utils/assetUrl'

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
const slugEdited = ref(false)
const heroPreview = ref('')
const currentYear = new Date().getFullYear()
const years = Array.from({ length: currentYear - 1899 }, (_, idx) => currentYear - idx)
const uploadError = ref('')
const uploading = ref(false)
const errorMsg = ref('')
const editorHost = ref<HTMLElement | null>(null)
const editorReady = ref(false)
const editorError = ref('')
let editorInstance: any = null

watch(
  () => form.value.title,
  (val) => {
    if (slugEdited.value) return
    form.value.slug = slugify(val)
  }
)

watch(
  () => form.value.heroImageUrl,
  (val) => {
    if (val && !val.startsWith('data:')) {
      heroPreview.value = buildAssetUrl(val)
    }
    if (!val) {
      heroPreview.value = ''
    }
  }
)

onMounted(async () => {
  await loadBrands()
  await initEditor()
})

onBeforeUnmount(async () => {
  if (editorInstance?.destroy) {
    await editorInstance.destroy()
  }
})

async function loadBrands() {
  try {
    const { data } = await client.get('/brands/featured')
    brands.value = data || []
  } catch (error) {
    brands.value = []
  }
}

function slugify(value: string) {
  // return value
  //   .toLowerCase()
  //   .normalize('NFD')
  //   .replace(/\p{Diacritic}/gu, '')
  //   .replace(/[^a-z0-9]+/g, '-')
  //   .replace(/(^-|-$)/g, '')
  //   .trim()
  return value
    .toLowerCase()
    .replace(/đ/g, "d")                   // FIX đ
    .normalize("NFD")                     // tách dấu
    .replace(/[\u0300-\u036f]/g, "")     // xoá dấu
    .replace(/[^a-z0-9]+/g, "-")         // ký tự lạ → -
    .replace(/^-+|-+$/g, "")             // xoá - đầu/cuối
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
    const { data } = await client.post<{ path?: string; url: string }>('/uploads', payload, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const storedPath = data.path || data.url
    form.value.heroImageUrl = storedPath
    heroPreview.value = buildAssetUrl(storedPath)
  } catch (error: any) {
    uploadError.value = error.response?.data?.message || 'Upload ảnh thất bại'
  } finally {
    uploading.value = false
  }
}

async function initEditor() {
  editorReady.value = false
  editorError.value = ''
  if (!editorHost.value) {
    editorError.value = 'Không tìm thấy vùng nhập nội dung'
    return
  }
  try {
    editorInstance = await ClassicEditor.create(editorHost.value as HTMLElement, {
      placeholder: 'Viết nội dung bài review...',
      toolbar: ['heading', '|', 'bold', 'italic', 'link', '|', 'bulletedList', 'numberedList', 'outdent', 'indent', '|', 'blockQuote', 'insertTable', 'imageUpload', '|', 'undo', 'redo']
    })
    editorInstance.plugins.get('FileRepository').createUploadAdapter = (loader: any) => createUploadAdapter(loader)
    if (form.value.content) {
      editorInstance.setData(form.value.content)
    }
    editorInstance.model.document.on('change:data', () => {
      form.value.content = editorInstance.getData()
    })
    editorReady.value = true
  } catch (error: any) {
    editorError.value = 'Không tải được trình soạn thảo'
  }
}

function createUploadAdapter(loader: any) {
  return {
    async upload() {
      const file = await loader.file
      const payload = new FormData()
      payload.append('file', file)
      const { data } = await client.post<{ path?: string; url: string }>('/uploads', payload, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      const storedPath = data.path || data.url
      const url = buildAssetUrl(storedPath)
      return { default: url }
    },
    abort() {}
  }
}

function handleSlugInput() {
  slugEdited.value = true
}

function normalizeSlug() {
  form.value.slug = slugify(form.value.slug || '')
}

function resolveBrandId() {
  const typed = (form.value.brandId || '').trim()
  if (!typed) return null
  const byId = brands.value.find((b) => String(b.id) === typed)
  if (byId) return byId.id
  const byName = brands.value.find((b) => b.name.toLowerCase() === typed.toLowerCase())
  return byName ? byName.id : null
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
  if (!editorReady.value) {
    errorMsg.value = 'Trình soạn thảo chưa sẵn sàng'
    return
  }
  if (!form.value.content || !form.value.content.trim()) {
    errorMsg.value = 'Nội dung không được để trống'
    return
  }
  const brandId = resolveBrandId()
  loading.value = true
  errorMsg.value = ''
  try {
    await client.post('/reviews', {
      title: form.value.title,
      slug: form.value.slug,
      excerpt: form.value.excerpt,
      content: form.value.content,
      heroImageUrl: heroUrl || null,
      vehicleModel: form.value.vehicleModel,
      vehicleYear: form.value.vehicleYear ? Number(form.value.vehicleYear) : null,
      fuelType: form.value.fuelType,
      priceSegment: form.value.priceSegment,
      brandId
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
  <div class="container page">
    <div class="form-card surface">
      <h1>Tạo bài review</h1>
      <p class="sub">Bài viết sẽ ở trạng thái chờ duyệt.</p>
      <form @submit.prevent="submit">
        <label>Tiêu đề<span class="required">*</span></label>
        <input v-model="form.title" required maxlength="200" />

        <label>Slug SEO (tự sinh từ tiêu đề)</label>
        <input v-model="form.slug" required maxlength="200" @input="handleSlugInput" @blur="normalizeSlug" />
        <p class="muted small">Đường dẫn xem trước: http://localhost:5173/post/{{ form.slug || 'tieu-de' }}/:id</p>

        <label>Ảnh đại diện (upload hoặc dán URL)</label>
        <div class="hero-row">
          <input v-model="form.heroImageUrl" placeholder="https://..." />
          <label class="upload-btn">
            Upload
            <input type="file" accept="image/*" :disabled="uploading" @change="onHeroFile" />
          </label>
        </div>

        <label>Tóm tắt<span class="required">*</span></label>
        <textarea v-model="form.excerpt" required maxlength="256" rows="2" />

        <label>Nội dung<span class="required">*</span></label>
        <div class="editor-shell" :class="{ ready: editorReady }">
          <div ref="editorHost"></div>
          <p v-if="editorError" class="error">{{ editorError }}</p>
        </div>
        <p v-if="uploadError" class="error">{{ uploadError }}</p>
        <div v-if="form.heroImageUrl || heroPreview" class="hero-preview">
          <img :src="heroPreview || form.heroImageUrl" alt="preview" />
        </div>

        <div class="row">
          <div class="field">
            <label>Hãng xe<span class="required">*</span></label>
            <input v-model="form.brandId" list="brandOptions" required placeholder="Chọn hoặc tìm hãng" />
          </div>
          <div class="field">
            <label>Mẫu xe</label>
            <input v-model="form.vehicleModel" />
          </div>
        </div>

        <div class="row">
          <div class="field">
            <label>Năm</label>
            <input v-model="form.vehicleYear" type="number" :min="1900" :max="currentYear" list="yearOptions" />
          </div>
          <div class="field">
            <label>Nhiên liệu</label>
            <input v-model="form.fuelType" />
          </div>
        </div>
        <div class="row">
          <div class="field">
            <label>Phân khúc giá</label>
            <input v-model="form.priceSegment" />
          </div>
          <div class="field"></div>
        </div>

        <datalist id="brandOptions">
          <option v-for="b in brands" :key="b.id" :value="b.name">{{ b.name }}</option>
        </datalist>

        <datalist id="yearOptions">
          <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
        </datalist>

        <button type="submit" :disabled="loading || uploading">{{ loading ? 'Đang gửi...' : uploading ? 'Đang upload ảnh...' : 'Gửi' }}</button>
        <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
      </form>
    </div>
  </div>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 32px 16px 48px;
  background: var(--bg);
  background-image: radial-gradient(circle at 20% 20%, rgba(14, 165, 233, 0.08), transparent 32%), radial-gradient(circle at 80% 0%, rgba(79, 70, 229, 0.08), transparent 30%);
  display: grid;
  align-items: start;
  color: var(--text);
}

.form-card {
  width: 100%;
  max-width: 1080px;
  margin: 0 auto;
  padding: 32px;
  border-radius: 24px;
  box-shadow: var(--shadow);
  background: var(--surface);
  display: grid;
  gap: 18px;
  border: 1px solid var(--border);
}

h1 {
  margin: 0;
  font-size: 28px;
  letter-spacing: -0.01em;
}

.sub {
  margin: 6px 0 14px;
  color: var(--muted);
}

.required {
  color: #e11d48;
  margin-left: 4px;
}

form {
  display: grid;
  gap: 14px;
}

.small {
  font-size: 13px;
}

.editor-shell {
  border: 1px solid var(--border);
  border-radius: 16px;
  background: var(--surface);
  padding: 10px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.editor-shell.ready {
  border-color: var(--primary);
  box-shadow: 0 10px 30px rgba(14, 165, 233, 0.12);
}

.editor-shell :deep(.ck) {
  --ck-color-base-foreground: var(--text);
  --ck-color-base-background: var(--surface);
  --ck-color-text: var(--text);
  --ck-color-shadow-drop: rgba(0, 0, 0, 0.12);
  --ck-color-shadow-inner: rgba(0, 0, 0, 0.08);
  --ck-color-toolbar-background: var(--surface);
  --ck-color-toolbar-border: var(--border);
  --ck-color-button-default-background: var(--surface);
  --ck-color-button-default-hover-background: var(--chip-bg);
  --ck-color-button-default-active-background: var(--chip-bg);
  --ck-color-button-default-disabled-background: var(--surface);
  --ck-color-dropdown-panel-background: var(--surface);
  --ck-color-dropdown-panel-border: var(--border);
  --ck-color-list-background: var(--surface);
  --ck-color-list-button-hover-background: var(--chip-bg);
  --ck-color-input-background: var(--surface);
  --ck-color-input-border: var(--border);
  --ck-color-input-text: var(--text);
  --ck-color-input-disabled-background: var(--chip-bg);
  --ck-color-input-disabled-border: var(--border);
  --ck-color-input-disabled-text: var(--muted);
  background: var(--surface);
  color: var(--text);
}

.editor-shell :deep(.ck-editor) {
  background: var(--surface);
  color: var(--text);
}

.editor-shell :deep(.ck-toolbar) {
  background: var(--surface);
  border: none;
  border-bottom: 1px solid var(--border);
  border-radius: 12px 12px 0 0;
}

.editor-shell :deep(.ck-toolbar__separator) {
  background: var(--border);
}

.editor-shell :deep(.ck-button) {
  color: var(--text);
}

.editor-shell :deep(.ck-button.ck-on) {
  background: var(--chip-bg);
}

.editor-shell :deep(.ck-content a) {
  color: var(--primary);
}

.editor-shell :deep(.ck-placeholder) {
  color: var(--muted) !important;
}

.editor-shell :deep(.ck-editor__editable_inline) {
  min-height: 720px;
  background: var(--surface);
  color: var(--text);
  border: 1px solid var(--border);
  border-radius: 0 0 12px 12px;
}

.editor-shell :deep(.ck-content) {
  min-height: 720px;
  color: var(--text);
}

.editor-shell :deep(.ck-dropdown__panel),
.editor-shell :deep(.ck-balloon-panel) {
  background: var(--surface);
  color: var(--text);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.editor-shell :deep(.ck-list__item .ck-button) {
  color: var(--text);
}

.editor-shell :deep(.ck-list__item .ck-button.ck-on) {
  background: var(--chip-bg);
}

@media (max-width: 720px) {
  .page {
    padding: 18px 12px 28px;
  }

  .form-card {
    padding: 22px;
    border-radius: 18px;
  }

  .editor-shell :deep(.ck-editor__editable_inline),
  .editor-shell :deep(.ck-content) {
    min-height: 480px;
  }
}

.hero-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  align-items: center;
}

.upload-btn {
  padding: 11px 16px;
  border: 1px dashed var(--border);
  border-radius: 12px;
  cursor: pointer;
  font-weight: 700;
  background: var(--chip-bg);
  color: var(--text);
  display: inline-flex;
  align-items: center;
  gap: 8px;
  position: relative;
  overflow: hidden;
  transition: all 0.2s ease;
}

.upload-btn:hover {
  border-color: var(--primary);
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.12);
}

.upload-btn input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.hero-preview {
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 12px;
  background: var(--chip-bg);
}

.hero-preview img {
  width: 100%;
  border-radius: 12px;
  object-fit: cover;
  max-height: 280px;
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.12);
}

.row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
}

label {
  font-weight: 700;
  color: var(--text);
  display: inline-block;
  margin-bottom: 8px;
}

.field {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-right: 45px;
}

.field label {
  margin: 0;
  margin-bottom: 0;
}

.field input,
.field select,
.field textarea {
  flex: 1;
}

input,
textarea,
select {
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid var(--border);
  background: var(--surface);
  font-size: 15px;
  color: var(--text);
  transition: all 0.2s ease;
}

select {
  height: 46px;
  padding-right: 38px;
  appearance: none;
  -moz-appearance: none;
  -webkit-appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg width='16' height='16' viewBox='0 0 20 20' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M5 7l5 5 5-5' stroke='%23a0aec0' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-size: 14px;
  background-position: calc(100% - 12px) center;
  max-height: 400px;
  overflow-y: auto;
}

select::-ms-expand {
  display: none;
}

select option {
  color: var(--text);
  background: var(--surface);
  padding: 8px 10px;
}

select::-webkit-scrollbar {
  width: 4px;
}

select::-webkit-scrollbar-thumb {
  background: var(--border);
  border-radius: 999px;
}

select::-webkit-scrollbar-track {
  background: transparent;
}

input:focus,
textarea:focus,
select:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.2);
  background: var(--surface);
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
  color: #fff;
  font-weight: 800;
  cursor: pointer;
  box-shadow: 0 16px 36px rgba(37, 99, 235, 0.3);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

button:hover:enabled {
  transform: translateY(-1px);
  box-shadow: 0 18px 42px rgba(37, 99, 235, 0.32);
}

button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 12px 30px rgba(37, 99, 235, 0.18);
}

.error {
  color: #b91c1c;
  font-weight: 700;
}
</style>
