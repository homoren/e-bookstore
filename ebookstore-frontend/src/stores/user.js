import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value.role === 2)

  // 登录
  const login = async (loginData) => {
    const res = await loginApi(loginData)
    token.value = res.data.token
    userInfo.value = {
      username: res.data.username,
      realName: res.data.realName,
      role: res.data.role,
    }

    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))

    return res
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    if (!token.value) return
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    } catch {
      logout()
    }
  }

  // 退出登录
  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    login,
    fetchUserInfo,
    logout,
  }
})
