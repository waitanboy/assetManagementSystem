<script setup lang="ts">
import { ref, computed } from 'vue'
import { Wrench, CheckCircle2, DollarSign } from 'lucide-vue-next'
import { useAssetStore, type Asset } from '../../stores/assetStore'

const props = defineProps<{
  asset: Asset | null
  type: 'START' | 'COMPLETE'
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const assetStore = useAssetStore()

const reason = ref('')
const estimatedCost = ref<number | null>(null)
const finalCost = ref<number | null>(null)
const resolutionNote = ref('')
const loading = ref(false)
const selectedAssetId = ref<number | null>(props.asset?.id || null)

const selectableAssets = computed(() => {
  return assetStore.assets.filter(a => a.status === 'AVAILABLE' || a.status === 'RENTED')
})

const handleSubmit = async () => {
  if (props.type === 'START' && (!reason.value || !selectedAssetId.value)) return
  if (props.type === 'COMPLETE' && (finalCost.value === null || !selectedAssetId.value)) return
  
  loading.value = true
  try {
    if (props.type === 'START') {
      await assetStore.startRepair(selectedAssetId.value!, reason.value, estimatedCost.value || undefined)
    } else {
      await assetStore.completeRepair(selectedAssetId.value!, finalCost.value!, resolutionNote.value)
    }
    emit('close')
  } catch (error) {
    console.error('Repair action failed', error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm">
    <div class="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden animate-in fade-in zoom-in duration-200">
      <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100"
           :class="type === 'START' ? 'bg-orange-50/50' : 'bg-green-50/50'">
        <h3 class="text-lg font-bold flex items-center gap-2" 
            :class="type === 'START' ? 'text-orange-800' : 'text-green-800'">
          <Wrench v-if="type === 'START'" class="w-5 h-5 text-orange-500" />
          <CheckCircle2 v-else class="w-5 h-5 text-green-500" />
          {{ type === 'START' ? '수리 접수' : '수리 완료' }}
        </h3>
        <button @click="emit('close')" class="text-gray-400 hover:text-gray-600 transition-colors">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <div class="p-6 space-y-5">
        <div class="p-4 bg-gray-50 border border-gray-100 rounded-xl flex flex-col justify-center">
          <p class="text-xs text-gray-500 font-bold mb-1.5">대상 자산 <span v-if="!props.asset" class="text-red-500">*</span></p>
          <div v-if="props.asset" class="flex justify-between items-center">
            <p class="font-semibold text-gray-800">{{ props.asset.name }}</p>
            <p class="text-sm text-gray-500 font-mono">{{ props.asset.serialNumber }}</p>
          </div>
          <select
            v-else
            v-model="selectedAssetId"
            class="w-full px-4 py-2 border border-gray-200 rounded-xl focus:ring-2 focus:ring-orange-400/20 focus:border-orange-400 outline-none transition-all bg-white"
            required
          >
            <option :value="null" disabled>수리할 자산을 선택하세요</option>
            <option v-for="a in selectableAssets" :key="a.id" :value="a.id">
              {{ a.name }} ({{ a.serialNumber }})
            </option>
          </select>
        </div>

        <!-- START REPAIR FIELDS -->
        <template v-if="type === 'START'">
          <div class="space-y-1.5">
            <label class="text-sm font-semibold text-gray-700 ml-1">고장 증상 / 수리 사유 <span class="text-red-500">*</span></label>
            <textarea
              v-model="reason"
              rows="3"
              placeholder="액정 파손, 부팅 불가 등 자세한 증상을 남겨주세요."
              class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-orange-400/20 focus:border-orange-400 outline-none transition-all resize-none bg-white"
              required
            ></textarea>
          </div>

          <div class="space-y-1.5">
            <label class="text-sm font-semibold text-gray-700 ml-1">예상 수리 비용 (선택)</label>
            <div class="relative">
              <span class="absolute inset-y-0 left-0 pl-3 flex items-center text-gray-400">
                <DollarSign class="w-4 h-4" />
              </span>
              <input
                v-model="estimatedCost"
                type="number"
                min="0"
                placeholder="예: 50000"
                class="w-full pl-9 pr-4 py-2 border border-gray-200 rounded-xl focus:ring-2 focus:ring-orange-400/20 focus:border-orange-400 outline-none transition-all bg-white"
              />
            </div>
          </div>
        </template>

        <!-- COMPLETE REPAIR FIELDS -->
        <template v-else>
          <div class="space-y-1.5">
            <label class="text-sm font-semibold text-gray-700 ml-1">최종 수리 비용 <span class="text-red-500">*</span></label>
             <div class="relative">
              <span class="absolute inset-y-0 left-0 pl-3 flex items-center text-gray-400">
                <DollarSign class="w-4 h-4" />
              </span>
              <input
                v-model="finalCost"
                type="number"
                min="0"
                placeholder="실제 지출된 비용 입력"
                class="w-full pl-9 pr-4 py-2 border border-gray-200 rounded-xl focus:ring-2 focus:ring-green-500/20 focus:border-green-500 outline-none transition-all bg-white"
                required
              />
            </div>
          </div>

          <div class="space-y-1.5">
            <label class="text-sm font-semibold text-gray-700 ml-1">수리 결과 및 메모 (선택)</label>
            <textarea
              v-model="resolutionNote"
              rows="3"
              placeholder="메인보드 교체 완료, 보증 수리 적용됨 등..."
              class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:ring-2 focus:ring-green-500/20 focus:border-green-500 outline-none transition-all resize-none bg-white"
            ></textarea>
          </div>
        </template>
      </div>

      <div class="px-6 py-4 bg-gray-50 border-t border-gray-100 flex items-center justify-end space-x-3">
        <button
          @click="emit('close')"
          class="px-4 py-2 text-sm font-semibold text-gray-600 hover:bg-gray-200 rounded-xl transition-colors"
        >
          취소
        </button>
        <button
          @click="handleSubmit"
          :disabled="loading || (type === 'START' && (!reason || !selectedAssetId)) || (type === 'COMPLETE' && (finalCost === null || !selectedAssetId))"
          :class="[
            'px-6 py-2 text-sm font-bold text-white rounded-xl transition-all shadow-sm flex items-center gap-2',
            loading || (type === 'START' && (!reason || !selectedAssetId)) || (type === 'COMPLETE' && (finalCost === null || !selectedAssetId))
              ? 'opacity-50 cursor-not-allowed bg-gray-400' 
              : (type === 'START' ? 'bg-orange-500 hover:bg-orange-600 hover:shadow-md active:scale-95' : 'bg-green-600 hover:bg-green-700 hover:shadow-md active:scale-95')
          ]"
        >
          <svg v-if="loading" class="animate-spin h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <span v-else>{{ type === 'START' ? '수리 시작 등록' : '수리 완료 처리' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>
