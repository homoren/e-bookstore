<template>
  <div class="order-detail-page">
    <Header />

    <div class="container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/orders' }">我的订单</el-breadcrumb-item>
        <el-breadcrumb-item>订单详情</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="detail-container" v-loading="loading">
        <div v-if="order.id" class="order-detail">
          <div class="order-status-bar" :class="statusClass">
            <div class="status-content">
              <h2>{{ getStatusText(order.status) }}</h2>
              <p v-if="order.status === 0 || order.status === 1">
                请在 {{ order.paymentDeadline }} 前完成汇款
              </p>
            </div>
          </div>

          <div class="info-section">
            <h3>收货信息</h3>
            <div class="info-content">
              <p>收货人：{{ order.receiverName }}</p>
              <p>联系电话：{{ order.receiverPhone }}</p>
              <p>收货地址：{{ order.receiverAddress }}</p>
              <p v-if="order.remark">备注：{{ order.remark }}</p>
            </div>
          </div>

          <div class="info-section">
            <h3>订单信息</h3>
            <div class="info-content">
              <p>订单号：{{ order.orderNo }}</p>
              <p>下单时间：{{ formatTime(order.createdAt) }}</p>
              <p v-if="order.paidAt">收款时间：{{ formatTime(order.paidAt) }}</p>
              <p v-if="order.deliveredAt">配送时间：{{ formatTime(order.deliveredAt) }}</p>
              <p v-if="order.completedAt">完成时间：{{ formatTime(order.completedAt) }}</p>
            </div>
          </div>

          <div class="items-section">
            <h3>商品清单</h3>
            <el-table :data="order.items" style="width: 100%">
              <el-table-column label="商品信息" min-width="400">
                <template #default="{ row }">
                  <div class="goods-info">
                    <div class="goods-cover">
                      <img :src="row.bookCover || 'https://picsum.photos/60/75'" :alt="row.bookTitle">
                    </div>
                    <div class="goods-detail">
                      <router-link :to="`/book/${row.bookId}`" class="goods-title">
                        {{ row.bookTitle }}
                      </router-link>
                      <p class="goods-author">{{ row.bookAuthor }}</p>
                    </div>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="单价" width="120">
                <template #default="{ row }">
                  <span>¥{{ row.bookPrice }}</span>
                </template>
              </el-table-column>

              <el-table-column label="数量" width="100">
                <template #default="{ row }">
                  <span>{{ row.quantity }}</span>
                </template>
              </el-table-column>

              <el-table-column label="小计" width="120">
                <template #default="{ row }">
                  <span class="subtotal">¥{{ row.subtotal }}</span>
                </template>
              </el-table-column>
            </el-table>

            <div class="order-total">
              <span>订单总额：</span>
              <span class="total-amount">¥{{ order.totalAmount }}</span>
            </div>
          </div>

          <div class="actions">
            <el-button @click="router.back()">返回</el-button>
            <el-button
              v-if="order.status === 0 || order.status === 1"
              type="danger"
              @click="handleCancel"
            >
              取消订单
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import { getOrderDetail, cancelOrder } from '@/api/order'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const order = ref({})

const statusClass = computed(() => {
  const map = {
    0: 'status-warning',
    1: 'status-info',
    2: 'status-primary',
    3: 'status-primary',
    4: 'status-success',
    5: 'status-default'
  }
  return map[order.value.status] || 'status-default'
})

onMounted(async () => {
  const id = route.params.id
  if (id) {
    await loadOrder(id)
  }
})

const loadOrder = async (id) => {
  loading.value = true
  try {
    const res = await getOrderDetail(id)
    order.value = res.data || res
  } catch (error) {
    console.error(error)
    router.push('/orders')
  } finally {
    loading.value = false
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

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await cancelOrder(order.value.id)
    ElMessage.success('订单已取消')
    loadOrder(order.value.id)
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}
</script>

<style scoped>
.order-detail-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.breadcrumb {
  padding: 20px 0;
}

.detail-container {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 40px;
}

.order-status-bar {
  padding: 30px 40px;
  border-radius: 8px 8px 0 0;
}

.order-status-bar.status-warning { background: #fdf6ec; border-bottom: 3px solid #e6a23c; }
.order-status-bar.status-info { background: #f4f4f5; border-bottom: 3px solid #909399; }
.order-status-bar.status-primary { background: #ecf5ff; border-bottom: 3px solid #409eff; }
.order-status-bar.status-success { background: #f0f9eb; border-bottom: 3px solid #67c23a; }
.order-status-bar.status-default { background: #f4f4f5; }

.order-status-bar h2 {
  font-size: 22px;
  margin-bottom: 8px;
}

.order-status-bar p {
  color: #666;
}

.info-section {
  padding: 24px 40px;
  border-bottom: 1px solid #eee;
}

.info-section h3 {
  font-size: 16px;
  margin-bottom: 12px;
  color: #333;
}

.info-content p {
  color: #666;
  line-height: 2;
}

.items-section {
  padding: 24px 40px;
  border-bottom: 1px solid #eee;
}

.items-section h3 {
  font-size: 16px;
  margin-bottom: 16px;
  color: #333;
}

.goods-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.goods-cover {
  width: 60px;
  height: 75px;
  background: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
}

.goods-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.goods-title {
  font-size: 14px;
  color: #333;
  text-decoration: none;
}

.goods-title:hover {
  color: #409eff;
}

.goods-author {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}

.subtotal {
  color: #f56c6c;
  font-weight: 500;
}

.order-total {
  text-align: right;
  padding-top: 16px;
  font-size: 16px;
}

.total-amount {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
  margin-left: 10px;
}

.actions {
  padding: 24px 40px;
  display: flex;
  gap: 15px;
  justify-content: flex-end;
}
</style>
