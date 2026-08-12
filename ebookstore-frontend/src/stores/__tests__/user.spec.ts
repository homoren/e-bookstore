import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useUserStore } from '@/stores/user'

describe('user store', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
  })

  it('初始未登录', () => {
    const store = useUserStore()
    expect(store.isLoggedIn).toBe(false)
    expect(store.isAdmin).toBe(false)
  })

  it('登录后写入 token 与用户信息(走 MSW mock 接口)', async () => {
    const store = useUserStore()
    await store.login({ username: 'testuser', password: '123456' })

    expect(store.isLoggedIn).toBe(true)
    expect(store.userInfo.username).toBe('testuser')
    expect(store.userInfo.role).toBe(1)
    expect(store.isAdmin).toBe(false)
    expect(localStorage.getItem('token')).toBe('fake-token')
  })

  it('店主账号 isAdmin 为 true', async () => {
    const store = useUserStore()
    store.userInfo = { id: 1, username: 'admin', role: 2 }
    expect(store.isAdmin).toBe(true)
  })

  it('退出登录清空状态', async () => {
    const store = useUserStore()
    await store.login({ username: 'testuser', password: '123456' })
    store.logout()

    expect(store.isLoggedIn).toBe(false)
    expect(localStorage.getItem('token')).toBeNull()
  })
})
