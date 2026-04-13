<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Clock, CheckCircle, XCircle, User, Package, Calendar, FileText, Check, X, Search, Info, Camera } from 'lucide-vue-next'
import { useRequestStore, type RentalRequest } from '../stores/requestStore'
import { useAuthStore } from '../stores/authStore'
import SignaturePad from '../components/common/SignaturePad.vue'
import CameraCapture from '../components/common/CameraCapture.vue'
import api from '../api'

const requestStore = useRequestStore()
const authStore = useAuthStore()

const activeTab = ref<'PENDING' | 'MY' | 'ALL'>(authStore.isAdmin ? 'PENDING' : 'MY')
const searchQuery = ref('')
const processingId = ref<number | null>(null)
const showApprovalModal = ref(false)
const showRejectModal = ref(false)
const selectedRequest = ref<RentalRequest | null>(null)
const signatureData = ref<string | null>(null)
const rejectReason = ref('')

// OCR State
const ocrStep = ref<'UPLOAD' | 'VERIFY' | 'DONE'>('UPLOAD')
const ocrLoading = ref(false)
const ocrResult = ref<{ name: string, birthDate: string } | null>(null)
const verificationError = ref<string | null>(null)
const idCardFile = ref<File | null>(null)
const showCamera = ref(false)

onMounted(() => {
  fetchData()
})

const fetchData = () => {
  if (activeTab.value === 'PENDING') requestStore.fetchPendingRequests()
  if (activeTab.value === 'MY') requestStore.fetchMyRequests()
  if (activeTab.value === 'ALL') requestStore.fetchAllRequests()
}

const filteredRequests = computed(() => {
  const list = activeTab.value === 'PENDING' ? requestStore.pendingRequests :
               activeTab.value === 'MY' ? requestStore.myRequests :
               requestStore.allRequests
  
  if (!searchQuery.value) return list
  
  const q = searchQuery.value.toLowerCase()
  return list.filter(r => 
    r.assetName?.toLowerCase().includes(q) || 
    r.assetSerial?.toLowerCase().includes(q) || 
    r.userEmail?.toLowerCase().includes(q)
  )
})

const openApprove = (req: RentalRequest) => {
  selectedRequest.value = req
  signatureData.value = null
  ocrStep.value = req.useOcr ? 'UPLOAD' : 'DONE'
  ocrResult.value = null
  verificationError.value = null
  idCardFile.value = null
  showApprovalModal.value = true
}

const openReject = (req: RentalRequest) => {
  selectedRequest.value = req
  rejectReason.value = ''
  showRejectModal.value = true
}

const handleApprove = async () => {
  if (!selectedRequest.value?.id || !signatureData.value) return
  
  processingId.value = selectedRequest.value.id
  try {
    const ocrDataStr = ocrResult.value ? JSON.stringify(ocrResult.value) : undefined
    await requestStore.approveRequest(selectedRequest.value.id, signatureData.value, ocrDataStr)
    showApprovalModal.value = false
  } catch (err) {
    alert('Approval failed')
  } finally {
    processingId.value = null
  }
}

const handleOcrUpload = async (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return

  idCardFile.value = file
  ocrLoading.value = true
  verificationError.value = null

  const formData = new FormData()
  formData.append('file', file)

  try {
    // We call the OCR endpoint in AIController
    const response = await api.post('/ai/ocr', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    // The response is { result: "{\"name\": \"...\", ...}" }
    const parsed = JSON.parse(response.data.result)
    ocrResult.value = parsed
    
    // Auto-match verification
    if (parsed.name !== selectedRequest.value?.userName) {
      verificationError.value = `Name mismatch: Extracted [${parsed.name}] vs Registered [${selectedRequest.value?.userName}]`
    }
    
    ocrStep.value = 'VERIFY'
  } catch (err) {
    alert('OCR processing failed. Please try again or use a clearer photo.')
  } finally {
    ocrLoading.value = false
  }
}

const handleCameraCapture = async (file: File) => {
  showCamera.value = false
  idCardFile.value = file
  ocrLoading.value = true
  verificationError.value = null

  const formData = new FormData()
  formData.append('file', file)

  try {
    const response = await api.post('/ai/ocr', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    const parsed = JSON.parse(response.data.result)
    ocrResult.value = parsed
    
    if (parsed.name !== selectedRequest.value?.userName) {
      verificationError.value = `Name mismatch: Extracted [${parsed.name}] vs Registered [${selectedRequest.value?.userName}]`
    }
    
    ocrStep.value = 'VERIFY'
  } catch (err) {
    alert('OCR processing failed. Please try again or use a clearer photo.')
  } finally {
    ocrLoading.value = false
  }
}

const confirmIdentity = () => {
  ocrStep.value = 'DONE'
}

const handleReject = async () => {
  if (!selectedRequest.value?.id || !rejectReason.value) return
  
  processingId.value = selectedRequest.value.id
  try {
    await requestStore.rejectRequest(selectedRequest.value.id, rejectReason.value)
    showRejectModal.value = false
  } catch (err) {
    alert('Rejection failed')
  } finally {
    processingId.value = null
  }
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'PENDING': return 'bg-amber-50 text-amber-600 border-amber-100'
    case 'APPROVED': return 'bg-emerald-50 text-emerald-600 border-emerald-100'
    case 'REJECTED': return 'bg-rose-50 text-rose-600 border-rose-100'
    default: return 'bg-slate-50 text-slate-600 border-slate-100'
  }
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<template>
  <div class="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
    <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
      <div>
        <h2 class="text-2xl font-black text-slate-800 tracking-tight">Rental Requests</h2>
        <p class="text-xs text-slate-400 font-bold uppercase tracking-widest mt-1">Manage asset distribution workflow</p>
      </div>

      <div class="flex bg-slate-100 p-1 rounded-xl">
        <button 
          v-if="authStore.isAdmin"
          @click="activeTab = 'PENDING'; fetchData()" 
          :class="['px-4 py-2 rounded-lg text-sm font-bold transition-all', activeTab === 'PENDING' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-700']"
        >
          Pending
        </button>
        <button 
          @click="activeTab = 'MY'; fetchData()" 
          :class="['px-4 py-2 rounded-lg text-sm font-bold transition-all', activeTab === 'MY' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-700']"
        >
          My Requests
        </button>
        <button 
          v-if="authStore.isAdmin"
          @click="activeTab = 'ALL'; fetchData()" 
          :class="['px-4 py-2 rounded-lg text-sm font-bold transition-all', activeTab === 'ALL' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-700']"
        >
          All History
        </button>
      </div>
    </div>

    <!-- Search -->
    <div class="relative max-w-md">
      <span class="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-400">
        <Search class="w-4 h-4" />
      </span>
      <input
        v-model="searchQuery"
        type="text"
        placeholder="Filter by asset name, user..."
        class="w-full pl-10 pr-4 py-2 bg-white border border-slate-100 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all"
      />
    </div>

    <!-- Empty State -->
    <div v-if="filteredRequests.length === 0" class="bg-white rounded-3xl border border-dashed border-slate-200 p-20 text-center">
      <div class="w-20 h-20 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-4 text-slate-200">
        <Clock class="w-10 h-10" />
      </div>
      <h3 class="text-lg font-bold text-slate-400">No requests found</h3>
      <p class="text-sm text-slate-400">Everything is caught up at the moment.</p>
    </div>

    <!-- List -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div 
        v-for="req in filteredRequests" 
        :key="req.id"
        class="group bg-white rounded-3xl border border-slate-50 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 overflow-hidden"
      >
        <div class="p-6 space-y-4">
          <div class="flex justify-between items-start">
            <span :class="['px-2.5 py-1 rounded-full text-[10px] font-black tracking-widest uppercase border', getStatusColor(req.status)]">
              {{ req.status }}
            </span>
            <span class="text-[10px] font-bold text-slate-300 uppercase tracking-widest">REQ#{{ req.id }}</span>
          </div>

          <div class="flex gap-4">
            <div class="w-12 h-12 bg-slate-50 rounded-2xl flex items-center justify-center text-slate-400 group-hover:bg-slate-900 group-hover:text-white transition-colors duration-300">
              <Package class="w-6 h-6" />
            </div>
            <div>
              <h4 class="font-bold text-slate-800 leading-tight">{{ req.assetName }}</h4>
              <p class="text-[10px] font-mono text-slate-400 mt-0.5">{{ req.assetSerial }}</p>
            </div>
          </div>

          <div class="space-y-2 pt-2 border-t border-slate-50">
            <div class="flex items-center gap-2 text-xs">
              <User class="w-3.5 h-3.5 text-slate-400" />
              <span class="font-bold text-slate-600">{{ req.userEmail }}</span>
            </div>
            <div class="flex items-center gap-2 text-xs">
              <Calendar class="w-3.5 h-3.5 text-slate-400" />
              <span class="text-slate-500">Planned: <span class="font-bold text-slate-700">{{ req.plannedDueDate }}</span></span>
            </div>
            <div v-if="req.purpose" class="pt-2">
              <div class="flex items-center gap-1.5 mb-1.5">
                <FileText class="w-3.5 h-3.5 text-slate-400" />
                <span class="text-[10px] font-black text-slate-400 uppercase tracking-widest">Purpose of Use</span>
              </div>
              <p class="text-[11px] text-slate-500 bg-slate-50 p-3 rounded-xl italic leading-relaxed">"{{ req.purpose }}"</p>
            </div>
            
            <div v-if="req.status === 'REJECTED' && req.rejectReason" class="pt-2">
               <div class="flex items-center gap-1.5 mb-1.5 text-rose-500">
                <Info class="w-3.5 h-3.5" />
                <span class="text-[10px] font-black uppercase tracking-widest">Reject Reason</span>
              </div>
              <p class="text-[11px] text-rose-600 bg-rose-50 p-3 rounded-xl leading-relaxed">{{ req.rejectReason }}</p>
            </div>
          </div>
        </div>

        <!-- Admin Actions -->
        <div v-if="authStore.isAdmin && req.status === 'PENDING'" class="p-4 bg-slate-50 flex gap-2">
          <button 
            @click="openReject(req)"
            class="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 bg-white text-rose-600 border border-slate-200 rounded-xl text-xs font-bold hover:bg-rose-50 hover:border-rose-100 transition-all active:scale-95"
          >
            <X class="w-3.5 h-3.5" /> Reject
          </button>
          <button 
            @click="openApprove(req)"
            class="flex-[2] flex items-center justify-center gap-2 px-4 py-2.5 bg-slate-900 text-white rounded-xl text-xs font-bold hover:bg-slate-800 shadow-sm transition-all active:scale-95"
          >
            <Check class="w-3.5 h-3.5" /> Approve & Handover
          </button>
        </div>
        <div v-else class="p-4 bg-slate-50 text-center">
          <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">
            Requested {{ formatDate(req.requestDate) }}
          </p>
          <p v-if="req.status !== 'PENDING'" class="text-[9px] text-slate-300 mt-1 italic">
             Processed by {{ req.processedByEmail }} at {{ formatDate(req.processDate) }}
          </p>
        </div>
      </div>
    </div>

    <!-- Approval Modal (Signature) -->
    <div v-if="showApprovalModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
      <div class="bg-white rounded-3xl shadow-2xl w-full max-w-lg overflow-hidden animate-in zoom-in-95">
        <div class="px-8 py-6 bg-slate-50 border-b border-slate-100 flex justify-between items-center">
          <div>
            <h2 class="text-xl font-black text-slate-800 tracking-tight">Rental Approval</h2>
            <p class="text-xs text-slate-400 font-bold uppercase tracking-widest mt-1">Final Handover Confirmation</p>
          </div>
          <button @click="showApprovalModal = false" class="p-2 text-slate-400 hover:text-slate-600">
            <X class="w-6 h-6" />
          </button>
        </div>
        
        <div class="p-8 space-y-6">
          <!-- Step Trace -->
          <div v-if="selectedRequest?.useOcr" class="flex items-center justify-center gap-4 mb-4">
             <div :class="['w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold', ocrStep === 'UPLOAD' || ocrStep === 'VERIFY' ? 'bg-slate-900 text-white' : 'bg-emerald-100 text-emerald-600']">
                <Check v-if="ocrStep === 'DONE'" class="w-4 h-4" />
                <span v-else>1</span>
             </div>
             <div class="w-12 h-0.5 bg-slate-100"></div>
             <div :class="['w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold', ocrStep === 'DONE' ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-400']">2</div>
          </div>

          <!-- Content: OCR Upload -->
          <div v-if="ocrStep === 'UPLOAD'" class="space-y-4 text-center">
            <div class="p-10 border-2 border-dashed border-slate-200 rounded-3xl bg-slate-50 hover:bg-slate-100 transition-colors cursor-pointer relative">
               <input type="file" @change="handleOcrUpload" class="absolute inset-0 opacity-0 cursor-pointer" accept="image/*" />
               <div v-if="ocrLoading" class="animate-pulse space-y-3">
                  <div class="w-12 h-12 bg-blue-100 rounded-full mx-auto flex items-center justify-center text-blue-600">
                    <Clock class="w-6 h-6 animate-spin" />
                  </div>
                  <p class="text-sm font-bold text-slate-600">Extracting Identity Info...</p>
               </div>
               <div v-else class="space-y-3">
                  <div class="flex items-center justify-center gap-6">
                    <div @click.stop class="relative group">
                      <input type="file" @change="handleOcrUpload" class="absolute inset-0 opacity-0 cursor-pointer" accept="image/*" />
                      <div class="w-14 h-14 bg-slate-100 rounded-2xl flex items-center justify-center text-slate-500 group-hover:bg-blue-600 group-hover:text-white transition-all shadow-sm">
                        <FileText class="w-7 h-7" />
                      </div>
                      <p class="text-[9px] font-black uppercase tracking-tighter mt-2 text-slate-400 group-hover:text-blue-600">Browse</p>
                    </div>
                    
                    <div @click.stop="showCamera = true" class="relative group cursor-pointer">
                      <div class="w-14 h-14 bg-slate-100 rounded-2xl flex items-center justify-center text-slate-500 group-hover:bg-blue-600 group-hover:text-white transition-all shadow-sm">
                        <Camera class="w-7 h-7" />
                      </div>
                      <p class="text-[9px] font-black uppercase tracking-tighter mt-2 text-slate-400 group-hover:text-blue-600">Take Photo</p>
                    </div>
                  </div>
                  <p class="text-xs font-bold text-slate-600 mt-4">Select or capture ID card</p>
                  <p class="text-[10px] text-slate-400 font-medium">Verify recipient Identity</p>
               </div>
            </div>
          </div>

          <!-- Content: OCR Verify -->
          <div v-else-if="ocrStep === 'VERIFY'" class="space-y-6">
            <div class="bg-slate-50 p-6 rounded-2xl border border-slate-100 divide-y divide-slate-200/50">
               <div class="pb-4 flex justify-between items-center">
                  <span class="text-xs font-bold text-slate-400 uppercase tracking-widest">Registered Name</span>
                  <span class="font-black text-slate-900">{{ selectedRequest?.userName }}</span>
               </div>
               <div class="pt-4 flex justify-between items-center">
                  <span class="text-xs font-bold text-slate-400 uppercase tracking-widest">Extracted Name</span>
                  <span :class="['font-black', verificationError ? 'text-rose-600' : 'text-emerald-600 text-lg underline decoration-wavy decoration-emerald-200 underline-offset-4']">
                    {{ ocrResult?.name }}
                  </span>
               </div>
            </div>

            <div v-if="verificationError" class="p-4 bg-rose-50 border border-rose-100 rounded-2xl flex items-start gap-3">
               <XCircle class="w-5 h-5 text-rose-500 shrink-0 mt-0.5" />
               <p class="text-xs text-rose-600 font-bold leading-relaxed">{{ verificationError }}</p>
            </div>

            <div v-else class="p-4 bg-emerald-50 border border-emerald-100 rounded-2xl flex items-start gap-3">
               <CheckCircle class="w-5 h-5 text-emerald-500 shrink-0 mt-0.5" />
               <p class="text-xs text-emerald-600 font-bold leading-relaxed">Identity Confirmed. The recipient matches the requestor.</p>
            </div>

            <div class="flex gap-3">
               <button @click="ocrStep = 'UPLOAD'" class="flex-1 py-3 text-slate-500 font-bold text-sm">Retry</button>
               <button @click="confirmIdentity" class="flex-[2] py-3 bg-slate-900 text-white font-bold rounded-xl active:scale-95">Next: Signature</button>
            </div>
          </div>

          <!-- Content: Signature (Final Step) -->
          <div v-else class="space-y-6">
            <div class="bg-slate-50 p-4 rounded-2xl border border-slate-100 flex items-center gap-4">
              <div class="w-12 h-12 bg-white rounded-xl flex items-center justify-center text-slate-400 shadow-sm">
                  <Package class="w-6 h-6" />
              </div>
              <div>
                <p class="text-[10px] font-black text-slate-400 uppercase tracking-widest">Handing Over</p>
                <p class="font-bold text-slate-800">{{ selectedRequest?.assetName }}</p>
              </div>
            </div>

            <div class="space-y-4">
              <div class="flex justify-between items-end">
                <h4 class="text-xs font-black text-slate-500 uppercase tracking-wider">Recipient's Signature</h4>
                <div v-if="selectedRequest?.useOcr" class="flex items-center gap-1 text-[10px] text-emerald-600 font-black uppercase tracking-tighter">
                  <CheckCircle class="w-3 h-3" /> Identity Verified
                </div>
              </div>
              <SignaturePad @change="(data) => signatureData = data" />
            </div>

            <div class="flex gap-3 pt-2">
              <button @click="showApprovalModal = false" class="flex-1 py-3 border border-slate-200 text-slate-600 font-bold rounded-xl active:scale-95">Cancel</button>
              <button 
                @click="handleApprove" 
                :disabled="!signatureData || processingId !== null"
                class="flex-[2] py-3 bg-emerald-600 text-white font-bold rounded-xl shadow-lg shadow-emerald-100 hover:bg-emerald-700 disabled:opacity-50 active:scale-95"
              >
                {{ processingId ? 'Processing...' : 'Confirm Approval' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Reject Modal -->
    <div v-if="showRejectModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
      <div class="bg-white rounded-3xl shadow-2xl w-full max-w-md overflow-hidden animate-in zoom-in-95">
        <div class="px-8 py-6 bg-rose-50 border-b border-rose-100 flex justify-between items-center">
          <h2 class="text-xl font-black text-rose-800 tracking-tight text-center">Reject Request</h2>
          <button @click="showRejectModal = false" class="p-2 text-rose-400 hover:text-rose-600"><X class="w-6 h-6" /></button>
        </div>
        <div class="p-8 space-y-6">
          <div class="space-y-2">
            <label class="text-xs font-black text-slate-500 uppercase tracking-wider">Reason for Rejection</label>
            <textarea 
              v-model="rejectReason" 
              class="w-full p-4 bg-slate-50 border border-slate-100 rounded-2xl focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500 outline-none resize-none" 
              rows="4"
              placeholder="Ex: This asset is scheduled for maintenance."
            ></textarea>
          </div>
          <div class="flex gap-3">
            <button @click="showRejectModal = false" class="flex-1 py-3 border border-slate-200 text-slate-600 font-bold rounded-xl">Cancel</button>
            <button 
              @click="handleReject" 
              :disabled="!rejectReason || processingId !== null"
              class="flex-[2] py-3 bg-rose-600 text-white font-bold rounded-xl shadow-lg shadow-rose-100 hover:bg-rose-700 disabled:opacity-50 active:scale-95"
            >
               Confirm Rejection
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Camera Overlay -->
    <CameraCapture 
      v-if="showCamera" 
      @capture="handleCameraCapture" 
      @close="showCamera = false" 
    />
  </div>
</template>
