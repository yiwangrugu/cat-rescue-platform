<template>
  <div class="volunteer-management">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><User /></el-icon>
          志愿者管理
        </h1>
        <p class="page-subtitle">管理平台志愿者信息和值班安排</p>
      </div>
      <div class="header-stats">
        <div class="stat-card total">
          <div class="stat-number">{{ volunteerStats.totalVolunteers || 0 }}</div>
          <div class="stat-label">志愿者总数</div>
        </div>
        <div class="stat-card active">
          <div class="stat-number">{{ volunteerStats.activeVolunteers || 0 }}</div>
          <div class="stat-label">活跃志愿者</div>
        </div>
        <div class="stat-card on-duty">
          <div class="stat-number">{{ todayOnDutyCount || 0 }}</div>
          <div class="stat-label">今日值班</div>
        </div>
      </div>
    </div>

    <div class="filter-section">
      <el-form :model="filterForm" inline>
        <el-form-item label="姓名">
          <el-input v-model="filterForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="filterForm.phone" placeholder="请输入电话" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable>
            <el-option label="空闲" value="空闲" />
            <el-option label="忙碌" value="忙碌" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadVolunteers">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetFilters">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="showAddVolunteerDialog = true" :icon="Plus">添加志愿者</el-button>
          <el-button @click="exportVolunteers" :icon="Download">导出数据</el-button>
          <el-button @click="exportSchedules" :icon="Download">导出排班表</el-button>
       </el-form-item>
      </el-form>
    </div>

    <div class="volunteer-table-section">
      <el-card>
        <el-tabs v-model="activeTab" class="management-tabs">
          <el-tab-pane label="志愿者列表" name="list">
            <template #label>
              <el-icon><User /></el-icon>
              <span>志愿者列表</span>
            </template>
            <div class="tab-header">
              <div class="table-actions">
          <el-button size="small" @click="batchEnable" :disabled="selectedVolunteers.length === 0">批量设为空闲</el-button>
          <el-button size="small" type="warning" @click="batchDisable" :disabled="selectedVolunteers.length === 0">批量设为忙碌</el-button>
        </div>
            </div>
        
        <el-table 
          :data="filteredVolunteers" 
          v-loading="loading"
          @selection-change="handleSelectionChange"
          @row-click="handleRowClick"
          style="width: 100%">
          <el-table-column type="selection" width="55" align="center" :selectable="() => true" />
          <el-table-column prop="id" label="ID" width="100" align="center" />
          <el-table-column prop="name" label="姓名" width="160" align="center" />
          <el-table-column prop="phone" label="电话" width="180" align="center" />
          <el-table-column prop="email" label="邮箱" width="180" align="center">
            <template #default="{ row }">{{ row.email || '-' }}</template>
          </el-table-column>
          <el-table-column prop="address" label="地址" min-width="150" show-overflow-tooltip align="center">
            <template #default="{ row }">{{ row.address || '-' }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="180" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '空闲' ? 'success' : 'warning'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="200" align="center">
            <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="280" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button size="small" @click.stop="editVolunteer(row)" class="edit-btn">编辑</el-button>
                <el-button 
                  v-if="row.status === '空闲'"
                  size="small" 
                  type="warning" 
                  @click.stop="toggleVolunteerStatus(row)"
                  class="busy-btn">
                  设为忙碌
                </el-button>
                <el-button 
                  v-else
                  size="small" 
                  type="success" 
                  @click.stop="toggleVolunteerStatus(row)"
                  class="idle-btn">
                  设为空闲
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

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
          </el-tab-pane>

          <el-tab-pane label="排班表" name="schedule">
            <template #label>
              <el-icon><Calendar /></el-icon>
              <span>排班表</span>
            </template>
            <div class="tab-header">
              <el-button type="primary" @click="loadAllSchedules" :icon="Refresh">刷新排班</el-button>
            </div>
            <div v-loading="scheduleLoading" class="schedule-table-wrapper">
              <el-table :data="scheduleData" border style="width: 100%">
                <el-table-column prop="volunteerName" label="志愿者" width="150" align="center" fixed="left" />
                <el-table-column v-for="day in weekDaysList" :key="day.dayOfWeek" :label="day.dayName" align="center">
                  <template #default="{ row }">
                    <div class="day-cell" :class="{ 'on-duty': row[day.dayOfWeek]?.isOnDuty }">
                      <div v-if="row[day.dayOfWeek]?.isOnDuty">
                        <div class="duty-status">
                          <el-tag type="success" size="small" effect="dark">
                            <el-icon><Check /></el-icon>
                            值班
                          </el-tag>
                        </div>
                        <div class="time-info">
                          <span class="time-text">{{ row[day.dayOfWeek].startTime || '09:00' }}</span>
                          <span class="time-separator"> - </span>
                          <span class="time-text">{{ row[day.dayOfWeek].endTime || '18:00' }}</span>
                        </div>
                        <el-button 
                          size="small" 
                          type="primary" 
                          link
                          @click.stop="editVolunteerSchedule(row.volunteerId)">
                          调整
                        </el-button>
                      </div>
                      <div v-else>
                        <div class="duty-status">
                          <el-tag type="info" size="small" effect="plain">
                            <el-icon><Clock /></el-icon>
                            休息
                          </el-tag>
                        </div>
                        <el-button 
                          size="small" 
                          type="primary" 
                          link
                          @click.stop="editVolunteerSchedule(row.volunteerId)">
                          安排
                        </el-button>
                      </div>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>

    <el-dialog 
      :title="editVolunteerForm.id ? '编辑志愿者' : '添加志愿者'" 
      v-model="showAddVolunteerDialog" 
      width="600px"
      @close="handleDialogClose">
      <el-form :model="editVolunteerForm" :rules="volunteerRules" ref="volunteerFormRef" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editVolunteerForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="editVolunteerForm.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editVolunteerForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input type="textarea" v-model="editVolunteerForm.address" :rows="3" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="editVolunteerForm.status" placeholder="请选择状态">
            <el-option label="空闲" value="空闲" />
            <el-option label="忙碌" value="忙碌" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showAddVolunteerDialog = false">取消</el-button>
        <el-button type="primary" @click="submitVolunteer" :loading="submitting">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog 
      title="值班安排" 
      v-model="showScheduleDialog" 
      width="700px">
      <div class="schedule-container">
        <div class="schedule-header">
          <h3>{{ currentVolunteer?.name }} 的值班安排</h3>
        </div>
        <el-table :data="currentWeekDays" style="width: 100%">
          <el-table-column prop="dayName" label="星期" width="100" />
          <el-table-column prop="isOnDuty" label="值班" width="100">
            <template #default="{ row }">
              <el-switch v-model="row.isOnDuty" @change="handleScheduleChange(row)" />
            </template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="150">
            <template #default="{ row }">
              <el-time-picker 
                v-model="row.startTime" 
                format="HH:mm"
                value-format="HH:mm"
                :disabled="!row.isOnDuty"
                placeholder="开始时间" />
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="150">
            <template #default="{ row }">
              <el-time-picker 
                v-model="row.endTime" 
                format="HH:mm"
                value-format="HH:mm"
                :disabled="!row.isOnDuty"
                placeholder="结束时间" />
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div slot="footer">
        <el-button @click="showScheduleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCurrentSchedule" :loading="submitting">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin.js'
import {
  Plus, 
  Download, 
  User, 
  Refresh,
  Search,
  Calendar,
  Check,
  Clock
} from '@element-plus/icons-vue'

const volunteers = ref([])
const loading = ref(false)
const selectedVolunteers = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showAddVolunteerDialog = ref(false)
const showScheduleDialog = ref(false)
const submitting = ref(false)
const volunteerFormRef = ref(null)
const currentVolunteer = ref(null)
const weekDays = ref([])
const allSchedules = ref([])
const scheduleLoading = ref(false)
const activeTab = ref('list')
const currentWeekDays = ref([])

const filterForm = ref({
  name: '',
  phone: '',
  status: ''
})

const volunteerStats = ref({
  totalVolunteers: 0,
  activeVolunteers: 0
})

const editVolunteerForm = ref({
  id: null,
  name: '',
  phone: '',
  email: '',
  address: '',
  status: '空闲'
})

const volunteerRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入电话', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

const dayNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']

const weekDaysList = computed(() => {
  return [
    { dayOfWeek: 1, dayName: '周一' },
    { dayOfWeek: 2, dayName: '周二' },
    { dayOfWeek: 3, dayName: '周三' },
    { dayOfWeek: 4, dayName: '周四' },
    { dayOfWeek: 5, dayName: '周五' },
    { dayOfWeek: 6, dayName: '周六' },
    { dayOfWeek: 7, dayName: '周日' }
  ]
})

const todayOnDutyCount = computed(() => {
  const today = new Date().getDay()
  const dayOfWeek = today === 0 ? 7 : today
  let count = 0
  volunteers.value.forEach(v => {
    const schedule = allSchedules.value.find(w => w.dayOfWeek === dayOfWeek && w.volunteerId === v.id)
    if (schedule?.isOnDuty) count++
  })
  return count
})

const filteredVolunteers = computed(() => {
  return volunteers.value.filter(volunteer => {
    const matchesName = !filterForm.value.name || 
      volunteer.name.toLowerCase().includes(filterForm.value.name.toLowerCase())
    const matchesPhone = !filterForm.value.phone || 
      volunteer.phone.includes(filterForm.value.phone)
    const matchesStatus = !filterForm.value.status || volunteer.status === filterForm.value.status
    
    return matchesName && matchesPhone && matchesStatus
  })
})

const scheduleData = computed(() => {
  return volunteers.value.map(volunteer => {
    const row = {
      volunteerId: volunteer.id,
      volunteerName: volunteer.name
    }
    weekDaysList.value.forEach(day => {
      const schedule = allSchedules.value.find(
        s => s.volunteerId === volunteer.id && s.dayOfWeek === day.dayOfWeek
      )
      row[day.dayOfWeek] = schedule || { isOnDuty: false }
    })
    return row
  })
})

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

const loadVolunteers = async () => {
  try {
    loading.value = true
    
    const response = await adminApi.getVolunteers({
      name: filterForm.value.name,
      phone: filterForm.value.phone,
      page: currentPage.value,
      size: pageSize.value
    })
    
    if (response && response.data) {
      volunteers.value = response.data.records || response.data.volunteers || response.data.list || response.data
      total.value = response.data.total || volunteers.value.length
    } else {
      volunteers.value = []
      total.value = 0
    }
    
    calculateVolunteerStats()
  } catch (error) {
    console.error('加载志愿者列表失败:', error)
    ElMessage.error('加载志愿者列表失败')
    volunteers.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const calculateVolunteerStats = () => {
  volunteerStats.value = {
    totalVolunteers: volunteers.value.length,
    activeVolunteers: volunteers.value.filter(v => v.status === '空闲').length
  }
}

const resetFilters = () => {
  filterForm.value = {
    name: '',
    phone: '',
    status: ''
  }
  loadVolunteers()
}

const handleSelectionChange = (selection) => {
  selectedVolunteers.value = selection
}

const handleRowClick = (row, column, event) => {
  if (column.type !== 'selection') {
    editVolunteer(row)
  }
}

const editVolunteer = (volunteer) => {
  editVolunteerForm.value = { ...volunteer }
  showAddVolunteerDialog.value = true
}

const toggleVolunteerStatus = async (volunteer) => {
  try {
    const action = volunteer.status === '空闲' ? '设为忙碌' : '设为空闲'
    
    await ElMessageBox.confirm(
      `确定要${action}志愿者 ${volunteer.name} 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await adminApi.toggleVolunteerStatus(volunteer.id)
    if (response && response.data) {
      volunteer.status = response.data.status
    }
    ElMessage.success(`${action}成功`)
    calculateVolunteerStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const manageSchedule = async (volunteer) => {
  currentVolunteer.value = volunteer
  try {
    const response = await adminApi.getVolunteerSchedules(volunteer.id)
    if (response && response.data && response.data.length > 0) {
      weekDays.value = response.data.map(s => ({
        ...s,
        dayName: dayNames[s.dayOfWeek]
      }))
    } else {
      await adminApi.initVolunteerSchedules(volunteer.id)
      const initResponse = await adminApi.getVolunteerSchedules(volunteer.id)
      weekDays.value = initResponse.data.map(s => ({
        ...s,
        dayName: dayNames[s.dayOfWeek]
      }))
    }
    showScheduleDialog.value = true
  } catch (error) {
    console.error('加载值班安排失败:', error)
    ElMessage.error('加载值班安排失败')
  }
}

const handleScheduleChange = (row) => {
  if (!row.isOnDuty) {
    row.startTime = '09:00'
    row.endTime = '18:00'
  }
}

const saveSchedules = async () => {
  try {
    const schedules = weekDays.value.map(w => ({
      id: w.id,
      volunteerId: currentVolunteer.value.id,
      dayOfWeek: w.dayOfWeek,
      isOnDuty: w.isOnDuty,
      startTime: w.startTime,
      endTime: w.endTime
    }))
    
    await adminApi.saveVolunteerSchedules(currentVolunteer.value.id, schedules)
    ElMessage.success('值班安排保存成功')
    showScheduleDialog.value = false
  } catch (error) {
    console.error('保存值班安排失败:', error)
    ElMessage.error('保存值班安排失败')
  }
}

const handleDialogClose = () => {
  editVolunteerForm.value = {
    id: null,
    name: '',
    phone: '',
    email: '',
    address: '',
    status: '空闲'
  }
  
  if (volunteerFormRef.value) {
    volunteerFormRef.value.clearValidate()
  }
}

const batchEnable = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要设为空闲选中的 ${selectedVolunteers.value.length} 个志愿者吗？`,
      '批量设为空闲',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const promises = selectedVolunteers.value.map(v => {
      if (v.status === '忙碌') {
        return adminApi.toggleVolunteerStatus(v.id)
      }
      return Promise.resolve()
    })
    
    await Promise.all(promises)
    
    selectedVolunteers.value.forEach(v => {
      if (v.status === '忙碌') {
        v.status = '空闲'
      }
    })
    
    ElMessage.success('批量设为空闲成功')
    selectedVolunteers.value = []
    calculateVolunteerStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量设为空闲失败')
    }
  }
}

const batchDisable = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要设为忙碌选中的 ${selectedVolunteers.value.length} 个志愿者吗？`,
      '批量设为忙碌',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const promises = selectedVolunteers.value.map(v => {
      if (v.status === '空闲') {
        return adminApi.toggleVolunteerStatus(v.id)
      }
      return Promise.resolve()
    })
    
    await Promise.all(promises)
    
    selectedVolunteers.value.forEach(v => {
      if (v.status === '空闲') {
        v.status = '忙碌'
      }
    })
    
    ElMessage.success('批量设为忙碌成功')
    selectedVolunteers.value = []
    calculateVolunteerStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量设为忙碌失败')
    }
  }
}

const submitVolunteer = async () => {
  try {
    if (!volunteerFormRef.value) {
      ElMessage.error('表单引用不存在')
      return
    }
    
    const valid = await volunteerFormRef.value.validate()
    if (!valid) {
      return
    }
    
    const submitData = {
      name: editVolunteerForm.value.name,
      phone: editVolunteerForm.value.phone,
      email: editVolunteerForm.value.email || null,
      address: editVolunteerForm.value.address || null,
      status: editVolunteerForm.value.status
    }
    
    if (editVolunteerForm.value.id) {
      await adminApi.updateVolunteer(editVolunteerForm.value.id, submitData)
      
      const index = volunteers.value.findIndex(v => v.id === editVolunteerForm.value.id)
      if (index !== -1) {
        volunteers.value[index] = { ...volunteers.value[index], ...submitData }
      }
      ElMessage.success('编辑志愿者成功')
    } else {
      const response = await adminApi.addVolunteer(submitData)
      
      if (response && response.data) {
        const newVolunteer = {
          ...response.data,
          createTime: new Date().toISOString()
        }
        volunteers.value.push(newVolunteer)
      }
      ElMessage.success('添加志愿者成功')
    }
    
    showAddVolunteerDialog.value = false
    calculateVolunteerStats()
    total.value = volunteers.value.length
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const exportVolunteers = async () => {
  try {
    const response = await adminApi.exportVolunteers()
    
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `志愿者数据_${new Date().toISOString().split('T')[0]}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('志愿者数据导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const exportSchedules = async () => {
  try {
    const response = await adminApi.exportVolunteerSchedules()
    
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `志愿者排班表_${new Date().toISOString().split('T')[0]}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('排班表导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadVolunteers()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadVolunteers()
}

const loadAllSchedules = async () => {
  try {
    scheduleLoading.value = true
    
    const response = await adminApi.getAllVolunteers()
    
    if (response && response.data) {
      const allVolunteers = response.data
      
      const promises = allVolunteers.map(volunteer => 
        adminApi.getVolunteerSchedules(volunteer.id)
      )
      
      const responses = await Promise.all(promises)
      
      allSchedules.value = responses.flatMap(r => r.data || [])
    }
  } catch (error) {
    ElMessage.error('加载排班信息失败，请检查权限或网络连接')
  } finally {
    scheduleLoading.value = false
  }
}

const editVolunteerSchedule = async (volunteerId) => {
  const volunteer = volunteers.value.find(v => v.id === volunteerId)
  if (!volunteer) {
    ElMessage.error('志愿者不存在')
    return
  }

  currentVolunteer.value = volunteer
  try {
    const response = await adminApi.getVolunteerSchedules(volunteerId)
    if (response && response.data && response.data.length > 0) {
      currentWeekDays.value = response.data.map(s => ({
        ...s,
        dayName: dayNames[s.dayOfWeek]
      }))
    } else {
      await adminApi.initVolunteerSchedules(volunteerId)
      const initResponse = await adminApi.getVolunteerSchedules(volunteerId)
      currentWeekDays.value = initResponse.data.map(s => ({
        ...s,
        dayName: dayNames[s.dayOfWeek]
      }))
    }
    showScheduleDialog.value = true
  } catch (error) {
    console.error('加载值班安排失败:', error)
    ElMessage.error('加载值班安排失败')
  }
}

const saveCurrentSchedule = async () => {
  try {
    submitting.value = true
    const schedules = currentWeekDays.value.map(w => ({
      id: w.id,
      volunteerId: currentVolunteer.value.id,
      dayOfWeek: w.dayOfWeek,
      isOnDuty: w.isOnDuty,
      startTime: w.startTime,
      endTime: w.endTime
    }))
    
    await adminApi.saveVolunteerSchedules(currentVolunteer.value.id, schedules)
    ElMessage.success('值班安排保存成功')
    showScheduleDialog.value = false
    await loadAllSchedules()
  } catch (error) {
    console.error('保存值班安排失败:', error)
    ElMessage.error('保存值班安排失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadVolunteers()
  await loadAllSchedules()
})

// 监听标签页切换，切换到排班表时自动加载排班数据
watch(activeTab, (newTab) => {
  if (newTab === 'schedule') {
    loadAllSchedules()
  }
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

.action-buttons {
  gap: 8px;
}

.action-buttons .el-button {
  min-width: 60px;
  height: 32px;
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
}

.enable-btn:hover {
  background-color: #85ce61;
  border-color: #85ce61;
}

.volunteer-management {
  max-width: 1550px;
  padding: 24px;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
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

.stat-card.total {
  background: rgba(255, 193, 7, 0.3);
}

.stat-card.active {
  background: rgba(40, 167, 69, 0.3);
}

.stat-card.on-duty {
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

.volunteer-table-section {
  margin-top: 20px;
  animation: fadeInUp 0.6s ease;
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
}

.schedule-container {
  padding: 10px 0;
}

.schedule-header {
  margin-bottom: 20px;
}

.schedule-header h3 {
  margin: 0;
  color: #303133;
}

@media (max-width: 1200px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
}

.management-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.tab-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 15px;
}

.schedule-table-wrapper {
  overflow-x: auto;
}

.day-cell {
  padding: 2px;
  min-height: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.3s;
  border-radius: 8px;
  border: 2px solid transparent;
}

.day-cell.on-duty {
  background: linear-gradient(135deg, #f0f9eb 0%, #e8f5e8 100%);
  border-color: #67c23a;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.2);
  position: relative;
  overflow: hidden;
}

.day-cell.on-duty::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #67c23a, #85ce61);
}

.day-cell:hover {
  background-color: #f5f7fa;
  transform: translateY(-2px);
}

.day-cell.on-duty:hover {
  background: linear-gradient(135deg, #e8f5e8 0%, #d1edc4 100%);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

.duty-status {
  margin-bottom: 10px;
}

.duty-status .el-tag {
  font-size: 14px;
  width: 64px;
  height: 32px;
}


.time-info {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  white-space: nowrap;
  flex-wrap: nowrap;
}

.time-text {
  font-weight: 600;
  color: #409eff;
  font-size: 16px;
  white-space: nowrap;
}

.time-separator {
  color: #909399;
  font-weight: 400;
  font-size: 14px;
  white-space: nowrap;
}

.off-duty {
  color: #909399;
  font-style: italic;
  font-size: 14px;
  white-space: nowrap;

  font-size: 14px;
}

@media (max-width: 1200px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
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
