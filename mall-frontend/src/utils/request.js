import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 12000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body?.code === 200) {
      return body.data
    }
    const message = body?.message || '请求失败'
    ElMessage.error(message)
    if (body?.code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(new Error(message))
  },
  (error) => {
    const status = error.response?.status
    const body = error.response?.data
    const message = status === 500
      ? '系统暂时不可用，请稍后重试'
      : body?.message || error.message || '网络异常'
    ElMessage.error(status === 403 ? '无权限执行该操作' : message)
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default request
