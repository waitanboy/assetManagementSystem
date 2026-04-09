<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, MapPin, Tag, Clock, User, ArrowUpRight, ArrowDownLeft, FileText } from 'lucide-vue-next'
import { useAssetStore, type Asset } from '../stores/assetStore'
import { useTransactionStore, type Transaction } from '../stores/transactionStore'

const route = useRoute()
const router = useRouter()
const assetStore = useAssetStore()
const transactionStore = useTransactionStore()

const asset = ref<Asset | null>(null)
const history = ref<Transaction[]>([])
const loading = ref(true)

const assetId = Number(route.params.id)

onMounted(async () => {
  loading.value = true
  try {
    asset.value = await assetStore.getAssetById(assetId)
    history.value = await transactionStore.fetchAssetHistory(assetId)
  } catch (err) {
    console.error('Failed to load asset details', err)
  } finally {
    loading.value = false
  }
})

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('ko-KR', {
    dateStyle: 'medium',
    timeStyle: 'short'
  })
}

const getStatusClass = (status: string) => {
  switch (status) {
    case 'AVAILABLE': return 'bg-green-50 text-green-700 border-green-200'
    case 'RENTED': return 'bg-orange-50 text-orange-700 border-orange-200'
    case 'REPAIRING': return 'bg-red-50 text-red-700 border-red-200'
    default: return 'bg-gray-50 text-gray-700 border-gray-200'
  }
}
</script>

<template>
  <div class="max-w-5xl mx-auto space-y-6 pb-12">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <button 
        @click="router.back()" 
        class="flex items-center space-x-2 text-gray-500 hover:text-gray-900 transition-colors"
      >
        <ArrowLeft class="w-5 h-5" />
        <span class="font-medium">Back to List</span>
      </button>
      <div v-if="asset" class="flex space-x-3">
        <!-- Edit button could be here -->
      </div>
    </div>

    <div v-if="loading" class="flex flex-col items-center justify-center h-64 text-gray-400">
      <p>Loading asset information...</p>
    </div>

    <template v-else-if="asset">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Sidebar: Basic Info -->
        <div class="lg:col-span-1 space-y-6">
          <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
            <div class="w-full aspect-square bg-gray-100 rounded-xl mb-6 flex items-center justify-center text-gray-300">
              <Tag v-if="!asset.imageUrl" class="w-12 h-12" />
              <img v-else :src="asset.imageUrl" class="w-full h-full object-cover rounded-xl" />
            </div>
            
            <h2 class="text-2xl font-bold text-gray-900 mb-2">{{ asset.name }}</h2>
            <div class="flex items-center mb-4 text-sm text-gray-500">
                <span :class="['px-3 py-1 rounded-full text-[10px] font-bold border', getStatusClass(asset.status)]">
                    {{ asset.status }}
                </span>
            </div>

            <div class="space-y-4 pt-4 border-t border-gray-50">
              <div class="flex items-start space-x-3 text-sm">
                <Tag class="w-4 h-4 text-gray-400 mt-0.5" />
                <div>
                  <p class="text-gray-400 text-xs">Serial Number</p>
                  <p class="font-mono font-medium">{{ asset.serialNumber }}</p>
                </div>
              </div>
              <div class="flex items-start space-x-3 text-sm">
                <MapPin class="w-4 h-4 text-gray-400 mt-0.5" />
                <div>
                  <p class="text-gray-400 text-xs">Current Location</p>
                  <p class="font-medium">{{ asset.location || 'Not designated' }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Main: History Timeline -->
        <div class="lg:col-span-2 space-y-6">
          <div class="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 min-h-[500px]">
            <div class="flex items-center justify-between mb-8">
              <h3 class="text-xl font-bold text-gray-800 flex items-center">
                <FileText class="w-5 h-5 mr-3 text-blue-600" />
                Activity Timeline
              </h3>
              <span class="text-xs px-3 py-1 bg-gray-100 text-gray-500 rounded-full font-medium">
                {{ history.length }} Total Events
              </span>
            </div>

            <div v-if="history.length === 0" class="flex flex-col items-center justify-center h-64 text-gray-300">
              <p>No activity history recorded for this asset.</p>
            </div>

            <div v-else class="relative space-y-8 before:absolute before:inset-0 before:ml-5 before:-translate-x-px before:h-full before:w-0.5 before:bg-gradient-to-b before:from-gray-100 before:to-gray-50">
              <div v-for="event in history" :key="event.id" class="relative flex items-start group">
                <div :class="[
                  'absolute left-0 w-10 h-10 rounded-xl flex items-center justify-center border-4 border-white shadow-sm ring-1 ring-gray-100 z-10 transition-transform group-hover:scale-110',
                  event.type === 'OUT' ? 'bg-blue-600' : 'bg-green-600'
                ]">
                  <ArrowUpRight v-if="event.type === 'OUT'" class="w-4 h-4 text-white" />
                  <ArrowDownLeft v-else class="w-4 h-4 text-white" />
                </div>
                
                <div class="pl-16 w-full">
                  <div class="bg-white p-4 rounded-xl border border-gray-100 group-hover:border-blue-100 transition-colors shadow-sm mb-1">
                    <div class="flex items-center justify-between mb-2">
                        <span :class="['text-[10px] font-bold px-2 py-0.5 rounded-md uppercase tracking-wider', event.type === 'OUT' ? 'bg-blue-50 text-blue-600' : 'bg-green-50 text-green-600']">
                            {{ event.type === 'OUT' ? 'Rent' : 'Return' }}
                        </span>
                        <div class="flex items-center text-xs text-gray-400">
                            <Clock class="w-3 h-3 mr-1" />
                            {{ formatDate(event.transactionDate) }}
                        </div>
                    </div>
                    
                    <div class="flex items-center space-x-2 mb-2">
                        <User class="w-3.5 h-3.5 text-gray-400" />
                        <span class="text-sm font-semibold text-gray-700">{{ event.userEmail }}</span>
                    </div>

                    <p v-if="event.note" class="text-sm text-gray-500 italic bg-gray-50/50 p-2 rounded-lg border border-gray-100/50">
                        "{{ event.note }}"
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
