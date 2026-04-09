<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Megaphone, Plus, Trash2, Edit2, Clock, User as UserIcon, Wand2 } from 'lucide-vue-next'
import { useNoticeStore, type Notice } from '../stores/noticeStore'
import { useAuthStore } from '../stores/authStore'
import api from '../api'

const noticeStore = useNoticeStore()
const authStore = useAuthStore()

const isModalOpen = ref(false)
const isEditing = ref(false)
const currentNoticeId = ref<number | null>(null)
const formData = ref({ title: '', content: '' })
const isGenerating = ref(false)

onMounted(() => {
  noticeStore.fetchNotices()
})

const openCreateModal = () => {
  isEditing.value = false
  currentNoticeId.value = null
  formData.value = { title: '', content: '' }
  isModalOpen.value = true
}

const openEditModal = (notice: Notice) => {
  isEditing.value = true
  currentNoticeId.value = notice.id
  formData.value = { title: notice.title, content: notice.content }
  isModalOpen.value = true
}

const saveNotice = async () => {
  if (isEditing.value && currentNoticeId.value !== null) {
    await noticeStore.updateNotice(currentNoticeId.value, formData.value)
  } else {
    await noticeStore.createNotice(formData.value)
  }
  isModalOpen.value = false
}

const deleteNotice = async (id: number) => {
  if (confirm('Are you sure you want to delete this announcement?')) {
    await noticeStore.deleteNotice(id)
  }
}

const draftWithAI = async () => {
  const topic = prompt("What is the notice about? (e.g. Please return overdue laptops by Friday)")
  if (!topic) return
  
  isGenerating.value = true
  try {
    const response = await api.post('/ai/generate-notice', { topic })
    const aiText = response.data.result
    
    const titleMatch = aiText.match(/\[제목\]\s*(.*)/)
    const contentMatch = aiText.match(/\[본문\]\s*([\s\S]*)/)
    
    if (titleMatch && contentMatch) {
      formData.value.title = titleMatch[1].trim()
      formData.value.content = contentMatch[1].trim()
    } else {
      formData.value.content = aiText
    }
  } catch (error: any) {
    alert("Failed to generate notice with AI.")
    console.error(error)
  } finally {
    isGenerating.value = false
  }
}
</script>

<template>
  <div class="max-w-6xl mx-auto space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-2xl font-bold text-gray-800 flex items-center">
        <Megaphone class="w-6 h-6 mr-3 text-blue-600" />
        System Announcements
      </h2>
      <button 
        v-if="authStore.isAdmin"
        @click="openCreateModal"
        class="bg-slate-900 text-white px-4 py-2.5 rounded-xl text-sm font-bold hover:bg-slate-800 transition-all flex items-center shadow-lg shadow-slate-900/20"
      >
        <Plus class="w-4 h-4 mr-2" />
        New Announcement
      </button>
    </div>

    <!-- Error State -->
    <div v-if="noticeStore.error" class="bg-red-50 text-red-600 p-4 rounded-xl font-medium border border-red-100">
      {{ noticeStore.error }}
    </div>

    <!-- Notice List -->
    <div class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
      <div v-if="noticeStore.loading" class="p-12 text-center">
        <div class="animate-pulse w-10 h-10 bg-gray-100 rounded-full mx-auto mb-4"></div>
        <p class="text-gray-400 font-medium">Loading announcements...</p>
      </div>

      <div v-else-if="noticeStore.notices.length === 0" class="p-12 text-center">
        <Megaphone class="w-12 h-12 text-gray-300 mx-auto mb-4" />
        <p class="text-gray-500 font-medium">No announcements published yet.</p>
      </div>

      <div v-else class="divide-y divide-gray-50">
        <div 
          v-for="notice in noticeStore.notices" 
          :key="notice.id"
          class="p-6 hover:bg-gray-50 transition-colors group"
        >
          <div class="flex items-start justify-between">
            <div class="space-y-1 w-full max-w-3xl">
              <h3 class="text-xl font-bold text-gray-800">{{ notice.title }}</h3>
              <div class="flex items-center space-x-4 text-xs font-medium text-gray-400 mb-4 pb-2 border-b border-gray-100">
                <span class="flex items-center"><UserIcon class="w-3.5 h-3.5 mr-1" /> {{ notice.authorEmail }}</span>
                <span class="flex items-center"><Clock class="w-3.5 h-3.5 mr-1" /> {{ new Date(notice.createdAt).toLocaleString() }}</span>
              </div>
              <p class="text-gray-600 leading-relaxed whitespace-pre-wrap">{{ notice.content }}</p>
            </div>
            
            <div v-if="authStore.isAdmin" class="flex space-x-2 opacity-0 group-hover:opacity-100 transition-opacity">
              <button 
                @click="openEditModal(notice)"
                class="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                title="Edit"
              >
                <Edit2 class="w-4 h-4" />
              </button>
              <button 
                @click="deleteNotice(notice.id)"
                class="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                title="Delete"
              >
                <Trash2 class="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="isModalOpen" class="fixed inset-0 bg-gray-900/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl overflow-hidden shadow-[0_20px_50px_rgba(0,0,0,0.2)]">
        <div class="p-6 border-b border-gray-100 flex justify-between items-center bg-slate-900 text-white">
          <h3 class="text-xl font-bold">{{ isEditing ? 'Edit Announcement' : 'New Announcement' }}</h3>
          <button @click="isModalOpen = false" class="text-gray-400 hover:text-white transition-colors">
            <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
          </button>
        </div>
        <form @submit.prevent="saveNotice" class="p-6 space-y-6">
          <div class="flex justify-between items-center bg-gradient-to-r from-blue-50/50 to-indigo-50/50 p-4 rounded-xl border border-blue-100/50">
            <div class="text-sm">
              <p class="font-bold text-blue-900 flex items-center">✨ AI Auto-Draft</p>
              <p class="text-blue-700/80 text-xs mt-0.5">Let Ollama generate a professional template for you.</p>
            </div>
            <button 
              type="button" 
              @click="draftWithAI"
              :disabled="isGenerating"
              class="px-4 py-2 bg-white text-blue-600 border border-blue-200 rounded-lg text-sm font-bold shadow-sm hover:bg-blue-50 hover:border-blue-300 transition-all flex items-center disabled:opacity-50"
            >
              <Wand2 class="w-4 h-4 mr-2" :class="{'animate-pulse': isGenerating}" />
              {{ isGenerating ? 'Generating...' : 'Draft with AI' }}
            </button>
          </div>

          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-2">Title</label>
            <input 
              v-model="formData.title" 
              required
              class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all"
              placeholder="Announcement clearly and concisely"
            />
          </div>
          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-2">Content</label>
            <textarea 
              v-model="formData.content" 
              required
              rows="6"
              class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all resize-none"
              placeholder="Details of the announcement..."
            ></textarea>
          </div>
          <div class="flex justify-end space-x-3 pt-4 border-t border-gray-100">
            <button 
              type="button" 
              @click="isModalOpen = false"
              class="px-5 py-2.5 text-sm font-bold text-gray-600 hover:bg-gray-100 rounded-xl transition-colors"
            >
              Cancel
            </button>
            <button 
              type="submit"
              :disabled="noticeStore.loading"
              class="px-5 py-2.5 text-sm font-bold text-white bg-blue-600 hover:bg-blue-700 rounded-xl transition-colors shadow-lg shadow-blue-600/30 disabled:opacity-50"
            >
              {{ noticeStore.loading ? 'Saving...' : 'Publish' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
