import request from '../utils/request'

export const createOrder = (data) => request.post('/orders', data)
export const getOrders = (params) => request.get('/orders', { params })
export const getOrderDetail = (id) => request.get(`/orders/${id}`)
export const submitPaymentNote = (id, data) => request.put(`/orders/${id}/payment-note`, data)
export const cancelOrder = (id) => request.put(`/orders/${id}/cancel`)
export const confirmReceipt = (id) => request.put(`/orders/${id}/confirm-receipt`)

export const getAdminOrders = (params) => request.get('/admin/orders', { params })
export const getAdminOrderDetail = (id) => request.get(`/admin/orders/${id}`)
export const confirmPayment = (id, data) => request.post(`/admin/orders/${id}/confirm-payment`, data)
export const shipOrder = (id, data) => request.put(`/admin/orders/${id}/ship`, data)
export const getStatistics = () => request.get('/admin/statistics')
