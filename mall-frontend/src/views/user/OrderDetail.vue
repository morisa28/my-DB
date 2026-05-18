<template>
  <div class="page detail-page">
    <section v-if="order" class="panel block">
      <div class="toolbar">
        <h1 class="section-title">订单详情</h1>
        <el-tag>{{ statusText(order.status) }}</el-tag>
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">￥{{ order.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ order.receiverName }} {{ order.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址">{{ order.receiverAddress }}</el-descriptions-item>
        <el-descriptions-item label="付款备注">{{ order.paymentNote || '-' }}</el-descriptions-item>
        <el-descriptions-item label="物流单号">{{ order.shippingNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ order.payTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发货时间">{{ order.shipTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="确认收货时间">{{ order.confirmTime || order.finishTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="取消时间">{{ order.cancelTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="管理员备注">{{ order.adminRemark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div class="detail-actions">
        <el-button v-if="order.status === 0" type="primary" @click="submitPayment">付款备注</el-button>
        <el-button v-if="order.status === 0" type="danger" @click="cancel">取消订单</el-button>
        <el-button v-if="order.status === 2" type="success" @click="receive">确认收货</el-button>
      </div>
    </section>

    <section v-if="order?.status === 0" class="panel block">
      <h2>线下付款说明</h2>
      <div class="payment-info">
        <div>订单金额：<strong>￥{{ order.totalAmount }}</strong></div>
        <div>付款备注：请填写订单号 {{ order.orderNo }}</div>
        <div>收款确认：管理员核对到账后，订单会进入待发货。</div>
      </div>
    </section>

    <section v-if="order" class="panel block">
      <h2>订单明细</h2>
      <el-table :data="order.items || []">
        <el-table-column label="商品" min-width="260">
          <template #default="{ row }">
            <div class="goods-cell"><img :src="row.productImage" :alt="row.productName" /><span>{{ row.productName }}</span></div>
          </template>
        </el-table-column>
        <el-table-column prop="productPrice" label="下单单价" width="130" />
        <el-table-column prop="quantity" label="数量" width="90" />
        <el-table-column prop="totalPrice" label="小计" width="120" />
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelOrder, confirmReceipt, getOrderDetail, submitPaymentNote } from '../../api/order'

const route = useRoute()
const order = ref(null)
const labels = ['待支付', '待发货', '已发货', '已完成', '已取消']
const statusText = (status) => labels[status] || '未知'

async function load() {
  order.value = await getOrderDetail(route.params.id)
}

async function submitPayment() {
  const { value } = await ElMessageBox.prompt('填写付款渠道、转账尾号或流水号', '付款备注', {
    inputType: 'textarea',
    inputValue: order.value?.paymentNote || '',
    confirmButtonText: '提交',
    cancelButtonText: '取消',
    inputValidator: (value) => !value || value.length <= 255 || '付款备注不能超过255个字符'
  })
  await submitPaymentNote(route.params.id, { paymentNote: value || '' })
  ElMessage.success('付款备注已提交，等待管理员确认收款')
  await load()
}

async function cancel() {
  await ElMessageBox.confirm('取消待支付订单后将恢复商品库存，是否继续？', '取消订单')
  await cancelOrder(route.params.id)
  ElMessage.success('订单已取消')
  await load()
}

async function receive() {
  await ElMessageBox.confirm('确认已收到商品？确认后订单将完成。', '确认收货')
  await confirmReceipt(route.params.id)
  ElMessage.success('订单已完成')
  await load()
}

onMounted(load)
</script>

<style scoped>
.detail-page {
  display: grid;
  gap: 18px;
}

.block {
  padding: 20px;
}

h2 {
  margin: 0 0 16px;
}

.detail-actions {
  margin-top: 18px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.payment-info {
  display: grid;
  gap: 8px;
  color: #4b5563;
  line-height: 1.7;
}

.goods-cell {
  display: flex;
  gap: 12px;
  align-items: center;
}

.goods-cell img {
  width: 64px;
  height: 48px;
  border-radius: 6px;
  object-fit: cover;
}
</style>
