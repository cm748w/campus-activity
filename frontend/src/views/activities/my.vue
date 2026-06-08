<template>
  <div class="page-container">
    <div class="card-header" style="margin-bottom: 20px;">
      <h2>我的活动</h2>
      <el-button type="primary" @click="showPublishDialog = true">
        <el-icon><Plus /></el-icon> 发布活动
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item>
          <el-input v-model="searchForm.keyword" placeholder="搜索活动" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 130px">
            <el-option label="草稿" :value="0" />
            <el-option label="待审核" :value="1" />
            <el-option label="已发布" :value="2" />
            <el-option label="报名中" :value="3" />
            <el-option label="已结束" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 活动列表 -->
    <el-table :data="activityList" stripe v-loading="loading">
      <el-table-column prop="title" label="活动名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="activityType" label="类型" width="80" />
      <el-table-column prop="location" label="地点" min-width="140" show-overflow-tooltip />
      <el-table-column prop="startTime" label="开始时间" width="160" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="currentParticipants" label="报名人数" width="100">
        <template #default="{ row }">
          {{ row.currentParticipants }}/{{ row.maxParticipants || '不限' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="viewRegistrations(row.id)">报名管理</el-button>
          <el-button link type="primary" @click="viewStats(row.id)">统计</el-button>
          <el-button link type="primary" @click="editActivity(row)" :disabled="row.status >= 4">编辑</el-button>
          <el-popconfirm title="确定删除该活动吗？" @confirm="deleteAct(row.id)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="table-pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="searchForm.pageNum"
        v-model:page-size="searchForm.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </div>

    <!-- 发布活动对话框 -->
    <el-dialog v-model="showPublishDialog" title="发布活动" width="600px" destroy-on-close>
      <el-form :model="publishForm" :rules="publishRules" ref="publishFormRef" label-width="100px">
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="publishForm.title" placeholder="请输入活动标题" />
        </el-form-item>
        <el-form-item label="活动类型" prop="activityType">
          <el-select v-model="publishForm.activityType" placeholder="选择类型" style="width: 100%">
            <el-option v-for="type in activityTypes" :key="type" :label="type" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="publishForm.location" placeholder="请输入活动地点" />
        </el-form-item>
        <el-form-item label="活动描述" prop="description">
          <el-input v-model="publishForm.description" type="textarea" :rows="4" placeholder="请输入活动详情描述" />
        </el-form-item>
        <el-form-item label="活动开始" prop="startTime">
          <el-date-picker v-model="publishForm.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="活动结束" prop="endTime">
          <el-date-picker v-model="publishForm.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="报名开始" prop="registerStart">
          <el-date-picker v-model="publishForm.registerStart" type="datetime" placeholder="选择报名开始时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="报名截止" prop="registerEnd">
          <el-date-picker v-model="publishForm.registerEnd" type="datetime" placeholder="选择报名截止时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="人数上限" prop="maxParticipants">
          <el-input-number v-model="publishForm.maxParticipants" :min="0" :max="9999" style="width: 100%" />
          <span class="form-tip">0表示不限人数</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPublishDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPublish" :loading="publishLoading">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listOrganizerActivities, publishActivity, updateActivity, deleteActivity, listActivityTypes } from '@/api/activity'

const router = useRouter()
const loading = ref(false)
const publishLoading = ref(false)
const showPublishDialog = ref(false)
const activityList = ref([])
const activityTypes = ref(['学术', '文体', '志愿', '社团', '其他'])
const total = ref(0)
const publishFormRef = ref(null)
const isEdit = ref(false)

const searchForm = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: '' })

const publishForm = reactive({
  id: null, title: '', activityType: '', location: '', description: '',
  startTime: '', endTime: '', registerStart: '', registerEnd: '', maxParticipants: 0
})

const publishRules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  activityType: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择活动开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择活动结束时间', trigger: 'change' }],
  registerStart: [{ required: true, message: '请选择报名开始时间', trigger: 'change' }],
  registerEnd: [{ required: true, message: '请选择报名截止时间', trigger: 'change' }]
}

const statusTagType = (s) => ({ 0: 'info', 1: 'warning', 2: 'success', 3: 'primary', 4: 'info', 5: 'danger' }[s] || '')

const loadData = async () => {
  loading.value = true
  try {
    const res = await listOrganizerActivities(searchForm)
    if (res.code === 200) { activityList.value = res.data.records || []; total.value = res.data.total }
  } finally { loading.value = false }
}

const resetSearch = () => { searchForm.keyword = ''; searchForm.status = ''; searchForm.pageNum = 1; loadData() }
const viewRegistrations = (id) => router.push(`/registrations/${id}`)
const viewStats = (id) => router.push(`/activity-stats/${id}`)

const editActivity = (row) => {
  isEdit.value = true
  Object.assign(publishForm, row)
  showPublishDialog.value = true
}

const submitPublish = async () => {
  const valid = await publishFormRef.value?.validate().catch(() => false)
  if (!valid) return
  publishLoading.value = true
  try {
    const res = isEdit.value
      ? await updateActivity(publishForm.id, publishForm)
      : await publishActivity(publishForm)
    if (res.code === 200) {
      ElMessage.success(res.message || '操作成功')
      showPublishDialog.value = false
      resetPublishForm()
      loadData()
    }
  } finally { publishLoading.value = false }
}

const resetPublishForm = () => {
  isEdit.value = false
  Object.keys(publishForm).forEach(k => publishForm[k] = k === 'maxParticipants' ? 0 : null)
}

const deleteAct = async (id) => {
  const res = await deleteActivity(id)
  if (res.code === 200) { ElMessage.success('删除成功'); loadData() }
}

onMounted(() => { loadData(); listActivityTypes().then(r => { if (r.code === 200) activityTypes.value = r.data }) })
</script>

<style scoped>
.form-tip { font-size: 12px; color: #909399; margin-left: 8px; }
</style>
