import request from '../utils/request'

export const getCategories = () => request.get('/categories')
export const getProducts = (params) => request.get('/products', { params })
export const getProductDetail = (id) => request.get(`/products/${id}`)

export const getAdminCategories = () => request.get('/admin/categories')
export const createCategory = (data) => request.post('/admin/categories', data)
export const updateCategory = (id, data) => request.put(`/admin/categories/${id}`, data)
export const deleteCategory = (id) => request.delete(`/admin/categories/${id}`)

export const getAdminProducts = (params) => request.get('/admin/products', { params })
export const createProduct = (data) => request.post('/admin/products', data)
export const updateProduct = (id, data) => request.put(`/admin/products/${id}`, data)
export const deleteProduct = (id) => request.delete(`/admin/products/${id}`)
export const updateProductStatus = (id, status) => request.put(`/admin/products/${id}/status`, { status })
export const updateProductStock = (id, stock) => request.put(`/admin/products/${id}/stock`, { stock })

