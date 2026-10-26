<template>
  <div class="admin-config">
    <div class="header">
      <h2>系统设置</h2>
    </div>

    <el-alert title="基础参数配置定义了全平台的运行逻辑，修改请谨慎。" type="warning" show-icon style="margin-bottom: 20px" />

    <el-table :data="configs" v-loading="loading">
      <el-table-column prop="configKey" label="配置项" width="200" />
      <el-table-column prop="configValue" label="当前值">
        <template #default="s">
          <el-input v-if="s.row.editable" v-model="s.row.configValue" size="small" />
          <span v-else>{{ s.row.configValue }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="说明" />
      <el-table-column label="操作" width="120">
        <template #default="s">
          <el-button v-if="!s.row.editable" type="primary" link @click="s.row.editable = true">修改</el-button>
          <template v-else>
            <el-button type="success" link @click="handleSave(s.row)">保存</el-button>
            <el-button type="info" link @click="s.row.editable = false">取消</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const configs = ref([])

const fetchData = async () => {
    loading.value = true
    const res = await request.get('/config/list')
    configs.value = res.data.map(item => ({ ...item, editable: false }))
    loading.value = false
}

const handleSave = async (row) => {
    await request.post('/config/save', row)
    ElMessage.success('配置已更新')
    row.editable = false
    fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.admin-config { padding: 20px; background: #fff; border-radius: 8px; }
.header { margin-bottom: 20px; }
</style>
