import request from './index'

// 登录
export const login = (data) => {
  return request.post('/users/login', data)
}

// 注册
export const register = (data) => {
  return request.post('/users/register', data)
}

// 检查用户名
export const checkUsername = (username) => {
  return request.get('/users/check-username', { params: { username } })
}

// 获取用户信息
export const getUserInfo = () => {
  return request.get('/users/info')
}
