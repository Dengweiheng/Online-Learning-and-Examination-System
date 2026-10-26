<template>
  <div class="notice-list">
    <div class="header">
      <h2>系统公告</h2>
      <el-button type="primary" link @click="fetchNotices">刷新</el-button>
    </div>

    <el-skeleton :loading="loading" animated>
      <template #default>
        <div v-if="notices.length === 0" class="empty">
          <el-empty description="暂无公告" />
        </div>
        <div v-else class="notice-container">
          <el-card v-for="notice in notices" :key="notice.id" class="notice-card shadow-sm">
            <template #header>
              <div class="card-header">
                <span class="title">{{ notice.title }}</span>
                <span class="time">{{ formatDate(notice.createTime) }}</span>
              </div>
            </template>
            <div class="content">{{ notice.content }}</div>
            <div class="footer" v-if="notice.targetRole !== 'ALL'">
              <el-tag size="small" type="warning">针对：{{ getRoleName(notice.targetRole) }}</el-tag>
            </div>
          </el-card>
        </div>
      </template>
    </el-skeleton>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const notices = ref([])

const fetchNotices = async () => {
  loading.value = true
  try {
    const res = await request.get('/notice/list', { params: { role: userStore.userInfo.role } })
    notices.value = res.data || []
  } catch (error) {
    console.error('获取公告失败:', error)
    notices.value = []
  } finally {
    loading.value = false
  }
}

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

const getRoleName = (role) => {
  const map = { 'TEACHER': '教师', 'STUDENT': '学生', 'ADMIN': '管理员' }
  return map[role] || role
}

onMounted(fetchNotices)
</script>

<style scoped>
.notice-list {
  max-width: 900px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.notice-card {
  margin-bottom: 20px;
  border-radius: 12px;
  transition: transform 0.3s;
}

.notice-card:hover {
  transform: translateY(-4px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.time {
  font-size: 13px;
  color: #909399;
}

.content {
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}

.footer {
  margin-top: 15px;
  text-align: right;
}

.empty {
  padding: 100px 0;
}

.shadow-sm {
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
</style>
