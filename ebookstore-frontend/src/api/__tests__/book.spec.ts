import { describe, expect, it } from 'vitest'
import { getBookPage, getLevel1Categories } from '@/api/book'

describe('book api(MSW mock)', () => {
  it('getBookPage 返回服务端分页数据', async () => {
    const res = await getBookPage({ page: 1, pageSize: 12 })

    expect(res.success).toBe(true)
    expect(res.data.total).toBe(1)
    expect(res.data.list).toHaveLength(1)
    expect(res.data.list[0].title).toBe('TypeScript 实战')
  })

  it('未 mock 的接口请求会抛错(onUnhandledRequest: error)', async () => {
    await expect(getLevel1Categories()).rejects.toThrow()
  })
})
