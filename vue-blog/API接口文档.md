# Vue Blog 管理端后端接口文档

## 基础信息

- **基础URL**: `http://your-domain.com/api`
- **管理端接口前缀**: `/admin`
- **认证方式**: Bearer Token (JWT)
- **请求头**: `Authorization: Bearer <token>`
- **响应格式**: JSON

## 通用响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "total": 100,
    "page": 1,
    "pageSize": 20,
    "totalPages": 5
  }
}
```

---

## 1. 仪表盘接口

### 1.1 获取统计数据
- **URL**: `GET /admin/stats`
- **描述**: 获取仪表盘统计数据
- **请求参数**: 无
- **响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalArticles": 150,
    "totalCategories": 12,
    "totalTags": 45,
    "totalComments": 320
  }
}
```

### 1.2 获取最新文章
- **URL**: `GET /admin/articles/recent`
- **描述**: 获取最新的文章列表
- **请求参数**:
  - `limit` (int, 可选): 限制数量，默认5
- **响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "文章标题",
      "status": "published",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

### 1.3 获取最新评论
- **URL**: `GET /admin/comments/recent`
- **描述**: 获取最新的评论列表
- **请求参数**:
  - `limit` (int, 可选): 限制数量，默认5
- **响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "author": "评论者",
      "content": "评论内容",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

---

## 2. 文章管理接口

### 2.1 获取文章列表
- **URL**: `GET /admin/articles`
- **描述**: 获取文章列表，支持分页和筛选
- **请求参数**:
  - `page` (int): 页码，默认1
  - `pageSize` (int): 每页数量，默认20
  - `keyword` (string, 可选): 搜索关键词
  - `categoryId` (int, 可选): 分类ID
  - `status` (string, 可选): 状态 (published/draft)
- **响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "title": "文章标题",
        "summary": "文章摘要",
        "content": "文章内容",
        "status": "published",
        "categoryId": 1,
        "category": {
          "id": 1,
          "name": "分类名称"
        },
        "tags": [
          {
            "id": 1,
            "name": "标签名称"
          }
        ],
        "coverImage": "封面图片URL",
        "isTop": false,
        "allowComment": true,
        "viewCount": 100,
        "createdAt": "2024-01-01T00:00:00Z",
        "updatedAt": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

### 2.2 获取文章详情
- **URL**: `GET /admin/articles/{id}`
- **描述**: 获取指定文章的详细信息
- **路径参数**:
  - `id` (int): 文章ID
- **响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "文章标题",
    "summary": "文章摘要",
    "content": "文章内容",
    "status": "published",
    "categoryId": 1,
    "tagIds": [1, 2, 3],
    "coverImage": "封面图片URL",
    "isTop": false,
    "allowComment": true,
    "slug": "article-slug",
    "metaDescription": "SEO描述",
    "keywords": "关键词1,关键词2",
    "viewCount": 100,
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  }
}
```

### 2.3 创建文章
- **URL**: `POST /admin/articles`
- **描述**: 创建新文章
- **请求体**:
```json
{
  "title": "文章标题",
  "summary": "文章摘要",
  "content": "文章内容",
  "status": "draft",
  "categoryId": 1,
  "tagIds": [1, 2, 3],
  "coverImage": "封面图片URL",
  "isTop": false,
  "allowComment": true,
  "slug": "article-slug",
  "metaDescription": "SEO描述",
  "keywords": "关键词1,关键词2"
}
```

### 2.4 更新文章
- **URL**: `PUT /admin/articles/{id}`
- **描述**: 更新指定文章
- **路径参数**:
  - `id` (int): 文章ID
- **请求体**: 同创建文章

### 2.5 删除文章
- **URL**: `DELETE /admin/articles/{id}`
- **描述**: 删除指定文章
- **路径参数**:
  - `id` (int): 文章ID

### 2.6 批量删除文章
- **URL**: `DELETE /admin/articles/batch`
- **描述**: 批量删除文章
- **请求体**:
```json
{
  "ids": [1, 2, 3]
}
```

### 2.7 切换文章状态
- **URL**: `PATCH /admin/articles/{id}/status`
- **描述**: 切换文章发布状态
- **路径参数**:
  - `id` (int): 文章ID
- **请求体**:
```json
{
  "status": "published"
}
```

---

## 3. 分类管理接口

### 3.1 获取分类列表
- **URL**: `GET /admin/categories`
- **描述**: 获取分类列表，支持分页和筛选
- **请求参数**:
  - `page` (int): 页码，默认1
  - `pageSize` (int): 每页数量，默认20
  - `keyword` (string, 可选): 搜索关键词
  - `status` (string, 可选): 状态 (active/inactive)
- **响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "分类名称",
        "description": "分类描述",
        "color": "#3B82F6",
        "sort": 0,
        "status": "active",
        "articleCount": 10,
        "createdAt": "2024-01-01T00:00:00Z",
        "updatedAt": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 20,
    "page": 1,
    "pageSize": 20
  }
}
```

### 3.2 获取分类详情
- **URL**: `GET /admin/categories/{id}`
- **描述**: 获取指定分类的详细信息
- **路径参数**:
  - `id` (int): 分类ID

### 3.3 创建分类
- **URL**: `POST /admin/categories`
- **描述**: 创建新分类
- **请求体**:
```json
{
  "name": "分类名称",
  "description": "分类描述",
  "color": "#3B82F6",
  "sort": 0,
  "status": "active"
}
```

### 3.4 更新分类
- **URL**: `PUT /admin/categories/{id}`
- **描述**: 更新指定分类
- **路径参数**:
  - `id` (int): 分类ID
- **请求体**: 同创建分类

### 3.5 删除分类
- **URL**: `DELETE /admin/categories/{id}`
- **描述**: 删除指定分类
- **路径参数**:
  - `id` (int): 分类ID

### 3.6 批量删除分类
- **URL**: `DELETE /admin/categories/batch`
- **描述**: 批量删除分类
- **请求体**:
```json
{
  "ids": [1, 2, 3]
}
```

---

## 4. 标签管理接口

### 4.1 获取标签列表
- **URL**: `GET /admin/tags`
- **描述**: 获取标签列表，支持分页和筛选
- **请求参数**:
  - `page` (int): 页码，默认1
  - `pageSize` (int): 每页数量，默认20
  - `keyword` (string, 可选): 搜索关键词
  - `status` (string, 可选): 状态 (active/inactive)
- **响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "标签名称",
        "description": "标签描述",
        "color": "#3B82F6",
        "sort": 0,
        "status": "active",
        "articleCount": 5,
        "createdAt": "2024-01-01T00:00:00Z",
        "updatedAt": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 50,
    "page": 1,
    "pageSize": 20
  }
}
```

### 4.2 获取标签详情
- **URL**: `GET /admin/tags/{id}`
- **描述**: 获取指定标签的详细信息
- **路径参数**:
  - `id` (int): 标签ID

### 4.3 创建标签
- **URL**: `POST /admin/tags`
- **描述**: 创建新标签
- **请求体**:
```json
{
  "name": "标签名称",
  "description": "标签描述",
  "color": "#3B82F6",
  "sort": 0,
  "status": "active"
}
```

### 4.4 更新标签
- **URL**: `PUT /admin/tags/{id}`
- **描述**: 更新指定标签
- **路径参数**:
  - `id` (int): 标签ID
- **请求体**: 同创建标签

### 4.5 删除标签
- **URL**: `DELETE /admin/tags/{id}`
- **描述**: 删除指定标签
- **路径参数**:
  - `id` (int): 标签ID

### 4.6 批量删除标签
- **URL**: `DELETE /admin/tags/batch`
- **描述**: 批量删除标签
- **请求体**:
```json
{
  "ids": [1, 2, 3]
}
```

---

## 5. 评论管理接口

### 5.1 获取评论列表
- **URL**: `GET /admin/comments`
- **描述**: 获取评论列表，支持分页和筛选
- **请求参数**:
  - `page` (int): 页码，默认1
  - `pageSize` (int): 每页数量，默认20
  - `keyword` (string, 可选): 搜索关键词
  - `status` (string, 可选): 状态 (pending/approved/rejected)
  - `articleId` (int, 可选): 文章ID
- **响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "author": "评论者",
        "email": "user@example.com",
        "content": "评论内容",
        "status": "pending",
        "ip": "192.168.1.1",
        "avatar": "头像URL",
        "article": {
          "id": 1,
          "title": "文章标题"
        },
        "replyTo": {
          "id": 2,
          "author": "被回复者",
          "avatar": "头像URL"
        },
        "createdAt": "2024-01-01T00:00:00Z",
        "updatedAt": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

### 5.2 获取评论详情
- **URL**: `GET /admin/comments/{id}`
- **描述**: 获取指定评论的详细信息
- **路径参数**:
  - `id` (int): 评论ID

### 5.3 更新评论状态
- **URL**: `PATCH /admin/comments/{id}/status`
- **描述**: 更新评论审核状态
- **路径参数**:
  - `id` (int): 评论ID
- **请求体**:
```json
{
  "status": "approved"
}
```

### 5.4 删除评论
- **URL**: `DELETE /admin/comments/{id}`
- **描述**: 删除指定评论
- **路径参数**:
  - `id` (int): 评论ID

### 5.5 批量删除评论
- **URL**: `DELETE /admin/comments/batch`
- **描述**: 批量删除评论
- **请求体**:
```json
{
  "ids": [1, 2, 3]
}
```

### 5.6 批量审核通过
- **URL**: `PATCH /admin/comments/batch/approve`
- **描述**: 批量审核通过评论
- **请求体**:
```json
{
  "ids": [1, 2, 3]
}
```

### 5.7 批量审核拒绝
- **URL**: `PATCH /admin/comments/batch/reject`
- **描述**: 批量审核拒绝评论
- **请求体**:
```json
{
  "ids": [1, 2, 3]
}
```

---

## 6. 更新记录管理接口

### 6.1 获取更新记录列表
- **URL**: `GET /admin/updates`
- **描述**: 获取更新记录列表，支持分页和筛选
- **请求参数**:
  - `page` (int): 页码，默认1
  - `pageSize` (int): 每页数量，默认20
  - `keyword` (string, 可选): 搜索关键词
  - `type` (string, 可选): 类型 (feature/bugfix/optimization/security/other)
- **响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "version": "v1.0.0",
        "title": "更新标题",
        "type": "feature",
        "content": "更新内容详情",
        "releaseDate": "2024-01-01",
        "isImportant": false,
        "link": "相关链接",
        "createdAt": "2024-01-01T00:00:00Z",
        "updatedAt": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 20,
    "page": 1,
    "pageSize": 20
  }
}
```

### 6.2 获取更新记录详情
- **URL**: `GET /admin/updates/{id}`
- **描述**: 获取指定更新记录的详细信息
- **路径参数**:
  - `id` (int): 更新记录ID

### 6.3 创建更新记录
- **URL**: `POST /admin/updates`
- **描述**: 创建新的更新记录
- **请求体**:
```json
{
  "version": "v1.0.0",
  "title": "更新标题",
  "type": "feature",
  "content": "更新内容详情",
  "releaseDate": "2024-01-01",
  "isImportant": false,
  "link": "相关链接"
}
```

### 6.4 更新更新记录
- **URL**: `PUT /admin/updates/{id}`
- **描述**: 更新指定更新记录
- **路径参数**:
  - `id` (int): 更新记录ID
- **请求体**: 同创建更新记录

### 6.5 删除更新记录
- **URL**: `DELETE /admin/updates/{id}`
- **描述**: 删除指定更新记录
- **路径参数**:
  - `id` (int): 更新记录ID

### 6.6 批量删除更新记录
- **URL**: `DELETE /admin/updates/batch`
- **描述**: 批量删除更新记录
- **请求体**:
```json
{
  "ids": [1, 2, 3]
}
```

---

## 7. 文件上传接口

### 7.1 上传图片
- **URL**: `POST /admin/upload/image`
- **描述**: 上传图片文件
- **请求类型**: `multipart/form-data`
- **请求参数**:
  - `file` (file): 图片文件
- **响应示例**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "https://example.com/uploads/image.jpg",
    "filename": "image.jpg",
    "size": 1024000
  }
}
```

### 7.2 上传文件
- **URL**: `POST /admin/upload/file`
- **描述**: 上传其他类型文件
- **请求类型**: `multipart/form-data`
- **请求参数**:
  - `file` (file): 文件
- **响应示例**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "https://example.com/uploads/file.pdf",
    "filename": "file.pdf",
    "size": 2048000
  }
}
```

---

## 8. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/Token无效 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 422 | 数据验证失败 |
| 500 | 服务器内部错误 |

---

## 9. 数据验证规则

### 文章验证
- `title`: 必填，1-100字符
- `content`: 必填，1-10000字符
- `summary`: 可选，最大200字符
- `status`: 必填，枚举值 (draft/published)
- `categoryId`: 可选，整数
- `tagIds`: 可选，整数数组
- `slug`: 可选，最大100字符，唯一
- `metaDescription`: 可选，最大160字符
- `keywords`: 可选，最大200字符

### 分类验证
- `name`: 必填，1-50字符，唯一
- `description`: 可选，最大200字符
- `color`: 必填，颜色值格式
- `sort`: 必填，0-9999整数
- `status`: 必填，枚举值 (active/inactive)

### 标签验证
- `name`: 必填，1-20字符，唯一
- `description`: 可选，最大100字符
- `color`: 必填，颜色值格式
- `sort`: 必填，0-9999整数
- `status`: 必填，枚举值 (active/inactive)

### 更新记录验证
- `version`: 必填，1-20字符，唯一
- `title`: 必填，1-100字符
- `type`: 必填，枚举值 (feature/bugfix/optimization/security/other)
- `content`: 必填，1-1000字符
- `releaseDate`: 必填，日期格式
- `link`: 可选，最大200字符

---

## 10. 注意事项

1. **认证**: 所有管理端接口都需要Bearer Token认证
2. **权限**: 需要管理员权限才能访问
3. **分页**: 所有列表接口都支持分页，默认每页20条
4. **搜索**: 支持关键词搜索，通常搜索标题和内容
5. **排序**: 列表默认按创建时间倒序排列
6. **软删除**: 建议使用软删除，保留数据完整性
7. **文件上传**: 限制文件大小和类型，建议图片最大2MB
8. **缓存**: 统计数据可以考虑缓存，提高性能
9. **日志**: 重要操作建议记录操作日志
10. **备份**: 删除操作前建议数据备份

---

## 11. 开发建议

1. **数据库设计**: 建议使用外键约束保证数据完整性
2. **索引优化**: 为常用查询字段添加索引
3. **API版本**: 建议使用版本控制，如 `/api/v1/admin`
4. **限流**: 对上传接口进行限流保护
5. **监控**: 添加接口调用监控和错误日志
6. **测试**: 编写完整的单元测试和集成测试
7. **文档**: 使用Swagger等工具生成API文档
8. **部署**: 使用Docker容器化部署
