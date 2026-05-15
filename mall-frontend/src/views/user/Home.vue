<template>
  <div class="page home-page">
    <section class="hero panel">
      <div>
        <h1>校园优选商城</h1>
        <p>围绕数据库课程大作业设计的可演示商城，覆盖商品、购物车、订单事务和后台统计。</p>
      </div>
      <el-button type="primary" size="large" @click="$router.push('/cart')">查看购物车</el-button>
    </section>

    <section class="filters panel">
      <el-input v-model="query.keyword" placeholder="搜索商品名称" clearable @keyup.enter="loadProducts">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable>
        <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-button type="primary" @click="loadProducts">筛选</el-button>
    </section>

    <el-skeleton :loading="loading" animated :count="8">
      <template #template>
        <div class="grid"><el-skeleton-item v-for="i in 8" :key="i" variant="rect" style="height: 280px" /></div>
      </template>
      <div v-if="products.length" class="grid">
        <ProductCard v-for="item in products" :key="item.id" :product="item" />
      </div>
      <el-empty v-else class="empty-wrap" description="暂无商品" />
    </el-skeleton>

    <div class="pager">
      <el-pagination
        layout="prev, pager, next, total"
        :total="total"
        :page-size="query.size"
        v-model:current-page="query.page"
        @current-change="loadProducts"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import ProductCard from '../../components/ProductCard.vue'
import { getCategories, getProducts } from '../../api/product'

const loading = ref(false)
const categories = ref([])
const products = ref([])
const total = ref(0)
const query = reactive({
  page: 1,
  size: 8,
  keyword: '',
  categoryId: null
})

watch(
  () => query.categoryId,
  () => {
    query.page = 1
    loadProducts()
  }
)

async function loadCategories() {
  categories.value = await getCategories()
}

async function loadProducts() {
  loading.value = true
  try {
    const data = await getProducts(query)
    products.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.home-page {
  display: grid;
  gap: 22px;
}

.hero {
  min-height: 180px;
  padding: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  background:
    linear-gradient(135deg, rgba(37, 99, 235, 0.12), rgba(16, 185, 129, 0.1)),
    #ffffff;
}

.hero h1 {
  margin: 0 0 10px;
  font-size: 34px;
  line-height: 1.2;
}

.hero p {
  max-width: 620px;
  margin: 0;
  color: #526173;
  line-height: 1.8;
}

.filters {
  padding: 16px;
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 200px auto;
  gap: 12px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.pager {
  display: flex;
  justify-content: center;
  padding-top: 6px;
}

@media (max-width: 1000px) {
  .grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .hero,
  .filters {
    grid-template-columns: 1fr;
    display: grid;
  }

  .hero h1 {
    font-size: 28px;
  }

  .grid {
    grid-template-columns: 1fr;
  }
}
</style>

