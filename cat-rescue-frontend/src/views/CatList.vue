<template>
  <div class="cat-list-container">
    <div class="header">
      <div class="header-content">
        <h1><el-icon><Management /></el-icon> 猫咪列表</h1>
        <p>浏览待领养的可爱猫咪，为它们找到温暖的家</p>
      </div>
    </div>
  </div>

    <!-- 搜索和筛选 -->
    <div class="filters">
      <el-row :gutter="20">
        <el-col :span="4">
          <el-input
            v-model="searchText"
            placeholder="搜索猫咪名称"
            prefix-icon="Search"
            clearable
          />
        </el-col>
        <el-col :span="3">
          <el-select v-model="filterStatus" placeholder="状态" clearable>
            <el-option label="照顾中" value="照顾中" />
            <el-option label="待领养" value="待领养" />
            <el-option label="已领养" value="已领养" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select v-model="filterBreed" placeholder="品种" clearable>
            <el-option v-for="breed in breeds" :key="breed" :label="breed" :value="breed" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <div class="button-group">
            <el-button type="primary" @click="loadCats">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetFilters">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 猫咪卡片列表 -->
    <div class="cats-grid">
      <div class="cats-grid-container">
        <div 
          class="cat-card" 
          v-for="cat in cats" 
          :key="cat.id"
          @mouseenter="handleCardHover(cat.id)"
          @mouseleave="handleCardLeave(cat.id)"
          @click="goToCatDetail(cat.id)"
        >
          <div class="card-inner" :class="{ flipped: flippedCards[cat.id] }">
            <!-- 卡片正面：猫咪照片 -->
            <div class="card-front">
              <div class="cat-image">
                <img :src="getCatImage(cat)" :alt="cat.name" />
                <div class="cat-status" :class="getStatusClass(cat.status)">
                  {{ getStatusText(cat.status) }}
                </div>
              </div>
            </div>
            
            <!-- 卡片背面：小猫明信片 -->
            <div class="card-back">
              <div class="postcard-content" :style="getCatBackgroundStyle(cat.id)">
                <div class="postcard-header">
                  <h3 class="postcard-title">小猫明信片</h3>
                  <div class="postcard-stamp">🐱</div>
                </div>
                
                <div class="postcard-info">
                  <div class="info-item">
                    <span class="label">姓名：</span>
                    <span class="value">{{ cat.name }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">品种：</span>
                    <span class="value">{{ cat.breed || '未知品种' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">年龄：</span>
                    <span class="value">{{ formatAge(cat.age) }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">性别：</span>
                    <span class="value">{{ getGenderText(cat.gender) }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">健康状况：</span>
                    <span class="value">{{ getHealthText(cat.healthStatus) }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">位置：</span>
                    <span class="value">{{ cat.location || '未知' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">性格特点：</span>
                    <span class="value">{{ cat.personality || '待发现' }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 30, 40]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Management, Refresh } from '@element-plus/icons-vue'
import { catApi } from '@/api/cat.js'


import placeholderCat from '@/public/img/placeholder-cat.jpg'

const router = useRouter()

const cats = ref([])
const breeds = ref([])
const searchText = ref('')
const filterStatus = ref('')
const filterBreed = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 5种清新柔和的渐变色背景
const backgroundGradients = [
  // 1. 清新薄荷绿
  'linear-gradient(135deg, #e8f5e8 0%, #f0f8ff 70%, #fff0f5 100%)',
  // 2. 温柔天空蓝
  'linear-gradient(75deg, #e3f2fd 0%, #f3e5f5 30%, #e8f5e8 100%)',
  // 3. 柔和樱花粉
  'linear-gradient(65deg, #fce4ec 0%, #f3e5f5 50%, #e3f2fd 100%)',
  // 4. 淡雅薰衣草
  'linear-gradient(140deg, #f3e5f5 0%, #e8f5e8 60%, #e0f2f1 100%)',
  // 5. 温暖阳光黄
  'linear-gradient(115deg, #fff9c4 0%, #f3e5f5 20%, #e8f5e8 100%)'
]

// 为每只猫咪生成随机背景色索引
const getRandomBackgroundIndex = (catId) => {
  // 使用猫咪ID作为种子，确保同一只猫咪每次显示相同的颜色
  return catId % backgroundGradients.length
}

// 获取猫咪的背景样式
const getCatBackgroundStyle = (catId) => {
  const index = getRandomBackgroundIndex(catId)
  return {
    background: backgroundGradients[index]
  }
}

// 翻转卡片状态管理
const flippedCards = ref({})

// 卡片悬停处理
const handleCardHover = (catId) => {
  flippedCards.value[catId] = true
}

// 卡片离开处理
const handleCardLeave = (catId) => {
  flippedCards.value[catId] = false
}

// 页面显示的数据直接使用cats.value，筛选在loadCats函数中处理

const parseImagePath = (imagePath) => {
  if (imagePath.startsWith('/uploads')) {
    return imagePath.startsWith('http') ? imagePath : `http://localhost:8080${imagePath}`
  } else if (imagePath.startsWith('/') && !imagePath.startsWith('/img')) {
    return `/img${imagePath}`
  } else if (imagePath.includes('../public/img/')) {
    return imagePath.replace('../public/img/', '/img/')
  } else if (!imagePath.startsWith('/')) {
    return `/img/${imagePath}`
  }
  return imagePath
}

const tryGetImageFromImages = (images) => {
  if (!images) return null
  
  let processedImages = images
  
  if (typeof processedImages === 'string') {
    try {
      processedImages = JSON.parse(processedImages)
    } catch {
      if (processedImages.startsWith('/')) {
        return processedImages
      }
      return null
    }
  }
  
  if (Array.isArray(processedImages) && processedImages.length > 0 && processedImages[0]) {
    return parseImagePath(processedImages[0])
  }
  
  if (processedImages && typeof processedImages === 'object') {
    const values = Object.values(processedImages)
    if (values.length > 0 && values[0]) {
      return parseImagePath(values[0])
    }
  }
  
  return null
}

const getCatImage = (cat) => {
  const imageSources = [cat.officialImages, cat.tempImages, cat.images]
  
  for (const images of imageSources) {
    try {
      const imagePath = tryGetImageFromImages(images)
      if (imagePath) {
        return imagePath
      }
    } catch {
    }
  }
  
  return placeholderCat
}

const getStatusClass = (status) => {
  const classes = {
    '待领养': 'status-waiting',
    '已领养': 'status-adopted',
    '照顾中': 'status-caring',
    '已故': 'status-deceased'
  }
  return classes[status] || 'status-unknown'
}

const getStatusText = (status) => {
  const texts = {
    '待领养': '待领养',
    '已领养': '已领养',
    '照顾中': '照顾中',
    '已故': '已故'
  }
  return texts[status] || status
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
    'HEALTHY': '健康',
    'NEEDS_CARE': '需要照顾',
    'SICK': '生病',
    'DISABLED': '残疾',
    'RECOVERING': '恢复中'
  }
  return texts[health] || health || '未知'
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


const goToCatDetail = (catId) => {
  router.push(`/cats/${catId}`)
}

// 重置筛选条件
const resetFilters = () => {
  searchText.value = ''
  filterStatus.value = ''
  filterBreed.value = ''
  currentPage.value = 1
  loadCats()
}

// 加载猫咪数据
const loadCats = async () => {
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      auditStatus: 'APPROVED'
    }
    
    if (filterStatus.value) {
      params.status = filterStatus.value
    }
    
    if (filterBreed.value) {
      params.breed = filterBreed.value
    }
    
    const response = await catApi.getCats(params.page, params.size, params.status, params.breed, params.auditStatus)
    const data = response.data
    
    let filteredCats = data.records || []
    
    if (searchText.value) {
      const keyword = searchText.value.toLowerCase()
      filteredCats = filteredCats.filter(cat => 
        cat.name.toLowerCase().includes(keyword)
      )
    }
    
    cats.value = [...filteredCats]
    total.value = data.total || 0
    
    await nextTick()
    
  } catch (error) {
    ElMessage.error('加载猫咪数据失败')
    cats.value = []
    total.value = 0
  }
}

// 加载品种列表
const loadBreeds = async () => {
  try {
    breeds.value = [
      '橘猫', '黑猫', '白猫', '三花猫', '狸花猫', '英短', '美短', 
      '暹罗猫', '布偶猫', '波斯猫', '缅因猫', '加菲猫', '孟买猫',
      '金渐层','银渐层','临清狮子猫'
    ]
  } catch (error) {
    breeds.value = ['橘猫', '黑猫', '白猫', '三花猫', '狸花猫']
  }
}

// 分页处理
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
  // 页面加载时初始化数据
  loadBreeds()
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

.cat-list-container {
  max-width: 1550px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 94px;
  margin-bottom: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
}

.header h1 {
  color: #fff;
  font-size: 2em;
  font-weight: 600;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.header h1 .el-icon {
  font-size: 1em;
}

.header p {
  color: #fff;
  opacity: 0.9;
  margin: 0;
}

.filters {
  margin-bottom: 30px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

}

.filters .el-row {
  gap: 5px;
}

.button-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.cats-grid {
  margin-bottom: 30px;
  padding: 0 20px;
}

.cats-grid-container {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.cat-card {
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  animation: fadeInUp 0.6s ease;
  height: 350px; /* 固定高度确保翻转效果一致 */
  perspective: 1000px; /* 3D透视效果 */
}

.cat-card:hover {
  transform: translateY(-2px);
}

/* 卡片内部容器 */
.card-inner {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.6s;
  transform-style: preserve-3d;
}

/* 翻转状态 */
.card-inner.flipped {
  transform: rotateY(180deg);
}

/* 卡片正反面共用样式 */
.card-front,
.card-back {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
}

/* 卡片正面 */
.card-front {
  background: white;
}

/* 卡片背面 */
.card-back {
  background: white;
  transform: rotateY(180deg);
  overflow: hidden; 
}

/* 明信片内容 */
.postcard-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  overflow: hidden; /* 确保内容不溢出 */
}

/* 明信片头部 */
.postcard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 5px; 
  border-bottom: 1px dashed #adb5bd; 
}

.postcard-title {
  color: #495057;
  font-size: 18px; 
  font-weight: 600;
  padding: 5px 0 5px 10px;
  margin: 0;
}

.postcard-stamp {
  font-size: 22px; 
  opacity: 0.8;
}

/* 明信片信息 */
.postcard-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px; 
  padding: 10px;
  overflow: hidden; 
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 8px; 
  font-size: 14px; 
  line-height: 1.5; 
  min-height: 16px; 
}


.info-item .label {
  color: #6c757d;
  font-weight: 500;
  min-width: 50px; 
  flex-shrink: 0;
  font-size: 14px; 
}

.info-item .value {
  color: #495057;
  flex: 1;
  word-break: break-word; /* 允许单词换行 */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 最多显示2行 */
  -webkit-box-orient: vertical;
}


.cat-image {
  position: relative;
  height: 350px;
  overflow: hidden;
}

.cat-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.cat-status {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 8px;
  border-radius: 4px;
  color: white;
  font-size: 12px;
  font-weight: bold;
}

.status-waiting { background: #67c23a; }
.status-adopted { background: #87CEEB; }
.status-caring { background: #e6a23c; }
.status-deceased { background: #f56c6c; }
.status-unknown { background: #909399; }

.pagination {
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .cats-grid-container {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 992px) {
  .cats-grid-container {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .cats-grid-container {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .cats-grid-container {
    grid-template-columns: 1fr;
  }
}
</style>