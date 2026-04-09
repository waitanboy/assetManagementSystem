<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { Package, ArrowDownLeft, ArrowUpRight, RefreshCw, AlertTriangle, Clock } from 'lucide-vue-next'
import { useAssetStore } from '../stores/assetStore'
import TransactionModal from '../components/assets/TransactionModal.vue'
import { ref } from 'vue'
import type { Asset } from '../stores/assetStore'

const assetStore = useAssetStore()
const showTransactionModal = ref(false)
const selectedAsset = ref<Asset | null>(null)

const today = new Date()
today.setHours(0, 0, 0, 0)

// 각 자산의 연체 여부 판단
const isOverdue = (asset: Asset): boolean => {
  if (!asset.dueDate) return false
  const due = new Date(asset.dueDate)
  due.setHours(0, 0, 0, 0)
  return due < today
}

// 반납 예정일까지 남은 일수 (음수면 연체)
const daysLeft = (asset: Asset): number | null => {
  if (!asset.dueDate) return null
  const due = new Date(asset.dueDate)
  due.setHours(0, 0, 0, 0)
  const diff = Math.round((due.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
  return diff
}

const formatDate = (dateStr: string) => {
  const d = new Date(dateStr)
  return d.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

const overdueCount = computed(() => assetStore.myAssets.filter(isOverdue).length)

onMounted(() => {
  assetStore.fetchMyAssets()
})

const openReturnModal = (asset: Asset) => {
  selectedAsset.value = asset
  showTransactionModal.value = true
}

const handleCloseModal = () => {
  showTransactionModal.value = false
  assetStore.fetchMyAssets()
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">My Assets</h1>
        <p class="text-sm text-gray-500 mt-1">내가 현재 대여 중인 자산 목록입니다.</p>
      </div>
      <button
        @click="assetStore.fetchMyAssets()"
        class="flex items-center space-x-2 px-4 py-2 bg-white border border-gray-200 text-gray-600 rounded-xl hover:bg-gray-50 transition-all shadow-sm text-sm font-medium"
      >
        <RefreshCw class="w-4 h-4" />
        <span>새로고침</span>
      </button>
    </div>

    <!-- Overdue Alert Banner -->
    <div v-if="overdueCount > 0" class="flex items-center gap-3 px-5 py-4 bg-red-50 border border-red-200 rounded-2xl text-red-700 shadow-sm">
      <AlertTriangle class="w-5 h-5 flex-shrink-0 text-red-500" />
      <div>
        <p class="font-bold text-sm">반납 기한 초과 자산 {{ overdueCount }}건</p>
        <p class="text-xs text-red-500 mt-0.5">아래 빨간색 항목들은 반납 예정일이 지났습니다. 즉시 반납해 주세요.</p>
      </div>
    </div>

    <!-- Empty State -->
    <div v-if="assetStore.myAssets.length === 0" class="flex flex-col items-center justify-center py-24 bg-white rounded-2xl border border-gray-100 shadow-sm">
      <div class="w-20 h-20 rounded-2xl bg-blue-50 flex items-center justify-center mb-5 shadow-inner">
        <Package class="w-10 h-10 text-blue-300" />
      </div>
      <h3 class="text-lg font-bold text-gray-700 mb-2">대여 중인 자산 없음</h3>
      <p class="text-sm text-gray-400 text-center max-w-xs leading-relaxed">
        현재 대여 중인 자산이 없습니다.<br />
        Assets 메뉴에서 자산을 대여할 수 있습니다.
      </p>
    </div>

    <!-- Asset Cards -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
      <div
        v-for="asset in assetStore.myAssets"
        :key="asset.id"
        :class="[
          'rounded-2xl border shadow-sm transition-all duration-200 flex flex-col overflow-hidden group',
          isOverdue(asset)
            ? 'bg-red-50 border-red-300 hover:border-red-400 hover:shadow-red-100 hover:shadow-md'
            : 'bg-white border-gray-100 hover:border-blue-200 hover:shadow-md'
        ]"
      >
        <!-- Card Top Accent -->
        <div :class="['h-1.5 w-full', isOverdue(asset) ? 'bg-gradient-to-r from-red-500 to-rose-500' : 'bg-gradient-to-r from-blue-500 to-indigo-500']"></div>

        <div class="p-5 flex flex-col flex-1">
          <!-- Badge + Link -->
          <div class="flex items-center justify-between mb-4">
            <span v-if="isOverdue(asset)" class="flex items-center gap-1 px-2.5 py-1 rounded-lg text-[10px] font-bold bg-red-100 text-red-600 uppercase tracking-wider border border-red-200">
              <AlertTriangle class="w-3 h-3" /> 기한 초과
            </span>
            <span v-else class="px-2.5 py-1 rounded-lg text-[10px] font-bold bg-blue-50 text-blue-600 uppercase tracking-wider border border-blue-100">
              대여중
            </span>
            <router-link
              :to="`/assets/${asset.id}`"
              :class="['p-1.5 rounded-lg transition-all', isOverdue(asset) ? 'text-red-300 hover:text-red-600 hover:bg-red-100' : 'text-gray-300 hover:text-blue-500 hover:bg-blue-50']"
              title="자산 상세 보기"
            >
              <ArrowUpRight class="w-4 h-4" />
            </router-link>
          </div>

          <!-- Asset Info -->
          <div class="flex-1 mb-4">
            <h4 :class="['font-bold text-base mb-1 leading-tight transition-colors', isOverdue(asset) ? 'text-red-800 group-hover:text-red-700' : 'text-gray-800 group-hover:text-blue-700']">
              {{ asset.name }}
            </h4>
            <p class="text-xs text-gray-400 font-mono">SN: {{ asset.serialNumber }}</p>
            <p v-if="asset.location" class="text-xs text-gray-400 mt-1">📍 {{ asset.location }}</p>

            <!-- Due Date Badge -->
            <div v-if="asset.dueDate" class="mt-3">
              <div :class="[
                'flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg text-xs font-semibold',
                isOverdue(asset)
                  ? 'bg-red-100 text-red-700 border border-red-200'
                  : daysLeft(asset)! <= 3
                    ? 'bg-orange-50 text-orange-600 border border-orange-200'
                    : 'bg-gray-50 text-gray-500 border border-gray-100'
              ]">
                <Clock class="w-3.5 h-3.5 flex-shrink-0" />
                <span>
                  반납 예정: {{ formatDate(asset.dueDate) }}
                  <template v-if="isOverdue(asset)">
                    ({{ Math.abs(daysLeft(asset)!) }}일 초과)
                  </template>
                  <template v-else-if="daysLeft(asset) === 0">
                    (오늘까지)
                  </template>
                  <template v-else>
                    ({{ daysLeft(asset) }}일 남음)
                  </template>
                </span>
              </div>
            </div>
          </div>

          <!-- Return Button -->
          <button
            @click="openReturnModal(asset)"
            :class="[
              'w-full py-2.5 text-white text-xs font-bold rounded-xl transition-all active:scale-95 flex items-center justify-center space-x-2 shadow-sm',
              isOverdue(asset) ? 'bg-red-600 hover:bg-red-500' : 'bg-slate-900 hover:bg-slate-700'
            ]"
          >
            <ArrowDownLeft class="w-3.5 h-3.5" />
            <span>{{ isOverdue(asset) ? '즉시 반납하기' : '반납하기' }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Transaction Modal -->
    <TransactionModal
      v-if="showTransactionModal && selectedAsset"
      :asset="selectedAsset"
      type="RETURN"
      @close="handleCloseModal"
    />
  </div>
</template>

