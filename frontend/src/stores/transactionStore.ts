import { defineStore } from 'pinia'
import api from '../api'

export interface Transaction {
  id: number
  assetId?: number | null
  userId: number
  type: 'RENT' | 'RETURN' | 'CREATE' | 'UPDATE' | 'DELETE' | 'IN' | 'OUT'
  transactionDate: string
  note?: string
  assetName?: string | null
  assetSerial?: string | null
  userEmail: string
  userRole: string
  userDepartment?: string | null
}

export const useTransactionStore = defineStore('transaction', {
  state: () => ({
    recentTransactions: [] as Transaction[],
    loading: false,
    error: null as string | null,
  }),

  actions: {
    async fetchRecentTransactions(limit = 10) {
      this.loading = true
      try {
        const response = await api.get<Transaction[]>('/transactions/recent', {
          params: { limit }
        })
        this.recentTransactions = response.data
      } catch (err: any) {
        this.error = err.message || 'Failed to fetch transactions'
      } finally {
        this.loading = false
      }
    },

    async fetchAssetHistory(assetId: number) {
      this.loading = true
      try {
        const response = await api.get<Transaction[]>(`/transactions/asset/${assetId}`)
        return response.data
      } catch (err: any) {
        this.error = err.message || 'Failed to fetch asset history'
        return []
      } finally {
        this.loading = false
      }
    },
  },
})
