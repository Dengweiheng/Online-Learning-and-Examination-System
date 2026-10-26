<template>
  <div class="layout-container">
    <el-container>
      <el-header class="header">
        <div class="logo" @click="$router.push('/')">
          <el-icon :size="24"><Reading /></el-icon>
          <span>智学在线系统</span>
        </div>
        
        <el-menu mode="horizontal" :default-active="$route.path" router class="flex-grow">
          <!-- 学生端菜单 -->
          <el-menu-item index="/square" v-if="userInfo.role === 'STUDENT'">课程广场</el-menu-item>
          <el-menu-item index="/my-courses" v-if="userInfo.role === 'STUDENT'">我的课程</el-menu-item>
          <el-menu-item index="/exam-list" v-if="userInfo.role === 'STUDENT'">在线考试</el-menu-item>
          <el-menu-item index="/notices" v-if="userInfo.role === 'STUDENT'">系统公告</el-menu-item>

          <!-- 教师端菜单 -->
          <el-menu-item index="/teacher/courses" v-if="userInfo.role === 'TEACHER'">课程管理</el-menu-item>
          <el-menu-item index="/teacher/questions" v-if="userInfo.role === 'TEACHER'">题库管理</el-menu-item>
          <el-menu-item index="/teacher/exams" v-if="userInfo.role === 'TEACHER'">试卷管理</el-menu-item>
          <el-menu-item index="/notices" v-if="userInfo.role === 'TEACHER'">系统公告</el-menu-item>

          <!-- 管理员菜单 -->
          <el-menu-item index="/admin/dashboard" v-if="userInfo.role === 'ADMIN'">统计看板</el-menu-item>
          <el-menu-item index="/admin/users" v-if="userInfo.role === 'ADMIN'">用户管理</el-menu-item>
          <el-menu-item index="/admin/categories" v-if="userInfo.role === 'ADMIN'">分类管理</el-menu-item>
          <el-menu-item index="/admin/notices" v-if="userInfo.role === 'ADMIN'">公告管理</el-menu-item>
        </el-menu>
        
        <div class="user-info">
          <el-dropdown @command="handleCommand">
            <div class="avatar-wrapper">
              <el-avatar :size="32" :src="userInfo.avatar ? 'http://localhost:8080' + userInfo.avatar : ''">
                {{ userInfo.nickname?.charAt(0) || userInfo.username?.charAt(0) }}
              </el-avatar>
              <span class="nickname">{{ userInfo.nickname || userInfo.username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>安全退出</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main">
        <!-- 系统公告栏 -->
        <div v-if="activeNotice" class="notice-bar">
          <el-alert
            :title="activeNotice.title"
            :description="activeNotice.content"
            type="info"
            show-icon
            @close="closeNotice"
          />
        </div>
        <RouterView />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { Reading } from '@element-plus/icons-vue'
import request from '@/utils/request'

const userStore = useUserStore()
const router = useRouter()
const userInfo = computed(() => userStore.userInfo)
const activeNotice = ref(null)

const fetchNotice = async () => {
  const res = await request.get('/notice/latest', { params: { role: userInfo.value.role } })
  if (res.data) {
    activeNotice.value = res.data
  }
}

const closeNotice = () => {
  activeNotice.value = null
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

onMounted(fetchNotice)
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #eee;
  padding: 0 40px;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  z-index: 100;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
  cursor: pointer;
  margin-right: 40px;
}

.flex-grow {
  flex-grow: 1;
}

.user-info {
  margin-left: 20px;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  outline: none;
}

.nickname {
  font-size: 14px;
  color: #666;
}

.main {
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
  padding: 24px 40px;
}
</style>
