import request from '@/utils/request'

export function login(data) {
    return request({
        url: '/api/user/user/login',
        method: 'post',
        data
    })
}

export function register(data) {
    return request({
        url: '/api/user/user/register',
        method: 'post',
        data
    })
}

export function getUserInfo() {
    return request({
        url: '/api/user/user/userinfo',
        method: 'get'
    })
}

// Google第三方授权相关接口
export function getGoogleAuthUrl() {
    return request({
        url: '/api/user/user/google/url',
        method: 'get'
    })
}

export function googleAuthCallback(code) {
    return request({
        url: '/api/user/user/google/callback',
        method: 'get',
        params: { code }
    })
}

// GitHub第三方授权相关接口
export function getGithubAuthUrl() {
    return request({
        url: '/api/user/user/github/url',
        method: 'get'
    })
}

export function githubAuthCallback(code) {
    return request({
        url: '/api/user/user/github/callback',
        method: 'get',
        params: { code }
    })
}