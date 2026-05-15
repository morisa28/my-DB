<template>
  <div class="admin-page">
    <div class="toolbar">
      <h1 class="section-title">用户管理</h1>
      <el-input v-model="query.keyword" placeholder="搜索用户名" clearable style="width: 240px" @keyup.enter="load" />
    </div>
    <el-table class="panel" :data="users" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column label="角色" width="100"><template #default="{ row }"><el-tag :type="row.role === 1 ? 'danger' : 'info'">{{ row.role === 1 ? '管理员' : '用户' }}</el-tag></template></el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-switch :model-value="row.status" :active-value="1" :inactive-value="0" @change="(value) => changeStatus(row, value)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180" />
    </el-table>
    <div class="pager">
      <el-pagination layout="prev, pager, next, total" :total="total" :page-size="query.size" v-model:current-page="query.page" @current-change="load" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminUsers, updateUserStatus } from '../../api/user'

const loading = ref(false)
const users = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, keyword: '' })

async function load() {
  loading.value = true
  try {
    const data = await getAdminUsers(query)
    users.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function changeStatus(row, status) {
  await updateUserStatus(row.id, status)
  ElMessage.success('状态已更新')
  await load()
}

onMounted(load)
</script>

<style scoped>
.admin-page {
  display: grid;
  gap: 18px;
}

.pager {
  display: flex;
  justify-content: center;
}
</style>

