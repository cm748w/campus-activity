<template>
  <div class="page-container">
    <el-page-header @back="$router.back()" :title="`报名管理 - ${activityTitle}`" />
    
    <div class="search-form" style="margin-top: 20px;">
      <el-form :model="searchForm" inline>
        <el-form-item>
          <el-input v-model="searchForm.keyword" placeholder="搜索姓名/学号" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已驳回" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <el-table :data="registrationList" stripe v-loading="loading">
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="department" label="院系" min-width="120" />
      <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      <el-table-column prop="registerTime" label="报名时间" width="160" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button link type="success" @click="handleAudit(row.id, 1)">通过</el-button>
            <el-button link type="danger" @click="showRejectDialog(row.id)">驳回</el-button>
          </template>
          <span v-else-if="row.status === 1" class="text-success">已通过</span>
          <span v-else class="text-danger">
            已驳回
            <el-tooltip v-if="row.rejectReason" :content="row.rejectReason" placement="top">
              <el-icon><QuestionFilled /></el-icon>
            </el-tooltip>
          </span>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="table-pagination" v-if="total > 0">
      <el-pagination v-model:current-page="searchForm.pageNum" v-model:page-size="searchForm.pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="loadData" />
    </div>

    <!-- 驳回对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回报名" width="400px">
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
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { QuestionFilled } from '@element-plus/icons-vue'
import { listRegistrations, auditRegistration } from '@/api/registration'
import { getActivityDetail } from '@/api/activity'

const route = useRoute()
const loading = ref(false)
const activityTitle = ref('')
const registrationList = ref([])
const total = ref(0)
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentAuditId = ref(null)

const searchForm = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: '' })

const statusTagType = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }[s] || '')
const statusText = (s) => ({ 0: '待审核', 1: '已通过', 2: '已驳回', 3: '已取消' }[s] || '未知')

const loadData = async () => {
  loading.value = true
  try {
    const res = await listRegistrations(route.params.activityId, searchForm)
    if (res.code === 200) { registrationList.value = res.data.records || []; total.value = res.data.total }
  } finally { loading.value = false }
}

const resetSearch = () => { searchForm.keyword = ''; searchForm.status = ''; searchForm.pageNum = 1; loadData() }

const handleAudit = async (id, status) => {
  const res = await auditRegistration(id, status)
  if (res.code === 200) { ElMessage.success('审核成功'); loadData() }
}

const showRejectDialog = (id) => { currentAuditId.value = id; rejectReason.value = ''; rejectDialogVisible.value = true }

const submitReject = async () => {
  if (!rejectReason.value.trim()) { ElMessage.warning('请输入驳回原因'); return }
  const res = await auditRegistration(currentAuditId.value, 2, rejectReason.value)
  if (res.code === 200) { ElMessage.success('已驳回'); rejectDialogVisible.value = false; loadData() }
}

onMounted(async () => {
  const res = await getActivityDetail(route.params.activityId)
  if (res.code === 200) activityTitle.value = res.data.title
  loadData()
})
</script>

<style scoped>
.text-success { color: #67c23a; }
.text-danger { color: #f56c6c; }
</style>
