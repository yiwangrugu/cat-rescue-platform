<template>
  <div class="my-favorites-container">
  <div class="page-content">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Star /></el-icon>
          我的收藏
        </h1>
        <p class="page-subtitle">查看和管理您收藏的猫咪和帖子</p>
      </div>
      <div class="header-stats">
        <div class="stat-card posts">
          <div class="stat-number">{{ favorites.length }}</div>
          <div class="stat-label">收藏帖子</div>
        </div>
        <div class="stat-card cats">
          <div class="stat-number">{{ favoriteCats.length }}</div>
          <div class="stat-label">收藏猫咪</div>
        </div>
      </div>
    </div>
  </div>

  <!-- 二级导航栏 -->
  <div class="secondary-nav">
      <div class="nav-tabs">
        <el-button 
          :type="activeTab === 'cats' ? 'primary' : 'default'"
          @click="activeTab = 'cats'"
          class="nav-tab"
        >
          <el-icon><Star /></el-icon>
          收藏猫咪 ({{ favoriteCats.length }})
        </el-button>
        <el-button 
          :type="activeTab === 'posts' ? 'primary' : 'default'"
          @click="activeTab = 'posts'"
          class="nav-tab"
        >
          <el-icon><Star /></el-icon>
          收藏帖子 ({{ favorites.length }})
        </el-button>
      </div>
  </div>

  <div class="favorites-content">
    <!-- 收藏的猫咪 -->
    <el-card class="favorites-card" v-show="activeTab === 'cats'">
      <template #header>
        <div class="card-header">
          <div>
            <span>收藏的猫咪</span>
          </div>
        </div>
      </template>

      <!-- 猫咪卡片列表 -->
      <div class="cats-grid">
        <div class="cats-grid-container">
          <div 
            class="cat-card" 
            v-for="cat in favoriteCats" 
            :key="cat.id"
            @mouseenter="handleCardHover(cat.id)"
            @mouseleave="handleCardLeave(cat.id)"
            @click="goToCatDetail(cat.id)"
          >
            <div class="card-inner" :class="{ flipped: flippedCards[cat.id] }">
              <!-- 卡片正面：猫咪照片 -->
              <div class="card-front">
                <div class="cat-image">
                  <img :src="getCatImage(cat)" :alt="cat.name" />
                  <div class="cat-status" :class="getStatusClass(cat.status)">
                    {{ getStatusText(cat.status) }}
                  </div>
                </div>
              </div>
              
              <!-- 卡片背面：小猫明信片 -->
              <div class="card-back">
                <div class="postcard-content" :style="getCatBackgroundStyle(cat.id)">
                  <div class="postcard-header">
                    <h3 class="postcard-title">小猫明信片</h3>
                    <div class="postcard-stamp">🐱</div>
                  </div>
                  
                  <div class="postcard-info">
                    <div class="info-item">
                      <span class="label">姓名：</span>
                      <span class="value">{{ cat.name }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">品种：</span>
                      <span class="value">{{ cat.breed || '未知品种' }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">年龄：</span>
                      <span class="value">{{ formatAge(cat.age) }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">性别：</span>
                      <span class="value">{{ getGenderText(cat.gender) }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">健康状况：</span>
                      <span class="value">{{ getHealthText(cat.healthStatus) }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">位置：</span>
                      <span class="value">{{ cat.location || '未知' }}</span>
                    </div>
                    <div class="info-item full-width">
                      <span class="label">性格特点：</span>
                      <span class="value">{{ cat.personality || '待发现' }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="favoriteCats.length === 0" class="empty-state">
        <el-icon size="48"><Star /></el-icon>
        <h3>暂无收藏猫咪</h3>
        <p>您还没有收藏任何猫咪</p>
        <div class="action-buttons">
          <el-button @click="$router.push('/cats')">
            去猫咪列表
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 收藏的帖子 -->
    <el-card class="favorites-card" v-show="activeTab === 'posts'">
      <template #header>
        <div class="card-header">
          <div>
            <span>收藏的帖子</span>
          </div>
        </div>
      </template>

      <div class="favorites-list" :class="{ 'has-posts': favorites.length > 0 }">
        <div v-if="favorites.length === 0" class="empty-state">
          <el-icon size="48"><Star /></el-icon>
          <h3>暂无收藏内容</h3>
          <p>您还没有收藏任何帖子</p>
          <div class="action-buttons">
            <el-button @click="$router.push('/community')">
              去社区论坛
            </el-button>
          </div>
        </div>
        
        <div v-else class="posts-list">
          <el-card v-for="post in favorites" :key="post.id" class="post-card" @click="viewPostDetail(post)">
              <div class="post-header">
                <div class="post-title-section">
                  <el-tag :type="getPostTypeTag(post.category)" size="small">{{ getPostTypeText(post.category) }}</el-tag>
                  <h3 class="post-title">{{ post.title }}</h3>
                </div>
                <div class="post-meta">
                  <div class="author-info">
                    <img 
                      v-if="post.authorAvatar" 
                      :src="post.authorAvatar" 
                      alt="作者头像" 
                      class="author-avatar"
                    >
                    <span v-else class="avatar-placeholder">{{ post.authorUsername?.charAt(0) || '?' }}</span>
                    <span class="author-name">{{ post.authorUsername || getUserName(post.authorId) }}</span>
                  </div>
                  <span class="time">{{ formatDate(post.createTime) }}</span>
                </div>
              </div>
              <hr class="divider">
              <div class="post-content">
                <p>{{ truncateContent(post.content, 150) }}</p>
              </div>
              
              <div class="post-stats">
                <span><el-icon><View /></el-icon> {{ post.viewCount || 0 }}</span>
                <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}</span>
                <span><DianzanIcon :active="false" :size="14" /> {{ post.likeCount || 0 }}</span>
                <span><ShoucangIcon :active="false" :size="14" /> {{ post.favoriteCount || 0 }}</span>
              </div>
              
              <div class="post-actions">
                <el-button size="small" @click.stop="viewPostDetail(post)">查看详情</el-button>
                <el-button 
                  size="small" 
                  :type="likeStore.hasLiked(post.id) ? 'text' : 'text'" 
                  @click.stop="handleLikePost(post)"
                >
                  <DianzanIcon :active="likeStore.hasLiked(post.id)" :size="14" />
                  点赞
                </el-button>
                <el-button 
                  size="small" 
                  :type="favoriteStore.hasFavorited(post.id) ? 'text' : 'text'" 
                  @click.stop="handleFavoritePost(post)"
                >
                  <ShoucangIcon :active="favoriteStore.hasFavorited(post.id)" :size="14" />
                  收藏
                </el-button>
                <el-button size="small"  @click.stop="handleComment(post)">
                  <el-icon><ChatDotRound /></el-icon> 评论
                </el-button>
              </div>
            </el-card>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { View, ChatDotRound, ArrowLeft, Star } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useLikeStore } from '@/stores/like'
import { useFavoriteStore } from '@/stores/favorite'
import { communityApi } from '@/api/community.js'
import { catFavoriteApi } from '@/api/catFavorite'
import { ElMessage, ElMessageBox } from 'element-plus'
import DianzanIcon from '@/components/DianzanIcon.vue'
import ShoucangIcon from '@/components/ShoucangIcon.vue'

const router = useRouter()
const authStore = useAuthStore()
const favoriteStore = useFavoriteStore()
const likeStore = useLikeStore()

const favorites = ref([])
const favoriteCats = ref([])
const activeTab = ref('cats') // 默认显示收藏猫咪

// 5种清新柔和的渐变色背景
const backgroundGradients = [
  // 1. 清新薄荷绿
  'linear-gradient(135deg, #e8f5e8 0%, #f0f8ff 70%, #fff0f5 100%)',
  // 2. 温柔天空蓝
  'linear-gradient(75deg, #e3f2fd 0%, #f3e5f5 30%, #e8f5e8 100%)',
  // 3. 柔和樱花粉
  'linear-gradient(65deg, #fce4ec 0%, #f3e5f5 50%, #e3f2fd 100%)',
  // 4. 淡雅薰衣草
  'linear-gradient(140deg, #f3e5f5 0%, #e8f5e8 60%, #e0f2f1 100%)',
  // 5. 温暖阳光黄
  'linear-gradient(115deg, #fff9c4 0%, #f3e5f5 20%, #e8f5e8 100%)'
]

// 翻转卡片状态管理
const flippedCards = ref({})

console.log('组件初始化，默认数据:')
console.log('收藏帖子:', favorites.value)
console.log('收藏猫咪:', favoriteCats.value)

onMounted(async () => {
  console.log('组件挂载，开始加载数据...')
  await loadFavorites()
  await loadFavoriteCats()
  console.log('收藏数据加载完成')
  console.log('最终数据状态:')
  console.log('收藏帖子:', favorites.value)
  console.log('收藏猫咪:', favoriteCats.value)
})

const loadFavorites = async () => {
  try {
    if (!authStore.isAuthenticated || !authStore.user?.id) {
      // 未认证状态下显示空数据
      favorites.value = []
      console.log('未认证状态，显示空数据')
      return
    }
    
    console.log('开始加载收藏列表，用户ID:', authStore.user.id)
    const response = await communityApi.getUserFavorites(authStore.user.id)
    console.log('API响应:', response)
    favorites.value = response.data || []
    console.log('收藏的帖子加载完成，共', favorites.value.length, '个帖子')
    console.log('收藏的帖子数据:', favorites.value)
  } catch (error) {
    console.error('加载收藏帖子失败:', error)
    console.error('错误详情:', error.response || error.message || error)
    ElMessage.error('加载收藏帖子失败')
    // 错误状态下显示空数据
    favorites.value = []
  }
}

const loadFavoriteCats = async () => {
  try {
    if (!authStore.isAuthenticated || !authStore.user?.id) {
      // 未认证状态下显示空数据
      favoriteCats.value = []
      console.log('未认证状态，显示空猫咪数据')
      return
    }
    
    console.log('开始加载收藏猫咪列表，用户ID:', authStore.user.id)
    const response = await catFavoriteApi.getUserFavoriteCats(authStore.user.id)
    console.log('猫咪API响应:', response)
    favoriteCats.value = response.data || []
    console.log('收藏的猫咪加载完成，共', favoriteCats.value.length, '只猫咪')
    console.log('收藏的猫咪数据:', favoriteCats.value)
  } catch (error) {
    console.error('加载收藏猫咪失败:', error)
    console.error('错误详情:', error.response || error.message || error)
    ElMessage.error('加载收藏猫咪失败')
    // 错误状态下显示空数据
    favoriteCats.value = []
  }
}

const viewPostDetail = (post) => {
  router.push(`/posts/${post.id}`)
}

const handleLikePost = async (post) => {
  if (!authStore.isAuthenticated) {
    ElMessageBox.confirm('请先登录才能点赞，是否跳转到登录页面？', '需要登录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push('/login')
    }).catch(() => {
      // 用户取消，不做任何操作
    })
    return
  }
  
  try {
    const postId = post.id
    
    if (likeStore.hasLiked(postId)) {
      // 取消点赞
      await communityApi.unlikePost(postId)
      post.likeCount = Math.max(0, (post.likeCount || 1) - 1)
      likeStore.removeLike(postId)
      ElMessage.success('取消点赞成功')
    } else {
      // 点赞
      await communityApi.likePost(postId)
      post.likeCount = (post.likeCount || 0) + 1
      likeStore.addLike(postId)
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

const handleFavoritePost = async (post) => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    if (favoriteStore.hasFavorited(post.id)) {
      // 取消收藏，显示确认弹窗
      await ElMessageBox.confirm(
        '确定要取消收藏这篇帖子吗？',
        '取消收藏',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      await communityApi.unfavoritePost(post.id)
      favoriteStore.removeFavorite(post.id)
      post.favoriteCount = Math.max(0, (post.favoriteCount || 0) - 1)
      ElMessage.success('取消收藏成功')
      
      // 从收藏列表中移除该帖子
      const index = favorites.value.findIndex(item => item.id === post.id)
      console.log('取消收藏，查找帖子索引:', index, '帖子ID:', post.id)
      if (index > -1) {
        favorites.value.splice(index, 1)
        console.log('成功从收藏列表中移除帖子')
      } else {
        console.log('未找到对应帖子，重新加载收藏列表')
        await loadFavorites()
      }
    } else {
      await communityApi.favoritePost(post.id)
      favoriteStore.addFavorite(post.id)
      post.favoriteCount = (post.favoriteCount || 0) + 1
      ElMessage.success('收藏成功')
      
      // 收藏成功后重新加载收藏列表
      await loadFavorites()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

const handleComment = (post) => {
  // 处理评论逻辑
  router.push(`/posts/${post.id}#comments`)
}

// 猫咪相关函数
const goToCatDetail = (catId) => {
  router.push(`/cats/${catId}`)
}

const viewCatDetail = (cat) => {
  router.push(`/cats/${cat.id}`)
}

// 为每只猫咪生成随机背景色索引
const getRandomBackgroundIndex = (catId) => {
  // 使用猫咪ID作为种子，确保同一只猫咪每次显示相同的颜色
  return catId % backgroundGradients.length
}

// 获取猫咪的背景样式
const getCatBackgroundStyle = (catId) => {
  const index = getRandomBackgroundIndex(catId)
  return {
    background: backgroundGradients[index]
  }
}

// 卡片悬停处理
const handleCardHover = (catId) => {
  flippedCards.value[catId] = true
}

// 卡片离开处理
const handleCardLeave = (catId) => {
  flippedCards.value[catId] = false
}

const handleUnfavoriteCat = async (cat) => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要取消收藏猫咪"${cat.name}"吗？`,
      '取消收藏',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await catFavoriteApi.unfavoriteCat(cat.id)
    ElMessage.success('取消收藏成功')
    
    // 从收藏列表中移除该猫咪
    const index = favoriteCats.value.findIndex(item => item.id === cat.id)
    if (index > -1) {
      favoriteCats.value.splice(index, 1)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消收藏猫咪失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

const getCatImage = (cat) => {
  if (cat.images) {
    try {
      let images = cat.images
      if (typeof images === 'string') {
        images = JSON.parse(images)
      }
      if (Array.isArray(images) && images.length > 0) {
        const image = images[0]
        if (typeof image === 'string') {
          if (image.startsWith('http')) {
            return image
          }
          if (image.startsWith('/uploads/')) {
            return image.startsWith('http') ? image : image
          }
          if (image.startsWith('../')) {
            const cleanPath = image.replace('../', '')
            const finalPath = cleanPath.replace('public/', '')
            return finalPath.startsWith('/img/') ? finalPath : '/img/' + finalPath
          }
          if (image.startsWith('/img/')) {
            return image
          }
          if (image.startsWith('/')) {
            if (!image.startsWith('/uploads') && !image.startsWith('/img')) {
              return `/img${image}`
            }
            return image
          } else {
            return `/img/${image}`
          }
        }
        if (typeof image === 'object' && image.url) {
          return image.url
        }
      }
    } catch (error) {
      console.error('图片解析错误:', error)
    }
  }
  return '/img/placeholder-cat.jpg'
}

const getStatusText = (status) => {
  const texts = {
    '待领养': '待领养',
    '已领养': '已领养',
    '照顾中': '照顾中',
    '已故': '已故'
  }
  return texts[status] || status || '未知'
}

const getStatusClass = (status) => {
  const classes = {
    '待领养': 'status-waiting',
    '已领养': 'status-adopted',
    '照顾中': 'status-caring',
    '已故': 'status-deceased'
  }
  return classes[status] || 'status-unknown'
}

const getGenderText = (gender) => {
  const texts = {
    'MALE': '公猫',
    'FEMALE': '母猫',
    'UNKNOWN': '未知'
  }
  return texts[gender] || gender
}

const getHealthText = (health) => {
  const texts = {
    'HEALTHY': '健康',
    'NEEDS_CARE': '需要照顾',
    'SICK': '生病',
    'DISABLED': '残疾',
    'RECOVERING': '康复中',
    'CRITICAL': '危重'
  }
  return texts[health] || health || '未知'
}

const formatAge = (ageInMonths) => {
  if (!ageInMonths) return '年龄未知'
  
  if (ageInMonths < 12) {
    return `${ageInMonths}个月`
  } else {
    const years = Math.floor(ageInMonths / 12)
    const months = ageInMonths % 12
    if (months === 0) {
      return `${years}岁`
    } else {
      return `${years}岁${months}个月`
    }
  }
}

const handleImageError = (event) => {
  const img = event.target
  if (img.src.includes('placeholder-cat.jpg')) {
    return
  }
  img.src = '/img/placeholder-cat.jpg'
  img.onerror = null
}

const getPostTypeTag = (category) => {
  // 统一转换为小写进行匹配
  const normalizedCategory = category?.toLowerCase() || ''
  const tagMap = {
    'adoption': 'success',
    'rescue': 'warning',
    'donation': 'info',
    'experience': 'success',
    'discussion': 'primary',
    'other': 'default',
    '求助': 'warning',
    '分享': 'success',
    '讨论': 'primary'
  }
  return tagMap[normalizedCategory] || 'default'
}

const getPostTypeText = (category) => {
  // 统一转换为小写进行匹配
  const normalizedCategory = category?.toLowerCase() || ''
  const textMap = {
    'adoption': '领养',
    'rescue': '救助',
    'donation': '捐赠',
    'experience': '经验分享',
    'discussion': '讨论',
    'other': '其他',
    '求助': '求助',
    '分享': '分享',
    '讨论': '讨论'
  }
  return textMap[normalizedCategory] || '其他'
}

const getUserName = (userId) => {
  // 根据userId获取用户名
  return '用户'
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const truncateContent = (content, maxLength) => {
  if (!content) return ''
  if (content.length <= maxLength) return content
  return content.substring(0, maxLength) + '...'
}
</script>

<style scoped>
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.my-favorites-container {
  max-width: 1550px;
  margin: 0 auto;
  padding: 20px;
}

/* 二级导航栏样式 */
.secondary-nav {
  margin-bottom: 20px;
}


.nav-tabs {
    display: flex;
    gap: 8px;
    background: #f5f7fa;
    border-radius: 8px;
    padding: 4px;
    width: fit-content;
}

.nav-tab {
  flex: 1;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.nav-tab:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.nav-tab .el-icon {
  margin-right: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-tabs {
    flex-direction: column;
  }
  
  .nav-tab {
    flex: none;
  }
}

.page-content {
  margin-bottom: 20px;
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
}

.header-content {
  color: white;
}

.page-title {
  font-size: 2em;
  font-weight: 600;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-subtitle {
  opacity: 0.9;
  margin: 0;
}

.header-stats {
  display: flex;
  gap: 16px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 16px 24px;
  text-align: center;
  color: white;
  min-width: 80px;
}

.stat-card.posts {
  background: rgba(107, 127, 244, 0.3);
}

.stat-card.cats {
  background: rgba(40, 167, 69, 0.3);
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  opacity: 0.9;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  animation: fadeInUp 0.6s ease !important;
}

.card-header .back-button {
  margin-right: 10px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  animation: fadeInUp 0.6s ease !important;
}

.empty-state h3 {
  margin: 20px 0 10px;
  color: #666;
}

/* 收藏列表容器 */
.favorites-list {
  animation: fadeInUp 0.6s ease !important;
}

.posts-list {
  animation: fadeInUp 0.6s ease !important;
}

/* el-card头部和主体动画 */
:deep(.el-card__header) {
  animation: fadeInUp 0.6s ease !important;
}

/* 帖子卡片样式 */
.post-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  animation: fadeInUp 0.6s ease !important;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.post-header {
  margin-bottom: 15px;
}

.post-title-section {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.post-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.post-meta {
  display: flex;
  justify-content: left;
  align-items: center;
  font-size: 16px;
  color: #999;
  gap:15px
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #999;
}

.author-name {
  font-size: 16px;
  color: #666;
}

.time {
  font-size: 14px;
  color: #999;
}

/* 猫咪卡片网格布局 */
.cats-grid {
  margin: 20px 0;
  animation: fadeInUp 0.6s ease !important;
}

.cats-grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  padding: 0;
}

.cat-card {
  height: 400px;
  perspective: 1000px;
  cursor: pointer;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.cat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.card-inner {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.6s;
  transform-style: preserve-3d;
}

.card-inner.flipped {
  transform: rotateY(180deg);
}

.card-front,
.card-back {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  border-radius: 16px;
  overflow: hidden;
}

.card-front {
  background: #fff;
}

.card-back {
  background: #fff;
  transform: rotateY(180deg);
}

.cat-image {
  position: relative;
  width: 100%;
  height: 100%;
}

.cat-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cat-status {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: white;
  backdrop-filter: blur(10px);
}

.status-waiting {
  background: rgba(255, 193, 7, 0.9);
}

.status-adopted {
  background: rgba(40, 167, 69, 0.9);
}

.status-caring {
  background: rgba(0, 123, 255, 0.9);
}

.status-deceased {
  background: rgba(108, 117, 125, 0.9);
}

.status-unknown {
  background: rgba(108, 117, 125, 0.9);
}

/* 明信片样式 */
.postcard-content {
  width: 100%;
  height: 100%;
  padding: 20px;
  display: flex;
  flex-direction: column;
  position: relative;
  box-sizing: border-box; /* 确保宽度计算包含padding */
}

.postcard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.postcard-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.postcard-stamp {
  font-size: 24px;
  opacity: 0.8;
}

.postcard-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item.full-width {
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.label {
  font-weight: 600;
  color: #666;
  min-width: 60px;
}

.value {
  color: #333;
  flex: 1;
}

.info-item.full-width .value {
  margin-top: 4px;
  line-height: 1.4;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cats-grid-container {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;
  }
  
  .cat-card {
    height: 350px;
  }
  
  .postcard-content {
    padding: 16px;
  }
}

.divider {
  margin: 15px 0;
  border: none;
  border-top: 2px solid #e0e0e0;
}

.post-content {
  margin-bottom: 15px;
}

.post-content p {
  margin: 0;
  font-size: 16px;
  line-height: 1.6;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.post-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
  font-size: 16px;
  color: #999;
}

.post-stats :deep(.el-icon) {
  color: #606266;
  font-size: 16px;
}

.post-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.post-actions :deep(.el-icon) {
  color: #606266;
  font-size: 16px;
}

.post-actions :deep(.el-button) {
  color: #606266;
  font-size: 16px;
}


@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .post-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .post-actions {
    flex-wrap: wrap;
  }
}
</style>