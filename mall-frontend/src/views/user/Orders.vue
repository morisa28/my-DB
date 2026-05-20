<template>
  <div class="page orders-page">
    <div class="toolbar">
      <h1 class="section-title">我的订单</h1>
      <el-select v-model="query.status" placeholder="全部状态" clearable @change="load">
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
    </div>

    <el-table class="panel" :data="orders" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" min-width="220" />
      <el-table-column label="金额" width="120"><template #default="{ row }"><span class="price">￥{{ row.totalAmount }}</span></template></el-table-column>
      <el-table-column label="状态" width="130"><template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag></template></el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="340">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" @click="$router.push(`/orders/${row.id}`)">详情</el-button>
            <el-button v-if="row.status === 0" size="small" type="primary" @click="submitPayment(row)">付款备注</el-button>
            <el-button v-if="row.status === 0" size="small" type="danger" @click="cancel(row.id)">取消</el-button>
            <el-button v-if="row.status === 2" size="small" type="success" @click="receive(row.id)">确认收货</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination layout="prev, pager, next, total" :total="total" :page-size="query.size" v-model:current-page="query.page" @current-change="load" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import { ElMessageBox } from 'element-plus/es/components/message-box/index.mjs'
import { cancelOrder, confirmReceipt, getOrders, submitPaymentNote } from '../../api/order'

const loading = ref(false)
const orders = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, status: null })
const statusOptions = [
  { label: '待支付', value: 0 },
  { label: '待发货', value: 1 },
  { label: '已发货', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
]

function statusText(status) {
  return statusOptions.find((item) => item.value === status)?.label || '未知'
}

function statusType(status) {
  return ['warning', 'primary', 'success', 'success', 'info'][status] || 'info'
}

async function load() {
  loading.value = true
  try {
    const data = await getOrders(query)
    orders.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function submitPayment(row) {
  const { value } = await ElMessageBox.prompt('填写付款渠道、转账尾号或流水号', '付款备注', {
    inputType: 'textarea',
    inputValue: row.paymentNote || '',
    confirmButtonText: '提交',
    cancelButtonText: '取消',
    inputValidator: (value) => !value || value.length <= 255 || '付款备注不能超过255个字符'
  })
  await submitPaymentNote(row.id, { paymentNote: value || '' })
  ElMessage.success('付款备注已提交，等待管理员确认收款')
  await load()
}

async function cancel(id) {
  await ElMessageBox.confirm('取消待支付订单后将恢复商品库存，是否继续？', '取消订单')
  await cancelOrder(id)
  ElMessage.success('订单已取消')
  await load()
}

async function receive(id) {
  await ElMessageBox.confirm('确认已收到商品？确认后订单将完成。', '确认收货')
  await confirmReceipt(id)
  ElMessage.success('订单已完成')
  await load()
}

onMounted(load)
</script>

<style scoped>
.orders-page {
  display: grid;
  gap: 18px;
}

.pager {
  display: flex;
  justify-content: center;
}
</style>
