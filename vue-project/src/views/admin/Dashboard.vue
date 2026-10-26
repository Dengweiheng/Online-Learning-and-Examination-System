<template>
  <div class="admin-dashboard">
    <div class="header">
      <h2>全局数据看板</h2>
      <el-button type="primary" :icon="Refresh" @click="fetchData">刷新数据</el-button>
    </div>

    <!-- 顶部核心指标 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6" v-for="card in statCards" :key="card.title">
        <el-card shadow="hover" class="stat-card">
          <div class="card-content">
            <div class="left">
                <el-icon :size="40" :color="card.color"><component :is="card.icon" /></el-icon>
            </div>
            <div class="right">
                <div class="label">{{ card.title }}</div>
                <div class="value">{{ card.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card header="用户活跃度趋势" class="chart-card">
          <div ref="activityChart" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card header="课程分类占比" class="chart-card">
          <div ref="categoryChart" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card header="考试平均成绩趋势" class="chart-card">
          <div ref="examChart" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="资源库容使用情况" class="chart-card">
          <div ref="resourceChart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Refresh, User, VideoPlay, Reading, DataBoard } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const statCards = ref([
  { title: '用户总数', value: 0, icon: User, color: '#409EFF' },
  { title: '课程数量', value: 0, icon: Reading, color: '#67C23A' },
  { title: '考试场次', value: 0, icon: DataBoard, color: '#E6A23C' },
  { title: '观看时长(时)', value: 0, icon: VideoPlay, color: '#F56C6C' }
])

const activityChart = ref(null)
const categoryChart = ref(null)
const examChart = ref(null)
const resourceChart = ref(null)

const fetchData = async () => {
    const res = await request.get('/stats/all')
    const data = res.data
    
    statCards.value[0].value = data.userCount
    statCards.value[1].value = data.courseCount
    statCards.value[2].value = data.examCount
    statCards.value[3].value = data.studyHours

    initCharts(data)
}

const initCharts = (data) => {
    // 1. 用户活跃度趋势（折线图）
    const lineInst = echarts.init(activityChart.value)
    lineInst.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: data.activityTrend.dates },
        yAxis: { type: 'value' },
        series: [{ data: data.activityTrend.values, type: 'line', smooth: true, areaStyle: { color: 'rgba(64, 158, 255, 0.1)' } }]
    })

    // 2. 课程分类占比（饼图）
    const pieInst = echarts.init(categoryChart.value)
    pieInst.setOption({
        tooltip: { trigger: 'item' },
        series: [{
            type: 'pie',
            radius: ['40%', '70%'],
            data: data.categoryStats,
            emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
        }]
    })

    // 3. 考试成绩趋势（柱状图）
    const barInst = echarts.init(examChart.value)
    barInst.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: data.examTrend.names },
        yAxis: { type: 'value' },
        series: [{ data: data.examTrend.avgScores, type: 'bar', itemStyle: { color: '#E6A23C' } }]
    })

    // 4. 资源分布（漏斗图/环形图示例）
    const resInst = echarts.init(resourceChart.value)
    resInst.setOption({
        tooltip: { trigger: 'item' },
        legend: { top: 'bottom' },
        series: [{
            name: '资源占比',
            type: 'pie',
            radius: [20, 100],
            roseType: 'area',
            itemStyle: { borderRadius: 8 },
            data: data.resourceStats
        }]
    })
}

onMounted(() => {
    nextTick(() => {
        fetchData()
    })
})
</script>

<style scoped>
.admin-dashboard { padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; }
.stat-card { border-radius: 12px; }
.card-content { display: flex; align-items: center; gap: 20px; }
.label { font-size: 14px; color: #999; margin-bottom: 5px; }
.value { font-size: 24px; font-weight: bold; color: #333; }
.chart-card { border-radius: 12px; }
.mb-4 { margin-bottom: 20px; }
</style>
