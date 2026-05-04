<template>
  <div class="create-cat">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">添加猫咪信息</h1>
      <p class="page-subtitle">请填写猫咪的基本信息和上传相关图片</p>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="content-wrapper">
      <!-- 合并后的表单区域 -->
      <div class="form-section">
        <div class="section-header">
          <el-icon class="section-icon"><Edit /></el-icon>
          <h3 class="section-title">猫咪基本信息</h3>
        </div>
        
        <el-form :model="form" label-width="100px" class="cat-form">
          <!-- 图片上传区域 -->
          <el-form-item label="猫咪图片">
            <div class="image-upload-section">
              <el-upload
                action="#"
                list-type="picture-card"
                :auto-upload="false"
                :on-change="handleImageChange"
                :on-remove="handleImageRemove"
                :file-list="fileList"
                multiple
                :before-upload="beforeImageUpload"
              >
                <el-icon><Plus /></el-icon>
                <div class="upload-text">
                  <div>点击上传</div>
                  <div class="upload-tip">支持上传多张图片，建议图片大小不超过2MB</div>
                </div>
              </el-upload>
            </div>
          </el-form-item>
      <el-form-item label="猫咪名称" required>
        <el-input v-model="form.name" placeholder="请输入猫咪名称" clearable></el-input>
      </el-form-item>
      
      <el-form-item label="猫咪品种" required>
        <div class="breed-input-container">
          <el-input 
            v-model="form.breed" 
            placeholder="请输入品种或使用AI识别" 
            clearable
            class="breed-input"
          >
            <template #append>
              <el-button 
                :icon="Search" 
                @click="showBreedRecognizer = true"
              >
                AI识别
              </el-button>
            </template>
          </el-input>
        </div>
        
        <!-- AI识别结果提示 -->
        <div v-if="recognizedBreed" class="ai-result-tip">
          <el-icon><Check /></el-icon>
          <span>AI识别结果：{{ recognizedBreed }}</span>
          <el-button type="text" @click="clearBreedResult">清除</el-button>
        </div>
      </el-form-item>
      
      <!-- 年龄和体重放在同一行 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="年龄（月）" required>
            <el-input-number 
              v-model="form.age" 
              :min="1" 
              :max="240" 
              placeholder="请输入年龄"
              style="width: 100%"
            ></el-input-number>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="体重（kg）">
            <el-input-number 
              v-model="form.weight" 
              :min="0.1" 
              :max="20" 
              :step="0.1" 
              placeholder="请输入体重"
              style="width: 100%"
            ></el-input-number>
          </el-form-item>
        </el-col>
      </el-row>
      
  
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="性别" required>
            <el-radio-group v-model="form.gender">
              <el-radio label="公">公</el-radio>
              <el-radio label="母">母</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="健康状况" required>
            <el-select v-model="form.healthStatus" placeholder="请选择健康状况" style="width: 100%;">
              <el-option label="健康" value="健康"></el-option>
              <el-option label="恢复中" value="恢复中"></el-option>
              <el-option label="残疾" value="残疾"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="性格特点">
        <el-input v-model="form.personality" placeholder="请输入性格特点" clearable></el-input>
      </el-form-item>
      
      <el-form-item label="医疗信息">
        <el-input 
          type="textarea" 
          v-model="form.medicalInfo" 
          placeholder="请输入医疗信息，如疫苗接种情况、病史、用药情况等"
          :rows="3"
          maxlength="500"
          show-word-limit
        ></el-input>
      </el-form-item>
      
      <el-form-item label="所在位置" required>
        <el-input v-model="form.location" placeholder="请输入猫咪所在位置" clearable></el-input>
      </el-form-item>
      
      <el-form-item label="猫咪状态" required>
        <el-select v-model="form.status" placeholder="请选择猫咪状态" style="width: 100%;">
          <el-option label="待领养" value="待领养"></el-option>
          <el-option label="照顾中" value="照顾中"></el-option>
          <el-option label="已领养" value="已领养"></el-option>
        </el-select>
      </el-form-item>
      
      <el-form-item label="详细描述" required>
        <el-input 
          type="textarea" 
          v-model="form.description" 
          placeholder="请输入猫咪详细描述"
          :rows="4"
          maxlength="500"
          show-word-limit
        ></el-input>
      </el-form-item>
      
          <el-form-item>
            <el-button type="primary" @click="submit" :loading="loading" size="large" class="submit-btn">
              <el-icon class="submit-icon"><Check /></el-icon>
              {{ submitButtonText }}
            </el-button>
            <el-button @click="$router.back()" size="large" class="cancel-btn">
              <el-icon><Close /></el-icon>
              取消
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
    
    <!-- AI品种识别对话框 -->
    <el-dialog
      v-model="showBreedRecognizer"
      title="🐱 AI猫咪品种识别"
      width="900px"
      custom-class="breed-recognizer-dialog"
    >
      <BreedRecognizer 
        ref="breedRecognizerRef"
        @breed-selected="handleBreedSelected"
        @close="showBreedRecognizer = false"
      />
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showBreedRecognizer = false">取消</el-button>
          <el-button type="primary" @click="handleBreedRecognizerClose">保存结果</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>


<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search, Check, Edit, Close } from '@element-plus/icons-vue'
import { catApi } from '@/api/cat.js'
import { authApi } from '@/api/auth.js'
import { useAuthStore } from '@/stores/auth.js'
import BreedRecognizer from '@/components/BreedRecognizer.vue'
import { loadKNNModel, isKNNModelAvailable } from '@/utils/catKNNPredictor'
import { loadImprovedModel, isImprovedModelAvailable } from '@/utils/improvedCatPredictor'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const fileList = ref([])
const breedRecognizerRef = ref(null)
const showBreedRecognizer = ref(false)
const recognizedBreed = ref('')

// 组件挂载时预加载AI模型
onMounted(() => {
  console.log('正在预加载AI识别模型...')
  
  Promise.allSettled([
    loadKNNModel(),
    loadImprovedModel()
  ]).then((results) => {
    console.log('AI模型预加载完成')
    const modelNames = ['KNN模型', '改进版KNN模型']
    results.forEach((result, index) => {
      if (result.status === 'fulfilled') {
        console.log(`${modelNames[index]} 加载成功`)
      } else {
        console.log(`${modelNames[index]} 加载失败:`, result.reason)
      }
    })
  })
})

// 根据用户角色显示不同的按钮文本
const submitButtonText = computed(() => {
  if (authStore.isAdmin) {
    return '确认添加'
  } else {
    return '提交审核'
  }
})

const form = reactive({
  name: '',
  breed: '',
  age: 1,
  weight: 3.0,
  gender: '公',
  healthStatus: '健康',
  medicalInfo: '',
  personality: '',
  location: '',
  status: '待领养',
  description: '',
  images: '[]'
})

// 处理图片上传
const handleImageChange = (file, uploadedFiles) => {
  fileList.value = uploadedFiles
  // 将图片数据保存到表单的images字段
  updateImagesField()
  // 自动触发AI识别
  autoAnalyzeBreed()
}

// 处理图片删除
const handleImageRemove = (file, uploadedFiles) => {
  fileList.value = uploadedFiles
  // 更新表单的images字段
  updateImagesField()
}

// 更新表单的images字段
const updateImagesField = () => {
  if (fileList.value.length === 0) {
    form.images = '[]'
    return
  }
  
  // 将文件列表转换为图片路径数组
  const imagePaths = fileList.value.map(file => {
    // 如果是已经上传的图片，直接使用url
    if (file.url) {
      return file.url
    }
    // 如果是新上传的图片，创建临时URL
    if (file.raw) {
      return URL.createObjectURL(file.raw)
    }
    return ''
  }).filter(path => path !== '')
  
  form.images = JSON.stringify(imagePaths)
}

// 图片上传前的验证
const beforeImageUpload = (file) => {
  const isJPGOrPNG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPGOrPNG) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 打开AI识别对话框
const openBreedRecognizer = () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先上传猫咪图片')
    return
  }
  
  // 先打开对话框
  showBreedRecognizer.value = true
  
  // 等待DOM更新
  nextTick(() => {
    // 延迟一下，确保组件完全初始化
    setTimeout(() => {
      // 获取第一张图片
      const firstFile = fileList.value[0]
      let imageUrl = ''
      
      if (firstFile.url) {
        imageUrl = firstFile.url
      } else if (firstFile.raw) {
        imageUrl = URL.createObjectURL(firstFile.raw)
      }
      
      if (imageUrl) {
        // 设置图片
        if (breedRecognizerRef.value && breedRecognizerRef.value.setImage) {
          breedRecognizerRef.value.setImage(imageUrl)
        }
        // 延迟执行识别，确保图片已加载
        setTimeout(() => {
          if (breedRecognizerRef.value && breedRecognizerRef.value.recognizeBreed) {
            breedRecognizerRef.value.recognizeBreed()
          }
        }, 400)
      } else {
        ElMessage.error('无法获取图片数据，请重新上传')
      }
    }, 100)
  })
}

// 自动AI识别（上传图片后自动触发）
const autoAnalyzeBreed = async () => {
  if (fileList.value.length > 0) {
    // 延迟1秒后自动打开AI识别对话框
    setTimeout(() => {
      openBreedRecognizer()
    }, 1000)
  }
}

// 处理AI识别结果
const handleBreedSelected = (breed) => {
  form.breed = breed
  recognizedBreed.value = breed
  showBreedRecognizer.value = false
  ElMessage.success(`🎯 AI识别成功：${breed} 已自动填入品种字段`)
}

// 清除AI识别结果
const clearBreedResult = () => {
  recognizedBreed.value = ''
  form.breed = ''
  ElMessage.info('已清除AI识别结果')
}

// 处理AI识别对话框关闭
const handleBreedRecognizerClose = () => {
  if (breedRecognizerRef.value && breedRecognizerRef.value.getResult) {
    const result = breedRecognizerRef.value.getResult()
    if (result && result.primaryBreed) {
      const breed = result.primaryBreed.name
      form.breed = breed
      recognizedBreed.value = breed
      ElMessage.success(`🎯 AI识别成功：${breed} 已自动填入品种字段`)
    } else {
      console.log('识别结果为空')
    }
  }
  showBreedRecognizer.value = false
}

// 提交猫咪信息
const submit = async () => {
  // 表单验证
  if (!form.name || !form.breed || !form.age || !form.location || !form.description) {
    ElMessage.error('请填写必填字段')
    return
  }

  try {
    loading.value = true
    
    // 处理图片数据
    const imageUrls = []
    
    // 上传所有图片
    for (const file of fileList.value) {
      if (file.raw) {
        const response = await authApi.uploadFile(file.raw)
        imageUrls.push(response.data.fileUrl)
      }
    }
    
    // 设置图片数据
    form.images = JSON.stringify(imageUrls)
    
    const isAdmin = authStore.isAdmin
    
    // 设置救助者ID（从当前登录用户获取）
    if (!isAdmin) {
      // 救援者添加猫咪时，设置救助者ID为当前用户ID
      form.rescuerId = authStore.user?.id
    } else {
      // 管理员添加猫咪时，救助者ID可以为空或设置为默认值
      form.rescuerId = null
    }
    
    if (isAdmin) {
      // 管理员：直接添加，无需审核
      const response = await catApi.createCat(form)
      ElMessage.success('猫咪信息添加成功')
      router.push('/admin/cats')
    } else {
      // 救援者：提交审核
      const response = await catApi.createCat(form)
      ElMessage.success('猫咪信息提交成功，等待审核')
      router.push('/rescuer/cats')
    }
  } catch (error) {
    ElMessage.error('提交失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.create-cat {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 40px 20px;
}

/* AI识别对话框样式 */
:deep(.breed-recognizer-dialog) {
  height: 600px !important;
  max-height: 600px !important;
  display: flex !important;
  flex-direction: column !important;
}

:deep(.breed-recognizer-dialog .el-dialog__body) {
  flex: 1 !important;
  overflow-y: auto !important;
  padding: 20px !important;
  height: 460px !important;
  max-height: 460px !important;
  min-height: 460px !important;
}

:deep(.breed-recognizer-dialog .el-dialog__header) {
  padding: 20px 20px 10px !important;
  border-bottom: 1px solid #e8e8e8 !important;
  height: 70px !important;
  min-height: 70px !important;
  max-height: 70px !important;
}

:deep(.breed-recognizer-dialog .el-dialog__footer) {
  padding: 10px 20px 20px !important;
  border-top: 1px solid #e8e8e8 !important;
  height: 70px !important;
  min-height: 70px !important;
  max-height: 70px !important;
}

/* 自定义滚动条样式 */
:deep(.breed-recognizer-dialog .el-dialog__body)::-webkit-scrollbar {
  width: 8px;
}

:deep(.breed-recognizer-dialog .el-dialog__body)::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

:deep(.breed-recognizer-dialog .el-dialog__body)::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

:deep(.breed-recognizer-dialog .el-dialog__body)::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 页面头部 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 1.1rem;
  color: #7f8c8d;
  font-weight: 400;
  line-height: 1.6;
}

/* 主要内容区域 */
.content-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

/* 表单区域 */
.form-section {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
}

/* 区域头部 */
.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f2f5;
}

.section-icon {
  font-size: 1.5rem;
  color: #3498db;
  margin-right: 12px;
}

.section-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

/* 图片上传区域 */
.image-upload-section {
  width: 100%;
}

.upload-text {
  text-align: center;
  margin-top: 8px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

/* 表单样式 */
.cat-form {
  width: 100%;
}

.cat-form :deep(.el-form-item) {
  margin-bottom: 25px;
}

.cat-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.95rem;
}

.cat-form :deep(.el-input),
.cat-form :deep(.el-select),
.cat-form :deep(.el-textarea) {
  width: 100%;
}

.cat-form :deep(.el-input__wrapper),
.cat-form :deep(.el-textarea__inner) {
  border-radius: 10px;
  border: 1px solid #e1e8ed;
  transition: all 0.3s ease;
  background: #fafbfc;
}

.cat-form :deep(.el-input__wrapper:hover),
.cat-form :deep(.el-textarea__inner:hover) {
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.1);
}

.cat-form :deep(.el-input__wrapper.is-focus),
.cat-form :deep(.el-textarea__inner:focus) {
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

/* 按钮样式 */
.submit-btn {
  border: none;
  border-radius: 10px;
  padding: 12px 30px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.submit-icon {
  margin-right: 8px;
}

.cancel-btn {
  border-radius: 10px;
  padding: 12px 30px;
  font-weight: 600;
  transition: all 0.3s ease;
  border: 1px solid #dcdfe6;
}

.cancel-btn:hover {
  border-color: #3498db;
  color: #3498db;
  transform: translateY(-2px);
}

/* AI识别结果提示 */
.ai-result-tip {
  margin-top: 10px;
  padding: 12px 15px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
  border: 1px solid #bae7ff;
  border-radius: 8px;
  color: #1890ff;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 8px;
  animation: fadeInUp 0.5s ease;
}

.ai-result-tip .el-icon {
  color: #52c41a;
}

.ai-result-tip .el-button {
  margin-left: auto;
  font-size: 0.8rem;
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .create-cat {
    padding: 20px 15px;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .page-subtitle {
    font-size: 1rem;
  }
  
  .form-section {
    padding: 20px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .section-icon {
    margin-right: 0;
  }
  
  .cat-form :deep(.el-form-item) {
    margin-bottom: 20px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.8rem;
  }
  
  .form-section {
    padding: 15px;
    border-radius: 12px;
  }
  
  .submit-btn,
  .cancel-btn {
    width: 100%;
    margin-bottom: 10px;
  }
}
</style>