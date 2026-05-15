<template>
  <div class="page address-page">
    <div class="toolbar">
      <h1 class="section-title">收货地址</h1>
      <el-button type="primary" @click="openDialog()">新增地址</el-button>
    </div>

    <div v-if="addresses.length" class="address-grid">
      <article v-for="item in addresses" :key="item.id" class="panel address-card">
        <div class="toolbar">
          <strong>{{ item.receiverName }} · {{ item.receiverPhone }}</strong>
          <el-tag v-if="item.isDefault === 1" type="success">默认</el-tag>
        </div>
        <p>{{ item.province }}{{ item.city }}{{ item.detailAddress }}</p>
        <div class="table-actions">
          <el-button size="small" @click="openDialog(item)">编辑</el-button>
          <el-button size="small" :disabled="item.isDefault === 1" @click="makeDefault(item.id)">设为默认</el-button>
          <el-button size="small" type="danger" @click="remove(item.id)">删除</el-button>
        </div>
      </article>
    </div>
    <el-empty v-else class="empty-wrap" description="还没有收货地址" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑地址' : '新增地址'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="收货人" prop="receiverName"><el-input v-model="form.receiverName" /></el-form-item>
        <el-form-item label="手机号" prop="receiverPhone"><el-input v-model="form.receiverPhone" /></el-form-item>
        <el-form-item label="省份"><el-input v-model="form.province" /></el-form-item>
        <el-form-item label="城市"><el-input v-model="form.city" /></el-form-item>
        <el-form-item label="详细地址" prop="detailAddress"><el-input v-model="form.detailAddress" /></el-form-item>
        <el-form-item label="默认地址"><el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" /></el-form-item>
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
import { createAddress, deleteAddress, getAddresses, setDefaultAddress, updateAddress } from '../../api/address'

const addresses = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  detailAddress: '',
  isDefault: 0
})
const rules = {
  receiverName: [{ required: true, message: '请输入收货人' }],
  receiverPhone: [{ required: true, message: '请输入手机号' }],
  detailAddress: [{ required: true, message: '请输入详细地址' }]
}

async function load() {
  addresses.value = await getAddresses()
}

function resetForm() {
  Object.assign(form, { id: null, receiverName: '', receiverPhone: '', province: '', city: '', detailAddress: '', isDefault: 0 })
}

function openDialog(row) {
  resetForm()
  if (row) Object.assign(form, row)
  dialogVisible.value = true
}

async function submit() {
  await formRef.value.validate()
  const payload = { ...form }
  if (form.id) await updateAddress(form.id, payload)
  else await createAddress(payload)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await load()
}

async function makeDefault(id) {
  await setDefaultAddress(id)
  await load()
}

async function remove(id) {
  await ElMessageBox.confirm('确认删除该地址？', '提示')
  await deleteAddress(id)
  await load()
}

onMounted(load)
</script>

<style scoped>
.address-page {
  display: grid;
  gap: 18px;
}

.address-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.address-card {
  padding: 18px;
}

.address-card p {
  color: #526173;
}

@media (max-width: 760px) {
  .address-grid {
    grid-template-columns: 1fr;
  }
}
</style>

