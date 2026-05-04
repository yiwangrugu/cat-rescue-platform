<template>
  <div class="post-detail-container">
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button @click="goBack" link>
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>
    
    <!-- 帖子详情 -->
    <div class="post-detail" v-if="post">
      <el-card class="post-card">
        <!-- 帖子头部 -->
        <div class="post-header">
          <div class="post-meta">
            <h1 class="post-title">{{ post.title }}</h1>
            <div class="post-info">
              <div class="author-info">
                <img 
                  v-if="post.authorAvatar" 
                  :src="post.authorAvatar" 
                  alt="作者头像" 
                  class="author-avatar"
                >
                <span v-else class="avatar-placeholder">{{ post.authorUsername?.charAt(0) || '?' }}</span>
                <span class="author-name">{{ post.authorUsername || getUserName(post.authorId) }}</span>
               <el-tag :type="getPostTypeTag(post.category)">{{ post.category }}</el-tag>
              </div>
              <span class="post-time">{{ formatTime(post.updateTime || post.createTime) }}</span>
            </div>
          </div>
          <div class="post-stats">
            <span><el-icon><View /></el-icon> {{ post.viewCount || 0 }}</span>
            <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}</span>
            <span><DianzanIcon :active="false" :size="14" /> {{ post.likeCount || 0 }}</span>
            <span><ShoucangIcon :active="false" :size="14" /> {{ post.favoriteCount || 0 }}</span>
          </div>
        </div>
        <!-- 横线 -->
        <hr class="divider">

        <!-- 帖子内容 -->
        <div class="post-content">
          <div class="content-text">{{ post.content }}</div>
          
          <!-- 帖子图片 -->
          <div class="post-images" v-if="postImages.length > 0">
            <el-image
              v-for="(image, index) in postImages"
              :key="index"
              :src="image"
              :preview-src-list="postImages"
              :initial-index="index"
              fit="cover"
              class="post-image"
            />
          </div>
        </div>

        <!-- 帖子操作 -->
        <div class="post-actions">
          <!-- 被拒绝帖子的操作按钮 -->
          <div v-if="post.status === 'REJECTED'">
            <el-button 
              link
              v-if="authStore.isAuthenticated && authStore.user?.id === post.authorId"
              @click="handleReEditPost(post)"
            >
              <el-icon><EditPen /></el-icon>
              重新编辑
            </el-button>
            
            <el-button 
              link
              v-if="authStore.isAuthenticated && authStore.user?.id === post.authorId"
              @click="handleDeletePost(post)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
          
          <!-- 正常帖子的操作按钮 -->
          <div v-else>
            <el-button 
              link
              @click="handleLikePost(post)"
            >
              <DianzanIcon :active="likeStore.hasLiked(post.id)" :size="14" />
              点赞
            </el-button>
            
            <el-button 
              link
              @click="handleFavoritePost(post)"
            >
              <ShoucangIcon :active="favoriteStore.hasFavorited(post.id)" :size="14" />
              收藏
            </el-button>
            
            <el-button link @click="scrollToNewComment">
              <el-icon><ChatDotRound /></el-icon>
              评论
            </el-button>
            
            <el-button 
              link
              v-if="authStore.isAuthenticated && authStore.user?.id === post.authorId"
              @click="handleEditPost(post)"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 新评论输入框 -->
      <div class="new-comment-section" v-if="showCommentForm">
        <el-card>
          <el-form :model="commentForm" :rules="commentRules" ref="commentFormRef">
            <el-form-item prop="content">
              <el-input
                type="textarea"
                v-model="commentForm.content"
                :rows="3"
                placeholder="发表新评论"
                maxlength="500"
                show-word-limit
                ref="commentInput"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitComment" :loading="submittingComment">
                发表评论
              </el-button>
              <el-button @click="showCommentForm = false">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <!-- 评论列表 -->
      <div class="comments-section">
        <h3>评论 ({{ comments.length }})</h3>
        
        <div class="comments-list" v-if="nestedComments.length > 0">
          <div v-for="comment in nestedComments" :key="comment.id" class="comment-item">
            <el-card @click="handleReply(comment)" class="comment-card">
              <div class="comment-header">
                <div class="comment-author-info">
                  <img 
                    v-if="comment.authorAvatar" 
                    :src="comment.authorAvatar" 
                    alt="评论者头像" 
                    class="author-avatar"
                  >
                  <span v-else class="avatar-placeholder">{{ comment.authorUsername?.charAt(0) || '?' }}</span>
                  <div class="author-details">
                    <div class="author-name-section">
                      <span class="comment-author">{{ comment.authorUsername || '用户' + comment.authorId }}</span>
                      <el-tag v-if="comment.isPostAuthor" type="warning" size="small" class="post-author-tag">帖主</el-tag>
                      <el-tag v-if="isNewComment(comment)" type="danger" size="small" class="new-comment-tag">新</el-tag>
                    </div>
                  </div>
                </div>
                <div class="comment-actions">
                  <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                  <el-button 
                    size="default" 
                    type="primary" 
                    link
                    @click.stop="handleReply(comment)"
                  >
                    <el-icon :size="16"><ChatDotRound /></el-icon> 回复
                  </el-button>
                  <el-button 
                    v-if="authStore.isAuthenticated && (authStore.user?.id === comment.authorId || authStore.user?.id === post.authorId || authStore.isAdmin)"
                    size="default" 
                    type="danger" 
                    link
                    @click.stop="deleteComment(comment)"
                  >
                    <el-icon :size="16"><Delete /></el-icon> 删除
                  </el-button>
                </div>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
              
              <!-- 根评论的回复输入框 -->
              <div v-if="replyingTo && replyingTo.id === comment.id" class="reply-input-section">
                <el-form :model="commentForm" :rules="commentRules" ref="commentFormRef">
                  <el-form-item prop="content">
                    <el-input
                      type="textarea"
                      v-model="commentForm.content"
                      :rows="2"
                      :placeholder="`回复 @${replyingTo.authorUsername}`"
                      maxlength="500"
                      show-word-limit
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" size="default" @click="submitComment" :loading="submittingComment">
                      回复
                    </el-button>
                    <el-button size="default" @click.stop="cancelReply">取消</el-button>
                  </el-form-item>
                </el-form>
              </div>
              
              <!-- 回复列表 -->
              <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
                <div v-for="reply in (isCommentExpanded(comment.id) || hasNewReplies(comment) ? comment.replies : comment.replies.slice(0, 2))" :key="reply.id" class="reply-item">
                  <el-card @click.stop="handleReply(reply)" class="comment-card">
                    <div class="comment-header">
                      <div class="comment-author-info">
                        <img 
                          v-if="reply.authorAvatar" 
                          :src="reply.authorAvatar" 
                          alt="评论者头像" 
                          class="author-avatar"
                        >
                        <span v-else class="avatar-placeholder">{{ reply.authorUsername?.charAt(0) || '?' }}</span>
                        <div class="author-details">
                          <div class="author-name-section">
                            <span class="comment-author">{{ reply.authorUsername || '用户' + reply.authorId }}</span>
                            <el-tag v-if="reply.isPostAuthor" type="warning" size="small" class="post-author-tag">帖主</el-tag>
                            <el-tag v-if="isNewComment(reply)" type="danger" size="small" class="new-comment-tag">新</el-tag>
                          </div>
                          <div v-if="reply.repliedToUsername" class="reply-to">
                            回复 <span class="replied-to-user">{{ reply.repliedToUsername }}</span>
                          </div>
                        </div>
                      </div>
                      <div class="comment-actions">
                        <span class="comment-time">{{ formatTime(reply.createTime) }}</span>
                        <el-button 
                    size="default" 
                    type="primary" 
                    link
                    @click.stop="handleReply(reply)"
                  >
                    <el-icon :size="16"><ChatDotRound /></el-icon> 回复
                  </el-button>
                  <el-button 
                    v-if="authStore.isAuthenticated && (authStore.user?.id === reply.authorId || authStore.user?.id === post.authorId || authStore.isAdmin)"
                    size="default" 
                    type="danger" 
                    link
                    @click.stop="deleteComment(reply)"
                  >
                    <el-icon :size="16"><Delete /></el-icon> 删除
                  </el-button>
                      </div>
                    </div>
                    <div class="comment-content">{{ reply.content }}</div>
                    
                    <!-- 回复输入框 -->
                    <div v-if="replyingTo && replyingTo.id === reply.id" class="reply-input-section">
                      <el-form :model="commentForm" :rules="commentRules" ref="commentFormRef">
                        <el-form-item prop="content">
                          <el-input
                            type="textarea"
                            v-model="commentForm.content"
                            :rows="2"
                            :placeholder="`回复 @${replyingTo.authorUsername}`"
                            maxlength="500"
                            show-word-limit
                          />
                        </el-form-item>
                        <el-form-item>
                          <el-button type="primary" size="small" @click="submitComment" :loading="submittingComment">
                            回复
                          </el-button>
                          <el-button size="small" @click.stop="cancelReply">取消</el-button>
                        </el-form-item>
                      </el-form>
                    </div>
                  </el-card>
                </div>
                
                <!-- 展开/收起按钮 -->
                <div v-if="comment.replies.length > 2" class="expand-collapse-btn">
                  <el-button 
                    size="small" 
                    type="primary" 
                    link
                    @click.stop="toggleCommentExpansion(comment.id)"
                  >
                    {{ isCommentExpanded(comment.id) ? '收起回复' : hasNewReplies(comment) ? `展开全部 ${comment.replies.length} 条回复（含新回复）` : `展开全部 ${comment.replies.length} 条回复` }}
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>
        </div>
        
        <div class="empty-comments" v-else>
          <el-empty description="暂无评论" />
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 错误状态 -->
    <div v-else class="error-container">
      <el-empty description="帖子不存在或已被删除" />
    </div>
  </div>

  <!-- 编辑帖子对话框 -->
  <el-dialog v-model="showEditDialog" title="编辑帖子" width="800px">
    <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="editForm.title" placeholder="请输入帖子标题" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="editForm.category" placeholder="请选择帖子分类">
          <el-option label="求助" value="求助" />
          <el-option label="分享" value="分享" />
          <el-option label="讨论" value="讨论" />
          <el-option label="其他" value="其他" />
        </el-select>
      </el-form-item>
      <el-form-item label="内容" prop="content">
        <el-input
          type="textarea"
          v-model="editForm.content"
          :rows="6"
          placeholder="请输入帖子内容"
          maxlength="5000"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="图片">
        <el-upload
          class="post-image-upload"
          action="#"
          :auto-upload="false"
          :show-file-list="true"
          :on-change="handleImageChange"
          :file-list="editForm.images"
          list-type="picture-card"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="submittingEdit">保存修改</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, View, ChatDotRound, Delete, Top, Edit, Plus } from '@element-plus/icons-vue'
import { communityApi } from '@/api/community.js'
import { useLikeStore } from '@/stores/like.js'
import { useAuthStore } from '@/stores/auth.js'
import { useFavoriteStore } from '@/stores/favorite.js'
import { authApi } from '@/api/auth.js'
import DianzanIcon from '@/components/DianzanIcon.vue'
import ShoucangIcon from '@/components/ShoucangIcon.vue'
import webSocketService from '@/utils/websocket'

// 获取帖子分类对应的标签类型
const getPostTypeTag = (category) => {
  const types = {
    '求助': 'danger',
    '分享': 'success',
    '讨论': 'primary',
    '其他': 'info'
  }
  return types[category] || 'info'
}

const route = useRoute()
const router = useRouter()
const likeStore = useLikeStore()
const authStore = useAuthStore()
const favoriteStore = useFavoriteStore()

// 数据
const post = ref(null)
const comments = ref([])
const loading = ref(true)
const submittingComment = ref(false)
const replyingTo = ref(null)
const showCommentForm = ref(false)
const expandedComments = ref(new Map())
const lastViewTime = ref(null)
const readCommentIds = ref(new Set())
// 使用全局点赞状态管理
// const userLikedPosts = ref(new Map())

// 评论表单
const commentForm = ref({
  content: ''
})

const commentFormRef = ref(null)
const commentInput = ref(null)
const commentRules = {
  content: [
    { required: true, message: '请输入评论内容', trigger: 'blur' },
    { min: 1, max: 500, message: '评论内容长度在 1 到 500 个字符', trigger: 'blur' }
  ]
}

// 编辑帖子表单
const showEditDialog = ref(false)
const editForm = ref({
  title: '',
  content: '',
  category: '',
  images: []
})

const editFormRef = ref(null)
const submittingEdit = ref(false)
const editRules = {
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'blur' },
    { min: 1, max: 5000, message: '内容长度在 1 到 5000 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择帖子分类', trigger: 'blur' }
  ]
}

// 计算属性：解析帖子图片
const postImages = computed(() => {
  if (!post.value || !post.value.images) return []
  
  try {
    const images = JSON.parse(post.value.images)
    return Array.isArray(images) ? images : []
  } catch {
    return []
  }
})

// 计算属性：将评论组织成两层嵌套结构
const nestedComments = computed(() => {
  if (!comments.value || comments.value.length === 0) return []
  
  const rootComments = []
  
  // 首先找到所有根评论（没有parentId的评论）
  comments.value.forEach(comment => {
    if (!comment.parentId) {
      rootComments.push({ ...comment, replies: [] })
    }
  })
  
  // 根评论按创建时间降序排列（最新的在上面）
  rootComments.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  
  // 然后将所有回复添加到对应的根评论下面
  comments.value.forEach(comment => {
    if (comment.parentId) {
      // 找到这条回复所属的根评论
      let rootComment = rootComments.find(rc => rc.id === comment.parentId)
      
      // 如果直接回复的不是根评论，继续向上查找
      if (!rootComment) {
        let currentComment = comment
        while (currentComment.parentId) {
          const parentComment = comments.value.find(c => c.id === currentComment.parentId)
          if (!parentComment) break
          
          if (!parentComment.parentId) {
            rootComment = rootComments.find(rc => rc.id === parentComment.id)
            break
          }
          currentComment = parentComment
        }
      }
      
      // 如果找到根评论，将回复添加到它的replies数组中
      if (rootComment) {
        rootComment.replies.push({ ...comment, replies: [] })
      }
    }
  })
  
  // 每个根评论的回复按创建时间正序排列（最早的在上面）
  rootComments.forEach(rootComment => {
    rootComment.replies.sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
  })
  
  return rootComments
})

// 加载帖子详情
const loadPostDetail = async () => {
  try {
    loading.value = true
    const postId = route.params.id
    
    if (!postId) {
      ElMessage.error('帖子ID不存在')
      return
    }
    
    // 获取上次查看时间
    const viewKey = `post_view_${postId}_${authStore.user?.id || 'guest'}`
    lastViewTime.value = localStorage.getItem(viewKey)
    
    // 更新查看时间
    localStorage.setItem(viewKey, new Date().toISOString())
    
    const response = await communityApi.getPostDetail(postId)
    post.value = response.data
    
    // 加载评论
    await loadComments(postId)
    
  } catch (error) {
    console.error('加载帖子详情失败:', error)
    ElMessage.error('加载帖子详情失败')
  } finally {
    loading.value = false
  }
}

// 加载评论
const loadComments = async (postId) => {
  try {
    console.log('加载评论，postId:', postId, '类型:', typeof postId)
    const response = await communityApi.getPostComments(postId)
    comments.value = response.data || []
    console.log('评论加载成功，数量:', comments.value.length)
  } catch (error) {
    console.error('加载评论失败:', error)
    console.error('错误详情:', error.response?.data)
    comments.value = []
  }
}

// 点赞功能
const likePost = async (post) => {
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

const handleReply = async (comment) => {
  if (!authStore.isAuthenticated) {
    ElMessageBox.confirm('请先登录才能回复，是否跳转到登录页面？', '需要登录', {
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
  
  // 标记评论为已读
  markCommentAsRead(comment)
  
  // 如果是回复，找到对应的根评论并保持展开状态
  if (comment.parentId) {
    // 在嵌套评论中查找根评论
    const rootComment = nestedComments.value.find(c => c.id === comment.parentId || c.replies?.some(r => r.id === comment.parentId))
    if (rootComment) {
      expandedComments.value.set(rootComment.id, true)
    }
  }
  
  // 创建一个深拷贝的评论对象，避免响应式更新导致的问题
  replyingTo.value = JSON.parse(JSON.stringify(comment))
  
  // 等待DOM更新后聚焦到回复输入框
  await nextTick()
  
  // 找到对应的回复输入框并聚焦
  const replyInputSections = document.querySelectorAll('.reply-input-section')
  replyInputSections.forEach(section => {
    const textarea = section.querySelector('textarea')
    if (textarea) {
      textarea.focus()
    }
  })
}

const cancelReply = () => {
  replyingTo.value = null
}

const toggleCommentExpansion = (commentId) => {
  if (expandedComments.value.has(commentId)) {
    expandedComments.value.delete(commentId)
  } else {
    expandedComments.value.set(commentId, true)
  }
}

const isCommentExpanded = (commentId) => {
  return expandedComments.value.has(commentId)
}

const isNewComment = (comment) => {
  if (!lastViewTime.value || !comment.createTime) return false
  const commentTime = new Date(comment.createTime)
  const viewTime = new Date(lastViewTime.value)
  // 检查是否是新评论：评论时间晚于上次查看时间，且不是用户自己的评论
  const isNew = commentTime > viewTime && 
               comment.authorId !== authStore.user?.id &&
               !readCommentIds.value.has(comment.id)
  
  // 检查是否是给当前用户的回复，或者是当前用户帖子的新评论
  const isForCurrentUser = comment.repliedToAuthorId === authStore.user?.id || 
                           (post.value && post.value.authorId === authStore.user?.id && !comment.parentId)
  
  return isNew && isForCurrentUser
}

const hasNewReplies = (comment) => {
  if (!comment.replies || comment.replies.length === 0) return false
  return comment.replies.some(reply => isNewComment(reply))
}

const markCommentAsRead = (comment) => {
  if (isNewComment(comment)) {
    readCommentIds.value.add(comment.id)
  }
}

// 提交评论
const submitComment = async () => {
  try {
    // 验证用户是否登录
    if (!authStore.isAuthenticated || !authStore.user?.id) {
      ElMessage.error('请先登录再发表评论')
      return
    }
    
    if (!commentForm.value.content.trim()) {
      ElMessage.error('请输入评论内容')
      return
    }
    
    submittingComment.value = true
    
    const commentData = {
      content: commentForm.value.content,
      authorId: authStore.user.id.toString(),
      postId: post.value.id.toString(),
      likeCount: 0,
      parentId: replyingTo.value ? replyingTo.value.id.toString() : null
    }
    
    await communityApi.addComment(post.value.id, commentData)
    
    ElMessage.success(replyingTo.value ? '回复成功' : '评论发表成功')
    commentForm.value.content = ''
    replyingTo.value = null
    
    // 重新加载评论
    await loadComments(post.value.id)
    
    // 更新帖子评论数
    post.value.commentCount = (post.value.commentCount || 0) + 1
    
    // 发送WebSocket通知
    webSocketService.send('COMMENT_DATA_UPDATED')
    
    // 发送localStorage事件通知其他标签页
    localStorage.setItem('commentDataUpdated', Date.now().toString())
    
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error('发表评论失败，请重试')
  } finally {
    submittingComment.value = false
  }
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

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return '未知时间'
  
  try {
    const date = new Date(timeString)
    return date.toLocaleString('zh-CN')
  } catch {
    return timeString
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 删除评论
const deleteComment = async (comment) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？此操作不可恢复。', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用后端API删除评论
    await communityApi.deleteComment(post.value.id, comment.id)
    
    // 更新前端状态
    const index = comments.value.findIndex(c => c.id === comment.id)
    if (index > -1) {
      comments.value.splice(index, 1)
      post.value.commentCount = Math.max(0, (post.value.commentCount || 1) - 1)
    }
    
    ElMessage.success('评论删除成功')
    
    // 发送WebSocket通知
    webSocketService.send('COMMENT_DATA_UPDATED')
    
    // 发送localStorage事件通知其他标签页
    localStorage.setItem('commentDataUpdated', Date.now().toString())
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}



const scrollToNewComment = async () => {
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
  
  showCommentForm.value = true
  
  // 等待DOM更新后聚焦到输入框
  await nextTick()
  
  const newCommentSection = document.querySelector('.new-comment-section')
  if (newCommentSection) {
    newCommentSection.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
  
  // 聚焦到评论输入框
  if (commentInput.value) {
    commentInput.value.focus()
  }
}

// 处理编辑帖子
const handleEditPost = (postData) => {
  // 填充编辑表单数据
  editForm.value = {
    title: postData.title,
    content: postData.content,
    category: postData.category,
    images: []
  }
  
  // 处理图片数据
  if (postData.images) {
    try {
      const images = JSON.parse(postData.images)
      if (Array.isArray(images)) {
        editForm.value.images = images.map((image, index) => ({
          name: `image-${index}`,
          url: image
        }))
      }
    } catch {
      // 解析失败，保持空数组
    }
  }
  
  // 打开编辑对话框
  showEditDialog.value = true
}

// 处理图片上传和删除
const handleImageChange = (file, fileList) => {
  // 只保留图片文件
  const validFiles = fileList.filter(item => item.url || item.raw)
  editForm.value.images = validFiles
}

// 处理重新编辑被拒绝的帖子
const handleReEditPost = async (postData) => {
  try {
    // 确认重新编辑
    const confirmResult = await ElMessageBox.confirm(
      '重新编辑后，帖子将变为草稿状态，需要重新提交审核。确定要继续吗？',
      '重新编辑确认',
      {
        confirmButtonText: '确定编辑',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (confirmResult) {
      // 将帖子状态重置为草稿
      const response = await communityApi.updatePost(postData.id, {
        ...postData,
        status: 'DRAFT'
      })
      
      if (response.data) {
        ElMessage.success('帖子已转为草稿，可以重新编辑')
        
        // 重新加载帖子详情
        await loadPostDetail()
        
        // 打开编辑表单
        editForm.value = {
          title: postData.title,
          content: postData.content,
          category: postData.category,
          images: []
        }
        
        // 处理图片数据
        if (postData.images) {
          try {
            const images = JSON.parse(postData.images)
            if (Array.isArray(images)) {
              editForm.value.images = images.map((image, index) => ({
                name: `image-${index}`,
                url: image
              }))
            }
          } catch {
            // 解析失败，保持空数组
          }
        }
        
        showEditDialog.value = true
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重新编辑帖子失败:', error)
      ElMessage.error('重新编辑失败，请稍后重试')
    }
  }
}

// 处理删除帖子
const handleDeletePost = async (postData) => {
  try {
    const confirmResult = await ElMessageBox.confirm(
      '确定要删除这个帖子吗？删除后将无法恢复。',
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (confirmResult) {
      const response = await communityApi.deletePost(postData.id)
      
      if (response.data) {
        ElMessage.success('帖子删除成功')
        
        // 返回上一页
        goBack()
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除帖子失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

// 提交编辑表单
const submitEdit = async () => {
  if (!editFormRef.value) return
  
  // 表单验证
  await editFormRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    
    try {
      submittingEdit.value = true
      
      // 准备图片数据
      const images = []
      for (const file of editForm.value.images) {
        if (file.raw) {
          // 上传新图片到服务器
          const response = await authApi.uploadFile(file.raw)
          images.push(response.data.fileUrl)
        } else if (file.url) {
          // 使用已有的图片URL
          images.push(file.url)
        }
      }
      
      // 调用API更新帖子
      const response = await communityApi.updatePost(post.value.id, {
        title: editForm.value.title,
        content: editForm.value.content,
        category: editForm.value.category,
        images: JSON.stringify(images),
        authorId: post.value.authorId
      })
      
      // 更新本地帖子数据，只更新被编辑的字段，保持其他字段不变
      if (response.data) {
        post.value = {
          ...post.value,
          ...response.data
        }
      }
      
      ElMessage.success('帖子编辑成功')
      showEditDialog.value = false
    } catch (error) {
      console.error('编辑帖子失败:', error)
      ElMessage.error('编辑失败，请重试')
    } finally {
      submittingEdit.value = false
    }
  })
}

// 组件卸载标志
let isComponentMounted = true

// WebSocket消息处理器ID
let wsHandlerId = null

onMounted(async () => {
  loadPostDetail()
  
  // 连接WebSocket并设置评论数据更新监听
  try {
    await webSocketService.connect()
    
    // 添加消息处理器
    wsHandlerId = webSocketService.addMessageHandler((message) => {
      // 检查组件是否已卸载
      if (!isComponentMounted) {
        return
      }
      
      console.log('收到WebSocket评论数据更新通知:', message)
      
      if (message === 'COMMENT_DATA_UPDATED') {
        console.log('重新加载评论数据')
        // 只有在post.value.id存在时才重新加载评论
        if (post.value && post.value.id) {
          loadComments(post.value.id)
        } else {
          console.warn('无法重新加载评论：post.value.id不存在')
        }
      }
    })
    
    console.log('帖子详情WebSocket监听已设置')
  } catch (error) {
    console.error('帖子详情WebSocket连接失败:', error)
    console.log('帖子详情WebSocket连接失败，将使用手动刷新方式更新数据')
    
    // 不再使用轮询，只在用户手动操作时更新数据
    // 这样可以大大减少系统资源消耗
  }
})

// 组件卸载时清理所有资源
onUnmounted(() => {
  isComponentMounted = false
  
  // 清理WebSocket连接
  if (wsHandlerId) {
    webSocketService.removeMessageHandler(wsHandlerId)
    wsHandlerId = null
  }
  webSocketService.disconnect()
})
</script>

<style scoped>
.post-detail-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.back-button {
  margin-bottom: 20px;
}

.post-card {
  margin-bottom: 20px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.post-title {
  margin: 0 0 8px 0;
  font-size: 28px;
  color: #333;
  font-weight: 700;
  line-height: 1.3;
}

.post-info {
  display: flex;
  gap: 12px;
  font-size: 14px;
  color: #666;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #1890ff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
}

.author-name {
  font-size: 16px;
  color: #333;
}

.post-stats {
  display: flex;
  gap: 15px;
  color: #666;
  font-size: 14px;
}

.post-stats :deep(.el-icon) {
  color: #606266;
}

.divider {
  margin: 16px 0;
  border: none;
  border-top: 2px solid #e0e0e0;
}



.post-content {
  margin-bottom: 25px;
}

.content-text {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 25px;
  white-space: pre-wrap;
}

.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.post-image {
  width: 200px;
  height: 150px;
  border-radius: 8px;
}

.post-actions {
  display: flex;
  gap: 10px;
}

.post-actions :deep(.el-icon) {
  color: #606266;
}

.post-actions :deep(.el-button) {
  color: #606266;
}

.comment-input-section {
  margin-bottom: 20px;
}

.comments-section h3 {
  margin-bottom: 15px;
  color: #333;
}

.comment-item {
  margin-bottom: 15px;
}

.comment-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.comment-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
  font-size: 16px;
  color: #666;
}

.comment-author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.post-author-tag {
  margin-left: 8px;
}

.new-comment-tag {
  margin-left: 8px;
  background: linear-gradient(135deg, #ff6b6b, #ff8787);
  border: none;
  color: white;
  font-weight: bold;
  padding: 4px 8px;
  animation: newCommentPulse 2s ease-in-out infinite;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.4);
}

@keyframes newCommentPulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 2px 8px rgba(255, 107, 107, 0.4);
  }
  50% {
    transform: scale(1.15);
    box-shadow: 0 4px 12px rgba(255, 107, 107, 0.6);
  }
}

.new-comment-card {
  background: linear-gradient(135deg, #fff5f5, #ffe4e1) !important;
  border: 2px solid #ff6b6b !important;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.2) !important;
  animation: cardGlow 2s ease-in-out infinite;
}

@keyframes cardGlow {
  0%, 100% {
    box-shadow: 0 2px 8px rgba(255, 107, 107, 0.2);
  }
  50% {
    box-shadow: 0 4px 16px rgba(255, 107, 107, 0.4);
  }
}

.reply-to {
  font-size: 12px;
  color: #999;
}

.replied-to-user {
  color: #1890ff;
  font-weight: 500;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-actions :deep(.el-icon) {
  color: #606266;
}

.comment-actions :deep(.el-button--danger) {
  color: #f56c6c;
}

.comment-actions :deep(.el-button--danger .el-icon) {
  color: #f56c6c;
}

.comment-content {
  font-size: 16px;
  line-height: 1.5;
  color: #333;
}

/* 回复输入框样式 */
.reply-input-section {
  margin-top: 15px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.reply-input-section :deep(.el-input__inner) {
  font-size: 16px;
}

.reply-input-section .el-form-item {
  margin-bottom: 25px;
}

.new-comment-section .el-form-item {
  margin-bottom: 25px;
}

.new-comment-section :deep(.el-input__inner) {
  font-size: 16px;
}

/* 新评论输入框样式 */
.new-comment-section {
  margin-top: 20px;
  margin-bottom: 20px;
}

/* 回复列表样式 */
.replies-list {
  margin-top: 15px;
  padding-left: 20px;
  border-left: 2px solid #e8e8e8;
}

.expand-collapse-btn {
  margin-top: 10px;
  text-align: center;
}

.reply-item {
  margin-bottom: 10px;
}

.reply-item .el-card {
  background-color: #f9f9f9;
  border: none;
  box-shadow: none;
}

.reply-item .comment-header {
  font-size: 15px;
}

.reply-item .comment-content {
  font-size: 15px;
}

.loading-container,
.error-container {
  text-align: center;
  padding: 50px 0;
}

@media (max-width: 768px) {
  .post-header {
    flex-direction: column;
    gap: 15px;
  }
  
  .post-images {
    justify-content: center;
  }
  
  .post-image {
    width: 100%;
    max-width: 300px;
    height: 200px;
  }
}

.el-card {
  padding: 30px;
}
</style>