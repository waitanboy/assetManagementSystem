import { defineStore } from 'pinia'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import api from '../api'
import { useAuthStore } from './authStore'

export interface ChatMessage {
  id?: number
  senderId: number
  receiverId: number
  content: string
  timestamp: string
  isRead: boolean
  senderName?: string
  senderEmail?: string
}

export const useChatStore = defineStore('chat', {
  state: () => ({
    client: null as Client | null,
    messages: [] as ChatMessage[],
    connected: false,
    unreadCount: 0,
    adminUser: null as any | null,
    systemUser: null as any | null,
    activeAdminChatId: null as number | null, // For Admin view
  }),

  actions: {
    async getAdminConversations() {
      try {
        const res = await api.get<ChatMessage[]>('/chat/admin/conversations')
        return res.data
      } catch (err) {
        console.error('Failed to fetch admin conversations', err)
        return []
      }
    },

    async fetchAdminUser() {
      try {
        const res = await api.get('/chat/admin-user')
        this.adminUser = res.data
      } catch (err) {
        console.error('Failed to fetch admin user', err)
      }
    },

    async fetchSystemUser() {
      try {
        const res = await api.get('/chat/system-user')
        this.systemUser = res.data
      } catch (err) {
        console.error('Failed to fetch system user', err)
      }
    },

    async fetchHistory(otherUserId: number) {
      try {
        const res = await api.get<ChatMessage[]>(`/chat/history/${otherUserId}`)
        this.messages = res.data
      } catch (err) {
        console.error('Failed to fetch chat history', err)
      }
    },

    async fetchUnreadCount() {
      try {
        const res = await api.get<number>('/chat/unread-count')
        this.unreadCount = res.data
      } catch (err) {
        console.error('Failed to fetch unread count', err)
      }
    },

    async markAsRead(senderId: number) {
      try {
        await api.post(`/chat/read/${senderId}`)
        this.fetchUnreadCount()
      } catch (err) {
        console.error('Failed to mark as read', err)
      }
    },

    setActiveAdminChat(userId: number | null) {
      this.activeAdminChatId = userId
    },

    connect() {
      if (this.connected || this.client?.active) return

      const authStore = useAuthStore()
      if (!authStore.isAuthenticated) return

      // SockJS automatically sends cookies (withCredentials), so the server can
      // authenticate via the jwt_token cookie during the HTTP handshake.
      const socketUrl = import.meta.env.DEV ? 'http://localhost:8080/ws' : '/ws'

      this.client = new Client({
        webSocketFactory: () => new SockJS(socketUrl),
        // No Authorization header needed — cookie handles auth via SockJS handshake
        connectHeaders: {},
        debug: (str) => {
          console.log('[STOMP]', str)
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      })

      this.client.onConnect = () => {
        this.connected = true
        console.log('[Chat] Connected to WebSocket ✓')

        // Subscribe to private messages for the current user
        this.client?.subscribe('/user/queue/messages', (message) => {
          const msg = JSON.parse(message.body) as ChatMessage
          console.log('[Chat] Received message:', msg)
          
          // Avoid duplicating sender's own messages already optimistically added
          const isDuplicate = this.messages.some(
            m => m.id && m.id === msg.id
          )
          if (!isDuplicate) {
            this.messages.push(msg)
          } else {
            // Update the existing entry with server-assigned id
            const idx = this.messages.findIndex(m => m.id === msg.id)
            if (idx !== -1) this.messages[idx] = msg
          }

          if (msg.senderId !== authStore.id) {
            this.unreadCount++
          }
        })
      }

      this.client.onStompError = (frame) => {
        console.error('[Chat] STOMP error:', frame.headers['message'])
        console.error('[Chat] Details:', frame.body)
      }

      this.client.onDisconnect = () => {
        this.connected = false
        console.log('[Chat] Disconnected from WebSocket')
      }

      this.client.activate()
    },

    disconnect() {
      this.client?.deactivate()
      this.connected = false
      this.client = null
    },

    sendMessage(receiverId: number, content: string) {
      const authStore = useAuthStore()
      if (!this.client?.active) {
        console.error('[Chat] Not connected to WebSocket')
        return
      }

      const payload = {
        senderId: authStore.id,
        receiverId,
        content,
      }

      console.log('[Chat] Sending message:', payload)
      this.client.publish({
        destination: '/app/chat.send',
        body: JSON.stringify(payload),
      })
    },
  },
})

