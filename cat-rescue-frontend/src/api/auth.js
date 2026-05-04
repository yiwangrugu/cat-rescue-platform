import api from './index.js'

export const authApi = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
  uploadAvatar: (file, userId) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('userId', userId)
    return api.post('/auth/upload-avatar', formData)
  },
  uploadFile: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/auth/upload-file', formData)
  },
  updateProfile: (userData) => api.put('/auth/update-profile', userData)
}