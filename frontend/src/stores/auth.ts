import { defineStore } from 'pinia'
import client from '../api/client'

export interface UserProfile {
  id: number
  username: string
  email: string
  avatarUrl?: string
  followers?: number
  rating?: number
  reviewCount?: number
}

interface AuthState {
  user: UserProfile | null
  loading: boolean
  error: string | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: null,
    loading: false,
    error: null
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.user)
  },
  actions: {
    async login(email: string, password: string) {
      this.loading = true
      this.error = null
      try {
        const { data } = await client.post('/auth/login', { email, password })
        this.user = data.user
      } catch (error: any) {
        this.error = error.response?.data?.message || 'Đăng nhập thất bại'
        throw error
      } finally {
        this.loading = false
      }
    },
    async register(username: string, email: string, password: string) {
      this.loading = true
      this.error = null
      try {
        const { data } = await client.post('/auth/register', { username, email, password })
        this.user = data.user
      } catch (error: any) {
        this.error = error.response?.data?.message || 'Đăng ký thất bại'
        throw error
      } finally {
        this.loading = false
      }
    },
    async logout() {
      await client.post('/auth/logout')
      this.user = null
    },
    async forgotPassword(email: string) {
      await client.post('/auth/forgot-password', { email })
    },
    async resetPassword(token: string, newPassword: string) {
      await client.post('/auth/reset-password', { token, newPassword })
    }
  }
})
