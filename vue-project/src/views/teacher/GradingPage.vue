<template>
  <div class="grading-page" v-if="record">
    <el-page-header @back="$router.back()" :title="'正在阅卷 - ' + record.nickname" class="mb-4" />

    <el-card v-for="(item, index) in details" :key="item.id" class="q-card">
      <div class="q-header">
        <span class="q-num">第{{ index + 1 }}题</span>
        <el-tag size="small">{{ getTypeName(item.question.type) }}</el-tag>
        <div class="grading-zone">
          得分：
          <el-input-number 
            v-model="item.score" 
            :max="item.question.score" 
            :min="0"
            :disabled="item.question.type !== 4"
            size="small"
          /> 
          / {{ item.question.score }}
        </div>
      </div>

      <div class="q-body">
        <div class="content">{{ item.question.content }}</div>
        
        <div class="ans-comparison">
          <div class="ans-item student">
            <div class="label">学生打答案：</div>
            <div class="val" :class="{ 'correct': item.score > 0, 'wrong': item.score === 0 }">
                {{ item.userAnswer || '(未答)' }}
            </div>
          </div>
          <div class="ans-item target">
            <div class="label">标准答案：</div>
            <div class="val">{{ item.question.answer }}</div>
          </div>
        </div>
        
        <div v-if="item.question.analysis" class="analysis">
          解析：{{ item.question.analysis }}
        </div>
      </div>
    </el-card>

    <div class="footer">
        <div class="current-total">当前总分：{{ totalScore }}</div>
        <el-button type="primary" size="large" @click="submitGrading">提交阅卷结果</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const recordId = route.params.id
const record = ref(null)
const details = ref([])

const fetchData = async () => {
    const res = await request.get(`/exam/recordDetails/${recordId}`)
    record.value = res.data.record
    details.value = res.data.details
}

const totalScore = computed(() => {
    return details.value.reduce((sum, d) => sum + (d.score || 0), 0)
})

const submitGrading = async () => {
    const gradingData = details.value.map(d => ({
        id: d.id,
        score: d.score
    }))
    await request.post(`/exam/submitGrading`, {
        recordId: recordId,
        details: gradingData
    })
    ElMessage.success('阅卷成功')
    router.back()
}

const getTypeName = (t) => {
    const map = { 1: '单选', 2: '多选', 3: '判断', 4: '填空' }
    return map[t]
}

onMounted(fetchData)
</script>

<style scoped>
.grading-page { padding: 20px; max-width: 900px; margin: 0 auto; padding-bottom: 100px; }
.q-card { margin-bottom: 20px; }
.q-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; padding-bottom: 15px; margin-bottom: 15px; }
.q-num { font-weight: bold; font-size: 16px; margin-right: 10px; }
.grading-zone { font-size: 14px; display: flex; align-items: center; gap: 5px; }
.ans-comparison { background: #f9f9f9; padding: 15px; border-radius: 8px; margin: 20px 0; }
.ans-item { display: flex; margin-bottom: 10px; }
.ans-item .label { color: #666; width: 100px; }
.ans-item .val { font-weight: bold; }
.correct { color: #67C23A; }
.wrong { color: #F56C6C; }
.analysis { font-size: 13px; color: #999; border-top: 1px dashed #ddd; padding-top: 10px; }
.footer { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; box-shadow: 0 -2px 10px rgba(0,0,0,0.05); padding: 20px 40px; display: flex; justify-content: space-between; align-items: center; z-index: 100; }
.current-total { font-size: 20px; font-weight: bold; color: #409EFF; }
</style>
