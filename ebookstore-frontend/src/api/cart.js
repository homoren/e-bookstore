import request from './index'

// 获取购物车列表
export const getCartList = () => {
  return request.get('/cart/list')
}

// 添加到购物车
export const addToCart = (data) => {
  return request.post('/cart/add', data)
}

// 更新数量
export const updateCartItem = (id, data) => {
  return request.put(`/cart/update/${id}`, data)
}

// 删除购物车项
export const deleteCartItem = (id) => {
  return request.delete(`/cart/delete/${id}`)
}

// 清空购物车
export const clearCart = () => {
  return request.delete('/cart/clear')
}
