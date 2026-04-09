<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import { Lock, Mail, Loader2, Building, Package } from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()

const mode = ref<'login' | 'signup'>('login')
const email = ref('')
const password = ref('')
const department = ref('')
const loading = ref(false)
const error = ref('')
const success = ref('')

const handleSubmit = async () => {
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    if (mode.value === 'login') {
      await authStore.login({ email: email.value, password: password.value })
      if (authStore.isAdmin) {
        router.push('/')
      } else {
        router.push('/assets')
      }
    } else {
      await authStore.signup({ 
        email: email.value, 
        password: password.value, 
        department: department.value 
      })
      success.value = 'Sign up successful! Please wait for admin approval.'
      mode.value = 'login'
      password.value = ''
    }
  } catch (err: any) {
    if (err.response?.data && typeof err.response.data === 'string') {
      error.value = err.response.data
    } else {
      error.value = mode.value === 'login' ? 'Invalid email or password' : 'Sign up failed'
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 px-4">
    <div class="w-full max-w-md">

      <!-- Logo / Branding -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-blue-600 shadow-lg shadow-blue-500/30 mb-4">
          <Package class="w-7 h-7 text-white" />
        </div>
        <h1 class="text-2xl font-bold text-white tracking-tight">Asset Manager</h1>
        <p class="text-slate-400 text-sm mt-1">Inventory & Asset Tracking System</p>
      </div>

      <!-- Card -->
      <div class="bg-white rounded-2xl shadow-2xl overflow-hidden">

        <!-- Tab Switcher -->
        <div class="flex border-b border-gray-100">
          <button
            @click="mode = 'login'; error = ''; success = ''"
            :class="[
              'flex-1 py-4 text-sm font-bold tracking-wide transition-all',
              mode === 'login'
                ? 'text-slate-900 border-b-2 border-slate-900 bg-white'
                : 'text-gray-400 hover:text-gray-600 bg-gray-50/50'
            ]"
          >
            Sign In
          </button>
          <button
            @click="mode = 'signup'; error = ''; success = ''"
            :class="[
              'flex-1 py-4 text-sm font-bold tracking-wide transition-all',
              mode === 'signup'
                ? 'text-slate-900 border-b-2 border-slate-900 bg-white'
                : 'text-gray-400 hover:text-gray-600 bg-gray-50/50'
            ]"
          >
            Sign Up
          </button>
        </div>

        <!-- Form -->
        <div class="p-8">
          <form @submit.prevent="handleSubmit" class="space-y-5">
            <div v-if="error" class="bg-red-50 text-red-600 px-4 py-3 rounded-xl text-sm font-medium border border-red-100 flex items-start gap-2">
              <span class="mt-0.5">⚠️</span>
              <span>{{ error }}</span>
            </div>
            <div v-if="success" class="bg-green-50 text-green-600 px-4 py-3 rounded-xl text-sm font-medium border border-green-100 flex items-start gap-2">
              <span class="mt-0.5">✅</span>
              <span>{{ success }}</span>
            </div>

            <div>
              <label class="block text-sm font-semibold text-gray-700 mb-1.5">Email Address</label>
              <div class="relative">
                <span class="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-400 pointer-events-none">
                  <Mail class="w-4.5 h-4.5" />
                </span>
                <input
                  v-model="email"
                  type="email"
                  required
                  class="block w-full pl-10 pr-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 focus:bg-white outline-none transition-all text-sm"
                  :placeholder="mode === 'login' ? 'admin@company.com' : 'newuser@company.com'"
                />
              </div>
            </div>

            <div v-if="mode === 'signup'">
              <label class="block text-sm font-semibold text-gray-700 mb-1.5">Department</label>
              <div class="relative">
                <span class="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-400 pointer-events-none">
                  <Building class="w-4.5 h-4.5" />
                </span>
                <input
                  v-model="department"
                  type="text"
                  class="block w-full pl-10 pr-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 focus:bg-white outline-none transition-all text-sm"
                  placeholder="Marketing, IT, etc."
                />
              </div>
            </div>

            <div>
              <label class="block text-sm font-semibold text-gray-700 mb-1.5">Password</label>
              <div class="relative">
                <span class="absolute inset-y-0 left-0 pl-3.5 flex items-center text-gray-400 pointer-events-none">
                  <Lock class="w-4.5 h-4.5" />
                </span>
                <input
                  v-model="password"
                  type="password"
                  required
                  class="block w-full pl-10 pr-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 focus:bg-white outline-none transition-all text-sm"
                  placeholder="••••••••"
                />
              </div>
            </div>

            <button
              type="submit"
              :disabled="loading"
              class="w-full bg-slate-900 text-white font-bold py-2.5 rounded-xl hover:bg-slate-700 transition-all shadow-lg flex items-center justify-center gap-2 mt-2 disabled:opacity-60 disabled:cursor-not-allowed"
            >
              <Loader2 v-if="loading" class="w-4 h-4 animate-spin" />
              <span>{{ loading ? (mode === 'login' ? 'Signing in...' : 'Signing up...') : (mode === 'login' ? 'Sign In' : 'Create Account') }}</span>
            </button>
          </form>
        </div>
      </div>

      <p class="text-center text-slate-500 text-xs mt-6">Asset Management System © 2026</p>
    </div>
  </div>
</template>

