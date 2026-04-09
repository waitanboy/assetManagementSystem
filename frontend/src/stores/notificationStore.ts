import { defineStore } from 'pinia'
import api from '../api'

export interface Notification {
  id: string
  type: string
  title: string
  message: string
  link: string
  timestamp: string
  isRead: boolean
}

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    notifications: [] as Notification[],
    loading: false,
    error: null as string | null,
  }),
  getters: {
    unreadCount(state) {
      return state.notifications.filter((n) => !n.isRead).length
    },
  },
  actions: {
    async fetchNotifications() {
      this.loading = true
      this.error = null
      try {
        const response = await api.get('/notifications')
        this.notifications = response.data
      } catch (err: any) {
        this.error = 'Failed to fetch notifications'
        console.error(err)
      } finally {
        this.loading = false
      }
    },
    markAsRead(id: string) {
      const n = this.notifications.find((notif) => notif.id === id)
      if (n) {
        n.isRead = true
      }
    },
    markAllAsRead() {
      this.notifications.forEach((n) => (n.isRead = true))
    },
  },
})
