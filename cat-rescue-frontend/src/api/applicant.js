import api from './index.js'

export const applicantApi = {
  // 获取当前用户的申请人信息
  getMyApplicantInfo: () => api.get('/applicants/my'),
  
  // 创建或更新申请人信息
  saveApplicantInfo: (data) => api.post('/applicants/save', data),
  
  // 检查当前用户是否已有申请人信息
  checkApplicantInfo: () => api.get('/applicants/check'),
  
  // 根据ID获取申请人信息（管理员使用）
  getApplicantById: (id) => api.get(`/applicants/${id}`),
  
  // 获取所有申请人信息（管理员使用）
  getAllApplicants: () => api.get('/applicants/all')
}