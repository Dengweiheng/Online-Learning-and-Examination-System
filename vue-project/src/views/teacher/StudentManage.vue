<template>
  <div class="student-mg">
    <el-page-header @back="$router.back()" title="学生管理" class="mb-4" />

    <el-table :data="students" v-loading="loading">
      <el-table-column label="头像" width="80">
        <template #default="scope">
          <el-avatar :size="30" :src="scope.row.avatar ? 'http://localhost:8080' + scope.row.avatar : ''" />
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column label="操作" width="100">
        <template #default="scope">
          <el-button type="danger" link @click="removeStudent(scope.row.id)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const courseId = route.params.id
const students = ref([])
const loading = ref(false)

const fetchData = async () => {
    loading.value = true
    try {
        const res = await request.get(`/course/students/${courseId}`)
        students.value = res.data
    } catch (err) {
        console.error(err)
    } finally {
        loading.value = false
    }
}

const removeStudent = (studentId) => {
    ElMessageBox.confirm('确定要将该学生移除课程吗？', '提示', { type: 'warning' }).then(async () => {
        await request.delete(`/course/removeStudent/${courseId}/${studentId}`)
        ElMessage.success('已移出课程')
        fetchData()
    })
}

onMounted(fetchData)
</script>

<style scoped>
.student-mg { padding: 20px; background: #fff; border-radius: 8px; }
.mb-4 { margin-bottom: 20px; }
</style>
