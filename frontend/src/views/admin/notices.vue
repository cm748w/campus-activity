<template>
  <div class="page-container">
    <div class="card-header" style="margin-bottom: 20px;">
      <h2>公告管理</h2>
      <el-button type="primary" @click="showDialog = true">
        <el-icon><Plus /></el-icon> 发布公告
      </el-button>
    </div>
    
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item>
          <el-input v-model="searchForm.keyword" placeholder="搜索公告标题" clearable @keyup.enter="loadData" style="width: 260px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <el-table :data="noticeList" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="publisherName" label="发布人" width="100" />
      <el-table-column prop="top" label="置顶" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.top === 1" type="danger" size="small">置顶</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column prop="publishTime" label="发布时间" width="160" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="editNotice(row)">编辑</el-button>
          <el-popconfirm title="确定删除该公告吗？" @confirm="deleteNoticeRow(row.id)">
            <template #reference><el-button link type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="table-pagination" v-if="total > 0">
      <el-pagination v-model:current-page="searchForm.pageNum" v-model:page-size="searchForm.pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="loadData" />
    </div>

    <!-- 公告对话框 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑公告' : '发布公告'" width="600px" destroy-on-close>
      <el-form :model="noticeForm" :rules="noticeRules" ref="noticeFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="noticeForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="noticeForm.content" type="textarea" :rows="8" placeholder="请输入公告内容" />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="noticeForm.top" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitNotice" :loading="submitLoading">{{ isEdit ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listNotices, publishNotice, updateNotice, deleteNotice } from '@/api/notice'

const loading = ref(false)
const submitLoading = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const noticeList = ref([])
const total = ref(0)
const noticeFormRef = ref(null)

const searchForm = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const noticeForm = reactive({ id: null, title: '', content: '', top: 0 })
const noticeRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await listNotices(searchForm)
    if (res.code === 200) { noticeList.value = res.data.records || []; total.value = res.data.total }
  } finally { loading.value = false }
}

const resetSearch = () => { searchForm.keyword = ''; searchForm.pageNum = 1; loadData() }

const editNotice = (row) => {
  isEdit.value = true
  Object.assign(noticeForm, row)
  showDialog.value = true
}

const submitNotice = async () => {
  const valid = await noticeFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const api = isEdit.value ? updateNotice : publishNotice
    const res = await api(noticeForm.id, noticeForm)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '保存成功' : '发布成功')
      showDialog.value = false
      resetForm()
      loadData()
    }
  } finally { submitLoading.value = false }
}

const deleteNoticeRow = async (id) => {
  const res = await deleteNotice(id)
  if (res.code === 200) { ElMessage.success('删除成功'); loadData() }
}

const resetForm = () => {
  isEdit.value = false
  noticeForm.id = null; noticeForm.title = ''; noticeForm.content = ''; noticeForm.top = 0
}

onMounted(loadData)
</script>
