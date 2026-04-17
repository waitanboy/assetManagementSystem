<script setup lang="ts">
import { useRegisterSW } from 'virtual:pwa-register/vue'
import { RefreshCw, X } from 'lucide-vue-next'

const {
  offlineReady,
  needRefresh,
  updateServiceWorker,
} = useRegisterSW()

const close = () => {
  offlineReady.value = false
  needRefresh.value = false
}
</script>

<template>
  <transition
    enter-active-class="transition duration-500 ease-out transform"
    enter-from-class="translate-y-full opacity-0 scale-95"
    enter-to-class="translate-y-0 opacity-100 scale-100"
    leave-active-class="transition duration-300 ease-in transform"
    leave-from-class="translate-y-0 opacity-100 scale-100"
    leave-to-class="translate-y-full opacity-0 scale-95"
  >
    <div
      v-if="offlineReady || needRefresh"
      class="fixed bottom-6 left-6 z-[60] p-4 bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl flex items-center space-x-5 max-w-sm ring-1 ring-white/10 backdrop-blur-xl animate-in fade-in slide-in-from-bottom-4 duration-500"
      role="alert"
    >
      <div class="flex-shrink-0 w-12 h-12 bg-blue-600 rounded-xl flex items-center justify-center text-white shadow-lg shadow-blue-500/20">
        <RefreshCw :class="['w-6 h-6', needRefresh ? 'animate-spin-slow' : '']" />
      </div>
      
      <div class="flex-1 min-w-0">
        <p class="text-sm font-bold text-white mb-1">
          {{ offlineReady ? 'Ready for Offline Use' : 'Update Available' }}
        </p>
        <p class="text-[11px] text-slate-400 font-medium leading-relaxed">
          {{ offlineReady 
            ? 'The app has been cached and is ready to work offline.' 
            : 'A new version of AssetAdmin is available. Refresh to update.' 
          }}
        </p>
      </div>

      <div class="flex flex-col space-y-2">
        <button
          v-if="needRefresh"
          @click="updateServiceWorker()"
          class="px-3 py-1.5 bg-blue-600 hover:bg-blue-500 text-white text-[10px] font-bold rounded-lg transition-all active:scale-95 whitespace-nowrap"
        >
          Update Now
        </button>
        <button
          @click="close"
          class="p-2 text-slate-500 hover:text-white hover:bg-white/5 rounded-lg transition-colors flex items-center justify-center"
        >
          <X class="w-4 h-4" />
        </button>
      </div>
    </div>
  </transition>
</template>

<style scoped>
@keyframes spin-slow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.animate-spin-slow {
  animation: spin-slow 8s linear infinite;
}
</style>
