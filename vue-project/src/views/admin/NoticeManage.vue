<template>
  <div class="admin-notice">
    <div class="header">
      <h2>公告管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">发布新公告</el-button>
    </div>

    <el-table :data="notices" v-loading="loading">
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="targetRole" label="面向对象" width="120">
        <template #default="s">
          <el-tag :type="s.row.targetRole === 'ALL' ? 'success' : 'warning'">
            {{ getRoleLabel(s.row.targetRole) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="180" />
      <el-table-column label="操作" width="150">
        <template #default="s">
          <el-button type="primary" link @click="handleEdit(s.row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(s.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '发布公告'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="输入公告标题" />
        </el-form-item>
        <el-form-item label="面向角色">
          <el-select v-model="form.targetRole" style="width: 100%">
            <el-option label="全体人员" value="ALL" />
            <el-option label="仅学生" value="STUDENT" />
            <el-option label="仅教师" value="TEACHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" rows="6" placeholder="输入公告详情..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确认发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const notices = ref([])
const dialogVisible = ref(false)
const form = ref({})

const fetchData = async () => {
    loading.value = true
    const res = await request.get('/notice/list')
    notices.value = res.data
    loading.value = false
}

const handleAdd = () => {
    form.value = { targetRole: 'ALL' }
    dialogVisible.value = true
}

const handleEdit = (row) => {
    form.value = { ...row }
    dialogVisible.value = true
}

const handleSave = async () => {
    await request.post('/notice/save', form.value)
    ElMessage.success('公告已发布')
    dialogVisible.value = false
    fetchData()
}

const handleDelete = (id) => {
    ElMessageBox.confirm('确定撤回/删除这条公告吗？', '提示').then(async () => {
        await request.delete(`/notice/${id}`)
        ElMessage.success('已删除')
        fetchData()
    })
}

const getRoleLabel = (r) => {
    const map = { 'ALL': '全体', 'STUDENT': '学生', 'TEACHER': '教师' }
    return map[r] || r
}

onMounted(fetchData)
</script>

<style scoped>
.admin-notice { padding: 20px; background: #fff; border-radius: 8px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
</style>
