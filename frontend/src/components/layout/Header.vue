<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, User, LogOut, ChevronDown, Mail, Shield, UserCircle } from 'lucide-vue-next'
import { useAuthStore } from '../../stores/authStore'
import { useNotificationStore } from '../../stores/notificationStore'

const authStore = useAuthStore()
const notificationStore = useNotificationStore()
const router = useRouter()
const isMenuOpen = ref(false)
const isNotificationsOpen = ref(false)

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value
  isNotificationsOpen.value = false
}

const toggleNotifications = () => {
  isNotificationsOpen.value = !isNotificationsOpen.value
  isMenuOpen.value = false
}

const closeMenu = (e: MouseEvent) => {
  const target = e.target as HTMLElement
  if (!target.closest('.profile-dropdown-container')) {
    isMenuOpen.value = false
  }
  if (!target.closest('.notification-dropdown-container')) {
    isNotificationsOpen.value = false
  }
}

onMounted(() => {
  window.addEventListener('click', closeMenu)
  notificationStore.fetchNotifications()
})

onUnmounted(() => {
  window.removeEventListener('click', closeMenu)
})

const handleNotificationClick = (link: string, id: string) => {
  notificationStore.markAsRead(id)
  isNotificationsOpen.value = false
  router.push(link)
}

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>

<template>
  <header class="flex h-16 items-center justify-between border-b bg-white/80 backdrop-blur-md sticky top-0 z-30 px-6">
    <div class="flex items-center">
      <h1 class="text-xl font-bold bg-gradient-to-r from-gray-800 to-gray-500 bg-clip-text text-transparent capitalize">
        {{ $route.name }}
      </h1>
    </div>

    <div class="flex items-center space-x-6">
      <div class="relative notification-dropdown-container">
        <button 
          @click.stop="toggleNotifications"
          class="relative text-gray-400 hover:text-blue-600 transition-colors"
        >
          <Bell class="w-5 h-5" />
          <span 
            v-if="notificationStore.unreadCount > 0" 
            class="absolute -top-1.5 -right-1.5 w-4 h-4 flex items-center justify-center bg-red-500 text-[9px] font-bold text-white rounded-full border-2 border-white"
          >
            {{ notificationStore.unreadCount }}
          </span>
        </button>

        <Transition
          enter-active-class="transition duration-200 ease-out"
          enter-from-class="transform scale-95 opacity-0 translate-y-2"
          enter-to-class="transform scale-100 opacity-100 translate-y-0"
          leave-active-class="transition duration-150 ease-in"
          leave-from-class="transform scale-100 opacity-100 translate-y-0"
          leave-to-class="transform scale-95 opacity-0 translate-y-2"
        >
          <div 
            v-if="isNotificationsOpen"
            class="absolute right-0 mt-3 w-80 bg-white rounded-2xl shadow-[0_20px_50px_rgba(0,0,0,0.1)] border border-gray-100 overflow-hidden"
          >
            <div class="px-5 py-4 border-b border-gray-100 flex items-center justify-between bg-gray-50/50">
              <h3 class="font-bold text-gray-800">Notifications</h3>
              <button 
                v-if="notificationStore.unreadCount > 0"
                @click="notificationStore.markAllAsRead()"
                class="text-xs text-blue-600 font-medium hover:text-blue-700 hover:underline"
              >
                Mark all as read
              </button>
            </div>
            <div class="max-h-80 overflow-y-auto">
              <div v-if="notificationStore.notifications.length === 0" class="p-8 text-center text-gray-400">
                <Bell class="w-8 h-8 mx-auto mb-2 opacity-50" />
                <p class="text-sm">No new notifications</p>
              </div>
              <div v-else>
                <div 
                  v-for="notif in notificationStore.notifications" 
                  :key="notif.id"
                  @click="handleNotificationClick(notif.link, notif.id)"
                  :class="[
                    'p-4 border-b border-gray-50 hover:bg-gray-50 cursor-pointer transition-colors',
                    !notif.isRead ? 'bg-blue-50/30' : ''
                  ]"
                >
                  <p :class="['text-sm mb-1', !notif.isRead ? 'font-bold text-gray-900' : 'font-medium text-gray-700']">
                    {{ notif.title }}
                  </p>
                  <p class="text-xs text-gray-500 mb-2 leading-relaxed">{{ notif.message }}</p>
                  <p class="text-[10px] text-gray-400">{{ new Date(notif.timestamp).toLocaleString() }}</p>
                </div>
              </div>
            </div>
          </div>
        </Transition>
      </div>

      <div class="relative profile-dropdown-container">
        <button 
          @click.stop="toggleMenu"
          class="group flex items-center space-x-3 p-1.5 pr-3 rounded-full hover:bg-gray-50 transition-all border border-transparent hover:border-gray-100"
        >
          <div class="w-8 h-8 rounded-full bg-gradient-to-tr from-blue-600 to-indigo-600 flex items-center justify-center text-white shadow-sm group-hover:scale-105 transition-transform">
            <User class="w-4 h-4" />
          </div>
          <div class="text-left hidden sm:block">
            <p class="text-xs font-semibold text-gray-700 leading-none mb-0.5 truncate max-w-[120px]">
              {{ authStore.email?.split('@')[0] }}
            </p>
            <p class="text-[10px] text-gray-400 leading-none">{{ authStore.isAdmin ? 'Admin' : 'User' }}</p>
          </div>
          <ChevronDown class="w-4 h-4 text-gray-400 group-hover:text-gray-600 transition-colors" />
        </button>

        <!-- Dropdown Menu -->
        <Transition
          enter-active-class="transition duration-200 ease-out"
          enter-from-class="transform scale-95 opacity-0 translate-y-2"
          enter-to-class="transform scale-100 opacity-100 translate-y-0"
          leave-active-class="transition duration-150 ease-in"
          leave-from-class="transform scale-100 opacity-100 translate-y-0"
          leave-to-class="transform scale-95 opacity-0 translate-y-2"
        >
          <div 
            v-if="isMenuOpen"
            class="absolute right-0 mt-3 w-72 bg-white rounded-2xl shadow-[0_20px_50px_rgba(0,0,0,0.1)] border border-gray-100 overflow-hidden"
          >
            <!-- Header Section -->
            <div class="p-6 bg-gradient-to-br from-gray-50 to-white border-b border-gray-100">
              <div class="flex items-center space-x-4">
                <div class="w-12 h-12 rounded-xl bg-blue-600 flex items-center justify-center text-white text-xl font-bold shadow-lg shadow-blue-200">
                  {{ authStore.email?.charAt(0).toUpperCase() }}
                </div>
                <div>
                  <h4 class="font-bold text-gray-900 truncate max-w-[160px]">{{ authStore.email?.split('@')[0] }}</h4>
                  <span v-if="authStore.isAdmin" class="text-xs px-2 py-0.5 bg-purple-100 text-purple-600 rounded-full font-medium">Administrator</span>
                  <span v-else class="text-xs px-2 py-0.5 bg-blue-100 text-blue-600 rounded-full font-medium">System User</span>
                </div>
              </div>
            </div>

            <!-- Details Section -->
            <div class="p-4 space-y-1">
              <div class="flex items-center space-x-3 p-2 rounded-lg text-gray-600 hover:bg-gray-50">
                <UserCircle class="w-4 h-4 text-gray-400" />
                <div class="text-xs">
                  <p class="text-gray-400">UID</p>
                  <p class="font-medium">#{{ authStore.id?.toString().padStart(4, '0') }}</p>
                </div>
              </div>
              <div class="flex items-center space-x-3 p-2 rounded-lg text-gray-600 hover:bg-gray-50">
                <Mail class="w-4 h-4 text-gray-400" />
                <div class="text-xs">
                  <p class="text-gray-400">Email Address</p>
                  <p class="font-medium truncate max-w-[180px]">{{ authStore.email }}</p>
                </div>
              </div>
              <div class="flex items-center space-x-3 p-2 rounded-lg text-gray-600 hover:bg-gray-50">
                <Shield class="w-4 h-4 text-gray-400" />
                <div class="text-xs">
                  <p class="text-gray-400">Role</p>
                  <p class="font-medium">{{ authStore.role }}</p>
                </div>
              </div>
            </div>

            <!-- Footer Section -->
            <div class="p-4 bg-gray-50/50">
              <button 
                @click="handleLogout"
                class="w-full flex items-center justify-center space-x-2 p-3 bg-white hover:bg-red-50 text-gray-700 hover:text-red-600 rounded-xl border border-gray-200 hover:border-red-100 transition-all shadow-sm"
              >
                <LogOut class="w-4 h-4" />
                <span class="text-sm font-semibold">Sign Out System</span>
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </header>
</template>
