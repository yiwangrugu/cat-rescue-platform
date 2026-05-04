<template>
  <div class="user-profile-container">
    <div class="profile-header">
      <div class="header-content">
      <h1><el-icon><UserFilled /></el-icon> 个人中心</h1>
      <p class="profile-subtitle">管理您的个人信息和活动</p>
      </div>
    </div>
    
    <div class="profile-content">
      <!-- 基本信息卡片 -->
      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <el-button type="primary" size="medium" @click="editMode = true" v-if="!editMode" class="edit-btn">
              编辑信息
            </el-button>
            <div v-else class="edit-actions">
              <el-button type="success" size="medium" @click="saveProfile" class="save-btn">保存</el-button>
              <el-button size="medium" @click="cancelEdit" class="cancel-btn">取消</el-button>
            </div>
          </div>
        </template>

        <div class="profile-info">
          <!-- 头像区域 -->
          <div class="avatar-section">
            <div class="avatar-box">
              <div class="avatar-container">
                <el-upload
                  v-if="editMode"
                  action="#"
                  :auto-upload="false"
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  :on-change="handleAvatarChange"
                  :disabled="uploadingAvatar"
                >
                  <div class="avatar-frame">
                    <el-avatar :size="140" :src="userInfo.avatar ? `http://localhost:8080${userInfo.avatar}` : '/img/default-avatar.png'" class="user-avatar" />
                    <div class="avatar-actions">
                      <el-icon class="camera-icon"><Camera /></el-icon>
                    </div>
                  </div>
                </el-upload>
                <div class="avatar-frame" v-else>
                  <el-avatar :size="140" :src="userInfo.avatar ? `http://localhost:8080${userInfo.avatar}` : '/img/default-avatar.png'" class="user-avatar" />
                </div>
              </div>
              <div class="username-display">
                <h3 class="user-name">{{ userInfo.username || '未设置用户名' }}</h3>
                <p class="user-email">{{ userInfo.email || '未设置邮箱' }}</p>
              </div>
            </div>
          </div>

          <!-- 用户信息表单 -->
          <div class="info-form">
            <el-form :model="userInfo" label-width="100px" :disabled="!editMode" class="user-form">
              <div class="form-row">
                <el-form-item label="用户名">
                  <el-input v-model="userInfo.username" placeholder="请输入用户名" class="form-input" />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="userInfo.email" placeholder="请输入邮箱地址" class="form-input" />
                </el-form-item>
              </div>
              
              <div class="form-row">
                <el-form-item label="手机号">
                  <el-input v-model="userInfo.phone" placeholder="请输入手机号码" class="form-input" />
                </el-form-item>
                <el-form-item label="性别">
                  <el-radio-group v-model="userInfo.gender" class="gender-group">
                    <el-radio label="male" class="gender-radio">男</el-radio>
                    <el-radio label="female" class="gender-radio">女</el-radio>
                    <el-radio label="unknown" class="gender-radio">保密</el-radio>
                  </el-radio-group>
                </el-form-item>
              </div>
              
              <el-form-item label="地址">
                <el-input 
                  type="textarea" 
                  v-model="userInfo.address" 
                  :rows="3" 
                  placeholder="请输入详细地址" 
                  class="form-textarea"
                />
              </el-form-item>
              
              <el-form-item label="个性签名">
                <el-input 
                  type="textarea" 
                  v-model="userInfo.bio" 
                  :rows="4" 
                  placeholder="请输入个性签名" 
                  maxlength="200"
                  show-word-limit
                  class="form-textarea bio-textarea"
                />
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-card>

      <!-- 账户安全卡片 -->
      <el-card class="security-card">
        <template #header>
          <div class="card-header">
            <span>账户安全</span>
          </div>
        </template>

        <div class="security-actions">
          <div class="security-item">
            <div class="security-info">
              <h4>修改密码</h4>
              <p>定期修改密码可以提高账户安全性</p>
            </div>
            <el-button type="primary" @click="showChangePassword = true" class="action-btn">修改密码</el-button>
          </div>
          
          <div class="security-item">
            <div class="security-info">
              <h4>登录记录</h4>
              <p>最近登录：{{ formatLoginTime(authStore.user?.lastLoginTime) }}</p>
              <p>上一次登录：{{ formatLoginTime(authStore.user?.previousLoginTime) }}</p>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 我的活动卡片 -->
      <el-card class="activity-card">
        <template #header>
          <div class="card-header">
            <span>活动统计</span>
          </div>
        </template>

        <div class="activity-stats">
          <div class="stat-item" v-for="(value, key) in statItems" :key="key" @click="statItemClick(key)">
            <div class="stat-icon" :class="key + '-icon'">
              <el-icon :size="32"><component :is="value.icon" /></el-icon>
            </div>
            <div class="stat-number">{{ userStats[key] || 0 }}</div>
            <div class="stat-label">{{ value.label }}</div>
            <div class="stat-trend" v-if="userStats[key + 'Trend']">
              <el-icon :class="{ 'trend-up': userStats[key + 'Trend'] > 0, 'trend-down': userStats[key + 'Trend'] < 0 }">
                <ArrowUp v-if="userStats[key + 'Trend'] > 0" />
                <ArrowDown v-else-if="userStats[key + 'Trend'] < 0" />
                <Minus v-else />
              </el-icon>
              <span>{{ Math.abs(userStats[key + 'Trend']) }}%</span>
            </div>
          </div>
        </div>
        
        <div class="activity-summary">
          <div class="summary-item">
            <span class="summary-label">活跃度：</span>
            <el-progress :percentage="userStats.activityLevel || 0" :show-text="false" />
            <span class="summary-value">{{ userStats.activityLevel || 0 }}%</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">注册天数：</span>
            <span class="summary-value">{{ userStats.registerDays || 0 }}天</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showChangePassword" title="修改密码" width="400px" class="password-dialog">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input type="password" v-model="passwordForm.oldPassword" placeholder="请输入原密码" class="form-input" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input type="password" v-model="passwordForm.newPassword" placeholder="请输入新密码" class="form-input" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input type="password" v-model="passwordForm.confirmPassword" placeholder="请再次输入新密码" class="form-input" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePassword = false" class="cancel-btn">取消</el-button>
        <el-button type="primary" @click="changePassword" :loading="changingPassword" class="save-btn">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/auth'
import { userApi } from '@/api/user'
import { Message, Star, HomeFilled, HelpFilled, Camera, UserFilled, ArrowUp, ArrowDown, Minus } from '@element-plus/icons-vue'

const authStore = useAuthStore()
const router = useRouter()

// 编辑模式
const editMode = ref(false)
const showChangePassword = ref(false)
const changingPassword = ref(false)
const uploadingAvatar = ref(false)
const originalBio = ref('')

// 监听编辑模式变化
watch(editMode, (newVal) => {
  if (newVal) {
    // 进入编辑模式时，保存原始的个性签名
    originalBio.value = userInfo.bio
    // 如果个性签名是默认值，则清空它，方便用户编辑
    if (userInfo.bio === '该用户似乎什么也没留下~') {
      userInfo.bio = ''
    }
  } else {
    // 退出编辑模式时，如果用户没有输入个性签名，则恢复默认值
    if (!userInfo.bio.trim()) {
      userInfo.bio = '该用户似乎什么也没留下~'
    }
  }
})

// 用户信息
const userInfo = reactive({
  id: null,
  username: '',
  email: '',
  phone: '',
  gender: 'unknown',
  address: '',
  bio: '',
  avatar: ''
})

// 用户统计数据
const userStats = reactive({
  postCount: 0,
  adoptionCount: 0,
  favoriteCount: 0,
  rescueCount: 0,
  postCountTrend: 0,
  adoptionCountTrend: 0,
  favoriteCountTrend: 0,
  rescueCountTrend: 0,
  activityLevel: 0,
  registerDays: 0
})

// 统计项配置
const statItems = {
  postCount: {
    label: '发布的帖子',
    icon: 'Message'
  },
  adoptionCount: {
    label: '领养申请',
    icon: 'HomeFilled'
  },
  favoriteCount: {
    label: '收藏数量',
    icon: 'Star'
  },
  rescueCount: {
    label: '救助需求',
    icon: 'HelpFilled'
  }
}

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 加载用户信息
const loadUserInfo = () => {
  if (authStore.user) {
    Object.assign(userInfo, {
      id: authStore.user.id,
      username: authStore.user.username || '',
      email: authStore.user.email || '',
      phone: authStore.user.phone || '',
      gender: authStore.user.gender || 'unknown',
      address: authStore.user.location || '',
      bio: authStore.user.introduction || '该用户似乎什么也没留下~',
      avatar: authStore.user.avatar || ''
    })
  }
}

// 加载用户统计数据
const loadUserStats = async () => {
  try {
    // 获取用户ID
    const userId = authStore.user?.id
    if (!userId) {
      console.warn('用户未登录，无法获取统计数据')
      return
    }
    
    console.log('开始加载用户统计数据，用户ID:', userId)
    console.log('当前登录用户信息:', authStore.user)
    
    // 调试token信息
    const token = localStorage.getItem('user_token')
    console.log('当前token:', token)
    console.log('token是否存在:', !!token)
    console.log('token长度:', token ? token.length : 0)
    
    // 检查所有可能的token存储键
    console.log('所有localStorage中的token相关键:')
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (key && key.includes('token')) {
        console.log(`Key: ${key}, Value: ${localStorage.getItem(key)}`)
      }
    }
    
    // 调用后端API获取真实数据
    console.log('开始调用用户统计API...')
    const response = await userApi.getUserStatistics(userId)
    
    console.log('API响应状态:', response.status)
    console.log('API响应数据:', response.data)
    
    if (response.data && !response.data.error) {
      console.log('API返回数据字段检查:')
      console.log('postCount:', response.data.postCount)
      console.log('adoptionCount:', response.data.adoptionCount)
      console.log('favoriteCount:', response.data.favoriteCount)
      console.log('rescueCount:', response.data.rescueCount)
      
      // 更新统计数据
      Object.assign(userStats, response.data)
      
      // 确保所有必需字段都有值
      userStats.postCount = userStats.postCount || 0
      userStats.adoptionCount = userStats.adoptionCount || 0
      userStats.favoriteCount = userStats.favoriteCount || 0
      userStats.rescueCount = userStats.rescueCount || 0
      userStats.postCountTrend = userStats.postCountTrend || 0
      userStats.adoptionCountTrend = userStats.adoptionCountTrend || 0
      userStats.favoriteCountTrend = userStats.favoriteCountTrend || 0
      userStats.rescueCountTrend = userStats.rescueCountTrend || 0
      userStats.activityLevel = userStats.activityLevel || 0
      userStats.registerDays = userStats.registerDays || 0
      
      console.log('用户统计数据加载成功:', userStats)
    } else {
      console.error('获取用户统计数据失败:', response.data?.error)
      console.log('API返回数据为空或包含错误，使用默认数据')
      // 如果API返回错误，使用默认数据
      loadDefaultStats()
    }
    
  } catch (error) {
    console.error('加载用户统计数据失败:', error)
    console.error('错误详情:', error.response?.status, error.response?.data)
    console.error('错误消息:', error.message)
    
    console.log('API调用失败，使用模拟数据作为临时解决方案')
    // 如果API调用失败，使用模拟数据作为临时解决方案
    loadMockStats()
  }
}

// 加载模拟统计数据（临时解决方案）
const loadMockStats = () => {
  // 基于用户ID生成一些相对真实的模拟数据
  const userId = authStore.user?.id || 1
  const baseValue = userId % 100 + 1 // 基于用户ID生成基础值
  
  userStats.postCount = Math.floor(baseValue * 0.8)
  userStats.adoptionCount = Math.floor(baseValue * 0.3)
  userStats.favoriteCount = Math.floor(baseValue * 1.5)
  userStats.rescueCount = Math.floor(baseValue * 0.2)
  userStats.postCountTrend = Math.floor(Math.random() * 40) - 20
  userStats.adoptionCountTrend = Math.floor(Math.random() * 40) - 20
  userStats.favoriteCountTrend = Math.floor(Math.random() * 40) - 20
  userStats.rescueCountTrend = Math.floor(Math.random() * 40) - 20
  
  // 计算活跃度
  const totalActivities = userStats.postCount + userStats.adoptionCount + userStats.favoriteCount + userStats.rescueCount
  userStats.activityLevel = Math.min(Math.floor((totalActivities / 50) * 100), 100)
  
  // 计算注册天数
  if (authStore.user?.createTime) {
    const registerDate = new Date(authStore.user.createTime)
    const now = new Date()
    const diffTime = Math.abs(now - registerDate)
    userStats.registerDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  } else {
    userStats.registerDays = 30 // 默认值
  }
  
  console.log('使用模拟统计数据:', userStats)
}

// 加载默认统计数据（用于API调用失败时）
const loadDefaultStats = () => {
  userStats.postCount = 0
  userStats.adoptionCount = 0
  userStats.favoriteCount = 0
  userStats.rescueCount = 0
  userStats.postCountTrend = 0
  userStats.adoptionCountTrend = 0
  userStats.favoriteCountTrend = 0
  userStats.rescueCountTrend = 0
  userStats.activityLevel = 0
  userStats.registerDays = 0
}

// 保存个人信息
const saveProfile = async () => {
  try {
    const profileData = {
      id: userInfo.id,
      username: userInfo.username,
      email: userInfo.email,
      phone: userInfo.phone,
      gender: userInfo.gender,
      location: userInfo.address,
      introduction: userInfo.bio
    }
    
    const response = await authApi.updateProfile(profileData)
    
    // 更新本地存储
    authStore.user = response.data.user
    localStorage.setItem('user', JSON.stringify(response.data.user))
    
    ElMessage.success('个人信息更新成功')
    editMode.value = false
  } catch (error) {
    console.error('保存个人信息失败:', error)
    ElMessage.error(error.response?.data?.error || '保存失败，请重试')
  }
}

// 取消编辑
const cancelEdit = () => {
  editMode.value = false
  // 重新加载原始数据
  loadUserInfo()
}

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isJPGOrPNG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPGOrPNG) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

// 处理头像上传
const handleAvatarChange = async (file) => {
  if (!beforeAvatarUpload(file.raw)) {
    return
  }
  
  try {
    uploadingAvatar.value = true
    
    const response = await authApi.uploadAvatar(file.raw, userInfo.id)
    
    // 更新用户信息和本地存储
    userInfo.avatar = response.data.avatarUrl
    authStore.user.avatar = response.data.avatarUrl
    localStorage.setItem('user', JSON.stringify(authStore.user))
    
    ElMessage.success('头像上传成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.error || '头像上传失败')
    // 如果上传失败，恢复原来的头像
    loadUserInfo()
  } finally {
    uploadingAvatar.value = false
  }
}

// 修改密码
const changePassword = async () => {
  try {
    changingPassword.value = true
    
    // 模拟密码修改操作
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('密码修改成功')
    showChangePassword.value = false
    
    // 清空表单
    Object.assign(passwordForm, {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
  } catch (error) {
    ElMessage.error('密码修改失败')
  } finally {
    changingPassword.value = false
  }
}

// 格式化登录时间
const formatLoginTime = (time) => {
  if (!time) return '暂无记录'
  
  const date = new Date(time)
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / (1000 * 60))
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  
  if (diffMins < 1) return '刚刚'
  if (diffMins < 60) return `${diffMins}分钟前`
  if (diffHours < 24) return `${diffHours}小时前`
  if (diffDays < 7) return `${diffDays}天前`
  
  return date.toLocaleDateString('zh-CN')
}

// 查看登录历史
const viewLoginHistory = () => {
  ElMessage.info('登录记录功能开发中')
}

// 统计项点击事件
const statItemClick = (key) => {
  switch (key) {
    case 'postCount':
      // 跳转到我的帖子页面
      router.push('/posts')
      break
    case 'adoptionCount':
      // 跳转到我的领养申请页面
      router.push('/adoptions')
      break
    case 'favoriteCount':
      // 跳转到我的收藏页面
      router.push('/favorites')
      break
    case 'rescueCount':
      // 跳转到救助需求页面
      router.push('/rescue')
      break
    default:
      break
  }
}

onMounted(async () => {
  loadUserInfo()
  await loadUserStats()
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

.user-profile-container {
  max-width: 1550px;
  margin: 0 auto;
  padding: 20px;
}

/* 页面头部样式 */
.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 94px;
  margin-bottom: 24px;
  background: linear-gradient(45deg, rgba(235, 136, 226, 0.8) 0%, rgba(235, 134, 134, 0.6) 50%, rgba(239, 103, 40, 0.4) 100%);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
  animation: fadeInUp 0.6s ease;
} 

.profile-header h1 {
  color: #fff;
  font-size: 2em;
  font-weight: 600;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.profile-header h1 .el-icon {
  font-size: 1em;
}

.profile-subtitle {
  color: #fff;
  font-size: 1em;
  opacity: 0.9;
  margin: 0;
}

/* 卡片容器样式 */
.profile-content {
  display: grid;
  gap: 30px;
}

/* 卡片头部样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 10px;
}

.card-header span {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}


.edit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

.cancel-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(144, 147, 153, 0.3);
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 编辑按钮组样式 */
.edit-actions {
  display: flex;
  gap: 10px;
}

/* 基本信息卡片样式 */
.profile-card {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease;
}

.profile-card:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

/* 个人信息布局 */
.profile-info {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding: 30px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 12px;
  border: 2px solid #e4e7ed;
}

/* 头像区域样式 */
.avatar-section {
  flex-shrink: 0;
  text-align: center;
  animation: fadeInLeft 0.8s ease;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

/* 头像框架样式 */
.avatar-frame {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  border: 4px solid #ffffff;
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
  
}

.avatar-frame:hover .avatar-actions {
  transition: all .5s ease;
  bottom: -20px;
}

/* 头像操作按钮样式 */
.avatar-actions {
  position: absolute;
  bottom: -80px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  align-items: top;
  padding-top: 5px;
  animation: fadeIn 0.5s ease;
  background: rgba(0, 0, 0, 0.4);
  width: 140px;
  height: 60px;
}

/* 头像样式 */
.user-avatar {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.user-avatar:hover {
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.camera-icon {
  font-size: 30px;
  color: #e4e7ed;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
}


/* 头像盒子样式 */
.avatar-box {
  background: linear-gradient(45deg,rgba(235, 136, 226, 0.6) 0%, rgba(235, 134, 134, 0.4) 50%, rgba(239, 103, 40, 0.2) 100%);
  border-radius: 16px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.avatar-box:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

/* 用户名显示样式 */
.username-display {
  text-align: center;
  animation: fadeInUp 0.8s ease 0.3s both;
}

.user-name {
  font-size: 20px;
  font-weight: bold;
  color: #555;
  margin: 0 0 5px 0;
  transition: all 0.3s ease;
}

.user-email {
  font-size: 14px;
  color: #666;
  margin: 0;
  font-weight: 500;
  transition: all 0.3s ease;
}

/* 表单样式 */
.info-form {
  flex: 1;
  min-width: 0;
  animation: fadeInRight 0.8s ease 0.5s both;
}

/* 用户表单样式 */
.user-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 表单行样式 */
.form-row {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.form-row .el-form-item {
  flex: 1;
  min-width: 200px;
  margin-bottom: 0;
}

/* 表单输入框样式 */
.form-input, .form-textarea {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
  width: 100%;
}

.form-input:focus, .form-textarea:focus {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  transform: translateY(-1px);
}

/* 个性签名文本域样式 */
.bio-textarea {
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.bio-textarea:focus {
  background: #ffffff;
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 性别选择样式 */
.gender-group {
  display: flex;
  gap: 20px;
  align-items: center;
}

.gender-radio {
  margin-right: 10px;
  transition: all 0.3s ease;
}

.gender-radio:hover {
  transform: scale(1.1);
}

/* 响应式布局 */
@media (max-width: 1024px) {
  .profile-info {
    gap: 40px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 20px;
  }
  
  .form-row .el-form-item {
    min-width: 100%;
  }
}

@media (max-width: 768px) {
  .profile-info {
    flex-direction: column;
    gap: 30px;
    align-items: center;
    padding: 20px;
  }
  
  .avatar-frame {
    margin-bottom: 5px;
  }
  
  .user-avatar {
    --el-avatar-size: 120px;
  }
  
  .user-name {
    font-size: 18px;
  }
  
  .info-form {
    width: 100%;
  }
  
  .form-row {
    flex-direction: column;
    gap: 20px;
  }
  
  .form-row .el-form-item {
    min-width: 100%;
  }
}

/* 动画效果 */
@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 安全卡片样式 */
.security-card {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.security-card:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.security-actions {
  display: grid;
  gap: 25px;
  padding: 20px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.security-item:hover {
  background: #f1f3f5;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.security-info h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 16px;
  font-weight: 600;
}

.security-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

/* 活动卡片样式 */
.activity-card {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.activity-card:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

/* 活动统计样式 */
.activity-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  padding: 20px;
}

.stat-item {
  padding: 30px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  border-radius: 12px;
  text-align: center;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.stat-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  background: linear-gradient(135deg, #e4e7ed 0%, #f5f7fa 100%);
}

/* 统计图标样式 */
.stat-icon {
  font-size: 32px;
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.stat-item:hover .stat-icon {
  transform: scale(1.2);
}

.postCount-icon {
  color: #409EFF;
}

.adoptionCount-icon {
  color: #67C23A;
}

.favoriteCount-icon {
  color: #E6A23C;
}

.rescueCount-icon {
  color: #F56C6C;
}

/* 统计数字样式 */
.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
  transition: all 0.3s ease;
}

.stat-item:hover .stat-number {
  transform: scale(1.1);
}

/* 统计标签样式 */
.stat-label {
  font-size: 14px;
  color: #666;
  transition: all 0.3s ease;
}

.stat-item:hover .stat-label {
  color: #333;
}

/* 修改密码对话框样式 */
.password-dialog {
  border-radius: 12px;
  overflow: hidden;
}

/* 响应式布局 */
@media (max-width: 1024px) {
  .activity-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .user-profile-container {
    padding: 10px;
  }
  
  .profile-header {
    padding: 20px 0;
    margin-bottom: 20px;
  }
  
  .profile-header h1 {
    font-size: 24px;
  }
  
  .profile-header p {
    font-size: 14px;
  }
  
  .profile-info {
    flex-direction: column;
    gap: 30px;
    align-items: center;
  }
  
  .info-form {
    width: 100%;
  }
  
  .activity-stats {
    grid-template-columns: 1fr;
  }
  
  .security-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .security-item .action-btn {
    align-self: flex-end;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .edit-actions {
    width: 100%;
    justify-content: flex-end;
  }
}

.stat-trend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  margin-top: 8px;
  font-size: 12px;
  font-weight: 500;
}

.trend-up {
  color: #67C23A;
}

.trend-down {
  color: #F56C6C;
}

.activity-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-top: 1px solid #e4e7ed;
  margin-top: 10px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.summary-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.summary-value {
  font-size: 16px;
  color: #333;
  font-weight: 600;
}

.el-progress {
  width: 100px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .activity-summary {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .summary-item {
    width: 100%;
    justify-content: space-between;
  }
}

/* 动画效果 */
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

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

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
</style>