<template>
  <div class="cat-audit-container">
    <div class="header">
      <h1>猫咪信息审核</h1>
      <p>审核救助端提交的猫咪信息，确保信息准确合规</p>
    </div>

    <!-- 审核列表 -->
    <div class="audit-list">
      <el-card v-for="cat in pendingCats" :key="cat.id" class="audit-card">
        <div class="cat-info">
          <!-- 猫咪图片 -->
          <div class="cat-images">
            <img 
              v-for="(image, index) in getCatImages(cat)" 
              :key="index"
              :src="image" 
              :alt="cat.name"
              class="cat-image"
            />
          </div>
          
          <!-- 猫咪基本信息 -->
          <div class="cat-details">
            <h3>{{ cat.name }}</h3>
            <div class="cat-meta">
              <span class="breed">{{ cat.breed || '未知品种' }}</span>
              <span class="age">{{ formatAge(cat.age) }}</span>
              <span class="gender">{{ getGenderText(cat.gender) }}</span>
            </div>
            <p class="description">{{ cat.description || '暂无描述' }}</p>
            <div class="cat-status">
              <el-tag :type="getStatusType(cat.status)">
                {{ getStatusText(cat.status) }}
              </el-tag>
              <el-tag type="warning">待审核</el-tag>
            </div>
          </div>
        </div>

        <!-- 审核操作 -->
        <div class="audit-actions">
          <el-button 
            type="success" 
            @click="handleApprove(cat.id)"
            :loading="loadingApprove === cat.id">
            <el-icon><Check /></el-icon>
            审核通过
          </el-button>
          
          <el-button 
            type="danger" 
            @click="handleReject(cat.id)"
            :loading="loadingReject === cat.id">
            <el-icon><Close /></el-icon>
            审核拒绝
          </el-button>
          
          <el-button 
            type="primary" 
            @click="showRemarkDialog(cat)">
            <el-icon><Edit /></el-icon>
            添加备注
          </el-button>
        </div>
      </el-card>

      <!-- 空状态 -->
      <div v-if="pendingCats.length === 0" class="empty-state">
        <el-empty description="暂无待审核的猫咪信息" />
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[5, 10, 20]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 审核备注对话框 -->
    <el-dialog
      v-model="remarkDialogVisible"
      title="审核备注"
      width="500px">
      <el-input
        v-model="remarkText"
        type="textarea"
        :rows="4"
        placeholder="请输入审核备注（可选）"
      />
      <template #footer>
        <el-button @click="remarkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAuditWithRemark">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close, Edit } from '@element-plus/icons-vue'
import { catApi } from '@/api/cat.js'
import { adminApi } from '@/api/admin.js'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 判断是否是管理端
const isAdminPage = computed(() => {
  return window.location.pathname.includes('/admin')
})

// 数据
const pendingCats = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载状态
const loadingApprove = ref(null)
const loadingReject = ref(null)

// 备注对话框
const remarkDialogVisible = ref(false)
const remarkText = ref('')
const currentCatId = ref(null)
const currentAuditType = ref('') // 'approve' or 'reject'

// 加载待审核猫咪列表
const loadPendingCats = async () => {
  try {
    let response
    if (isAdminPage.value) {
      response = await adminApi.getPendingCats(currentPage.value, pageSize.value)
    } else {
      response = await catApi.getPendingCats(currentPage.value, pageSize.value)
    }
    const data = response.data
    pendingCats.value = isAdminPage.value ? (data.data || []) : (data.records || [])
    total.value = data.total || 0
  } catch (error) {
    console.error('加载待审核猫咪失败:', error)
    ElMessage.error('加载待审核猫咪失败')
  }
}

// 获取猫咪图片
const getCatImages = (cat) => {
  // 优先使用临时图片，如果没有则使用images字段
  if (cat.tempImages) {
    try {
      let images = JSON.parse(cat.tempImages)
      // 处理数据库中的图片路径，确保能够正确加载
      if (Array.isArray(images)) {
        return images.map(imagePath => {
          if (imagePath.startsWith('/') && !imagePath.startsWith('/uploads') && !imagePath.startsWith('/img')) {
            // 如果是根路径且不是uploads或img开头，添加img前缀
            return `/img${imagePath}`
          } else if (imagePath.includes('../public/img/')) {
            // 处理相对路径，转换为前端可访问的路径
            return imagePath.replace('../public/img/', '/img/')
          }
          return imagePath
        })
      }
      return images
    } catch {
      // 解析失败，使用images字段
    }
  }
  
  if (cat.images) {
    try {
      let images = JSON.parse(cat.images)
      // 处理数据库中的图片路径，确保能够正确加载
      if (Array.isArray(images)) {
        return images.map(imagePath => {
          if (imagePath.startsWith('/') && !imagePath.startsWith('/uploads') && !imagePath.startsWith('/img')) {
            // 如果是根路径且不是uploads或img开头，添加img前缀
            return `/img${imagePath}`
          } else if (imagePath.includes('../public/img/')) {
            // 处理相对路径，转换为前端可访问的路径
            return imagePath.replace('../public/img/', '/img/')
          }
          return imagePath
        })
      }
      return images
    } catch {
      return []
    }
  }
  
  if (cat.officialImages) {
    try {
      let images = JSON.parse(cat.officialImages)
      // 处理数据库中的图片路径，确保能够正确加载
      if (Array.isArray(images)) {
        return images.map(imagePath => {
          if (imagePath.startsWith('/') && !imagePath.startsWith('/uploads') && !imagePath.startsWith('/img')) {
            // 如果是根路径且不是uploads或img开头，添加img前缀
            return `/img${imagePath}`
          } else if (imagePath.includes('../public/img/')) {
            // 处理相对路径，转换为前端可访问的路径
            return imagePath.replace('../public/img/', '/img/')
          }
          return imagePath
        })
      }
      return images
    } catch {
      // 解析失败，返回空数组
    }
  }
  
  return []
}

// 格式化年龄
const formatAge = (ageInMonths) => {
  if (!ageInMonths) return '未知年龄'
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

// 获取性别文本
const getGenderText = (gender) => {
  const genders = {
    'MALE': '公',
    'FEMALE': '母',
    'UNKNOWN': '未知'
  }
  return genders[gender] || '未知'
}

// 获取状态文本
const getStatusText = (status) => {
  const statuses = {
    'WAITING_ADOPTION': '待领养',
    'ADOPTED': '已领养',
    'UNDER_CARE': '照顾中',
    'DECEASED': '已故'
  }
  return statuses[status] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    'WAITING_ADOPTION': 'success',
    'ADOPTED': 'info',
    'UNDER_CARE': 'warning',
    'DECEASED': 'danger'
  }
  return types[status] || 'info'
}

// 审核通过
const handleApprove = async (catId) => {
  try {
    loadingApprove.value = catId
    await ElMessageBox.confirm('确定要通过该猫咪信息的审核吗？', '审核确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    if (isAdminPage.value) {
      await adminApi.reviewCat(catId, 'approve', '')
    } else {
      await catApi.auditCat(catId, 'APPROVED', '', authStore.user?.id)
    }
    ElMessage.success('审核通过成功')
    await loadPendingCats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核通过失败:', error)
      ElMessage.error('审核通过失败')
    }
  } finally {
    loadingApprove.value = null
  }
}

// 审核拒绝
const handleReject = (catId) => {
  currentCatId.value = catId
  currentAuditType.value = 'reject'
  remarkDialogVisible.value = true
}

// 显示备注对话框
const showRemarkDialog = (cat) => {
  currentCatId.value = cat.id
  currentAuditType.value = 'approve'
  remarkText.value = ''
  remarkDialogVisible.value = true
}

// 带备注的审核操作
const handleAuditWithRemark = async () => {
  try {
    const action = currentAuditType.value === 'approve' ? 'approve' : 'reject'
    
    if (isAdminPage.value) {
      await adminApi.reviewCat(currentCatId.value, action, remarkText.value)
    } else {
      const status = currentAuditType.value === 'approve' ? 'APPROVED' : 'REJECTED'
      await catApi.auditCat(currentCatId.value, status, remarkText.value, authStore.user?.id)
    }
    
    ElMessage.success(`审核${currentAuditType.value === 'approve' ? '通过' : '拒绝'}成功`)
    remarkDialogVisible.value = false
    remarkText.value = ''
    await loadPendingCats()
  } catch (error) {
    console.error('审核操作失败:', error)
    ElMessage.error('审核操作失败')
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadPendingCats()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadPendingCats()
}

// 初始化
onMounted(() => {
  loadPendingCats()
})
</script>

<style scoped>
.cat-audit-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  margin-bottom: 30px;
  text-align: center;
}

.header h1 {
  color: #333;
  margin-bottom: 10px;
}

.header p {
  color: #666;
  font-size: 16px;
}

.audit-card {
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
}

.cat-info {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.cat-images {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.cat-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.cat-details {
  flex: 1;
}

.cat-details h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 20px;
}

.cat-meta {
  display: flex;
  gap: 15px;
  margin-bottom: 10px;
  color: #666;
}

.cat-meta span {
  padding: 4px 8px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
}

.description {
  color: #666;
  line-height: 1.6;
  margin-bottom: 10px;
}

.cat-status {
  display: flex;
  gap: 10px;
}

.audit-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  border-top: 1px solid #e4e7ed;
  padding-top: 20px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .cat-info {
    flex-direction: column;
  }
  
  .audit-actions {
    flex-direction: column;
  }
}
</style>