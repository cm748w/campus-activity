<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px;">用户管理</h2>
    
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item>
          <el-input v-model="searchForm.keyword" placeholder="搜索用户名/姓名/院系" clearable @keyup.enter="loadData" style="width: 260px" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.roleCode" placeholder="角色" clearable style="width: 140px">
            <el-option label="管理员" value="admin" />
            <el-option label="负责人" value="organizer" />
            <el-option label="学生" value="student" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <el-table :data="userList" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="真实姓名" width="100" />
      <el-table-column prop="roleName" label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="roleTagType(row.roleCode)" size="small">{{ row.roleName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="department" label="院系" min-width="120" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="160" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" link type="danger" @click="toggleStatus(row.id, 0)">禁用</el-button>
          <el-button v-else link type="success" @click="toggleStatus(row.id, 1)">启用</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="table-pagination" v-if="total > 0">
      <el-pagination v-model:current-page="searchForm.pageNum" v-model:page-size="searchForm.pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="loadData" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsers, updateUserStatus } from '@/api/user'

const loading = ref(false)
const userList = ref([])
const total = ref(0)
const searchForm = reactive({ pageNum: 1, pageSize: 10, keyword: '', roleCode: '' })

const roleTagType = (code) => ({ admin: 'danger', organizer: 'warning', student: 'success' }[code] || '')

const loadData = async () => {
  loading.value = true
  try {
    const res = await listUsers(searchForm)
    if (res.code === 200) { userList.value = res.data.records || []; total.value = res.data.total }
  } finally { loading.value = false }
}

const resetSearch = () => { searchForm.keyword = ''; searchForm.roleCode = ''; searchForm.pageNum = 1; loadData() }

const toggleStatus = async (id, status) => {
  try {
    await ElMessageBox.confirm(`确定要${status === 1 ? '启用' : '禁用'}该用户吗？`, '确认', { type: 'warning' })
    const res = await updateUserStatus(id, status)
    if (res.code === 200) { ElMessage.success('操作成功'); loadData() }
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

onMounted(loadData)
</script>
