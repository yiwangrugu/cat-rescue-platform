import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 收藏状态管理
 * 用于全局管理用户的收藏状态，避免页面切换时状态丢失
 */
export const useFavoriteStore = defineStore('favorite', () => {
  // 存储用户收藏过的帖子ID集合
  const favoritedPosts = ref(new Set())

  /**
   * 检查用户是否已经收藏过某个帖子
   */
  const hasFavorited = (postId) => {
    return favoritedPosts.value.has(postId)
  }

  /**
   * 添加收藏记录
   */
  const addFavorite = (postId) => {
    favoritedPosts.value.add(postId)
    // 保存到本地存储，确保页面刷新后状态不丢失
    saveToLocalStorage()
  }

  /**
   * 移除收藏记录
   */
  const removeFavorite = (postId) => {
    favoritedPosts.value.delete(postId)
    saveToLocalStorage()
  }

  /**
   * 切换收藏状态
   */
  const toggleFavorite = (postId) => {
    if (hasFavorited(postId)) {
      removeFavorite(postId)
    } else {
      addFavorite(postId)
    }
  }

  /**
   * 获取收藏状态列表
   */
  const getFavoritedPosts = () => {
    return Array.from(favoritedPosts.value)
  }

  /**
   * 清空收藏记录
   */
  const clearFavorites = () => {
    favoritedPosts.value.clear()
    localStorage.removeItem('userFavoritedPosts')
  }

  /**
   * 保存到本地存储
   */
  const saveToLocalStorage = () => {
    const favoritedPostsArray = Array.from(favoritedPosts.value)
    localStorage.setItem('userFavoritedPosts', JSON.stringify(favoritedPostsArray))
  }

  /**
   * 从本地存储加载收藏状态
   */
  const loadFromLocalStorage = () => {
    const storedFavorites = localStorage.getItem('userFavoritedPosts')
    if (storedFavorites) {
      try {
        const favoritedPostsArray = JSON.parse(storedFavorites)
        favoritedPosts.value = new Set(favoritedPostsArray)
      } catch (error) {
        console.error('加载收藏状态失败:', error)
        favoritedPosts.value = new Set()
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
    favoritedPosts,
    hasFavorited,
    addFavorite,
    removeFavorite,
    toggleFavorite,
    getFavoritedPosts,
    clearFavorites,
    initialize
  }
})
