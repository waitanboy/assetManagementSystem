<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Mail, ArrowLeft, Send, CheckCircle2 } from 'lucide-vue-next'
import api from '../api'

const router = useRouter()
const email = ref('')
const isLoading = ref(false)
const isSent = ref(false)
const error = ref('')

const handleForgot = async () => {
  error.value = ''
  isLoading.value = true
  try {
    await api.post('/api/auth/forgot-password', { email: email.value })
    isSent.value = true
  } catch (err: any) {
    error.value = err.response?.data || '비밀번호 초기화 요청에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-slate-50 flex items-center justify-center p-4">
    <div class="w-full max-w-md">
      <!-- Back to Login -->
      <button 
        @click="router.push('/login')" 
        class="mb-8 flex items-center gap-2 text-slate-400 hover:text-slate-600 font-bold text-xs uppercase tracking-widest transition-colors group"
      >
        <ArrowLeft class="w-4 h-4 group-hover:-translate-x-1 transition-transform" />
        Back to Login
      </button>

      <div class="bg-white rounded-[2.5rem] shadow-2xl shadow-slate-200/50 overflow-hidden border border-slate-100 animate-in fade-in slide-in-from-bottom-8 duration-700">
        <div class="p-10">
          <div v-if="!isSent">
            <div class="w-16 h-16 bg-blue-50 rounded-2xl flex items-center justify-center mb-8">
              <Mail class="w-8 h-8 text-blue-600" />
            </div>

            <h1 class="text-3xl font-black text-slate-900 tracking-tight mb-2">Forgot Password?</h1>
            <p class="text-slate-500 font-medium leading-relaxed mb-8">
              가입하신 이메일 주소를 입력해 주세요. 비밀번호 재설정 링크를 보내드립니다.
            </p>

            <form @submit.prevent="handleForgot" class="space-y-6">
              <div class="space-y-2">
                <label class="text-[10px] font-black uppercase tracking-widest text-slate-400 ml-1">Email Address</label>
                <div class="relative group">
                  <Mail class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-300 group-focus-within:text-blue-500 transition-colors" />
                  <input 
                    v-model="email"
                    type="email" 
                    required
                    placeholder="name@company.com"
                    class="w-full pl-12 pr-4 py-4 bg-slate-50 border border-slate-100 rounded-2xl focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 outline-none transition-all font-medium"
                  />
                </div>
              </div>

              <div v-if="error" class="p-4 bg-rose-50 border border-rose-100 rounded-2xl text-rose-600 text-sm font-bold animate-in shake duration-500">
                {{ error }}
              </div>

              <button 
                type="submit"
                :disabled="isLoading"
                class="w-full py-4 bg-slate-900 text-white font-black rounded-2xl shadow-xl shadow-slate-900/20 hover:bg-slate-800 active:scale-[0.98] transition-all disabled:opacity-50 flex items-center justify-center gap-2 group"
              >
                <span v-if="isLoading" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                <template v-else>
                  Send Reset Link
                  <Send class="w-4 h-4 group-hover:translate-x-1 group-hover:-translate-y-1 transition-transform" />
                </template>
              </button>
            </form>
          </div>

          <div v-else class="text-center animate-in zoom-in-95 duration-500">
            <div class="w-20 h-20 bg-emerald-50 rounded-full flex items-center justify-center mx-auto mb-8">
              <CheckCircle2 class="w-10 h-10 text-emerald-600" />
            </div>
            <h2 class="text-3xl font-black text-slate-900 tracking-tight mb-4">Email Sent!</h2>
            <p class="text-slate-500 font-medium leading-relaxed mb-8">
              <span class="font-bold text-slate-900">{{ email }}</span>(으)로 비밀번호 재설정 링크가 전송되었습니다. 이메일을 확인해 주세요.
            </p>
            <button 
              @click="router.push('/login')"
              class="w-full py-4 bg-slate-100 text-slate-900 font-black rounded-2xl hover:bg-slate-200 transition-colors"
            >
              Return to Login
            </button>
          </div>
        </div>

        <!-- Decorative Footer -->
        <div class="px-10 py-6 bg-slate-50 border-t border-slate-100 flex items-center justify-center gap-2">
          <span class="w-1.5 h-1.5 bg-blue-500 rounded-full animate-pulse"></span>
          <p class="text-[10px] font-black text-slate-400 uppercase tracking-[0.2em]">Secure Recovery System</p>
        </div>
      </div>

      <p class="text-center mt-8 text-slate-400 text-xs font-medium">
        이메일이 도착하지 않았나요? 스팸함을 확인해 보거나 잠시 후 다시 시도해 주세요.
      </p>
    </div>
  </div>
</template>

<style scoped>
.animate-in {
  animation-fill-mode: both;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.shake {
  animation: shake 0.5s ease-in-out;
}
</style>
