import { setupServer } from 'msw/node'
import { http, HttpResponse } from 'msw'

// MSW 在网络层 mock 后端接口,测试不依赖真实后端
export const handlers = [
  http.get('/api/books/page', () => {
    return HttpResponse.json({
      code: 200,
      success: true,
      message: '操作成功',
      data: {
        list: [
          { id: 1, title: 'TypeScript 实战', author: '张三', price: 59.9, stock: 20, difficultyLevel: 2 },
        ],
        total: 1,
        page: 1,
        pageSize: 12,
      },
    })
  }),
  http.post('/api/users/login', () => {
    return HttpResponse.json({
      code: 200,
      success: true,
      message: '登录成功',
      data: { token: 'fake-token', username: 'testuser', realName: '测试用户', role: 1 },
    })
  }),
]

export const server = setupServer(...handlers)
