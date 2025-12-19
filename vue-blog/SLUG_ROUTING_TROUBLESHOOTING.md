# Slug 路由問題故障排除指南

## 問題描述

在生產環境中，使用 slug 訪問文章頁面（如 `/article/deploy-personal-website-guide`）可能無法正常工作，顯示"加載文章失敗"錯誤。

## 快速檢查清單

### 1. 檢查服務器配置（最重要）

**問題**：服務器沒有正確配置 SPA 路由支持，直接訪問 `/article/deploy-personal-website-guide` 時返回 404。

**解決方案**：確保 Nginx 配置包含以下內容：

```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

**檢查方法**：
1. 檢查 Nginx 配置文件
2. 重啟 Nginx：`sudo nginx -s reload`
3. 測試直接訪問文章 URL

### 2. 檢查 API 基礎 URL 配置

**問題**：生產環境的 API 基礎 URL 配置錯誤，導致 API 請求失敗。

**解決方案**：
1. 創建 `.env.production` 文件
2. 如果 API 與前端同域，設置為空字符串：
   ```env
   VITE_API_BASE_URL=
   ```
3. 如果 API 在不同域名，設置完整的 API 地址：
   ```env
   VITE_API_BASE_URL=https://api.yourdomain.com
   ```
4. 重新構建：`npm run build`

**檢查方法**：
1. 打開瀏覽器開發者工具
2. 查看網絡請求，檢查實際請求的 URL
3. 檢查控制台是否有 API 連接錯誤

### 3. 檢查 API 請求是否成功

**問題**：API 請求失敗，可能是 CORS、網絡連接或後端服務器問題。

**檢查方法**：
1. 打開瀏覽器開發者工具 → 網絡面板
2. 訪問文章頁面
3. 查找請求 `/user/articles/slug/deploy-personal-website-guide`
4. 檢查請求狀態碼：
   - `200`：請求成功
   - `404`：文章不存在或 API 端點錯誤
   - `500`：後端服務器錯誤
   - `CORS error`：跨域問題
   - `Network error`：無法連接到服務器

### 4. 檢查路由參數解析

**問題**：路由參數沒有正確解析為 slug。

**檢查方法**：
1. 打開瀏覽器控制台
2. 查看是否有日誌輸出（開發環境）
3. 檢查 `routeParam.value` 是否正確
4. 檢查 `isSlug.value` 是否為 `true`

### 5. 檢查後端 API 端點

**問題**：後端 API 端點 `/user/articles/slug/{slug}` 可能不存在或配置錯誤。

**檢查方法**：
1. 使用 curl 或 Postman 測試 API：
   ```bash
   curl http://your-api-domain/user/articles/slug/deploy-personal-website-guide
   ```
2. 檢查後端服務器日誌
3. 驗證 API 端點是否正確處理 slug 參數

## 常見錯誤和解決方案

### 錯誤 1：直接訪問 URL 返回 404

**症狀**：在瀏覽器中直接輸入 `/article/deploy-personal-website-guide`，返回 404 頁面。

**原因**：服務器沒有正確配置 SPA 路由支持。

**解決方案**：
1. 檢查 Nginx 配置
2. 確保有 `try_files $uri $uri/ /index.html;`
3. 重啟 Nginx

### 錯誤 2：顯示"無法連接到服務器"

**症狀**：頁面顯示"加載文章失敗"或"無法連接到服務器"。

**原因**：API 基礎 URL 配置錯誤或後端服務器未運行。

**解決方案**：
1. 檢查 `.env.production` 文件
2. 檢查後端服務器是否運行
3. 檢查網絡連接
4. 檢查 API 基礎 URL 是否正確

### 錯誤 3：API 請求返回 404

**症狀**：網絡請求顯示 404 錯誤。

**原因**：API 端點不存在或 slug 不正確。

**解決方案**：
1. 檢查後端 API 是否支持 slug 查詢
2. 驗證 slug 是否正確
3. 檢查 API 路由配置

### 錯誤 4：CORS 錯誤

**症狀**：瀏覽器控制台顯示 CORS 錯誤。

**原因**：後端服務器沒有正確配置 CORS。

**解決方案**：
1. 檢查後端 CORS 配置
2. 確保允許前端域名
3. 檢查請求頭配置

## 調試步驟

### 步驟 1：檢查瀏覽器控制台

1. 打開瀏覽器開發者工具（F12）
2. 查看控制台是否有錯誤信息
3. 查看網絡請求是否成功
4. 檢查錯誤詳情

### 步驟 2：檢查網絡請求

1. 打開網絡面板
2. 過濾 XHR/Fetch 請求
3. 查找 `/user/articles/slug/` 請求
4. 檢查請求詳情：
   - 請求 URL
   - 請求方法
   - 狀態碼
   - 響應內容

### 步驟 3：檢查路由參數

在瀏覽器控制台中執行：

```javascript
// 檢查當前路由
console.log(window.location.pathname)
console.log(window.location.href)

// 檢查 Vue Router 路由
// 需要在 Vue 組件中或使用 Vue DevTools
```

### 步驟 4：測試 API 端點

使用 curl 測試 API：

```bash
# 測試 slug API
curl -X GET "http://your-api-domain/user/articles/slug/deploy-personal-website-guide"

# 測試 ID API（如果知道文章 ID）
curl -X GET "http://your-api-domain/user/articles/1"
```

### 步驟 5：檢查服務器日誌

1. 檢查 Nginx 訪問日誌：`sudo tail -f /var/log/nginx/access.log`
2. 檢查 Nginx 錯誤日誌：`sudo tail -f /var/log/nginx/error.log`
3. 檢查後端服務器日誌

## 驗證修復

修復後，驗證以下內容：

1. ✅ 直接訪問 `/article/deploy-personal-website-guide` 可以正常顯示文章
2. ✅ 從文章列表點擊文章可以正常跳轉
3. ✅ 瀏覽器刷新頁面不會返回 404
4. ✅ API 請求成功返回文章數據
5. ✅ 沒有控制台錯誤
6. ✅ 網絡請求狀態碼為 200

## 相關文件

- `PRODUCTION_DEPLOYMENT.md` - 生產環境部署配置指南
- `src/views/ArticleDetail.vue` - 文章詳情頁面
- `src/api/article.js` - 文章 API
- `src/utils/request.js` - API 請求配置
- `src/router/index.js` - 路由配置

## 獲取幫助

如果問題仍然存在，請提供以下信息：

1. 瀏覽器控制台錯誤信息
2. 網絡請求詳情（URL、狀態碼、響應）
3. 服務器配置（Nginx 配置片段）
4. 環境變量配置（隱藏敏感信息）
5. 後端服務器日誌（相關錯誤）



