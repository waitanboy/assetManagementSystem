import { defineStore } from 'pinia'
import api from '../api'

export interface Department {
  id?: number
  name: string
}

export const useDepartmentStore = defineStore('department', {
  state: () => ({
    departments: [] as Department[],
    loading: false,
    error: null as string | null
  }),

  actions: {
    async fetchDepartments() {
      this.loading = true
      try {
        const response = await api.get('/departments')
        this.departments = response.data
      } catch (err: any) {
        this.error = err.message
      } finally {
        this.loading = false
      }
    },

    async addDepartment(name: string) {
      try {
        const response = await api.post('/departments', { name })
        this.departments.push(response.data)
      } catch (err: any) {
        throw err
      }
    },

    async updateDepartment(id: number, name: string) {
      try {
        await api.put(`/departments/${id}`, { name })
        const index = this.departments.findIndex(d => d.id === id)
        if (index !== -1) {
          this.departments[index].name = name
        }
      } catch (err: any) {
        throw err
      }
    },

    async deleteDepartment(id: number) {
      try {
        await api.delete(`/departments/${id}`)
        this.departments = this.departments.filter(d => d.id !== id)
      } catch (err: any) {
        throw err
      }
    }
  }
})
