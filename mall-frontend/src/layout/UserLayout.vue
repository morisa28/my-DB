<template>
  <div class="user-shell">
    <header class="topbar">
      <div class="page topbar-inner">
        <router-link class="brand" to="/">
          <el-icon><ShoppingBag /></el-icon>
          <span>校园优选商城</span>
        </router-link>
        <nav class="nav">
          <router-link to="/">首页</router-link>
          <router-link v-if="auth.isLogin" to="/cart">购物车 {{ cart.count ? `(${cart.count})` : '' }}</router-link>
          <router-link v-if="auth.isLogin" to="/orders">我的订单</router-link>
          <router-link v-if="auth.isLogin" to="/address">地址</router-link>
          <router-link v-if="auth.isAdmin" to="/admin">后台</router-link>
        </nav>
        <div class="account">
          <template v-if="auth.isLogin">
            <span class="muted">{{ auth.user?.username }}</span>
            <el-button size="small" @click="logout">退出</el-button>
          </template>
          <template v-else>
            <el-button size="small" @click="$router.push('/login')">登录</el-button>
            <el-button size="small" type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>
    <main>
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { useCartStore } from '../store/cart'

const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()

onMounted(() => {
  if (auth.isLogin) cart.refreshCount()
})

async function logout() {
  await auth.logout()
  cart.count = 0
  router.push('/')
}
</script>

<style scoped>
.user-shell {
  min-height: 100vh;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(255, 255, 255, 0.94);
  border-bottom: 1px solid #e6ebf2;
  backdrop-filter: blur(10px);
}

.topbar-inner {
  height: 64px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 24px;
  align-items: center;
}

.brand {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  color: #172033;
  font-weight: 800;
}

.nav {
  display: flex;
  gap: 18px;
  align-items: center;
  font-size: 14px;
  color: #4a5568;
}

.nav a.router-link-active {
  color: #2563eb;
  font-weight: 700;
}

.account {
  display: flex;
  gap: 10px;
  align-items: center;
}

main {
  padding: 28px 0 48px;
}

@media (max-width: 760px) {
  .topbar-inner {
    height: auto;
    padding: 12px 0;
    grid-template-columns: 1fr;
  }

  .nav {
    overflow-x: auto;
    padding-bottom: 4px;
  }
}
</style>
