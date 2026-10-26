<template>
  <div class="exam-execution-container" v-if="exam">
    <!-- 顶部状态栏 -->
    <div class="exam-header">
      <div class="left">
        <h2 class="title">{{ exam.title }}</h2>
      </div>
      <div class="right">
        <div class="timer">
          <el-icon><Timer /></el-icon>
           倒计时：<span :class="{ 'warning': timeLeft < 300 }">{{ formatTime(timeLeft) }}</span>
        </div>
        <el-button type="danger" @click="confirmSubmit">立即交卷</el-button>
      </div>
    </div>

    <div class="main-layout">
      <!-- 左侧题目 -->
      <div class="question-pane">
        <div class="question-card" v-for="(q, index) in questions" :key="q.id" :id="'q-' + (index + 1)">
          <div class="q-header">
            <span class="q-num">{{ index + 1 }}.</span>
            <el-tag size="small" type="info">{{ getTypeName(q.type) }}</el-tag>
            <span class="q-score">({{ q.score }}分)</span>
          </div>
          <div class="q-content">{{ q.content }}</div>
          
          <!-- 单选题 -->
          <div v-if="q.type === 1" class="q-options">
            <el-radio-group v-model="userAnswers[q.id]">
              <el-radio v-for="(opt, key) in parseOptions(q.options, q.type)" :key="key" :value="opt">
                {{ opt }}
              </el-radio>
            </el-radio-group>
          </div>

          <!-- 判断题 -->
          <div v-else-if="q.type === 3" class="q-options">
            <el-radio-group v-model="userAnswers[q.id]" size="large">
              <el-radio-button label="正确" value="正确" />
              <el-radio-button label="错误" value="错误" />
            </el-radio-group>
          </div>

          <!-- 多选题 -->
          <div v-else-if="q.type === 2" class="q-options">
            <el-checkbox-group v-model="userAnswers[q.id]">
              <el-checkbox v-for="(opt, key) in parseOptions(q.options, q.type)" :key="key" :value="opt">
                {{ opt }}
              </el-checkbox>
            </el-checkbox-group>
          </div>

          <!-- 填空题 -->
          <div v-else-if="q.type === 4" class="q-answer">
            <el-input v-model="userAnswers[q.id]" placeholder="请输入您的答案" />
          </div>
        </div>
      </div>

      <!-- 右侧答题卡 -->
      <div class="answer-card">
        <div class="card-title">答题卡</div>
        <div class="card-grid">
          <div 
            v-for="i in questions.length" 
            :key="i" 
            :class="['card-item', { answered: isAnswered(questions[i-1].id) }]"
            @click="scrollToQuestion(i)"
          >
            {{ i }}
          </div>
        </div>
        <div class="legend">
          <div class="l-item"><span class="box answered"></span> 已答</div>
          <div class="l-item"><span class="box"></span> 未答</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Timer } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const exam = ref(null)
const questions = ref([])
const recordId = ref(null)
const userAnswers = ref({})
const timeLeft = ref(0)
let timerId = null

const fetchData = async () => {
  const examId = route.params.id
  try {
    const res = await request.post('/exam/start', {
      examId,
      studentId: userStore.userInfo.id
    })
    exam.value = res.data.exam
    questions.value = res.data.questions
    recordId.value = res.data.recordId
    
    // 使用服务端计算的剩余时间
    timeLeft.value = res.data.remainingTime

    // 恢复本地存储的答案（防刷新丢失）
    const localAns = localStorage.getItem(`exam_ans_${recordId.value}`)
    if (localAns) {
      userAnswers.value = JSON.parse(localAns)
    } else {
      // 初始化多选题答案为数组
      questions.value.forEach(q => {
        if (q.type === 2) {
          userAnswers.value[q.id] = []
        }
      })
    }

    startTimer()
    startAutoSave()
  } catch (err) {
    console.error(err)
  }
}

let autoSaveId = null
const startAutoSave = () => {
  autoSaveId = setInterval(() => {
    localStorage.setItem(`exam_ans_${recordId.value}`, JSON.stringify(userAnswers.value))
  }, 5000)
}

const startTimer = () => {
  timerId = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--
    } else {
      autoSubmit()
    }
  }, 1000)
}

const formatTime = (seconds) => {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${s < 10 ? '0' + s : s}`
}

const parseOptions = (optionsJson, type) => {
  if (type === 3 && (!optionsJson || optionsJson === 'null')) {
    return { 'T': '正确', 'F': '错误' }
  }
  try {
    return JSON.parse(optionsJson)
  } catch (e) {
    return {}
  }
}

const getTypeName = (type) => {
  const map = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题' }
  return map[type] || '其他'
}

const isAnswered = (qId) => {
  const ans = userAnswers.value[qId]
  if (Array.isArray(ans)) return ans.length > 0
  return ans !== undefined && ans !== ''
}

const scrollToQuestion = (index) => {
  const el = document.getElementById('q-' + index)
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}

const confirmSubmit = () => {
  ElMessageBox.confirm('确定提交试卷吗？提交后无法更改。', '交卷提醒', {
    confirmButtonText: '确认交卷',
    cancelButtonText: '检查一下',
    type: 'warning'
  }).then(() => {
    submit()
  })
}

const autoSubmit = () => {
  ElMessage.warning('考试时间到，系统已自动交卷')
  submit()
}

const submit = async () => {
  clearInterval(timerId)
  if (autoSaveId) clearInterval(autoSaveId)
  localStorage.removeItem(`exam_ans_${recordId.value}`)
  
  // 转换答案格式
  const details = Object.entries(userAnswers.value).map(([qId, ans]) => ({
    questionId: qId,
    userAnswer: Array.isArray(ans) ? ans.sort().join(',') : ans
  }))

  try {
    await request.post(`/exam/submit`, {
      recordId: recordId.value,
      details: details
    })
    ElMessage.success('试卷提交成功')
    router.push('/exam-list')
  } catch (err) {}
}

onMounted(fetchData)

onUnmounted(() => {
  if (timerId) clearInterval(timerId)
})
</script>

<style scoped>
.exam-execution-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}

.exam-header {
  height: 70px;
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  z-index: 10;
}

.right {
  display: flex;
  align-items: center;
  gap: 30px;
}

.timer {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
  display: flex;
  align-items: center;
  gap: 8px;
}

.timer span.warning {
  color: #f56c6c;
}

.main-layout {
  flex: 1;
  display: flex;
  overflow: hidden;
  padding: 20px 40px;
  gap: 20px;
}

.question-pane {
  flex: 1;
  overflow-y: auto;
  padding-right: 10px;
}

.question-card {
  background: #fff;
  padding: 30px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.q-header {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.q-num {
  font-size: 20px;
  font-weight: bold;
}

.q-score {
  color: #999;
  font-size: 14px;
}

.q-content {
  font-size: 17px;
  margin-bottom: 25px;
  line-height: 1.6;
}

.q-options .el-radio-group, .q-options .el-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 15px;
  align-items: flex-start;
  width: 100%;
}

.answer-card {
  width: 280px;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  height: fit-content;
}

.card-title {
  font-weight: bold;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
  margin-bottom: 30px;
}

.card-item {
  height: 35px;
  border: 1px solid #ddd;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.card-item:hover {
  border-color: #409EFF;
  color: #409EFF;
}

.card-item.answered {
  background: #409EFF;
  border-color: #409EFF;
  color: #fff;
}

.legend {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #999;
}

.legend .box {
  display: inline-block;
  width: 12px;
  height: 12px;
  border: 1px solid #ddd;
  vertical-align: middle;
  border-radius: 2px;
}

.legend .box.answered {
  background: #409EFF;
  border-color: #409EFF;
}
</style>
