<template>
  <div class="admin-user-list">
    <div class="header">
      <h2>用户管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
    </div>

    <div class="filter-bar">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="角色">
          <el-select v-model="queryParams.role" placeholder="全部" clearable style="width: 120px">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="搜索用户名或昵称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="users" v-loading="loading">
      <el-table-column label="头像" width="80">
        <template #default="s">
          <el-avatar :size="30" :src="s.row.avatar ? 'http://localhost:8080' + s.row.avatar : ''" />
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="role" label="角色">
        <template #default="s">
          <el-tag :type="getRoleType(s.row.role)">{{ s.row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="s">
          <el-switch 
            v-model="s.row.status" 
            :active-value="1" 
            :inactive-value="0"
            @change="handleStatusChange(s.row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180" />
      <el-table-column label="操作" width="220">
        <template #default="s">
          <el-button type="primary" link @click="handleEdit(s.row)">编辑</el-button>
          <el-button type="warning" link @click="handleResetPwd(s.row.id)">重置密码</el-button>
          <el-button type="danger" link @click="handleDelete(s.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="fetchData"
      />
    </div>

    <!-- 用户编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="密码" v-if="!form.id">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const users = ref([])
const total = ref(0)
const queryParams = reactive({
  current: 1,
  size: 10,
  role: null,
  keyword: ''
})

const fetchData = async () => {
    loading.value = true
    try {
        const res = await request.get('/user/admin/page', { params: queryParams })
        users.value = res.data.records
        total.value = res.data.total
    } finally {
        loading.value = false
    }
}

const handleStatusChange = async (row) => {
    try {
        await request.put('/user/admin/update', { id: row.id, status: row.status })
        ElMessage.success('状态已更新')
    } catch (err) {
        row.status = row.status === 1 ? 0 : 1 // 失败回滚
    }
}

const handleResetPwd = (id) => {
    ElMessageBox.confirm('确定将该用户的密码重置为 123456 吗？', '重置提示').then(async () => {
        await request.post(`/user/admin/resetPassword`, { userId: id, newPassword: '123456' })
        ElMessage.success('密码已重置为 123456')
    })
}

const dialogVisible = ref(false)
const form = ref({})

const handleAdd = () => {
    form.value = { role: 'STUDENT', status: 1 }
    dialogVisible.value = true
}

const handleEdit = (row) => {
    form.value = { ...row }
    dialogVisible.value = true
}

const handleSave = async () => {
    if (form.value.id) {
        await request.put('/user/admin/update', form.value)
    } else {
        await request.post('/user/register', form.value)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    fetchData()
}

const handleDelete = (id) => {
    ElMessageBox.confirm('彻底删除该用户？此操作不可逆。', '警告', { type: 'error' }).then(async () => {
        await request.delete(`/user/${id}`)
        ElMessage.success('已删除')
        fetchData()
    })
}

const getRoleType = (r) => {
    if (r === 'ADMIN') return 'danger'
    if (r === 'TEACHER') return 'warning'
    return 'info'
}

onMounted(fetchData)
</script>

<style scoped>
.admin-user-list { padding: 20px; background: #fff; border-radius: 8px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.filter-bar { background: #f9f9f9; padding: 18px; border-radius: 8px; margin-bottom: 20px; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>
