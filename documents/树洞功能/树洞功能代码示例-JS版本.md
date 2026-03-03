# 树洞功能代码示例 - JavaScript 版本

本文档提供树洞功能的完整 JavaScript 代码示例，适配您的接口文档。

---

## 1. API 接口文件

**文件路径**: `src/api/treeHole.js`

```javascript
import http from '@/utils/request.js'

/**
 * 获取树洞弹幕列表
 * @returns {Promise} 返回树洞列表数据
 */
export function getTreeHoleList() {
  return http({
    url: '/user/tree/list',
    method: 'get'
  })
}

/**
 * 发送弹幕
 * @param {Object} data - 弹幕数据
 * @param {number} data.userId - 用户ID
 * @param {string} data.username - 用户名
 * @param {string} data.content - 弹幕内容（最多64字符）
 * @returns {Promise} 返回操作结果
 */
export function addTreeHole(data) {
  return http({
    url: '/user/tree',
    method: 'post',
    data: data
  })
}
```

---

## 2. 完整组件代码（纯 JavaScript 版本）

**文件路径**: `src/views/TreeHole/index.vue`

```vue
<template>
  <div class="container">
    <!-- 输入区域 -->
    <div class="content_container">
      <div class="title">树洞</div>
      <div class="input_wrapper">
        <input 
          v-model="content" 
          @focus="isShowSubmit = true" 
          @blur="handleInputBlur"
          type="text" 
          placeholder="在这里留下自己的足迹吧..."
          maxlength="64"
          :disabled="loading"
          @keyup.enter="addTreeHoleBtn"
        >
        <button 
          v-show="isShowSubmit" 
          @click="addTreeHoleBtn"
          :disabled="loading"
        >
          <span class="submit-text">提交</span>
        </button>
      </div>
      <!-- 字符计数提示 -->
      <div class="char-count" v-if="content.length > 0">
        {{ content.length }}/64
      </div>
    </div>

    <!-- 弹幕组件 -->
    <vue-danmaku 
      :debounce="3000"
      random-channel
      :speeds="80"
      :channels="5"
      is-suspend
      v-model:danmus="treeHoleList"
      use-slot
      loop
      style="height:100vh; width:100vw;"
    >
      <!-- 自定义弹幕样式 -->
      <template v-slot:dm="{ danmu }">
        <div class="barrage_container">
          <div class="avatar_wrapper">
            <el-avatar :src="danmu.avatar" :size="40">
              <template #default>
                {{ danmu.nickname ? danmu.nickname.charAt(0) : '匿' }}
              </template>
            </el-avatar>
          </div>
          <div class="content_wrapper">
            <span class="nickname">{{ danmu.nickname }}：</span>
            <span class="content">{{ danmu.content }}</span>
          </div>
        </div>
      </template>
    </vue-danmaku>

    <!-- 加载遮罩 -->
    <div v-if="loading" class="loading_mask">
      <el-icon class="is-loading">
        <Loading />
      </el-icon>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import vueDanmaku from 'vue3-danmaku'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getTreeHoleList, addTreeHole } from '@/api/treeHole.js'
import { GET_TOKEN } from '@/utils/auth.js'

// 如果使用 Pinia store
// import { useUserStore } from '@/stores/user.js'

// 响应式数据
const treeHoleList = ref([])        // 树洞列表数据
const isShowSubmit = ref(false)     // 控制提交按钮显示
const content = ref('')             // 输入框内容
const loading = ref(false)          // 加载状态

// 用户信息（根据需要调整获取方式）
// const userStore = useUserStore()

/**
 * 获取用户信息
 * 方式1：从 localStorage 获取
 * 方式2：从 Pinia store 获取
 * 方式3：调用用户信息接口
 */
function getUserInfo() {
  // 方式1：从 localStorage 获取
  try {
    const userInfoStr = localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo')
    if (userInfoStr) {
      const userInfo = JSON.parse(userInfoStr)
      return {
        id: userInfo.id || userInfo.userId,
        username: userInfo.username || userInfo.nickname
      }
    }
  } catch (error) {
    console.error('解析用户信息失败:', error)
  }

  // 方式2：从 Pinia store 获取（如果使用）
  // if (userStore.userInfo) {
  //   return {
  //     id: userStore.userInfo.id || userStore.userInfo.userId,
  //     username: userStore.userInfo.username || userStore.userInfo.nickname
  //   }
  // }

  return null
}

/**
 * 获取默认头像
 */
function getDefaultAvatar(username) {
  // 可以根据用户名生成头像
  if (username) {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(username)}&background=random&size=40`
  }
  // 或使用项目中的默认头像
  return '/src/assets/img/avatar.jpg'
}

/**
 * 获取树洞列表
 */
async function getTreeHole() {
  try {
    loading.value = true
    const res = await getTreeHoleList()
    
    if (res.code === 200) {
      // 转换数据格式
      treeHoleList.value = (res.data || []).map(item => {
        const username = item.username || '匿名用户'
        return {
          id: item.id || item.user_id,
          nickname: username,
          avatar: item.avatar || getDefaultAvatar(username),
          content: item.content,
          createTime: item.create_time
        }
      })
    } else {
      ElMessage.error(res.msg || '获取数据失败')
    }
  } catch (error) {
    console.error('获取树洞列表失败:', error)
    ElMessage.error('获取数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 处理输入框失焦
 */
function handleInputBlur() {
  // 延迟隐藏，避免点击提交按钮时按钮消失
  setTimeout(() => {
    if (content.value === '') {
      isShowSubmit.value = false
    }
  }, 200)
}

/**
 * 提交树洞内容
 */
async function addTreeHoleBtn() {
  // 验证输入
  if (!content.value || content.value.trim() === '') {
    ElMessage.warning('请输入内容')
    return
  }

  // 验证长度（数据库限制64字符）
  if (content.value.trim().length > 64) {
    ElMessage.warning('内容不能超过64个字符')
    return
  }

  // 检查是否登录
  const token = GET_TOKEN()
  if (!token) {
    ElMessage.warning('请先登录后再发表弹幕')
    // 可以跳转到登录页
    // router.push('/login?redirect=/tree-hole')
    return
  }

  // 获取用户信息
  const userInfo = getUserInfo()
  if (!userInfo || !userInfo.id) {
    ElMessage.error('无法获取用户信息，请重新登录')
    return
  }

  try {
    loading.value = true
    const res = await addTreeHole({
      userId: userInfo.id,
      username: userInfo.username || '匿名用户',
      content: content.value.trim()
    })

    if (res.code === 200) {
      ElMessage.success(res.msg || '发表成功')
      content.value = ''           // 清空输入
      isShowSubmit.value = false   // 隐藏提交按钮
      await getTreeHole()          // 刷新列表
    } else {
      ElMessage.error(res.msg || '发表失败')
    }
  } catch (error) {
    console.error('发表弹幕失败:', error)
    
    // 处理特定错误
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
      } else if (status === 403) {
        ElMessage.error('没有权限发表弹幕')
      } else {
        ElMessage.error(error.response.data?.msg || '发表失败，请稍后重试')
      }
    } else {
      ElMessage.error('发表失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getTreeHole()
})
</script>

<style scoped lang="scss">
.container {
  position: relative;
  background-image: url('你的背景图片URL');  // 替换为实际背景图
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  min-width: 100vw;
  height: 100vh;
  overflow: hidden;

  // 输入区域样式
  .content_container {
    position: absolute;
    top: 40%;
    left: 50%;
    z-index: 2;
    transform: translate(-50%, -50%);
    text-align: center;

    .title {
      color: white;
      font-size: 2rem;
      font-weight: bold;
      text-shadow: 0 0 10px rgba(0, 0, 0, 0.8);
      text-align: center;
      text-transform: uppercase;
      letter-spacing: 0.2rem;
      margin-bottom: 1rem;
    }

    .input_wrapper {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 0.5rem;

      input {
        width: 16rem;
        height: 2rem;
        border: #409EFF solid 1px;
        border-radius: 1rem;
        outline: none;
        padding: 0 1rem;
        font-size: 1rem;
        background-color: rgba(255, 255, 255, 0.2);
        color: white;
        transition: all 0.3s ease;

        &:focus {
          background-color: rgba(255, 255, 255, 0.3);
          border-color: #66b1ff;
        }

        &::placeholder {
          color: rgba(255, 255, 255, 0.7);
          font-style: italic;
        }

        &:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }
      }

      button {
        width: 5rem;
        height: 2rem;
        border-radius: 1rem;
        outline: none;
        background-color: rgba(255, 255, 255, 0.2);
        border: #409EFF solid 1px;
        color: white;
        font-size: 1rem;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover:not(:disabled) {
          background-color: rgba(255, 255, 255, 0.5);
          border-color: #66b1ff;
        }

        &:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }

        .submit-text {
          color: white;
          font-style: italic;
          font-weight: bold;
        }
      }
    }

    .char-count {
      margin-top: 0.5rem;
      color: rgba(255, 255, 255, 0.8);
      font-size: 0.875rem;
    }
  }

  // 弹幕项样式
  .barrage_container {
    display: flex;
    align-items: center;
    position: relative;
    padding: 0.5rem;
    border-radius: 0.5rem;
    background-color: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;

    &::after {
      content: '';
      position: absolute;
      left: 0;
      bottom: 0;
      width: 0;
      height: 0.2em;
      border-radius: 0.1em;
      background: linear-gradient(to right, #00c6ff, #0072ff);
      transition: width 0.3s ease;
    }

    &:hover {
      background-color: rgba(255, 255, 255, 0.25);

      &::after {
        width: 100%;
      }
    }

    .avatar_wrapper {
      flex-shrink: 0;
    }

    .content_wrapper {
      margin-left: 0.5rem;
      display: flex;
      align-items: center;
      flex-wrap: wrap;

      .nickname {
        color: white;
        font-weight: bold;
        margin-right: 0.25rem;
      }

      .content {
        color: white;
        font-size: 1rem;
      }
    }
  }

  // 加载遮罩
  .loading_mask {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.3);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;

    .el-icon {
      font-size: 2rem;
      color: white;
    }
  }
}
</style>
```

---

## 3. 路由配置示例

**文件路径**: `src/router/index.js`

```javascript
import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/views/Layout/index.vue'  // 根据实际路径调整

const routes = [
  {
    path: '/',
    component: Layout,
    children: [
      // ... 其他路由
      
      // 树洞路由
      {
        path: '/tree-hole',
        name: 'treeHole',
        component: () => import('@/views/TreeHole/index.vue'),
        meta: {
          title: '心灵树洞',
          requiresAuth: false  // 查看不需要登录，但发表需要
        }
      },
      
      // ... 其他路由
    ]
  },
  // ... 其他路由配置
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫（可选）
router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '博客'
  next()
})

export default router
```

---

## 4. 使用 Pinia Store 获取用户信息的完整示例

如果您的项目使用 Pinia，可以这样实现：

**文件路径**: `src/stores/user.js`（示例）

```javascript
import { defineStore } from 'pinia'
import { GET_TOKEN } from '@/utils/auth.js'
import { getUserInfo } from '@/api/user.js'  // 根据实际路径调整

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    token: GET_TOKEN()
  }),

  actions: {
    async getInfo() {
      if (!this.token) {
        return null
      }
      
      try {
        const res = await getUserInfo()
        if (res.code === 200) {
          this.userInfo = res.data
          return res.data
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        return null
      }
    }
  }
})
```

在组件中使用：

```javascript
import { useUserStore } from '@/stores/user.js'

const userStore = useUserStore()

// 在 onMounted 中
onMounted(async () => {
  // 确保用户信息已加载
  if (!userStore.userInfo && userStore.token) {
    await userStore.getInfo()
  }
  getTreeHole()
})

// 在提交时
const userId = userStore.userInfo?.id || userStore.userInfo?.userId
const username = userStore.userInfo?.username || userStore.userInfo?.nickname
```

---

## 5. 注意事项

### 5.1 接口路径

根据您的接口文档：
- 获取列表：`GET /user/tree/list`
- 发送弹幕：`POST /user/tree`

### 5.2 请求体格式

发送弹幕时的请求体：
```javascript
{
  userId: 7,           // 用户ID
  username: "评论者昵称",
  content: "评论内容"   // 最多64字符
}
```

### 5.3 响应格式

后端返回格式：
```javascript
{
  code: 200,
  msg: "评论发表成功",  // 或 null
  data: [...]          // 数据数组
}
```

### 5.4 字符长度限制

- 数据库字段限制：`varchar(64)`
- 前端需要做验证和提示
- `maxlength="64"` 属性限制输入长度

### 5.5 认证处理

- 获取列表不需要认证
- 发送弹幕需要认证，需检查 token 和用户信息

---

## 6. 错误处理建议

```javascript
// 在 API 调用中添加错误处理
try {
  const res = await addTreeHole(data)
  // 处理成功
} catch (error) {
  // 处理错误
  if (error.response) {
    // 服务器返回错误
    switch (error.response.status) {
      case 401:
        ElMessage.error('登录已过期')
        // 跳转到登录页
        break
      case 403:
        ElMessage.error('没有权限')
        break
      case 500:
        ElMessage.error('服务器错误')
        break
      default:
        ElMessage.error(error.response.data?.msg || '操作失败')
    }
  } else {
    // 网络错误
    ElMessage.error('网络错误，请检查网络连接')
  }
}
```

---

## 7. 完整实施 Checklist

- [ ] 确认项目已安装 `vue3-danmaku`
- [ ] 创建 `src/api/treeHole.js`
- [ ] 创建 `src/views/TreeHole/index.vue`
- [ ] 在路由中添加树洞路由
- [ ] 确认用户信息获取方式
- [ ] 测试获取列表功能
- [ ] 测试发表功能（需要登录）
- [ ] 测试未登录时的提示
- [ ] 测试字符长度限制
- [ ] 添加背景图片
- [ ] 调整样式适配
- [ ] 测试移动端显示

---

## 8. 常见问题

### Q: 弹幕不显示？
检查数据格式是否正确，确保 `treeHoleList` 包含 `id`, `nickname`, `avatar`, `content` 字段。

### Q: 提交时提示未登录？
检查 `GET_TOKEN()` 是否返回有效 token，检查请求头是否包含认证信息。

### Q: 用户信息获取失败？
确认用户信息存储位置（localStorage、Pinia store 等），检查字段名称是否匹配。

### Q: 样式不生效？
确认项目支持 SCSS，如果只支持 CSS，将 `<style scoped lang="scss">` 改为 `<style scoped>`。

---

完成以上步骤，您的树洞功能就可以正常运行了！

