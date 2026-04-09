<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { X, Calendar, UserCog } from 'lucide-vue-next'
import { useAssetStore, type Asset } from '../../stores/assetStore'
import { useCategoryStore } from '../../stores/categoryStore'
import { useUserStore } from '../../stores/userStore'

const props = defineProps<{
  asset: Asset | null
}>()

const emit = defineEmits(['close'])
const assetStore = useAssetStore()
const categoryStore = useCategoryStore()
const userStore = useUserStore()

const formData = ref<Asset>({
  categoryId: 1,
  name: '',
  serialNumber: '',
  status: 'AVAILABLE',
  location: '',
  imageUrl: '',
})

// 대여자 변경 폼 (RENTED 상태 자산 수정 시)
const newRenterId = ref<number | null>(null)
const newDueDate = ref('')

const isEditingRented = computed(() =>
  !!props.asset?.id && props.asset?.status === 'RENTED'
)

const today = new Date().toISOString().split('T')[0]

// LocalDate가 배열([2026,4,16]) 또는 문자열("2026-04-16") 양쪽 형태로 올 수 있으므로 안전 파싱
const parseDateSafe = (val: any): string => {
  if (!val) return ''
  if (Array.isArray(val)) {
    // [year, month, day]
    const [y, m, d] = val
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
  }
  // ISO string "2026-04-16" or "2026-04-16T00:00:00"
  return String(val).slice(0, 10)
}

onMounted(async () => {
  await categoryStore.fetchCategories()
  await userStore.fetchActiveUsers()
  if (props.asset) {
    formData.value = { ...props.asset }
    if (isEditingRented.value) {
      newRenterId.value = props.asset.rentedByUserId ?? null
      newDueDate.value = parseDateSafe(props.asset.dueDate)
    }
  } else if (categoryStore.categories.length > 0) {
    formData.value.categoryId = categoryStore.categories[0].id!
  }
})

// removed comment

const handleSubmit = async () => {
  try {
    if (props.asset?.id) {
      // 1. 기본 자산 정보 수정
      await assetStore.updateAsset(props.asset.id, formData.value)

      // 2. RENTED 상태이고 대여자 변경이 있으면 reassign
      if (isEditingRented.value && newRenterId.value) {
        const originalRenterId = props.asset.rentedByUserId
        const originalDueDate = props.asset.dueDate ?? ''
        const dueDateChanged = newDueDate.value !== originalDueDate
        const renterChanged = newRenterId.value !== originalRenterId

        if (renterChanged || dueDateChanged) {
          await assetStore.reassignRenter(
            props.asset.id,
            newRenterId.value,
            newDueDate.value || undefined
          )
        }
      }
    } else {
      await assetStore.addAsset(formData.value)
    }
    emit('close')
  } catch (err: any) {
    alert(err.response?.data?.message || err.message || 'Something went wrong')
  }
}
</script>

<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm animate-in fade-in duration-200">
    <div class="bg-white w-full max-w-lg rounded-2xl shadow-2xl overflow-hidden animate-in slide-in-from-bottom-4 duration-300 max-h-[90vh] flex flex-col">
      <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 shrink-0">
        <h3 class="text-xl font-bold text-gray-800">{{ asset?.id ? 'Edit Asset' : 'Add New Asset' }}</h3>
        <button @click="$emit('close')" class="p-1 text-gray-400 hover:text-gray-600 rounded-lg transition-colors">
          <X class="w-6 h-6" />
        </button>
      </div>

      <form @submit.prevent="handleSubmit" class="p-6 space-y-5 overflow-y-auto">
        <div class="grid grid-cols-1 gap-5">
          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-1">Asset Name</label>
            <input
              v-model="formData.name"
              type="text"
              required
              placeholder="e.g. MacBook Pro M3"
              class="w-full px-4 py-2 border rounded-xl outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all font-medium"
            />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-semibold text-gray-700 mb-1">Category</label>
              <select
                v-model="formData.categoryId"
                class="w-full px-4 py-2 border rounded-xl outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all bg-white cursor-pointer font-medium"
                required
              >
                <option v-for="cat in categoryStore.categories" :key="cat.id" :value="cat.id">
                  {{ cat.name }}
                </option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-semibold text-gray-700 mb-1">Status</label>
              <select
                v-model="formData.status"
                class="w-full px-4 py-2 border rounded-xl outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all bg-white"
              >
                <option value="AVAILABLE">Available</option>
                <option value="RENTED">Rented</option>
                <option value="REPAIRING">Repairing</option>
              </select>
            </div>
          </div>

          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-1">Serial Number</label>
            <input
              v-model="formData.serialNumber"
              type="text"
              required
              placeholder="SN-XXXX-XXXX"
              class="w-full px-4 py-2 border rounded-xl outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all font-mono"
            />
          </div>

          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-1">Location / Office</label>
            <input
              v-model="formData.location"
              type="text"
              placeholder="e.g. 7th Floor, Room 702"
              class="w-full px-4 py-2 border rounded-xl outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all"
            />
          </div>
        </div>

        <!-- 대여자 변경 섹션 (RENTED 상태 자산 수정 시에만 표시) -->
        <div v-if="isEditingRented" class="border-t border-gray-100 pt-5 space-y-4">
          <div class="flex items-center gap-2 mb-1">
            <div class="w-6 h-6 rounded-lg bg-orange-100 flex items-center justify-center">
              <UserCog class="w-3.5 h-3.5 text-orange-500" />
            </div>
            <p class="text-sm font-bold text-gray-700">대여 정보 수정</p>
            <span class="text-[10px] bg-orange-50 text-orange-500 border border-orange-100 px-1.5 py-0.5 rounded font-semibold">Admin Only</span>
          </div>

          <!-- 현재 대여자 정보 표시 -->
          <div v-if="asset?.rentedByEmail" class="flex items-center gap-2.5 px-3 py-2.5 bg-orange-50 border border-orange-100 rounded-xl">
            <div class="w-7 h-7 rounded-full bg-orange-200 flex items-center justify-center shrink-0 text-xs font-bold text-orange-700">
              {{ asset.rentedByEmail.charAt(0).toUpperCase() }}
            </div>
            <div>
              <p class="text-xs text-orange-500 font-semibold">현재 대여자</p>
              <p class="text-sm font-bold text-orange-800 leading-tight">{{ asset.rentedByEmail }}</p>
              <p v-if="asset.rentedByDepartment" class="text-[11px] text-orange-500">{{ asset.rentedByDepartment }}</p>
            </div>
          </div>

          <div>
            <label class="block text-sm font-semibold text-gray-600 mb-1">대여자 변경</label>
            <select
              v-model="newRenterId"
              class="w-full px-4 py-2 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-orange-400/20 focus:border-orange-400 transition-all bg-white cursor-pointer text-sm"
            >
              <option :value="null" disabled>사용자 선택</option>
              <option v-for="user in userStore.activeUsers" :key="user.id" :value="user.id">
                {{ user.email }} {{ user.department ? `(${user.department})` : '' }}
              </option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-semibold text-gray-600 mb-1 flex items-center gap-1.5">
              <Calendar class="w-3.5 h-3.5 text-orange-400" />
              반납 예정일 변경
            </label>
            <input
              v-model="newDueDate"
              type="date"
              :min="today"
              class="w-full px-4 py-2 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-orange-400/20 focus:border-orange-400 transition-all text-sm"
            />
          </div>
        </div>

        <div class="flex justify-end space-x-3 pt-2 border-t border-gray-100">
          <button
            type="button"
            @click="$emit('close')"
            class="px-4 py-2 text-sm font-medium text-gray-600 hover:bg-gray-100 rounded-xl transition-colors font-semibold"
          >
            Cancel
          </button>
          <button
            type="submit"
            class="px-6 py-2 bg-slate-900 text-white text-sm font-semibold rounded-xl hover:bg-slate-800 transition-colors shadow-sm"
          >
            {{ asset?.id ? 'Save Changes' : 'Register Asset' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
