<template>
  <div class="user-posts-container">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Document /></el-icon>
          我的帖子
        </h1>
        <p class="page-subtitle">查看和管理您发布的帖子</p>
      </div>
      <div class="header-stats">
        <div class="stat-card total">
          <div class="stat-number">{{ totalPostCount }}</div>
          <div class="stat-label">总帖子数</div>
        </div>
        <div class="stat-card published">
          <div class="stat-number">{{ publishedCount }}</div>
          <div class="stat-label">已发布</div>
        </div>
        <div class="stat-card pending">
          <div class="stat-number">{{ pendingCount }}</div>
          <div class="stat-label">审核中</div>
        </div>
        <div class="stat-card rejected">
          <div class="stat-number">{{ rejectedCount }}</div>
          <div class="stat-label">未过审</div>
        </div>
        <div class="stat-card draft">
          <div class="stat-number">{{ draftCount }}</div>
          <div class="stat-label">草稿</div>
        </div>
      </div>
    </div>

    <!-- 二级导航栏 -->
    <div class="secondary-nav">
      <div class="nav-tabs">
        <el-button 
          :type="activeTab === 'published' ? 'primary' : 'default'" 
          @click="activeTab = 'published'"
          class="nav-tab"
        >
          <el-icon><Document /></el-icon>
          我的帖子
        </el-button>
        <el-button 
          :type="activeTab === 'drafts' ? 'primary' : 'default'" 
          @click="activeTab = 'drafts'"
          class="nav-tab"
        >
          <el-icon><EditPen /></el-icon>
          我的草稿
        </el-button>
      </div>
    </div>

    <div class="posts-content">
      <el-card class="posts-card">
        <template #header>
          <div class="card-header">
            <div>
              <span>{{ activeTab === 'published' ? '我的帖子' : '我的草稿' }}</span>
            </div>
            <el-button type="primary" @click="showPostForm = true">
              <el-icon><Plus /></el-icon>
              发布帖子
            </el-button>
          </div>
        </template>

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
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" @click="submitPost" :loading="submitting">提交审核</el-button>
          </template>
        </el-dialog>



        <!-- 帖子列表 -->
        <div class="my-posts-list" :class="{ 'has-posts': (activeTab === 'published' ? myPosts.length : myDrafts.length) > 0 }">
          <!-- 我的帖子 -->
          <div v-if="activeTab === 'published'">
            <div v-if="myPosts.length === 0" class="empty-state">
              <el-icon size="48"><ChatDotRound /></el-icon>
              <h3>暂无发布的帖子</h3>
              <p>您还没有发布任何帖子</p>
              <el-button type="primary" @click="$router.push('/community')">
                去社区逛逛
              </el-button>
            </div>
            
            <div v-else class="posts-list">
              <el-card v-for="post in myPosts" :key="post.id" class="post-card" @click="viewPostDetail(post)">
                <!-- 审核状态标签 -->
                <div class="status-badge">
                  <el-tag :type="getStatusType(post.status)" size="large" effect="light">
                    {{ getStatusText(post.status) }}
                  </el-tag>
                </div>
                
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
                
              
                <!-- 操作按钮区域 -->
                <div>
                <!-- 被拒绝帖子的操作按钮 -->
                <div v-if="post.status === 'REJECTED'">
                  <div class="footer2">
                 <!-- 拒绝理由显示 -->
                      <div v-if="post.status === 'REJECTED' && post.auditRemark" class="reject-reason">
                        <div class="reject-reason-text" :title="post.auditRemark">
                            <span class="reject-icon">⚠️</span>
                             拒绝理由: {{ truncateReason(post.auditRemark) }}
                        </div>
                      </div>      
                    <div>
                    <el-button size="small" @click.stop="viewPostDetail(post)"><div class="delete">查看详情</div></el-button>
                    <el-button size="small" type="primary" @click.stop="reEditPost(post)">
                      <el-icon style="color: white;"><EditPen /></el-icon>
                      重新编辑
                    </el-button>
                    <el-button size="small" type="danger" @click.stop="deletePost(post)">
                      <el-icon><Delete /></el-icon> <div class="delete">删除</div>
                    </el-button>
                  </div>
                </div>
              </div>
                  
                  <!-- 正常帖子的操作按钮 -->
                  <div v-else class="post-actions-right">
                    <div class="footer1">
                    <el-button size="small" @click.stop="viewPostDetail(post)"><div class="delete">查看详情</div></el-button>
                    <el-button 
                      size="small" 
                      link
                      @click.stop="handleLikePost(post)"
                    >
                      <DianzanIcon :active="likeStore.hasLiked(post.id)" :size="14" />
                      点赞
                    </el-button>
                    <el-button 
                      size="small" 
                      link
                      @click.stop="handleFavoritePost(post)"
                    >
                      <ShoucangIcon :active="favoriteStore.hasFavorited(post.id)" :size="14" />
                      收藏
                    </el-button>
                    <el-button size="small" link @click.stop="handleComment(post)">
                      <el-icon><ChatDotRound /></el-icon> 评论
                    </el-button>
                    <el-button size="small" type="danger" @click.stop="deletePost(post)">
                      <el-icon><Delete /></el-icon> <div class="delete">删除</div>
                    </el-button>
                  </div>
                  </div>
                </div>
              </el-card>
            </div>
          </div>
          
          <!-- 我的草稿 -->
          <div v-if="activeTab === 'drafts'">
            <div v-if="myDrafts.length === 0" class="empty-state">
              <el-icon size="48"><EditPen /></el-icon>
              <h3>暂无草稿</h3>
              <p>您还没有保存任何草稿</p>
              <el-button type="primary" @click="showPostForm = true">
                创建新帖子
              </el-button>
            </div>
            
            <div v-else class="posts-list">
              <el-card v-for="post in myDrafts" :key="post.id" class="post-card draft-card" @click="editDraft(post)">
                <div class="post-header">
                  <div class="post-title-section">
                    <el-tag type="warning" size="small">草稿</el-tag>
                    <el-tag :type="getPostTypeTag(post.category)" size="small">{{ getPostTypeText(post.category) }}</el-tag>
                    <h3 class="post-title">{{ post.title || '无标题草稿' }}</h3>
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
                  <p>{{ truncateContent(post.content, 150) || '暂无内容' }}</p>
                </div>
                
                <div class="post-actions">
                  <el-button size="small" type="primary" @click.stop="editDraft(post)">继续编辑</el-button>
                  <el-button size="small" @click.stop="publishDraft(post)">提交审核</el-button>
                  <el-button size="small" type="danger" @click.stop="deleteDraft(post)">删除草稿</el-button>
                </div>
              </el-card>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, View, ArrowLeft, Close, Plus, Document, EditPen, Warning } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useLikeStore } from '@/stores/like'
import { useFavoriteStore } from '@/stores/favorite'
import { communityApi } from '@/api/community'
import DianzanIcon from '@/components/DianzanIcon.vue'
import ShoucangIcon from '@/components/ShoucangIcon.vue'
import { authApi } from '@/api/auth'
import webSocketService from '@/utils/websocket'

const router = useRouter()
const authStore = useAuthStore()
const likeStore = useLikeStore()
const favoriteStore = useFavoriteStore()

const myPosts = ref([])
const myDrafts = ref([])
const showPostForm = ref(false)
const submitting = ref(false)
const draftCount = ref(0)
const activeTab = ref('published')
const editingPostId = ref(null) // 当前正在编辑的帖子ID，null表示新建帖子

// 统计变量
const totalPostCount = ref(0)
const publishedCount = ref(0)
const pendingCount = ref(0)
const rejectedCount = ref(0)

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

// 加载我的帖子
const loadMyPosts = async () => {
  console.log('开始加载我的帖子...')
  console.log('认证状态:', authStore.isAuthenticated)
  console.log('用户ID:', authStore.user?.id)
  
  try {
    if (!authStore.isAuthenticated || !authStore.user?.id) {
      // 未认证状态下显示空数据
      myPosts.value = []
      console.log('未认证状态，显示空数据')
      return
    }
    
    console.log('开始调用API获取我的帖子...')
    console.log('调用的API方法: communityApi.getUserPosts')
    console.log('传递的用户ID:', authStore.user.id)
    
    const response = await communityApi.getUserPosts(authStore.user.id)
    console.log('API调用成功，响应:', response)
    
    // 过滤掉草稿，只显示已发布、审核中、已拒绝的帖子
    const allPosts = response.data || []
    myPosts.value = allPosts.filter(post => post.status !== 'DRAFT')
    console.log('我的帖子加载完成，共', myPosts.value.length, '个帖子（已过滤草稿）')
    console.log('帖子数据:', myPosts.value)
    
    // 更新统计数据
    updateStatistics()
  } catch (error) {
    console.error('加载我的帖子失败:', error)
    console.error('错误详情:', error.response || error.message || error)
    ElMessage.error('加载我的帖子失败')
    // 错误状态下显示空数据
    myPosts.value = []
  }
  
  console.log('帖子加载完成，最终数据长度:', myPosts.value.length)
  console.log('最终数据内容:', myPosts.value)
}

// 查看帖子详情
const viewPostDetail = (post) => {
  if (post && post.id) {
    router.push(`/posts/${post.id}`)
  }
}

// 处理点赞
const handleLikePost = async (post) => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    if (likeStore.hasLiked(post.id)) {
      await communityApi.unlikePost(post.id)
      likeStore.removeLike(post.id)
      post.likeCount = Math.max(0, (post.likeCount || 0) - 1)
      ElMessage.success('取消点赞成功')
    } else {
      await communityApi.likePost(post.id)
      likeStore.addLike(post.id)
      post.likeCount = (post.likeCount || 0) + 1
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 处理收藏
const handleFavoritePost = async (post) => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    if (favoriteStore.hasFavorited(post.id)) {
      await communityApi.unfavoritePost(post.id)
      favoriteStore.removeFavorite(post.id)
      post.favoriteCount = Math.max(0, (post.favoriteCount || 0) - 1)
      ElMessage.success('取消收藏成功')
    } else {
      await communityApi.favoritePost(post.id)
      favoriteStore.addFavorite(post.id)
      post.favoriteCount = (post.favoriteCount || 0) + 1
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 处理评论
const handleComment = (post) => {
  if (post && post.id) {
    router.push(`/posts/${post.id}#comments`)
  }
}

// 获取帖子类型标签
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

// 获取帖子类型文本
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

// 格式化日期
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

// 截断内容
const truncateContent = (content, maxLength) => {
  if (!content) return ''
  if (content.length <= maxLength) return content
  return content.substring(0, maxLength) + '...'
}

// 截断拒绝理由
const truncateReason = (reason) => {
  if (!reason) return ''
  if (reason.length <= 15) return reason
  return reason.substring(0, 15) + '...'
}

// 获取用户名
const getUserName = (userId) => {
  return `用户${userId}`
}

// 获取审核状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PUBLISHED': '已发布',
    'PENDING_REVIEW': '审核中',
    'REJECTED': '已拒绝',
    'DRAFT': '草稿'
  }
  return statusMap[status] || '未知状态'
}

// 获取审核状态对应的标签类型
const getStatusType = (status) => {
  const types = {
    'PUBLISHED': 'success',
    'PENDING_REVIEW': 'warning',
    'REJECTED': 'danger',
    'DRAFT': 'info'
  }
  return types[status] || 'info'
}

// 获取审核状态标签样式类（保留兼容性）
const getStatusTagClass = (status) => {
  const classMap = {
    'PUBLISHED': 'status-published',
    'PENDING_REVIEW': 'status-pending',
    'REJECTED': 'status-rejected',
    'DRAFT': 'status-draft'
  }
  return classMap[status] || 'status-unknown'
}

// 提交帖子
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

  // 表单验证
  if (!postForm.value.title || !postForm.value.title.trim()) {
    ElMessage.error('请输入帖子标题')
    return
  }
  
  if (!postForm.value.category) {
    ElMessage.error('请选择帖子类型')
    return
  }
  
  if (!postForm.value.content || !postForm.value.content.trim()) {
    ElMessage.error('请输入帖子内容')
    return
  }

  try {
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
    
    const postData = {
      title: postForm.value.title,
      category: postForm.value.category,
      content: postForm.value.content,
      images: JSON.stringify(imageUrls),
      authorId: authStore.user.id,
      status: 'PENDING_REVIEW'
    }
    
    let newPost
    
    if (editingPostId.value) {
      // 如果是编辑现有草稿，更新帖子状态为待审核
      const response = await communityApi.updatePost(editingPostId.value, postData)
      newPost = response.data
      
      // 从草稿列表中移除
      myDrafts.value = myDrafts.value.filter(d => d.id !== editingPostId.value)
      
      // 添加到帖子列表
      myPosts.value = [newPost, ...myPosts.value]
    } else {
      // 如果是新建帖子，创建新帖子
      const response = await communityApi.createPost(postData)
      newPost = response.data
      
      // 添加到帖子列表
      myPosts.value = [newPost, ...myPosts.value]
    }
    
    // 更新统计数据
    updateStatistics()
    
    ElMessage.success('帖子已提交审核，请等待管理员审核通过')
    
    // 重置表单
    resetForm()
  } catch (error) {
    console.error('发布帖子失败:', error)
    ElMessage.error('发布帖子失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 加载草稿
const loadMyDrafts = async () => {
  try {
    if (!authStore.isAuthenticated || !authStore.user?.id) {
      myDrafts.value = []
      draftCount.value = 0
      return
    }
    
    // 调用API获取所有帖子，然后过滤出状态为DRAFT的帖子
    const response = await communityApi.getUserPosts(authStore.user.id)
    const allPosts = response.data || []
    
    // 过滤出草稿（状态为DRAFT的帖子）
    myDrafts.value = allPosts.filter(post => post.status === 'DRAFT')
    draftCount.value = myDrafts.value.length
    
    console.log('草稿加载完成，共', myDrafts.value.length, '个草稿')
    console.log('草稿数据:', myDrafts.value)
  } catch (error) {
    console.error('加载草稿失败:', error)
    myDrafts.value = []
    draftCount.value = 0
  }
}

// 编辑草稿
const editDraft = (draft) => {
  // 设置正在编辑的帖子ID
  editingPostId.value = draft.id
  
  // 将草稿数据填充到表单中
  postForm.value = {
    title: draft.title || '',
    category: draft.category || '',
    content: draft.content || '',
    images: draft.images ? JSON.parse(draft.images).map(url => ({ url })) : []
  }
  showPostForm.value = true
}

// 重新编辑被拒绝的帖子
const reEditPost = async (post) => {
  try {
    // 确认重新编辑
    const confirmResult = await ElMessageBox.confirm(
      '重新编辑后，需要重新提交审核。确定要继续吗？',
      '重新编辑确认',
      {
        confirmButtonText: '确定编辑',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (confirmResult) {
      // 创建一个新的草稿帖子，而不是修改原来的被拒绝帖子
      const newDraftData = {
        title: post.title || '',
        category: post.category || '',
        content: post.content || '',
        images: post.images || '',
        authorId: authStore.user.id,
        status: 'DRAFT'
      }
      
      const response = await communityApi.createPost(newDraftData)
      
      if (response.data) {
        ElMessage.success('已创建新的草稿，可以重新编辑')
        
        // 重新加载数据
        await loadMyPosts()
        await loadMyDrafts()
        
        // 打开编辑表单
        editingPostId.value = response.data.id
        postForm.value = {
          title: post.title || '',
          category: post.category || '',
          content: post.content || '',
          images: []
        }
        
        // 处理图片数据
        if (post.images) {
          try {
            const images = JSON.parse(post.images)
            if (Array.isArray(images)) {
              postForm.value.images = images.map((image, index) => ({
                name: `image-${index}`,
                url: image
              }))
            }
          } catch {
            // 解析失败，保持空数组
          }
        }
        
        showPostForm.value = true
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重新编辑帖子失败:', error)
      ElMessage.error('重新编辑失败，请稍后重试')
    }
  }
}

// 提交草稿审核
const publishDraft = async (draft) => {
  // 表单验证
  if (!draft.title || !draft.title.trim()) {
    ElMessage.error('草稿标题不能为空，请先编辑草稿填写标题')
    return
  }
  
  // 处理category字段，可能是数组或字符串
  const categoryValue = Array.isArray(draft.category) ? draft.category[0]?.fieldValue || '' : draft.category || ''
  
  if (!categoryValue) {
    ElMessage.error('草稿类型不能为空，请先编辑草稿选择类型')
    return
  }
  
  if (!draft.content || !draft.content.trim()) {
    ElMessage.error('草稿内容不完整，请先编辑草稿填写内容')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      '确定要提交这篇草稿进行审核吗？审核通过后将在社区中显示。',
      '确认提交审核',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 更新帖子状态为待审核
    const updatedPost = {
      ...draft,
      status: 'PENDING_REVIEW',
      category: categoryValue // 使用处理后的category值
    }
    
    // 调用API更新帖子状态
    await communityApi.updatePost(draft.id, updatedPost)
    
    // 从草稿列表中移除
    myDrafts.value = myDrafts.value.filter(d => d.id !== draft.id)
    
    // 重新加载数据，触发WebSocket通知
    await loadMyDrafts()
    
    // 注意：待审核的帖子不会立即显示在帖子列表中
    // 只有审核通过的帖子才会显示
    
    ElMessage.success('草稿已提交审核，请等待管理员审核通过')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交草稿审核失败:', error)
      ElMessage.error('提交审核失败，请重试')
    }
  }
}

// 删除帖子
const deletePost = async (post) => {
  try {
    const statusText = getStatusText(post.status)
    await ElMessageBox.confirm(
      `确定要删除这篇${statusText}的帖子吗？删除后将无法恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API删除帖子（后端改为标记为无效）
    await communityApi.deletePost(post.id)
    
    // 重新加载数据以反映最新的状态
    await loadMyPosts()
    
    // 更新统计数据
    updateStatistics()
    
    ElMessage.success('帖子删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除帖子失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

// 处理取消按钮
const handleCancel = async () => {
  // 检查表单是否有内容
  const hasContent = postForm.value.title || postForm.value.content || postForm.value.images.length > 0
  
  if (!hasContent) {
    // 如果没有内容，直接关闭表单
    resetForm()
    return
  }
  
  try {
    // 根据是否在编辑现有帖子来显示不同的提示信息
    const isEditing = editingPostId.value !== null
    const message = isEditing 
      ? '您有未保存的修改，是否保存为草稿？' 
      : '您有未保存的内容，是否保存为草稿？'
    
    // 询问是否保存草稿
    try {
      await ElMessageBox.confirm(
        message,
        '保存草稿',
        {
          confirmButtonText: '保存草稿',
          cancelButtonText: '不保存',
          type: 'warning',
          distinguishCancelAndClose: true
        }
      )
      
      // 用户选择保存草稿
      await saveDraft()
    } catch (error) {
      // 用户选择不保存，直接关闭表单
      resetForm()
      ElMessage.success('已取消编辑，内容未保存')
    }
  } catch (error) {
    if (error === 'cancel') {
      // 用户点击取消按钮，关闭对话框但不关闭表单
      return
    }
  }
}

// 重置表单
const resetForm = () => {
  showPostForm.value = false
  editingPostId.value = null
  postForm.value = {
    title: '',
    category: '',
    content: '',
    images: []
  }
}

// 保存草稿
const saveDraft = async () => {
  try {
    // 上传图片到服务器
    const imageUrls = []
    for (const file of postForm.value.images) {
      if (file.raw) {
        // 如果是新上传的图片，需要上传到服务器
        const response = await authApi.uploadFile(file.raw)
        imageUrls.push(response.data.fileUrl)
      } else if (file.url) {
        // 如果是已有的图片URL，直接使用
        imageUrls.push(file.url)
      }
    }
    
    // 准备草稿数据
    const draftData = {
      title: postForm.value.title || '',
      category: postForm.value.category || '',
      content: postForm.value.content || '',
      images: JSON.stringify(imageUrls),
      authorId: authStore.user.id,
      status: 'DRAFT'  // 确保状态为草稿
    }
    
    let savedDraft
    
    if (editingPostId.value) {
      // 如果是编辑现有帖子，更新帖子
      const response = await communityApi.updatePost(editingPostId.value, draftData)
      savedDraft = response.data
      
      // 更新草稿列表中的对应草稿
      const draftIndex = myDrafts.value.findIndex(d => d.id === editingPostId.value)
      if (draftIndex !== -1) {
        myDrafts.value[draftIndex] = savedDraft
      }
    } else {
      // 如果是新建帖子，创建新草稿
      const response = await communityApi.createPost(draftData)
      savedDraft = response.data
      
      // 添加到草稿列表
      myDrafts.value = [savedDraft, ...myDrafts.value]
    }
    
    // 更新草稿数量
    draftCount.value = myDrafts.value.length
    
    // 重置表单
    resetForm()
    
    ElMessage.success('草稿保存成功')
  } catch (error) {
    console.error('保存草稿失败:', error)
    ElMessage.error('保存草稿失败，请重试')
  }
}

// 删除草稿
const deleteDraft = async (draft) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这篇草稿吗？删除后将无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API删除草稿（后端改为标记为无效）
    await communityApi.deletePost(draft.id)
    
    // 重新加载草稿数据以反映最新的状态
    await loadMyDrafts()
    
    ElMessage.success('草稿删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除草稿失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

// 更新统计数据
const updateStatistics = () => {
  const published = myPosts.value.filter(post => post.status === 'PUBLISHED').length
  const pending = myPosts.value.filter(post => post.status === 'PENDING_REVIEW').length
  const rejected = myPosts.value.filter(post => post.status === 'REJECTED').length
  const total = myPosts.value.length
  
  publishedCount.value = published
  pendingCount.value = pending
  rejectedCount.value = rejected
  totalPostCount.value = total
}

// 监听activeTab变化
watch(activeTab, (newTab) => {
  if (newTab === 'drafts') {
    loadMyDrafts()
  }
})

// 组件挂载时加载数据
onMounted(async () => {
  console.log('MyPosts组件挂载，开始加载我的帖子和草稿...')
  loadMyPosts()
  loadMyDrafts() // 同时加载草稿数据
  console.log('初始帖子数据:', myPosts.value)
  console.log('初始草稿数据:', myDrafts.value)
  
  // 初始化WebSocket连接
  try {
    await webSocketService.connect()
    
    // 添加帖子审核实时更新处理器
    const postReviewHandlerId = webSocketService.addPostReviewHandler(async (data) => {
      console.log('收到帖子审核实时更新:', data)
      
      // 解析更新数据
      try {
        const updateData = JSON.parse(data)
        
        // 根据审核结果处理数据
        if (updateData.action === 'APPROVE' || updateData.action === 'REJECT') {
          // 重新加载帖子列表
          await loadMyPosts()
          
          // 显示审核结果通知
          if (updateData.action === 'APPROVE') {
            ElMessage.success('恭喜！您的帖子已通过审核')
          } else {
            ElMessage.error('您的帖子审核未通过，原因：' + (updateData.reviewComment || '无'))
          }
        }
      } catch (error) {
        console.error('解析WebSocket消息失败:', error)
      }
    })
    
    console.log('WebSocket帖子审核实时更新已启用')
    
    // 组件卸载时清理处理器
    onUnmounted(() => {
      webSocketService.removeMessageHandler(postReviewHandlerId)
    })
  } catch (error) {
    console.error('WebSocket连接失败:', error)
  }
})
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

.user-posts-container {
  max-width: 1550px;
  margin: 0 auto;
  padding: 20px;
}

.page-content {
  margin-bottom: 20px;
}

/* 页面头部样式 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.stat-card.total {
  background: rgba(102, 126, 234, 0.3);
}

.stat-card.published {
  background: rgba(40, 167, 69, 0.3);
}

.stat-card.pending {
  background: rgba(255, 193, 7, 0.3);
}

.stat-card.rejected {
  background: rgba(220, 53, 69, 0.3);
}

.stat-card.draft {
  background: rgba(241, 90, 44, 0.3);
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

/* 二级导航栏样式 */
.secondary-nav {
  margin-bottom: 24px;
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
  border-radius: 6px ;
  padding: 8px 16px ;
  font-weight: 500;
  transition: all 0.3s ease;
  border: none ;
}

.delete{
  font-size: 14px;
}

/* 审核状态标签样式 */
.status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

/* 拒绝理由样式 */
.reject-reason {
  max-width: 200px;
}

.reject-reason-text {
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  color: #d63031;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  cursor: help;
}

.reject-icon {
  margin-right: 4px;
  font-weight: bold;
}

.status-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  color: white;
  z-index: 10;
}

.status-published {
  background: linear-gradient(45deg, #28a745, #20c997);
  box-shadow: 0 2px 4px rgba(40, 167, 69, 0.3);
}

.status-pending {
  background: linear-gradient(45deg, #ffc107, #fd7e14);
  box-shadow: 0 2px 4px rgba(255, 193, 7, 0.3);
}

.status-rejected {
  background: linear-gradient(45deg, #dc3545, #e83e8c);
  box-shadow: 0 2px 4px rgba(220, 53, 69, 0.3);
}

.status-draft {
  background: linear-gradient(45deg, #6c757d, #adb5bd);
  box-shadow: 0 2px 4px rgba(108, 117, 125, 0.3);
}

.status-unknown {
  background: linear-gradient(45deg, #6c757d, #adb5bd);
  box-shadow: 0 2px 4px rgba(108, 117, 125, 0.3);
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-form-card {
  margin-bottom: 20px;
  border: 1px solid #e0e0e0;
  animation: fadeInUp 0.6s ease ;
}

.upload-tips {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 10px;
  animation: fadeInUp 0.6s ease ;
}


.card-header > div {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-right: 10px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  animation: fadeInUp 0.6s ease ;
}

.empty-state h3 {
  margin: 20px 0 10px;
  color: #666;
}

/* 帖子列表容器 */
.my-posts-list {
  animation: fadeInUp 0.6s ease ;
}

.posts-list {
  animation: fadeInUp 0.6s ease ;
}
.footer1{
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}
.footer2{
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}
/* el-card头部和主体动画 */
:deep(.el-card__header) {
  animation: fadeInUp 0.6s ease ;
}

:deep(.el-card__body) {
  animation: fadeInUp 0.6s ease ;
}

/* 帖子卡片样式 */
.post-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  animation: fadeInUp 0.6s ease ;
  position: relative;
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
  gap:15px;
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


.post-actions-right {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: auto;
  margin-left: auto;
}

.post-actions :deep(.el-icon),
.post-actions-right :deep(.el-icon) {
  color: #606266;
  font-size: 14px;
}

.post-actions :deep(.el-button),
.post-actions-right :deep(.el-button) {
  color: #606266;
  font-size: 14px;
}

.post-actions :deep(.el-button--danger),
.post-actions-right :deep(.el-button--danger) {
  color: white ;
}

.post-actions :deep(.el-button--primary),
.post-actions-right :deep(.el-button--primary) {
  color: white ;
}

.post-actions :deep(.el-button--danger .el-icon),
.post-actions-right :deep(.el-button--danger .el-icon) {
  color: white ;
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