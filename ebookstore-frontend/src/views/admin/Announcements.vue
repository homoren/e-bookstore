<template>
  <div class="admin-announcements">
    <div class="page-header">
      <h2 class="page-title">公告管理</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon> 发布公告
      </el-button>
    </div>

    <el-table :data="announcements" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="置顶" width="80">
        <template #default="{ row }">
          <el-icon v-if="row.isTop" color="#f56c6c"><Top /></el-icon>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="浏览次数" width="100">
        <template #default="{ row }">
          {{ row.viewCount }}
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="160">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="togglePublish(row)"
          >
            {{ row.status === 1 ? '下架' : '发布' }}
          </el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑公告' : '发布公告'"
      width="700px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="请输入公告内容，支持HTML标签"
          />
        </el-form-item>

        <el-form-item label="设置">
          <el-checkbox v-model="form.isTop">置顶</el-checkbox>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          {{ editingId ? '保存修改' : '发布' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import {
  getAllAnnouncements,
  createAnnouncement,
  updateAnnouncement,
  deleteAnnouncement
} from '@/api/admin'
import type { Announcement } from '@/api/types'

const loading = ref(false)
const saving = ref(false)
const announcements = ref<Announcement[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<{ validate: () => Promise<boolean> } | null>(null)

const form = reactive({
  title: '',
  content: '',
  isTop: false
})

const rules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' }
  ]
}

onMounted(() => {
  loadAnnouncements()
})

const loadAnnouncements = async () => {
  loading.value = true
  try {
    const res = await getAllAnnouncements()
    announcements.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  editingId.value = null
  form.title = ''
  form.content = ''
  form.isTop = false
  dialogVisible.value = true
}

const openEditDialog = (row: Announcement) => {
  editingId.value = row.id
  form.title = row.title
  form.content = row.content
  form.isTop = row.isTop === 1
  dialogVisible.value = true
}

const resetForm = () => {
  editingId.value = null
  form.title = ''
  form.content = ''
  form.isTop = false
}

const togglePublish = async (row: Announcement) => {
  const action = row.status === 1 ? '下架' : '发布'
  try {
    await ElMessageBox.confirm(`确定要${action}该公告吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await updateAnnouncement(row.id, {
      title: row.title,
      content: row.content,
      isTop: row.isTop
    })
    row.status = row.status === 1 ? 0 : 1
    ElMessage.success(`${action}成功`)
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？此操作不可恢复。', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteAnnouncement(id)
    ElMessage.success('公告已删除')
    loadAnnouncements()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const data = {
      title: form.title,
      content: form.content,
      isTop: form.isTop ? 1 : 0
    }

    if (editingId.value) {
      await updateAnnouncement(editingId.value, data)
      ElMessage.success('公告已更新')
    } else {
      await createAnnouncement(data)
      ElMessage.success('公告已发布')
    }

    dialogVisible.value = false
    loadAnnouncements()
  } catch (error) {
    console.error(error)
  } finally {
    saving.value = false
  }
}

const formatTime = (time?: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.admin-announcements {
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
</style>
