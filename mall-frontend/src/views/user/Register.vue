<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h1>注册账号</h1>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" /></el-form-item>
        <el-button class="wide" type="primary" :loading="loading" @click="submit">注册</el-button>
      </el-form>
      <p class="muted">已有账号？<router-link to="/login">去登录</router-link></p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import { register } from '../../api/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '', phone: '', email: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名' }, { min: 3, message: '至少 3 位' }],
  password: [{ required: true, message: '请输入密码' }, { min: 6, message: '至少 6 位' }]
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
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
  width: min(440px, 100%);
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
