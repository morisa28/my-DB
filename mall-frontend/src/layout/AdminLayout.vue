<template>
  <el-container class="admin-shell">
    <el-aside width="232px" class="admin-aside">
      <div class="admin-brand">商城后台</div>
      <el-menu router :default-active="$route.path" class="admin-menu">
        <el-menu-item index="/admin"><el-icon><DataBoard /></el-icon><span>统计概览</span></el-menu-item>
        <el-menu-item index="/admin/products"><el-icon><Goods /></el-icon><span>商品管理</span></el-menu-item>
        <el-menu-item index="/admin/categories"><el-icon><Collection /></el-icon><span>分类管理</span></el-menu-item>
        <el-menu-item index="/admin/orders"><el-icon><Tickets /></el-icon><span>订单管理</span></el-menu-item>
        <el-menu-item index="/admin/users"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <span class="muted">数据库课程商城平台</span>
        <div>
          <el-button @click="$router.push('/')">返回前台</el-button>
          <el-button @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const auth = useAuthStore()

async function logout() {
  await auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-shell {
  min-height: 100vh;
  background: #f5f7fb;
}

.admin-aside {
  background: #111827;
  color: #fff;
}

.admin-brand {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 22px;
  font-size: 18px;
  font-weight: 800;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.admin-menu {
  border-right: none;
  background: transparent;
}

.admin-menu :deep(.el-menu-item) {
  color: #d1d5db;
}

.admin-menu :deep(.el-menu-item.is-active) {
  background: #1f2937;
  color: #ffffff;
}

.admin-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid #e6ebf2;
}

.admin-main {
  padding: 22px;
}

@media (max-width: 860px) {
  .admin-shell {
    display: block;
  }

  .admin-aside {
    width: 100% !important;
  }

  .admin-menu {
    display: flex;
    overflow-x: auto;
  }
}
</style>
