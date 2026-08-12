import request from './index'

export const createBook = (data) => {
  return request.post('/admin/book/create', data)
}

export const updateBook = (id, data) => {
  return request.put(`/admin/book/update/${id}`, data)
}

export const toggleBookStatus = (id) => {
  return request.put(`/admin/book/status/${id}`)
}

export const getAllOrders = () => {
  return request.get('/admin/order/list')
}

export const confirmPayment = (id) => {
  return request.put(`/admin/order/confirm-payment/${id}`)
}

export const confirmDelivery = (id) => {
  return request.put(`/admin/order/confirm-delivery/${id}`)
}

export const completeOrder = (id, receiptSignature) => {
  return request.put(`/admin/order/complete/${id}`, null, {
    params: { receiptSignature }
  })
}

export const getTodaySettlement = () => {
  return request.get('/admin/settlement/today')
}

export const getSettlements = (startDate, endDate) => {
  if (startDate && endDate) {
    return request.get('/admin/settlement/range', { params: { startDate, endDate } })
  }
  return request.get('/admin/settlement/list')
}

export const generateSettlement = (date) => {
  return request.post('/admin/settlement/generate', null, { params: { date } })
}

export const getAllCustomers = () => {
  return request.get('/admin/customer/list')
}

export const getPurchases = () => {
  return request.get('/admin/purchase/list')
}

export const createPurchase = (data) => {
  return request.post('/admin/purchase/create', data)
}


export const getAllAnnouncements = () => {
  return request.get('/admin/announcement/list')
}

export const createAnnouncement = (data) => {
  return request.post('/admin/announcement/create', data)
}

export const updateAnnouncement = (id, data) => {
  return request.put(`/admin/announcement/update/${id}`, data)
}

export const deleteAnnouncement = (id) => {
  return request.delete(`/admin/announcement/delete/${id}`)
}

export const getAllMessages = () => {
  return request.get('/admin/message/list')
}

export const replyMessage = (id, data) => {
  return request.put(`/admin/message/reply/${id}`, data)
}

export const updateMessageStatus = (id, status) => {
  return request.put(`/admin/message/toggle/${id}`, null, { params: { status } })
}

export const deleteMessage = (id) => {
  return request.delete(`/admin/message/delete/${id}`)
}
