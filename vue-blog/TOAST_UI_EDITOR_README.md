# Toast UI Editor 集成说明

## 概述

本项目已成功集成 Toast UI Editor，实现了类似图片中效果的新建文章和编辑文章功能。

## 功能特性

### 1. 编辑器功能
- **Markdown 编辑**: 支持 Markdown 语法编辑
- **实时预览**: 左右分屏显示编辑器和预览
- **工具栏**: 完整的格式化工具栏
- **图片上传**: 支持图片拖拽上传
- **代码高亮**: 支持代码块语法高亮

### 2. 文章管理
- **新建文章**: 完整的文章创建流程
- **编辑文章**: 支持文章内容编辑
- **分类管理**: 文章分类选择
- **标签管理**: 多标签选择
- **SEO设置**: 自定义URL、Meta描述、关键词

### 3. 界面设计
- **响应式布局**: 适配不同屏幕尺寸
- **暗色主题**: 支持暗色模式
- **面包屑导航**: 清晰的页面导航
- **表单验证**: 完整的表单验证机制

## 文件结构

```
src/
├── components/admin/
│   └── ToastUIEditor.vue          # Toast UI Editor 组件
├── views/admin/
│   ├── ArticleCreate.vue          # 新建文章页面
│   ├── ArticleEdit.vue            # 编辑文章页面
│   └── ArticleManagement.vue      # 文章管理页面
└── views/
    └── TestEditor.vue             # 测试页面
```

## 使用方法

### 1. 访问编辑器

#### 新建文章
- 访问 `/admin/articles/new` 或点击"新建文章"按钮
- 填写文章标题、选择分类
- 使用 Toast UI Editor 编辑内容
- 设置标签、封面图片等
- 保存草稿或发布文章

#### 编辑文章
- 访问 `/admin/articles/:id/edit` 或点击"编辑"按钮
- 修改文章内容
- 更新文章设置
- 保存更改

#### 测试页面
- 访问 `/test-editor` 查看编辑器效果
- 测试各种编辑功能

### 2. 编辑器配置

Toast UI Editor 已配置以下功能：

```javascript
const editor = new Editor({
  el: editorRef.value,
  height: '600px',
  initialEditType: 'markdown',
  previewStyle: 'vertical',
  initialValue: form.content,
  usageStatistics: false,
  toolbarItems: [
    ['heading', 'bold', 'italic', 'strike'],
    ['hr', 'quote'],
    ['ul', 'ol', 'task', 'indent', 'outdent'],
    ['table', 'image', 'link'],
    ['code', 'codeblock'],
    ['scrollSync']
  ]
})
```

### 3. 数据格式

文章数据结构：

```javascript
{
  title: '文章标题',
  summary: '文章摘要',
  content: 'Markdown内容',
  status: '0', // 0: 已发布, 2: 草稿
  categoryId: '分类ID',
  tagIds: ['标签ID数组'],
  coverImage: '封面图片URL',
  isTop: false,
  slug: '自定义URL',
  metaDescription: 'SEO描述',
  keywords: '关键词'
}
```

## 技术实现

### 1. 依赖安装

项目已安装以下依赖：

```json
{
  "@toast-ui/editor": "^3.2.2"
}
```

### 2. 样式导入

```javascript
import '@toast-ui/editor/dist/toastui-editor.css'
```

### 3. 组件集成

Toast UI Editor 已完全集成到 Vue 3 组件中，支持：

- 响应式数据绑定
- 生命周期管理
- 事件处理
- 样式定制

## 自定义配置

### 1. 工具栏配置

可以自定义工具栏按钮：

```javascript
toolbarItems: [
  ['heading', 'bold', 'italic', 'strike'],
  ['hr', 'quote'],
  ['ul', 'ol', 'task', 'indent', 'outdent'],
  ['table', 'image', 'link'],
  ['code', 'codeblock'],
  ['scrollSync']
]
```

### 2. 样式定制

支持自定义样式：

```css
:deep(.toast-ui-editor .toastui-editor-defaultUI-toolbar) {
  background-color: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}
```

### 3. 图片上传

已配置图片上传功能：

```javascript
hooks: {
  addImageBlobHook: (blob, callback) => {
    // 处理图片上传
    handleImageUpload(blob, callback)
  }
}
```

## 注意事项

1. **编辑器初始化**: 确保在 DOM 元素挂载后再初始化编辑器
2. **内存管理**: 组件销毁时需要调用 `editor.destroy()` 清理资源
3. **数据同步**: 编辑器内容变化时会自动同步到表单数据
4. **图片上传**: 需要配置正确的上传接口和认证信息

## 故障排除

### 1. 编辑器不显示
- 检查 DOM 元素是否正确挂载
- 确认 CSS 文件是否正确导入
- 检查控制台是否有错误信息

### 2. 图片上传失败
- 检查 API 接口是否正确配置
- 确认认证信息是否有效
- 检查文件大小和格式限制

### 3. 样式问题
- 检查 CSS 导入顺序
- 确认样式覆盖是否正确
- 检查暗色主题适配

## 更新日志

- **v1.0.0**: 初始版本，集成 Toast UI Editor
- 支持 Markdown 编辑和实时预览
- 完整的文章管理功能
- 响应式界面设计
- 暗色主题支持

## 相关链接

- [Toast UI Editor 官方文档](https://ui.toast.com/tui-editor)
- [Vue 3 官方文档](https://vuejs.org/)
- [Element Plus 组件库](https://element-plus.org/)
