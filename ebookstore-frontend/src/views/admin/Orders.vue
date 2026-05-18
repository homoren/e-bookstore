<template>
  <div class="admin-orders">
    <div class="page-header">
      <h2 class="page-title">订单管理</h2>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="filterStatus" @change="loadOrders">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="0">待付款</el-radio-button>
        <el-radio-button value="1">待汇款确认</el-radio-button>
        <el-radio-button value="2">待配送</el-radio-button>
        <el-radio-button value="3">已配送</el-radio-button>
        <el-radio-button value="4">已完成</el-radio-button>
        <el-radio-button value="5">已取消</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="orders" style="width: 100%" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="receiverName" label="收货人" width="100" />
      <el-table-column prop="receiverPhone" label="联系电话" width="130" />
      <el-table-column label="收货地址" min-width="200" :show-overflow-tooltip="true">
        <template #default="{ row }">
          {{ row.receiverAddress }}
        </template>
      </el-table-column>
      <el-table-column label="金额" width="100">
        <template #default="{ row }">
          ¥{{ row.totalAmount }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" width="160">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link @click="viewDetail(row.id)">详情</el-button>
          <el-button v-if="row.status === 1" type="primary" size="small" @click="handleConfirmPayment(row.id)">
            确认收款
          </el-button>
          <el-button v-if="row.status === 2" type="success" size="small" @click="handleConfirmDelivery(row.id)">
            确认配送
          </el-button>
          <el-button v-if="row.status === 3" type="warning" size="small" @click="handleComplete(row)">
            完成订单
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="完成订单" width="400px">
      <p>确认已完成配送并收到客户签收回执？</p>
      <el-form>
        <el-form-item label="回执备注">
          <el-input v-model="receiptRemark" placeholder="可为空" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmComplete">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllOrders, confirmPayment, confirmDelivery, completeOrder } from '@/api/admin'

const router = useRouter()

const loading = ref(false)
const orders = ref([])
const filterStatus = ref('all')
const dialogVisible = ref(false)
const receiptRemark = ref('')
const currentCompleteId = ref(null)

onMounted(() => {
  loadOrders()
})

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getAllOrders()
    let all = res.data || []
    if (filterStatus.value !== 'all') {
      all = all.filter(o => o.status === parseInt(filterStatus.value))
    }
    orders.value = all
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const viewDetail = (id) => {
  router.push(`/order/${id}`)
}

const handleConfirmPayment = async (id) => {
  try {
    await ElMessageBox.confirm('确认已收到该订单的汇款？', '确认收款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await confirmPayment(id)
    ElMessage.success('已确认收款')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleConfirmDelivery = async (id) => {
  try {
    await ElMessageBox.confirm('确认该订单已配送？', '确认配送', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await confirmDelivery(id)
    ElMessage.success('已确认配送')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleComplete = (row) => {
  currentCompleteId.value = row.id
  receiptRemark.value = ''
  dialogVisible.value = true
}

const confirmComplete = async () => {
  try {
    await completeOrder(currentCompleteId.value, receiptRemark.value)
    ElMessage.success('订单已完成')
    dialogVisible.value = false
    loadOrders()
  } catch (error) {
    console.error(error)
  }
}

const getStatusText = (status) => {
  const map = {
    0: '待付款',
    1: '待汇款确认',
    2: '待配送',
    3: '已配送',
    4: '已完成',
    5: '已取消'
  }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'info',
    2: 'primary',
    3: 'primary',
    4: 'success',
    5: 'info'
  }
  return map[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.admin-orders {
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

.filter-bar {
  margin-bottom: 20px;
}
</style>
