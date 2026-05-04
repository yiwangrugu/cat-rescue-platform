<template>
  <div class="user-management">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><User /></el-icon>
          用户角色管理
        </h1>
        <p class="page-subtitle">管理系统用户角色和权限，确保平台安全运行</p>
      </div>
      <div class="header-stats">
        <div class="stat-card user">
          <div class="stat-number">{{ userStats.totalUsers || 0 }}</div>
          <div class="stat-label">总用户数</div>
        </div>
        <div class="stat-card rescuer">
          <div class="stat-number">{{ userStats.rescuerCount || 0 }}</div>
          <div class="stat-label">救助人员</div>
        </div>
        <div class="stat-card admin">
          <div class="stat-number">{{ userStats.adminCount || 0 }}</div>
          <div class="stat-label">管理员</div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-form :model="filterForm" inline>
        <el-form-item label="用户名">
          <el-input v-model="filterForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="filterForm.email" placeholder="请输入邮箱" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="filterForm.role" placeholder="请选择角色" clearable>
            <el-option label="普通用户" value="USER" />
            <el-option label="救助人员" value="RESCUER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable>
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="禁用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadUsers">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetFilters">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="showAddUserDialog = true" :icon="Plus">添加用户</el-button>
          <el-button @click="exportUsers" :icon="Download">导出数据</el-button>
       </el-form-item>
      </el-form>
    </div>

    <!-- 统计信息 -->
    <div class="statistics-section">
      <el-row :gutter="20">
        <el-col :span="5">
          <el-card class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon user-icon"><User /></el-icon>
              <div class="stat-info">
                <div class="stat-number">{{ userStats.totalUsers || 0 }}</div>
                <div class="stat-label">总用户数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="5">
          <el-card class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon rescuer-icon"><UserFilled /></el-icon>
              <div class="stat-info">
                <div class="stat-number">{{ userStats.totalRescuers || 0 }}</div>
                <div class="stat-label">救助人员</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="5">
          <el-card class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon admin-icon"><Avatar /></el-icon>
              <div class="stat-info">
                <div class="stat-number">{{ userStats.totalAdmins || 0 }}</div>
                <div class="stat-label">管理员</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon active-icon"><CircleCheck /></el-icon>
              <div class="stat-info">
                <div class="stat-number">{{ userStats.activeUsers || 0 }}</div>
                <div class="stat-label">活跃用户</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon inactive-icon"><CircleClose /></el-icon>
              <div class="stat-info">
                <div class="stat-number">{{ userStats.inactiveCount || 0 }}</div>
                <div class="stat-label">禁用用户</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 用户列表 -->
    <div class="user-table-section">
      <el-card>
        <template #header>
          <span>用户列表</span>
          <div class="table-actions">
            <el-button size="small" @click="batchEnable" :disabled="selectedUsers.length === 0">批量启用</el-button>
            <el-button size="small" type="danger" @click="batchDisable" :disabled="selectedUsers.length === 0">批量禁用</el-button>
          </div>
        </template>
        
        <el-table 
          :data="filteredUsers" 
          v-loading="loading"
          @selection-change="handleSelectionChange"
          style="width: 100%">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="email" label="邮箱" width="180" />
          <el-table-column prop="realName" label="真实姓名" width="100" />
          <el-table-column prop="phone" label="手机号" width="120" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="getRoleType(row.role)">{{ getRoleText(row.role) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
                {{ row.status === 'ACTIVE' ? '活跃' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="150">
            <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
          </el-table-column>
          <el-table-column prop="lastLoginTime" label="最后登录" width="150">
            <template #default="{ row }">{{ formatDate(row.lastLoginTime) || '-' }}</template>
          </el-table-column>
          <el-table-column prop="banEndTime" label="解禁时间" width="150">
            <template #default="{ row }">
              <span v-if="row.status === 'INACTIVE' && row.banEndTime" class="ban-time">
                {{ formatDate(row.banEndTime) }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button size="small" @click="editUser(row)" class="edit-btn">编辑</el-button>
                <el-dropdown v-if="row.status === 'ACTIVE'" @command="(command) => handleUserAction(row, command)">
                  <el-button 
                    size="small" 
                    type="danger" 
                    :disabled="row.role === 'ADMIN'"
                    class="disable-btn"
                  >
                    禁用<el-icon><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="1minute">禁用1分钟</el-dropdown-item>
                      <el-dropdown-item divided command="1">禁用1天</el-dropdown-item>
                      <el-dropdown-item command="7">禁用7天</el-dropdown-item>
                      <el-dropdown-item command="30">禁用30天</el-dropdown-item>
                      <el-dropdown-item command="90">禁用90天</el-dropdown-item>
                      <el-dropdown-item command="180">禁用180天</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <el-button 
                  v-else
                  size="small" 
                  type="success" 
                  @click="toggleUserStatus(row)"
                  class="enable-btn">
                  启用
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-section">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog 
      :title="editUserForm.id ? '编辑用户' : '添加用户'" 
      v-model="showAddUserDialog" 
      width="600px"
      @close="handleDialogClose">
      <el-form :model="editUserForm" :rules="userRules" ref="userFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editUserForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editUserForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editUserForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editUserForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editUserForm.role" placeholder="请选择角色">
            <el-option label="普通用户" value="USER" />
            <el-option label="救助人员" value="RESCUER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="editUserForm.status" placeholder="请选择状态">
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="禁用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="editUserForm.remark" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showAddUserDialog = false">取消</el-button>
        <el-button type="primary" @click="submitUser" :loading="submitting">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin.js'
import { userWebSocketService } from '@/utils/userWebSocket'
import { useAuthStore } from '@/stores/auth'
import { useRoute } from 'vue-router'
import {
  Plus, 
  Download, 
  User, 
  UserFilled, 
  Avatar, 
  CircleCheck,
  CircleClose,
  ArrowDown,
  Refresh,
  Search
} from '@element-plus/icons-vue'

const users = ref([])
const loading = ref(false)
const selectedUsers = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showAddUserDialog = ref(false)
const submitting = ref(false)
const userFormRef = ref(null)
const route = useRoute()

const filterForm = ref({
  username: '',
  email: '',
  role: '',
  status: ''
})

const userStats = ref({
  totalUsers: 0,
  totalRescuers: 0,
  totalAdmins: 0,
  activeUsers: 0,
  inactiveCount: 0
})

const editUserForm = ref({
  id: null,
  username: '',
  email: '',
  realName: '',
  phone: '',
  role: 'USER',
  status: 'ACTIVE',
  remark: ''
})

const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

const filteredUsers = computed(() => {
  return users.value.filter(user => {
    const matchesUsername = !filterForm.value.username || 
      user.username.toLowerCase().includes(filterForm.value.username.toLowerCase())
    const matchesEmail = !filterForm.value.email || 
      user.email.toLowerCase().includes(filterForm.value.email.toLowerCase())
    const matchesRole = !filterForm.value.role || user.role === filterForm.value.role
    const matchesStatus = !filterForm.value.status || user.status === filterForm.value.status
    
    return matchesUsername && matchesEmail && matchesRole && matchesStatus
  })
})

const getRoleType = (role) => {
  const types = {
    'ADMIN': 'danger',
    'RESCUER': 'warning',
    'USER': 'success'
  }
  return types[role] || 'info'
}

const getRoleText = (role) => {
  const texts = {
    'ADMIN': '管理员',
    'RESCUER': '救助人员',
    'USER': '普通用户'
  }
  return texts[role] || '未知'
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

const loadUsers = async () => {
  try {
    loading.value = true
    
    // 调用API加载用户列表
    const response = await adminApi.getUsers({
      username: filterForm.value.username,
      email: filterForm.value.email,
      role: filterForm.value.role,
      status: filterForm.value.status,
      page: currentPage.value,
      size: pageSize.value
    })
    
    if (response && response.data) {
      users.value = response.data.users || response.data.list || response.data
      total.value = response.data.total || users.value.length
    } else {
      users.value = []
      total.value = 0
    }
    
    // 计算统计信息
    calculateUserStats()
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
    users.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const calculateUserStats = () => {
  const totalUsers = users.value.length
  const totalRescuers = users.value.filter(u => u.role === 'RESCUER').length
  const totalAdmins = users.value.filter(u => u.role === 'ADMIN').length
  const activeUsers = users.value.filter(u => u.status === 'ACTIVE').length
  const inactiveCount = users.value.filter(u => u.status === 'INACTIVE').length
  
  userStats.value = {
    totalUsers: totalUsers,
    totalRescuers: totalRescuers,
    totalAdmins: totalAdmins,
    rescuerCount: totalRescuers, // 添加缺失的字段
    adminCount: totalAdmins,     // 添加缺失的字段
    activeUsers: activeUsers,
    inactiveCount: inactiveCount
  }
}

const resetFilters = () => {
  filterForm.value = {
    username: '',
    email: '',
    role: '',
    status: ''
  }
  loadUsers()
}

const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

const editUser = (user) => {
  editUserForm.value = { ...user }
  showAddUserDialog.value = true
}

const toggleUserStatus = async (user) => {
  try {
    // 检查是否为管理员账号（如果是禁用操作）
    if (user.status === 'ACTIVE' && user.role === 'ADMIN') {
      ElMessage.warning('管理员账号不可被禁用')
      return
    }
    
    const action = user.status === 'ACTIVE' ? '禁用' : '启用'
    
    await ElMessageBox.confirm(
      `确定要${action}用户 ${user.username} 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API切换用户状态
    await adminApi.toggleUserStatus(user.id)
    
    // 更新本地状态
    const newStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    user.status = newStatus
    
    // 如果是启用用户，清除解禁时间
    if (newStatus === 'ACTIVE') {
      user.banEndTime = null
    }
    
    // 发送WebSocket通知
    sendUserStatusNotification(user.id, newStatus)
    
    ElMessage.success(`${action}成功`)
    calculateUserStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const disableUser = async (user, duration) => {
  try {
    // 检查是否为管理员账号
    if (user.role === 'ADMIN') {
      ElMessage.warning('管理员账号不可被禁用')
      return
    }
    
    const durationText = getDurationText(duration)
    await ElMessageBox.confirm(
      `确定要禁用用户 ${user.username} ${durationText}吗？`,
      '确认禁用',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 计算禁用结束时间
    const endTime = new Date()
    if (duration === '1minute') {
      endTime.setMinutes(endTime.getMinutes() + 1) // 1分钟
    } else {
      endTime.setDate(endTime.getDate() + parseInt(duration)) // 按天数
    }
    
    // 调用API切换用户状态，传递禁用时长
    await adminApi.toggleUserStatus(user.id, duration)
    
    // 更新本地状态
    user.status = 'INACTIVE'
    user.banEndTime = endTime.toISOString()
    
    // 更新localStorage中的用户数据（如果被禁用的用户当前已登录）
    const authStore = useAuthStore()
    if (authStore.user && String(authStore.user.id) === String(user.id)) {
      authStore.user.status = 'INACTIVE'
      authStore.user.banEndTime = endTime.toISOString()
      localStorage.setItem(authStore.USER_KEY(), JSON.stringify(authStore.user))
    }
    
    ElMessage.success(`用户 ${user.username} 已被禁用 ${durationText}`)
    calculateUserStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('禁用失败')
    }
  }
}

const getDurationText = (duration) => {
  const durations = {
    '1minute': '1分钟',
    '1': '1天',
    '7': '7天',
    '30': '30天',
    '90': '90天',
    '180': '180天'
  }
  return durations[duration] || `${duration}天`
}

// 处理用户操作
const handleUserAction = async (user, command) => {
  if (command === '1minute') {
    // 禁用1分钟（测试用）
    await disableUser(user, '1minute') // 特殊标记，表示1分钟
  } else {
    // 定时禁用
    await disableUser(user, command)
  }
}

// 处理对话框关闭
const handleDialogClose = () => {
  // 重置表单
  editUserForm.value = {
    id: null,
    username: '',
    email: '',
    realName: '',
    phone: '',
    role: 'USER',
    status: 'ACTIVE',
    remark: ''
  }
  
  // 清除表单验证
  if (userFormRef.value) {
    userFormRef.value.clearValidate()
  }
}

const batchEnable = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要启用选中的 ${selectedUsers.value.length} 个用户吗？`,
      '批量启用',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API批量启用用户
    const promises = selectedUsers.value.map(user => {
      if (user.status === 'INACTIVE') {
        return adminApi.toggleUserStatus(user.id)
      }
      return Promise.resolve()
    })
    
    await Promise.all(promises)
    
    // 更新本地状态
    selectedUsers.value.forEach(user => {
      if (user.status === 'INACTIVE') {
        user.status = 'ACTIVE'
      }
    })
    
    ElMessage.success('批量启用成功')
    selectedUsers.value = []
    calculateUserStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量启用失败')
    }
  }
}

const batchDisable = async () => {
  try {
    // 检查选中的用户中是否有管理员
    const adminUsers = selectedUsers.value.filter(user => user.role === 'ADMIN')
    if (adminUsers.length > 0) {
      ElMessage.warning(`选中的用户中包含 ${adminUsers.length} 个管理员账号，管理员账号不可被禁用`)
      return
    }
    
    await ElMessageBox.confirm(
      `确定要禁用选中的 ${selectedUsers.value.length} 个用户吗？`,
      '批量禁用',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API批量禁用用户
    const promises = selectedUsers.value.map(user => {
      if (user.status === 'ACTIVE') {
        return adminApi.toggleUserStatus(user.id)
      }
      return Promise.resolve()
    })
    
    await Promise.all(promises)
    
    // 更新本地状态
    selectedUsers.value.forEach(user => {
      if (user.status === 'ACTIVE') {
        user.status = 'INACTIVE'
      }
    })
    
    ElMessage.success('批量禁用成功')
    selectedUsers.value = []
    calculateUserStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量禁用失败')
    }
  }
}

const submitUser = async () => {
  try {
    // 表单验证
    if (!userFormRef.value) {
      ElMessage.error('表单引用不存在')
      return
    }
    
    // 验证表单
    const valid = await userFormRef.value.validate()
    if (!valid) {
      return
    }
    
    if (editUserForm.value.id) {
      // 编辑用户 - 调用API
      await adminApi.updateUser(editUserForm.value.id, {
        username: editUserForm.value.username,
        email: editUserForm.value.email,
        realName: editUserForm.value.realName,
        phone: editUserForm.value.phone,
        role: editUserForm.value.role,
        status: editUserForm.value.status,
        remark: editUserForm.value.remark
      })
      
      // 更新本地数据
      const index = users.value.findIndex(u => u.id === editUserForm.value.id)
      if (index !== -1) {
        users.value[index] = { ...editUserForm.value }
      }
      ElMessage.success('编辑用户成功')
    } else {
      // 添加用户 - 调用API
      const response = await adminApi.addUser({
        username: editUserForm.value.username,
        email: editUserForm.value.email,
        realName: editUserForm.value.realName,
        phone: editUserForm.value.phone,
        role: editUserForm.value.role,
        status: editUserForm.value.status,
        remark: editUserForm.value.remark,
        password: '123456' // 默认密码
      })
      
      // 添加新用户到本地列表
      if (response && response.data) {
        const newUser = {
          ...response.data,
          createTime: new Date().toISOString(),
          lastLoginTime: null
        }
        users.value.push(newUser)
      }
      ElMessage.success('添加用户成功')
    }
    
    showAddUserDialog.value = false
    calculateUserStats()
    total.value = users.value.length
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const exportUsers = async () => {
  try {
    // 调用API导出用户数据
    const response = await adminApi.exportUsers()
    
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `用户数据_${new Date().toISOString().split('T')[0]}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('用户数据导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadUsers()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadUsers()
}

// WebSocket消息处理器
const handleWebSocketMessage = (type, data) => {
  
  switch (type) {
    case 'USER_STATUS_CHANGED':
      // 更新用户状态
      const userIndex = users.value.findIndex(u => u.id === data.userId)
      if (userIndex !== -1) {
        users.value[userIndex].status = data.status
        calculateUserStats()
      }
      break
    case 'USER_BANNED':
      // 用户被禁用
      // 处理纯文本格式: USER_BANNED|userId|endTime
      if (typeof data === 'string' && data.startsWith('USER_BANNED|')) {
        const parts = data.split('|')
        if (parts.length >= 2) {
          const userId = parts[1]
          // 从本地用户列表查找用户名
          const user = users.value.find(u => String(u.id) === String(userId))
          if (user) {
            ElMessage.info(`用户 ${user.username} 已被禁用`)
          } else {
            ElMessage.info(`用户(ID:${userId}) 已被禁用`)
          }
        } else {
          ElMessage.info('用户已被禁用')
        }
      } else if (data.username) {
        // 处理JSON格式
        ElMessage.info(`用户 ${data.username} 已被禁用`)
      } else {
        ElMessage.info('用户已被禁用')
      }
      break
    case 'USER_UNBANNED':
      // 用户解禁，更新用户状态
      const unbannedUserIndex = users.value.findIndex(u => u.id === data.userId)
      if (unbannedUserIndex !== -1) {
        users.value[unbannedUserIndex].status = 'ACTIVE'
        users.value[unbannedUserIndex].banEndTime = null
        calculateUserStats()
        ElMessage.success(`用户已自动解禁，状态已更新`)
      }
      break
  }
}

// 初始化WebSocket连接
const initWebSocket = async () => {
  try {
    userWebSocketService.setToken(authStore.token)
    await userWebSocketService.connect()
    userWebSocketService.addMessageHandler('userManagement', handleWebSocketMessage)
  
  } catch (error) {

  }
}

// 发送用户状态变化通知
const sendUserStatusNotification = (userId, status, duration = null, endTime = null) => {
  const message = {
    type: 'USER_STATUS_CHANGED',
    data: {
      userId,
      status,
      duration,
      endTime,
      timestamp: new Date().toISOString()
    }
  }
  
  userWebSocketService.send(message)
}

// 定时检查用户状态（用于自动解禁检测）
let statusCheckInterval = null

const startStatusCheck = () => {
  // 每30秒检查一次用户状态
  statusCheckInterval = setInterval(() => {
    checkUserStatusUpdates()
  }, 30000)
}

const stopStatusCheck = () => {
  if (statusCheckInterval) {
    clearInterval(statusCheckInterval)
    statusCheckInterval = null
  }
}

// 检查用户状态更新（自动解禁检测）
const checkUserStatusUpdates = async () => {
  try {
    // 重新加载用户列表以获取最新状态
    await loadUsers()
    
    // 检查是否有用户已自动解禁
    const now = new Date()
    let hasUpdates = false
    
    users.value.forEach(user => {
      if (user.status === 'INACTIVE' && user.banEndTime) {
        const endTime = new Date(user.banEndTime)
        if (now >= endTime) {
          // 用户已自动解禁
          user.status = 'ACTIVE'
          user.banEndTime = null
          hasUpdates = true
  
        }
      }
    })
    
    if (hasUpdates) {
      calculateUserStats()
      ElMessage.success('检测到用户状态更新，列表已刷新')
    }
  } catch (error) {

  }
}

onMounted(async () => {
  if (route.query.role) {
    filterForm.value.role = route.query.role
  }
  await loadUsers()
  await initWebSocket()
  startStatusCheck()
})

onUnmounted(() => {
  // 清理WebSocket处理器
  userWebSocketService.removeMessageHandler('userManagement')
  // 停止状态检查
  stopStatusCheck()
})

onUnmounted(() => {
  // 清理WebSocket处理器
  userWebSocketService.removeMessageHandler('userManagement')
})
</script>

<style scoped>
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-buttons .el-button {
  min-width: 60px;
  height: 32px;
}

.ban-time {
  color: #f56c6c;
  font-weight: bold;
  font-size: 12px;
}

.edit-btn {
  background-color: #409eff;
  border-color: #409eff;
  color: white;
}

.edit-btn:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.disable-btn {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

.disable-btn:hover:not(.is-disabled) {
  background-color: #f78989;
  border-color: #f78989;
}

.enable-btn {
  background-color: #67c23a;
  border-color: #67c23a;
  margin-left: 0px;

}

.enable-btn:hover {
  background-color: #85ce61;
  border-color: #85ce61;
}


.user-management {
  max-width: 1550px;
  padding: 24px;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  height: 94px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
}

.header-content {
  color: white;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.header-stats {
  display: flex;
  gap: 16px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 16px 24px;
  text-align: center;
  color: white;
  min-width: 80px;
}

.stat-card.user {
  background: rgba(255, 193, 7, 0.3);
}

.stat-card.rescuer {
  background: rgba(40, 167, 69, 0.3);
}

.stat-card.admin {
  background: rgba(40, 167, 69, 0.3);
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  opacity: 0.9;
}

.filter-section {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.filter-section .el-form-item {
  margin-bottom: 0;
}

.statistics-section {
  margin-bottom: 20px;
  animation: fadeInUp 0.6s ease;
}

.stat-card {
  text-align: center;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  justify-content: start;
  padding: 20px;
}

.stat-icon {
  font-size: 36px;
  margin-right: 15px;
}

.user-icon {
  color: #409EFF;
}

.rescuer-icon {
  color: #E6A23C;
}

.admin-icon {
  color: #F56C6C;
}

.active-icon {
  color: #67C23A;
}

.inactive-icon {
  color: #F56C6C;
}

.stat-info {
  text-align: left;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  margin-top: 5px;
}

.stat-content .stat-number {
  color:#1a1919;
}

.stat-content .stat-label {
  color:#1d1c1c;
}

.user-table-section {
  margin-top: 20px;
  animation: fadeInUp 0.6s ease ;
}

.table-actions {
  display: flex;
  gap: 10px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  animation: fadeInUp 0.6s ease ;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stat-content {
    flex-direction: column;
    text-align: center;
  }
  
  .stat-icon {
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .stat-info {
    text-align: center;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .filter-section .el-form {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }
  
  .filter-section .el-form-item {
    width: 100%;
  }
  
}
</style>