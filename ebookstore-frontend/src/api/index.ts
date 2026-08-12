import axios from 'axios'
import type { AxiosRequestConfig } from 'axios'

export interface Result<T = unknown> {
  code: number
  success: boolean
  message: string
  data: T
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

const instance = axios.create({
  // 走 Vite 代理,避免跨域;生产环境可改为后端地址
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 响应拦截器:成功时透传响应体,失败时统一提示并 reject
instance.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.success === false) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      ElMessage.error('登录已过期，请重新登录')
      setTimeout(() => {
        window.location.href = '/login'
      }, 1500)
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误')
    }
    return Promise.reject(error)
  },
)

// 拦截器已将响应体透传,因此方法泛型即响应体类型(通常是 Result<T>)
const request = instance as unknown as {
  get<T = Result>(url: string, config?: AxiosRequestConfig): Promise<T>
  post<T = Result>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T>
  put<T = Result>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T>
  delete<T = Result>(url: string, config?: AxiosRequestConfig): Promise<T>
}

export default request
