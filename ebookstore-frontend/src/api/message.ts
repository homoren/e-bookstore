import request from './index'
import type { Result } from './index'
import type { Message } from './types'

// 获取留言列表
export const getMessages = () => {
  return request.get<Result<Message[]>>('/messages/list')
}

// 发布留言
export const createMessage = (data: { content: string }) => {
  return request.post<Result<Message>>('/messages/create', data)
}
