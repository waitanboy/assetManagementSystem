<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'

const isSidebarOpen = ref(false)
const route = useRoute()

// Close sidebar on navigation (mobile)
watch(() => route.path, () => {
  isSidebarOpen.value = false
})
</script>

<template>
  <div class="flex h-screen bg-gray-50 text-slate-800 overflow-hidden relative">
    <!-- Mobile Backdrop -->
    <Transition
      enter-active-class="transition-opacity duration-300 ease-out"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-200 ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div 
        v-if="isSidebarOpen" 
        @click="isSidebarOpen = false"
        class="fixed inset-0 z-30 bg-slate-900/50 backdrop-blur-sm lg:hidden"
      ></div>
    </Transition>

    <Sidebar :is-open="isSidebarOpen" />
    
    <div class="flex flex-col flex-1 min-w-0">
      <Header @toggle-sidebar="isSidebarOpen = !isSidebarOpen" />
      <main class="flex-1 overflow-x-hidden overflow-y-auto bg-gray-50 p-4 sm:p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>
