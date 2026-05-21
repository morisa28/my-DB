<template>
  <div class="page checkout-page">
    <h1 class="section-title">确认订单</h1>

    <section class="panel block">
      <div class="toolbar">
        <h2>选择收货地址</h2>
        <el-button @click="$router.push('/address')">管理地址</el-button>
      </div>
      <el-radio-group v-model="addressId" class="address-list">
        <el-radio v-for="item in addresses" :key="item.id" :value="item.id" border>
          {{ item.receiverName }} {{ item.receiverPhone }} · {{ item.province }}{{ item.city }}{{ item.detailAddress }}
        </el-radio>
      </el-radio-group>
    </section>

    <section class="panel block">
      <h2>商品清单</h2>
      <el-table :data="selectedItems">
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column label="小计" width="140"><template #default="{ row }">￥{{ row.totalPrice }}</template></el-table-column>
      </el-table>
    </section>

    <section class="panel block">
      <h2>线下付款说明</h2>
      <div class="payment-lines">
        <div>提交订单后进入待支付状态。</div>
        <div>付款后在订单详情填写付款渠道、转账尾号或流水号。</div>
        <div>管理员确认收款后安排发货。</div>
      </div>
    </section>

    <div class="submit panel">
      <strong>应付金额：<span class="price">￥{{ totalAmount }}</span></strong>
      <el-button type="primary" :disabled="!addressId || !selectedItems.length" :loading="submitting" @click="submit">提交订单</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import { getAddresses } from '../../api/address'
import { getCart } from '../../api/cart'
import { createOrder } from '../../api/order'
import { useCartStore } from '../../store/cart'

const route = useRoute()
const router = useRouter()
const cart = useCartStore()
const addresses = ref([])
const cartItems = ref([])
const addressId = ref(null)
const submitting = ref(false)
const requestId = ref(createRequestId())
const selectedIds = computed(() => String(route.query.ids || '').split(',').filter(Boolean).map(Number))
const selectedItems = computed(() => cartItems.value.filter((item) => selectedIds.value.includes(item.id)))
const totalAmount = computed(() => selectedItems.value.reduce((sum, item) => sum + Number(item.totalPrice || 0), 0).toFixed(2))

async function load() {
  addresses.value = await getAddresses()
  addressId.value = addresses.value.find((item) => item.isDefault === 1)?.id || addresses.value[0]?.id || null
  cartItems.value = await getCart()
}

async function submit() {
  submitting.value = true
  try {
    const order = await createOrder({
      requestId: requestId.value,
      addressId: addressId.value,
      cartItemIds: selectedIds.value
    })
    await cart.refreshCount()
    ElMessage.success('订单创建成功')
    router.push(`/orders/${order.orderId}`)
  } finally {
    submitting.value = false
  }
}

function createRequestId() {
  if (window.crypto?.randomUUID) {
    return window.crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(36).slice(2, 12)}`
}

onMounted(load)
</script>

<style scoped>
.checkout-page {
  display: grid;
  gap: 18px;
}

.block {
  padding: 20px;
}

h2 {
  margin: 0 0 16px;
  font-size: 18px;
}

.address-list {
  display: grid;
  gap: 12px;
}

.submit {
  padding: 18px;
  display: flex;
  justify-content: flex-end;
  gap: 20px;
  align-items: center;
}

.payment-lines {
  display: grid;
  gap: 8px;
  color: #4b5563;
  line-height: 1.7;
}

@media (max-width: 680px) {
  .submit {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
