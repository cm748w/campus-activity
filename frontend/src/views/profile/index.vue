<template>
  <div class="page-container">
    <el-card>
      <el-tabs v-model="activeTab">
        <!-- 个人信息 -->
        <el-tab-pane label="个人信息" name="profile">
          <el-form :model="userForm" label-width="100px" style="max-width: 500px;">
            <el-form-item label="用户名">
              <el-input v-model="userForm.username" disabled />
            </el-form-item>
            <el-form-item label="真实姓名">
              <el-input v-model="userForm.realName" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="userForm.phone" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="userForm.email" />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="userForm.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
                <el-radio :label="0">保密</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="院系/部门">
              <el-input v-model="userForm.department" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="100px" style="max-width: 500px;">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码（6-20位）" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword" :loading="pwdLoading">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCurrentUser, updateUserInfo, updatePassword } from '@/api/user'
import { useUserStore } from '@/store'

const route = useRoute()
const userStore = useUserStore()
const activeTab = ref(route.query.tab || 'profile')
const saving = ref(false)
const pwdLoading = ref(false)
const pwdFormRef = ref(null)

const userForm = reactive({ username: '', realName: '', phone: '', email: '', gender: 0, department: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, max: 20, message: '长度6-20位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, {
    validator: (rule, value, callback) => {
      if (value !== pwdForm.newPassword) callback(new Error('两次输入不一致'))
      else callback()
    }, trigger: 'blur'
  }]
}

const loadUserInfo = async () => {
  const res = await getCurrentUser()
  if (res.code === 200) Object.assign(userForm, res.data)
}

const saveProfile = async () => {
  saving.value = true
  try {
    const res = await updateUserInfo(userForm)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      userStore.fetchUserInfo()
    }
  } finally { saving.value = false }
}

const changePassword = async () => {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return
  pwdLoading.value = true
  try {
    const res = await updatePassword(pwdForm)
    if (res.code === 200) {
      ElMessage.success(res.message || '密码修改成功')
      pwdForm.oldPassword = ''; pwdForm.newPassword = ''; pwdForm.confirmPassword = ''
    }
  } finally { pwdLoading.value = false }
}

onMounted(loadUserInfo)
</script>
