import { defineStore } from 'pinia'
import { login, logout, getCurrentUser } from '../api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null
  }),
  actions: {
    async login(loginForm) {
      const data = await login(loginForm)
      this.user = data
      if (data.token) {
        sessionStorage.setItem('token', data.token)
      }
      return data
    },
    async logout() {
      await logout()
      this.user = null
      sessionStorage.removeItem('token')
    },
    async fetchUser() {
      try {
        const data = await getCurrentUser()
        this.user = data
      } catch (error) {
        this.user = null
      }
    }
  }
})
