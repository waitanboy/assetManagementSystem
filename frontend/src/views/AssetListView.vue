<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { Plus, Search, MapPin, Tag, Edit2, Trash2, ArrowUpRight, ArrowDownLeft, Filter, History, UserCircle, Lock, Download, Upload } from 'lucide-vue-next'
import { useAssetStore, type Asset } from '../stores/assetStore'
import { useAuthStore } from '../stores/authStore'
import { useCategoryStore } from '../stores/categoryStore'
import AssetFormModal from '../components/assets/AssetFormModal.vue'
import TransactionModal from '../components/assets/TransactionModal.vue'
import RentalRequestModal from '../components/assets/RentalRequestModal.vue'

const assetStore = useAssetStore()
const authStore = useAuthStore()
const categoryStore = useCategoryStore()
const showModal = ref(false)
const showTransactionModal = ref(false)
const showRequestModal = ref(false)
const searchQuery = ref('')
const selectedStatus = ref('')
const selectedAsset = ref<Asset | null>(null)
const transactionType = ref<'RENT' | 'RETURN'>('RENT')

let timeout: any = null
const debouncedFetch = () => {
  if (timeout) clearTimeout(timeout)
  timeout = setTimeout(() => {
    assetStore.fetchAssets(searchQuery.value, selectedStatus.value)
  }, 300)
}

onMounted(() => {
  assetStore.fetchAssets()
})

watch([searchQuery, selectedStatus], () => {
  debouncedFetch()
})

const openAddModal = () => {
  selectedAsset.value = null
  showModal.value = true
}

const openEditModal = (asset: Asset) => {
  selectedAsset.value = asset
  showModal.value = true
}

const openRentModal = (asset: Asset) => {
  selectedAsset.value = asset
  if (authStore.isAdmin) {
    transactionType.value = 'RENT'
    showTransactionModal.value = true
  } else {
    showRequestModal.value = true
  }
}

const openReturnModal = (asset: Asset) => {
  selectedAsset.value = asset
  transactionType.value = 'RETURN'
  showTransactionModal.value = true
}

const handleDelete = async (id: number) => {
  if (confirm('Are you sure you want to delete this asset?')) {
    await assetStore.deleteAsset(id)
  }
}

const getStatusClass = (status: string) => {
  switch (status) {
    case 'AVAILABLE': return 'bg-green-50 text-green-700 border-green-200'
    case 'RENTED': return 'bg-orange-50 text-orange-700 border-orange-200'
    case 'REPAIRING': return 'bg-red-50 text-red-700 border-red-200'
    case 'REQUESTED': return 'bg-purple-50 text-purple-700 border-purple-200'
    default: return 'bg-gray-50 text-gray-700 border-gray-200'
  }
}

// ========================
// CSV Export / Import Logic
// ========================
const fileInput = ref<HTMLInputElement | null>(null)

const exportCSV = () => {
  const headers = ['Name', 'Category', 'Status', 'Serial Number', 'Location']
  const rows = assetStore.assets.map(asset => {
    const cat = categoryStore.categories.find(c => c.id === asset.categoryId)
    const categoryName = cat ? cat.name : String(asset.categoryId)
    return [
      `"${asset.name || ''}"`,
      `"${categoryName || ''}"`,
      `"${asset.status || 'AVAILABLE'}"`,
      `"${asset.serialNumber || ''}"`,
      `"${asset.location || ''}"`
    ].join(',')
  })
  
  const csvContent = [headers.join(','), ...rows].join('\n')
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' }) // BOM for Excel
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.setAttribute('href', url)
  link.setAttribute('download', 'assets_export.csv')
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

const handleFileUpload = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = async (e) => {
    try {
      const text = e.target?.result as string
      // Basic CSV parsing splitting by newline and comma (ignoring commas inside quotes for simplicity in this basic version, but ideally using a CSV parser)
      const lines = text.split('\n').map(line => line.trim()).filter(line => line)
      if (lines.length < 2) throw new Error('파일에 데이터가 없거나 잘못된 형식입니다.')
      
      const newAssets: Asset[] = []
      // Skip header (index 0)
      for (let i = 1; i < lines.length; i++) {
        // split by comma, remove quotes
        const row = lines[i].match(/(".*?"|[^",\s]+)(?=\s*,|\s*$)/g)?.map(val => val.replace(/^"|"$/g, '').trim()) || lines[i].split(',').map(val => val.replace(/^"|"$/g, '').trim())
        
        if (row.length < 4) continue // Skip invalid rows (Name, Cat, Status, Serial is minimum)
        
        const [name, catName, status, serialNumber, location] = row
        
        const cat = categoryStore.categories.find(c => c.name.toLowerCase() === catName.toLowerCase())
        if (!cat) {
            throw new Error(`CSV 행 ${i + 1}: 알 수 없는 카테고리입니다 ("${catName}"). 먼저 카테고리를 시스템에 등록해 주세요.`)
        }

        newAssets.push({
          name: name || 'Unnamed Asset',
          categoryId: cat.id!,
          status: (status as 'AVAILABLE' | 'RENTED' | 'REPAIRING') || 'AVAILABLE',
          serialNumber: serialNumber || `SN-${Math.random().toString(36).substring(2,8).toUpperCase()}`,
          location: location || '',
        })
      }
      
      if (newAssets.length === 0) throw new Error('유효한 자산 데이터가 없습니다.')
      
      await assetStore.importAssetsBulk(newAssets)
      alert(`${newAssets.length}건의 자산이 일괄 등록되었습니다.`)
      
    } catch (err: any) {
      alert(`[Import 에러] ${err.message}`)
    } finally {
      if (fileInput.value) fileInput.value.value = '' // reset input
    }
  }
  reader.readAsText(file) // Read as text. Use EUC-KR if needed for Korean windows excel, but we assume UTF-8 for web.
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between space-y-4 lg:space-y-0 gap-4">
      <div class="flex flex-1 flex-col sm:flex-row gap-4 items-center">
        <div class="relative flex-1 w-full">
          <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
            <Search class="w-5 h-5" />
          </span>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Search by name, serial number..."
            class="block w-full pl-10 pr-4 py-2 bg-white border border-gray-100 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all"
          />
        </div>
        
        <div class="relative flex-shrink-0 w-full sm:w-auto">
          <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400 pointer-events-none">
            <Filter class="w-4 h-4" />
          </span>
          <select 
            v-model="selectedStatus"
            class="appearance-none block w-full pl-9 pr-10 py-2 bg-white border border-gray-100 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all cursor-pointer text-sm font-medium"
          >
            <option value="">All Status</option>
            <option value="AVAILABLE">Available</option>
            <option value="RENTED">Rented</option>
            <option value="REPAIRING">Repairing</option>
          </select>
          <div class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-gray-400">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path></svg>
          </div>
        </div>
      </div>

      <div v-if="authStore.isAdmin" class="flex gap-2">
        <input type="file" ref="fileInput" accept=".csv" class="hidden" @change="handleFileUpload" />
        <button
          @click="fileInput?.click()"
          class="flex items-center justify-center space-x-2 px-4 py-2 bg-white border border-gray-200 text-gray-700 rounded-xl hover:bg-gray-50 transition-all shadow-sm active:scale-95"
        >
          <Upload class="w-4 h-4" />
          <span class="font-semibold text-sm">Import CSV</span>
        </button>
        <button
          @click="exportCSV"
          class="flex items-center justify-center space-x-2 px-4 py-2 bg-white border border-gray-200 text-gray-700 rounded-xl hover:bg-gray-50 transition-all shadow-sm active:scale-95"
        >
          <Download class="w-4 h-4" />
          <span class="font-semibold text-sm">Export CSV</span>
        </button>
        <button
          @click="openAddModal"
          class="flex items-center justify-center space-x-2 px-6 py-2 bg-slate-900 text-white rounded-xl hover:bg-slate-800 transition-all shadow-lg shadow-slate-200 active:scale-95"
        >
          <Plus class="w-5 h-5" />
          <span class="font-semibold">Add Asset</span>
        </button>
      </div>
    </div>

    <!-- Assets List -->
    <h3 class="text-lg font-bold text-gray-800 mb-4 flex items-center pt-2">
      <span class="w-8 h-8 rounded-lg bg-gray-100 text-gray-600 flex items-center justify-center mr-3">
        <Search class="w-4 h-4" />
      </span>
      All Assets Registry
    </h3>
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-gray-50/50 border-b border-gray-100">
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">자산 정보</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">상태</th>
              <th v-if="authStore.isAdmin" class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">대여자</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">위치</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">시리얼 번호</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-400"></th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr v-if="assetStore.loading">
               <td :colspan="authStore.isAdmin ? 6 : 5" class="px-6 py-12 text-center text-gray-400">Loading assets...</td>
            </tr>
            <tr v-else-if="assetStore.assets.length === 0">
               <td :colspan="authStore.isAdmin ? 6 : 5" class="px-6 py-12 text-center text-gray-400">No assets found.</td>
            </tr>
            <tr v-for="asset in assetStore.assets" :key="asset.id" class="hover:bg-gray-50/80 transition-all group">
              <!-- 자산명 + 카테고리 -->
              <td class="px-6 py-4">
                <div class="flex items-center gap-3">
                  <div class="w-9 h-9 bg-blue-50 rounded-lg flex items-center justify-center text-blue-500 shrink-0">
                    <Tag class="w-4.5 h-4.5" />
                  </div>
                  <div>
                    <router-link :to="`/assets/${asset.id}`" class="font-semibold text-gray-800 hover:text-blue-600 transition-colors block text-sm leading-tight">
                      {{ asset.name }}
                    </router-link>
                    <span class="text-[10px] bg-slate-100 text-slate-400 px-1.5 py-0.5 rounded font-mono mt-0.5 inline-block">
                      Cat #{{ asset.categoryId }}
                    </span>
                  </div>
                </div>
              </td>

              <!-- 상태 뱃지 -->
              <td class="px-6 py-4">
                <span :class="['px-2.5 py-1 rounded-full text-[11px] font-bold tracking-wider uppercase border', getStatusClass(asset.status)]">
                  {{ asset.status }}
                </span>
              </td>

              <!-- 대여자 (Admin 전용 컬럼) -->
              <td v-if="authStore.isAdmin" class="px-6 py-4">
                <div v-if="asset.status === 'RENTED' && asset.rentedByEmail" class="flex items-center gap-2">
                  <div class="w-6 h-6 rounded-full bg-orange-100 flex items-center justify-center shrink-0">
                    <UserCircle class="w-4 h-4 text-orange-500" />
                  </div>
                  <div>
                    <p class="text-xs font-medium text-gray-700 leading-tight">{{ asset.rentedByEmail }}</p>
                    <p v-if="asset.rentedByDepartment" class="text-[10px] text-gray-400">{{ asset.rentedByDepartment }}</p>
                  </div>
                </div>
                <span v-else class="text-xs text-gray-300">—</span>
              </td>

              <!-- 위치 -->
              <td class="px-6 py-4 text-sm text-gray-600">
                <div class="flex items-center gap-1">
                  <MapPin class="w-3.5 h-3.5 text-gray-300 shrink-0" />
                  <span>{{ asset.location || '—' }}</span>
                </div>
              </td>

              <!-- 시리얼 -->
              <td class="px-6 py-4 text-xs font-mono text-gray-400">
                {{ asset.serialNumber }}
              </td>

              <!-- 액션 버튼 -->
              <td class="px-6 py-4 text-right whitespace-nowrap">
                <div class="flex items-center justify-end gap-1">
                  <template v-if="asset.status === 'AVAILABLE'">
                    <button @click="openRentModal(asset)" class="flex items-center gap-1 px-3 py-1.5 bg-blue-600 text-white hover:bg-blue-700 text-[11px] font-bold rounded-lg shadow-sm transition-all active:scale-95">
                      <ArrowUpRight class="w-3.5 h-3.5" />
                      <span>{{ authStore.isAdmin ? 'Rent' : 'Request' }}</span>
                    </button>
                  </template>
                  <template v-else-if="asset.status === 'REQUESTED'">
                    <span class="flex items-center gap-1 px-3 py-1.5 bg-purple-50 text-purple-500 text-[11px] font-bold rounded-lg border border-purple-100 select-none">
                      <Clock class="w-3.5 h-3.5" />
                      <span>신청중</span>
                    </span>
                  </template>
                  <template v-else-if="asset.status === 'RENTED'">
                    <!-- Admin: 모든 자산 반납 가능 / 일반 유저: 본인 대여분만 반납 가능 -->
                    <button
                      v-if="authStore.isAdmin || asset.rentedByUserId === authStore.id"
                      @click="openReturnModal(asset)"
                      class="flex items-center gap-1 px-3 py-1.5 bg-orange-500 text-white hover:bg-orange-600 text-[11px] font-bold rounded-lg shadow-sm transition-all active:scale-95"
                    >
                      <ArrowDownLeft class="w-3.5 h-3.5" />
                      <span>Return</span>
                    </button>
                    <span v-else class="flex items-center gap-1 px-3 py-1.5 bg-gray-100 text-gray-400 text-[11px] font-semibold rounded-lg cursor-not-allowed select-none">
                      <Lock class="w-3 h-3" />
                      <span>대여중</span>
                    </span>
                  </template>
                  
                  <template v-if="authStore.isAdmin">
                    <div class="w-px h-4 bg-gray-200 mx-1"></div>

                    <button @click="openEditModal(asset)" class="p-1.5 text-gray-400 hover:text-slate-700 hover:bg-slate-100 rounded-lg transition-all" title="Edit">
                      <Edit2 class="w-4 h-4" />
                    </button>
                    <button @click="handleDelete(asset.id!)" class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-all" title="Delete">
                      <Trash2 class="w-4 h-4" />
                    </button>
                  </template>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>


    <!-- Modals -->
    <AssetFormModal v-if="showModal" :asset="selectedAsset" @close="showModal = false" />
    <TransactionModal v-if="showTransactionModal" :asset="selectedAsset!" :type="transactionType" @close="showTransactionModal = false" />
    <RentalRequestModal v-if="showRequestModal" :asset="selectedAsset!" @close="showRequestModal = false" />
  </div>
</template>
