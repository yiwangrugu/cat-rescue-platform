import api from './index.js'

export const userApi = {
  // 获取用户统计数据
  getUserStatistics: (userId) => api.get(`/users/${userId}/statistics`),

  // 获取用户基本信息
  getUserInfo: (userId) => api.get(`/users/${userId}`),

  // 更新用户信息
  updateUser: (userData) => api.put(`/users/${userData.id}`, userData),

  // 获取用户活动历史
  getUserActivities: (userId, params) => api.get(`/users/${userId}/activities`, { params })
}