import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'

const routes = [
  {
    path: '/',
    component: () => import('../layout/UserLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('../views/user/Home.vue') },
      { path: 'products/:id', name: 'product-detail', component: () => import('../views/user/ProductDetail.vue') },
      { path: 'cart', name: 'cart', meta: { requiresAuth: true }, component: () => import('../views/user/Cart.vue') },
      { path: 'checkout', name: 'checkout', meta: { requiresAuth: true }, component: () => import('../views/user/Checkout.vue') },
      { path: 'address', name: 'address', meta: { requiresAuth: true }, component: () => import('../views/user/Address.vue') },
      { path: 'orders', name: 'orders', meta: { requiresAuth: true }, component: () => import('../views/user/Orders.vue') },
      { path: 'orders/:id', name: 'order-detail', meta: { requiresAuth: true }, component: () => import('../views/user/OrderDetail.vue') }
    ]
  },
  { path: '/login', name: 'login', component: () => import('../views/user/Login.vue') },
  { path: '/register', name: 'register', component: () => import('../views/user/Register.vue') },
  {
    path: '/admin',
    component: () => import('../layout/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: 'admin-dashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'products', name: 'admin-products', component: () => import('../views/admin/ProductManage.vue') },
      { path: 'categories', name: 'admin-categories', component: () => import('../views/admin/CategoryManage.vue') },
      { path: 'orders', name: 'admin-orders', component: () => import('../views/admin/OrderManage.vue') },
      { path: 'users', name: 'admin-users', component: () => import('../views/admin/UserManage.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (auth.token && !auth.user) {
    try {
      await auth.fetchUser()
    } catch {
      auth.logout()
    }
  }
  if (to.meta.requiresAuth && !auth.isLogin) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && !auth.isAdmin) {
    ElMessage.warning('无管理员权限')
    return '/'
  }
  return true
})

export default router

