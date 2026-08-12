import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import Footer from '@/components/Footer.vue'

describe('Footer 组件', () => {
  it('渲染品牌与版权信息', () => {
    const wrapper = mount(Footer)

    expect(wrapper.text()).toContain('My-eBookStore')
    expect(wrapper.text()).toContain('All rights reserved')
  })
})
