<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item>
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="搜索活动名称、地点" 
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.activityType" placeholder="活动类型" clearable style="width: 140px">
            <el-option 
              v-for="type in activityTypes" 
              :key="type" 
              :label="type" 
              :value="type" 
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.status" placeholder="活动状态" clearable style="width: 140px">
            <el-option label="已发布" :value="2" />
            <el-option label="报名中" :value="3" />
            <el-option label="已结束" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 活动列表 -->
    <el-row :gutter="20">
      <el-col 
        v-for="activity in activityList" 
        :key="activity.id" 
        :xs="24" :sm="12" :md="8" :lg="8" :xl="6"
        class="activity-col"
      >
        <el-card class="activity-card" :body-style="{ padding: '0' }" shadow="hover">
          <div class="activity-cover">
            <el-image 
              :src="activity.coverImage || defaultCover" 
              fit="cover"
              style="width: 100%; height: 160px"
            >
              <template #error>
                <div class="cover-placeholder">
                  <el-icon size="40"><Calendar /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="activity-type-tag">
              <el-tag size="small" :type="typeTagType(activity.activityType)">
                {{ activity.activityType }}
              </el-tag>
            </div>
            <div class="activity-status-badge" :class="`status-${activity.status}`">
              {{ activity.statusText }}
            </div>
          </div>
          
          <div class="activity-content">
            <h3 class="activity-title" @click="viewDetail(activity.id)">{{ activity.title }}</h3>
            <div class="activity-meta">
              <p><el-icon><Location /></el-icon> {{ activity.location }}</p>
              <p><el-icon><Clock /></el-icon> {{ activity.startTime }}</p>
              <p>
                <el-icon><User /></el-icon> 
                已报名 {{ activity.currentParticipants }}/{{ activity.maxParticipants || '不限' }}
              </p>
            </div>
            
            <div class="activity-footer">
              <span class="organizer">发布人：{{ activity.organizerName }}</span>
              <el-button 
                v-if="activity.status === 3 && !activity.hasRegistered" 
                type="primary" 
                size="small"
                @click="handleRegister(activity.id)"
                :loading="registerLoading === activity.id"
              >
                立即报名
              </el-button>
              <el-tag v-else-if="activity.hasRegistered" :type="registrationTagType(activity.registrationStatus)" size="small">
                {{ registrationStatusText(activity.registrationStatus) }}
              </el-tag>
              <el-button v-else link type="primary" size="small" @click="viewDetail(activity.id)">
                查看详情
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <el-empty v-if="activityList.length === 0 && !loading" description="暂无活动" />

    <!-- 分页 -->
    <div class="table-pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="searchForm.pageNum"
        v-model:page-size="searchForm.pageSize"
        :total="total"
        :page-sizes="[8, 12, 24, 48]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Calendar, Location, Clock, User } from '@element-plus/icons-vue'
import { listActivities, listActivityTypes } from '@/api/activity'
import { registerActivity } from '@/api/registration'
import { useUserStore } from '@/store'

const router = useRouter()
const userStore = useUserStore()
const defaultCover = 'https://images.unsplash.com/photo-1523580494863-6f3031224c94?w=400&h=200&fit=crop'

const loading = ref(false)
const registerLoading = ref(null)
const activityList = ref([])
const activityTypes = ref(['学术', '文体', '志愿', '社团', '其他'])
const total = ref(0)

const searchForm = reactive({
  pageNum: 1,
  pageSize: 12,
  keyword: '',
  activityType: '',
  status: ''
})

const typeTagType = (type) => {
  const map = { '学术': 'primary', '文体': 'success', '志愿': 'warning', '社团': 'info' }
  return map[type] || ''
}

const registrationTagType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return map[status] || 'info'
}

const registrationStatusText = (status) => {
  const map = { 0: '审核中', 1: '已通过', 2: '已驳回', 3: '已取消' }
  return map[status] || '未知'
}

const loadActivities = async () => {
  loading.value = true
  try {
    const res = await listActivities(searchForm)
    if (res.code === 200) {
      activityList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  searchForm.pageNum = 1
  loadActivities()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.activityType = ''
  searchForm.status = ''
  searchForm.pageNum = 1
  loadActivities()
}

const handleSizeChange = (val) => {
  searchForm.pageSize = val
  loadActivities()
}

const handlePageChange = (val) => {
  searchForm.pageNum = val
  loadActivities()
}

const viewDetail = (id) => {
  router.push(`/activity/${id}`)
}

const handleRegister = async (activityId) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await ElMessageBox.confirm('确定要报名该活动吗？', '确认报名', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    registerLoading.value = activityId
    const res = await registerActivity(activityId)
    if (res.code === 200) {
      ElMessage.success(res.message || '报名成功')
      loadActivities()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('报名失败:', error)
    }
  } finally {
    registerLoading.value = null
  }
}

onMounted(() => {
  loadActivities()
  listActivityTypes().then(res => {
    if (res.code === 200) {
      activityTypes.value = res.data
    }
  })
})
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.activity-col {
  margin-bottom: 20px;
}

.activity-card {
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.3s;
}

.activity-card:hover {
  transform: translateY(-4px);
}

.activity-cover {
  position: relative;
  background: #f5f7fa;
}

.cover-placeholder {
  width: 100%;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
}

.activity-type-tag {
  position: absolute;
  top: 10px;
  left: 10px;
}

.activity-status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  color: #fff;
  font-weight: 500;
}

.status-2 { background: #67c23a; }
.status-3 { background: #409eff; }
.status-4 { background: #909399; }

.activity-content {
  padding: 16px;
}

.activity-title {
  margin: 0 0 10px;
  font-size: 16px;
  color: #303133;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-title:hover {
  color: #409eff;
}

.activity-meta {
  font-size: 13px;
  color: #606266;
  margin-bottom: 12px;
}

.activity-meta p {
  margin: 4px 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
}

.organizer {
  font-size: 12px;
  color: #909399;
}
</style>
