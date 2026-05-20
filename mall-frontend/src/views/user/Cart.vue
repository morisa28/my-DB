<template>
  <div class="page cart-page">
    <div class="toolbar">
      <h1 class="section-title">购物车</h1>
      <el-button :disabled="!items.length" @click="clear">清空购物车</el-button>
    </div>

    <el-table class="panel" :data="items" v-loading="loading" @selection-change="selected = $event">
      <el-table-column type="selection" width="48" />
      <el-table-column label="商品" min-width="280">
        <template #default="{ row }">
          <div class="goods-cell">
            <img :src="row.productImage" :alt="row.productName" />
            <div>
              <strong>{{ row.productName }}</strong>
              <p class="muted">库存 {{ row.stock }}</p>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="单价" width="120"><template #default="{ row }">￥{{ row.productPrice }}</template></el-table-column>
      <el-table-column label="数量" width="180">
        <template #default="{ row }">
          <el-input-number :model-value="row.quantity" :min="1" :max="row.stock" @change="(value) => update(row, value)" />
        </template>
      </el-table-column>
      <el-table-column label="小计" width="120"><template #default="{ row }"><span class="price">￥{{ row.totalPrice }}</span></template></el-table-column>
      <el-table-column label="操作" width="100"><template #default="{ row }"><el-button link type="danger" @click="remove(row.id)">删除</el-button></template></el-table-column>
    </el-table>

    <div class="checkout panel">
      <span>已选 {{ selected.length }} 件</span>
      <strong>合计：<span class="price">￥{{ totalAmount }}</span></strong>
      <el-button type="primary" :disabled="!selected.length" @click="goCheckout">去结算</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import { ElMessageBox } from 'element-plus/es/components/message-box/index.mjs'
import { clearCart, deleteCart, getCart, updateCart } from '../../api/cart'
import { useCartStore } from '../../store/cart'

const router = useRouter()
const cart = useCartStore()
const loading = ref(false)
const items = ref([])
const selected = ref([])
const totalAmount = computed(() => selected.value.reduce((sum, item) => sum + Number(item.totalPrice || 0), 0).toFixed(2))

async function load() {
  loading.value = true
  try {
    items.value = await getCart()
  } finally {
    loading.value = false
  }
}

async function update(row, quantity) {
  await updateCart(row.id, quantity)
  ElMessage.success('数量已更新')
  await load()
}

async function remove(id) {
  await deleteCart(id)
  await cart.refreshCount()
  await load()
}

async function clear() {
  await ElMessageBox.confirm('确认清空购物车？', '提示')
  await clearCart()
  await cart.refreshCount()
  await load()
}

function goCheckout() {
  router.push({ path: '/checkout', query: { ids: selected.value.map((item) => item.id).join(',') } })
}

onMounted(load)
</script>

<style scoped>
.cart-page {
  display: grid;
  gap: 18px;
}

.goods-cell {
  display: flex;
  gap: 12px;
  align-items: center;
}

.goods-cell img {
  width: 72px;
  height: 54px;
  object-fit: cover;
  border-radius: 6px;
}

.goods-cell p {
  margin: 6px 0 0;
}

.checkout {
  padding: 16px;
  display: flex;
  gap: 22px;
  justify-content: flex-end;
  align-items: center;
}
</style>
