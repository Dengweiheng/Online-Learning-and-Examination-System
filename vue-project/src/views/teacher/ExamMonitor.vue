<template>
  <div class="exam-stats">
    <el-page-header @back="$router.back()" title="考试情况监控" class="mb-4" />

    <div v-if="loading" class="loading-box">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else>
      <el-row :gutter="20" class="summary-cards">
        <el-col :span="6">
          <el-card shadow="never">
            <div class="stats-label">参与人数</div>
            <div class="stats-value">{{ summary.totalParticipants || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never">
            <div class="stats-label">平均分</div>
            <div class="stats-value">{{ summary.averageScore?.toFixed(1) || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never">
            <div class="stats-label">最高分</div>
            <div class="stats-value">{{ summary.maxScore || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never">
            <div class="stats-label">及格率</div>
            <div class="stats-value text-success">{{ (summary.passRate * 100)?.toFixed(1) || 0 }}%</div>
          </el-card>
        </el-col>
      </el-row>

      <div class="table-title">学生成绩明细</div>
      <el-table :data="records" stripe style="width: 100%">
        <el-table-column label="学生" width="180">
          <template #default="s">
            <div style="display: flex; align-items: center; gap: 10px">
              <el-avatar :size="30" :src="s.row.avatar ? 'http://localhost:8080' + s.row.avatar : ''" />
              <span>{{ s.row.nickname || s.row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="最终得分" sortable>
          <template #default="s">
            <span :class="s.row.score >= passThreshold ? 'text-success' : 'text-danger'">
              {{ s.row.score }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="交卷时间" width="180">
          <template #default="s">{{ formatDate(s.row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="s">
            <el-button type="primary" link @click="viewDetail(s.row.id)">查阅阅卷</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import dayjs from 'dayjs'

const route = useRoute()
const id = route.params.id
const loading = ref(false)
const summary = ref({})
const records = ref([])
const passThreshold = ref(60)

const fetchData = async () => {
    loading.value = true
    try {
        const res = await request.get(`/exam/stats/${id}`)
        summary.value = res.data.summary
        records.value = res.data.records
        passThreshold.value = res.data.passScore
    } catch (err) {
        console.error(err)
    } finally {
        loading.value = false
    }
}

const formatDate = (d) => d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'

const viewDetail = (recordId) => {
    // 逻辑：跳转到阅卷详情页
    console.log('Viewing record:', recordId)
}

onMounted(fetchData)
</script>

<style scoped>
.exam-stats { padding: 20px; background: #fff; border-radius: 8px; }
.mb-4 { margin-bottom: 20px; }
.summary-cards { margin-bottom: 30px; text-align: center; }
.stats-label { color: #999; font-size: 14px; margin-bottom: 10px; }
.stats-value { font-size: 28px; font-weight: bold; color: #333; }
.table-title { font-weight: bold; margin-bottom: 15px; font-size: 16px; border-left: 4px solid #409EFF; padding-left: 10px; }
.text-success { color: #67C23A; }
.text-danger { color: #F56C6C; }
</style>
