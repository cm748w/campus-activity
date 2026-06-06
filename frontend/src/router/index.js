import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册', public: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'activities',
        name: 'Activities',
        component: () => import('@/views/activities/index.vue'),
        meta: { title: '活动广场', icon: 'Calendar' }
      },
      {
        path: 'activity/:id',
        name: 'ActivityDetail',
        component: () => import('@/views/activities/detail.vue'),
        meta: { title: '活动详情', hidden: true }
      },
      {
        path: 'my-registrations',
        name: 'MyRegistrations',
        component: () => import('@/views/registrations/my.vue'),
        meta: { title: '我的报名', icon: 'DocumentChecked' }
      },
      {
        path: 'my-activities',
        name: 'MyActivities',
        component: () => import('@/views/activities/my.vue'),
        meta: { title: '我的活动', icon: 'List', roles: ['organizer'] }
      },
      {
        path: 'activity-stats/:id',
        name: 'ActivityStats',
        component: () => import('@/views/activities/stats.vue'),
        meta: { title: '活动统计', hidden: true, roles: ['organizer'] }
      },
      {
        path: 'registrations/:activityId',
        name: 'Registrations',
        component: () => import('@/views/registrations/index.vue'),
        meta: { title: '报名管理', hidden: true, roles: ['organizer'] }
      },
      {
        path: 'notices',
        name: 'Notices',
        component: () => import('@/views/notices/index.vue'),
        meta: { title: '公告通知', icon: 'BellFilled' }
      },
      {
        path: 'notice/:id',
        name: 'NoticeDetail',
        component: () => import('@/views/notices/detail.vue'),
        meta: { title: '公告详情', hidden: true }
      },
      {
        path: 'user-management',
        name: 'UserManagement',
        component: () => import('@/views/admin/users.vue'),
        meta: { title: '用户管理', icon: 'UserFilled', roles: ['admin'] }
      },
      {
        path: 'activity-audit',
        name: 'ActivityAudit',
        component: () => import('@/views/admin/audit.vue'),
        meta: { title: '活动审核', icon: 'CircleCheck', roles: ['admin'] }
      },
      {
        path: 'notice-management',
        name: 'NoticeManagement',
        component: () => import('@/views/admin/notices.vue'),
        meta: { title: '公告管理', icon: 'Management', roles: ['admin'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', hidden: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title + ' - 校园活动报名系统'
  }

  // 公开页面直接放行
  if (to.meta.public) {
    next()
    return
  }

  // 未登录跳转到登录页
  if (!userStore.token) {
    next('/login')
    return
  }

  // 检查角色权限
  if (to.meta.roles && !to.meta.roles.includes(userStore.roleCode)) {
    next('/dashboard')
    return
  }

  next()
})

export default router
