import request from '@/utils/request'

export function login(data) {
    return request({
        url: '/user/user/login',
        method: 'post',
        data
    })
}

export function register(data) {
    return request({
        url: '/user/user/register',
        method: 'post',
        data
    })
}

export function getUserInfo() {
    return request({
        url: '/user/user/userinfo',
        method: 'get'
    })
}

// 前端直登（Google id_token 交換應用端 token）
export function loginWithGoogleIdToken(idToken) {
    return request({
        url: '/user/user/google/idtoken-login',
        method: 'post',
        data: { idToken }
    })
}

// Google第三方授权相关接口
export function getGoogleAuthUrl() {
    return request({
        url: '/user/user/google/url',
        method: 'get'
    })
}

export function googleAuthCallback(code) {
    return request({
        url: '/user/user/google/callback',
        method: 'get',
        params: { code }
    })
}

// GitHub第三方授权相关接口
export function getGithubAuthUrl() {
    return request({
        url: '/user/user/github/url',
        method: 'get'
    })
}

export function githubAuthCallback(code) {
    return request({
        url: '/user/user/github/callback',
        method: 'get',
        params: { code }
    })
}

// 微信第三方授权相关接口
export function getWechatAuthUrl() {
    return request({
        url: '/user/user/wechat/url',
        method: 'get'
    })
}

export function wechatAuthCallback(code) {
    return request({
        url: '/user/user/wechat/callback',
        method: 'get',
        params: { code }
    })
}

// 用户退出登录
export function logout() {
    return request({
        url: '/user/user/logout',
        method: 'post'
    })
}