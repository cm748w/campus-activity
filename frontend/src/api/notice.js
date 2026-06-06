import request from './request'

export const publishNotice = (data) => request.post('/notice/publish', data)
export const updateNotice = (id, data) => request.put(`/notice/${id}`, data)
export const deleteNotice = (id) => request.delete(`/notice/${id}`)
export const getNoticeDetail = (id) => request.get(`/notice/${id}`)
export const listNotices = (params) => request.get('/notice/list', { params })
export const listLatestNotices = (limit = 5) => request.get('/notice/latest', { params: { limit } })
