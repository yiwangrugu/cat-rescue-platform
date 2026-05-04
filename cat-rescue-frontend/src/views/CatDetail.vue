<template>
  <div class="cat-detail-container">
    
      <!-- 返回上一路由按钮 -->
       <div class="back">
      <el-button 
        type="text" 
        @click="goBack" 
        class="back-button"
      >
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      </div>
    
    <div class="cat-detail" v-if="cat.id">
      <el-row :gutter="40">
        <el-col :span="12">
          <div class="cat-images">
            <!-- 图片轮播展示 -->
            <div class="carousel-container">
              <div class="carousel-wrapper">
                <div 
                  class="carousel-slide" 
                  :style="{ transform: `translateX(-${currentImageIndex * 100}%)` }"
                >
                  <div 
                    v-for="(image, index) in catImages" 
                    :key="index" 
                    class="carousel-item"
                  >
                    <img 
                      :src="image" 
                      :alt="cat.name" 
                      class="carousel-image" 
                      @error="handleImageError"
                      loading="lazy"
                    />
                  </div>
                </div>
                
                <!-- 轮播导航按钮 -->
                <button 
                  v-if="catImages.length > 1"
                  class="carousel-btn carousel-prev"
                  @click="prevImage"
                  :disabled="currentImageIndex === 0"
                >
                  <el-icon><ArrowLeft /></el-icon>
                </button>
                <button 
                  v-if="catImages.length > 1"
                  class="carousel-btn carousel-next"
                  @click="nextImage"
                  :disabled="currentImageIndex === catImages.length - 1"
                >
                  <el-icon><ArrowRight /></el-icon>
                </button>
                
                <!-- 轮播指示器 -->
                <div v-if="catImages.length > 1" class="carousel-indicators">
                  <div 
                    v-for="(image, index) in catImages" 
                    :key="index" 
                    class="indicator"
                    :class="{ active: currentImageIndex === index }"
                    @click="goToImage(index)"
                  ></div>
                </div>
              </div>
            </div>
            
            <!-- 缩略图展示 -->
            <div v-if="catImages.length > 1" class="thumbnail-gallery">
              <div 
                v-for="(image, index) in catImages" 
                :key="index" 
                class="thumbnail-item"
                :class="{ active: currentImageIndex === index }"
                @click="goToImage(index)"
              >
                <img 
                  :src="image" 
                  :alt="cat.name" 
                  class="thumbnail-image" 
                  @error="handleImageError"
                  loading="lazy"
                />
              </div>
            </div>
            
            <div class="cat-status" :class="getStatusClass(cat.status)">
              {{ getStatusText(cat.status) }}
            </div>
          </div>
        </el-col>

        <el-col :span="12">
          <div class="cat-info">
            <h1>{{ cat.name }}</h1>
            <div class="basic-info">
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="info-item">
                    <span class="label">品种：</span>
                    <span class="value">{{ cat.breed || '未知' }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <span class="label">年龄：</span>
                    <span class="value">{{ formatAge(cat.age) }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <span class="label">性别：</span>
                    <span class="value">{{ getGenderText(cat.gender) }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <span class="label">体重：</span>
                    <span class="value">{{ formatWeight(cat.weight) }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <span class="label">健康状况：</span>
                    <span class="value">{{ getHealthText(cat.healthStatus) }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <span class="label">位置：</span>
                    <span class="value">{{ cat.location || '未知' }}</span>
                  </div>
                </el-col>
              </el-row>
            </div>

            <div class="medical-info">
              <h3>医疗信息</h3>
              <p>{{ cat.medicalInfo || '暂无医疗信息' }}</p>
            </div>

            <div class="description">
              <h3>猫咪描述</h3>
              <p>{{ cat.description || '暂无详细描述' }}</p>
            </div>

            <div class="personality-info">
              <h3>性格特点</h3>
              <p>{{ cat.personality || '暂无性格描述' }}</p>
            </div>

            <div class="actions">
              <el-button 
                type="primary" 
                size="large" 
                @click="goToAdoptionForm"
              >
                申请领养
              </el-button>
              <el-button 
                size="large" 
                :type="isFavorited ? 'warning' : 'default'"
                @click="handleFavorite"
                :loading="favoriteLoading"
              >
                <el-icon><Star /></el-icon>
                {{ isFavorited ? '已收藏' : '收藏猫咪' }}
              </el-button>
              <el-button size="large" @click="goToCatList">
                <el-icon><ArrowLeft /></el-icon>
                返回列表
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="loading" v-else-if="loading">
      <el-skeleton :rows="6" animated />
    </div>

    <div class="error" v-else>
      <el-result icon="error" title="猫咪信息加载失败" sub-title="请检查网络连接或稍后重试">
        <template #extra>
          <el-button type="primary" @click="loadCatDetail">重新加载</el-button>
        </template>
      </el-result>
    </div>

    <!-- 领养申请表单已移至独立页面 -->
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { catApi } from '@/api/cat'
import { adoptionApi } from '@/api/adoption'
import { catFavoriteApi } from '@/api/catFavorite'
import { useAuthStore } from '@/stores/auth.js'
import { ZoomIn, ArrowLeft, ArrowRight, Star } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const cat = ref({})
const loading = ref(true)
const hasShownAdoptionWarning = ref(false)

// 收藏相关
const isFavorited = ref(false)
const favoriteLoading = ref(false)

// 轮播相关
const currentImageIndex = ref(0)
const autoPlayInterval = ref(null)

// 自动轮播
const startAutoPlay = () => {
  console.log('启动自动轮播，图片数量:', catImages.value.length)
  if (catImages.value.length > 1) {
    stopAutoPlay()
    autoPlayInterval.value = setInterval(() => {
      console.log('自动切换到下一张图片')
      nextImage()
    }, 5000) // 5秒切换一次
  } else {
    console.log('图片数量不足，不启动自动轮播')
  }
}

// 停止自动轮播
const stopAutoPlay = () => {
  if (autoPlayInterval.value) {
    clearInterval(autoPlayInterval.value)
    autoPlayInterval.value = null
  }
}

// 切换到指定图片
const goToImage = (index) => {
  currentImageIndex.value = index
  stopAutoPlay()
  startAutoPlay()
}

// 上一张图片
const prevImage = () => {
  console.log('切换到上一张图片，当前索引:', currentImageIndex.value)
  if (currentImageIndex.value > 0) {
    currentImageIndex.value--
  } else {
    currentImageIndex.value = catImages.value.length - 1
  }
  console.log('切换后索引:', currentImageIndex.value)
  stopAutoPlay()
  startAutoPlay()
}

// 下一张图片
const nextImage = () => {
  console.log('切换到下一张图片，当前索引:', currentImageIndex.value)
  if (currentImageIndex.value < catImages.value.length - 1) {
    currentImageIndex.value++
  } else {
    currentImageIndex.value = 0
  }
  console.log('切换后索引:', currentImageIndex.value)
  stopAutoPlay()
  startAutoPlay()
}

const catImages = computed(() => {
  if (cat.value.images) {
    try {
      let images = cat.value.images
      
      // 如果images是字符串，尝试解析JSON
      if (typeof images === 'string') {
        images = JSON.parse(images)
      }
      
      // 确保images是数组
      if (!Array.isArray(images)) {
        images = [images]
      }
      
      // 处理图片URL，确保是完整路径
          const processedImages = images.map(image => {
            if (typeof image === 'string') {
              // 如果是完整URL，直接返回
              if (image.startsWith('http')) {
                return image
              }
              // 如果是相对路径，检查是否是上传的图片
              if (image.startsWith('/uploads/')) {
                // 上传的图片，添加后端基础URL
                return `http://localhost:8080${image}`
              }
              // 处理相对路径（如../public/img/cat1.jpg）
              if (image.startsWith('../')) {
                // 移除../前缀，转换为正确的静态资源路径
                const cleanPath = image.replace('../', '')
                // 移除public前缀，Vite会自动处理
                const finalPath = cleanPath.replace('public/', '')
                // 确保路径以/img/开头
                return finalPath.startsWith('/img/') ? finalPath : '/img/' + finalPath
              }
              // 如果是静态资源路径（/img/开头），直接返回
              if (image.startsWith('/img/')) {
                return image
              }
              // 其他相对路径，使用Vite的静态资源处理
              if (image.startsWith('/')) {
                // 已经是绝对路径，检查是否需要添加/img/前缀
                if (!image.startsWith('/uploads') && !image.startsWith('/img')) {
                  // 如果是根路径且不是uploads或img开头，添加img前缀
                  return `/img${image}`
                }
                return image
              } else {
                // 相对路径（如cat1.jpg），添加/img/前缀
                return `/img/${image}`
              }
            }
            // 如果是对象，尝试获取url属性
            if (typeof image === 'object' && image.url) {
              return image.url
            }
            return image
          })
      
      // 确保至少有一张图片
      return processedImages.length > 0 ? processedImages : ['/img/placeholder-cat.jpg']
    } catch (error) {
      console.error('图片解析错误:', error)
      return ['/img/placeholder-cat.jpg']
    }
  }
  return ['/img/placeholder-cat.jpg']
})

// 领养申请表单已移至独立页面

const getStatusText = (status) => {
  const texts = {
    '待领养': '待领养',
    '已领养': '已领养',
    '照顾中': '照顾中',
  }
  return texts[status] || status || '未知'
}

const getStatusClass = (status) => {
  const classes = {
    '待领养': 'status-waiting',
    '已领养': 'status-adopted',
    '照顾中': 'status-caring',
    '已拒绝': 'status-rejected'
  }
  return classes[status] || 'status-unknown'
}


const getGenderText = (gender) => {
  const texts = {
    'MALE': '公猫',
    'FEMALE': '母猫',
    'UNKNOWN': '未知'
  }
  return texts[gender] || gender
}

const getHealthText = (health) => {
  const texts = {
    'HEALTHY': '健康',
    'NEEDS_CARE': '需要照顾',
    'SICK': '生病',
    'DISABLED': '残疾',
    'RECOVERING': '恢复中'
  }
  return texts[health] || health
}


const formatAge = (ageInMonths) => {
  if (!ageInMonths) return '年龄未知'
  
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

const formatWeight = (weight) => {
  if (!weight) return '未知'
  return `${weight.toFixed(1)}kg`
}

const loadCatDetail = async () => {
  try {
    loading.value = true
    const catId = route.params.id
    
    // 调用API获取真实数据
    const response = await catApi.getCat(catId)
    cat.value = response.data
    
    // 加载收藏状态
    await checkFavoriteStatus()
    
    // 调试信息：打印猫咪数据和图片信息
    console.log('猫咪数据:', cat.value)
    console.log('图片字段类型:', typeof cat.value.images)
    console.log('图片字段内容:', cat.value.images)
    console.log('解析后的图片:', catImages.value)
    console.log('图片数量:', catImages.value.length)
    console.log('当前轮播索引:', currentImageIndex.value)
    
  } catch (error) {
    console.error('加载猫咪详情失败:', error)
    ElMessage.error('加载猫咪详情失败')
    
    // 如果API调用失败，使用默认数据
    cat.value = {
      id: catId,
      name: '猫咪信息加载失败',
      breed: '未知',
      age: 0,
      weight: 0,
      gender: 'UNKNOWN',
      healthStatus: '未知',
      location: '未知',
      description: '无法加载猫咪信息，请稍后重试。',
      status: '未知',
      images: JSON.stringify(['/img/placeholder-cat.jpg'])
    }
  } finally {
    loading.value = false
  }
}

const goToAdoptionForm = () => {
  if (!authStore.isAuthenticated) {
    ElMessageBox.confirm('请先登录才能申请领养，是否跳转到登录页面？', '需要登录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push('/login')
    }).catch(() => {
      // 用户取消登录
    })
    return
  }
  
  // 检查猫咪状态，如果不是待领养状态则提示并返回
  if (cat.value.status !== '待领养') {
    // 避免重复显示警告
    if (!hasShownAdoptionWarning.value) {
      ElMessage.warning('该猫咪暂时不可领养')
      hasShownAdoptionWarning.value = true
    }
    return
  }
  
  // 重置警告标志
  hasShownAdoptionWarning.value = false
  
  // 跳转到领养申请页面，传递猫咪ID
  router.push({
    path: '/adoption/form',
    query: { catId: cat.value.id }
  })
}

// 返回猫咪列表
const goToCatList = () => {
  router.push('/cats')
}

// 返回上一个路由
const goBack = () => {
  router.back()
}

// 图片加载错误处理
const handleImageError = (event) => {
  const img = event.target
  // 防止重复处理错误
  if (img.src.includes('placeholder-cat.jpg')) {
    return
  }

  // 设置占位图片
  img.src = '/img/placeholder-cat.jpg'
  
  // 移除错误监听器，防止重复触发
  img.onerror = null
}

// 检查收藏状态
const checkFavoriteStatus = async () => {
  if (!authStore.isAuthenticated) {
    isFavorited.value = false
    return
  }
  
  try {
    const response = await catFavoriteApi.checkFavoriteStatus(cat.value.id)
    isFavorited.value = response.data
  } catch (error) {
    console.error('检查收藏状态失败:', error)
    isFavorited.value = false
  }
}

// 处理收藏操作
const handleFavorite = async () => {
  if (!authStore.isAuthenticated) {
    ElMessageBox.confirm('请先登录才能收藏猫咪，是否跳转到登录页面？', '需要登录', {
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
  
  favoriteLoading.value = true
  
  try {
    if (isFavorited.value) {
      // 取消收藏
      await ElMessageBox.confirm(
        '确定要取消收藏这只猫咪吗？',
        '取消收藏',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      await catFavoriteApi.unfavoriteCat(cat.value.id)
      isFavorited.value = false
      ElMessage.success('取消收藏成功')
    } else {
      // 收藏猫咪
      await catFavoriteApi.favoriteCat(cat.value.id)
      isFavorited.value = true
      ElMessage.success('收藏猫咪成功')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('收藏操作失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  } finally {
    favoriteLoading.value = false
  }
}

// 图片加载成功处理
const handleImageLoad = (event) => {
  const img = event.target
  // 图片加载成功，移除错误监听器
  img.onerror = null
}

// 图片预览相关函数（已弃用，保留函数避免错误）
const openImagePreview = (index) => {
  // 此函数已不再使用，但保留以避免错误
  console.log('图片预览功能已弃用')
}

// 注意：prevImage和nextImage函数已在轮播功能中重新定义

onMounted(() => {
  loadCatDetail()
})

// 监听猫咪数据变化，启动自动轮播
watch(catImages, (newImages) => {
  if (newImages.length > 0) {
    // 重置当前索引
    currentImageIndex.value = 0
    // 启动自动轮播
    startAutoPlay()
  }
}, { immediate: true })

// 组件卸载时清理定时器
onUnmounted(() => {
  stopAutoPlay()
})
</script>

<style scoped>
.cat-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  gap: 20px;
}


.cat-detail {
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.cat-images {
  position: relative;
}

/* 轮播容器 */
.carousel-container {
  position: relative;
  width: 100%;
  height: 550px;
  overflow: hidden;
  border-radius: 8px;
  margin-bottom: 15px;
  background: #f5f5f5;
}

.carousel-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

.carousel-slide {
  display: flex;
  transition: transform 0.5s ease;
  height: 100%;
}

.carousel-item {
  flex: 0 0 100%;
  height: 100%;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 轮播导航按钮 */
.carousel-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.8);
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 10;
}

.carousel-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-50%) scale(1.1);
}

.carousel-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.carousel-prev {
  left: 15px;
}

.carousel-next {
  right: 15px;
}

/* 轮播指示器 */
.carousel-indicators {
  position: absolute;
  bottom: 15px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  z-index: 10;
}

.indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
}

.indicator.active {
  background: #fff;
  transform: scale(1.2);
}

.indicator:hover {
  background: rgba(255, 255, 255, 0.8);
}

/* 缩略图展示 */
.thumbnail-gallery {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-bottom: 20px;
}

.thumbnail-item {
  width: 80px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.thumbnail-item.active {
  border-color: #409eff;
}

.thumbnail-item:hover {
  transform: scale(1.05);
}

.thumbnail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cat-status {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 6px 12px;
  border-radius: 4px;
  color: white;
  font-size: 14px;
  font-weight: bold;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.status-waiting { background: #67c23a ; }
.status-adopted { background: #64cbf3 ; }
.status-caring { background: #e6a23c ; }
.status-rejected { background: #f56c6c ; }
.status-unknown { background: #909399 ; }

.cat-info h1 {
  color: #333;
  margin-bottom: 20px;
  font-size: 28px;
}

.basic-info {
  margin-bottom: 30px;
}

.info-item {
  margin-bottom: 15px;
  font-size: 16px;
}

.label {
  color: #666;
  font-weight: bold;
}

.value {
  color: #333;
}

.medical-info {
  margin-bottom: 30px;
}

.medical-info h3 {
  margin-bottom: 15px;
  color: #333;
}

.medical-item {
  margin-bottom: 10px;
}

.description {
  margin-bottom: 30px;
}

.description h3 {
  margin-bottom: 15px;
  color: #333;
}

.description p {
  line-height: 1.6;
}

.temperament {
  margin-bottom: 30px;
}

.temperament h3 {
  margin-bottom: 15px;
  color: #333;
}

.actions {
  margin-top: 30px;
}

.loading {
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.error {
  background: white;
  border-radius: 8px;
  padding: 50px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.adoption-form-content {
  padding: 20px 0;
}

.cat-preview {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 24px;
}

.cat-avatar {
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.cat-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.cat-breed {
  font-size: 14px;
  color: #606266;
}

.adoption-form {
  max-height: 600px;
  overflow-y: auto;
  padding-right: 10px;
}

.adoption-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.adoption-form :deep(.el-textarea) {
  resize: vertical;
}

.adoption-form :deep(.el-checkbox) {
  margin-top: 8px;
}

.back{
  font-size: 24px;
  color: #606266;
  margin-bottom: 20px;
}

.back-button {
  font-size: 18px;
  padding: 12px 20px;
  height: auto;
}

.back-button .el-icon {
  font-size: 20px;
  margin-right: 8px;
}

/* 自定义滚动条 */
.adoption-form::-webkit-scrollbar {
  width: 6px;
}

.adoption-form::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.adoption-form::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.adoption-form::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>