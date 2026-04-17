<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import { MessageCircle, Search, Send, User, Clock } from 'lucide-vue-next'
import { useChatStore, type ChatMessage } from '../stores/chatStore'
import { useAuthStore } from '../stores/authStore'

const chatStore = useChatStore()
const authStore = useAuthStore()

const conversations = ref<ChatMessage[]>([])
const activeUserId = ref<number | null>(null)
const activeUserName = ref('')
const messageText = ref('')
const scrollContainer = ref<HTMLElement | null>(null)
const loading = ref(false)

const fetchConversations = async () => {
  loading.value = true
  try {
    await chatStore.fetchAdminUser()
    conversations.value = await chatStore.getAdminConversations()
  } catch (err) {
    console.error('Failed to fetch conversations', err)
  } finally {
    loading.value = false
  }
}

const selectUser = async (user: any) => {
  activeUserId.value = user.senderId
  activeUserName.value = user.senderName
  await chatStore.fetchHistory(user.senderId)
  await chatStore.markAsRead(user.senderId)
  scrollToBottom()
}

const handleSend = () => {
  if (!messageText.value.trim() || !activeUserId.value) return
  chatStore.sendMessage(activeUserId.value, messageText.value)
  messageText.value = ''
  scrollToBottom()
}

const scrollToBottom = async () => {
  await nextTick()
  if (scrollContainer.value) {
    scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight
  }
}

watch(() => chatStore.messages.length, () => {
  scrollToBottom()
})

onMounted(() => {
  fetchConversations()
  chatStore.connect()
})

const isSelf = (senderId: number) => senderId === authStore.id
</script>

<template>
  <div class="h-[calc(100vh-8rem)] flex bg-white rounded-3xl shadow-xl overflow-hidden border border-gray-100">
    <!-- Sidebar: Conversation List -->
    <div class="w-80 border-r border-gray-100 flex flex-col bg-gray-50/30">
      <div class="p-6">
        <h2 class="text-xl font-bold text-gray-800 mb-4">Customer Inquiries</h2>
        <div class="relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            placeholder="Search users..."
            class="w-full pl-10 pr-4 py-2 bg-white border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-blue-500/20 outline-none transition-all"
          />
        </div>
      </div>

      <div class="flex-1 overflow-y-auto px-3 pb-6">
        <div v-if="conversations.length === 0" class="text-center py-10 opacity-40">
          <MessageCircle class="w-10 h-10 mx-auto mb-2" />
          <p class="text-xs font-medium">No inquiries yet.</p>
        </div>
        
        <button
          v-for="conv in conversations"
          :key="conv.senderId"
          @click="selectUser(conv)"
          :class="[
            'w-full flex items-center p-4 rounded-2xl transition-all mb-2 text-left group',
            activeUserId === conv.senderId ? 'bg-blue-600 text-white shadow-lg shadow-blue-200' : 'bg-transparent hover:bg-white hover:shadow-md'
          ]"
        >
          <div :class="['w-10 h-10 rounded-full flex items-center justify-center mr-3 shrink-0', activeUserId === conv.senderId ? 'bg-white/20' : 'bg-blue-100']">
            <User :class="['w-5 h-5', activeUserId === conv.senderId ? 'text-white' : 'text-blue-600']" />
          </div>
          <div class="min-w-0 flex-1">
            <div class="flex justify-between items-start">
              <h4 :class="['font-bold truncate', activeUserId === conv.senderId ? 'text-white' : 'text-gray-800']">
                {{ conv.senderName }}
              </h4>
              <span :class="['text-[10px] ml-2 shrink-0', activeUserId === conv.senderId ? 'text-blue-100' : 'text-gray-400']">
                {{ new Date(conv.timestamp).toLocaleDateString([], { month: 'short', day: 'numeric' }) }}
              </span>
            </div>
            <p :class="['text-xs truncate mt-0.5', activeUserId === conv.senderId ? 'text-blue-100' : 'text-gray-500']">
              {{ conv.content }}
            </p>
          </div>
          <div v-if="!conv.isRead && activeUserId !== conv.senderId" class="w-2 h-2 rounded-full bg-blue-500 ml-2"></div>
        </button>
      </div>
    </div>

    <!-- Main Content: Active Chat -->
    <div class="flex-1 flex flex-col bg-white">
      <div v-if="activeUserId" class="flex-1 flex flex-col h-full">
        <!-- Chat Header -->
        <div class="px-8 py-5 border-b border-gray-100 flex items-center justify-between">
          <div class="flex items-center space-x-4">
            <div class="w-12 h-12 rounded-full bg-blue-50 flex items-center justify-center">
              <User class="w-6 h-6 text-blue-600" />
            </div>
            <div>
              <h3 class="text-lg font-bold text-gray-800">{{ activeUserName }}</h3>
              <p class="text-xs text-gray-500 flex items-center gap-1">
                <Clock class="w-3 h-3" />
                Active Inquiry
              </p>
            </div>
          </div>
        </div>

        <!-- Messages Area -->
        <div ref="scrollContainer" class="flex-1 overflow-y-auto p-8 space-y-6 bg-gray-50/20">
          <div
            v-for="msg in chatStore.messages"
            :key="msg.id"
            :class="['flex', isSelf(msg.senderId) ? 'justify-end' : 'justify-start']"
          >
            <div class="flex flex-col" :class="isSelf(msg.senderId) ? 'items-end' : 'items-start'">
              <div
                :class="[
                  'max-w-[450px] p-4 rounded-2xl text-sm shadow-sm transition-all leading-relaxed',
                  isSelf(msg.senderId) 
                    ? 'bg-blue-600 text-white rounded-tr-none' 
                    : 'bg-white text-gray-800 border border-gray-100 rounded-tl-none'
                ]"
              >
                {{ msg.content }}
              </div>
              <span class="text-[10px] text-gray-400 mt-1.5 px-1 font-medium">
                {{ new Date(msg.timestamp).toLocaleString([], { hour: '2-digit', minute: '2-digit' }) }}
              </span>
            </div>
          </div>
        </div>

        <!-- Input Area -->
        <div class="p-8 bg-white border-t border-gray-100">
          <div class="flex items-center space-x-4">
            <div class="flex-1 relative flex items-center bg-gray-50 rounded-2xl border border-gray-100 focus-within:ring-2 focus-within:ring-blue-500/20 focus-within:bg-white focus-within:border-blue-500 transition-all">
              <input
                v-model="messageText"
                @keyup.enter="handleSend"
                type="text"
                placeholder="Type your response..."
                class="flex-1 bg-transparent border-none outline-none px-6 py-4"
              />
              <button
                @click="handleSend"
                class="mx-4 p-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-all shadow-lg shadow-blue-200 active:scale-95 disabled:opacity-50"
                :disabled="!messageText.trim()"
              >
                <Send class="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="flex-1 flex flex-col items-center justify-center text-center p-20 opacity-30">
        <MessageCircle class="w-24 h-24 mb-6" />
        <h3 class="text-2xl font-bold text-gray-800">No Chat Selected</h3>
        <p class="max-w-xs mt-2 font-medium">Select a user from the sidebar to start responding to inquiries.</p>
      </div>
    </div>
  </div>
</template>
