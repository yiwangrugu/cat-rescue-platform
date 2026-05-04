<template>
  <!-- 救助需求详情对话框 -->
  <el-dialog 
    :model-value="visible" 
    :title="rescueData?.title || '救助需求详情'" 
    width="1100px" 
    class="rescue-detail-dialog" 
    style="margin-top: 65px;"
    @update:model-value="$emit('update:visible', $event)"
    @close="handleClose"
  >
    <div class="rescue-detail" style="max-height: 550px; overflow-y: auto;">
      <div class="detail-header">
        <div class="detail-tags">
          <el-tag :type="getUrgencyType(rescueData?.urgencyLevel)" size="medium">
            {{ getUrgencyText(rescueData?.urgencyLevel) }}
          </el-tag>
          <el-tag :type="getStatusType(rescueData?.status)" size="medium">
            {{ getStatusText(rescueData?.status) }}
          </el-tag>
        </div>
        <div class="detail-time">
          <el-icon><Timer /></el-icon>
          <span>{{ formatDate(rescueData?.createTime) }}</span>
        </div>
      </div>
      
      <div class="detail-section" v-if="rescueData?.images && rescueData.images.length > 0">
        <h3>相关图片</h3>
        <div class="image-gallery">
          <div 
            v-for="(image, index) in parseImages(rescueData.images)" 
            :key="index" 
            class="image-item"
          >
            <el-image 
              :src="getImageUrl(image.url || image)" 
              :preview-src-list="parseImages(rescueData.images).map(img => getImageUrl(img.url || img))"
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
        <h3>详细描述</h3>
        <p>{{ rescueData?.description || '无描述' }}</p>
      </div>
      
      <!-- 具体位置占一行 -->
      <div class="detail-section">
        <h3>具体位置</h3>
        <p>{{ rescueData?.location || '未知位置' }}</p>
      </div>
      
      <!-- 猫咪状况 -->
      <div class="detail-section" v-if="rescueData?.catCondition">
        <h3>猫咪状况</h3>
        <p>{{ rescueData?.catCondition || '未提供' }}</p>
      </div>
      
      <!-- 联系信息和志愿者信息放一行 -->
      <div class="detail-section contact-row">
        <div class="contact-column">
          <h3>联系信息</h3>
          <div class="contact-info">
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span>{{ rescueData?.reporterUsername || '匿名用户' }}</span>
            </div>
            <div class="info-item">
              <el-icon><Phone /></el-icon>
              <span>{{ rescueData?.contactPhone || '未提供' }}</span>
            </div>
          </div>
        </div>
        
        <div class="contact-column" v-if="rescueData?.volunteerName || rescueData?.volunteerPhone">
          <h3>志愿者信息</h3>
          <div class="contact-info">
            <div class="info-item" v-if="rescueData?.volunteerName">
              <el-icon><User /></el-icon>
              <span>{{ rescueData?.volunteerName }}</span>
            </div>
            <div class="info-item" v-if="rescueData?.volunteerPhone">
              <el-icon><Phone /></el-icon>
              <span>{{ rescueData?.volunteerPhone }}</span>
            </div>
            <div class="info-item" v-if="!rescueData?.volunteerName && !rescueData?.volunteerPhone">
              <el-icon><User /></el-icon>
              <span>暂无志愿者信息</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 负责人信息 -->
      <div class="detail-section" v-if="rescueData?.rescuerId">
        <h3>负责人信息</h3>
        <div class="contact-info">
          <div class="info-item">
            <el-icon><UserFilled /></el-icon>
            <span>负责人ID：{{ rescueData?.rescuerId }}</span>
          </div>
          <div class="info-item" v-if="rescueData?.rescuerUsername">
            <el-icon><User /></el-icon>
            <span>负责人：{{ rescueData?.rescuerUsername }}</span>
          </div>
        </div>
      </div>
      
      <!-- 处理时间 -->
      <div class="detail-section" v-if="rescueData?.acceptTime || rescueData?.completeTime">
        <h3>处理时间</h3>
        <div class="contact-info">
          <div class="info-item" v-if="rescueData?.acceptTime">
            <el-icon><Clock /></el-icon>
            <span>处理时间：{{ formatDate(rescueData?.acceptTime) }}</span>
          </div>
          <div class="info-item" v-if="rescueData?.completeTime">
            <el-icon><Check /></el-icon>
            <span>结束时间：{{ formatDate(rescueData?.completeTime) }}</span>
          </div>
        </div>
      </div>
      
      <!-- 救援日志图片 -->
      <div class="detail-section" v-if="rescueData?.rescueLogImages && rescueData.rescueLogImages.length > 0">
        <h3>救援日志图片</h3>
        <div class="rescue-log-images">
          <div 
            v-for="(image, index) in parseRescueLogImages(rescueData.rescueLogImages)" 
            :key="index" 
            class="log-image-item"
          >
            <el-image 
              :src="getImageUrl(image.url || image)" 
              fit="cover" 
              class="log-image"
              :preview-src-list="parseRescueLogImages(rescueData.rescueLogImages).map(img => getImageUrl(img.url || img))"
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
      <div class="detail-actions">
        <el-button @click="handleClose">关闭</el-button>
        <slot name="actions" :rescue="rescueData">
          <!-- 默认操作按钮插槽 -->
          <el-button 
            type="primary" 
            v-if="rescueData?.status === '待处理' || rescueData?.status === '待处理' || rescueData?.status === 'PENDING'"
            @click="handleTakeRescueClick"
          >
            马上救援
          </el-button>
          <el-button 
            type="success" 
            v-if="rescueData?.status === '进行中' || rescueData?.status === 'IN_PROGRESS'"
            @click="handleCompleteRescueClick"
          >
            救援完成
          </el-button>
          <!-- 已完成状态不显示关闭任务按钮，只显示关闭按钮 -->
        </slot>
      </div>
    </template>
    </el-dialog>

    <!-- 马上救援对话框 -->
    <el-dialog title="马上救援" v-model="showTakeRescueDialog" width="600px">
      <el-form :model="takeRescueForm" :rules="takeRescueRules" ref="takeRescueFormRef" label-width="100px">
        <el-form-item label="选择志愿者">
          <el-select v-model="takeRescueForm.selectedVolunteerId" placeholder="今日值班志愿者" clearable @change="handleVolunteerSelect">
            <el-option 
              v-for="volunteer in volunteerList" 
              :key="volunteer.id" 
              :label="`${volunteer.name} - ${volunteer.phone}`" 
              :value="volunteer.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="志愿者姓名" prop="volunteerName">
          <el-input v-model="takeRescueForm.volunteerName" placeholder="请输入志愿者姓名" />
        </el-form-item>
        <el-form-item label="志愿者电话" prop="volunteerPhone">
          <el-input v-model="takeRescueForm.volunteerPhone" placeholder="请输入志愿者电话" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showTakeRescueDialog = false">取消</el-button>
        <el-button type="primary" @click="submitTakeRescue" :loading="taking">确定救援</el-button>
      </div>
    </el-dialog>

    <!-- 救援完成弹窗 -->
    <el-dialog
      v-model="showCompleteDialog"
      title="救援完成"
      width="600px"
      :before-close="() => showCompleteDialog = false"
    >
      <div class="complete-rescue-dialog">
        <p>请上传救援完成后的图片作为救援日志：</p>
        
        <el-upload
          v-model:file-list="rescueLogImages"
          list-type="picture-card"
          :auto-upload="false"
          :multiple="true"
          :limit="5"
          accept="image/*"
          :on-change="handleImageChange"
        >
          <el-icon><Plus /></el-icon>
          <div class="el-upload__text">
            上传图片<br />
            <span style="font-size: 12px; color: #999;">最多5张</span>
          </div>
        </el-upload>
        
        <div class="upload-tips">
          <el-text type="info" size="small">
            上传救援完成后的现场照片，如猫咪被救助后的状态、救援环境等
          </el-text>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCompleteDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            :loading="completing"
            @click="handleCompleteRescueProcess"
          >
            确认完成
          </el-button>
        </span>
      </template>
    </el-dialog>
</template>

<script setup>
import { ref, watch, computed, onMounted } from 'vue'
import { Timer, Picture, User, Phone, UserFilled, Clock, Check, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { volunteerApi } from '@/api/volunteer.js'
import { rescueApi } from '@/api/rescue.js'
import { useAuthStore } from '@/stores/auth'

// 定义组件属性
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  rescueData: {
    type: Object,
    default: () => ({})
  }
})

// 定义组件事件
const emit = defineEmits(['update:visible', 'close', 'take-rescue', 'complete-rescue', 'close-rescue'])

// 马上救援相关数据
const showTakeRescueDialog = ref(false)
const taking = ref(false)
const volunteerList = ref([])

// 加载今日值班志愿者列表
const loadVolunteers = async () => {
  try {
    const response = await volunteerApi.getTodayOnDutyVolunteers()
    volunteerList.value = response.data || []
  } catch (error) {
    console.error('加载今日值班志愿者列表失败:', error)
    ElMessage.error('加载今日值班志愿者列表失败')
  }
}

// 组件挂载时加载志愿者数据
onMounted(() => {
  loadVolunteers()
})
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

const takeRescueFormRef = ref(null)

// 救援完成相关数据
const showCompleteDialog = ref(false)
const completing = ref(false)
const rescueLogImages = ref([])

// 处理对话框关闭
const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

// 时间格式化函数
const formatDate = (dateTime) => {
  if (!dateTime) return '未知时间'
  const dateObj = new Date(dateTime)
  return dateObj.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 紧急程度标签类型
const getUrgencyType = (urgencyLevel) => {
  switch (urgencyLevel) {
    case 'CRITICAL': return 'danger'
    case 'HIGH': return 'danger'
    case 'MEDIUM': return 'warning'
    case 'LOW': return 'info'
    default: return ''
  }
}

// 紧急程度文本
const getUrgencyText = (urgencyLevel) => {
  switch (urgencyLevel) {
    case 'CRITICAL': return '非常紧急'
    case 'HIGH': return '紧急'
    case 'MEDIUM': return '中等'
    case 'LOW': return '低'
    default: return urgencyLevel || '未知'
  }
}

// 状态标签类型
const getStatusType = (status) => {
  switch (status) {
    case '待处理':
    case 'PENDING': return 'danger'
    case '进行中':
    case 'IN_PROGRESS': return 'warning'
    case '已完成':
    case 'COMPLETED': return 'success'
    default: return ''
  }
}

// 状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待处理',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    '待处理': '待处理',
    '进行中': '进行中',
    '已完成': '已完成'
  }
  return statusMap[status] || status || '未知'
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
      if (Array.isArray(parsed)) {
        return parsed
      }
      return []
    }
    
    // 如果是数组，直接返回
    if (Array.isArray(imagesData)) {
      // 如果是字符串数组，转换为对象数组
      if (imagesData.length > 0 && typeof imagesData[0] === 'string') {
        return imagesData.map(url => ({ url, name: '图片' }))
      }
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

// 获取图片URL
const getImageUrl = (image) => {
  if (!image) return ''
  
  // 如果是完整URL，直接返回
  if (typeof image === 'string' && (image.startsWith('http') || image.startsWith('/'))) {
    return image
  }
  
  // 如果是对象，尝试获取url属性
  if (typeof image === 'object' && image.url) {
    return image.url
  }
  
  // 如果是相对路径，添加基础URL
  if (typeof image === 'string') {
    return `http://localhost:8080${image.startsWith('/') ? image : '/' + image}`
  }
  
  return image
}

// 马上救援相关函数
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

const submitTakeRescue = async () => {
  if (!takeRescueFormRef.value || !props.rescueData?.id) return
  
  try {
    await takeRescueFormRef.value.validate()
    taking.value = true
    
    // 调用API接受救援任务
    const volunteerData = {
      volunteerName: takeRescueForm.value.volunteerName,
      volunteerPhone: takeRescueForm.value.volunteerPhone
    }
    
    const response = await rescueApi.takeRescueWithVolunteer(props.rescueData.id, volunteerData)
    
    // 关闭所有对话框
    showTakeRescueDialog.value = false
    emit('update:visible', false)
    
    // 重置表单
    takeRescueForm.value = {
      selectedVolunteerId: null,
      volunteerName: '',
      volunteerPhone: ''
    }
    
    // 通知父组件更新数据，传递更新后的救援数据
    emit('take-rescue', response.data || props.rescueData)
    
    ElMessage.success('救援任务已接受')
  } catch (error) {
    console.error('接受救援任务失败:', error)
    ElMessage.error('接受救援任务失败')
  } finally {
    taking.value = false
  }
}



// 处理马上救援按钮点击
const handleTakeRescueClick = () => {
  // 显示马上救援对话框
  showTakeRescueDialog.value = true
  
  // 同时触发事件，让父组件知道点击了马上救援按钮
  emit('take-rescue', props.rescueData)
}

// 处理图片变化
const handleImageChange = (file, fileList) => {
  // 确保fileList是最新的
  rescueLogImages.value = fileList
}

// 处理救援完成按钮点击
const handleCompleteRescueClick = () => {
  // 显示救援完成对话框
  showCompleteDialog.value = true
  
  // 同时触发事件，让父组件知道点击了救援完成按钮
  emit('complete-rescue', props.rescueData)
}

// 救援完成相关函数
const handleCompleteRescueProcess = async () => {
  if (!props.rescueData?.id) return
  
  try {
    completing.value = true
    
    let response
    
    // 获取当前用户信息作为负责人ID
    const authStore = useAuthStore()
    const rescuerId = authStore.user?.id || props.rescueData?.rescuerId
    
    console.log('当前用户信息:', authStore.user)
    console.log('获取到的负责人ID:', rescuerId)
    console.log('救助数据中的负责人ID:', props.rescueData?.rescuerId)
    
    // 直接上传图片文件到救援完成API
    if (rescueLogImages.value.length > 0) {
      console.log('开始上传救援日志图片，数量:', rescueLogImages.value.length)
      
      // 构建FormData，直接包含图片文件
      const formData = new FormData()
      
      // 添加负责人ID到FormData
      if (rescuerId) {
        console.log('添加负责人ID到FormData:', rescuerId)
        formData.append('rescuerId', rescuerId.toString()) // 确保是字符串格式
      }
      
      // 添加所有图片文件到FormData
      rescueLogImages.value.forEach((file, index) => {
        if (file.raw) {
          console.log('添加图片文件:', file.raw.name)
          formData.append('rescueLogImages', file.raw)
        }
      })
      
      console.log('FormData内容:', formData)
      
      response = await rescueApi.completeRescueWithImages(props.rescueData.id, formData)
      console.log('带图片完成响应:', response)
    } else {
      // 没有图片，使用普通完成API，传递负责人ID
      console.log('使用普通完成API')
      response = await rescueApi.completeRescue(props.rescueData.id, { params: { rescuerId } })
      console.log('普通完成响应:', response)
    }
    
    // 关闭所有对话框
    showCompleteDialog.value = false
    emit('update:visible', false)
    
    // 重置图片列表
    rescueLogImages.value = []
    
    // 通知父组件更新数据，传递更新后的救援数据
    console.log('通知父组件更新数据:', response?.data)
    emit('complete-rescue', response?.data || props.rescueData)
    
    // 只在组件内部显示一次成功消息
    ElMessage.success('救援任务已完成')
  } catch (error) {
    console.error('救援完成失败:', error)
    ElMessage.error('救援完成失败: ' + (error.response?.data?.message || error.message))
  } finally {
    completing.value = false
  }
}
</script>

<style scoped>
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

.contact-row {
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

.contact-column {
  flex: 1;
  min-width: 0;
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

/* 确保所有info-item元素都应用正确的样式 */
.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
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
}

.log-image:hover {
  transform: scale(1.05);
}

/* 救助需求详情对话框样式 */
.rescue-detail-dialog {
  max-height: 700px !important;
  overflow: hidden !important;
}

.rescue-detail-dialog .el-dialog__body {
  padding: 0 !important;
  overflow: hidden !important;
  max-height: 550px !important;
}

/* 图片加载失败样式 */
.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.image-error .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.no-images {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
  margin-top: 15px;
}

/* 操作按钮样式 */
.detail-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
</style>