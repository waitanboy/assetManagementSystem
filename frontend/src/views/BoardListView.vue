<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { Plus, Search, MessageSquare, User, Eye, Calendar, ChevronRight } from 'lucide-vue-next'
import { useBoardStore } from '../stores/boardStore'
import { useAuthStore } from '../stores/authStore'
import BoardFormModal from '../components/board/BoardFormModal.vue'

const boardStore = useBoardStore()
const authStore = useAuthStore()
const showModal = ref(false)
const searchQuery = ref('')

onMounted(() => {
  boardStore.fetchPosts()
})

let timeout: any = null
watch(searchQuery, () => {
  if (timeout) clearTimeout(timeout)
  timeout = setTimeout(() => {
    boardStore.fetchPosts(searchQuery.value)
  }, 300)
})

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const stripHtml = (html: string) => {
  const tmp = document.createElement('DIV')
  tmp.innerHTML = html
  return tmp.textContent || tmp.innerText || ''
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header & Search -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div class="relative flex-1 max-w-lg">
        <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
          <Search class="w-5 h-5" />
        </span>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="게시글 제목, 내용 검색..."
          class="block w-full pl-10 pr-4 py-2.5 bg-white border border-gray-100 rounded-2xl shadow-sm focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all"
        />
      </div>

      <button
        @click="showModal = true"
        class="flex items-center justify-center space-x-2 px-6 py-2.5 bg-slate-900 text-white rounded-2xl hover:bg-slate-800 transition-all shadow-lg shadow-slate-200 active:scale-95"
      >
        <Plus class="w-5 h-5" />
        <span class="font-bold">글쓰기</span>
      </button>
    </div>

    <!-- Post List -->
    <div class="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden">
      <div v-if="boardStore.loading && boardStore.posts.length === 0" class="p-20 text-center text-gray-400">
        <div class="animate-spin w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full mx-auto mb-4"></div>
        <p>게시글을 불러오는 중입니다...</p>
      </div>

      <div v-else-if="boardStore.posts.length === 0" class="p-20 text-center text-gray-400">
        <MessageSquare class="w-12 h-12 mx-auto mb-4 opacity-20" />
        <p>등록된 게시글이 없습니다.</p>
      </div>

      <div v-else class="divide-y divide-gray-50">
        <router-link
          v-for="post in boardStore.posts"
          :key="post.id"
          :to="`/board/${post.id}`"
          class="block p-6 hover:bg-gray-50/50 transition-colors group"
        >
          <div class="flex items-start justify-between gap-4">
            <div class="flex-1 space-y-2">
              <h3 class="text-lg font-bold text-gray-800 group-hover:text-blue-600 transition-colors leading-tight">
                {{ post.title }}
              </h3>
              <p class="text-gray-500 text-sm line-clamp-2 leading-relaxed">
                {{ stripHtml(post.content) }}
              </p>
              
              <div class="flex items-center gap-4 pt-2 text-xs text-gray-400 font-medium">
                <div class="flex items-center gap-1.5">
                  <div class="w-5 h-5 rounded-full bg-gray-100 flex items-center justify-center">
                    <User class="w-3 h-3" />
                  </div>
                  <span>{{ post.authorName }}</span>
                </div>
                <div class="flex items-center gap-1.5">
                  <Calendar class="w-3.5 h-3.5" />
                  <span>{{ formatDate(post.createdAt) }}</span>
                </div>
                <div class="flex items-center gap-1.5">
                  <Eye class="w-3.5 h-3.5" />
                  <span>{{ post.viewCount }}</span>
                </div>
              </div>
            </div>
            
            <div class="shrink-0 self-center">
              <div class="w-10 h-10 rounded-xl bg-gray-50 text-gray-300 flex items-center justify-center group-hover:bg-blue-50 group-hover:text-blue-500 transition-all">
                <ChevronRight class="w-5 h-5" />
              </div>
            </div>
          </div>
        </router-link>
      </div>
    </div>

    <BoardFormModal v-if="showModal" @close="showModal = false" />
  </div>
</template>
