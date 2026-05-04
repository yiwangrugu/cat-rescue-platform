<template>
  <header class="app-header admin-header">
    <div class="header-container">
      <!-- Logo和网站名称 -->
      <div class="logo-section" @click="$router.push('/')">
        <div class="logo">🐱</div>
        <span class="site-name">流浪猫救助平台</span>
      </div>

      <!-- 主导航菜单 -->
      <nav class="main-nav">
        <el-menu 
          :default-active="activeMenu" 
          mode="horizontal" 
          class="nav-menu"
          :ellipsis="false"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/admin">
          <el-icon><House /></el-icon>
          <span>待办事务</span>
          <el-badge v-if="pendingCounts.total > 0" :value="pendingCounts.total" :max="999" class="badge" />
        </el-menu-item>
          <el-menu-item index="/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/cats">
            <el-icon><Star /></el-icon>
            <span>猫咪管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/volunteers">
            <el-icon><span style="font-size: 18px;">♡</span></el-icon>
            <span>志愿者管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/statistics">
            <el-icon><Monitor /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
          <el-menu-item index="/admin/logs">
            <el-icon><Document /></el-icon>
            <span>系统日志</span>
          </el-menu-item>
        </el-menu>
      </nav>

      <!-- 用户操作区域 -->
      <div class="user-section">
        <template v-if="authStore.isAuthenticated">
          <!-- 用户头像和菜单 -->
          <el-dropdown @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="authStore.user?.avatar || '/img/default-avatar.png'" />
              <span class="username">{{ authStore.user?.username || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="favorites">
                  <el-icon><Star /></el-icon>
                  我的收藏
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <!-- 未登录状态 -->
          <div class="auth-buttons">
            <el-button link @click="$router.push('/login')">登录</el-button>
          </div>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { adminApi } from '@/api/admin.js'
import webSocketService from '@/utils/websocket.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  House,
  User,
  UserFilled,
  Service,
  Monitor,
  Setting,
  SwitchButton,
  ArrowDown,
  Star,
  Document
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 待办事务统计
const pendingCounts = ref({
  cats: 0,
  posts: 0,
  rescues: 0,
  adoptions: 0,
  total: 0
})

// 计算当前激活的菜单项
const activeMenu = computed(() => {
  const path = route.path
  return path
})

// 加载待办事务统计
const loadPendingCounts = async () => {
  try {
    console.log('开始加载待办事务统计...')
    const response = await adminApi.getPendingCounts()
    console.log('API响应:', response)
    if (response.data) {
      const data = response.data
      console.log('待办事务数据:', data)
      pendingCounts.value = {
        cats: data.cats || 0,
        posts: data.posts || 0,
        rescues: data.rescues || 0,
        adoptions: data.adoptions || 0,
        total: (data.cats || 0) + (data.posts || 0) + (data.rescues || 0) + (data.adoptions || 0)
      }
      console.log('待办事务统计已更新:', pendingCounts.value)
    }
  } catch (error) {
    console.error('加载待办事务统计失败:', error)
    if (error.response) {
      console.error('错误响应:', error.response)
    }
  }
}

// WebSocket消息处理器
const handleWebSocketMessage = (message) => {
  try {
    console.log('收到WebSocket待办事务消息:', message)
    
    // 检查是否是待办事务更新消息
    if (message === 'PENDING_COUNTS_UPDATED') {
      console.log('WebSocket触发更新待办事务统计')
      loadPendingCounts()
    }
  } catch (error) {
    console.error('处理WebSocket消息失败:', error)
  }
}

// 初始化
const init = async () => {
  try {
    // 先加载一次待办事务统计
    await loadPendingCounts()
    console.log('待办事务统计加载完成')
  } catch (error) {
    console.error('初始化失败:', error)
  }
}

// 处理菜单选择
const handleMenuSelect = (index) => {
  console.log('管理端导航栏菜单点击，路由路径:', index)
  console.log('当前路由对象:', router)
  console.log('当前路由路径:', route.path)
  router.push(index)
}

// 处理用户命令
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'favorites':
      router.push('/favorites')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 处理退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '确认退出', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 断开WebSocket连接
    webSocketService.disconnect()
    
    authStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('退出登录失败')
    }
  }
}

// 组件挂载时初始化
onMounted(() => {
  init()
})
</script>

<style scoped>
.app-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.admin-header {
  border-bottom: 3px solid #dee1e5;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 64px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.logo-section:hover {
  opacity: 0.8;
}

.logo {
  font-size: 28px;
}

.site-name {
  font-size: 20px;
  font-weight: bold;
  color: #E6A23C;
}
.badge {
  top: -30px;
  right:0;
}

.main-nav {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu {
  border-bottom: none;
}

.nav-menu .el-menu-item {
  font-size: 16px;
  padding: 0 20px;
  height: 60px;
  line-height: 60px;
}

.user-section {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #333;
}

.auth-buttons {
  display: flex;
  gap: 12px;
}

.auth-buttons .el-button {
  font-size: 14px;
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
  }
  
  .site-name {
    font-size: 18px;
  }
  
  .nav-menu .el-menu-item {
    padding: 0 12px;
    font-size: 14px;
  }
  
  .username {
    display: none;
  }
}
</style>