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
        <el-descriptions-item label="支付时间">{{ order.payTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发货时间">{{ order.shipTime || '-' }}</el-descriptions-item>
      </el-descriptions>
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
import { getOrderDetail } from '../../api/order'

const route = useRoute()
const order = ref(null)
const labels = ['待支付', '待发货', '已发货', '已完成', '已取消']
const statusText = (status) => labels[status] || '未知'

onMounted(async () => {
  order.value = await getOrderDetail(route.params.id)
})
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

