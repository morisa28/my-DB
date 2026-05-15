import request from '../utils/request'

export const getCart = () => request.get('/cart')
export const getCartCount = () => request.get('/cart/count')
export const addCart = (data) => request.post('/cart', data)
export const updateCart = (id, quantity) => request.put(`/cart/${id}`, { quantity })
export const deleteCart = (id) => request.delete(`/cart/${id}`)
export const clearCart = () => request.delete('/cart/clear')

