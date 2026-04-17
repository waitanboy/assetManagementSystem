<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { Terminal, RefreshCw, Trash2, Power, ShieldAlert, Cpu, Activity } from 'lucide-vue-next'
import api from '../api'

const logs = ref<string[]>([])
const loading = ref(false)
const autoRefresh = ref(true)
const logContainer = ref<HTMLElement | null>(null)
const filterText = ref('')

let refreshInterval: any = null

const fetchLogs = async () => {
  try {
    const response = await api.get<string[]>('/system/logs?lines=200')
    logs.value = response.data
    if (autoRefresh.value) {
      scrollToBottom()
    }
  } catch (error) {
    console.error('Failed to fetch logs', error)
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (logContainer.value) {
      logContainer.value.scrollTop = logContainer.value.scrollHeight
    }
  })
}

const toggleAutoRefresh = () => {
  autoRefresh.value = !autoRefresh.value
  if (autoRefresh.value) {
    startPolling()
  } else {
    stopPolling()
  }
}

const startPolling = () => {
  stopPolling()
  refreshInterval = setInterval(fetchLogs, 3000)
}

const stopPolling = () => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
    refreshInterval = null
  }
}

const clearLogs = () => {
  logs.value = []
}

onMounted(() => {
  fetchLogs()
  if (autoRefresh.value) {
    startPolling()
  }
})

onUnmounted(() => {
  stopPolling()
})

const filteredLogs = computed(() => {
  if (!filterText.value) return logs.value
  return logs.value.filter(line => 
    line.toLowerCase().includes(filterText.value.toLowerCase())
  )
})

const getLogColorClass = (line: string) => {
  if (line.includes('ERROR')) return 'text-rose-400'
  if (line.includes('WARN')) return 'text-amber-400'
  if (line.includes('DEBUG')) return 'text-slate-500'
  if (line.includes('INFO')) return 'text-emerald-400'
  return 'text-slate-300'
}
</script>

<template>
  <div class="max-w-7xl mx-auto space-y-6">
    <!-- Header -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h2 class="text-2xl font-bold text-slate-800 flex items-center">
          <Terminal class="w-7 h-7 mr-3 text-slate-900" />
          System Console
        </h2>
        <p class="text-sm text-slate-400 font-medium mt-1">Real-time application logs and system events.</p>
      </div>
      
      <div class="flex items-center gap-3">
        <div class="flex items-center px-4 py-2 bg-white border border-slate-100 rounded-2xl shadow-sm">
          <Activity class="w-4 h-4 mr-2 text-emerald-500 animate-pulse" />
          <span class="text-xs font-bold text-slate-600 uppercase tracking-widest">Active Stream</span>
        </div>
        
        <button 
          @click="toggleAutoRefresh"
          :class="['px-5 py-2.5 rounded-2xl font-bold text-xs uppercase tracking-widest transition-all flex items-center gap-2', 
            autoRefresh ? 'bg-emerald-50 text-emerald-600' : 'bg-slate-100 text-slate-400']"
        >
          <RefreshCw :class="['w-4 h-4', autoRefresh ? 'animate-spin-slow' : '']" />
          {{ autoRefresh ? 'Auto Syncing' : 'Paused' }}
        </button>
      </div>
    </div>

    <!-- Terminal -->
    <div class="bg-slate-950 rounded-[2.5rem] shadow-2xl border border-slate-900 overflow-hidden flex flex-col h-[700px]">
      <!-- Terminal Header -->
      <div class="px-8 py-4 bg-slate-900/50 border-b border-slate-900 flex items-center justify-between">
        <div class="flex items-center space-x-2">
          <div class="w-3 h-3 rounded-full bg-rose-500"></div>
          <div class="w-3 h-3 rounded-full bg-amber-500"></div>
          <div class="w-3 h-3 rounded-full bg-emerald-500"></div>
          <span class="ml-4 text-[10px] font-black text-slate-500 uppercase tracking-[0.3em]">bash — logs@asset-server</span>
        </div>
        
        <div class="flex items-center gap-4">
          <div class="relative">
            <input 
              v-model="filterText"
              type="text" 
              placeholder="Filter logs..."
              class="bg-slate-800/50 border-none rounded-xl px-4 py-1.5 text-xs text-slate-300 focus:ring-1 focus:ring-slate-700 w-48 placeholder:text-slate-600"
            />
          </div>
          <button @click="clearLogs" class="text-slate-500 hover:text-rose-400 transition-colors">
            <Trash2 class="w-4 h-4" />
          </button>
        </div>
      </div>

      <!-- Log Content -->
      <div 
        ref="logContainer"
        class="flex-1 overflow-y-auto p-8 font-mono text-[13px] leading-relaxed custom-terminal-scrollbar bg-[radial-gradient(circle_at_center,_var(--tw-gradient-stops))] from-slate-900/20 to-transparent"
      >
        <div v-if="filteredLogs.length === 0" class="h-full flex flex-col items-center justify-center opacity-20">
          <Cpu class="w-16 h-16 mb-4 text-slate-400" />
          <p class="font-bold uppercase tracking-widest">No matching logs</p>
        </div>
        <div 
          v-for="(line, index) in filteredLogs" 
          :key="index"
          class="flex group py-0.5"
        >
          <span class="w-12 text-slate-700 shrink-0 select-none text-[10px] font-bold">{{ index + 1 }}</span>
          <span :class="['break-all whitespace-pre-wrap', getLogColorClass(line)]">{{ line }}</span>
        </div>
      </div>

      <!-- Terminal Footer -->
      <div class="px-8 py-3 bg-slate-900/30 border-t border-slate-900 flex items-center justify-between text-[10px] font-bold text-slate-600 uppercase tracking-widest">
        <div class="flex items-center gap-6">
          <div class="flex items-center gap-2">
            <div class="w-1.5 h-1.5 rounded-full bg-blue-500"></div>
            <span>JVM: OK</span>
          </div>
          <div class="flex items-center gap-2">
            <div class="w-1.5 h-1.5 rounded-full bg-indigo-500"></div>
            <span>Buffer: {{ filteredLogs.length }} lines</span>
          </div>
        </div>
        <div class="text-slate-700 italic">
          Asset Management System v3.0 // Server Time: {{ new Date().toLocaleTimeString() }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.custom-terminal-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.custom-terminal-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-terminal-scrollbar::-webkit-scrollbar-thumb {
  background: #1e293b;
  border-radius: 4px;
}
.custom-terminal-scrollbar:hover::-webkit-scrollbar-thumb {
  background: #334155;
}

.animate-spin-slow {
  animation: spin 3s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
