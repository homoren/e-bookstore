<template>
  <div class="announcements-page">
    <Header />

    <div class="container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>书店公告</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="announcements-container">
        <h2 class="page-title">书店公告</h2>

        <div v-loading="loading">
          <div v-if="announcements.length">
            <div
              v-for="item in announcements"
              :key="item.id"
              class="announcement-card"
              @click="viewDetail(item.id)"
            >
              <div class="announcement-header">
                <div class="title-row">
                  <el-tag v-if="item.isTop" type="danger" size="small">置顶</el-tag>
                  <h3>{{ item.title }}</h3>
                </div>
                <span class="time">{{ formatTime(item.createdAt) }}</span>
              </div>
              <div class="announcement-preview">
                <p>{{ getPreview(item.content) }}</p>
              </div>
              <div class="announcement-footer">
                <span class="view-count">
                  <el-icon><View /></el-icon>
                  {{ item.viewCount }} 次阅读
                </span>
                <el-button link type="primary">查看详情</el-button>
              </div>
            </div>
          </div>

          <el-empty v-else description="暂无公告" />
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
import { getPublishedAnnouncements, getAnnouncementDetail } from '@/api/announcement'

const router = useRouter()
const loading = ref(false)
const announcements = ref([])

onMounted(() => {
  loadAnnouncements()
})

const loadAnnouncements = async () => {
  loading.value = true
  try {
    const res = await getPublishedAnnouncements()
    announcements.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const getPreview = (content) => {
  if (!content) return ''
  const text = content.replace(/<[^>]+>/g, '')
  return text.length > 150 ? text.substring(0, 150) + '...' : text
}

const viewDetail = async (id) => {
  try {
    const res = await getAnnouncementDetail(id)
    const detail = res.data || res

    const content = detail.content || ''
    const w = window.open('', '_blank', 'width=800,height=600')
    w.document.write(`
      <html>
        <head><title>${detail.title}</title></head>
        <body style="padding: 30px; font-family: sans-serif; line-height: 1.8;">
          <h1>${detail.title}</h1>
          <p style="color: #999; margin-bottom: 20px;">${formatTime(detail.createdAt)} | ${detail.viewCount} 次阅读</p>
          <hr>
          <div>${content}</div>
        </body>
      </html>
    `)
    loadAnnouncements()
  } catch (error) {
    console.error(error)
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.announcements-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.breadcrumb {
  padding: 20px 0;
}

.announcements-container {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 40px;
}

.page-title {
  font-size: 24px;
  margin-bottom: 24px;
  color: #333;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.announcement-card {
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.announcement-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-row h3 {
  font-size: 18px;
  color: #333;
}

.time {
  font-size: 13px;
  color: #999;
}

.announcement-preview {
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}

.announcement-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.view-count {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #999;
  font-size: 13px;
}
</style>
