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
          path: 'categories',
          name: 'Categories',
          component: () => import('../views/CategoryListView.vue'),
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
          path: 'transactions',
          name: 'Transactions',
          component: () => import('../views/TransactionListView.vue'),
          meta: { requiresRole: 'ADMIN' },
        },
        {
          path: 'repairs',
          name: 'Maintenance',
          component: () => import('../views/MaintenanceView.vue'),
          meta: { requiresRole: 'ADMIN' },
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
