<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Users, Search, Mail, Building, Shield, ChevronRight, CheckCircle2, XCircle, MoreVertical } from 'lucide-vue-next'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()
const router = useRouter()
const searchQuery = ref('')

onMounted(() => {
  userStore.fetchUsers()
})

const filteredUsers = computed(() => {
  if (!searchQuery.value) return userStore.users
  const query = searchQuery.value.toLowerCase()
  return userStore.users.filter(u => 
    u.name?.toLowerCase().includes(query) || 
    u.email.toLowerCase().includes(query) ||
    u.department?.toLowerCase().includes(query)
  )
})

const getRoleBadgeClass = (role: string) => {
  switch (role.toUpperCase()) {
    case 'ADMIN': return 'bg-purple-100 text-purple-700'
    case 'MANAGER': return 'bg-blue-100 text-blue-700'
    default: return 'bg-gray-100 text-gray-700'
  }
}

const getStatusBadgeClass = (status: string) => {
  switch (status?.toUpperCase()) {
    case 'APPROVED': return 'bg-emerald-100 text-emerald-700'
    case 'REJECTED': return 'bg-rose-100 text-rose-700'
    case 'PENDING': return 'bg-amber-100 text-amber-700'
    default: return 'bg-gray-100 text-gray-700'
  }
}

const handleUserClick = (id: number) => {
  router.push(`/users/${id}`)
}

const handleApprove = async (e: Event, id: number) => {
  e.stopPropagation()
  if (confirm('사용자 가입을 승인하시겠습니까?')) {
    await userStore.approveUser(id)
  }
}

const handleReject = async (e: Event, id: number) => {
  e.stopPropagation()
  if (confirm('사용자 가입을 거절하시겠습니까?')) {
    await userStore.rejectUser(id)
  }
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-white p-6 rounded-3xl border border-gray-100 shadow-sm">
      <div class="flex items-center space-x-4">
        <div class="p-3 bg-blue-600 rounded-2xl shadow-lg shadow-blue-200">
          <Users class="w-6 h-6 text-white" />
        </div>
        <div>
          <h2 class="text-xl font-bold text-gray-800 tracking-tight">User Directory</h2>
          <p class="text-sm text-gray-400 font-medium">Manage organization members and permissions</p>
        </div>
      </div>
      
      <div class="relative max-w-sm w-full">
        <Search class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
        <input 
          v-model="searchQuery"
          type="text" 
          placeholder="Search name, email, department..." 
          class="w-full pl-11 pr-4 py-2.5 bg-gray-50 border-none rounded-2xl text-sm focus:ring-2 focus:ring-blue-500/20 focus:bg-white transition-all outline-none"
        />
      </div>
    </div>

    <!-- User Table -->
    <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
      <table class="w-full text-left border-collapse">
        <thead>
          <tr class="bg-gray-50/50 border-b border-gray-50">
            <th class="px-6 py-4 text-[10px] font-bold text-gray-400 uppercase tracking-widest">Member Info</th>
            <th class="px-6 py-4 text-[10px] font-bold text-gray-400 uppercase tracking-widest">Department</th>
            <th class="px-6 py-4 text-[10px] font-bold text-gray-400 uppercase tracking-widest">Role</th>
            <th class="px-6 py-4 text-[10px] font-bold text-gray-400 uppercase tracking-widest">Status</th>
            <th class="px-6 py-4 text-right text-[10px] font-bold text-gray-400 uppercase tracking-widest">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <tr 
            v-for="user in filteredUsers" 
            :key="user.id"
            @click="handleUserClick(user.id)"
            class="hover:bg-blue-50/30 transition-colors cursor-pointer group"
          >
            <td class="px-6 py-4">
              <div class="flex items-center space-x-3">
                <div class="w-10 h-10 rounded-xl bg-gray-100 flex items-center justify-center text-gray-400 font-bold group-hover:bg-white transition-colors">
                  {{ user.name?.charAt(0) || user.email.charAt(0).toUpperCase() }}
                </div>
                <div>
                  <div class="text-sm font-bold text-gray-800">{{ user.name || 'Set Profile Name' }}</div>
                  <div class="text-[11px] text-gray-400 font-medium">{{ user.email }}</div>
                </div>
              </div>
            </td>
            <td class="px-6 py-4">
              <div class="flex items-center text-xs font-semibold text-gray-600">
                <Building class="w-3.5 h-3.5 mr-1.5 text-gray-300" />
                {{ user.department || 'Unassigned' }}
              </div>
            </td>
            <td class="px-6 py-4">
              <span :class="['px-2.5 py-1 rounded-lg text-[10px] font-bold uppercase tracking-wider', getRoleBadgeClass(user.role)]">
                {{ user.role }}
              </span>
            </td>
            <td class="px-6 py-4">
              <span :class="['px-2.5 py-1 rounded-lg text-[10px] font-bold uppercase tracking-wider', getStatusBadgeClass(user.status)]">
                {{ user.status || 'ACTIVE' }}
              </span>
            </td>
            <td class="px-6 py-4 text-right">
              <div class="flex items-center justify-end space-x-2">
                <template v-if="user.status === 'PENDING'">
                  <button @click="handleApprove($event, user.id)" class="p-2 text-emerald-500 hover:bg-emerald-50 rounded-lg transition-all" title="Approve">
                    <CheckCircle2 class="w-4 h-4" />
                  </button>
                  <button @click="handleReject($event, user.id)" class="p-2 text-rose-500 hover:bg-rose-50 rounded-lg transition-all" title="Reject">
                    <XCircle class="w-4 h-4" />
                  </button>
                </template>
                <div class="w-8 h-8 flex items-center justify-center text-gray-300 group-hover:text-blue-500 transition-colors">
                  <ChevronRight class="w-4 h-4" />
                </div>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      
      <!-- Empty State -->
      <div v-if="filteredUsers.length === 0" class="p-20 text-center">
        <Users class="w-12 h-12 text-gray-100 mx-auto mb-4" />
        <p class="text-gray-400 font-medium">No members found matching your search</p>
      </div>
    </div>
  </div>
</template>
