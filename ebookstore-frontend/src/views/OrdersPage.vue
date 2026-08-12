<template>
  <div class="orders-page">
    <Header />

    <div class="container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>我的订单</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="orders-container">
        <h2 class="page-title">我的订单</h2>

        <el-tabs v-model="activeStatus" @tab-change="handleStatusChange">
          <el-tab-pane label="全部" name="all" />
          <el-tab-pane label="待付款" name="0" />
          <el-tab-pane label="待汇款确认" name="1" />
          <el-tab-pane label="待配送" name="2" />
          <el-tab-pane label="已配送" name="3" />
          <el-tab-pane label="已完成" name="4" />
        </el-tabs>

        <div v-loading="loading">
          <div v-if="orderList.length">
            <div
              v-for="order in orderList"
              :key="order.id"
              class="order-card"
            >
              <div class="order-header">
                <div class="order-info">
                  <span class="order-no">订单号：{{ order.orderNo }}</span>
                  <span class="order-time">{{ formatTime(order.createdAt) }}</span>
                </div>
                <div class="order-status">
                  <el-tag :type="getStatusType(order.status)">
                    {{ getStatusText(order.status) }}
                  </el-tag>
                </div>
              </div>

              <div class="order-items">
                <div
                  v-for="(item, index) in order.items"
                  :key="index"
                  class="order-item"
                >
                  <div class="item-cover">
                    <img :src="getBookCover(item)" :alt="item.bookTitle">
                  </div>
                  <div class="item-info">
                    <router-link :to="`/book/${item.bookId}`" class="item-title">
                      {{ item.bookTitle }}
                    </router-link>
                    <p class="item-author">{{ item.bookAuthor }}</p>
                  </div>
                  <div class="item-price">¥{{ item.bookPrice }}</div>
                  <div class="item-quantity">x{{ item.quantity }}</div>
                  <div class="item-subtotal">¥{{ item.subtotal }}</div>
                </div>
              </div>

              <div class="order-footer">
                <div class="deadline-info" v-if="order.status === 0 || order.status === 1">
                  <el-icon><Clock /></el-icon>
                  <span>
                    汇款截止：{{ order.paymentDeadline }}
                    <el-tag v-if="isOverdue(order.paymentDeadline)" type="danger" size="small">
                      已逾期
                    </el-tag>
                  </span>
                </div>
                <div class="deadline-info" v-if="order.status === 2">
                  <el-icon><Clock /></el-icon>
                  <span>预计配送截止：{{ order.deliveryDeadline }}</span>
                </div>

                <div class="order-total">
                  <span>共 {{ getTotalQuantity(order) }} 件商品</span>
                  <span class="total-label">实付：</span>
                  <span class="total-amount">¥{{ order.totalAmount }}</span>
                </div>

                <div class="order-actions">
                  <el-button link @click="viewDetail(order.id)">查看详情</el-button>
                  <el-button
                    v-if="order.status === 0 || order.status === 1"
                    type="danger"
                    link
                    @click="handleCancel(order.id)"
                  >
                    取消订单
                  </el-button>
                  <el-button
                    v-if="order.status === 0"
                    type="primary"
                    size="small"
                    @click="showPaymentInfo(order)"
                  >
                    查看汇款信息
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <el-empty v-else description="暂无订单">
            <el-button type="primary" @click="goShopping">去逛逛</el-button>
          </el-empty>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import { getOrderList, cancelOrder } from '@/api/order'
import type { Order } from '@/api/types'

const router = useRouter()

const loading = ref(false)
const orderList = ref<Order[]>([])
const activeStatus = ref('all')

onMounted(() => {
  loadOrders()
})

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getOrderList()
    let orders = res.data || []

    if (activeStatus.value !== 'all') {
      orders = orders.filter(o => o.status === parseInt(activeStatus.value))
    }

    orderList.value = orders
  } catch {
    console.error('获取订单失败')
  } finally {
    loading.value = false
  }
}

const handleStatusChange = () => {
  loadOrders()
}

const formatTime = (time?: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
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

const isOverdue = (deadline?: string) => {
  if (!deadline) return false
  return new Date(deadline) < new Date()
}

const getTotalQuantity = (order: Order) => {
  return order.items?.reduce((sum, item) => sum + (item.quantity ?? 0), 0) || 0
}

const getBookCover = (_item?: unknown) => {
  return '/placeholder-book.jpg'
}


const viewDetail = (id: number) => {
  router.push(`/order/${id}`)
}

const handleCancel = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await cancelOrder(id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('取消订单失败')
    }
  }
}

const showPaymentInfo = (order: Order) => {
  ElMessageBox.alert(
    `订单号：${order.orderNo}<br>应付金额：¥${order.totalAmount}<br><br>
     汇款账户：小童书店<br>
     账号：6222 **** **** 1234<br>
     开户行：XX银行北京分行<br><br>
     请汇款后联系店主确认收款，或在备注中注明订单号。`,
    '汇款信息',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '我知道了'
    }
  )
}

const goShopping = () => {
  router.push('/books')
}
</script>

<style scoped>
.orders-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.breadcrumb {
  padding: 20px 0;
}

.orders-container {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 40px;
}

.page-title {
  font-size: 24px;
  margin-bottom: 24px;
  color: #333;
}

.order-card {
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 20px;
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #fafafa;
  border-bottom: 1px solid #eee;
}

.order-info {
  display: flex;
  gap: 20px;
}

.order-no {
  color: #333;
  font-weight: 500;
}

.order-time {
  color: #999;
}

.order-items {
  padding: 0 20px;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.order-item:last-child {
  border-bottom: none;
}

.item-cover {
  width: 60px;
  height: 75px;
  background: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
  margin-right: 16px;
}

.item-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 2;
}

.item-title {
  font-size: 14px;
  color: #333;
  text-decoration: none;
}

.item-title:hover {
  color: #409eff;
}

.item-author {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}

.item-price {
  flex: 1;
  text-align: center;
  color: #666;
}

.item-quantity {
  flex: 0.5;
  text-align: center;
  color: #666;
}

.item-subtotal {
  flex: 1;
  text-align: right;
  color: #f56c6c;
  font-weight: 500;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #fafafa;
  border-top: 1px solid #eee;
}

.deadline-info {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #e6a23c;
  font-size: 14px;
}

.order-total {
  display: flex;
  align-items: center;
  gap: 10px;
}

.total-label {
  color: #666;
}

.total-amount {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.order-actions {
  display: flex;
  gap: 15px;
}
</style>
