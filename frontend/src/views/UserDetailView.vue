<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  ArrowLeft, Mail, Building, Shield, Calendar, 
  Package, History, CheckCircle2, Clock, ChevronRight, X, Save
} from 'lucide-vue-next'
import { useUserStore } from '../stores/userStore'
import api from '../api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const userId = Number(route.params.id)

const userAssets = ref<any[]>([])
const userHistory = ref<any[]>([])
const loading = ref(true)

// Edit Modal State
const isEditModalOpen = ref(false)
const editForm = reactive({
  name: '',
  email: '',
  department: '',
  role: '',
  status: ''
})

onMounted(async () => {
  await loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    await userStore.fetchUserById(userId)
    
    // Fetch user's rented assets
    const assetsRes = await api.get(`/assets/user/${userId}`)
    userAssets.value = assetsRes.data
    
    // Fetch user's transaction history
    const historyRes = await api.get(`/transactions/user/${userId}`)
    userHistory.value = historyRes.data
  } catch (error) {
    console.error('Failed to load user details', error)
  } finally {
    loading.value = false
  }
}

const openEditModal = () => {
  if (userStore.selectedUser) {
    editForm.name = userStore.selectedUser.name
    editForm.email = userStore.selectedUser.email
    editForm.department = userStore.selectedUser.department || ''
    editForm.role = userStore.selectedUser.role
    editForm.status = userStore.selectedUser.status || 'APPROVED'
    isEditModalOpen.value = true
  }
}

const handleUpdateUser = async () => {
  try {
    await userStore.updateUser(userId, { ...editForm })
    isEditModalOpen.value = false
    await loadData()
  } catch (error) {
    alert('Failed to update user')
  }
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const getStatusColor = (status: string) => {
  switch (status?.toUpperCase()) {
    case 'APPROVED': return 'text-emerald-600 bg-emerald-50'
    case 'PENDING': return 'text-amber-600 bg-amber-50'
    case 'REJECTED': return 'text-rose-600 bg-rose-50'
    default: return 'text-gray-500 bg-gray-50'
  }
}
</script>

<template>
  <div v-if="userStore.selectedUser" class="max-w-6xl mx-auto space-y-8 animate-in fade-in duration-500">
    <!-- Header -->
    <div class="bg-white p-8 rounded-3xl border border-gray-100 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-6">
      <div class="flex items-center space-x-6">
        <div class="w-20 h-20 rounded-2xl bg-slate-50 flex items-center justify-center text-3xl font-bold text-slate-300 border border-slate-100">
          {{ userStore.selectedUser.name?.charAt(0) || 'U' }}
        </div>
        <div class="space-y-1">
          <div class="flex items-center gap-3">
            <h1 class="text-2xl font-bold text-slate-800">{{ userStore.selectedUser.name || 'Member' }}</h1>
            <span :class="['px-2.5 py-0.5 rounded-lg text-[10px] font-bold uppercase tracking-wider', getStatusColor(userStore.selectedUser.status)]">
              {{ userStore.selectedUser.status || 'ACTIVE' }}
            </span>
          </div>
          <div class="flex items-center text-sm text-slate-400 font-medium">
            <Mail class="w-3.5 h-3.5 mr-2" />
            {{ userStore.selectedUser.email }}
          </div>
        </div>
      </div>
      
      <div class="flex items-center gap-3">
        <button @click="router.back()" class="px-5 py-2.5 bg-gray-50 text-gray-500 rounded-xl font-bold text-xs uppercase tracking-widest hover:bg-gray-100 transition-all">
          Directory
        </button>
        <button @click="openEditModal" class="px-5 py-2.5 bg-slate-900 text-white rounded-xl font-bold text-xs uppercase tracking-widest hover:bg-slate-800 transition-all shadow-lg shadow-slate-200">
          Edit Profile
        </button>
      </div>
    </div>

    <!-- Content Grid -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- Info Sidebar -->
      <div class="lg:col-span-1 space-y-6">
        <div class="bg-white p-6 rounded-3xl border border-gray-100 shadow-sm space-y-4">
          <h3 class="text-xs font-black text-slate-300 uppercase tracking-widest px-2">Member Info</h3>
          <div class="space-y-1">
            <div class="flex items-center p-3 rounded-2xl bg-slate-50 text-slate-600">
              <Building class="w-4 h-4 mr-3 text-slate-400" />
              <div class="text-xs font-bold">{{ userStore.selectedUser.department || 'Not Assigned' }}</div>
            </div>
            <div class="flex items-center p-3 rounded-2xl bg-slate-50 text-slate-600">
              <Shield class="w-4 h-4 mr-3 text-slate-400" />
              <div class="text-xs font-bold uppercase">{{ userStore.selectedUser.role }}</div>
            </div>
            <div class="flex items-center p-3 rounded-2xl bg-slate-50 text-slate-600">
              <Calendar class="w-4 h-4 mr-3 text-slate-400" />
              <div class="text-xs font-mono font-bold">USR-{{ String(userStore.selectedUser.id).padStart(4, '0') }}</div>
            </div>
          </div>
        </div>

        <div class="bg-blue-600 p-8 rounded-3xl text-white shadow-xl shadow-blue-100 relative overflow-hidden">
          <Package class="absolute -right-4 -bottom-4 w-24 h-24 opacity-10" />
          <div class="relative z-10">
            <div class="text-4xl font-black mb-1">{{ userAssets.length }}</div>
            <div class="text-xs font-bold uppercase tracking-widest opacity-80">Currently Rented</div>
          </div>
        </div>
      </div>

      <!-- Main Lists -->
      <div class="lg:col-span-2 space-y-8">
        <!-- Assets List -->
        <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
          <div class="px-8 py-5 border-b border-gray-50 bg-gray-50/50 flex items-center justify-between">
            <h3 class="text-xs font-black text-slate-400 uppercase tracking-[0.2em]">Currently Rented Assets</h3>
            <span class="px-3 py-1 bg-blue-600 text-white rounded-lg text-[10px] font-black">{{ userAssets.length }}</span>
          </div>
          <div class="p-6">
            <div v-if="userAssets.length === 0" class="py-16 text-center">
              <Package class="w-12 h-12 mx-auto mb-3 text-slate-100" />
              <p class="text-xs font-bold text-slate-300 uppercase tracking-widest">No assets checked out</p>
            </div>
            <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div 
                v-for="asset in userAssets" 
                :key="asset.id"
                @click="router.push(`/assets/${asset.id}`)"
                class="flex items-center p-4 bg-slate-50 rounded-2xl hover:bg-white hover:shadow-xl hover:ring-1 hover:ring-blue-50 transition-all cursor-pointer group"
              >
                <div class="w-12 h-12 rounded-xl bg-white flex items-center justify-center mr-4 shadow-sm group-hover:scale-110 transition-transform">
                  <Package class="w-6 h-6 text-blue-500" />
                </div>
                <div class="flex-1">
                  <div class="text-[13px] font-bold text-slate-800 mb-0.5">{{ asset.name }}</div>
                  <div class="text-[10px] font-mono text-slate-400 font-medium">{{ asset.serialNumber }}</div>
                </div>
                <ChevronRight class="w-4 h-4 text-slate-300 group-hover:text-blue-500 transition-all" />
              </div>
            </div>
          </div>
        </div>

        <!-- Activity History -->
        <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
          <div class="px-8 py-5 border-b border-gray-50 bg-gray-50/50">
            <h3 class="text-sm font-bold text-slate-800 uppercase tracking-wider">Recent Activity</h3>
          </div>
          <div class="divide-y divide-gray-50">
            <div v-if="userHistory.length === 0" class="py-12 text-center opacity-30">
              <p class="text-xs font-bold">No records found</p>
            </div>
            <div 
              v-for="tx in userHistory" 
              :key="tx.id"
              class="px-8 py-4 flex items-center justify-between group hover:bg-gray-50/50 transition-colors"
            >
              <div class="flex items-center space-x-4">
                <div :class="['w-8 h-8 rounded-lg flex items-center justify-center', tx.type === 'RENT' ? 'bg-blue-50 text-blue-600' : 'bg-emerald-50 text-emerald-600']">
                  <Clock v-if="tx.type === 'RENT'" class="w-3.5 h-3.5" />
                  <CheckCircle2 v-else class="w-3.5 h-3.5" />
                </div>
                <div>
                  <div class="text-xs font-bold text-slate-800">{{ tx.type === 'RENT' ? 'Borrowed' : 'Returned' }} {{ tx.assetName }}</div>
                  <div class="text-[10px] text-slate-400 font-medium">{{ formatDate(tx.transactionDate) }}</div>
                </div>
              </div>
              <span class="text-[10px] font-bold text-slate-300 font-mono">{{ tx.assetSerial }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit Modal -->
    <div v-if="isEditModalOpen" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4 animate-in fade-in duration-300">
      <div class="bg-white w-full max-w-md rounded-[2.5rem] shadow-2xl overflow-hidden animate-in zoom-in-95 duration-300">
        <div class="px-8 py-6 bg-slate-50 border-b border-gray-100 flex items-center justify-between">
          <h3 class="text-lg font-bold text-slate-800">Edit Profile</h3>
          <button @click="isEditModalOpen = false" class="p-2 hover:bg-gray-200 rounded-xl transition-colors">
            <X class="w-5 h-5 text-gray-500" />
          </button>
        </div>
        
        <form @submit.prevent="handleUpdateUser" class="p-8 space-y-5">
          <div class="space-y-1.5">
            <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Full Name</label>
            <input v-model="editForm.name" type="text" required class="w-full px-5 py-3.5 bg-slate-50 border-none rounded-2xl focus:ring-2 focus:ring-blue-500 transition-all font-bold text-slate-700" />
          </div>
          
          <div class="space-y-1.5">
            <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Email Address</label>
            <input v-model="editForm.email" type="email" required class="w-full px-5 py-3.5 bg-slate-50 border-none rounded-2xl focus:ring-2 focus:ring-blue-500 transition-all font-bold text-slate-700" />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-1.5">
              <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Department</label>
              <input v-model="editForm.department" type="text" class="w-full px-5 py-3.5 bg-slate-50 border-none rounded-2xl focus:ring-2 focus:ring-blue-500 transition-all font-bold text-slate-700" />
            </div>
            <div class="space-y-1.5">
              <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Role</label>
              <select v-model="editForm.role" class="w-full px-5 py-3.5 bg-slate-50 border-none rounded-2xl focus:ring-2 focus:ring-blue-500 transition-all font-bold text-slate-700 appearance-none">
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </div>
          </div>

          <div class="space-y-1.5 pt-2">
            <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Account Status</label>
            <div class="flex gap-2">
              <button 
                type="button"
                v-for="status in ['APPROVED', 'PENDING', 'REJECTED']"
                :key="status"
                @click="editForm.status = status"
                :class="['flex-1 py-2.5 rounded-xl text-[10px] font-bold transition-all border-2', 
                  editForm.status === status ? 'bg-slate-900 border-slate-900 text-white shadow-lg' : 'bg-white border-gray-100 text-gray-400 hover:border-gray-200']"
              >
                {{ status }}
              </button>
            </div>
          </div>

          <div class="pt-6">
            <button type="submit" class="w-full py-4 bg-blue-600 text-white rounded-2xl font-black text-xs uppercase tracking-[0.2em] shadow-xl shadow-blue-100 hover:bg-blue-700 transition-all flex items-center justify-center gap-2">
              <Save class="w-4 h-4" />
              Save Changes
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <!-- Loading -->
  <div v-else-if="loading" class="py-20 text-center">
    <div class="w-10 h-10 border-4 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
    <p class="text-xs font-bold text-slate-400 uppercase tracking-widest">Loading Member Data...</p>
  </div>
</template>
