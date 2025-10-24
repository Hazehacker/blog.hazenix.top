# Vue Blog 用户端后端接口文档

## 基础信息

- **基础URL**: `http://your-domain.com/`
- **认证方式**: Bearer Token (JWT) - 部分接口需要认证
- **请求头**: `Authorization: Bearer <token>` (需要认证的接口)
- **响应格式**: JSON
- **内容类型**: `application/json`

## 通用响应格式

### 成功响应
```json
{
  "code": 200,
  "msg": "success",
  "data": {},
}
```

### 分页响应格式
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [],
    "total": 100,
  },

}
```

### 错误响应
```json
{
  "code": 400,
  "msg": "请求参数错误",
  "data": null,
}
```

---

## 1. 认证相关接口

### 1.1 用户登录
- **URL**: `POST /auth/login`
- **描述**: 用户登录获取访问令牌
- **认证**: 不需要
- **请求体**:
```json
{
  "username": "用户名或邮箱",
  "password": "密码"
}
```
- **响应示例**:
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "hazenix",
      "email": "hazenix@example.com",
      "avatar": "头像URL",
      "gender":1,
      "status":0,
      "role":2,

    },

  }
}
```
* 备注：
    * gender约定：0保密/未知 1男 2女
    * status约定：0正常 1锁定（默认设为0）
    * role约定：0管理员 2用户（默认设为2）
### 1.2 用户注册(暂时，后期采用OAuth2.0)
- **URL**: `POST /auth/register`
- **描述**: 用户注册新账号
- **认证**: 不需要
- **请求体**:
```json
{
  "username": "用户名",
  "password": "密码",
  "email": "邮箱",
}
```
- **响应示例**:
```json
{
  "code": 200,
  "msg": "注册成功",
  "data": {
    "id": 1,
    "username": "hazenix",
    "email": "hazenix@example.com",
  }
}
```

### 1.3 获取用户信息
- **URL**: `GET /auth/userinfo`
- **描述**: 获取当前登录用户信息
- **认证**: 需要
- **请求参数**: 无
- **响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "hazenix",
    "email": "hazenix@example.com",
    "avatar": "头像URL",
    "gender":1,
    "status":0,
    "role":2,
  }
}
```

---

## 2. 文章相关接口
* 备注
    * status:0正常  2草稿
    * isTop:0不置顶 1置顶

### 2.1 获取文章列表
- **URL**: `GET /user/articles`
- **描述**: 获取文章列表，支持筛选(用户端文章列表页面的文章展示暂时不用分页)
- **认证**: 不需要
- **请求参数**:
  - `keyword` (string, 可选): 搜索关键词
  - `categoryId` (int, 可选): 分类ID
  - `tagId` (int, 可选): 标签ID
  - `status` (string, 可选): 状态 (0/2)，默认0
- **响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {

    "id": 1,
    "title": "文章标题",
    //"summary": "文章摘要",(暂时没加这个字段，后期可能会加上)
    "content": "文章内容",
    "categoryId": 1,
    "categoryName": "分类名称",
    "tags": [
        {
        "id": 1,
        "name": "标签名称"
        }
    ],
    "coverImage": "封面图片URL",//注意不一定每篇文章都有封面，大部分应该不加封面
    "slug": "article-slug",
    "status": 0,
    "isTop": 0,
    "viewCount": 100,
    "likeCount": 50,
    "favoriteCount":30,
    "commentCount": 20,
    "createTime": "2024-01-01T00:00:00Z",
    "updateTime": "2024-01-01T00:00:00Z"
    }


}

```

### 2.2 获取文章详情（根据id）
- **URL**: `GET /user/articles/{id}`
- **描述**: 获取指定文章的详细信息
- **认证**: 不需要
- **路径参数**:
  - `id` (int): 文章ID
- **响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "title": "文章标题",
    //"summary": "文章摘要",(暂时没加这个字段，后期可能会加上)
    "content": "文章内容",
    "categoryId": 1,
    "categoryName": "分类名称",
    "tags": [
        {
        "id": 1,
        "name": "标签名称"
        }
    ],
    "coverImage": "封面图片URL",//注意不一定每篇文章都有封面，大部分应该不加封面
    "slug": "article-slug",
    "status": 0,
    "isTop": 0,
    "viewCount": 100,
    "likeCount": 50,
    "favoriteCount":30,
    "commentCount": 20,
    "createTime": "2024-01-01T00:00:00Z",
    "updateTime": "2024-01-01T00:00:00Z"
  }
}
```

### 2.3 根据slug获取文章
- **URL**: `GET /user/articles/slug/{slug}`
- **描述**: 根据文章slug获取文章详情
- **认证**: 不需要
- **路径参数**:
  - `slug` (string): 文章slug
- **响应示例**: 同2.2

### 2.4 搜索文章
- **URL**: `GET /user/articles`
- **描述**: 搜索文章
- **认证**: 不需要
- **请求参数**:
  - `title` (string, 可选): 搜索关键词
  - `categoryId` (int, 可选): 分类ID
  - `tagId` (int, 可选): 标签ID
- **响应示例**: 同2.1

### 2.5 点赞文章
- **URL**: `POST /user/articles/{id}/like`
- **描述**: 点赞或取消点赞文章
- **认证**: 需要
- **路径参数**:
  - `id` (int): 文章ID
- **响应示例**:
```json
{
  "code": 200,
  "msg": "点赞成功",
  "data": {
    "isLiked": 1,
    "likeCount": 51
  }
}
```

### 2.6 收藏文章
- **URL**: `POST /user/articles/{id}/favorite`
- **描述**: 收藏或取消收藏文章
- **认证**: 需要
- **路径参数**:
  - `id` (int): 文章ID
- **响应示例**:
```json
{
  "code": 200,
  "msg": "收藏成功",
  "data": null
}
```

### 2.7 获取相关文章
- **URL**: `GET /user/articles/{id}/related`
- **描述**: 获取与指定文章相关的文章列表
- **认证**: 不需要
- **路径参数**:
  - `id` (int): 文章ID
- **请求参数**:
  - `limit` (int, 可选): 限制数量，默认5
- **响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 2,
      "title": "相关文章标题",
      "summary": "相关文章摘要",
      "author": "作者",
      "createTime": "2024-01-01T00:00:00Z",
      "viewCount": 80,
      "tags": [
        {
          "id": 1,
          "name": "标签名称"
        }
      ]
    }
  ]
}
```

### 2.8 获取热门文章
- **URL**: `GET /user/articles/popular`
- **描述**: 获取热门文章列表
- **认证**: 不需要
- **请求参数**:
  - `limit` (int, 可选): 限制数量，默认10
  - `timeRange` (string, 可选): 时间范围 (week/month/year/all)，默认week
- **响应示例**: 同2.1







### 2.12 增加文章浏览量
- **URL**: `PUT /user/articles/{id}/view`
- **描述**: 增加文章浏览量
- **认证**: 不需要
- **路径参数**:
  - `id` (int): 文章ID
- **响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```



---

## 3. 分类相关接口

### 3.1 获取分类列表
- **URL**: `GET /user/categories`
- **描述**: 获取所有分类列表
- **认证**: 不需要
- **请求参数**: 无
- **响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "name": "前端开发",
      "description": "前端技术相关文章",
      "color": "#3B82F6",
      "articleCount": 15,
      "createTime": "2024-01-01T00:00:00Z"
    },
    {
      "id": 2,
      "name": "后端开发",
      "description": "后端技术相关文章",
      "color": "#10B981",
      "articleCount": 12,
      "createTime": "2024-01-01T00:00:00Z"
    }
  ]
}
```

### 3.2 获取分类详情
- **URL**: `GET /user/categories/{id}`
- **描述**: 获取指定分类的详细信息
- **认证**: 不需要
- **路径参数**:
  - `id` (int): 分类ID
- **响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "name": "前端开发",
    "description": "前端技术相关文章",
    "color": "#3B82F6",
    "articleCount": 15,
    "createTime": "2024-01-01T00:00:00Z",
    "updateTime": "2024-01-01T00:00:00Z"
  }
}
```

### 3.3 获取分类下的文章列表
- **URL**: `GET /user/categories/{id}/articles`
- **描述**: 获取指定分类下的文章列表
- **认证**: 不需要
- **路径参数**:
  - `id` (int): 分类ID
- **请求参数**:
  - `page` (int, 可选): 页码，默认1
  - `pageSize` (int, 可选): 每页数量，默认10
- **响应示例**: 同2.1

---

## 4. 标签相关接口

### 4.1 获取标签列表
- **URL**: `GET /user/tags`
- **描述**: 获取所有标签列表
- **认证**: 不需要
- **请求参数**: 无
- **响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "name": "Vue3",
      "articleCount": 8,
      "createTime": "2024-01-01T00:00:00Z"
    },
    {
      "id": 2,
      "name": "JavaScript",
      "articleCount": 15,
      "createTime": "2024-01-01T00:00:00Z"
    }
  ]
}
```

### 4.2 获取标签详情
- **URL**: `GET /user/tags/{id}`
- **描述**: 获取指定标签的详细信息
- **认证**: 不需要
- **路径参数**:
  - `id` (int): 标签ID
- **响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "name": "Vue3",
    "articleCount": 8,
    "createTime": "2024-01-01T00:00:00Z",
    "updateTime": "2024-01-01T00:00:00Z"
  }
}
```

### 4.3 获取标签下的文章列表
- **URL**: `GET /user/tags/{id}/articles`
- **描述**: 获取指定标签下的文章列表
- **认证**: 不需要
- **路径参数**:
  - `id` (int): 标签ID
- **请求参数**:
  - `page` (int, 可选): 页码，默认1
  - `pageSize` (int, 可选): 每页数量，默认10
- **响应示例**: 同2.1

---

## 5. 评论相关接口

### 5.1 获取评论列表
- **URL**: `GET /user/comments`
- **描述**: 获取评论列表
- **认证**: 不需要
- **请求参数**:
  - `articleId` (int, 可选): 文章ID
  - `page` (int, 可选): 页码，默认1
  - `pageSize` (int, 可选): 每页数量，默认10
  - `status` (string, 可选): 状态 (approved/pending/rejected)，默认approved
  - `sortBy` (string, 可选): 排序字段 (createTime/likeCount)，默认createTime
  - `sortOrder` (string, 可选): 排序方向 (asc/desc)，默认desc
- **响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "content": "评论内容",
        "nickname": "评论者昵称",
        "email": "评论者邮箱",
        "avatar": "头像URL",
        "status": "approved",
        "likeCount": 5,
        "isLiked": false,
        "article": {
          "id": 1,
          "title": "文章标题"
        },
        "replyTo": {
          "id": 2,
          "nickname": "被回复者昵称"
        },
        "createTime": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 50,
    "current": 1,
    "size": 10,
    "pages": 5
  }
}
```

### 5.2 获取评论详情
- **URL**: `GET /user/comments/{id}`
- **描述**: 获取指定评论的详细信息
- **认证**: 不需要
- **路径参数**:
  - `id` (int): 评论ID
- **响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "content": "评论内容",
    "nickname": "评论者昵称",
    "email": "评论者邮箱",
    "avatar": "头像URL",
    "status": "approved",
    "likeCount": 5,
    "isLiked": false,
    "article": {
      "id": 1,
      "title": "文章标题"
    },
    "replyTo": {
      "id": 2,
      "nickname": "被回复者昵称"
    },
    "createTime": "2024-01-01T00:00:00Z"
  }
}
```

### 5.3 创建评论
- **URL**: `POST /user/comments`
- **描述**: 创建新评论
- **认证**: 不需要
- **请求体**:
```json
{
  "articleId": 1,
  "content": "评论内容",
  "nickname": "评论者昵称",
  "email": "评论者邮箱",
  "parentId": null,
  "replyTo": ""
}
```
- **响应示例**:
```json
{
  "code": 200,
  "msg": "评论发表成功",
  "data": {
    "id": 1,
    "content": "评论内容",
    "nickname": "评论者昵称",
    "email": "评论者邮箱",
    "status": "pending",
    "createTime": "2024-01-01T00:00:00Z"
  }
}
```

### 5.4 点赞或取消点赞评论
- **URL**: `POST /user/comments/{id}/like`
- **描述**: 点赞或取消点赞评论
- **认证**: 需要
- **路径参数**:
  - `id` (int): 评论ID
- **响应示例**:
```json
{
  "code": 200,
  "msg": "点赞成功",
  "data": {
    "isLiked": true,
    "likeCount": 6
  }
}
```

### 5.5 获取评论统计
- **URL**: `GET /user/comments/stats`
- **描述**: 获取评论统计数据
- **认证**: 不需要
- **请求参数**:
  - `articleId` (int, 可选): 文章ID，不传则获取全局统计
- **响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "totalComments": 150,
    "approvedComments": 140,
    "pendingComments": 10,
    "articleComments": 20
  }
}
```

### 5.6 获取最新评论
- **URL**: `GET /user/comments/latest`
- **描述**: 获取最新评论列表
- **认证**: 不需要
- **请求参数**:
  - `limit` (int, 可选): 限制数量，默认10
- **响应示例**: 同5.1







---

## 7. 用户相关接口



### 7.1 获取用户收藏的文章
- **URL**: `GET /user/collections`
- **描述**: 获取用户收藏的文章列表
- **认证**: 需要
- **请求参数**:
  - `page` (int, 可选): 页码，默认1
  - `pageSize` (int, 可选): 每页数量，默认10
- **响应示例**: 同2.1

### 7.2 获取用户点赞的文章
- **URL**: `GET /user/likes`
- **描述**: 获取用户点赞的文章列表
- **认证**: 需要
- **请求参数**:
  - `page` (int, 可选): 页码，默认1
  - `pageSize` (int, 可选): 每页数量，默认10
- **响应示例**: 同2.1

---



### 评论验证
- 登录过的用户才能发表评论






---

## 12. 接口调用示例

### JavaScript/Axios 示例

```javascript
// 获取文章列表
const getArticles = async (params = {}) => {
  try {
    const response = await axios.get('/user/articles', { params })
    return response.data
  } catch (error) {
    console.error('获取文章列表失败:', error)
    throw error
  }
}

// 创建评论
const createComment = async (commentData) => {
  try {
    const response = await axios.post('/user/comments', commentData)
    return response.data
  } catch (error) {
    console.error('创建评论失败:', error)
    throw error
  }
}

// 上传文件
const uploadFile = async (file) => {
  try {
    const formData = new FormData()
    formData.append('file', file)
    const response = await axios.post('/user/common/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${token}`
      }
    })
    return response.data
  } catch (error) {
    console.error('文件上传失败:', error)
    throw error
  }
}
```

---


