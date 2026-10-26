<template>
  <div class="exam-management">
    <div class="header">
      <h2>试卷与考试管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">创建新考试</el-button>
    </div>

    <el-table :data="exams" v-loading="loading">
      <el-table-column prop="title" label="考试标题" min-width="150" />
      <el-table-column label="关联课程" width="150">
        <template #default="scope">
          <el-tag size="small">{{ scope.row.courseTitle || '未绑定' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="duration" label="时长(分)" width="90" />
      <el-table-column label="起止时间" width="300">
        <template #default="scope">
          {{ formatDate(scope.row.startTime) }} - {{ formatDate(scope.row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-button type="primary" link @click="handleEdit(scope.row.id)">编辑</el-button>
          <el-button type="warning" link @click="viewScores(scope.row.id)">成绩</el-button>
          <el-button type="danger" link @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'

import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const exams = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/exam/studentList', {
      params: { teacherId: userStore.userInfo.id }
    })
    exams.value = res.data.map(item => ({...item.exam, statusText: item.status}))
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  router.push('/teacher/exam/edit/new')
}

const handleEdit = (id) => {
  router.push(`/teacher/exam/edit/${id}`)
}


const viewScores = (id) => {
  router.push(`/teacher/exam/scores/${id}`)
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这场考试及其所有学生成绩吗？', '严正警告', { type: 'error' }).then(async () => {
    await request.delete(`/exam/${id}`)
    ElMessage.success('已删除')
    fetchData()
  })
}

const formatDate = (date) => dayjs(date).format('MM-DD HH:mm')

onMounted(fetchData)
</script>

<style scoped>
.exam-management { padding: 20px; background: #fff; border-radius: 8px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
</style>
