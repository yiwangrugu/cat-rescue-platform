<template>
  <div class="system-log">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Document /></el-icon>
          系统日志
        </h1>
        <p class="page-subtitle">查看系统操作日志记录</p>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="模块">
          <el-select v-model="filterForm.module" placeholder="全部模块" clearable style="width: 150px;">
            <el-option
              v-for="module in modules"
              :key="module"
              :label="module"
              :value="module"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="操作">
          <el-select v-model="filterForm.action" placeholder="全部操作" clearable style="width: 150px;">
            <el-option
              v-for="action in actions"
              :key="action"
              :label="action"
              :value="action"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="filterForm.username" placeholder="用户名" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="IP地址">
          <el-input v-model="filterForm.ipAddress" placeholder="IP地址" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadLogs">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
        <el-form-item>
        <el-button type="primary" @click="loadLogs">
          <el-icon><Refresh /></el-icon>
          刷新日志
        </el-button>
        </el-form-item>
        <el-form-item>
        <el-button @click="exportLogs">
          <el-icon><Download /></el-icon>
          导出日志
        </el-button>
        </el-form-item>
        <el-form-item>
        <el-button @click="cleanLogs">
          <el-icon><Delete /></el-icon>
          清理过期日志
        </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志统计 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" style="color: #409EFF;"><Document /></el-icon>
            <div class="stat-info">
              <div class="stat-number">{{ statistics.total || 0 }}</div>
              <div class="stat-label">总日志数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" style="color: #67C23A;"><User /></el-icon>
            <div class="stat-info">
              <div class="stat-number">{{ todayCount || 0 }}</div>
              <div class="stat-label">今日日志</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" style="color: #E6A23C;"><Setting /></el-icon>
            <div class="stat-info">
              <div class="stat-number">{{ activeModules || 0 }}</div>
              <div class="stat-label">活跃模块</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" style="color: #F56C6C;"><TrendCharts /></el-icon>
            <div class="stat-info">
              <div class="stat-number">{{ activeUsers || 0 }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 统计图表 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>模块分布</span>
          </template>
          <div id="moduleChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>操作类型分布</span>
          </template>
          <div id="actionChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日志列表 -->
    <el-card style="margin-top: 20px;">
      <el-table :data="logs" style="width: 100%" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="user.username" label="操作人" width="120">
          <template #default="{ row }">
            {{ row.user ? row.user.username : '系统' }}
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作" width="100">
          <template #default="{ row }">
            <el-tag :type="getActionType(row.action)" size="small">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="createTime" label="操作时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <!-- 日志详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="日志详情" width="600px">
      <el-descriptions :column="1" border v-if="currentLog">
        <el-descriptions-item label="ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.user ? currentLog.user.username : '系统' }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="操作">{{ currentLog.action }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ currentLog.description }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ formatTime(currentLog.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Refresh, Delete, Download, User, Setting, TrendCharts } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin.js'
import * as echarts from 'echarts'

const loading = ref(false)
const chartInstances = ref({})
const logs = ref([])
const modules = ref([])
const actions = ref([])
const statistics = ref({})
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const dateRange = ref([])
const todayCount = ref(0)
const activeModules = ref(0)
const activeUsers = ref(0)
const detailDialogVisible = ref(false)
const currentLog = ref(null)

const filterForm = ref({
  module: '',
  action: '',
  username: '',
  ipAddress: ''
})

const loadModulesAndActions = async () => {
  try {
    const [modulesRes, actionsRes] = await Promise.all([
      adminApi.getLogModules(),
      adminApi.getLogActions()
    ])
    modules.value = modulesRes.data || []
    actions.value = actionsRes.data || []
  } catch (error) {
    console.error('加载模块和操作列表失败:', error)
  }
}

const loadLogs = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (filterForm.value.module) {
      params.module = filterForm.value.module
    }
    if (filterForm.value.action) {
      params.action = filterForm.value.action
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    const response = await adminApi.getSystemLogs(params)
    
    if (response.data) {
      if (response.data.records) {
        logs.value = response.data.records
        total.value = response.data.total || 0
      } else if (Array.isArray(response.data)) {
        logs.value = response.data
        total.value = response.data.length
      }
    }

    await loadStatistics()
    
    ElMessage.success('日志加载成功')
  } catch (error) {
    console.error('加载日志失败:', error)
    if (error.response?.status === 403) {
      ElMessage.error('权限不足，需要管理员权限才能查看系统日志')
    } else {
      ElMessage.error('加载日志失败')
    }
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    console.log('调用 getLogStatistics 参数:', params)
    const response = await adminApi.getLogStatistics(params)
    console.log('getLogStatistics 响应:', response)
    console.log('getLogStatistics response.data:', response.data)
    statistics.value = response.data || {}
    
    if (statistics.value) {
      activeModules.value = statistics.value.moduleStats?.length || 0
      activeUsers.value = statistics.value.activeUsers || 0
    }
    console.log('统计数据:', statistics.value)
    console.log('活跃模块数:', activeModules.value)
    console.log('活跃用户数:', activeUsers.value)
    
    const today = new Date().toISOString().split('T')[0]
    const todayParams = { startDate: today, endDate: today }
    console.log('调用今日统计参数:', todayParams)
    const todayRes = await adminApi.getLogStatistics(todayParams)
    console.log('今日统计响应:', todayRes)
    todayCount.value = todayRes.data?.total || 0
    console.log('今日日志数:', todayCount.value)
    
    await nextTick()
    initCharts()
  } catch (error) {
    console.error('加载日志统计失败:', error)
    if (error.response?.status === 403) {
      ElMessage.error('权限不足，需要管理员权限才能查看系统日志')
    } else {
      ElMessage.error('加载日志统计失败')
    }
  }
}

const initCharts = () => {
  initModuleChart()
  initActionChart()
}

const initModuleChart = () => {
  const chartElement = document.getElementById('moduleChart')
  if (!chartElement) return
  
  if (chartInstances.value.moduleChart) {
    chartInstances.value.moduleChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.moduleChart = chart
  const moduleStats = statistics.value.moduleStats || []
  const chartData = moduleStats.map(item => ({
    value: item.count,
    name: item.module
  }))
  
  const option = {
    title: { text: '模块分布', left: 'left', top: 'top' },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: chartData.length > 0 ? chartData : [{ value: 1, name: '暂无数据' }],
      label: {
        show: true,
        formatter: '{b}: {c} ({d}%)'
      }
    }]
  }
  chart.setOption(option)
}

const initActionChart = () => {
  const chartElement = document.getElementById('actionChart')
  if (!chartElement) return
  
  if (chartInstances.value.actionChart) {
    chartInstances.value.actionChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.actionChart = chart
  const actionStats = statistics.value.actionStats || []
  const chartData = actionStats.map(item => ({
    value: item.count,
    name: item.action
  }))
  
  const option = {
    title: { text: '操作类型分布', left: 'left', top: 'top' },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '70%',
      data: chartData.length > 0 ? chartData : [{ value: 1, name: '暂无数据' }],
      label: {
        show: true,
        formatter: '{b}: {c} ({d}%)'
      }
    }]
  }
  chart.setOption(option)
}

const disposeCharts = () => {
  Object.values(chartInstances.value).forEach(chart => {
    if (chart && chart.dispose) {
      chart.dispose()
    }
  })
  chartInstances.value = {}
}

const resetFilter = () => {
  filterForm.value = {
    module: '',
    action: '',
    username: '',
    ipAddress: ''
  }
  dateRange.value = []
  currentPage.value = 1
  loadLogs()
}

const cleanLogs = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清理30天前的过期日志吗？此操作不可恢复。',
      '确认清理',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await adminApi.cleanOldLogs({ days: 30 })
    ElMessage.success('过期日志清理成功')
    loadLogs()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清理日志失败')
    }
  }
}

const exportLogs = async () => {
  try {
    const params = {}
    if (filterForm.value.module) {
      params.module = filterForm.value.module
    }
    if (filterForm.value.action) {
      params.action = filterForm.value.action
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const response = await adminApi.exportSystemLogs(params)
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `system_logs_${new Date().toISOString().split('T')[0]}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('日志导出成功')
  } catch (error) {
    console.error('导出日志失败:', error)
    ElMessage.error('导出日志失败')
  }
}

const showDetail = (row) => {
  currentLog.value = row
  detailDialogVisible.value = true
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadLogs()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadLogs()
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const getActionType = (action) => {
  const typeMap = {
    '登录': 'success',
    '登出': 'info',
    '创建': 'success',
    '更新': 'warning',
    '删除': 'danger',
    '审核': 'success',
    '导出': 'info',
    '导入': 'info',
    '备份': 'warning',
    '恢复': 'success',
    '查询': 'info',
    '其他': 'info',
    '发布': 'success',
    '完成': 'success',
    '通过': 'success',
    '拒绝': 'danger'
  }
  return typeMap[action] || 'info'
}

onMounted(() => {
  loadModulesAndActions()
  loadLogs()
})

onUnmounted(() => {
  disposeCharts()
})
</script>

<style scoped>
.system-log {
  max-width: 1550px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 24px 32px;
  margin-bottom: 24px;
  height: 110px;
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

.page-title .el-icon {
  font-size: 28px;
}

.page-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-card {
  margin-bottom: 20px;
}

.stat-card {
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 16px;
}

.stat-icon {
  font-size: 48px;
  margin-right: 16px;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

:deep(.el-pagination) {
  display: flex;
}
</style>
