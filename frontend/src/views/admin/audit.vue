<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px;">活动审核</h2>
    
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item>
          <el-input v-model="searchForm.keyword" placeholder="搜索活动名称" clearable @keyup.enter="loadData" style="width: 260px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <el-table :data="activityList" stripe v-loading="loading">
      <el-table-column prop="title" label="活动名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="activityType" label="类型" width="80" />
      <el-table-column prop="organizerName" label="发布人" width="100" />
      <el-table-column prop="location" label="地点" min-width="140" />
      <el-table-column prop="startTime" label="开始时间" width="160" />
      <el-table-column prop="maxParticipants" label="人数上限" width="90">
        <template #default="{ row }">{{ row.maxParticipants || '不限' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag type="warning" size="small">{{ row.statusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="160" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="success" @click="handleAudit(row.id, 2)">通过</el-button>
          <el-button link type="danger" @click="showRejectDialog(row.id)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="table-pagination" v-if="total > 0">
      <el-pagination v-model:current-page="searchForm.pageNum" v-model:page-size="searchForm.pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="loadData" />
    </div>
    
    <el-empty v-if="activityList.length === 0 && !loading" description="暂无待审核的活动" />

    <!-- 驳回对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回活动" width="400px">
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入驳回原因" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listActivities, auditActivity } from '@/api/activity'

const loading = ref(false)
const activityList = ref([])
const total = ref(0)
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentAuditId = ref(null)
const searchForm = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: 1 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await listActivities(searchForm)
    if (res.code === 200) { activityList.value = res.data.records || []; total.value = res.data.total }
  } finally { loading.value = false }
}

const resetSearch = () => { searchForm.keyword = ''; searchForm.pageNum = 1; loadData() }

const handleAudit = async (id, status) => {
  const res = await auditActivity(id, status)
  if (res.code === 200) { ElMessage.success('审核通过'); loadData() }
}

const showRejectDialog = (id) => { currentAuditId.value = id; rejectReason.value = ''; rejectDialogVisible.value = true }

const submitReject = async () => {
  const res = await auditActivity(currentAuditId.value, 1, rejectReason.value)
  if (res.code === 200) { ElMessage.success('已驳回'); rejectDialogVisible.value = false; loadData() }
}

onMounted(loadData)
</script>
