<template>
  <div class="admin-page">
    <div class="toolbar">
      <h1 class="section-title">操作日志</h1>
      <div class="filters">
        <el-select v-model="query.module" placeholder="全部模块" clearable style="width: 150px" @change="load">
          <el-option v-for="item in moduleOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="query.action" placeholder="全部动作" clearable style="width: 190px" @change="load">
          <el-option v-for="item in actionOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>
    </div>

    <el-table class="panel" :data="logs" v-loading="loading">
      <el-table-column prop="createTime" label="时间" width="180" />
      <el-table-column label="模块" width="120">
        <template #default="{ row }"><el-tag>{{ moduleText(row.module) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="动作" min-width="170">
        <template #default="{ row }">{{ actionText(row.action) }}</template>
      </el-table-column>
      <el-table-column prop="targetName" label="对象" min-width="180" />
      <el-table-column prop="operatorUsername" label="操作者" width="120" />
      <el-table-column prop="remark" label="备注" min-width="200" />
    </el-table>

    <div class="pager">
      <el-pagination layout="prev, pager, next, total" :total="total" :page-size="query.size" v-model:current-page="query.page" @current-change="load" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { getAdminOperationLogs } from '../../api/adminLog'

const loading = ref(false)
const logs = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, module: '', action: '' })

const moduleOptions = [
  { label: '商品', value: 'PRODUCT' },
  { label: '分类', value: 'CATEGORY' },
  { label: '用户', value: 'USER' }
]
const actionLabels = {
  CREATE_PRODUCT: '创建商品',
  UPDATE_PRODUCT: '更新商品',
  DISABLE_PRODUCT: '下架商品',
  UPDATE_PRODUCT_STATUS: '更新商品状态',
  UPDATE_PRODUCT_STOCK: '更新商品库存',
  CREATE_CATEGORY: '创建分类',
  UPDATE_CATEGORY: '更新分类',
  DISABLE_CATEGORY: '停用分类',
  DELETE_CATEGORY: '删除分类',
  UPDATE_USER_STATUS: '更新用户状态'
}
const actionOptions = computed(() => Object.entries(actionLabels).map(([value, label]) => ({ value, label })))
const moduleText = (module) => moduleOptions.find((item) => item.value === module)?.label || module || '-'
const actionText = (action) => actionLabels[action] || action || '-'

async function load() {
  loading.value = true
  try {
    const data = await getAdminOperationLogs(query)
    logs.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.admin-page {
  display: grid;
  gap: 18px;
}

.filters {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.pager {
  display: flex;
  justify-content: center;
}
</style>
