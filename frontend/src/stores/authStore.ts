import { defineStore } from 'pinia'
import api from '../api'

interface UserState {
  id: number | null
  email: string | null
  name: string | null
  role: string | null
  department: string | null
  isAuthenticated: boolean
  initialized: boolean
}

export const useAuthStore = defineStore('auth', {
  state: (): UserState => ({
    id: null,
    email: null,
    name: null,
    role: null,
    department: null,
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
        this.name = response.data.name
        this.role = response.data.role
        this.department = response.data.department
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
          this.name = response.data.name
          this.role = response.data.role
          this.department = response.data.department
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
    
    async updateProfile(profileData: any) {
      try {
        await api.put('/users/profile', profileData)
        if (profileData.email) this.email = profileData.email
        if (profileData.name) this.name = profileData.name
        if (profileData.department) this.department = profileData.department
        return true
      } catch (error) {
        throw error
      }
    }
  },
})
