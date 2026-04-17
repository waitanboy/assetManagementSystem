<script setup lang="ts">
import { onMounted } from 'vue'
import { LayoutDashboard, Package, Users, Activity, Megaphone, Briefcase, Wrench, Settings2, CheckSquare, MessageSquare, Terminal } from 'lucide-vue-next'
import { useAuthStore } from '../../stores/authStore'
import { useChatStore } from '../../stores/chatStore'
import { useRequestStore } from '../../stores/requestStore'

defineProps<{
  isOpen: boolean
}>()

const authStore = useAuthStore()
const chatStore = useChatStore()
const requestStore = useRequestStore()

onMounted(() => {
  if (authStore.isAuthenticated) {
    chatStore.fetchUnreadCount()
    if (authStore.isAdmin) {
      requestStore.fetchPendingRequests()
    }
  }
})

const handleChatClick = (e: Event) => {
  if (!authStore.isAdmin) {
    e.preventDefault()
    // Trigger chat widget opening via an event or direct store state if we added it
    // For now, let's use a window event as a simple way to communicate with ChatWidget
    window.dispatchEvent(new CustomEvent('open-chat-widget'))
  }
}
</script>

<template>
  <aside 
    :class="[
      'fixed inset-y-0 left-0 z-40 flex w-68 flex-col bg-slate-900 border-r min-h-screen text-slate-300 transition-transform duration-300 lg:static lg:translate-x-0',
      isOpen ? 'translate-x-0 shadow-2xl' : '-translate-x-full'
    ]"
  >
    <div class="flex h-20 items-center px-8 border-b border-slate-800">
      <div class="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center mr-3 shadow-lg shadow-blue-500/20">
        <Package class="w-5 h-5 text-white" />
      </div>
      <span class="text-xl font-bold text-white tracking-tight">AssetAdmin</span>
    </div>
    
    <nav class="flex-1 space-y-8 px-4 py-8 overflow-y-auto custom-scrollbar">
      <!-- Group 1: GENERAL -->
      <div>
        <div class="px-4 mb-2 flex items-center">
          <div class="w-1 h-3 bg-blue-500 rounded-full mr-2"></div>
          <p class="text-[10px] font-bold text-slate-500 uppercase tracking-widest">General</p>
        </div>
        <div class="space-y-1">
          <router-link v-if="authStore.isAdmin" to="/" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
            <LayoutDashboard class="w-5 h-5 opacity-70" />
            <span class="font-semibold">Dashboard</span>
          </router-link>

          <router-link to="/notices" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
            <Megaphone class="w-5 h-5 opacity-70" />
            <span class="font-semibold">Announcements</span>
          </router-link>
        </div>
      </div>

      <!-- Group 2: ASSET OPERATIONS -->
      <div>
        <div class="px-4 mb-2 flex items-center">
          <div class="w-1 h-3 bg-indigo-500 rounded-full mr-2"></div>
          <p class="text-[10px] font-bold text-slate-500 uppercase tracking-widest">Asset Operations</p>
        </div>
        <div class="space-y-1">
          <router-link to="/assets" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
            <Package class="w-5 h-5 opacity-70" />
            <span class="font-semibold">All Assets</span>
          </router-link>

          <router-link to="/requests" class="flex items-center justify-between px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40 group">
            <div class="flex items-center space-x-3">
              <CheckSquare class="w-5 h-5 opacity-70" />
              <span class="font-semibold">{{ authStore.isAdmin ? 'Manage Requests' : 'Rental Requests' }}</span>
            </div>
            <span v-if="authStore.isAdmin && requestStore.pendingRequests.length > 0" class="flex h-5 min-w-[20px] items-center justify-center rounded-full bg-indigo-500 px-1.5 text-[10px] font-bold text-white ring-2 ring-slate-900 group-[.bg-blue-600]:ring-blue-600">
              {{ requestStore.pendingRequests.length }}
            </span>
          </router-link>

          <router-link v-if="authStore.isAdmin" to="/repairs" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
            <Wrench class="w-5 h-5 opacity-70" />
            <span class="font-semibold">Maintenance</span>
          </router-link>
        </div>
      </div>

      <!-- Group 3: USER CENTER -->
      <div>
        <div class="px-4 mb-2 flex items-center">
          <div class="w-1 h-3 bg-emerald-500 rounded-full mr-2"></div>
          <p class="text-[10px] font-bold text-slate-500 uppercase tracking-widest">User Center</p>
        </div>
        <div class="space-y-1">
          <router-link to="/my-assets" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
            <Briefcase class="w-5 h-5 opacity-70" />
            <span class="font-semibold">My Assets</span>
          </router-link>

          <router-link 
            to="/support" 
            @click="handleChatClick"
            class="flex items-center justify-between px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" 
            active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40 group"
          >
            <div class="flex items-center space-x-3">
              <MessageSquare class="w-5 h-5 opacity-70" />
              <span class="font-semibold">Support Chat</span>
            </div>
            <span v-if="chatStore.unreadCount > 0" class="flex h-5 min-w-[20px] items-center justify-center rounded-full bg-red-500 px-1.5 text-[10px] font-bold text-white ring-2 ring-slate-900 group-[.bg-blue-600]:ring-blue-600 animate-pulse">
              {{ chatStore.unreadCount > 99 ? '99+' : chatStore.unreadCount }}
            </span>
          </router-link>

          <router-link to="/board" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
            <Activity class="w-5 h-5 opacity-70" />
            <span class="font-semibold">Community Board</span>
          </router-link>
        </div>
      </div>

      <!-- Group 4: SYSTEM (Admin only) -->
      <div v-if="authStore.isAdmin">
        <div class="px-4 mb-2 flex items-center">
          <div class="w-1 h-3 bg-slate-500 rounded-full mr-2"></div>
          <p class="text-[10px] font-bold text-slate-500 uppercase tracking-widest">System</p>
        </div>
        <div class="space-y-1">
          <router-link to="/users" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
            <Users class="w-5 h-5 opacity-70" />
            <span class="font-semibold">Users</span>
          </router-link>
          
          <router-link to="/transactions" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
            <Activity class="w-5 h-5 opacity-70" />
            <span class="font-semibold">Activity Logs</span>
          </router-link>

          <router-link to="/settings" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
            <Settings2 class="w-5 h-5 opacity-70" />
            <span class="font-semibold">Settings</span>
          </router-link>

          <router-link to="/system/console" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
            <Terminal class="w-5 h-5 opacity-70" />
            <span class="font-semibold">System Console</span>
          </router-link>
        </div>
      </div>
    </nav>

    <div class="p-4 border-t border-slate-800">
      <div class="bg-slate-800/50 p-4 rounded-2xl border border-slate-700/50">
        <p class="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2">System Version</p>
        <div class="flex items-center justify-between">
          <span class="text-xs font-bold text-white px-2 py-0.5 bg-blue-500/10 text-blue-400 rounded-md border border-blue-500/20">v3.0.0 Stable</span>
          <div class="flex space-x-1">
            <div class="w-1.5 h-1.5 rounded-full bg-green-500"></div>
          </div>
        </div>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #1e293b;
  border-radius: 10px;
}
.custom-scrollbar:hover::-webkit-scrollbar-thumb {
  background: #334155;
}
</style>

