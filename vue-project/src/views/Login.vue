<template>
  <div class="login-page">
    <div class="login-left">
      <div class="brand">
        <el-icon :size="40" color="#fff"><Reading /></el-icon>
        <h1>智学考评系统</h1>
      </div>
      <div class="welcome-text">
        <h2>开启您的智慧学习之旅</h2>
        <p>一站式在线学习与考试解决方案，助力教育数字化转型。</p>
      </div>
      <div class="footer-links">
        <span>© 2026 智学教育科技</span>
        <span>隐私政策</span>
        <span>服务条款</span>
      </div>
    </div>
    
    <div class="login-right">
      <div class="login-box">
        <div class="login-header">
          <h2>账号登录</h2>
          <p>欢迎回到系统，请填写您的凭证</p>
        </div>
        
        <el-form :model="loginForm" :rules="rules" ref="loginRef" label-position="top" size="large">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入用户名" :prefix-icon="User" />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
          </el-form-item>
          
          <div class="login-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </div>
          
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">立即登录</el-button>
          
          <div class="login-footer">
            还没有账号？<el-link type="primary" :underline="false" @click="$router.push('/register')">立即注册</el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock, Reading } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loginRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  loginRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(loginForm)
        ElMessage.success('登录成功')
        router.push('/')
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
.login-page {
  height: 100vh;
  display: flex;
  background-color: #f4f7f9;
}

.login-left {
  flex: 1.2;
  background: linear-gradient(135deg, #1a365d 0%, #2d3748 100%);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 60px;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.login-left::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('@/assets/images/login-bg.jpg') center/cover no-repeat;
  opacity: 0.15;
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

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
}

.login-box {
  width: 100%;
  max-width: 440px;
  padding: 40px;
}

.login-header {
  margin-bottom: 40px;
}

.login-header h2 {
  font-size: 32px;
  color: #1a202c;
  margin-bottom: 12px;
  font-weight: 700;
}

.login-header p {
  color: #718096;
  font-size: 16px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.login-btn {
  width: 100%;
  height: 50px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(to right, #3182ce, #2b6cb0);
  border: none;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(49, 130, 206, 0.3);
}

.login-footer {
  text-align: center;
  margin-top: 30px;
  font-size: 15px;
  color: #718096;
}

/* PC Responsive */
@media (max-width: 1024px) {
  .login-left {
    display: none;
  }
}
</style>
