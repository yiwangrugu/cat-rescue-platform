import api from './index.js'

export const catFavoriteApi = {
  // 收藏猫咪
  favoriteCat: (catId) => api.post(`/cats/${catId}/favorite`),

  // 取消收藏猫咪
  unfavoriteCat: (catId) => api.post(`/cats/${catId}/unfavorite`),

  // 获取用户收藏的猫咪列表
  getUserFavoriteCats: (userId) => api.get(`/cats/users/${userId}/favorites`),

  // 检查猫咪收藏状态
  checkFavoriteStatus: (catId) => api.get(`/cats/${catId}/favorite/status`)
}