<script setup lang="ts">
import { ref } from 'vue'
import { X, Calendar, FileText, Send } from 'lucide-vue-next'
import { useAssetStore, type Asset } from '../../stores/assetStore'
import { useRequestStore } from '../../stores/requestStore'

const props = defineProps<{
  asset: Asset
}>()

const emit = defineEmits(['close'])

const assetStore = useAssetStore()
const requestStore = useRequestStore()

const plannedDueDate = ref('')
const purpose = ref('')
const isSubmitting = ref(false)

const handleSubmit = async () => {
  if (!plannedDueDate.value || !purpose.value) return
  
  isSubmitting.value = true
  try {
    await requestStore.createRequest({
      assetId: props.asset.id,
      plannedDueDate: plannedDueDate.value,
      purpose: purpose.value
    })
    // Also update asset store to reflect REQUESTED status immediately
    await assetStore.fetchAssets()
    emit('close')
  } catch (err) {
    alert('Failed to submit request')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-300">
    <div class="bg-white rounded-3xl shadow-2xl w-full max-w-lg overflow-hidden animate-in zoom-in-95 duration-300">
      <!-- Header -->
      <div class="px-8 py-6 bg-slate-50 border-b border-slate-100 flex justify-between items-center">
        <div>
          <h2 class="text-xl font-black text-slate-800 tracking-tight">Rental Request</h2>
          <p class="text-xs text-slate-400 font-bold uppercase tracking-widest mt-1">{{ asset.name }}</p>
        </div>
        <button @click="$emit('close')" class="p-2 hover:bg-white rounded-xl transition-colors text-slate-400 hover:text-slate-600">
          <X class="w-6 h-6" />
        </button>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="p-8 space-y-6">
        <div class="space-y-4">
          <!-- Due Date -->
          <div class="space-y-2">
            <label class="text-xs font-black text-slate-500 uppercase tracking-wider flex items-center gap-2">
              <Calendar class="w-3.5 h-3.5" /> Planned Due Date
            </label>
            <input
              v-model="plannedDueDate"
              type="date"
              required
              class="w-full px-4 py-3 bg-slate-50 border border-slate-100 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all font-medium"
            />
          </div>

          <!-- Purpose -->
          <div class="space-y-2">
            <label class="text-xs font-black text-slate-500 uppercase tracking-wider flex items-center gap-2">
              <FileText class="w-3.5 h-3.5" /> Purpose of Use
            </label>
            <textarea
              v-model="purpose"
              required
              rows="4"
              placeholder="Please describe why you need this asset..."
              class="w-full px-4 py-3 bg-slate-50 border border-slate-100 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all font-medium resize-none"
            ></textarea>
          </div>
        </div>

        <!-- Warning -->
        <div class="p-4 bg-blue-50 rounded-2xl border border-blue-100">
          <p class="text-[11px] text-blue-600 font-medium leading-relaxed">
            Your request will be reviewed by an administrator. Once approved, you will be notified and can proceed with the handover.
          </p>
        </div>

        <!-- Actions -->
        <div class="flex gap-3 pt-2">
          <button
            type="button"
            @click="$emit('close')"
            class="flex-1 px-6 py-3 border border-slate-200 text-slate-600 font-bold rounded-xl hover:bg-slate-50 transition-all active:scale-95"
          >
            Cancel
          </button>
          <button
            type="submit"
            :disabled="isSubmitting"
            class="flex-[2] px-6 py-3 bg-slate-900 text-white font-bold rounded-xl hover:bg-slate-800 transition-all shadow-lg shadow-slate-200 flex items-center justify-center gap-2 disabled:opacity-50 active:scale-95"
          >
            <Send v-if="!isSubmitting" class="w-4 h-4" />
            <div v-else class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
            {{ isSubmitting ? 'Submitting...' : 'Submit Request' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
