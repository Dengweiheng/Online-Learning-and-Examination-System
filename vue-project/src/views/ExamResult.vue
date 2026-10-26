<template>
  <div class="exam-result-container" v-if="record">
    <el-page-header @back="$router.push('/exam-list')" title="考试结果" class="mb-4" />
    
    <div class="result-header shadow-sm">
      <div class="score-card">
        <div class="score-circle">
          <span class="num">{{ record.score }}</span>
          <span class="unit">分</span>
        </div>
        <div class="status-tag">
          <el-tag :type="record.score >= exam.passScore ? 'success' : 'danger'" size="large">
            {{ record.score >= exam.passScore ? '恭喜通过' : '未及格' }}
          </el-tag>
        </div>
      </div>
      
      <div class="info-list">
        <div class="info-item">
          <span class="label">考试名称：</span>
          <span class="val">{{ exam.title }}</span>
        </div>
        <div class="info-item">
          <span class="label">交卷时间：</span>
          <span class="val">{{ formatDate(record.submitTime) }}</span>
        </div>
        <div class="info-item">
          <span class="label">及格分数：</span>
          <span class="val">{{ exam.passScore }} 分</span>
        </div>
        <div class="info-item">
          <span class="label">全卷总分：</span>
          <span class="val">{{ exam.totalPoints }} 分</span>
        </div>
      </div>
    </div>

    <div class="detail-section">
      <div class="section-header">
        <h3>答题详情</h3>
        <el-button v-if="isTeacher" type="success" @click="saveGrades">保存批改结果</el-button>
      </div>
      <div class="question-list">
        <el-card v-for="(item, index) in details" :key="item.detail.id" class="q-card mb-4" shadow="never">
          <div class="q-header">
            <span class="q-num">{{ index + 1 }}.</span>
            
            <template v-if="isTeacher">
                <el-radio-group v-model="item.detail.isCorrect" size="small" class="mr-4">
                  <el-radio-button :label="true">正确</el-radio-button>
                  <el-radio-button :label="false">错误</el-radio-button>
                </el-radio-group>
                <div class="score-input">
                    得分：<el-input-number v-model="item.detail.score" :min="0" :max="item.question.score" size="small" />
                </div>
            </template>
            <template v-else>
                <el-tag size="small" :type="item.detail.isCorrect ? 'success' : 'danger'">
                  {{ item.detail.isCorrect ? '正确' : '错误' }}
                </el-tag>
                <span class="q-score">{{ item.detail.score }} 分</span>
            </template>
          </div>
          <div class="q-content">{{ item.question.content }}</div>
          
          <div class="answer-box">
            <div class="user-ans" :class="{ error: !item.detail.isCorrect }">
              您的答案：{{ item.detail.userAnswer || '(未答)' }}
            </div>
            <div class="correct-ans">
              正确答案：{{ item.question.answer }}
            </div>
          </div>
          
          <div class="analysis" v-if="item.question.analysis">
            <div class="a-title">解析：</div>
            <div class="a-content">{{ item.question.analysis }}</div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'

const route = useRoute()
const record = ref(null)
const exam = ref(null)
const details = ref([])

const isTeacher = computed(() => {
  const user = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return user.role === 'TEACHER'
})

const fetchData = async () => {
  const recordId = route.params.id
  const res = await request.get(`/exam/recordDetails/${recordId}`)
  record.value = res.data.record
  details.value = res.data.details
  
  // 获取考试基本信息
  const examRes = await request.get(`/exam/detail/${record.value.examId}`)
  exam.value = examRes.data.exam
}

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD HH:mm:ss')

const saveGrades = async () => {
    try {
        const payload = {
            recordId: record.value.id,
            grades: details.value.map(d => ({
                detailId: d.detail.id,
                score: d.detail.score,
                isCorrect: d.detail.isCorrect
            }))
        }
        await request.post('/exam/gradeRecord', payload)
        ElMessage.success('批改成功')
        fetchData() // 刷新数据
    } catch (e) {
        ElMessage.error('保存失败')
    }
}

onMounted(fetchData)
</script>

<style scoped>
.exam-result-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.result-header {
  background: #fff;
  padding: 40px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  gap: 60px;
  margin-bottom: 30px;
}

.score-card {
  text-align: center;
}

.score-circle {
  width: 140px;
  height: 140px;
  border: 8px solid #f0f2f5;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
  color: #f56c6c;
}

.score-circle .num {
  font-size: 48px;
  font-weight: bold;
}

.score-circle .unit {
  font-size: 14px;
}

.info-list {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.info-item {
  font-size: 15px;
}

.info-item .label {
  color: #999;
}

.info-item .val {
  color: #333;
  font-weight: 500;
}

.detail-section h3 {
  margin: 0;
  font-size: 20px;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.q-card {
  border-radius: 12px;
}

.q-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.mr-4 { margin-right: 16px; }
.score-input { font-size: 14px; color: #666; }

.q-num {
  font-size: 18px;
  font-weight: bold;
}

.q-score {
  margin-left: auto;
  color: #999;
  font-size: 14px;
}

.q-content {
  font-size: 16px;
  margin-bottom: 20px;
  color: #333;
}

.answer-box {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
}

.user-ans {
  margin-bottom: 8px;
  color: #67C23A;
  font-weight: bold;
}

.user-ans.error {
  color: #F56C6C;
}

.correct-ans {
  color: #409EFF;
  font-weight: bold;
}

.analysis {
  border-top: 1px dashed #eee;
  padding-top: 15px;
}

.a-title {
  font-weight: bold;
  color: #666;
  margin-bottom: 5px;
}

.a-content {
  color: #888;
  font-size: 14px;
  line-height: 1.6;
}

.shadow-sm {
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.mb-4 {
  margin-bottom: 20px;
}
</style>
