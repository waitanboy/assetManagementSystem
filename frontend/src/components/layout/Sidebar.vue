<script setup lang="ts">
import { LayoutDashboard, Package, Tag, Users, Activity, Megaphone, Briefcase, Wrench, Settings2, CheckSquare } from 'lucide-vue-next'
import { useAuthStore } from '../../stores/authStore'

defineProps<{
  isOpen: boolean
}>()

const authStore = useAuthStore()
</script>

<template>
  <aside 
    :class="[
      'fixed inset-y-0 left-0 z-40 flex w-64 flex-col bg-slate-900 border-r min-h-screen text-slate-300 transition-transform duration-300 lg:static lg:translate-x-0',
      isOpen ? 'translate-x-0 shadow-2xl' : '-translate-x-full'
    ]"
  >
    <div class="flex h-20 items-center px-8 border-b border-slate-800">
      <div class="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center mr-3 shadow-lg shadow-blue-500/20">
        <Package class="w-5 h-5 text-white" />
      </div>
      <span class="text-xl font-bold text-white tracking-tight">AssetAdmin</span>
    </div>
    
    <nav class="flex-1 space-y-1 px-4 py-6">
      <div class="pt-4 pb-2 px-4">
        <p class="text-[10px] font-bold text-slate-500 uppercase tracking-widest">Main Menu</p>
      </div>

      <router-link to="/notices" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
        <Megaphone class="w-5 h-5" />
        <span class="font-semibold">Announcements</span>
      </router-link>

      <router-link to="/assets" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
        <Package class="w-5 h-5" />
        <span class="font-semibold">Assets</span>
      </router-link>

      <router-link to="/my-assets" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
        <Briefcase class="w-5 h-5" />
        <span class="font-semibold">My Assets</span>
      </router-link>

      <router-link to="/requests" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
        <CheckSquare class="w-5 h-5" />
        <span class="font-semibold">Rental Requests</span>
      </router-link>

      <div v-if="authStore.isAdmin">
        <div class="pt-4 pb-2 px-4">
          <p class="text-[10px] font-bold text-slate-500 uppercase tracking-widest">Management</p>
        </div>

        <router-link to="/" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
          <LayoutDashboard class="w-5 h-5" />
          <span class="font-semibold">Dashboard</span>
        </router-link>

        <router-link to="/settings" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
          <Settings2 class="w-5 h-5" />
          <span class="font-semibold">System Settings</span>
        </router-link>

        <router-link to="/users" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
          <Users class="w-5 h-5" />
          <span class="font-semibold">Users</span>
        </router-link>
        
        <router-link to="/transactions" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
          <Activity class="w-5 h-5" />
          <span class="font-semibold">Activity Logs</span>
        </router-link>

        <router-link to="/repairs" class="flex items-center space-x-3 px-4 py-3 hover:bg-slate-800 hover:text-white rounded-xl transition-all duration-200" active-class="bg-blue-600 !text-white shadow-lg shadow-blue-900/40">
          <Wrench class="w-5 h-5" />
          <span class="font-semibold">Maintenance</span>
        </router-link>
      </div>
    </nav>

    <div class="p-4 border-t border-slate-800">
      <div class="bg-slate-800/50 p-4 rounded-2xl border border-slate-700/50">
        <p class="text-xs font-medium text-slate-400 mb-1">System Version</p>
        <p class="text-xs font-bold text-white">v3.0.0 Stable</p>
      </div>
    </div>
  </aside>
</template>

