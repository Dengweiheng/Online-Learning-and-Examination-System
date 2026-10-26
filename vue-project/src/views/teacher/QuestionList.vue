<template>
  <div class="question-list">
    <div class="header">
      <h2>题库管理</h2>
      <div class="btns">
        <el-upload
          action="http://localhost:8080/questionBank/import"
          :show-file-list="false"
          :on-success="handleImportSuccess"
          style="display: inline-block; margin-right: 12px"
        >
          <el-button :icon="Upload">批量导入</el-button>
        </el-upload>
        <el-button :icon="Download" @click="handleExport">导出Execl</el-button>
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增题目</el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <div class="filter-bar">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="所属课程">
          <el-select v-model="queryParams.courseId" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="c in courseList" :key="c.id" :label="c.title" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="queryParams.type" placeholder="全部" clearable style="width: 120px">
            <el-option label="单选题" :value="1" />
            <el-option label="多选题" :value="2" />
            <el-option label="判断题" :value="3" />
            <el-option label="填空题" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="queryParams.difficulty" placeholder="全部" clearable style="width: 100px">
            <el-option label="入门" :value="1" />
            <el-option label="进阶" :value="2" />
            <el-option label="挑战" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="搜索题目内容" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="questions" v-loading="loading">
      <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
      <el-table-column label="所属课程" width="150">
        <template #default="scope">
          {{ getCourseName(scope.row.courseId) }}
        </template>
      </el-table-column>
      <el-table-column prop="type" label="题型" width="100">
        <template #default="scope">
          <el-tag>{{ getTypeName(scope.row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="difficulty" label="难度" width="80">
        <template #default="scope">
          <span :class="'diff-' + scope.row.difficulty">{{ getDiffName(scope.row.difficulty) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="分值" width="80" />
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button type="primary" link @click="handleEdit(scope.row)">详情</el-button>
          <el-button type="danger" link @click="handleDelete(scope.row.id)">删除</el-button>
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

    <!-- 题目编辑器弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑题目' : '新增题目'" width="600px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="所属课程" required>
          <el-select v-model="editForm.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option v-for="c in courseList" :key="c.id" :label="c.title" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型">
          <el-radio-group v-model="editForm.type" @change="handleTypeChange">
            <el-radio :value="1">单选</el-radio>
            <el-radio :value="2">多选</el-radio>
            <el-radio :value="3">判断</el-radio>
            <el-radio :value="4">填空</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="难度">
          <el-rate v-model="editForm.difficulty" :max="3" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="editForm.content" type="textarea" rows="3" />
        </el-form-item>
        
        <!-- 选项管理 (单选/多选) -->
        <template v-if="editForm.type === 1 || editForm.type === 2">
          <el-form-item label="选项">
            <div v-for="(val, key) in dynamicOptions" :key="key" class="option-item">
              <el-input v-model="dynamicOptions[key]">
                <template #prepend>{{ key }}</template>
                <template #append>
                  <el-button :icon="Delete" @click="removeOption(key)" />
                </template>
              </el-input>
            </div>
            <el-button type="primary" link :icon="Plus" @click="addOption">添加选项</el-button>
          </el-form-item>
        </template>

        <el-form-item label="正确答案">
          <el-radio-group v-if="editForm.type === 3" v-model="editForm.answer">
            <el-radio value="正确">正确</el-radio>
            <el-radio value="错误">错误</el-radio>
          </el-radio-group>
          <el-input v-else v-model="editForm.answer" placeholder="多选请用逗号隔开" />
        </el-form-item>
        
        <el-form-item label="分值">
          <el-input-number v-model="editForm.score" :min="1" />
        </el-form-item>
        <el-form-item label="解析">
          <el-input v-model="editForm.analysis" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveQuestion">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Upload, Download, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const questions = ref([])
const total = ref(0)
const courseList = ref([])

const queryParams = reactive({
  current: 1,
  size: 10,
  type: null,
  difficulty: null,
  courseId: null,
  keyword: '',
  teacherId: userStore.userInfo.id
})

const fetchCourses = async () => {
    const res = await request.get('/course/search', { params: { teacherId: userStore.userInfo.id, size: 1000 } })
    courseList.value = res.data.records
}

const fetchData = async () => {
    loading.value = true
    try {
        const res = await request.get('/questionBank/page', { params: queryParams })
        questions.value = res.data.records
        total.value = res.data.total
    } finally {
        loading.value = false
    }
}

const handleSearch = () => {
    queryParams.current = 1
    fetchData()
}

// 选项处理
const dynamicOptions = ref({ A: '', B: '', C: '', D: '' })

const handleTypeChange = () => {
    if (editForm.value.type === 3) {
        editForm.value.answer = '正确'
        editForm.value.options = null
    } else if (editForm.value.type === 4) {
        editForm.value.options = null
    }
}

const addOption = () => {
    const keys = Object.keys(dynamicOptions.value)
    const lastKey = keys.length > 0 ? keys[keys.length - 1] : '@'
    const nextKey = String.fromCharCode(lastKey.charCodeAt(0) + 1)
    dynamicOptions.value[nextKey] = ''
}

const removeOption = (key) => {
    delete dynamicOptions.value[key]
}

const getCourseName = (id) => {
    const course = courseList.value.find(c => c.id === id)
    return course ? course.title : '未知课程'
}

// 编辑器
const dialogVisible = ref(false)
const editForm = ref({
    id: null,
    type: 1,
    content: '',
    options: '',
    answer: '',
    score: 5,
    difficulty: 1,
    analysis: '',
    courseId: null
})

const handleAdd = () => {
    editForm.value = { 
        id: null, 
        type: 1, 
        content: '', 
        options: '', 
        answer: '', 
        score: 5, 
        difficulty: 1, 
        analysis: '',
        courseId: queryParams.courseId // 默认选中当前筛选的课程
    }
    dynamicOptions.value = { A: '', B: '', C: '', D: '' }
    dialogVisible.value = true
}

const handleEdit = (row) => {
    editForm.value = { ...row }
    // 兼容旧数据：将“正确/错误”转换为“T/F”
    if (row.type === 3) {
        if (row.answer === 'T') editForm.value.answer = '正确'
        if (row.answer === 'F') editForm.value.answer = '错误'
    }
    if (row.options) {
        dynamicOptions.value = JSON.parse(row.options)
    }
    dialogVisible.value = true
}

const saveQuestion = async () => {
    if (!editForm.value.courseId) {
        ElMessage.warning('请选择所属课程')
        return
    }
    if (editForm.value.type === 1 || editForm.value.type === 2) {
        editForm.value.options = JSON.stringify(dynamicOptions.value)
    } else if (editForm.value.type === 3) {
        editForm.value.options = JSON.stringify({ '正确': '正确', '错误': '错误' })
    }
    await request.post('/questionBank/save', editForm.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
}

const handleDelete = (id) => {
    ElMessageBox.confirm('确定删除该题目吗？', '提示').then(async () => {
        await request.delete(`/questionBank/${id}`)
        ElMessage.success('已删除')
        fetchData()
    })
}

const handleExport = () => {
    window.open('http://localhost:8080/questionBank/export')
}

const handleImportSuccess = () => {
    ElMessage.success('导入成功')
    fetchData()
}

const getTypeName = (t) => {
    const map = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题' }
    return map[t]
}

const getDiffName = (d) => {
    const map = { 1: '入门', 2: '进阶', 3: '挑战' }
    return map[d]
}

onMounted(async () => {
    await fetchCourses()
    fetchData()
})
</script>

<style scoped>
.question-list { padding: 20px; background: #fff; border-radius: 8px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.filter-bar { background: #f9f9f9; padding: 18px; border-radius: 8px; margin-bottom: 20px; }
.option-item { margin-bottom: 10px; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
.diff-1 { color: #67C23A; }
.diff-2 { color: #E6A23C; }
.diff-3 { color: #F56C6C; }
</style>
