<template>
  <div class="admin-customers">
    <div class="page-header">
      <h2 class="page-title">客户管理</h2>
    </div>

    <el-table :data="customers" style="width: 100%" v-loading="loading">
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="真实姓名" width="100" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column label="地址" min-width="200" :show-overflow-tooltip="true">
        <template #default="{ row }">
          {{ row.address }}
        </template>
      </el-table-column>
      <el-table-column label="订单数" width="80">
        <template #default="{ row }">
          {{ row.orderCount }}
        </template>
      </el-table-column>
      <el-table-column label="累计消费" width="120">
        <template #default="{ row }">
          ¥{{ row.totalSpent?.toFixed(2) || '0.00' }}
        </template>
      </el-table-column>
      <el-table-column label="最后下单" width="160">
        <template #default="{ row }">
          {{ formatTime(row.lastOrderTime) }}
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="160">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAllCustomers } from '@/api/admin'
import type { Customer } from '@/api/types'

const loading = ref(false)
const customers = ref<Customer[]>([])

onMounted(() => {
  loadCustomers()
})

const loadCustomers = async () => {
  loading.value = true
  try {
    const res = await getAllCustomers()
    customers.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const formatTime = (time?: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.admin-customers {
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
