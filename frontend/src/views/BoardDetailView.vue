<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, User, Calendar, Eye, MessageSquare, Trash2, Edit2, Send } from 'lucide-vue-next'
import { useBoardStore } from '../stores/boardStore'
import { useAuthStore } from '../stores/authStore'
import BoardFormModal from '../components/board/BoardFormModal.vue'

const route = useRoute()
const router = useRouter()
const boardStore = useBoardStore()
const authStore = useAuthStore()

const commentText = ref('')
const showEditModal = ref(false)
const postId = Number(route.params.id)

onMounted(async () => {
  await boardStore.fetchPost(postId)
})

const isAuthor = computed(() => {
  return boardStore.currentPost?.authorId === authStore.id || authStore.isAdmin
})

const handleDeletePost = async () => {
  if (confirm('게시글을 삭제하시겠습니까?')) {
    await boardStore.deletePost(postId)
    router.push('/board')
  }
}

const handleAddComment = async () => {
  if (!commentText.value.trim()) return
  try {
    await boardStore.addComment(postId, commentText.value)
    commentText.value = ''
  } catch (error) {
    console.error('Failed to add comment', error)
  }
}

const handleDeleteComment = async (id: number) => {
  if (confirm('댓글을 삭제하시겠습니까?')) {
    await boardStore.deleteComment(id, postId)
  }
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<template>
  <div class="max-w-4xl mx-auto space-y-6 pb-20">
    <!-- Header -->
    <button @click="router.back()" class="flex items-center text-gray-500 hover:text-gray-800 transition-colors group">
      <ArrowLeft class="w-4 h-4 mr-2 group-hover:-translate-x-1 transition-transform" />
      <span class="font-medium">목록으로 돌아가기</span>
    </button>

    <div v-if="boardStore.currentPost" class="space-y-6">
      <!-- Post Content -->
      <div class="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden">
        <div class="p-8 space-y-6">
          <div class="space-y-4">
            <h1 class="text-3xl font-extrabold text-gray-900 leading-tight">
              {{ boardStore.currentPost.title }}
            </h1>
            
            <div class="flex flex-wrap items-center gap-6 py-4 border-y border-gray-50 text-sm text-gray-400 font-medium">
              <div class="flex items-center gap-2">
                <div class="w-8 h-8 rounded-full bg-blue-50 text-blue-600 flex items-center justify-center font-bold">
                  {{ boardStore.currentPost.authorName?.charAt(0) }}
                </div>
                <span class="text-gray-700 font-bold">{{ boardStore.currentPost.authorName }}</span>
              </div>
              <div class="flex items-center gap-2">
                <Calendar class="w-4 h-4" />
                <span>{{ formatDate(boardStore.currentPost.createdAt) }}</span>
              </div>
              <div class="flex items-center gap-2">
                <Eye class="w-4 h-4" />
                <span>조회수 {{ boardStore.currentPost.viewCount }}</span>
              </div>

              <div v-if="isAuthor" class="ml-auto flex items-center gap-2">
                <button @click="showEditModal = true" class="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-xl transition-all">
                  <Edit2 class="w-4 h-4" />
                </button>
                <button @click="handleDeletePost" class="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-xl transition-all">
                  <Trash2 class="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>

          <div class="text-gray-700 leading-relaxed min-h-[300px] quill-content" v-html="boardStore.currentPost.content">
          </div>
        </div>
      </div>

      <!-- Comments Section -->
      <div class="space-y-4 pt-4">
        <h3 class="text-lg font-bold text-gray-800 flex items-center gap-2">
          <MessageSquare class="w-5 h-5 text-blue-500" />
          댓글 <span class="text-blue-600">{{ boardStore.comments.length }}</span>
        </h3>

        <!-- Comment Input -->
        <div class="bg-white p-4 rounded-2xl shadow-sm border border-gray-100 flex gap-4 items-start">
          <div class="w-10 h-10 rounded-full bg-gray-100 shrink-0 flex items-center justify-center font-bold text-gray-400">
            {{ authStore.name?.charAt(0) }}
          </div>
          <div class="flex-1 relative">
            <textarea
              v-model="commentText"
              rows="2"
              placeholder="댓글을 남겨주세요..."
              class="w-full px-4 py-3 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:bg-white outline-none transition-all resize-none text-sm"
            ></textarea>
            <button
              @click="handleAddComment"
              :disabled="!commentText.trim()"
              class="absolute bottom-3 right-3 p-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-all shadow-md active:scale-95"
            >
              <Send class="w-4 h-4" />
            </button>
          </div>
        </div>

        <!-- Comment List -->
        <div class="space-y-3">
          <div
            v-for="comment in boardStore.comments"
            :key="comment.id"
            class="bg-white p-5 rounded-2xl shadow-sm border border-gray-50 flex gap-4 group"
          >
            <div class="w-9 h-9 rounded-full bg-gray-50 flex items-center justify-center text-xs font-bold text-gray-500 shrink-0">
              {{ comment.authorName?.charAt(0) }}
            </div>
            <div class="flex-1 space-y-1">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-2">
                  <span class="text-sm font-bold text-gray-800">{{ comment.authorName }}</span>
                  <span class="text-[10px] text-gray-400 font-medium">{{ formatDate(comment.createdAt) }}</span>
                </div>
                <button
                  v-if="comment.authorId === authStore.id || authStore.isAdmin"
                  @click="handleDeleteComment(comment.id!)"
                  class="p-1.5 text-gray-300 hover:text-red-500 hover:bg-red-50 rounded-lg transition-all opacity-0 group-hover:opacity-100"
                >
                  <Trash2 class="w-3.5 h-3.5" />
                </button>
              </div>
              <p class="text-sm text-gray-600 leading-relaxed">{{ comment.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-20 text-gray-400">
      게시글을 찾을 수 없습니다.
    </div>

    <BoardFormModal v-if="showEditModal" :post="boardStore.currentPost" @close="showEditModal = false" />
  </div>
</template>

<style>
.quill-content {
  font-size: 1rem;
}
.quill-content p {
  margin-bottom: 0.5rem;
}
.quill-content h1 { font-size: 2rem; font-weight: bold; margin: 1rem 0; }
.quill-content h2 { font-size: 1.5rem; font-weight: bold; margin: 0.75rem 0; }
.quill-content ul { list-style-type: disc; padding-left: 1.5rem; margin: 0.5rem 0; }
.quill-content ol { list-style-type: decimal; padding-left: 1.5rem; margin: 0.5rem 0; }
.quill-content blockquote { border-left: 4px solid #e2e8f0; padding-left: 1rem; color: #64748b; font-style: italic; }
.quill-content pre { background: #f8fafc; padding: 1rem; border-radius: 0.5rem; overflow-x: auto; font-family: monospace; }
</style>
