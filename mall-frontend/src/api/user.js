import request from '../utils/request'

export const register = (data) => request.post('/user/register', data)
export const login = (data) => request.post('/user/login', data)
export const getUserInfo = () => request.get('/user/info')
export const updateUserInfo = (data) => request.put('/user/info', data)
export const updatePassword = (data) => request.put('/user/password', data)

export const getAdminUsers = (params) => request.get('/admin/users', { params })
export const updateUserStatus = (id, status) => request.put(`/admin/users/${id}/status`, { status })
export const getAdminUserOrderSummary = (id) => request.get(`/admin/users/${id}/order-summary`)
