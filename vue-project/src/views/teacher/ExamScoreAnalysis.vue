<template>
  <div class="score-analysis">
    <el-page-header @back="$router.back()" title="成绩管理与阅卷" class="mb-4" />

    <div class="header-actions">
        <el-button type="primary" :icon="Download" @click="handleExport">导出成绩单 (Excel)</el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="24">
        <div class="stats-summary mb-4">
            <el-card shadow="never">
                <template #header>考试概况</template>
                <div class="summary-content">
                    <div class="summary-item">
                        <div class="label">参加人数</div>
                        <div class="val">{{ records.length }}</div>
                    </div>
                    <div class="summary-item">
                        <div class="label">平均分</div>
                        <div class="val">{{ averageScore }}</div>
                    </div>
                    <div class="summary-item">
                        <div class="label">最高分</div>
                        <div class="val text-success">{{ maxScore }}</div>
                    </div>
                </div>
            </el-card>
        </div>
      </el-col>

      <el-col :span="24">
        <el-table :data="records" border stripe style="margin-top: 20px">
          <el-table-column prop="username" label="学号/用户名" width="150" />
          <el-table-column prop="nickname" label="姓名" width="150" />
          <el-table-column prop="score" label="考试得分">
            <template #default="s">
                <span style="font-weight: bold; font-size: 16px; color: #f56c6c">{{ s.row.score }}</span>
            </template>
          </el-table-column>
          <el-table-column label="交卷时间" width="200">
            <template #default="s">
                {{ formatDate(s.row.submitTime) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="s">
              <el-tag type="success">已自动批改</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="s">
               <el-button type="primary" plain size="small" @click="viewDetail(s.row.id)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Download } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const records = ref([])
const distribution = ref({
    '0-59': 0,
    '60-69': 0,
    '70-79': 0,
    '80-89': 0,
    '90-100': 0
})

const fetchData = async () => {
    const res = await request.get(`/exam/records/${id}`)
    records.value = res.data
    // 计算分布
    calculateDistribution()
}

const calculateDistribution = () => {
    const dist = { '0-59': 0, '60-69': 0, '70-79': 0, '80-89': 0, '90-100': 0 }
    records.value.forEach(r => {
        if (r.score < 60) dist['0-59']++
        else if (r.score < 70) dist['60-69']++
        else if (r.score < 80) dist['70-79']++
        else if (r.score < 90) dist['80-89']++
        else dist['90-100']++
    })
    distribution.value = dist
}

const averageScore = computed(() => {
    if (records.value.length === 0) return 0
    const total = records.value.reduce((sum, r) => sum + r.score, 0)
    return (total / records.value.length).toFixed(1)
})

const maxScore = computed(() => {
    if (records.value.length === 0) return 0
    return Math.max(...records.value.map(r => r.score))
})

const maxValChart = computed(() => {
    return Math.max(...Object.values(distribution.value), 1)
})

const viewDetail = (recordId) => {
    // 老师也可以查看学生的答题详情，复用结果页面
    router.push(`/exam/result/${recordId}`)
}

const handleExport = () => {
    window.open(`http://localhost:8080/exam/export/${id}`)
}

const formatDate = (date) => date ? new Date(date).toLocaleString() : '-'

onMounted(fetchData)
</script>

<style scoped>
.score-analysis { padding: 20px; }
.mb-4 { margin-bottom: 20px; }
.header-actions { margin-bottom: 20px; text-align: right; }
.stat-card { margin-bottom: 20px; }
.card-header { font-weight: bold; margin-bottom: 20px; }
.chart-box { display: flex; align-items: flex-end; justify-content: space-around; background: #fcfcfc; border-radius: 8px; }
.bar-wrapper { flex: 1; display: flex; flex-direction: column; align-items: center; }
.bar { width: 40px; background: #66b1ff; position: relative; border-radius: 4px 4px 0 0; transition: height 0.5s; }
.bar .val { position: absolute; top: -25px; left: 50%; transform: translateX(-50%); font-size: 12px; color: #666; }
.label { margin-top: 10px; font-size: 12px; color: #999; }

.summary-content { display: flex; justify-content: space-around; padding: 10px 0; }
.summary-item { text-align: center; }
.summary-item .label { font-size: 14px; color: #999; margin-bottom: 8px; }
.summary-item .val { font-size: 28px; font-weight: bold; color: #333; }
.text-success { color: #67C23A !important; }
</style>
