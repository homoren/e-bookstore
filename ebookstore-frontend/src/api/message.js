import request from './index'

// 获取留言列表
export const getMessages = () => {
  return request.get('/messages/list')
}

// 发布留言
export const createMessage = (data) => {
  return request.post('/messages/create', data)
}
