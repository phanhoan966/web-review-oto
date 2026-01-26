<script setup lang="ts">
import { onMounted, ref, nextTick, computed, watch } from 'vue'
import { useRoute, RouterLink, useRouter } from 'vue-router'
import client from '../../api/client'
import { useAuthStore } from '../../stores/auth'
import { slugify } from '../utils/slugify'
import { buildAssetUrl } from '../utils/assetUrl'
import HoverPopover from '../components/common/HoverPopover.vue'
import ReviewerPopoverCard from '../components/common/ReviewerPopoverCard.vue'
import TopReviewers, { type ReviewerItem } from '../components/Sidebar/TopReviewers.vue'
import FeaturedBrands, { type BrandItem } from '../components/Sidebar/FeaturedBrands.vue'
import MostViewed, { type ViewedItem } from '../components/Sidebar/MostViewed.vue'

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
  authorUsername?: string
  authorFollowers?: number
  authorReviewCount?: number
  authorRating?: number
  authorBio?: string
}

interface CommentDetail {
  id: number
  content: string
  authorName?: string
  authorAvatar?: string
  authorUsername?: string
  authorFollowers?: number
  authorReviewCount?: number
  authorRating?: number
  authorBio?: string
  anonymous?: boolean
  createdAt?: string
  likes?: number
  parentId?: number | null
  children?: CommentDetail[]
}

const defaultAvatar = 'https://as1.ftcdn.net/v2/jpg/16/50/75/40/1000_F_1650754099_NnbV1a2Cgvj26kogaurRePYoipRlFEao.jpg'
const anonAvatar = 'https://as1.ftcdn.net/v2/jpg/16/50/75/40/1000_F_1650754099_NnbV1a2Cgvj26kogaurRePYoipRlFEao.jpg'

const route = useRoute()
const router = useRouter()
const review = ref<ReviewDetail | null>(null)
const heroSrc = computed(() => buildAssetUrl(review.value?.heroImageUrl || ''))
const profilePath = computed(() =>
  review.value?.authorUsername ? `/user/${encodeURIComponent(review.value.authorUsername)}` : ''
)
const loading = ref(false)
const errorMsg = ref('')

const reviewers = ref<ReviewerItem[]>([])
const brands = ref<BrandItem[]>([])
const mostViewed = ref<ViewedItem[]>([])

watch(
  () => review.value?.title,
  (title: string | undefined) => {
    document.title = title ? `${title} | Đánh Giá Xe` : 'Đánh Giá Xe'
  },
  { immediate: true }
)

const comments = ref<CommentDetail[]>([])
const commentsVisible = ref(false)
const commentsLoading = ref(false)
const commentsError = ref('')
const formError = ref('')
const newComment = ref('')
const submitting = ref(false)

const page = ref(0)
const pageSize = 10
const hasMore = ref(true)

const commentsSection = ref<HTMLElement | null>(null)
const commentList = ref<HTMLElement | null>(null)
const highlightedIds = ref<Set<number>>(new Set())
const slideIds = ref<Set<number>>(new Set())
const likesState = ref<Record<number, { count: number; liked: boolean }>>({})
const commentTab = ref<'top' | 'newest'>('top')
const replyTarget = ref<CommentDetail | null>(null)
const rootComposerVisible = ref(true)
const replyInputs: Record<number, HTMLTextAreaElement | null> = {}
const depthMap = ref<Record<number, number>>({})
let highlightTimer: number | undefined
let slideTimer: number | undefined

function flattenComments(list: CommentDetail[]): CommentDetail[] {
  return list.flatMap((item) => [item, ...(item.children ? flattenComments(item.children) : [])])
}

function mergeComments(items: CommentDetail[], reset = false) {
  const map = new Map<number, CommentDetail>()
  const sources = reset ? [] : flattenComments(comments.value)
  const upsert = (entry: CommentDetail) => {
    const existing = map.get(entry.id) || {}
    map.set(entry.id, { ...existing, ...entry, children: [] })
  }
  sources.forEach(upsert)
  items.forEach(upsert)
  map.forEach((node) => {
    if (!node.parentId) return
    const parent = map.get(node.parentId)
    if (parent) {
      parent.children = parent.children || []
      if (!parent.children.some((child) => child.id === node.id)) {
        parent.children.push(node)
      }
    }
  })
  const sortByDate = (a: CommentDetail, b: CommentDetail) => (a.createdAt || '').localeCompare(b.createdAt || '')
  map.forEach((node) => {
    if (node.children?.length) {
      node.children.sort(sortByDate)
    }
  })
  const roots: CommentDetail[] = []
  map.forEach((node) => {
    if (!node.parentId) {
      roots.push(node)
    }
  })
  roots.sort(sortByDate)
  const nextDepth: Record<number, number> = {}
  const stack: { node: CommentDetail; depth: number }[] = roots.map((node) => ({ node, depth: 1 }))
  while (stack.length) {
    const { node, depth } = stack.pop() as { node: CommentDetail; depth: number }
    nextDepth[node.id] = depth
    if (node.children?.length) {
      node.children.forEach((child) => stack.push({ node: child, depth: depth + 1 }))
    }
  }
  depthMap.value = nextDepth
  totalComments.value = map.size
  return roots
}

const auth = useAuthStore()
const modalVisible = ref(false)

const totalComments = ref(0)

const commentMap = computed(() => {
  const map: Record<number, CommentDetail> = {}
  flattenComments(comments.value).forEach((c) => {
    map[c.id] = c
  })
  return map
})

function childRows(root: CommentDetail) {
  const list = root.children || []
  const sortByDate = (a: CommentDetail, b: CommentDetail) => (a.createdAt || '').localeCompare(b.createdAt || '')
  return [...list].sort(sortByDate).flatMap((child) => [child, ...((child.children || []).sort(sortByDate))])
}

onMounted(() => {
  load()
  loadComments(true, false)
  loadSidebar()
})

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const { data } = await client.get(`/reviews/${route.params.id}`)
    if (data?.title) {
      route.params.slug = slugify(data.title)
    }
    review.value = data
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || 'Không tìm thấy bài viết'
  } finally {
    loading.value = false
  }
}

async function loadSidebar() {
  try {
    const [reviewersRes, brandsRes, viewedRes] = await Promise.all([
      client.get('/reviewers/top', { params: { limit: 3 } }),
      client.get('/brands/featured'),
      client.get('/reviews/most-viewed', { params: { limit: 3 } })
    ])
    reviewers.value = reviewersRes.data || []
    brands.value = brandsRes.data || []
    mostViewed.value = viewedRes.data || []
  } catch (error) {
    reviewers.value = []
    brands.value = []
    mostViewed.value = []
  }
}

function onBrandSelect(brand: BrandItem) {
  router.push({ name: 'feed-brand', params: { brand: brand.name } })
}

async function loadComments(reset = false, autoScroll = true, highlightNew = true) {
  if (reset) {
    page.value = 0
    hasMore.value = true
    comments.value = []
    rootComposerVisible.value = true
    replyTarget.value = null
  }
  if (!hasMore.value && !reset) {
    return
  }
  commentsVisible.value = true
  commentsLoading.value = true
  commentsError.value = ''
  try {
    const { data } = await client.get<CommentDetail[]>(`/reviews/${route.params.id}/comments`, {
      params: { page: page.value, size: pageSize, sort: commentTab.value === 'newest' ? 'latest' : 'top' }
    })
    const rootCount = Array.isArray(data) ? data.filter((item) => !item.parentId).length : 0
    comments.value = mergeComments(data, reset)
    initLikes(data)
    if (rootCount < pageSize) {
      hasMore.value = false
    } else {
      page.value += 1
    }
    if (autoScroll) {
      await nextTick()
      requestAnimationFrame(() => {
        const target = commentList.value?.lastElementChild as HTMLElement | null
        if (target) {
          target.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'nearest' })
        } else {
          commentsSection.value?.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'nearest' })
        }
      })
    }
    if (highlightNew) {
      markHighlighted(data)
    }
    markSlide(data)
  } catch (error: any) {
    commentsError.value = error.response?.data?.message || 'Không tải được bình luận'
    if (reset) {
      comments.value = []
    }
  } finally {
    commentsLoading.value = false
  }
}

async function submitComment() {
  const content = newComment.value.trim()
  if (!content) {
    formError.value = 'Vui lòng nhập nội dung bình luận'
    return
  }
  formError.value = ''
  if (!auth.user) {
    modalVisible.value = true
    return
  }
  submitting.value = true
  commentsError.value = ''
  modalVisible.value = false
  try {
    const target = replyTarget.value
    let parentId: number | null = null
    if (target?.id) {
      parentId = target.id
    }
    const { data } = await client.post<CommentDetail>(`/reviews/${route.params.id}/comments`, {
      content,
      anonymous: false,
      parentId
    })
    data.anonymous = false
    if (auth.user) {
      data.authorName = auth.user.username
      data.authorAvatar = auth.user.avatarUrl
    }
    comments.value = mergeComments([data], false)
    initLikes([data])
    newComment.value = ''
    replyTarget.value = null
    rootComposerVisible.value = true
    commentsVisible.value = true
    markHighlighted([data])
    markSlide([data])
    if (review.value) {
      review.value.commentsCount = (review.value.commentsCount ?? 0) + 1
    }
  } catch (error: any) {
    formError.value = error.response?.data?.message || 'Gửi bình luận thất bại'
  } finally {
    submitting.value = false
  }
}

function markHighlighted(items: CommentDetail[]) {
  if (!items.length) return
  highlightedIds.value = new Set(items.map((c) => c.id))
  if (highlightTimer) {
    clearTimeout(highlightTimer)
  }
  highlightTimer = window.setTimeout(() => {
    highlightedIds.value = new Set()
  }, 3000)
}

function initLikes(items: CommentDetail[]) {
  const next = { ...likesState.value }
  items.forEach((c) => {
    const existing = next[c.id]
    if (!existing) {
      next[c.id] = { count: c.likes ?? 0, liked: false }
    } else {
      next[c.id] = { ...existing, count: c.likes ?? existing.count }
    }
  })
  likesState.value = next
}

async function toggleLike(id: number) {
  const current = likesState.value[id] || { count: 0, liked: false }
  const liked = !current.liked
  const count = Math.max(0, current.count + (liked ? 1 : -1))
  likesState.value = { ...likesState.value, [id]: { count, liked } }
  try {
    if (liked) {
      await client.post(`/comments/${id}/like`)
    } else {
      await client.post(`/comments/${id}/unlike`)
    }
  } catch (error: any) {
    likesState.value = { ...likesState.value, [id]: current }
    commentsError.value = error.response?.data?.message || 'Không thể cập nhật lượt thích'
  }
}

function setReplyInputRef(id: number, el: HTMLTextAreaElement | null) {
  if (el) {
    replyInputs[id] = el
  } else {
    delete replyInputs[id]
  }
}

function canReply() {
  return true
}

function startReply(comment: CommentDetail) {
  if (!canReply()) return
  replyTarget.value = comment
  rootComposerVisible.value = false
  const prefix = comment.authorUsername || comment.authorName || 'người dùng'
  newComment.value = `@${prefix} `
  nextTick(() => {
    const input = replyInputs[comment.id]
    input?.focus()
    input?.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'nearest' })
  })
}

function goLogin() {
  modalVisible.value = false
  router.push({ name: 'login', query: { redirect: route.fullPath } })
}

function goRegister() {
  modalVisible.value = false
  router.push({ name: 'register', query: { redirect: route.fullPath } })
}

const visibleRoots = computed(() => {
  const list = comments.value || []
  const sortByDate = (a: CommentDetail, b: CommentDetail) => (b.createdAt || '').localeCompare(a.createdAt || '')
  if (commentTab.value === 'newest') {
    return [...list].sort(sortByDate)
  }
  return [...list].sort((a, b) => {
    const likeA = likesState.value[a.id]?.count ?? 0
    const likeB = likesState.value[b.id]?.count ?? 0
    if (likeB !== likeA) return likeB - likeA
    return sortByDate(a, b)
  })
})

function markSlide(items: CommentDetail[]) {
  if (!items.length) return
  slideIds.value = new Set(items.map((c) => c.id))
  if (slideTimer) {
    clearTimeout(slideTimer)
  }
  slideTimer = window.setTimeout(() => {
    slideIds.value = new Set()
  }, 600)
}

function cancelReply() {
  replyTarget.value = null
  rootComposerVisible.value = true
  newComment.value = ''
  formError.value = ''
}

function formatDate(value?: string) {
  if (!value) return ''
  return new Date(value).toLocaleString('vi-VN')
}

function displayContent(body?: string, parentLabel?: string) {
  if (!body) return ''
  if (!parentLabel) return body
  const trimmed = body.trimStart()
  const lowerLabel = parentLabel.toLowerCase()
  const prefixed = trimmed.toLowerCase()
  if (prefixed.startsWith(`@${lowerLabel}`)) {
    const sliced = trimmed.slice(parentLabel.length + 1).replace(/^[:\s]+/, '')
    return sliced.trimStart()
  }
  return body
}

function getParent(comment?: CommentDetail | null) {
  if (!comment?.parentId) return null
  return commentMap.value[comment.parentId] || null
}

function mentionLabel(comment?: CommentDetail | null) {
  const parent = getParent(comment)
  if (!parent) return ''
  return parent.authorName || parent.authorUsername || ''
}

function shouldShowMention(comment?: CommentDetail | null) {
  const parent = getParent(comment)
  if (!parent) return false
  return Boolean(comment?.parentId)
}

</script>

<template>
  <div class="container page">
    <div class="layout" v-if="!loading && !errorMsg && review">
      <div class="main">
        <article class="card">
          <header class="post-head">
            <div class="author-block">
              <HoverPopover v-if="profilePath">
                <template #trigger>
                  <div class="author-trigger">
                    <RouterLink class="avatar-link" :to="profilePath">
                      <img class="avatar" :src="review.authorAvatar || 'https://as1.ftcdn.net/v2/jpg/16/50/75/40/1000_F_1650754099_NnbV1a2Cgvj26kogaurRePYoipRlFEao.jpg'" alt="avatar" />
                    </RouterLink>
                    <div>
                      <div class="name-row">
                        <RouterLink class="name-link" :to="profilePath">{{ review.authorName || 'Reviewer' }}</RouterLink>
                        <span class="muted">· {{ formatDate(review.publishedAt) || 'Vừa đăng' }}</span>
                      </div>
                      <div class="eyebrow">{{ review.brand || 'Xe' }}</div>
                    </div>
                  </div>
                </template>
                <ReviewerPopoverCard
                  :name="review.authorName || 'Reviewer'"
                  :username="review.authorUsername"
                  :avatar-url="review.authorAvatar"
                  :bio="review.authorBio"
                  :followers="review.authorFollowers"
                  :review-count="review.authorReviewCount"
                  :rating="review.authorRating"
                />
              </HoverPopover>
              <div v-else class="author-trigger">
                <div class="avatar-link">
                  <img class="avatar" :src="review.authorAvatar || 'https://as1.ftcdn.net/v2/jpg/16/50/75/40/1000_F_1650754099_NnbV1a2Cgvj26kogaurRePYoipRlFEao.jpg'" alt="avatar" />
                </div>
                <div>
                  <div class="name-row">
                    <strong>{{ review.authorName || 'Reviewer' }}</strong>
                    <span class="muted">· {{ formatDate(review.publishedAt) || 'Vừa đăng' }}</span>
                  </div>
                  <div class="eyebrow">{{ review.brand || 'Xe' }}</div>
                </div>
              </div>
            </div>
            <div class="head-actions">
              <button class="ghost">Đăng ký</button>
              <div class="dots">⋯</div>
            </div>
          </header>

          <div class="title">{{ review.title }}</div>
          <i><p class="excerpt">{{ review.excerpt }}</p></i>
          <div class="body" v-html="review.content" />

          <div class="hero" v-if="review.heroImageUrl">
            <img :src="heroSrc" :alt="review.title" />
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

        <section class="card comments" ref="commentsSection">
          <div class="comments-head">
            <div class="tabs">
              <button class="tab" :class="{ active: commentTab === 'top' }" type="button" @click="commentTab = 'top'">Quan tâm nhất</button>
              <button class="tab" :class="{ active: commentTab === 'newest' }" type="button" @click="commentTab = 'newest'">Mới nhất</button>
            </div>
            <h3>Bình luận ({{ totalComments }})</h3>
          </div>
          <div v-if="commentsLoading" class="status">Đang tải bình luận...</div>
          <div v-else-if="commentsError" class="status error">{{ commentsError }}</div>
          <div v-else-if="commentsVisible">
            <div class="inline-form">
              <form class="comment-form" @submit.prevent>
                <textarea v-model="newComment" rows="3" placeholder="Viết bình luận của bạn..." />
                <div v-if="formError" class="form-error">{{ formError }}</div>
                <div class="comment-actions">
                  <button class="ghost" type="button" @click="loadComments(true)" :disabled="commentsLoading">Tải lại bình luận</button>
                  <div class="action-group">
                    <button class="primary" type="button" @click="submitComment" :disabled="submitting">
                      {{ submitting ? 'Đang gửi...' : 'Đăng bình luận' }}
                    </button>
                  </div>
                </div>
              </form>
            </div>
            <div v-if="!totalComments" class="status">Chưa có bình luận</div>
            <div v-else class="comment-list" ref="commentList">
              <div
                v-for="comment in visibleRoots"
                :key="comment.id"
                class="comment-item"
                :class="{ flash: highlightedIds.has(comment.id), 'slide-in': slideIds.has(comment.id) }"
              >
                <div v-if="!comment.anonymous && (comment.authorUsername || comment.authorName)" class="comment-row">
                  <div class="comment-line">
                    <HoverPopover>
                      <template #trigger>
                        <div class="comment-avatar">
                          <img :src="comment.authorAvatar || defaultAvatar" alt="avatar" />
                        </div>
                      </template>
                      <ReviewerPopoverCard
                        :name="comment.authorName || comment.authorUsername || 'Reviewer'"
                        :username="comment.authorUsername"
                        :avatar-url="comment.authorAvatar"
                        :bio="comment.authorBio"
                        :followers="comment.authorFollowers"
                        :review-count="comment.authorReviewCount"
                        :rating="comment.authorRating"
                      />
                    </HoverPopover>
                    <p class="comment-line-text">
                      <strong class="comment-name-inline">
                        <RouterLink v-if="comment.authorUsername" class="comment-name" :to="`/user/${encodeURIComponent(comment.authorUsername)}`">
                          {{ comment.authorName || comment.authorUsername }}
                        </RouterLink>
                        <span v-else>{{ comment.authorName }}</span>
                      </strong>
                      <span class="comment-text">{{ comment.content }}</span>
                    </p>
                  </div>
                  <div class="comment-actions-row">
                    <div class="actions-left">
                      <button class="chip-btn" type="button" :class="{ liked: likesState[comment.id]?.liked }" @click="toggleLike(comment.id)">
                        ❤ {{ likesState[comment.id]?.count ?? 0 }}
                      </button>
                      <button v-if="canReply()" class="chip-btn" type="button" @click="startReply(comment)">Trả lời</button>
                    </div>
                    <span class="date-time-comment muted">{{ formatDate(comment.createdAt) }}</span>
                  </div>
                  <div v-if="replyTarget?.id === comment.id" class="inline-form nested">
                      <form class="comment-form" @submit.prevent>
                        <textarea
                          :ref="(el) => setReplyInputRef(comment.id, el as HTMLTextAreaElement | null)"
                          v-model="newComment"
                          rows="3"
                          placeholder="Phản hồi bình luận này..."
                        />
                        <div class="replying">Đang trả lời {{ comment.authorName || comment.authorUsername || 'bình luận' }}</div>
                        <div v-if="formError" class="form-error">{{ formError }}</div>
                        <div class="comment-actions">
                          <button class="ghost" type="button" @click="cancelReply" :disabled="submitting">Huỷ</button>
                          <div class="action-group">
                            <button class="primary" type="button" @click="submitComment" :disabled="submitting">
                              {{ submitting ? 'Đang gửi...' : 'Trả lời' }}
                            </button>
                          </div>
                        </div>
                      </form>
                    </div>
                  </div>
                <div v-else class="comment-row">
                  <div class="comment-line">
                    <div class="comment-avatar">
                      <img :src="comment.anonymous ? anonAvatar : comment.authorAvatar || defaultAvatar" alt="avatar" />
                    </div>
                    <p class="comment-line-text">
                      <strong class="comment-name-inline">{{ comment.anonymous ? 'Ẩn danh' : comment.authorName || 'Ẩn danh' }}</strong>
                      <span class="comment-text">{{ comment.content }}</span>
                    </p>
                  </div>
                  <div class="comment-actions-row">
                    <div class="actions-left">
                      <button class="chip-btn" type="button" :class="{ liked: likesState[comment.id]?.liked }" @click="toggleLike(comment.id)">
                        ❤ {{ likesState[comment.id]?.count ?? 0 }}
                      </button>
                      <button v-if="canReply()" class="chip-btn" type="button" @click="startReply(comment)">Trả lời</button>
                    </div>
                    <span class="date-time-comment muted">{{ formatDate(comment.createdAt) }}</span>
                  </div>
                    <div v-if="replyTarget?.id === comment.id" class="inline-form nested">
                      <form class="comment-form" @submit.prevent>
                        <textarea
                          :ref="(el) => setReplyInputRef(comment.id, el as HTMLTextAreaElement | null)"
                          v-model="newComment"
                          rows="3"
                          placeholder="Phản hồi bình luận này..."
                        />
                        <div class="replying">Đang trả lời bình luận</div>
                        <div v-if="formError" class="form-error">{{ formError }}</div>
                        <div class="comment-actions">
                          <button class="ghost" type="button" @click="cancelReply" :disabled="submitting">Huỷ</button>
                          <div class="action-group">
                            <button class="primary" type="button" @click="submitComment" :disabled="submitting">
                              {{ submitting ? 'Đang gửi...' : 'Trả lời' }}
                            </button>
                          </div>
                        </div>
                      </form>
                    </div>
                  </div>
                <div v-if="comment.children?.length" class="child-list">
                  <div
                    v-for="child in childRows(comment)"
                    :key="child.id"
                    class="comment-child"
                    :class="{ flash: highlightedIds.has(child.id), 'slide-in': slideIds.has(child.id) }"
                  >
                    <div v-if="!child.anonymous && (child.authorUsername || child.authorName)" class="comment-row child-row">
                      <div class="comment-line">
                        <HoverPopover>
                          <template #trigger>
                            <div class="comment-avatar">
                              <img :src="child.authorAvatar || defaultAvatar" alt="avatar" />
                            </div>
                          </template>
                          <ReviewerPopoverCard
                            :name="child.authorName || child.authorUsername || 'Reviewer'"
                            :username="child.authorUsername"
                            :avatar-url="child.authorAvatar"
                            :bio="child.authorBio"
                            :followers="child.authorFollowers"
                            :review-count="child.authorReviewCount"
                            :rating="child.authorRating"
                          />
                        </HoverPopover>
                        <p class="comment-line-text">
                          <strong class="comment-name-inline">
                            <RouterLink v-if="child.authorUsername" class="comment-name" :to="`/user/${encodeURIComponent(child.authorUsername)}`">
                              {{ child.authorName || child.authorUsername }}
                            </RouterLink>
                            <span v-else>{{ child.authorName }}</span>
                          </strong>
                          <span v-if="shouldShowMention(child)" class="mention-inline">
                            <RouterLink
                              v-if="getParent(child)?.authorUsername"
                              class="mention-link"
                              :to="`/user/${encodeURIComponent(getParent(child)?.authorUsername || '')}`"
                              >@{{ getParent(child)?.authorName || getParent(child)?.authorUsername }}</RouterLink
                            >
                            <span v-else>@{{ getParent(child)?.authorName }}</span>
                          </span>
                          <span class="comment-text">{{ displayContent(child.content, mentionLabel(child)) }}</span>
                        </p>
                      </div>
                      <div class="comment-actions-row">
                        <div class="actions-left">
                          <button class="chip-btn" type="button" :class="{ liked: likesState[child.id]?.liked }" @click="toggleLike(child.id)">
                            ❤ {{ likesState[child.id]?.count ?? 0 }}
                          </button>
                          <button v-if="canReply()" class="chip-btn" type="button" @click="startReply(child)">Trả lời</button>
                        </div>
                        <span class="date-time-comment muted">{{ formatDate(child.createdAt) }}</span>
                      </div>
                      <div v-if="replyTarget?.id === child.id" class="inline-form nested">
                        <form class="comment-form" @submit.prevent>
                          <textarea
                            :ref="(el) => setReplyInputRef(child.id, el as HTMLTextAreaElement | null)"
                            v-model="newComment"
                            rows="3"
                            placeholder="Phản hồi bình luận này..."
                          />
                          <div class="replying">Đang trả lời {{ child.authorName || child.authorUsername || 'bình luận' }}</div>
                          <div v-if="formError" class="form-error">{{ formError }}</div>
                          <div class="comment-actions">
                            <button class="ghost" type="button" @click="cancelReply" :disabled="submitting">Huỷ</button>
                            <div class="action-group">
                              <button class="primary" type="button" @click="submitComment" :disabled="submitting">
                                {{ submitting ? 'Đang gửi...' : 'Trả lời' }}
                              </button>
                            </div>
                          </div>
                        </form>
                      </div>
                    </div>
                    <div v-else class="comment-row child-row">
                      <div class="comment-line">
                        <div class="comment-avatar">
                          <img :src="child.anonymous ? anonAvatar : child.authorAvatar || defaultAvatar" alt="avatar" />
                        </div>
                        <p class="comment-line-text">
                          <strong class="comment-name-inline">{{ child.anonymous ? 'Ẩn danh' : child.authorName || 'Ẩn danh' }}</strong>
                          <span v-if="shouldShowMention(child)" class="mention-inline">
                            <RouterLink
                              v-if="getParent(child)?.authorUsername"
                              class="mention-link"
                              :to="`/user/${encodeURIComponent(getParent(child)?.authorUsername || '')}`"
                              >@{{ getParent(child)?.authorName || getParent(child)?.authorUsername }}</RouterLink
                            >
                            <span v-else>@{{ getParent(child)?.authorName }}</span>
                          </span>
                          <span class="comment-text">{{ displayContent(child.content, mentionLabel(child)) }}</span>
                        </p>
                      </div>
                      <div class="comment-actions-row">
                        <div class="actions-left">
                          <button class="chip-btn" type="button" :class="{ liked: likesState[child.id]?.liked }" @click="toggleLike(child.id)">
                            ❤ {{ likesState[child.id]?.count ?? 0 }}
                          </button>
                          <button v-if="canReply()" class="chip-btn" type="button" @click="startReply(child)">Trả lời</button>
                        </div>
                        <span class="date-time-comment muted">{{ formatDate(child.createdAt) }}</span>
                      </div>
                      <div v-if="replyTarget?.id === child.id" class="inline-form nested">
                        <form class="comment-form" @submit.prevent>
                          <textarea
                            :ref="(el) => setReplyInputRef(child.id, el as HTMLTextAreaElement | null)"
                            v-model="newComment"
                            rows="3"
                            placeholder="Phản hồi bình luận này..."
                          />
                          <div class="replying">Đang trả lời bình luận</div>
                          <div v-if="formError" class="form-error">{{ formError }}</div>
                          <div class="comment-actions">
                            <button class="ghost" type="button" @click="cancelReply" :disabled="submitting">Huỷ</button>
                            <div class="action-group">
                              <button class="primary" type="button" @click="submitComment" :disabled="submitting">
                                {{ submitting ? 'Đang gửi...' : 'Trả lời' }}
                              </button>
                            </div>
                          </div>
                        </form>
                      </div>
                    </div>

                  </div>
                </div>
              </div>
              <button v-if="hasMore && !commentsLoading" class="ghost load-more" type="button" @click="loadComments(false, true)">Xem thêm bình luận</button>
            </div>
          </div>
        </section>
      </div>
      <aside class="side">
        <MostViewed :items="mostViewed" />
        <FeaturedBrands :brands="brands" @select="onBrandSelect" />
        <TopReviewers :reviewers="reviewers" title="Top Reviewers" />
      </aside>
    </div>

    <div v-else class="status" :class="{ error: Boolean(errorMsg) }">
      {{ loading ? 'Đang tải...' : errorMsg || 'Không tìm thấy bài viết' }}
    </div>

    <div v-if="modalVisible" class="modal">
      <div class="modal-card">
        <h4>Cần đăng nhập</h4>
        <p class="muted">Vui lòng đăng nhập hoặc đăng ký để bình luận</p>
        <div class="modal-actions">
          <button class="ghost" type="button" @click="modalVisible = false">Đóng</button>
          <button class="ghost" type="button" @click="goRegister">Đăng ký</button>
          <button class="primary" type="button" @click="goLogin">Đăng nhập</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.page {
  background: var(--bg);
  min-height: 100vh;
  padding: 20px 0 40px;
  display: grid;
}

.layout {
  display: grid;
  grid-template-columns: 2.5fr 1fr;
  gap: 20px;
}

@media (max-width: 1024px) {
  .layout {
    grid-template-columns: 1fr;
  }
}

.card {
  background: var(--surface);
  border-radius: 20px;
  box-shadow: var(--shadow);
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

.author-trigger {
  display: flex;
  gap: 12px;
  align-items: center;
}

.avatar-link {
  display: block;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
}

.name-link {
  text-decoration: none;
  color: inherit;
  font-weight: 700;
}

.name-link:hover {
  text-decoration: underline;
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
  background: var(--chip-bg);
  cursor: pointer;
}

.title {
  font-size: 22px;
  font-weight: 800;
  margin-top: 6px;
}

.excerpt {
  margin: 6px 0;
  font-size: 14px;
  color: var(--text);
}

.body {
  color: var(--text);
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
  font-size: smaller;
}

.chip {
  padding: 6px 10px;
  border-radius: 12px;
  background: var(--chip-bg);
  color: var(--primary);
  font-weight: 700;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.pill {
  border: 1px solid var(--pill-border);
  background: var(--pill-bg);
  border-radius: 999px;
  padding: 8px 12px;
  font-weight: 700;
  color: var(--text);
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
  border: 1px solid var(--border);
  padding: 12px;
  font-size: 15px;
  resize: vertical;
  background: var(--pill-bg);
  color: var(--text);
}

.form-error {
  color: var(--error);
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

.replying {
  font-size: 13px;
  color: var(--muted);
  margin-top: -4px;
}

.primary,
.ghost,
.pill {
  cursor: pointer;
}

.primary {
  background: linear-gradient(135deg, var(--accent), var(--primary));
  color: #fff;
  border: none;
  padding: 10px 16px;
  border-radius: 999px;
  font-weight: 700;
}

.ghost {
  background: var(--chip-bg);
  color: var(--text);
  border: 1px solid var(--border);
  padding: 8px 14px;
  border-radius: 999px;
  font-weight: 700;
}

.comments {
  display: grid;
  gap: 12px;
}

.comments-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.tabs {
  display: flex;
  gap: 8px;
}

.tab {
  padding: 8px 12px;
  border-radius: 12px;
  border: 1px solid var(--border);
  background: var(--chip-bg);
  font-weight: 700;
}

.tab.active {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}

.inline-form {
  margin-bottom: 14px;
  padding: 10px 12px;
  border-radius: 12px;
  background: var(--chip-bg);
  border: 1px solid var(--border);
}

.inline-form.nested {
  margin-top: 10px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0;
}

.comment-item {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
  padding: 12px 12px 10px;
  border: 1px solid #d9d9d9;
  background: #fafafa;
  border-radius: 6px;
}

.child-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 10px;
  padding-left: 18px;
  border-left: 1px solid #e0e0e0;
}

.comment-child {
  padding: 0;
  background: transparent;
  border: none;
}

.comment-trigger {
  display: grid;
  grid-template-columns: 40px 1fr;
  gap: 10px;
  align-items: flex-start;
}

.chip-btn {
  padding: 2px 6px;
  border-radius: 6px;
  border: none;
  background: transparent;
  color: #6b7280;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}

.comment-actions-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-size: 13px;
  color: #6b7280;
  margin-top: 2px;
  padding-left: 50px;
}

.actions-left {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.comment-name-inline {
  margin-right: 6px;
  color: #0f172a;
  font-weight: 700;
}

.comment-text {
  white-space: pre-line;
  color: #3f3f46;
}

.chip-btn.liked {
  color: #d81b60;
}

.comment-name {
  font-weight: 700;
  color: inherit;
  text-decoration: none;
}

.comment-name:hover {
  text-decoration: underline;
}

.comment-item.flash {
  animation: flash 3s ease;
}

.comment-item.slide-in {
  animation: slideUp 0.5s ease;
}

.comment-item:last-of-type {
  border-bottom: none;
}

.comment-actions-row .chip-btn svg,
.comment-actions-row .chip-btn i {
  color: #6b7280;
}

@keyframes flash {
  0% {
    background: var(--highlight);
  }
  40% {
    background: var(--highlight);
  }
  100% {
    background: var(--chip-bg);
  }
}

@keyframes slideUp {
  0% {
    transform: translateY(12px);
    opacity: 0;
  }
  100% {
    transform: translateY(0);
    opacity: 1;
  }
}

.comment-line {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  width: 100%;
}

.comment-line > :first-child {
  display: inline-flex;
  align-items: flex-start;
  width: auto;
  flex-shrink: 0;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-line-text {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  margin: 0;
  color: #111827;
  flex: 1;
  flex-wrap: wrap;
  font-size: 15px;
  line-height: 1.5;
}

.comment-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #e5e7eb;
}

.comment-content {
  display: grid;
  gap: 6px;
}

.mention-link {
  color: #16a34a;
  font-weight: 700;
  text-decoration: none;
}

.mention-link:hover {
  text-decoration: underline;
}

.mention-inline {
  color: #16a34a;
  font-weight: 700;
}

.mention-inline::after {
  content: ':';
  margin-left: 2px;
  margin-right: 2px;
}

.comment-content p {
  display: inline-flex;
  gap: 6px;
  align-items: baseline;
}

.comment-meta {
  display: flex;
  gap: 8px;
  align-items: baseline;
}

.comment-content p {
  margin: 0;
  color: var(--text);
}

.side {
  display: flow;
  gap: 12px;
}

.status {
  text-align: center;
  color: var(--muted);
  padding: 12px 0;
}

.error {
  color: var(--error);
}

.modal {
  position: fixed;
  inset: 0;
  background: var(--modal-backdrop);
  display: grid;
  place-items: center;
  z-index: 20;
}

.modal-card {
  background: var(--surface);
  padding: 18px;
  border-radius: 16px;
  box-shadow: var(--shadow);
  width: min(420px, 90vw);
  display: grid;
  gap: 10px;
  border: 1px solid var(--border);
}

.modal-card input {
  width: 100%;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: var(--pill-bg);
  color: var(--text);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.modal-error {
  color: var(--error);
  font-weight: 700;
}

.date-time-comment {
  font-size: smaller;
  color: #9ca3af;
}

</style>