<template>
  <div class="exam-list-container">
    <div class="header">
      <h2 class="title">在线考试</h2>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部考试" name="all" />
        <el-tab-pane label="进行中" name="ongoing" />
        <el-tab-pane label="未开始" name="upcoming" />
        <el-tab-pane label="已结束" name="finished" />
      </el-tabs>
    </div>

    <div v-if="loading" class="loading-box">
      <el-skeleton :rows="4" animated />
    </div>

    <el-row v-else :gutter="20">
      <el-col v-for="item in filteredExams" :key="item.exam.id" :span="24">
        <el-card class="exam-card">
          <div class="exam-content">
            <div class="main-info">
              <h3 class="exam-title">{{ item.exam.title }}</h3>
              <div class="exam-tags">
                <el-tag :type="getStatusType(item.status)">{{ item.status }}</el-tag>
                <el-tag type="info" effect="plain" v-if="item.exam.courseTitle">{{ item.exam.courseTitle }}</el-tag>
                <span class="time-limit">
                  <el-icon><Timer /></el-icon> {{ item.exam.duration }} 分钟
                </span>
                <span class="total-score">
                  <el-icon><Tickets /></el-icon> 总分：{{ item.exam.totalPoints }}
                </span>
              </div>
              <div class="time-range">
                开放时间：{{ formatDate(item.exam.startTime) }} ~ {{ formatDate(item.exam.endTime) }}
              </div>
            </div>
            <div class="action-zone">
              <div v-if="item.hasTaken" class="result-info">
                <div class="score-display">最高分：<span class="score">{{ item.records[0].score }}</span></div>
                <el-button type="success" plain @click="viewResult(item.records[0].id)">查看详情</el-button>
              </div>
              <el-button 
                v-if="!item.hasTaken || item.records.length < item.exam.maxAttempts"
                type="primary" 
                size="large" 
                :disabled="item.status !== '进行中'"
                @click="enterExam(item.exam.id)"
              >
                {{ item.status === '进行中' ? (item.hasTaken ? '再次挑战' : '进入考试') : '暂不可考' }}
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Timer, Tickets } from '@element-plus/icons-vue'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const router = useRouter()
const loading = ref(false)
const exams = ref([])
const activeTab = ref('all')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/exam/studentList', {
      params: { studentId: userStore.userInfo.id }
    })
    exams.value = res.data
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const filteredExams = computed(() => {
  if (activeTab.value === 'all') return exams.value
  return exams.value.filter(item => {
    if (activeTab.value === 'ongoing') return item.status === '进行中'
    if (activeTab.value === 'upcoming') return item.status === '未开始'
    if (activeTab.value === 'finished') return item.status === '已结束'
    return true
  })
})

const getStatusType = (status) => {
  if (status === '进行中') return 'success'
  if (status === '未开始') return 'warning'
  return 'info'
}

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

const enterExam = (id) => {
  router.push(`/exam/${id}`)
}

const viewResult = (recordId) => {
  router.push(`/exam/result/${recordId}`)
}

const handleTabChange = () => {}

onMounted(fetchData)
</script>

<style scoped>
.exam-list-container {
  max-width: 1000px;
  margin: 0 auto;
}

.header {
  margin-bottom: 30px;
}

.title {
  margin: 0 0 20px;
  font-size: 24px;
}

.exam-card {
  margin-bottom: 20px;
  border-radius: 12px;
  transition: all 0.3s;
}

.exam-card:hover {
  box-shadow: 0 8px 16px rgba(0,0,0,0.1);
}

.exam-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.exam-title {
  margin: 0 0 12px;
  font-size: 20px;
  color: #333;
}

.exam-tags {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 15px;
  color: #666;
  font-size: 14px;
}

.exam-tags .el-icon {
  vertical-align: -2px;
}

.time-range {
  font-size: 13px;
  color: #999;
}

.loading-box {
  padding: 40px 0;
}

.action-zone {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.result-info {
  text-align: right;
  margin-bottom: 5px;
}

.score-display {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.score {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}
</style>
