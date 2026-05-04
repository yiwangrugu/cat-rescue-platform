<template>
  <div class="adoption-review">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><DocumentChecked /></el-icon>
          领养申请审核
        </h1>
        <p class="page-subtitle">审核用户提交的领养申请，确保每只猫咪都能找到合适的家庭</p>
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

    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <el-card shadow="never">
        <el-row :gutter="20" align="middle">
          <el-col :span="8">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索申请人姓名或猫咪名称"
              prefix-icon="Search"
              clearable
              @clear="loadApplications"
              @keyup.enter="loadApplications"
            />
          </el-col>
          <el-col :span="5">
            <el-select v-model="sortBy" placeholder="排序方式" @change="loadApplications">
              <el-option label="最新申请" value="createdAt_desc" />
              <el-option label="最早申请" value="createdAt_asc" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="loadApplications">
              <el-icon><Search /></el-icon>搜索
            </el-button>
            <el-button @click="resetFilters">
              <el-icon><Refresh /></el-icon>重置
            </el-button>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 申请列表 -->
    <div class="applications-section">
      <el-card shadow="never" v-loading="loading">
        <!-- 子标签页 -->
        <el-tabs v-model="activeSubTab" class="adoption-sub-tabs">
          <!-- 待审核 -->
          <el-tab-pane label="待审核" name="pending">
            <div v-if="filteredApplications('PENDING').length === 0" class="empty-state">
              <el-empty description="暂无待审核领养申请" />
            </div>
            <div v-else class="adoptions-section">
              <el-table :data="filteredApplications('PENDING')" style="width: 100%" v-loading="loading" @row-click="viewDetail">
                <el-table-column type="index" label="序号" width="80" align="center" />
                <el-table-column label="申请用户" width="180" align="center">
                  <template #default="{ row }">
                    <div class="applicant-name">{{ row.applicantName || '未知用户' }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="联系电话" width="150" align="center">
                  <template #default="{ row }">
                    <div>{{ row.applicantPhone || '未提供' }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="申请猫咪" width="150" align="center">
                  <template #default="{ row }">
                    <div class="cat-name">{{ row.catName || '未知猫咪' }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="猫咪品种" width="150" align="center">
                  <template #default="{ row }">
                    <div class="cat-breed">{{ row.catBreed || '未知品种' }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="申请理由" width="400" align="center">
                  <template #default="{ row }">
                    <div class="reason-text">{{ (row.applicationReason || '暂无理由').substring(0, 50) }}{{ (row.applicationReason && row.applicationReason.length > 50) ? '...' : '' }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="申请时间" width="180" align="center">
                  <template #default="{ row }">
                    <div class="apply-time">{{ formatTime(row.createTime) }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="200" align="center" fixed="right">
                  <template #default="{ row }">
                    <div>
                      <el-button size="small" @click="viewDetail(row)">
                        <el-icon><View /></el-icon>查看详情
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          
          <!-- 已通过 -->
          <el-tab-pane label="已通过" name="approved">
            <div v-if="filteredApplications('APPROVED').length === 0" class="empty-state">
              <el-empty description="暂无已通过领养申请" />
            </div>
            <div v-else class="adoptions-section">
              <el-table :data="filteredApplications('APPROVED')" style="width: 100%" v-loading="loading" @row-click="viewDetail">
                <el-table-column type="index" label="序号" width="100" align="center" />
                <el-table-column label="申请用户" width="180" align="center">
                  <template #default="{ row }">
                    <div class="applicant-name">{{ row.applicantName || '未知用户' }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="联系电话" width="160" align="center">
                  <template #default="{ row }">
                    <div>{{ row.applicantPhone || '未提供' }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="申请猫咪" width="160" align="center">
                  <template #default="{ row }">
                    <div class="cat-name">{{ row.catName || '未知猫咪' }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="猫咪品种" width="150" align="center">
                  <template #default="{ row }">
                    <div class="cat-breed">{{ row.catBreed || '未知品种' }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="申请理由" width="400" align="center">
                  <template #default="{ row }">
                    <div class="reason-text">{{ (row.applicationReason || '暂无理由').substring(0, 50) }}{{ (row.applicationReason && row.applicationReason.length > 50) ? '...' : '' }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="通过时间" width="180" align="center">
                  <template #default="{ row }">
                    <div class="approve-time">{{ formatTime(row.approveTime || row.auditTime) }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150" align="center" fixed="right">
                  <template #default="{ row }">
                    <div>
                      <el-button size="small" @click="viewDetail(row)">
                        <el-icon><View /></el-icon>查看详情
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
          
          <!-- 已拒绝 -->
          <el-tab-pane label="已拒绝" name="rejected">
            <div v-if="filteredApplications('REJECTED').length === 0" class="empty-state">
              <el-empty description="暂无已拒绝领养申请" />
            </div>
            <div v-else class="adoptions-section">
              <el-table :data="filteredApplications('REJECTED')" style="width: 100%" v-loading="loading" @row-click="viewDetail">
                <el-table-column type="index" label="序号" width="100" align="center" />
                <el-table-column label="申请用户" width="180" align="center">
                  <template #default="{ row }">
                    <div class="applicant-name">{{ row.applicantName || '未知用户' }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="联系电话" width="180" align="center">
                  <template #default="{ row }">
                    <div>{{ row.applicantPhone || '未提供' }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="申请猫咪" width="160" align="center">
                  <template #default="{ row }">
                    <div class="cat-name">{{ row.catName || '未知猫咪' }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="猫咪品种" width="200" align="center">
                  <template #default="{ row }">
                    <div class="cat-breed">{{ row.catBreed || '未知品种' }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="拒绝理由" width="220" align="center">
                  <template #default="{ row }">
                    <div class="reject-reason">{{ row.reviewComment || '无理由' }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="拒绝时间" width="250" align="center">
                  <template #default="{ row }">
                    <div class="reject-time">{{ formatTime(row.rejectTime || row.auditTime) }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="190" align="center" fixed="right">
                  <template #default="{ row }">
                    <div>
                      <el-button size="small" @click="viewDetail(row)">
                        <el-icon><View /></el-icon>查看详情
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>

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

    <!-- 拒绝申请对话框 -->
    <el-dialog 
      title="拒绝领养申请" 
      v-model="showRejectForm" 
      width="500px"
      :close-on-click-modal="false"
      :before-close="handleCloseRejectForm"
    >
      <div class="reject-dialog-content">
        <div class="reject-info">
          <p><strong>申请人：</strong>{{ currentApplication?.applicantName }}</p>
          <p><strong>申请猫咪：</strong>{{ currentApplication?.catName }}</p>
        </div>
        <el-form :model="rejectForm" ref="rejectFormRef" label-width="80px">
          <el-form-item label="拒绝理由">
            <el-select v-model="rejectForm.reason" placeholder="请选择拒绝理由" style="width: 100%">
              <el-option label="居住环境不适合养猫" value="居住环境不适合养猫" />
              <el-option label="养猫经验不足" value="养猫经验不足" />
              <el-option label="经济状况不稳定" value="经济状况不稳定" />
              <el-option label="家庭成员不同意" value="家庭成员不同意" />
              <el-option label="工作时间过长" value="工作时间过长" />
              <el-option label="其他原因" value="其他原因" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="rejectForm.reason === '其他原因'" label="详细说明">
            <el-input
              v-model="rejectForm.customReason"
              type="textarea"
              :rows="3"
              placeholder="请输入详细的拒绝理由"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="handleCloseRejectForm">取消</el-button>
        <el-button type="danger" @click="rejectApplication" :loading="reviewing">
          确认拒绝
        </el-button>
      </template>
    </el-dialog>

    <!-- 领养申请详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="领养申请详情"
      width="800px"
      :before-close="handleCloseDetailDialog"
    >
      <div v-if="currentApplication" class="adoption-detail">
        <!-- 猫咪信息 -->
        <div class="detail-section">
          <h3>猫咪信息</h3>
          <div class="cat-info-grid">
            <!-- 上半部分：三列布局 -->
            <div class="cat-info-top">
              <!-- 第一列：图片 -->
              <div class="cat-image-section">
                <el-image
                  v-if="getCatImages(currentApplication.catImage).length > 0"
                  :src="getCatImagePath(getCatImages(currentApplication.catImage)[0])"
                  :preview-src-list="getCatImages(currentApplication.catImage).map(img => getCatImagePath(img))"
                  :preview-teleported="true"
                  :hide-on-click-modal="true"
                  :z-index="9999"
                  fit="cover"
                  style="width: 200px; height: 150px; border-radius: 8px;"
                  @error="handleImageError"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                      <span>图片加载失败</span>
                    </div>
                  </template>
                </el-image>
                <span v-else class="no-image">暂无图片</span>
              </div>
              
              <!-- 第二列：姓名、品种、年龄 -->
              <div class="cat-info-column">
                <div class="detail-row">
                  <span class="detail-label">猫咪姓名：</span>
                  <span class="detail-value">{{ currentApplication.catName || '未知' }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">品种：</span>
                  <span class="detail-value">{{ currentApplication.catBreed || '未知品种' }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">年龄：</span>
                  <span class="detail-value">{{ currentApplication.catAge ? currentApplication.catAge + '月' : '未知' }}</span>
                </div>
              </div>
              
              <!-- 第三列：性别、健康状态、体重 -->
              <div class="cat-info-column">
                <div class="detail-row">
                  <span class="detail-label">性别：</span>
                  <span class="detail-value">{{ currentApplication.catGender || '未知' }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">健康状态：</span>
                  <span class="detail-value">{{ currentApplication.catHealth || '未知' }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">体重：</span>
                  <span class="detail-value">{{ currentApplication.catWeight ? currentApplication.catWeight + 'kg' : '未知' }}</span>
                </div>
              </div>
            </div>
            
            <!-- 下半部分：两行布局 -->
            <div class="cat-info-bottom">
              <!-- 第一行：位置和医疗信息 -->
              <div class="cat-info-row">
                <div class="detail-row full-width">
                  <span class="detail-label">位置：</span>
                  <span class="detail-value">{{ currentApplication.catLocation || '未知' }}</span>
                </div>
                <div class="detail-row full-width">
                  <span class="detail-label">医疗信息：</span>
                  <span class="detail-value">{{ currentApplication.catMedical || '无' }}</span>
                </div>
              </div>
              
              <!-- 第二行：性格和描述 -->
              <div class="cat-info-row">
                <div class="detail-row full-width">
                  <span class="detail-label">性格特点：</span>
                  <span class="detail-value">{{ currentApplication.catPersonality || '未知' }}</span>
                </div>
                <div class="detail-row full-width">
                  <span class="detail-label">猫咪描述：</span>
                  <span class="detail-value">{{ currentApplication.catDescription || '暂无描述' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 申请人信息 -->
        <div class="detail-section">
          <h3>申请人信息</h3>
          <div class="applicant-info-grid">
            <div class="detail-row">
              <span class="detail-label">姓名：</span>
              <span class="detail-value">{{ getApplicantName(currentApplication) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">用户ID：</span>
              <span class="detail-value">{{ currentApplication.userId || '未知' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">身份证号：</span>
              <span class="detail-value">{{ currentApplication.idCard || '未填写' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">联系电话：</span>
              <span class="detail-value">{{ currentApplication.phone || currentApplication.applicantPhone || '未填写' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">家庭住址：</span>
              <span class="detail-value">{{ currentApplication.address || '未填写' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">申请理由：</span>
              <span class="detail-value reason-text">{{ currentApplication.reason || currentApplication.applicationReason || '未填写申请理由' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">居住环境：</span>
              <span class="detail-value">{{ currentApplication.livingCondition || '未填写' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">养猫经验：</span>
              <span class="detail-value">{{ currentApplication.experience || '无经验' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">家庭成员：</span>
              <span class="detail-value">{{ currentApplication.familyMembers || '未填写' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">经济状况：</span>
              <span class="detail-value">{{ currentApplication.financialStatus || '未填写' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">工作时间：</span>
              <span class="detail-value">{{ currentApplication.workSchedule || '未填写' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">其他说明：</span>
              <span class="detail-value">{{ currentApplication.additionalInfo || '无' }}</span>
            </div>
          </div>
        </div>

        <!-- 审核信息（所有状态显示，内容根据状态变化） -->
        <div v-if="currentApplication && currentApplication.status" class="detail-section">
          <h3>审核信息</h3>
          <div class="review-info-grid">
            <div class="detail-row">
              <span class="detail-label">审核状态：</span>
              <span class="detail-value">
                <el-tag :type="currentApplication.status === 'PENDING' ? 'warning' : currentApplication.status === 'APPROVED' ? 'success' : 'danger'">
                  {{ currentApplication.status === 'PENDING' ? '待审核' : currentApplication.status === 'APPROVED' ? '已通过' : '已拒绝' }}
                </el-tag>
              </span>
            </div>
            <div v-if="currentApplication.status !== 'PENDING'" class="detail-row">
              <span class="detail-label">{{ currentApplication.status === 'APPROVED' ? '通过时间：' : '拒绝时间：' }}</span>
              <span class="detail-value">{{ formatTime(currentApplication.processTime || currentApplication.approveTime) || '未知' }}</span>
            </div>
            <div v-if="currentApplication.status === 'REJECTED'" class="detail-row full-width">
              <span class="detail-label">拒绝理由：</span>
              <span class="detail-value">{{ currentApplication.reviewComment || '无理由' }}</span>
            </div>
            <div v-if="currentApplication.status === 'PENDING'" class="detail-row full-width">
              <span class="detail-label">申请时间：</span>
              <span class="detail-value">{{ formatTime(currentApplication.createTime) || '未知' }}</span>
            </div>
          </div>
        </div>

        <!-- 审核操作（仅待审核状态显示） -->
        <div v-if="currentApplication.status === 'PENDING'" class="detail-section">
          <h3>审核操作</h3>
          <div class="action-buttons">
            <el-button type="success" size="large" @click="approveApplication(currentApplication)">
              <el-icon><Check /></el-icon>通过申请
            </el-button>
            <el-button type="danger" size="large" plain @click="showRejectDialog(currentApplication)">
              <el-icon><Close /></el-icon>拒绝申请
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  DocumentChecked, Search, Refresh, Phone, Clock, 
  View, Check, Close, RefreshLeft, InfoFilled, Picture,
  Star, User, Document, ChatDotRound
} from '@element-plus/icons-vue'
import { adoptionApi } from '@/api/adoption.js'
import { adminApi } from '@/api/admin.js'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 数据
const applications = ref([])
const loading = ref(false)
const processingId = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const filterStatus = ref('')
const sortBy = ref('createdAt_desc')

// 子标签页
const activeSubTab = ref('pending')

// 监听子标签页变化
watch(activeSubTab, (newTab) => {
  console.log('子标签页切换至:', newTab)
  // 重置页码到第一页
  currentPage.value = 1
  // 重新加载数据
  loadApplications()
})

// 拒绝对话框
const showRejectForm = ref(false)
const reviewing = ref(false)
const currentApplication = ref(null)
const rejectForm = ref({ reason: '', customReason: '' })
const rejectFormRef = ref(null)

// 详情对话框
const showDetailDialog = ref(false)

// 判断是否是管理端
const isAdminPage = computed(() => {
  return window.location.pathname.includes('/admin')
})

// 统计数量
const pendingCount = computed(() => applications.value.filter(a => a.status === 'PENDING').length)
const approvedCount = computed(() => applications.value.filter(a => a.status === 'APPROVED').length)
const rejectedCount = computed(() => applications.value.filter(a => a.status === 'REJECTED').length)

// 过滤申请列表
const filteredApplications = (status) => {
  return applications.value.filter(app => app.status === status)
}

// 加载申请列表
const loadApplications = async () => {
  loading.value = true
  try {
    // 检查认证状态
    if (!authStore.isAuthenticated) {
      ElMessage.error('请先登录')
      router.push('/rescuer/login')
      return
    }
    
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      reviewer: true // 明确表示这是审核人员查看所有申请
    }
    
    if (filterStatus.value && filterStatus.value !== '') {
      params.status = filterStatus.value
    }
    
    console.log('请求参数:', params)
    console.log('当前用户信息:', authStore.user)
    
    // 添加认证头信息
    const config = {
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    }
    
    // 根据当前子标签页设置状态参数
    if (activeSubTab.value === 'pending') {
      params.status = 'PENDING'
    } else if (activeSubTab.value === 'approved') {
      params.status = 'APPROVED'
    } else if (activeSubTab.value === 'rejected') {
      params.status = 'REJECTED'
    }
    
    let response
    if (isAdminPage.value) {
      if (activeSubTab.value === 'pending') {
        response = await adminApi.getPendingAdoptions(currentPage.value, pageSize.value)
      } else if (activeSubTab.value === 'approved') {
        response = await adminApi.getApprovedAdoptions(currentPage.value, pageSize.value)
      } else if (activeSubTab.value === 'rejected') {
        response = await adminApi.getRejectedAdoptions(currentPage.value, pageSize.value)
      }
    } else {
      // 使用通用的领养申请接口，通过状态参数筛选
      response = await adoptionApi.getAdoptionList(params, config)
    }
    
    console.log('API响应:', response)
    
    if (response.data) {
      const data = response.data
      console.log('响应数据:', data)
      
      // 为每个申请记录添加默认状态
      let records = isAdminPage.value ? (data.data || []) : (data.records || [])
      applications.value = records.map(app => ({
        ...app,
        status: app.status || 'PENDING' // 如果 status 为 null，默认为 'PENDING'
      }))
      total.value = data.total || 0
      
      console.log('处理后的申请列表:', applications.value)
    } else {
      console.log('响应数据为空')
      applications.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载领养申请失败:', error)
    console.error('错误详情:', error.response || error.message)
    ElMessage.error('加载领养申请失败')
    applications.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  filterStatus.value = ''
  sortBy.value = 'createdAt_desc'
  currentPage.value = 1
  loadApplications()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  loadApplications()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadApplications()
}

// 获取状态样式
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

const getStatusClass = (status) => {
  if (!status) return 'status-unknown'
  return `status-${status.toLowerCase()}`
}

// 获取猫咪图片
const getCatImage = (app) => {
  if (app.catImage) {
    try {
      // 尝试解析 JSON 数组
      const images = JSON.parse(app.catImage)
      if (Array.isArray(images) && images.length > 0) {
        return fixImagePath(images[0])
      }
      // 如果不是数组，直接返回
      return fixImagePath(app.catImage)
    } catch (error) {
      // 解析失败，直接返回
      return fixImagePath(app.catImage)
    }
  }
  return '/img/placeholder-cat.jpg'
}

// 获取猫咪图片数组（处理JSON格式的图片路径，模仿管理端）
const getCatImages = (imagesData) => {
  if (!imagesData) return []
  
  // 如果已经是数组，直接返回
  if (Array.isArray(imagesData)) {
    return imagesData
  }
  
  // 如果是JSON字符串，尝试解析
  if (typeof imagesData === 'string') {
    try {
      // 尝试解析JSON
      const parsed = JSON.parse(imagesData)
      if (Array.isArray(parsed)) {
        return parsed
      }
      // 如果是单个图片路径，包装成数组
      if (typeof parsed === 'string') {
        return [parsed]
      }
    } catch (error) {
      // 如果不是JSON，可能是单个图片路径
      if (imagesData.trim() !== '') {
        return [imagesData]
      }
    }
  }
  
  return []
}

// 获取猫咪图片路径（模仿管理端的处理方式）
const getCatImagePath = (imagePath) => {
  if (!imagePath) return '/img/placeholder-cat.jpg'
  
  // 处理数据库中的图片路径，确保能够正确加载
  if (imagePath.startsWith('/') && !imagePath.startsWith('/uploads') && !imagePath.startsWith('/img')) {
    // 如果是根路径且不是uploads或img开头，添加img前缀
    return `/img${imagePath}`
  } else if (imagePath.includes('../public/img/')) {
    // 处理相对路径，转换为前端可访问的路径
    return imagePath.replace('../public/img/', '/img/')
  } else if (!imagePath.startsWith('/')) {
    // 如果是相对路径（如cat1.jpg），添加/img/前缀
    return `/img/${imagePath}`
  }
  
  return imagePath
}

// 修正图片路径
const fixImagePath = (imagePath) => {
  if (!imagePath) return '/img/placeholder-cat.jpg'
  
  // 如果路径包含 /public/img/，替换为 /img/
  if (imagePath.includes('/public/img/')) {
    return imagePath.replace('/public/img/', '/img/')
  }
  
  // 如果路径包含 ../public/img/，替换为 /img/
  if (imagePath.includes('../public/img/')) {
    return imagePath.replace('../public/img/', '/img/')
  }
  
  return imagePath
}

// 格式化日期时间
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

// 格式化时间（包含年份信息）
const formatTime = (dateTime) => {
  if (!dateTime) return '未知'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 查看详情
const viewDetail = (app) => {
  console.log('查看详情 - app:', app)
  console.log('查看详情 - applicantName:', app.applicantName)
  console.log('查看详情 - applicantPhone:', app.applicantPhone)
  console.log('查看详情 - catName:', app.catName)
  console.log('查看详情 - catBreed:', app.catBreed)
  console.log('查看详情 - status:', app.status)
  currentApplication.value = app
  showDetailDialog.value = true
}

// 关闭详情对话框
const handleCloseDetailDialog = () => {
  showDetailDialog.value = false
  currentApplication.value = null
}

// 通过申请
const approveApplication = async (app) => {
  try {
    await ElMessageBox.confirm(
      `确定要通过 ${app.applicantName} 对 ${app.catName} 的领养申请吗？`,
      '确认通过',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    
    processingId.value = app.id
    const response = await adoptionApi.approveApplication(app.id)
    
    if (response.data) {
      ElMessage.success('申请已通过')
      loadApplications()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('通过申请失败:', error)
      ElMessage.error('通过申请失败')
    }
  } finally {
    processingId.value = null
  }
}

// 显示拒绝对话框
const showRejectDialog = (app) => {
  currentApplication.value = app
  rejectForm.value = { reason: '', customReason: '' }
  showRejectForm.value = true
}

// 图片加载错误处理
const handleImageError = (error) => {
  console.error('图片加载失败:', error)
  console.error('图片路径信息:', error.target?.src)
}

// 获取申请人姓名
const getApplicantName = (application) => {
  if (!application) return '未知用户'
  
  // 如果applicant是对象，提取realName
  if (application.applicant && typeof application.applicant === 'object') {
    return application.applicant.realName || application.applicantName || '未知用户'
  }
  
  // 如果applicant是字符串，直接使用
  if (typeof application.applicant === 'string') {
    return application.applicant
  }
  
  // 最后尝试applicantName
  return application.applicantName || '未知用户'
}

// 关闭拒绝对话框
const handleCloseRejectForm = () => {
  showRejectForm.value = false
  rejectForm.value = {
    reason: '',
    customReason: ''
  }
}

// 拒绝申请
const rejectApplication = async () => {
  if (!rejectFormRef.value) return
  
  // 表单验证
  if (!rejectForm.value.reason) {
    ElMessage.error('请选择拒绝理由')
    return
  }
  
  // 如果是其他原因，需要填写详细说明
  if (rejectForm.value.reason === '其他原因' && !rejectForm.value.customReason) {
    ElMessage.error('请填写详细的拒绝理由')
    return
  }
  
  // 构建拒绝理由
  let reviewComment = rejectForm.value.reason
  if (rejectForm.value.reason === '其他原因' && rejectForm.value.customReason) {
    reviewComment = rejectForm.value.customReason
  }
  
  reviewing.value = true
  try {
    const response = await adoptionApi.rejectApplication(
      currentApplication.value.id,
      { reviewComment: reviewComment }
    )
    
    if (response.data) {
      ElMessage.success('申请已拒绝')
      showRejectForm.value = false
      loadApplications()
    }
  } catch (error) {
    console.error('拒绝申请失败:', error)
    ElMessage.error('拒绝申请失败')
  } finally {
    reviewing.value = false
  }
}

// 撤销通过
const revokeApproval = async (app) => {
  try {
    await ElMessageBox.confirm(
      `确定要撤销 ${app.applicantName} 对 ${app.catName} 的领养申请通过状态吗？`,
      '确认撤销',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    processingId.value = app.id
    // 调用更新接口将状态改回待审核
    const response = await adoptionApi.updateAdoption(app.id, {
      status: 'PENDING',
      reviewComment: '审核状态已撤销'
    })
    
    if (response.data) {
      ElMessage.success('已通过状态已撤销')
      loadApplications()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤销失败:', error)
      ElMessage.error('撤销失败')
    }
  } finally {
    processingId.value = null
  }
}

onMounted(() => {
  loadApplications()
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

/* el-card头部和主体动画 */
:deep(.el-card__header) {
  /* animation: fadeInUp 0.6s ease ; */
}

:deep(.el-card__body) {
  /* animation: fadeInUp 0.6s ease ; */
}

.adoption-review {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
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
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
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

.stat-card.pending {
  background: rgba(255, 193, 7, 0.3);
}

.stat-card.approved {
  background: rgba(40, 167, 69, 0.3);
}

.stat-card.rejected {
  background: rgba(220, 53, 69, 0.3);
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
  margin-bottom: 24px;
}

.applications-section {
  margin-bottom: 24px;
}

/* 子标签页样式 */
.adoption-sub-tabs {
  margin-bottom: 20px;
}

.adoption-sub-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.adoption-sub-tabs :deep(.el-tabs__content) {
  padding: 20px 0;
}

/* 表格样式 */
.adoptions-section {
  margin-bottom: 20px;
}

.adoptions-section :deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

.adoptions-section :deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
  color: #606266;
}

.adoptions-section :deep(.el-table .cell) {
  padding: 8px 12px;
}



/* 申请理由样式 */
.reason-text {
  max-width: 100%;
  word-wrap: break-word;
  white-space: normal;
  line-height: 1.5;
}



/* 拒绝理由样式 */
.reject-reason {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #f56c6c;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.action-buttons .el-button {
  min-width: 60px;
}

/* 空状态样式 */
.empty-state {
  padding: 40px 0;
  text-align: center;
}

/* 领养申请详情对话框样式 */
.adoption-detail {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 10px;
}

.adoption-detail::-webkit-scrollbar {
  width: 8px;
}

.adoption-detail::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.adoption-detail::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 4px;
}

.adoption-detail::-webkit-scrollbar-thumb:hover {
  background: #909399;
}

.detail-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.detail-section h3 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 2px solid #409eff;
  padding-bottom: 8px;
}

/* 猫咪信息网格布局 */
.cat-info-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cat-info-top {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.cat-image-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.cat-info-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cat-info-bottom {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cat-info-row {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 申请人信息网格 */
.applicant-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 12px;
}

.review-info-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 详细信息行样式 */
.detail-row {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    background: #f8f9fa;
    border-radius: 8px;
    border-left: 4px solid #67C23A;
    transition: all 0.3s ease;
}

.full-width {
  grid-column: 1 / -1;
}

.detail-label {
  min-width: 80px;
  font-weight: 500;
  color: #606266;
  margin-right: 8px;
}

.detail-value {
  flex: 1;
  color: #303133;
  word-break: break-word;
}

.detail-value.reason-text {
  line-height: 1.6;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}

/* 图片错误样式 */
.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 200px;
  height: 150px;
  background: #f5f7fa;
  border-radius: 8px;
  color: #909399;
  gap: 8px;
}

.no-image {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 200px;
  height: 150px;
  background: #f5f7fa;
  border-radius: 8px;
  color: #909399;
}

/* 审核操作按钮 */
.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  padding: 16px 0;
}

.application-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.application-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  border-left: 4px solid #dcdfe6;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  animation: fadeInUp 0.6s ease ;
}

.application-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.application-card.status-pending {
  border-left-color: #e6a23c;
}

.application-card.status-approved {
  border-left-color: #67c23a;
}

.application-card.status-rejected {
  border-left-color: #f56c6c;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.applicant-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.applicant-avatar {
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.applicant-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}


.applicant-contact {
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 4px;
}



.card-body {
  margin-bottom: 16px;
}

.cat-section {
  margin-bottom: 16px;
}

.section-title {
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.cat-info {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f5f7fa;
  padding: 12px;
  border-radius: 8px;
}

.cat-avatar {
  border: 2px solid #fff;
}

.cat-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}


.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-item {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 8px;
}

.info-item.full-width {
  grid-column: span 2;
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
}

.info-value.reason {
  line-height: 1.6;
}

.card-footer {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.reject-reason {
  flex: 1;
  font-size: 13px;
  color: #f56c6c;
  display: flex;
  align-items: center;
  gap: 6px;
  background: #fef0f0;
  padding: 8px 12px;
  border-radius: 6px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.reject-dialog-content {
  .reject-info {
    background: #f5f7fa;
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 20px;
    
    p {
      margin: 0 0 8px 0;
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

.detail-content {
  .detail-section {
    margin-bottom: 24px;
    
    h4 {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 12px 0;
      padding-bottom: 8px;
      border-bottom: 1px solid #ebeef5;
    }
  }
  
  .detail-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .detail-item {
    display: flex;
    flex-direction: column;
    gap: 4px;
    
    &.full-width {
      grid-column: span 2;
    }
    
    .label {
      font-size: 12px;
      color: #909399;
    }
    
    .value {
      font-size: 14px;
      color: #303133;
    }
  }
  
  .detail-cat {
    display: flex;
    align-items: center;
    gap: 16px;
    background: #f5f7fa;
    padding: 16px;
    border-radius: 8px;
    
    .cat-info-detail {
    
      
      .cat-breed {
        font-size: 14px;
        color: #606266;
      }
    }
  }
  
  .review-comment {
    background: #fef0f0;
    color: #f56c6c;
    padding: 12px;
    border-radius: 6px;
    margin: 0;
  }
}

/* 不同标签页的标题图标 */
.detail-section:nth-child(1) h3::before {
  content: "🐱";
  margin-right: 8px;
  font-size: 16px;
}

.detail-section:nth-child(2) h3::before {
  content: "👤";
  margin-right: 8px;
  font-size: 16px;
}

.detail-section:nth-child(3) h3::before {
  content: "✅";
  margin-right: 8px;
  font-size: 16px;
}

.detail-section:nth-child(4) h3::before {
  content: "💬";
  margin-right: 8px;
  font-size: 16px;
}
</style>
