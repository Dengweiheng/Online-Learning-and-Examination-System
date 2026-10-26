<template>
  <div class="teacher-courses">
    <div class="header">
      <h2>课程管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新建课程</el-button>
    </div>

    <el-table :data="courseList" v-loading="loading" style="width: 100%">
      <el-table-column label="封面" width="120">
        <template #default="scope">
          <el-image 
            :src="scope.row.cover ? 'http://localhost:8080' + scope.row.cover : ''" 
            style="width: 80px; height: 50px; border-radius: 4px"
            fit="cover"
          />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="课程名称" />
      <el-table-column prop="isPublic" label="公开状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.isPublic ? 'success' : 'info'">
            {{ scope.row.isPublic ? '公开' : '私有' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="250">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row.id)">编辑/大纲</el-button>
          <el-button size="small" type="primary" plain @click="viewStudents(scope.row.id)">学生管理</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'

import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const courseList = ref([])
const loading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/course/search', { 
      params: { 
        size: 100,
        teacherId: userStore.userInfo.id 
      } 
    })
    courseList.value = res.data.records
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  router.push('/teacher/course/new')
}

const handleEdit = (id) => {
  router.push(`/teacher/course/edit/${id}`)
}

const viewStudents = (id) => {
  router.push(`/teacher/course/${id}/students`)
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该课程吗？删除后所有章节和学习记录将消失。', '警告', {
    type: 'error'
  }).then(async () => {
    try {
      await request.delete(`/course/${id}`)
      ElMessage.success('删除成功')
      fetchData()
    } catch (err) {}
  })
}

onMounted(fetchData)
</script>

<style scoped>
.teacher-courses {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  font-size: 20px;
}
</style>
