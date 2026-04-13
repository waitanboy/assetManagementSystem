import { defineStore } from 'pinia'
import api from '../api'

export interface Asset {
  id?: number
  categoryId: number
  name: string
  serialNumber: string
  status: 'AVAILABLE' | 'RENTED' | 'REPAIRING' | 'REQUESTED'
  location?: string
  imageUrl?: string
  dueDate?: string // ISO date string (YYYY-MM-DD), 대여 중일 때만 포함
  rentedByEmail?: string       // Admin에게만 표시, RENTED 상태일 때만 포함
  rentedByDepartment?: string  // Admin에게만 표시, RENTED 상태일 때만 포함
  rentedByUserId?: number      // Return 권한 체크용
}

export interface AssetStats {
  total: number
  available: number
  rented: number
  repairing: number
}

export interface RepairLog {
  id: number
  assetId: number
  reportedBy: number
  reason: string
  estimatedCost?: number
  finalCost?: number
  status: 'IN_PROGRESS' | 'COMPLETED'
  startDate: string
  endDate?: string
}

export const useAssetStore = defineStore('asset', {
  state: () => ({
    assets: [] as Asset[],
    myAssets: [] as Asset[],
    repairs: [] as RepairLog[],
    loading: false,
    error: null as string | null,
    stats: {
      total: 0,
      available: 0,
      rented: 0,
      repairing: 0,
    } as AssetStats,
  }),

  actions: {
    async fetchRepairs() {
      try {
        const response = await api.get<RepairLog[]>('/assets/repairs/all')
        this.repairs = response.data
      } catch (err: any) {
        console.error('Failed to fetch repairs', err)
      }
    },

    async fetchAssets(search?: string, status?: string) {
      this.loading = true
      try {
        const response = await api.get<Asset[]>('/assets', {
          params: { search, status }
        })
        this.assets = response.data
      } catch (err: any) {
        this.error = err.message || 'Failed to fetch assets'
      } finally {
        this.loading = false
      }
    },

    async fetchMyAssets() {
      try {
        const response = await api.get<Asset[]>('/assets/my')
        this.myAssets = response.data
      } catch (err: any) {
        console.error('Failed to fetch my assets', err)
      }
    },

    async addAsset(asset: Asset) {
      try {
        const response = await api.post<Asset>('/assets', asset)
        this.assets.push(response.data)
      } catch (err: any) {
        throw err
      }
    },

    async updateAsset(id: number, asset: Asset) {
      try {
        await api.put(`/assets/${id}`, asset)
        const index = this.assets.findIndex(a => a.id === id)
        if (index !== -1) {
          this.assets[index] = { ...this.assets[index], ...asset }
        }
      } catch (err: any) {
        throw err
      }
    },

    async deleteAsset(id: number) {
      try {
        await api.delete(`/assets/${id}`)
        this.assets = this.assets.filter(a => a.id !== id)
      } catch (err: any) {
        throw err
      }
    },

    async fetchStats() {
      try {
        const response = await api.get<AssetStats>('/assets/stats')
        this.stats = response.data
      } catch (err: any) {
        console.error('Failed to fetch stats', err)
      }
    },

    async rentAsset(id: number, userId: number, note: string, dueDate?: string) {
      try {
        await api.post(`/assets/${id}/rent`, { userId, note, dueDate })
        await this.fetchAssets()
        await this.fetchMyAssets()
        await this.fetchStats()
      } catch (err: any) {
        throw err
      }
    },

    async returnAsset(id: number, userId: number, note: string) {
      try {
        await api.post(`/assets/${id}/return`, { userId, note })
        await this.fetchAssets()
        await this.fetchMyAssets()
        await this.fetchStats()
      } catch (err: any) {
        throw err
      }
    },

    async reassignRenter(id: number, userId: number, dueDate?: string) {
      try {
        await api.patch(`/assets/${id}/reassign`, { userId, dueDate })
        await this.fetchAssets()
      } catch (err: any) {
        throw err
      }
    },

    async getAssetById(id: number) {
      this.loading = true
      try {
        const response = await api.get<Asset>(`/assets/${id}`)
        return response.data
      } catch (err: any) {
        this.error = err.message || 'Failed to fetch asset'
        return null
      } finally {
        this.loading = false
      }
    },

    async importAssetsBulk(assets: Asset[]) {
      try {
        await api.post('/assets/bulk', assets)
        await this.fetchAssets()
        await this.fetchStats()
      } catch (err: any) {
        throw err
      }
    },

    async startRepair(id: number, reason: string, estimatedCost?: number) {
      try {
        await api.post(`/assets/${id}/repair/start`, { reason, estimatedCost })
        await this.fetchAssets()
        await this.fetchStats()
      } catch (err: any) {
        throw err
      }
    },

    async completeRepair(id: number, finalCost: number, resolutionNote?: string) {
      try {
        await api.post(`/assets/${id}/repair/complete`, { finalCost, resolutionNote })
        await this.fetchAssets()
        await this.fetchStats()
      } catch (err: any) {
        throw err
      }
    },
  },
})
