import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Profile from '../views/Profile.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('../layout/Index.vue'),
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('../views/HomeView.vue') // 或者保留为空，由 beforeEach 处理
        },
        {
          path: 'square',
          name: 'square',
          component: () => import('../views/CourseSquare.vue')
        },
        {
          path: 'my-courses',
          name: 'myCourses',
          component: () => import('../views/MyCourses.vue')
        },
        {
          path: 'course/:id',
          name: 'courseDetail',
          component: () => import('../views/CourseDetail.vue')
        },
        {
          path: 'learning/:id',
          name: 'learning',
          component: () => import('../views/Learning.vue')
        },
        {
          path: 'exam-list',
          name: 'examList',
          component: () => import('../views/ExamList.vue')
        },
        {
          path: 'notices',
          name: 'notices',
          component: () => import('../views/NoticeList.vue')
        },
        {
          path: 'exam/result/:id',
          name: 'examResult',
          component: () => import('../views/ExamResult.vue')
        },
        {
          path: 'profile',
          name: 'profile',
          component: Profile,
          meta: { requiresAuth: true }
        },
        // 教师端
        {
          path: 'teacher/courses',
          component: () => import('../views/teacher/CourseList.vue')
        },
        {
          path: 'teacher/course/edit/:id',
          component: () => import('../views/teacher/CourseEdit.vue')
        },
        {
          path: 'teacher/course/new',
          redirect: '/teacher/course/edit/new'
        },
        {
          path: 'teacher/course/:id/students',
          component: () => import('../views/teacher/StudentManage.vue')
        },
        {
          path: 'teacher/questions',
          component: () => import('../views/teacher/QuestionList.vue')
        },
        // 考试管理
        {
          path: 'teacher/exams',
          component: () => import('../views/teacher/ExamManagement.vue')
        },
        {
          path: 'teacher/exam/edit/:id',
          component: () => import('../views/teacher/ExamEdit.vue')
        },
        {
          path: 'teacher/exam/stats/:id',
          component: () => import('../views/teacher/ExamMonitor.vue')
        },
        {
          path: 'teacher/exam/scores/:id',
          component: () => import('../views/teacher/ExamScoreAnalysis.vue')
        },
        {
          path: 'teacher/exam/grading/:id',
          component: () => import('../views/teacher/GradingPage.vue')
        },
        // 管理员端
        {
          path: 'admin/users',
          component: () => import('../views/admin/UserList.vue')
        },
        {
          path: 'admin/categories',
          component: () => import('../views/admin/CategoryManage.vue')
        },
        {
          path: 'admin/notices',
          component: () => import('../views/admin/NoticeManage.vue')
        },
        {
          path: 'admin/dashboard',
          component: () => import('../views/admin/Dashboard.vue')
        }
      ]
    },
    {
      path: '/exam/:id',
      name: 'examExecution',
      component: () => import('../views/ExamExecution.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: Login
    },
    {
      path: '/register',
      name: 'register',
      component: Register
    }
  ]
})

// 路由守卫
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  
  // 1. 如果去登录/注册且已有 token，直接去首页
  if (token && (to.path === '/login' || to.path === '/register')) {
    return '/'
  }

  // 2. 根路径根据角色重定向
  if (to.path === '/') {
    if (!token) {
      return '/login'
    } else {
      if (userInfo.role === 'ADMIN') return '/admin/dashboard'
      if (userInfo.role === 'TEACHER') return '/teacher/courses'
      return '/square'
    }
  }

  // 3. 角色访问控制：非学生访问学生专属页面时重定向
  const studentPaths = ['/square', '/my-courses', '/exam-list']
  if (token && userInfo.role !== 'STUDENT' && studentPaths.includes(to.path)) {
    if (userInfo.role === 'ADMIN') return '/admin/dashboard'
    if (userInfo.role === 'TEACHER') return '/teacher/courses'
  }

  // 4. 权限校验
  if (to.meta.requiresAuth && !token) {
    return '/login'
  }
  
  return true
})

export default router
