import request from './index'
import type { Result } from './index'
import type { Order } from './types'

// 创建订单
export const createOrder = (data: Record<string, unknown>) => {
  return request.post<Result<Order>>('/orders/create', data)
}

// 获取订单列表
export const getOrderList = () => {
  return request.get<Result<Order[]>>('/orders/list')
}

// 获取订单详情
export const getOrderDetail = (id: number | string) => {
  return request.get<Result<Order>>(`/orders/detail/${id}`)
}

// 取消订单
export const cancelOrder = (id: number) => {
  return request.put<Result<null>>(`/orders/cancel/${id}`)
}
