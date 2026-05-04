import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 点赞状态管理
 * 用于全局管理用户的点赞状态，避免页面切换时状态丢失
 */
export const useLikeStore = defineStore('like', () => {
  // 存储用户点赞过的帖子ID集合
  const likedPosts = ref(new Set())

  /**
   * 检查用户是否已经点赞过某个帖子
   */
  const hasLiked = (postId) => {
    return likedPosts.value.has(postId)
  }

  /**
   * 添加点赞记录
   */
  const addLike = (postId) => {
    likedPosts.value.add(postId)
    // 保存到本地存储，确保页面刷新后状态不丢失
    saveToLocalStorage()
  }

  /**
   * 移除点赞记录
   */
  const removeLike = (postId) => {
    likedPosts.value.delete(postId)
    saveToLocalStorage()
  }

  /**
   * 切换点赞状态
   */
  const toggleLike = (postId) => {
    if (hasLiked(postId)) {
      removeLike(postId)
    } else {
      addLike(postId)
    }
  }

  /**
   * 获取点赞状态列表
   */
  const getLikedPosts = () => {
    return Array.from(likedPosts.value)
  }

  /**
   * 清空点赞记录
   */
  const clearLikes = () => {
    likedPosts.value.clear()
    localStorage.removeItem('userLikedPosts')
  }

  /**
   * 保存到本地存储
   */
  const saveToLocalStorage = () => {
    const likedPostsArray = Array.from(likedPosts.value)
    localStorage.setItem('userLikedPosts', JSON.stringify(likedPostsArray))
  }

  /**
   * 从本地存储加载点赞状态
   */
  const loadFromLocalStorage = () => {
    const storedLikes = localStorage.getItem('userLikedPosts')
    if (storedLikes) {
      try {
        const likedPostsArray = JSON.parse(storedLikes)
        likedPosts.value = new Set(likedPostsArray)
      } catch (error) {
        console.error('加载点赞状态失败:', error)
        likedPosts.value = new Set()
      }
    }
  }

  /**
   * 初始化状态管理
   */
  const initialize = () => {
    loadFromLocalStorage()
  }

  // 初始化
  initialize()

  return {
    likedPosts,
    hasLiked,
    addLike,
    removeLike,
    toggleLike,
    getLikedPosts,
    clearLikes,
    initialize
  }
})