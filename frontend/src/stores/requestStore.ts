import { defineStore } from 'pinia'
import api from '../api'

export interface RentalRequest {
  id?: number
  assetId: number
  userId?: number
  requestDate?: string
  plannedDueDate: string
  purpose: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  processedBy?: number
  processDate?: string
  rejectReason?: string
  
  // Joined fields
  assetName?: string
  assetSerial?: string
  userName?: string
  userEmail?: string
  userDepartment?: string
  processedByEmail?: string
  ocrData?: string
  useOcr?: boolean
}

export const useRequestStore = defineStore('request', {
  state: () => ({
    pendingRequests: [] as RentalRequest[],
    myRequests: [] as RentalRequest[],
    allRequests: [] as RentalRequest[],
    loading: false,
    error: null as string | null,
  }),

  actions: {
    async fetchPendingRequests() {
      this.loading = true
      try {
        const response = await api.get<RentalRequest[]>('/requests/pending')
        this.pendingRequests = response.data
      } catch (err: any) {
        this.error = err.message || 'Failed to fetch pending requests'
      } finally {
        this.loading = false
      }
    },

    async fetchMyRequests() {
      this.loading = true
      try {
        const response = await api.get<RentalRequest[]>('/requests/my')
        this.myRequests = response.data
      } catch (err: any) {
        this.error = err.message || 'Failed to fetch my requests'
      } finally {
        this.loading = false
      }
    },

    async fetchAllRequests() {
      this.loading = true
      try {
        const response = await api.get<RentalRequest[]>('/requests/all')
        this.allRequests = response.data
      } catch (err: any) {
        this.error = err.message || 'Failed to fetch requests'
      } finally {
        this.loading = false
      }
    },

    async createRequest(request: Partial<RentalRequest>) {
      try {
        await api.post('/requests', request)
        await this.fetchMyRequests()
      } catch (err: any) {
        throw err
      }
    },

    async approveRequest(id: number, signatureData: string, ocrData?: string) {
      try {
        await api.post(`/requests/${id}/approve`, { signatureData, ocrData })
        await this.fetchPendingRequests()
      } catch (err: any) {
        throw err
      }
    },

    async rejectRequest(id: number, reason: string) {
      try {
        await api.post(`/requests/${id}/reject`, { reason })
        await this.fetchPendingRequests()
      } catch (err: any) {
        throw err
      }
    }
  }
})
