<template>
  <div class="cart-page">
    <Header />

    <div class="container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>购物车</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="cart-container">
        <h2 class="page-title">我的购物车</h2>

        <div v-if="cartList.length" v-loading="loading">
          <el-table
            ref="tableRef"
            :data="cartList"
            style="width: 100%"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" />

            <el-table-column label="商品信息" min-width="400">
              <template #default="{ row }">
                <div class="goods-info">
                  <div class="goods-cover">
                    <img :src="row.coverImage || '/placeholder-book.jpg'" :alt="row.bookTitle">
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
                <span class="price">¥{{ row.price }}</span>
              </template>
            </el-table-column>

            <el-table-column label="数量" width="150">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.quantity"
                  :min="1"
                  :max="row.stock"
                  size="small"
                  @change="handleQuantityChange(row as CartItem)"
                />
              </template>
            </el-table-column>

            <el-table-column label="小计" width="120">
              <template #default="{ row }">
                <span class="subtotal">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="danger" link @click="handleDelete(row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="cart-footer">
            <div class="left-actions">
              <el-checkbox v-model="selectAll" @change="handleSelectAll">
                全选
              </el-checkbox>
              <el-button @click="handleDeleteSelected" :disabled="!selectedIds.length">
                删除选中
              </el-button>
              <el-button @click="handleClearCart">清空购物车</el-button>
            </div>

            <div class="right-summary">
              <div class="summary-info">
                <span>已选商品 <strong>{{ selectedIds.length }}</strong> 件</span>
                <span class="total-label">合计：</span>
                <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
              </div>
              <el-button
                type="primary"
                size="large"
                :disabled="!selectedIds.length"
                @click="handleCheckout"
              >
                去结算
              </el-button>
            </div>
          </div>
        </div>

        <el-empty v-else description="购物车空空如也">
          <el-button type="primary" @click="goShopping">去逛逛</el-button>
        </el-empty>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import { getCartList, updateCartItem, deleteCartItem, clearCart } from '@/api/cart'
import type { CartItem } from '@/api/types'

const router = useRouter()

const loading = ref(false)
const cartList = ref<CartItem[]>([])
const selectedItems = ref<CartItem[]>([])
const selectAll = ref(false)
const tableRef = ref<{ toggleAllSelection: (val?: boolean) => void } | null>(null)

const selectedIds = computed(() => selectedItems.value.map(item => item.id))

const totalPrice = computed(() => {
  return selectedItems.value.reduce((sum, item) => {
    return sum + (item.price ?? 0) * (item.quantity ?? 0)
  }, 0)
})

onMounted(() => {
  loadCart()
})

const loadCart = async () => {
  loading.value = true
  try {
    const res = await getCartList()
    cartList.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (selection: CartItem[]) => {
  selectedItems.value = selection
  selectAll.value = selection.length === cartList.value.length
}

const handleSelectAll = (val: string | number | boolean) => {
  tableRef.value?.toggleAllSelection(Boolean(val))
}

const handleQuantityChange = async (row: CartItem) => {
  if (row.quantity > row.stock) {
    ElMessage.warning(`库存不足，最多可购买 ${row.stock} 件`)
    row.quantity = row.stock
  }

  try {
    await updateCartItem(row.id, { quantity: row.quantity })
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteCartItem(id)
    ElMessage.success('删除成功')
    loadCart()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleDeleteSelected = async () => {
  if (!selectedIds.value.length) return

  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 件商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    for (const id of selectedIds.value) {
      await deleteCartItem(id)
    }
    ElMessage.success('删除成功')
    selectedItems.value = []
    loadCart()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleClearCart = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await clearCart()
    ElMessage.success('购物车已清空')
    loadCart()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleCheckout = () => {
  const ids = selectedItems.value.map(item => item.id)
  router.push({ name: 'checkout', query: { ids: ids.join(',') } })
}

const goShopping = () => {
  router.push('/books')
}
</script>

<style scoped>
.cart-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.breadcrumb {
  padding: 20px 0;
}

.cart-container {
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

.goods-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.goods-cover {
  width: 80px;
  height: 100px;
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
  font-size: 15px;
  color: #333;
  text-decoration: none;
  display: block;
  margin-bottom: 8px;
}

.goods-title:hover {
  color: #409eff;
}

.goods-author {
  color: #999;
  font-size: 13px;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.subtotal {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.cart-footer {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.right-summary {
  display: flex;
  align-items: center;
  gap: 24px;
}

.summary-info {
  font-size: 15px;
}

.total-label {
  margin-left: 16px;
  color: #666;
}

.total-price {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}
</style>
