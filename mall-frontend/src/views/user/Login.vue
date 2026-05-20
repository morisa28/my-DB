<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h1>登录商城</h1>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-button class="wide" type="primary" :loading="loading" @click="submit">登录</el-button>
      </el-form>
      <p class="muted">没有账号？<router-link to="/register">立即注册</router-link></p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import { useAuthStore } from '../../store/auth'
import { useCartStore } from '../../store/cart'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const cart = useCartStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'user', password: 'user123456' })
const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }]
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await auth.login(form)
    await cart.refreshCount()
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: #f5f7fb;
  padding: 20px;
}

.auth-card {
  width: min(420px, 100%);
}

h1 {
  margin: 0 0 24px;
}

.wide {
  width: 100%;
}

p {
  text-align: center;
  margin: 18px 0 0;
}

a {
  color: #2563eb;
  font-weight: 700;
}
</style>
