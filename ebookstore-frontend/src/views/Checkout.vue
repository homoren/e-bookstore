<template>
  <div class="checkout-page">
    <Header />

    <div class="container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/cart' }">购物车</el-breadcrumb-item>
        <el-breadcrumb-item>确认订单</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="checkout-container">
        <h2 class="page-title">确认订单信息</h2>

        <div class="section">
          <h3 class="section-title">收货信息</h3>
          <el-form
            ref="formRef"
            :model="orderForm"
            :rules="rules"
            label-width="100px"
          >
            <el-form-item label="收货人" prop="receiverName">
              <el-input v-model="orderForm.receiverName" placeholder="请输入收货人姓名" />
            </el-form-item>

            <el-form-item label="联系电话" prop="receiverPhone">
              <el-input v-model="orderForm.receiverPhone" placeholder="请输入联系电话" />
            </el-form-item>

            <el-form-item label="收货地址" prop="receiverAddress">
              <el-input
                v-model="orderForm.receiverAddress"
                type="textarea"
                :rows="2"
                placeholder="请输入详细收货地址"
              />
            </el-form-item>

            <el-form-item label="备注">
              <el-input
                v-model="orderForm.remark"
                type="textarea"
                :rows="2"
                placeholder="选填，可备注配送时间等要求"
              />
            </el-form-item>
          </el-form>
        </div>

        <div class="section">
          <h3 class="section-title">商品清单</h3>
          <el-table :data="orderItems" style="width: 100%">
            <el-table-column label="商品信息" min-width="400">
              <template #default="{ row }">
                <div class="goods-info">
                  <div class="goods-cover">
                    <img :src="row.coverImage || '/placeholder-book.jpg'" :alt="row.bookTitle">
                  </div>
                  <div class="goods-detail">
                    <span class="goods-title">{{ row.bookTitle }}</span>
                    <p class="goods-author">{{ row.bookAuthor }}</p>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="单价" width="120">
              <template #default="{ row }">
                <span class="price">¥{{ row.price }}</span>
              </template>
            </el-table-column>

            <el-table-column label="数量" width="100">
              <template #default="{ row }">
                <span>{{ row.quantity }}</span>
              </template>
            </el-table-column>

            <el-table-column label="小计" width="120">
              <template #default="{ row }">
                <span class="subtotal">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="section">
          <h3 class="section-title">支付方式</h3>
          <div class="payment-method">
            <el-radio-group v-model="paymentMethod">
              <el-radio label="remittance">银行汇款</el-radio>
            </el-radio-group>
            <div class="payment-tip">
              <el-alert
                title="汇款说明"
                type="info"
                :closable="false"
                show-icon
              >
                下单后请在7日内将货款汇至以下账户，店主确认收款后安排配送。
                <br>
                账户名：小童书店 &nbsp;&nbsp; 账号：6222 **** **** 1234 &nbsp;&nbsp; 开户行：XX银行北京分行
              </el-alert>
            </div>
          </div>
        </div>

        <div class="order-summary">
          <div class="summary-info">
            <span>商品总计：<strong>¥{{ totalAmount.toFixed(2) }}</strong></span>
          </div>
          <div class="actions">
            <el-button @click="router.back()">返回修改</el-button>
            <el-button
              type="primary"
              size="large"
              :loading="submitting"
              @click="handleSubmitOrder"
            >
              提交订单
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
import { useUserStore } from '@/stores/user'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import { getCartList } from '@/api/cart'
import { createOrder } from '@/api/order'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const submitting = ref(false)
const orderItems = ref([])
const paymentMethod = ref('remittance')

const orderForm = ref({
  receiverName: userStore.userInfo.realName || '',
  receiverPhone: userStore.userInfo.phone || '',
  receiverAddress: userStore.userInfo.address || '',
  remark: ''
})

const rules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  receiverAddress: [
    { required: true, message: '请输入收货地址', trigger: 'blur' }
  ]
}

const totalAmount = computed(() => {
  return orderItems.value.reduce((sum, item) => {
    return sum + item.price * item.quantity
  }, 0)
})

onMounted(async () => {
  const ids = route.query.ids
  const cartIds = ids ? String(ids).split(',').map(Number).filter(Boolean) : []
  if (!cartIds.length) {
    ElMessage.warning('请先选择商品')
    router.push('/cart')
    return
  }

  await loadOrderItems(cartIds)
})

const loadOrderItems = async (cartIds) => {
  try {
    const res = await getCartList()
    const allItems = res.data || []
    orderItems.value = allItems.filter(item => cartIds.includes(item.id))

    if (!orderItems.value.length) {
      ElMessage.warning('商品信息获取失败')
      router.push('/cart')
    }
  } catch (error) {
    console.error(error)
    router.push('/cart')
  }
}

const handleSubmitOrder = async () => {
  if (!orderForm.value.receiverName || !orderForm.value.receiverPhone || !orderForm.value.receiverAddress) {
    ElMessage.warning('请填写完整的收货信息')
    return
  }

  submitting.value = true
  try {
    const data = {
      ...orderForm.value,
      cartItemIds: orderItems.value.map(item => item.id)
    }

    const res = await createOrder(data)
    ElMessage.success('订单提交成功，请在7日内完成汇款')
    router.push({ name: 'orderDetail', params: { id: res.data.id } })
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.checkout-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.breadcrumb {
  padding: 20px 0;
}

.checkout-container {
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

.section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.section-title {
  font-size: 16px;
  margin-bottom: 20px;
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
}

.goods-author {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}

.price {
  color: #f56c6c;
}

.subtotal {
  color: #f56c6c;
  font-weight: bold;
}

.payment-method {
  padding: 10px 0;
}

.payment-tip {
  margin-top: 20px;
}

.order-summary {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 30px;
  padding-top: 20px;
}

.summary-info {
  font-size: 16px;
}

.summary-info strong {
  font-size: 24px;
  color: #f56c6c;
}
</style>
