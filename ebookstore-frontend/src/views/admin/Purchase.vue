<template>
  <div class="admin-purchase">
    <div class="page-header">
      <h2 class="page-title">进货管理</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon> 新增进货
      </el-button>
    </div>

    <el-table :data="purchases" style="width: 100%" v-loading="loading">
      <el-table-column prop="purchaseNo" label="进货单号" width="180" />
      <el-table-column prop="supplier" label="供应商" width="150" />
      <el-table-column label="总成本" width="120">
        <template #default="{ row }">
          ¥{{ row.totalCost }}
        </template>
      </el-table-column>
      <el-table-column label="商品数量" width="100">
        <template #default="{ row }">
          {{ row.items?.length || 0 }} 种
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="进货时间" width="160">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link @click="viewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showAddDialog" title="新增进货" width="700px">
      <el-form :model="purchaseForm" label-width="80px">
        <el-form-item label="供应商">
          <el-input v-model="purchaseForm.supplier" placeholder="请输入供应商名称" />
        </el-form-item>

        <el-form-item label="商品列表">
          <div class="purchase-items">
            <div v-for="(item, index) in purchaseForm.items" :key="index" class="purchase-item">
              <el-input-number
                v-model="item.bookId"
                placeholder="图书ID"
                :min="1"
                controls-position="right"
                style="width: 100px;"
              />
              <el-input
                v-model="item.bookTitle"
                placeholder="书名"
                style="width: 180px;"
                disabled
              />
              <el-input-number
                v-model="item.quantity"
                :min="1"
                controls-position="right"
                placeholder="数量"
                style="width: 100px;"
              />
              <el-input-number
                v-model="item.costPrice"
                :min="0"
                :precision="2"
                controls-position="right"
                placeholder="成本单价"
                style="width: 120px;"
              />
              <el-button type="danger" @click="removeItem(index)" :disabled="purchaseForm.items.length === 1">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
          <el-button type="primary" link @click="addItem">
            <el-icon><Plus /></el-icon> 添加商品
          </el-button>
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="purchaseForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPurchase">
          确认进货
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDetailDialog" title="进货详情" width="600px">
      <div v-if="currentPurchase">
        <p>进货单号：{{ currentPurchase.purchaseNo }}</p>
        <p>供应商：{{ currentPurchase.supplier || '-' }}</p>
        <p>总成本：¥{{ currentPurchase.totalCost }}</p>
        <p>备注：{{ currentPurchase.remark || '-' }}</p>

        <el-table :data="currentPurchase.items" style="width: 100%; margin-top: 16px;">
          <el-table-column prop="bookTitle" label="书名" min-width="200" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="成本单价" width="100">
            <template #default="{ row }">
              ¥{{ row.costPrice }}
            </template>
          </el-table-column>
          <el-table-column label="小计" width="100">
            <template #default="{ row }">
              ¥{{ row.subtotal }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPurchases, createPurchase } from '@/api/admin'
import type { Purchase } from '@/api/types'

const loading = ref(false)
const submitting = ref(false)
const purchases = ref<Purchase[]>([])
const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const currentPurchase = ref<Purchase | null>(null)

const purchaseForm = ref({
  supplier: '',
  remark: '',
  items: [
    { bookId: null, bookTitle: '', quantity: 1, costPrice: 0 }
  ]
})

onMounted(() => {
  loadPurchases()
})

const loadPurchases = async () => {
  loading.value = true
  try {
    const res = await getPurchases()
    purchases.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const addItem = () => {
  purchaseForm.value.items.push({ bookId: null, bookTitle: '', quantity: 1, costPrice: 0 })
}

const removeItem = (index: number) => {
  purchaseForm.value.items.splice(index, 1)
}

const submitPurchase = async () => {
  const valid = purchaseForm.value.items.every(item => item.bookId && item.quantity > 0)
  if (!valid) {
    ElMessage.warning('请填写完整的商品信息')
    return
  }

  submitting.value = true
  try {
    await createPurchase(purchaseForm.value)
    ElMessage.success('进货成功')
    showAddDialog.value = false
    purchaseForm.value = {
      supplier: '',
      remark: '',
      items: [{ bookId: null, bookTitle: '', quantity: 1, costPrice: 0 }]
    }
    loadPurchases()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const viewDetail = (row: Purchase) => {
  currentPurchase.value = row
  showDetailDialog.value = true
}

const formatTime = (time?: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.admin-purchase {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  color: #333;
}

.purchase-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 10px;
}

.purchase-item {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
