import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCurrentUser } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const roleCode = computed(() => userInfo.value?.roleCode || '')
  const isAdmin = computed(() => roleCode.value === 'admin')
  const isOrganizer = computed(() => roleCode.value === 'organizer')
  const isStudent = computed(() => roleCode.value === 'student')

  // Actions
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = info
  }

  const fetchUserInfo = async () => {
    try {
      const res = await getCurrentUser()
      if (res.code === 200) {
        userInfo.value = res.data
        return res.data
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
    return null
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    roleCode,
    isAdmin,
    isOrganizer,
    isStudent,
    setToken,
    setUserInfo,
    fetchUserInfo,
    logout
  }
})
