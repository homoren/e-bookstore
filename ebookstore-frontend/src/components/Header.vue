<template>
  <header class="header">
    <div class="header-top">
      <div class="container">
        <div class="header-top-content">
          <span class="welcome">欢迎来到 My-eBookStore 网上书店</span>
          <div class="user-links">
            <template v-if="userStore.isLoggedIn">
              <span class="username">{{
                userStore.userInfo.realName || userStore.userInfo.username
              }}</span>
              <el-dropdown @command="handleCommand">
                <span class="el-dropdown-link">
                  我的账户 <el-icon><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                    <el-dropdown-item command="cart">购物车</el-dropdown-item>
                    <el-dropdown-item v-if="userStore.isAdmin" command="admin" divided
                      >后台管理</el-dropdown-item
                    >
                    <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <router-link to="/login">登录</router-link>
              <span class="divider">|</span>
              <router-link to="/register">注册</router-link>
            </template>
          </div>
        </div>
      </div>
    </div>
    <div class="header-main">
      <div class="container">
        <div class="header-main-content">
          <router-link to="/" class="logo">
            <h1>My-eBookStore</h1>
          </router-link>

          <!-- 👇 修复：加了 v-model + 加了点击搜索 -->
          <div class="search-box">
            <el-input
              v-model="keyword"
              placeholder="搜索图书..."
              prefix-icon="Search"
              @keyup.enter="handleSearch"
            />
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </div>

          <router-link to="/cart" class="cart-link">
            <el-badge :value="cartCount" :hidden="cartCount === 0">
              <el-icon :size="28"><ShoppingCart /></el-icon>
            </el-badge>
            <span>购物车</span>
          </router-link>
        </div>
      </div>
    </div>
    <nav class="nav">
      <div class="container">
        <ul class="nav-list">
          <li><router-link to="/">首页</router-link></li>
          <li><router-link to="/books">图书</router-link></li>
          <li><router-link to="/announcements">书店公告</router-link></li>
          <li><router-link to="/messages">客户留言</router-link></li>
        </ul>
      </div>
    </nav>
  </header>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getLevel1Categories } from '@/api/book'

const router = useRouter()
const userStore = useUserStore()

const categories = ref([])
const cartCount = ref(0)

// 👇 修复：搜索关键词
const keyword = ref('')

onMounted(async () => {
  try {
    const res = await getLevel1Categories()
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类失败', error)
  }
})

const handleCommand = (command) => {
  switch (command) {
    case 'orders':
      router.push('/orders')
      break
    case 'cart':
      router.push('/cart')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/')
      break
  }
}

// 👇 修复：搜索事件
const handleSearch = () => {
  // 关键：去除关键词前后空格
  const searchWord = keyword.value.trim()
  if (!searchWord) {
    ElMessage.warning('请输入搜索内容')
    return
  }
  // 用 encodeURIComponent 安全编码中文，避免乱码和多余符号
  router.push(`/books?keyword=${encodeURIComponent(searchWord)}`)
}
</script>

<style scoped>
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-top {
  background: #f5f5f5;
  padding: 8px 0;
  font-size: 13px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header-top-content {
  display: flex;
  justify-content: space-between;
}

.user-links {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-links a {
  color: #666;
  text-decoration: none;
}

.user-links a:hover {
  color: #409eff;
}

.divider {
  color: #ddd;
}

.username {
  color: #409eff;
}

.header-main {
  padding: 20px 0;
}

.header-main-content {
  display: flex;
  align-items: center;
  gap: 30px;
}

.logo {
  text-decoration: none;
}

.logo h1 {
  color: #409eff;
  font-size: 28px;
  margin: 0;
}

.search-box {
  flex: 1;
  display: flex;
  gap: 10px;
}

.search-box .el-input {
  flex: 1;
}

.cart-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: #333;
  font-size: 13px;
}

.cart-link:hover {
  color: #409eff;
}

.nav {
  background: #409eff;
}

.nav-list {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav-list li a {
  display: block;
  padding: 14px 24px;
  color: #fff;
  text-decoration: none;
  font-size: 15px;
  transition: background 0.3s;
}

.nav-list li a:hover {
  background: #66b1ff;
}

.nav-list li a.router-link-active {
  background: #3a8ee6;
}
</style>
