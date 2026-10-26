<template>
  <div class="exam-editor">
    <el-page-header @back="$router.back()" :title="id === 'new' ? '创建考试' : '编辑考试'" class="mb-4" />

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 基础配置 -->
      <el-tab-pane label="基本设置" name="basic">
        <el-form :model="examForm" label-width="100px" style="max-width: 700px">
          <el-form-item label="考试标题">
            <el-input v-model="examForm.title" />
          </el-form-item>
          <el-form-item label="绑定课程">
            <el-select v-model="examForm.courseId" placeholder="请选择课程">
              <el-option v-for="c in courses" :key="c.id" :label="c.title" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="考试时间">
            <el-date-picker
              v-model="timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
          <el-form-item label="考试时长">
            <el-input-number v-model="examForm.duration" :min="1" /> 分钟
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="及格分">
                <el-input-number v-model="examForm.passScore" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="总分">
                <el-input-number v-model="examForm.totalPoints" disabled />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="最大尝试次数">
            <el-input-number v-model="examForm.maxAttempts" :min="1" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="activeTab = 'questions'">下一步：配置题目</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 题目配置 -->
      <el-tab-pane label="题目设置" name="questions">
        <div class="q-actions">
          <el-space>
            <el-button type="success" :icon="Cpu" @click="showRandomDialog">智能随机组卷</el-button>
            <el-button type="primary" :icon="Search" @click="showManualDialog">手动挑选题目</el-button>
          </el-space>
          <div class="summary">已选题目：{{ selectedQuestions.length }} | 当前总分：{{ currentPoints }}</div>
        </div>

        <el-table :data="selectedQuestions" border style="margin-top: 20px">
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
          <el-table-column label="题型" width="100">
            <template #default="s"><el-tag size="small">{{ getTypeName(s.row.type) }}</el-tag></template>
          </el-table-column>
          <el-table-column label="本题分值" width="120">
            <template #default="s">
              <el-input-number v-model="s.row.score" :min="1" size="small" @change="calcTotal" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="s">
              <el-button type="danger" link @click="removeQuestion(s.$index)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div style="margin-top: 30px; text-align: center">
          <el-button type="primary" size="large" @click="handleSave">确认并发布考试</el-button>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 手动选题弹窗 -->
    <el-dialog v-model="manualVisible" title="从题库挑选" width="80%">
      <div class="manual-filter">
        <el-input v-model="bankParams.keyword" placeholder="搜索题目..." style="width: 250px" @change="fetchBank" />
        <el-button type="primary" @click="fetchBank">刷新题库</el-button>
      </div>
      <el-table :data="bank" @selection-change="handleSelectionChange" v-loading="bankLoading">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
        <el-table-column prop="difficulty" label="难度" width="80" />
      </el-table>
      <template #footer>
        <el-button @click="manualVisible = false">取消</el-button>
        <el-button type="primary" @click="addSelected">确定加入 ({{ tempSelection.length }})</el-button>
      </template>
    </el-dialog>

    <!-- 随机抽题弹窗 -->
    <el-dialog v-model="randomVisible" title="智能随机抽题" width="500px">
      <el-alert title="系统将根据您的策略在题库中自动选择题目" type="info" show-icon style="margin-bottom: 20px" />
      <div v-for="(cfg, idx) in randomConfigs" :key="idx" class="rand-cfg">
        <el-select v-model="cfg.type" placeholder="题型" style="width: 100px">
          <el-option label="单选" :value="1" />
          <el-option label="多选" :value="2" />
          <el-option label="判断" :value="3" />
          <el-option label="填空" :value="4" />
        </el-select>
        数量：<el-input-number v-model="cfg.quantity" :min="1" size="small" />
        难度：<el-input-number v-model="cfg.difficulty" :min="1" :max="3" size="small" />
        <el-button :icon="Delete" circle type="danger" size="small" @click="randomConfigs.splice(idx,1)" />
      </div>
      <el-button type="primary" link :icon="Plus" @click="randomConfigs.push({type:1,quantity:5,difficulty:1})">增加抽题规则</el-button>
      <template #footer>
        <el-button @click="randomVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRandom">执行随机抽题</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Plus, Search, Cpu, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const activeTab = ref('basic')
const timeRange = ref([])

const examForm = reactive({
  id: null,
  title: '',
  courseId: null,
  startTime: '',
  endTime: '',
  duration: 60,
  totalPoints: 0,
  passScore: 60,
  maxAttempts: 1
})

const courses = ref([])
const selectedQuestions = ref([])

const fetchInitialData = async () => {
    // 获取课程列表用于绑定
    const cRes = await request.get('/course/search', { params: { size: 100 } })
    courses.value = cRes.data.records

    if (id !== 'new') {
        const res = await request.get(`/exam/detail/${id}`)
        const data = res.data
        Object.assign(examForm, data.exam)
        selectedQuestions.value = data.questions
        
        // 转换日期范围
        if (examForm.startTime && examForm.endTime) {
            timeRange.value = [examForm.startTime.replace('T', ' '), examForm.endTime.replace('T', ' ')]
        }
    }
}

const currentPoints = computed(() => {
    return selectedQuestions.value.reduce((sum, q) => sum + (q.score || 0), 0)
})

const calcTotal = () => {
    examForm.totalPoints = currentPoints.value
}

const handleSave = async () => {
    if (selectedQuestions.value.length === 0) {
        return ElMessage.warning('请至少添加一个题目')
    }
    if (!timeRange.value || timeRange.value.length < 2) {
        return ElMessage.warning('请设置考试起止时间')
    }

    examForm.startTime = timeRange.value[0]
    examForm.endTime = timeRange.value[1]
    examForm.totalPoints = currentPoints.value

    const payload = {
        exam: examForm,
        questions: selectedQuestions.value.map(q => ({ questionId: q.id, score: q.score }))
    }

    await request.post('/exam/save', payload)
    ElMessage.success('考试已发布')
    router.push('/teacher/exams')
}

// 手动选题逻辑
const manualVisible = ref(false)
const bank = ref([])
const bankLoading = ref(false)
const tempSelection = ref([])
const bankParams = reactive({ keyword: '', current: 1, size: 100 })

const fetchBank = async () => {
    bankLoading.value = true
    const res = await request.get('/questionBank/page', { params: bankParams })
    bank.value = res.data.records
    bankLoading.value = false
}

const showManualDialog = () => {
    manualVisible.value = true
    fetchBank()
}

const handleSelectionChange = (val) => {
    tempSelection.value = val
}

const addSelected = () => {
    tempSelection.value.forEach(q => {
        if (!selectedQuestions.value.find(sq => sq.id === q.id)) {
            selectedQuestions.value.push({ ...q })
        }
    })
    calcTotal()
    manualVisible.value = false
}

// 随机抽题逻辑
const randomVisible = ref(false)
const randomConfigs = ref([{ type: 1, quantity: 5, difficulty: 1 }])

const showRandomDialog = () => {
    randomVisible.value = true
}

const confirmRandom = async () => {
    const res = await request.post('/exam/randomQuestions', randomConfigs.value)
    selectedQuestions.value = res.data
    calcTotal()
    randomVisible.value = false
    ElMessage.success(`已随机抽取 ${res.data.length} 道题目`)
}

const removeQuestion = (idx) => {
    selectedQuestions.value.splice(idx, 1)
    calcTotal()
}

const getTypeName = (t) => {
    const map = { 1: '单选', 2: '多选', 3: '判断', 4: '填空' }
    return map[t]
}

onMounted(fetchInitialData)
</script>

<style scoped>
.exam-editor { padding: 20px; }
.mb-4 { margin-bottom: 20px; }
.q-actions { display: flex; justify-content: space-between; align-items: center; }
.summary { font-size: 14px; font-weight: bold; color: #409EFF; }
.manual-filter { margin-bottom: 15px; display: flex; gap: 10px; }
.rand-cfg { background: #f9f9f9; padding: 10px; margin-bottom: 10px; display: flex; align-items: center; gap: 10px; border-radius: 4px; }
</style>
