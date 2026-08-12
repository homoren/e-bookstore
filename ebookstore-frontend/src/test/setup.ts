import { afterAll, afterEach, beforeAll } from 'vitest'
import { server } from './server'

// Node 25 自带实验性 localStorage,与 jsdom 冲突,这里用内存实现兜底,保证测试环境一致
const memory = new Map<string, string>()
const localStorageMock = {
  getItem: (key: string) => memory.get(key) ?? null,
  setItem: (key: string, value: string) => memory.set(key, String(value)),
  removeItem: (key: string) => memory.delete(key),
  clear: () => memory.clear(),
  key: (index: number) => [...memory.keys()][index] ?? null,
  get length() {
    return memory.size
  },
}
Object.defineProperty(globalThis, 'localStorage', { value: localStorageMock, configurable: true })

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())
