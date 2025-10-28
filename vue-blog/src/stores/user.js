import { defineStore } from 'pinia'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { login as loginApi, register as registerApi, getUserInfo, googleAuthCallback } from '@/api/auth'

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
                this.userInfo = res.data
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
                this.userInfo = res.data
            } else {
                throw new Error(res.msg || '注册失败')
            }
        },

        async getUserInfo() {
            const res = await getUserInfo()
            // 根据新的API响应格式处理
            if (res.code === 200) {
                this.userInfo = res.data
            } else {
                throw new Error(res.msg || '获取用户信息失败')
            }
        },

        async googleLogin(code) {
            const res = await googleAuthCallback(code)
            if (res.code === 200) {
                this.token = res.data.token
                setToken(res.data.token)
                this.userInfo = res.data
            } else {
                throw new Error(res.msg || 'Google登录失败')
            }
        },

        logout() {
            this.token = ''
            this.userInfo = null
            removeToken()
        }
    }
})
