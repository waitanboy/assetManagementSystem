<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Lock, CheckCircle2, AlertCircle, Eye, EyeOff } from 'lucide-vue-next'
import api from '../api'

const router = useRouter()
const route = useRoute()
const token = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const showPassword = ref(false)
const isLoading = ref(false)
const isSuccess = ref(false)
const error = ref('')

onMounted(() => {
  const queryToken = route.query.token
  if (!queryToken) {
    error.value = '유효하지 않은 접근입니다. 이메일의 링크를 다시 확인해 주세요.'
  } else {
    token.value = queryToken as string
  }
})

const handleReset = async () => {
  if (newPassword.value !== confirmPassword.value) {
    error.value = '비밀번호가 일치하지 않습니다.'
    return
  }

  error.value = ''
  isLoading.value = true
  try {
    await api.post('/api/auth/reset-password', {
      token: token.value,
      newPassword: newPassword.value
    })
    isSuccess.value = true
  } catch (err: any) {
    error.value = err.response?.data || '비밀번호 재설정에 실패했습니다. 토큰이 만료되었을 수 있습니다.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-slate-50 flex items-center justify-center p-4">
    <div class="w-full max-w-md">
      <div class="bg-white rounded-[2.5rem] shadow-2xl shadow-slate-200/50 overflow-hidden border border-slate-100 animate-in fade-in slide-in-from-bottom-8 duration-700">
        <div class="p-10">
          <div v-if="!isSuccess">
            <div class="w-16 h-16 bg-blue-50 rounded-2xl flex items-center justify-center mb-8">
              <Lock class="w-8 h-8 text-blue-600" />
            </div>

            <h1 class="text-3xl font-black text-slate-900 tracking-tight mb-2">Reset Password</h1>
            <p class="text-slate-500 font-medium leading-relaxed mb-8">
              새로운 비밀번호를 설정해 주세요. 안전을 위해 8자 이상의 강력한 비밀번호를 권장합니다.
            </p>

            <form v-if="token" @submit.prevent="handleReset" class="space-y-6">
              <div class="space-y-2">
                <label class="text-[10px] font-black uppercase tracking-widest text-slate-400 ml-1">New Password</label>
                <div class="relative group">
                  <Lock class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-300 group-focus-within:text-blue-500 transition-colors" />
                  <input 
                    v-model="newPassword"
                    :type="showPassword ? 'text' : 'password'" 
                    required
                    placeholder="••••••••"
                    class="w-full pl-12 pr-12 py-4 bg-slate-50 border border-slate-100 rounded-2xl focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 outline-none transition-all font-medium"
                  />
                  <button 
                    type="button"
                    @click="showPassword = !showPassword"
                    class="absolute x-100 right-4 top-1/2 -translate-y-1/2 text-slate-300 hover:text-slate-600"
                  >
                    <Eye v-if="!showPassword" class="w-5 h-5" />
                    <EyeOff v-else class="w-5 h-5" />
                  </button>
                </div>
              </div>

              <div class="space-y-2">
                <label class="text-[10px] font-black uppercase tracking-widest text-slate-400 ml-1">Confirm Password</label>
                <div class="relative group">
                  <Lock class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-300 group-focus-within:text-blue-500 transition-colors" />
                  <input 
                    v-model="confirmPassword"
                    :type="showPassword ? 'text' : 'password'" 
                    required
                    placeholder="••••••••"
                    class="w-full pl-12 pr-4 py-4 bg-slate-50 border border-slate-100 rounded-2xl focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 outline-none transition-all font-medium"
                  />
                </div>
              </div>

              <div v-if="error" class="p-4 bg-rose-50 border border-rose-100 rounded-2xl flex items-center gap-3 text-rose-600 text-sm font-bold animate-in shake duration-500">
                <AlertCircle class="w-5 h-5 shrink-0" />
                {{ error }}
              </div>

              <button 
                type="submit"
                :disabled="isLoading"
                class="w-full py-4 bg-slate-900 text-white font-black rounded-2xl shadow-xl shadow-slate-900/20 hover:bg-slate-800 active:scale-[0.98] transition-all disabled:opacity-50 flex items-center justify-center gap-2 group"
              >
                <span v-if="isLoading" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                <template v-else>
                  Update Password
                  <CheckCircle2 class="w-4 h-4" />
                </template>
              </button>
            </form>

            <div v-else class="p-6 bg-rose-50 border border-rose-100 rounded-3xl text-center">
               <AlertCircle class="w-12 h-12 text-rose-500 mx-auto mb-4" />
               <p class="text-rose-600 font-bold mb-4">{{ error }}</p>
               <button 
                 @click="router.push('/forgot-password')"
                 class="px-6 py-2 bg-rose-600 text-white rounded-xl text-sm font-bold"
               >
                 초기화 링크 재요청
               </button>
            </div>
          </div>

          <div v-else class="text-center animate-in zoom-in-95 duration-500">
            <div class="w-20 h-20 bg-emerald-50 rounded-full flex items-center justify-center mx-auto mb-8">
              <CheckCircle2 class="w-10 h-10 text-emerald-600" />
            </div>
            <h2 class="text-3xl font-black text-slate-900 tracking-tight mb-4">Success!</h2>
            <p class="text-slate-500 font-medium leading-relaxed mb-8">
              비밀번호가 성공적으로 변경되었습니다. 이제 새 비밀번호로 로그인해 주세요.
            </p>
            <button 
              @click="router.push('/login')"
              class="w-full py-4 bg-emerald-600 text-white font-black rounded-2xl shadow-xl shadow-emerald-600/20 hover:bg-emerald-700 transition-all"
            >
              Go to Login
            </button>
          </div>
        </div>

        <!-- Decorative Footer -->
        <div class="px-10 py-6 bg-slate-50 border-t border-slate-100 flex items-center justify-center gap-2">
          <span class="w-1.5 h-1.5 bg-blue-500 rounded-full animate-pulse"></span>
          <p class="text-[10px] font-black text-slate-400 uppercase tracking-[0.2em]">Secure Authentication System</p>
        </div>
      </div>
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
