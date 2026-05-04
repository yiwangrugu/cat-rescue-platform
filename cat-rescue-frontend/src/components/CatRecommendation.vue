<template>
  <div class="cat-recommendation">
    <h3>为您推荐的猫咪</h3>
    <p class="recommendation-desc">基于您的浏览历史和偏好，为您推荐合适的猫咪</p>
    
    <!-- 推荐算法选择 -->
    <div class="algorithm-selector">
      <el-radio-group v-model="algorithmType" @change="loadRecommendations">
        <el-radio label="basedOnHistory">基于浏览历史</el-radio>
        <el-radio label="basedOnPreferences">基于偏好设置</el-radio>
        <el-radio label="popular">热门推荐</el-radio>
      </el-radio-group>
    </div>
    
    <!-- 推荐结果 -->
    <div v-if="recommendations.length > 0" class="recommendation-list">
      <el-row :gutter="20">
        <el-col :span="8" v-for="cat in recommendations" :key="cat.id">
          <el-card class="recommendation-card" shadow="hover">
            <div class="cat-image-container">
              <img :src="getCatImage(cat)" :alt="cat.name" class="cat-image" />
              <div class="recommendation-score">
                <el-tag type="success">{{ (cat.matchScore * 100).toFixed(0) }}%匹配</el-tag>
              </div>
            </div>
            <div class="cat-info">
              <h4>{{ cat.name }}</h4>
              <p class="breed">{{ cat.breed }}</p>
              <p class="details">{{ cat.age }}个月 • {{ getGenderText(cat.gender) }}</p>
              <p class="description">{{ cat.description || '暂无描述' }}</p>
              <div class="recommendation-reason">
                <span class="reason-label">推荐理由：</span>
                <span class="reason-text">{{ getRecommendationReason(cat) }}</span>
              </div>
              <div class="card-actions">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="$emit('cat-selected', cat)"
                >
                  查看详情
                </el-button>
                <el-button 
                  size="small" 
                  @click="addToFavorites(cat.id)"
                  :disabled="cat.isFavorited"
                >
                  {{ cat.isFavorited ? '已收藏' : '收藏' }}
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 无推荐结果 -->
    <div v-else class="no-recommendations">
      <el-empty description="暂无推荐，请先浏览一些猫咪信息">
        <el-button type="primary" @click="$router.push('/cats')">去浏览猫咪</el-button>
      </el-empty>
    </div>
    
    <!-- 偏好设置 -->
    <el-collapse v-model="preferencesPanel" class="preferences-panel">
      <el-collapse-item title="偏好设置" name="preferences">
        <div class="preferences-form">
          <el-form :model="preferences" label-width="100px">
            <el-form-item label="品种偏好">
              <el-select v-model="preferences.breeds" multiple placeholder="选择偏好的品种">
                <el-option 
                  v-for="breed in availableBreeds" 
                  :key="breed" 
                  :label="breed" 
                  :value="breed" 
                />
              </el-select>
            </el-form-item>
            <el-form-item label="年龄范围">
              <el-slider 
                v-model="preferences.ageRange" 
                range 
                :min="0" 
                :max="120" 
                :marks="{0: '0月', 60: '5岁', 120: '10岁'}"
              />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="preferences.gender">
                <el-radio label="">不限</el-radio>
                <el-radio label="MALE">公猫</el-radio>
                <el-radio label="FEMALE">母猫</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="健康状况">
              <el-checkbox-group v-model="preferences.healthConditions">
                <el-checkbox label="HEALTHY">健康</el-checkbox>
                <el-checkbox label="NEEDS_CARE">需要照顾</el-checkbox>
                <el-checkbox label="DISABLED">残疾</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="savePreferences">保存偏好</el-button>
              <el-button @click="resetPreferences">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

const algorithmType = ref('basedOnHistory')
const recommendations = ref([])
const preferencesPanel = ref([])
const availableBreeds = ref([
  '布偶猫', '英国短毛猫', '暹罗猫', '波斯猫', '美国短毛猫', 
  '中华田园猫', '苏格兰折耳猫', '缅因猫', '孟加拉豹猫', '俄罗斯蓝猫'
])

const preferences = ref({
  breeds: [],
  ageRange: [0, 120],
  gender: '',
  healthConditions: []
})

onMounted(() => {
  loadPreferences()
  loadRecommendations()
})

const loadPreferences = () => {
  // TODO: 从localStorage或API加载用户偏好
  const saved = localStorage.getItem('catPreferences')
  if (saved) {
    preferences.value = JSON.parse(saved)
  }
}

const loadRecommendations = async () => {
  // TODO: 调用API获取推荐数据
  try {
    // 模拟推荐数据
    recommendations.value = [
      {
        id: 1,
        name: '小花',
        breed: '中华田园猫',
        age: 24,
        gender: 'FEMALE',
        description: '性格温顺，喜欢亲近人',
        images: '[]',
        matchScore: 0.85,
        isFavorited: false,
        recommendationReason: '与您浏览过的猫咪品种相似'
      },
      {
        id: 2,
        name: '小黑',
        breed: '英国短毛猫',
        age: 18,
        gender: 'MALE',
        description: '活泼好动，喜欢玩耍',
        images: '[]',
        matchScore: 0.78,
        isFavorited: true,
        recommendationReason: '符合您的年龄偏好设置'
      },
      {
        id: 3,
        name: '小白',
        breed: '波斯猫',
        age: 36,
        gender: 'FEMALE',
        description: '优雅安静，毛发华丽',
        images: '[]',
        matchScore: 0.72,
        isFavorited: false,
        recommendationReason: '热门推荐，近期关注度高'
      }
    ]
  } catch (error) {
    ElMessage.error('加载推荐失败')
  }
}

const getCatImage = (cat) => {
  if (cat.images) {
    try {
      const images = JSON.parse(cat.images)
      return images[0] || '/placeholder-cat.jpg'
    } catch {
      return '/placeholder-cat.jpg'
    }
  }
  return '/placeholder-cat.jpg'
}

const getGenderText = (gender) => {
  const genders = {
    'MALE': '公',
    'FEMALE': '母'
  }
  return genders[gender] || '未知'
}

const getRecommendationReason = (cat) => {
  return cat.recommendationReason || '系统为您精心推荐'
}

const addToFavorites = async (catId) => {
  try {
    // TODO: 调用API添加收藏
    ElMessage.success('已添加到收藏')
    
    // 更新收藏状态
    const cat = recommendations.value.find(c => c.id === catId)
    if (cat) {
      cat.isFavorited = true
    }
  } catch (error) {
    ElMessage.error('收藏失败')
  }
}

const savePreferences = () => {
  try {
    localStorage.setItem('catPreferences', JSON.stringify(preferences.value))
    ElMessage.success('偏好设置已保存')
    loadRecommendations()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const resetPreferences = () => {
  preferences.value = {
    breeds: [],
    ageRange: [0, 120],
    gender: '',
    healthConditions: []
  }
  ElMessage.info('偏好设置已重置')
}

// 定义组件事件
const emit = defineEmits(['cat-selected'])
</script>

<style scoped>
.cat-recommendation {
  padding: 20px;
}

.recommendation-desc {
  color: #666;
  margin-bottom: 20px;
}

.algorithm-selector {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.recommendation-list {
  margin-bottom: 20px;
}

.recommendation-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.recommendation-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.cat-image-container {
  position: relative;
  margin-bottom: 15px;
}

.cat-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 4px;
}

.recommendation-score {
  position: absolute;
  top: 10px;
  right: 10px;
}

.cat-info h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.breed {
  color: #409EFF;
  font-weight: 500;
  margin: 5px 0;
}

.details {
  color: #666;
  font-size: 14px;
  margin: 5px 0;
}

.description {
  color: #999;
  font-size: 13px;
  line-height: 1.4;
  margin: 10px 0;
}

.recommendation-reason {
  background: #f0f9ff;
  padding: 10px;
  border-radius: 4px;
  margin: 10px 0;
  font-size: 13px;
}

.reason-label {
  font-weight: 500;
  color: #409EFF;
}

.reason-text {
  color: #666;
}

.card-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.no-recommendations {
  text-align: center;
  padding: 40px 0;
}

.preferences-panel {
  margin-top: 20px;
}

.preferences-form {
  padding: 20px;
}
</style>