<template>
  <div class="page-container">
    <h2 style="margin-bottom: 20px;">公告通知</h2>
    
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item>
          <el-input v-model="searchForm.keyword" placeholder="搜索公告标题" clearable @keyup.enter="loadData" style="width: 280px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <el-card v-for="notice in noticeList" :key="notice.id" class="notice-card" shadow="hover" @click="viewDetail(notice.id)">
      <div class="notice-header">
        <h3>
          <el-tag v-if="notice.top === 1" size="small" type="danger" effect="dark" style="margin-right: 8px;">置顶</el-tag>
          {{ notice.title }}
        </h3>
        <span class="notice-time">{{ notice.publishTime || notice.createTime }}</span>
      </div>
      <p class="notice-preview">{{ notice.content?.substring(0, 150) }}...</p>
      <div class="notice-footer">
        <span>发布人：{{ notice.publisherName }}</span>
        <span><el-icon><View /></el-icon> {{ notice.viewCount || 0 }} 次浏览</span>
      </div>
    </el-card>
    
    <div class="table-pagination" v-if="total > 0">
      <el-pagination v-model:current-page="searchForm.pageNum" v-model:page-size="searchForm.pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="loadData" />
    </div>
    
    <el-empty v-if="noticeList.length === 0 && !loading" description="暂无公告" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listNotices } from '@/api/notice'
import { View } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const noticeList = ref([])
const total = ref(0)
const searchForm = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: 1 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await listNotices(searchForm)
    if (res.code === 200) { noticeList.value = res.data.records || []; total.value = res.data.total }
  } finally { loading.value = false }
}

const resetSearch = () => { searchForm.keyword = ''; searchForm.pageNum = 1; loadData() }
const viewDetail = (id) => router.push(`/notice/${id}`)

onMounted(loadData)
</script>

<style scoped>
.notice-card { margin-bottom: 16px; cursor: pointer; transition: transform 0.3s; }
.notice-card:hover { transform: translateY(-2px); }
.notice-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.notice-header h3 { margin: 0; font-size: 16px; color: #303133; }
.notice-time { font-size: 13px; color: #909399; }
.notice-preview { color: #606266; font-size: 14px; line-height: 1.6; margin: 0 0 10px; }
.notice-footer { display: flex; justify-content: space-between; font-size: 12px; color: #909399; }
</style>
