<template>
  <div class="page-container" v-loading="loading">
    <el-page-header @back="$router.back()" title="活动详情" />
    
    <el-card class="detail-card" v-if="activity">
      <div class="detail-header">
        <h1>{{ activity.title }}</h1>
        <div class="detail-tags">
          <el-tag :type="typeTagType(activity.activityType)">{{ activity.activityType }}</el-tag>
          <el-tag :type="statusTagType(activity.status)">{{ activity.statusText }}</el-tag>
        </div>
      </div>
      
      <el-descriptions :column="2" border class="detail-info">
        <el-descriptions-item label="活动地点">{{ activity.location }}</el-descriptions-item>
        <el-descriptions-item label="发布人">{{ activity.organizerName }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ activity.organizerContact || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报名人数">{{ activity.currentParticipants }}/{{ activity.maxParticipants || '不限' }}</el-descriptions-item>
        <el-descriptions-item label="活动开始">{{ activity.startTime }}</el-descriptions-item>
        <el-descriptions-item label="活动结束">{{ activity.endTime }}</el-descriptions-item>
        <el-descriptions-item label="报名开始">{{ activity.registerStart }}</el-descriptions-item>
        <el-descriptions-item label="报名截止">{{ activity.registerEnd }}</el-descriptions-item>
      </el-descriptions>
      
      <div class="detail-description">
        <h3>活动详情</h3>
        <p>{{ activity.description || '暂无活动详情描述' }}</p>
      </div>
      
      <div class="detail-actions">
        <el-button 
          v-if="activity.status === 3 && !activity.hasRegistered" 
          type="primary" 
          size="large"
          @click="handleRegister"
          :loading="registerLoading"
        >
          立即报名
        </el-button>
        <el-tag v-else-if="activity.hasRegistered" :type="registrationTagType(activity.registrationStatus)" size="large">
          已报名 - {{ registrationStatusText(activity.registrationStatus) }}
        </el-tag>
        <el-button v-if="activity.hasRegistered && activity.registrationStatus === 0" 
                   @click="handleCancel">取消报名</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getActivityDetail } from '@/api/activity'
import { registerActivity, cancelRegistration, listMyRegistrations } from '@/api/registration'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const registerLoading = ref(false)
const activity = ref(null)

const typeTagType = (type) => {
  const map = { '学术': 'primary', '文体': 'success', '志愿': 'warning', '社团': 'info' }
  return map[type] || ''
}

const statusTagType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'primary', 4: 'info', 5: 'danger' }
  return map[status] || ''
}

const registrationTagType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return map[status] || 'info'
}

const registrationStatusText = (status) => {
  const map = { 0: '审核中', 1: '已通过', 2: '已驳回', 3: '已取消' }
  return map[status] || '未知'
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getActivityDetail(route.params.id)
    if (res.code === 200) {
      activity.value = res.data
    }
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  registerLoading.value = true
  try {
    const res = await registerActivity(activity.value.id)
    if (res.code === 200) {
      ElMessage.success(res.message || '报名成功')
      loadDetail()
    }
  } finally {
    registerLoading.value = false
  }
}

const handleCancel = async () => {
  try {
    // 先查询报名ID
    const res = await listMyRegistrations({ pageNum: 1, pageSize: 100 })
    if (res.code === 200) {
      const reg = res.data.records.find(r => r.activityId === activity.value.id)
      if (reg) {
        const cancelRes = await cancelRegistration(reg.id)
        if (cancelRes.code === 200) {
          ElMessage.success('报名已取消')
          loadDetail()
        }
      }
    }
  } catch (error) {
    console.error('取消报名失败:', error)
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.detail-card {
  margin-top: 20px;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-header h1 {
  margin: 0 0 12px;
  font-size: 24px;
  color: #303133;
}

.detail-tags {
  display: flex;
  gap: 10px;
}

.detail-info {
  margin-bottom: 20px;
}

.detail-description {
  margin-bottom: 20px;
}

.detail-description h3 {
  margin-bottom: 12px;
  font-size: 16px;
  color: #303133;
}

.detail-description p {
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

.detail-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>
