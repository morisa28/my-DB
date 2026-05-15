<template>
  <div class="admin-page">
    <div class="toolbar">
      <h1 class="section-title">分类管理</h1>
      <el-button type="primary" @click="openDialog()">新增分类</el-button>
    </div>
    <el-table class="panel" :data="categories">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="420px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createCategory, deleteCategory, getAdminCategories, updateCategory } from '../../api/product'

const categories = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, name: '', sortOrder: 0, status: 1 })

async function load() {
  categories.value = await getAdminCategories()
}

function openDialog(row) {
  Object.assign(form, row || { id: null, name: '', sortOrder: 0, status: 1 })
  dialogVisible.value = true
}

async function submit() {
  if (form.id) await updateCategory(form.id, form)
  else await createCategory(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await load()
}

async function remove(id) {
  await ElMessageBox.confirm('确认删除或停用该分类？', '提示')
  await deleteCategory(id)
  await load()
}

onMounted(load)
</script>

<style scoped>
.admin-page {
  display: grid;
  gap: 18px;
}
</style>

