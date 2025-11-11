# Google 前端實現授權登入（基於 Google Identity Services）

本方案在前端取得 Google id_token（JWT），再呼叫後端單一接口交換站內登入態，避免國內服務器與 Google 直接互動。

## 前端行為概要
- 透過 `https://accounts.google.com/gsi/client` 取得使用者的 `id_token`
- 將 `id_token` POST 給後端，後端驗簽並建立/綁定站內帳號
- 後端回傳站內 `token` 與使用者資訊

環境變數：
- `VITE_GOOGLE_CLIENT_ID`: Google OAuth 2.0 Client ID

## 後端接口設計

- 方法：POST  
- 路徑：`/user/user/google/idtoken-login`
- 說明：接收前端取得的 Google `id_token`，後端驗證簽名與聲明，建立/查找用戶並簽發站內 Token。

### 請求
Content-Type: `application/json`
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsInR..."
}
```

### 響應
成功（200）：
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "your_app_jwt_or_session_token",
    "id": 123,
    "userName": "Hazenix",
    "avatar": "https://.../avatar.png",
    "email": "user@example.com",
    "role": 2
  }
}
```

失敗：
```json
{
  "code": 400,
  "msg": "invalid id_token"
}
```

## 後端驗證與邏輯要點
1. 透過 Google 公鑰驗簽 id_token（建議使用官方 SDK 或緩存 `jwks` 的方式）
2. 校驗聲明：
   - `aud` 必須等於你的 `GOOGLE_CLIENT_ID`
   - `iss` 應為 `https://accounts.google.com` 或 `accounts.google.com`
   - `exp`、`nbf`、`iat` 時間有效
   - 建議校驗 `hd`（若只允許指定網域）
3. 取出 `sub`（Google 唯一使用者 ID）、`email`、`name`、`picture` 等信息
4. 用 `sub` 或 `email` 查找/建立站內用戶，並返回站內 `token` 與用戶資料

## 前端使用位置
- `src/components/common/LoginDialog.vue` 按下 Google 登入按鈕時，會：
  1) 以 `VITE_GOOGLE_CLIENT_ID` 取得 `id_token`
  2) 呼叫 `/user/user/google/idtoken-login`
  3) 儲存站內 `token` 並載入用戶資訊

## 與舊有重導向流程的關係
- 原本的 `GET /user/user/google/url`、`GET /user/user/google/callback` 可保留以兼容；本方案無需跳轉，僅需新增上述 `idtoken-login` 接口。


