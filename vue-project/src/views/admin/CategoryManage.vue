<template>
  <div class="admin-category">
    <div class="header">
      <h2>分类管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增分类</el-button>
    </div>

    <el-tabs v-model="activeType" @tab-change="fetchData">
      <el-tab-pane label="课程分类" :name="1" />
      <el-tab-pane label="题库分类" :name="2" />
    </el-tabs>

    <el-table :data="categories" v-loading="loading">
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column label="操作" width="150">
        <template #default="s">
          <el-button type="primary" link @click="handleEdit(s.row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(s.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
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
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const activeType = ref(1)
const categories = ref([])
const dialogVisible = ref(false)
const form = ref({})

const fetchData = async () => {
    loading.value = true
    const res = await request.get('/category/list', { params: { type: activeType.value } })
    categories.value = res.data
    loading.value = false
}

const handleAdd = () => {
    form.value = { type: activeType.value }
    dialogVisible.value = true
}

const handleEdit = (row) => {
    form.value = { ...row }
    dialogVisible.value = true
}

const handleSave = async () => {
    await request.post('/category/save', form.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
}

const handleDelete = (id) => {
    ElMessageBox.confirm('删除分类可能影响关联数据，确定吗？', '提示').then(async () => {
        await request.delete(`/category/${id}`)
        ElMessage.success('已删除')
        fetchData()
    })
}

onMounted(fetchData)
</script>

<style scoped>
.admin-category { padding: 20px; background: #fff; border-radius: 8px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
</style>
