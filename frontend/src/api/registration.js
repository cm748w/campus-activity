import request from './request'

export const registerActivity = (activityId, remark) => request.post(`/registration/apply/${activityId}?remark=${remark || ''}`)
export const cancelRegistration = (id) => request.put(`/registration/cancel/${id}`)
export const auditRegistration = (id, status, rejectReason) => request.put(`/registration/audit/${id}?status=${status}&rejectReason=${rejectReason || ''}`)
export const listRegistrations = (activityId, params) => request.get(`/registration/list/${activityId}`, { params })
export const listMyRegistrations = (params) => request.get('/registration/my', { params })
export const exportApprovedList = (activityId) => {
  window.open(`/api/registration/export/${activityId}`, '_blank')
}
