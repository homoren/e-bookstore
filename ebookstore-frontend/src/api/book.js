import request from './index'

// 获取一级分类
export const getLevel1Categories = () => {
  return request.get('/books/categories/level1')
}

// 获取二级分类
export const getLevel2Categories = (parentId) => {
  return request.get('/books/categories/level2', { params: { parentId } })
}

// 获取图书列表（支持 二级分类ID 或 一级分类ID）
// 获取图书列表（修复版：支持分类 + 关键词）
export const getBookList = (categoryId = 0, keyword = '') => {
  const params = { categoryId }
  if (keyword) params.keyword = keyword
  return request.get('/books/list', { params })
}

// 获取图书详情
export const getBookDetail = (id) => {
  return request.get(`/books/detail/${id}`)
}

// 获取一级分类下的所有图书
export const getBooksByParentCategory = (parentId) => {
  return request.get('/books/list-by-parent', { params: { parentId } })
}
