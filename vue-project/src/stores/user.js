import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),
  actions: {
    async login(loginForm) {
      const res = await request.post('/user/login', loginForm)
      this.token = res.data.token
      this.userInfo = res.data.user
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userInfo', JSON.stringify(res.data.user))
    },
    async logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
    async updateInfo(user) {
      await request.put('/user/update', user)
      // 更新本地存储
      const newUser = { ...this.userInfo, ...user }
      this.userInfo = newUser
      localStorage.setItem('userInfo', JSON.stringify(newUser))
    }
  }
})
