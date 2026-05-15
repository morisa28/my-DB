<template>
  <div class="admin-page">
    <h1 class="section-title">统计概览</h1>
    <div class="stats-grid">
      <div v-for="card in cards" :key="card.label" class="panel stat-card">
        <span class="muted">{{ card.label }}</span>
        <strong>{{ card.value }}</strong>
      </div>
    </div>
    <div class="dash-grid">
      <section class="panel block">
        <h2>低库存商品</h2>
        <el-table :data="stats.lowStockProducts || []">
          <el-table-column prop="productName" label="商品" />
          <el-table-column prop="stock" label="库存" width="100" />
        </el-table>
      </section>
      <section class="panel block">
        <h2>销量排行 Top 5</h2>
        <el-table :data="stats.topProducts || []">
          <el-table-column prop="productName" label="商品" />
          <el-table-column prop="sales" label="销量" width="100" />
          <el-table-column prop="salesAmount" label="销售额" width="120" />
        </el-table>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getStatistics } from '../../api/order'

const stats = ref({})
const cards = computed(() => [
  { label: '总订单数', value: stats.value.totalOrders ?? 0 },
  { label: '总销售额', value: `￥${stats.value.totalSalesAmount ?? 0}` },
  { label: '待发货订单', value: stats.value.waitingShipOrders ?? 0 },
  { label: '商品总数', value: stats.value.totalProducts ?? 0 }
])

onMounted(async () => {
  stats.value = await getStatistics()
})
</script>

<style scoped>
.admin-page {
  display: grid;
  gap: 18px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  padding: 20px;
  display: grid;
  gap: 10px;
}

.stat-card strong {
  font-size: 28px;
}

.dash-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.block {
  padding: 18px;
}

h2 {
  margin: 0 0 14px;
  font-size: 18px;
}

@media (max-width: 960px) {
  .stats-grid,
  .dash-grid {
    grid-template-columns: 1fr;
  }
}
</style>

