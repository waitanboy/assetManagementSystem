import { defineStore } from 'pinia'
import api from '../api'

export interface Notice {
  id: number
  title: string
  content: string
  authorId: number
  authorEmail: string
  createdAt: string
  updatedAt: string | null
}

export const useNoticeStore = defineStore('notice', {
  state: () => ({
    notices: [] as Notice[],
    loading: false,
    error: null as string | null,
  }),
  actions: {
    async fetchNotices() {
      this.loading = true
      this.error = null
      try {
        const response = await api.get('/notices')
        this.notices = response.data
      } catch (err: any) {
        this.error = 'Failed to fetch announcements'
        console.error(err)
      } finally {
        this.loading = false
      }
    },
    async createNotice(data: { title: string; content: string }) {
      this.loading = true
      this.error = null
      try {
        const response = await api.post('/notices', data)
        this.notices.unshift(response.data)
      } catch (err: any) {
        this.error = 'Failed to create notice'
        throw err
      } finally {
        this.loading = false
      }
    },
    async updateNotice(id: number, data: { title: string; content: string }) {
      this.loading = true
      this.error = null
      try {
        const response = await api.put(`/notices/${id}`, data)
        const index = this.notices.findIndex((n) => n.id === id)
        if (index !== -1) {
          this.notices[index] = response.data
        }
      } catch (err: any) {
        this.error = 'Failed to update notice'
        throw err
      } finally {
        this.loading = false
      }
    },
    async deleteNotice(id: number) {
      this.loading = true
      this.error = null
      try {
        await api.delete(`/notices/${id}`)
        this.notices = this.notices.filter((n) => n.id !== id)
      } catch (err: any) {
        this.error = 'Failed to delete notice'
        throw err
      } finally {
        this.loading = false
      }
    },
  },
})
