import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getUserInfo } from '@/api/user'
import type { LoginResult, UserInfo } from '@/api/types'

const ROLE_ADMIN = 2

// 从 localStorage 加载用户信息;处理残留的 "null"/损坏 JSON,避免 userInfo 为 null 导致模板崩溃
function loadInitialUserInfo(): UserInfo {
  const raw = localStorage.getItem('userInfo')
  if (!raw) return {} as UserInfo
  try {
    return (JSON.parse(raw) || {}) as UserInfo
  } catch {
    return {} as UserInfo
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo>(loadInitialUserInfo())

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value.role === ROLE_ADMIN)

  // 登录
  const login = async (loginData: { username: string; password: string }) => {
    const res = await loginApi(loginData)
    const data: LoginResult = res.data
    token.value = data.token
    userInfo.value = {
      username: data.username,
      realName: data.realName,
      role: data.role,
    } as UserInfo

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
    userInfo.value = {} as UserInfo
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
