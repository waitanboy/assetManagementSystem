<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/authStore'
import ChatWidget from './components/chat/ChatWidget.vue'
import ReloadPrompt from './components/common/ReloadPrompt.vue'

const authStore = useAuthStore()
const router = useRouter()

onMounted(async () => {
  await authStore.checkAuth()
  if (!authStore.isAuthenticated) {
    router.push('/login')
  }
})
</script>

<template>
  <router-view />
  <ChatWidget v-if="authStore.isAuthenticated" />
  <ReloadPrompt />
</template>
