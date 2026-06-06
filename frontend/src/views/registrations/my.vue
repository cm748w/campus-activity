<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px;">我的报名</h2>
    
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item>
          <el-select v-model="searchForm.status" placeholder="报名状态" clearable style="width: 140px">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已驳回" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">筛选</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <el-table :data="registrationList" stripe v-loading="loading">
      <el-table-column prop="activity.title" label="活动名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="activity.location" label="活动地点" min-width="140" />
      <el-table-column prop="activity.startTime" label="活动时间" width="160" />
      <el-table-column prop="realName" label="报名姓名" width="100" />
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="registerTime" label="报名时间" width="160" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" link type="danger" @click="handleCancel(row.id)">取消报名</el-button>
          <el-button link type="primary" @click="viewActivity(row.activityId)">查看活动</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="table-pagination" v-if="total > 0">
      <el-pagination v-model:current-page="searchForm.pageNum" v-model:page-size="searchForm.pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="loadData" />
    </div>
    
    <el-empty v-if="registrationList.length === 0 && !loading" description="暂无报名记录，去活动广场看看吧！">
      <el-button type="primary" @click="$router.push('/activities')">去报名</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listMyRegistrations, cancelRegistration } from '@/api/registration'

const router = useRouter()
const loading = ref(false)
const registrationList = ref([])
const total = ref(0)
const searchForm = reactive({ pageNum: 1, pageSize: 10, status: '' })

const statusTagType = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }[s] || '')

const loadData = async () => {
  loading.value = true
  try {
    const res = await listMyRegistrations(searchForm)
    if (res.code === 200) { registrationList.value = res.data.records || []; total.value = res.data.total }
  } finally { loading.value = false }
}

const resetSearch = () => { searchForm.status = ''; searchForm.pageNum = 1; loadData() }

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消该报名吗？', '确认取消', { type: 'warning' })
    const res = await cancelRegistration(id)
    if (res.code === 200) { ElMessage.success('报名已取消'); loadData() }
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const viewActivity = (id) => router.push(`/activity/${id}`)

onMounted(loadData)
</script>
