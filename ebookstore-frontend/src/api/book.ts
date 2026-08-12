import request from './index'
import type { Result, PageResult } from './index'
import type { BookDetail, BookList, Category } from './types'

// 获取一级分类
export const getLevel1Categories = () => {
  return request.get<Result<Category[]>>('/books/categories/level1')
}

// 获取二级分类
export const getLevel2Categories = (parentId: number) => {
  return request.get<Result<Category[]>>('/books/categories/level2', { params: { parentId } })
}

// 获取图书列表（支持 二级分类ID 或 一级分类ID，可带关键词）
export const getBookList = (categoryId = 0, keyword = '') => {
  const params: Record<string, unknown> = { categoryId }
  if (keyword) params.keyword = keyword
  return request.get<Result<BookList[]>>('/books/list', { params })
}

// 获取图书详情
export const getBookDetail = (id: number | string) => {
  return request.get<Result<BookDetail>>(`/books/detail/${id}`)
}

// 获取一级分类下的所有图书
export const getBooksByParentCategory = (parentId: number) => {
  return request.get<Result<BookList[]>>('/books/list-by-parent', { params: { parentId } })
}

// 分页获取图书列表（服务端分页 + 排序）
export const getBookPage = (params: Record<string, unknown>) => {
  return request.get<Result<PageResult<BookList>>>('/books/page', { params })
}
