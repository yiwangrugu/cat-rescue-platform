<template>
  <div id="app">
    <!-- 根据应用类型和路由显示不同的导航栏 -->
    <UserHeader v-if="appType === 'user' && !isAuthPage" />
    <RescuerHeader v-else-if="appType === 'rescuer' && !isAuthPage" />
    <AdminHeader v-else-if="appType === 'admin' && !isAuthPage" />
    
    <!-- 主内容区域 -->
    <main :class="['main-content', { 'full-height': isAuthPage }]">
      <router-view />
    </main>
    
    <!-- 页脚 -->
    <footer v-if="!isAuthPage" class="app-footer">
      <div class="footer-container">
        <p>&copy; 2026 流浪猫救助平台. 为流浪猫寻找温暖的家.</p>
      </div>
    </footer>
    
    <!-- 回到顶部信标 -->
    <div 
      v-if="showBackToTop && !isAuthPage" 
      class="back-to-top"
      @click="scrollToTop"
      title="回到顶部"
    >
      <el-icon><ArrowUp /></el-icon>
      <span class="back-to-top-text">回到顶部</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRoute } from 'vue-router'
import UserHeader from '@/components/UserHeader.vue'
import RescuerHeader from '@/components/RescuerHeader.vue'
import AdminHeader from '@/components/AdminHeader.vue'
import { ArrowUp } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const authStore = useAuthStore()
const route = useRoute()
const appType = ref(import.meta.env.VITE_APP_TYPE || 'user')
const showBackToTop = ref(false)

// 判断是否为认证页面（登录/注册）
const isAuthPage = computed(() => {
  const authRoutes = ['/login', '/register']
  return authRoutes.includes(route.path)
})

// 监听滚动事件
const handleScroll = () => {
  showBackToTop.value = window.scrollY > 300
}

// 回到顶部
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

// 应用启动时尝试恢复登录状态
onMounted(() => {
  console.log('当前应用类型:', appType.value)
  authStore.tryAutoLogin()
  window.addEventListener('scroll', handleScroll)
})

// 组件卸载时移除事件监听器
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  min-height: calc(100vh - 64px - 60px); /* 减去头部和底部高度 */
  padding-top: 64px; /* 为固定导航栏留出空间 */
}

.main-content.full-height {
  min-height: 100vh;
  padding-top: 0; /* 认证页面不需要顶部padding */
}

/* 确保认证页面容器占满整个屏幕 */
:deep(.login-container),
:deep(.register-container) {
  min-height: 100vh;
}

.app-footer {
  background: #f5f7fa;
  border-top: 1px solid #e4e7ed;
  padding: 20px 0;
  text-align: center;
  margin-top: auto;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.app-footer p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

/* 回到顶部信标样式 */
.back-to-top {
  position: fixed;
  right: 5px;
  bottom: 35px;
  min-width: 120px;
  height: 50px;
  border-radius: 25px;
  background: linear-gradient(45deg, #ea66e3aa 0%, #d69240b4 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4);
  transition: all 0.3s ease;
  z-index: 999;
  font-weight: 600;
  font-size: 16px;
  padding: 0 20px;
}

.back-to-top:hover {
  transform: translateY(-5px) scale(1.05);
  box-shadow: 0 6px 20px rgba(255, 107, 107, 0.6);
  animation: none;
}

.back-to-top .el-icon {
  font-size: 20px;
  transition: transform 0.3s ease;
}

.back-to-top:hover .el-icon {
  transform: translateY(-2px);
}

.back-to-top-text {
  font-size: 16px;
  font-weight: 600;
}

/* 脉冲动画 */
@keyframes pulse {
  0% {
    box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
  }
  50% {
    box-shadow: 0 6px 24px rgba(64, 158, 255, 0.6);
  }
  100% {
    box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
  }
}

@media (max-width: 768px) {
  .back-to-top {
    right: 20px;
    bottom: 20px;
    min-width: 100px;
    height: 45px;
    font-size: 14px;
    padding: 0 15px;
  }
  
  .back-to-top .el-icon {
    font-size: 18px;
  }
}
</style>

<style>
/* 全局样式：设置窗口最小宽度限制 */
html, body {
  min-width: 1050px;
  overflow-x: auto;
  margin: 0;
}

#app {
  min-width: 1050px;
}

.el-overlay-dialog{
  overflow: hidden;
  top:-20px;
  right: 0;
}

</style>