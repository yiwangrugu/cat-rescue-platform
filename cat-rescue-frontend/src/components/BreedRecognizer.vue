<template>
  <div class="breed-recognizer">
    <div class="header-section">
      <h2>AI猫咪品种识别</h2>
      <p class="subtitle">识别结果仅供参考</p>
      <div class="photo-tips">
        <el-alert
          title="图片建议"
          type="info"
          :closable="false"
          style="margin-top: 15px; text-align: left;"
        >
          <ul>
            <li>猫咪位于照片中心区域</li>
            <li>背景颜色不要太突兀，尤其是黑色</li>
            <li>光线不要过亮或过暗</li>
            <li>照片中只有一只猫咪</li>
          </ul>
        </el-alert>
      </div>
    </div>

    <div class="upload-section">
      <el-card>
        <template #header>
          <span>上传猫咪图片</span>
        </template>
        <div class="upload-area" @click="triggerFileInput" :class="{ 'has-image': imageUrl }">
          <div v-if="!imageUrl" class="upload-placeholder">
            <el-icon size="64" color="#409EFF"><Upload /></el-icon>
            <div class="upload-text">点击或拖拽图片到此处</div>
            <div class="upload-hint">支持 JPG、PNG 格式，最大 10MB</div>
          </div>
          <div v-else class="image-container">
            <img :src="imageUrl" alt="猫咪图片" class="preview-image" />
            <div class="image-overlay">
              <el-button type="primary" size="small" @click.stop="triggerFileInput">更换图片</el-button>
            </div>
          </div>
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            @change="handleFileSelect"
            style="display: none"
          />
        </div>
      </el-card>
    </div>

    <div v-if="result" class="result-section">
      <el-card>
        <template #header>
          <span>识别结果</span>
          <div class="result-header-actions">
            <el-button size="small" @click="saveResult">保存结果</el-button>
          </div>
        </template>
        
        <div class="result-content">
          <div class="primary-result">
            <div class="breed-info">
              <h3>{{ result.primaryBreed.name }}</h3>
              <div class="confidence-bar">
                <el-progress 
                  :percentage="Math.round(result.primaryBreed.confidence * 100)" 
                  :stroke-width="8"
                  :color="getConfidenceColor(result.primaryBreed.confidence)"
                />
                <span class="confidence-text">{{ (result.primaryBreed.confidence * 100).toFixed(1) }}%</span>
              </div>
              <div class="breed-description">
                {{ result.primaryBreed.description }}
              </div>
            </div>
            
            <div class="breed-characteristics">
              <h4>品种特征</h4>
              <el-row :gutter="20">
                <el-col :span="8">
                  <div class="characteristic-item">
                    <el-icon><User /></el-icon>
                    <span>性格：{{ result.primaryBreed.temperament }}</span>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="characteristic-item">
                    <el-icon><DataBoard /></el-icon>
                    <span>体型：{{ result.primaryBreed.size }}</span>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="characteristic-item">
                    <el-icon><Brush /></el-icon>
                    <span>毛发：{{ result.primaryBreed.coat }}</span>
                  </div>
                </el-col>
              </el-row>
            </div>
          </div>
          
          <div v-if="result.alternativeBreeds.length > 0" class="alternative-breeds">
            <h4>其他可能品种</h4>
            <div class="breed-list">
              <div 
                v-for="(breed, index) in result.alternativeBreeds" 
                :key="index"
                class="breed-item"
              >
                <div class="breed-name">{{ breed.name }}</div>
                <div class="breed-confidence">
                  <el-progress 
                    :percentage="Math.round(breed.confidence * 100)" 
                    :show-text="false"
                    :stroke-width="6"
                  />
                  <span>{{ (breed.confidence * 100).toFixed(1) }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <div v-if="error" class="error-section">
      <el-alert :title="error" type="error" show-icon :closable="false" />
    </div>

    <div v-if="recognizing" class="loading-section">
      <el-card>
        <div class="loading-content">
          <el-icon class="loading-icon" size="48"><Loading /></el-icon>
          <h3>AI正在分析中...</h3>
          <p>正在识别猫咪品种，请稍候</p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Upload, 
  Search, 
  Refresh, 
  User, 
  DataBoard, 
  Brush, 
  Loading
} from '@element-plus/icons-vue'
import { getPresetTrainingSamples } from '@/utils/catTrainingSamples'
import { loadCatBreedModel, predictCatBreed, isModelAvailable } from '@/utils/catBreedModel'
import { loadKNNModel, predictCatBreedWithKNN, isKNNModelAvailable } from '@/utils/catKNNPredictor'
import { loadImprovedModel, predictCatBreedWithImprovedModel, isImprovedModelAvailable } from '@/utils/improvedCatPredictor'

const fileInput = ref()
const imageUrl = ref('')
const recognizing = ref(false)
const result = ref(null)
const error = ref('')
const trainingSamples = ref([])

const emit = defineEmits(['breed-selected', 'close'])

const breedDatabase = {
  '橘猫': {
    name: '橘猫',
    color: '橘色',
    colorRange: { minR: 150, maxR: 255, minG: 80, maxG: 200, minB: 0, maxB: 150 },
    temperament: '活泼',
    size: '中型',
    coat: '短毛',
    description: '中华田园猫的一种，性格亲人，易于饲养'
  },
  '黑猫': {
    name: '黑猫',
    color: '黑色',
    colorRange: { minR: 0, maxR: 100, minG: 0, maxG: 100, minB: 0, maxB: 100 },
    temperament: '安静',
    size: '中型',
    coat: '短毛',
    description: '传说中的玄猫，性格独立'
  },
  '白猫': {
    name: '白猫',
    color: '白色',
    colorRange: { minR: 200, maxR: 255, minG: 200, maxG: 255, minB: 200, maxB: 255 },
    temperament: '温和',
    size: '中型',
    coat: '短毛',
    description: '毛色纯白，优雅可爱'
  },
  '狸花猫': {
    name: '狸花猫',
    color: '虎斑',
    colorRange: { minR: 80, maxR: 200, minG: 60, maxG: 180, minB: 40, maxB: 140 },
    temperament: '机敏',
    size: '中型',
    coat: '短毛',
    description: '中国本土猫种，2010年获TICA国际认可'
  },
  '三花猫': {
    name: '三花猫',
    color: '三色',
    colorRange: { minR: 50, maxR: 255, minG: 50, maxG: 255, minB: 50, maxB: 255 },
    temperament: '温顺',
    size: '中型',
    coat: '短毛',
    description: '三种颜色相间，中国传统特色猫种'
  },
  '奶牛猫': {
    name: '奶牛猫',
    color: '黑白',
    colorRange: { minR: 0, maxR: 255, minG: 0, maxG: 255, minB: 0, maxB: 255 },
    temperament: '活泼好动',
    size: '中型',
    coat: '短毛',
    description: '黑白相间的猫咪，活泼好动'
  },
  '玳瑁猫': {
    name: '玳瑁猫',
    color: '玳瑁',
    colorRange: { minR: 80, maxR: 220, minG: 50, maxG: 150, minB: 30, maxB: 120 },
    temperament: '温和亲人',
    size: '中型',
    coat: '短毛',
    description: '黑橘白三色混合，中国传统特色猫种'
  },
  '四川简州猫': {
    name: '四川简州猫',
    color: '虎斑',
    colorRange: { minR: 90, maxR: 210, minG: 70, maxG: 190, minB: 50, maxB: 150 },
    temperament: '机敏',
    size: '中型',
    coat: '短毛',
    description: '中国古老品种，产自四川简州'
  },
  '临清狮子猫': {
    name: '临清狮子猫',
    color: '白色',
    colorRange: { minR: 180, maxR: 255, minG: 180, maxG: 255, minB: 180, maxB: 255 },
    temperament: '温顺',
    size: '中型',
    coat: '长毛',
    description: '山东珍贵品种，毛发蓬松如狮子'
  },
  '蓝猫': {
    name: '蓝猫',
    color: '灰色',
    colorRange: { minR: 80, maxR: 150, minG: 90, maxG: 160, minB: 100, maxB: 180 },
    temperament: '温顺',
    size: '中型',
    coat: '短毛',
    description: '英国短毛猫的一种，毛色呈蓝灰色'
  },
  '布偶猫': {
    name: '布偶猫',
    color: '双色',
    colorRange: { minR: 150, maxR: 255, minG: 130, maxG: 240, minB: 120, maxB: 230 },
    temperament: '温顺',
    size: '大型',
    coat: '长毛',
    description: '布偶猫，毛发柔软，性格温顺'
  },
  '金渐层': {
    name: '金渐层',
    color: '金色',
    colorRange: { minR: 170, maxR: 240, minG: 130, maxG: 210, minB: 70, maxB: 150 },
    temperament: '亲人',
    size: '中型',
    coat: '短毛',
    description: '英国短毛猫的一种，毛色呈金色渐变'
  },
  '银渐层': {
    name: '银渐层',
    color: '银色',
    colorRange: { minR: 140, maxR: 200, minG: 135, maxG: 195, minB: 130, maxB: 190 },
    temperament: '温和',
    size: '中型',
    coat: '短毛',
    description: '英国短毛猫的一种，毛色呈银色渐变'
  },
  '缅因猫': {
    name: '缅因猫',
    color: '虎斑',
    colorRange: { minR: 110, maxR: 200, minG: 90, maxG: 170, minB: 60, maxB: 140 },
    temperament: '温柔',
    size: '大型',
    coat: '长毛',
    description: '缅因猫，体型巨大，性格温柔'
  },
  '暹罗猫': {
    name: '暹罗猫',
    color: '重点色',
    colorRange: { minR: 50, maxR: 220, minG: 50, maxG: 220, minB: 50, maxB: 220 },
    temperament: '活泼',
    size: '中型',
    coat: '短毛',
    description: '泰国猫，有重点色特征，性格活泼'
  }
}

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过10MB')
    return
  }
  
  imageUrl.value = URL.createObjectURL(file)
  result.value = null
  error.value = ''
  
  setTimeout(() => {
    recognizeBreed()
  }, 100)
}

const extractEnhancedColorFeatures = async (imgElement) => {
  return new Promise((resolve) => {
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    const maxSize = 400
    let width = imgElement.width
    let height = imgElement.height
    
    if (width > height) {
      if (width > maxSize) {
        height = (height / width) * maxSize
        width = maxSize
      }
    } else {
      if (height > maxSize) {
        width = (width / height) * maxSize
        height = maxSize
      }
    }
    
    canvas.width = width
    canvas.height = height
    ctx.drawImage(imgElement, 0, 0, width, height)
    
    const imageData = ctx.getImageData(0, 0, width, height)
    const data = imageData.data
    
    let totalR = 0, totalG = 0, totalB = 0
    let dominantR = 0, dominantG = 0, dominantB = 0
    let minR = 255, minG = 255, minB = 255
    let pixelCount = 0
    let colorVariance = 0
    let contrastScore = 0
    
    let darkPixelCount = 0
    let lightPixelCount = 0
    let hasBlack = false
    let hasWhite = false
    let hasOrange = false
    let hasGray = false
    
    const colorHistogram = {}
    const hueHistogram = {}
    const saturationHistogram = {}
    const brightnessHistogram = {}
    
    for (let i = 0; i < data.length; i += 4) {
      const r = data[i]
      const g = data[i + 1]
      const b = data[i + 2]
      const a = data[i + 3]
      
      if (a > 128) {
        totalR += r
        totalG += g
        totalB += b
        
        if (r > dominantR) dominantR = r
        if (g > dominantG) dominantG = g
        if (b > dominantB) dominantB = b
        if (r < minR) minR = r
        if (g < minG) minG = g
        if (b < minB) minB = b
        
        const colorKey = `${Math.floor(r/6)}-${Math.floor(g/6)}-${Math.floor(b/6)}`
        colorHistogram[colorKey] = (colorHistogram[colorKey] || 0) + 1
        
        const brightness = (r + g + b) / 3
        const brightnessKey = Math.floor(brightness / 25)
        brightnessHistogram[brightnessKey] = (brightnessHistogram[brightnessKey] || 0) + 1
        
        if (brightness < 80) darkPixelCount++
        if (brightness > 180) lightPixelCount++
        
        if (r < 60 && g < 60 && b < 60) hasBlack = true
        if (r > 200 && g > 200 && b > 200) hasWhite = true
        if (r > 150 && g > 80 && g < 180 && b < 120) hasOrange = true
        if (Math.abs(r - g) < 40 && Math.abs(g - b) < 40 && Math.abs(r - b) < 40) hasGray = true
        
        const max = Math.max(r, g, b)
        const min = Math.min(r, g, b)
        const delta = max - min
        
        if (delta > 0) {
          const saturation = delta / max
          const satKey = Math.floor(saturation * 10)
          saturationHistogram[satKey] = (saturationHistogram[satKey] || 0) + 1
          
          let hue = 0
          if (delta === 0) {
            hue = 0
          } else if (max === r) {
            hue = ((g - b) / delta) % 6
          } else if (max === g) {
            hue = (b - r) / delta + 2
          } else {
            hue = (r - g) / delta + 4
          }
          hue = Math.round(hue * 60)
          if (hue < 0) hue += 360
          
          const hueKey = Math.floor(hue / 20)
          hueHistogram[hueKey] = (hueHistogram[hueKey] || 0) + 1
        }
        
        if (i + 4 < data.length) {
          const nextR = data[i + 4]
          const nextG = data[i + 5]
          const nextB = data[i + 6]
          const contrast = Math.abs(r - nextR) + Math.abs(g - nextG) + Math.abs(b - nextB)
          contrastScore += contrast
        }
        
        pixelCount++
      }
    }
    
    if (pixelCount === 0) pixelCount = 1
    
    const avgR = totalR / pixelCount
    const avgG = totalG / pixelCount
    const avgB = totalB / pixelCount
    const avgBrightness = (avgR + avgG + avgB) / 3
    
    for (let i = 0; i < data.length; i += 16) {
      const r = data[i]
      const g = data[i + 1]
      const b = data[i + 2]
      if (data[i + 3] > 128) {
        colorVariance += Math.pow(r - avgR, 2) + Math.pow(g - avgG, 2) + Math.pow(b - avgB, 2)
      }
    }
    
    colorVariance = colorVariance / (pixelCount / 4) / (255 * 255 * 3)
    contrastScore = contrastScore / (pixelCount) / 765
    
    const darkRatio = darkPixelCount / pixelCount
    const lightRatio = lightPixelCount / pixelCount
    
    const sortedColors = Object.entries(colorHistogram)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 10)
      .map(([key, count]) => {
        const [r, g, b] = key.split('-').map(v => v * 6 + 3)
        return { r, g, b, count, ratio: count / pixelCount }
      })
    
    const sortedHues = Object.entries(hueHistogram)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 4)
      .map(([hueKey, count]) => ({
        hue: parseInt(hueKey) * 20 + 10,
        ratio: count / pixelCount
      }))
    
    const sortedSaturations = Object.entries(saturationHistogram)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 3)
      .map(([satKey, count]) => ({
        saturation: parseInt(satKey) * 0.1 + 0.05,
        ratio: count / pixelCount
      }))
    
    const sortedBrightness = Object.entries(brightnessHistogram)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 3)
      .map(([brightnessKey, count]) => ({
        brightness: parseInt(brightnessKey) * 25 + 12,
        ratio: count / pixelCount
      }))
    
    const distinctColorCount = Object.keys(colorHistogram).length
    
    resolve({
      avgR, avgG, avgB,
      dominantR, dominantG, dominantB,
      minR, minG, minB,
      brightness: avgBrightness,
      colorRatio: {
        red: avgR / (avgR + avgG + avgB + 1),
        green: avgG / (avgR + avgG + avgB + 1),
        blue: avgB / (avgR + avgG + avgB + 1)
      },
      dominantColors: sortedColors,
      dominantHues: sortedHues,
      dominantSaturations: sortedSaturations,
      dominantBrightness: sortedBrightness,
      colorRange: {
        minR, maxR: dominantR, minG, maxG: dominantG, minB, maxB: dominantB
      },
      colorVariance,
      contrastScore,
      colorComplexity: Math.min(distinctColorCount / 150, 1),
      distinctColorCount,
      darkRatio,
      lightRatio,
      hasBlack,
      hasWhite,
      hasOrange,
      hasGray
    })
  })
}

const calculateFeatureSimilarity = (features1, features2) => {
  let similarity = 0
  
  const colorDistance = Math.sqrt(
    Math.pow(features1.avgR - features2.avgR, 2) +
    Math.pow(features1.avgG - features2.avgG, 2) +
    Math.pow(features1.avgB - features2.avgB, 2)
  )
  const colorSimilarity = 1 - (colorDistance / 442)
  
  const brightnessDistance = Math.abs(features1.brightness - features2.brightness)
  const brightnessSimilarity = 1 - (brightnessDistance / 255)
  
  const varianceSimilarity = 1 - Math.abs(features1.colorVariance - features2.colorVariance)
  const contrastSimilarity = 1 - Math.abs(features1.contrastScore - features2.contrastScore)
  const complexitySimilarity = 1 - Math.abs(features1.colorComplexity - features2.colorComplexity)
  
  let dominantColorSimilarity = 0
  if (features1.dominantColors && features2.dominantColors) {
    const maxCompare = Math.min(features1.dominantColors.length, features2.dominantColors.length, 4)
    let totalDCSimilarity = 0
    for (let i = 0; i < maxCompare; i++) {
      const color1 = features1.dominantColors[i]
      const color2 = features2.dominantColors[i]
      if (color1 && color2) {
        const dcDistance = Math.sqrt(
          Math.pow(color1.r - color2.r, 2) +
          Math.pow(color1.g - color2.g, 2) +
          Math.pow(color1.b - color2.b, 2)
        )
        const weight = 1 - (i * 0.15)
        totalDCSimilarity += (1 - (dcDistance / 442)) * weight * color1.ratio * color2.ratio
      }
    }
    dominantColorSimilarity = totalDCSimilarity / maxCompare
  }
  
  let hueSimilarity = 0
  if (features1.dominantHues && features2.dominantHues) {
    const maxHueCompare = Math.min(features1.dominantHues.length, features2.dominantHues.length, 2)
    let totalHueSimilarity = 0
    for (let i = 0; i < maxHueCompare; i++) {
      const hue1 = features1.dominantHues[i]
      const hue2 = features2.dominantHues[i]
      if (hue1 && hue2) {
        const hueDiff = Math.min(Math.abs(hue1.hue - hue2.hue), 360 - Math.abs(hue1.hue - hue2.hue))
        totalHueSimilarity += (1 - hueDiff / 180) * hue1.ratio * hue2.ratio
      }
    }
    hueSimilarity = totalHueSimilarity / maxHueCompare
  }
  
  let saturationSimilarity = 0
  if (features1.dominantSaturations && features2.dominantSaturations) {
    const maxSatCompare = Math.min(features1.dominantSaturations.length, features2.dominantSaturations.length, 2)
    let totalSatSimilarity = 0
    for (let i = 0; i < maxSatCompare; i++) {
      const sat1 = features1.dominantSaturations[i]
      const sat2 = features2.dominantSaturations[i]
      if (sat1 && sat2) {
        const satDiff = Math.abs(sat1.saturation - sat2.saturation)
        totalSatSimilarity += (1 - satDiff) * sat1.ratio * sat2.ratio
      }
    }
    saturationSimilarity = totalSatSimilarity / maxSatCompare
  }
  
  similarity += colorSimilarity * 0.25
  similarity += brightnessSimilarity * 0.12
  similarity += varianceSimilarity * 0.08
  similarity += contrastSimilarity * 0.06
  similarity += complexitySimilarity * 0.05
  similarity += dominantColorSimilarity * 0.18
  similarity += hueSimilarity * 0.12
  similarity += saturationSimilarity * 0.08
  
  return Math.max(0, Math.min(1, similarity))
}

const matchWithTrainingSamples = (colorFeatures) => {
  if (trainingSamples.value.length === 0) {
    return null
  }
  
  const breedScores = {}
  
  trainingSamples.value.forEach(sample => {
    const similarity = calculateFeatureSimilarity(colorFeatures, sample.features.colorFeatures)
    
    if (!breedScores[sample.breed]) {
      breedScores[sample.breed] = {
        totalSimilarity: 0,
        count: 0,
        maxSimilarity: 0,
        topSimilarities: []
      }
    }
    
    breedScores[sample.breed].totalSimilarity += similarity
    breedScores[sample.breed].count++
    if (similarity > breedScores[sample.breed].maxSimilarity) {
      breedScores[sample.breed].maxSimilarity = similarity
    }
    
    breedScores[sample.breed].topSimilarities.push(similarity)
    breedScores[sample.breed].topSimilarities.sort((a, b) => b - a)
    if (breedScores[sample.breed].topSimilarities.length > 3) {
      breedScores[sample.breed].topSimilarities = breedScores[sample.breed].topSimilarities.slice(0, 3)
    }
  })
  
  const breedResults = Object.entries(breedScores).map(([breed, data]) => {
    const avgSimilarity = data.totalSimilarity / data.count
    const countBonus = Math.min(data.count / 5, 1) * 0.12
    const maxSimilarityBonus = data.maxSimilarity * 0.25
    const top3Avg = data.topSimilarities.reduce((sum, sim) => sum + sim, 0) / data.topSimilarities.length
    const top3Bonus = top3Avg * 0.18
    const finalScore = avgSimilarity * 0.45 + maxSimilarityBonus + top3Bonus + countBonus
    
    return {
      name: breed,
      score: Math.min(finalScore, 0.98),
      sampleCount: data.count,
      avgSimilarity: avgSimilarity,
      maxSimilarity: data.maxSimilarity
    }
  })
  
  breedResults.sort((a, b) => b.score - a.score)
  
  if (breedResults.length > 0 && breedResults[0].score > 0.7) {
    return breedResults
  }
  
  return null
}

const matchBreedByEnhancedFeatures = (colorFeatures) => {
  const breeds = Object.keys(breedDatabase)
  
  const scores = breeds.map(breed => {
    const breedInfo = breedDatabase[breed]
    let score = 0
    
    if (breedInfo.colorRange) {
      const targetR = (breedInfo.colorRange.minR + breedInfo.colorRange.maxR) / 2
      const targetG = (breedInfo.colorRange.minG + breedInfo.colorRange.maxG) / 2
      const targetB = (breedInfo.colorRange.minB + breedInfo.colorRange.maxB) / 2
      
      const distance = Math.sqrt(
        Math.pow(colorFeatures.avgR - targetR, 2) +
        Math.pow(colorFeatures.avgG - targetG, 2) +
        Math.pow(colorFeatures.avgB - targetB, 2)
      )
      
      score = Math.max(0, 1 - distance / 300)
    }
    
    if (breedInfo.color === '橘色') {
      if (colorFeatures.avgR > 150 && colorFeatures.avgG > 80 && colorFeatures.avgB < 150) {
        score += 0.2
      }
      if (colorFeatures.dominantHues && colorFeatures.dominantHues.some(h => h.hue >= 15 && h.hue <= 45)) {
        score += 0.1
      }
    } else if (breedInfo.color === '黑色') {
      if (colorFeatures.brightness < 120) {
        score += 0.15
      }
      if (colorFeatures.brightness < 100) {
        score += 0.15
      }
      if (colorFeatures.brightness < 80) {
        score += 0.15
      }
      if (colorFeatures.darkRatio > 0.6) {
        score += 0.2
      }
      if (colorFeatures.darkRatio > 0.75) {
        score += 0.1
      }
      if (colorFeatures.contrastScore < 0.15) {
        score += 0.1
      }
    } else if (breedInfo.color === '白色') {
      if (colorFeatures.brightness > 170) {
        score += 0.15
      }
      if (colorFeatures.brightness > 190) {
        score += 0.15
      }
      if (colorFeatures.brightness > 210) {
        score += 0.15
      }
      if (colorFeatures.lightRatio > 0.5) {
        score += 0.2
      }
      if (colorFeatures.lightRatio > 0.7) {
        score += 0.1
      }
      if (colorFeatures.contrastScore < 0.2) {
        score += 0.1
      }
    } else if (breedInfo.color === '灰色') {
      const grayBalance = Math.abs(colorFeatures.avgR - colorFeatures.avgG) + 
                        Math.abs(colorFeatures.avgG - colorFeatures.avgB)
      if (grayBalance < 50) {
        score += 0.2
      }
      if (grayBalance < 30) {
        score += 0.1
      }
    } else if (breedInfo.color === '金色') {
      if (colorFeatures.avgR > 160 && colorFeatures.avgG > 120) {
        score += 0.2
      }
      if (colorFeatures.dominantHues && colorFeatures.dominantHues.some(h => h.hue >= 30 && h.hue <= 60)) {
        score += 0.1
      }
    } else if (breedInfo.color === '银色') {
      const silverBalance = Math.abs(colorFeatures.avgR - colorFeatures.avgG) + 
                          Math.abs(colorFeatures.avgG - colorFeatures.avgB)
      if (colorFeatures.brightness > 150 && silverBalance < 40) {
        score += 0.2
      }
      if (silverBalance < 20) {
        score += 0.1
      }
    } else if (breedInfo.color === '虎斑') {
      if (colorFeatures.dominantColors && colorFeatures.dominantColors.length >= 3) {
        score += 0.15
      }
    } else if (breedInfo.color === '三色') {
      if (colorFeatures.hasBlack && colorFeatures.hasWhite && colorFeatures.hasOrange) {
        score += 0.35
      }
      if (colorFeatures.hasBlack && colorFeatures.hasWhite) {
        score += 0.15
      }
      if (colorFeatures.dominantColors && colorFeatures.dominantColors.length >= 4) {
        score += 0.15
      }
      if (colorFeatures.colorComplexity > 0.5) {
        score += 0.15
      }
      if (colorFeatures.distinctColorCount > 80) {
        score += 0.1
      }
    } else if (breedInfo.color === '玳瑁') {
      if (colorFeatures.hasBlack && colorFeatures.hasOrange) {
        score += 0.35
      }
      if (colorFeatures.hasBlack && colorFeatures.hasOrange && !colorFeatures.hasWhite) {
        score += 0.15
      }
      if (colorFeatures.dominantColors && colorFeatures.dominantColors.length >= 3) {
        score += 0.15
      }
      if (colorFeatures.colorComplexity > 0.6) {
        score += 0.2
      }
      if (colorFeatures.distinctColorCount > 100) {
        score += 0.1
      }
      if (colorFeatures.colorVariance > 0.15) {
        score += 0.1
      }
    }
    
    return { name: breed, score: Math.min(score, 0.98) }
  })
  
  scores.sort((a, b) => b.score - a.score)
  return scores
}

const recognizeBreed = async () => {
  if (!imageUrl.value) {
    return
  }
  
  recognizing.value = true
  error.value = ''
  
  try {
    const imgElement = new Image()
    imgElement.crossOrigin = 'anonymous'
    
    await new Promise((resolve, reject) => {
      imgElement.onload = resolve
      imgElement.onerror = reject
      imgElement.src = imageUrl.value
    })
    
    let useImprovedModel = false
    
    if (isImprovedModelAvailable()) {
      useImprovedModel = true
    } else {
      try {
        loadImprovedModel()
        if (isImprovedModelAvailable()) {
          useImprovedModel = true
        }
      } catch (err) {
        console.log('ai识别模型加载失败，尝试其他方法:', err)
      }
    }
    
    if (useImprovedModel) {
      const predictions = await predictCatBreedWithImprovedModel(imgElement, 3)
      
      if (predictions) {
        const primaryBreedInfo = breedDatabase[predictions.primaryBreed.breed] || {
          name: predictions.primaryBreed.breed,
          temperament: '未知',
          size: '中型',
          coat: '短毛',
          description: ''
        }
        
        result.value = {
          primaryBreed: {
            ...primaryBreedInfo,
            name: predictions.primaryBreed.breed,
            confidence: predictions.primaryBreed.confidence
          },
          alternativeBreeds: predictions.alternativeBreeds.map(item => ({
            name: item.breed,
            confidence: item.confidence
          }))
        }
        
        ElMessage.success('ai猫咪品种识别完成！')
        recognizing.value = false
        return
      }
    }
    
    let useKNNModel = false
    
    if (isKNNModelAvailable()) {
      useKNNModel = true
    } else {
      try {
        loadKNNModel()
        if (isKNNModelAvailable()) {
          useKNNModel = true
        }
      } catch (err) {
        console.log('KNN模型加载失败，使用其他方法:', err)
      }
    }
    
    if (useKNNModel) {
      const predictions = await predictCatBreedWithKNN(imgElement, 3)
      
      if (predictions) {
        const primaryBreedInfo = breedDatabase[predictions.primaryBreed.breed] || {
          name: predictions.primaryBreed.breed,
          temperament: '未知',
          size: '中型',
          coat: '短毛',
          description: ''
        }
        
        result.value = {
          primaryBreed: {
            ...primaryBreedInfo,
            name: predictions.primaryBreed.breed,
            confidence: predictions.primaryBreed.confidence
          },
          alternativeBreeds: predictions.alternativeBreeds.map(item => ({
            name: item.breed,
            confidence: item.confidence
          }))
        }
        
        ElMessage.success('KNN机器学习模型识别完成！')
        recognizing.value = false
        return
      }
    }
    
    let useTFModel = false
    
    if (isModelAvailable()) {
      useTFModel = true
    } else {
      try {
        await loadCatBreedModel()
        if (isModelAvailable()) {
          useTFModel = true
        }
      } catch (err) {
        console.log('TensorFlow.js模型加载失败，使用规则匹配:', err)
      }
    }
    
    if (useTFModel) {
      const predictions = await predictCatBreed(imgElement)
      
      if (predictions) {
        const primaryBreedInfo = breedDatabase[predictions.primaryBreed.breed] || {
          name: predictions.primaryBreed.breed,
          temperament: '未知',
          size: '中型',
          coat: '短毛',
          description: ''
        }
        
        result.value = {
          primaryBreed: {
            ...primaryBreedInfo,
            name: predictions.primaryBreed.breed,
            confidence: predictions.primaryBreed.confidence
          },
          alternativeBreeds: predictions.alternativeBreeds.map(item => ({
            name: item.breed,
            confidence: item.confidence
          }))
        }
        
        ElMessage.success('AI深度学习模型识别完成')
        recognizing.value = false
        return
      }
    }
    
    const colorFeatures = await extractEnhancedColorFeatures(imgElement)
    
    let recognitionResult
    let usedTrainingSamples = false
    
    const isSpecialBreed = (colorFeatures.hasBlack && colorFeatures.darkRatio > 0.6) ||
                          (colorFeatures.hasWhite && colorFeatures.lightRatio > 0.5) ||
                          (colorFeatures.hasBlack && colorFeatures.hasOrange) ||
                          (colorFeatures.hasBlack && colorFeatures.hasWhite && colorFeatures.hasOrange)
    
    if (!isSpecialBreed && trainingSamples.value.length > 0) {
      const sampleResults = matchWithTrainingSamples(colorFeatures)
      
      if (sampleResults && sampleResults.length > 0 && sampleResults[0].score > 0.65) {
        usedTrainingSamples = true
        recognitionResult = sampleResults.map(item => ({
          name: item.name,
          score: item.score
        }))
        ElMessage.info(`使用了${trainingSamples.value.length}个训练样本进行识别，置信度：${(sampleResults[0].score * 100).toFixed(1)}%`)
      }
    }
    
    if (!usedTrainingSamples) {
      recognitionResult = matchBreedByEnhancedFeatures(colorFeatures)
    }
    
    const primaryBreed = breedDatabase[recognitionResult[0].name]
    const alternativeBreeds = recognitionResult.slice(1, 4).map(item => ({
      name: item.name,
      confidence: Math.min(item.score, 0.85)
    }))
    
    result.value = {
      primaryBreed: {
        ...primaryBreed,
        confidence: Math.min(recognitionResult[0].score, 0.95)
      },
      alternativeBreeds: alternativeBreeds
    }
    
    ElMessage.success('AI识别完成')
  } catch (err) {
    console.error('识别失败:', err)
    error.value = '识别失败，请重试'
    ElMessage.error('识别失败')
  } finally {
    recognizing.value = false
  }
}

const reset = () => {
  if (imageUrl.value) {
    URL.revokeObjectURL(imageUrl.value)
  }
  imageUrl.value = ''
  result.value = null
  error.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const getConfidenceColor = (confidence) => {
  if (confidence >= 0.8) return '#67c23a'
  if (confidence >= 0.6) return '#e6a23c'
  return '#f56c6c'
}

const saveResult = () => {
  if (result.value) {
    emit('breed-selected', result.value.primaryBreed.name)
    ElMessage.success('品种已自动填入表单')
  }
}

const getResult = () => {
  return result.value
}

const loadTrainingSamples = () => {
  try {
    trainingSamples.value = getPresetTrainingSamples()
    console.log(`加载了 ${trainingSamples.value.length} 个训练样本`)
  } catch (err) {
    console.error('加载训练样本失败:', err)
  }
}

const setImage = (url) => {
  if (imageUrl.value) {
    URL.revokeObjectURL(imageUrl.value)
  }
  imageUrl.value = url
  result.value = null
  error.value = ''
}

defineExpose({
  setImage,
  getResult,
  recognizeBreed
})

onUnmounted(() => {
  if (imageUrl.value) {
    URL.revokeObjectURL(imageUrl.value)
  }
})

loadTrainingSamples()
</script>

<style scoped>
.breed-recognizer {
  max-width: 100%;
  margin: 0 auto;
  padding: 0;
  max-height: 500px; 
  overflow-y: auto;
}

.header-section {
  text-align: center;
  margin-bottom: 30px;
  padding: 0 20px;
}

.header-section h2 {
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-size: 1.8rem;
  font-weight: 600;
}

.subtitle {
  color: #7f8c8d;
  font-size: 1rem;
  margin: 0;
}

.model-info-tag {
  margin-top: 15px;
  padding: 12px 20px;
  font-size: 14px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.accuracy-text {
  font-weight: 600;
  font-size: 15px;
  color: #529b2e;
}

.upload-area {
  border: 2px dashed #e0e6ed;
  border-radius: 16px;
  padding: 50px 30px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 20px;
  background: linear-gradient(145deg, #f8fafc, #f1f5f9);
  min-height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.upload-area:hover {
  border-color: #409eff;
  background: linear-gradient(145deg, #f0f8ff, #e6f7ff);
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.15);
}

.upload-area.has-image {
  border: none !important;
  background: transparent !important;
  padding: 0 !important;
  margin-bottom: 0 !important;
  cursor: default !important;
  min-height: auto;
  display: block;
  transform: none !important;
  box-shadow: none !important;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.upload-text {
  display: block;
  margin-top: 8px;
  color: #475569;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.upload-hint {
  color: #64748b;
  font-size: 13px;
  font-weight: 400;
}

.image-container {
  position: relative;
  display: inline-block;
  max-width: 100%;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  transition: all 0.3s ease;
}

.image-container:hover {
  transform: scale(1.02);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.18);
}

.preview-image {
  max-width: 240px;
  max-height: 240px;
  width: auto;
  height: auto;
  border-radius: 12px;
  object-fit: contain;
  display: block;
}

.image-overlay {
  position: absolute;
  top: 10px;
  right: 10px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-container:hover .image-overlay {
  opacity: 1;
}

.recognition-controls {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-bottom: 20px;
}

.result-section {
  margin-bottom: 30px;
}

.result-header-actions {
  display: flex;
  gap: 10px;
}

.result-content {
  padding: 0;
}

.primary-result {
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
  border-radius: 12px;
  padding: 25px;
  margin-bottom: 25px;
  border: 1px solid #bae7ff;
}

.breed-info h3 {
  margin: 0 0 15px 0;
  color: #1890ff;
  font-size: 1.5rem;
  font-weight: 600;
}

.confidence-bar {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.confidence-bar :deep(.el-progress) {
  flex: 1;
}

.confidence-text {
  color: #1890ff;
  font-weight: 600;
  font-size: 1.1rem;
  min-width: 60px;
}

.breed-description {
  color: #595959;
  line-height: 1.6;
  font-size: 14px;
  margin-bottom: 20px;
}

.breed-characteristics h4 {
  margin: 0 0 15px 0;
  color: #262626;
  font-size: 1.1rem;
  font-weight: 600;
}

.characteristic-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #595959;
}

.characteristic-item .el-icon {
  color: #1890ff;
}

.alternative-breeds {
  margin-top: 25px;
}

.alternative-breeds h4 {
  margin: 0 0 15px 0;
  color: #262626;
  font-size: 1.1rem;
  font-weight: 600;
}

.breed-list {
  display: grid;
  gap: 15px;
}

.breed-item {
  background: white;
  border-radius: 8px;
  padding: 15px;
  border: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
}

.breed-item:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
}

.breed-name {
  font-weight: 500;
  color: #262626;
}

.breed-confidence {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 100px;
}

.breed-confidence span {
  color: #8c8c8c;
  font-size: 12px;
  min-width: 40px;
}

.error-section {
  margin-bottom: 30px;
}

.loading-section {
  margin-bottom: 30px;
}

.loading-content {
  text-align: center;
  padding: 40px 20px;
}

.loading-icon {
  color: #1890ff;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.loading-content h3 {
  margin: 0 0 10px 0;
  color: #262626;
  font-size: 1.3rem;
}

.loading-content p {
  color: #8c8c8c;
  margin: 0 0 30px 0;
}
</style>
