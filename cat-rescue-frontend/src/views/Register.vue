<template>
  <div class="register-container">
    <div class="register-content">
      <!-- 左侧图片 -->
      <div class="register-left">
        <div class="register-banner">
          <h1>猫咪救助平台</h1>
          <p>每一只猫咪都值得被温柔以待</p>
          <div class="banner-image">
            <img src="../public/img/login1.jpg" alt="猫咪救助" />
          </div>
        </div>
      </div>
      <!-- 右侧注册表单 -->
      <div class="register-form">
        <div class="form-header">
          <h2 class="register-title">欢迎加入</h2>
          <p class="register-subtitle">请创建您的账号</p>
        </div>
        <el-form :model="form" :rules="rules" ref="registerForm" label-width="0">
          <el-form-item prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="用户名" 
              size="large"
              class="register-input"
            />
          </el-form-item>
          <el-form-item prop="email">
            <el-input 
              v-model="form.email" 
              placeholder="邮箱" 
              size="large"
              class="register-input"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="密码" 
              size="large"
              show-password
              class="register-input"
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="form.confirmPassword" 
              type="password" 
              placeholder="确认密码" 
              size="large"
              show-password
              class="register-input"
            />
          </el-form-item>
          <el-form-item prop="realName">
            <el-input 
              v-model="form.realName" 
              placeholder="真实姓名" 
              size="large"
              class="register-input"
            />
          </el-form-item>
          <el-form-item prop="phone">
            <el-input 
              v-model="form.phone" 
              placeholder="手机号" 
              size="large"
              class="register-input"
            />
          </el-form-item>
          <el-form-item prop="location">
            <el-input 
              v-model="form.location" 
              placeholder="所在地区" 
              size="large"
              class="register-input"
            />
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleRegister" 
              :loading="loading"
              size="large"
              style="width: 100%"
              class="register-button"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>
        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="login-link">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    const registerForm = ref(null)
    const loading = ref(false)
    
    const form = ref({
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      realName: '',
      phone: '',
      location: ''
    })
    
    const validatePassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请确认密码'))
      } else if (value !== form.value.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
    
    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      email: [
        { required: false, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, validator: validatePassword, trigger: 'blur' }
      ],
      realName: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' }
      ]
    }
    
    const handleRegister = async () => {
      try {
        await registerForm.value.validate()
        loading.value = true
        
        await authApi.register(form.value)
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (error) {
        console.error('注册错误:', error)
        const errorMessage = error.response?.data?.error || error.message || '注册失败'
        ElMessage.error(errorMessage)
      } finally {
        loading.value = false
      }
    }
    
    return {
      form,
      rules,
      registerForm,
      loading,
      handleRegister
    }
  }
}
</script>

<style scoped>

.register-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: url("../public/img/login.jpg") no-repeat center center/cover fixed;
  padding: 0;
  margin: 0;
  overflow: hidden;
}

.register-content {
  display: flex;
  width: 100%;
  max-width: 1000px;
  height: 700px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 左侧图片部分 */
.register-left {
  flex: 1;
  background: linear-gradient(150deg, rgba(255, 200, 150, 0.8) 0%, rgba(220, 120, 120, 0.6) 30%, rgba(255, 160, 120, 0.6) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: white;
  backdrop-filter: blur(5px);
}

.register-banner h1 {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 20px;
  text-align: center;
}

.register-banner p {
  font-size: 16px;
  margin-bottom: 40px;
  text-align: center;
  opacity: 0.9;
}

.banner-image {
  width: 100%;
  border-radius: 12px;
  margin-bottom: 60px;
  overflow: hidden;
}

.banner-image img {
  width: 100%;
  height: 300px;
  object-fit: cover;
}

/* 右侧表单部分 */
.register-form {
  flex: 1;
  padding: 30px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-top: 20px;
}

.register-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.register-subtitle {
  font-size: 14px;
  color: #666;
}

.register-input {
  border-radius: 12px ;
  height: 45px;
  font-size: 16px;
  border: 2px solid #f0f0f0 ;
  transition: all 0.3s ease;
}

.register-input:focus {
  border-color: #667eea ;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2) ;
}

.register-button {
  height: 50px;
  border-radius: 12px ;
  font-size: 16px;
  font-weight: bold;
  background: linear-gradient(45deg, #ea66e3aa 0%, #d69240b4 100%);
  border: none ;
  transition: all 0.3s ease;
  width: 100%;
}

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.register-footer {
  text-align: center;
  margin-bottom: 10px;
  font-size: 14px;
  color: #666;
}

.login-link {
  color: #667eea;
  text-decoration: none;
  font-weight: bold;
  margin-left: 5px;
}

.login-link:hover {
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-content {
    flex-direction: column;
    height: auto;
    max-width: 400px;
    margin: 20px;
  }
  
  .register-left {
    padding: 40px;
  }
  
  .register-form {
    padding: 40px;
  }
  
  .banner-image img {
    height: 200px;
  }
}
</style>