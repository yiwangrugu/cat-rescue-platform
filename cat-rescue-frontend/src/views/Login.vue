<template>
  <div class="login-container">
    <div class="login-content">
      <!-- 左侧图片 -->
      <div class="login-left">
        <div class="login-banner">
          <h1>猫咪救助平台</h1>
          <p>每一只猫咪都值得被温柔以待</p>
          <div class="banner-image">
            <img src="../public/img/login1.jpg" alt="猫咪救助" />
          </div>
        </div>
      </div>
      <!-- 右侧登录表单 -->
      <div class="login-form">
        <div class="form-header">
          <h2 class="login-title">欢迎回来</h2>
          <p class="login-subtitle">请登录您的账号</p>
        </div>
        <el-form :model="form" :rules="rules" ref="loginForm">
          <el-form-item prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="请输入手机号"
              prefix-icon="Phone"
              size="large"
              class="login-input"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              show-password
              class="login-input"
            />
          </el-form-item>
          <div class="form-options">
            <el-checkbox v-model="form.remember">
              我已阅读并同意
              <span class="agreement-link">《用户协议》</span>
              与
              <span class="agreement-link">《隐私政策》</span>
            </el-checkbox>
            <router-link to="#" class="forgot-password">忘记密码？</router-link>
          </div>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              style="width: 100%"
              :loading="loading"
              :disabled="!form.remember"
              @click="handleLogin"
              class="login-button"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>
        <div class="login-divider">
          <span>其他登录方式</span>
        </div>
        <div class="social-login">
          <el-button type="default" @click="handleChatClick">
            <el-icon><ChatRound /></el-icon>
          </el-button>
          <el-button type="default" @click="handleMessageClick">
            <el-icon><Message /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Message, Lock, ChatRound } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { userWebSocketService } from '@/utils/userWebSocket'

const router = useRouter()
const authStore = useAuthStore()

const loginForm = ref()
const loading = ref(false)

const form = ref({
  phone: '',
  password: '',
  remember: false
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  remember: [
    { required: true, message: '请阅读并同意用户协议与隐私政策', trigger: 'change' }
  ]
}

const handleLogin = async () => {
  if (!loginForm.value) return
  
  try {
    await loginForm.value.validate()
    loading.value = true
    
    await authStore.login(form.value)
    
    // 登录成功后建立WebSocket连接
    try {
      userWebSocketService.setToken(authStore.token)
      await userWebSocketService.connect()
      console.log('✅ 用户状态WebSocket连接建立成功')
    } catch (error) {
      console.error('❌ 用户状态WebSocket连接失败:', error)
      // 连接失败不影响登录流程，继续执行
    }
    
    // 登录成功后检查用户是否被禁用
    const user = authStore.user
    if (user && user.status === 'INACTIVE' && user.banEndTime) {
      const endTime = new Date(user.banEndTime)
      const now = new Date()
      
      if (now < endTime) {
        // 用户仍在禁用期内
        const formattedTime = endTime.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        })
        
        const message = `您因违反猫猫社区规章制度被限制登录，请于 ${formattedTime} 后重新尝试。\n\n如有疑问，请联系客服电话：10086`
        
        // 显示禁用提示并强制退出登录
        await ElMessageBox.alert(message, '账户已被禁用', {
          confirmButtonText: '确定',
          type: 'error',
          showClose: false,
          closeOnClickModal: false,
          closeOnPressEscape: false
        })
        
        authStore.logout()
        return
      } else {
        // 禁用期已过，自动启用用户
        user.status = 'ACTIVE'
        user.banEndTime = null
        localStorage.setItem(authStore.USER_KEY(), JSON.stringify(user))
      }
    }
    
    ElMessage.success('登录成功')
    
    // 根据应用类型跳转到对应的首页
    const appType = import.meta.env.VITE_APP_TYPE || 'user'
    console.log('应用类型:', appType)
    
    switch (appType) {
      case 'admin':
        // 管理端跳转到管理首页
        router.push('/admin')
        break
      case 'rescuer':
        // 救助端跳转到救助人员工作台
        router.push('/rescuer/cats')
        break
      case 'user':
      default:
        // 用户端跳转到用户首页
        router.push('/')
        break
    }
  } catch (error) {
    // 检查是否是账户被禁用的错误
    if (error.response?.data?.error && error.response.data.error.startsWith('账户已被禁用')) {
      // 显示友好的禁用提示，包含时间信息
      let message = '您的账户因违反猫猫社区规章制度已被禁用。'
      
      // 解析错误信息中的时间信息
      const errorParts = error.response.data.error.split('|')
      if (errorParts.length > 1) {
        const endTime = new Date(errorParts[1])
        if (!isNaN(endTime.getTime())) {
          const formattedTime = endTime.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
          })
          message = `您因违反猫猫社区规章制度被限制登录，请于 ${formattedTime} 后重新尝试。`
        }
      }
      
      message += '\n\n如有疑问，请联系客服电话：10086'
      
      await ElMessageBox.alert(message, '账户已被禁用', {
        confirmButtonText: '确定',
        type: 'error',
        showClose: false,
        closeOnClickModal: false,
        closeOnPressEscape: false
      })
    } else {
      ElMessage.error(error.response?.data?.error || '登录失败')
    }
  } finally {
    loading.value = false
  }
}

//功能开发中
const handleChatClick = () => {
  ElMessage.info('敬请期待')
}

const handleMessageClick = () => {
  ElMessage.info('敬请期待')
}
</script>

<style scoped>

.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  background: url("../public/img/login.jpg") no-repeat center center/cover fixed;
  padding: 0;
  margin: 0;
  overflow: hidden;
}

.login-content {
  display: flex;
  width: 100%;
  max-width: 1000px;
  height: 550px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transform: translateY(-5%);
}

/* 左侧图片部分 */
.login-left {
  flex: 1;
  background: linear-gradient(150deg, rgba(255, 200, 150, 0.8) 0%, rgba(220, 120, 120, 0.6) 30%, rgba(255, 160, 120, 0.6) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: white;
  backdrop-filter: blur(5px);
}

.login-banner h1 {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 20px;
  text-align: center;
}

.login-banner p {
  font-size: 16px;
  margin-bottom: 40px;
  text-align: center;
  opacity: 0.9;
}

.banner-image {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
}

.banner-image img {
  width: 100%;
  height: 300px;
  object-fit: cover;
}

/* 右侧表单部分 */
.login-form {
  flex: 1;
  padding: 30px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-top: 20px;
}

.login-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.login-subtitle {
  font-size: 14px;
  color: #666;
}

.login-input {
  border-radius: 12px !important;
  height: 45px;
  font-size: 16px;
  border: 2px solid #f0f0f0 !important;
  transition: all 0.3s ease;
}

.login-input:focus {
  border-color: #667eea !important;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2) !important;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  font-size: 12px;
}

.form-options .el-checkbox {
  font-size: 12px;
}

.form-options .el-checkbox__label {
  font-size: 12px;
}

.form-options .el-checkbox {
  --el-checkbox-font-size: 12px;
}

.forgot-password {
  color: #667eea;
  text-decoration: none;
  font-size: 12px;
}

.forgot-password:hover {
  text-decoration: underline;
}

.agreement-link {
  color: #667eea;
  cursor: pointer;
  transition: all 0.3s ease;
}

.agreement-link:hover {
  text-decoration: underline;
  color: #764ba2;
}

.login-button {
  height: 50px;
  border-radius: 12px !important;
  font-size: 16px;
  font-weight: bold;
  background: linear-gradient(45deg, #ea66e3aa 0%, #d69240b4 100%);
  border: none !important;
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.login-footer {
  text-align: center;
  margin-top: 10px;
  font-size: 14px;
  color: #666;
}

.register-link {
  color: #667eea;
  text-decoration: none;
  font-weight: bold;
  margin-left: 5px;
}

.register-link:hover {
  text-decoration: underline;
}

.login-divider {
  margin-top: 20px;
  margin-bottom: 10px;
  text-align: center;
  position: relative;
}

.login-divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #f0f0f0;
  transform: translateY(-50%);
}

.login-divider span {
  background: rgba(255,255,255,0.1);
  padding: 0 20px;
  position: relative;
  color: #999;
  font-size: 14px;
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.social-login .el-button {
  width: 50px;
  height: 50px;
  border-radius: 50% !important;
  border: 2px solid #f0f0f0 !important;
  transition: all 0.3s ease;
}

.social-login .el-button:hover {
  border-color: #667eea !important;
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(102, 126, 234, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-content {
    flex-direction: column;
    height: auto;
    max-width: 400px;
  }
  
  .login-left {
    padding: 40px;
  }
  
  .login-form {
    padding: 40px;
  }
  
  .banner-image img {
    height: 200px;
  }
}

</style>


