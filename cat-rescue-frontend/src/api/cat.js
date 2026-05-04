import api from './index.js'

export const catApi = {
  getCats: (page = 1, size = 10, status = null, breed = null, auditStatus = null) => {
    const params = { page, size }
    if (status && status !== 'null' && status !== 'undefined') params.status = status
    if (breed && breed !== 'null' && breed !== 'undefined') params.breed = breed
    if (auditStatus && auditStatus !== 'null' && auditStatus !== 'undefined') params.auditStatus = auditStatus
    return api.get('/cats', { params })
  },

  getCat: (id) => api.get(`/cats/${id}`),

  createCat: (cat) => api.post('/cats', cat),

  updateCat: (id, cat) => api.put(`/cats/${id}`, cat),

  deleteCat: (id) => api.delete(`/cats/${id}`),

  getBreeds: () => api.get('/cats/breeds'),

  getCatsByRescuer: (rescuerId) => api.get(`/cats/rescuer/${rescuerId}`),

  // 审核相关接口
  getPendingCats: (page = 1, size = 10) => {
    return api.get('/cats/pending', { params: { page, size } })
  },

  auditCat: (id, status, remark, auditorId) => {
    const params = { status }
    if (remark) params.remark = remark
    if (auditorId) params.auditorId = auditorId
    return api.post(`/cats/${id}/audit`, null, { params })
  },

  submitCat: (cat) => api.post('/cats/rescuer/submit', cat)
}