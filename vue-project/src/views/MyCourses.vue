<template>
  <div class="my-courses-container">
    <div class="header-section">
      <h2 class="page-title">我的课程</h2>
      <el-button type="primary" @click="$router.push('/square')">加入更多课程</el-button>
    </div>

    <div v-if="loading" class="loading-box">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="courses.length === 0" class="empty-box">
      <el-empty description="您还没有加入任何课程" />
    </div>

    <el-row v-else :gutter="24">
      <el-col v-for="item in courses" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="course-card" :body-style="{ padding: '0px' }" @click="viewDetail(item.id)">
          <div class="cover-wrapper">
            <img :src="item.cover ? 'http://localhost:8080' + item.cover : placeholderImage" class="cover-img" />
            <div class="progress-bar" :style="{ width: getCourseProgress(item.id) + '%' }"></div>
          </div>
          <div class="info-content">
            <h4 class="title">{{ item.title }}</h4>
            <div class="progress-text">学习进度：{{ getCourseProgress(item.id) }}%</div>
            <div class="actions">
              <el-button type="primary" size="small" @click.stop="goLearn(item.id)">继续学习</el-button>
              <el-button type="danger" size="small" plain @click.stop="quitCourse(item.id)">退出</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const courses = ref([])
const progresses = ref({})
const placeholderImage = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800&auto=format&fit=crop&q=60'

const fetchData = async () => {
  loading.value = true
  try {
    // 假设后端有获取学生已选课程的接口
    const res = await request.get(`/course/myEnrolled`, {
      params: { studentId: userStore.userInfo.id }
    })
    courses.value = res.data.courses
    progresses.value = res.data.progressMap || {}
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const getCourseProgress = (courseId) => {
  return progresses.value[courseId] || 0
}

const viewDetail = (id) => {
  router.push(`/course/${id}`)
}

const goLearn = (courseId) => {
  router.push(`/course/${courseId}`)
}

const quitCourse = (courseId) => {
  ElMessageBox.confirm('确定要退出该课程吗？退出后学习记录将被保留，但需重新加入。', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/course/selection/${courseId}`, {
        params: { studentId: userStore.userInfo.id }
      })
      ElMessage.success('已退出课程')
      fetchData()
    } catch (err) {}
  })
}

onMounted(fetchData)
</script>

<style scoped>
.my-courses-container {
  max-width: 1200px;
  margin: 0 auto;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.course-card {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 12px;
  margin-bottom: 24px;
}

.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.1);
}

.cover-wrapper {
  position: relative;
  height: 160px;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.progress-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 4px;
  background: #409EFF;
  transition: width 0.5s;
}

.info-content {
  padding: 16px;
}

.title {
  margin: 0 0 10px;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.progress-text {
  font-size: 13px;
  color: #999;
  margin-bottom: 15px;
}

.actions {
  display: flex;
  justify-content: space-between;
}

.loading-box, .empty-box {
  padding: 40px 0;
}
</style>
