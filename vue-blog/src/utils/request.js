import axios from "axios";
import { ElMessage } from "element-plus";
import router from "@/router/index.js";
import { getToken } from "@/utils/auth";
import { getApiBaseURL } from "@/utils/apiConfig";

const request = axios.create({
    baseURL: getApiBaseURL(),
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
    // 调试日志：记录所有请求
    // console.log('发送请求:', {
    //     method: config.method?.toUpperCase(),
    //     url: config.url,
    //     baseURL: config.baseURL,
    //     fullURL: `${config.baseURL}${config.url}`,
    //     data: config.data,
    //     headers: config.headers
    // })
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
        const url = error.config?.url
        const method = error.config?.method?.toUpperCase()
        const fullURL = `${error.config?.baseURL || ''}${url}`
        const isPublicResource = error.config?.isPublicResource || false

        // 記錄詳細錯誤信息
        console.error('API請求錯誤:', {
            method,
            url: fullURL,
            status: error.response?.status,
            statusText: error.response?.statusText,
            data: error.response?.data,
            code: error.code,
            message: error.message,
            isPublicResource
        })

        if (error.response) {
            const status = error.response.status
            const errorData = error.response.data

            // 對於公開資源，某些錯誤可以靜默處理
            if (status === 404) {
                // 404 錯誤不顯示通用錯誤提示，由具體頁面處理
                if (!isPublicResource) {
                    console.error(`接口不存在: ${fullURL}`)
                }
            } else if (status === 500) {
                if (!isPublicResource) {
                    ElMessage.error('服務器內部錯誤，請稍後重試')
                }
            } else if (status === 401) {
                // 對於公開資源，不顯示"未授權，請重新登錄"的錯誤
                // 允許用戶繼續訪問，只是某些需要登錄的功能可能不可用
                if (!isPublicResource) {
                    ElMessage.error('未授權，請重新登錄')
                } else {
                    // 公開資源的401錯誤，靜默處理或只記錄日志
                    console.warn('公開資源訪問返回401，可能是token過期，但不影響訪問')
                }
            } else if (status === 403) {
                if (!isPublicResource) {
                    ElMessage.error('權限不足')
                }
            } else {
                // 其他狀態碼錯誤，根據是否為公開資源決定是否顯示
                if (!isPublicResource) {
                    const errorMsg = errorData?.message || errorData?.error || `請求失敗 (${status})`
                    ElMessage.error(errorMsg)
                }
            }
        } else if (error.code === 'ECONNREFUSED' || error.code === 'ERR_NETWORK') {
            console.error('連接失敗:', {
                code: error.code,
                url: fullURL,
                apiBaseURL: error.config?.baseURL,
                message: error.message
            })
            // 網絡錯誤只在非公開資源或生產環境顯示
            if (!isPublicResource || import.meta.env.PROD) {
                ElMessage.error('無法連接到服務器，請檢查網絡連接和API配置')
            }
        } else if (error.code === 'ENOTFOUND') {
            console.error('域名解析失敗:', error.code)
            if (!isPublicResource) {
                ElMessage.error('網絡連接失敗，請檢查網絡設置')
            }
        } else if (error.message?.includes('timeout') || error.code === 'ECONNABORTED') {
            console.error('請求超時:', error.code)
            if (!isPublicResource) {
                ElMessage.error('請求超時，請檢查網絡連接')
            }
        } else {
            console.error('網絡請求失敗:', error.message)
            // 其他錯誤根據是否為公開資源決定是否顯示
            if (!isPublicResource) {
                ElMessage.error('網絡請求失敗，請檢查網絡連接')
            }
        }
        return Promise.reject(error)
    }

)


export default request

