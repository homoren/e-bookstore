<template>
  <div class="book-detail-page">
    <Header />

    <div class="container">
      <!-- 面包屑 -->
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="book.parentCategoryName">
          {{ book.parentCategoryName }}
        </el-breadcrumb-item>
        <el-breadcrumb-item v-if="book.categoryName">
          <a @click="goToCategory">{{ book.categoryName }}</a>
        </el-breadcrumb-item>
        <el-breadcrumb-item>{{ book.title }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div v-loading="loading" class="book-detail">
        <!-- 基本信息区 -->
        <div class="book-basic">
          <div class="book-cover">
            <img :src="book.coverImage || '/placeholder-book.jpg'" :alt="book.title">
          </div>

          <div class="book-meta">
            <h1 class="book-title">{{ book.title }}</h1>
            <p class="book-author">作者：{{ book.author }}</p>
            <p class="book-publisher">出版社：{{ book.publisher }}</p>
            <p class="book-isbn">ISBN：{{ book.isbn }}</p>
            <p class="book-publish-date">出版时间：{{ book.publishDate }}</p>

            <div class="book-tags">
              <el-tag v-if="book.difficultyLevel" type="info">
                {{ getDifficultyText(book.difficultyLevel) }}
              </el-tag>
            </div>

            <div class="book-price-box">
              <span class="label">售价</span>
              <span class="price">¥{{ book.price }}</span>
            </div>

            <div class="book-stock-info">
              <span>库存 {{ book.stock }} 件</span>
              <span v-if="book.stock > 0" class="in-stock">有货</span>
              <span v-else class="out-stock">缺货</span>
            </div>

            <div class="book-actions">
              <el-input-number
                v-model="quantity"
                :min="1"
                :max="book.stock"
                :disabled="book.stock === 0"
                size="large"
              />
              <el-button
                type="primary"
                size="large"
                :disabled="book.stock === 0"
                @click="addToCart"
              >
                <el-icon><ShoppingCart /></el-icon>
                加入购物车
              </el-button>
              <el-button
                type="danger"
                size="large"
                :disabled="book.stock === 0"
                @click="buyNow"
              >
                立即购买
              </el-button>
            </div>

            <!-- 随书源码下载（特色功能） -->
            <div v-if="book.sampleCodeUrl" class="sample-code">
              <el-link :href="book.sampleCodeUrl" target="_blank" type="primary">
                <el-icon><Download /></el-icon>
                随书源码下载
              </el-link>
            </div>
          </div>
        </div>

        <!-- 详细介绍区（三层信息） -->
        <div class="book-detail-info">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="内容简介" name="desc">
              <div class="description">
                <p>{{ book.description }}</p>
              </div>
            </el-tab-pane>

            <el-tab-pane label="详细内容" name="detail">
              <div class="detail-html" v-html="book.detailHtml || '暂无详细内容'"></div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import { getBookDetail } from '@/api/book'
import { addToCart as addToCartApi } from '@/api/cart'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const book = ref({})
const quantity = ref(1)
const activeTab = ref('desc')

onMounted(async () => {
  const bookId = route.params.id
  if (bookId) {
    await loadBook(bookId)
  }
})

const loadBook = async (id) => {
  loading.value = true
  try {
    const res = await getBookDetail(id)
    book.value = res.data || res
  } catch (error) {
    console.error('获取图书详情失败', error)
    ElMessage.error('获取图书信息失败')
  } finally {
    loading.value = false
  }
}

const addToCart = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }

  try {
    await addToCartApi({
      bookId: book.value.id,
      quantity: quantity.value
    })
    ElMessage.success('已加入购物车')
  } catch (error) {
    console.error('加入购物车失败', error)
  }
}

const buyNow = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }

  await addToCart()
  router.push('/cart')
}

const goToCategory = () => {
  // 跳转到分类页面
  router.push('/books')
}

const getDifficultyText = (level) => {
  const map = { 1: '入门', 2: '进阶', 3: '高级' }
  return map[level] || ''
}
</script>

<style scoped>
.book-detail-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.breadcrumb {
  padding: 20px 0;
}

.breadcrumb a {
  cursor: pointer;
  color: #666;
}

.breadcrumb a:hover {
  color: #409eff;
}

.book-detail {
  padding-bottom: 40px;
}

.book-basic {
  display: flex;
  gap: 40px;
  padding: 30px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.book-cover {
  width: 300px;
  height: 400px;
  flex-shrink: 0;
  background: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-meta {
  flex: 1;
}

.book-title {
  font-size: 28px;
  margin-bottom: 20px;
  color: #333;
}

.book-author,
.book-publisher,
.book-isbn,
.book-publish-date {
  color: #666;
  margin-bottom: 8px;
}

.book-tags {
  margin: 15px 0;
}

.book-price-box {
  background: #fef0f0;
  padding: 20px;
  border-radius: 8px;
  margin: 20px 0;
}

.book-price-box .label {
  color: #666;
  margin-right: 20px;
}

.book-price-box .price {
  font-size: 32px;
  font-weight: bold;
  color: #f56c6c;
}

.book-stock-info {
  margin-bottom: 20px;
  color: #666;
}

.book-stock-info .in-stock {
  margin-left: 20px;
  color: #67c23a;
}

.book-stock-info .out-stock {
  margin-left: 20px;
  color: #f56c6c;
}

.book-actions {
  display: flex;
  gap: 15px;
}

.book-actions .el-input-number {
  width: 120px;
}

.sample-code {
  margin-top: 20px;
}

.book-detail-info {
  margin-top: 30px;
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.description {
  line-height: 1.8;
  color: #333;
}

.detail-html {
  line-height: 1.8;
  color: #333;
}

.detail-html :deep(h3) {
  margin: 20px 0 10px;
}

.detail-html :deep(p) {
  margin-bottom: 15px;
}
</style>
