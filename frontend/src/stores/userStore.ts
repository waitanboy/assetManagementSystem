import { defineStore } from 'pinia'
import api from '../api'

export interface User {
  id: number
  email: string
  name: string
  role: string
  department?: string
  status?: string
}

export const useUserStore = defineStore('user', {
  state: () => ({
    users: [] as User[],
    activeUsers: [] as User[], // APPROVED 상태의 유저만 저정 (대여/수정에 사용)
    selectedUser: null as User | null,
    loading: false,
    error: null as string | null,
  }),

  actions: {
    async fetchUserById(id: number) {
      this.loading = true
      this.selectedUser = null
      try {
        const response = await api.get<User>(`/users/${id}`)
        this.selectedUser = response.data
      } catch (err: any) {
        this.error = err.message || 'Failed to fetch user'
      } finally {
        this.loading = false
      }
    },
    async fetchUsers() {
      this.loading = true
      try {
        const response = await api.get<User[]>('/users')
        this.users = response.data
      } catch (err: any) {
        this.error = err.message || 'Failed to fetch users'
      } finally {
        this.loading = false
      }
    },

    async fetchActiveUsers() {
      this.loading = true
      try {
        const response = await api.get<User[]>('/users/active')
        this.activeUsers = response.data
      } catch (err: any) {
        this.error = err.message || 'Failed to fetch active users'
      } finally {
        this.loading = false
      }
    },

    async approveUser(id: number) {
      this.error = null
      try {
        await api.put(`/users/${id}/approve`)
        const user = this.users.find((u) => u.id === id)
        if (user) {
          user.status = 'APPROVED'
        }
      } catch (err: any) {
        this.error = err.message || 'Failed to approve user'
        throw err
      }
    },

    async rejectUser(id: number) {
      this.error = null
      try {
        await api.put(`/users/${id}/reject`)
        const user = this.users.find((u) => u.id === id)
        if (user) {
          user.status = 'REJECTED'
        }
      } catch (err: any) {
        this.error = err.message || 'Failed to reject user'
        throw err
      }
    },

    async updateUser(id: number, userData: Partial<User>) {
      this.error = null
      try {
        await api.put(`/users/${id}`, userData)
        if (this.selectedUser && this.selectedUser.id === id) {
          this.selectedUser = { ...this.selectedUser, ...userData }
        }
        await this.fetchUsers() // Refresh list
      } catch (err: any) {
        this.error = err.message || 'Failed to update user'
        throw err
      }
    },
  },
})
