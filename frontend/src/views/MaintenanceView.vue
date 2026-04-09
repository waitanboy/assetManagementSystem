<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Wrench, CheckCircle2, Search, Package, UserCircle, CreditCard, Clock } from 'lucide-vue-next'
import { useAssetStore, type Asset, type RepairLog } from '../stores/assetStore'
import { useUserStore } from '../stores/userStore'
import RepairModal from '../components/assets/RepairModal.vue'

const assetStore = useAssetStore()
const userStore = useUserStore()

const showRepairModal = ref(false)
const repairType = ref<'START' | 'COMPLETE'>('START')
const selectedAsset = ref<Asset | null>(null)
const searchQuery = ref('')

onMounted(async () => {
  await assetStore.fetchAssets()
  await assetStore.fetchRepairs()
  await userStore.fetchUsers() // Need users to resolve user email
})

// Join RepairLogs with Asset and User data
const richRepairs = computed(() => {
  let list = assetStore.repairs.map(repair => {
    const asset = assetStore.assets.find(a => a.id === repair.assetId)
    const user = userStore.users.find(u => u.id === repair.reportedBy)
    return {
      ...repair,
      assetName: asset ? asset.name : 'Unknown Asset',
      assetSerial: asset ? asset.serialNumber : 'Unknown',
      userEmail: user ? user.email : 'Unknown User',
    }
  })
  
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter(r => 
      r.assetName.toLowerCase().includes(q) || 
      r.assetSerial.toLowerCase().includes(q) || 
      r.reason.toLowerCase().includes(q)
    )
  }
  
  return list
})

const openStartModal = () => {
  selectedAsset.value = null // Let the modal show the dropdown
  repairType.value = 'START'
  showRepairModal.value = true
}

const openCompleteModal = (repair: RepairLog) => {
  const asset = assetStore.assets.find(a => a.id === repair.assetId)
  if (asset) {
    selectedAsset.value = asset
    repairType.value = 'COMPLETE'
    showRepairModal.value = true
  }
}

const formatDate = (val: any) => {
  if (!val) return '—'
  // Backend returns array [yyyy, mm, dd, hh, mm, ss] or string
  if (Array.isArray(val)) {
    const [y, m, d, hh, mm] = val
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(hh || 0).padStart(2,'0')}:${String(mm || 0).padStart(2,'0')}`
  }
  return String(val).replace('T', ' ').substring(0, 16)
}

const formatCurrency = (val: any) => {
  if (val === null || val === undefined) return '—'
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(val)
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between space-y-4 lg:space-y-0 gap-4">
      <div class="relative flex-1 max-w-md w-full">
        <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
          <Search class="w-5 h-5" />
        </span>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Search repairs by asset name, serial, or reason..."
          class="block w-full pl-10 pr-4 py-2 bg-white border border-gray-100 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all"
        />
      </div>

      <button
        @click="openStartModal"
        class="flex items-center justify-center space-x-2 px-6 py-2 bg-orange-500 text-white rounded-xl hover:bg-orange-600 transition-all shadow-lg shadow-orange-500/20 active:scale-95"
      >
        <Wrench class="w-5 h-5" />
        <span class="font-semibold">Request Repair</span>
      </button>
    </div>

    <!-- Repairs List -->
    <h3 class="text-lg font-bold text-gray-800 mb-4 flex items-center pt-2">
      <span class="w-8 h-8 rounded-lg bg-orange-100 text-orange-600 flex items-center justify-center mr-3">
        <Wrench class="w-4 h-4" />
      </span>
      Maintenance History
    </h3>
    
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-gray-50/50 border-b border-gray-100">
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Asset</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Status</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Requested By</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider w-1/4">Reason</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Cost (Est / Final)</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-500 uppercase tracking-wider">Date</th>
              <th class="px-6 py-4 text-xs font-semibold text-gray-400"></th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr v-if="richRepairs.length === 0">
               <td colspan="7" class="px-6 py-12 text-center text-gray-400">No repair records found.</td>
            </tr>
            <tr v-for="repair in richRepairs" :key="repair.id" class="hover:bg-gray-50/80 transition-all group">
              
              <!-- Asset -->
              <td class="px-6 py-4">
                <div class="flex items-center gap-3">
                  <div class="w-9 h-9 bg-gray-100 rounded-lg flex items-center justify-center text-gray-500 shrink-0">
                    <Package class="w-4.5 h-4.5" />
                  </div>
                  <div>
                    <span class="font-semibold text-gray-800 block text-sm leading-tight">{{ repair.assetName }}</span>
                    <span class="text-[11px] text-gray-400 font-mono">{{ repair.assetSerial }}</span>
                  </div>
                </div>
              </td>

               <!-- Status -->
               <td class="px-6 py-4">
                <span v-if="repair.status === 'IN_PROGRESS'" class="px-2.5 py-1 rounded-full text-[11px] font-bold tracking-wider uppercase border bg-orange-50 text-orange-700 border-orange-200 inline-flex items-center gap-1">
                  <Clock class="w-3 h-3" /> In Progress
                </span>
                <span v-else class="px-2.5 py-1 rounded-full text-[11px] font-bold tracking-wider uppercase border bg-green-50 text-green-700 border-green-200 inline-flex items-center gap-1">
                  <CheckCircle2 class="w-3 h-3" /> Completed
                </span>
              </td>

              <!-- Repoted By -->
              <td class="px-6 py-4">
                 <div class="flex items-center gap-2">
                  <UserCircle class="w-4 h-4 text-gray-400" />
                  <span class="text-xs font-medium text-gray-700">{{ repair.userEmail }}</span>
                </div>
              </td>

              <!-- Reason -->
              <td class="px-6 py-4 text-sm text-gray-600 line-clamp-2">
                {{ repair.reason }}
              </td>

              <!-- Cost -->
              <td class="px-6 py-4">
                 <div class="flex flex-col text-xs">
                    <span class="text-gray-500">Est: {{ formatCurrency(repair.estimatedCost) }}</span>
                    <span class="font-bold text-gray-800" v-if="repair.status === 'COMPLETED'">Fin: {{ formatCurrency(repair.finalCost) }}</span>
                 </div>
              </td>
              
               <!-- Date -->
               <td class="px-6 py-4 text-xs font-mono text-gray-500">
                <div class="flex flex-col gap-1">
                  <span class="text-orange-600" title="Start Date">S: {{ formatDate(repair.startDate) }}</span>
                  <span class="text-green-600" v-if="repair.status === 'COMPLETED'" title="End Date">E: {{ formatDate(repair.endDate) }}</span>
                </div>
              </td>

              <!-- Action -->
              <td class="px-6 py-4 text-right whitespace-nowrap">
                <button 
                  v-if="repair.status === 'IN_PROGRESS'"
                  @click="openCompleteModal(repair)" 
                  class="flex ml-auto items-center gap-1 px-3 py-1.5 bg-green-500 text-white hover:bg-green-600 text-[11px] font-bold rounded-lg shadow-sm transition-all active:scale-95"
                >
                  <CheckCircle2 class="w-3.5 h-3.5" />
                  <span>Complete</span>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal -->
    <RepairModal 
      v-if="showRepairModal" 
      :asset="selectedAsset" 
      :type="repairType" 
      @close="() => { showRepairModal = false; assetStore.fetchRepairs() }" 
    />
  </div>
</template>
