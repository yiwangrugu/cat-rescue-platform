import api from './index.js'

export const adminApi = {
  // 获取用户列表
  getUsers: (params) => api.get('/admin/users', { params }),

  // 获取用户详情
  getUser: (userId) => api.get(`/admin/users/${userId}`),

  // 添加用户
  addUser: (userData) => api.post('/admin/users', userData),

  // 编辑用户
  updateUser: (userId, userData) => api.put(`/admin/users/${userId}`, userData),

  // 切换用户状态
  toggleUserStatus: (userId, duration) => api.post(`/admin/users/${userId}/toggle-status`, null, {
    params: duration ? { duration: duration } : {}
  }),

  // 获取猫咪列表
  getCats: (params) => api.get('/admin/cats', { params }),

  // 获取统计信息
  getStatistics: () => api.get('/admin/statistics/realtime'),

  // 获取实时统计信息（别名）
  getRealTimeStatistics: () => api.get('/admin/statistics/realtime'),

  // 获取日期区间统计数据
  getDateRangeStatistics: (params) => api.get('/admin/statistics/date-range', { params }),

  // 导出用户数据
  exportUsers: (params) => api.get('/admin/export/users', { responseType: 'blob', params }),

  // 导出猫咪数据
  exportCats: (params) => api.get('/admin/export/cats', { responseType: 'blob', params }),

  // 导出救助数据
  exportRescues: (params) => api.get('/admin/export/rescues', { responseType: 'blob', params }),

  // 导出领养数据
  exportAdoptions: (params) => api.get('/admin/export/adoptions', { responseType: 'blob', params }),

  // 导出帖子数据
  exportPosts: (params) => api.get('/admin/export/posts', { responseType: 'blob', params }),

  // 导出志愿者数据
  exportVolunteers: (params) => api.get('/admin/export/volunteers', { responseType: 'blob', params }),

  // 获取待办事务统计
  getPendingCounts: () => api.get('/admin/pending-counts'),

  // 导出志愿者排班表
  exportVolunteerSchedules: () => api.get('/admin/export/volunteer-schedules', { responseType: 'blob' }),

  // 导出评论数据
  exportComments: (params) => api.get('/admin/export/comments', { responseType: 'blob', params }),

  // 志愿者管理
  getVolunteers: (params) => api.get('/admin/volunteers/page', { params }),
  getAllVolunteers: () => api.get('/admin/volunteers'),
  getVolunteer: (id) => api.get(`/admin/volunteers/${id}`),
  addVolunteer: (data) => api.post('/admin/volunteers', data),
  updateVolunteer: (id, data) => api.put(`/admin/volunteers/${id}`, data),
  deleteVolunteer: (id) => api.delete(`/admin/volunteers/${id}`),
  toggleVolunteerStatus: (id) => api.put(`/admin/volunteers/${id}/toggle-status`),

  // 志愿者值班安排
  getVolunteerSchedules: (volunteerId) => api.get(`/admin/volunteer-schedules/volunteer/${volunteerId}`),
  getVolunteerSchedulesMap: (volunteerId) => api.get(`/admin/volunteer-schedules/volunteer/${volunteerId}/map`),
  getSchedulesByDay: (dayOfWeek) => api.get(`/admin/volunteer-schedules/day/${dayOfWeek}`),
  saveVolunteerSchedules: (volunteerId, schedules) => api.post(`/admin/volunteer-schedules/volunteer/${volunteerId}`, schedules),
  initVolunteerSchedules: (volunteerId) => api.post(`/admin/volunteer-schedules/volunteer/${volunteerId}/init`),
  updateVolunteerSchedule: (id, schedule) => api.put(`/admin/volunteer-schedules/${id}`, schedule),
  deleteVolunteerSchedules: (volunteerId) => api.delete(`/admin/volunteer-schedules/volunteer/${volunteerId}`),

  // 删除用户
  deleteUser: (userId) => api.delete(`/admin/users/${userId}`),

  // 删除猫咪
  deleteCat: (catId) => api.delete(`/admin/cats/${catId}`),

  // 获取系统日志
  getSystemLogs: (params) => api.get('/admin/logs', { params }),

  // 获取待办任务统计
  getPendingCounts: () => api.get('/admin/pending-counts'),

  // 获取待审核猫咪列表
  getPendingCats: () => api.get('/admin/pending-cats'),

  // 获取已通过猫咪列表
  getApprovedCats: (page = 1, size = 10) => api.get('/admin/approved-cats', { params: { page, size } }),

  // 获取已拒绝猫咪列表
  getRejectedCats: (page = 1, size = 10) => api.get('/admin/rejected-cats', { params: { page, size } }),

  // 获取待审核帖子列表
  getPendingPosts: (page = 1, size = 10) => api.get('/admin/pending-posts', { params: { page, size } }),

  // 审核通过帖子
  approvePost: (postId, data) => api.post(`/admin/posts/${postId}/approve`, data),

  // 审核拒绝帖子
  rejectPost: (postId, data) => api.post(`/admin/posts/${postId}/reject`, data),

  // 获取帖子统计信息
  getPostStatistics: () => api.get('/admin/posts/statistics'),

  // 获取待处理救助列表
  getPendingRescues: (page = 1, size = 10) => api.get('/admin/pending-rescues', { params: { page, size } }),

  // 获取待审核领养列表
  getPendingAdoptions: (page = 1, size = 10) => api.get('/admin/pending-adoptions', { params: { page, size } }),

  // 获取已通过领养列表
  getApprovedAdoptions: (page = 1, size = 10) => api.get('/admin/approved-adoptions', { params: { page, size } }),

  // 获取已拒绝领养列表
  getRejectedAdoptions: (page = 1, size = 10) => api.get('/admin/rejected-adoptions', { params: { page, size } }),

  // 审核猫咪
  reviewCat: (catId, action, remark = '') => api.post(`/admin/cats/${catId}/review`, { action, remark }),

  // 审核帖子
  reviewPost: (postId, action, data = {}) => api.post(`/admin/posts/${postId}/review`, { action, ...data }),

  // 处理救助
  processRescue: (rescueId, action) => api.post(`/admin/rescues/${rescueId}/process`, { action }),

  // 审核领养
  reviewAdoption: (adoptionId, data) => api.post(`/admin/adoptions/${adoptionId}/review`, data),

  // 获取领养申请详情
  getAdoptionDetail: (adoptionId) => api.get(`/admin/adoptions/${adoptionId}`),

  // 获取已通过帖子列表
  getApprovedPosts: (page = 1, size = 10) => api.get('/admin/approved-posts', { params: { page, size } }),

  // 获取已拒绝帖子列表
  getRejectedPosts: (page = 1, size = 10) => api.get('/admin/rejected-posts', { params: { page, size } }),

  // 获取所有帖子列表（管理员专用）
  getAllPosts: (page = 1, size = 100) => api.get('/admin/all-posts', { params: { page, size } }),

  // 获取系统日志模块列表
  getLogModules: () => api.get('/admin/logs/modules'),

  // 获取系统日志操作列表
  getLogActions: () => api.get('/admin/logs/actions'),

  // 获取系统日志统计信息
  getLogStatistics: (params) => api.get('/admin/logs/statistics', { params }),

  // 清理过期日志
  cleanOldLogs: (params) => api.delete('/admin/logs/clean', { params }),

  // 导出系统日志
  exportSystemLogs: (params) => api.get('/admin/export/system-logs', { responseType: 'blob', params })
}