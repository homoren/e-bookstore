<template>
  <div class="admin-settlement">
    <div class="page-header">
      <h2 class="page-title">日结帐管理</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleDateChange"
        />
        <el-button type="primary" :loading="generating" @click="generateTodaySettlement">
          生成今日日结
        </el-button>
      </div>
    </div>

    <el-table :data="settlements" style="width: 100%" v-loading="loading" show-summary>
      <el-table-column prop="settleDate" label="日期" width="150" />
      <el-table-column label="订单数" width="100">
        <template #default="{ row }">
          {{ row.orderCount }}
        </template>
      </el-table-column>
      <el-table-column label="已收款订单" width="120">
        <template #default="{ row }">
          {{ row.paidOrderCount }}
        </template>
      </el-table-column>
      <el-table-column label="销售额" width="150">
        <template #default="{ row }">
          ¥{{ row.totalSales?.toFixed(2) || '0.00' }}
        </template>
      </el-table-column>
      <el-table-column label="成本" width="150">
        <template #default="{ row }">
          ¥{{ row.totalCost?.toFixed(2) || '0.00' }}
        </template>
      </el-table-column>
      <el-table-column label="利润" width="150">
        <template #default="{ row }">
          <span :style="{ color: (row.totalProfit || 0) >= 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
            ¥{{ row.totalProfit?.toFixed(2) || '0.00' }}
          </span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSettlements, generateSettlement } from '@/api/admin'

const loading = ref(false)
const generating = ref(false)
const settlements = ref([])
const dateRange = ref([])

onMounted(() => {
  loadSettlements()
})

const loadSettlements = async (startDate, endDate) => {
  loading.value = true
  try {
    let res
    if (startDate && endDate) {
      res = await getSettlements(startDate, endDate)
    } else {
      res = await getSettlements()
    }
    settlements.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleDateChange = (val) => {
  if (val && val.length === 2) {
    loadSettlements(val[0], val[1])
  } else {
    loadSettlements()
  }
}

const generateTodaySettlement = async () => {
  generating.value = true
  try {
    const today = new Date().toISOString().split('T')[0]
    await generateSettlement(today)
    ElMessage.success('日结已生成')
    loadSettlements()
  } catch (error) {
    console.error(error)
  } finally {
    generating.value = false
  }
}
</script>

<style scoped>
.admin-settlement {
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

.header-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}
</style>
