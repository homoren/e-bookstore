import request from './index'
import type { Result } from './index'
import type { CartItem } from './types'

// 获取购物车列表
export const getCartList = () => {
  return request.get<Result<CartItem[]>>('/cart/list')
}

// 添加到购物车
export const addToCart = (data: { bookId: number; quantity: number }) => {
  return request.post<Result<null>>('/cart/add', data)
}

// 更新数量
export const updateCartItem = (id: number, data: { quantity: number }) => {
  return request.put<Result<null>>(`/cart/update/${id}`, data)
}

// 删除购物车项
export const deleteCartItem = (id: number) => {
  return request.delete<Result<null>>(`/cart/delete/${id}`)
}

// 清空购物车
export const clearCart = () => {
  return request.delete<Result<null>>('/cart/clear')
}
