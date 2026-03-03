# OAuth 登录重定向地址配置说明

## 📋 当前配置概览

### 重定向地址

**GitHub 登录重定向地址：**
```
${window.location.origin}/home?source=github
```
- GitHub 允许在 redirect_uri 中包含查询参数
- 使用 URL 参数 `source=github` 来标识登录来源

**Google 登录重定向地址：**
```
${window.location.origin}/home
```
- ⚠️ **重要**：Google 不允许在 redirect_uri 中包含查询参数，否则会报 `redirect_uri_mismatch` 错误
- 使用 `sessionStorage` 来记录登录来源，在回调时从 `sessionStorage` 读取

### 环境适配情况

| 环境 | `window.location.origin` | GitHub 重定向地址 | Google 重定向地址 |
|------|-------------------------|------------------|------------------|
| 开发环境 | `http://localhost:5173` (Vite 默认端口) | `http://localhost:5173/home?source=github` | `http://localhost:5173/home` |
| 生产环境 | 生产域名 (如 `https://yourdomain.com`) | `https://yourdomain.com/home?source=google` | `https://yourdomain.com/home` |

## 🔄 工作流程

### 1. 前端处理流程 (`src/components/common/LoginDialog.vue`)

```javascript
// GitHub 登录
1. 使用 sessionStorage 记录 oauth_source = 'github'（备用方案）
2. 调用后端 API: GET /user/user/github/url
3. 获取后端返回的授权 URL
4. 解析 URL，检查是否有 redirect_uri 参数
5. 如果有 redirect_uri：
   - 解码现有的 redirect_uri
   - 添加 source=github 参数
   - 重新编码并设置回 URL
6. 如果没有 redirect_uri：
   - 添加 redirect_uri=${window.location.origin}/home?source=github
7. 跳转到 GitHub 授权页面

// Google 登录
1. 使用 sessionStorage 记录 oauth_source = 'google'（必需，因为 redirect_uri 不能包含查询参数）
2. 调用后端 API: GET /user/user/google/url
3. 获取后端返回的授权 URL
4. 解析 URL，检查是否有 redirect_uri 参数
5. 如果有 redirect_uri：
   - 解码现有的 redirect_uri
   - 移除所有查询参数和 hash，只保留路径
   - 设置为 ${window.location.origin}/home（不包含任何查询参数）
6. 如果没有 redirect_uri：
   - 添加 redirect_uri=${window.location.origin}/home（不包含查询参数）
7. 跳转到 Google 授权页面
```

### 2. 回调处理流程 (`src/views/Home.vue`)

```javascript
// GitHub 回调
1. GitHub 授权后重定向到: /home?code=xxx&source=github
2. Home.vue 的 handleOAuthCallback 函数处理回调
3. 从 URL 参数中读取 source=github
4. 调用 GitHub 登录 API
5. 登录成功后清除 URL 中的 code 和 source 参数
6. 清除 sessionStorage 中的 oauth_source
7. 保持在 /home 页面

// Google 回调
1. Google 授权后重定向到: /home?code=xxx（不包含 source 参数）
2. Home.vue 的 handleOAuthCallback 函数处理回调
3. URL 中没有 source 参数，从 sessionStorage 读取 oauth_source=google
4. 调用 Google 登录 API
5. 登录成功后清除 URL 中的 code 参数
6. 清除 sessionStorage 中的 oauth_source
7. 保持在 /home 页面
```

## ✅ 环境适配性分析

### 优点

1. **自动适配环境**：使用 `window.location.origin` 可以自动适配开发和生产环境
2. **无需手动配置**：前端代码无需根据环境变量修改重定向地址
3. **统一回调处理**：所有 OAuth 回调都统一在 `/home` 页面处理

### 潜在问题

1. **后端配置不匹配**：
   - 如果后端返回的授权 URL 中已经包含硬编码的 `redirect_uri`（比如生产环境地址）
   - 前端修改后的 `redirect_uri` 可能和后端配置不一致
   - 可能导致 OAuth 提供商（GitHub/Google）验证失败

2. **OAuth 应用配置**：
   - 需要在 GitHub OAuth App 和 Google OAuth Client 中配置允许的重定向地址
   - 必须同时配置开发环境和生产环境的重定向地址
   - ⚠️ **Google 特别重要**：redirect_uri 必须完全匹配，不能包含查询参数

3. **Google redirect_uri_mismatch 错误**：
   - Google OAuth 对 redirect_uri 的验证非常严格
   - redirect_uri 必须与 Google Cloud Console 中配置的完全一致
   - 不能包含查询参数（如 `?source=google`）
   - 解决方案：使用 `sessionStorage` 记录登录来源，而不是在 redirect_uri 中传递

## 🔧 建议的优化方案

### 方案 1：使用环境变量（推荐）

**前端配置：**

创建 `.env.development` 和 `.env.production` 文件：

```env
# .env.development
VITE_APP_URL=http://localhost:5173

# .env.production
VITE_APP_URL=https://yourdomain.com
```

**修改 `LoginDialog.vue`：**

```javascript
// 使用环境变量，如果不存在则使用 window.location.origin
const baseUrl = import.meta.env.VITE_APP_URL || window.location.origin
url.searchParams.set('redirect_uri', `${baseUrl}/home?source=github`)
```

### 方案 2：后端动态返回（最佳实践）

**后端配置：**

后端应该根据请求的 `Origin` 或 `Referer` 头动态生成 `redirect_uri`：

```java
// 后端代码示例（Java）
String origin = request.getHeader("Origin");
if (origin == null) {
    origin = request.getHeader("Referer");
}
String redirectUri = origin + "/home?source=github";
```

这样可以确保：
- 后端返回的 `redirect_uri` 与前端实际运行的地址一致
- 不需要在前端修改 `redirect_uri`
- 更安全，避免重定向地址被篡改

### 方案 3：保持现状但完善配置

如果保持当前实现，需要确保：

1. **GitHub OAuth App 配置**：
   - 开发环境：`http://localhost:5173/home?source=github`
   - 生产环境：`https://yourdomain.com/home?source=google`

2. **Google OAuth Client 配置**：
   - ⚠️ **重要**：Google 的 redirect_uri 不能包含查询参数
   - 开发环境：`http://localhost:5173/home`（不包含查询参数）
   - 生产环境：`https://yourdomain.com/home`（不包含查询参数）
   - 登录来源通过 `sessionStorage` 传递，而不是 URL 参数

3. **后端配置**：
   - 后端返回的授权 URL 中不应该包含 `redirect_uri`，让前端动态添加
   - 或者后端返回的 `redirect_uri` 应该是通配符或动态生成的
   - ⚠️ **Google 特别要求**：如果后端返回了 redirect_uri，必须确保不包含查询参数
   - 前端会自动清理 redirect_uri 中的查询参数（对于 Google 登录）

## 📝 检查清单

- [ ] 检查后端返回的授权 URL 是否包含 `redirect_uri`
- [ ] 检查 GitHub OAuth App 中配置的重定向地址（可以包含查询参数）
- [ ] 检查 Google OAuth Client 中配置的重定向地址（**不能包含查询参数**）
- [ ] 测试开发环境的 GitHub OAuth 登录流程
- [ ] 测试开发环境的 Google OAuth 登录流程
- [ ] 测试生产环境的 GitHub OAuth 登录流程
- [ ] 测试生产环境的 Google OAuth 登录流程
- [ ] 验证 Google 的重定向地址是否与 OAuth 应用配置完全一致（不包含查询参数）
- [ ] 验证 sessionStorage 机制是否正常工作

## 🔍 调试方法

### 1. 查看实际的重定向地址

在浏览器开发者工具中：
1. 打开 Network 标签
2. 点击 GitHub/Google 登录按钮
3. 查看跳转的 URL，检查 `redirect_uri` 参数

### 2. 查看后端返回的授权 URL

在 `LoginDialog.vue` 中添加日志：

```javascript
console.log('后端返回的授权 URL:', authUrl)
console.log('修改后的授权 URL:', authUrl)
```

### 3. 检查 OAuth 应用配置

- GitHub: https://github.com/settings/developers
- Google: https://console.cloud.google.com/apis/credentials

## 🚀 实施建议

1. **短期**：检查并确保 OAuth 应用配置中包含开发和生产环境的重定向地址
2. **中期**：考虑使用环境变量方案，提高配置的灵活性
3. **长期**：优化后端，使其能够动态生成重定向地址

## 📚 相关文件

- `src/components/common/LoginDialog.vue` - OAuth 登录处理
- `src/views/Home.vue` - OAuth 回调处理
- `src/api/auth.js` - OAuth API 接口
- `src/utils/request.js` - 请求配置

---

**最后更新**：2025-01-XX  
**维护者**：前端开发团队

