import { defineStore } from 'pinia'
import api from '../api'

export interface Category {
  id?: number
  name: string
}

export const useCategoryStore = defineStore('category', {
  state: () => ({
    categories: [] as Category[],
    loading: false,
    error: null as string | null,
  }),

  actions: {
    async fetchCategories() {
      this.loading = true
      try {
        const response = await api.get<Category[]>('/categories')
        this.categories = response.data
      } catch (err: any) {
        this.error = err.message || 'Failed to fetch categories'
      } finally {
        this.loading = false
      }
    },

    async addCategory(name: string) {
      try {
        const response = await api.post<Category>('/categories', { name })
        this.categories.push(response.data)
      } catch (err: any) {
        throw err
      }
    },

    async updateCategory(id: number, name: string) {
      try {
        await api.put(`/categories/${id}`, { name })
        const category = this.categories.find(c => c.id === id)
        if (category) category.name = name
      } catch (err: any) {
        throw err
      }
    },

    async deleteCategory(id: number) {
      try {
        await api.delete(`/categories/${id}`)
        this.categories = this.categories.filter(c => c.id !== id)
      } catch (err: any) {
        throw err
      }
    },
  },
})
