<template>
  <div class="admin-statistics">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><TrendCharts /></el-icon>
          数据统计与分析
        </h1>
        <p class="page-subtitle">查看平台整体数据统计和分析报告</p>
      </div>
      <div class="header-stats">
        <div class="stat-card total">
          <div class="stat-number">{{ metrics.totalUsers || 0 }}</div>
          <div class="stat-label">用户总数</div>
        </div>
        <div class="stat-card active">
          <div class="stat-number">{{ metrics.totalCats || 0 }}</div>
          <div class="stat-label">猫咪总数</div>
        </div>
        <div class="stat-card rescues">
          <div class="stat-number">{{ metrics.totalRescues || 0 }}</div>
          <div class="stat-label">救助总数</div>
        </div>
        <div class="stat-card adoptions">
          <div class="stat-number">{{ metrics.totalAdoptions || 0 }}</div>
          <div class="stat-label">领养总数</div>
        </div>
        <div class="stat-card posts">
          <div class="stat-number">{{ metrics.totalPosts || 0 }}</div>
          <div class="stat-label">帖子总数</div>
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
              <el-option label="最近7天" value="last7days" />
              <el-option label="今日" value="today" />
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
            <el-dropdown @command="exportTable">
              <el-button style="margin-right: 20px;" type="primary">
                <el-icon><Download /></el-icon>
                导出报表
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="users">用户表</el-dropdown-item>
                  <el-dropdown-item command="cats">猫咪表</el-dropdown-item>
                  <el-dropdown-item command="posts">帖子表</el-dropdown-item>
                  <el-dropdown-item command="adoptions">领养申请表</el-dropdown-item>
                  <el-dropdown-item command="rescues">救助表</el-dropdown-item>
                  <el-dropdown-item command="volunteers">志愿者表</el-dropdown-item>
                  <el-dropdown-item command="comments">评论表</el-dropdown-item>
                  <el-dropdown-item divided command="statistics-excel">统计报告(Excel)</el-dropdown-item>
                  <el-dropdown-item command="statistics-word">统计报告(Word)</el-dropdown-item>
                  <el-dropdown-item command="statistics-images">统计图片</el-dropdown-item>
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
              <el-icon class="metric-icon user-icon"><User /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.totalUsers }}</div>
                <div class="metric-label">用户总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon active-icon"><UserFilled /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.activeUsers }}</div>
                <div class="metric-label">活跃用户</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon ban-icon"><Lock /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.bannedUsers }}</div>
                <div class="metric-label">禁用用户</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon online-icon"><Odometer /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.onlineUsers }}</div>
                <div class="metric-label">在线用户</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon new-user-icon"><Plus /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.newUsers || 0 }}</div>
                <div class="metric-label">新增用户</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon cat-icon"><Star /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.totalCats }}</div>
                <div class="metric-label">猫咪总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon rejected-icon"><CircleCloseFilled /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.rejectedCats || 0 }}</div>
                <div class="metric-label">已拒绝</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon caring-icon"><HomeFilled /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.caringCats }}</div>
                <div class="metric-label">照顾中</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon adoptable-icon"><Box /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.adoptableCats }}</div>
                <div class="metric-label">待领养</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon adopted-icon"><Avatar /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.adoptedCats }}</div>
                <div class="metric-label">已领养</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon new-cat-icon"><Plus /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.newCats || 0 }}</div>
                <div class="metric-label">新增猫咪</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <!-- 救助任务相关卡片 -->
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon rescue-icon"><DocumentAdd /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.totalRescues }}</div>
                <div class="metric-label">救助任务总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon pending-icon"><Clock /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.pendingRescues }}</div>
                <div class="metric-label">待处理</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon progress-icon"><Loading /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.inProgressRescues }}</div>
                <div class="metric-label">进行中</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon completed-icon"><Check /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.completedRescues }}</div>
                <div class="metric-label">已完成</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon new-rescue-icon"><Plus /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.newRescues || 0 }}</div>
                <div class="metric-label">新增救助</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <!-- 领养申请相关卡片 -->
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon adoption-icon"><SuccessFilled /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.totalAdoptions }}</div>
                <div class="metric-label">领养申请总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon pending-icon"><Clock /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.pendingAdoptions }}</div>
                <div class="metric-label">待审核</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon approved-icon"><Select /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.approvedAdoptions }}</div>
                <div class="metric-label">已通过</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon rejected-icon"><CircleCloseFilled /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.rejectedAdoptions }}</div>
                <div class="metric-label">已拒绝</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon new-adoption-icon"><Plus /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.newAdoptions || 0 }}</div>
                <div class="metric-label">新增申请</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon post-icon"><ChatDotRound /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.totalPosts }}</div>
                <div class="metric-label">论坛帖子</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon pending-icon"><Clock /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.pendingPosts }}</div>
                <div class="metric-label">待审核</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon post-approved-icon"><Select /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.approvedPosts }}</div>
                <div class="metric-label">已通过</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon post-rejected-icon"><CircleCloseFilled /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.rejectedPosts }}</div>
                <div class="metric-label">已拒绝</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="metric-card">
            <div class="metric-content">
              <el-icon class="metric-icon new-post-icon"><Plus /></el-icon>
              <div class="metric-info">
                <div class="metric-number">{{ metrics.newPosts || 0 }}</div>
                <div class="metric-label">新增帖子</div>
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
              <span>用户注册趋势（折线图）</span>
              <el-radio-group v-model="chartMode.userTrend" size="small" @change="() => initUserTrendChart(statisticsData)">
                <el-radio-button value="total">总数</el-radio-button>
                <el-radio-button value="daily">每天新增</el-radio-button>
              </el-radio-group>
            </template>
            <div id="userTrendChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>猫咪状态分布（饼状图）</span>
            </template>
            <div id="catStatusChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>救助任务趋势（折线图）</span>
              <el-radio-group v-model="chartMode.rescueTrend" size="small" @change="() => initRescueTrendChart(statisticsData)">
                <el-radio-button value="total">总数</el-radio-button>
                <el-radio-button value="daily">每天新增</el-radio-button>
              </el-radio-group>
            </template>
            <div id="rescueTrendChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>救助状态分布（饼状图）</span>
            </template>
            <div id="rescueStatusChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>领养申请趋势（折线图）</span>
              <el-radio-group v-model="chartMode.adoptionTrend" size="small" @change="() => initAdoptionTrendChart(statisticsData)">
                <el-radio-button value="total">总数</el-radio-button>
                <el-radio-button value="daily">每天新增</el-radio-button>
              </el-radio-group>
            </template>
            <div id="adoptionTrendChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>领养状态分布（饼状图）</span>
            </template>
            <div id="adoptionStatusChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>帖子发布趋势（折线图）</span>
              <el-radio-group v-model="chartMode.postTrend" size="small" @change="() => initPostTrendChart(statisticsData)">
                <el-radio-button value="total">总数</el-radio-button>
                <el-radio-button value="daily">每天新增</el-radio-button>
              </el-radio-group>
            </template>
            <div id="postTrendChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>每日活动统计（柱状图）</span>
            </template>
            <div id="dailyActivityChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>用户角色分布（饼状图）</span>
            </template>
            <div id="userRoleChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>猫咪健康状态分布（饼状图）</span>
            </template>
            <div id="catHealthChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>救助紧急程度分布（饼状图）</span>
            </template>
            <div id="rescueUrgencyChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>系统活跃度分析（雷达图）</span>
            </template>
            <div id="systemActivityChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Download, Refresh, User, Star, DocumentAdd, HelpFilled, ChatDotRound, 
  SuccessFilled, TrendCharts, Lock, UserFilled, Plus, Odometer, CircleCheck,
  Tools, HomeFilled, Box, Avatar, CircleCheckFilled, CircleCloseFilled,
  Select, Close, ArrowDown, Clock, Loading, Check
} from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin.js'
import * as echarts from 'echarts'
import * as XLSX from 'xlsx'
import html2canvas from 'html2canvas'
import { saveAs } from 'file-saver'

const loading = ref(false)
const statisticsData = ref({})
const chartInstances = ref({})

const chartMode = ref({
  userTrend: 'total',
  rescueTrend: 'total',
  adoptionTrend: 'total',
  postTrend: 'total'
})

const timeRangeForm = ref({
  type: 'last7days',
  startDate: null,
  endDate: null
})

const metrics = ref({
  totalUsers: 0,
  bannedUsers: 0,
  activeUsers: 0,
  newUsers: 0,
  onlineUsers: 0,
  totalCats: 0,
  healthyCats: 0,
  recoveringCats: 0,
  caringCats: 0,
  adoptableCats: 0,
  adoptedCats: 0,
  totalRescues: 0,
  pendingRescues: 0,
  inProgressRescues: 0,
  completedRescues: 0,
  criticalRescues: 0,
  highRescues: 0,
  mediumRescues: 0,
  lowRescues: 0,
  totalAdoptions: 0,
  pendingAdoptions:0,
  approvedAdoptions: 0,
  rejectedAdoptions: 0,
  totalPosts: 0,
  pendingPosts: 0,
  newPosts: 0,
  approvedPosts: 0,
  rejectedPosts: 0
})

const trends = ref({
  userTrend: {},
  catTrend: {},
  rescueTrend: {},
  adoptionTrend: {},
  postTrend: {}
})

const handleTimeTypeChange = () => {
  if (timeRangeForm.value.type !== 'custom') {
    timeRangeForm.value.startDate = null
    timeRangeForm.value.endDate = null
  }
}

const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const loadStatistics = async () => {
  try {
    loading.value = true
    
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
    
    let response
    if (startDate && endDate) {
      params = {
        startDate: formatDate(startDate),
        endDate: formatDate(endDate)
      }
      response = await adminApi.getDateRangeStatistics(params)
    } else {
      response = await adminApi.getRealTimeStatistics()
    }
    
    if (response.data) {
      const data = response.data
      statisticsData.value = data
      console.log('API响应数据:', data) // 调试信息
      metrics.value = {
        totalUsers: data.totalUsers || 0,
        bannedUsers: data.bannedUsers || 0,
        activeUsers: data.activeUsers || 0,
        newUsers: data.newUsers || data.newUsersToday || 0,
        onlineUsers: data.onlineUsers || 0,
        totalCats: data.totalCats || 0,
        healthyCats: data.healthyCats || 0,
        recoveringCats: data.recoveringCats || 0,
        caringCats: data.caringCats || 0,
        adoptableCats: data.adoptableCats || 0,
        adoptedCats: data.adoptedCats || 0,
        newCats: data.newCats || 0,
        rejectedCats: data.rejectedCats || 0,
        totalRescues: data.totalRescues || 0,
        pendingRescues: data.pendingRescues || 0,
        inProgressRescues: data.inProgressRescues || 0,
        completedRescues: data.completedRescues || 0,
        newRescues: data.newRescues || 0,
        // 救助紧急程度统计（后端已提供）
        criticalRescues: data.criticalRescues || 0,
        highRescues: data.highRescues || 0,
        mediumRescues: data.mediumRescues || 0,
        lowRescues: data.lowRescues || 0,
        totalAdoptions: data.totalAdoptions || 0,
        // 领养申请状态统计（后端已提供）
        pendingAdoptions: data.pendingAdoptions || 0,
        approvedAdoptions: data.approvedAdoptions || 0,
        rejectedAdoptions: data.rejectedAdoptions || 0,
        newAdoptions: data.newAdoptions || 0,
        totalPosts: data.totalPosts || 0,
        // 帖子状态统计（后端已提供）
        pendingPosts: data.pendingPosts || 0,
        newPosts: data.newPosts || data.newPostsToday || 0,
        approvedPosts: data.approvedPosts || 0,
        rejectedPosts: data.rejectedPosts || 0
      }
      
      trends.value = {
        userTrend: data.userTrend || {},
        catTrend: data.catTrend || {},
        rescueTrend: data.rescueTrend || {},
        adoptionTrend: data.adoptionTrend || {},
        postTrend: data.postTrend || {}
      }
      
      await nextTick()
      initCharts(data)
    }
    
    ElMessage.success('数据加载成功')
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

const initCharts = (data) => {
  initUserTrendChart(data)
  initCatStatusChart(data)
  initRescueTrendChart(data)
  initRescueStatusChart(data)
  initAdoptionTrendChart(data)
  initAdoptionStatusChart(data)
  initPostTrendChart(data)
  initDailyActivityChart(data)
  initUserRoleChart(data)
  initCatHealthChart(data)
  initRescueUrgencyChart(data)
  initSystemActivityChart(data)
}

const initUserTrendChart = (data) => {
  const chartElement = document.getElementById('userTrendChart')
  if (!chartElement || !data) return
  
  if (chartInstances.value.userTrendChart) {
    chartInstances.value.userTrendChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.userTrendChart = chart
  const trendData = data.userTrend || []
  const dates = trendData.map(item => item.date)
  const values = chartMode.value.userTrend === 'total' 
    ? trendData.map(item => item.totalUsers) 
    : trendData.map(item => item.newUsers)
  
  const option = {
    title: { text: '用户增长趋势', left: 'left', top: 'top' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: dates.length > 0 ? dates : ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: { type: 'value' },
    series: [{
      name: chartMode.value.userTrend === 'total' ? '用户总数' : '每日新增',
      data: values.length > 0 ? values : (chartMode.value.userTrend === 'total' ? [100, 105, 110, 115, 120, 125, 130] : [5, 8, 6, 10, 12, 8, 15]),
      type: 'line',
      smooth: true,
      itemStyle: { color: '#409eff' }
    }]
  }
  chart.setOption(option)
}

const initCatStatusChart = (data) => {
  const chartElement = document.getElementById('catStatusChart')
  if (!chartElement || !data) return
  
  if (chartInstances.value.catStatusChart) {
    chartInstances.value.catStatusChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.catStatusChart = chart
  const option = {
    title: { text: '猫咪状态分布', left: 'left', top: 'top' },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '70%',
      data: [
        { value: data.adoptableCats || 0, name: '待领养' },
        { value: data.adoptedCats || 0, name: '已领养' },
        { value: data.caringCats || 0, name: '照顾中' }
      ]
    }]
  }
  chart.setOption(option)
}

const initRescueTrendChart = (data) => {
  const chartElement = document.getElementById('rescueTrendChart')
  if (!chartElement || !data) return
  
  if (chartInstances.value.rescueTrendChart) {
    chartInstances.value.rescueTrendChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.rescueTrendChart = chart
  const trendData = data.rescueTrend || []
  const dates = trendData.map(item => item.date)
  const values = chartMode.value.rescueTrend === 'total' 
    ? trendData.map(item => item.totalRescues) 
    : trendData.map(item => item.newRescues)
  
  const option = {
    title: { text: '救助趋势', left: 'left', top: 'top' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: dates.length > 0 ? dates : ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: { type: 'value' },
    series: [{
      name: chartMode.value.rescueTrend === 'total' ? '救助总数' : '每日新增',
      data: values.length > 0 ? values : (chartMode.value.rescueTrend === 'total' ? [10, 15, 8, 12, 20, 18, 14] : [2, 3, 1, 4, 5, 3, 2]),
      type: 'line',
      smooth: true,
      itemStyle: { color: '#e6a23c' }
    }]
  }
  chart.setOption(option)
}

const initRescueStatusChart = (data) => {
  const chartElement = document.getElementById('rescueStatusChart')
  if (!chartElement || !data) return
  
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
      ]
    }]
  }
  chart.setOption(option)
}

const initAdoptionTrendChart = (data) => {
  const chartElement = document.getElementById('adoptionTrendChart')
  if (!chartElement || !data) return
  
  if (chartInstances.value.adoptionTrendChart) {
    chartInstances.value.adoptionTrendChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.adoptionTrendChart = chart
  const trendData = data.adoptionTrend || []
  const dates = trendData.map(item => item.date)
  const values = chartMode.value.adoptionTrend === 'total' 
    ? trendData.map(item => item.totalAdoptions) 
    : trendData.map(item => item.newAdoptions)
  
  const option = {
    title: { text: '领养趋势', left: 'left', top: 'top' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: dates.length > 0 ? dates : ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: { type: 'value' },
    series: [{
      name: chartMode.value.adoptionTrend === 'total' ? '申请总数' : '每日新增',
      data: values.length > 0 ? values : (chartMode.value.adoptionTrend === 'total' ? [5, 8, 6, 10, 12, 8, 15] : [1, 2, 1, 3, 2, 1, 2]),
      type: 'line',
      smooth: true,
      itemStyle: { color: '#67c23a' }
    }]
  }
  chart.setOption(option)
}

const initAdoptionStatusChart = (data) => {
  const chartElement = document.getElementById('adoptionStatusChart')
  if (!chartElement || !data) return
  
  if (chartInstances.value.adoptionStatusChart) {
    chartInstances.value.adoptionStatusChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.adoptionStatusChart = chart
  const option = {
    title: { text: '领养状态分布', left: 'left', top: 'top' },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '70%',
      data: [
        { value: data.approvedAdoptions || 0, name: '已通过' },
        { value: data.rejectedAdoptions || 0, name: '已拒绝' },
        { value: (data.totalAdoptions || 0) - (data.approvedAdoptions || 0) - (data.rejectedAdoptions || 0), name: '待审核' }
      ]
    }]
  }
  chart.setOption(option)
}

const initPostTrendChart = (data) => {
  const chartElement = document.getElementById('postTrendChart')
  if (!chartElement || !data) return
  
  if (chartInstances.value.postTrendChart) {
    chartInstances.value.postTrendChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.postTrendChart = chart
  const trendData = data.postTrend || []
  const dates = trendData.map(item => item.date)
  const values = chartMode.value.postTrend === 'total' 
    ? trendData.map(item => item.totalPosts) 
    : trendData.map(item => item.newPosts)
  
  const option = {
    title: { text: '帖子趋势', left: 'left', top: 'top' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: dates.length > 0 ? dates : ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: { type: 'value' },
    series: [{
      name: chartMode.value.postTrend === 'total' ? '帖子总数' : '每日新增',
      data: values.length > 0 ? values : (chartMode.value.postTrend === 'total' ? [20, 25, 18, 30, 35, 28, 40] : [4, 5, 3, 6, 7, 5, 8]),
      type: 'line',
      smooth: true,
      itemStyle: { color: '#909399' }
    }]
  }
  chart.setOption(option)
}

const initDailyActivityChart = (data) => {
  const chartElement = document.getElementById('dailyActivityChart')
  if (!chartElement || !data) return
  
  if (chartInstances.value.dailyActivityChart) {
    chartInstances.value.dailyActivityChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.dailyActivityChart = chart
  const activityData = data.dailyActivity || []
  const dates = activityData.map(item => item.date)
  
  const option = {
    title: { text: '每日活跃度', left: 'left', top: 'top' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['用户', '帖子', '救助', '领养'] },
    xAxis: {
      type: 'category',
      data: dates.length > 0 ? dates : ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '用户',
        type: 'bar',
        data: dates.length > 0 ? activityData.map(item => item.users) : [5, 8, 6, 10, 12, 8, 15],
        itemStyle: { color: '#409eff' }
      },
      {
        name: '帖子',
        type: 'bar',
        data: dates.length > 0 ? activityData.map(item => item.posts) : [20, 25, 18, 30, 35, 28, 40],
        itemStyle: { color: '#909399' }
      },
      {
        name: '救助',
        type: 'bar',
        data: dates.length > 0 ? activityData.map(item => item.rescues) : [10, 15, 8, 12, 20, 18, 14],
        itemStyle: { color: '#e6a23c' }
      },
      {
        name: '领养',
        type: 'bar',
        data: dates.length > 0 ? activityData.map(item => item.adoptions) : [5, 8, 6, 10, 12, 8, 15],
        itemStyle: { color: '#67c23a' }
      }
    ]
  }
  chart.setOption(option)
}

const refreshData = () => {
  loadStatistics()
}

const initUserRoleChart = (data) => {
  const chartElement = document.getElementById('userRoleChart')
  if (!chartElement || !data) return
  
  if (chartInstances.value.userRoleChart) {
    chartInstances.value.userRoleChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.userRoleChart = chart
  
  const userDistribution = data.userDistribution || {}
  const option = {
    title: { text: '用户角色分布', left: 'left', top: 'top' },
    tooltip: { trigger: 'item' },
    color: ['#409EFF', '#67C23A', '#F56C6C'],
    series: [{
      type: 'pie',
      radius: '70%',
      data: [
        { value: userDistribution['普通用户'] || 0, name: '普通用户' },
        { value: userDistribution['救助者'] || 0, name: '救助者' },
        { value: userDistribution['管理员'] || 0, name: '管理员' }
      ],
      label: {
        show: true,
        formatter: '{b}: {c} ({d}%)'
      }
    }]
  }
  chart.setOption(option)
}

const initCatHealthChart = (data) => {
  const chartElement = document.getElementById('catHealthChart')
  if (!chartElement || !data) return
  
  if (chartInstances.value.catHealthChart) {
    chartInstances.value.catHealthChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.catHealthChart = chart
  const option = {
    title: { text: '猫咪健康状况', left: 'left', top: 'top' },
    tooltip: { trigger: 'item' },
    color: ['#67C23A', '#409EFF', '#E6A23C'],
    series: [{
      type: 'pie',
      radius: '70%',
      data: [
        { value: data.healthyCats || 0, name: '健康' },
        { value: data.recoveringCats || 0, name: '恢复中' },
        { value: data.caringCats || 0, name: '照顾中' }
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
  if (!chartElement || !data) return
  
  if (chartInstances.value.rescueUrgencyChart) {
    chartInstances.value.rescueUrgencyChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.rescueUrgencyChart = chart
  const option = {
    title: { text: '救助紧急程度', left: 'left', top: 'top' },
    tooltip: { trigger: 'item' },
    color: ['#F56C6C', '#E6A23C', '#409EFF', '#67C23A'],
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

const initSystemActivityChart = (data) => {
  const chartElement = document.getElementById('systemActivityChart')
  if (!chartElement || !data) return
  
  if (chartInstances.value.systemActivityChart) {
    chartInstances.value.systemActivityChart.dispose()
  }
  const chart = echarts.init(chartElement)
  chartInstances.value.systemActivityChart = chart
  const option = {
    title: { text: '系统活跃度', left: 'left', top: 'top' },
    tooltip: { trigger: 'axis' },
    radar: {
      indicator: [
        { name: '用户活跃度', max: 100 },
        { name: '内容发布', max: 100 },
        { name: '救助效率', max: 100 },
        { name: '领养完成率', max: 100 },
        { name: '系统响应', max: 100 }
      ]
    },
    series: [{
      type: 'radar',
      data: [{
        value: [
          data.activeUsers ? Math.min(data.activeUsers / (data.totalUsers || 1) * 100, 100) : 50,
          data.totalPosts ? Math.min(data.totalPosts / 100 * 100, 100) : 60,
          data.completedRescues ? Math.min(data.completedRescues / (data.totalRescues || 1) * 100, 100) : 70,
          data.approvedAdoptions ? Math.min(data.approvedAdoptions / (data.totalAdoptions || 1) * 100, 100) : 80,
          90
        ],
        name: '系统活跃度'
      }]
    }]
  }
  chart.setOption(option)
}

const exportTable = async (type) => {
  try {
    if (type === 'statistics-excel') {
      await exportStatisticsReport()
      return
    }
    
    if (type === 'statistics-word') {
      await exportStatisticsWord()
      return
    }
    
    if (type === 'statistics-images') {
      await exportStatisticsImages()
      return
    }
    
    const filenames = {
      users: '用户表',
      cats: '猫咪表',
      posts: '帖子表',
      adoptions: '领养申请表',
      rescues: '救助表',
      volunteers: '志愿者表',
      comments: '评论表'
    }
    
    const now = new Date()
    const dateStr = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
    const filename = `${filenames[type]}_${dateStr}.xlsx`
    
    const params = {}
    if (timeRangeForm.startDate && timeRangeForm.endDate) {
      params.startDate = timeRangeForm.startDate.toISOString().split('T')[0]
      params.endDate = timeRangeForm.endDate.toISOString().split('T')[0]
    } else if (timeRangeForm.type !== 'custom') {
      const now = new Date()
      let startDate, endDate
      switch (timeRangeForm.type) {
        case 'today':
          startDate = new Date(now)
          startDate.setHours(0, 0, 0, 0)
          endDate = new Date(now)
          endDate.setHours(23, 59, 59, 999)
          break
        case 'last7days':
          startDate = new Date(now)
          startDate.setDate(now.getDate() - 6)
          startDate.setHours(0, 0, 0, 0)
          endDate = new Date(now)
          endDate.setHours(23, 59, 59, 999)
          break
        case 'week':
          const day = now.getDay()
          const diff = now.getDate() - day + (day === 0 ? -6 : 1)
          startDate = new Date(now.setDate(diff))
          startDate.setHours(0, 0, 0, 0)
          endDate = new Date(startDate)
          endDate.setDate(endDate.getDate() + 6)
          endDate.setHours(23, 59, 59, 999)
          break
        case 'month':
          startDate = new Date(now.getFullYear(), now.getMonth(), 1)
          startDate.setHours(0, 0, 0, 0)
          endDate = new Date(now.getFullYear(), now.getMonth() + 1, 0)
          endDate.setHours(23, 59, 59, 999)
          break
        case 'year':
          startDate = new Date(now.getFullYear(), 0, 1)
          startDate.setHours(0, 0, 0, 0)
          endDate = new Date(now.getFullYear(), 11, 31)
          endDate.setHours(23, 59, 59, 999)
          break
      }
      if (startDate && endDate) {
        params.startDate = startDate.toISOString().split('T')[0]
        params.endDate = endDate.toISOString().split('T')[0]
      }
    }
    
    let response
    switch (type) {
      case 'users':
        response = await adminApi.exportUsers(params)
        break
      case 'cats':
        response = await adminApi.exportCats(params)
        break
      case 'posts':
        response = await adminApi.exportPosts(params)
        break
      case 'adoptions':
        response = await adminApi.exportAdoptions(params)
        break
      case 'rescues':
        response = await adminApi.exportRescues(params)
        break
      case 'volunteers':
        response = await adminApi.exportVolunteers(params)
        break
      case 'comments':
        response = await adminApi.exportComments(params)
        break
    }
    
    downloadFile(response.data, filename)
    ElMessage.success(`${filenames[type]}导出成功`)
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

const exportStatisticsReport = async () => {
  try {
    const wb = XLSX.utils.book_new()
    
    const summaryData = [
      ['统计项', '数值'],
      ['用户总数', metrics.value.totalUsers || 0],
      ['活跃用户', metrics.value.activeUsers || 0],
      ['新增用户', metrics.value.newUsers || 0],
      ['在线用户', metrics.value.onlineUsers || 0],
      ['禁用用户', metrics.value.bannedUsers || 0],
      ['猫咪总数', metrics.value.totalCats || 0],
      ['待领养', metrics.value.adoptableCats || 0],
      ['已领养', metrics.value.adoptedCats || 0],
      ['照顾中', metrics.value.caringCats || 0],
      ['救助任务总数', metrics.value.totalRescues || 0],
      ['待处理', metrics.value.pendingRescues || 0],
      ['进行中', metrics.value.inProgressRescues || 0],
      ['已完成', metrics.value.completedRescues || 0],
      ['领养申请总数', metrics.value.totalAdoptions || 0],
      ['待审核', metrics.value.pendingAdoptions || 0],
      ['已通过', metrics.value.approvedAdoptions || 0],
      ['已拒绝', metrics.value.rejectedAdoptions || 0],
      ['论坛帖子', metrics.value.totalPosts || 0],
      ['待审核', metrics.value.pendingPosts || 0],
      ['已通过', metrics.value.approvedPosts || 0],
      ['已拒绝', metrics.value.rejectedPosts || 0]
    ]
    
    const summarySheet = XLSX.utils.aoa_to_sheet(summaryData)
    XLSX.utils.book_append_sheet(wb, summarySheet, '统计摘要')
    
    const now = new Date()
    const dateStr = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
    XLSX.writeFile(wb, `统计报告_${dateStr}.xlsx`)
    
    ElMessage.success('统计报告导出成功')
  } catch (error) {
    console.error('导出统计报告失败:', error)
    ElMessage.error('导出统计报告失败')
  }
}

const exportStatisticsImages = async () => {
  try {
    const chartIds = [
      'userTrendChart',
      'catStatusChart',
      'rescueTrendChart',
      'rescueStatusChart',
      'adoptionTrendChart',
      'adoptionStatusChart',
      'postTrendChart',
      'dailyActivityChart',
      'userRoleChart',
      'catHealthChart',
      'rescueUrgencyChart',
      'systemActivityChart'
    ]
    
    const chartTitles = [
      '用户注册趋势',
      '猫咪状态分布',
      '救助任务趋势',
      '救助状态分布',
      '领养申请趋势',
      '领养状态分布',
      '帖子发布趋势',
      '每日活动统计',
      '用户角色分布',
      '猫咪健康状态分布',
      '救助紧急程度分布',
      '系统活跃度分析'
    ]
    
    for (let i = 0; i < chartIds.length; i++) {
      const chartElement = document.getElementById(chartIds[i])
      if (!chartElement) continue
      
      const chart = chartInstances.value[chartIds[i]]
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
    
    ElMessage.success('统计图片导出成功')
  } catch (error) {
    console.error('导出统计图片失败:', error)
    ElMessage.error('导出统计图片失败')
  }
}

const exportStatisticsWord = async () => {
  try {
    ElMessage.info('正在生成Word文档，请稍候...')
    
    const chartIds = [
      'userTrendChart',
      'catStatusChart',
      'rescueTrendChart',
      'rescueStatusChart',
      'adoptionTrendChart',
      'adoptionStatusChart',
      'postTrendChart',
      'dailyActivityChart',
      'userRoleChart',
      'catHealthChart',
      'rescueUrgencyChart',
      'systemActivityChart'
    ]
    
    const chartTitles = [
      '用户注册趋势',
      '猫咪状态分布',
      '救助任务趋势',
      '救助状态分布',
      '领养申请趋势',
      '领养状态分布',
      '帖子发布趋势',
      '每日活动统计',
      '用户角色分布',
      '猫咪健康状态分布',
      '救助紧急程度分布',
      '系统活跃度分析'
    ]
    
    let htmlContent = `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <style>
          body { font-family: 'Microsoft YaHei', Arial, sans-serif; padding: 20px; }
          h1 { text-align: center; color: #333; margin-bottom: 30px; }
          h2 { color: #409eff; margin-top: 30px; margin-bottom: 15px; }
          .chart-container { text-align: center; margin-bottom: 30px; }
          .chart-container img { max-width: 100%; height: auto; }
          .metrics-section { margin-bottom: 30px; }
          .metrics-table { width: 100%; border-collapse: collapse; }
          .metrics-table th, .metrics-table td { border: 1px solid #ddd; padding: 12px; text-align: left; }
          .metrics-table th { background-color: #f5f7fa; font-weight: bold; }
        </style>
      </head>
      <body>
        <h1>猫咪救助平台数据统计报告</h1>
        <p style="text-align: center; color: #666;">生成时间: ${new Date().toLocaleString('zh-CN')}</p>
        
        <div class="metrics-section">
          <h2>关键指标</h2>
          <table class="metrics-table">
            <tr><th>指标</th><th>数值</th></tr>
            <tr><td>用户总数</td><td>${metrics.value.totalUsers || 0}</td></tr>
            <tr><td>活跃用户</td><td>${metrics.value.activeUsers || 0}</td></tr>
            <tr><td>在线用户</td><td>${metrics.value.onlineUsers || 0}</td></tr>
            <tr><td>猫咪总数</td><td>${metrics.value.totalCats || 0}</td></tr>
            <tr><td>待领养猫咪</td><td>${metrics.value.adoptableCats || 0}</td></tr>
            <tr><td>已领养猫咪</td><td>${metrics.value.adoptedCats || 0}</td></tr>
            <tr><td>救助任务总数</td><td>${metrics.value.totalRescues || 0}</td></tr>
            <tr><td>已完成救助</td><td>${metrics.value.completedRescues || 0}</td></tr>
            <tr><td>领养申请总数</td><td>${metrics.value.totalAdoptions || 0}</td></tr>
            <tr><td>帖子总数</td><td>${metrics.value.totalPosts || 0}</td></tr>
          </table>
        </div>
    `
    
    for (let i = 0; i < chartIds.length; i++) {
      const chartId = chartIds[i]
      const chart = chartInstances.value[chartId]
      if (chart) {
        const base64 = chart.getDataURL({
          type: 'png',
          pixelRatio: 2,
          backgroundColor: '#fff'
        })
        htmlContent += `
          <div class="chart-container">
            <h2>${chartTitles[i]}</h2>
            <img src="${base64}" alt="${chartTitles[i]}" />
          </div>
        `
      }
    }
    
    htmlContent += `
      </body>
      </html>
    `
    
    const blob = new Blob([htmlContent], { type: 'application/msword' })
    const now = new Date()
    const dateStr = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
    saveAs(blob, `统计报告_${dateStr}.doc`)
    
    ElMessage.success('统计报告Word文档导出成功')
  } catch (error) {
    console.error('导出Word文档失败:', error)
    ElMessage.error('导出Word文档失败')
  }
}

const downloadFile = (blob, filename) => {
  const url = window.URL.createObjectURL(new Blob([blob]))
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const disposeCharts = () => {
  Object.values(chartInstances.value).forEach(chart => {
    if (chart) {
      chart.dispose()
    }
  })
  chartInstances.value = {}
}

onMounted(() => {
  loadStatistics()
})

onUnmounted(() => {
  disposeCharts()
})
</script>

<style scoped>
.admin-statistics {
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
  opacity: 0.9;
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

.stat-card.active {
  background: rgba(241, 171, 102, 0.3);
  color: white;
}

.stat-card.rescues {
  background: rgba(72, 202, 254, 0.3);
  color: white;
}

.stat-card.adoptions {
  background: rgba(38, 237, 134, 0.3);
  color: white;
}

.stat-card.posts {
  background: rgba(234, 117, 45, 0.3);
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

.ban-icon {
  background: #fef0f0;
  color: #f56c6c;
}

.active-icon {
  background: #ecf5ff;
  color: #409eff;
}

.new-user-icon {
  background: #f0f9ff;
  color: #67c23a;
}

.online-icon {
  background: #f0f9ff;
  color: #e6a23c;
}

.cat-icon {
  background: #fef0f0;
  color: #f56c6c;
}

.healthy-icon {
  background: #f0f9eb;
  color: #67c23a;
}

.recovering-icon {
  background: #fdf6ec;
  color: #e6a23c;
}

.caring-icon {
  background: #ecf5ff;
  color: #409eff;
}

.adoptable-icon {
  background: #f0f9ff;
  color: #909399;
}

.adopted-icon {
  background: #fef0f0;
  color: #e6a23c;
}
.new-cat-icon{
  background: #f0f9ff;
  color: #67c23a;
}

.rescue-icon {
  background: #fdf6ec;
  color: #e6a23c;
}

.adoption-icon {
  background: #f0f9eb;
  color: #67c23a;
}

.approved-icon {
  background: #f0f9eb;
  color: #67c23a;
}

.rejected-icon {
  background: #fef0f0;
  color: #f56c6c;
}

.post-icon {
  background: #f4f4f5;
  color: #909399;
}

.new-post-icon {
  background: #f0f9ff;
  color: #409eff;
}

.post-approved-icon {
  background: #f0f9eb;
  color: #67c23a;
}

.post-rejected-icon {
  background: #fef0f0;
  color: #f56c6c;
}

/* 新增救助和领养卡片样式 */
.pending-icon {
  background: #fdf6ec;
  color: #e6a23c;
}

.progress-icon {
  background: #ecf5ff;
  color: #409eff;
}

.completed-icon {
  background: #f0f9eb;
  color: #67c23a;
}

.new-rescue-icon {
  background: #f0f9ff;
  color: #409eff;
}

.new-adoption-icon {
  background: #f0f9ff;
  color: #67c23a;
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

</style>
