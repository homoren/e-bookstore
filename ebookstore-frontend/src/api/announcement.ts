import request from './index'
import type { Result } from './index'
import type { Announcement } from './types'

// 获取已发布公告列表
export const getPublishedAnnouncements = () => {
  return request.get<Result<Announcement[]>>('/announcements/list')
}

// 获取公告详情
export const getAnnouncementDetail = (id: number | string) => {
  return request.get<Result<Announcement>>(`/announcements/detail/${id}`)
}
