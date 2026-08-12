<template>
  <div class="admin-messages">
    <div class="page-header">
      <h2 class="page-title">留言管理</h2>
    </div>

    <el-table :data="messages" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="留言用户" width="120" />
      <el-table-column prop="content" label="留言内容" min-width="250" :show-overflow-tooltip="true" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '显示' : '隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="留言时间" width="160">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openReplyDialog(row)">
            {{ row.reply ? '修改回复' : '回复' }}
          </el-button>
          <el-button
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '隐藏' : '显示' }}
          </el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="replyDialogVisible"
      :title="replyForm.reply ? '修改回复' : '回复留言'"
      width="600px"
    >
      <div class="original-message">
        <p class="label">留言内容：</p>
        <p class="content">{{ currentMessage?.content }}</p>
        <p class="user-info">
          —— {{ currentMessage?.username || '匿名用户' }}
          {{ formatTime(currentMessage?.createdAt) }}
        </p>
      </div>

      <el-form :model="replyForm" label-width="80px" style="margin-top: 20px;">
        <el-form-item label="回复内容">
          <el-input
            v-model="replyForm.reply"
            type="textarea"
            :rows="5"
            placeholder="请输入回复内容..."
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="replying" @click="handleReply">
          提交回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import {
  getAllMessages,
  replyMessage,
  deleteMessage,
  updateMessageStatus
} from '@/api/admin'
import type { Message } from '@/api/types'

const loading = ref(false)
const replying = ref(false)
const messages = ref<Message[]>([])
const replyDialogVisible = ref(false)
const currentMessage = ref<Message | null>(null)

const replyForm = reactive({
  reply: ''
})

onMounted(() => {
  loadMessages()
})

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await getAllMessages()
    messages.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const openReplyDialog = (row: Message) => {
  currentMessage.value = row
  replyForm.reply = row.reply || ''
  replyDialogVisible.value = true
}

const handleReply = async () => {
  if (!replyForm.reply.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  replying.value = true
  try {
    await replyMessage(currentMessage.value?.id ?? 0, { reply: replyForm.reply })
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    loadMessages()
  } catch (error) {
    console.error(error)
  } finally {
    replying.value = false
  }
}

const toggleStatus = async (row: Message) => {
  const action = row.status === 1 ? '隐藏' : '显示'
  try {
    await ElMessageBox.confirm(`确定要${action}该留言吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const newStatus = row.status === 1 ? 0 : 1
    await updateMessageStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(`${action}成功`)
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该留言吗？此操作不可恢复。', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteMessage(id)
    ElMessage.success('留言已删除')
    loadMessages()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const formatTime = (time?: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.admin-messages {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  color: #333;
}

.original-message {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  border-left: 3px solid #909399;
}

.original-message .label {
  font-weight: bold;
  color: #666;
  margin-bottom: 8px;
}

.original-message .content {
  color: #333;
  line-height: 1.8;
}

.original-message .user-info {
  color: #999;
  font-size: 13px;
  margin-top: 8px;
}
</style>
