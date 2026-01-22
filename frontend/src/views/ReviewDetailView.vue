<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import client from '../api/client'

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

onMounted(() => {
  load()
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

async function loadComments() {
  commentsVisible.value = true
  commentsLoading.value = true
  commentsError.value = ''
  try {
    const { data } = await client.get<CommentDetail[]>(`/reviews/${route.params.id}/comments`)
    comments.value = data
  } catch (error: any) {
    commentsError.value = error.response?.data?.message || 'Không tải được bình luận'
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

async function submitComment() {
  const content = newComment.value.trim()
  if (!content) {
    commentsError.value = 'Vui lòng nhập nội dung bình luận'
    return
  }
  submitting.value = true
  commentsError.value = ''
  try {
    const { data } = await client.post<CommentDetail>(`/reviews/${route.params.id}/comments`, { content })
    comments.value = [data, ...comments.value]
    newComment.value = ''
    commentsVisible.value = true
    if (review.value) {
      review.value.commentsCount = (review.value.commentsCount ?? 0) + 1
    }
  } catch (error: any) {
    commentsError.value = error.response?.data?.message || 'Gửi bình luận thất bại'
  } finally {
    submitting.value = false
  }
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
          <div class="reply-head">Đăng câu trả lời của bạn</div>
          <form class="comment-form" @submit.prevent="submitComment">
            <textarea v-model="newComment" rows="3" placeholder="Viết bình luận của bạn..." />
            <div class="comment-actions">
              <button class="ghost" type="button" @click="loadComments" :disabled="commentsLoading">
                {{ commentsVisible ? 'Tải lại bình luận' : 'Hiển thị bình luận' }}
              </button>
              <button class="primary" type="submit" :disabled="submitting">
                {{ submitting ? 'Đang gửi...' : 'Trả lời' }}
              </button>
            </div>
          </form>
        </section>

        <section class="card comments">
          <div class="comments-head">
            <h3>Bình luận</h3>
            <p class="muted">Bất kỳ ai cũng có thể xem và bình luận</p>
          </div>
          <div v-if="commentsLoading" class="status">Đang tải bình luận...</div>
          <div v-else-if="commentsError" class="status error">{{ commentsError }}</div>
          <div v-else-if="commentsVisible">
            <div v-if="!comments.length" class="status">Chưa có bình luận</div>
            <div v-else class="comment-list">
              <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <div class="comment-avatar">
                  <img :src="comment.authorAvatar || 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=60'" alt="avatar" />
                </div>
                <div class="comment-content">
                  <div class="comment-meta">
                    <strong>{{ comment.authorName || 'Ẩn danh' }}</strong>
                    <span class="muted">{{ formatDate(comment.createdAt) }}</span>
                  </div>
                  <p>{{ comment.content }}</p>
                </div>
              </div>
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

.comment-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
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

</style>