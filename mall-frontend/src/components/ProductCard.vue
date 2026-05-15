<template>
  <article class="product-card panel" @click="$router.push(`/products/${product.id}`)">
    <img :src="product.imageUrl" :alt="product.name" />
    <div class="card-body">
      <h3>{{ product.name }}</h3>
      <p>{{ product.description }}</p>
      <div class="card-foot">
        <span class="price">￥{{ product.price }}</span>
        <span class="muted">库存 {{ product.stock }} · 销量 {{ product.sales }}</span>
      </div>
    </div>
  </article>
</template>

<script setup>
defineProps({
  product: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.product-card {
  overflow: hidden;
  cursor: pointer;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease;
}

.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 16px 34px rgba(22, 34, 51, 0.1);
}

img {
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  display: block;
}

.card-body {
  padding: 16px;
}

h3 {
  margin: 0 0 8px;
  font-size: 16px;
  line-height: 1.35;
}

p {
  height: 40px;
  margin: 0 0 14px;
  color: #718096;
  font-size: 13px;
  line-height: 1.5;
  overflow: hidden;
}

.card-foot {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  font-size: 13px;
}
</style>

