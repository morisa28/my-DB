<template>
  <div class="admin-page">
    <div class="toolbar">
      <h1 class="section-title">商品管理</h1>
      <div class="table-actions">
        <el-input v-model="query.keyword" placeholder="搜索商品" clearable style="width: 220px" @keyup.enter="load" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="load">
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
        <el-button type="primary" @click="openDialog()">新增商品</el-button>
      </div>
    </div>

    <el-table class="panel" :data="products" v-loading="loading">
      <el-table-column label="商品" min-width="260">
        <template #default="{ row }">
          <div class="goods-cell"><img :src="row.imageUrl" :alt="row.name" /><div><strong>{{ row.name }}</strong><p class="muted">{{ row.categoryName }}</p></div></div>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="110"><template #default="{ row }">￥{{ row.price }}</template></el-table-column>
      <el-table-column prop="stock" label="库存" width="90" />
      <el-table-column prop="sales" label="销量" width="90" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-switch :model-value="row.status" :active-value="1" :inactive-value="0" @change="(value) => changeStatus(row, value)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="210">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" @click="openStock(row)">库存</el-button>
            <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination layout="prev, pager, next, total" :total="total" :page-size="query.size" v-model:current-page="query.page" @current-change="load" />
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="680px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" style="width: 100%"><el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" /></el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="库存" prop="stock"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="图片"><el-input v-model="form.imageUrl" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" rows="3" /></el-form-item>
        <el-form-item label="上架"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="stockVisible" title="修改库存" width="360px">
      <el-input-number v-model="stockForm.stock" :min="0" />
      <template #footer>
        <el-button @click="stockVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStock">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createProduct,
  deleteProduct,
  getAdminCategories,
  getAdminProducts,
  updateProduct,
  updateProductStatus,
  updateProductStock
} from '../../api/product'

const loading = ref(false)
const products = ref([])
const categories = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const stockVisible = ref(false)
const formRef = ref()
const query = reactive({ page: 1, size: 10, keyword: '', status: null })
const form = reactive({ id: null, name: '', categoryId: null, price: 0, stock: 0, imageUrl: '', description: '', status: 1 })
const stockForm = reactive({ id: null, stock: 0 })
const rules = {
  name: [{ required: true, message: '请输入商品名称' }],
  categoryId: [{ required: true, message: '请选择分类' }],
  price: [{ required: true, message: '请输入价格' }],
  stock: [{ required: true, message: '请输入库存' }]
}

async function load() {
  loading.value = true
  try {
    const data = await getAdminProducts(query)
    products.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  categories.value = await getAdminCategories()
}

function openDialog(row) {
  Object.assign(form, row || { id: null, name: '', categoryId: null, price: 0, stock: 0, imageUrl: '', description: '', status: 1 })
  dialogVisible.value = true
}

async function submit() {
  await formRef.value.validate()
  if (form.id) await updateProduct(form.id, form)
  else await createProduct(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await load()
}

async function changeStatus(row, status) {
  await updateProductStatus(row.id, status)
  await load()
}

function openStock(row) {
  Object.assign(stockForm, { id: row.id, stock: row.stock })
  stockVisible.value = true
}

async function submitStock() {
  await updateProductStock(stockForm.id, stockForm.stock)
  stockVisible.value = false
  await load()
}

async function remove(id) {
  await ElMessageBox.confirm('确认下架该商品？', '提示')
  await deleteProduct(id)
  await load()
}

onMounted(() => {
  loadCategories()
  load()
})
</script>

<style scoped>
.admin-page {
  display: grid;
  gap: 18px;
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

.goods-cell p {
  margin: 4px 0 0;
}

.pager {
  display: flex;
  justify-content: center;
}
</style>

