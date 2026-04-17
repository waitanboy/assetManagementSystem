<script setup lang="ts">
import { ref, onMounted, nextTick, watch, computed } from 'vue'
import { MessageCircle, X, Send, User, ChevronLeft, Loader2 } from 'lucide-vue-next'
import { useChatStore, type ChatMessage } from '../../stores/chatStore'
import { useAuthStore } from '../../stores/authStore'

const chatStore = useChatStore()
const authStore = useAuthStore()

const isOpen = ref(false)
const currentView = ref<'list' | 'chat'>('list')
const activeUserId = ref<number | null>(null)
const activeUserName = ref<string>('')
const messageText = ref('')
const loadingUsers = ref(false)
const scrollContainer = ref<HTMLElement | null>(null)
const conversations = ref<ChatMessage[]>([])

const toggleChat = async () => {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    if (authStore.isAdmin) {
      currentView.value = 'list'
      await fetchAdminConversations()
    } else {
      currentView.value = 'list' // Always show list for users too to distinguish Admin/System
      // Re-fetch if null to avoid "no reaction" issues
      if (!chatStore.adminUser || !chatStore.systemUser) {
        loadingUsers.value = true
        Promise.all([chatStore.fetchAdminUser(), chatStore.fetchSystemUser()])
          .finally(() => loadingUsers.value = false)
      }
    }
    scrollToBottom()
  }
}

const fetchAdminConversations = async () => {
  conversations.value = await chatStore.getAdminConversations()
}

const selectUser = async (user: any) => {
  activeUserId.value = user.senderId
  activeUserName.value = user.senderName
  currentView.value = 'chat'
  await chatStore.fetchHistory(user.senderId)
  await chatStore.markAsRead(user.senderId)
  scrollToBottom()
}

const goBack = () => {
  currentView.value = 'list'
  activeUserId.value = null
  fetchAdminConversations()
}

const scrollToBottom = async () => {
  await nextTick()
  if (scrollContainer.value) {
    scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight
  }
}

const handleSend = () => {
  if (!messageText.value.trim()) return
  
  const recipientId = authStore.isAdmin ? activeUserId.value : chatStore.adminUser?.id
  if (!recipientId) return

  chatStore.sendMessage(recipientId, messageText.value)
  messageText.value = ''
  scrollToBottom()
}

const filteredMessages = computed(() => {
  if (!activeUserId.value) {
    return authStore.isAdmin ? [] : chatStore.messages // Admin list view needs empty, User list view needs all (or doesn't matter)
  }
  
  // Filter messages to only show conversation with the active user
  return chatStore.messages.filter(m => 
    (m.senderId === activeUserId.value && m.receiverId === authStore.id) ||
    (m.senderId === authStore.id && m.receiverId === activeUserId.value)
  )
})

watch(() => chatStore.messages.length, () => {
  if (isOpen.value && currentView.value === 'chat') {
    scrollToBottom()
  }
})

onMounted(async () => {
  if (authStore.isAuthenticated) {
    chatStore.connect()
    await Promise.all([
      chatStore.fetchAdminUser(),
      chatStore.fetchSystemUser()
    ])
    chatStore.fetchUnreadCount()
  }

  // Listen for open event from sidebar
  window.addEventListener('open-chat-widget', () => {
    if (!isOpen.value) toggleChat()
  })
})

const isSelf = (senderId: number) => senderId === authStore.id
</script>

<template>
  <div class="fixed bottom-6 right-6 z-50 flex flex-col items-end">
    <!-- Chat Window -->
    <transition
      enter-active-class="transition duration-300 ease-out transform"
      enter-from-class="opacity-0 translate-y-10 scale-95"
      enter-to-class="opacity-100 translate-y-0 scale-100"
      leave-active-class="transition duration-200 ease-in transform"
      leave-from-class="opacity-100 translate-y-0 scale-100"
      leave-to-class="opacity-0 translate-y-10 scale-95"
    >
      <div v-if="isOpen" class="mb-4 w-96 max-w-[calc(100vw-3rem)] h-[500px] max-h-[calc(100vh-8rem)] bg-white/90 backdrop-blur-xl border border-white/20 rounded-3xl shadow-2xl flex flex-col overflow-hidden">
        <!-- Header -->
        <div class="p-5 bg-gradient-to-r from-blue-600 to-indigo-700 text-white flex items-center justify-between">
          <div class="flex items-center space-x-3">
            <button v-if="currentView === 'chat'" @click="goBack" class="p-1 hover:bg-white/10 rounded-lg transition-colors mr-1">
              <ChevronLeft class="w-5 h-5" />
            </button>
            <div class="w-10 h-10 rounded-full bg-white/20 flex items-center justify-center backdrop-blur-md">
              <User class="w-6 h-6 text-white" />
            </div>
            <div>
              <h3 class="font-bold text-sm tracking-tight text-white">
                {{ currentView === 'chat' ? activeUserName : (authStore.isAdmin ? 'Customer Inquiries' : 'Conversations') }}
              </h3>
              <div class="flex items-center space-x-1">
                <div class="w-2 h-2 rounded-full bg-green-400 animate-pulse"></div>
                <span class="text-[10px] text-blue-100 font-medium">Online</span>
              </div>
            </div>
          </div>
          <button @click="toggleChat" class="p-2 hover:bg-white/10 rounded-xl transition-colors">
            <X class="w-5 h-5" />
          </button>
        </div>

        <!-- Content Area -->
        <div class="flex-1 overflow-hidden flex flex-col">
          <!-- Conversation List (Unified for Admin & Users) -->
          <div v-if="currentView === 'list'" class="flex-1 overflow-y-auto p-4 space-y-3 bg-gray-50/50">
            <!-- Admin View: List of Users who sent messages -->
            <template v-if="authStore.isAdmin">
              <div v-if="conversations.length === 0" class="flex flex-col items-center justify-center h-full text-center space-y-2 opacity-40">
                <MessageCircle class="w-12 h-12 mb-2" />
                <p class="text-sm font-medium">새로운 문의사항이 없습니다.</p>
              </div>
              
              <button
                v-for="conv in conversations"
                :key="conv.senderId"
                @click="selectUser(conv)"
                class="w-full flex items-center p-4 bg-white hover:bg-white/80 border border-gray-100 rounded-2xl transition-all shadow-sm hover:shadow-md text-left group"
              >
                <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center mr-3 shrink-0">
                  <User class="w-5 h-5 text-blue-600" />
                </div>
                <div class="min-w-0 flex-1">
                  <div class="flex justify-between items-start">
                    <h4 class="font-bold text-sm text-gray-800 truncate">{{ conv.senderName }}</h4>
                    <span class="text-[10px] text-gray-400 shrink-0">{{ new Date(conv.timestamp).toLocaleDateString([], { month: 'short', day: 'numeric' }) }}</span>
                  </div>
                  <p class="text-xs text-gray-500 truncate mt-0.5">{{ conv.content }}</p>
                </div>
                <div v-if="!conv.isRead" class="w-2 h-2 rounded-full bg-blue-600 ml-2 shadow-sm animate-pulse"></div>
              </button>
            </template>

            <!-- User View: List of Admins (Human & System) -->
            <template v-else>
              <div v-if="loadingUsers && !chatStore.adminUser && !chatStore.systemUser" class="flex flex-col items-center justify-center h-full text-center space-y-2 opacity-40">
                <Loader2 class="w-10 h-10 animate-spin mb-2" />
                <p class="text-sm font-medium">연결 중입니다...</p>
                <button @click="isOpen = false" class="text-xs text-blue-500 underline mt-2">닫기</button>
              </div>

              <div v-else-if="!chatStore.adminUser && !chatStore.systemUser" class="flex flex-col items-center justify-center h-full text-center p-6 space-y-3 opacity-60">
                <MessageCircle class="w-12 h-12 text-gray-300" />
                <p class="text-sm font-semibold text-gray-500 leading-relaxed">연결 가능한 상담원이 없습니다.<br/>잠시 후 다시 시도해 주세요.</p>
                <button @click="toggleChat" class="px-4 py-1.5 bg-gray-100 text-gray-600 rounded-lg text-xs font-bold hover:bg-gray-200 transition-all">새로고침</button>
              </div>

              <!-- Human Admin -->
              <button
                v-if="chatStore.adminUser"
                @click="selectUser({ senderId: chatStore.adminUser.id, senderName: '관리자 지원' })"
                class="w-full flex items-center p-4 bg-white hover:bg-white/80 border border-gray-100 rounded-2xl transition-all shadow-sm hover:shadow-md text-left group"
              >
                <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center mr-3 shrink-0">
                  <User class="w-5 h-5 text-blue-600" />
                </div>
                <div class="min-w-0 flex-1">
                  <h4 class="font-bold text-sm text-gray-800">관리자 지원</h4>
                  <p class="text-xs text-gray-500 mt-0.5">운영진과 1:1 채팅하기</p>
                </div>
                <div class="text-blue-500 opacity-0 group-hover:opacity-100 transition-opacity">
                  <ChevronLeft class="w-4 h-4 rotate-180" />
                </div>
              </button>

              <!-- System Admin -->
              <button
                v-if="chatStore.systemUser"
                @click="selectUser({ senderId: chatStore.systemUser.id, senderName: '시스템 알림' })"
                class="w-full flex items-center p-4 bg-white hover:bg-white/80 border border-gray-100 rounded-2xl transition-all shadow-sm hover:shadow-md text-left group"
              >
                <div class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center mr-3 shrink-0">
                  <MessageCircle class="w-5 h-5 text-indigo-600" />
                </div>
                <div class="min-w-0 flex-1">
                  <h4 class="font-bold text-sm text-gray-800">시스템 알림</h4>
                  <p class="text-xs text-gray-500 mt-0.5">자동 발송된 시스템 메시지 확인</p>
                </div>
                <div class="text-indigo-500 opacity-0 group-hover:opacity-100 transition-opacity">
                  <ChevronLeft class="w-4 h-4 rotate-180" />
                </div>
              </button>
            </template>
          </div>

          <!-- Chat Messages (User or Admin Detail) -->
          <template v-else>
            <div ref="scrollContainer" class="flex-1 overflow-y-auto p-5 space-y-4 bg-gray-50/50">
              <div v-if="filteredMessages.length === 0" class="flex flex-col items-center justify-center h-full text-center space-y-2 opacity-40">
                <MessageCircle class="w-12 h-12 mb-2" />
                <p class="text-sm font-medium">대화를 시작해보세요!</p>
              </div>
              
              <div
                v-for="msg in filteredMessages"
                :key="msg.id"
                :class="['flex', isSelf(msg.senderId) ? 'justify-end' : 'justify-start']"
              >
                <div
                  :class="[
                    'max-w-[80%] p-3.5 rounded-2xl text-sm shadow-sm transition-all',
                    isSelf(msg.senderId) 
                      ? 'bg-blue-600 text-white rounded-tr-none' 
                      : 'bg-white text-gray-800 border border-gray-100 rounded-tl-none'
                  ]"
                >
                  <p class="leading-relaxed">{{ msg.content }}</p>
                  <span class="block text-[10px] mt-1.5 opacity-60 text-right">
                    {{ new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Input Area -->
            <div v-if="!(!authStore.isAdmin && activeUserId === chatStore.systemUser?.id)" class="p-4 bg-white border-t border-gray-100">
              <div class="relative flex items-center bg-gray-100 rounded-2xl group focus-within:ring-2 focus-within:ring-blue-500/20 focus-within:bg-white focus-within:border-blue-500 border border-transparent transition-all">
                <input
                  v-model="messageText"
                  @keyup.enter="handleSend"
                  type="text"
                  placeholder="메시지를 입력하세요..."
                  class="flex-1 bg-transparent border-none outline-none px-4 py-3 text-sm placeholder:text-gray-400"
                />
                <button
                  @click="handleSend"
                  class="p-2 mr-1 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-all shadow-md active:scale-95 disabled:opacity-50 disabled:grayscale"
                  :disabled="!messageText.trim()"
                >
                  <Send class="w-4 h-4" />
                </button>
              </div>
            </div>
            <div v-else class="p-4 bg-gray-50 border-t border-gray-100 text-center">
              <p class="text-[10px] text-gray-400 font-medium italic">시스템 알림에는 회신할 수 없습니다.</p>
            </div>
          </template>
        </div>
      </div>
    </transition>

    <!-- Floating Bubble -->
    <button
      @click="toggleChat"
      :class="[
        'w-14 h-14 rounded-full flex items-center justify-center shadow-lg transition-all duration-500 active:scale-90 relative group',
        isOpen ? 'bg-white text-blue-600 rotate-90 scale-110 shadow-blue-500/20 border border-gray-100' : 'bg-gradient-to-br from-blue-500 to-indigo-600 text-white hover:shadow-xl hover:-translate-y-1'
      ]"
    >
      <X v-if="isOpen" class="w-6 h-6" />
      <MessageCircle v-else class="w-7 h-7" />
      
      <!-- Unread Badge -->
      <transition enter-active-class="animate-bounce" leave-active-class="opacity-0">
        <div v-if="!isOpen && chatStore.unreadCount > 0" class="absolute -top-1 -right-1 min-w-[22px] h-[22px] bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center px-1 border-2 border-white ring-4 ring-red-500/20">
          {{ chatStore.unreadCount > 99 ? '99+' : chatStore.unreadCount }}
        </div>
      </transition>
    </button>
  </div>
</template>

<style scoped>
.scroll-container::-webkit-scrollbar {
  width: 4px;
}
.scroll-container::-webkit-scrollbar-track {
  background: transparent;
}
.scroll-container::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}
</style>
