<template>
  <div class="user-adoptions-container">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Document /></el-icon>
          我的申请
        </h1>
        <p class="page-subtitle">查看和管理您的领养申请记录</p>
      </div>
      <div class="header-stats">
        <div class="stat-card pending">
          <div class="stat-number">{{ pendingCount }}</div>
          <div class="stat-label">待审核</div>
        </div>
        <div class="stat-card approved">
          <div class="stat-number">{{ approvedCount }}</div>
          <div class="stat-label">已通过</div>
        </div>
        <div class="stat-card rejected">
          <div class="stat-number">{{ rejectedCount }}</div>
          <div class="stat-label">已拒绝</div>
        </div>
      </div>
    </div>

    <!-- 筛选 -->
    <div class="filter-section">
      <el-card shadow="never">
        <el-row :gutter="20" align="middle">
          <el-col :span="6">
            <el-select v-model="filterStatus" placeholder="申请状态" clearable @change="loadApplications">
              <el-option label="全部" value="" />
              <el-option label="待审核" value="PENDING">
                <el-tag type="warning" size="small">待审核</el-tag>
              </el-option>
              <el-option label="审核中" value="UNDER_REVIEW">
                <el-tag type="primary" size="small">审核中</el-tag>
              </el-option>
              <el-option label="已通过" value="APPROVED">
                <el-tag type="success" size="small">已通过</el-tag>
              </el-option>
              <el-option label="已拒绝" value="REJECTED">
                <el-tag type="danger" size="small">已拒绝</el-tag>
              </el-option>
            </el-select>
          </el-col>
          <el-col :span="18">
            <el-button type="primary" @click="$router.push('/cats')">
              <el-icon><Plus /></el-icon>
              申请新领养
            </el-button>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 申请列表 -->
    <div class="applications-section">
      <el-card shadow="never" v-loading="loading">
        <el-empty v-if="applications.length === 0" description="暂无领养申请记录">
          <el-button type="primary" @click="$router.push('/cats')">
            去浏览猫咪
          </el-button>
        </el-empty>

        <div v-else class="application-list">
          <div 
            v-for="app in applications" 
            :key="app.id" 
            class="application-card"
            :class="getStatusClass(app.status)"
            @click="handleCardClick(app, $event)"
          >
            <div class="card-header">
              <div class="cat-section">
                <div class="cat-info">
                  <div class="cat-name">{{ app.catName || '未知猫咪' }}</div>
                </div>
              </div>
              <div class="apply-time-status">
                <div class="apply-time">
                  <span class="label">申请时间：</span>
                  <span class="value">{{ formatDateTime(app.createTime) }}</span>
              </div>
              <div class="status-badge">
                <el-tag :type="getStatusType(app.status)" size="large" effect="light">
                  {{ getStatusText(app.status) }}
                </el-tag>
              </div>
              </div>
              
            </div>

            <div class="card-body">
          <div class="card-content">
            <!-- 左边：猫咪照片 -->
            <div class="cat-image-section">
              <el-image 
                :src="getCatImage(app)" 
                fit="cover"
                class="cat-image"
              >
                <template #error>
                  <div class="image-error">
                    <i class="el-icon-picture"></i>
                    <span>图片加载失败</span>
                  </div>
                </template>
              </el-image>
            </div>
            
            <!-- 右边：小猫明信片内容 -->
            <div class="postcard-content">
              <div class="postcard-details">
                <div class="detail-row">
                  <div class="detail-item">
                    <span class="label">品种：</span>
                    <span class="value">{{ app.catBreed || '未知品种' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">年龄：</span>
                    <span class="value">{{ app.catAge || '未知' }}个月</span>
                  </div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-item">
                    <span class="label">性别：</span>
                    <span class="value">{{ getGenderText(app.catGender) }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">健康状况：</span>
                    <span class="value">{{ getHealthText(app.catHealth) }}</span>
                  </div>
                </div>
                
                <div class="detail-row">
                  <div class="detail-item">
                    <span class="label">位置：</span>
                    <span class="value">{{ app.catLocation || '未知' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">性格：</span>
                    <span class="value">{{ app.catPersonality || '未知' }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="app.reviewComment" class="review-comment">
            <el-icon><InfoFilled /></el-icon>
            <span>审核意见：{{ app.reviewComment }}</span>
          </div>

          <div class="card-footer">
            <el-button 
              @click="viewDetail(app)"
            >
              <el-icon><Document /></el-icon>查看申请详情
            </el-button>

            <template v-if="app.status === 'REJECTED'">
              <el-button 
                type="success"
                @click.stop="reapply(app)"
              >
                <el-icon><RefreshRight /></el-icon>重新申请
              </el-button>
            </template>



            <template v-if="app.status === 'PENDING'">
              <el-button 
                type="danger"
                @click.stop="cancelApplication(app)"
              >
                <el-icon><Close /></el-icon>取消申请
              </el-button>
            </template>
          </div>
        </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="total > 0">
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
      </el-card>
    </div>

    <!-- 申请详情对话框 -->
    <el-dialog 
      v-model="showDetailDialog" 
      width="1200px"
      class="detail-dialog"
      :close-on-click-modal="false"
      :modal="true"
    >
      <div v-if="currentApplication" class="detail-content">
        <!-- 头部信息 -->
        <div class="detail-header">
          <div class="cat-name">{{ currentApplication.catName || '未知猫咪' }}</div>
          <div class="apply-time-status">
            <div class="detail-time">
              <el-icon><Timer /></el-icon>
              <span>{{ formatDateTime(currentApplication.createTime) }}</span>
            </div>
            <div class="detail-tags">
              <el-tag :type="getStatusType(currentApplication.status)" size="default">
                {{ getStatusText(currentApplication.status) }}
              </el-tag>
              <el-tag v-if="isEditing" type="warning" size="default">编辑模式</el-tag>
            </div>
          </div>
        </div>
      
        
        <!-- 申请信息 -->
        <div class="detail-section">
          <h3>申请信息</h3>
          <div v-if="!isEditing" class="info-grid">
            <div v-for="field in getApplicationFields(currentApplication)" :key="field.key" class="info-item">
              <span class="label">{{ field.label }}：</span>
              <span class="value">{{ field.value }}</span>
            </div>
          </div>
          
          <!-- 编辑模式下的表单 -->
          <div v-if="isEditing" class="edit-form">
            <el-form 
              :model="editForm" 
              :rules="formRules" 
              ref="editFormRef"
              label-width="120px" 
              :disabled="editLoading"
            >
              <el-form-item label="姓名：" prop="realName">
                <el-input 
                  v-model="editForm.realName" 
                  placeholder="请输入真实姓名" 
                  maxlength="20"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="身份证号：" prop="idCard">
                <el-input 
                  v-model="editForm.idCard" 
                  placeholder="请输入18位身份证号" 
                  maxlength="18"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="联系电话：" prop="phone">
                <el-input 
                  v-model="editForm.phone" 
                  placeholder="请输入11位手机号码" 
                  maxlength="11"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="家庭住址：" prop="address">
                <el-input 
                  v-model="editForm.address" 
                  type="textarea" 
                  :rows="2" 
                  placeholder="请输入详细家庭住址" 
                  maxlength="100"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="申请理由：" prop="applicationReason">
                <el-input 
                  v-model="editForm.applicationReason" 
                  type="textarea" 
                  :rows="3" 
                  placeholder="请详细说明申请领养的理由，包括您对猫咪的期望和承诺" 
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="居住环境：" prop="livingCondition">
                <el-input 
                  v-model="editForm.livingCondition" 
                  type="textarea" 
                  :rows="3" 
                  placeholder="请详细描述您的居住环境，包括房屋类型、面积、是否有阳台等" 
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="养猫经验：" prop="experience">
                <el-select v-model="editForm.experience" placeholder="请选择养猫经验">
                  <el-option label="无经验" value="无经验" />
                  <el-option label="1年以下" value="1年以下" />
                  <el-option label="1-2年" value="1-2年" />
                  <el-option label="2-5年" value="2-5年" />
                  <el-option label="5年以上" value="5年以上" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="家庭成员：" >
                <el-input 
                  v-model="editForm.familyMembers" 
                  placeholder="请描述家庭成员情况，包括人数、年龄、是否同意养猫等" 
                  maxlength="100"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="经济状况：" >
                <el-select v-model="editForm.financialStatus" placeholder="请选择经济状况">
                  <el-option label="稳定" value="稳定" />
                  <el-option label="良好" value="良好" />
                  <el-option label="一般" value="一般" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="工作时间：" >
                <el-input 
                  v-model="editForm.workSchedule" 
                  placeholder="请描述工作时间安排，包括每天在家的时间" 
                  maxlength="100"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="其他说明：" >
                <el-input 
                  v-model="editForm.additionalInfo" 
                  type="textarea" 
                  :rows="2" 
                  placeholder="其他需要说明的情况，如是否有其他宠物、过敏情况等" 
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button v-if="!isEditing" @click="showDetailDialog = false" size="default">关闭</el-button>
          <el-button v-if="!isEditing" 
            type="warning"
            @click="startEdit" 
            size="default"
          >
            修改信息
          </el-button>
          <el-button v-if="!isEditing" 
            type="primary"
            @click="viewCatDetail(currentApplication)" 
            size="default"
          >
            查看猫咪详情
          </el-button>
          
          <!-- 编辑模式下的按钮 -->
          <el-button v-if="isEditing" @click="cancelEdit" :disabled="editLoading" size="default">取消</el-button>
          <el-button v-if="isEditing" 
            type="primary" 
            @click="submitEdit" 
            :loading="editLoading"
            size="default"
          >
            确认修改
          </el-button>
        </div>
      </template>
    </el-dialog>


  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Document, Plus, View, RefreshRight, Check, InfoFilled, Search, Refresh 
} from '@element-plus/icons-vue'
import { adoptionApi } from '@/api/adoption'
import webSocketService from '@/utils/websocket'

const router = useRouter()

const applications = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref('')

const showDetailDialog = ref(false)
const currentApplication = ref(null)
const editForm = ref({})
const editLoading = ref(false)
const isEditing = ref(false)
const editFormRef = ref()

// 表单验证规则
const formRules = ref({
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入家庭住址', trigger: 'blur' },
    { min: 5, max: 100, message: '地址长度在 5 到 100 个字符', trigger: 'blur' }
  ],
  applicationReason: [
    { required: true, message: '请输入申请理由', trigger: 'blur' },
    { min: 10, max: 500, message: '申请理由长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  livingCondition: [
    { required: true, message: '请输入居住环境描述', trigger: 'blur' },
    { min: 10, max: 500, message: '居住环境描述长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  experience: [
    { required: true, message: '请选择养猫经验', trigger: 'change' }
  ],
  familyMembers: [
    { required: true, message: '请输入家庭成员情况', trigger: 'blur' },
    { min: 2, max: 100, message: '家庭成员描述长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  financialStatus: [
    { required: true, message: '请选择经济状况', trigger: 'change' }
  ],
  workSchedule: [
    { required: true, message: '请输入工作时间安排', trigger: 'blur' },
    { min: 5, max: 100, message: '工作时间描述长度在 5 到 100 个字符', trigger: 'blur' }
  ],
  additionalInfo: [
    { max: 200, message: '其他说明长度不超过 200 个字符', trigger: 'blur' }
  ]
})

const pendingCount = computed(() => applications.value.filter(a => a.status === 'PENDING').length)
const approvedCount = computed(() => applications.value.filter(a => a.status === 'APPROVED').length)
const rejectedCount = computed(() => applications.value.filter(a => a.status === 'REJECTED').length)

const getCatImage = (app) => {
  console.log('猫咪数据:', app) // 调试日志
  
  if (!app.catImage) {
    console.log('没有catImage字段，使用占位图')
    return '/img/placeholder-cat.jpg'
  }
  
  try {
    let image = app.catImage
    
    // 如果是字符串，尝试解析JSON
    if (typeof image === 'string') {
      try {
        const parsed = JSON.parse(image)
        if (Array.isArray(parsed) && parsed.length > 0) {
          image = parsed[0]
        } else if (typeof parsed === 'object' && parsed.url) {
          image = parsed.url
        }
      } catch (e) {
        // 解析失败，保持原样
        console.log('JSON解析失败，使用原字符串:', image)
      }
    }
    
    // 如果是数组，取第一个元素
    if (Array.isArray(image)) {
      image = image[0]
    }
    
    // 如果是对象，尝试获取url属性
    if (typeof image === 'object' && image.url) {
      image = image.url
    }
    
    // 处理图片URL
    if (typeof image === 'string') {
      // 如果是完整URL，直接返回
      if (image.startsWith('http')) {
        return image
      }
      // 如果是上传的图片，添加后端基础URL
      if (image.startsWith('/uploads/')) {
        return `http://localhost:8080${image}`
      }
      // 如果是静态资源路径，直接返回
      if (image.startsWith('/img/')) {
        return image
      }
      // 其他情况，添加/img/前缀
      if (image.startsWith('/')) {
        return `/img${image}`
      } else {
        return `/img/${image}`
      }
    }
    
    console.log('最终图片URL:', image)
    return image || '/img/placeholder-cat.jpg'
  } catch (error) {
    console.error('解析图片数据失败:', error)
    return '/img/placeholder-cat.jpg'
  }
}

const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'UNDER_REVIEW': 'primary',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'PENDING': '待审核',
    'UNDER_REVIEW': '审核中',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return texts[status] || status
}

const getGenderText = (gender) => {
  const texts = {
    'MALE': '公',
    'FEMALE': '母',
    'UNKNOWN': '未知',
    '公': '公',
    '母': '母',
    '未知': '未知',
    'male': '公',
    'female': '母',
    'unknown': '未知'
  }
  
  if (!gender) return '未知'
  return texts[gender] || gender
}

const getHealthText = (health) => {
  const texts = {
    'HEALTHY': '健康',
    'NEEDS_CARE': '需要照顾',
    'DISABLED': '残疾',
    '健康': '健康',
    '需要照顾': '需要照顾',
    '残疾': '残疾',
    '良好': '健康',
    '一般': '需要照顾',
    '需要治疗': '需要治疗'
  }
  return texts[health] || '未知'
}

const getStatusClass = (status) => {
  if (!status) return 'status-unknown'
  return `status-${status.toLowerCase()}`
}

// 获取申请人字段值
const getApplicantField = (application, fieldName) => {
  if (!application || !application.applicant) return null
  
  const applicant = application.applicant
  const value = applicant[fieldName]
  
  // 检查值是否有效（不为空、null、undefined）
  if (value !== null && value !== undefined && value !== '') {
    return value
  }
  
  return null
}

// 获取申请信息字段列表
const getApplicationFields = (application) => {
  if (!application) return []
  
  const fields = [
    {
      key: 'realName',
      label: '姓名',
      value: getApplicantField(application, 'realName') || application.applicantName || application.name || '未知'
    },
    {
      key: 'idCard',
      label: '身份证号',
      value: getApplicantField(application, 'idCard') || application.applicantIdCard || application.idCard || '未知'
    },
    {
      key: 'phone',
      label: '联系电话',
      value: getApplicantField(application, 'phone') || application.applicantPhone || application.phone || '未知'
    },
    {
      key: 'address',
      label: '家庭住址',
      value: getApplicantField(application, 'address') || application.applicantAddress || application.address || '未知'
    },
    {
      key: 'applicationReason',
      label: '申请理由',
      value: getApplicantField(application, 'applicationReason') || application.applicationReason || application.reason || '未知'
    },
    {
      key: 'livingCondition',
      label: '居住环境',
      value: getApplicantField(application, 'livingCondition') || application.livingCondition || application.livingEnvironment || '未知'
    },
    {
      key: 'experience',
      label: '养猫经验',
      value: getApplicantField(application, 'experience') || application.experience || application.petExperience || '未知'
    },
    {
      key: 'familyMembers',
      label: '家庭成员',
      value: getApplicantField(application, 'familyMembers') || application.familySituation || application.familyMembers || '未知'
    },
    {
      key: 'financialStatus',
      label: '经济状况',
      value: getApplicantField(application, 'financialStatus') || application.financialSituation || application.financialStatus || '未知'
    },
    {
      key: 'workSchedule',
      label: '工作时间',
      value: getApplicantField(application, 'workSchedule') || application.workArrangement || application.workSchedule || '未知'
    },
    {
      key: 'additionalInfo',
      label: '其他说明',
      value: getApplicantField(application, 'additionalInfo') || application.remarks || application.additionalInfo || '未知'
    }
  ]
  
  // 过滤掉值为'未知'的字段，只显示有实际内容的字段
  return fields.filter(field => field.value && field.value !== '未知')
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '未知时间'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadApplications = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    if (filterStatus.value) {
      params.status = filterStatus.value
    }
    
    const response = await adoptionApi.getMyApplications(params)
    console.log('我的领养申请:', response)
    
    if (response.data) {
      const data = response.data
      applications.value = data.records || []
      total.value = data.total || 0
    }
  } catch (error) {
    console.error('加载领养申请失败:', error)
    ElMessage.error('加载领养申请失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadApplications()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadApplications()
}

const viewCatDetail = (app) => {
  router.push(`/cats/${app.catId}`)
}

const viewDetail = (app) => {
  currentApplication.value = app
  showDetailDialog.value = true
}

const startEdit = async () => {
  try {
    // 确认是否要修改申请信息
    await ElMessageBox.confirm(
      '确定要修改申请信息吗？修改后需要重新审核。',
      '确认修改',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 进入编辑模式
    isEditing.value = true
    
    // 获取申请人信息
    const applicant = currentApplication.value.applicant || {}
    
    editForm.value = {
      id: currentApplication.value.id,
      realName: applicant.realName || currentApplication.value.realName || currentApplication.value.applicantName || currentApplication.value.name || '',
      idCard: applicant.idCard || currentApplication.value.idCard || currentApplication.value.applicantIdCard || '',
      phone: applicant.phone || currentApplication.value.phone || currentApplication.value.applicantPhone || '',
      address: applicant.address || currentApplication.value.address || currentApplication.value.applicantAddress || '',
      applicationReason: applicant.applicationReason || currentApplication.value.applicationReason || currentApplication.value.reason || '',
      livingCondition: applicant.livingCondition || currentApplication.value.livingCondition || currentApplication.value.livingEnvironment || '',
      experience: applicant.experience || currentApplication.value.experience || currentApplication.value.petExperience || '',
      familyMembers: applicant.familyMembers || currentApplication.value.familyMembers || currentApplication.value.familySituation || '',
      financialStatus: applicant.financialStatus || currentApplication.value.financialStatus || currentApplication.value.financialSituation || '',
      workSchedule: applicant.workSchedule || currentApplication.value.workSchedule || currentApplication.value.workArrangement || '',
      additionalInfo: applicant.additionalInfo || currentApplication.value.additionalInfo || currentApplication.value.remarks || ''
    }
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('进入编辑模式失败:', error)
      ElMessage.error('进入编辑模式失败')
    }
  }
}

const cancelEdit = () => {
  isEditing.value = false
  editForm.value = {}
  if (editFormRef.value) {
    editFormRef.value.clearValidate()
  }
}

const submitEdit = async () => {
  try {
    // 表单验证
    if (!editFormRef.value) return
    
    const valid = await editFormRef.value.validate()
    if (!valid) {
      ElMessage.warning('请检查表单填写是否正确')
      return
    }
    
    editLoading.value = true
    
    // 检查申请状态，只有待审核的申请才能修改
    if (currentApplication.value.status !== 'PENDING') {
      ElMessage.warning('只有待审核的申请才能修改信息')
      editLoading.value = false
      return
    }
    
    // 获取申请人ID
    const applicantId = currentApplication.value.applicantId || currentApplication.value.applicant?.id
    
    console.log('申请人ID:', applicantId)
    
    // 准备更新数据 - 新的数据结构
    const updateData = {
      // 申请基本信息
      applicationReason: editForm.value.applicationReason,
      livingCondition: editForm.value.livingCondition,
      experience: editForm.value.experience,
      familyMembers: editForm.value.familyMembers,
      financialStatus: editForm.value.financialStatus,
      workSchedule: editForm.value.workSchedule,
      additionalInfo: editForm.value.additionalInfo,
      
      // 申请人ID关联
      applicantId: applicantId,
      
      // 申请人基本信息（用于更新applicants表）
      realName: editForm.value.realName,
      idCard: editForm.value.idCard,
      phone: editForm.value.phone,
      address: editForm.value.address
    }
    
    console.log('发送的更新数据:', updateData)
    
    // 调用API更新申请信息
    console.log('开始调用更新API，申请ID:', editForm.value.id)
    const response = await adoptionApi.updateAdoption(editForm.value.id, updateData)
    console.log('API响应:', response)
    
    // 检查响应格式
    if (response && (response.code === 200 || response.status === 200 || response.success)) {
      ElMessage.success('申请信息修改成功！')
      
      // 退出编辑模式
      isEditing.value = false
      
      // 重新加载申请列表
      await loadApplications()
      
      // 如果当前详情对话框打开，更新显示的数据
      if (currentApplication.value && currentApplication.value.id === editForm.value.id) {
        // 从重新加载的申请列表中查找更新后的申请数据
        const updatedApplication = applications.value.find(app => app.id === editForm.value.id)
        if (updatedApplication) {
          // 完全替换当前申请数据
          currentApplication.value = { ...updatedApplication }
        } else {
          // 如果找不到，手动更新字段
          Object.keys(editForm.value).forEach(key => {
            if (key !== 'id') {
              // 更新申请记录本身的字段
              if (currentApplication.value[key] !== undefined) {
                currentApplication.value[key] = editForm.value[key]
              }
              // 更新applicant对象中的字段
              if (currentApplication.value.applicant && currentApplication.value.applicant[key] !== undefined) {
                currentApplication.value.applicant[key] = editForm.value[key]
              }
            }
          })
        }
      }
    } else {
      // 处理不同的响应格式
      const errorMessage = response?.message || response?.msg || response?.data?.message || '修改申请信息失败'
      ElMessage.error(errorMessage)
      console.error('API响应错误:', response)
    }
    
  } catch (error) {
    console.error('提交修改失败:', error)
    
    // 处理不同的错误格式
    if (error?.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error?.message) {
      ElMessage.error(error.message)
    } else if (error?.errors) {
      // 表单验证错误，不显示错误消息
      return
    } else {
      ElMessage.error('修改申请信息失败，请检查网络连接')
    }
  } finally {
    editLoading.value = false
  }
}

const handleCardClick = (app, event) => {
  viewDetail(app)
}

const reapply = async (app) => {
  try {
    await ElMessageBox.confirm(
      `确定要重新申请领养 ${app.catName} 吗？`,
      '确认重新申请',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 创建新的申请记录，保留已拒绝的记录
    const newAdoption = {
      catId: app.catId,
      applicantId: app.applicantId,
      userId: app.userId,
      status: 'PENDING'
    }
    
    await adoptionApi.createAdoption(newAdoption)
    ElMessage.success('重新申请成功！新的申请已提交，请等待审核')
    
    // 重新加载申请列表
    loadApplications()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重新申请失败:', error)
      ElMessage.error(error.response?.data?.message || '重新申请失败')
    }
  }
}



const cancelApplication = async (app) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消对 ${app.catName} 的领养申请吗？取消后无法恢复。`,
      '确认取消申请',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '再想想',
        type: 'warning'
      }
    )
    
    // 调用取消申请API
    await adoptionApi.cancelApplication(app.id)
    ElMessage.success('申请已取消')
    
    // 重新加载申请列表
    loadApplications()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消申请失败:', error)
      ElMessage.error(error.response?.data?.message || '取消申请失败')
    }
  }
}

// WebSocket消息处理器ID
let adoptionApplicationHandlerId = null
let adoptionReviewHandlerId = null

// 创建领养审核更新处理器
const handleAdoptionReviewUpdate = async (data) => {
  console.log('收到领养审核实时更新:', data)
  
  // 解析更新数据
  try {
    const updateData = JSON.parse(data)
    
    // 根据审核结果处理数据
    if (updateData.action === 'APPROVE' || updateData.action === 'REJECT') {
      // 重新加载申请列表
      loadApplications()
      
      // 显示审核结果通知
      if (updateData.action === 'APPROVE') {
        ElMessage.success('恭喜！您的领养申请已通过审核')
      } else {
        ElMessage.error('您的领养申请已被拒绝，拒绝原因：' + (updateData.reviewComment || '无'))
      }
    }
  } catch (error) {
    console.error('解析WebSocket消息失败:', error)
  }
}

onMounted(async () => {
  loadApplications()
  
  // 初始化WebSocket连接
  try {
    await webSocketService.connect()
    
    // 添加领养申请实时更新处理器
    adoptionApplicationHandlerId = webSocketService.addAdoptionApplicationHandler((data) => {
      console.log('收到领养申请实时更新:', data)
      
      // 解析更新数据
      try {
        const updateData = JSON.parse(data)
        
        // 根据更新类型处理数据
        if (updateData.action === 'CREATE' || updateData.action === 'UPDATE') {
          // 重新加载申请列表
          loadApplications()
          
          // 显示更新通知
          ElMessage.success('领养申请列表已更新')
        } else if (updateData.action === 'DELETE') {
          // 重新加载申请列表
          loadApplications()
          
          // 显示删除通知
          ElMessage.info('领养申请已删除')
        }
      } catch (error) {
        console.error('解析WebSocket消息失败:', error)
      }
    })
    
    // 添加领养审核实时更新处理器
    adoptionReviewHandlerId = webSocketService.addAdoptionReviewHandler(handleAdoptionReviewUpdate)
    
    console.log('WebSocket实时更新已启用')
  } catch (error) {
    console.error('WebSocket连接失败:', error)
  }
})

onUnmounted(() => {
  // 清理WebSocket处理器
  if (adoptionApplicationHandlerId) {
    webSocketService.removeMessageHandler(adoptionApplicationHandlerId)
    adoptionApplicationHandlerId = null
  }
  if (adoptionReviewHandlerId) {
    webSocketService.removeMessageHandler(adoptionReviewHandlerId)
    adoptionReviewHandlerId = null
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

.user-adoptions-container{
  padding: 20px;
}

.my-adoptions-container {
  max-width: 1550px;
  margin: 0 auto;
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e3e8f0 100%);
  min-height: 100vh;
}

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


.filter-section {
  margin-bottom: 24px;
}

.applications-section {
  margin-bottom: 24px;
  animation: fadeInUp 0.6s ease;
}

.application-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header-content {
  color: white;
  z-index: 1;
  position: relative;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-subtitle {
  opacity: 0.9;
  margin: 0;
  font-size: 16px;
}

.header-stats {
  display: flex;
  gap: 16px;
  z-index: 1;
  position: relative;
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
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-card.pending {
  background: rgba(235, 215, 5, 0.3);
}

.stat-card.approved {
  background: rgba(14, 236, 66, 0.3);
}

.stat-card.rejected {
  background: rgba(245, 36, 57, 0.5);
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

.filter-section {
  margin-bottom: 32px;
}

.applications-section {
  margin-bottom: 32px;
  animation: fadeInUp 0.6s ease ;
}

.application-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.application-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border: 1px solid #e0e0e0;
  transition: all 0.3s ease;
  overflow: hidden;
}

.application-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.card-body {
  padding: 20px;
}

.card-content {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
}

.cat-image-section {
  flex-shrink: 0;
}

.cat-image {
  width: 200px;
  height: 200px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  object-fit: cover;
  border: 1px solid #dbdded;
}

.postcard-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}



.status-tag {
  flex-shrink: 0;
}

.postcard-details {
  flex: 1;
}

.detail-row {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  flex: 1;
  padding: 6px 8px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 3px solid #667eea;
}

.detail-item.full-width {
  flex: 0 0 100%;
}

.detail-item .label {
  color: #666;
  font-weight: 600;
  min-width: 60px;
  flex-shrink: 0;
}

.detail-item .value {
  color: #333;
  font-weight: 500;
}

.card-footer {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
}

.cat-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.cat-avatar {
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.cat-info {
  display: flex;
  gap: 4px;
}

.cat-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-left: 5px;
}

.cat-breed {
  font-size: 13px;
  color: #606266;
}



.info-item {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 8px;
}

.info-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  line-height: 1.6;
}

.review-comment {
  background: #fef0f0;
  color: #f56c6c;
  padding: 12px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.card-footer {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
  animation: fadeInUp 0.6s ease ;
}


.detail-dialog {
  max-height: 700px;
  overflow: hidden ;
}

.detail-dialog .el-dialog__body {
  padding: 0;
  overflow: hidden;
  max-height: 550px;
}


/* 头部信息 */
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.detail-tags {
  display: flex;
  gap: 10px;
}

.detail-time {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
}


.detail-section h3 {
  margin: 0 0 25px 0;
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  border-left: 4px solid #667eea;
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

/* 图片画廊 */
.image-gallery {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.image-item {
  flex: 0 0 auto;
}

.detail-image {
  width: 200px;
  height: 150px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  object-fit: cover;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 25px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 3px solid #409EFF;
}

.info-item .label {
  font-size: 14px;
  min-width: 80px;
  flex-shrink: 0;
}

.info-item .value {
  color: #303133;
  font-size: 14px;
  line-height: 1.5;
}

/* 图片加载失败样式 */
.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 14px;
}

.image-error .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

/* 编辑表单样式 */
.edit-form {
  max-height: 400px;
  overflow-y: auto;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.edit-form .el-form-item {
  margin-bottom: 16px;
}

.edit-form .el-form-item:last-child {
  margin-bottom: 0;
}

/* 申请详情对话框样式 */
.detail-dialog .el-dialog {
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  max-height: 85vh;
  display: flex;
}

.detail-dialog .el-dialog__header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px 16px 0 0;
  padding: 24px 32px;
  margin: 0;
  flex-shrink: 0;
}

.detail-dialog .el-dialog__title {
  color: white;
  font-size: 20px;
  font-weight: 600;
}

.detail-dialog .el-dialog__headerbtn {
  top: 24px;
  right: 32px;
}

.detail-dialog .el-dialog__headerbtn .el-dialog__close {
  color: white;
  font-size: 20px;
}

.detail-dialog .el-dialog__body {
  padding: 0;
  flex: 1;
  overflow-y: auto;
}

.detail-content {
  padding: 8px;
  max-height: calc(85vh - 140px);
}

.apply-time-status {
  display: flex;
  gap: 12px;
}
.apply-time {
  display: flex;
  gap: 4px;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
}
.status-badge {
  display: flex;
  align-items: center;
  gap: 4px;
}
/* 猫咪信息卡片 */
.detail-cat-card {
  display: flex;
  gap: 24px;
  margin-bottom: 32px;
  padding: 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  border-left: 6px solid #667eea;
}

.cat-image-section {
  flex-shrink: 0;
}

.cat-detail-image {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  object-fit: cover;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 120px;
  background: #f5f5f5;
  border-radius: 12px;
  color: #999;
  font-size: 14px;
}

.image-error i {
  font-size: 32px;
  margin-bottom: 8px;
}

.cat-info-section {
  flex: 1;
}

.cat-name-large {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
}

.cat-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.info-item {
  display: flex;
  gap: 8px;
}

.info-item .label {
  font-weight: 600;
  min-width: 80px;
}

.info-item .value {
  color: #333;
}

/* 详情部分 */
.detail-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 12px;
  border-bottom: 2px solid #667eea;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.detail-item {
  display: flex;
  gap: 8px;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #667eea;
  height: 45px;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-item .label {
  font-weight: 500;
  color: #666;
  font-size: 14px;
}

.detail-item .value {
  color: #333;
  line-height: 1.5;
}

/* 审核意见卡片 */
.review-comment-card {
  padding: 20px;
  background: #fff3e0;
  border-radius: 8px;
  border-left: 4px solid #ff9800;
}

.review-comment-detail {
  margin: 0 0 16px 0;
  color: #333;
  line-height: 1.6;
}

.review-info {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #666;
}

.reviewer {
  font-weight: 500;
}

/* 对话框底部 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 5px 32px;
  border-top: 1px solid #f0f0f0;
}

.dialog-footer .el-button {
  border-radius: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .my-adoptions-container {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 24px;
    text-align: center;
  }
  
  .header-stats {
    justify-content: center;
  }
  
  .card-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .card-footer {
    flex-direction: column;
    gap: 12px;
  }
  
  .card-footer .el-button {
    width: 100%;
    margin: 0;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .detail-item.full-width {
    grid-column: 1;
  }
  
  /* 移动端对话框适配 */
  .detail-dialog {
    width: 95% ;
    max-width: 400px;
  }
  
  .detail-content {
    padding: 20px;
  }
  
  .detail-cat-card {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .cat-info-grid {
    grid-template-columns: 1fr;
  }
  
}
</style>
