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
      <el-table-column label="状态" width="130"><template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag></template></el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" @click="openDetail(row.id)">详情</el-button>
            <el-button v-if="row.status === 0" size="small" type="success" @click="confirmMoney(row.id)">确认收款</el-button>
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
          <el-descriptions-item label="状态">{{ statusText(detail.status) }}</el-descriptions-item>
          <el-descriptions-item label="金额">￥{{ detail.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="付款备注">{{ detail.paymentNote || '-' }}</el-descriptions-item>
          <el-descriptions-item label="管理员备注">{{ detail.adminRemark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="物流单号">{{ detail.shippingNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货信息">{{ detail.receiverName }} {{ detail.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ detail.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ detail.payTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ detail.shipTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ detail.confirmTime || detail.finishTime || '-' }}</el-descriptions-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { confirmPayment, getAdminOrderDetail, getAdminOrders, shipOrder } from '../../api/order'

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
const statusType = (status) => ['warning', 'primary', 'success', 'success', 'info'][status] || 'info'

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

async function confirmMoney(id) {
  const { value } = await ElMessageBox.prompt('确认已收到该订单款项，可填写管理员备注', '确认收款', {
    inputType: 'textarea',
    confirmButtonText: '确认收款',
    cancelButtonText: '取消',
    inputValidator: (value) => !value || value.length <= 255 || '管理员备注不能超过255个字符'
  })
  await confirmPayment(id, { adminRemark: value || '' })
  ElMessage.success('已确认收款')
  await load()
}

async function ship(id) {
  const { value } = await ElMessageBox.prompt('填写物流单号或配送编号', '订单发货', {
    confirmButtonText: '发货',
    cancelButtonText: '取消',
    inputPattern: /^.{1,64}$/,
    inputErrorMessage: '物流单号不能为空且不能超过64个字符'
  })
  await shipOrder(id, { shippingNo: value })
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
