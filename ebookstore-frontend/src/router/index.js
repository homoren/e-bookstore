import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/Home.vue')
    },
    {
      path: '/books',
      name: 'books',
      component: () => import('@/views/BookList.vue')
    },
    {
      path: '/book/:id',
      name: 'bookDetail',
      component: () => import('@/views/BookDetail.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginPage.vue'),
      meta: { guest: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterPage.vue'),
      meta: { guest: true }
    },
    {
      path: '/cart',
      name: 'cart',
      component: () => import('@/views/Cart.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/checkout',
      name: 'checkout',
      component: () => import('@/views/Checkout.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('@/views/OrdersPage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/order/:id',
      name: 'orderDetail',
      component: () => import('@/views/OrderDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/announcements',
      name: 'announcements',
      component: () => import('@/views/Announcements.vue')
    },
    {
      path: '/messages',
      name: 'messages',
      component: () => import('@/views/Messages.vue')
    },
    {
      path: '/admin',
      component: () => import('@/components/Layout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: () => import('@/views/admin/Dashboard.vue')
        },
        {
          path: 'books',
          name: 'adminBooks',
          component: () => import('@/views/admin/BooksPage.vue')
        },
        {
          path: 'orders',
          name: 'adminOrders',
          component: () => import('@/views/admin/Orders.vue')
        },
        {
          path: 'customers',
          name: 'adminCustomers',
          component: () => import('@/views/admin/Customers.vue')
        },
        {
          path: 'purchase',
          name: 'adminPurchase',
          component: () => import('@/views/admin/Purchase.vue')
        },
        {
          path: 'settlement',
          name: 'adminSettlement',
          component: () => import('@/views/admin/Settlement.vue')
        },
        {
          path: 'announcements',
          name: 'adminAnnouncements',
          component: () => import('@/views/admin/Announcements.vue')
        },
        {
          path: 'messages',
          name: 'adminMessages',
          component: () => import('@/views/admin/Messages.vue')
        }

      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'login', query: { redirect: to.fullPath } })
  } else if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next({ name: 'home' })
  } else if (to.meta.guest && userStore.isLoggedIn) {
    next({ name: 'home' })
  } else {
    next()
  }
})

export default router
