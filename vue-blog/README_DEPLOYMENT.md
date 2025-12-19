# 部署配置說明

## 環境變量配置

### 開發環境

開發環境使用 `http://localhost:9090` 作為 API 基礎 URL，直接連接到後端服務。

可選配置（`.env.local` 或 `.env.development`）：
```env
# 開發環境可選配置，默認為 http://localhost:9090
VITE_API_BASE_URL=http://localhost:9090
```

### 生產環境

**必須配置** `.env.production` 文件：
```env
# 生產環境 API 基礎 URL（通過 Nginx 代理）
VITE_API_BASE_URL=https://blog.hazenix.top/api
```

## 請求流程說明

### 開發環境
- 前端請求：`/user/articles`
- 實際請求：`http://localhost:9090/user/articles`
- 直接連接到後端服務

### 生產環境
- 前端請求：`/user/articles`
- 實際請求：`https://blog.hazenix.top/api/user/articles`
- Nginx 匹配 `location /api/`，轉發為：`http://blog:8080/user/articles`
- 所有 API 請求都通過 `/api` 前綴統一由 Nginx 代理

## Nginx 配置說明

當前 Nginx 配置：
```nginx
location /api/ {
    proxy_pass http://blog:8080/;  # 注意結尾的 /
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

**工作原理**：
- 請求 `https://blog.hazenix.top/api/user/articles`
- Nginx 匹配 `location /api/`
- 去掉 `/api` 前綴，轉發為 `http://blog:8080/user/articles`
- 後端接收到的請求路徑是 `/user/articles`

## API 路徑說明

前端代碼中的 API 路徑：
- `/user/*` - 用戶端接口（如 `/user/articles`、`/user/user/login`）
- `/api/*` - 管理端接口（如 `/api/articles`、`/api/comments`）
- `/admin/*` - 管理端接口（如 `/admin/stats`、`/admin/articles`）
- `/common/*` - 通用接口（如 `/common/upload`）

**生產環境請求示例**：
- `/user/articles` → `https://blog.hazenix.top/api/user/articles` → 後端：`/user/articles` ✓
- `/api/articles` → `https://blog.hazenix.top/api/api/articles` → 後端：`/api/articles` ✓
- `/admin/stats` → `https://blog.hazenix.top/api/admin/stats` → 後端：`/admin/stats` ✓
- `/common/upload` → `https://blog.hazenix.top/api/common/upload` → 後端：`/common/upload` ✓

## 構建和部署步驟

### 1. 配置環境變量

創建 `.env.production` 文件：
```bash
VITE_API_BASE_URL=https://blog.hazenix.top/api
```

### 2. 構建生產版本

```bash
npm run build
```

### 3. 部署靜態資源

將 `dist` 目錄內容部署到 Nginx 的 `/usr/share/nginx/html` 目錄。

### 4. 驗證配置

1. 檢查構建後的代碼中是否包含正確的 API 基礎 URL
2. 打開瀏覽器開發者工具，查看網絡請求
3. 確認所有 API 請求都通過 `/api` 前綴
4. 確認 Nginx 正確轉發請求到後端

## 注意事項

1. **環境變量必須在構建時設置**：Vite 會在構建時將環境變量注入到代碼中，構建後修改不會生效
2. **Nginx 配置正確**：確保 `proxy_pass` 後面有 `/`，這樣會正確去掉 `/api` 前綴
3. **後端服務**：確保後端服務運行在 `http://blog:8080`（或 Docker 容器名對應的地址）
4. **HTTPS 配置**：確保 SSL 證書配置正確

## 故障排除

### 問題 1：API 請求 404

**檢查**：
1. 確認 `.env.production` 中 `VITE_API_BASE_URL` 設置正確
2. 確認 Nginx 配置中 `proxy_pass` 後端地址正確
3. 確認後端服務正在運行

### 問題 2：CORS 錯誤

**解決**：確保 Nginx 配置了正確的代理頭：
```nginx
proxy_set_header Host $host;
proxy_set_header X-Real-IP $remote_addr;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
proxy_set_header X-Forwarded-Proto $scheme;
```

### 問題 3：請求路徑錯誤

**檢查**：
1. 確認前端 API 路徑以 `/` 開頭（如 `/user/articles`）
2. 確認 `VITE_API_BASE_URL` 不包含末尾斜杠
3. 確認 Nginx `proxy_pass` 配置正確



