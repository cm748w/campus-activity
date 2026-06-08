import request from './request'

export const publishActivity = (data) => request.post('/activity/publish', data)
export const updateActivity = (id, data) => request.put(`/activity/${id}`, data)
export const deleteActivity = (id) => request.delete(`/activity/${id}`)
export const listActivities = (params) => request.get('/activity/list', { params })
export const getActivityDetail = (id) => request.get(`/activity/${id}`)
export const auditActivity = (id, status, rejectReason) => request.put(`/activity/audit/${id}`, null, { params: { status, rejectReason: rejectReason || '' } })
export const listOrganizerActivities = (params) => request.get('/activity/organizer/list', { params })
export const getActivityStats = (id) => request.get(`/activity/stats/${id}`)
export const listActivityTypes = () => request.get('/activity/types')
