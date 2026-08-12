<template>
  <div class="messages-page">
    <Header />

    <div class="container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>客户留言</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="messages-container">
        <div class="page-header">
          <h2 class="page-title">客户留言</h2>
          <el-button type="primary" @click="showForm = true">
            <el-icon><Edit /></el-icon>
            我要留言
          </el-button>
        </div>

        <transition name="fade">
          <div v-if="showForm" class="message-form">
            <el-form :model="form" :rules="rules" ref="formRef">
              <el-form-item prop="content">
                <el-input
                  v-model="form.content"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入您的留言内容..."
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="submitting" @click="submitMessage">
                  提交留言
                </el-button>
                <el-button @click="showForm = false">取消</el-button>
              </el-form-item>
            </el-form>
          </div>
        </transition>

        <div v-loading="loading" class="message-list">
          <div v-if="messages.length">
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="message-card"
            >
              <div class="message-header">
                <div class="user-info">
                  <el-avatar :size="36" icon="UserFilled" />
                  <div class="user-detail">
                    <span class="username">{{ msg.username || '匿名用户' }}</span>
                    <span class="time">{{ formatTime(msg.createdAt) }}</span>
                  </div>
                </div>
              </div>

              <div class="message-content">
                <p>{{ msg.content }}</p>
              </div>

              <div v-if="msg.reply" class="message-reply">
                <div class="reply-header">
                  <el-icon><UserFilled /></el-icon>
                  <span>店主回复</span>
                  <span class="reply-time">{{ formatTime(msg.repliedAt) }}</span>
                </div>
                <p>{{ msg.reply }}</p>
              </div>
            </div>
          </div>

          <el-empty v-else description="暂无留言" />
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import { getMessages, createMessage } from '@/api/message'

const loading = ref(false)
const submitting = ref(false)
const showForm = ref(false)
const messages = ref([])
const formRef = ref(null)

const form = ref({
  content: ''
})

const rules = {
  content: [
    { required: true, message: '请输入留言内容', trigger: 'blur' }
  ]
}

onMounted(() => {
  loadMessages()
})

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await getMessages()
    messages.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const submitMessage = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createMessage(form.value)
    ElMessage.success('留言成功')
    form.value.content = ''
    showForm.value = false
    loadMessages()
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.messages-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.breadcrumb {
  padding: 20px 0;
}

.messages-container {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 40px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.page-title {
  font-size: 24px;
  color: #333;
}

.message-form {
  margin-bottom: 24px;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.message-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
}

.message-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: 500;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
}

.message-content {
  color: #333;
  line-height: 1.8;
  margin-bottom: 16px;
}

.message-reply {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  border-left: 3px solid #409eff;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #409eff;
  font-weight: 500;
}

.reply-time {
  font-size: 12px;
  color: #999;
  font-weight: normal;
}

.message-reply p {
  color: #333;
  line-height: 1.8;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
