<template>
  <div class="square-container">
    <div class="search-section">
      <el-input
        v-model="queryParams.keyword"
        placeholder="搜索感兴趣的课程..."
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="handleSearch" />
        </template>
      </el-input>
    </div>
    <div class="carousel-section mb-6">
      <el-carousel :interval="4000" type="card" height="300px" indicator-position="outside">
        <el-carousel-item v-for="item in banners" :key="item">
          <div class="banner-item">
            <img :src="item" class="banner-img" />
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <div class="category-tabs">
      <el-radio-group v-model="queryParams.categoryId" @change="handleSearch">
        <el-radio-button :value="null">全部课程</el-radio-button>
        <el-radio-button v-for="cat in categories" :key="cat.id" :value="cat.id">
          {{ cat.name }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <el-row :gutter="24" class="course-grid">
      <el-col v-for="item in courseList" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card class="course-card" :body-style="{ padding: '0px' }" @click="viewDetail(item.id)">
          <div class="course-image-wrapper">
            <img :src="item.cover ? 'http://localhost:8080' + item.cover : placeholderImage" class="course-image" />
            <div class="course-tag">{{ item.isPublic ? '公开课' : '私有课' }}</div>
          </div>
          <div class="course-info">
            <h4 class="title">{{ item.title }}</h4>
            <p class="desc">{{ item.description }}</p>
            <div class="meta">
              <span class="students"><el-icon><User /></el-icon> 2k+ 已加入</span>
              <el-button type="primary" link @click.stop="handleJoin(item)">立即加入</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="pagination">
      <el-pagination
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        layout="prev, pager, next"
        :total="total"
        @current-change="handleSearch"
      />
    </div>

    <!-- 加入课程弹窗 -->
    <el-dialog v-model="joinDialogVisible" title="通过课程码加入" width="350px">
      <el-input v-model="courseCode" placeholder="请输入课程码" />
      <template #footer>
        <el-button @click="joinDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmJoin">确认加入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, User } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const placeholderImage = 'https://images.unsplash.com/photo-1546410531-bb4caa6b424d?w=800&auto=format&fit=crop&q=60'
const banners = ref([
  '/banners/banner1.png',
  '/banners/banner2.png',
  '/banners/banner3.png'
])

const queryParams = reactive({
  current: 1,
  size: 12,
  categoryId: null,
  keyword: ''
})

const total = ref(0)
const courseList = ref([])
const categories = ref([])
const joinDialogVisible = ref(false)
const courseCode = ref('')
const currentSelectCourseId = ref(null)

const handleSearch = async () => {
  const res = await request.get('/course/search', { params: queryParams })
  courseList.value = res.data.records
  total.value = res.data.total
}

const fetchCategories = async () => {
  const res = await request.get('/category/list', { params: { type: 1 } })
  categories.value = res.data
}

const viewDetail = (id) => {
  router.push(`/course/${id}`)
}

const handleJoin = (course) => {
  if (course.isPublic) {
    confirmJoinPublic(course.id)
  } else {
    currentSelectCourseId.value = course.id
    joinDialogVisible.value = true
  }
}

const confirmJoinPublic = async (courseId) => {
  try {
    await request.post('/course/join', {
      studentId: userStore.userInfo.id,
      courseId: courseId
    })
    ElMessage.success('加入课程成功')
  } catch (err) {}
}

const confirmJoin = async () => {
  try {
    await request.post('/course/join', {
      studentId: userStore.userInfo.id,
      courseId: currentSelectCourseId.value,
      code: courseCode.value
    })
    ElMessage.success('加入课程成功')
    joinDialogVisible.value = false
  } catch (err) {}
}

onMounted(() => {
  fetchCategories()
  handleSearch()
})
</script>

<style scoped>
.square-container {
  max-width: 1400px;
  margin: 0 auto;
}

.search-section {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.search-input {
  max-width: 600px;
}

.category-tabs {
  margin-bottom: 30px;
  display: flex;
  justify-content: center;
}

.course-grid {
  margin-bottom: 40px;
}

.course-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
}

.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
}

.course-image-wrapper {
  position: relative;
  height: 180px;
}

.course-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0,0,0,0.6);
  color: #fff;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.course-info {
  padding: 16px;
}

.course-info h4 {
  margin: 0 0 10px;
  font-size: 18px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-info .desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
  height: 40px;
  line-height: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.students {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.mb-6 { margin-bottom: 24px; }

.carousel-section {
    padding: 10px 0;
}

.banner-item {
    width: 100%;
    height: 100%;
    border-radius: 12px;
    overflow: hidden;
}

.banner-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s;
}

.banner-img:hover {
    transform: scale(1.05);
}

:deep(.el-carousel__item--card) {
    border-radius: 12px;
}
</style>
