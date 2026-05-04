import api from './index.js'

export const adoptionApi = {
  // 获取领养申请列表
  getAdoptionList: (params) => api.get('/adoptions', { params }),

  // 获取领养申请详情
  getAdoptionDetail: (id) => api.get(`/adoptions/${id}`),

  // 创建领养申请
  createAdoption: (data) => api.post('/adoptions', data),

  // 更新领养申请
  updateAdoption: (id, data) => api.put(`/adoptions/${id}`, data),

  // 删除领养申请
  deleteAdoption: (id) => api.delete(`/adoptions/${id}`),

  // 获取我的领养申请
  getMyApplications: (params) => api.get('/adoptions/my', { params }),

  // 获取待审核的领养申请
  getPendingApplications: (params) => api.get('/adoptions/pending', { params }),

  // 获取已通过的领养申请（管理端API）
  getApprovedApplications: (params) => api.get('/admin/approved-adoptions', { params }),

  // 获取已拒绝的领养申请（管理端API）
  getRejectedApplications: (params) => api.get('/admin/rejected-adoptions', { params }),

  // 审核领养申请
  reviewAdoption: (id, data) => api.post(`/adoptions/${id}/review`, data),

  // 通过领养申请
  approveApplication: (id) => api.post(`/adoptions/${id}/approve`),

  // 拒绝领养申请
  rejectApplication: (id, data) => api.post(`/adoptions/${id}/reject`, data),

  // 取消领养申请
  cancelApplication: (id) => api.post(`/adoptions/${id}/cancel`)
}