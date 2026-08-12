<template>
  <div class="book-list-page">
    <Header />

    <div class="container">
      <!-- 面包屑导航 -->
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentCategory">{{ currentCategory.name }}</el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentSubCategory">{{
          currentSubCategory.name
        }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="main-content">
        <!-- 左侧分类菜单 -->
        <div class="sidebar">
          <h3 class="sidebar-title">图书分类</h3>
          <el-menu :default-active="activeCategory" @select="handleCategorySelect">
            <el-menu-item v-for="cat in categories" :key="'cat-' + cat.id" :index="'cat-' + cat.id">
              {{ cat.name }}
            </el-menu-item>
          </el-menu>

          <template v-if="subCategories.length">
            <h3 class="sidebar-title sub-title">子分类</h3>
            <el-menu :default-active="activeSubCategory" @select="handleSubCategorySelect">
              <el-menu-item v-for="sub in subCategories" :key="sub.id" :index="'sub-' + sub.id">
                {{ sub.name }}
              </el-menu-item>
            </el-menu>
          </template>
        </div>

        <!-- 右侧图书列表 -->
        <div class="content">
          <!-- 排序 -->
          <div class="toolbar">
            <div class="result-count">共 {{ total }} 本图书</div>
            <div class="sort-options">
              <span :class="{ active: sortBy === 'default' }" @click="changeSort('default')"
                >默认</span
              >
              <span :class="{ active: sortBy === 'price_asc' }" @click="changeSort('price_asc')"
                >价格从低到高</span
              >
              <span :class="{ active: sortBy === 'price_desc' }" @click="changeSort('price_desc')"
                >价格从高到低</span
              >
              <span :class="{ active: sortBy === 'sales' }" @click="changeSort('sales')"
                >销量优先</span
              >
            </div>
          </div>

          <!-- 图书列表 -->
          <div v-loading="loading" class="books-wrapper">
            <div v-if="bookList.length" class="book-grid-list">
              <div
                v-for="book in pagedBooks"
                :key="book.id"
                class="book-item"
                @click="goToDetail(book.id)"
              >
                <div class="book-cover">
                  <img :src="book.coverImage || '/placeholder-book.jpg'" :alt="book.title" />
                </div>
                <div class="book-info">
                  <h3 class="book-title">{{ book.title }}</h3>
                  <p class="book-author">{{ book.author }}</p>
                  <p class="book-publisher">{{ book.publisher }}</p>
                  <div class="book-tags">
                    <el-tag v-if="book.difficultyLevel" size="small" type="info">
                      {{ getDifficultyText(book.difficultyLevel) }}
                    </el-tag>
                  </div>
                  <div class="book-footer">
                    <span class="book-price">¥{{ book.price }}</span>
                    <span class="book-stock" :class="{ low: book.stock < 10 }">
                      库存 {{ book.stock }} 件
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <el-empty v-else description="暂无图书" />
          </div>

          <!-- 分页 -->
          <div v-if="total > pageSize" class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import {
  getLevel1Categories,
  getLevel2Categories,
  getBookList,
  getBooksByParentCategory,
} from '@/api/book'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const categories = ref([])
const subCategories = ref([])
const bookList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const sortBy = ref('default')

const activeCategory = ref('')
const activeSubCategory = ref('')
const currentCategoryId = ref(null)
const currentSubCategoryId = ref(null)

const currentCategory = computed(() => {
  return categories.value.find((c) => c.id === currentCategoryId.value)
})

const currentSubCategory = computed(() => {
  return subCategories.value.find((s) => s.id === currentSubCategoryId.value)
})

onMounted(async () => {
  await loadCategories()

  // 从 URL 获取分类参数
  const categoryId = route.query.category
  const subCategoryId = route.query.subCategory

  if (categoryId) {
    currentCategoryId.value = parseInt(categoryId)
    activeCategory.value = `cat-${categoryId}`
    await loadSubCategories(categoryId)
  }

  if (subCategoryId) {
    currentSubCategoryId.value = parseInt(subCategoryId)
    activeSubCategory.value = `sub-${subCategoryId}`
  }

  loadBooks()
})

// 监听路由变化（搜索/切换分类都会重新加载）
watch(
  () => route.query,
  () => {
    loadBooks()
  },
)

const loadCategories = async () => {
  try {
    const res = await getLevel1Categories()
    categories.value = Array.isArray(res) ? res : res.data || []
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

const loadSubCategories = async (parentId) => {
  try {
    const res = await getLevel2Categories(parentId)
    subCategories.value = Array.isArray(res) ? res : res.data || []
  } catch (error) {
    console.error('获取子分类失败', error)
    subCategories.value = []
  }
}

const loadBooks = async () => {
  loading.value = true
  try {
    // ✅ 从 URL 读取关键词
    const keyword = route.query.keyword || ''

    let res
    if (currentSubCategoryId.value) {
      // 选了二级分类 → 只查当前二级
      res = await getBookList(currentSubCategoryId.value, keyword)
    } else if (currentCategoryId.value) {
      // 只选了一级分类 → 查该一级下所有
      res = await getBooksByParentCategory(currentCategoryId.value)
    } else {
      // 没选分类 → 全部
      res = await getBookList(0, keyword)
    }

    let books = Array.isArray(res) ? res : res.data || []
    books = sortBooks(books)
    bookList.value = books
    total.value = books.length
  } catch (error) {
    console.error('获取图书列表失败', error)
    bookList.value = []
  } finally {
    loading.value = false
  }
}

const sortBooks = (books) => {
  const sorted = [...books]
  switch (sortBy.value) {
    case 'price_asc':
      return sorted.sort((a, b) => a.price - b.price)
    case 'price_desc':
      return sorted.sort((a, b) => b.price - a.price)
    case 'sales':
      return sorted.sort((a, b) => (b.salesCount || 0) - (a.salesCount || 0))
    default:
      return sorted
  }
}

// 当前页展示的图书（前端分页）
const pagedBooks = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return bookList.value.slice(start, start + pageSize.value)
})

const handleCategorySelect = async (index) => {
  const catId = parseInt(index.replace('cat-', ''))
  currentCategoryId.value = catId
  currentSubCategoryId.value = null
  activeSubCategory.value = ''
  currentPage.value = 1

  await loadSubCategories(catId)
  loadBooks()

  // 更新 URL
  router.replace({ query: { category: catId } })
}

const handleSubCategorySelect = (index) => {
  const subId = parseInt(index.replace('sub-', ''))
  currentSubCategoryId.value = subId
  currentPage.value = 1
  loadBooks()

  // 更新 URL
  router.replace({
    query: {
      category: currentCategoryId.value,
      subCategory: subId,
    },
  })
}

const changeSort = (type) => {
  sortBy.value = type
  currentPage.value = 1
  loadBooks()
}

const handlePageChange = (page) => {
  currentPage.value = page
}

const goToDetail = (id) => {
  router.push(`/book/${id}`)
}

const getDifficultyText = (level) => {
  const map = { 1: '入门', 2: '进阶', 3: '高级' }
  return map[level] || ''
}
</script>

<style scoped>
.book-list-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.breadcrumb {
  padding: 20px 0;
}

.main-content {
  display: flex;
  gap: 24px;
  padding-bottom: 40px;
}

.sidebar {
  width: 220px;
  flex-shrink: 0;
}

.sidebar-title {
  font-size: 16px;
  padding: 12px 0;
  margin: 0;
  border-bottom: 1px solid #e8e8e8;
}

.sidebar-title.sub-title {
  margin-top: 20px;
}

.content {
  flex: 1;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e8e8e8;
  margin-bottom: 20px;
}

.result-count {
  color: #666;
}

.sort-options {
  display: flex;
  gap: 20px;
}

.sort-options span {
  cursor: pointer;
  color: #666;
  transition: color 0.3s;
}

.sort-options span:hover,
.sort-options span.active {
  color: #409eff;
}

.books-wrapper {
  min-height: 400px;
}

.book-grid-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.book-item {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  padding: 16px;
}

.book-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.book-cover {
  width: 100px;
  height: 140px;
  flex-shrink: 0;
  background: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-info {
  flex: 1;
  padding-left: 16px;
  display: flex;
  flex-direction: column;
}

.book-title {
  font-size: 15px;
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.book-author {
  color: #666;
  font-size: 13px;
  margin-bottom: 4px;
}

.book-publisher {
  color: #999;
  font-size: 12px;
  margin-bottom: 8px;
}

.book-tags {
  margin-bottom: 8px;
}

.book-footer {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.book-price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.book-stock {
  font-size: 12px;
  color: #67c23a;
}

.book-stock.low {
  color: #e6a23c;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>
