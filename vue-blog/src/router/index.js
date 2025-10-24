import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const routes = [
  // 首页路由
  { path: '', name: 'Index', component: () => import('@/views/index.vue'), meta: { title: '首页' } },

  { path: '/', name: 'Index', component: () => import('@/views/index.vue'), meta: { title: '首页' } },

  {
    path: '/',
    component: () => import('@/components/layout/AppLayout.vue'),
    meta: {
      requiresAuth: true,
      title: '主页'
    },
    children: [
      { path: '/home', name: 'Home', component: () => import('@/views/Home.vue'), meta: { title: '主页' } },
      { path: '/articles', name: 'ArticleList', component: () => import('@/views/ArticleList.vue'), meta: { title: '文章列表' } },
      { path: '/article/:id', name: 'ArticleDetail', component: () => import('@/views/ArticleDetail.vue'), meta: { title: '文章详情' }, props: true },
      { path: '/categories', name: 'CategoryList', component: () => import('@/views/CategoryList.vue'), meta: { title: '分类列表' } },
      { path: '/category/:id', name: 'CategoryDetail', component: () => import('@/views/CategoryDetail.vue'), meta: { title: '分类详情' }, props: true },
      { path: '/tags', name: 'TagList', component: () => import('@/views/TagList.vue'), meta: { title: '标签列表' } },
      { path: '/tag/:id', name: 'TagDetail', component: () => import('@/views/TagDetail.vue'), meta: { title: '标签详情' }, props: true },
      { path: '/album', name: "Album", component: () => import("@/views/Album.vue"), meta: { title: '相册' }, props: true }



    ]
  },





  // 用户认证路由
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresGuest: true }
  },

  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', requiresGuest: true }
  },

  // 管理端路由
  {
    path: '/admin',
    component: () => import('@/components/layout/AdminLayout.vue'),
    meta: {
      requiresAuth: true,
      requiresAdmin: true,
      title: '管理后台'
    },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'articles',
        name: 'AdminArticles',
        component: () => import('@/views/admin/ArticleManagement.vue'),
        meta: { title: '文章管理' }
      },
      {
        path: 'articles/new',
        name: 'AdminArticleCreate',
        component: () => import('@/views/admin/ArticleCreate.vue'),
        meta: { title: '新建文章' }
      },
      {
        path: 'articles/:id',
        name: 'AdminArticleView',
        component: () => import('@/views/ArticleDetail.vue'),
        meta: { title: '查看文章' },
        props: true
      },
      {
        path: 'articles/:id/edit',
        name: 'AdminArticleEdit',
        component: () => import('@/views/admin/ArticleEdit.vue'),
        meta: { title: '编辑文章' },
        props: true
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/CategoryManagement.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'tags',
        name: 'AdminTags',
        component: () => import('@/views/admin/TagManagement.vue'),
        meta: { title: '标签管理' }
      },
      {
        path: 'comments',
        name: 'AdminComments',
        component: () => import('@/views/admin/CommentManagement.vue'),
        meta: { title: '评论管理' }
      },
      {
        path: 'updates',
        name: 'AdminUpdates',
        component: () => import('@/views/admin/UpdateManagement.vue'),
        meta: { title: '更新记录' }
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('@/views/admin/Settings.vue'),
        meta: { title: '系统设置' }
      }
    ]
  },

  // 用户个人中心路由
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: {
      requiresAuth: true,
      title: '个人中心'
    }
  },

  {
    path: '/profile/edit',
    name: 'ProfileEdit',
    component: () => import('@/views/ProfileEdit.vue'),
    meta: {
      requiresAuth: true,
      title: '编辑资料'
    }
  },

  // 搜索页面
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/Search.vue'),
    meta: { title: '搜索结果' }
  },

  // 关于页面
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/About.vue'),
    meta: { title: '关于我们' }
  },

  // 404页面
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' }
  },

  // 捕获所有未匹配的路由
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 临时禁用认证检查，直到登录功能完成
  // const userStore = useUserStore()
  // const token = getToken()

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - Vue Blog`
  }

  // 暂时跳过认证检查
  // 检查是否需要认证
  // if (to.meta.requiresAuth) {
  //   if (!token) {
  //     ElMessage.warning('请先登录')
  //     next({
  //       path: '/login',
  //       query: { redirect: to.fullPath }
  //     })
  //     return
  //   }

  //   // 检查用户信息是否存在
  //   if (!userStore.userInfo) {
  //     try {
  //       await userStore.getUserInfo()
  //     } catch (error) {
  //       console.error('获取用户信息失败:', error)
  //       ElMessage.error('获取用户信息失败，请重新登录')
  //       next('/login')
  //       return
  //     }
  //   }

  //   // 检查是否需要管理员权限
  //   if (to.meta.requiresAdmin && !userStore.userInfo?.isAdmin) {
  //     ElMessage.error('权限不足')
  //     next('/')
  //     return
  //   }
  // }

  next()
})

// 路由错误处理
router.onError((error) => {
  console.error('路由错误:', error)
  ElMessage.error('页面加载失败')
})

export default router
