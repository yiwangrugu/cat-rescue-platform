<template>
  <div class="cat-management">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Management /></el-icon>
          猫咪管理
        </h1>
        <p class="page-subtitle">管理所有猫咪信息，包括救助状态、健康状况和领养情况</p>
      </div>
      <div class="header-stats">
        <div class="stat-card total">
          <div class="stat-number">{{ totalCount }}</div>
          <div class="stat-label">猫咪总数</div>
        </div>
        <div class="stat-card draft">
          <div class="stat-number">{{ draftCount }}</div>
          <div class="stat-label">草稿箱</div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filters-section">
      <el-row :gutter="20">
        <el-col :span="4">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索猫咪名称或品种"
            prefix-icon="Search"
            clearable
          />
        </el-col>
        <el-col :span="3">
          <el-select v-model="filterStatus" placeholder="状态筛选" clearable>
             <el-option label="照顾中" value="照顾中" />
            <el-option label="待领养" value="待领养" />
            <el-option label="已领养" value="已领养" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select v-model="filterHealth" placeholder="健康状况" clearable>
            <el-option label="健康" value="健康" />
            <el-option label="恢复中" value="恢复中" />
            <el-option label="残疾" value="残疾" />
          </el-select>
        </el-col>
        <el-col :span="14">
          <el-button type="primary" @click="loadCats">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetFilters">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="primary" @click="$router.push('/cats/new')">
            <el-icon><Plus /></el-icon>
            添加猫咪
          </el-button>
          <el-button @click="exportCats">
            <el-icon><Download /></el-icon>
            导出数据
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 猫咪列表 -->
    <div class="cats-table-section">
      <el-table :data="filteredCats" style="width: 100%" v-loading="loading" :cell-style="{ textAlign: 'center' }" :header-cell-style="{ textAlign: 'center' }" @row-click="handleRowClick">
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column label="猫咪信息" width="160" align="center">
          <template #default="{ row }">
            <div class="cat-info">
              <el-avatar :size="40" :src="getCatImage(row)" />
              <div class="cat-details">
                <div class="cat-name">{{ row.name }}</div>
                <div class="cat-breed">{{ row.breed || '未知品种' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="110" align="center">
          <template #default="{ row }">{{ row.age }}个月</template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="110" align="center">
          <template #default="{ row }">{{ getGenderText(row.gender) }}</template>
        </el-table-column>
        <el-table-column prop="healthStatus" label="健康状况" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="getHealthType(row.healthStatus)" size="small">
              {{ getHealthText(row.healthStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="位置信息" width="200" align="center">
          <template #default="{ row }">
            <div class="location-info">
              <el-icon><Location /></el-icon>
              <span>{{ row.location }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="添加时间" width="200" align="center">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="审核状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="getAuditStatusType(row.auditStatus, row.auditTime)"
              size="small"
            >
              {{ getAuditStatusText(row.auditStatus, row.auditTime) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" @click="viewCatDetail(row.id)">查看</el-button>
            <el-button size="small" type="primary" @click="editCat(row.id)">编辑</el-button>
            
            <!-- 管理端审核功能 -->
            <el-button 
              size="small" 
              type="success" 
              v-if="isAdmin && !row.auditTime && row.auditStatus === 'PENDING'"
              @click="approveCat(row)"
            >
              通过审核
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              v-if="isAdmin && !row.auditTime && row.auditStatus === 'PENDING'"
              @click="rejectCat(row)"
            >
              拒绝审核
            </el-button>
            
            <!-- 救援端操作 -->
            <el-button 
              size="small" 
              type="success" 
              v-if="!isAdmin && row.status === 'WAITING_RESCUE'"
              @click="startRescue(row)"
            >
              开始救助
            </el-button>
            <el-button 
              size="small" 
              type="warning" 
              v-if="!isAdmin && row.status === 'IN_PROGRESS'"
              @click="completeRescue(row)"
            >
              完成救助
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteCat(row.id)"
            >
              删除
            </el-button>
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
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Location, Management, Search, Refresh } from '@element-plus/icons-vue'
import { catApi } from '@/api/cat'
import { adminApi } from '@/api/admin'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 用户角色判断
const isAdmin = computed(() => authStore.isAdmin)

const cats = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const filterStatus = ref('')
const filterHealth = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filteredCats = computed(() => {
  return cats.value.filter(cat => {
    const matchesSearch = !searchKeyword.value || 
      cat.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      (cat.breed && cat.breed.toLowerCase().includes(searchKeyword.value.toLowerCase()))
    const matchesStatus = !filterStatus.value || cat.status === filterStatus.value
    const matchesHealth = !filterHealth.value || cat.healthStatus === filterHealth.value
    
    return matchesSearch && matchesStatus && matchesHealth
  })
})

// 统计计算属性
const totalCount = computed(() => {
  return cats.value.length
})

const draftCount = computed(() => {
  return cats.value.filter(cat => cat.status === 'DRAFT').length
})

const loadCats = async () => {
  try {
    loading.value = true
    
    // 检查当前路由路径是否包含 /admin 来判断是否是管理端
    const isAdminApp = window.location.pathname.includes('/admin')
    
    let response
    if (isAdminApp) {
      // 管理端使用 adminApi
      const params = {
        page: currentPage.value,
        size: pageSize.value,
        keyword: searchKeyword.value || null
      }
      response = await adminApi.getCats(params)
      if (response.data) {
        cats.value = response.data.cats || []
        total.value = response.data.total || 0
      }
    } else {
      // 救助端使用 catApi
      response = await catApi.getCats(currentPage.value, pageSize.value, filterStatus.value || null, searchKeyword.value || null)
      if (response.data) {
        cats.value = response.data.records || []
        total.value = response.data.total || 0
      }
    }
  } catch (error) {
    console.error('加载猫咪数据失败:', error)
    ElMessage.error('加载猫咪数据失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  searchKeyword.value = ''
  filterStatus.value = ''
  filterHealth.value = ''
  currentPage.value = 1
  loadCats()
}

const getCatImage = (cat) => {
  // 优先使用数据库中的图片路径（审核通过后）
  if (cat.officialImages) {
    try {
      let images = cat.officialImages
      
      // 如果是字符串，尝试解析为JSON
      if (typeof images === 'string') {
        try {
          images = JSON.parse(images)
        } catch {
          // 如果解析失败，检查是否是直接的图片路径
          if (images.startsWith('/')) {
            return images
          }
        }
      }
      
      // 如果是数组格式，返回第一个元素
      if (Array.isArray(images) && images.length > 0 && images[0]) {
        let imagePath = images[0]
        // 处理数据库中的图片路径，确保能够正确加载
        if (imagePath.startsWith('/') && !imagePath.startsWith('/uploads') && !imagePath.startsWith('/img')) {
          // 如果是根路径且不是uploads或img开头，添加img前缀
          return `/img${imagePath}`
        } else if (imagePath.includes('../public/img/')) {
          // 处理相对路径，转换为前端可访问的路径
          return imagePath.replace('../public/img/', '/img/')
        }
        return imagePath
      }
    } catch (error) {
      console.log('解析officialImages失败:', error)
    }
  }
  
  // 其次尝试使用临时图片路径（审核期间）
  if (cat.tempImages) {
    try {
      let images = cat.tempImages
      
      // 如果是字符串，尝试解析为JSON
      if (typeof images === 'string') {
        try {
          images = JSON.parse(images)
        } catch {
          // 如果解析失败，检查是否是直接的图片路径
          if (images.startsWith('/')) {
            return images
          }
        }
      }
      
      // 如果是数组格式，返回第一个元素
      if (Array.isArray(images) && images.length > 0 && images[0]) {
        let imagePath = images[0]
        // 处理数据库中的图片路径，确保能够正确加载
        if (imagePath.startsWith('/') && !imagePath.startsWith('/uploads') && !imagePath.startsWith('/img')) {
          // 如果是根路径且不是uploads或img开头，添加img前缀
          return `/img${imagePath}`
        } else if (imagePath.includes('../public/img/')) {
          // 处理相对路径，转换为前端可访问的路径
          return imagePath.replace('../public/img/', '/img/')
        }
        return imagePath
      }
    } catch (error) {
      console.log('解析tempImages失败:', error)
    }
  }
  
  // 最后尝试使用原始的images字段
  if (cat.images) {
    try {
      let images = cat.images
      
      // 如果是字符串，尝试解析为JSON
      if (typeof images === 'string') {
        try {
          images = JSON.parse(images)
        } catch {
          // 如果解析失败，检查是否是直接的图片路径
          if (images.startsWith('/')) {
            return images
          }
        }
      }
      
      // 如果是数组格式，返回第一个元素
      if (Array.isArray(images) && images.length > 0 && images[0]) {
        let imagePath = images[0]
        // 处理数据库中的图片路径，确保能够正确加载
        if (imagePath.startsWith('/') && !imagePath.startsWith('/uploads') && !imagePath.startsWith('/img')) {
          // 如果是根路径且不是uploads或img开头，添加img前缀
          return `/img${imagePath}`
        } else if (imagePath.includes('../public/img/')) {
          // 处理相对路径，转换为前端可访问的路径
          return imagePath.replace('../public/img/', '/img/')
        }
        return imagePath
      }
    } catch (error) {
      console.log('解析images失败:', error)
    }
  }
  
  //  fallback to placeholder image
  return '/img/placeholder-cat.jpg'
}

const getGenderText = (gender) => {
  const texts = {
    'MALE': '公',
    'FEMALE': '母',
    'UNKNOWN': '未知'
  }
  return texts[gender] || gender
}

const getHealthText = (health) => {
  const texts = {
    '健康': '健康',
    '恢复中': '恢复中',
    '残疾': '残疾'
  }
  return texts[health] || health
}

const getHealthType = (health) => {
  const types = {
    '健康': 'success',
    '恢复中': 'warning',
    '残疾': 'danger'
  }
  return types[health] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    '已拒绝': '已拒绝',
    '照顾中': '照顾中',
    '待领养': '待领养',
    '已领养': '已领养'
  }
  return texts[status] || status
}

const getStatusType = (status) => {
  const types = {
    '已拒绝': 'danger',
    '照顾中': 'warning',
    '待领养': 'primary',
    '已领养': 'success'
  }
  return types[status] || 'info'
}

// 审核状态相关函数
const getAuditStatusText = (auditStatus, auditTime) => {
  // 根据审核时间和审核状态判断
  if (!auditTime && auditStatus === 'PENDING') {
    return '待审核'
  }
  
  const texts = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return texts[auditStatus] || auditStatus
}

const getAuditStatusType = (auditStatus, auditTime) => {
  // 根据审核时间和审核状态判断
  if (!auditTime && auditStatus === 'PENDING') {
    return 'warning'
  }
  
  const types = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return types[auditStatus] || 'info'
}

const formatDate = (date) => {
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleRowClick = (row, column, event) => {
  // 检查是否点击了操作按钮
  const target = event.target
  // 如果点击的是按钮元素或按钮的子元素，则不触发查看详情
  if (target.closest('button') || target.closest('.el-button')) {
    return
  }
  viewCatDetail(row.id)
}

const viewCatDetail = (id) => {
  router.push(`/cats/${id}`)
}

const editCat = (id) => {
  router.push(`/cats/${id}/edit`)
}

const startRescue = async (cat) => {
  try {
    await ElMessageBox.confirm(
      `确定要开始救助"${cat.name}"吗？`,
      '确认开始救助',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    // TODO: 调用API开始救助
    ElMessage.success('救助任务已开始')
    loadCats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const completeRescue = async (cat) => {
  try {
    await ElMessageBox.confirm(
      `确定要完成"${cat.name}"的救助吗？`,
      '确认完成救助',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    // TODO: 调用API完成救助
    ElMessage.success('救助任务已完成')
    loadCats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const deleteCat = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这只猫咪吗？此操作不可恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await catApi.deleteCat(id)
    ElMessage.success('猫咪已删除')
    loadCats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 审核相关函数
const approveCat = async (cat) => {
  try {
    await ElMessageBox.confirm(
      `确定要通过"${cat.name}"的审核吗？审核通过后猫咪将显示在领养列表中。`,
      '确认通过审核',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用审核通过API
    const authStore = useAuthStore()
    const response = await catApi.auditCat(cat.id, 'APPROVED', null, authStore.user?.id)
    ElMessage.success('猫咪审核已通过')
    loadCats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核通过失败:', error)
      ElMessage.error('审核通过失败')
    }
  }
}

const rejectCat = async (cat) => {
  try {
    const { value: remark } = await ElMessageBox.prompt(
      `请输入拒绝"${cat.name}"审核的原因：`,
      '拒绝审核',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请输入拒绝原因...',
        inputValidator: (value) => {
          if (!value || value.trim().length === 0) {
            return '拒绝原因不能为空'
          }
          return true
        }
      }
    )
    
    // 调用审核拒绝API
    const authStore = useAuthStore()
    const response = await catApi.auditCat(cat.id, 'REJECTED', remark, authStore.user?.id)
    ElMessage.success('猫咪审核已拒绝')
    loadCats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核拒绝失败:', error)
      ElMessage.error('审核拒绝失败')
    }
  }
}

const exportCats = () => {
  ElMessage.info('导出功能开发中')
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadCats()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadCats()
}

onMounted(() => {
  loadCats()
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

.cat-management {
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

.stat-card.waiting {
  background: rgba(255, 193, 7, 0.3);
}

.stat-card.in-progress {
  background: rgba(40, 167, 69, 0.3);
}

.stat-card.adopted {
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


.filters-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.cats-table-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
  animation: fadeInUp 0.6s ease ;
}
:deep(.cats-table-section .el-table-column label) {
  font-weight: 700;
}

.cat-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cat-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.cat-name {
  font-weight: 500;
  color: #303133;
}

.cat-breed {
  font-size: 12px;
  color: #909399;
}

.pagination-section {
  display: flex;
  justify-content: center;
  padding: 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  animation: fadeInUp 0.6s ease 0.6s both ;
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

.el-table {
  border-radius: 0;
}

/* 猫咪管理表格样式优化 */
:deep(.el-table) {
  font-size: 14px;
  width: 100% ;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
  color: #303133;
  font-size: 14px;
  padding: 12px 8px;
}

:deep(.el-table th.el-table__cell) {
  background-color: #f5f7fa ;
  color: #303133 ;
}

:deep(.el-table td) {
  padding: 12px 8px;
  font-size: 14px;
}

/* 表格行动画 */
:deep(.el-table__row) {
  animation: fadeInUp 0.6s ease;
}

:deep(.el-table .el-tag) {
  font-size: 12px;
  padding: 4px 8px;
}

:deep(.el-table .el-button) {
  font-size: 12px;
  padding: 6px 12px;
  margin: 0 4px;
}

:deep(.el-table .el-table__row:hover) {
  background-color: #f5f7fa;
}

:deep(.el-table .cat-info) {
  justify-content: center;
}

:deep(.el-table .cat-details) {
  text-align: left;
}

:deep(.el-table .cat-name) {
  font-size: 14px;
  font-weight: 600;
}

:deep(.el-table .cat-breed) {
  font-size: 12px;
}

/* 表格响应式优化 */
@media (max-width: 1200px) {
  :deep(.el-table) {
    font-size: 13px;
  }
  
  :deep(.el-table th),
  :deep(.el-table td) {
    padding: 10px 6px;
  }
}

.el-button + .el-button {
  margin-left: 8px;
}
</style>