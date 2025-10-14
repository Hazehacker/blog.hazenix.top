import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/',
    component: () => import('@/components/layout/AppLayout.vue'),
    children: [
      { path: '', name: 'index', component: () => import('@/views/index.vue') },
      { path: 'home', name: 'home', component: () => import('@/views/Home.vue') },
      { path: 'articles', name: 'ArticleList', component: () => import('@/views/ArticleList.vue') },
      { path: 'article/:id', name: 'ArticleDetail', component: () => import('@/views/ArticleDetail.vue') },
      { path: 'categories', name: 'CategoryList', component: () => import('@/views/CategoryList.vue') },
      { path: 'tags', name: 'TagList', component: () => import('@/views/TagList.vue') },
    ]
  },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue') },
  { path: '/index', name: 'Index', component: () => import('@/views/index.vue') },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/NotFound.vue') },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = getToken()
  // 可以根据需要添加需要登录的页面判断
  next()
})

export default router
