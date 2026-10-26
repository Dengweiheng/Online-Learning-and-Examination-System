<template>
  <div class="detail-container" v-if="course">
    <el-row :gutter="40">
      <el-col :span="16">
        <div class="course-header">
          <h1 class="title">{{ course.title }}</h1>
          <div class="subtitle">{{ course.description }}</div>
          <div class="teacher-info">
            <el-avatar :size="40">教师</el-avatar>
            <span>主讲教师：张老师</span>
          </div>
        </div>

        <el-tabs v-model="activeTab" class="content-tabs">
          <el-tab-pane label="课程大纲" name="outline">
            <div class="outline-tree">
              <div v-for="chapter in chapters" :key="chapter.id" class="chapter-item">
                <div class="chapter-title">{{ chapter.title }}</div>
                <div v-for="res in chapter.resources" :key="res.id" class="resource-item" @click="goLearn(res.id)">
                  <el-icon><VideoCamera v-if="res.type === 'VIDEO'" /><Document v-else /></el-icon>
                  <span class="res-name">{{ res.title }}</span>
                  <el-tag size="small" type="success" v-if="getProgress(res.id) === 100">已完成</el-tag>
                </div>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="详情介绍" name="intro">
            <div class="course-intro" v-html="course.description || '暂无详细介绍'"></div>
          </el-tab-pane>
        </el-tabs>
      </el-col>

      <el-col :span="8">
        <el-card class="action-card">
          <img :src="course.cover ? 'http://localhost:8080' + course.cover : placeholderImage" class="sidebar-cover" />
          <div class="stats">
            <div class="stat-item">
              <div class="val">12</div>
              <div class="lab">章节数</div>
            </div>
            <div class="stat-item">
              <div class="val">2.5k</div>
              <div class="lab">学习人数</div>
            </div>
          </div>
          <el-button type="primary" size="large" class="start-btn" @click="startLearn">开始学习</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { VideoCamera, Document } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const activeTab = ref('outline')
const course = ref(null)
const chapters = ref([])
const progresses = ref([])
const placeholderImage = 'https://images.unsplash.com/photo-1546410531-bb4caa6b424d?w=800&auto=format&fit=crop&q=60'

const fetchDetail = async () => {
  const courseId = route.params.id
  const res = await request.get('/course/detail', { params: { courseId } })
  course.value = res.data.course
  chapters.value = res.data.chapters || []
}

const goLearn = (resourceId) => {
  router.push(`/learning/${resourceId}`)
}

const startLearn = () => {
  if (chapters.value.length > 0 && chapters.value[0].resources.length > 0) {
    goLearn(chapters.value[0].resources[0].id)
  }
}

const getProgress = (resId) => {
  const p = progresses.value.find(item => item.resourceId === resId)
  return p ? p.progress : 0
}

onMounted(fetchDetail)
</script>

<style scoped>
.detail-container {
  max-width: 1200px;
  margin: 0 auto;
}

.course-header {
  margin-bottom: 30px;
}

.course-header .title {
  font-size: 32px;
  margin: 0 0 15px;
  color: #1a1a1a;
}

.course-header .subtitle {
  font-size: 16px;
  color: #666;
  margin-bottom: 20px;
}

.teacher-info {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.content-tabs {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.chapter-item {
  margin-bottom: 25px;
}

.chapter-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
}

.resource-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.resource-item:hover {
  background: #f9f9f9;
  color: #409EFF;
}

.res-name {
  flex: 1;
  font-size: 14px;
}

.action-card {
  border-radius: 12px;
  overflow: hidden;
}

.sidebar-cover {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 20px;
}

.stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 30px;
  text-align: center;
}

.stat-item .val {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-item .lab {
  font-size: 12px;
  color: #999;
}

.start-btn {
  width: 100%;
  border-radius: 8px;
}
</style>
