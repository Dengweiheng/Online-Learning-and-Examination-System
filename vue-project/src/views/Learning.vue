<template>
  <div class="learning-container" v-if="resource">
    <div class="top-bar">
      <el-button :icon="Back" circle @click="$router.back()" />
      <h3 class="title">{{ resource.title }}</h3>
    </div>
    
    <div class="content-wrapper">
      <div class="player-section">
        <!-- 视频播放器 -->
        <div v-if="resource.type === 'VIDEO'" class="video-container">
          <video 
            ref="videoRef"
            :src="'http://localhost:8080' + resource.content" 
            controls 
            class="video-player"
            @timeupdate="handleTimeUpdate"
            @ended="handleEnded"
          ></video>
        </div>
        
        <!-- 文档查看器 (PDF/PPT 示例采用 iframe) -->
        <div v-else-if="isDocumentResource(resource.type)" class="doc-container">
          <iframe :src="'http://localhost:8080' + resource.content" width="100%" height="100%" style="border: none;"></iframe>
        </div>

        <!-- 富文本内容 -->
        <div v-else class="article-container" v-html="resource.content"></div>
      </div>

      <div class="sidebar">
        <div class="tabs">
          <div class="tab active">目录</div>
        </div>
        <div class="chapter-list">
          <div v-for="res in siblings" :key="res.id" 
               :class="['res-item', { active: res.id === resource.id }]"
               @click="switchResource(res.id)">
            <el-icon><VideoCamera v-if="res.type === 'VIDEO'" /><Document v-else /></el-icon>
            <span class="res-title">{{ res.title }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Back, VideoCamera, Document } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const resource = ref(null)
const siblings = ref([])
const videoRef = ref(null)
let lastSentProgress = 0

const fetchData = async () => {
  const resourceId = route.params.id
  try {
    // 获取资源详情及其同级资源列表
    const res = await request.get(`/course/resource/${resourceId}`)
    resource.value = res.data.resource
    siblings.value = res.data.siblings || []
    lastSentProgress = 0

    // 断点续学：获取当前进度
    const progressRes = await request.get(`/course/progress/${resourceId}`, { params: { studentId: userStore.userInfo.id } })
    if (progressRes.data) {
      const savedProgress = progressRes.data.progress || 0
      
      if (resource.value.type === 'VIDEO') {
        // 等待视频元数据加载后再设置时间
        const setVideoTime = () => {
          if (videoRef.value && videoRef.value.duration) {
            const time = (savedProgress / 100) * videoRef.value.duration
            videoRef.value.currentTime = time
            videoRef.value.removeEventListener('loadedmetadata', setVideoTime)
          }
        }
        if (videoRef.value) {
           videoRef.value.addEventListener('loadedmetadata', setVideoTime)
           // 如果已经加载好了
           if (videoRef.value.readyState >= 1) setVideoTime()
        }
      } else {
        // 非视频资源进入即视为 100% 完成
        updateProgress(100)
      }
    } else {
      // 没有任何记录时，非视频资源也标记为开始（或完成）
      if (resource.value.type !== 'VIDEO') {
        updateProgress(100)
      }
    }
  } catch (err) {
    console.error(err)
  }
}

const handleTimeUpdate = () => {
  if (!videoRef.value) return
  const current = videoRef.value.currentTime
  const duration = videoRef.value.duration
  if (!duration) return
  
  const progress = Math.floor((current / duration) * 100)
  
  // 每隔 10% 发送一次进度，避免请求过于频繁
  if (progress % 10 === 0 && progress > lastSentProgress) {
    updateProgress(progress)
    lastSentProgress = progress
  }
}

const handleEnded = () => {
  updateProgress(100)
}

const updateProgress = async (val) => {
  try {
    await request.post('/course/progress', {
      studentId: userStore.userInfo.id,
      resourceId: resource.value.id,
      progress: val
    })
  } catch (err) {}
}

const switchResource = (id) => {
  router.push(`/learning/${id}`)
}

const isDocumentResource = (type) => {
  return ['DOC', 'PDF', 'PPT'].includes(type)
}

watch(() => route.params.id, fetchData)

onMounted(fetchData)
</script>

<style scoped>
.learning-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #1a1a1a;
  color: #fff;
}

.top-bar {
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  background: #262626;
  border-bottom: 1px solid #333;
}

.title {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.content-wrapper {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.player-section {
  flex: 1;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.video-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-player {
  width: 100%;
  max-height: 100%;
  outline: none;
}

.doc-container {
  width: 100%;
  height: 100%;
  background: #fff;
}

.article-container {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  background: #fff;
  color: #333;
  padding: 40px;
  box-sizing: border-box;
}

.sidebar {
  width: 300px;
  background: #262626;
  border-left: 1px solid #333;
  display: flex;
  flex-direction: column;
}

.sidebar .tabs {
  padding: 15px 20px;
  border-bottom: 1px solid #333;
}

.tab.active {
  color: #409EFF;
  font-weight: bold;
}

.chapter-list {
  flex: 1;
  overflow-y: auto;
}

.res-item {
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  color: #999;
}

.res-item:hover {
  background: #333;
  color: #fff;
}

.res-item.active {
  background: #409EFF22;
  color: #409EFF;
}

.res-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
