<template>
  <div class="page">
    <el-skeleton :loading="loading" animated>
      <section v-if="product" class="detail panel">
        <img :src="product.imageUrl" :alt="product.name" />
        <div class="info">
          <el-tag>{{ product.categoryName }}</el-tag>
          <h1>{{ product.name }}</h1>
          <p class="desc">{{ product.description }}</p>
          <div class="price big">￥{{ product.price }}</div>
          <div class="meta">
            <span>库存 {{ product.stock }}</span>
            <span>销量 {{ product.sales }}</span>
          </div>
          <div class="buy">
            <el-input-number v-model="quantity" :min="1" :max="product.stock" />
            <el-button type="primary" :disabled="product.stock <= 0" @click="add">加入购物车</el-button>
          </div>
        </div>
      </section>
    </el-skeleton>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus/es/components/message/index.mjs'
import { getProductDetail } from '../../api/product'
import { addCart } from '../../api/cart'
import { useAuthStore } from '../../store/auth'
import { useCartStore } from '../../store/cart'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const loading = ref(false)
const product = ref(null)
const quantity = ref(1)

async function load() {
  loading.value = true
  try {
    product.value = await getProductDetail(route.params.id)
  } finally {
    loading.value = false
  }
}

async function add() {
  if (!auth.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  await addCart({ productId: product.value.id, quantity: quantity.value })
  await cart.refreshCount()
  ElMessage.success('已加入购物车')
}

onMounted(load)
</script>

<style scoped>
.detail {
  display: grid;
  grid-template-columns: minmax(320px, 520px) 1fr;
  gap: 34px;
  padding: 28px;
}

img {
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  border-radius: 8px;
}

h1 {
  margin: 16px 0 12px;
  font-size: 30px;
}

.desc {
  color: #526173;
  line-height: 1.8;
}

.big {
  margin: 18px 0;
  font-size: 30px;
}

.meta,
.buy {
  display: flex;
  gap: 14px;
  align-items: center;
}

.meta {
  margin-bottom: 24px;
  color: #718096;
}

@media (max-width: 780px) {
  .detail {
    grid-template-columns: 1fr;
  }
}
</style>
