import api from './index.js'

export const volunteerApi = {
  // 获取志愿者列表
  getVolunteerList: (params) => api.get('/volunteers', { params }),

  // 获取今日值班志愿者列表
  getTodayOnDutyVolunteers: () => api.get('/volunteer-schedules/today'),

  // 分页获取志愿者列表
  getVolunteerPage: (params) => api.get('/volunteers/page', { params }),

  // 获取志愿者详情
  getVolunteerDetail: (id) => api.get(`/volunteers/${id}`),

  // 创建志愿者（仅管理端）
  createVolunteer: (data) => api.post('/admin/volunteers', data),

  // 更新志愿者（仅管理端）
  updateVolunteer: (id, data) => api.put(`/admin/volunteers/${id}`, data),

  // 删除志愿者（仅管理端）
  deleteVolunteer: (id) => api.delete(`/admin/volunteers/${id}`),

  // 切换志愿者状态（仅管理端）
  toggleVolunteerStatus: (id) => api.put(`/admin/volunteers/${id}/toggle-status`)
}