<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
          <el-button type="primary" @click="handleUpdate">保存修改</el-button>
        </div>
      </template>
      
      <div class="profile-content">
        <div class="avatar-section">
          <el-upload
            class="avatar-uploader"
            action="http://localhost:8080/file/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
          >
            <img v-if="userInfo.avatar" :src="'http://localhost:8080' + userInfo.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            <div class="avatar-mask">更换头像</div>
          </el-upload>
          <h3>{{ userInfo.nickname || userInfo.username }}</h3>
          <el-tag>{{ userInfo.role }}</el-tag>
        </div>
        
        <el-form :model="userInfo" label-width="100px" class="profile-form">
          <el-form-item label="用户名">
            <el-input v-model="userInfo.username" disabled />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="userInfo.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="userInfo.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="userInfo.phone" placeholder="请输入手机号" />
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const handleAvatarSuccess = (res) => {
  if (res.code === 200) {
    userStore.updateInfo({ avatar: res.data })
    ElMessage.success('头像上传成功')
  }
}

const handleUpdate = async () => {
  try {
    await userStore.updateInfo(userInfo.value)
    ElMessage.success('修改成功')
  } catch (err) {
    console.error(err)
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-content {
  display: flex;
  gap: 40px;
  padding: 20px 0;
}

.avatar-section {
  width: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.avatar-uploader {
  border: 1px dashed var(--el-border-color);
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 120px;
  height: 120px;
}

.avatar-uploader:hover {
  border-color: var(--el-color-primary);
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.avatar-mask {
  position: absolute;
  bottom: 0;
  width: 100%;
  background: rgba(0, 0, 0, 0.4);
  color: #fff;
  font-size: 12px;
  text-align: center;
  padding: 4px 0;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-uploader:hover .avatar-mask {
  opacity: 1;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
  line-height: 120px;
}

.profile-form {
  flex: 1;
}
</style>
