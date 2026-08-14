import type { components } from './generated'

// 以 SpringDoc 生成的 OpenAPI schema 为唯一来源(运行 npm run gen:types 重新生成后自动同步)。
// 生成字段默认可选,此处用交叉类型补回前端实际必用的必填字段。
type Schema = components['schemas']

export type Category = Schema['Category'] & {
  id: number
  name: string
  parentId: number
  sortOrder: number
  books?: BookList[]
}

// 列表接口未返回 salesCount/categoryId 等,前端展示需要,标可选
export type BookList = Schema['BookListDTO'] & {
  id: number
  title: string
  price: number
  stock: number
  salesCount?: number
  categoryId?: number
  costPrice?: number
  status?: number
  isbn?: string
  description?: string
}

export type BookDetail = Schema['BookDetailDTO'] & {
  id: number
  stock: number
  categoryId?: number
  costPrice?: number
  status?: number
  salesCount?: number
}

export type UserInfo = Schema['UserInfoDTO'] & {
  id: number
  username: string
  role: number
}

export type LoginResult = Schema['LoginResponse'] & {
  token: string
  username: string
  role: number
}

export type CartItem = Schema['CartItemDTO'] & {
  id: number
  bookId: number
  price: number
  quantity: number
  stock: number
}

export type OrderItem = Schema['OrderItemDTO'] & {
  bookId: number
  bookPrice: number
  quantity: number
}

export type Order = Schema['OrderDTO'] & {
  id: number
  totalAmount: number
  status: number
}

export type Announcement = Schema['AnnouncementDTO'] & {
  id: number
  title: string
  content: string
  status?: number
}

export type Message = Schema['MessageDTO'] & {
  id: number
  username: string
  content: string
  status?: number
}

export type Purchase = Schema['PurchaseDTO'] & {
  id: number
  totalCost: number
  status: number
}

export type PurchaseItem = Schema['PurchaseItemDTO'] & {
  bookId: number
  quantity: number
  costPrice: number
}

export type Customer = Schema['CustomerDTO'] & {
  userId?: number
}

export type DailySettlement = Schema['DailySettlementDTO'] & {
  id: number
  settleDate: string
  totalSales: number
  totalCost: number
  totalProfit: number
  orderCount: number
  paidOrderCount: number
}

// 店主后台今日实时统计
export interface TodayStats {
  orderCount: number
  totalSales: number
  totalProfit: number
  memberCount: number
}
