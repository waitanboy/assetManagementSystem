<script setup lang="ts">
import { ref, reactive } from 'vue'
import { User, Mail, Building2, Lock, Shield, CheckCircle, AlertTriangle, ChevronRight, Save } from 'lucide-vue-next'
import { useAuthStore } from '../stores/authStore'
import { useDepartmentStore } from '../stores/departmentStore'
import { onMounted } from 'vue'

const authStore = useAuthStore()
const departmentStore = useDepartmentStore()

onMounted(() => {
  departmentStore.fetchDepartments()
})

const profileForm = reactive({
  email: authStore.email || '',
  name: authStore.name || '',
  department: authStore.department || '',
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const isSubmitting = ref(false)
const status = ref<{ type: 'success' | 'error', message: string } | null>(null)

const handleUpdateProfile = async () => {
  if (profileForm.newPassword && profileForm.newPassword !== profileForm.confirmPassword) {
    status.value = { type: 'error', message: '새 비밀번호가 일치하지 않습니다.' }
    return
  }

  isSubmitting.value = true
  status.value = null

  try {
    const updateData: any = {
      email: profileForm.email,
      name: profileForm.name,
      department: profileForm.department
    }
    
    if (profileForm.newPassword) {
      updateData.password = profileForm.newPassword
    }

    await authStore.updateProfile(updateData)
    status.value = { type: 'success', message: '프로필 정보가 성공적으로 업데이트되었습니다.' }
    
    // Reset password fields
    profileForm.currentPassword = ''
    profileForm.newPassword = ''
    profileForm.confirmPassword = ''
  } catch (err: any) {
    status.value = { type: 'error', message: err.response?.data || '프로필 업데이트에 실패했습니다.' }
  } finally {
    isSubmitting.value = false
    setTimeout(() => { status.value = null }, 5000)
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto py-8">
    <div class="mb-8">
      <h2 class="text-2xl font-black text-gray-900 flex items-center gap-3">
        <div class="p-2 bg-blue-600 rounded-xl text-white shadow-lg shadow-blue-200">
          <User class="w-6 h-6" />
        </div>
        My Account Profile
      </h2>
      <p class="text-sm text-gray-500 mt-2 font-medium">관리자 시스템에서 본인의 개인 정보 및 보안 설정을 관리하세요.</p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- Left: Profile Summary Card -->
      <div class="lg:col-span-1 space-y-6">
        <div class="bg-white rounded-3xl shadow-sm border border-gray-100 p-8 text-center relative overflow-hidden">
          <div class="absolute top-0 left-0 w-full h-24 bg-gradient-to-br from-blue-600 to-indigo-700 -z-0 opacity-10"></div>
          
          <div class="relative z-10">
            <div class="w-24 h-24 rounded-2xl bg-gradient-to-tr from-blue-600 to-indigo-600 mx-auto flex items-center justify-center text-white text-3xl font-black shadow-xl shadow-blue-100 mb-4 border-4 border-white">
              {{ authStore.name?.charAt(0).toUpperCase() || authStore.email?.charAt(0).toUpperCase() }}
            </div>
            <h3 class="text-lg font-bold text-gray-900 truncate px-2">{{ authStore.name || authStore.email?.split('@')[0] }}</h3>
            <div class="flex items-center justify-center gap-2 mt-2">
              <span class="px-2.5 py-1 bg-blue-50 text-blue-600 text-[10px] font-black uppercase tracking-wider rounded-lg border border-blue-100">
                {{ authStore.role }}
              </span>
              <span class="px-2.5 py-1 bg-green-50 text-green-600 text-[10px] font-black uppercase tracking-wider rounded-lg border border-green-100">
                ACTIVE
              </span>
            </div>
          </div>

          <div class="mt-8 pt-6 border-t border-gray-50 space-y-4 text-left">
            <div class="flex items-center gap-3">
              <div class="p-1.5 bg-gray-50 rounded-lg text-gray-400">
                <Mail class="w-3.5 h-3.5" />
              </div>
              <div class="min-w-0">
                <p class="text-[9px] text-gray-400 font-bold uppercase tracking-tight">Email Address</p>
                <p class="text-[11px] font-medium text-gray-700 truncate">{{ authStore.email }}</p>
              </div>
            </div>
            <div class="flex items-center gap-3">
              <div class="p-1.5 bg-gray-50 rounded-lg text-gray-400">
                <Building2 class="w-3.5 h-3.5" />
              </div>
              <div class="min-w-0">
                <p class="text-[9px] text-gray-400 font-bold uppercase tracking-tight">Department</p>
                <p class="text-[11px] font-medium text-gray-700 truncate">{{ authStore.department || 'Not Assigned' }}</p>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-amber-50 rounded-2xl p-6 border border-amber-100">
          <div class="flex items-center gap-2 text-amber-700 mb-2">
            <Shield class="w-4 h-4" />
            <h4 class="text-xs font-black uppercase tracking-tight">Security Note</h4>
          </div>
          <p class="text-[11px] text-amber-600 leading-relaxed font-medium">
            권한이나 계정 상태 변경은 시스템 관리자에게 문의해 주세요. 비밀번호 변경 시 즉시 새 비밀번호가 적용됩니다.
          </p>
        </div>
      </div>

      <!-- Right: Form Section -->
      <div class="lg:col-span-2">
        <div class="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden">
          <div class="p-8 border-b border-gray-50 flex items-center justify-between">
            <div>
              <h3 class="text-base font-bold text-gray-900">Account Settings</h3>
              <p class="text-xs text-gray-500 mt-0.5">기본 정보 및 비밀번호 수정</p>
            </div>
            
            <Transition enter-active-class="transition duration-300" enter-from-class="opacity-0 translate-x-4">
              <div v-if="status" :class="[
                'px-4 py-2 rounded-xl text-xs font-bold flex items-center gap-2',
                status.type === 'success' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
              ]">
                <CheckCircle v-if="status.type === 'success'" class="w-4 h-4" />
                <AlertTriangle v-else class="w-4 h-4" />
                {{ status.message }}
              </div>
            </Transition>
          </div>

          <form @submit.prevent="handleUpdateProfile" class="p-8 space-y-8">
            <!-- Basic Info Section -->
            <div class="space-y-6">
              <h4 class="text-[11px] font-black text-blue-600 uppercase tracking-widest flex items-center gap-2">
                Basic Information <div class="h-px flex-1 bg-blue-50"></div>
              </h4>
              
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div class="space-y-2">
                  <label class="block text-xs font-bold text-gray-700 ml-1">Email Address</label>
                  <div class="relative group">
                    <Mail class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 group-focus-within:text-blue-500 transition-colors" />
                    <input 
                      v-model="profileForm.email"
                      type="email" 
                      placeholder="email@address.com"
                      class="w-full bg-gray-50 border border-transparent focus:bg-white focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 rounded-xl px-11 py-3.5 text-sm font-medium transition-all outline-none"
                    />
                  </div>
                </div>

                <div class="space-y-2">
                  <label class="block text-xs font-bold text-gray-700 ml-1">Full Name</label>
                  <div class="relative group">
                    <div class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-blue-500 transition-colors text-sm">👤</div>
                    <input 
                      v-model="profileForm.name"
                      type="text" 
                      placeholder="Your Full Name"
                      class="w-full bg-gray-50 border border-transparent focus:bg-white focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 rounded-xl px-11 py-3.5 text-sm font-medium transition-all outline-none"
                    />
                  </div>
                </div>

                <div class="space-y-2">
                  <label class="block text-xs font-bold text-gray-700 ml-1">Department</label>
                  <div class="relative group">
                    <Building2 class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 group-focus-within:text-blue-500 transition-colors" />
                    <select 
                      v-model="profileForm.department"
                      class="w-full bg-gray-50 border border-transparent focus:bg-white focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 rounded-xl px-11 py-3.5 text-sm font-medium transition-all outline-none appearance-none"
                    >
                      <option value="">Select Department</option>
                      <option v-for="dept in departmentStore.departments" :key="dept.id" :value="dept.name">
                        {{ dept.name }}
                      </option>
                    </select>
                    <ChevronRight class="absolute right-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none rotate-90" />
                  </div>
                </div>
              </div>
            </div>

            <!-- Security Section -->
            <div class="space-y-6 pt-4">
              <h4 class="text-[11px] font-black text-rose-500 uppercase tracking-widest flex items-center gap-2">
                Security & Password <div class="h-px flex-1 bg-rose-50"></div>
              </h4>
              
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div class="space-y-2">
                  <label class="block text-xs font-bold text-gray-700 ml-1">New Password</label>
                  <div class="relative group">
                    <Lock class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 group-focus-within:text-blue-500 transition-colors" />
                    <input 
                      v-model="profileForm.newPassword"
                      type="password" 
                      placeholder="••••••••"
                      class="w-full bg-gray-50 border border-transparent focus:bg-white focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 rounded-xl px-11 py-3.5 text-sm font-medium transition-all outline-none"
                    />
                  </div>
                  <p class="text-[10px] text-gray-400 ml-1">변경을 원하실 경우에만 입력하세요.</p>
                </div>

                <div class="space-y-2">
                  <label class="block text-xs font-bold text-gray-700 ml-1">Confirm New Password</label>
                  <div class="relative group">
                    <Lock class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 group-focus-within:text-blue-500 transition-colors" />
                    <input 
                      v-model="profileForm.confirmPassword"
                      type="password" 
                      placeholder="••••••••"
                      class="w-full bg-gray-50 border border-transparent focus:bg-white focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 rounded-xl px-11 py-3.5 text-sm font-medium transition-all outline-none"
                    />
                  </div>
                </div>
              </div>
            </div>

            <div class="pt-6 flex items-center justify-end">
              <button 
                type="submit" 
                :disabled="isSubmitting"
                class="flex items-center gap-3 px-8 py-4 bg-gray-900 border border-gray-800 text-white rounded-2xl text-xs font-black uppercase tracking-widest shadow-xl shadow-gray-200 hover:bg-black transition-all active:scale-95 disabled:opacity-50 disabled:active:scale-100"
              >
                <Save v-if="!isSubmitting" class="w-4 h-4" />
                <div v-else class="w-4 h-4 border-2 border-gray-400 border-t-white rounded-full animate-spin"></div>
                {{ isSubmitting ? 'Saving Changes...' : 'Save Profile Changes' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>
