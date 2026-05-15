<template>
  <div class="admin-page">
    <div class="toolbar">
      <h1 class="section-title">订单管理</h1>
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 150px" @change="load">
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
    </div>

    <el-table class="panel" :data="orders" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" min-width="220" />
      <el-table-column prop="username" label="用户" width="110" />
      <el-table-column label="金额" width="120"><template #default="{ row }"><span class="price">￥{{ row.totalAmount }}</span></template></el-table-column>
      <el-table-column label="状态" width="130"><template #default="{ row }"><el-tag>{{ statusText(row.status) }}</el-tag></template></el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" @click="openDetail(row.id)">详情</el-button>
            <el-button v-if="row.status === 1" size="small" type="primary" @click="ship(row.id)">发货</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination layout="prev, pager, next, total" :total="total" :page-size="query.size" v-model:current-page="query.page" @current-change="load" />
    </div>

    <el-drawer v-model="drawerVisible" title="订单详情" size="520px">
      <template v-if="detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="收货信息">{{ detail.receiverName }} {{ detail.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ detail.receiverAddress }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="detail.items || []" style="margin-top: 18px">
          <el-table-column prop="productName" label="商品" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="totalPrice" label="小计" width="110" />
        </el-table>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminOrderDetail, getAdminOrders, shipOrder } from '../../api/order'

const loading = ref(false)
const orders = ref([])
const total = ref(0)
const detail = ref(null)
const drawerVisible = ref(false)
const query = reactive({ page: 1, size: 10, status: null })
const statusOptions = [
  { label: '待支付', value: 0 },
  { label: '待发货', value: 1 },
  { label: '已发货', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
]
const statusText = (status) => statusOptions.find((item) => item.value === status)?.label || '未知'

async function load() {
  loading.value = true
  try {
    const data = await getAdminOrders(query)
    orders.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function openDetail(id) {
  detail.value = await getAdminOrderDetail(id)
  drawerVisible.value = true
}

async function ship(id) {
  await shipOrder(id)
  ElMessage.success('订单已发货')
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

