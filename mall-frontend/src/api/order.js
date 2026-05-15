import request from '../utils/request'

export const createOrder = (data) => request.post('/orders', data)
export const getOrders = (params) => request.get('/orders', { params })
export const getOrderDetail = (id) => request.get(`/orders/${id}`)
export const payOrder = (id) => request.put(`/orders/${id}/pay`)
export const cancelOrder = (id) => request.put(`/orders/${id}/cancel`)

export const getAdminOrders = (params) => request.get('/admin/orders', { params })
export const getAdminOrderDetail = (id) => request.get(`/admin/orders/${id}`)
export const shipOrder = (id) => request.put(`/admin/orders/${id}/ship`)
export const getStatistics = () => request.get('/admin/statistics')

