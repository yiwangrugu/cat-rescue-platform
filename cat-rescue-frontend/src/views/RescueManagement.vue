<template>
  <div class="rescue-management">
    <div class="management-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Help /></el-icon>
          救助任务管理
        </h1>
        <p class="page-subtitle">管理和处理救助任务</p>
      </div>
      <div class="header-stats">
        <div class="stat-card">
          <div class="stat-number">{{ pendingCount || 0 }}</div>
          <div class="stat-label">待处理</div>
        </div>
        <div class="stat-card in-progress">
          <div class="stat-number">{{ inProgressCount || 0 }}</div>
          <div class="stat-label">进行中</div>
        </div>
        <div class="stat-card completed">
          <div class="stat-number">{{ completedCount || 0 }}</div>
          <div class="stat-label">已完成</div>
        </div>
        <div class="stat-card urgent">
          <div class="stat-number">{{ urgentCount || 0 }}</div>
          <div class="stat-label">紧急任务</div>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filters-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索任务标题或位置"
            prefix-icon="Search"
            clearable
          />
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterStatus" placeholder="状态筛选" clearable>
            <el-option label="待处理" value="PENDING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterUrgency" placeholder="紧急程度" clearable>
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="CRITICAL" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadRescues">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetFilters">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 救助任务列表 -->
    <div class="rescues-section">
      <el-table :data="filteredRescues" style="width: 100%" v-loading="loading" @row-click="handleRowClick" :empty-text="emptyText">
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column label="任务信息" width="300" align="center">
          <template #default="{ row }">
            <div class="rescue-info">
              <div class="rescue-title">
                <span class="title-text">{{ row.title }}</span>
                <el-tag v-if="row.urgencyLevel === 'HIGH'" size="small" type="danger">紧急</el-tag>
                <el-tag v-if="row.urgencyLevel === 'CRITICAL'" size="small" type="danger">非常紧急</el-tag>
              </div>
              <div class="rescue-description">{{ row.description || '暂无描述' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="位置信息" width="180" align="center">
          <template #default="{ row }">
            <div class="location-info">
              <el-icon><Location /></el-icon>
              <span>{{ row.location }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="reporterUsername" label="报告人" width="120" align="center" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" align="center" />
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="报告时间" width="160" align="center">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="volunteerName" label="志愿者" width="120" align="center">
          <template #default="{ row }">
            {{ row.volunteerName || '待定' }}
          </template>
        </el-table-column>
        <el-table-column prop="volunteerPhone" label="志愿者电话" width="140" align="center">
          <template #default="{ row }">
            {{ row.volunteerPhone || '待定' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <div style="position: relative; display: inline-block;">
              <el-button size="small" @click="viewDetail(row)">查看详情</el-button>
              <div 
                v-if="row.status === '待处理' || row.status === 'PENDING'" 
                class="status-dot"
                style="position: absolute; top: -1px; right: 6px; width: 13px; height: 13px; background-color: #f56c6c; border-radius: 50%; border: 2px solid #fff; z-index: 10;"
              ></div>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 更新进度对话框 -->
    <el-dialog title="更新救助进度" v-model="showProgressDialog" width="600px">
      <el-form :model="progressForm" :rules="progressRules" ref="progressFormRef" label-width="100px">
        <el-form-item label="当前进度" prop="progress">
          <el-slider v-model="progressForm.progress" :min="0" :max="100" :step="10" show-stops />
          <div style="text-align: center; margin-top: 10px;">
            <span>{{ progressForm.progress }}%</span>
          </div>
        </el-form-item>
        <el-form-item label="进度说明" prop="progressNote">
          <el-input 
            type="textarea" 
            v-model="progressForm.progressNote" 
            :rows="4" 
            placeholder="请详细描述当前救助进度和情况"
          />
        </el-form-item>
        <el-form-item label="上传照片">
          <el-upload
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :file-list="progressForm.images"
            :on-change="handleImageChange"
            :on-remove="handleImageRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showProgressDialog = false">取消</el-button>
        <el-button type="primary" @click="submitProgress" :loading="updating">保存进度</el-button>
      </div>
    </el-dialog>



    <!-- 救助需求详情对话框 -->
    <RescueDetailDialog
      v-model:visible="showRescueDetail"
      :rescue-data="selectedRescue"
      @take-rescue="takeRescue"
      @complete-rescue="completeRescue"
      @close-rescue="closeRescue"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Plus, Help, Search, Refresh } from '@element-plus/icons-vue'
import { rescueApi } from '@/api/rescue'
import { volunteerApi } from '@/api/volunteer'
import { adminApi } from '@/api/admin'
import { useAuthStore } from '@/stores/auth'
import webSocketService from '@/utils/websocket'
import RescueDetailDialog from '@/components/RescueDetailDialog.vue'

const router = useRouter()
const authStore = useAuthStore()

const rescues = ref([]) // 当前页数据
const allRescues = ref([]) // 全部数据用于统计
const loading = ref(false)
const searchKeyword = ref('')
const filterStatus = ref('')
const filterUrgency = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showProgressDialog = ref(false)
const updating = ref(false)
const currentRescue = ref(null)
const showRescueDetail = ref(false)
const selectedRescue = ref(null)
const showTakeRescueDialog = ref(false)
const taking = ref(false)

const emptyText = ref('暂无救助任务')

// 志愿者相关数据
const showVolunteerManagement = ref(false)
const showCreateVolunteer = ref(false)
const volunteerList = ref([])
const takeRescueForm = ref({
  selectedVolunteerId: null,
  volunteerName: '',
  volunteerPhone: ''
})

const takeRescueRules = {
  volunteerName: [
    { required: true, message: '请输入志愿者姓名', trigger: 'blur' }
  ],
  volunteerPhone: [
    { required: true, message: '请输入志愿者电话', trigger: 'blur' }
  ]
}

// 救援完成相关数据
const showCompleteDialog = ref(false)
const completing = ref(false)
const rescueLogImages = ref([])
const completeRescueForm = ref({
    rescueLogImages: []
  })

const takeRescueFormRef = ref(null)

const newVolunteerForm = ref({
  name: '',
  phone: '',
  email: '',
  address: ''
})

const volunteerRules = {
  name: [
    { required: true, message: '请输入志愿者姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入志愿者电话', trigger: 'blur' }
  ]
}

const volunteerFormRef = ref(null)

const progressForm = ref({
  progress: 0,
  progressNote: '',
  images: []
})

const progressRules = {
  progressNote: [
    { required: true, message: '请输入进度说明', trigger: 'blur' }
  ]
}



const pendingCount = computed(() => {
  return allRescues.value.filter(r => r.status === 'PENDING' || r.status === '待处理').length
})

const inProgressCount = computed(() => {
  return allRescues.value.filter(r => r.status === 'IN_PROGRESS' || r.status === '进行中').length
})

const completedCount = computed(() => {
  return allRescues.value.filter(r => r.status === 'COMPLETED' || r.status === '已完成').length
})

const filteredRescues = computed(() => {
  return rescues.value.filter(rescue => {
    const matchesSearch = !searchKeyword.value || 
      rescue.title.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      rescue.location.toLowerCase().includes(searchKeyword.value.toLowerCase())
    
    let matchesStatus = true
    if (filterStatus.value) {
      if (filterStatus.value === 'PENDING') {
        matchesStatus = rescue.status === 'PENDING' || rescue.status === '待处理'
      } else if (filterStatus.value === 'IN_PROGRESS') {
        matchesStatus = rescue.status === 'IN_PROGRESS' || rescue.status === '进行中'
      } else if (filterStatus.value === 'COMPLETED') {
        matchesStatus = rescue.status === 'COMPLETED' || rescue.status === '已完成'
      } else if (filterStatus.value === 'CANCELLED') {
        matchesStatus = rescue.status === 'CANCELLED' || rescue.status === '已取消'
      } else {
        matchesStatus = rescue.status === filterStatus.value
      }
    }
    
    let matchesUrgency = true
    if (filterUrgency.value) {
      if (filterUrgency.value === 'LOW') {
        matchesUrgency = rescue.urgencyLevel === 'LOW' || rescue.urgencyLevel === '低'
      } else if (filterUrgency.value === 'MEDIUM') {
        matchesUrgency = rescue.urgencyLevel === 'MEDIUM' || rescue.urgencyLevel === '中'
      } else if (filterUrgency.value === 'HIGH') {
        matchesUrgency = rescue.urgencyLevel === 'HIGH' || rescue.urgencyLevel === '高'
      } else if (filterUrgency.value === 'CRITICAL') {
        matchesUrgency = rescue.urgencyLevel === 'CRITICAL' || rescue.urgencyLevel === '紧急'
      } else {
        matchesUrgency = rescue.urgencyLevel === filterUrgency.value
      }
    }
    
    return matchesSearch && matchesStatus && matchesUrgency
  })
})

const urgentCount = computed(() => {
  return allRescues.value.filter(r => 
    r.urgencyLevel === 'HIGH' || r.urgencyLevel === 'CRITICAL' || 
    r.urgencyLevel === '高' || r.urgencyLevel === '紧急'
  ).length
})

// 判断是否是管理端
const isAdminPage = computed(() => {
  return window.location.pathname.includes('/admin')
})

// 加载全部数据用于统计
const loadAllRescuesForStats = async () => {
  try {
    const params = {
      page: 1,
      size: 1000 // 获取全部数据用于统计
    }

    const config = {
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    }

    let response
    if (isAdminPage.value) {
      response = await adminApi.getPendingRescues(1, 1000)
    } else {
      response = await rescueApi.getRescueList(params, config)
    }
    
    const data = response.data

    // 后端已返回中文状态，直接使用
    let records = isAdminPage.value ? (data.data || []) : (data.records || [])

    allRescues.value = records
    console.log('全部救助数据已加载，数量:', allRescues.value.length)

  } catch (error) {
    console.error('加载全部救助数据失败:', error)
  }
}

const loadRescues = async () => {
  try {
    loading.value = true

    // 检查认证状态
    if (!authStore.isAuthenticated) {
      ElMessage.error('请先登录')
      router.push('/rescuer/login')
      return
    }

    console.log('当前用户信息:', authStore.user)

    const params = {
      page: currentPage.value,
      size: pageSize.value
    }

    console.log('开始加载救助任务，参数:', params)

    // 添加认证头信息
    const config = {
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    }

    let response
    if (isAdminPage.value) {
      response = await adminApi.getPendingRescues(currentPage.value, pageSize.value)
    } else {
      response = await rescueApi.getRescueList(params, config)
    }
    console.log('获取到响应:', response)

    const data = response.data
    console.log('响应数据:', data)

    // 后端已返回中文状态，直接使用
    let records = isAdminPage.value ? (data.data || []) : (data.records || [])
    console.log('原始救助数据:', records)

    records = records.map(rescue => {
      console.log('处理救助需求:', rescue.id, '状态:', rescue.status, '紧急程度:', rescue.urgencyLevel)

      // 后端已返回中文状态，无需映射
      console.log('状态:', rescue.status)

      // 后端已返回中文紧急程度，无需映射
      console.log('紧急程度:', rescue.urgencyLevel)

      console.log('处理后救助需求:', rescue.id, '状态:', rescue.status, '紧急程度:', rescue.urgencyLevel)
      return rescue
    })

    rescues.value = records
    total.value = data.total || 0
    console.log('救助任务列表:', rescues.value)

    // 同时加载全部数据用于统计
    await loadAllRescuesForStats()

  } catch (error) {
    console.error('加载救助任务失败:', error)
    console.error('错误详情:', error.response?.data)
    console.error('错误状态码:', error.response?.status)
    console.error('错误响应头:', error.response?.headers)

    if (error.response?.status === 403) {
      ElMessage.error('权限不足，请检查您的登录状态或联系管理员')
      // 尝试重新登录
      if (authStore.isAuthenticated) {
        ElMessage.warning('检测到认证问题，正在尝试重新登录...')
        authStore.logout()
        router.push('/rescuer/login')
      }
    } else {
      ElMessage.error('加载救助任务失败')
    }
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  searchKeyword.value = ''
  filterStatus.value = ''
  filterUrgency.value = ''
  currentPage.value = 1
  loadRescues()
}

const getStatusText = (status) => {
  const texts = {
    'PENDING': '待处理',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    '待处理': '待处理',
    '进行中': '进行中',
    '已完成': '已完成'
  }
  return texts[status] || status
}

const getStatusType = (status) => {
  const types = {
    'PENDING': 'danger',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'info',
    '待处理': 'danger',
    '进行中': 'warning',
    '已完成': 'success'
  }
  return types[status] || 'info'
}

const getUrgencyType = (level) => {
  const types = {
    'LOW': 'success',
    'MEDIUM': 'info',
    'HIGH': 'warning',
    'CRITICAL': 'danger',
    '低': 'success',
    '中': 'info',
    '高': 'warning',
    '紧急': 'danger'
  }
  return types[level] || 'info'
}

const getUrgencyText = (level) => {
  const texts = {
    'LOW': '低',
    'MEDIUM': '中',
    'HIGH': '高',
    'CRITICAL': '紧急',
    '低': '低',
    '中': '中',
    '高': '高',
    '紧急': '紧急'
  }
  return texts[level] || '未知'
}

const formatDate = (date) => {
  if (!date) return ''
  const dateObj = new Date(date)
  return dateObj.toLocaleString('zh-CN', {
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

const viewDetail = (rescue) => {
  selectedRescue.value = rescue
  showRescueDetail.value = true
}

// 处理行点击事件
const handleRowClick = (row, column, event) => {
  // 如果点击的是操作列（查看详情按钮），则不处理行点击
  if (column && column.property === '操作') {
    return
  }
  // 如果点击的是小红点区域，则不处理行点击
  if (event.target.closest('.status-dot')) {
    return
  }
  viewDetail(row)
}

// 加载志愿者列表
const loadVolunteers = async () => {
  try {
    const response = await volunteerApi.getVolunteerList()
    volunteerList.value = response.data
  } catch (error) {
    console.error('加载志愿者列表失败:', error)
    ElMessage.error('加载志愿者列表失败')
  }
}

// 选择志愿者事件
const handleVolunteerSelect = (volunteerId) => {
  if (volunteerId) {
    const selectedVolunteer = volunteerList.value.find(v => v.id === volunteerId)
    if (selectedVolunteer) {
      takeRescueForm.value.volunteerName = selectedVolunteer.name
      takeRescueForm.value.volunteerPhone = selectedVolunteer.phone
    }
  } else {
    takeRescueForm.value.volunteerName = ''
    takeRescueForm.value.volunteerPhone = ''
  }
}

// 选择志愿者（从管理对话框）
const selectVolunteer = (volunteer) => {
  takeRescueForm.value.selectedVolunteerId = volunteer.id
  takeRescueForm.value.volunteerName = volunteer.name
  takeRescueForm.value.volunteerPhone = volunteer.phone
  showVolunteerManagement.value = false
}

// 创建志愿者
const createVolunteer = async () => {
  try {
    await volunteerFormRef.value.validate()
    await volunteerApi.createVolunteer(newVolunteerForm.value)
    ElMessage.success('志愿者创建成功')
    showCreateVolunteer.value = false
    newVolunteerForm.value = { name: '', phone: '', email: '', address: '' }
    await loadVolunteers()
  } catch (error) {
    console.error('创建志愿者失败:', error)
    ElMessage.error('创建志愿者失败')
  }
}

// 删除志愿者
const deleteVolunteer = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个志愿者吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await volunteerApi.deleteVolunteer(id)
    ElMessage.success('志愿者删除成功')
    await loadVolunteers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除志愿者失败')
    }
  }
}

const takeRescue = (rescue) => {
  console.log('马上救援按钮被点击，救助需求:', rescue)
  console.log('救助需求状态:', rescue.status)
  
  currentRescue.value = rescue
  showTakeRescueDialog.value = true
  // 关闭详情对话框
  showRescueDetail.value = false
  
  console.log('马上救援对话框已打开')
}

const submitTakeRescue = async () => {
  try {
    await takeRescueFormRef.value.validate()
    taking.value = true
    
    const rescueId = currentRescue.value.id
    // 使用当前登录用户的ID作为负责人ID
    const authStore = useAuthStore()
    const rescuerId = authStore.user?.id
    
    const requestData = {
      rescuerId: rescuerId,
      volunteerName: takeRescueForm.value.volunteerName,
      volunteerPhone: takeRescueForm.value.volunteerPhone
    }
    
    console.log('马上救援请求数据:', requestData)
    console.log('救助需求ID:', rescueId)
    const response = await rescueApi.takeRescueWithVolunteer(rescueId, requestData)
    console.log('马上救援响应:', response)
    console.log('响应状态码:', response.status)
    console.log('响应数据:', response.data)
    
    ElMessage.success('马上救援成功，请尽快处理')
    showTakeRescueDialog.value = false
    // 重置表单
    takeRescueForm.value = {
      selectedVolunteerId: null,
      volunteerName: '',
      volunteerPhone: ''
    }
    // 重新加载数据以更新状态
    await loadRescues()
    // 更新当前选中的救助需求
    if (currentRescue.value) {
      const updatedRescue = rescues.value.find(r => r.id === currentRescue.value.id)
      if (updatedRescue) {
        selectedRescue.value = updatedRescue
      }
    }
    // WebSocket会自动通知其他客户端，无需手动触发
  } catch (error) {
    console.error('马上救援失败:', error)
    console.error('错误详情:', error.response?.data)
    console.error('错误状态码:', error.response?.status)
    ElMessage.error(error.response?.data?.message || '马上救援失败')
  } finally {
    taking.value = false
  }
}

const completeRescue = async (rescue) => {
  // 设置当前救援任务并打开完成弹窗
  currentRescue.value = rescue
  showCompleteDialog.value = true
}

const handleCompleteRescue = async () => {
  if (!currentRescue.value) return
  
  try {
    completing.value = true
    
    const formData = new FormData()
    
    // 添加救援日志图片
    if (rescueLogImages.value.length > 0) {
      rescueLogImages.value.forEach((file, index) => {
        formData.append('rescueLogImages', file.raw)
      })
    }
    
    // 添加认证头信息
    const config = {
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    }
    
    const response = await rescueApi.completeRescueWithImages(currentRescue.value.id, formData, config)
    
    console.log('救援完成响应:', response)
    console.log('响应状态码:', response.status)
    console.log('响应数据:', response.data)
    
    // 后端直接返回救援对象，检查响应状态码
    if (response.status === 200) {
      ElMessage.success('救援任务已完成')
      
      // 更新本地数据
      const index = rescues.value.findIndex(r => r.id === currentRescue.value.id)
      if (index !== -1) {
        rescues.value[index].status = '已完成'
        rescues.value[index].completeTime = new Date().toISOString()
        // 如果有救援日志图片，更新到本地数据
        if (response.data && response.data.rescueLogImages) {
          rescues.value[index].rescueLogImages = response.data.rescueLogImages
        }
      }
      
      // 关闭弹窗
      showCompleteDialog.value = false
      
      // 重新加载数据
      loadRescues()
      
      // 发送WebSocket通知
      webSocketService.send('RESCUE_DATA_UPDATED')
    } else {
      ElMessage.error('操作失败：' + (response.data?.message || '未知错误'))
    }
  } catch (error) {
    console.error('完成救援失败:', error)
    ElMessage.error('操作失败')
  } finally {
    completing.value = false
  }
}

const updateProgress = (rescue) => {
  currentRescue.value = rescue
  progressForm.value = {
    progress: rescue.progress || 0,
    progressNote: '',
    images: []
  }
  showProgressDialog.value = true
}

const submitProgress = async () => {
  try {
    updating.value = true
    // TODO: 调用API更新进度
    ElMessage.success('进度更新成功')
    showProgressDialog.value = false
    loadRescues()
  } catch (error) {
    ElMessage.error('进度更新失败')
  } finally {
    updating.value = false
  }
}

const cancelRescue = async (rescue) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消"${rescue.title}"的救助吗？`,
      '确认取消',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    // TODO: 调用API取消救助
    ElMessage.success('救助任务已取消')
    loadRescues()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

const handleImageChange = (file) => {
  progressForm.value.images.push(file)
}

const handleImageRemove = (file) => {
  const index = progressForm.value.images.indexOf(file)
  if (index > -1) {
    progressForm.value.images.splice(index, 1)
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadRescues()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadRescues()
}

// WebSocket消息处理器ID
let wsHandlerId = null

// 组件卸载时移除消息处理器并断开连接
onUnmounted(() => {
  if (wsHandlerId) {
    webSocketService.removeMessageHandler(wsHandlerId)
    wsHandlerId = null
  }
  webSocketService.disconnect()
})

onMounted(async () => {
  loadRescues()
  loadVolunteers()
  
  try {
    // 连接WebSocket
    await webSocketService.connect()
    
    // 添加消息处理器
    wsHandlerId = webSocketService.addMessageHandler((message) => {
      console.log('收到WebSocket数据更新通知:', message)
      
      // 只在收到救助数据更新通知时才重新加载数据
      if (message === 'RESCUE_DATA_UPDATED') {
        console.log('重新加载救助数据')
        loadRescues()
      }
      
    })
    
    console.log('WebSocket监听已设置')
  } catch (error) {
    console.error('WebSocket连接失败:', error)
    console.log('WebSocket连接失败，将使用手动刷新方式更新数据')
    
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

.rescue-management {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}

/* 头部管理区域 */
.management-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 32px;
  border-radius: 16px;
  margin-bottom: 30px;
  box-shadow: 0 8px 32px rgba(103, 194, 58, 0.3);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-subtitle {
  font-size: 14px;
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

.stat-card.in-progress {
  background: rgba(40, 167, 69, 0.3);
}

.stat-card.completed {
  background: rgba(40, 167, 69, 0.3);
}

.stat-card.urgent {
  background: rgba(255, 87, 34, 0.3);
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

/* 筛选和搜索区域 */
.filters-section {
  background: white;
  padding: 24px;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.filters-section .el-row {
  align-items: center;
}

.filters-section .el-input {
  width: 100%;
}

.filters-section .el-input__wrapper {
  border-radius: 12px;
  padding: 8px 16px;
}

.filters-section .el-select .el-input__wrapper {
  border-radius: 12px;
}

.filters-section .el-button {
  border-radius: 12px;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.filters-section .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 救助任务列表 */
.rescues-section {
  background: white;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  animation: fadeInUp 0.6s ease;
}

.rescues-section .el-table {
  border-radius: 16px;
  overflow: hidden;
}

.rescues-section .el-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  padding: 16px;
  border-bottom: 2px solid #e4e7ed;
}

.rescues-section .el-table td {
  padding: 16px;
  font-size: 14px;
  color: #606266;
  border-bottom: 1px solid #ebeef5;
}

.rescues-section .el-table tr:hover {
  background-color: #f5f7fa;
}

.rescues-section .el-table tr {
  transition: all 0.3s ease;
}

/* 任务信息样式 */
.rescue-info {
  line-height: 1.5;
}

.rescue-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.title-text {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rescue-description {
  color: #606266;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 4px;
}

/* 位置信息样式 */
.location-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 14px;
}

/* 状态标签样式 */
.rescues-section .el-tag {
  border-radius: 12px;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 600;
}

/* 操作按钮样式 */
.rescues-section .el-button {
  border-radius: 8px;
  padding: 6px 16px;
  font-size: 12px;
  font-weight: 600;
  margin-right: 8px;
  transition: all 0.3s ease;
}

.rescues-section .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 空状态提示样式 */
.rescues-section .el-table__empty-block {
  display: flex ;
  justify-content: center ;
  align-items: center ;
  height: 200px ;
  min-height: 200px ;
}

.rescues-section .el-table__empty-text {
  font-size: 16px ;
  color: #909399 ;
  text-align: center ;
  line-height: 1.5 ;
  margin: 0 ;
}

/* 分页区域 */
.pagination-section {
  display: flex;
  justify-content: center;
  padding: 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  animation: fadeInUp 0.6s ease 0.6s both;
}

.pagination-section .el-pagination {
  display: flex;
  align-items: center;
}

.pagination-section .el-pagination__item {
  border-radius: 8px;
  margin: 0 4px;
  transition: all 0.3s ease;
}

.pagination-section .el-pagination__item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.pagination-section .el-pagination__item.is-active {
  background-color: #67C23A;
  border-color: #67C23A;
  color: white;
}

/* 对话框样式 */
.el-dialog {
  border-radius: 16px;
  overflow: hidden;
}

.el-dialog__header {
  background: linear-gradient(135deg, #67C23A 0%, #4CAF50 100%);
  color: white;
  padding: 20px;
}

.el-dialog__title {
  color: white;
  font-size: 18px;
  font-weight: bold;
}

.el-dialog__body {
  padding: 24px;
}

.el-dialog__footer {
  padding: 20px;
  background-color: #f5f7fa;
  border-top: 1px solid #ebeef5;
}

/* 表单样式 */
.el-form-item {
  margin-bottom: 20px;
}

.el-form-item__label {
  font-weight: 600;
  color: #303133;
}

.el-input__wrapper {
  border-radius: 12px;
}

.el-textarea__inner {
  border-radius: 12px;
  min-height: 120px;
}

.el-slider {
  margin: 20px 0;
}

/* 上传组件样式 */
.el-upload {
  margin-top: 10px;
}

.el-upload-list__item {
  border-radius: 8px;
}

/* 操作按钮样式 */
.operation-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

/* 动画效果 */
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

/* 响应式设计 */
@media (max-width: 1200px) {
  .rescue-management {
    padding: 15px;
  }
  
  .management-header {
    padding: 20px;
  }
  
  .management-header h1 {
    font-size: 32px;
  }
  
  .header-stats {
    gap: 12px;
  }
  
  .header-stats .el-tag {
    padding: 8px 16px;
    font-size: 14px;
  }
  
  .filters-section {
    padding: 20px;
  }
  
  .rescues-section .el-table th,
  .rescues-section .el-table td {
    padding: 12px;
  }
}

@media (max-width: 768px) {
  .management-header h1 {
    font-size: 28px;
  }
  
  .header-stats {
    flex-direction: column;
    align-items: center;
  }
  
  .header-stats .el-tag {
    width: 100%;
    text-align: center;
  }
  
  .filters-section .el-row {
    flex-direction: column;
    gap: 12px;
  }
  
  .filters-section .el-col {
    width: 100%;
    max-width: 100%;
  }
  
  .rescues-section .el-table {
    font-size: 12px;
  }
  
  .rescues-section .el-table th,
  .rescues-section .el-table td {
    padding: 8px;
  }
  
  .rescues-section .el-button {
    padding: 4px 12px;
    font-size: 11px;
    margin-right: 4px;
  }
}

/* 详情对话框样式 */
.rescue-detail {
  padding: 20px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
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

.contact-info .info-item .el-icon {
  color: #67C23A;
  font-size: 18px;
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

.contact-column h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  border-left: 4px solid #67C23A;
  padding-left: 15px;
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

/* 详情对话框操作按钮样式 */
.detail-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  align-items: center;
  flex-wrap: wrap;
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
  
  .detail-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .detail-actions .el-button {
    width: 100%;
  }
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

/* 救援完成弹窗样式 */
.complete-rescue-dialog {
  padding: 10px 0;
}

.complete-rescue-dialog p {
  margin-bottom: 16px;
  color: #606266;
}

.upload-tips {
  margin-top: 16px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

/* 救助需求详情对话框样式 */
.rescue-detail-dialog {
  max-height: 700px ;
  overflow: hidden ;
}

.rescue-detail-dialog .el-dialog__body {
  padding: 0 ;
  overflow: hidden ;
  max-height: 550px ;
}
</style>