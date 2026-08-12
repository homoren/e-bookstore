// 与后端 DTO 对齐的前端类型定义
export interface Category {
  id: number
  name: string
  parentId: number
  sortOrder: number
  createdAt?: string
  books?: BookList[]
}

export interface BookList {
  id: number
  title: string
  author?: string
  coverImage?: string
  price: number
  stock: number
  difficultyLevel?: number
  publisher?: string
  salesCount?: number
}

export interface BookDetail extends BookList {
  isbn?: string
  publishDate?: string
  categoryId?: number
  costPrice?: number
  description?: string
  detailHtml?: string
  sampleCodeUrl?: string
  categoryName?: string
  parentCategoryName?: string
  status?: number
}

export interface UserInfo {
  id: number
  username: string
  realName?: string
  email?: string
  phone?: string
  address?: string
  role: number
}

export interface LoginResult {
  token: string
  username: string
  realName?: string
  role: number
}

export interface CartItem {
  id: number
  bookId: number
  bookTitle: string
  bookAuthor?: string
  coverImage?: string
  price: number
  quantity: number
  stock: number
}

export interface OrderItem {
  id?: number
  bookId: number
  bookTitle: string
  bookAuthor?: string
  bookPrice: number
  quantity: number
  subtotal: number
}

export interface Order {
  id: number
  orderNo: string
  userId: number
  totalAmount: number
  status: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  remark?: string
  paymentDeadline?: string
  deliveryDeadline?: string
  paidAt?: string
  deliveredAt?: string
  completedAt?: string
  createdAt?: string
  items?: OrderItem[]
}

export interface Announcement {
  id: number
  title: string
  content: string
  isTop?: number
  status?: number
  viewCount?: number
  createdAt?: string
}

export interface Message {
  id: number
  userId?: number | null
  username: string
  content: string
  reply?: string
  repliedAt?: string
  status?: number
  createdAt?: string
}

export interface PurchaseItem {
  id?: number
  bookId: number
  bookTitle: string
  quantity: number
  costPrice: number
  subtotal: number
}

export interface Purchase {
  id: number
  purchaseNo: string
  supplier?: string
  totalCost: number
  status: number
  remark?: string
  createdAt?: string
  items?: PurchaseItem[]
}

export interface Customer {
  userId: number
  username: string
  realName?: string
  email?: string
  phone?: string
  address?: string
  role: number
  status: number
  createdAt?: string
}

export interface DailySettlement {
  id: number
  settleDate: string
  totalSales: number
  totalCost: number
  totalProfit: number
  orderCount: number
  paidOrderCount: number
}
