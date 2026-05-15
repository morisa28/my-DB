import request from '../utils/request'

export const getAddresses = () => request.get('/address')
export const createAddress = (data) => request.post('/address', data)
export const updateAddress = (id, data) => request.put(`/address/${id}`, data)
export const deleteAddress = (id) => request.delete(`/address/${id}`)
export const setDefaultAddress = (id) => request.put(`/address/${id}/default`)

