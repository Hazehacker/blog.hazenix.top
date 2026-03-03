# Vue3 个人博客前端项目

基于 Vue 3 + Element Plus + Tailwind CSS + Pinia 构建的现代化个人博客前端系统。

## 🚀 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Element Plus** - Vue 3 组件库
- **Tailwind CSS** - 实用优先的 CSS 框架
- **Pinia** - Vue 状态管理库
- **Vue Router** - Vue 官方路由管理器
- **Axios** - HTTP 客户端
- **Markdown-it** - Markdown 解析器
- **Highlight.js** - 代码高亮

## 📁 项目结构

```
src/
├── api/                    # API接口定义
│   ├── article.js         # 文章相关API
│   ├── auth.js            # 认证相关API
│   ├── comment.js         # 评论相关API
│   ├── category.js        # 分类相关API
│   └── tag.js             # 标签相关API
├── components/             # 公共组件
│   ├── layout/            # 布局组件
│   ├── article/           # 文章组件
│   └── common/            # 通用组件
├── stores/                 # Pinia状态管理
│   ├── user.js            # 用户状态
│   └── theme.js           # 主题状态
├── views/                  # 页面视图
├── router/                 # 路由配置
├── utils/                  # 工具函数
└── assets/                 # 静态资源
```

## 🛠️ 开发环境

### 环境要求

- Node.js >= 20.19.0 或 >= 22.12.0
- npm 或 yarn

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

项目将在 http://localhost:5173 启动

### 构建生产版本

```bash
npm run build
```

## 🎨 功能特性

### ✅ 已实现功能

- 🏠 **首页展示** - 个人介绍和最新文章
- 📝 **文章管理** - 文章列表、详情、搜索
- 💬 **评论系统** - 文章评论功能
- 🏷️ **分类标签** - 文章分类和标签管理
- 🔐 **用户认证** - 登录、注册功能
- 🌙 **主题切换** - 明暗主题切换
- 📱 **响应式设计** - 移动端适配
- 🔍 **搜索功能** - 文章搜索
- 📄 **Markdown支持** - Markdown渲染和代码高亮

### 🔄 待完善功能

- 📊 文章统计
- 📸 图片上传
- 🔔 消息通知
- 📈 访问统计

## 🎯 页面路由

- `/` - 首页
- `/articles` - 文章列表
- `/article/:id` - 文章详情
- `/categories` - 分类列表
- `/tags` - 标签列表
- `/login` - 登录页
- `/register` - 注册页

## 🔧 配置说明

### 环境变量

创建 `.env.local` 文件配置环境变量：

```env
VITE_API_BASE_URL=http://localhost:9090
```

### API接口

项目使用 Axios 进行 HTTP 请求，所有 API 接口定义在 `src/api/` 目录下。

### 主题配置

主题配置在 `src/stores/theme.js` 中，支持明暗主题切换。

## 📦 主要依赖

```json
{
  "vue": "^3.5.22",
  "vue-router": "^4.5.1",
  "element-plus": "latest",
  "tailwindcss": "latest",
  "pinia": "latest",
  "axios": "latest",
  "markdown-it": "latest",
  "highlight.js": "latest",
  "js-cookie": "latest",
  "dayjs": "latest"
}
```

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 👨‍💻 作者

**Hazenix**

- GitHub: [@Hazenix](https://github.com/HazeHacker)

## 🙏 致谢

感谢以下开源项目：

- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [Tailwind CSS](https://tailwindcss.com/)
- [Pinia](https://pinia.vuejs.org/)

---

**Nothing but enthusiasm brightens up the endless years.**  
道阻且长，行则将至
