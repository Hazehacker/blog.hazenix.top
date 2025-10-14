import { defineStore } from 'pinia'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { login as loginApi, register as registerApi, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', {
    state: () => ({
        token: getToken() || '',
        userInfo: null,
    }),

    actions: {
        async login(loginForm) {
            const res = await loginApi(loginForm)
            this.token = res.data.token
            setToken(res.data.token)
            await this.getUserInfo()
        },

        async register(registerForm) {
            await registerApi(registerForm)
        },

        async getUserInfo() {
            const res = await getUserInfo()
            this.userInfo = res.data
        },

        logout() {
            this.token = ''
            this.userInfo = null
            removeToken()
        }
    }
})
