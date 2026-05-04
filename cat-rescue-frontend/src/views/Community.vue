<template>
  <div class="community-container">
    <div class="community-header">
      <div class="header-content">
      <h1><el-icon><ChatDotSquare /></el-icon> 社区论坛</h1>
      <p>让每只猫都有机会找到自己的家</p>
      </div>
    </div>
    
    <!-- 搜索和筛选 -->
    <div class="community-filters">
      <el-row :gutter="20">
        <el-col :span="4">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索帖子标题或内容"
            prefix-icon="Search"
            clearable
            @change="loadPosts"
          />
        </el-col>
        <el-col :span="3">
          <el-select v-model="filterType" placeholder="帖子类型" clearable @change="loadPosts">
            <el-option label="求助" value="求助" />
            <el-option label="分享" value="分享" />
            <el-option label="讨论" value="讨论" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select v-model="sortBy" placeholder="排序方式" @change="loadPosts">
            <el-option label="最新发布" value="createdAt" />
            <el-option label="最多评论" value="commentCount" />
            <el-option label="最多点赞" value="likeCount" />
            <el-option label="最多浏览" value="viewCount" />
          </el-select>
        </el-col>
        <el-col :span="14">
          <el-button type="primary" @click="loadPosts">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetFilters">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button v-if="!isAdminApp" type="primary" @click="handleCreatePost" size="large">
            <el-icon><Plus /></el-icon>
            发布帖子
          </el-button>
        </el-col>
      </el-row>
    </div>

    <div class="community-content">
      <!-- 热门帖子 -->
      <div class="hot-posts-section" v-if="hotPosts.length > 0">
        <h3>🔥 热门帖子</h3>
        <hr class="divider">
        <div class="hot-posts">
          <el-card v-for="post in hotPosts" :key="post.id" class="hot-post-card" @click="viewPostDetail(post)">
            <div class="hot-post-content">
              <h4 class="hot-post-title">{{ post.title }}</h4>
              <div class="hot-post-stats">
                <span><el-icon :size="16"><View /></el-icon> {{ post.viewCount || 0 }}</span>
                <span><el-icon :size="16"><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}</span>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 帖子列表 -->
      <div class="posts-list">
        <el-card v-for="post in visiblePosts" :key="post.id" class="post-card" @click="viewPostDetail(post)">
          <!-- 未读回复通知小红点 -->
          <div v-if="postUnreadNotifications.get(post.id)" class="notification-badge">
            <div class="notification-icon">
              <el-icon><Bell /></el-icon>
            </div>
            <div class="notification-count">{{ postUnreadNotifications.get(post.id) }}</div>
            <div class="notification-text">新回复</div>
          </div>
          <div class="post-header">
            <div class="post-title-section">
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
              <el-tag :type="getPostTypeTag(post.category)" size="small">{{ getPostTypeText(post.category) }}</el-tag>
              <h3 class="post-title">{{ post.title }}</h3>
            </div>
            <div class="post-meta">
              <span class="time">{{ formatDate(post.createTime) }}</span>
            </div>
          </div>
          <hr class="divider">
          <div class="post-content">
            <p>{{ truncateContent(post.content, 150) }}</p>
          </div>
          
          <div class="post-stats">
            <span><el-icon :size="18"><View /></el-icon> {{ post.viewCount || 0 }}</span>
            <span><el-icon :size="18"><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}</span>
            <span><DianzanIcon :active="false" :size="18" /> {{ post.likeCount || 0 }}</span>
            <span><ShoucangIcon :active="false" :size="18" /> {{ post.favoriteCount || 0 }}</span>
          </div>
          
          <div class="post-actions">
            <el-button size="default" @click.stop="viewPostDetail(post)">查看详情</el-button>
            <el-button 
              size="default" 
              :link="!likeStore.hasLiked(post.id)"
              @click.stop="handleLikePost(post)"
            >
              <DianzanIcon :active="likeStore.hasLiked(post.id)" :size="18" />
              点赞
            </el-button>
            <el-button 
              size="default" 
              :link="!favoriteStore.hasFavorited(post.id)"
              @click.stop="handleFavoritePost(post)"
            >
              <ShoucangIcon :active="favoriteStore.hasFavorited(post.id)" :size="18" />
              收藏
            </el-button>
            <el-button size="default" link @click.stop="handleComment(post)">
              <el-icon :size="18"><ChatDotRound /></el-icon> 评论
            </el-button>
            <!-- 删除按钮：只对帖子作者和管理员显示 -->
            <el-button 
              v-if="authStore.isAuthenticated && (authStore.user?.id === post.authorId || authStore.isAdmin)"
              size="default" 
              type="danger" 
              @click.stop="deletePost(post)"
            >
              <el-icon :size="18"><Delete /></el-icon> 删除
            </el-button>
          </div>
        </el-card>

        <!-- 空状态 -->
        <div v-if="visiblePosts.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无帖子" />
        </div>

        <!-- 加载更多指示器 -->
        <div v-if="loading" class="loading-indicator">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-if="hasMore && !loading" class="load-more-indicator">
          <el-button link @click="loadMorePosts" class="load-more-btn">
            <el-icon><ArrowDown /></el-icon>
            加载更多帖子
          </el-button>
        </div>
        
        <div v-if="!hasMore && visiblePosts.length > 0" class="no-more-indicator">
          <el-divider>
            <el-text type="info">没有更多帖子了</el-text>
          </el-divider>
        </div>
      </div>
    </div>

    <!-- 提交帖子审核对话框 -->
    <el-dialog v-model="showPostForm" title="提交帖子审核" width="800px">
      <el-form :model="postForm" :rules="postRules" ref="postFormRef" label-width="80px">
        <el-form-item label="帖子类型" prop="category">
          <el-select v-model="postForm.category" placeholder="请选择帖子类型">
            <el-option label="求助" value="求助" />
            <el-option label="分享" value="分享" />
            <el-option label="讨论" value="讨论" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="postForm.title" placeholder="请输入帖子标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input 
            type="textarea" 
            v-model="postForm.content" 
            :rows="8" 
            placeholder="请输入帖子内容" 
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :file-list="postForm.images"
            :on-change="handleImageChange"
            :on-remove="handleImageRemove"
            :before-upload="beforeImageUpload"
            :limit="6"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tips">支持 JPG、PNG 格式，大小不超过 5MB，最多上传 6 张图片</div>
        </el-form-item>
      </el-form>
      <template #footer>
          <el-button @click="showPostForm = false">取消</el-button>
          <el-button type="primary" @click="submitPost" :loading="submitting">提交审核</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, View, ChatDotRound, ChatDotSquare, Bell, ArrowDown, Refresh } from '@element-plus/icons-vue'
import { communityApi } from '@/api/community.js'
import { authApi } from '@/api/auth.js'
import { useLikeStore } from '@/stores/like.js'
import { useAuthStore } from '@/stores/auth.js'
import { useFavoriteStore } from '@/stores/favorite.js'
import DianzanIcon from '@/components/DianzanIcon.vue'
import ShoucangIcon from '@/components/ShoucangIcon.vue'
import webSocketService from '@/utils/websocket'
import { userWebSocketService } from '@/utils/userWebSocket.js'

const router = useRouter()
const likeStore = useLikeStore()
const authStore = useAuthStore()
const favoriteStore = useFavoriteStore()

const isAdminApp = computed(() => import.meta.env.VITE_APP_TYPE === 'admin')

const posts = ref([])
const hotPosts = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showPostForm = ref(false)
const submitting = ref(false)
const postFormRef = ref(null)
const searchKeyword = ref('')
const filterType = ref('')
const sortBy = ref('createdAt')
// 帖子未读通知数量
const postUnreadNotifications = ref(new Map())
// 懒加载相关变量
const loading = ref(false)
const hasMore = ref(true)
const allPosts = ref([])
const visiblePosts = ref([])
const postsPerLoad = ref(5)
const scrollThreshold = ref(200)

const postForm = ref({
  title: '',
  category: '',
  content: '',
  images: []
})

const postRules = {
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度在 5 到 100 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择帖子类型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'blur' },
    { min: 10, max: 2000, message: '内容长度在 10 到 2000 个字符', trigger: 'blur' }
  ]
}

// 使用全局点赞状态管理
// const userLikedPosts = ref(new Map())

const truncateContent = (content, length) => {
  if (!content) return ''
  return content.length > length ? content.substring(0, length) + '...' : content
}

// 获取用户名（模拟数据）
const getUserName = (authorId) => {
  const userNames = {
    1: '猫奴小张',
    2: '爱心救助员',
    3: '猫咪爱好者',
    4: '流浪猫守护者',
    5: '宠物医生'
  }
  return userNames[authorId] || `用户${authorId}`
}

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getPostTypeTag = (category) => {
  const types = {
    '求助': 'danger',
    '分享': 'success',
    '讨论': 'primary',
    '其他': 'info'
  }
  return types[category] || 'info'
}

const getPostTypeText = (category) => {
  return category || '其他'
}

// 初始加载所有帖子数据
const loadAllPosts = async () => {
  try {
    // 检查组件是否已卸载
    if (!isComponentMounted) {
      return
    }
    
    loading.value = true
    console.log('开始加载所有帖子数据...')
    
    // 获取所有帖子数据（不分页）
    const response = await communityApi.getPosts({ size: 1000 })
    const data = response.data
    
    // 检查组件是否已卸载
    if (!isComponentMounted) {
      return
    }
    
    // 修复数据格式问题
    let postsData = data.records || []
    
    // 如果后端返回的是IPage对象但records为空，尝试直接使用data
    if (postsData.length === 0 && Array.isArray(data)) {
      postsData = data
    }
    
    // 应用筛选和排序
    allPosts.value = applyFiltersAndSort(postsData)
    
    // 检查组件是否已卸载
    if (!isComponentMounted) {
      return
    }
    
    // 立即显示初始的帖子
    initialLoadPosts()
    
    // 设置热门帖子（浏览量最多的3个）
    hotPosts.value = [...allPosts.value]
      .sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0))
      .slice(0, 3)
    
    console.log('所有帖子数据加载完成，共', allPosts.value.length, '个帖子')
    
    // 加载帖子未读通知数量
    await loadPostNotifications()
    
  } catch (error) {
    // 检查组件是否已卸载
    if (!isComponentMounted) {
      return
    }
    
    console.error('加载所有帖子数据失败:', error)
    ElMessage.error('加载帖子列表失败')
    
    // 如果API调用失败，使用模拟数据
    allPosts.value = applyFiltersAndSort(getMockPosts())
    
    // 检查组件是否已卸载
    if (!isComponentMounted) {
      return
    }
    
    initialLoadPosts()
    
    hotPosts.value = [...allPosts.value]
      .sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0))
      .slice(0, 3)
    
    // 加载帖子未读通知数量
    await loadPostNotifications()
  } finally {
    // 检查组件是否已卸载
    if (!isComponentMounted) {
      return
    }
    
    loading.value = false
  }
}

// 应用筛选和排序
const applyFiltersAndSort = (postsData) => {
  let filteredPosts = [...postsData]
  
  // 筛选帖子类型
  if (filterType.value) {
    filteredPosts = filteredPosts.filter(post => post.category === filterType.value)
  }
  
  // 筛选搜索关键词
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filteredPosts = filteredPosts.filter(post => 
      post.title.toLowerCase().includes(keyword) ||
      post.content.toLowerCase().includes(keyword)
    )
  }
  
  // 应用排序
  filteredPosts.sort((a, b) => {
    switch (sortBy.value) {
      case 'commentCount':
        return (b.commentCount || 0) - (a.commentCount || 0)
      case 'likeCount':
        return (b.likeCount || 0) - (a.likeCount || 0)
      case 'viewCount':
        return (b.viewCount || 0) - (a.viewCount || 0)
      case 'createdAt':
      default:
        return new Date(b.createTime) - new Date(a.createTime)
    }
  })
  
  return filteredPosts
}

// 懒加载更多帖子
const loadMorePosts = () => {
  if (loading.value || !hasMore.value) return
  
  const startIndex = visiblePosts.value.length
  const endIndex = startIndex + postsPerLoad.value
  
  if (startIndex >= allPosts.value.length) {
    hasMore.value = false
    return
  }
  
  const newPosts = allPosts.value.slice(startIndex, endIndex)
  visiblePosts.value = [...visiblePosts.value, ...newPosts]
  
  // 更新是否有更多数据
  hasMore.value = endIndex < allPosts.value.length
  
  console.log('懒加载完成，当前显示', visiblePosts.value.length, '个帖子')
}

// 初始加载帖子
const initialLoadPosts = () => {
  if (allPosts.value.length === 0) return
  
  // 立即显示初始的帖子
  const initialPosts = allPosts.value.slice(0, postsPerLoad.value)
  visiblePosts.value = [...initialPosts]
  
  // 更新是否有更多数据
  hasMore.value = allPosts.value.length > postsPerLoad.value
  
  console.log('初始加载完成，显示', visiblePosts.value.length, '个帖子')
}

// 滚动事件处理
const handleScroll = () => {
  if (loading.value || !hasMore.value) return
  
  const scrollElement = document.querySelector('.community-content')
  if (!scrollElement) return
  
  const { scrollTop, scrollHeight, clientHeight } = scrollElement
  const distanceToBottom = scrollHeight - scrollTop - clientHeight
  
  if (distanceToBottom < scrollThreshold.value) {
    loadMorePosts()
  }
}

// 重新加载帖子（筛选条件变化时）
const loadPosts = async () => {
  await loadAllPosts()
}

// 加载帖子未读通知数量
const loadPostNotifications = async () => {
  try {
    if (!authStore.isAuthenticated || !authStore.user?.id) return
    
    const userId = authStore.user.id
    const newNotificationsMap = new Map()
    
    // 为每个帖子获取未读通知数量
    for (const post of visiblePosts.value) {
      try {
        // 暂时注释掉不存在的API调用，后续可以添加通知功能
        // const response = await communityApi.getPostUnreadNotificationCount(post.id, userId)
        // const count = response.data || 0
        const count = 0 // 暂时设置为0，表示没有未读通知
        if (count > 0) {
          newNotificationsMap.set(post.id, count)
        }
      } catch (error) {
        console.error(`获取帖子 ${post.id} 通知数量失败:`, error)
      }
    }
    
    postUnreadNotifications.value = newNotificationsMap
  } catch (error) {
    console.error('加载帖子通知失败:', error)
  }
}

// 添加模拟数据作为备选方案
const getMockPosts = () => {
  return [
    {
      id: 1,
      title: '求助：发现一只受伤的橘猫需要救助',
      content: '在朝阳区XX小区发现一只右腿受伤的橘猫，需要紧急救助。猫咪看起来很虚弱，希望有救助人员能尽快前往。',
      authorId: 2,
      category: '求助',
      viewCount: 156,
      likeCount: 15,
      commentCount: 8,
      createTime: '2026-01-24 10:30:00'
    },
    {
      id: 2,
      title: '分享：我家猫咪的日常照片',
      content: '分享一些我家猫咪的可爱照片，希望大家喜欢！猫咪名字叫小花，是一只三花猫，性格非常温顺。',
      authorId: 1,
      category: '分享',
      viewCount: 289,
      likeCount: 45,
      commentCount: 12,
      createTime: '2026-01-24 09:15:00'
    },
    {
      id: 3,
      title: '讨论：如何正确喂养流浪猫',
      content: '想和大家讨论一下如何科学地喂养流浪猫，避免对它们造成伤害。有什么好的建议和经验可以分享吗？',
      authorId: 3,
      category: '讨论',
      viewCount: 167,
      likeCount: 23,
      commentCount: 18,
      createTime: '2026-01-24 08:45:00'
    }
  ]
}

const resetFilters = () => {
  searchKeyword.value = ''
  filterType.value = ''
  sortBy.value = 'createdAt'
  currentPage.value = 1
  loadPosts()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadPosts()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadPosts()
}

const viewPostDetail = async (post) => {
  console.log('查看帖子详情:', post.id)
  
  // 标记帖子通知为已读
  if (authStore.isAuthenticated && authStore.user?.id && postUnreadNotifications.value.get(post.id)) {
    try {
      const userId = authStore.user.id
      // 暂时注释掉不存在的API调用，后续可以添加通知功能
      // await communityApi.markPostNotificationsAsRead(post.id, userId)
      // 更新本地通知状态
      postUnreadNotifications.value.delete(post.id)
    } catch (error) {
      console.error('标记通知为已读失败:', error)
    }
  }
  
  // 跳转到帖子详情页面
  router.push(`/posts/${post.id}`)
}

const likePost = async (post) => {
  try {
    // 检查用户是否已经点过赞
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

const showCommentForm = (post) => {
  viewPostDetail(post)
}

const handleCreatePost = () => {
  if (!authStore.isAuthenticated) {
    ElMessageBox.confirm('请先登录才能发布帖子，是否跳转到登录页面？', '需要登录', {
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
  showPostForm.value = true
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
  await likePost(post)
}

// 收藏功能
const favoritePost = async (post) => {
  try {
    const postId = post.id
    
    if (favoriteStore.hasFavorited(postId)) {
      // 取消收藏
      await communityApi.unfavoritePost(postId)
      favoriteStore.removeFavorite(postId)
      post.favoriteCount = Math.max(0, (post.favoriteCount || 1) - 1)
      ElMessage.success('取消收藏成功')
    } else {
      // 收藏
      await communityApi.favoritePost(postId)
      favoriteStore.addFavorite(postId)
      post.favoriteCount = (post.favoriteCount || 0) + 1
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

const handleFavoritePost = async (post) => {
  if (!authStore.isAuthenticated) {
    ElMessageBox.confirm('请先登录才能收藏，是否跳转到登录页面？', '需要登录', {
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
  await favoritePost(post)
}

const handleComment = (post) => {
  if (!authStore.isAuthenticated) {
    ElMessageBox.confirm('请先登录才能评论，是否跳转到登录页面？', '需要登录', {
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
  viewPostDetail(post)
}

const beforeImageUpload = (file) => {
  const isJPGOrPNG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isJPGOrPNG) {
    ElMessage.error('图片只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleImageChange = (file, fileList) => {
  postForm.value.images = fileList
}

const handleImageRemove = (file, fileList) => {
  postForm.value.images = fileList
}

const submitPost = async () => {
  try {
    // 验证用户是否登录
    if (!authStore.isAuthenticated || !authStore.user?.id) {
      ElMessage.error('请先登录再提交帖子审核')
      return
    }
    
    // 验证表单
    if (!postForm.value.title.trim()) {
      ElMessage.error('请输入帖子标题')
      return
    }
    if (!postForm.value.category) {
      ElMessage.error('请选择帖子类型')
      return
    }
    if (!postForm.value.content.trim()) {
      ElMessage.error('请输入帖子内容')
      return
    }
    
    submitting.value = true
    
    // 上传图片到服务器
    const imageUrls = []
    for (const file of postForm.value.images) {
      if (file.raw) {
        const response = await authApi.uploadFile(file.raw)
        imageUrls.push(response.data.fileUrl)
      } else if (file.url) {
        imageUrls.push(file.url)
      }
    }
    
    // 准备发布数据，确保与后端实体类匹配
    const postData = {
      title: postForm.value.title,
      category: postForm.value.category,
      content: postForm.value.content,
      images: JSON.stringify(imageUrls),
      authorId: authStore.user.id,
      viewCount: 0,
      likeCount: 0,
      commentCount: 0,
      status: 'PUBLISHED'
    }
    
    // 调用后端API发布帖子
    const response = await communityApi.createPost(postData)
    const newPost = response.data
    
    // 添加到帖子列表（确保响应性）
    posts.value = [newPost, ...posts.value]
    
    // 更新总数量
    total.value += 1
    
    ElMessage.success('帖子发布成功')
    showPostForm.value = false
    
    // 重置表单
    postForm.value = {
      title: '',
      category: '',
      content: '',
      images: []
    }
    
    // 发送WebSocket通知
    webSocketService.sendMessage('POST_DATA_UPDATED')
    
    // 发送localStorage事件通知其他标签页
    localStorage.setItem('postDataUpdated', Date.now().toString())
    
    // 强制触发响应性更新
    await nextTick()
    
  } catch (error) {
    console.error('发布帖子失败:', error)
    ElMessage.error('发布失败，请重试')
  } finally {
    submitting.value = false
  }
}

const deletePost = async (post) => {
  try {
    // 确认删除
    await ElMessageBox.confirm(
      '确定要删除这篇帖子吗？此操作不可恢复。',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用后端API删除帖子
    await communityApi.deletePost(post.id)
    
    // 更新前端状态，从帖子列表中移除
    const index = posts.value.findIndex(p => p.id === post.id)
    if (index > -1) {
      posts.value.splice(index, 1)
      total.value--
    }
    
    // 从热门帖子中移除
    const hotIndex = hotPosts.value.findIndex(p => p.id === post.id)
    if (hotIndex > -1) {
      hotPosts.value.splice(hotIndex, 1)
    }
    
    ElMessage.success('帖子删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除帖子失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

// WebSocket消息处理器ID
let wsHandlerId = null

onMounted(async () => {
  // 页面加载时初始化数据
  loadPosts()
  
  // 连接社区数据WebSocket并设置社区数据更新监听
        try {
          await webSocketService.connect()
          
          // 添加消息处理器
          wsHandlerId = webSocketService.addMessageHandler((message) => {
            // 检查组件是否已卸载
            if (!isComponentMounted) {
              return
            }
            
            if (message === 'POST_DATA_UPDATED' || message === 'COMMENT_DATA_UPDATED') {
              loadPosts()
            }
          })
        } catch (error) {
        }
  
  // 连接用户状态WebSocket并设置用户状态监听
        try {
          const authStore = useAuthStore()
          userWebSocketService.setToken(authStore.token)
          await userWebSocketService.connect()
        } catch (error) {
        }
  
  // 添加滚动事件监听
  const scrollElement = document.querySelector('.community-content')
  if (scrollElement) {
    scrollElement.addEventListener('scroll', handleScroll)
  }
})

// 组件卸载标志
let isComponentMounted = true

// 组件卸载时清理所有资源
onUnmounted(() => {
  isComponentMounted = false
  
  // 清理WebSocket连接
  if (wsHandlerId) {
    webSocketService.removeMessageHandler(wsHandlerId)
    wsHandlerId = null
  }
  
  // 清理用户状态WebSocket处理器
  userWebSocketService.removeMessageHandler('community')
  
  // 移除滚动事件监听
  const scrollElement = document.querySelector('.community-content')
  if (scrollElement) {
    scrollElement.removeEventListener('scroll', handleScroll)
  }
})
</script>

<style scoped>
/* 上移动画 */
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

.community-container {
  max-width: 1550px;
  margin: 0 auto;
  padding: 20px;
}

.community-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 94px;
  margin-bottom: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
}

.community-header h1 {
  color: #fff;
  font-size: 2em;
  font-weight: 600;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.community-header h1 .el-icon {
  font-size: 1em;
}

.community-header p {
  color: #fff;
  opacity: 0.9;
  margin: 0;
}



.community-filters {
  margin-bottom: 30px;
}

.hot-posts-section {
  margin-bottom: 30px;
}

.hot-posts-section h3 {
  color: #333;
  margin-bottom: 15px;
}

.hot-posts-section {
  margin-bottom: 30px;
  padding: 10px;
  border-radius: 12px;
  border: 1px solid #e0e0e0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  animation: fadeInUp 0.4s;
}

.hot-posts-section h3 {
  margin: 0 0 15px 0;
  color: #ff6b6b;
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.divider {
  margin: 10px 0;
  border: none;
  border-top: 2px solid #e0e0e0;
}



.hot-posts {
  display: flex;
  gap: 15px;
  overflow: hidden;
}

.hot-post-card {
  min-width: 192px;
  cursor: pointer;
  transition: all 0.5s ease;
  border-radius: 8px;
  border: solid 2px #ff6b6b;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1;
  animation: fadeInUp  0.4s, breathe 2s ease-in-out infinite !important;
}

@keyframes breathe {
  0%, 100% {
    transform: scale(0.97);
  }
  50% {
    transform: scale(1.0);
  }
}


.hot-post-content {
  padding: 10px;
}

.hot-post-content h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 17px;
  line-height: 1.4;
  font-weight: 600;
}

.hot-post-title {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  word-break: break-all;
}

.hot-post-stats {
  display: flex;
  gap: 10px;
  font-size: 14px;
  color: #ff6b6b;
  font-weight: 500;
}

.hot-post-card:hover {
  transform: scale(0.95);
  box-shadow: 0 6px 16px rgba(222, 143, 40, 0.3);
  animation: none;
}

.hot-post-card:hover .hot-post-content h4 {
  color: #ff0000;
}

.hot-post-card:hover .hot-post-stats {
  color: #ff0000;
}

.posts-list {
  margin-bottom: 30px;
  animation: fadeInUp 0.4s;
}

.post-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.post-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

/* 通知小红点 */
.notification-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: linear-gradient(135deg, #ff6b6b, #ff8787);
  color: white;
  border-radius: 20px;
  padding: 8px 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: bold;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.4);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 2px 8px rgba(255, 107, 107, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 4px 12px rgba(255, 107, 107, 0.6);
  }
}

.notification-icon {
  font-size: 14px;
}

.notification-count {
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
}

.notification-text {
  font-size: 12px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.post-title-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.post-title {
  margin: 0;
  color: #333;
  font-size: 18px;
  cursor: pointer;
}

.post-meta {
  display: flex;
  gap: 15px;
  font-size: 14px;
  color: #666;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #1890ff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.author-name {
  font-weight: 500;
}

.post-content {
  margin-bottom: 15px;
}

.post-content p {
  margin: 0;
  color: #666;
  line-height: 1.6;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.post-stats {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
  font-size: 14px;
  color: #666;
}

.post-stats :deep(.el-icon) {
  color: #606266;
}

.post-actions {
  display: flex;
  gap: 10px;
}

.post-actions .el-button {
  /* 防止按钮点击事件冒泡到卡片 */
  pointer-events: auto;
}

.post-actions :deep(.el-icon) {
  color: #606266;
}

.post-actions :deep(.el-button) {
  color: #606266;
}

.post-actions :deep(.el-button--danger) {
  color: #fff;
}

.post-actions :deep(.el-button--danger .el-icon) {
  color: #fff;
}

.empty-state {
  text-align: center;
  padding: 50px 0;
}

.pagination-section {
  display: flex;
  justify-content: center;
}

.upload-tips {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

/* 懒加载相关样式 */
.loading-indicator {
  margin: 20px 0;
  padding: 20px;
  text-align: center;
}

.load-more-indicator {
  margin: 20px 0;
  text-align: center;
}

.load-more-btn {
  color: #1890ff;
  font-size: 14px;
  font-weight: 500;
  padding: 12px 24px;
  border: 1px solid #1890ff;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.load-more-btn:hover {
  background-color: #1890ff;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.no-more-indicator {
  margin: 30px 0;
}

/* 确保社区内容区域可滚动 */
.community-content {
  max-height: calc(100vh - 200px);
  overflow-y: auto;
  padding-right: 8px;
}

/* 滚动条样式 */
.community-content::-webkit-scrollbar {
  width: 6px;
}

.community-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.community-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.community-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

@media (max-width: 768px) {
  .community-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .post-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .hot-posts {
    flex-direction: column;
  }
  
  .hot-post-card {
    min-width: auto;
  }
  
  .load-more-btn {
    padding: 10px 20px;
    font-size: 13px;
  }
}
</style>