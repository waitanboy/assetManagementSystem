<script setup lang="ts">
import { onMounted } from 'vue'
import { Users, Mail, Shield, Building, UserCircle, CheckCircle2, XCircle } from 'lucide-vue-next'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()

onMounted(() => {
  userStore.fetchUsers()
})

const getRoleBadgeClass = (role: string) => {
  switch (role.toUpperCase()) {
    case 'ADMIN':
      return 'bg-purple-50 text-purple-600 border-purple-100'
    case 'MANAGER':
      return 'bg-blue-50 text-blue-600 border-blue-100'
    default:
      return 'bg-gray-50 text-gray-600 border-gray-100'
  }
}

const handleApprove = async (id: number) => {
  try {
    await userStore.approveUser(id)
  } catch (error) {
    console.error('Failed to approve user', error)
  }
}

const handleReject = async (id: number) => {
  try {
    await userStore.rejectUser(id)
  } catch (error) {
    console.error('Failed to reject user', error)
  }
}
</script>

<template>
  <div class="max-w-6xl mx-auto space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <h2 class="text-2xl font-bold text-gray-800 flex items-center">
        <Users class="w-6 h-6 mr-3 text-blue-600" />
        User Management
      </h2>
      <div class="text-sm text-gray-500 font-medium">
        Total <span class="text-gray-900 font-bold font-mono">{{ userStore.users.length }}</span> users registered
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="userStore.loading" class="bg-white rounded-2xl border border-gray-100 p-12 text-center shadow-sm">
      <div class="animate-pulse flex flex-col items-center">
        <div class="w-12 h-12 bg-gray-100 rounded-full mb-4"></div>
        <div class="h-4 w-48 bg-gray-100 rounded mb-2"></div>
        <div class="h-3 w-32 bg-gray-50 rounded"></div>
      </div>
      <p class="mt-4 text-gray-400 font-medium italic">Syncing user directory...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="userStore.error" class="bg-red-50 border border-red-100 rounded-2xl p-6 text-red-600 flex items-center">
      <Shield class="w-5 h-5 mr-3" />
      <p class="font-medium">{{ userStore.error }}</p>
    </div>

    <!-- User Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div 
        v-for="user in userStore.users" 
        :key="user.id"
        class="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm hover:shadow-xl hover:border-blue-100 transition-all duration-300 group"
      >
        <div class="flex items-start justify-between mb-4">
          <div class="w-12 h-12 bg-gray-50 rounded-xl flex items-center justify-center text-gray-400 group-hover:bg-blue-50 group-hover:text-blue-600 transition-colors">
            <UserCircle class="w-7 h-7" />
          </div>
          <span 
            :class="[
              'px-3 py-1 rounded-full text-xs font-bold border uppercase tracking-wider',
              getRoleBadgeClass(user.role)
            ]"
          >
            {{ user.role }}
          </span>
        </div>

        <div class="space-y-3">
          <div class="mb-4">
            <h3 class="text-lg font-black text-slate-800">{{ user.name || user.email.split('@')[0] }}</h3>
            <p v-if="!user.name" class="text-[9px] text-rose-400 font-bold uppercase tracking-tighter">* Profile name not set</p>
          </div>

          <div>
            <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-1">Email Address</p>
            <div class="flex items-center text-gray-700">
              <Mail class="w-4 h-4 mr-2 text-gray-400" />
              <p class="font-bold truncate">{{ user.email }}</p>
            </div>
          </div>

          <div>
            <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-1">Department</p>
            <div class="flex items-center text-gray-700">
              <Building class="w-4 h-4 mr-2 text-gray-400" />
              <p class="font-medium">{{ user.department || 'Not Assigned' }}</p>
            </div>
          </div>
        </div>

        <!-- Progress/Activity Indicator (Mock) -->
        <div class="mt-6 pt-6 border-t border-gray-50 flex items-center justify-between text-xs">
          <div class="flex items-center space-x-2">
            <span v-if="user.status === 'PENDING'" class="px-2 py-1 bg-yellow-50 text-yellow-600 border border-yellow-100 rounded text-[10px] font-bold uppercase tracking-wider">
              Pending
            </span>
            <span v-else-if="user.status === 'REJECTED'" class="px-2 py-1 bg-red-50 text-red-600 border border-red-100 rounded text-[10px] font-bold uppercase tracking-wider">
              Rejected
            </span>
            <span v-else class="px-2 py-1 bg-green-50 text-green-600 border border-green-100 rounded text-[10px] font-bold uppercase tracking-wider">
              Approved
            </span>
          </div>
          <div v-if="user.status === 'PENDING'" class="flex space-x-2">
            <button 
              @click="handleApprove(user.id)"
              class="flex items-center space-x-1 px-3 py-1.5 bg-blue-50 text-blue-600 hover:bg-blue-600 hover:text-white rounded-lg transition-colors font-medium border border-blue-100 hover:border-blue-600"
            >
              <CheckCircle2 class="w-3.5 h-3.5" />
              <span>Approve</span>
            </button>
            <button 
              @click="handleReject(user.id)"
              class="flex items-center space-x-1 px-3 py-1.5 bg-red-50 text-red-600 hover:bg-red-600 hover:text-white rounded-lg transition-colors font-medium border border-red-100 hover:border-red-600"
            >
              <XCircle class="w-3.5 h-3.5" />
              <span>Reject</span>
            </button>
          </div>
          <span v-else class="text-gray-400 font-medium italic">Employee ID: <span class="font-mono text-gray-600">USR-{{ String(user.id).padStart(4, '0') }}</span></span>
        </div>
      </div>
    </div>
  </div>
</template>
