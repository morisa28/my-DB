import request from '../utils/request'

export const getAdminOperationLogs = (params) => request.get('/admin/operation-logs', { params })
