<template>
  <div class="edit-container">
    <el-page-header @back="$router.back()" :title="id === 'new' ? '创建课程' : '编辑课程'" class="mb-4" />
    
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 基础信息 -->
      <el-tab-pane label="基础设置" name="basic">
        <el-form :model="form" label-width="100px" style="max-width: 600px">
          <el-form-item label="课程标题">
            <el-input v-model="form.title" placeholder="输入课程标题" />
          </el-form-item>
          <el-form-item label="封面图">
            <el-upload
              class="cover-uploader"
              action="http://localhost:8080/file/upload"
              :show-file-list="false"
              :on-success="handleCoverSuccess"
              :headers="uploadHeaders"
            >
              <img v-if="form.cover" :src="'http://localhost:8080' + form.cover" class="cover" />
              <el-icon v-else class="uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>
          <el-form-item label="课程分类">
            <el-select v-model="form.categoryId" placeholder="请选择分类">
              <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="公开课">
            <el-switch v-model="form.isPublic" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="课程码" v-if="!form.isPublic">
            <el-input v-model="form.courseCode" placeholder="设置选课密钥" />
          </el-form-item>
          <el-form-item label="简介">
            <el-input v-model="form.description" type="textarea" rows="3" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveCourse">保存基础信息</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 章节管理 -->
      <el-tab-pane label="章节管理" name="outline" :disabled="!form.id">
        <div class="outline-header">
          <el-button type="primary" size="small" @click="addChapter">添加章节</el-button>
        </div>

        <el-collapse v-model="openChapters">
          <el-collapse-item v-for="(chapter, cIdx) in chapters" :key="chapter.id" :name="chapter.id">
            <template #title>
              <div class="chapter-bar">
                <span>{{ chapter.title }}</span>
                <div class="btns">
                  <el-button type="primary" link @click.stop="addResource(chapter.id)">添加资料</el-button>
                  <el-button type="danger" link @click.stop="deleteChapter(chapter.id)">删除</el-button>
                </div>
              </div>
            </template>

            <el-table :data="chapter.resources" size="small">
              <el-table-column prop="title" label="资料名称" />
              <el-table-column prop="type" label="类型" width="100">
                <template #default="scope">
                  <el-tag size="small" :type="scope.row.type === 'VIDEO' ? 'success' : 'warning'">
                    {{ scope.row.type === 'VIDEO' ? '视频' : '文档' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button type="primary" link @click="handleEditResource(scope.row)">编辑</el-button>
                  <el-button type="danger" link @click="deleteResource(scope.row.id)">移除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-collapse-item>
        </el-collapse>
      </el-tab-pane>
    </el-tabs>

    <!-- 添加/编辑资料弹窗 -->
    <el-dialog v-model="resDialogVisible" :title="resForm.id ? '编辑资料' : '上传资料'" width="450px">
      <el-form :model="resForm" label-width="80px">
        <el-form-item label="资料名称">
          <el-input v-model="resForm.title" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="resForm.type">
            <el-radio value="VIDEO">视频</el-radio>
            <el-radio value="DOC">文档</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="文件">
          <el-upload
            action="http://localhost:8080/file/upload"
            :limit="1"
            :on-success="handleFileSuccess"
            :headers="uploadHeaders"
          >
            <el-button type="info" size="small">{{ resForm.content ? '重新上传' : '选择文件并上传' }}</el-button>
            <div v-if="resForm.content" class="file-status">已关联文件</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveResource">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const id = route.params.id
const activeTab = ref('basic')
const categories = ref([])
const chapters = ref([])
const openChapters = ref([])

const uploadHeaders = {
  Authorization: localStorage.getItem('token')
}

const form = ref({
  id: null,
  title: '',
  cover: '',
  categoryId: null,
  isPublic: 1,
  courseCode: '',
  description: ''
})

const fetchCategories = async () => {
  const res = await request.get('/category/list', { params: { type: 1 } })
  categories.value = res.data
}

const fetchData = async () => {
  if (id === 'new') return
  const res = await request.get('/course/detail', { params: { courseId: id } })
  form.value = res.data.course
  chapters.value = res.data.chapters
}

const handleCoverSuccess = (res) => {
  console.log('Cover Upload Response:', res)
  if (res.code === 200) {
    form.value.cover = res.data
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

const saveCourse = async () => {
  await request.post('/course/save', form.value)
  ElMessage.success('课程信息已更新')
}

const addChapter = () => {
  ElMessageBox.prompt('请输入章节标题', '添加章节').then(async ({ value }) => {
    await request.post('/course/chapter/save', {
      courseId: form.value.id,
      title: value
    })
    fetchData()
  })
}

const deleteChapter = (cid) => {
  request.delete(`/course/chapter/${cid}`).then(() => fetchData())
}

// 资料管理
const resDialogVisible = ref(false)
const resForm = ref({
  chapterId: null,
  title: '',
  type: 'VIDEO',
  content: ''
})

const addResource = (chapterId) => {
  resForm.value = { id: null, chapterId, title: '', type: 'VIDEO', content: '' }
  resDialogVisible.value = true
}

const handleEditResource = (row) => {
  resForm.value = { ...row }
  resDialogVisible.value = true
}

const handleFileSuccess = (res) => {
  resForm.value.content = res.data
}

const saveResource = async () => {
  await request.post('/course/resource/save', resForm.value)
  ElMessage.success('资料已上传')
  resDialogVisible.value = false
  fetchData()
}

const deleteResource = (rid) => {
  request.delete(`/course/resource/${rid}`).then(() => fetchData())
}

onMounted(() => {
  fetchCategories()
  fetchData()
})
</script>

<style scoped>
.edit-container {
  padding: 20px;
}

.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  width: 178px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.outline-header {
  margin-bottom: 20px;
}

.chapter-bar {
  display: flex;
  justify-content: space-between;
  width: 100%;
  padding-right: 20px;
}

.mb-4 {
  margin-bottom: 20px;
}
</style>
