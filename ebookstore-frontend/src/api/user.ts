import request from './index'
import type { Result } from './index'
import type { LoginResult, UserInfo } from './types'

// 登录
export const login = (data: { username: string; password: string }) => {
  return request.post<Result<LoginResult>>('/users/login', data)
}

// 注册
export const register = (data: Record<string, unknown>) => {
  return request.post<Result<null>>('/users/register', data)
}

// 检查用户名
export const checkUsername = (username: string) => {
  return request.get<Result<boolean>>('/users/check-username', { params: { username } })
}

// 获取用户信息
export const getUserInfo = () => {
  return request.get<Result<UserInfo>>('/users/info')
}
