<template>
  <div class="dashboard-container">
    <!-- 欢迎语 -->
    <el-card class="welcome-card">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>欢迎回来，{{ userStore.userInfo?.realName || '同学' }}！</h2>
          <p>{{ welcomeMessage }}</p>
        </div>
        <el-avatar :size="64" :src="userStore.userInfo?.avatar || defaultAvatar" />
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-icon blue"><el-icon size="28"><Calendar /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.activityCount }}</div>
            <div class="stat-label">正在进行的活动</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-icon green"><el-icon size="28"><DocumentChecked /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.myRegistrationCount }}</div>
            <div class="stat-label">我的报名</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-icon orange"><el-icon size="28"><Timer /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingAuditCount }}</div>
            <div class="stat-label">待审核报名</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-icon purple"><el-icon size="28"><Bell /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.noticeCount }}</div>
            <div class="stat-label">最新公告</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最新活动和公告 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span><el-icon><Star /></el-icon> 最新活动</span>
              <el-link type="primary" @click="$router.push('/activities')">查看更多</el-link>
            </div>
          </template>
          <el-table :data="latestActivities" stripe style="width: 100%">
            <el-table-column prop="title" label="活动名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="activityType" label="类型" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="typeTagType(row.activityType)">
                  {{ row.activityType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="location" label="地点" min-width="150" show-overflow-tooltip />
            <el-table-column prop="registerEnd" label="报名截止" width="160" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewActivity(row.id)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span><el-icon><BellFilled /></el-icon> 系统公告</span>
              <el-link type="primary" @click="$router.push('/notices')">更多</el-link>
            </div>
          </template>
          <div class="notice-list">
            <div 
              v-for="notice in latestNotices" 
              :key="notice.id" 
              class="notice-item"
              @click="viewNotice(notice.id)"
            >
              <el-tag v-if="notice.top === 1" size="small" type="danger" effect="dark">置顶</el-tag>
              <span class="notice-title" :class="{ 'top-notice': notice.top === 1 }">
                {{ notice.title }}
              </span>
              <span class="notice-time">{{ formatDate(notice.createTime) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, DocumentChecked, Timer, Bell, Star, BellFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/store'
import { listActivities } from '@/api/activity'
import { listLatestNotices } from '@/api/notice'
import { listMyRegistrations } from '@/api/registration'

const router = useRouter()
const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const stats = ref({
  activityCount: 0,
  myRegistrationCount: 0,
  pendingAuditCount: 0,
  noticeCount: 0
})
const latestActivities = ref([])
const latestNotices = ref([])

const welcomeMessage = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好！新的一天，去发现精彩的校园活动吧！'
  if (hour < 18) return '下午好！充实的一天，参加个活动放松一下吧！'
  return '晚上好！今天过得怎么样？'
})

const typeTagType = (type) => {
  const map = { '学术': 'primary', '文体': 'success', '志愿': 'warning', '社团': 'info' }
  return map[type] || ''
}

const formatDate = (date) => {
  if (!date) return ''
  return date.split(' ')[0]
}

const viewActivity = (id) => router.push(`/activity/${id}`)
const viewNotice = (id) => router.push(`/notice/${id}`)

const loadData = async () => {
  try {
    // 最新活动
    const actRes = await listActivities({ pageNum: 1, pageSize: 5 })
    if (actRes.code === 200) {
      latestActivities.value = actRes.data.records || []
      stats.value.activityCount = actRes.data.total || 0
    }
    
    // 最新公告
    const noticeRes = await listLatestNotices(6)
    if (noticeRes.code === 200) {
      latestNotices.value = noticeRes.data.records || []
      stats.value.noticeCount = noticeRes.data.total || 0
    }
    
    // 我的报名
    const regRes = await listMyRegistrations({ pageNum: 1, pageSize: 1 })
    if (regRes.code === 200) {
      stats.value.myRegistrationCount = regRes.data.total || 0
    }
  } catch (error) {
    console.error('加载首页数据失败:', error)
  }
}

onMounted(loadData)
</script>

<style scoped>
.welcome-card {
  margin-bottom: 20px;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h2 {
  margin: 0 0 8px;
  font-size: 20px;
  color: #303133;
}

.welcome-text p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-icon.blue { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-icon.green { background: linear-gradient(135deg, #11998e, #38ef7d); }
.stat-icon.orange { background: linear-gradient(135deg, #f093fb, #f5576c); }
.stat-icon.purple { background: linear-gradient(135deg, #4facfe, #00f2fe); }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.notice-list {
  max-height: 400px;
  overflow-y: auto;
}

.notice-item {
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: background 0.3s;
}

.notice-item:hover {
  background: #f5f7fa;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-title {
  flex: 1;
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.top-notice {
  color: #f56c6c;
  font-weight: 500;
}

.notice-time {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}
</style>
