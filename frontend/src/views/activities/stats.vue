<template>
  <div class="page-container">
    <el-page-header @back="$router.back()" title="活动统计" />
    
    <el-card class="stats-card" v-loading="loading">
      <template #header>
        <span>报名数据统计</span>
      </template>
      
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <div class="stat-box">
            <div class="stat-number">{{ stats.totalCount }}</div>
            <div class="stat-label">总报名人数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-box approved">
            <div class="stat-number">{{ stats.approvedCount }}</div>
            <div class="stat-label">已通过</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-box pending">
            <div class="stat-number">{{ stats.pendingCount }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-box rejected">
            <div class="stat-number">{{ stats.rejectedCount }}</div>
            <div class="stat-label">已驳回</div>
          </div>
        </el-col>
      </el-row>
      
      <div class="stats-progress">
        <div class="progress-label">
          <span>报名进度</span>
          <span>{{ stats.currentParticipants }}/{{ stats.maxParticipants > 0 ? stats.maxParticipants : '不限' }}</span>
        </div>
        <el-progress 
          :percentage="participationRate" 
          :status="participationRate >= 100 ? 'success' : ''"
          :stroke-width="18"
        />
        <div class="approval-rate">通过率：{{ stats.approvalRate }}</div>
      </div>
      
      <div class="export-action">
        <el-button type="primary" @click="handleExport">
          <el-icon><Download /></el-icon> 导出报名名单
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getActivityStats } from '@/api/activity'
import { exportApprovedList } from '@/api/registration'
import { Download } from '@element-plus/icons-vue'

const route = useRoute()
const loading = ref(false)
const stats = ref({ totalCount: 0, approvedCount: 0, pendingCount: 0, rejectedCount: 0, approvalRate: '0%', currentParticipants: 0, maxParticipants: 0 })

const participationRate = computed(() => {
  if (!stats.value.maxParticipants || stats.value.maxParticipants === 0) return 0
  return Math.min(Math.round((stats.value.currentParticipants / stats.value.maxParticipants) * 100), 100)
})

const loadStats = async () => {
  loading.value = true
  try {
    const res = await getActivityStats(route.params.id)
    if (res.code === 200) stats.value = res.data
  } finally { loading.value = false }
}

const handleExport = () => exportApprovedList(route.params.id)

onMounted(loadStats)
</script>

<style scoped>
.stats-card { margin-top: 20px; }
.stats-row { margin-bottom: 30px; }
.stat-box {
  text-align: center;
  padding: 20px;
  border-radius: 12px;
  background: #f5f7fa;
}
.stat-box.approved { background: #f0f9ff; }
.stat-box.pending { background: #fffaf0; }
.stat-box.rejected { background: #fff5f5; }
.stat-number {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
}
.stat-box.approved .stat-number { color: #409eff; }
.stat-box.pending .stat-number { color: #e6a23c; }
.stat-box.rejected .stat-number { color: #f56c6c; }
.stat-label { font-size: 14px; color: #606266; margin-top: 6px; }
.stats-progress { margin: 30px 0; }
.progress-label { display: flex; justify-content: space-between; margin-bottom: 10px; color: #606266; }
.approval-rate { margin-top: 10px; color: #409eff; font-size: 14px; }
.export-action { text-align: center; margin-top: 20px; padding-top: 20px; border-top: 1px solid #ebeef5; }
</style>
