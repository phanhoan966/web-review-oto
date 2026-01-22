<script setup lang="ts">
import { onMounted, ref, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import client from '../api/client'
import { useAuthStore } from '../stores/auth'

interface ReviewDetail {
  id: number
  title: string
  excerpt: string
  content: string
  heroImageUrl: string
  brand?: string
  vehicleModel?: string
  vehicleYear?: number
  fuelType?: string
  priceSegment?: string
  likes?: number
  commentsCount?: number
  views?: number
  publishedAt?: string
  authorName?: string
  authorAvatar?: string
}

interface CommentDetail {
  id: number
  content: string
  authorName?: string
  authorAvatar?: string
  createdAt?: string
}

const route = useRoute()
const review = ref<ReviewDetail | null>(null)
const loading = ref(false)
const errorMsg = ref('')

const comments = ref<CommentDetail[]>([])
const commentsVisible = ref(false)
const commentsLoading = ref(false)
const commentsError = ref('')
const newComment = ref('')
const submitting = ref(false)

const page = ref(0)
const pageSize = 10
const hasMore = ref(true)

const commentsSection = ref<HTMLElement | null>(null)
const highlightedIds = ref<Set<number>>(new Set())
let highlightTimer: number | undefined

const auth = useAuthStore()
const replyMode = ref<'auth' | 'anon'>('auth')
const modalVisible = ref(false)
const modalEmail = ref('')
const modalError = ref('')

onMounted(() => {
  load()
  loadComments(true, false)
})

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const { data } = await client.get(`/reviews/${route.params.id}`)
    review.value = data
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || 'Không tìm thấy bài viết'
  } finally {
    loading.value = false
  }
}

async function loadComments(reset = false, autoScroll = true, highlightNew = true) {
  if (reset) {
    page.value = 0
    hasMore.value = true
    comments.value = []
  }
  if (!hasMore.value && !reset) {
    return
  }
  commentsVisible.value = true
  commentsLoading.value = true
  commentsError.value = ''
  try {
    const { data } = await client.get<CommentDetail[]>(`/reviews/${route.params.id}/comments`, {
      params: { page: page.value, size: pageSize }
    })
    comments.value = reset ? data : [...comments.value, ...data]
    if (data.length < pageSize) {
      hasMore.value = false
    } else {
      page.value += 1
    }
    if (autoScroll) {
      await nextTick()
      commentsSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
    if (highlightNew) {
      markHighlighted(data)
    }
  } catch (error: any) {
    commentsError.value = error.response?.data?.message || 'Không tải được bình luận'
    if (reset) {
      comments.value = []
    }
  } finally {
    commentsLoading.value = false
  }
}

function openModal(mode: 'auth' | 'anon') {
  replyMode.value = mode
  modalError.value = ''
  modalEmail.value = auth.user?.email || ''
  if (mode === 'auth' && auth.user) {
    modalVisible.value = false
    submitComment()
    return
  }
  modalVisible.value = true
}

function validateEmail(value: string) {
  return /^[\w-.]+@[\w-]+\.[\w-.]+$/.test(value)
}

async function submitComment() {
  const content = newComment.value.trim()
  if (!content) {
    commentsError.value = 'Vui lòng nhập nội dung bình luận'
    return
  }

  if (replyMode.value === 'auth') {
    if (!auth.user) {
      modalError.value = 'Bạn cần đăng nhập để trả lời'
      modalVisible.value = true
      return
    }
    if (!validateEmail(modalEmail.value) || modalEmail.value !== auth.user.email) {
      modalError.value = 'Email không đúng với tài khoản của bạn'
      modalVisible.value = true
      return
    }
  }

  if (replyMode.value === 'anon') {
    if (!validateEmail(modalEmail.value)) {
      modalError.value = 'Email không hợp lệ'
      modalVisible.value = true
      return
    }
    if (auth.user && modalEmail.value !== auth.user.email) {
      modalError.value = 'Email không đúng với tài khoản của bạn'
      modalVisible.value = true
      return
    }
  }

  submitting.value = true
  commentsError.value = ''
  modalVisible.value = false
  try {
    const { data } = await client.post<CommentDetail>(`/reviews/${route.params.id}/comments`, { content })
    if (replyMode.value === 'auth' && auth.user) {
      data.authorName = auth.user.username
      data.authorAvatar = auth.user.avatarUrl
    }
    comments.value = [data, ...comments.value]
    newComment.value = ''
    commentsVisible.value = true
    markHighlighted([data])
    if (review.value) {
      review.value.commentsCount = (review.value.commentsCount ?? 0) + 1
    }
  } catch (error: any) {
    commentsError.value = error.response?.data?.message || 'Gửi bình luận thất bại'
  } finally {
    submitting.value = false
  }
}

function markHighlighted(items: CommentDetail[]) {
  highlightedIds.value = new Set(items.map((c) => c.id))
  if (highlightTimer) {
    clearTimeout(highlightTimer)
  }
  highlightTimer = window.setTimeout(() => {
    highlightedIds.value = new Set()
  }, 3000)
}

function formatDate(value?: string) {
  if (!value) return ''
  return new Date(value).toLocaleString('vi-VN')
}
</script>

<template>
  <div class="page">
    <div class="container layout" v-if="!loading && !errorMsg && review">
      <div class="main">
        <article class="card">
          <header class="post-head">
            <div class="author-block">
              <img class="avatar" :src="review.authorAvatar || 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=60'" alt="avatar" />
              <div>
                <div class="name-row">
                  <strong>{{ review.authorName || 'Reviewer' }}</strong>
                  <span class="muted">· {{ formatDate(review.publishedAt) || 'Vừa đăng' }}</span>
                </div>
                <div class="eyebrow">{{ review.brand || 'Xe' }}</div>
              </div>
            </div>
            <div class="head-actions">
              <button class="ghost">Đăng ký</button>
              <div class="dots">⋯</div>
            </div>
          </header>

          <div class="title">{{ review.title }}</div>
          <p class="excerpt">{{ review.excerpt }}</p>
          <div class="body" v-html="review.content" />

          <div class="hero" v-if="review.heroImageUrl">
            <img :src="review.heroImageUrl" :alt="review.title" />
          </div>

          <div class="meta-row">
            <div class="chips">
              <span v-if="review.vehicleYear" class="chip">{{ review.vehicleYear }}</span>
              <span v-if="review.fuelType" class="chip">{{ review.fuelType }}</span>
              <span v-if="review.priceSegment" class="chip">{{ review.priceSegment }}</span>
            </div>
            <div class="muted">{{ review.views ?? 0 }} lượt xem</div>
          </div>

          <div class="actions">
            <button class="pill">💬 {{ review.commentsCount ?? 0 }}</button>
            <button class="pill">🔁 Chia sẻ</button>
            <button class="pill">❤ {{ review.likes ?? 0 }}</button>
            <button class="pill">🔖 Lưu</button>
          </div>
        </article>

        <section class="card reply">
          <div class="reply-head">Cho review của bạn về xe</div>
          <form class="comment-form" @submit.prevent>
            <textarea v-model="newComment" rows="3" placeholder="Viết bình luận của bạn..." />
            <div v-if="commentsError" class="form-error">{{ commentsError }}</div>
            <div class="comment-actions">
              <button class="ghost" type="button" @click="loadComments(true)" :disabled="commentsLoading">
                {{ commentsVisible ? 'Tải lại bình luận' : 'Hiển thị bình luận' }}
              </button>
              <div class="action-group">
                <button class="ghost" type="button" @click="openModal('anon')" :disabled="submitting">Trả lời ẩn danh</button>
                <button class="primary" type="button" @click="openModal('auth')" :disabled="submitting">
                  {{ submitting ? 'Đang gửi...' : 'Trả lời' }}
                </button>
              </div>
            </div>
          </form>
        </section>

        <section class="card comments" ref="commentsSection">
          <div class="comments-head">
            <h3>Bình luận ({{ comments.length }})</h3>
          </div>
          <div v-if="commentsLoading" class="status">Đang tải bình luận...</div>
          <div v-else-if="commentsError" class="status error">{{ commentsError }}</div>
          <div v-else-if="commentsVisible">
            <div v-if="!comments.length" class="status">Chưa có bình luận</div>
            <div v-else class="comment-list">
              <div
                v-for="comment in comments"
                :key="comment.id"
                class="comment-item"
                :class="{ flash: highlightedIds.has(comment.id) }"
              >
                <div class="comment-avatar">
                  <img :src="comment.authorAvatar || 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=60'" alt="avatar" />
                </div>
                <div class="comment-content">
                  <div class="comment-meta">
                    <strong>{{ comment.authorName || 'Ẩn danh' }}</strong>
                    <span class="date-time-comment muted">{{ formatDate(comment.createdAt) }}</span>
                  </div>
                  <p>{{ comment.content }}</p>
                </div>
              </div>
              <button v-if="hasMore && !commentsLoading" class="ghost load-more" type="button" @click="loadComments(false, true)">Xem thêm bình luận</button>
            </div>
          </div>
        </section>
      </div>

      <aside class="side">
        <div class="card side-card">
          <input class="search" placeholder="Tìm kiếm" />
        </div>
        <div class="card side-card">
          <div class="side-title">Người liên quan</div>
          <div class="side-user">
            <div>
              <div class="name">{{ review.authorName || 'Reviewer' }}</div>
              <div class="muted">Chuyên gia xe và đánh giá</div>
            </div>
            <button class="ghost">Theo dõi</button>
          </div>
        </div>
        <div class="card side-card">
          <div class="side-title">Tin nổi bật</div>
          <ul class="trends">
            <li>Xe điện</li>
            <li>Hybrid</li>
            <li>Suất ưu đãi</li>
          </ul>
        </div>
      </aside>
    </div>

    <div v-else class="status" :class="{ error: Boolean(errorMsg) }">
      {{ loading ? 'Đang tải...' : errorMsg || 'Không tìm thấy bài viết' }}
    </div>

    <div v-if="modalVisible" class="modal">
      <div class="modal-card">
        <h4>Xác nhận email</h4>
        <p class="muted">Nhập email {{ replyMode === 'auth' ? 'tài khoản' : 'của bạn' }} để tiếp tục</p>
        <input v-model="modalEmail" type="email" placeholder="your@email.com" />
        <div v-if="modalError" class="modal-error">{{ modalError }}</div>
        <div class="modal-actions">
          <button class="ghost" type="button" @click="modalVisible = false">Huỷ</button>
          <button class="primary" type="button" @click="submitComment">Xác nhận</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.page {
  background: #f5f6f8;
  min-height: 100vh;
  padding: 20px 0 40px;
}

.layout {
  display: grid;
  grid-template-columns: minmax(0, 720px) 320px;
  gap: 20px;
}

@media (max-width: 1024px) {
  .layout {
    grid-template-columns: 1fr;
  }
}

.card {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(17, 24, 39, 0.08);
  padding: 18px;
}

.main {
  display: grid;
  gap: 16px;
}

.post-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.author-block {
  display: flex;
  gap: 12px;
  align-items: center;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
}

.name-row {
  display: flex;
  gap: 6px;
  align-items: center;
}

.eyebrow {
  margin-top: 2px;
  color: var(--muted);
  font-weight: 600;
  letter-spacing: 0.01em;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dots {
  font-size: 20px;
  line-height: 1;
  padding: 6px 10px;
  border-radius: 999px;
  background: #f3f4f6;
  cursor: pointer;
}

.title {
  font-size: 22px;
  font-weight: 800;
  margin-top: 6px;
}

.excerpt {
  margin: 6px 0;
  font-size: 16px;
  color: #2f3c4f;
}

.body {
  color: #1f2a3d;
  line-height: 1.7;
  white-space: pre-wrap;
}

.hero {
  border-radius: 16px;
  overflow: hidden;
  margin-top: 10px;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chip {
  padding: 6px 10px;
  border-radius: 12px;
  background: #eef2ff;
  color: #1d4ed8;
  font-weight: 700;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.pill {
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  border-radius: 999px;
  padding: 8px 12px;
  font-weight: 700;
  color: #111827;
}

.reply-head {
  font-weight: 700;
  margin-bottom: 8px;
}

.comment-form {
  display: grid;
  gap: 10px;
}

.comment-form textarea {
  width: 100%;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 12px;
  font-size: 15px;
  resize: vertical;
  background: #f9fafb;
}

.form-error {
  color: #b91c1c;
  font-weight: 700;
  margin-top: 6px;
}

.comment-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.action-group {
  display: flex;
  gap: 8px;
}

.primary,
.ghost,
.pill {
  cursor: pointer;
}

.primary {
  background: #0ea5e9;
  color: #fff;
  border: none;
  padding: 10px 16px;
  border-radius: 999px;
  font-weight: 700;
}

.ghost {
  background: #f3f4f6;
  color: #111827;
  border: 1px solid #e5e7eb;
  padding: 8px 14px;
  border-radius: 999px;
  font-weight: 700;
}

.comments {
  display: grid;
  gap: 12px;
}

.comment-list {
  display: grid;
  gap: 12px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #edf2f7;
}

.comment-item.flash {
  animation: flash 3s ease;
}

@keyframes flash {
  0% {
    background: #fff7ed;
  }
  40% {
    background: #fff7ed;
  }
  100% {
    background: #f8fafc;
  }
}

.comment-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.comment-content {
  display: grid;
  gap: 6px;
}

.comment-meta {
  display: flex;
  gap: 8px;
  align-items: baseline;
}

.comment-content p {
  margin: 0;
  color: #1f2a3d;
}

.side {
  display: grid;
  gap: 12px;
}

.side-card {
  display: grid;
  gap: 12px;
}

.search {
  width: 100%;
  padding: 10px 14px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
}

.side-title {
  font-weight: 800;
}

.side-user {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.trends {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 8px;
  color: #1f2937;
  font-weight: 700;
}

.status {
  text-align: center;
  color: var(--muted);
  padding: 12px 0;
}

.error {
  color: #b91c1c;
}

.modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: grid;
  place-items: center;
  z-index: 20;
}

.modal-card {
  background: #fff;
  padding: 18px;
  border-radius: 16px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.1);
  width: min(420px, 90vw);
  display: grid;
  gap: 10px;
}

.modal-card input {
  width: 100%;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.modal-error {
  color: #b91c1c;
  font-weight: 700;
}

.date-time-comment {
  font-size: smaller;
}

</style>