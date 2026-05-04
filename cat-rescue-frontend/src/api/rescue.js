import api from './index.js'

export const rescueApi = {
  // 获取救助需求列表
  getRescueList: (params) => api.get('/rescues', { params }),

  // 获取救助需求详情
  getRescueDetail: (id) => api.get(`/rescues/${id}`),

  // 创建救助需求
  createRescue: (data) => api.post('/rescues', data),

  // 更新救助需求
  updateRescue: (id, data) => api.put(`/rescues/${id}`, data),

  // 删除救助需求
  deleteRescue: (id) => api.delete(`/rescues/${id}`),

  // 接单救助
  takeRescue: (id, rescuerId) => api.post(`/rescues/${id}/take`, rescuerId),

  // 接单救助（带志愿者信息）
  takeRescueWithVolunteer: (id, data) => api.post(`/rescues/${id}/take-with-volunteer`, data),

  // 完成救助
  completeRescue: (id, config = {}) => api.post(`/rescues/${id}/complete`, null, config),

  // 完成救助（带救援日志图片）
  completeRescueWithImages: (id, formData, config = {}) => {
    const headers = {
      'Content-Type': 'multipart/form-data',
      ...config.headers
    }
    return api.post(`/rescues/${id}/complete-with-images`, formData, {
      ...config,
      headers
    })
  },

  // 获取我的救助任务
  getMyRescues: (params) => api.get('/rescues/my', { params }),

  // 获取救助统计信息
  getRescueStatistics: (params = {}) => {
    return api.get('/rescues/statistics', { params });
  },

  // 导出救助统计数据
  exportStatistics: (params = {}) => {
    return api.get('/rescues/statistics', { params });
  }
}