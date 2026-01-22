import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  withCredentials: true
})

let refreshPromise: Promise<unknown> | null = null

client.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const config = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined
    const status = error.response?.status
    const url = config?.url || ''
    const shouldSkip = url.includes('/auth/refresh') || url.includes('/auth/login') || url.includes('/auth/admin/login') || url.includes('/auth/register') || url.includes('/auth/forgot-password') || url.includes('/auth/reset-password')
    if (status === 401 && config && !config._retry && !shouldSkip) {
      config._retry = true
      if (!refreshPromise) {
        refreshPromise = client.post('/auth/refresh').finally(() => {
          refreshPromise = null
        })
      }
      await refreshPromise
      return client(config)
    }
    return Promise.reject(error)
  }
)

export default client
