import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../components/layout/MainLayout.vue'
import { useAuthStore } from '../stores/authStore'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/LoginView.vue'),
      meta: { requiresGuest: true },
    },
    {
      path: '/forgot-password',
      name: 'Forgot',
      component: () => import('../views/ForgotView.vue'),
      meta: { requiresGuest: true },
    },
    {
      path: '/reset-password',
      name: 'Reset',
      component: () => import('../views/ResetView.vue'),
      meta: { requiresGuest: true },
    },
    {
      path: '/',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'Dashboard',
          component: () => import('../views/DashboardView.vue'),
          meta: { requiresRole: 'ADMIN' },
        },
        {
          path: 'assets',
          name: 'AssetList',
          component: () => import('../views/AssetListView.vue'),
        },
        {
          path: 'my-assets',
          name: 'MyAssets',
          component: () => import('../views/MyAssetsView.vue'),
        },
        {
          path: 'assets/:id',
          name: 'AssetDetail',
          component: () => import('../views/AssetDetailView.vue'),
        },
        {
          path: 'settings',
          name: 'System Settings',
          component: () => import('../views/SystemSettingsView.vue'),
          meta: { requiresRole: 'ADMIN' },
        },
        {
          path: 'notices',
          name: 'Announcements',
          component: () => import('../views/NoticeListView.vue'),
        },
        {
          path: 'users',
          name: 'Users',
          component: () => import('../views/UserListView.vue'),
          meta: { requiresRole: 'ADMIN' },
        },
        {
          path: 'users/:id',
          name: 'User Details',
          component: () => import('../views/UserDetailView.vue'),
          meta: { requiresRole: 'ADMIN' },
        },
        {
          path: 'logs',
          name: 'Activity Logs',
          component: () => import('../views/TransactionListView.vue'),
          meta: { requiresRole: 'ADMIN' },
        },
        {
          path: 'system/console',
          name: 'System Console',
          component: () => import('../views/SystemConsoleView.vue'),
          meta: { requiresRole: 'ADMIN' },
        },
        {
          path: 'repairs',
          name: 'Maintenance',
          component: () => import('../views/MaintenanceView.vue'),
          meta: { requiresRole: 'ADMIN' },
        },
        {
          path: 'transactions',
          name: 'Transactions',
          component: () => import('../views/TransactionListView.vue'),
          meta: { requiresRole: 'ADMIN' },
        },
        {
          path: 'profile',
          name: 'My Profile',
          component: () => import('../views/ProfileView.vue'),
        },
        {
          path: 'requests',
          name: 'Rental Requests',
          component: () => import('../views/RentalRequestView.vue'),
        },
        {
          path: 'support',
          name: 'Support Chat',
          component: () => import('../views/AdminChatView.vue'),
          meta: { requiresRole: 'ADMIN' },
        },
        {
          path: 'board',
          name: 'Community Board',
          component: () => import('../views/BoardListView.vue'),
        },
        {
          path: 'board/:id',
          name: 'Board Detail',
          component: () => import('../views/BoardDetailView.vue'),
        },
      ],
    },
  ],
})

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()
  
  if (!authStore.initialized) {
    await authStore.checkAuth()
  }

  const isAuthenticated = authStore.isAuthenticated

  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else if (to.meta.requiresGuest && isAuthenticated) {
    next(authStore.isAdmin ? '/' : '/assets')
  } else if (to.meta.requiresRole && to.meta.requiresRole !== authStore.role) {
    next('/assets')
  } else {
    next()
  }
})

export default router
