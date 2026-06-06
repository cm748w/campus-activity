<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <el-icon size="48" color="#409EFF"><School /></el-icon>
        <h1>校园活动报名系统</h1>
        <p>Campus Activity Registration System</p>
      </div>
      
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名/学号/工号"
            size="large"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            class="login-btn" 
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
        
        <div class="login-options">
          <el-link type="primary" @click="$router.push('/register')">还没有账号？立即注册</el-link>
        </div>
        
        <el-divider>测试账号</el-divider>
        <div class="test-accounts">
          <el-tag size="small" type="info" class="account-tag">管理员: admin/123456</el-tag>
          <el-tag size="small" type="info" class="account-tag">负责人: 2021001/123456</el-tag>
          <el-tag size="small" type="info" class="account-tag">学生: 20220101/123456</el-tag>
        </div>
      </el-form>
    </div>
    
    <div class="login-footer">
      <p>© 2024 校园活动报名系统 | 课程设计项目</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, School } from '@element-plus/icons-vue'
import { login } from '@/api/user'
import { useUserStore } from '@/store'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login(form)
    if (res.code === 200) {
      const { token, userInfo } = res.data
      userStore.setToken(token)
      userStore.setUserInfo(userInfo)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 24px;
  color: #303133;
  margin: 12px 0 4px;
}

.login-header p {
  font-size: 13px;
  color: #909399;
}

.login-form {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
}

.login-options {
  text-align: center;
  margin-top: 10px;
}

.test-accounts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.account-tag {
  cursor: pointer;
}

.login-footer {
  position: fixed;
  bottom: 20px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
}
</style>
