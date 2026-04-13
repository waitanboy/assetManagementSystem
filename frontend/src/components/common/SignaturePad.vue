<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import SignaturePad from 'signature_pad'
import { Eraser, RotateCcw } from 'lucide-vue-next'

const props = defineProps<{
  width?: number
  height?: number
}>()

const emit = defineEmits<{
  (e: 'change', data: string | null): void
}>()

const canvasRef = ref<HTMLCanvasElement | null>(null)
let signaturePad: SignaturePad | null = null

const initPad = () => {
  if (canvasRef.value) {
    signaturePad = new SignaturePad(canvasRef.value, {
      backgroundColor: 'rgb(248, 250, 252)', // slate-50
      penColor: 'rgb(15, 23, 42)',         // slate-900
    })
    
    signaturePad.addEventListener('endStroke', () => {
      const data = signaturePad?.isEmpty() ? null : signaturePad?.toDataURL()
      emit('change', data ?? null)
    })
    
    resizeCanvas()
  }
}

const resizeCanvas = () => {
  if (canvasRef.value) {
    const ratio = Math.max(window.devicePixelRatio || 1, 1)
    canvasRef.value.width = canvasRef.value.offsetWidth * ratio
    canvasRef.value.height = canvasRef.value.offsetHeight * ratio
    canvasRef.value.getContext('2d')?.scale(ratio, ratio)
    signaturePad?.clear() // Clear on resize to avoid distortion
  }
}

const clear = () => {
  signaturePad?.clear()
  emit('change', null)
}

const undo = () => {
  const data = signaturePad?.toData()
  if (data && data.length > 0) {
    data.pop()
    signaturePad?.fromData(data)
    const dataUrl = signaturePad?.isEmpty() ? null : signaturePad?.toDataURL()
    emit('change', dataUrl ?? null)
  }
}

onMounted(() => {
  initPad()
  window.addEventListener('resize', resizeCanvas)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCanvas)
})

defineExpose({
  clear,
  undo,
  isEmpty: () => signaturePad?.isEmpty() ?? true,
  getDataUrl: () => signaturePad?.toDataURL() ?? null
})
</script>

<template>
  <div class="space-y-2">
    <div class="relative w-full aspect-[2/1] bg-slate-50 rounded-xl border-2 border-dashed border-slate-200 overflow-hidden group">
      <canvas ref="canvasRef" class="w-full h-full touch-none cursor-crosshair"></canvas>
      
      <div class="absolute bottom-3 right-3 flex gap-2 opacity-10 group-hover:opacity-100 transition-opacity">
        <button 
          type="button"
          @click="undo"
          class="p-2 bg-white rounded-lg shadow-sm border border-slate-200 text-slate-500 hover:text-slate-900 hover:bg-slate-50 transition-all"
          title="Undo"
        >
          <RotateCcw class="w-4 h-4" />
        </button>
        <button 
          type="button"
          @click="clear"
          class="p-2 bg-white rounded-lg shadow-sm border border-slate-200 text-slate-500 hover:text-red-600 hover:bg-red-50 transition-all"
          title="Clear"
        >
          <Eraser class="w-4 h-4" />
        </button>
      </div>
      
      <div class="absolute top-3 left-3 pointer-events-none">
        <span class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Digital Signature Pad</span>
      </div>
    </div>
    <p class="text-[10px] text-slate-400 text-center italic">Please sign inside the box above using your mouse or touch screen.</p>
  </div>
</template>
