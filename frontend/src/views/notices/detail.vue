<template>
  <div class="page-container" v-loading="loading">
    <el-page-header @back="$router.back()" title="公告详情" />
    
    <el-card class="notice-detail-card" v-if="notice">
      <h1 class="notice-title">
        <el-tag v-if="notice.top === 1" size="small" type="danger" effect="dark" style="margin-right: 8px;">置顶</el-tag>
        {{ notice.title }}
      </h1>
      <div class="notice-meta">
        <span><el-icon><User /></el-icon> {{ notice.publisherName }}</span>
        <span><el-icon><Clock /></el-icon> {{ notice.publishTime || notice.createTime }}</span>
        <span><el-icon><View /></el-icon> {{ notice.viewCount }} 次浏览</span>
      </div>
      <el-divider />
      <div class="notice-content">{{ notice.content }}</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getNoticeDetail } from '@/api/notice'
import { User, Clock, View } from '@element-plus/icons-vue'

const route = useRoute()
const loading = ref(false)
const notice = ref(null)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getNoticeDetail(route.params.id)
    if (res.code === 200) notice.value = res.data
  } finally { loading.value = false }
})
</script>

<style scoped>
.notice-detail-card { margin-top: 20px; }
.notice-title { font-size: 22px; color: #303133; margin: 0 0 16px; }
.notice-meta { display: flex; gap: 24px; font-size: 13px; color: #909399; }
.notice-meta span { display: flex; align-items: center; gap: 4px; }
.notice-content { font-size: 15px; color: #303133; line-height: 1.8; white-space: pre-wrap; }
</style>
