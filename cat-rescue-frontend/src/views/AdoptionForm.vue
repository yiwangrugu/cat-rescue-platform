<template>
  <div class="adoption-form-container">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Document /></el-icon>
          领养申请
        </h1>
        <p class="page-subtitle">填写领养申请，为流浪猫咪提供一个温暖的家</p>
      </div>
    </div>

    <div class="form-content">
      <div class="three-column-layout">
        <!-- 左侧：猫咪信息 -->
        <div class="column left-column" v-if="cat.id">
          <el-card shadow="never" class="cat-info-card">
            <template #header>
              <div class="card-header">
                <span class="header-title">猫咪信息</span>
              </div>
            </template>

            <div class="cat-detail">
              <el-image 
                :src="getCatImage()" 
                fit="cover"
                class="cat-detail-image"
              />
              <div class="cat-detail-info">
                <h3 class="cat-name-large">{{ cat.name }}</h3>
                <div class="info-row">
                  <span class="label">品种：</span>
                  <span class="value">{{ cat.breed || '未知' }}</span>
                </div>
                <div class="info-row">
                  <span class="label">年龄：</span>
                  <span class="value">{{ formatAge(cat.age) }}</span>
                </div>
                <div class="info-row">
                  <span class="label">性别：</span>
                  <span class="value">{{ getGenderText(cat.gender) }}</span>
                </div>
                <div class="info-row">
                  <span class="label">健康状况：</span>
                  <span class="value">{{ getHealthText(cat.healthStatus) }}</span>
                </div>
                <div class="info-row">
                  <span class="label">性格：</span>
                  <span class="value">{{ cat.personality || '未知' }}</span>
                </div>
                <div class="cat-description">
                  <p>{{ cat.description || '暂无描述' }}</p>
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 中间：领养申请表 -->
        <div class="column middle-column">
          <el-card shadow="never" class="form-card">
            <template #header>
              <div class="card-header">
                <span class="header-title">领养申请表</span>
                <el-tag type="info" size="small">请认真填写以下信息</el-tag>
              </div>
            </template>

            <el-form 
              ref="formRef" 
              :model="form" 
              :rules="rules" 
              label-width="100px"
              label-position="left"
              class="compact-form"
            >

              <el-form-item label="姓名" prop="name">
                <el-input v-model="form.name" placeholder="请填写您的真实姓名" />
              </el-form-item>

              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="form.idCard" placeholder="请填写您的身份证号码" maxlength="18" show-word-limit />
              </el-form-item>

              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="form.phone" placeholder="请填写您的手机号码" maxlength="11" show-word-limit />
              </el-form-item>

              <el-form-item label="家庭住址" prop="address">
                <el-input v-model="form.address" placeholder="请填写您的详细家庭住址" maxlength="200" show-word-limit />
              </el-form-item>

              <el-form-item label="申请理由" prop="applicationReason">
                <el-input
                  v-model="form.applicationReason"
                  type="textarea"
                  :rows="4"
                  placeholder="请详细说明您想领养这只猫咪的理由，例如：喜欢它的性格、想给它一个温暖的家等"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="居住环境" prop="livingCondition">
                <el-input
                  v-model="form.livingCondition"
                  type="textarea"
                  :rows="3"
                  placeholder="请描述您的居住环境，例如：公寓/别墅、是否有阳台、是否允许养宠物等"
                  maxlength="300"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="养猫经验" prop="experience">
                <el-select v-model="form.experience" placeholder="请选择您的养猫经验" style="width: 100%">
                  <el-option label="无经验" value="无经验" />
                  <el-option label="1年以下" value="1年以下" />
                  <el-option label="1-2年" value="1-2年" />
                  <el-option label="3-5年" value="3-5年" />
                  <el-option label="5年以上" value="5年以上" />
                </el-select>
              </el-form-item>

              <el-form-item label="家庭成员">
                <el-input
                  v-model="form.familyMembers"
                  placeholder="请说明家庭成员情况，例如：与父母同住、有小孩、有其他宠物等"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="经济状况">
                <el-input
                  v-model="form.financialStatus"
                  placeholder="请说明您是否有能力承担猫咪的日常开销（食物、医疗、疫苗等）"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="工作时间">
                <el-input
                  v-model="form.workSchedule"
                  placeholder="请说明您的工作时间安排，例如：朝九晚五、经常出差等"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="其他说明">
                <el-input
                  v-model="form.additionalInfo"
                  type="textarea"
                  :rows="3"
                  placeholder="其他您想补充的信息"
                  maxlength="300"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item>
                <el-checkbox v-model="form.agreed">
                  我已阅读并同意《领养协议》，承诺会善待猫咪，不离不弃
                </el-checkbox>
              </el-form-item>

              <el-form-item>
                <div class="form-actions">
                  <el-button size="large" @click="goBack">
                    <el-icon><Back /></el-icon>返回
                  </el-button>
                  <el-button 
                    type="primary" 
                    size="large" 
                    @click="submitForm"
                    :loading="submitting"
                    :disabled="!form.agreed"
                  >
                    <el-icon><Check /></el-icon>提交申请
                  </el-button>
                </div>
              </el-form-item>
            </el-form>
          </el-card>
        </div>

        <!-- 右侧：领养须知 -->
        <div class="column right-column">
          <el-card shadow="never" class="info-card">
            <template #header>
              <div class="card-header">
                <span class="header-title">领养须知</span>
              </div>
            </template>

            <div class="notice-content">
              <h4>领养前请确认</h4>
              <ul class="notice-list">
                <li>您已获得家庭成员的一致同意</li>
                <li>您有稳定的住所和经济来源</li>
                <li>您有足够的时间和精力照顾猫咪</li>
                <li>您承诺不会随意遗弃猫咪</li>
                <li>您愿意为猫咪提供必要的医疗和疫苗</li>
              </ul>

              <h4>领养流程</h4>
              <el-steps direction="vertical" :space="60" class="steps-vertical">
                <el-step title="提交申请" description="填写并提交领养申请表" />
                <el-step title="审核" description="救助人员审核您的申请" />
                <el-step title="面谈" description="通过审核后安排面谈" />
                <el-step title="完成领养" description="签署协议并接猫咪回家" />
              </el-steps>

              <el-alert
                title="温馨提示"
                type="warning"
                :closable="false"
                show-icon
              >
                <p>提交申请后，救助人员会在1-3个工作日内联系您。请保持电话畅通。</p>
              </el-alert>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Back, Check } from '@element-plus/icons-vue'
import { catApi } from '@/api/cat'
import { adoptionApi } from '@/api/adoption'
import { applicantApi } from '@/api/applicant'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const formRef = ref(null)
const submitting = ref(false)
const cat = ref({})
const loading = ref(false)

const form = ref({
    catId: null,
    name: '',
    idCard: '',
    phone: '',
    address: '',
    applicationReason: '',
    livingCondition: '',
    experience: '',
    familyMembers: '',
    financialStatus: '',
    workSchedule: '',
    additionalInfo: '',
    agreed: false
  })

  const rules = {
    name: [
      { required: true, message: '请填写姓名', trigger: 'blur' }
    ],
    idCard: [
      { required: true, message: '请填写身份证号码', trigger: 'blur' },
      { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '请输入正确的身份证号码', trigger: 'blur' }
    ],
    phone: [
      { required: true, message: '请填写联系电话', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
    ],
    address: [
      { required: true, message: '请填写家庭住址', trigger: 'blur' }
    ],
    applicationReason: [
      { required: true, message: '请填写申请理由', trigger: 'blur' },
      { min: 20, message: '申请理由至少20个字符', trigger: 'blur' }
    ],
    livingCondition: [
      { required: true, message: '请填写居住环境', trigger: 'blur' },
      { min: 10, message: '居住环境描述至少10个字符', trigger: 'blur' }
    ],
    experience: [
      { required: true, message: '请选择养猫经验', trigger: 'change' }
    ]
  }

const getCatImage = () => {
  console.log('猫咪图片数据:', cat.value.images) // 调试日志
  
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
            return image.startsWith('http') ? image : `${import.meta.env.VITE_API_BASE_URL || ''}${image}`
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
      const imageUrl = processedImages[0] || '/img/placeholder-cat.jpg'
      console.log('处理后的图片URL:', imageUrl) // 调试日志
      return imageUrl
    } catch (error) {
      console.error('解析图片数据失败:', error)
      return '/img/placeholder-cat.jpg'
    }
  }
  return '/img/placeholder-cat.jpg'
}

const formatAge = (age) => {
  if (!age) return '未知'
  return `${age}个月`
}

const getGenderText = (gender) => {
  console.log('性别枚举值:', gender) // 调试日志
  const texts = {
    '公': '公',
    '母': '母',
    '未知': '未知',
    'MALE': '公',
    'FEMALE': '母',
    'UNKNOWN': '未知'
  }
  return texts[gender] || '未知'
}

const getHealthText = (health) => {
  console.log('健康状态枚举值:', health) // 调试日志
  const texts = {
    '良好': '健康',
    '一般': '需要照顾',
    '需要治疗': '需要治疗',
    '健康': '健康',
    '需要照顾': '需要照顾',
    '残疾': '残疾',
    'HEALTHY': '健康',
    'NEEDS_CARE': '需要照顾',
    'DISABLED': '残疾'
  }
  return texts[health] || '未知'
}

const loadCatDetail = async () => {
  try {
    loading.value = true
    const catId = route.query.catId
    console.log('路由参数中的猫咪ID:', catId) // 调试日志
    
    if (catId) {
      // 从猫咪详情页面跳转过来，加载指定猫咪信息
      const response = await catApi.getCat(catId)
      console.log('API返回的猫咪数据:', response.data) // 调试日志
      
      if (response.data) {
        // 在设置猫咪数据前先检查状态
        if (response.data.status !== '待领养') {
          ElMessage.warning('该猫咪暂时不可领养')
          // 直接跳转回猫咪详情页面，不显示领养表单
          router.push(`/cats/${catId}`)
          return
        }
        
        cat.value = response.data
        form.value.catId = catId
        console.log('设置后的猫咪数据:', cat.value) // 调试日志
      }
    } else {
      // 直接访问领养申请页面，显示通用表单
      cat.value = {}
      form.value.catId = null
      console.log('没有猫咪ID，显示通用表单') // 调试日志
    }
  } catch (error) {
    console.error('加载猫咪信息失败:', error)
    if (route.query.catId) {
      ElMessage.error('加载猫咪信息失败')
      router.push('/cats')
    }
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    if (!form.value.agreed) {
      ElMessage.warning('请先同意领养协议')
      return
    }

    try {
      submitting.value = true

      // 1. 先保存申请人信息（包含申请理由、居住环境、养猫经验）
      const applicantData = {
        realName: form.value.name,
        idCard: form.value.idCard,
        phone: form.value.phone,
        address: form.value.address,
        familyMembers: form.value.familyMembers,
        financialStatus: form.value.financialStatus,
        workSchedule: form.value.workSchedule,
        additionalInfo: form.value.additionalInfo,
        applicationReason: form.value.applicationReason,
        livingCondition: form.value.livingCondition,
        experience: form.value.experience
      }

      const applicantResponse = await applicantApi.saveApplicantInfo(applicantData)
      if (!applicantResponse.data) {
        throw new Error('保存申请人信息失败')
      }

      // 2. 再提交领养申请（只需要猫咪ID，其他信息在申请人表中）
      const response = await adoptionApi.createAdoption({
        catId: form.value.catId
      })

      if (response.data) {
        await ElMessageBox.alert(
          '您的领养申请已提交成功！救助人员会在1-3个工作日内审核您的申请，请保持电话畅通。',
          '提交成功',
          {
            confirmButtonText: '查看我的申请',
            type: 'success'
          }
        )
        router.push('/adoptions')
      }
    } catch (error) {
      console.error('提交申请失败:', error)
      ElMessage.error(error.response?.data?.message || '提交申请失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

const goBack = () => {
  if (cat.value.id) {
    router.push(`/cats/${cat.value.id}`)
  } else {
    router.push('/cats')
  }
}

onMounted(() => {
  if (!authStore.isAuthenticated) {
    ElMessageBox.confirm('请先登录才能申请领养，是否跳转到登录页面？', '需要登录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push('/login')
    }).catch(() => {
      router.push('/cats')
    })
    return
  }

  checkApplicantInfo()
  loadCatDetail()
})

// 检查申请人信息
const checkApplicantInfo = async () => {
  try {
    const response = await applicantApi.getMyApplicantInfo()
    if (response.data && response.data.hasInfo) {
      // 已有申请人信息，填充到表单
      const applicant = response.data.applicant
      form.value.name = applicant.realName || ''
      form.value.idCard = applicant.idCard || ''
      form.value.phone = applicant.phone || ''
      form.value.address = applicant.address || ''
      form.value.familyMembers = applicant.familyMembers || ''
      form.value.financialStatus = applicant.financialStatus || ''
      form.value.workSchedule = applicant.workSchedule || ''
      form.value.additionalInfo = applicant.additionalInfo || ''
    }
  } catch (error) {
    console.log('获取申请人信息失败:', error)
  }
}
</script>

<style scoped>
.adoption-form-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e3e8f0 100%);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  box-sizing: border-box;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 32px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
  width: 100%;
  max-width: 1450px;
  height: 94px;
}

.header-content {
  color: white;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
  line-height: 1.6;
}

.form-content {
  margin-bottom: 32px;
  width: 100%;
  max-width: 1550px;
  display: flex;
  justify-content: center;
  flex: 1;
}

.three-column-layout {
  display: flex;
  gap: 24px;
  width: 100%;
  max-width: 1550px;
  align-items: stretch; /* 确保高度一致 */
}

.column {
  display: flex;
  flex-direction: column;
  gap: 24px;
  height: 100%; /* 确保高度继承 */
}

/* 确保卡片撑满容器高度 */
.column > .el-card {
  flex: 1; /* 卡片撑满容器高度 */
  display: flex;
  flex-direction: column;
  min-height: 500px; /* 设置最小高度确保一致性 */
}

.column > .el-card .el-card__body {
  flex: 1; /* 卡片内容区域撑满 */
  display: flex;
  flex-direction: column;
}

/* 猫咪信息卡片内容区域高度处理 */
.cat-info-card .el-card__body {
  height: 100%;
}

.cat-detail {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 领养须知卡片内容区域高度处理 */
.info-card .el-card__body {
  height: 100%;
  overflow: visible; /* 确保内容不被隐藏 */
}

.notice-content {
  height: auto; /* 高度自适应内容 */
  display: flex;
  flex-direction: column;
  gap: 16px; /* 减少间距 */
}

.notice-content h4 {
  margin: 0 0 8px 0; /* 减少标题间距 */
}

.notice-list {
  margin: 0;
  padding-left: 20px;
}

.steps-vertical {
  margin: 0;
}

/* 领养申请表内容区域高度处理 */
.form-card .el-card__body {
  height: 100%;
  padding-bottom: 0; /* 移除底部内边距 */
}

.compact-form {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.compact-form .el-form {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-bottom: 0; /* 移除底部边距 */
}

.compact-form .el-form .el-form-item {
  margin-bottom: 16px; /* 减少表单项间距 */
}

.compact-form .el-form .el-form-item:last-child {
  margin-top: auto; /* 将操作按钮推到最下方 */
  margin-bottom: 0; /* 移除底部边距 */
}

.left-column {
  flex: 1; /* 1:3:1比例中的1 */
  min-width: 0; /* 允许收缩 */
}

.middle-column {
  flex: 3; /* 1:3:1比例中的3 */
  min-width: 0; /* 允许收缩 */
}

.right-column {
  flex: 1; /* 1:3:1比例中的1 */
  min-width: 0; /* 允许收缩 */
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .three-column-layout {
    flex-wrap: wrap;
    justify-content: center;
    min-height: auto; /* 移除固定高度 */
  }
  
  .left-column,
  .right-column {
    flex: 1; /* 平板端保持1:1比例 */
    min-width: 280px;
    max-width: 400px;
  }
  
  .middle-column {
    flex: 2 1 100%; /* 中间栏占更多空间 */
    order: 3;
    min-width: 100%;
    max-width: 100%;
  }
}

@media (max-width: 768px) {
  .three-column-layout {
    flex-direction: column;
    gap: 16px;
    min-height: auto; /* 移除固定高度 */
  }
  
  .left-column,
  .middle-column,
  .right-column {
    flex: 1 1 100%;
    min-width: 100%;
    max-width: 100%;
    height: auto; /* 移动端高度自适应 */
  }
  
  .left-column,
  .right-column {
    order: 2;
  }
  
  .middle-column {
    order: 1;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #e4e7ed;
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.form-card {
  margin-bottom: 0;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: none;
}

.cat-preview {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.cat-avatar {
  border: 4px solid #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.cat-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cat-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.cat-breed {
  font-size: 16px;
  color: #606266;
}

.form-actions {
  display: flex;
  gap: 16px;
  justify-content: space-between;
  padding-top: 32px;
  margin-top: 24px;
  border-top: 1px solid #e4e7ed;
}

.info-card {
  margin-bottom: 24px;
}

.cat-info-card {
  margin-bottom: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: none;
  width: 100%;
  max-width: 300px;
}

.cat-detail {
  text-align: center;
}

.cat-detail-image {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.cat-name-large {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-row .label {
  color: #606266;
  font-weight: 500;
}

.info-row .value {
  color: #303133;
  text-align: right;
}

.cat-description {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

.cat-description p {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin: 0;
}

.compact-form {
  padding: 0 16px;
}

.compact-form .el-form-item {
  margin-bottom: 20px;
}

.compact-form .el-form-item__label {
  font-weight: 600;
  color: #303133;
}

.compact-form .el-input,
.compact-form .el-textarea,
.compact-form .el-select {
  width: 100%;
}

.compact-form .el-textarea .el-textarea__inner {
  resize: vertical;
  min-height: 80px;
}

.compact-form .el-checkbox {
  margin-top: 8px;
}

.compact-form .el-checkbox__label {
  font-size: 14px;
  line-height: 1.5;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .adoption-form-container {
    padding: 16px;
  }
  
  .page-header {
    padding: 24px 16px;
    margin-bottom: 24px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .compact-form {
    padding: 0 8px;
  }
  
  .compact-form .el-form-item {
    margin-bottom: 16px;
  }
  
  .form-actions {
    flex-direction: column;
    gap: 12px;
  }
  
  .cat-preview {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
}

.notice-content {
  h4 {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 20px 0 12px 0;
  }
}

.notice-list {
  list-style: none;
  padding: 0;
  margin: 0 0 20px 0;

  li {
    padding: 8px 0;
    padding-left: 24px;
    position: relative;
    color: #606266;
    line-height: 1.6;

    &::before {
      content: '✓';
      position: absolute;
      left: 0;
      color: #67c23a;
      font-weight: bold;
    }
  }
}

.steps-vertical {
  margin-bottom: 20px;
}

.cat-card {
  margin-bottom: 24px;
}

.cat-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cat-detail-image {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  object-fit: cover;
}

.cat-detail-info {
  h3 {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 16px 0;
  }
}

.info-row {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;

  .label {
    width: 80px;
    color: #909399;
    font-size: 14px;
  }

  .value {
    flex: 1;
    color: #303133;
    font-size: 14px;
  }
}

.cat-description {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;

  p {
    margin: 0;
    color: #606266;
    line-height: 1.6;
    font-size: 14px;
  }
}

:deep(.el-alert) {
  p {
    margin: 0;
    font-size: 13px;
  }
}
</style>
