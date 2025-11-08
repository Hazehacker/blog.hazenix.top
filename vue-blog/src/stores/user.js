import { defineStore } from 'pinia'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { login as loginApi, register as registerApi, getUserInfo, googleAuthCallback, getGithubAuthUrl, githubAuthCallback, logout as logoutApi } from '@/api/auth'

// 规范化用户数据，统一role和isAdmin字段
function normalizeUserData(userData) {
    if (!userData) return null

    // 如果已经有isAdmin字段，直接返回
    if (userData.hasOwnProperty('isAdmin')) {
        return userData
    }

    // 如果有role字段，将其转换为isAdmin
    // role: 0管理员 2用户（根据用户端API接口文档的备注）
    if (userData.hasOwnProperty('role')) {
        return {
            ...userData,
            isAdmin: userData.role === 0
        }
    }

    // 如果没有role字段，但id为1，则认为是管理员（兼容处理）
    // 注意：这是一个临时的兼容方案，理想情况下后端应该返回role字段
    if (userData.id === 1) {
        return {
            ...userData,
            isAdmin: true
        }
    }

    return userData
}

export const useUserStore = defineStore('user', {
    state: () => ({
        token: getToken() || '',
        userInfo: null,
    }),

    actions: {
        async login(loginForm) {
            const res = await loginApi(loginForm)
            // 根据新的API响应格式处理
            if (res.code === 200) {
                this.token = res.data.token
                setToken(res.data.token)
                // 处理登录响应：可能data包含user对象，也可能直接是user数据
                const userData = res.data.user || res.data
                console.log('登录响应数据:', res.data)
                console.log('提取的用户数据:', userData)
                this.userInfo = normalizeUserData(userData)
                console.log('规范化后的用户信息:', this.userInfo)
            } else {
                throw new Error(res.msg || '登录失败')
            }
        },

        async register(registerForm) {
            const res = await registerApi(registerForm)
            // 根据新的API响应格式处理
            if (res.code === 200) {
                this.token = res.data.token
                setToken(res.data.token)
                const userData = res.data.user || res.data
                this.userInfo = normalizeUserData(userData)
            } else {
                throw new Error(res.msg || '注册失败')
            }
        },

        async getUserInfo() {
            const res = await getUserInfo()
            // 根据新的API响应格式处理
            if (res.code === 200) {
                console.log('获取用户信息响应:', res.data)
                this.userInfo = normalizeUserData(res.data)
                console.log('更新后的用户信息:', this.userInfo)
            } else {
                throw new Error(res.msg || '获取用户信息失败')
            }
        },

        async googleLogin(code) {
            const res = await googleAuthCallback(code)
            if (res.code === 200) {
                this.token = res.data.token
                setToken(res.data.token)
                const userData = res.data.user || res.data
                this.userInfo = normalizeUserData(userData)
            } else {
                throw new Error(res.msg || 'Google登录失败')
            }
        },

        async githubLogin(code) {
            const res = await githubAuthCallback(code)
            if (res.code === 200) {
                // 根据接口文档，响应数据格式为：
                // { code: 200, msg: "success", data: { id, userName, avatar, email, token } }
                // token 和用户信息都在 data 对象中
                this.token = res.data.token
                setToken(res.data.token)
                // 从 data 中提取用户信息（排除 token 字段）
                const { token, ...userData } = res.data
                this.userInfo = normalizeUserData(userData)
            } else {
                throw new Error(res.msg || 'GitHub登录失败')
            }
        },

        async logout() {
            try {
                // 调用后端logout接口
                await logoutApi()
            } catch (error) {
                // 即使后端接口调用失败，也要清除本地数据
                console.error('退出登录接口调用失败:', error)
            } finally {
                // 清除本地token和用户信息
                this.token = ''
                this.userInfo = null
                removeToken()
            }
        }
    }
})
