import request from './request'

export const login = (data) => request.post('/user/login', data)
export const register = (data) => request.post('/user/register', data)
export const getCurrentUser = () => request.get('/user/info')
export const updatePassword = (data) => request.put('/user/password', data)
export const updateUserInfo = (data) => request.put('/user/info', data)
export const listUsers = (params) => request.get('/user/list', { params })
export const updateUserStatus = (userId, status) => request.put(`/user/status/${userId}?status=${status}`)
