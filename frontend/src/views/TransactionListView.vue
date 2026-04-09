<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { 
  Activity, Clock, ArrowUpRight, ArrowDownLeft, Search, Filter, 
  Plus, Edit3, Trash2, Box, User, Briefcase, X 
} from 'lucide-vue-next'
import { useTransactionStore, type Transaction } from '../stores/transactionStore'

const transactionStore = useTransactionStore()
const selectedLog = ref<Transaction | null>(null)

onMounted(() => {
  transactionStore.fetchRecentTransactions(50)
})

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getEventTypeInfo = (type: string) => {
  switch (type) {
    case 'RENT':
    case 'OUT':
      return { label: 'Checked Out', color: 'blue', icon: ArrowUpRight }
    case 'RETURN':
    case 'IN':
      return { label: 'Returned', color: 'emerald', icon: ArrowDownLeft }
    case 'CREATE':
      return { label: 'Created', color: 'green', icon: Plus }
    case 'UPDATE':
      return { label: 'Modified', color: 'amber', icon: Edit3 }
    case 'DELETE':
      return { label: 'Deleted', color: 'rose', icon: Trash2 }
    default:
      return { label: type, color: 'gray', icon: Activity }
  }
}

const getStatusBadgeClass = (type: string) => {
  const { color } = getEventTypeInfo(type)
  const variants: Record<string, string> = {
    blue: 'bg-blue-50 text-blue-600 border-blue-100',
    emerald: 'bg-emerald-50 text-emerald-600 border-emerald-100',
    green: 'bg-green-50 text-green-600 border-green-100',
    amber: 'bg-amber-50 text-amber-600 border-amber-100',
    rose: 'bg-rose-50 text-rose-600 border-rose-100',
    gray: 'bg-gray-50 text-gray-600 border-gray-100'
  }
  return variants[color] || variants.gray
}
</script>

<template>
  <div class="max-w-6xl mx-auto space-y-6 relative">
    <!-- Header -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <h2 class="text-2xl font-bold text-gray-800 flex items-center">
        <Activity class="w-6 h-6 mr-3 text-blue-600" />
        Activity History
      </h2>
      <div class="flex items-center space-x-3">
        <div class="relative group">
          <Search class="w-4 h-4 absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-blue-500" />
          <input 
            type="text" 
            placeholder="Search assets or users..." 
            class="pl-11 pr-4 py-2 bg-white border border-gray-100 rounded-xl outline-none focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all font-medium text-sm w-64 shadow-sm"
          />
        </div>
        <button class="p-2 border border-gray-100 rounded-xl bg-white text-gray-500 hover:bg-gray-50 transition-colors shadow-sm">
          <Filter class="w-5 h-5" />
        </button>
      </div>
    </div>

    <!-- Table Container -->
    <div class="bg-white rounded-3xl border border-gray-100 shadow-xl shadow-blue-900/5 overflow-hidden">
      <div v-if="transactionStore.loading" class="p-12 text-center text-gray-400">
        <div class="animate-spin w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full mx-auto mb-4"></div>
        <p class="font-medium">Loading history...</p>
      </div>

      <div v-else-if="transactionStore.recentTransactions.length === 0" class="p-12 text-center">
        <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300">
          <Activity class="w-8 h-8" />
        </div>
        <p class="text-gray-500 font-bold mb-1">No activities logged yet</p>
        <p class="text-gray-400 text-sm">System events and asset transfers will appear here.</p>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-gray-50/50">
              <th class="px-6 py-4 text-xs font-bold text-gray-400 uppercase tracking-widest border-b border-gray-100">Event</th>
              <th class="px-6 py-4 text-xs font-bold text-gray-400 uppercase tracking-widest border-b border-gray-100">Subject</th>
              <th class="px-6 py-4 text-xs font-bold text-gray-400 uppercase tracking-widest border-b border-gray-100">Actor</th>
              <th class="px-6 py-4 text-xs font-bold text-gray-400 uppercase tracking-widest border-b border-gray-100">Timestamp</th>
              <th class="px-6 py-4 text-xs font-bold text-gray-400 uppercase tracking-widest border-b border-gray-100">Status</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
            <tr 
              v-for="tx in transactionStore.recentTransactions" 
              :key="tx.id"
              @click="selectedLog = tx"
              class="group hover:bg-blue-50/50 transition-colors cursor-pointer"
            >
              <td class="px-6 py-5">
                <div :class="[
                  'w-10 h-10 rounded-xl flex items-center justify-center transition-transform group-hover:scale-110 shadow-sm',
                  tx.type === 'RENT' || tx.type === 'OUT' ? 'bg-blue-100 text-blue-600' : 
                  tx.type === 'RETURN' || tx.type === 'IN' ? 'bg-emerald-100 text-emerald-600' :
                  tx.type === 'CREATE' ? 'bg-green-100 text-green-600' :
                  tx.type === 'UPDATE' ? 'bg-amber-100 text-amber-600' :
                  tx.type === 'DELETE' ? 'bg-rose-100 text-rose-600' : 'bg-gray-100 text-gray-600'
                ]">
                  <component :is="getEventTypeInfo(tx.type).icon" class="w-5 h-5" />
                </div>
              </td>
              <td class="px-6 py-5">
                <div v-if="tx.assetId" class="flex items-center">
                  <Box class="w-4 h-4 mr-2 text-gray-300" />
                  <div>
                    <p class="font-bold text-gray-800">{{ tx.assetName || 'Unknown Asset' }}</p>
                    <p class="text-xs text-gray-400 font-medium">#{{ String(tx.assetId).padStart(4, '0') }}</p>
                  </div>
                </div>
                <div v-else class="flex items-center text-gray-400 italic">
                  <Activity class="w-4 h-4 mr-2 opacity-30" />
                  <span class="text-sm">System / General</span>
                </div>
              </td>
              <td class="px-6 py-5">
                <div class="flex items-center">
                  <div class="w-8 h-8 rounded-full bg-slate-200 border-2 border-white flex items-center justify-center text-xs font-bold text-slate-500 mr-2 shadow-sm">
                    {{ tx.userEmail.charAt(0).toUpperCase() }}
                  </div>
                  <p class="text-sm font-semibold text-gray-700">{{ tx.userEmail }}</p>
                </div>
              </td>
              <td class="px-6 py-5">
                <div class="flex items-center text-gray-500">
                  <Clock class="w-4 h-4 mr-2 opacity-50" />
                  <span class="text-sm font-medium">{{ formatDate(tx.transactionDate) }}</span>
                </div>
              </td>
              <td class="px-6 py-5 whitespace-nowrap">
                <span 
                  :class="[
                    'px-3 py-1 rounded-full text-[10px] font-bold border uppercase tracking-wider',
                    getStatusBadgeClass(tx.type)
                  ]"
                >
                  {{ getEventTypeInfo(tx.type).label }}
                </span>
                <p v-if="tx.note" class="text-[10px] text-gray-400 mt-1 italic max-w-[200px] truncate" :title="tx.note">"{{ tx.note }}"</p>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Slide-over Detail Panel -->
    <div 
      v-if="selectedLog" 
      class="fixed inset-0 z-50 overflow-hidden pointer-events-none"
    >
      <!-- Backdrop -->
      <div 
        class="absolute inset-0 bg-gray-900/20 backdrop-blur-sm transition-opacity pointer-events-auto"
        @click="selectedLog = null"
      ></div>

      <div class="absolute inset-y-0 right-0 max-w-full flex">
        <div 
          class="w-screen max-w-md pointer-events-auto bg-white shadow-2xl transform transition-transform duration-500"
          :class="selectedLog ? 'translate-x-0' : 'translate-x-full'"
        >
          <div class="h-full flex flex-col">
            <!-- Header -->
            <div class="px-6 py-6 border-b border-gray-100 flex items-center justify-between bg-gray-50/50">
              <h3 class="text-xl font-bold text-gray-800 flex items-center">
                <Activity class="w-5 h-5 mr-3 text-blue-600" />
                Event Detail
              </h3>
              <button 
                @click="selectedLog = null"
                class="p-2 hover:bg-gray-200 rounded-full transition-colors"
              >
                <X class="w-5 h-5 text-gray-400" />
              </button>
            </div>

            <!-- Content -->
            <div class="flex-1 overflow-y-auto px-6 py-8 space-y-10">
              <!-- Event Status Section -->
              <div>
                <div class="flex items-center gap-4 mb-6">
                  <div :class="[
                    'w-16 h-16 rounded-2xl flex items-center justify-center shadow-lg',
                    selectedLog.type === 'RENT' || selectedLog.type === 'OUT' ? 'bg-blue-600 text-white' : 
                    selectedLog.type === 'RETURN' || selectedLog.type === 'IN' ? 'bg-emerald-600 text-white' :
                    selectedLog.type === 'CREATE' ? 'bg-green-600 text-white' :
                    selectedLog.type === 'UPDATE' ? 'bg-amber-600 text-white' :
                    selectedLog.type === 'DELETE' ? 'bg-rose-600 text-white' : 'bg-gray-600 text-white'
                  ]">
                    <component :is="getEventTypeInfo(selectedLog.type).icon" class="w-8 h-8" />
                  </div>
                  <div>
                    <h4 class="text-2xl font-black text-gray-800 tracking-tight">
                      {{ getEventTypeInfo(selectedLog.type).label }}
                    </h4>
                    <p class="text-gray-400 text-sm font-medium flex items-center mt-1">
                      <Clock class="w-4 h-4 mr-2" />
                      {{ formatDate(selectedLog.transactionDate) }}
                    </p>
                  </div>
                </div>
                <!-- Description/Note -->
                <div class="bg-gray-50 rounded-2xl p-6 border border-gray-100">
                  <p class="text-sm font-bold text-gray-400 uppercase tracking-widest mb-2">Detailed Log</p>
                  <p class="text-gray-700 leading-relaxed font-semibold italic">
                    "{{ selectedLog.note || 'No additional notes provided.' }}"
                  </p>
                </div>
              </div>

              <!-- Actor Section -->
              <div class="space-y-4">
                <p class="text-sm font-bold text-gray-400 uppercase tracking-widest">Perfomed By (Actor)</p>
                <div class="bg-white border border-gray-100 rounded-2xl p-5 shadow-sm space-y-4">
                  <div class="flex items-center">
                    <div class="w-10 h-10 rounded-full bg-blue-50 text-blue-600 flex items-center justify-center mr-4">
                      <User class="w-5 h-5" />
                    </div>
                    <div>
                      <p class="text-xs text-gray-400 font-bold mb-0.5">Email / Member ID</p>
                      <p class="text-gray-800 font-bold">{{ selectedLog.userEmail }}</p>
                    </div>
                  </div>
                  <div class="grid grid-cols-2 gap-4 pt-4 border-t border-gray-50">
                    <div>
                      <div class="flex items-center text-gray-400 text-xs font-bold mb-1 opacity-60">
                        <Briefcase class="w-3.5 h-3.5 mr-1.5" />
                        Role
                      </div>
                      <span class="px-2 py-0.5 bg-indigo-50 text-indigo-600 rounded-md text-[10px] font-black uppercase">
                        {{ selectedLog.userRole }}
                      </span>
                    </div>
                    <div>
                      <div class="flex items-center text-gray-400 text-xs font-bold mb-1 opacity-60">
                        <Box class="w-3.5 h-3.5 mr-1.5" />
                        Department
                      </div>
                      <span class="text-gray-700 font-bold text-xs">{{ selectedLog.userDepartment || 'N/A' }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Subject Section -->
              <div class="space-y-4">
                <p class="text-sm font-bold text-gray-400 uppercase tracking-widest">Affected Subject</p>
                <div v-if="selectedLog.assetId" class="bg-white border border-gray-100 rounded-2xl p-5 shadow-sm">
                  <div class="flex items-center mb-4">
                    <div class="w-10 h-10 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center mr-4">
                      <Box class="w-5 h-5" />
                    </div>
                    <div>
                      <p class="text-xs text-gray-400 font-bold mb-0.5">Asset Name</p>
                      <p class="text-gray-800 font-bold">{{ selectedLog.assetName }}</p>
                    </div>
                  </div>
                  <div class="flex items-center justify-between pt-4 border-t border-gray-50">
                    <p class="text-xs font-bold text-gray-400 uppercase">Serial Number</p>
                    <p class="text-blue-600 font-mono font-bold text-sm tracking-tighter bg-blue-50 px-2 py-0.5 rounded italic">
                      {{ selectedLog.assetSerial || 'Unknown' }}
                    </p>
                  </div>
                </div>
                <div v-else class="text-center py-8 bg-gray-50 rounded-2xl border border-dashed border-gray-200">
                  <Activity class="w-8 h-8 text-gray-300 mx-auto mb-2 opacity-50" />
                  <p class="text-sm font-bold text-gray-400">General System Activity</p>
                  <p class="text-[10px] text-gray-400">This log is related to category or system settings.</p>
                </div>
              </div>
            </div>

            <!-- Footer -->
            <div class="px-6 py-6 border-t border-gray-100 bg-gray-50/50">
              <button 
                @click="selectedLog = null"
                class="w-full py-4 bg-gray-800 text-white rounded-2xl font-bold hover:bg-gray-900 transition-all shadow-lg active:scale-95"
              >
                Close Detail
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
