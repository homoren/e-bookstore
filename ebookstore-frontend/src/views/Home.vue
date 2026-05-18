<template>
  <div class="home">
    <Header />

    <!-- 轮播图 -->
    <div class="banner">
      <el-carousel height="400px">
        <el-carousel-item v-for="item in banners" :key="item">
          <div class="banner-item" :style="{ background: item.color }">
            <h2>{{ item.title }}</h2>
            <p>{{ item.desc }}</p>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 最新公告 -->
    <div class="container">
      <div class="announcement-bar" v-if="announcements.length">
        <el-icon><Bell /></el-icon>
        <span class="label">最新公告：</span>
        <router-link :to="`/announcements`">{{ announcements[0]?.title }}</router-link>
      </div>
    </div>

    <!-- 图书分类展示 -->
    <div class="container" v-for="cat in categories" :key="cat.id">
      <div class="section-header">
        <h2>{{ cat.name }} 热销图书</h2>
        <router-link :to="`/books?category=${cat.id}`" class="more">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>
      <div class="book-grid">
        <div
          class="book-card"
          v-for="book in cat.books"
          :key="book.id"
          @click="goToDetail(book.id)"
        >
          <div class="book-cover">
            <img :src="book.coverImage || '/placeholder-book.jpg'" :alt="book.title" />
          </div>
          <div class="book-info">
            <h3 class="book-title">{{ book.title }}</h3>
            <p class="book-author">{{ book.author }}</p>
            <div class="book-price">
              <span class="price">¥{{ book.price }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import { getLevel1Categories, getBookList } from '@/api/book'
import { getPublishedAnnouncements } from '@/api/announcement'

const router = useRouter()

const banners = ref([
  { title: '计算机图书专场', desc: '编程、数据库、操作系统，全场8折', color: '#667eea' },
  { title: '英语学习必备', desc: '词汇、语法、阅读，全面提升', color: '#f093fb' },
  { title: '新书上架', desc: '最新技术图书，抢先阅读', color: '#4facfe' },
])

const categories = ref([])
const announcements = ref([])

onMounted(async () => {
  try {
    // 获取分类
    const catRes = await getLevel1Categories()
    const cats = Array.isArray(catRes) ? catRes : catRes.data || []

    // 获取每个分类下的图书
    for (const cat of cats) {
      try {
        const bookRes = await getBookList(cat.id)
        cat.books = Array.isArray(bookRes) ? bookRes.slice(0, 4) : (bookRes.data || []).slice(0, 4)
      } catch (error) {
        cat.books = []
      }
    }
    categories.value = cats

    // 获取公告
    const annRes = await getPublishedAnnouncements()
    announcements.value = Array.isArray(annRes) ? annRes : annRes.data || []
  } catch (error) {
    console.error('获取数据失败', error)
  }
})

const goToDetail = (id) => {
  router.push(`/book/${id}`)
}
</script>

<style scoped>
.banner {
  margin-bottom: 30px;
}

.banner-item {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #fff;
}

.banner-item h2 {
  font-size: 48px;
  margin-bottom: 20px;
}

.banner-item p {
  font-size: 20px;
}

.announcement-bar {
  background: #f0f9ff;
  border-left: 4px solid #409eff;
  padding: 15px 20px;
  margin-bottom: 30px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.announcement-bar .label {
  font-weight: bold;
  color: #333;
}

.announcement-bar a {
  color: #409eff;
  text-decoration: none;
}

.announcement-bar a:hover {
  text-decoration: underline;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 40px 0 20px;
}

.section-header h2 {
  font-size: 24px;
  color: #333;
}

.section-header .more {
  color: #409eff;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 5px;
}

.book-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.book-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition:
    transform 0.3s,
    box-shadow 0.3s;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.book-cover {
  aspect-ratio: 3/4;
  overflow: hidden;
  background: #f5f5f5;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-info {
  padding: 15px;
}

.book-title {
  font-size: 16px;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  color: #999;
  font-size: 13px;
  margin-bottom: 10px;
}

.book-price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.book-price .price {
  color: #f56c6c;
}
</style>
