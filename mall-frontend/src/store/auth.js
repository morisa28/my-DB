import { defineStore } from 'pinia'
import { getUserInfo, login as loginApi, logout as logoutApi } from '../api/user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),
  getters: {
    isLogin: (state) => Boolean(state.token),
    isAdmin: (state) => state.user?.role === 1
  },
  actions: {
    async login(payload) {
      const data = await loginApi(payload)
      this.token = data.token
      this.user = data.user
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(data.user))
      return data.user
    },
    async fetchUser() {
      if (!this.token) return null
      this.user = await getUserInfo()
      localStorage.setItem('user', JSON.stringify(this.user))
      return this.user
    },
    async logout(remote = true) {
      if (remote && this.token) {
        try {
          await logoutApi()
        } catch {
          // 本地退出必须可靠，远端失效失败时仍清理浏览器状态。
        }
      }
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
