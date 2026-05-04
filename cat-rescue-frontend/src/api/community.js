import api from './index.js'

export const communityApi = {
  // 获取帖子列表
  getPosts: (params) => api.get('/community/posts', { params }),

  // 获取帖子详情
  getPostDetail: (id) => api.get(`/community/posts/${id}`),

  // 创建帖子
  createPost: (data) => api.post('/community/posts', data),

  // 更新帖子
  updatePost: (id, data) => api.put(`/community/posts/${id}`, data),

  // 删除帖子
  deletePost: (id) => api.delete(`/community/posts/${id}`),

  // 点赞帖子
  likePost: (id) => api.post(`/community/posts/${id}/like`),

  // 取消点赞
  unlikePost: (id) => api.post(`/community/posts/${id}/unlike`),

  // 获取帖子评论
  getPostComments: (postId, params) => api.get(`/community/posts/${postId}/comments`, { params }),

  // 添加评论
  addComment: (postId, data) => api.post(`/community/posts/${postId}/comments`, data),

  // 删除评论
  deleteComment: (postId, commentId) => api.delete(`/community/posts/${postId}/comments/${commentId}`),

  // 获取用户未读通知数量
  getUnreadNotificationCount: (userId) => api.get('/community/notifications/unread/count', { params: { userId } }),

  // 获取帖子未读通知数量
  getPostUnreadNotificationCount: (postId, userId) => api.get(`/community/posts/${postId}/notifications/unread/count`, { params: { userId } }),

  // 标记帖子通知为已读
  markPostNotificationsAsRead: (postId, userId) => api.put(`/community/posts/${postId}/notifications/read`, null, { params: { userId } }),

  // 收藏帖子
  favoritePost: (id) => api.post(`/community/posts/${id}/favorite`),

  // 取消收藏
  unfavoritePost: (id) => api.post(`/community/posts/${id}/unfavorite`),

  // 获取用户收藏的帖子
  getUserFavorites: (userId) => api.get(`/community/users/${userId}/favorites`),

  // 获取用户自己发布的帖子
  getUserPosts: (userId) => api.get(`/community/posts/author/${userId}`)
}