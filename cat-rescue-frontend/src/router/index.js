import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  // 公共路由
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/cats',
    name: 'CatList',
    component: () => import('@/views/CatList.vue'),
    meta: { title: '猫咪列表' }
  },
  {
    path: '/cats/:id',
    name: 'CatDetail',
    component: () => import('@/views/CatDetail.vue'),
    meta: { title: '猫咪详情' }
  },
  {
    path: '/posts/:id',
    name: 'PostDetail',
    component: () => import('@/views/PostDetail.vue'),
    meta: { title: '帖子详情' }
  },
  {
    path: '/community',
    name: 'Community',
    component: () => import('@/views/Community.vue'),
    meta: { title: '社区论坛' }
  },

  // 用户端路由
  {
    path: '/rescue',
    name: 'Rescue',
    component: () => import('@/views/RescueUser.vue'),
    meta: { title: '救助需求', requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: () => import('@/views/UserProfile.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/adoptions',
    name: 'UserAdoptions',
    component: () => import('@/views/MyAdoptions.vue'),
    meta: { title: '我的申请', requiresAuth: true }
  },
  {
    path: '/favorites',
    name: 'MyFavorites',
    component: () => import('@/views/MyFavorites.vue'),
    meta: { title: '我的收藏', requiresAuth: true }
  },
  {
    path: '/posts',
    name: 'MyPosts',
    component: () => import('@/views/MyPosts.vue'),
    meta: { title: '我的帖子', requiresAuth: true }
  },
  {
    path: '/adoption/form',
    name: 'AdoptionForm',
    component: () => import('@/views/AdoptionForm.vue'),
    meta: { title: '领养申请表', requiresAuth: true }
  },

  // 救援端路由
  {
    path: '/rescuer/cats',
    name: 'CatManagement',
    component: () => import('@/views/CatManagement.vue'),
    meta: { title: '猫咪管理', requiresAuth: true, requiresRescuer: true }
  },
  {
    path: '/rescues',
    name: 'RescueManagement',
    component: () => import('@/views/RescueManagement.vue'),
    meta: { title: '救助需求管理', requiresAuth: true, requiresRescuer: true }
  },
  {
    path: '/cats/new',
    name: 'CreateCat',
    component: () => import('@/views/CreateCat.vue'),
    meta: { title: '添加猫咪', requiresAuth: true, requiresRescuer: true }
  },
  {
    path: '/cats/:id/edit',
    name: 'EditCat',
    component: () => import('@/views/EditCat.vue'),
    meta: { title: '编辑猫咪', requiresAuth: true, requiresRescuer: true }
  },
  {
    path: '/adoption-review',
    name: 'AdoptionReview',
    component: () => import('@/views/AdoptionReview.vue'),
    meta: { title: '领养申请审核', requiresAuth: true, requiresRescuer: true }
  },
  {
    path: '/statistics',
    name: 'RescueStatistics',
    component: () => import('@/views/RescueStatistics.vue'),
    meta: { title: '救助统计', requiresAuth: true, requiresRescuer: true }
  },


  // 管理端路由
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/AdminDashboard.vue'),
    meta: { title: '待办事务', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/users',
    name: 'UserManagement',
    component: () => import('@/views/UserManagement.vue'),
    meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/cats',
    name: 'AdminCatManagement',
    component: () => import('@/views/CatManagement.vue'),
    meta: { title: '猫咪管理', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/adoptions',
    name: 'AdminAdoptionManagement',
    component: () => import('@/views/AdoptionReview.vue'),
    meta: { title: '领养管理', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/rescues',
    name: 'AdminRescueManagement',
    component: () => import('@/views/RescueManagement.vue'),
    meta: { title: '救助管理', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/statistics',
    name: 'AdminStatistics',
    component: () => import('@/views/AdminStatistics.vue'),
    meta: { title: '数据统计', requiresAuth: true, requiresAdmin: true }
  },

  {
    path: '/admin/logs',
    name: 'SystemLogs',
    component: () => import('@/views/SystemLog.vue'),
    meta: { title: '系统日志', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/cat-audit',
    name: 'CatAudit',
    component: () => import('@/views/CatAudit.vue'),
    meta: { title: '猫咪信息审核', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/content',
    name: 'ContentAudit',
    component: () => import('@/views/AdminDashboard.vue'),
    meta: { title: '内容审核', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/volunteers',
    name: 'VolunteerManagement',
    component: () => import('@/views/VolunteerManagement.vue'),
    meta: { title: '志愿者管理', requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 流浪猫救助平台`
  }

  // 检查是否需要认证
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
    return
  }

  // 检查管理员权限
  if (to.meta.requiresAdmin && (!authStore.isAuthenticated || !authStore.isAdmin)) {
    next('/')
    return
  }

  // 检查救助人员权限（管理员也可以访问救援端功能）
  if (to.meta.requiresRescuer && (!authStore.isAuthenticated || (!authStore.isRescuer && !authStore.isAdmin))) {
    next('/')
    return
  }

  next()
})

export default router