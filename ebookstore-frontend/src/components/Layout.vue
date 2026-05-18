<template>
  <div class="admin-layout">
    <div class="admin-sidebar">
      <div class="sidebar-header">
        <router-link to="/" class="logo">
          <h2>My-eBookStore</h2>
        </router-link>
        <p class="subtitle">后台管理</p>
      </div>

      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/admin">
          <el-icon><DataBoard /></el-icon>
          <span>控制台</span>
        </el-menu-item>

        <el-menu-item index="/admin/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/books">
          <el-icon><Reading /></el-icon>
          <span>图书管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/purchase">
          <el-icon><ShoppingBag /></el-icon>
          <span>进货管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/customers">
          <el-icon><UserFilled /></el-icon>
          <span>客户管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/settlement">
          <el-icon><TrendCharts /></el-icon>
          <span>日结帐</span>
        </el-menu-item>

        <el-menu-item index="/admin/announcements">
          <el-icon><Notification /></el-icon>
          <span>公告管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/messages">
          <el-icon><ChatLineSquare /></el-icon>
          <span>留言管理</span>
        </el-menu-item>

      </el-menu>

      <div class="sidebar-footer">
        <el-button link @click="handleLogout" style="color: #bfcbd9;">
          <el-icon><SwitchButton /></el-icon>
          退出后台
        </el-button>
      </div>
    </div>

    <div class="admin-main">
      <div class="admin-header">
        <div class="header-left">
          <el-icon><Operation /></el-icon>
          <span>欢迎回来，{{ userStore.userInfo.realName || userStore.userInfo.username }}</span>
        </div>
        <div class="header-right">
          <router-link to="/" class="home-link">
            <el-icon><HomeFilled /></el-icon>
            返回前台
          </router-link>
        </div>
      </div>

      <div class="admin-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  return route.path
})

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出后台')
  router.push('/')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.admin-sidebar {
  width: 220px;
  background: #304156;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 20px 16px;
  text-align: center;
}

.sidebar-header .logo {
  text-decoration: none;
}

.sidebar-header h2 {
  color: #fff;
  font-size: 20px;
  margin: 0;
}

.sidebar-header .subtitle {
  color: #bfcbd9;
  font-size: 12px;
  margin-top: 4px;
}

.admin-sidebar .el-menu {
  border-right: none;
  flex: 1;
}

.sidebar-footer {
  padding: 16px;
  text-align: center;
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}

.admin-header {
  height: 56px;
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
}

.home-link {
  color: #409eff;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 5px;
}

.admin-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>
