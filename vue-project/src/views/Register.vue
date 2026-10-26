<template>
  <div class="register-page">
    <div class="register-left">
      <div class="brand">
        <el-icon :size="40" color="#fff"><Reading /></el-icon>
        <h1>智学考评系统</h1>
      </div>
      <div class="welcome-text">
        <h2>加入我们，共创未来</h2>
        <p>通过注册，您可以访问海量优质课程，参与在线考试，并追踪您的每一个进步时刻。</p>
      </div>
      <div class="footer-links">
        <span>© 2026 智学教育科技</span>
        <span>隐私政策</span>
        <span>服务条款</span>
      </div>
    </div>
    
    <div class="register-right">
      <div class="register-box">
        <div class="register-header">
          <h2>创建账号</h2>
          <p>请填写以下信息完成注册</p>
        </div>
        
        <el-form :model="regForm" :rules="rules" ref="regRef" label-position="top" size="large">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="regForm.username" placeholder="请输入用户名" :prefix-icon="User" />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input v-model="regForm.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
          </el-form-item>
          
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="regForm.nickname" placeholder="请输入昵称" :prefix-icon="Postcard" />
          </el-form-item>
          
          <el-form-item label="身份" prop="role">
            <el-radio-group v-model="regForm.role">
              <el-radio label="STUDENT">学生</el-radio>
              <el-radio label="TEACHER">教师</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-button type="primary" class="register-btn" :loading="loading" @click="handleRegister">立即注册</el-button>
          
          <div class="register-footer">
            已经有账号？<el-link type="primary" :underline="false" @click="$router.push('/login')">直接登录</el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock, Postcard, Reading } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const regRef = ref(null)
const loading = ref(false)

const regForm = reactive({
  username: '',
  password: '',
  nickname: '',
  role: 'STUDENT'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  role: [{ required: true, message: '请选择身份', trigger: 'change' }]
}

const handleRegister = async () => {
  regRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await request.post('/user/register', regForm)
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (err) {
        console.error(err)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-page {
  height: 100vh;
  display: flex;
  background-color: #f4f7f9;
}

.register-left {
  flex: 1.2;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 60px;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.register-left::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('@/assets/images/register-bg.jpg') center/cover no-repeat;
  opacity: 0.1;
}

.brand {
  display: flex;
  align-items: center;
  gap: 15px;
  position: relative;
  z-index: 1;
}

.brand h1 {
  margin: 0;
  font-size: 28px;
  letter-spacing: 2px;
  font-weight: 700;
}

.welcome-text {
  position: relative;
  z-index: 1;
}

.welcome-text h2 {
  font-size: 42px;
  margin-bottom: 20px;
  font-weight: 700;
  line-height: 1.2;
}

.welcome-text p {
  font-size: 18px;
  opacity: 0.8;
  max-width: 500px;
  line-height: 1.6;
}

.footer-links {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 30px;
  font-size: 14px;
  opacity: 0.6;
}

.register-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
}

.register-box {
  width: 100%;
  max-width: 440px;
  padding: 40px;
}

.register-header {
  margin-bottom: 30px;
}

.register-header h2 {
  font-size: 32px;
  color: #1a202c;
  margin-bottom: 12px;
  font-weight: 700;
}

.register-header p {
  color: #718096;
  font-size: 16px;
}

.register-btn {
  width: 100%;
  height: 50px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(to right, #38a169, #2f855a);
  border: none;
  transition: all 0.3s ease;
  margin-top: 10px;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(56, 161, 105, 0.3);
}

.register-footer {
  text-align: center;
  margin-top: 30px;
  font-size: 15px;
  color: #718096;
}

@media (max-width: 1024px) {
  .register-left {
    display: none;
  }
}
</style>
