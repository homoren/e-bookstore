<template>
  <div class="dashboard">
    <h2 class="page-title">店主后台管理</h2>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background: #ecf5ff;">
          <el-icon :size="32" color="#409eff"><ShoppingBag /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ todayData.orderCount }}</div>
          <div class="stat-label">今日订单数</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: #fef0f0;">
          <el-icon :size="32" color="#f56c6c"><Money /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">¥{{ todayData.totalSales?.toFixed(2) || '0.00' }}</div>
          <div class="stat-label">今日销售额</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: #f0f9eb;">
          <el-icon :size="32" color="#67c23a"><TrendCharts /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">¥{{ todayData.totalProfit?.toFixed(2) || '0.00' }}</div>
          <div class="stat-label">今日利润</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: #fdf6ec;">
          <el-icon :size="32" color="#e6a23c"><UserFilled /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ customerCount }}</div>
          <div class="stat-label">会员总数</div>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <h3>快捷操作</h3>
      <div class="action-buttons">
        <el-button type="primary" @click="router.push('/admin/orders')">
          <el-icon><List /></el-icon> 订单管理
        </el-button>
        <el-button type="success" @click="router.push('/admin/purchase')">
          <el-icon><Plus /></el-icon> 进货管理
        </el-button>
        <el-button type="warning" @click="router.push('/admin/books')">
          <el-icon><Reading /></el-icon> 图书管理
        </el-button>
        <el-button type="info" @click="router.push('/admin/customers')">
          <el-icon><User /></el-icon> 客户管理
        </el-button>
      </div>
    </div>

    <div class="recent-orders">
      <div class="section-header">
        <h3>待处理订单</h3>
        <el-button link @click="router.push('/admin/orders')">查看全部</el-button>
      </div>

      <el-table :data="pendingOrders" style="width: 100%" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="receiverName" label="收货人" width="120" />
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            ¥{{ row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column label="下单时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="primary" size="small" @click="handleConfirmPayment(row.id)">
              确认收款
            </el-button>
            <el-button v-if="row.status === 2" type="success" size="small" @click="handleConfirmDelivery(row.id)">
              确认配送
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTodayStats } from '@/api/admin'
import { getAllOrders, confirmPayment, confirmDelivery } from '@/api/admin'
import type { Order } from '@/api/types'

const router = useRouter()

const loading = ref(false)
const todayData = ref({
  orderCount: 0,
  totalSales: 0,
  totalProfit: 0
})
const customerCount = ref(0)
const pendingOrders = ref<Order[]>([])

onMounted(async () => {
  await loadTodayData()
  await loadPendingOrders()
})

const loadTodayData = async () => {
  try {
    const res = await getTodayStats()
    const stats = res.data
    if (stats) {
      todayData.value = {
        orderCount: stats.orderCount ?? 0,
        totalSales: stats.totalSales ?? 0,
        totalProfit: stats.totalProfit ?? 0
      }
      customerCount.value = stats.memberCount ?? 0
    }
  } catch (error) {
    console.error(error)
  }
}

const loadPendingOrders = async () => {
  loading.value = true
  try {
    const res = await getAllOrders()
    const orders = res.data || []
    pendingOrders.value = orders.filter(o => o.status === 1 || o.status === 2).slice(0, 5)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleConfirmPayment = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认已收到该订单的汇款？', '确认收款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await confirmPayment(id)
    ElMessage.success('已确认收款')
    loadPendingOrders()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleConfirmDelivery = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认该订单已配送？', '确认配送', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await confirmDelivery(id)
    ElMessage.success('已确认配送')
    loadPendingOrders()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '待付款',
    1: '待汇款确认',
    2: '待配送',
    3: '已配送',
    4: '已完成',
    5: '已取消'
  }
  return map[status] || '未知'
}

const getStatusType = (status: number) => {
  const map: Record<number, 'warning' | 'info' | 'primary' | 'success' | 'danger'> = {
    0: 'warning',
    1: 'info',
    2: 'primary',
    3: 'primary',
    4: 'success',
    5: 'info'
  }
  return map[status] || 'info'
}

const formatTime = (time?: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  margin-bottom: 24px;
  color: #333;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

.quick-actions {
  margin-bottom: 30px;
}

.quick-actions h3 {
  margin-bottom: 16px;
  color: #333;
}

.action-buttons {
  display: flex;
  gap: 15px;
}

.recent-orders {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  color: #333;
}
</style>
