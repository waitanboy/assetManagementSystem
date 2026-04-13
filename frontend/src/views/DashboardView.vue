<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { 
  Package, CheckCircle, AlertCircle, Wrench, ArrowUpRight, ArrowDownLeft, 
  Clock, Plus, Pencil, Trash2, RotateCcw, AlertTriangle, UserCircle, Mail, ExternalLink 
} from 'lucide-vue-next'
import { useAssetStore, type Asset } from '../stores/assetStore'
import { useTransactionStore } from '../stores/transactionStore'
import { useAuthStore } from '../stores/authStore'
import api from '../api'
import { ref } from 'vue'

const assetStore = useAssetStore()
const transactionStore = useTransactionStore()
const authStore = useAuthStore()

onMounted(async () => {
  assetStore.fetchStats()
  assetStore.fetchAssets() // Need full list for overdue calculation
  transactionStore.fetchRecentTransactions(15)
})

const emailSending = ref(false)
const emailStatus = ref<{ type: 'success' | 'error', message: string } | null>(null)

const sendTestEmail = async () => {
  emailSending.value = true
  emailStatus.value = null
  try {
    const res = await api.post('/users/test-email')
    emailStatus.value = { type: 'success', message: res.data }
  } catch (err: any) {
    emailStatus.value = { type: 'error', message: err.response?.data || 'Failed to send test email' }
  } finally {
    emailSending.value = false
    // Hide status after 5 seconds
    setTimeout(() => { emailStatus.value = null }, 5000)
  }
}

const today = new Date().toISOString().split('T')[0]

const overdueAssets = computed(() => {
  return assetStore.assets.filter(asset => {
    if (asset.status !== 'RENTED' || !asset.dueDate) return false
    return asset.dueDate < today
  })
})

const formatDate = (dateStr: string) => {
  if (!dateStr) return '—'
  const date = new Date(dateStr)
  return date.toLocaleString('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const parseDateSafe = (val: any): string => {
  if (!val) return ''
  if (Array.isArray(val)) {
    const [y, m, d] = val
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
  }
  return String(val).split('T')[0]
}

const getTypeLabel = (type: string) => {
  switch (type) {
    case 'RENT':    return '대여'
    case 'RETURN':  return '반납'
    case 'CREATE':  return '등록'
    case 'UPDATE':  return '수정'
    case 'DELETE':  return '삭제'
    default:        return type
  }
}

const getTypeColor = (type: string) => {
  switch (type) {
    case 'RENT':   return 'bg-blue-50 text-blue-600'
    case 'RETURN': return 'bg-green-50 text-green-600'
    case 'CREATE': return 'bg-purple-50 text-purple-600'
    case 'UPDATE': return 'bg-yellow-50 text-yellow-600'
    case 'DELETE': return 'bg-red-50 text-red-600'
    default:       return 'bg-gray-50 text-gray-600'
  }
}
</script>

<template>
  <div class="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
    <!-- Header Summary Section -->
    <div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
      <div class="group bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex items-center space-x-4 transition-all hover:shadow-md hover:-translate-y-1">
        <div class="p-4 bg-blue-50 rounded-xl text-blue-600 group-hover:bg-blue-600 group-hover:text-white transition-colors">
          <Package class="w-6 h-6" />
        </div>
        <div>
          <p class="text-xs text-gray-400 font-bold uppercase tracking-wider">Total Assets</p>
          <div class="flex items-baseline space-x-1">
            <p class="text-2xl font-black text-gray-800">{{ assetStore.stats.total }}</p>
            <span class="text-[10px] text-gray-400 font-medium">Items</span>
          </div>
        </div>
      </div>
      
      <div class="group bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex items-center space-x-4 transition-all hover:shadow-md hover:-translate-y-1">
        <div class="p-4 bg-green-50 rounded-xl text-green-600 group-hover:bg-green-600 group-hover:text-white transition-colors">
          <CheckCircle class="w-6 h-6" />
        </div>
        <div>
          <p class="text-xs text-gray-400 font-bold uppercase tracking-wider">Available</p>
          <div class="flex items-baseline space-x-1">
            <p class="text-2xl font-black text-green-600">{{ assetStore.stats.available }}</p>
            <span class="text-[10px] text-gray-400 font-medium font-bold">In Stock</span>
          </div>
        </div>
      </div>

      <div class="group bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex items-center space-x-4 transition-all hover:shadow-md hover:-translate-y-1">
        <div class="p-4 bg-orange-50 rounded-xl text-orange-600 group-hover:bg-orange-600 group-hover:text-white transition-colors">
          <AlertCircle class="w-6 h-6" />
        </div>
        <div>
          <p class="text-xs text-gray-400 font-bold uppercase tracking-wider">Rented</p>
          <div class="flex items-baseline space-x-1">
            <p class="text-2xl font-black text-orange-600">{{ assetStore.stats.rented }}</p>
            <span class="text-[10px] text-gray-400 font-medium font-bold">Out</span>
          </div>
        </div>
      </div>

      <div class="group bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex items-center space-x-4 transition-all hover:shadow-md hover:-translate-y-1">
        <div class="p-4 bg-red-50 rounded-xl text-red-600 group-hover:bg-red-600 group-hover:text-white transition-colors">
          <Wrench class="w-6 h-6" />
        </div>
        <div>
          <p class="text-xs text-gray-400 font-bold uppercase tracking-wider">Repairing</p>
          <div class="flex items-baseline space-x-1">
            <p class="text-2xl font-black text-red-600">{{ assetStore.stats.repairing }}</p>
            <span class="text-[10px] text-gray-400 font-medium font-bold">Maint.</span>
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- Left: Overdue Assets List (Admins only) -->
      <div v-if="authStore.isAdmin" class="lg:col-span-1 space-y-4">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-bold text-gray-800 flex items-center gap-2">
            <AlertTriangle class="w-5 h-5 text-red-500" />
            Overdue Assets
            <span class="bg-red-500 text-white text-[10px] px-1.5 py-0.5 rounded-full ml-1" v-if="overdueAssets.length > 0">
              {{ overdueAssets.length }}
            </span>
          </h3>
          <router-link to="/assets" class="text-xs font-semibold text-blue-600 hover:underline flex items-center gap-1">
            View All <ExternalLink class="w-3 h-3" />
          </router-link>
        </div>

        <div class="bg-red-50/30 rounded-2xl border border-red-100 p-2 max-h-[600px] overflow-y-auto no-scrollbar">
          <div v-if="overdueAssets.length === 0" class="p-8 text-center bg-white/50 rounded-xl border border-dashed border-red-200">
            <Package class="w-10 h-10 mx-auto text-red-200 mb-2" />
            <p class="text-sm text-red-400 font-medium">No overdue assets</p>
          </div>
          <div v-else class="space-y-2">
            <div v-for="asset in overdueAssets" :key="asset.id" class="p-4 bg-white rounded-xl shadow-sm border border-red-100 hover:border-red-300 transition-colors">
              <div class="flex justify-between items-start mb-2">
                <div>
                   <p class="font-bold text-gray-800 text-sm leading-tight">{{ asset.name }}</p>
                   <p class="text-[10px] font-mono text-gray-400 mt-0.5">{{ asset.serialNumber }}</p>
                </div>
                <span class="text-[10px] font-black text-red-600 bg-red-50 px-2 py-0.5 rounded uppercase border border-red-100">
                   DELAYED
                </span>
              </div>
              <div class="flex items-center gap-2 mt-3 pt-3 border-t border-gray-50">
                <div class="w-7 h-7 rounded-full bg-slate-100 flex items-center justify-center">
                   <UserCircle class="w-4 h-4 text-slate-400" />
                </div>
                <div class="flex-1 min-w-0">
                   <p class="text-[11px] font-bold text-gray-700 truncate">{{ asset.rentedByEmail }}</p>
                   <p class="text-[9px] text-gray-400 flex items-center gap-1">
                      <Clock class="w-2.5 h-2.5" /> Due: <span class="text-red-500 font-bold">{{ parseDateSafe(asset.dueDate) }}</span>
                   </p>
                </div>
                <button class="p-1.5 text-blue-500 hover:bg-blue-50 rounded-lg transition-colors" title="Contact User">
                   <Mail class="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- System Diagnostics (Admins only) -->
        <div class="bg-indigo-50/30 rounded-2xl border border-indigo-100 p-6 space-y-4">
          <div>
            <h4 class="text-sm font-black text-indigo-900 flex items-center gap-2">
              <Mail class="w-4 h-4" /> System Utilities
            </h4>
            <p class="text-[10px] text-indigo-400 mt-1 uppercase font-bold tracking-tight">Admin Diagnostics</p>
          </div>
          
          <button 
            @click="sendTestEmail"
            :disabled="emailSending"
            class="w-full flex items-center justify-center gap-2 px-4 py-3 bg-white border border-indigo-200 rounded-xl text-xs font-bold text-indigo-600 hover:bg-indigo-600 hover:text-white disabled:opacity-50 disabled:hover:bg-white disabled:hover:text-indigo-600 transition-all shadow-sm active:scale-95"
          >
            <RotateCcw v-if="emailSending" class="w-4 h-4 animate-spin" />
            <Mail v-else class="w-4 h-4" />
            {{ emailSending ? 'Sending...' : 'Send Test Email' }}
          </button>

          <transition enter-active-class="transition duration-300" enter-from-class="opacity-0 -translate-y-2" leave-active-class="transition duration-200" leave-to-class="opacity-0 translate-y-1">
            <div v-if="emailStatus" :class="[
              'p-3 rounded-lg text-[10px] font-bold flex items-center gap-2',
              emailStatus.type === 'success' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
            ]">
              <CheckCircle v-if="emailStatus.type === 'success'" class="w-3 h-3" />
              <AlertTriangle v-else class="w-3 h-3" />
              {{ emailStatus.message }}
            </div>
          </transition>
        </div>
      </div>

      <!-- Right: Recent Activity -->
      <div :class="[authStore.isAdmin ? 'lg:col-span-2' : 'lg:col-span-3', 'space-y-4']">
        <h3 class="text-lg font-bold text-gray-800">Recent System Activity</h3>
        
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden min-h-[500px]">
          <div v-if="transactionStore.loading" class="flex flex-col items-center justify-center h-[500px] text-gray-400">
            <div class="w-10 h-10 border-4 border-blue-100 border-t-blue-500 rounded-full animate-spin mb-4"></div>
            <p class="text-sm font-medium">Crunching transaction history...</p>
          </div>
          
          <div v-else-if="transactionStore.recentTransactions.length === 0" class="flex flex-col items-center justify-center h-[500px] text-gray-400">
            <RotateCcw class="w-12 h-12 mb-4 opacity-20" />
            <p class="text-sm">Found no traces of history</p>
          </div>
          
          <div v-else class="divide-y divide-gray-50">
            <div v-for="tx in transactionStore.recentTransactions" :key="tx.id" class="p-5 hover:bg-gray-50/50 transition-colors flex items-start space-x-4">
              <div :class="['p-3 rounded-xl shrink-0 shadow-sm transition-transform hover:scale-110', getTypeColor(tx.type)]">
                <ArrowUpRight  v-if="tx.type === 'RENT'"   class="w-5 h-5" />
                <ArrowDownLeft v-else-if="tx.type === 'RETURN'" class="w-5 h-5" />
                <Plus          v-else-if="tx.type === 'CREATE'" class="w-5 h-5" />
                <Pencil        v-else-if="tx.type === 'UPDATE'" class="w-5 h-5" />
                <Trash2        v-else-if="tx.type === 'DELETE'" class="w-5 h-5" />
                <RotateCcw     v-else class="w-5 h-5" />
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between mb-1">
                  <p class="font-extrabold text-gray-900 text-sm truncate pr-4">
                    {{ tx.assetName ?? 'System Maintenance' }}
                  </p>
                  <div class="flex items-center text-[10px] text-gray-400 font-mono italic">
                    <Clock class="w-3 h-3 mr-1" />
                    {{ formatDate(tx.transactionDate) }}
                  </div>
                </div>
                <div class="flex items-center text-xs text-gray-600">
                  <span class="font-bold text-blue-600 bg-blue-50 px-1.5 py-0.5 rounded-md mr-2">{{ tx.userEmail.split('@')[0] }}</span>
                  <span>님이 자산을 <span class="font-bold text-gray-800">{{ getTypeLabel(tx.type) }}</span>했습니다.</span>
                </div>
                <div v-if="tx.note" class="mt-2 text-[11px] text-gray-500 bg-gray-50 p-3 rounded-lg border-l-2 border-gray-200 italic leading-relaxed">
                  "{{ tx.note }}"
                </div>
              </div>
            </div>
          </div>
          
          <div class="p-4 bg-gray-50 text-center">
            <router-link to="/transactions" class="text-xs font-bold text-gray-500 hover:text-blue-600 transition-colors uppercase tracking-widest">
              View All Logged History
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>

