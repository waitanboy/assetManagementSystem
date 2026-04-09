import { defineStore } from 'pinia'
import api from '../api'

interface UserState {
  id: number | null
  email: string | null
  role: string | null
  isAuthenticated: boolean
  initialized: boolean
}

export const useAuthStore = defineStore('auth', {
  state: (): UserState => ({
    id: null,
    email: null,
    role: null,
    isAuthenticated: false,
    initialized: false,
  }),

  getters: {
    isAuthenticated: (state) => !!state.email,
    isAdmin: (state) => state.role === 'ADMIN',
  },

  actions: {
    async signup(credentials: any) {
      try {
        const response = await api.post('/auth/signup', credentials)
        return response.data
      } catch (error) {
        throw error
      }
    },

    async login(credentials: any) {
      try {
        const response = await api.post('/auth/login', credentials)
        this.id = response.data.id
        this.email = response.data.email
        this.role = response.data.role
        this.isAuthenticated = true
        return true
      } catch (error) {
        this.isAuthenticated = false
        throw error
      }
    },

    async checkAuth() {
      try {
        const response = await api.get('/auth/me')
        if (response.data) {
          this.id = response.data.id
          this.email = response.data.email
          this.role = response.data.role
          this.isAuthenticated = true
          return true
        }
      } catch (error) {
        this.email = null
        this.role = null
        this.isAuthenticated = false
      } finally {
        this.initialized = true
      }
      return false
    },

    async logout() {
      try {
        await api.post('/auth/logout')
        this.id = null
        this.email = null
        this.role = null
        this.isAuthenticated = false
      } catch (error) {
        console.error('Logout failed', error)
      }
    },
  },
})
