<template>
  <div class="rescue-statistics">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><TrendCharts /></el-icon>
          救助数据统计
        </h1>
        <p class="page-subtitle">查看和分析救助任务数据统计</p>
      </div>
      <div class="header-stats">
        <div class="stat-card total">
          <div class="stat-number">{{ stats.totalRescues || 0 }}</div>
          <div class="stat-label">总救助任务</div>
        </div>
        <div class="stat-card pending">
          <div class="stat-number">{{ stats.pendingRescues || 0 }}</div>
          <div class="stat-label">待处理救助</div>
        </div>
        <div class="stat-card in-progress">
          <div class="stat-number">{{ stats.inProgressRescues || 0 }}</div>
          <div class="stat-label">进行中救助</div>
        </div>
        <div class="stat-card completed">
          <div class="stat-number">{{ stats.completedRescues || 0 }}</div>
          <div class="stat-label">已完成救助</div>
        </div>
      </div>
    </div>

    <div class="time-range-section">
      <el-card>
        <template #header>
          <span>统计时间范围</span>
        </template>
        <el-form :model="timeRangeForm" inline>
          <el-form-item label="统计类型">
            <el-select v-model="timeRangeForm.type" @change="handleTimeTypeChange">
              <el-option label="今日" value="today" />
              <el-option label="最近7天" value="last7days" />
              <el-option label="本周" value="week" />
              <el-option label="本月" value="month" />
              <el-option label="本年" value="year" />
              <el-option label="自定义" value="custom" />
            </el-select>
          </el-form-item>
          <el-form-item label="开始时间" v-if="timeRangeForm.type === 'custom'">
            <el-date-picker
              v-model="timeRangeForm.startDate"
              type="date"
              placeholder="选择开始日期"
            />
          </el-form-item>
          <el-form-item label="结束时间" v-if="timeRangeForm.type === 'custom'">
            <el-date-picker
              v-model="timeRangeForm.endDate"
              type="date"
              placeholder="选择结束日期"
            />
          </el-form-item>
          <el-form-item>
            <el-button style="margin-right: 20px;" type="primary" @click="loadStatistics">应用</el-button>
            <el-dropdown @command="handleExportCommand">
          <el-button style="margin-right: 20px;" type="primary">
            <el-icon><Download /></el-icon>
            导出报表
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="excel">统计报告(Excel)</el-dropdown-item>
              <el-dropdown-item command="word">统计报告(Word)</el-dropdown-item>
              <el-dropdown-item command="images">统计图片</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
         <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
         </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    

    <div class="key-metrics-section">
      <el-row :gutter="20">
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon user-icon"><Help /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ stats.totalRescues }}</div>
                <div class="metric-label">总救助任务</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon cat-icon"><Clock /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ stats.pendingRescues }}</div>
                <div class="metric-label">待处理救助</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon rescue-icon"><Clock /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ stats.inProgressRescues }}</div>
                <div class="metric-label">进行中救助</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon adoption-icon"><Check /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ stats.completedRescues }}</div>
                <div class="metric-label">已完成救助</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon post-icon"><Document /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ stats.pendingAdoptions }}</div>
                <div class="metric-label">待审核领养</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon active-icon"><User /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ stats.totalCats }}</div>
                <div class="metric-label">救助猫咪总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>救助任务状态分布</span>
            </template>
            <div id="rescueStatusChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>救助任务紧急程度分布</span>
            </template>
            <div id="rescueUrgencyChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>救助任务完成率</span>
            </template>
            <div id="rescueCompletionChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>救助趋势</span>
            </template>
            <div id="rescueTrendChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>救助耗时分析</span>
            </template>
            <div id="rescueDurationChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>志愿者工作量</span>
            </template>
            <div id="rescuerWorkloadChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="data-section">
      <el-card>
        <template #header>
          <span>我的救助记录</span>
          <el-button style="margin-left: 10px;" type="primary" size="small" @click="exportData">
            <el-icon><Download /></el-icon>
            导出数据
          </el-button>
        </template>
        <el-table :data="myRescues" style="width: 100%" v-loading="loading">
          <el-table-column prop="title" label="救助标题" width="300" />
          <el-table-column prop="location" label="位置" width="250" />
          <el-table-column prop="urgencyLevel" label="紧急程度" width="180">
            <template #default="{ row }">
              <el-tag :type="getUrgencyType(row.urgencyLevel)">{{ getUrgencyText(row.urgencyLevel) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="180">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="开始时间" width="200">
            <template #default="{ row }">{{ formatDateTime(row.acceptTime || row.createTime) || '-' }}</template>
          </el-table-column>
          <el-table-column label="完成时间" width="200">
            <template #default="{ row }">{{ formatDateTime(row.completeTime) || '-' }}</template>
          </el-table-column>
          <el-table-column label="耗时" width="200">
            <template #default="{ row }">{{ calculateDuration(row.acceptTime || row.createTime, row.completeTime) }}</template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-section">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { TrendCharts, Refresh, Download, Help, Clock, Check, Document, User, ArrowDown } from '@element-plus/icons-vue'
import { rescueApi } from '@/api/rescue'
import { useAuthStore } from '@/stores/auth'
import * as echarts from 'echarts'
import * as XLSX from 'xlsx'
import { saveAs } from 'file-saver'

const loading = ref(false)
const chartInstances = ref({})

const timeRangeForm = ref({
  type: 'last7days',
  startDate: null,
  endDate: null
})

const stats = ref({
  totalRescues: 0,
  pendingRescues: 0,
  inProgressRescues: 0,
  completedRescues: 0,
  pendingAdoptions: 0,
  totalCats: 0,
  criticalRescues: 0,
  highRescues: 0,
  mediumRescues: 0,
  lowRescues: 0,
  recentDailyStats: {}
})

const myRescues = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const handleTimeTypeChange = () => {
  if (timeRangeForm.value.type !== 'custom') {
    timeRangeForm.value.startDate = null
    timeRangeForm.value.endDate = null
  }
}

const loadStatistics = async () => {
  try {
    loading.value = true
    
    let params = {}
    
    const now = new Date()
    let startDateTime = null
    let endDateTime = null
    
    switch (timeRangeForm.value.type) {
      case 'today':
        startDateTime = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}T00:00:00`
        endDateTime = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}T23:59:59`
        break
      case 'last7days':
        const last7Start = new Date(now)
        last7Start.setDate(now.getDate() - 6)
        startDateTime = `${last7Start.getFullYear()}-${String(last7Start.getMonth() + 1).padStart(2, '0')}-${String(last7Start.getDate()).padStart(2, '0')}T00:00:00`
        endDateTime = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}T23:59:59`
        break
      case 'week':
        const dayOfWeek = now.getDay() || 7
        const weekStart = new Date(now)
        weekStart.setDate(now.getDate() - dayOfWeek + 1)
        const weekEnd = new Date(now)
        startDateTime = `${weekStart.getFullYear()}-${String(weekStart.getMonth() + 1).padStart(2, '0')}-${String(weekStart.getDate()).padStart(2, '0')}T00:00:00`
        endDateTime = `${weekEnd.getFullYear()}-${String(weekEnd.getMonth() + 1).padStart(2, '0')}-${String(weekEnd.getDate()).padStart(2, '0')}T23:59:59`
        break
      case 'month':
        startDateTime = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01T00:00:00`
        const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0)
        endDateTime = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${lastDay.getDate()}T23:59:59`
        break
      case 'year':
        startDateTime = `${now.getFullYear()}-01-01T00:00:00`
        endDateTime = `${now.getFullYear()}-12-31T23:59:59`
        break
      case 'custom':
        if (timeRangeForm.value.startDate && timeRangeForm.value.endDate) {
          const customStart = new Date(timeRangeForm.value.startDate)
          const customEnd = new Date(timeRangeForm.value.endDate)
          startDateTime = `${customStart.getFullYear()}-${String(customStart.getMonth() + 1).padStart(2, '0')}-${String(customStart.getDate()).padStart(2, '0')}T00:00:00`
          endDateTime = `${customEnd.getFullYear()}-${String(customEnd.getMonth() + 1).padStart(2, '0')}-${String(customEnd.getDate()).padStart(2, '0')}T23:59:59`
        }
        break
    }
    
    if (startDateTime && endDateTime) {
      params.startTime = startDateTime
      params.endTime = endDateTime
    }
    

    
    const response = await rescueApi.getRescueStatistics(params)
    
    if (response.data) {
      stats.value = {
        totalRescues: response.data.totalRescues || 0,
        pendingRescues: response.data.pendingRescues || 0,
        inProgressRescues: response.data.inProgressRescues || 0,
        completedRescues: response.data.completedRescues || 0,
        pendingAdoptions: response.data.pendingAdoptions || 0,
        totalCats: response.data.totalCats || 0,
        criticalRescues: response.data.criticalRescues || 0,
        highRescues: response.data.highRescues || 0,
        mediumRescues: response.data.mediumRescues || 0,
        lowRescues: response.data.lowRescues || 0,
        recentDailyStats: response.data.recentDailyStats || {}
      }
      
      await nextTick()
      initCharts(response.data)
    }
    
    ElMessage.success('数据加载成功')
  } catch (error) {
    console.error('获取救助统计信息失败:', error)
    ElMessage.error('获取救助统计信息失败')
  } finally {
    loading.value = false
  }
}

const initCharts = (data) => {
  initRescueStatusChart(data)
  initRescueUrgencyChart(data)
  initRescueCompletionChart(data)
  initRescueTrendChart(data)
  initRescueDurationChart(data)
  initRescuerWorkloadChart(data)
}

const initRescueStatusChart = (data) => {
  const chartElement = document.getElementById('rescueStatusChart')
  if (!chartElement) return
  
  if (chartInstances.value.rescueStatusChart) {
    chartInstances.value.rescueStatusChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.rescueStatusChart = chart
  const option = {
    title: { text: '救助状态分布', left: 'left', top: 'top' },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '70%',
      data: [
        { value: data.pendingRescues || 0, name: '待处理' },
        { value: data.inProgressRescues || 0, name: '进行中' },
        { value: data.completedRescues || 0, name: '已完成' }
      ],
      label: {
        show: true,
        formatter: '{b}: {c} ({d}%)'
      }
    }]
  }
  chart.setOption(option)
}

const initRescueUrgencyChart = (data) => {
  const chartElement = document.getElementById('rescueUrgencyChart')
  if (!chartElement) return
  
  if (chartInstances.value.rescueUrgencyChart) {
    chartInstances.value.rescueUrgencyChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.rescueUrgencyChart = chart
  const option = {
    title: { text: '救助紧急程度', left: 'left', top: 'top' },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '70%',
      data: [
        { value: data.criticalRescues || 0, name: '紧急' },
        { value: data.highRescues || 0, name: '高' },
        { value: data.mediumRescues || 0, name: '中' },
        { value: data.lowRescues || 0, name: '低' }
      ],
      label: {
        show: true,
        formatter: '{b}: {c} ({d}%)'
      }
    }]
  }
  chart.setOption(option)
}

const initRescueCompletionChart = (data) => {
  const chartElement = document.getElementById('rescueCompletionChart')
  if (!chartElement) return
  
  if (chartInstances.value.rescueCompletionChart) {
    chartInstances.value.rescueCompletionChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.rescueCompletionChart = chart
  const completionRate = data.totalRescues > 0 ? Math.round((data.completedRescues / data.totalRescues) * 100) : 0
  
  const option = {
    title: { text: '救助完成率', left: 'left', top: 'top' },
    tooltip: { trigger: 'item' },
    color: ['#67c23a', '#409eff'],
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { value: data.completedRescues || 0, name: '已完成' },
        { value: (data.totalRescues || 0) - (data.completedRescues || 0), name: '未完成' }
      ],
      label: {
        show: true,
        formatter: '{b}: {c} ({d}%)'
      }
    }]
  }
  chart.setOption(option)
}

const initRescueTrendChart = (data) => {
  const chartElement = document.getElementById('rescueTrendChart')
  if (!chartElement) return
  
  if (chartInstances.value.rescueTrendChart) {
    chartInstances.value.rescueTrendChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.rescueTrendChart = chart
  const dailyStats = data.recentDailyStats || {}
  const dates = Object.keys(dailyStats).sort()
  const values = dates.map(date => dailyStats[date])
  
  const option = {
    title: { text: '救助趋势', left: 'left', top: 'top' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: dates.length > 0 ? dates : ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: { type: 'value' },
    series: [{
      data: values.length > 0 ? values : [10, 15, 8, 12, 20, 18, 14],
      type: 'line',
      smooth: true
    }]
  }
  chart.setOption(option)
}

const initRescueDurationChart = (data) => {
  const chartElement = document.getElementById('rescueDurationChart')
  if (!chartElement) return
  
  if (chartInstances.value.rescueDurationChart) {
    chartInstances.value.rescueDurationChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.rescueDurationChart = chart
  const durationStats = data.rescueDurationStats || {}
  const categories = ['0-1小时', '1-4小时', '4-12小时', '12-24小时', '1-3天', '3天以上']
  const values = categories.map(category => durationStats[category] || 0)
  
  const option = {
    title: { text: '救助耗时分析', left: 'left', top: 'top' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: categories
    },
    yAxis: { type: 'value' },
    series: [{
      data: values,
      type: 'bar',
      itemStyle: {
        color: '#409eff'
      },
      label: {
        show: true,
        position: 'top'
      }
    }]
  }
  chart.setOption(option)
}

const initRescuerWorkloadChart = (data) => {
  const chartElement = document.getElementById('rescuerWorkloadChart')
  if (!chartElement) return
  
  if (chartInstances.value.rescuerWorkloadChart) {
    chartInstances.value.rescuerWorkloadChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.rescuerWorkloadChart = chart
  const workloadStats = data.rescuerWorkloadStats || {}
  const chartData = Object.entries(workloadStats).map(([name, value]) => ({ value, name }))
  
  const hasValidData = chartData.some(item => item.value > 0)
  
  const option = {
    title: { text: '志愿者工作量', left: 'left', top: 'top' },
    tooltip: { trigger: 'item' },
    color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399'],
    series: [{
      type: 'pie',
      radius: '70%',
      data: hasValidData ? chartData : [{ value: 1, name: '暂无数据' }],
      label: {
        show: true,
        formatter: hasValidData ? '{b}: {c} ({d}%)' : '{b}'
      }
    }]
  }
  chart.setOption(option)
}

const refreshData = () => {
  loadStatistics()
}

const exportStatistics = async () => {
  try {
    let params = {}
    let startDate = null
    let endDate = null
    
    const now = new Date()
    switch (timeRangeForm.value.type) {
      case 'today':
        startDate = new Date(now.getFullYear(), now.getMonth(), now.getDate())
        endDate = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59)
        break
      case 'last7days':
        startDate = new Date(now)
        startDate.setDate(now.getDate() - 6)
        startDate.setHours(0, 0, 0, 0)
        endDate = new Date(now)
        endDate.setHours(23, 59, 59, 999)
        break
      case 'week':
        const dayOfWeek = now.getDay() || 7
        startDate = new Date(now)
        startDate.setDate(now.getDate() - dayOfWeek + 1)
        startDate.setHours(0, 0, 0, 0)
        endDate = new Date(now)
        endDate.setHours(23, 59, 59, 999)
        break
      case 'month':
        startDate = new Date(now.getFullYear(), now.getMonth(), 1)
        endDate = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59)
        break
      case 'year':
        startDate = new Date(now.getFullYear(), 0, 1)
        endDate = new Date(now.getFullYear(), 11, 31, 23, 59, 59)
        break
      case 'custom':
        if (timeRangeForm.value.startDate && timeRangeForm.value.endDate) {
          startDate = new Date(timeRangeForm.value.startDate)
          startDate.setHours(0, 0, 0, 0)
          endDate = new Date(timeRangeForm.value.endDate)
          endDate.setHours(23, 59, 59, 999)
        }
        break
    }
    
    if (startDate && endDate) {
      const formatDateTime = (date) => {
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        const seconds = String(date.getSeconds()).padStart(2, '0')
        return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
      }
      params.startTime = formatDateTime(startDate)
      params.endTime = formatDateTime(endDate)
    }
    
    const response = await rescueApi.getRescueStatistics(params)
    
    if (response.data) {
      const wb = XLSX.utils.book_new()
      
      const baseStatsData = [
        ['统计项目', '数值'],
        ['总救助任务', response.data.totalRescues || 0],
        ['待处理救助', response.data.pendingRescues || 0],
        ['进行中救助', response.data.inProgressRescues || 0],
        ['已完成救助', response.data.completedRescues || 0],
        ['待领养猫咪', response.data.pendingAdoptions || 0],
        ['救助猫咪总数', response.data.totalCats || 0],
        ['紧急', response.data.criticalRescues || 0],
        ['高', response.data.highRescues || 0],
        ['中', response.data.mediumRescues || 0],
        ['低', response.data.lowRescues || 0]
      ]
      
      const baseStatsSheet = XLSX.utils.aoa_to_sheet(baseStatsData)
      XLSX.utils.book_append_sheet(wb, baseStatsSheet, '基础统计')
      
      const dailyStatsData = [['日期', '救助数量']]
      if (response.data.recentDailyStats) {
        // 按日期递增排序
        Object.entries(response.data.recentDailyStats)
          .sort(([dateA], [dateB]) => new Date(dateA) - new Date(dateB))
          .forEach(([date, count]) => {
            dailyStatsData.push([date, count])
          })
      }
      
      const dailyStatsSheet = XLSX.utils.aoa_to_sheet(dailyStatsData)
      XLSX.utils.book_append_sheet(wb, dailyStatsSheet, '每日统计')
      
      const exportDate = new Date()
      const dateStr = exportDate.toISOString().split('T')[0]
      const filename = `救助统计数据_${dateStr}.xlsx`
      
      XLSX.writeFile(wb, filename)
      ElMessage.success('统计数据导出成功')
    }
  } catch (error) {
    console.error('导出统计数据失败:', error)
    ElMessage.error('导出统计数据失败: ' + (error.message || '未知错误'))
  }
}

const exportData = async () => {
  try {
    const authStore = useAuthStore()
    const currentUser = authStore.user
    const rescuerId = currentUser?.role === 'RESCUER' ? currentUser.id : null
    
    const params = { size: 10000 }
    if (rescuerId) {
      params.rescuerId = rescuerId
    }
    
    const response = await rescueApi.getMyRescues(params)
    
    if (response.data && response.data.records) {
      const wb = XLSX.utils.book_new()
      
      const rescueData = [
        ['救助标题', '位置', '紧急程度', '状态', '开始时间', '完成时间', '耗时', '描述']
      ]
      
      response.data.records.forEach(rescue => {
        rescueData.push([
          rescue.title || '',
          rescue.location || '',
          getUrgencyText(rescue.urgencyLevel) || '',
          getStatusText(rescue.status) || '',
          formatDateTime(rescue.acceptTime || rescue.createTime) || '',
          formatDateTime(rescue.completeTime) || '',
          calculateDuration(rescue.acceptTime || rescue.createTime, rescue.completeTime) || '',
          rescue.description || ''
        ])
      })
      
      const rescueSheet = XLSX.utils.aoa_to_sheet(rescueData)
      XLSX.utils.book_append_sheet(wb, rescueSheet, '救助记录')
      
      const exportDate = new Date()
      const dateStr = exportDate.toISOString().split('T')[0]
      const filename = `救助记录数据_${dateStr}.xlsx`
      
      XLSX.writeFile(wb, filename)
      ElMessage.success('救助记录数据导出成功')
    }
  } catch (error) {
    console.error('导出救助记录数据失败:', error)
    ElMessage.error('导出救助记录数据失败: ' + (error.message || '未知错误'))
  }
}

const loadMyRescues = async () => {
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    const authStore = useAuthStore()
    const currentUser = authStore.user
    if (currentUser?.role === 'RESCUER') {
      params.rescuerId = currentUser.id
    }
    
    const response = await rescueApi.getMyRescues(params)
    
    if (response.data) {
      myRescues.value = response.data.records || []
      total.value = response.data.total || 0
    }
  } catch (error) {
    console.error('加载救助记录失败:', error)
    ElMessage.error('加载救助记录失败')
  }
}

const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
  loadMyRescues()
}

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
  loadMyRescues()
}

const getUrgencyType = (urgency) => {
  const urgencyMap = {
    '紧急': 'danger',
    '高': 'warning', 
    '中': 'info',
    '低': 'success',
    'CRITICAL': 'danger',
    'HIGH': 'warning',
    'MEDIUM': 'info',
    'LOW': 'success'
  }
  return urgencyMap[urgency] || 'info'
}

const getUrgencyText = (urgency) => {
  const textMap = {
    '紧急': '紧急',
    '高': '高',
    '中': '中', 
    '低': '低',
    'CRITICAL': '紧急',
    'HIGH': '高',
    'MEDIUM': '中',
    'LOW': '低'
  }
  return textMap[urgency] || urgency || '未知'
}

const getStatusType = (status) => {
  const statusMap = {
    '已报告': 'info',
    '进行中': 'warning',
    '已完成': 'success',
    '已取消': 'danger',
    'PENDING': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待处理',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    '待处理': '待处理',
    '进行中': '进行中',
    '已完成': '已完成',
    '已取消': '已取消'
  }
  return textMap[status] || status || '未知'
}

const formatDateTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const calculateDuration = (startTime, endTime) => {
  if (!startTime || !endTime) return '-'
  
  const start = new Date(startTime)
  const end = new Date(endTime)
  const durationMs = end.getTime() - start.getTime()
  
  if (durationMs < 0) return '-'
  
  const hours = Math.floor(durationMs / (1000 * 60 * 60))
  const minutes = Math.floor((durationMs % (1000 * 60 * 60)) / (1000 * 60))
  
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

const handleExportCommand = async (command) => {
  if (command === 'excel') {
    await exportRescueStatisticsExcel()
  } else if (command === 'word') {
    await exportRescueStatisticsWord()
  } else if (command === 'images') {
    await exportRescueStatisticsImages()
  }
}

const exportRescueStatisticsWord = async () => {
  try {
    const now = new Date()
    const dateStr = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
    
    const htmlContent = `
<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
<head>
  <meta charset='utf-8'>
  <title>救助数据统计报告</title>
  <style>
    body { font-family: 'Microsoft YaHei', sans-serif; padding: 20px; }
    h1 { color: #303133; text-align: center; }
    .header { margin-bottom: 30px; text-align: center; }
    .stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin: 20px 0; }
    .stat-card { background: #f5f7fa; padding: 15px; border-radius: 8px; text-align: center; }
    .stat-number { font-size: 24px; font-weight: bold; color: #409eff; }
    .stat-label { color: #606266; margin-top: 5px; }
    .section { margin: 30px 0; }
    .section-title { font-size: 18px; font-weight: bold; color: #303133; border-left: 4px solid #409eff; padding-left: 10px; margin-bottom: 15px; }
    table { width: 100%; border-collapse: collapse; margin: 15px 0; }
    th, td { border: 1px solid #e4e7ed; padding: 10px; text-align: center; }
    th { background: #f5f7fa; font-weight: bold; }
  </style>
</head>
<body>
  <div class='header'>
    <h1>救助数据统计报告</h1>
    <p>报告生成时间: ${now.toLocaleString('zh-CN')}</p>
  </div>

  <div class='section'>
    <div class='section-title'>统计摘要</div>
    <div class='stats-grid'>
      <div class='stat-card'><div class='stat-number'>${stats.value.totalRescues || 0}</div><div class='stat-label'>总救助任务</div></div>
      <div class='stat-card'><div class='stat-number'>${stats.value.pendingRescues || 0}</div><div class='stat-label'>待处理救助</div></div>
      <div class='stat-card'><div class='stat-number'>${stats.value.inProgressRescues || 0}</div><div class='stat-label'>进行中救助</div></div>
      <div class='stat-card'><div class='stat-number'>${stats.value.completedRescues || 0}</div><div class='stat-label'>已完成救助</div></div>
      <div class='stat-card'><div class='stat-number'>${stats.value.pendingAdoptions || 0}</div><div class='stat-label'>待审核领养</div></div>
      <div class='stat-card'><div class='stat-number'>${stats.value.totalCats || 0}</div><div class='stat-label'>救助猫咪总数</div></div>
    </div>
  </div>

  <div class='section'>
    <div class='section-title'>紧急程度分布</div>
    <table>
      <tr><th>紧急程度</th><th>数量</th></tr>
      <tr><td>紧急</td><td>${stats.value.criticalRescues || 0}</td></tr>
      <tr><td>高</td><td>${stats.value.highRescues || 0}</td></tr>
      <tr><td>中</td><td>${stats.value.mediumRescues || 0}</td></tr>
      <tr><td>低</td><td>${stats.value.lowRescues || 0}</td></tr>
    </table>
  </div>

  <div class='section'>
    <div class='section-title'>备注</div>
    <p>本报告由猫咪救助平台自动生成，数据仅供参考。</p>
  </div>
</body>
</html>`
    
    const blob = new Blob([htmlContent], { type: 'application/msword' })
    saveAs(blob, `救助数据统计报告_${dateStr}.doc`)
    
    ElMessage.success('救助统计报告(Word)导出成功')
  } catch (error) {
    console.error('导出救助统计报告(Word)失败:', error)
    ElMessage.error('导出救助统计报告(Word)失败')
  }
}

const disposeCharts = () => {
  Object.values(chartInstances.value).forEach(chart => {
    if (chart && chart.dispose) {
      chart.dispose()
    }
  })
  chartInstances.value = {}
}

const exportRescueStatisticsExcel = async () => {
  try {
    const wb = XLSX.utils.book_new()
    
    const summaryData = [
      ['统计项', '数值'],
      ['总救助任务', stats.value.totalRescues || 0],
      ['待处理救助', stats.value.pendingRescues || 0],
      ['进行中救助', stats.value.inProgressRescues || 0],
      ['已完成救助', stats.value.completedRescues || 0],
      ['待审核领养', stats.value.pendingAdoptions || 0],
      ['救助猫咪总数', stats.value.totalCats || 0],
      ['紧急', stats.value.criticalRescues || 0],
      ['高', stats.value.highRescues || 0],
      ['中', stats.value.mediumRescues || 0],
      ['低', stats.value.lowRescues || 0]
    ]
    
    const summarySheet = XLSX.utils.aoa_to_sheet(summaryData)
    XLSX.utils.book_append_sheet(wb, summarySheet, '统计摘要')
    
    const now = new Date()
    const dateStr = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
    XLSX.writeFile(wb, `救助统计报告_${dateStr}.xlsx`)
    
    ElMessage.success('救助统计报告导出成功')
  } catch (error) {
    console.error('导出救助统计报告失败:', error)
    ElMessage.error('导出救助统计报告失败')
  }
}

const exportRescueStatisticsImages = async () => {
  try {
    const chartIds = [
      'rescueStatusChart',
      'rescueUrgencyChart',
      'rescueCompletionChart',
      'rescueTrendChart',
      'rescueDurationChart',
      'rescuerWorkloadChart'
    ]
    
    const chartTitles = [
      '救助任务状态分布',
      '救助任务紧急程度分布',
      '救助任务完成率',
      '救助趋势',
      '救助耗时分析',
      '志愿者工作量'
    ]
    
    for (let i = 0; i < chartIds.length; i++) {
      const chartElement = document.getElementById(chartIds[i])
      if (!chartElement) continue
      
      const chart = echarts.getInstanceByDom(chartElement)
      if (!chart) continue
      
      const base64 = chart.getDataURL({
        type: 'png',
        pixelRatio: 2,
        backgroundColor: '#fff'
      })
      
      const link = document.createElement('a')
      link.download = `${chartTitles[i]}_${new Date().toISOString().split('T')[0]}.png`
      link.href = base64
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      await new Promise(resolve => setTimeout(resolve, 300))
    }
    
    ElMessage.success('救助统计图片导出成功')
  } catch (error) {
    console.error('导出救助统计图片失败:', error)
    ElMessage.error('导出救助统计图片失败')
  }
}

onMounted(() => {
  loadStatistics()
  loadMyRescues()
})

onUnmounted(() => {
  disposeCharts()
})
</script>

<style scoped>
.rescue-statistics {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 32px;
  border-radius: 16px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  color: white;
}

.header-content {
  flex: 1;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 28px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
}

.header-stats {
  display: flex;
  gap: 16px;
}

.stat-card {
  padding: 16px 24px;
  border-radius: 8px;
  text-align: center;
  min-width: 100px;
}

.stat-card.total {
  background: rgba(241, 37, 170, 0.3);
  color: white;
}

.stat-card.pending {
  background: rgba(241, 171, 102, 0.3);
  color: white;
}

.stat-card.in-progress {
  background: rgba(75, 202, 254, 0.3);
  color: white;
}

.stat-card.completed {
  background: rgba(67, 233, 123, 0.3);
  color: white;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  opacity: 0.9;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.time-range-section {
  margin-bottom: 20px;
}

.key-metrics-section {
  margin-bottom: 20px;
}

.metric-card {
  margin-bottom: 20px;
}

.metric-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.metric-icon {
  font-size: 48px;
  padding: 12px;
  border-radius: 12px;
}

.user-icon {
  background: #f0f9ff;
  color: #409eff;
}

.cat-icon {
  background: #fef0f0;
  color: #f56c6c;
}

.rescue-icon {
  background: #fdf6ec;
  color: #e6a23c;
}

.adoption-icon {
  background: #f0f9eb;
  color: #67c23a;
}

.post-icon {
  background: #f4f4f5;
  color: #909399;
}

.active-icon {
  background: #ecf5ff;
  color: #409eff;
}

.metric-info {
  flex: 1;
}

.metric-number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.metric-label {
  font-size: 14px;
  color: #909399;
}

.charts-section {
  margin-bottom: 20px;
}

.data-section {
  margin-bottom: 20px;
}

.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
