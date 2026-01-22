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
  <div class="container page">
    <div v-if="loading" class="status">Đang tải...</div>
    <div v-else-if="errorMsg" class="status error">{{ errorMsg }}</div>
    <article v-else-if="review" class="content-card surface">
      <div class="hero" v-if="review.heroImageUrl">
        <img :src="review.heroImageUrl" :alt="review.title" />
      </div>
      <div class="head">
        <p class="eyebrow">{{ review.brand || 'Xe' }}</p>
        <h1>{{ review.title }}</h1>
        <div class="meta">
          <div class="author">
            <img class="avatar" :src="review.authorAvatar || 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=60'" alt="avatar" />
            <span>{{ review.authorName }}</span>
          </div>
          <div class="chips">
            <span v-if="review.vehicleYear" class="chip">{{ review.vehicleYear }}</span>
            <span v-if="review.fuelType" class="chip">{{ review.fuelType }}</span>
            <span v-if="review.priceSegment" class="chip">{{ review.priceSegment }}</span>
          </div>
        </div>
      </div>
      <p class="excerpt">{{ review.excerpt }}</p>
      <div class="body" v-html="review.content" />
      <div class="metrics">
        <span>❤ {{ review.likes ?? 0 }}</span>
        <span>💬 {{ review.commentsCount ?? 0 }}</span>
        <span>👁 {{ review.views ?? 0 }} lượt xem</span>
      </div>

      <section class="comments">
        <div class="comments-head">
          <div>
            <h3>Bình luận</h3>
            <p class="muted">Bất kỳ ai cũng có thể chia sẻ cảm nghĩ và xem bình luận</p>
          </div>
          <button class="ghost" type="button" @click="loadComments" :disabled="commentsLoading">
            {{ commentsVisible ? 'Tải lại bình luận' : 'Hiển thị bình luận' }}
          </button>
        </div>

        <form class="comment-form" @submit.prevent="submitComment">
          <textarea v-model="newComment" rows="3" placeholder="Viết bình luận của bạn..." />
          <button class="primary" type="submit" :disabled="submitting">
            {{ submitting ? 'Đang gửi...' : 'Gửi bình luận' }}
          </button>
        </form>

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
    </article>
  </div>
</template>

<style scoped lang="scss">
.page {
  padding-top: 20px;
}

.content-card {
  padding: 18px;
  border-radius: 20px;
  box-shadow: var(--shadow);
  display: grid;
  gap: 14px;
}

.hero {
  border-radius: 18px;
  overflow: hidden;
  max-height: 420px;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.head {
  display: grid;
  gap: 8px;
}

.eyebrow {
  margin: 0;
  color: var(--muted);
  font-weight: 600;
  letter-spacing: 0.02em;
}

h1 {
  margin: 0;
  font-size: 28px;
  letter-spacing: -0.02em;
}

.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chip {
  padding: 6px 10px;
  border-radius: 12px;
  background: var(--chip-bg);
  color: #1f2a3d;
  font-weight: 600;
}

.excerpt {
  margin: 0;
  font-size: 16px;
  color: #2f3c4f;
}

.body {
  color: #1f2a3d;
  line-height: 1.7;
  white-space: pre-wrap;
}

.metrics {
  display: flex;
  gap: 16px;
  color: var(--muted);
  font-weight: 600;
}

.comments {
  border-top: 1px solid var(--border);
  padding-top: 12px;
  display: grid;
  gap: 12px;
}

.comments-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.comment-form {
  display: grid;
  gap: 10px;
}

.comment-form textarea {
  width: 100%;
  border-radius: 12px;
  border: 1px solid var(--border);
  padding: 12px;
  font-size: 15px;
  resize: vertical;
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

.status {
  text-align: center;
  color: var(--muted);
  padding: 12px 0;
}

.error {
  color: #b91c1c;
}
</style>
