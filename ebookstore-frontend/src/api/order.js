import request from './index'

// 创建订单
export const createOrder = (data) => {
  return request.post('/orders/create', data)
}

// 获取订单列表
export const getOrderList = () => {
  return request.get('/orders/list')
}

// 获取订单详情
export const getOrderDetail = (id) => {
  return request.get(`/orders/detail/${id}`)
}

// 取消订单
export const cancelOrder = (id) => {
  return request.put(`/orders/cancel/${id}`)
}
