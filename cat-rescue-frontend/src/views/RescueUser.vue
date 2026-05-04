<template>
  <div class="rescue-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Help /></el-icon>
          救助需求
        </h1>
        <p class="page-subtitle">发布和查看需要救助的猫咪信息</p>
      </div>
      <div class="header-stats">
        <div class="stat-card total">
          <div class="stat-number">{{ stats.totalRescues || 0 }}</div>
          <div class="stat-label">总救助需求</div>
        </div>
        <div class="stat-card pending">
          <div class="stat-number">{{ stats.pendingRescues || 0 }}</div>
          <div class="stat-label">待处理</div>
        </div>
        <div class="stat-card in-progress">
          <div class="stat-number">{{ stats.inProgressRescues || 0 }}</div>
          <div class="stat-label">进行中</div>
        </div>
        <div class="stat-card completed">
          <div class="stat-number">{{ stats.completedRescues || 0 }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
    </div>
    
    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <el-card shadow="never">
        <el-row :gutter="20" align="middle">
          <el-col :span="6">
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索救助需求" 
              prefix-icon="Search"
              clearable
              @clear="loadRescueList"
              @keyup.enter="loadRescueList"
            />
          </el-col>
          <el-col :span="5">
            <el-select v-model="filterStatus" placeholder="状态筛选" clearable @change="loadRescueList">
              <el-option label="待处理" value="待处理" />
              <el-option label="进行中" value="进行中" />
              <el-option label="已完成" value="已完成" />
            </el-select>
          </el-col>
          <el-col :span="5">
            <el-select v-model="filterUrgency" placeholder="紧急程度" clearable @change="loadRescueList">
              <el-option label="低" value="低" />
              <el-option label="中" value="中" />
              <el-option label="高" value="高" />
              <el-option label="紧急" value="紧急" />
            </el-select>
          </el-col>
          <el-col :span="8">
            <div class="action-buttons">
              <el-button type="primary" @click="loadRescueList">
                <el-icon><Search /></el-icon>搜索
              </el-button>
              <el-button @click="resetFilters">
                <el-icon><Refresh /></el-icon>重置
              </el-button>
              <el-button type="primary" size="large" @click="showRescueForm = true">
                <el-icon><Plus /></el-icon>
                发布救助需求
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>
    
    <!-- 救助需求列表 -->
    <div class="rescue-list" ref="rescueListContainer">
      <template v-if="visibleRescueList.length > 0">
        <el-card v-for="rescue in visibleRescueList" :key="rescue.id" class="rescue-card" :class="[getStatusClass(rescue.status), getUrgencyClass(rescue.urgencyLevel)]" @click="viewRescueDetail(rescue)">
          <template #header>
            <div class="rescue-card-header">
              <!-- 第一行：标签和时间 -->
              <div class="header-top">
                <div class="header-tags">
                  <el-tag :type="getUrgencyType(rescue.urgencyLevel)" size="small">
                    {{ getUrgencyText(rescue.urgencyLevel) }}
                  </el-tag>
                  <el-tag :type="getStatusType(rescue.status)" size="small">
                    {{ getStatusText(rescue.status) }}
                  </el-tag>
                </div>
                <div class="header-time">
                  <el-icon><Timer /></el-icon>
                  <span class="rescue-time">{{ formatDate(rescue.createTime) }}</span>
                </div>
              </div>
              <!-- 第二行：标题 -->
              <div class="header-bottom">
                <span class="rescue-title">{{ rescue.title }}</span>
              </div>
            </div>
          </template>
          
          <div class="rescue-content">
            <p class="rescue-description" :style="{ borderLeftColor: getUrgencyColor(rescue.urgencyLevel) }">{{ rescue.description }}</p>
            
            <div class="cat-condition" v-if="rescue.catCondition" :style="{ borderLeftColor: getUrgencyColor(rescue.urgencyLevel) }">
              <h4 :style="{ color: getUrgencyColor(rescue.urgencyLevel) }">猫咪状况：</h4>
              <p>{{ rescue.catCondition }}</p>
            </div>
            
            <!-- 已完成标识 -->
            <div v-if="rescue.status === '已完成' || rescue.status === 'COMPLETED'" class="completed-overlay">
              <img src="/icon/completed.svg" alt="已完成" class="completed-icon" />
            </div>
          </div>
          
          <template #footer>
            <div class="rescue-actions">
              <el-button size="small" @click="viewRescueDetail(rescue)">查看详情</el-button>
              <el-button 
                size="small" 
                type="danger" 
                v-if="authStore.isAuthenticated && authStore.user?.id === rescue.reporterId && rescue.status === '待处理'" 
                @click="(e) => { e.stopPropagation(); cancelRescue(rescue); }"
              >
                撤销需求
              </el-button>
            </div>
          </template>
        </el-card>
      </template>
      
      <!-- 加载更多指示器 -->
      <div v-if="loadingMore" class="loading-more">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在加载更多...</span>
      </div>
      
      <!-- 没有更多数据提示 -->
      <div v-else-if="!hasMore && visibleRescueList.length > 0" class="no-more-data">
        <span>已显示全部救助需求</span>
      </div>
      
      <div v-else-if="filteredRescueList.length === 0" class="empty-state">
        <el-empty description="暂无救助需求" />
        <div class="empty-actions">
          <el-button type="primary" @click="showRescueForm = true">发布第一个救助需求</el-button>
        </div>
      </div>
    </div>
    
    <!-- 发布救助需求对话框 -->
    <el-dialog v-model="showRescueForm" title="发布救助需求" width="700px">
      <el-form :model="rescueForm" :rules="rescueRules" ref="rescueFormRef" label-width="100px">
        <el-form-item label="需求标题" prop="title">
          <el-input v-model="rescueForm.title" placeholder="请输入救助需求标题" maxlength="50" show-word-limit />
        </el-form-item>
        
        <el-form-item label="需求描述" prop="description">
          <el-input v-model="rescueForm.description" type="textarea" placeholder="请详细描述救助需求" rows="4" maxlength="500" show-word-limit />
        </el-form-item>
        
        <el-form-item label="具体位置" prop="location">
          <el-input v-model="rescueForm.location" placeholder="请输入猫咪所在位置" maxlength="100" show-word-limit />
        </el-form-item>
        
        <el-form-item label="猫咪状况" prop="catCondition">
          <el-input v-model="rescueForm.catCondition" type="textarea" placeholder="请描述猫咪的健康状况、受伤情况等" rows="3" maxlength="300" show-word-limit />
        </el-form-item>
        
        <el-form-item label="紧急程度" prop="urgencyLevel">
          <el-select v-model="rescueForm.urgencyLevel" placeholder="请选择紧急程度">
            <el-option label="低" value="低" />
            <el-option label="中" value="中" />
            <el-option label="高" value="高" />
            <el-option label="紧急" value="紧急" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="rescueForm.contactPhone" placeholder="请输入联系电话" maxlength="20" show-word-limit />
        </el-form-item>
        
        <el-form-item label="上传图片">
          <el-upload
            v-model:file-list="rescueForm.images"
            :auto-upload="false"
            list-type="picture-card"
            :multiple="true"
            :limit="5"
            :on-change="handleImageChange"
            :on-remove="handleImageRemove"
            :on-exceed="handleExceed"
            :before-upload="beforeUpload"
          >
            <el-icon><Plus /></el-icon>
            <div class="el-upload__text">
              点击上传图片<br>
              <span class="el-upload__tip">支持jpg、png格式，最多5张</span>
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showRescueForm = false">取消</el-button>
        <el-button type="primary" @click="submitRescueForm" :loading="submitting">发布</el-button>
      </template>
    </el-dialog>
    
    <!-- 救助需求详情对话框 -->
           <el-dialog v-model="showRescueDetail" title="救助需求详情" width="1100px" class="rescue-detail-dialog" style="margin-top: 65px;">
             <div v-if="selectedRescue" class="rescue-detail" style="max-height: 550px; overflow-y: auto;">
        <div class="detail-header">
          <div class="detail-tags">
            <el-tag :type="getUrgencyType(selectedRescue?.urgencyLevel)" size="medium">
              {{ getUrgencyText(selectedRescue?.urgencyLevel) }}
            </el-tag>
            <el-tag :type="getStatusType(selectedRescue?.status)" size="medium">
              {{ getStatusText(selectedRescue?.status) }}
            </el-tag>
          </div>
          <div class="detail-time">
            <el-icon><Timer /></el-icon>
            <span>{{ formatDate(selectedRescue?.createTime) }}</span>
          </div>
        </div>
        
        <!-- 相关图片放在最上面 -->
        <div class="detail-section" v-if="selectedRescue?.images && selectedRescue.images.length > 0">
          <h3 :style="{ borderLeftColor: getUrgencyColor(selectedRescue?.urgencyLevel), color: getUrgencyColor(selectedRescue?.urgencyLevel) }">相关图片</h3>
          <div class="image-gallery">
            <div 
              v-for="(image, index) in parseImages(selectedRescue.images)" 
              :key="index" 
              class="image-item"
            >
              <el-image 
                :src="image.url" 
                :preview-src-list="parseImages(selectedRescue.images).map(img => img.url)"
                fit="cover"
                class="detail-image"
                :preview-teleported="true"
                :hide-on-click-modal="true"
                :z-index="9999"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <span>图片加载失败</span>
                  </div>
                </template>
              </el-image>
            </div>
          </div>
        </div>
        
        <!-- 详细信息占一行 -->
        <div class="detail-section">
          <h3 :style="{ borderLeftColor: getUrgencyColor(selectedRescue?.urgencyLevel), color: getUrgencyColor(selectedRescue?.urgencyLevel) }">详细描述</h3>
          <p>{{ selectedRescue?.description }}</p>
        </div>
        
        <!-- 具体位置占一行 -->
        <div class="detail-section">
          <h3 :style="{ borderLeftColor: getUrgencyColor(selectedRescue?.urgencyLevel), color: getUrgencyColor(selectedRescue?.urgencyLevel) }">具体位置</h3>
          <p>{{ selectedRescue?.location }}</p>
        </div>
        
        <!-- 猫咪状况 -->
        <div class="detail-section" v-if="selectedRescue?.catCondition">
          <h3 :style="{ borderLeftColor: getUrgencyColor(selectedRescue?.urgencyLevel), color: getUrgencyColor(selectedRescue?.urgencyLevel) }">猫咪状况</h3>
          <p>{{ selectedRescue?.catCondition || '未提供' }}</p>
        </div>
        
        <!-- 联系信息和志愿者信息放一行 -->
        <div class="detail-section contact-row">
          <div class="contact-column">
            <h3 :style="{ borderLeftColor: getUrgencyColor(selectedRescue?.urgencyLevel), color: getUrgencyColor(selectedRescue?.urgencyLevel) }">联系信息</h3>
            <div class="contact-info">
              <div class="info-item">
                <el-icon :style="{ color: getUrgencyColor(selectedRescue?.urgencyLevel) }"><User /></el-icon>
                <span>{{ selectedRescue?.reporterUsername || '匿名用户' }}</span>
              </div>
              <div class="info-item">
                <el-icon :style="{ color: getUrgencyColor(selectedRescue?.urgencyLevel) }"><Phone /></el-icon>
                <span>{{ selectedRescue?.contactPhone || '未提供' }}</span>
              </div>
            </div>
          </div>
          
          <div class="contact-column" v-if="selectedRescue?.volunteerName">
            <h3 :style="{ borderLeftColor: getUrgencyColor(selectedRescue?.urgencyLevel), color: getUrgencyColor(selectedRescue?.urgencyLevel) }">志愿者信息</h3>
            <div class="contact-info">
              <div class="info-item">
                <el-icon :style="{ color: getUrgencyColor(selectedRescue?.urgencyLevel) }"><User /></el-icon>
                <span>{{ selectedRescue?.volunteerName || '未分配' }}</span>
              </div>
              <div class="info-item">
                <el-icon :style="{ color: getUrgencyColor(selectedRescue?.urgencyLevel) }"><Phone /></el-icon>
                <span>{{ selectedRescue?.volunteerPhone || '未提供' }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 负责人信息 -->
        <div class="detail-section" v-if="selectedRescue?.rescuerId">
          <h3 :style="{ borderLeftColor: getUrgencyColor(selectedRescue?.urgencyLevel), color: getUrgencyColor(selectedRescue?.urgencyLevel) }">负责人信息</h3>
          <div class="contact-info">
            <div class="info-item">
              <el-icon :style="{ color: getUrgencyColor(selectedRescue?.urgencyLevel) }"><UserFilled /></el-icon>
              <span>负责人ID：{{ selectedRescue?.rescuerId }}</span>
            </div>
            <div class="info-item" v-if="selectedRescue?.rescuerUsername">
              <el-icon :style="{ color: getUrgencyColor(selectedRescue?.urgencyLevel) }"><User /></el-icon>
              <span>负责人：{{ selectedRescue?.rescuerUsername }}</span>
            </div>
          </div>
        </div>
        
        <!-- 处理时间 -->
        <div class="detail-section" v-if="selectedRescue?.acceptTime || selectedRescue?.completeTime">
          <h3 :style="{ borderLeftColor: getUrgencyColor(selectedRescue?.urgencyLevel), color: getUrgencyColor(selectedRescue?.urgencyLevel) }">处理时间</h3>
          <div class="contact-info">
            <div class="info-item" v-if="selectedRescue?.acceptTime">
              <el-icon :style="{ color: getUrgencyColor(selectedRescue?.urgencyLevel) }"><Clock /></el-icon>
              <span>处理时间：{{ formatDate(selectedRescue?.acceptTime) }}</span>
            </div>
            <div class="info-item" v-if="selectedRescue?.completeTime">
              <el-icon :style="{ color: getUrgencyColor(selectedRescue?.urgencyLevel) }"><Check /></el-icon>
              <span>结束时间：{{ formatDate(selectedRescue?.completeTime) }}</span>
            </div>
          </div>
        </div>
        
        <!-- 救援日志图片 -->
        <div class="detail-section" v-if="selectedRescue?.rescueLogImages">
          <h3 :style="{ borderLeftColor: getUrgencyColor(selectedRescue?.urgencyLevel), color: getUrgencyColor(selectedRescue?.urgencyLevel) }">救援日志图片</h3>
          <div class="rescue-log-images">
            <div 
              v-for="(image, index) in parseRescueLogImages(selectedRescue.rescueLogImages)" 
              :key="index" 
              class="log-image-item"
            >
              <el-image 
                :src="image" 
                fit="cover" 
                class="log-image"
                :preview-src-list="parseRescueLogImages(selectedRescue.rescueLogImages)"
                :initial-index="index"
                :preview-teleported="true"
                :hide-on-click-modal="true"
                :z-index="9999"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <span>图片加载失败</span>
                  </div>
                </template>
              </el-image>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showRescueDetail = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Timer, Location, User, Phone, Picture, Plus, Loading } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { rescueApi } from '@/api/rescue'
import { authApi } from '@/api/auth'
import eventBus from '@/utils/eventBus'
import webSocketService from '@/utils/websocket'

// 智能数据同步机制
let syncInterval = null
let lastSyncTime = null

// 检查数据更新的函数
const checkForUpdates = async () => {
  try {
    const response = await rescueApi.getRescueList()
    let newData = []
    
    if (response && response.data) {
      if (response.data.records && Array.isArray(response.data.records)) {
        newData = response.data.records
      } else if (Array.isArray(response.data)) {
        newData = response.data
      }
    } else if (Array.isArray(response)) {
      newData = response
    }
    
    // 检查是否有新数据或数据变化
    if (JSON.stringify(newData) !== JSON.stringify(rescueList.value)) {
      console.log('检测到数据变化，更新列表')
      rescueList.value = newData
      updateStats()
    }
    
    lastSyncTime = Date.now()
  } catch (error) {
    console.error('检查数据更新失败:', error)
  }
}

// 启动智能同步
const startSmartSync = () => {
  // 页面获得焦点时同步
  const handleFocus = () => {
    console.log('页面获得焦点，检查数据更新')
    checkForUpdates()
  }
  
  // 定期检查（30秒一次）
  syncInterval = setInterval(checkForUpdates, 30000)
  
  // 监听页面可见性变化
  document.addEventListener('visibilitychange', () => {
    if (!document.hidden) {
      handleFocus()
    }
  })
  
  // 监听页面焦点事件
  window.addEventListener('focus', handleFocus)
  
  return () => {
    if (syncInterval) {
      clearInterval(syncInterval)
      syncInterval = null
    }
    document.removeEventListener('visibilitychange', handleFocus)
    window.removeEventListener('focus', handleFocus)
  }
}

// 手动触发数据同步（在用户操作后调用）
const triggerDataSync = () => {
  console.log('用户操作后触发数据同步')
  checkForUpdates()
}
// 日期格式化函数
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

// 解析图片数据
const parseImages = (imagesData) => {
  if (!imagesData) return []
  
  try {
    // 如果imagesData是字符串，尝试解析为JSON
    if (typeof imagesData === 'string') {
      const parsed = JSON.parse(imagesData)
      // 如果是字符串数组，转换为对象数组
      if (Array.isArray(parsed) && parsed.length > 0 && typeof parsed[0] === 'string') {
        return parsed.map(url => ({ url, name: '图片' }))
      }
      // 如果是对象数组，直接返回
      if (Array.isArray(parsed) && parsed.length > 0 && typeof parsed[0] === 'object') {
        return parsed
      }
    }
    
    // 如果是数组，直接返回
    if (Array.isArray(imagesData)) {
      return imagesData
    }
    
    return []
  } catch (error) {
    console.error('解析图片数据失败:', error)
    return []
  }
}

// 解析救援日志图片数据
const parseRescueLogImages = (imagesData) => {
  if (!imagesData) return []
  
  try {
    // 如果imagesData是字符串，可能是逗号分隔的URL
    if (typeof imagesData === 'string') {
      // 尝试解析为JSON
      try {
        const parsed = JSON.parse(imagesData)
        if (Array.isArray(parsed)) {
          return parsed
        }
      } catch (e) {
        // 如果不是JSON，尝试按逗号分割
        if (imagesData.includes(',')) {
          return imagesData.split(',').map(url => url.trim()).filter(url => url.length > 0)
        }
        // 如果是单个URL
        return [imagesData]
      }
    }
    
    // 如果是数组，直接返回
    if (Array.isArray(imagesData)) {
      return imagesData
    }
    
    return []
  } catch (error) {
    console.error('解析救援日志图片数据失败:', error)
    return []
  }
}

const authStore = useAuthStore()

// 模板引用
const rescueListContainer = ref(null)

// 响应式变量
const rescueList = ref([])
const searchKeyword = ref('')
const filterStatus = ref('')
const filterUrgency = ref('')
const showRescueForm = ref(false)
const showRescueDetail = ref(false)
const selectedRescue = ref(null)
const submitting = ref(false)

// 懒加载相关变量
const visibleCount = ref(8) // 初始显示的卡片数量
const loadingMore = ref(false) // 是否正在加载更多
const hasMore = ref(true) // 是否还有更多数据可以加载

// 表单相关
const rescueForm = ref({
  title: '',
  description: '',
  location: '',
  catCondition: '',
  urgencyLevel: '中',
  contactPhone: '',
  images: [] // 图片列表
})

const rescueRules = {
  title: [
    { required: true, message: '请输入需求标题', trigger: 'blur' },
    { min: 5, max: 50, message: '标题长度在 5 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入需求描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入具体位置', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' }
  ]
}

const rescueFormRef = ref(null)

// 统计数据
const stats = ref({
  totalRescues: 0,
  pendingRescues: 0,
  inProgressRescues: 0,
  completedRescues: 0
})

// 筛选后的救助需求列表
const filteredRescueList = computed(() => {
  // 确保rescueList.value是数组
  if (!Array.isArray(rescueList.value)) {
    console.warn('rescueList.value不是数组:', rescueList.value)
    return []
  }
  
  let list = [...rescueList.value]
  
  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(rescue => 
      rescue && rescue.title && rescue.title.toLowerCase().includes(keyword) || 
      rescue && rescue.description && rescue.description.toLowerCase().includes(keyword) || 
      rescue && rescue.location && rescue.location.toLowerCase().includes(keyword) || 
      (rescue && rescue.reporterUsername && rescue.reporterUsername.toLowerCase().includes(keyword))
    )
  }
  
  // 状态筛选
  if (filterStatus.value) {
    list = list.filter(rescue => rescue && getStatusText(rescue.status) === filterStatus.value)
  }
  
  // 紧急程度筛选
  if (filterUrgency.value) {
    list = list.filter(rescue => rescue && getUrgencyText(rescue.urgencyLevel) === filterUrgency.value)
  }
  
  return list
})

// 可见的救助需求列表（懒加载）
const visibleRescueList = computed(() => {
  return filteredRescueList.value.slice(0, visibleCount.value)
})

// 加载更多卡片
const loadMoreCards = () => {
  if (loadingMore.value || !hasMore.value) return
  
  loadingMore.value = true
  
  // 模拟异步加载
  setTimeout(() => {
    const newVisibleCount = visibleCount.value + 8
    
    // 检查是否还有更多数据可以加载
    if (newVisibleCount >= filteredRescueList.value.length) {
      visibleCount.value = filteredRescueList.value.length
      hasMore.value = false
    } else {
      visibleCount.value = newVisibleCount
    }
    
    loadingMore.value = false
  }, 300)
}

// 滚动监听
const handleScroll = () => {
  const container = rescueListContainer.value
  if (!container) return
  
  const scrollTop = container.scrollTop
  const scrollHeight = container.scrollHeight
  const clientHeight = container.clientHeight
  
  // 当滚动到底部时加载更多
  if (scrollTop + clientHeight >= scrollHeight - 100 && hasMore.value) {
    loadMoreCards()
  }
}

// 获取紧急程度类型
const getUrgencyType = (level) => {
  const types = {
    '低': 'success',
    '中': 'primary',
    '高': 'warning',
    '紧急': 'danger'
  }
  return types[level] || 'primary'
}

// 获取紧急程度文本
const getUrgencyText = (level) => {
  const texts = {
    '低': '低',
    '中': '中',
    '高': '高',
    '紧急': '紧急'
  }
  return texts[level] || '未知'
}

// 获取紧急程度CSS类名
const getUrgencyClass = (level) => {
  const classes = {
    '低': 'urgency-low',
    '中': 'urgency-medium',
    '高': 'urgency-high',
    '紧急': 'urgency-critical'
  }
  return classes[level] || 'urgency-medium'
}

// 获取紧急程度对应的颜色值
const getUrgencyColor = (level) => {
  const colors = {
    '低': '#67C23A', // success 绿色
    '中': '#409EFF', // primary 蓝色
    '高': '#E6A23C', // warning 橙色
    '紧急': '#F56C6C' // danger 红色
  }
  return colors[level] || '#409EFF'
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    '待处理': 'warning',
    '进行中': 'primary',
    '已完成': 'success',
    'PENDING': 'warning',
    'IN_PROGRESS': 'primary',
    'COMPLETED': 'success'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    '待处理': '待处理',
    '进行中': '进行中',
    '已完成': '已完成',
    'PENDING': '待处理',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成'
  }
  return texts[status] || '未知'
}

// 获取状态类名
const getStatusClass = (status) => {
  const classMap = {
    '待处理': 'status-pending',
    '进行中': 'status-in-progress',
    '已完成': 'status-completed',
    'PENDING': 'status-pending',
    'IN_PROGRESS': 'status-in-progress',
    'COMPLETED': 'status-completed'
  }
  return classMap[status] || ''
}

// 重置筛选条件
const resetFilters = () => {
  searchKeyword.value = ''
  filterStatus.value = ''
  filterUrgency.value = ''
  loadRescueList()
}

// 图片上传相关函数
const handleImageChange = (file, fileList) => {
  rescueForm.value.images = fileList
}

const handleImageRemove = (file, fileList) => {
  rescueForm.value.images = fileList
}

const handleExceed = (files, fileList) => {
  ElMessage.warning(`最多只能上传 5 张图片，您选择了 ${files.length} 张图片，共 ${files.length + fileList.length} 张`)
}

const beforeUpload = (file) => {
  const isJPGOrPNG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isJPGOrPNG) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
  }
  return isJPGOrPNG && isLt5M
}

const handleUploadSuccess = (response, file, fileList) => {
  console.log('上传成功:', response, file, fileList)
  ElMessage.success('图片上传成功')
}

const handleUploadError = (error, file, fileList) => {
  console.error('上传失败:', error)
  ElMessage.error('图片上传失败，请重试')
}

const handleRemove = (file, fileList) => {
  console.log('移除图片:', file, fileList)
}

// 加载救助需求列表
const loadRescueList = async () => {
  try {
    console.log('开始加载救助需求列表...')
    
    // 获取所有数据，不进行分页
    const params = {
      page: 1,
      size: 1000 // 设置较大的size值获取所有数据
    }
    
    const response = await rescueApi.getRescueList(params)
    console.log('API返回数据:', response)
    
    // 处理API返回的数据格式
    let data = []
    if (response && response.data) {
      // 处理分页格式的数据
      if (response.data.records && Array.isArray(response.data.records)) {
        data = response.data.records
      } else if (Array.isArray(response.data)) {
        data = response.data
      }
    } else if (Array.isArray(response)) {
      data = response
    }
    
    rescueList.value = data
    console.log('救助需求列表已设置，数量:', rescueList.value.length)
    updateStats()
  } catch (error) {
    console.error('加载救助需求列表失败:', error)
    ElMessage.error('加载失败，请重试')
  }
}

// 更新统计数据
const updateStats = () => {
  stats.value = {
    totalRescues: rescueList.value.length,
    pendingRescues: rescueList.value.filter(r => r.status === 'PENDING' || r.status === '待处理').length,
    inProgressRescues: rescueList.value.filter(r => r.status === 'IN_PROGRESS' || r.status === '进行中').length,
    completedRescues: rescueList.value.filter(r => r.status === 'COMPLETED' || r.status === '已完成').length
  }
}

// 提交救助需求表单
const submitRescueForm = async () => {
  try {
    // 检查用户是否已登录
    if (!authStore.isAuthenticated || !authStore.user?.id) {
      ElMessage.error('请先登录后再发布救助需求')
      return
    }
    
    await rescueFormRef.value.validate()
    submitting.value = true
    
    // 转换紧急程度为英文，确保与后端一致
    const urgencyLevelMap = {
      '低': 'LOW',
      '中': 'MEDIUM', 
      '高': 'HIGH',
      '紧急': 'URGENT'
    }
    
    // 处理图片数据
    const imageUrls = []
    
    // 上传所有图片
    for (const file of rescueForm.value.images) {
      if (file.raw) {
        const response = await authApi.uploadFile(file.raw)
        imageUrls.push(response.data.fileUrl)
      }
    }
    
    // 设置图片数据
    const requestData = {
      title: rescueForm.value.title,
      description: rescueForm.value.description,
      location: rescueForm.value.location,
      catCondition: rescueForm.value.catCondition,
      urgencyLevel: rescueForm.value.urgencyLevel, // 直接使用中文，后端会处理映射
      contactPhone: rescueForm.value.contactPhone,
      images: JSON.stringify(imageUrls), // 转换为JSON字符串格式
      status: '待处理', // 使用中文状态
      reporterId: authStore.user.id // 必须提供reporterId，数据库字段没有默认值
    }
    
    const data = await rescueApi.createRescue(requestData)
    
    // 重新加载完整的数据，确保显示正确的信息
    await loadRescueList()
    ElMessage.success('救助需求发布成功')
    showRescueForm.value = false
    
    // 重置表单
    rescueForm.value = {
      title: '',
      description: '',
      location: '',
      catCondition: '',
      urgencyLevel: '中',
      contactPhone: '',
      images: []
    }
    
    // 更新统计数据
    updateStats()
    // WebSocket会自动通知其他客户端，无需手动触发
    
  } catch (error) {
    console.error('发布救助需求发布失败:', error)
    console.error('错误详情:', error.response?.data)
    console.error('错误状态码:', error.response?.status)
    console.error('错误响应头:', error.response?.headers)
    console.error('请求数据:', {
      title: rescueForm.value.title,
      description: rescueForm.value.description,
      location: rescueForm.value.location,
      catCondition: rescueForm.value.catCondition,
      urgencyLevel: rescueForm.value.urgencyLevel,
      contactPhone: rescueForm.value.contactPhone,
      images: rescueForm.value.images,
      reporterId: authStore.user.id
    })
    ElMessage.error(error.response?.data?.message || '发布失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 查看救助详情
const viewRescueDetail = (rescue) => {
  console.log('点击查看详情:', rescue)
  selectedRescue.value = rescue
  showRescueDetail.value = true
  console.log('对话框状态:', showRescueDetail.value)
}

// 撤销救助需求
const cancelRescue = async (rescue) => {
  try {
    // 检查状态是否为待处理，只有待处理状态才能撤销
    if (rescue.status !== 'PENDING' && rescue.status !== '待处理') {
      ElMessage.warning('只有"待处理"状态的救助需求才能撤销')
      return
    }
    
    await ElMessageBox.confirm(
      `确定要撤销"${rescue.title}"的救助需求吗？此操作不可恢复。`,
      '确认撤销',
      {
        confirmButtonText: '确定撤销',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await rescueApi.deleteRescue(rescue.id)
    
    // 从列表中移除
    rescueList.value = rescueList.value.filter(r => r.id !== rescue.id)
    
    ElMessage.success('救助需求已撤销')
    
    // 更新统计数据
    updateStats()
    // WebSocket会自动通知其他客户端，无需手动触发
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤销救助需求失败:', error)
      ElMessage.error('撤销失败，请重试')
    }
  }
}

onMounted(async () => {
  loadRescueList()
  
  // 设置滚动监听
  setTimeout(() => {
    if (rescueListContainer.value) {
      rescueListContainer.value.addEventListener('scroll', handleScroll)
    }
  }, 100)
  
  try {
    // 连接WebSocket
    await webSocketService.connect()
    
    // 添加消息处理器
    const handlerId = webSocketService.addMessageHandler((message) => {
      console.log('收到WebSocket数据更新通知:', message)
      
      // 只在收到救助数据更新通知时才重新加载数据
      if (message === 'RESCUE_DATA_UPDATED') {
        console.log('重新加载救助数据')
        loadRescueList()
        
        // 小红点实时更新 - 通过事件总线通知导航栏组件
        eventBus.emit('rescueStatusChanged')
      }
    })
    
    console.log('WebSocket监听已设置')
    
    // 组件卸载时移除消息处理器并断开连接
    onUnmounted(() => {
      webSocketService.removeMessageHandler(handlerId)
      webSocketService.disconnect()
      
      // 移除滚动监听
      if (rescueListContainer.value) {
        rescueListContainer.value.removeEventListener('scroll', handleScroll)
      }
    })
  } catch (error) {
    console.error('WebSocket连接失败:', error)
    console.log('WebSocket连接失败，将使用手动刷新方式更新数据')
    
    // 不再使用轮询，只在用户手动操作时更新数据
    // 这样可以大大减少系统资源消耗
  }
})

// 预览图片
const previewImage = (imageUrl) => {
  // 使用Element Plus的图片预览功能
  // 这里不需要额外实现，因为el-image组件已经内置了预览功能
  console.log('预览图片:', imageUrl)
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

.rescue-container {
  padding: 20px;
}

/* 页面头部样式 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.page-title {
  font-size: 2em;
  font-weight: 700;
  margin: 0;
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
  flex-shrink: 0;
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
  background: rgba(32, 224, 185, 0.3);
}

.stat-card.pending {
  background: rgba(231, 58, 46, 0.6);
}

.stat-card.in-progress {
  background: rgba(20, 196, 240, 0.3);
}

.stat-card.completed {
  background: rgba(4, 246, 85, 0.3);
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

/* 筛选区域样式 */
.filter-section {
  margin-bottom: 16px;
}

.filter-section .el-card {
  border: none;
}

/* 按钮区域样式 */
.action-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: flex-start;
}

.rescue-list {
  margin-top: 5px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 8px;
}

/* 加载更多指示器 */
.loading-more {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}

.loading-more .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

/* 没有更多数据提示 */
.no-more-data {
  grid-column: 1 / -1;
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}

.rescue-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  border-left: 6px solid #dcdfe6;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  position: relative;
  z-index: 1;
  border: 1px solid #e0e0e0;
  animation: fadeInUp 0.6s ease;
}

.rescue-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
  border-color: #d0d0d0;
}

/* 根据紧急程度设置卡片颜色主题 */
.rescue-card.urgency-low {
  border-left-color: #67c23a ;
  background: linear-gradient(135deg, #f6ffed 0%, #ffffff 100%);
}

.rescue-card.urgency-medium {
  border-left-color: #409eff ;
  background: linear-gradient(135deg, #ecf5ff 0%, #ffffff 100%);
}

.rescue-card.urgency-high {
  border-left-color: #e6a23c ;
  background: linear-gradient(135deg, #fff9e6 0%, #ffffff 100%);
}

.rescue-card.urgency-critical {
  border-left-color: #f56c6c ;
  background: linear-gradient(135deg, #fef0f0 0%, #ffffff 100%);
}


.rescue-card-header {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #a69d9d;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.header-tags {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 14px;
  font-weight: 500;
}

.header-bottom {
  display: flex;
  align-items: center;
}

.rescue-title {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  line-height: 1.4;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  max-height: calc(1.4em * 1); /* 确保不超过一行高度 */
}

.rescue-content {
  margin-top: 20px;
}

.rescue-description {
  margin-bottom: 20px;
  color: #606266;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  max-height: calc(1.3em * 2); /* 确保不超过两行高度 */
  font-size: 15px;
  background: #f8f9fa;
  padding: 16px;
  border-radius: 12px;
  border-left: 4px solid;
}

.rescue-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
  padding: 12px 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.info-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.cat-condition {
  margin-top: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 12px;
  border-left: 4px solid;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.cat-condition:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.cat-condition h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}

.cat-condition p {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  max-height: calc(1.6em * 1); /* 确保不超过一行高度 */
}

/* 已完成标识 */
.completed-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;
  border-radius: 16px;
}

.completed-icon {
  width: 192px;
  height: 192px;
  filter: brightness(0) saturate(100%) invert(44%) sepia(98%) saturate(385%) hue-rotate(75deg) brightness(95%) contrast(90%);
}

.rescue-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  padding-top: 20px;
  border-top: 1px solid #a69d9d;
}

.empty-state {
  display: flex ;
  flex-direction: column ;
  justify-content: center;
  align-items: center ;
  min-height: 300px ;
  text-align: center;
  padding: 50px 0 ;
}

.empty-actions {
  margin-top: 20px;
}

.rescue-detail {
  padding: 20px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #a69d9d;
}

.detail-tags {
  display: flex;
  gap: 12px;
}

.detail-time {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
  font-weight: 500;
}

.detail-section {
  margin-bottom: 30px;
}

.detail-section h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  border-left: 4px solid #67C23A;
  padding-left: 15px;
}

.detail-section p {
  margin: 0;
  color: #606266;
  line-height: 1.7;
  font-size: 16px;
  background: #f8f9fa;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.contact-info {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.contact-info .info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 15px;
  font-weight: 500;
  padding: 12px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.contact-info .info-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 联系信息水平布局样式 */
.contact-row {
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

.contact-column {
  flex: 1;
  min-width: 0;
}

/* 救援日志图片样式 - 与相关图片保持一致 */
.rescue-log-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 16px;
  margin-top: 15px;
}

.log-image-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.log-image-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.log-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.log-image:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #c0c4cc;
  font-size: 12px;
}

.image-error .el-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.contact-column h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  border-left: 4px solid #67C23A;
  padding-left: 15px;
}

.contact-info .info-item .el-icon {
  color: #67C23A;
  font-size: 18px;
}

@media (max-width: 768px) {
  .rescue-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .rescue-info {
    flex-direction: column;
    gap: 10px;
  }
  
  .rescue-actions {
    flex-direction: column;
  }
  
  .detail-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .detail-tags {
    flex-wrap: wrap;
  }
  
  .contact-info {
    flex-direction: column;
    gap: 10px;
  }
  
  .contact-row {
    flex-direction: column;
    gap: 20px;
  }
  
  .contact-column {
    flex: none;
  }
}

/* 图片展示样式 */
.image-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 16px;
  margin-top: 15px;
}

.image-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.image-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.detail-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.detail-image:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 12px;
}

.image-error .el-icon {
  font-size: 24px;
  margin-bottom: 5px;
}

/* 响应式图片布局 */
@media (max-width: 768px) {
  .image-gallery {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 10px;
  }
  
  .detail-image {
    height: 120px;
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .rescue-list {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 768px) {
  .rescue-container {
    padding: 16px;
  }
  
  .rescue-list {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .rescue-card {
    padding: 20px;
    border-radius: 12px;
  }
  
  .header-top {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .header-time {
    align-self: flex-end;
  }
  
  .rescue-title {
    font-size: 18px;
  }
  
  .rescue-info {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .info-item {
    padding: 10px 12px;
  }
  
  .rescue-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .rescue-actions .el-button {
    width: 100%;
    margin-bottom: 8px;
  }
}

/* 救助需求详情对话框样式 */
.rescue-detail-dialog {
  max-height: 700px ;
  overflow: hidden ;
}

.rescue-detail-dialog .el-dialog__body {
  padding: 0 ;
  overflow: hidden ;
  max-height: 600px ;
}


.el-card {
  --el-card-border-radius: 10px;
  --el-card-padding: 20px 20px 10px 20px;
  --el-card-border-color:#a69d9d;
}

</style>