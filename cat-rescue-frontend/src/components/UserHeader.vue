<template>
  <header class="app-header user-header">
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
          <el-menu-item index="/">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/cats">
            <el-icon><Star /></el-icon>
            <span>猫咪浏览</span>
          </el-menu-item>
          <el-menu-item index="/community">
            <el-icon><ChatDotRound /></el-icon>
            <span>社区论坛</span>
          </el-menu-item>
          <el-menu-item index="/rescue">
            <el-icon><Help /></el-icon>
            <span>救助需求</span>
          </el-menu-item>
          <el-menu-item index="/adoptions">
            <el-icon><Document /></el-icon>
            <span>我的申请</span>
          </el-menu-item>
          <el-menu-item index="/favorites">
            <el-icon><Star /></el-icon>
            <span>我的收藏</span>
          </el-menu-item>
          <el-menu-item index="/posts">
            <el-icon><ChatDotRound /></el-icon>
            <span>我的帖子</span>
          </el-menu-item>
        </el-menu>
      </nav>

      <!-- 用户操作区域 -->
      <div class="user-section">
        <template v-if="authStore.isAuthenticated">
          <!-- 用户头像和菜单 -->
          <el-dropdown @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="authStore.user?.avatar ? `http://localhost:8080${authStore.user?.avatar}` : '/img/default-avatar.png'" />
              <span class="username">{{ authStore.user?.username || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
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
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </div>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  House,
  Star,
  ChatDotRound,
  Help,
  User,
  Document,
  SwitchButton,
  ArrowDown
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 计算当前激活的菜单项
const activeMenu = computed(() => {
  return route.path
})

// 处理菜单选择
const handleMenuSelect = (index) => {
  console.log('菜单点击:', index, '当前路由:', route.path)
  console.log('用户认证状态:', authStore.isAuthenticated)
  router.push(index)
}

// 处理用户命令
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
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
    
    authStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('退出登录失败')
    }
  }
}
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

.user-header {
  border-bottom: 3px solid #dee1e5;
}

.header-container {
  max-width: 1350px;
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
  width: 200px;
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
  color: #409EFF;
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