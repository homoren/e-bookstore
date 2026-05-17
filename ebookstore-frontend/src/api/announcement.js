import request from './index'

// 获取已发布公告列表
export const getPublishedAnnouncements = () => {
  return request.get('/announcements/list')
}

// 获取公告详情
export const getAnnouncementDetail = (id) => {
  return request.get(`/announcements/detail/${id}`)
}
