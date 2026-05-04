<template>
  <div class="admin-dashboard">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><House /></el-icon>
          待办事务
        </h1>
        <p class="page-subtitle">处理待审核的猫咪、帖子、救助和领养申请</p>
      </div>
      <div class="header-stats">
        <div class="stat-card pending">
          <div class="stat-number">{{ pendingCounts.cats }}</div>
          <div class="stat-label">待审核猫咪</div>
        </div>
        <div class="stat-card pending">
          <div class="stat-number">{{ pendingCounts.posts }}</div>
          <div class="stat-label">待审核帖子</div>
        </div>
        <div class="stat-card pending">
          <div class="stat-number">{{ pendingCounts.rescues }}</div>
          <div class="stat-label">待处理救助</div>
        </div>
        <div class="stat-card pending">
          <div class="stat-number">{{ pendingCounts.adoptions }}</div>
          <div class="stat-label">待审核领养</div>
        </div>
      </div>
    </div>

    <!-- 二级导航栏 -->
    <div class="secondary-nav">
      <el-menu 
        :default-active="activeTab" 
        mode="horizontal" 
        class="secondary-menu"
        @select="handleTabSelect"
      >
        <el-menu-item index="cats">
          <el-icon><Star /></el-icon>
          <span>猫咪审核</span>
          <el-badge :value="pendingCounts.cats" :max="99" class="badge" />
        </el-menu-item>
        <el-menu-item index="posts">
          <el-icon><Document /></el-icon>
          <span>帖子审核</span>
          <el-badge :value="pendingCounts.posts" :max="99" class="badge" />
        </el-menu-item>
        <el-menu-item index="rescues">
          <el-icon><Help /></el-icon>
          <span>救助管理</span>
          <el-badge :value="pendingCounts.rescues" :max="99" class="badge" />
        </el-menu-item>
        <el-menu-item index="adoptions">
          <el-icon><Document /></el-icon>
          <span>领养审核</span>
          <el-badge :value="pendingCounts.adoptions" :max="99" class="badge" />
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 内容区域 -->
    <div class="content-area">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 猫咪审核 -->
      <div v-else-if="activeTab === 'cats'" class="tab-content">
        <el-card class="content-card">
          <!-- 猫咪审核子标签页 -->
          <el-tabs v-model="catSubTab" class="cat-sub-tabs">
            <!-- 待审核猫咪 -->
            <el-tab-pane label="待审核" name="pending">
              <div v-if="pendingCats.length === 0" class="empty-state">
                <el-empty description="暂无待审核猫咪" />
              </div>
              <div v-else class="cats-section">
                <el-table :data="pendingCats" style="width: 100%" v-loading="loading" :empty-text="catEmptyText" @row-click="handleCatRowClick">
                  <el-table-column type="index" label="序号" width="100" align="center" />
                  <el-table-column label="猫咪信息" width="120" align="center">
                    <template #default="{ row }">
                      <div class="cat-info">
                        <el-avatar :size="40" :src="getCatImage(row)" />
                        <div class="cat-details">
                          <div class="cat-name">{{ row.name || '未知猫咪' }}</div>
                          <div class="cat-breed">{{ row.breed || '未知品种' }}</div>
                        </div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="年龄" width="120" align="center">
                    <template #default="{ row }">
                      <div class="cat-age">{{ row.age || '未知' }}个月</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="性别" width="120" align="center">
                    <template #default="{ row }">
                      <div class="cat-gender">{{ row.gender || '未知' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="体重" width="120" align="center">
                    <template #default="{ row }">
                      <div class="cat-weight">{{ row.weight || '未知' }}kg</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="健康状况" width="150" align="center">
                    <template #default="{ row }">
                      <el-tag :type="getHealthType(row.healthStatus)" size="small">
                        {{ getHealthText(row.healthStatus) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="状态" width="130" align="center">
                    <template #default="{ row }">
                      <el-tag :type="getStatusType(row.status)" size="small">
                        {{ getStatusText(row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="位置" width="200" align="center">
                    <template #default="{ row }">
                      <div class="cat-location">{{ row.location || '未知' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="性格" width="250" align="center">
                    <template #default="{ row }">
                      <div class="cat-personality">{{ row.personality || '未知' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="180" align="center" fixed="right">
                    <template #default="{ row }">
                      <div class="action-buttons">
                        <el-button size="small" @click="viewCatDetail(row)">查看详情</el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
                
                <!-- 待审核猫咪分页 -->
                <div class="pagination-section">
                  <el-pagination
                    v-model:current-page="pendingCatPagination.currentPage"
                    v-model:page-size="pendingCatPagination.pageSize"
                    :total="pendingCatPagination.total"
                    :page-sizes="[10, 20, 50]"
                    layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handlePendingCatSizeChange"
                    @current-change="handlePendingCatCurrentChange"
                  />
                </div>
              </div>
            </el-tab-pane>
            
            <!-- 已通过猫咪 -->
            <el-tab-pane label="已通过" name="approved">
              <div v-if="approvedCats.length === 0" class="empty-state">
                <el-empty description="暂无已通过猫咪" />
              </div>
              <div v-else class="cats-section">
                <el-table :data="approvedCats" style="width: 100%" v-loading="loading" :empty-text="catEmptyText" @row-click="handleCatRowClick">
                  <el-table-column type="index" label="序号" width="100" align="center" />
                  <el-table-column label="猫咪信息" width="110" align="center">
                    <template #default="{ row }">
                      <div class="cat-info">
                        <el-avatar :size="40" :src="getCatImage(row)" />
                        <div class="cat-details">
                          <div class="cat-name">{{ row.name || '未知猫咪' }}</div>
                          <div class="cat-breed">{{ row.breed || '未知品种' }}</div>
                        </div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="年龄" width="120" align="center">
                    <template #default="{ row }">
                      <div class="cat-age">{{ row.age || '未知' }}个月</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="性别" width="100" align="center">
                    <template #default="{ row }">
                      <div class="cat-gender">{{ row.gender || '未知' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="体重" width="100" align="center">
                    <template #default="{ row }">
                      <div class="cat-weight">{{ row.weight || '未知' }}kg</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="健康状况" width="130" align="center">
                    <template #default="{ row }">
                      <el-tag :type="getHealthType(row.healthStatus)" size="small">
                        {{ getHealthText(row.healthStatus) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="状态" width="130" align="center">
                    <template #default="{ row }">
                      <el-tag :type="getStatusType(row.status)" size="small">
                        {{ getStatusText(row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="位置" width="150" align="center">
                    <template #default="{ row }">
                      <div class="cat-location">{{ row.location || '未知' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="性格" width="250" align="center">
                    <template #default="{ row }">
                      <div class="cat-personality">{{ row.personality || '未知' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="通过时间" width="120" align="center">
                    <template #default="{ row }">
                      <div class="approve-time">{{ formatTime(row.auditTime) }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="180" align="center" fixed="right">
                    <template #default="{ row }">
                      <div class="action-buttons">
                        <el-button size="small" @click="viewCatDetail(row)">查看详情</el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
                
                <!-- 已通过猫咪分页 -->
                <div class="pagination-section">
                  <el-pagination
                    v-model:current-page="approvedCatPagination.currentPage"
                    v-model:page-size="approvedCatPagination.pageSize"
                    :total="approvedCatPagination.total"
                    :page-sizes="[10, 20, 50]"
                    layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handleApprovedCatSizeChange"
                    @current-change="handleApprovedCatCurrentChange"
                  />
                </div>
              </div>
            </el-tab-pane>
            
            <!-- 已拒绝猫咪 -->
            <el-tab-pane label="已拒绝" name="rejected">
              <div v-if="rejectedCats.length === 0" class="empty-state">
                <el-empty description="暂无已拒绝猫咪" />
              </div>
              <div v-else class="cats-section">
                <el-table :data="rejectedCats" style="width: 100%" v-loading="loading" :empty-text="catEmptyText" @row-click="handleCatRowClick">
                  <el-table-column type="index" label="序号" width="80" align="center" />
                  <el-table-column label="猫咪信息" width="110" align="center">
                    <template #default="{ row }">
                      <div class="cat-info">
                        <el-avatar :size="40" :src="getCatImage(row)" />
                        <div class="cat-details">
                          <div class="cat-name">{{ row.name || '未知猫咪' }}</div>
                          <div class="cat-breed">{{ row.breed || '未知品种' }}</div>
                        </div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="年龄" width="100" align="center">
                    <template #default="{ row }">
                      <div class="cat-age">{{ row.age || '未知' }}个月</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="性别" width="80" align="center">
                    <template #default="{ row }">
                      <div class="cat-gender">{{ row.gender || '未知' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="体重" width="80" align="center">
                    <template #default="{ row }">
                      <div class="cat-weight">{{ row.weight || '未知' }}kg</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="健康状况" width="110" align="center"> 
                    <template #default="{ row }">
                      <el-tag :type="getHealthType(row.healthStatus)" size="small">
                        {{ getHealthText(row.healthStatus) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="状态" width="130" align="center"> 
                    <template #default="{ row }">
                      <el-tag :type="getStatusType(row.status)" size="small">
                        {{ getStatusText(row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="位置" width="150" align="center">
                    <template #default="{ row }">
                      <div class="cat-location">{{ row.location || '未知' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="性格" width="200" align="center">
                    <template #default="{ row }">
                      <div class="cat-personality">{{ row.personality || '未知' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="拒绝理由" width="160" align="center">
                    <template #default="{ row }">
                      <div class="reject-reason">{{ row.auditRemark || '无理由' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="拒绝时间" width="150" align="center">
                    <template #default="{ row }">
                      <div class="reject-time">{{ formatTime(row.auditTime) }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="150" align="center" fixed="right">
                    <template #default="{ row }">
                      <div class="action-buttons">
                        <el-button size="small" @click="viewCatDetail(row)">查看详情</el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
                
                <!-- 已拒绝猫咪分页 -->
                <div class="pagination-section">
                  <el-pagination
                    v-model:current-page="rejectedCatPagination.currentPage"
                    v-model:page-size="rejectedCatPagination.pageSize"
                    :total="rejectedCatPagination.total"
                    :page-sizes="[10, 20, 50]"
                    layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handleRejectedCatSizeChange"
                    @current-change="handleRejectedCatCurrentChange"
                  />
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>

      <!-- 帖子审核 -->
      <div v-else-if="activeTab === 'posts'" class="tab-content">
        <el-card class="content-card">
          <!-- 帖子审核子标签页 -->
          <el-tabs v-model="postSubTab" class="post-sub-tabs">
            <!-- 待审核帖子 -->
            <el-tab-pane label="待审核" name="pending">
              <div v-if="pendingPosts.length === 0" class="empty-state">
                <el-empty description="暂无待审核帖子" />
              </div>
              <div v-else class="posts-section">
                <el-table :data="pendingPosts" style="width: 100%" v-loading="loading" :empty-text="postEmptyText" @row-click="handleRowClick">
                  <el-table-column type="index" label="序号" width="80" align="center" />
                  <el-table-column label="发帖用户" width="150" align="center">
                    <template #default="{ row }">
                      <div class="author-info">
                        <div class="author-name">{{ row.authorUsername || `用户${row.authorId}` || '未知用户' }}</div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="用户ID" width="120" align="center">
                    <template #default="{ row }">
                      <div>{{ row.authorId || '未知' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="帖子类型" width="120" align="center">
                    <template #default="{ row }">
                      <el-tag v-if="row.category" size="small" :type="getPostTypeTag(row.category)">{{ getPostTypeText(row.category) }}</el-tag>
                      <span v-else class="no-type">未知类型</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="帖子标题" width="250" align="center">
                    <template #default="{ row }">
                      <div class="post-title text-center">{{ row.title || '未知标题' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="帖子内容" width="350" align="center">
                    <template #default="{ row }">
                      <div class="post-content">{{ (row.content || '暂无内容').substring(0, 100) }}{{ (row.content && row.content.length > 100) ? '...' : '' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="提交时间" width="180" align="center">
                    <template #default="{ row }">
                      <div class="submit-time">{{ formatTime(row.createTime) }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="220" align="center" fixed="right">
                    <template #default="{ row }">
                      <div class="action-buttons">
                        <el-button size="small" @click="viewPostDetail(row)">查看详情</el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
                
                <!-- 待审核帖子分页 -->
                <div class="pagination-section">
                  <el-pagination
                    v-model:current-page="pendingPagination.currentPage"
                    v-model:page-size="pendingPagination.pageSize"
                    :total="pendingPagination.total"
                    :page-sizes="[10, 20, 50]"
                    layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handlePendingSizeChange"
                    @current-change="handlePendingCurrentChange"
                  />
                </div>
              </div>
            </el-tab-pane>
            
            <!-- 已通过帖子 -->
            <el-tab-pane label="已通过" name="approved">
              <div v-if="approvedPosts.length === 0" class="empty-state">
                <el-empty description="暂无已通过帖子" />
              </div>
              <div v-else class="posts-section">
                <el-table :data="approvedPosts" style="width: 100%" v-loading="loading" :empty-text="postEmptyText" @row-click="handleRowClick">
                  <el-table-column type="index" label="序号" width="80" align="center" />
                  <el-table-column label="发帖用户" width="150" align="center">
                    <template #default="{ row }">
                      <div class="author-info">
                        <div class="author-name">{{ row.authorUsername || `用户${row.authorId}` || '未知用户' }}</div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="用户ID" width="120" align="center">
                    <template #default="{ row }">
                      <div>{{ row.authorId || '未知' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="帖子类型" width="120" align="center">
                    <template #default="{ row }">
                      <el-tag v-if="row.category" size="small" :type="getPostTypeTag(row.category)">{{ getPostTypeText(row.category) }}</el-tag>
                      <span v-else class="no-type">未知类型</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="帖子标题" width="250" align="center">
                    <template #default="{ row }">
                      <div class="post-title text-center">{{ row.title || '未知标题' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="帖子内容" width="350" align="center">
                    <template #default="{ row }">
                      <div class="post-content">{{ (row.content || '暂无内容').substring(0, 100) }}{{ (row.content && row.content.length > 100) ? '...' : '' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="审核时间" width="180" align="center">
                    <template #default="{ row }">
                      <div class="submit-time">{{ formatTime(row.auditTime) }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="220" align="center" fixed="right">
                    <template #default="{ row }">
                      <div class="action-buttons">
                        <el-button size="small" @click="viewPostDetail(row)">查看详情</el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
                
                <!-- 已通过帖子分页 -->
                <div class="pagination-section">
                  <el-pagination
                    v-model:current-page="approvedPagination.currentPage"
                    v-model:page-size="approvedPagination.pageSize"
                    :total="approvedPagination.total"
                    :page-sizes="[10, 20, 50]"
                    layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handleApprovedSizeChange"
                    @current-change="handleApprovedCurrentChange"
                  />
                </div>
              </div>
            </el-tab-pane>
            
            <!-- 已拒绝帖子 -->
            <el-tab-pane label="已拒绝" name="rejected">
              <div v-if="rejectedPosts.length === 0" class="empty-state">
                <el-empty description="暂无已拒绝帖子" />
              </div>
              <div v-else class="posts-section">
                <el-table :data="rejectedPosts" style="width: 100%" v-loading="loading" :empty-text="postEmptyText" @row-click="handleRowClick">
                  <el-table-column type="index" label="序号" width="60" align="center" />
                  <el-table-column label="发帖用户" width="140" align="center">
                    <template #default="{ row }">
                      <div class="author-info">
                        <div class="author-name">{{ row.authorUsername || `用户${row.authorId}` || '未知用户' }}</div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="用户ID" width="120" align="center">
                    <template #default="{ row }">
                      <div>{{ row.authorId || '未知' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="帖子类型" width="80" align="center">
                    <template #default="{ row }">
                      <el-tag v-if="row.category" size="small" :type="getPostTypeTag(row.category)">{{ getPostTypeText(row.category) }}</el-tag>
                      <span v-else class="no-type">未知类型</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="帖子标题" width="250" align="center">
                    <template #default="{ row }">
                      <div class="post-title text-center">{{ row.title || '未知标题' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="帖子内容" width="300" align="center">
                    <template #default="{ row }">
                      <div class="post-content">{{ (row.content || '暂无内容').substring(0, 100) }}{{ (row.content && row.content.length > 100) ? '...' : '' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="拒绝理由" width="150" align="center">
                    <template #default="{ row }">
                      <div class="post-content">{{ row.auditRemark || '无理由' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="拒绝时间" width="180" align="center">
                    <template #default="{ row }">
                      <div class="submit-time">{{ formatTime(row.auditTime) }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="220" align="center" fixed="right">
                    <template #default="{ row }">
                      <div class="action-buttons">
                        <el-button size="small" @click="viewPostDetail(row)">查看详情</el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
                
                <!-- 已拒绝帖子分页 -->
                <div class="pagination-section">
                  <el-pagination
                    v-model:current-page="rejectedPagination.currentPage"
                    v-model:page-size="rejectedPagination.pageSize"
                    :total="rejectedPagination.total"
                    :page-sizes="[10, 20, 50]"
                    layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handleRejectedSizeChange"
                    @current-change="handleRejectedCurrentChange"
                  />
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>

      <!-- 救助管理 -->
      <div v-else-if="activeTab === 'rescues'" class="tab-content">
        <el-card class="content-card">
          
          <!-- 筛选和搜索 -->
          <div class="filters-section">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-input
                  v-model="rescueSearchKeyword"
                  placeholder="搜索救助位置或描述"
                  prefix-icon="Search"
                  clearable
                />
              </el-col>
              <el-col :span="4">
                <el-select v-model="rescueFilterStatus" placeholder="状态筛选" clearable>
                  <el-option label="待处理" value="待处理" />
                  <el-option label="进行中" value="进行中" />
                  <el-option label="已完成" value="已完成" />
                </el-select>
              </el-col>
              <el-col :span="4">
                <el-select v-model="rescueFilterUrgency" placeholder="紧急程度筛选" clearable>
                  <el-option label="低" value="低" />
                  <el-option label="中" value="中" />
                  <el-option label="高" value="高" />
                  <el-option label="紧急" value="紧急" />
                </el-select>
              </el-col>
              <el-col :span="4">
                <el-button type="primary" @click="loadPendingRescues">
                  <el-icon><Search /></el-icon>
                  搜索
                </el-button>
                <el-button @click="resetRescueFilters">
                  <el-icon><Refresh /></el-icon>
                  重置
                </el-button>
              </el-col>
            </el-row>
          </div>

          <!-- 救助需求列表 -->
          <div class="rescues-section">
            <el-table :data="filteredRescues" style="width: 100%" v-loading="loading" @row-click="handleRescueRowClick" :empty-text="rescueEmptyText">
              <el-table-column type="index" label="序号" width="80" align="center" />
              <el-table-column label="任务信息" width="300" align="center">
                <template #default="{ row }">
                  <div class="rescue-info">
                    <div class="rescue-title">
                      <span class="title-text">{{ row.title || '救助任务' }}</span>
                      <el-tag v-if="row.urgencyLevel === 'HIGH'" size="small" type="danger">紧急</el-tag>
                      <el-tag v-if="row.urgencyLevel === 'CRITICAL'" size="small" type="danger">非常紧急</el-tag>
                    </div>
                    <div class="rescue-description">{{ row.description || '暂无描述' }}</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="位置信息" width="180" align="center">
                <template #default="{ row }">
                  <div class="location-info">
                    <el-icon><Location /></el-icon>
                    <span>{{ row.location || '未知位置' }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="reporterUsername" label="报告人" width="120" align="center" />
              <el-table-column prop="contactPhone" label="联系电话" width="140" align="center" />
              <el-table-column label="状态" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="getRescueStatusType(row.status)">
                    {{ getRescueStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="报告时间" width="160" align="center">
                <template #default="{ row }">{{ formatRescueDate(row.createTime) }}</template>
              </el-table-column>
              <el-table-column prop="volunteerName" label="志愿者" width="120" align="center">
                <template #default="{ row }">
                  {{ row.volunteerName || '待定' }}
                </template>
              </el-table-column>
              <el-table-column prop="volunteerPhone" label="志愿者电话" width="140" align="center">
                <template #default="{ row }">
                  {{ row.volunteerPhone || '待定' }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right" align="center">
                <template #default="{ row }">
                  <div style="position: relative; display: inline-block;">
                    <el-button size="small" @click="viewRescueDetail(row)" :loading="loading">查看详情</el-button>
                    <div 
                      v-if="row.status === '待处理'" 
                      class="status-dot"
                      style="position: absolute; top: -1px; right: -5px; width: 13px; height: 13px; background-color: #f56c6c; border-radius: 50%; border: 2px solid #fff; z-index: 10;"
                    ></div>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <!-- 救助管理分页 -->
          <div class="pagination-section">
            <el-pagination
              v-model:current-page="rescueCurrentPage"
              v-model:page-size="rescuePageSize"
              :total="filteredRescueTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleRescueSizeChange"
              @current-change="handleRescueCurrentChange"
            />
          </div>
        </el-card>
      </div>

      <!-- 领养审核 -->
      <div v-else-if="activeTab === 'adoptions'" class="tab-content">
        <el-card class="content-card">
          <!-- 领养审核子标签页 -->
          <el-tabs v-model="adoptionSubTab" class="adoption-sub-tabs">
            <!-- 待审核领养 -->
            <el-tab-pane label="待审核" name="pending">
              <div v-if="pendingAdoptions.length === 0" class="empty-state">
                <el-empty description="暂无待审核领养申请" />
              </div>
              <div v-else class="adoptions-section">
                <el-table :data="pendingAdoptions" style="width: 100%" v-loading="loading" :empty-text="adoptionEmptyText" @row-click="viewAdoptionDetail">
                  <el-table-column type="index" label="序号" width="80" align="center" />
                  <el-table-column label="申请用户" width="150" align="center">
                    <template #default="{ row }">
                      <div class="applicant-name">{{ row.applicant || '未知用户' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="用户ID" width="120" align="center">
                    <template #default="{ row }">
                      <div>{{ row.userId || '未知' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="申请猫咪" width="140" align="center">
                    <template #default="{ row }">
                      <div class="cat-name">{{ row.catName || '未知猫咪' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="猫咪ID" width="120" align="center">
                    <template #default="{ row }">
                      <div class="cat-id">{{ row.catId || '未知' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="申请理由" width="450" align="center">
                    <template #default="{ row }">
                      <div class="reason-text">{{ (row.reason || '暂无理由').substring(0, 50) }}{{ (row.reason && row.reason.length > 50) ? '...' : '' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="申请时间" width="200" align="center">
                    <template #default="{ row }">
                      <div class="apply-time">{{ formatTime(row.createTime) }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="240" align="center" fixed="right">
                    <template #default="{ row }">
                      <div class="action-buttons">
                        <el-button size="small" @click="viewAdoptionDetail(row)">查看详情</el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
                
                <!-- 待审核领养分页 -->
                <div class="pagination-section">
                  <el-pagination
                    v-model:current-page="pendingAdoptionPagination.currentPage"
                    v-model:page-size="pendingAdoptionPagination.pageSize"
                    :total="pendingAdoptionPagination.total"
                    :page-sizes="[10, 20, 50]"
                    layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handlePendingAdoptionSizeChange"
                    @current-change="handlePendingAdoptionCurrentChange"
                  />
                </div>
              </div>
            </el-tab-pane>
            
            <!-- 已通过领养 -->
            <el-tab-pane label="已通过" name="approved">
              <div v-if="approvedAdoptions.length === 0" class="empty-state">
                <el-empty description="暂无已通过领养申请" />
              </div>
              <div v-else class="adoptions-section">
                <el-table :data="approvedAdoptions" style="width: 100%" v-loading="loading" :empty-text="adoptionEmptyText" @row-click="viewAdoptionDetail">
                  <el-table-column type="index" label="序号" width="80" align="center" />
                  <el-table-column label="申请用户" width="150" align="center">
                    <template #default="{ row }">
                      <div class="applicant-name">{{ row.applicant || '未知用户' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="用户ID" width="120" align="center">
                    <template #default="{ row }">
                      <div>{{ row.userId || '未知' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="申请猫咪" width="140" align="center">
                    <template #default="{ row }">
                      <div class="cat-name">{{ row.catName || '未知猫咪' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="猫咪ID" width="120" align="center">
                    <template #default="{ row }">
                      <div class="cat-id">{{ row.catId || '未知' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="申请理由" width="450" align="center">
                    <template #default="{ row }">
                      <div class="reason-text">{{ (row.reason || '暂无理由').substring(0, 50) }}{{ (row.reason && row.reason.length > 50) ? '...' : '' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="通过时间" width="200" align="center">
                    <template #default="{ row }">
                      <div class="approve-time">{{ formatTime(row.approveTime) }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="240" align="center" fixed="right">
                    <template #default="{ row }">
                      <div class="action-buttons">
                        <el-button size="small" @click="viewAdoptionDetail(row)">查看详情</el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
                
                <!-- 已通过领养分页 -->
                <div class="pagination-section">
                  <el-pagination
                    v-model:current-page="approvedAdoptionPagination.currentPage"
                    v-model:page-size="approvedAdoptionPagination.pageSize"
                    :total="approvedAdoptionPagination.total"
                    :page-sizes="[10, 20, 50]"
                    layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handleApprovedAdoptionSizeChange"
                    @current-change="handleApprovedAdoptionCurrentChange"
                  />
                </div>
              </div>
            </el-tab-pane>
            
            <!-- 已拒绝领养 -->
            <el-tab-pane label="已拒绝" name="rejected">
              <div v-if="rejectedAdoptions.length === 0" class="empty-state">
                <el-empty description="暂无已拒绝领养申请" />
              </div>
              <div v-else class="adoptions-section">
                <el-table :data="rejectedAdoptions" style="width: 100%" v-loading="loading" :empty-text="adoptionEmptyText" @row-click="viewAdoptionDetail">
                  <el-table-column type="index" label="序号" width="80" align="center" />
                  <el-table-column label="申请用户" width="150" align="center">
                    <template #default="{ row }">
                      <div class="applicant-name">{{ row.applicant || '未知用户' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="用户ID" width="120" align="center">
                    <template #default="{ row }">
                      <div>{{ row.userId || '未知' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="申请猫咪" width="140" align="center">
                    <template #default="{ row }">
                      <div class="cat-name">{{ row.catName || '未知猫咪' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="猫咪ID" width="120" align="center">
                    <template #default="{ row }">
                      <div class="cat-id">{{ row.catId || '未知' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="拒绝理由" width="450" align="center">
                    <template #default="{ row }">
                      <div class="reject-reason">{{ row.rejectReason || '无理由' }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="拒绝时间" width="200" align="center">
                    <template #default="{ row }">
                      <div class="reject-time">{{ formatTime(row.rejectTime) }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="240" align="center" fixed="right">
                    <template #default="{ row }">
                      <div class="action-buttons">
                        <el-button size="small" @click="viewAdoptionDetail(row)">查看详情</el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
                
                <!-- 已拒绝领养分页 -->
                <div class="pagination-section">
                  <el-pagination
                    v-model:current-page="rejectedAdoptionPagination.currentPage"
                    v-model:page-size="rejectedAdoptionPagination.pageSize"
                    :total="rejectedAdoptionPagination.total"
                    :page-sizes="[10, 20, 50]"
                    layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handleRejectedAdoptionSizeChange"
                    @current-change="handleRejectedAdoptionCurrentChange"
                  />
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>
    </div>

    <!-- 救助需求详情对话框 -->
    <RescueDetailDialog
      v-model:visible="showRescueDetail"
      :rescue-data="selectedRescue"
      @take-rescue="handleTakeRescue"
      @complete-rescue="handleCompleteRescue"
      @close-rescue="handleCloseRescue"
    />

    <!-- 领养申请详情对话框 -->
    <el-dialog
      v-model="showAdoptionDetail"
      title="领养申请详情"
      width="800px"
      :before-close="handleCloseAdoptionDetail"
    >
      <div v-if="selectedAdoption" class="adoption-detail">
        <!-- 猫咪信息 -->
        <div class="detail-section">
          <h3>
            猫咪信息
          </h3>
          <div class="cat-info-grid">
            <!-- 上半部分：三列布局 -->
            <div class="cat-info-top">
              <!-- 第一列：图片 -->
              <div class="cat-image-section">
                <el-image
                  v-if="getCatImages(selectedAdoption.catImage).length > 0"
                  :src="getCatImagePath(getCatImages(selectedAdoption.catImage)[0]) || '/img/placeholder-cat.jpg'"
                  :preview-src-list="getCatImages(selectedAdoption.catImage).map(img => getCatImagePath(img))"
                  :preview-teleported="true"
                  :hide-on-click-modal="true"
                  :z-index="9999"
                  fit="cover"
                  style="width: 200px; height: 150px; border-radius: 8px;"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                      <span>图片加载失败</span>
                    </div>
                  </template>
                </el-image>
                <span v-else class="no-image">暂无图片</span>
              </div>
              
              <!-- 第二列：姓名、年龄、体重 -->
              <div class="cat-info-column">
                <div class="detail-row">
                  <span class="detail-label">猫咪姓名：</span>
                  <span class="detail-value">{{ selectedAdoption.catName || '未知' }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">年龄：</span>
                  <span class="detail-value">{{ selectedAdoption.catAge ? selectedAdoption.catAge + '月' : '未知' }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">体重：</span>
                  <span class="detail-value">{{ selectedAdoption.catWeight ? selectedAdoption.catWeight + 'kg' : '未知' }}</span>
                </div>
              </div>
              
              <!-- 第三列：ID、性别、健康状态 -->
              <div class="cat-info-column">
                <div class="detail-row">
                  <span class="detail-label">猫咪ID：</span>
                  <span class="detail-value">{{ selectedAdoption.catId || '未知' }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">性别：</span>
                  <span class="detail-value">{{ selectedAdoption.catGender || '未知' }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">健康状态：</span>
                  <span class="detail-value">{{ selectedAdoption.catHealth || '未知' }}</span>
                </div>
              </div>
            </div>
            
            <!-- 下半部分：两行布局 -->
            <div class="cat-info-bottom">
              <!-- 第一行：位置和医疗信息 -->
              <div class="cat-info-row">
                <div class="detail-row full-width">
                  <span class="detail-label">位置：</span>
                  <span class="detail-value">{{ selectedAdoption.catLocation || '未知' }}</span>
                </div>
                <div class="detail-row full-width">
                  <span class="detail-label">医疗信息：</span>
                  <span class="detail-value">{{ selectedAdoption.catMedical || '无' }}</span>
                </div>
              </div>
              
              <!-- 第二行：性格和描述 -->
              <div class="cat-info-row">
                <div class="detail-row full-width">
                  <span class="detail-label">性格特点：</span>
                  <span class="detail-value">{{ selectedAdoption.catPersonality || '未知' }}</span>
                </div>
                <div class="detail-row full-width">
                  <span class="detail-label">猫咪描述：</span>
                  <span class="detail-value">{{ selectedAdoption.catDescription || '暂无描述' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 申请人信息 -->
        <div class="detail-section">
          <h3>
            申请人信息
          </h3>
          <div class="applicant-info-grid">
            <div class="detail-row">
              <span class="detail-label">姓名：</span>
              <span class="detail-value">{{ selectedAdoption.applicant || '未知用户' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">用户ID：</span>
              <span class="detail-value">{{ selectedAdoption.userId || '未知' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">身份证号：</span>
              <span class="detail-value">{{ selectedAdoption.idCard || '未填写' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">联系电话：</span>
              <span class="detail-value">{{ selectedAdoption.phone || '未填写' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">家庭住址：</span>
              <span class="detail-value">{{ selectedAdoption.address || '未填写' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">申请理由：</span>
              <span class="detail-value reason-text">{{ selectedAdoption.reason || selectedAdoption.applicationReason || '未填写申请理由' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">居住环境：</span>
              <span class="detail-value">{{ selectedAdoption.livingCondition || '未填写' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">养猫经验：</span>
              <span class="detail-value">{{ selectedAdoption.experience || '无经验' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">家庭成员：</span>
              <span class="detail-value">{{ selectedAdoption.familyMembers || '未填写' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">经济状况：</span>
              <span class="detail-value">{{ selectedAdoption.financialStatus || '未填写' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">工作时间：</span>
              <span class="detail-value">{{ selectedAdoption.workSchedule || '未填写' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">其他说明：</span>
              <span class="detail-value">{{ selectedAdoption.additionalInfo || '无' }}</span>
            </div>
          </div>
        </div>

        <!-- 审核信息（已审核状态显示） -->
        <div v-if="selectedAdoption && selectedAdoption.status" class="detail-section">
          <h3>审核信息</h3>
          <div class="review-info-grid">
            <div class="detail-row">
              <span class="detail-label">审核状态：</span>
              <span class="detail-value">
                <el-tag :type="selectedAdoption.status === 'PENDING' ? 'warning' : selectedAdoption.status === 'APPROVED' ? 'success' : 'danger'">
                  {{ selectedAdoption.status === 'PENDING' ? '待审核' : selectedAdoption.status === 'APPROVED' ? '已通过' : '已拒绝' }}
                </el-tag>
              </span>
            </div>
            <div v-if="selectedAdoption.status !== 'PENDING'" class="detail-row">
              <span class="detail-label">{{ selectedAdoption.status === 'APPROVED' ? '通过时间：' : '拒绝时间：' }}</span>
              <span class="detail-value">{{ formatTime(selectedAdoption.processTime || selectedAdoption.approveTime) || '未知' }}</span>
            </div>
            <div v-if="selectedAdoption.status === 'REJECTED'" class="detail-row full-width">
              <span class="detail-label">拒绝理由：</span>
              <span class="detail-value">{{ selectedAdoption.reviewComment || '无理由' }}</span>
            </div>
            <div v-if="selectedAdoption.status === 'PENDING'" class="detail-row full-width">
              <span class="detail-label">申请时间：</span>
              <span class="detail-value">{{ formatTime(selectedAdoption.time || selectedAdoption.createTime) || '未知' }}</span>
            </div>
          </div>
        </div>

        <!-- 审核操作（仅待审核状态显示） -->
        <div v-if="adoptionSubTab === 'pending'" class="detail-section">
          <h3>
            审核操作
          </h3>
          <div class="action-buttons">
            <el-button type="success" size="large" @click="handleAdoptionApprove(selectedAdoption.id)">
              <el-icon><Check /></el-icon>通过申请
            </el-button>
            <el-button type="danger" size="large" plain @click="showRejectDialog = true">
              <el-icon><Close /></el-icon>拒绝申请
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 拒绝理由对话框 -->
    <el-dialog
      v-model="showRejectDialog"
      title="拒绝领养申请"
      width="500px"
      :before-close="handleCloseRejectDialog"
    >
      <div class="reject-dialog">
        <el-form :model="rejectForm" label-width="80px">
          <el-form-item label="拒绝理由">
            <el-select v-model="rejectForm.reason" placeholder="请选择拒绝理由" style="width: 100%">
              <el-option label="居住环境不适合养猫" value="居住环境不适合养猫" />
              <el-option label="养猫经验不足" value="养猫经验不足" />
              <el-option label="经济状况不稳定" value="经济状况不稳定" />
              <el-option label="家庭成员不同意" value="家庭成员不同意" />
              <el-option label="工作时间过长" value="工作时间过长" />
              <el-option label="其他原因" value="其他原因" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="rejectForm.reason === '其他原因'" label="详细说明">
            <el-input
              v-model="rejectForm.customReason"
              type="textarea"
              :rows="3"
              placeholder="请输入详细的拒绝理由"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
        <div class="dialog-footer">
          <el-button @click="handleCloseRejectDialog">取消</el-button>
          <el-button type="danger" @click="handleAdoptionReject">确认拒绝</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 猫咪详情对话框 -->
    <el-dialog
      v-model="showCatDetail"
      title="猫咪信息详情"
      width="800px"
      :before-close="handleCloseCatDetail"
    >
      <div v-if="selectedCat" class="cat-detail">
        <!-- 猫咪图片 -->
        <div class="detail-section">
          <h3>猫咪图片</h3>
          <div class="cat-images">
            <el-image
              v-if="getCatImages(selectedCat.images).length > 0"
              :src="getCatImagePath(getCatImages(selectedCat.images)[0]) || '/img/placeholder-cat.jpg'"
              :preview-src-list="getCatImages(selectedCat.images).map(img => getCatImagePath(img))"
              :preview-teleported="true"
              :hide-on-click-modal="true"
              :z-index="9999"
              fit="cover"
              style="width: 300px; height: 200px; border-radius: 8px;"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>图片加载失败</span>
                </div>
              </template>
            </el-image>
            <span v-else class="no-image">暂无图片</span>
          </div>
        </div>

        <!-- 猫咪基本信息 -->
        <div class="detail-section">
          <h3>猫咪基本信息</h3>
          <div class="cat-info-grid">
            <div class="detail-row">
              <span class="detail-label">猫咪姓名：</span>
              <span class="detail-value">{{ selectedCat.name || '未知' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">年龄：</span>
              <span class="detail-value">{{ selectedCat.age || '未知' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">性别：</span>
              <span class="detail-value">{{ selectedCat.gender || '未知' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">体重：</span>
              <span class="detail-value">{{ selectedCat.weight || '未知' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">健康状态：</span>
              <span class="detail-value">{{ selectedCat.healthStatus || '未知' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">状态：</span>
              <span class="detail-value">{{ selectedCat.status || '未知' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">位置：</span>
              <span class="detail-value">{{ selectedCat.location || '未知' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">医疗信息：</span>
              <span class="detail-value">{{ selectedCat.medicalInfo || '无' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">猫咪描述：</span>
              <span class="detail-value">{{ selectedCat.description || '暂无描述' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">性格特点：</span>
              <span class="detail-value">{{ selectedCat.personality || '未知' }}</span>
            </div>
          </div>
        </div>

        <!-- 审核信息（已审核状态显示） -->
        <div v-if="selectedCat.auditStatus && selectedCat.auditStatus !== 'PENDING'" class="detail-section">
          <h3>审核信息</h3>
          <div class="cat-info-grid">
            <div class="detail-row">
              <span class="detail-label">审核状态：</span>
              <span class="detail-value">
                <el-tag :type="selectedCat.auditStatus === 'APPROVED' ? 'success' : 'danger'">
                  {{ selectedCat.auditStatus === 'APPROVED' ? '已通过' : '已拒绝' }}
                </el-tag>
              </span>
            </div>
            <div class="detail-row">
              <span class="detail-label">{{ selectedCat.auditStatus === 'APPROVED' ? '通过时间：' : '拒绝时间：' }}</span>
              <span class="detail-value">{{ formatTime(selectedCat.auditTime) || '未知' }}</span>
            </div>
            <div v-if="selectedCat.auditStatus === 'REJECTED'" class="detail-row">
              <span class="detail-label">拒绝理由：</span>
              <span class="detail-value">{{ selectedCat.auditRemark || '无理由' }}</span>
            </div>
          </div>
        </div>

        <!-- 审核操作（仅待审核状态显示） -->
        <div v-if="catSubTab === 'pending'" class="detail-section">
          <h3>审核操作</h3>
          <div class="action-buttons">
            <el-button type="success" size="large" @click="handleCatApprove(selectedCat.id)">
              <el-icon><Check /></el-icon>通过审核
            </el-button>
            <el-button type="danger" size="large" plain @click="showCatRejectDialog = true">
              <el-icon><Close /></el-icon>拒绝审核
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 猫咪拒绝理由对话框 -->
    <el-dialog
      v-model="showCatRejectDialog"
      title="拒绝猫咪审核"
      width="500px"
      :before-close="handleCloseCatRejectDialog"
    >
      <div class="reject-dialog">
        <el-form :model="catRejectForm" label-width="80px">
          <el-form-item label="拒绝理由">
            <el-select v-model="catRejectForm.reason" placeholder="请选择拒绝理由" style="width: 100%">
              <el-option label="信息不完整" value="信息不完整" />
              <el-option label="图片质量差" value="图片质量差" />
              <el-option label="信息不真实" value="信息不真实" />
              <el-option label="重复提交" value="重复提交" />
              <el-option label="不符合平台要求" value="不符合平台要求" />
              <el-option label="其他原因" value="其他原因" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="catRejectForm.reason === '其他原因'" label="详细说明">
            <el-input
              v-model="catRejectForm.customReason"
              type="textarea"
              :rows="3"
              placeholder="请输入详细的拒绝理由"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
        <div class="dialog-footer">
          <el-button @click="handleCloseCatRejectDialog">取消</el-button>
          <el-button type="danger" @click="handleCatReject">确认拒绝</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 帖子审核拒绝对话框 -->
    <el-dialog
      v-model="showPostRejectDialog"
      title="拒绝帖子审核"
      width="500px"
      :before-close="handleClosePostRejectDialog"
    >
      <div class="reject-dialog">
        <el-form :model="postRejectForm" label-width="80px">
          <el-form-item label="拒绝理由">
            <el-select v-model="postRejectForm.reason" placeholder="请选择拒绝理由" style="width: 100%">
              <el-option label="内容违规" value="内容违规" />
              <el-option label="垃圾信息" value="垃圾信息" />
              <el-option label="内容不当" value="内容不当" />
              <el-option label="重复发布" value="重复发布" />
              <el-option label="内容质量低" value="内容质量低" />
              <el-option label="其他原因" value="其他原因" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="postRejectForm.reason === '其他原因'" label="详细说明">
            <el-input
              v-model="postRejectForm.customReason"
              type="textarea"
              :rows="3"
              placeholder="请输入详细的拒绝理由"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
        <div class="dialog-footer">
          <el-button @click="handleClosePostRejectDialog">取消</el-button>
          <el-button type="danger" @click="handlePostReject">确认拒绝</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 帖子详情对话框 -->
    <el-dialog v-model="showPostDetailDialog" title="帖子详情" width="1000px"  class="post-detail-dialog">
      <div class="post-detail-container" v-if="currentPost">
        <!-- 帖子标题 -->
        <div class="post-title-section">
          <h1 class="post-title">{{ currentPost.title || '无标题' }}</h1>
        </div>
        
        <!-- 作者信息 -->
        <div class="author-section">
          <div class="author-avatar">
            <el-avatar 
              v-if="currentPost.authorAvatar" 
              :size="48" 
              :src="getImageUrl(currentPost.authorAvatar)"
              class="author-avatar-img"
            >
              <template #error>
                <span class="avatar-fallback">{{ (currentPost.authorUsername || '未知').charAt(0) }}</span>
              </template>
            </el-avatar>
            <span v-else class="avatar-placeholder">{{ (currentPost.authorUsername || '未知').charAt(0) }}</span>
          </div>
          <div class="author-details">
            <div class="author-name">{{ currentPost.authorUsername || '未知用户' }}</div>
            <div class="post-meta">
              <el-tag :type="getPostTypeTag(currentPost.category)" size="small">{{ getPostTypeText(currentPost.category) }}</el-tag>
              <span class="post-time">{{ formatTime(currentPost.createTime) }}</span>
            </div>
          </div>
        </div>
        
        <!-- 帖子内容 -->
        <div class="content-section">
          <h3 class="section-title">帖子内容</h3>
          <div class="content-text">{{ currentPost.content || '无内容' }}</div>
        </div>
        
        <!-- 拒绝理由（仅显示已拒绝的帖子） -->
        <div class="reject-reason-section" v-if="currentPost.status === 'REJECTED'">
          <h3 class="section-title">拒绝理由</h3>
          <div class="reject-reason-text">{{ currentPost.auditRemark || '无拒绝理由' }}</div>
        </div>
        
        <!-- 帖子图片 -->
        <div class="images-section" v-if="getPostImages(currentPost.images).length > 0">
          <h3 class="section-title">帖子图片</h3>
          <div class="post-images">
            <el-image
              v-for="(image, index) in getPostImages(currentPost.images)"
              :key="index"
              :src="getImageUrl(image)"
              :preview-src-list="getPostImages(currentPost.images).map(img => getImageUrl(img))"
              :initial-index="index"
              :preview-teleported="true"
              :hide-on-click-modal="true"
              :z-index="9999"
              fit="cover"
              class="post-image"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>图片加载失败</span>
                </div>
              </template>
            </el-image>
          </div>
        </div>
        
        <!-- 审核状态和操作 -->
        <div class="review-section">
          <div class="review-actions" v-if="currentPost.status === 'PENDING' || currentPost.status === 'PENDING_REVIEW'">
            <el-button type="success" @click="handlePostReview(currentPost.id, 'approve')" :loading="loading" size="large">
              <el-icon><Check /></el-icon>
              通过
            </el-button>
            <el-button type="danger" @click="handlePostReview(currentPost.id, 'reject')" :loading="loading" size="large">
              <el-icon><Close /></el-icon>
              拒绝
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, onUnmounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  House, 
  Star, 
  Document, 
  Help, 
  User, 
  Setting, 
  Search, 
  Refresh, 
  View, 
  Check, 
  Close,
  ChatDotRound
} from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'
import { communityApi } from '@/api/community'
import RescueDetailDialog from '@/components/RescueDetailDialog.vue'
import webSocketService from '@/utils/websocket'

// 时间格式化函数
const formatTime = (timeString) => {
  if (!timeString) return '未知'
  
  try {
    const date = new Date(timeString)
    if (isNaN(date.getTime())) return '未知'
    
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    
    return `${year}-${month}-${day} ${hours}:${minutes}`
  } catch (error) {
    console.error('时间格式化错误:', error)
    return '未知'
  }
}

const activeTab = ref('cats')
const loading = ref(false)
const rescueLoading = ref(false) // 救助管理分页loading状态
const adoptionLoading = ref(false) // 领养审核分页loading状态
const pendingCounts = ref({
  cats: 0,
  posts: 0,
  rescues: 0,
  adoptions: 0
})

// 真实任务数据
const catTasks = ref([])
const postTasks = ref([])
const rescueTasks = ref([])
const adoptionTasks = ref([])

// 帖子审核子标签页相关
const postSubTab = ref('pending')
const pendingPosts = ref([])
const approvedPosts = ref([])
const rejectedPosts = ref([])

// 帖子审核分页相关
const pendingPagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})
const approvedPagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})
const rejectedPagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 领养审核子标签页相关
const adoptionSubTab = ref('pending')
const pendingAdoptions = ref([])
const approvedAdoptions = ref([])
const rejectedAdoptions = ref([])

// 领养审核分页相关
const pendingAdoptionPagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})
const approvedAdoptionPagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})
const rejectedAdoptionPagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 救助管理筛选相关
const rescueSearchKeyword = ref('')
const rescueFilterStatus = ref('')
const rescueFilterUrgency = ref('')

// 救助详情相关
const showRescueDetail = ref(false)
const selectedRescue = ref(null)

// 马上救援相关
const showTakeRescueDialog = ref(false)
const currentRescue = ref(null)
const rescueCurrentPage = ref(1)
const rescuePageSize = ref(10)
const rescueTotal = ref(0)

// 帖子详情相关
const showPostDetailDialog = ref(false)
const currentPost = ref(null)

// 领养审核筛选相关
const adoptionSearchKeyword = ref('')
const adoptionCurrentPage = ref(1)
const adoptionPageSize = ref(10)
const adoptionTotal = ref(0)

// 领养详情对话框相关
const showAdoptionDetail = ref(false)
const selectedAdoption = ref(null)
const showRejectDialog = ref(false)
const rejectForm = ref({
  reason: '',
  customReason: ''
})

// 帖子审核拒绝对话框相关
const showPostRejectDialog = ref(false)
const postRejectForm = ref({
  reason: '',
  customReason: ''
})
const selectedPostForReject = ref(null)

// 猫咪审核子标签页相关
const catSubTab = ref('pending')
const pendingCats = ref([])
const approvedCats = ref([])
const rejectedCats = ref([])

// 猫咪审核分页相关
const pendingCatPagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})
const approvedCatPagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})
const rejectedCatPagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 猫咪详情对话框相关
const showCatDetail = ref(false)
const selectedCat = ref(null)
const showCatRejectDialog = ref(false)
const catRejectForm = ref({
  reason: '',
  customReason: ''
})

// 响应式变量 - 待处理状态的救助数量（用于小红点）
const pendingRescueCount = ref(0)

// 计算待处理救助数量
const calculatePendingRescueCount = () => {
  console.log('计算小红点数量，救助数据总数:', rescueTasks.value.length)
  console.log('救助数据状态字段示例:', rescueTasks.value.map(r => r.status))
  
  // 支持多种待处理状态表示
  const pendingStatuses = ['待处理', '待处理', 'PENDING', 'pending']
  const count = rescueTasks.value.filter(rescue => 
    pendingStatuses.includes(rescue.status)
  ).length
  
  console.log('待处理状态的救助数量:', count)
  pendingRescueCount.value = count
}

// 计算属性 - 筛选后的救助总数
const filteredRescueTotal = computed(() => {
  let filtered = rescueTasks.value
  
  // 关键词搜索
  if (rescueSearchKeyword.value) {
    const keyword = rescueSearchKeyword.value.toLowerCase()
    filtered = filtered.filter(rescue => 
      (rescue.location && rescue.location.toLowerCase().includes(keyword)) ||
      (rescue.description && rescue.description.toLowerCase().includes(keyword))
    )
  }
  
  // 状态筛选
  if (rescueFilterStatus.value) {
    filtered = filtered.filter(rescue => rescue.status === rescueFilterStatus.value)
  }
  
  // 紧急程度筛选
  if (rescueFilterUrgency.value) {
    // 同时支持中文和英文紧急程度值
    const urgencyValues = {
      '低': ['低', 'LOW'],
      '中': ['中', 'MEDIUM'], 
      '高': ['高', 'HIGH'],
      '紧急': ['紧急', 'CRITICAL']
    }
    const validValues = urgencyValues[rescueFilterUrgency.value]
    filtered = filtered.filter(rescue => validValues.includes(rescue.urgencyLevel))
  }
  
  return filtered.length
})

// 计算属性 - 筛选后的救助列表（先筛选再分页）
const filteredRescues = computed(() => {
  let filtered = rescueTasks.value
  
  // 关键词搜索
  if (rescueSearchKeyword.value) {
    const keyword = rescueSearchKeyword.value.toLowerCase()
    filtered = filtered.filter(rescue => 
      (rescue.location && rescue.location.toLowerCase().includes(keyword)) ||
      (rescue.description && rescue.description.toLowerCase().includes(keyword))
    )
  }
  
  // 状态筛选
  if (rescueFilterStatus.value) {
    filtered = filtered.filter(rescue => rescue.status === rescueFilterStatus.value)
  }
  
  // 紧急程度筛选
  if (rescueFilterUrgency.value) {
    // 同时支持中文和英文紧急程度值
    const urgencyValues = {
      '低': ['低', 'LOW'],
      '中': ['中', 'MEDIUM'], 
      '高': ['高', 'HIGH'],
      '紧急': ['紧急', 'CRITICAL']
    }
    const validValues = urgencyValues[rescueFilterUrgency.value]
    filtered = filtered.filter(rescue => validValues.includes(rescue.urgencyLevel))
  }
  
  // 检查当前页是否超出筛选后的数据范围
  const totalPages = Math.ceil(filtered.length / rescuePageSize.value)
  if (rescueCurrentPage.value > totalPages && totalPages > 0) {
    rescueCurrentPage.value = 1
  }
  
  // 前端分页
  const startIndex = (rescueCurrentPage.value - 1) * rescuePageSize.value
  const endIndex = startIndex + rescuePageSize.value
  return filtered.slice(startIndex, endIndex)
})

// 计算属性 - 筛选后的领养列表（仅搜索和筛选，不分页）
const filteredAdoptions = computed(() => {
  let filtered = adoptionTasks.value
  
  // 关键词搜索
  if (adoptionSearchKeyword.value) {
    const keyword = adoptionSearchKeyword.value.toLowerCase()
    filtered = filtered.filter(adoption => 
      (adoption.catName && adoption.catName.toLowerCase().includes(keyword)) ||
      (adoption.applicantName && adoption.applicantName.toLowerCase().includes(keyword))
    )
  }
  
  return filtered
})

// 空状态文本
const rescueEmptyText = computed(() => {
  if (rescueSearchKeyword.value || rescueFilterStatus.value || rescueFilterUrgency.value) {
    return '暂无符合条件的救助需求'
  }
  return '暂无救助需求'
})

// 猫咪审核空状态文本
const catEmptyText = computed(() => {
  return '暂无待审核猫咪'
})

// 帖子审核空状态文本
const postEmptyText = computed(() => {
  return '暂无待审核帖子'
})

// 领养申请空状态文本
const adoptionEmptyText = computed(() => {
  if (adoptionSearchKeyword.value) {
    return '暂无符合条件的领养申请'
  }
  return '暂无领养申请'
})

// 紧急程度标签类型
const getUrgencyType = (urgencyLevel) => {
  switch (urgencyLevel) {
    case 'CRITICAL': return 'danger'
    case 'HIGH': return 'danger'
    case 'MEDIUM': return 'warning'
    case 'LOW': return 'info'
    default: return ''
  }
}

// 紧急程度文本
const getUrgencyText = (urgencyLevel) => {
  switch (urgencyLevel) {
    case 'CRITICAL': return '非常紧急'
    case 'HIGH': return '紧急'
    case 'MEDIUM': return '中等'
    case 'LOW': return '低'
    default: return urgencyLevel || '未知'
  }
}

// 猫咪审核状态标签类型
const getCatStatusType = (status) => {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'APPROVED': return 'success'
    case 'REJECTED': return 'danger'
    case '已报告': return 'warning'
    case '已通过': return 'success'
    case '已拒绝': return 'danger'
    default: return ''
  }
}

// 猫咪审核状态文本
const getCatStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待审核'
    case 'APPROVED': return '已通过'
    case 'REJECTED': return '已拒绝'
    case '已报告': return '待审核'
    case '已通过': return '已通过'
    case '已拒绝': return '已拒绝'
    default: return status || '未知'
  }
}

// 帖子审核状态标签类型
const getPostStatusType = (status) => {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'APPROVED': return 'success'
    case 'REJECTED': return 'danger'
    case '已报告': return 'warning'
    case '已通过': return 'success'
    case '已拒绝': return 'danger'
    default: return ''
  }
}

// 帖子审核状态文本
const getPostStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待审核'
    case 'APPROVED': return '已通过'
    case 'REJECTED': return '已拒绝'
    case '已报告': return '待审核'
    case '已通过': return '已通过'
    case '已拒绝': return '已拒绝'
    default: return status || '未知'
  }
}

// 查看帖子详情
const viewPostDetail = (post) => {
  currentPost.value = post
  showPostDetailDialog.value = true
}

// 处理表格行点击事件
const handleRowClick = (row, column, event) => {
  // 如果点击的是操作列（包含按钮），则不触发详情对话框
  if (column && column.property === 'action') {
    return
  }
  
  // 如果点击的是操作按钮区域，则不触发详情对话框
  if (event && event.target && (
    event.target.closest('.action-buttons') ||
    event.target.closest('.el-button')
  )) {
    return
  }
  
  // 打开详情对话框
  viewPostDetail(row)
}

// 救助状态标签类型
const getRescueStatusType = (status) => {
  switch (status) {
    case '待处理': return 'danger'
    case '待处理': return 'danger'
    case '进行中': return 'warning'
    case '已完成': return 'success'
    case '已关闭': return 'info'
    default: return ''
  }
}

// 救助状态文本
const getRescueStatusText = (status) => {
  switch (status) {
    case '待处理': return '待处理'
    case '待处理': return '待处理'
    case '进行中': return '进行中'
    case '已完成': return '已完成'
    case '已关闭': return '已关闭'
    default: return status || '未知状态'
  }
}

// 救助时间格式化
const formatRescueDate = (date) => {
  if (!date) return ''
  const dateObj = new Date(date)
  return dateObj.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 救助行点击事件
const handleRescueRowClick = (row) => {
  viewRescueDetail(row)
}

// 查看救助详情
const viewRescueDetail = (row) => {
  console.log('查看救助详情数据:', row)
  console.log('图片字段images:', row.images)
  console.log('救援日志图片字段rescueLogImages:', row.rescueLogImages)
  console.log('负责人字段rescuerId:', row.rescuerId)
  console.log('处理时间字段acceptTime:', row.acceptTime, 'completeTime:', row.completeTime)
  selectedRescue.value = row
  showRescueDetail.value = true
}

// 解析图片数据
const parseImages = (imagesData) => {
  if (!imagesData) return []
  
  try {
    // 如果imagesData是字符串，尝试解析为JSON
    if (typeof imagesData === 'string') {
      const parsed = JSON.parse(imagesData)
      // 如果是字符串数组，转换为对象数组
      if (Array.isArray(parsed) && parsed.length > 0 && typeof parsed[0] === 'string') {
        return parsed.map(url => ({ url, name: '图片' }))
      }
      return Array.isArray(parsed) ? parsed : []
    }
    
    // 如果已经是数组，直接返回
    if (Array.isArray(imagesData)) {
      return imagesData
    }
    
    return []
  } catch (error) {
    console.error('解析图片数据失败:', error)
    return []
  }
}

// 解析救援日志图片数据
const parseRescueLogImages = (imagesData) => {
  if (!imagesData) return []
  
  try {
    // 如果imagesData是字符串，可能是逗号分隔的URL
    if (typeof imagesData === 'string') {
      // 尝试解析为JSON
      try {
        const parsed = JSON.parse(imagesData)
        if (Array.isArray(parsed)) {
          return parsed
        }
      } catch (jsonError) {
        // 如果不是JSON，尝试按逗号分割
        if (imagesData.includes(',')) {
          return imagesData.split(',').map(url => url.trim()).filter(url => url)
        }
        // 如果是单个URL
        return [imagesData]
      }
    }
    
    // 如果已经是数组，直接返回
    if (Array.isArray(imagesData)) {
      return imagesData
    }
    
    return []
  } catch (error) {
    console.error('解析救援日志图片数据失败:', error)
    return []
  }
}

// 领养申请状态类型
const getAdoptionStatusType = (status) => {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'UNDER_REVIEW': return 'primary'
    case 'APPROVED': return 'success'
    case 'REJECTED': return 'danger'
    default: return ''
  }
}

// 领养申请状态文本
const getAdoptionStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待审核'
    case 'UNDER_REVIEW': return '审核中'
    case 'APPROVED': return '已通过'
    case 'REJECTED': return '已拒绝'
    default: return status || '未知状态'
  }
}

// 领养申请状态类名
const getAdoptionStatusClass = (status) => {
  switch (status) {
    case 'PENDING': return 'status-pending'
    case 'UNDER_REVIEW': return 'status-review'
    case 'APPROVED': return 'status-approved'
    case 'REJECTED': return 'status-rejected'
    default: return ''
  }
}

// 获取猫咪图片数组（处理JSON格式的图片路径）
const getCatImages = (imagesData) => {
  if (!imagesData) return []
  
  // 如果已经是数组，直接返回
  if (Array.isArray(imagesData)) {
    return imagesData
  }
  
  // 如果是JSON字符串，尝试解析
  if (typeof imagesData === 'string') {
    try {
      // 尝试解析JSON
      const parsed = JSON.parse(imagesData)
      if (Array.isArray(parsed)) {
        return parsed
      }
      // 如果是单个图片路径，包装成数组
      if (typeof parsed === 'string') {
        return [parsed]
      }
    } catch (error) {
      // 如果不是JSON，可能是单个图片路径
      if (imagesData.trim() !== '') {
        return [imagesData]
      }
    }
  }
  
  return []
}

// 获取猫咪图片路径（模仿用户端CatList.vue的处理方式）
const getCatImagePath = (imagePath) => {
  if (!imagePath) return ''
  
  // 处理数据库中的图片路径，确保能够正确加载
  if (imagePath.startsWith('/') && !imagePath.startsWith('/uploads') && !imagePath.startsWith('/img')) {
    // 如果是根路径且不是uploads或img开头，添加img前缀
    return `/img${imagePath}`
  } else if (imagePath.includes('../public/img/')) {
    // 处理相对路径，转换为前端可访问的路径
    return imagePath.replace('../public/img/', '/img/')
  } else if (!imagePath.startsWith('/')) {
    // 如果是相对路径（如cat1.jpg），添加/img/前缀
    return `/img/${imagePath}`
  }
  
  return imagePath
}

// 获取猫咪图片（模仿猫咪管理页面的实现）
const getCatImage = (cat) => {
  if (!cat) return '/img/placeholder-cat.jpg'
  
  // 优先使用images字段中的第一张图片
  if (cat.images) {
    const images = getCatImages(cat.images)
    if (images.length > 0) {
      return getCatImagePath(images[0])
    }
  }
  
  // 如果没有图片，返回默认图片
  return '/img/placeholder-cat.jpg'
}

// 健康状况相关函数
const getHealthText = (health) => {
  const texts = {
    '健康': '健康',
    '恢复中': '恢复中',
    '残疾': '残疾'
  }
  return texts[health] || health
}

const getHealthType = (health) => {
  const types = {
    '健康': 'success',
    '恢复中': 'warning',
    '残疾': 'danger'
  }
  return types[health] || 'info'
}

// 状态相关函数
const getStatusText = (status) => {
  const texts = {
    '已拒绝': '已拒绝',
    '照顾中': '照顾中',
    '待领养': '待领养',
    '已领养': '已领养'
  }
  return texts[status] || status
}

const getStatusType = (status) => {
  const types = {
    '已拒绝': 'danger',
    '照顾中': 'warning',
    '待领养': 'primary',
    '已领养': 'success'
  }
  return types[status] || 'info'
}

// 获取图片URL
const getImageUrl = (imagePath) => {
  if (!imagePath) return ''
  
  // 如果已经是完整的URL，直接返回
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    return imagePath
  }
  
  // 图片路径应该直接访问服务器根路径，而不是API路径
  // 从API基础URL中提取服务器地址
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
  const serverBaseUrl = apiBaseUrl.replace('/api', '')
  
  // 确保图片路径以/开头
  const normalizedPath = imagePath.startsWith('/') ? imagePath : '/' + imagePath
  return `${serverBaseUrl}${normalizedPath}`
}

// 获取帖子类型文本
const getPostTypeText = (type) => {
  // 支持中英文类型值
  const texts = {
    'HELP': '求助',
    'SHARE': '分享',
    'DISCUSSION': '讨论',
    'ANNOUNCEMENT': '公告',
    '求助': '求助',
    '分享': '分享',
    '讨论': '讨论',
    '公告': '公告'
  }
  return texts[type] || type || '未知类型'
}

// 获取帖子类型标签类型
const getPostTypeTag = (type) => {
  // 支持中英文类型值
  switch (type) {
    case 'HELP':
    case '求助':
      return 'danger'
    case 'SHARE':
    case '分享':
      return 'success'
    case 'DISCUSSION':
    case '讨论':
      return 'primary'
    case 'ANNOUNCEMENT':
    case '公告':
      return 'warning'
    default: return 'info'
  }
}

// 获取帖子图片数组
const getPostImages = (images) => {
  if (!images) return []
  
  try {
    // 如果已经是数组，直接返回
    if (Array.isArray(images)) {
      return images
    }
    
    // 如果是JSON字符串，解析为数组
    if (typeof images === 'string') {
      // 先尝试直接解析JSON
      try {
        const parsed = JSON.parse(images)
        if (Array.isArray(parsed)) {
          return parsed
        }
      } catch (jsonError) {
        // JSON解析失败，尝试其他格式
      }
      
      // 如果是逗号分隔的字符串
      if (images.includes(',')) {
        return images.split(',').map(url => url.trim()).filter(url => url)
      }
      
      // 如果是单个URL
      return [images]
    }
    
    return []
  } catch (error) {
    console.error('解析帖子图片失败:', error)
    return []
  }
}

// 加载待办任务统计
const loadPendingCounts = async () => {
  try {
    const response = await adminApi.getPendingCounts()
    if (response.data && response.data.data) {
      pendingCounts.value = response.data.data
      console.log('待办任务统计加载成功:', pendingCounts.value)
    } else {
      console.warn('待办任务统计响应格式异常:', response.data)
    }
  } catch (error) {
    console.error('获取待办任务统计失败:', error)
    ElMessage.error('获取待办任务统计失败')
  }
}

// 加载待审核猫咪列表
const loadPendingCats = async () => {
  try {
    const response = await adminApi.getPendingCats(pendingCatPagination.value.currentPage, pendingCatPagination.value.pageSize)
    if (response.data && response.data.data) {
      pendingCats.value = response.data.data
      pendingCatPagination.value.total = response.data.total || 0
    } else {
      pendingCats.value = []
      pendingCatPagination.value.total = 0
    }
  } catch (error) {
    console.error('获取待审核猫咪列表失败:', error)
    pendingCats.value = []
    pendingCatPagination.value.total = 0
  }
}

// 加载已通过猫咪列表
const loadApprovedCats = async () => {
  try {
    const response = await adminApi.getApprovedCats(approvedCatPagination.value.currentPage, approvedCatPagination.value.pageSize)
    if (response.data && response.data.data) {
      approvedCats.value = response.data.data
      approvedCatPagination.value.total = response.data.total || 0
    } else {
      approvedCats.value = []
      approvedCatPagination.value.total = 0
    }
  } catch (error) {
    console.error('获取已通过猫咪列表失败:', error)
    approvedCats.value = []
    approvedCatPagination.value.total = 0
  }
}

// 加载已拒绝猫咪列表
const loadRejectedCats = async () => {
  try {
    const response = await adminApi.getRejectedCats(rejectedCatPagination.value.currentPage, rejectedCatPagination.value.pageSize)
    if (response.data && response.data.data) {
      rejectedCats.value = response.data.data
      rejectedCatPagination.value.total = response.data.total || 0
    } else {
      rejectedCats.value = []
      rejectedCatPagination.value.total = 0
    }
  } catch (error) {
    console.error('获取已拒绝猫咪列表失败:', error)
    rejectedCats.value = []
    rejectedCatPagination.value.total = 0
  }
}

// 加载待审核帖子列表
const loadPendingPosts = async () => {
  try {
    const response = await adminApi.getPendingPosts(pendingPagination.value.currentPage, pendingPagination.value.pageSize)
    if (response.data && response.data.data) {
      pendingPosts.value = response.data.data
      pendingPagination.value.total = response.data.total || 0
      console.log('加载待审核帖子数据成功:', response.data.data, '总数:', pendingPagination.value.total)
    } else {
      console.warn('获取待审核帖子列表返回数据格式异常:', response.data)
      // 如果数据格式不符合预期，尝试使用默认数据格式
      pendingPosts.value = response.data || []
      pendingPagination.value.total = pendingPosts.value.length
    }
  } catch (error) {
    console.error('获取待审核帖子列表失败:', error)
    ElMessage.error('获取待审核帖子列表失败')
  }
}

// 加载已通过帖子列表
const loadApprovedPosts = async () => {
  try {
    const response = await communityApi.getPosts({ page: 1, size: 1000 })
    console.log('获取已发布帖子API完整响应(已通过):', response)
    console.log('获取已发布帖子API响应数据(已通过):', response.data)
    
    // 检查不同的数据结构可能性
    let allPostsData = []
    if (response.data && response.data.records) {
      allPostsData = response.data.records
    } else if (response.data && Array.isArray(response.data)) {
      allPostsData = response.data
    } else if (response.data && response.data.data) {
      allPostsData = response.data.data
    }
    
    console.log('提取的所有帖子数据(已通过):', allPostsData)
    
    if (allPostsData && allPostsData.length > 0) {
      // 前端分页处理
      const startIndex = (approvedPagination.value.currentPage - 1) * approvedPagination.value.pageSize
      const endIndex = startIndex + approvedPagination.value.pageSize
      
      // 社区API返回的已经是已发布的帖子，直接使用
      approvedPosts.value = allPostsData.slice(startIndex, endIndex)
      approvedPagination.value.total = allPostsData.length
      
      console.log('加载已通过帖子数据成功:', approvedPosts.value, '总数:', approvedPagination.value.total)
    } else {
      console.warn('获取已通过帖子列表返回数据为空或格式异常:', response.data)
      approvedPosts.value = []
      approvedPagination.value.total = 0
    }
  } catch (error) {
    console.error('获取已通过帖子列表失败:', error)
    // 不显示错误信息，避免干扰用户体验
    approvedPosts.value = []
    approvedPagination.value.total = 0
  }
}

// 加载已拒绝帖子列表
const loadRejectedPosts = async () => {
  try {
    // 使用管理员API获取已拒绝帖子列表
    const response = await adminApi.getRejectedPosts(rejectedPagination.value.currentPage, rejectedPagination.value.pageSize)
    console.log('获取已拒绝帖子API完整响应:', response)
    console.log('获取已拒绝帖子API响应数据:', response.data)
    
    // 检查不同的数据结构可能性
    let postsData = []
    if (response.data && response.data.data) {
      postsData = response.data.data
      rejectedPagination.value.total = response.data.total || 0
    } else if (response.data && response.data.records) {
      postsData = response.data.records
      rejectedPagination.value.total = response.data.total || 0
    } else if (response.data && Array.isArray(response.data)) {
      postsData = response.data
      rejectedPagination.value.total = postsData.length
    }
    
    console.log('提取的帖子数据(已拒绝):', postsData, '总数:', rejectedPagination.value.total)
    
    if (postsData && postsData.length > 0) {
      rejectedPosts.value = postsData
      console.log('加载已拒绝帖子数据成功:', rejectedPosts.value)
    } else {
      console.warn('获取已拒绝帖子列表返回数据为空或格式异常:', response.data)
      rejectedPosts.value = []
      rejectedPagination.value.total = 0
    }
  } catch (error) {
    console.error('获取已拒绝帖子列表失败:', error)
    // 不显示错误信息，避免干扰用户体验
    rejectedPosts.value = []
    rejectedPagination.value.total = 0
  }
}

// 加载所有帖子数据
const loadAllPosts = async () => {
  await Promise.all([
    loadPendingPosts(),
    loadApprovedPosts(),
    loadRejectedPosts()
  ])
}

// 加载待处理救助列表
const loadPendingRescues = async () => {
  try {
    rescueLoading.value = true
    console.log('开始加载数据，rescueLoading状态设置为:', rescueLoading.value)
    console.log('调用救助API，获取所有数据')
    // 获取所有数据，不分页
    const response = await adminApi.getPendingRescues(1, 1000) // 获取足够大的页面大小来获取所有数据
    console.log('API响应状态:', response.status)
    console.log('API响应数据:', response.data)
    
    if (response.data && response.data.data) {
      rescueTasks.value = response.data.data
      rescueTotal.value = response.data.total || 0
      console.log('加载救助数据成功，总数:', rescueTotal.value, '当前数据:', rescueTasks.value.length)
      
      // 调试：打印第一条数据的紧急程度字段
      if (rescueTasks.value.length > 0) {
        console.log('第一条救助数据的紧急程度字段:', rescueTasks.value[0].urgencyLevel)
        console.log('第一条救助数据的完整信息:', rescueTasks.value[0])
      }
      
      // 检查分页信息
      console.log('分页信息 - 当前页:', rescueCurrentPage.value, '页面大小:', rescuePageSize.value, '总条数:', rescueTotal.value)
      
      // 如果总数为0但数据不为空，说明后端可能没有正确返回total
      if (rescueTotal.value === 0 && rescueTasks.value.length > 0) {
        console.warn('后端可能没有正确返回total字段，数据长度:', rescueTasks.value.length)
        rescueTotal.value = rescueTasks.value.length
      }
      
      // 数据加载完成后立即计算小红点
      calculatePendingRescueCount()
    } else {
      console.warn('API响应数据格式异常:', response.data)
      // 如果响应格式不符合预期，尝试直接使用响应数据
      if (response.data && Array.isArray(response.data)) {
        rescueTasks.value = response.data
        rescueTotal.value = response.data.length
        console.log('使用直接数组数据，总数:', rescueTotal.value)
        // 保持当前分页状态
        
        // 数据加载完成后立即计算小红点
        calculatePendingRescueCount()
      }
    }
  } catch (error) {
    console.error('获取待处理救助列表失败:', error)
    ElMessage.error('获取待处理救助列表失败')
  } finally {
    rescueLoading.value = false
    console.log('数据加载完成，rescueLoading状态重置为:', rescueLoading.value)
  }
}

// 加载待审核领养列表
const loadPendingAdoptions = async () => {
  try {
    adoptionLoading.value = true
    const response = await adminApi.getPendingAdoptions(pendingAdoptionPagination.value.currentPage, pendingAdoptionPagination.value.pageSize)
    
    if (response.data && response.data.data) {
      pendingAdoptions.value = response.data.data
      pendingAdoptionPagination.value.total = response.data.total || 0
    } else {
      console.warn('API响应数据格式异常:', response.data)
      pendingAdoptions.value = []
      pendingAdoptionPagination.value.total = 0
    }
  } catch (error) {
    console.error('获取待审核领养列表失败:', error)
    pendingAdoptions.value = []
    pendingAdoptionPagination.value.total = 0
  } finally {
    adoptionLoading.value = false
  }
}

// 加载已通过领养列表
const loadApprovedAdoptions = async () => {
  try {
    adoptionLoading.value = true
    const response = await adminApi.getApprovedAdoptions(approvedAdoptionPagination.value.currentPage, approvedAdoptionPagination.value.pageSize)
    
    if (response.data && response.data.data) {
      approvedAdoptions.value = response.data.data
      approvedAdoptionPagination.value.total = response.data.total || 0
    } else {
      console.warn('API响应数据格式异常:', response.data)
      approvedAdoptions.value = []
      approvedAdoptionPagination.value.total = 0
    }
  } catch (error) {
    console.error('获取已通过领养列表失败:', error)
    approvedAdoptions.value = []
    approvedAdoptionPagination.value.total = 0
  } finally {
    adoptionLoading.value = false
  }
}

// 加载已拒绝领养列表
const loadRejectedAdoptions = async () => {
  try {
    adoptionLoading.value = true
    const response = await adminApi.getRejectedAdoptions(rejectedAdoptionPagination.value.currentPage, rejectedAdoptionPagination.value.pageSize)
    
    if (response.data && response.data.data) {
      rejectedAdoptions.value = response.data.data
      rejectedAdoptionPagination.value.total = response.data.total || 0
    } else {
      console.warn('API响应数据格式异常:', response.data)
      rejectedAdoptions.value = []
      rejectedAdoptionPagination.value.total = 0
    }
  } catch (error) {
    console.error('获取已拒绝领养列表失败:', error)
    rejectedAdoptions.value = []
    rejectedAdoptionPagination.value.total = 0
  } finally {
    adoptionLoading.value = false
  }
}

const handleTabSelect = (index) => {
  activeTab.value = index
  // 切换标签时加载对应数据
  switch (index) {
    case 'cats':
      loadPendingCats()
      break
    case 'posts':
      loadAllPosts()
      break
    case 'rescues':
      loadPendingRescues()
      break
    case 'adoptions':
      // 根据当前子标签页加载对应数据
      switch (adoptionSubTab.value) {
        case 'pending':
          loadPendingAdoptions()
          break
        case 'approved':
          loadApprovedAdoptions()
          break
        case 'rejected':
          loadRejectedAdoptions()
          break
      }
      break
  }
}

const handleCatReview = async (id, action) => {
  try {
    loading.value = true
    await adminApi.reviewCat(id, action)
    ElMessage.success(`猫咪审核${action === 'approve' ? '通过' : '拒绝'}成功`)
    // 重新加载数据
    loadPendingCats()
    loadRejectedCats()
    loadPendingCounts()
  } catch (error) {
    console.error('猫咪审核失败:', error)
    ElMessage.error('猫咪审核失败')
  } finally {
    loading.value = false
  }
}

const handlePostReview = async (id, action) => {
  try {
    loading.value = true
    
    let reviewData = {}
    
    // 如果是拒绝操作，打开拒绝对话框
    if (action === 'reject') {
      selectedPostForReject.value = id
      showPostRejectDialog.value = true
      return // 等待用户在对话框中完成操作
    }
    
    console.log('开始审核帖子，ID:', id, '操作:', action, '数据:', reviewData)
    
    // 调用审核API
    const response = await adminApi.reviewPost(id, action, reviewData)
    console.log('审核API响应:', response)
    
    ElMessage.success(`帖子审核${action === 'approve' ? '通过' : '拒绝'}成功`)
    
    // 关闭对话框
    showPostDetailDialog.value = false
    
    // 重新加载数据
    console.log('重新加载待审核帖子列表...')
    await loadPendingPosts()
    console.log('重新加载待办统计...')
    await loadPendingCounts()
    
    console.log('审核流程完成')
  } catch (error) {
    // 如果是用户取消操作，不显示错误信息
    if (error === 'cancel' || error === 'close') {
      console.log('用户取消操作')
      return
    }
    console.error('帖子审核失败:', error)
    ElMessage.error('帖子审核失败')
  } finally {
    loading.value = false
  }
}

// 关闭帖子审核拒绝对话框
const handleClosePostRejectDialog = () => {
  showPostRejectDialog.value = false
  postRejectForm.value = {
    reason: '',
    customReason: ''
  }
  selectedPostForReject.value = null
}

// 处理帖子审核拒绝
const handlePostReject = async () => {
  try {
    if (!postRejectForm.value.reason) {
      ElMessage.error('请选择拒绝理由')
      return
    }
    
    if (postRejectForm.value.reason === '其他原因' && !postRejectForm.value.customReason) {
      ElMessage.error('请输入详细的拒绝理由')
      return
    }
    
    const finalReason = postRejectForm.value.reason === '其他原因' ? postRejectForm.value.customReason : postRejectForm.value.reason
    
    console.log('开始拒绝帖子，ID:', selectedPostForReject.value, '拒绝理由:', finalReason)
    
    // 调用审核API
    const response = await adminApi.reviewPost(selectedPostForReject.value, 'reject', { remark: finalReason })
    console.log('拒绝帖子API响应:', response)
    
    ElMessage.success('帖子拒绝成功')
    
    // 关闭对话框
    handleClosePostRejectDialog()
    showPostDetailDialog.value = false // 同时关闭帖子详情对话框
    
    // 重新加载数据
    console.log('重新加载待审核帖子列表...')
    await loadPendingPosts()
    console.log('重新加载待办统计...')
    await loadPendingCounts()
    
    console.log('拒绝流程完成')
  } catch (error) {
    console.error('拒绝帖子失败:', error)
    ElMessage.error('帖子拒绝失败')
  }
}

// 马上救援
const takeRescue = (rescue) => {
  console.log('马上救援按钮被点击，救助需求:', rescue)
  console.log('救助需求状态:', rescue.status)
  
  // 这里可以添加马上救援的逻辑
  ElMessage.info('马上救援功能待实现')
  
  // 关闭详情对话框
  showRescueDetail.value = false
}



const volunteerRules = {
  name: [
    { required: true, message: '请输入志愿者姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入志愿者电话', trigger: 'blur' }
  ]
}

// 救援完成
const completeRescue = async (rescue) => {
  console.log('救援完成按钮被点击，救助需求:', rescue)
  
  // 关闭详情对话框
  showRescueDetail.value = false
}

// 处理马上救援事件 - 与救援端完全一致
const handleTakeRescue = (rescue) => {
  console.log('马上救援按钮被点击，救助需求:', rescue)
  console.log('救助需求状态:', rescue.status)
  
  currentRescue.value = rescue
  showTakeRescueDialog.value = true
  // 关闭详情对话框
  showRescueDetail.value = false
  
  console.log('马上救援对话框已打开')
}

// 处理救援完成事件 - 与救援端完全一致
const handleCompleteRescue = (rescue) => {
  console.log('救援完成事件触发:', rescue)
  // 这里可以添加管理端的救援完成逻辑
  // 现在由RescueDetailDialog组件处理救援完成
  
  // 刷新数据
  loadPendingRescues()
  loadPendingCounts()
  
  // 不在管理端显示成功消息，由RescueDetailDialog组件显示
  // ElMessage.success('救援任务已完成')
}

// 处理关闭任务事件 - 与救援端完全一致
const handleCloseRescue = (rescue) => {
  console.log('关闭任务事件触发:', rescue)
  // 这里可以添加管理端的关闭任务逻辑
  ElMessage.info('关闭任务功能待实现')
}

// 关闭任务
const closeRescue = async (rescue) => {
  console.log('关闭任务按钮被点击，救助需求:', rescue)
  
  // 这里可以添加关闭任务的逻辑
  ElMessage.info('关闭任务功能待实现')
  
  // 关闭详情对话框
  showRescueDetail.value = false
}

const handleRescueProcess = async (id, action) => {
  try {
    loading.value = true
    await adminApi.processRescue(id, action)
    ElMessage.success(`救助${action === 'accept' ? '受理' : '分配'}成功`)
    // 重新加载数据
    loadPendingRescues()
    loadPendingCounts()
  } catch (error) {
    console.error('救助处理失败:', error)
    ElMessage.error('救助处理失败')
  } finally {
    loading.value = false
  }
}

const handleAdoptionReview = async (id, action) => {
  try {
    loading.value = true
    await adminApi.reviewAdoption(id, action)
    ElMessage.success(`领养审核${action === 'approve' ? '通过' : '拒绝'}成功`)
    // 重新加载数据
    loadPendingAdoptions()
    loadPendingCounts()
  } catch (error) {
    console.error('领养审核失败:', error)
    ElMessage.error('领养审核失败')
  } finally {
    loading.value = false
  }
}

// 救助管理筛选重置
const resetRescueFilters = () => {
  rescueSearchKeyword.value = ''
  rescueFilterStatus.value = ''
  rescueFilterUrgency.value = ''
  rescueCurrentPage.value = 1
}

// 领养审核筛选重置
const resetAdoptionFilters = () => {
  adoptionSearchKeyword.value = ''
  adoptionCurrentPage.value = 1
}

// 救助管理分页处理
const handleRescueSizeChange = (size) => {
  console.log('页面大小改变事件触发，新大小:', size)
  rescuePageSize.value = size
  rescueCurrentPage.value = 1
  console.log('更新后的分页参数 - 页面大小:', rescuePageSize.value, '当前页:', rescueCurrentPage.value)
  loadPendingRescues()
}

const handleRescueCurrentChange = (page) => {
  console.log('页码改变事件触发，新页码:', page)
  rescueCurrentPage.value = page
  console.log('更新后的分页参数 - 页面大小:', rescuePageSize.value, '当前页:', rescueCurrentPage.value)
  loadPendingRescues()
}

// 帖子审核分页处理
const handlePendingSizeChange = (size) => {
  console.log('待审核帖子页面大小改变事件触发，新大小:', size)
  pendingPagination.value.pageSize = size
  pendingPagination.value.currentPage = 1
  console.log('更新后的分页参数 - 页面大小:', pendingPagination.value.pageSize, '当前页:', pendingPagination.value.currentPage)
  loadPendingPosts()
}

const handlePendingCurrentChange = (page) => {
  console.log('待审核帖子页码改变事件触发，新页码:', page)
  pendingPagination.value.currentPage = page
  console.log('更新后的分页参数 - 页面大小:', pendingPagination.value.pageSize, '当前页:', pendingPagination.value.currentPage)
  loadPendingPosts()
}

const handleApprovedSizeChange = (size) => {
  console.log('已通过帖子页面大小改变事件触发，新大小:', size)
  approvedPagination.value.pageSize = size
  approvedPagination.value.currentPage = 1
  console.log('更新后的分页参数 - 页面大小:', approvedPagination.value.pageSize, '当前页:', approvedPagination.value.currentPage)
  loadApprovedPosts()
}

const handleApprovedCurrentChange = (page) => {
  console.log('已通过帖子页码改变事件触发，新页码:', page)
  approvedPagination.value.currentPage = page
  console.log('更新后的分页参数 - 页面大小:', approvedPagination.value.pageSize, '当前页:', approvedPagination.value.currentPage)
  loadApprovedPosts()
}

const handleRejectedSizeChange = (size) => {
  console.log('已拒绝帖子页面大小改变事件触发，新大小:', size)
  rejectedPagination.value.pageSize = size
  rejectedPagination.value.currentPage = 1
  console.log('更新后的分页参数 - 页面大小:', rejectedPagination.value.pageSize, '当前页:', rejectedPagination.value.currentPage)
  loadRejectedPosts()
}

const handleRejectedCurrentChange = (page) => {
  console.log('已拒绝帖子页码改变事件触发，新页码:', page)
  rejectedPagination.value.currentPage = page
  console.log('更新后的分页参数 - 页面大小:', rejectedPagination.value.pageSize, '当前页:', rejectedPagination.value.currentPage)
  loadRejectedPosts()
}

// 领养审核分页处理
const handleAdoptionSizeChange = (size) => {
  adoptionPageSize.value = size
  adoptionCurrentPage.value = 1
}

const handleAdoptionCurrentChange = (page) => {
  adoptionCurrentPage.value = page
}

// 领养审核子标签页分页处理
const handlePendingAdoptionSizeChange = (size) => {
  console.log('待审核领养页面大小改变事件触发，新大小:', size)
  pendingAdoptionPagination.value.pageSize = size
  pendingAdoptionPagination.value.currentPage = 1
  console.log('更新后的分页参数 - 页面大小:', pendingAdoptionPagination.value.pageSize, '当前页:', pendingAdoptionPagination.value.currentPage)
  loadPendingAdoptions()
}

const handlePendingAdoptionCurrentChange = (page) => {
  console.log('待审核领养页码改变事件触发，新页码:', page)
  pendingAdoptionPagination.value.currentPage = page
  console.log('更新后的分页参数 - 页面大小:', pendingAdoptionPagination.value.pageSize, '当前页:', pendingAdoptionPagination.value.currentPage)
  loadPendingAdoptions()
}

const handleApprovedAdoptionSizeChange = (size) => {
  console.log('已通过领养页面大小改变事件触发，新大小:', size)
  approvedAdoptionPagination.value.pageSize = size
  approvedAdoptionPagination.value.currentPage = 1
  console.log('更新后的分页参数 - 页面大小:', approvedAdoptionPagination.value.pageSize, '当前页:', approvedAdoptionPagination.value.currentPage)
  loadApprovedAdoptions()
}

const handleApprovedAdoptionCurrentChange = (page) => {
  console.log('已通过领养页码改变事件触发，新页码:', page)
  approvedAdoptionPagination.value.currentPage = page
  console.log('更新后的分页参数 - 页面大小:', approvedAdoptionPagination.value.pageSize, '当前页:', approvedAdoptionPagination.value.currentPage)
  loadApprovedAdoptions()
}

const handleRejectedAdoptionSizeChange = (size) => {
  console.log('已拒绝领养页面大小改变事件触发，新大小:', size)
  rejectedAdoptionPagination.value.pageSize = size
  rejectedAdoptionPagination.value.currentPage = 1
  console.log('更新后的分页参数 - 页面大小:', rejectedAdoptionPagination.value.pageSize, '当前页:', rejectedAdoptionPagination.value.currentPage)
  loadRejectedAdoptions()
}

const handleRejectedAdoptionCurrentChange = (page) => {
  console.log('已拒绝领养页码改变事件触发，新页码:', page)
  rejectedAdoptionPagination.value.currentPage = page
  console.log('更新后的分页参数 - 页面大小:', rejectedAdoptionPagination.value.pageSize, '当前页:', rejectedAdoptionPagination.value.currentPage)
  loadRejectedAdoptions()
}

// 猫咪审核分页处理
const handlePendingCatSizeChange = (size) => {
  console.log('待审核猫咪页面大小改变事件触发，新大小:', size)
  pendingCatPagination.value.pageSize = size
  pendingCatPagination.value.currentPage = 1
  console.log('更新后的分页参数 - 页面大小:', pendingCatPagination.value.pageSize, '当前页:', pendingCatPagination.value.currentPage)
  loadPendingCats()
}

const handlePendingCatCurrentChange = (page) => {
  console.log('待审核猫咪页码改变事件触发，新页码:', page)
  pendingCatPagination.value.currentPage = page
  console.log('更新后的分页参数 - 页面大小:', pendingCatPagination.value.pageSize, '当前页:', pendingCatPagination.value.currentPage)
  loadPendingCats()
}

const handleApprovedCatSizeChange = (size) => {
  console.log('已通过猫咪页面大小改变事件触发，新大小:', size)
  approvedCatPagination.value.pageSize = size
  approvedCatPagination.value.currentPage = 1
  console.log('更新后的分页参数 - 页面大小:', approvedCatPagination.value.pageSize, '当前页:', approvedCatPagination.value.currentPage)
  loadApprovedCats()
}

const handleApprovedCatCurrentChange = (page) => {
  console.log('已通过猫咪页码改变事件触发，新页码:', page)
  approvedCatPagination.value.currentPage = page
  console.log('更新后的分页参数 - 页面大小:', approvedCatPagination.value.pageSize, '当前页:', approvedCatPagination.value.currentPage)
  loadApprovedCats()
}

const handleRejectedCatSizeChange = (size) => {
  console.log('已拒绝猫咪页面大小改变事件触发，新大小:', size)
  rejectedCatPagination.value.pageSize = size
  rejectedCatPagination.value.currentPage = 1
  console.log('更新后的分页参数 - 页面大小:', rejectedCatPagination.value.pageSize, '当前页:', rejectedCatPagination.value.currentPage)
  loadRejectedCats()
}

const handleRejectedCatCurrentChange = (page) => {
  console.log('已拒绝猫咪页码改变事件触发，新页码:', page)
  rejectedCatPagination.value.currentPage = page
  console.log('更新后的分页参数 - 页面大小:', rejectedCatPagination.value.pageSize, '当前页:', rejectedCatPagination.value.currentPage)
  loadRejectedCats()
}

// 领养详情对话框相关函数
const viewAdoptionDetail = async (adoption) => {
  try {
    adoptionLoading.value = true
    
    // 通过API获取领养申请的详细信息
    const response = await adminApi.getAdoptionDetail(adoption.id)
    
    if (response.data && response.data.data) {
      selectedAdoption.value = response.data.data
    } else {
      // 如果API没有返回详细信息，使用表格行数据作为备选
      selectedAdoption.value = adoption
    }
    
    showAdoptionDetail.value = true
  } catch (error) {
    console.error('获取领养申请详情失败:', error)
    // 如果API调用失败，使用表格行数据作为备选
    selectedAdoption.value = adoption
    showAdoptionDetail.value = true
  } finally {
    adoptionLoading.value = false
  }
}

const handleCloseAdoptionDetail = () => {
  showAdoptionDetail.value = false
  selectedAdoption.value = null
}

const handleAdoptionApprove = async (adoptionId) => {
  try {
    loading.value = true
    // 调用API通过领养申请
    await adminApi.reviewAdoption(adoptionId, { action: 'approve' })
    ElMessage.success('领养申请已通过')
    showAdoptionDetail.value = false
    // 重新加载数据
    loadPendingAdoptions()
    loadPendingCounts()
  } catch (error) {
    console.error('通过领养申请失败:', error)
    ElMessage.error('通过领养申请失败')
  } finally {
    loading.value = false
  }
}

const handleCloseRejectDialog = () => {
  showRejectDialog.value = false
  rejectForm.value = {
    reason: '',
    customReason: ''
  }
}

const handleAdoptionReject = async () => {
  if (!rejectForm.value.reason) {
    ElMessage.warning('请选择拒绝理由')
    return
  }

  try {
    loading.value = true
    const rejectReason = rejectForm.value.reason === '其他原因' 
      ? rejectForm.value.customReason 
      : rejectForm.value.reason
    
    // 调用API拒绝领养申请
    await adminApi.reviewAdoption(selectedAdoption.value.id, { action: 'reject', reviewComment: rejectReason })
    ElMessage.success('领养申请已拒绝')
    showRejectDialog.value = false
    showAdoptionDetail.value = false
    // 重新加载数据
    loadPendingAdoptions()
    loadPendingCounts()
  } catch (error) {
    console.error('拒绝领养申请失败:', error)
    ElMessage.error('拒绝领养申请失败')
  } finally {
    loading.value = false
    handleCloseRejectDialog()
  }
}

// 猫咪审核表格行点击处理
const handleCatRowClick = (row, column, event) => {
  // 检查是否点击了操作按钮
  const target = event.target
  // 如果点击的是按钮元素或按钮的子元素，则不触发查看详情
  if (target.closest('button') || target.closest('.el-button')) {
    return
  }
  viewCatDetail(row)
}

// 猫咪详情对话框相关函数
const viewCatDetail = (cat) => {
  selectedCat.value = cat
  showCatDetail.value = true
}

const handleCloseCatDetail = () => {
  showCatDetail.value = false
  selectedCat.value = null
}

const handleCatApprove = async (catId) => {
  try {
    loading.value = true
    // 调用API通过猫咪审核
    await adminApi.reviewCat(catId, 'approve')
    ElMessage.success('猫咪审核已通过')
    showCatDetail.value = false
    // 重新加载数据
    loadPendingCats()
    loadPendingCounts()
  } catch (error) {
    console.error('通过猫咪审核失败:', error)
    ElMessage.error('通过猫咪审核失败')
  } finally {
    loading.value = false
  }
}

const handleCloseCatRejectDialog = () => {
  showCatRejectDialog.value = false
  catRejectForm.value = {
    reason: '',
    customReason: ''
  }
}

const handleCatReject = async () => {
  if (!catRejectForm.value.reason) {
    ElMessage.warning('请选择拒绝理由')
    return
  }

  try {
    loading.value = true
    const rejectReason = catRejectForm.value.reason === '其他原因' 
      ? catRejectForm.value.customReason 
      : catRejectForm.value.reason
    
    // 调用API拒绝猫咪审核
    await adminApi.reviewCat(selectedCat.value.id, 'reject', rejectReason)
    ElMessage.success('猫咪审核已拒绝')
    showCatRejectDialog.value = false
    showCatDetail.value = false
    // 重新加载数据
    loadPendingCats()
    loadPendingCounts()
  } catch (error) {
    console.error('拒绝猫咪审核失败:', error)
    ElMessage.error('拒绝猫咪审核失败')
  } finally {
    loading.value = false
    handleCloseCatRejectDialog()
  }
}

// 监听领养审核子标签页变化
watch(adoptionSubTab, (newTab) => {
  if (activeTab.value === 'adoptions') {
    switch (newTab) {
      case 'pending':
        loadPendingAdoptions()
        break
      case 'approved':
        loadApprovedAdoptions()
        break
      case 'rejected':
        loadRejectedAdoptions()
        break
    }
  }
})

// 监听猫咪审核子标签页变化
watch(catSubTab, (newTab) => {
  if (activeTab.value === 'cats') {
    switch (newTab) {
      case 'pending':
        loadPendingCats()
        break
      case 'approved':
        loadApprovedCats()
        break
      case 'rejected':
        loadRejectedCats()
        break
    }
  }
})

// WebSocket消息处理器ID
let adoptionReviewHandlerId = null
let adoptionApplicationHandlerId = null
let postApplicationHandlerId = null
let postReviewHandlerId = null

// 创建领养审核更新处理器
const handleAdoptionReviewUpdate = async (data) => {
  console.log('收到领养审核实时更新:', data)
  console.log('当前活跃标签页:', activeTab.value)
  console.log('当前领养子标签页:', adoptionSubTab.value)
  
  // 简化处理：只要收到领养审核更新消息，就重新加载数据
  console.log('开始重新加载领养申请列表...')
  
  try {
    // 使用Promise.all确保所有异步操作完成
    await Promise.all([
      loadPendingAdoptions(),
      loadApprovedAdoptions(), 
      loadRejectedAdoptions(),
      loadPendingCounts()
    ])
    
    // 显示更新通知
    ElMessage.success('领养审核列表已更新')
    console.log('领养审核列表更新完成')
  } catch (error) {
    console.error('重新加载领养申请列表失败:', error)
    ElMessage.error('数据更新失败，请手动刷新页面')
  }
}

// 创建领养申请更新处理器
const handleAdoptionApplicationUpdate = async (data) => {
  console.log('收到领养申请实时更新:', data)
  console.log('当前活跃标签页:', activeTab.value)
  console.log('当前领养子标签页:', adoptionSubTab.value)
  
  // 解析更新数据
  try {
    const updateData = JSON.parse(data)
    
    // 根据更新类型处理数据
    if (updateData.action === 'CREATE') {
      console.log('开始重新加载待审核列表和小红点...')
      
      // 重新加载待审核列表和小红点
      await Promise.all([
        loadPendingAdoptions(),
        loadPendingCounts()
      ])
      
      // 显示新申请通知
      ElMessage.success('收到新的领养申请，请及时审核')
      console.log('领养申请列表和小红点更新完成')
    }
  } catch (error) {
    console.error('解析WebSocket消息失败:', error)
  }
}

// 创建帖子申请更新处理器
const handlePostApplicationUpdate = async (data) => {
  console.log('收到帖子申请实时更新:', data)
  console.log('当前活跃标签页:', activeTab.value)
  console.log('当前帖子子标签页:', postSubTab.value)
  
  // 解析更新数据
  try {
    const updateData = JSON.parse(data)
    console.log('解析后的更新数据:', updateData)
    
    // 根据更新类型处理数据
    if (updateData.action === 'CREATE' || updateData.action === 'UPDATE') {
      console.log('开始重新加载待审核帖子列表和小红点...')
      
      // 检查当前页面状态
      console.log('当前活跃标签页:', activeTab.value)
      console.log('当前帖子子标签页:', postSubTab.value)
      console.log('当前待审核帖子数量:', pendingPosts.value.length)
      console.log('当前小红点数量:', pendingCounts.value.posts)
      
      // 重新加载待审核帖子列表和小红点
      console.log('调用loadPendingPosts...')
      await loadPendingPosts()
      console.log('调用loadPendingCounts...')
      await loadPendingCounts()
      
      // 检查更新后的状态
      console.log('更新后待审核帖子数量:', pendingPosts.value.length)
      console.log('更新后小红点数量:', pendingCounts.value.posts)
      
      // 显示新帖子通知
      ElMessage.success('收到新的帖子申请，请及时审核')
      console.log('帖子申请列表和小红点更新完成')
    } else if (updateData.action === 'DELETE') {
      console.log('收到帖子删除通知，重新加载待审核帖子列表...')
      
      // 重新加载待审核帖子列表和小红点
      await loadPendingPosts()
      await loadPendingCounts()
      
      // 显示删除通知
      ElMessage.info(updateData.message || '有帖子被删除')
      console.log('帖子删除处理完成')
    }
  } catch (error) {
    console.error('解析WebSocket消息失败:', error)
  }
}

// 创建帖子审核更新处理器
const handlePostReviewUpdate = async (data) => {
  console.log('收到帖子审核实时更新:', data)
  console.log('当前活跃标签页:', activeTab.value)
  console.log('当前帖子子标签页:', postSubTab.value)
  
  // 简化处理：只要收到帖子审核更新消息，就重新加载数据
  console.log('开始重新加载帖子审核列表...')
  
  try {
    // 使用Promise.all确保所有异步操作完成
    await Promise.all([
      loadPendingPosts(),
      loadApprovedPosts(), 
      loadRejectedPosts(),
      loadPendingCounts()
    ])
    
    // 显示更新通知
    ElMessage.success('帖子审核列表已更新')
    console.log('帖子审核列表更新完成')
  } catch (error) {
    console.error('重新加载帖子审核列表失败:', error)
    ElMessage.error('数据更新失败，请手动刷新页面')
  }
}

// 初始化加载数据
onMounted(async () => {
  console.log('AdminDashboard组件开始挂载...')
  
  // 先加载页面数据
  await loadPendingCounts()
  await loadPendingCats()
  // 初始化时加载救助数据，以便计算小红点
  await loadPendingRescues()
  
  console.log('页面数据加载完成，开始初始化WebSocket...')
  
  // 初始化WebSocket连接
  try {
    console.log('开始连接WebSocket...')
    
    // 检查是否已连接
    if (webSocketService.isConnected) {
      console.log('WebSocket已连接，跳过重复连接')
    } else {
      await webSocketService.connect()
      console.log('WebSocket连接成功')
    }
    
    // 检查连接状态
    console.log('WebSocket连接状态:', webSocketService.isConnected)
    
    // 检查用户身份识别状态
    console.log('管理端用户ID:', webSocketService.getUserId())
    
    // 添加领养审核实时更新处理器
    console.log('注册领养审核消息处理器...')
    adoptionReviewHandlerId = webSocketService.addAdoptionReviewHandler(handleAdoptionReviewUpdate)
    console.log('领养审核消息处理器已注册，ID:', adoptionReviewHandlerId)
    
    // 添加领养申请实时更新处理器
    console.log('注册领养申请消息处理器...')
    adoptionApplicationHandlerId = webSocketService.addAdoptionApplicationHandler(handleAdoptionApplicationUpdate)
    console.log('领养申请消息处理器已注册，ID:', adoptionApplicationHandlerId)
    
    // 添加帖子申请实时更新处理器
    console.log('注册帖子申请消息处理器...')
    postApplicationHandlerId = webSocketService.addPostApplicationHandler(handlePostApplicationUpdate)
    console.log('帖子申请消息处理器已注册，ID:', postApplicationHandlerId)
    
    // 添加帖子审核实时更新处理器
    console.log('注册帖子审核消息处理器...')
    postReviewHandlerId = webSocketService.addPostReviewHandler(handlePostReviewUpdate)
    console.log('帖子审核消息处理器已注册，ID:', postReviewHandlerId)
    
    // 检查已注册的处理器
    console.log('当前已注册的WebSocket处理器数量:', webSocketService.messageHandlers?.size || 0)
    console.log('所有已注册的处理器ID:', Array.from(webSocketService.messageHandlers?.keys() || []))
    
    // 测试消息处理器是否工作
    console.log('测试帖子申请消息处理器...')
    setTimeout(() => {
      console.log('发送测试消息到帖子申请处理器...')
      handlePostApplicationUpdate('{"postId":999,"action":"CREATE","userId":1,"status":"PENDING"}')
    }, 1000)
    
    // 测试消息处理器是否工作
    console.log('测试消息处理器...')
    setTimeout(() => {
      console.log('发送测试消息到处理器...')
      handleAdoptionReviewUpdate('测试消息')
    }, 1000)
    
    console.log('WebSocket领养审核实时更新已启用')
  } catch (error) {
    console.error('WebSocket连接失败:', error)
  }
})

onUnmounted(() => {
  // 清理WebSocket处理器
  if (adoptionReviewHandlerId) {
    webSocketService.removeMessageHandler(adoptionReviewHandlerId)
    adoptionReviewHandlerId = null
  }
  if (adoptionApplicationHandlerId) {
    webSocketService.removeMessageHandler(adoptionApplicationHandlerId)
    adoptionApplicationHandlerId = null
  }
  if (postApplicationHandlerId) {
    webSocketService.removeMessageHandler(postApplicationHandlerId)
    postApplicationHandlerId = null
  }
  if (postReviewHandlerId) {
    webSocketService.removeMessageHandler(postReviewHandlerId)
    postReviewHandlerId = null
  }
})
</script>

<style scoped>
.admin-dashboard {
  max-width: 1550px;
  margin: 0 auto;
  padding: 24px;
}

/* 详情对话框样式 */
.rescue-detail {
  padding: 20px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.detail-tags {
  display: flex;
  gap: 12px;
}

.detail-time {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
}

.detail-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #ebeef5;
}

.detail-section h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  border-left: 4px solid #67C23A;
  padding-left: 15px;
  display: flex;
  align-items: center;
}

.detail-section h3::before {
  content: "📝";
  margin-right: 8px;
  font-size: 16px;
}

.detail-section p {
  margin: 0;
  color: #606266;
  line-height: 1.7;
  font-size: 16px;
  background: #f8f9fa;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.contact-row {
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

.contact-column {
  flex: 1;
  min-width: 0;
}

.contact-info {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.contact-info .info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 15px;
  font-weight: 500;
  padding: 12px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.contact-info .info-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.contact-info .info-item .el-icon {
  color: #67C23A;
  font-size: 18px;
}


.full-width {
  grid-column: 1 / -1;
}

.reason {
  line-height: 1.6;
  white-space: pre-wrap;
}

/* 图片展示样式 */
.image-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 16px;
  margin-top: 15px;
}

.image-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.image-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.detail-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.detail-image:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 14px;
}

.no-images {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
  margin-top: 15px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  text-align: center;
}

/* 救援日志图片样式 - 与相关图片保持一致 */
.rescue-log-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 16px;
  margin-top: 15px;
}

.log-image-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.log-image-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.log-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.log-image:hover {
  transform: scale(1.05);
}

/* 详情对话框操作按钮样式 */
.detail-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  align-items: center;
  flex-wrap: wrap;
}

/* 救助需求详情对话框样式 */
.rescue-detail-dialog {
  max-height: 700px ;
  overflow: hidden ;
}

.rescue-detail-dialog .el-dialog__body {
  padding: 0 ;
  overflow: hidden ;
  max-height: 550px ;
}

/* 页面头部样式 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  height: 94px;
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

.page-title .el-icon {
  font-size: 28px;
}

.page-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

/* 统计卡片样式 */
.header-stats {
  display: flex;
  gap: 16px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  min-width: 100px;
  backdrop-filter: blur(10px);
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: white;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

/* 二级导航栏样式 */
.secondary-nav {
  margin-bottom: 24px;
}

.secondary-menu {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.secondary-menu .el-menu-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
}

.badge {
  position: absolute;
  bottom: 30px;
  right: 0px;
}

/* 内容区域样式 */
.content-area {
  min-height: 400px;
}

.content-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

/* 加载状态样式 */
.loading-container {
  padding: 40px 0;
}

.loading-container .el-skeleton {
  max-width: 600px;
  margin: 0 auto;
}

/* 空状态样式 */
.empty-state {
  padding: 40px 0;
  text-align: center;
}

/* 任务列表样式 */
.task-list {
  margin-top: 16px;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.task-item:last-child {
  border-bottom: none;
}

/* 筛选区域样式 */
.filters-section {
  margin-bottom: 20px;
}

/* 救助管理表格样式 */
.rescues-section {
  margin-top: 20px;
}

/* 猫咪审核表格样式 */
.cats-section {
  margin-top: 20px;
}

.cat-info {
  text-align: left;
}

.cat-name {
  align-items: center;
  gap: 8px;
}

.name-text {
  font-weight: 600;
  font-size: 14px;
}

.cat-description {
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}

.basic-info {
  font-size: 12px;
  color: #666;
  line-height: 1.6;
}

.cat-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
}

/* 帖子审核表格样式 */
.posts-section {
  margin-top: 20px;
}

.posts-section .el-table {
  cursor: pointer;
}

.posts-section .el-table .el-table__row:hover {
  background-color: #f5f7fa;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
}

.post-info {
  text-align: left;
}

.title-text {
  font-weight: 600;
  font-size: 14px;
}


.author-info {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}

.author-name {
  font-size: 14px;
  color: #333;
}



.post-content {
  font-size: 13px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  max-height: 1.5em;
}


.no-type {
  font-size: 13px;
  color: #999;
}

.text-center {
  text-align: center;
}

/* 帖子标题样式 - 只显示一行 */
.post-title {
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}


.rescue-title {
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.title-text {
  font-weight: 600;
  color: #303133;
}

.rescue-description {
  font-size: 12px;
  color: #606266;
  line-height: 1.4;
}

.location-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 领养申请卡片样式 */
.applications-section {
  margin-top: 20px;
}

.application-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.application-card {
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  background: white;
  transition: all 0.3s ease;
  overflow: hidden;
}

.application-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.application-card.status-pending {
  border-left: 4px solid #e6a23c;
}

.application-card.status-review {
  border-left: 4px solid #409eff;
}

.application-card.status-approved {
  border-left: 4px solid #67c23a;
}

.application-card.status-rejected {
  border-left: 4px solid #f56c6c;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 20px 0;
  margin-bottom: 16px;
}

.applicant-section {
  align-items: center;
  gap: 12px;
}

.applicant-info {
  flex: 1;
}


.apply-time {
  align-items: center;
  gap: 4px;
}

.status-badge {
  margin-left: 12px;
}

.card-body {
  padding: 0 20px;
}

.cat-section {
  margin-bottom: 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
}

.cat-info {
  align-items: center;
  gap: 12px;
}

.cat-details {
  flex: 1;
}

.cat-breed {
  font-size: 12px;
  color: #909399;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

/* 领养审核信息网格的info-item样式 */
.info-grid .info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.info-value {
  font-size: 13px;
  color: #303133;
  line-height: 1.4;
}

.info-value.reason {
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.card-footer {
  padding: 16px 20px 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 分页样式 */
.pagination-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.task-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.task-name {
  font-weight: 500;
  color: #333;
}

.task-author,
.task-description,
.task-time {
  font-size: 12px;
  color: #666;
}

.task-actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 768px) {
  .header-stats {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .stat-card {
    min-width: 80px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .task-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .task-actions {
    align-self: flex-end;
  }
  
}

/* 帖子详情对话框样式 */
.post-detail-dialog .el-dialog {
  margin-top: 5vh ;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
}

.post-detail-dialog .el-dialog__body {
  flex: 1;
  overflow: hidden;
  padding: 0;
}

.post-detail-container {
  padding: 0;
  max-height: 60vh;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 帖子标题区域 */
.post-title-section {
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f0f0;
}

.post-detail-container .post-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  line-height: 1.4;
  text-align: center;
}

/* 作者信息区域 */
.author-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.author-avatar {
  flex-shrink: 0;
}

.author-avatar-img {
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.avatar-placeholder {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.avatar-fallback {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
}

.author-details {
  flex: 1;
}

.post-detail-container .author-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.post-time {
  font-size: 14px;
  color: #909399;
}

/* 内容区域 */
.content-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.content-text {
  font-size: 16px;
  line-height: 1.7;
  color: #606266;
  white-space: pre-wrap;
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.reject-reason-text {
  font-size: 16px;
  line-height: 1.7;
  color: #606266;
  white-space: pre-wrap;
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #f56c6c;
}

/* 图片区域 */
.images-section {
  margin-bottom: 24px;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.post-image {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.post-image:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 14px;
  border-radius: 8px;
}

.no-image {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 200px;
  height: 150px;
  background: #f5f7fa;
  color: #909399;
  font-size: 14px;
  border-radius: 8px;
  border: 2px dashed #dcdfe6;
}

/* 审核状态和操作区域 */
.review-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-top: 24px;
}

.review-actions {
  display: flex;
  gap: 16px;
}

/* 领养申请详情美化样式 */
.adoption-detail {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 10px;
}

/* 猫咪信息区域新布局 */
.cat-info-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 10px 0;
}

/* 上半部分：三列布局 */
.cat-info-top {
  display: grid;
  grid-template-columns: 200px 1fr 1fr;
  gap: 20px;
  align-items: start;
}

.cat-image-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.cat-image-section .el-image {
  border: 3px solid #e6f7ff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.cat-image-section .el-image:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.cat-info-column {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 下半部分：两行布局 */
.cat-info-bottom {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.cat-info-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

/* 全宽度信息行 */
.full-width {
  grid-column: 1 / -1;
}

.applicant-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  padding: 10px 0;
}

.detail-row {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #67C23A;
  transition: all 0.3s ease;
}

.detail-row:hover {
  background: #f0f9ff;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.detail-label {
  font-weight: 600;
  color: #303133;
  min-width: 100px;
  margin-right: 12px;
  font-size: 14px;
}

.detail-value {
  color: #606266;
  font-size: 14px;
  flex: 1;
  word-break: break-word;
}

.reason-text {
  white-space: pre-wrap;
  border-radius: 6px;
}

.action-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.action-buttons .el-button {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.action-buttons .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 猫咪审核列表样式（模仿猫咪管理页面） */
.cat-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cat-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}


.cat-breed {
  font-size: 12px;
  color: #909399;
}

/* 表格中的猫咪信息样式 */
:deep(.el-table .cat-info) {
  justify-content: center;
}

:deep(.el-table .cat-details) {
  text-align: left;
}



:deep(.el-table .cat-breed) {
  font-size: 12px;
}

/* 猫咪详情对话框滚动条样式 */
.cat-detail {
  max-height: 600px;
  overflow-y: auto;
  padding-right: 10px;
}

.cat-detail::-webkit-scrollbar {
  width: 8px;
}

.cat-detail::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.cat-detail::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 4px;
}

.cat-detail::-webkit-scrollbar-thumb:hover {
  background: #909399;
}

/* 不同标签页的标题图标 */
.detail-section:nth-child(1) h3::before {
  content: "🐱";
}

.detail-section:nth-child(2) h3::before {
  content: "👤";
}

.detail-section:nth-child(3) h3::before {
  content: "✅";
}

.detail-section:nth-child(4) h3::before {
  content: "💬";
}
</style>