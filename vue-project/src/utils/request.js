import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
    baseURL: 'http://localhost:8080', // 后端地址
    timeout: 5000
})

// 请求拦截器
request.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers['Authorization'] = token // 统一携带 token
    }
    return config
}, error => {
    return Promise.reject(error)
})

// 响应拦截器
request.interceptors.response.use(response => {
    const res = response.data
    // 如果返回码不是 200，弹出错误
    if (res.code !== 200) {
        ElMessage.error(res.msg || '系统错误')
        return Promise.reject(new Error(res.msg || 'Error'))
    }
    return res
}, error => {
    ElMessage.error(error.message || '网络连接失败')
    return Promise.reject(error)
})

export default request
