import axios from "axios";
import { ElMessage } from "element-plus";
import router from "@/router/index.js";
import { getToken } from "@/utils/auth";

const request = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:9090",
    timeout: 30000 //后台接口超时时间
})

//request 拦截器
//可以自动在请求发送前对请求做一些处理
request.interceptors.request.use(config => {
    const token = getToken();
    if (token) {
        // 后端JWT拦截器读取的是 authentication 请求头
        config.headers['authentication'] = token;
    }
    // 如果请求中没有设置Content-Type，则默认设置为application/json
    // 对于文件上传，应该让axios自动设置multipart/form-data（包含boundary）
    if (!config.headers['Content-Type'] && !(config.data instanceof FormData)) {
        config.headers['Content-Type'] = 'application/json;charset=utf-8';
    }
    return config
}, error => {
    return Promise.reject(error)
});

//response 拦截器
//可以自动在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        //兼容服务器返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res;
        }
        return res;
    },
    error => {
        console.error('API请求错误:', error)

        if (error.response) {
            const status = error.response.status
            const url = error.config?.url
            // 判断是否为公开资源（不需要认证的API）
            const isPublicResource = error.config?.isPublicResource || false

            console.error(`请求失败: ${status} - ${url}`)

            if (status === 404) {
                ElMessage.error(`接口不存在: ${url}`)
            } else if (status === 500) {
                ElMessage.error('服务器内部错误，请查看后端控制台')
            } else if (status === 401) {
                // 对于公开资源，不显示"未授权，请重新登录"的错误
                // 允许用户继续访问，只是某些需要登录的功能可能不可用
                if (!isPublicResource) {
                    ElMessage.error('未授权，请重新登录')
                } else {
                    // 公开资源的401错误，静默处理或只记录日志
                    console.warn('公开资源访问返回401，可能是token过期，但不影响访问')
                }
            } else if (status === 403) {
                ElMessage.error('权限不足')
            } else {
                ElMessage.error(`请求失败 (${status})`)
            }
        } else if (error.code === 'ECONNREFUSED') {
            console.error('连接被拒绝，请检查后端服务是否启动')
            ElMessage.error('无法连接到服务器，请检查后端服务是否启动')
        } else if (error.code === 'ENOTFOUND') {
            console.error('域名解析失败')
            ElMessage.error('网络连接失败，请检查网络设置')
        } else if (error.message.includes('timeout')) {
            console.error('请求超时')
            ElMessage.error('请求超时，请检查网络连接')
        } else {
            console.error('网络请求失败:', error.message)
            ElMessage.error('网络请求失败，请检查网络连接')
        }
        return Promise.reject(error)
    }

)


export default request

