<template>
  <div class="edit-cat-container">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="$router.back()" :icon="Back">返回</el-button>
        <h1>编辑猫咪信息</h1>
      </div>
    </div>

    <el-card class="form-card">
      <el-form 
        :model="form" 
        :rules="rules"
        ref="formRef"
        label-width="120px" 
        class="cat-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="猫咪名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入猫咪名称"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品种" prop="breed">
              <el-input v-model="form.breed" placeholder="请输入品种"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="0" :max="30" style="width: 100%">
                <template #suffix>个月</template>
              </el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="公" value="MALE"></el-option>
                <el-option label="母" value="FEMALE"></el-option>
                <el-option label="未知" value="UNKNOWN"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="体重(kg)" prop="weight">
              <el-input-number v-model="form.weight" :min="0" :max="50" :precision="1" style="width: 100%">
                <template #suffix>kg</template>
              </el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="位置" prop="location">
              <el-input v-model="form.location" placeholder="请输入猫咪所在位置"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="健康状况" prop="healthStatus">
              <el-select v-model="form.healthStatus" placeholder="请选择健康状况" style="width: 100%">
                <el-option label="健康" value="健康"></el-option>
                <el-option label="恢复中" value="恢复中"></el-option>
                <el-option label="残疾" value="残疾"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择猫咪状态" style="width: 100%">
                <el-option label="照顾中" value="照顾中"></el-option>
                <el-option label="待领养" value="待领养"></el-option>
                <el-option label="已领养" value="已领养"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 猫咪图片上传 -->
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
            <div class="current-images" v-if="currentImages.length > 0">
              <h4>当前图片：</h4>
              <div class="image-preview-list">
                <div v-for="(image, index) in currentImages" :key="index" class="image-preview-item">
                  <img :src="image" :alt="'猫咪图片' + (index + 1)" />
                  <div class="image-actions">
                    <el-button type="danger" size="small" @click="removeCurrentImage(index)">删除</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="猫咪描述" prop="description">
          <el-input 
            type="textarea" 
            v-model="form.description" 
            :rows="4"
            placeholder="请输入猫咪描述"
            maxlength="500"
            show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item label="性格特点" prop="personality">
          <el-input 
            type="textarea" 
            v-model="form.personality" 
            :rows="3"
            placeholder="请输入性格特点"
            maxlength="200"
            show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item label="医疗信息" prop="medicalInfo">
          <el-input 
            type="textarea" 
            v-model="form.medicalInfo" 
            :rows="3"
            placeholder="请输入医疗信息，如疫苗接种情况、病史、用药情况等"
            maxlength="500"
            show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button @click="$router.back()">取消</el-button>
            <el-button type="primary" @click="submit" :loading="submitting">保存修改</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, Plus } from '@element-plus/icons-vue'
import { catApi } from '@/api/cat'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const submitting = ref(false)

// 图片上传相关
const fileList = ref([])
const currentImages = ref([])

const form = ref({
  name: '',
  breed: '',
  age: null,
  gender: 'UNKNOWN',
  weight: null,
  location: '',
  healthStatus: 'HEALTHY',
  status: 'WAITING_RESCUE',
  description: '',
  personality: '',
  medicalInfo: '',
  images: '[]'
})

const rules = {
  name: [
    { required: true, message: '请输入猫咪名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  breed: [
    { required: true, message: '请输入品种', trigger: 'blur' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  healthStatus: [
    { required: true, message: '请选择健康状况', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择猫咪状态', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请输入位置', trigger: 'blur' }
  ]
}

const loadCatDetail = async () => {
  const catId = route.params.id
  if (!catId) {
    ElMessage.error('猫咪ID不存在')
    router.push('/cats')
    return
  }

  try {
    const response = await catApi.getCat(catId)
    const catData = response.data
    
    form.value = {
      name: catData.name || '',
      breed: catData.breed || '',
      age: catData.age || null,
      gender: catData.gender || 'UNKNOWN',
      weight: catData.weight || null,
      location: catData.location || '',
      healthStatus: catData.healthStatus || 'HEALTHY',
      medicalInfo: catData.medicalInfo || '',
      status: catData.status || 'WAITING_RESCUE',
      description: catData.description || '',
      personality: catData.personality || '',
      images: catData.images || '[]'
    }
    
    // 加载当前图片
    if (catData.images) {
      try {
        const images = JSON.parse(catData.images)
        currentImages.value = Array.isArray(images) ? images : []
      } catch (e) {
        currentImages.value = []
      }
    }
  } catch (error) {
    console.error('加载猫咪信息失败:', error)
    ElMessage.error('加载猫咪信息失败')
    router.push('/cats')
  }
}

// 图片上传相关函数
const handleImageChange = (file, files) => {
  fileList.value = files
}

const handleImageRemove = (file, files) => {
  fileList.value = files
}

const beforeImageUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传图片只能是 JPG/PNG/GIF 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
    return false
  }
  return true
}

const removeCurrentImage = (index) => {
  currentImages.value.splice(index, 1)
  // 更新表单中的图片数据
  form.value.images = JSON.stringify(currentImages.value)
}

const submit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请填写完整信息')
      return
    }

    try {
      submitting.value = true
      
      // 处理新上传的图片
      const newImageUrls = []
      for (const file of fileList.value) {
        if (file.raw) {
          const response = await authApi.uploadFile(file.raw)
          newImageUrls.push(response.data.fileUrl)
        }
      }
      
      // 合并当前图片和新上传的图片
      const allImages = [...currentImages.value, ...newImageUrls]
      form.value.images = JSON.stringify(allImages)
      
      const catId = route.params.id
      await catApi.updateCat(catId, form.value)
      ElMessage.success('猫咪信息更新成功')
      
      // 根据用户角色跳转到不同的页面
      const authStore = useAuthStore()
      if (authStore.isAdmin) {
        router.push('/admin/cats')
      } else {
        router.push('/rescuer/cats')
      }
    } catch (error) {
      console.error('更新猫咪信息失败:', error)
      ElMessage.error(error.response?.data?.message || '更新猫咪信息失败')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadCatDetail()
})
</script>

<style scoped>
.edit-cat-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.form-card {
  max-width: 800px;
  margin: 0 auto;
}

.cat-form {
  padding: 20px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

/* 图片上传样式 */
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

.current-images {
  margin-top: 20px;
}

.current-images h4 {
  margin-bottom: 10px;
  color: #606266;
}

.image-preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image-preview-item {
  position: relative;
  width: 100px;
  height: 100px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.image-preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.5);
  padding: 5px;
  display: flex;
  justify-content: center;
}

.image-actions .el-button {
  color: white;
  background: rgba(255, 255, 255, 0.2);
  border: none;
}

.image-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>
