<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Camera, X, RefreshCw, CameraOff } from 'lucide-vue-next'

const emit = defineEmits<{
  (e: 'capture', file: File): void
  (e: 'close'): void
}>()

const videoRef = ref<HTMLVideoElement | null>(null)
const canvasRef = ref<HTMLCanvasElement | null>(null)
const stream = ref<MediaStream | null>(null)
const error = ref<string | null>(null)
const isLoading = ref(true)

const startCamera = async () => {
  error.value = null
  isLoading.value = true
  try {
    const constraints = {
      video: {
        facingMode: 'environment', // Use back camera by default
        width: { ideal: 1280 },
        height: { ideal: 720 }
      }
    }
    
    stream.value = await navigator.mediaDevices.getUserMedia(constraints)
    if (videoRef.value) {
      videoRef.value.srcObject = stream.value
    }
  } catch (err: any) {
    console.error('Camera access failed:', err)
    error.value = '카메라 접근에 실패했습니다. 권한을 확인해 주세요.'
  } finally {
    isLoading.value = false
  }
}

const stopCamera = () => {
  if (stream.value) {
    stream.value.getTracks().forEach(track => track.stop())
    stream.value = null
  }
}

const capturePhoto = () => {
  if (!videoRef.value || !canvasRef.value) return

  const video = videoRef.value
  const canvas = canvasRef.value
  const context = canvas.getContext('2d')
  
  if (!context) return

  // Match canvas size to video stream
  canvas.width = video.videoWidth
  canvas.height = video.videoHeight
  
  // Draw current frame to canvas
  context.drawImage(video, 0, 0, canvas.width, canvas.height)
  
  // Convert canvas to blob/file
  canvas.toBlob((blob) => {
    if (blob) {
      const file = new File([blob], `id_capture_${Date.now()}.jpg`, { type: 'image/jpeg' })
      emit('capture', file)
    }
  }, 'image/jpeg', 0.9)
}

onMounted(() => {
  startCamera()
})

onUnmounted(() => {
  stopCamera()
})
</script>

<template>
  <div class="fixed inset-0 z-[60] bg-black flex flex-col items-center justify-center p-4">
    <!-- Header -->
    <div class="absolute top-0 left-0 w-full p-6 flex justify-between items-center z-10 bg-gradient-to-b from-black/60 to-transparent">
      <h3 class="text-white font-black text-sm uppercase tracking-widest flex items-center gap-2">
        <Camera class="w-4 h-4" /> ID Card Scanner
      </h3>
      <button @click="emit('close')" class="p-2 bg-white/10 hover:bg-white/20 text-white rounded-full transition-colors">
        <X class="w-6 h-6" />
      </button>
    </div>

    <!-- Video Preview -->
    <div class="relative w-full max-w-2xl aspect-[4/3] bg-slate-900 rounded-3xl overflow-hidden shadow-2xl border border-white/10">
      <video 
        ref="videoRef" 
        autoplay 
        playsinline 
        class="w-full h-full object-cover"
      ></video>

      <!-- Overlay Guide -->
      <div class="absolute inset-0 border-[30px] border-black/40 pointer-events-none flex items-center justify-center">
        <div class="w-[85%] h-[60%] border-2 border-dashed border-white/60 rounded-2xl relative">
          <div class="absolute -top-10 left-0 w-full text-center">
            <p class="text-[10px] font-black text-white uppercase tracking-widest bg-black/40 px-3 py-1 rounded-full inline-block">Align ID card within the frame</p>
          </div>
          <!-- Corners -->
          <div class="absolute -top-1 -left-1 w-6 h-6 border-t-4 border-l-4 border-blue-500"></div>
          <div class="absolute -top-1 -right-1 w-6 h-6 border-t-4 border-r-4 border-blue-500"></div>
          <div class="absolute -bottom-1 -left-1 w-6 h-6 border-b-4 border-l-4 border-blue-500"></div>
          <div class="absolute -bottom-1 -right-1 w-6 h-6 border-b-4 border-r-4 border-blue-500"></div>
        </div>
      </div>

      <!-- Loading/Error States -->
      <div v-if="isLoading" class="absolute inset-0 bg-slate-900 flex flex-center items-center justify-center">
         <div class="text-center space-y-4">
           <RefreshCw class="w-10 h-10 text-blue-500 animate-spin mx-auto" />
           <p class="text-xs text-slate-400 font-bold uppercase tracking-widest">Initializing Camera...</p>
         </div>
      </div>

      <div v-if="error" class="absolute inset-0 bg-slate-900 flex items-center justify-center p-8 text-center">
         <div class="space-y-4">
           <CameraOff class="w-12 h-12 text-rose-500 mx-auto" />
           <p class="text-sm text-white font-bold">{{ error }}</p>
           <button @click="startCamera" class="px-6 py-2 bg-blue-600 text-white text-xs font-bold rounded-xl mt-4">Try Again</button>
         </div>
      </div>
    </div>

    <!-- Hidden Canvas for Capture -->
    <canvas ref="canvasRef" style="display: none"></canvas>

    <!-- Controls -->
    <div v-if="!error && !isLoading" class="mt-12 flex flex-col items-center gap-6">
      <button 
        @click="capturePhoto"
        class="w-20 h-20 bg-white rounded-full flex items-center justify-center shadow-2xl active:scale-90 transition-transform relative group"
      >
        <div class="w-16 h-16 border-4 border-slate-100 rounded-full group-hover:scale-105 transition-transform"></div>
        <Camera class="absolute w-8 h-8 text-slate-900" />
      </button>
      <p class="text-slate-400 text-[10px] font-bold uppercase tracking-[0.2em]">Press to capture ID card</p>
    </div>
  </div>
</template>

<style scoped>
/* Any additional custom styles if needed */
</style>
