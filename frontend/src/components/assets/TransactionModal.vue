<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { X, Calendar } from 'lucide-vue-next'
import { useAssetStore, type Asset } from '../../stores/assetStore'
import { useUserStore } from '../../stores/userStore'
import { useAuthStore } from '../../stores/authStore'

const props = defineProps<{
  asset: Asset
  type: 'RENT' | 'RETURN'
}>()

const emit = defineEmits(['close'])
const assetStore = useAssetStore()
const userStore = useUserStore()
const authStore = useAuthStore()

const userId = ref<number | null>(null)
const note = ref('')
const dueDate = ref('')
const loading = ref(false)

// 오늘 날짜 (min 속성용)
const today = new Date().toISOString().split('T')[0]

// 기본값: 오늘로부터 7일 후
const defaultDueDate = computed(() => {
  const d = new Date()
  d.setDate(d.getDate() + 7)
  return d.toISOString().split('T')[0]
})

onMounted(() => {
  if (props.type === 'RENT') {
    dueDate.value = defaultDueDate.value
    if (authStore.isAdmin) {
      userStore.fetchActiveUsers()
    } else {
      userId.value = authStore.id
    }
  }
})

const handleSubmit = async () => {
  if (props.type === 'RENT' && !userId.value) return
  if (props.type === 'RENT' && !dueDate.value) return
  
  loading.value = true
  try {
    const targetUserId = userId.value || authStore.id!
    if (props.type === 'RENT') {
      await assetStore.rentAsset(props.asset.id!, targetUserId, note.value, dueDate.value)
    } else {
      await assetStore.returnAsset(props.asset.id!, targetUserId, note.value)
    }
    emit('close')
  } catch (error) {
    console.error('Transaction failed', error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm">
    <div class="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden animate-in fade-in zoom-in duration-200">
      <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gray-50/50">
        <h3 class="text-lg font-bold text-gray-800">
          {{ type === 'RENT' ? '자산 대여' : '자산 반납' }}
        </h3>
        <button @click="emit('close')" class="p-2 text-gray-400 hover:text-gray-600 rounded-full transition-colors">
          <X class="w-5 h-5" />
        </button>
      </div>

      <div class="p-6 space-y-4">
        <div class="p-4 bg-blue-50 border border-blue-100 rounded-xl flex items-center space-x-3 text-blue-800">
          <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center font-bold">
            {{ asset.name.charAt(0) }}
          </div>
          <div>
            <p class="font-bold">{{ asset.name }}</p>
            <p class="text-xs text-blue-600/80">{{ asset.serialNumber }}</p>
          </div>
        </div>

        <div v-if="type === 'RENT' && authStore.isAdmin" class="space-y-1.5">
          <label class="text-sm font-semibold text-gray-600 ml-1">대여 대상자</label>
          <select
            v-model="userId"
            class="w-full px-4 py-2 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all"
            required
          >
            <option :value="null" disabled>사용자 선택</option>
            <option v-for="user in userStore.activeUsers" :key="user.id" :value="user.id">
              {{ user.email }} ({{ user.department || 'N/A' }})
            </option>
          </select>
        </div>
        
        <div v-if="type === 'RENT' && !authStore.isAdmin" class="p-3 bg-gray-50 border border-gray-200 rounded-xl flex flex-col justify-center">
          <label class="text-xs font-bold text-gray-500 mb-0.5">대여 대상자</label>
          <p class="font-semibold text-gray-800">{{ authStore.email }} (본인)</p>
        </div>

        <!-- 반납 예정일 (RENT 시에만 표시) -->
        <div v-if="type === 'RENT'" class="space-y-1.5">
          <label class="text-sm font-semibold text-gray-600 ml-1 flex items-center gap-1.5">
            <Calendar class="w-4 h-4 text-blue-500" />
            반납 예정일 <span class="text-red-500">*</span>
          </label>
          <input
            v-model="dueDate"
            type="date"
            :min="today"
            class="w-full px-4 py-2 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all text-gray-700"
            required
          />
          <p class="text-xs text-gray-400 ml-1">예정일 초과 시 My Assets에서 빨간색으로 표시됩니다.</p>
        </div>

        <div class="space-y-1.5">
          <label class="text-sm font-semibold text-gray-600 ml-1">메모 (선택)</label>
          <textarea
            v-model="note"
            rows="3"
            placeholder="추가 정보를 입력하세요..."
            class="w-full px-4 py-2 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all resize-none"
          ></textarea>
        </div>
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
          :disabled="loading || (type === 'RENT' && (!userId || !dueDate))"
          class="px-6 py-2 bg-slate-900 text-white text-sm font-semibold rounded-xl hover:bg-slate-800 transition-colors disabled:opacity-50 disabled:cursor-not-allowed shadow-sm"
        >
          {{ loading ? '처리 중...' : (type === 'RENT' ? '대여 확정' : '반납 처리') }}
        </button>
      </div>
    </div>
  </div>
</template>

