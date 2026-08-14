import request from './index'
import type { Result } from './index'
import type {
  Announcement,
  BookDetail,
  Customer,
  DailySettlement,
  Message,
  Order,
  Purchase,
  TodayStats,
} from './types'

export const createBook = (data: Record<string, unknown>) => {
  return request.post<Result<BookDetail>>('/admin/book/create', data)
}

export const updateBook = (id: number, data: Record<string, unknown>) => {
  return request.put<Result<null>>(`/admin/book/update/${id}`, data)
}

export const toggleBookStatus = (id: number) => {
  return request.put<Result<null>>(`/admin/book/status/${id}`)
}

export const getAllOrders = () => {
  return request.get<Result<Order[]>>('/admin/order/list')
}

export const confirmPayment = (id: number) => {
  return request.put<Result<null>>(`/admin/order/confirm-payment/${id}`)
}

export const confirmDelivery = (id: number) => {
  return request.put<Result<null>>(`/admin/order/confirm-delivery/${id}`)
}

export const completeOrder = (id: number, receiptSignature?: string) => {
  return request.put<Result<null>>(`/admin/order/complete/${id}`, null, {
    params: { receiptSignature }
  })
}

export const getTodaySettlement = () => {
  return request.get<Result<DailySettlement>>('/admin/settlement/today')
}

// 今日实时统计(订单数/销售额/利润/会员数)
export const getTodayStats = () => {
  return request.get<Result<TodayStats>>('/admin/stats/today')
}

export const getSettlements = (startDate?: string, endDate?: string) => {
  if (startDate && endDate) {
    return request.get<Result<DailySettlement[]>>('/admin/settlement/range', {
      params: { startDate, endDate }
    })
  }
  return request.get<Result<DailySettlement[]>>('/admin/settlement/list')
}

export const generateSettlement = (date: string) => {
  return request.post<Result<DailySettlement>>('/admin/settlement/generate', null, { params: { date } })
}

export const getAllCustomers = () => {
  return request.get<Result<Customer[]>>('/admin/customer/list')
}

export const getPurchases = () => {
  return request.get<Result<Purchase[]>>('/admin/purchase/list')
}

export const createPurchase = (data: Record<string, unknown>) => {
  return request.post<Result<Purchase>>('/admin/purchase/create', data)
}

export const getAllAnnouncements = () => {
  return request.get<Result<Announcement[]>>('/admin/announcement/list')
}

export const createAnnouncement = (data: Record<string, unknown>) => {
  return request.post<Result<Announcement>>('/admin/announcement/create', data)
}

export const updateAnnouncement = (id: number, data: Record<string, unknown>) => {
  return request.put<Result<Announcement>>(`/admin/announcement/update/${id}`, data)
}

export const deleteAnnouncement = (id: number) => {
  return request.delete<Result<null>>(`/admin/announcement/delete/${id}`)
}

export const getAllMessages = () => {
  return request.get<Result<Message[]>>('/admin/message/list')
}

export const replyMessage = (id: number, data: Record<string, unknown>) => {
  return request.put<Result<Message>>(`/admin/message/reply/${id}`, data)
}

export const updateMessageStatus = (id: number, status: number) => {
  return request.put<Result<null>>(`/admin/message/toggle/${id}`, null, { params: { status } })
}

export const deleteMessage = (id: number) => {
  return request.delete<Result<null>>(`/admin/message/delete/${id}`)
}
