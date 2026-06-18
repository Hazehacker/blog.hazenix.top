**`smtpPassword` 字段是干嘛的？**

它是邮件服务器（SMTP 服务器）的登录密码 / 授权码，**不是**你博客系统的登录密码。

工作流程：**博客后端要发邮件 → 必须先连接到一台 SMTP 服务器（比如 QQ 邮箱的 `smtp.qq.com`、163 的 `smtp.163.com`、Gmail 的 `smtp.gmail.com`） → SMTP 服务器要求用 `smtpUsername` + `smtpPassword` 登录验证 → 登录成功后才允许借用它发邮件**。

实际填法：

- **QQ 邮箱**：`smtpUsername` = 你的 QQ 邮箱地址，`smtpPassword` = QQ 邮箱设置里生成的「**SMTP 授权码**」（不是 QQ 密码）
- **163 邮箱**：同理，要在邮箱设置里开启 SMTP 并生成「客户端授权密码」
- **Gmail**：要生成 App Password

收件人 (`recipient`) 和发件人 (`smtpUsername`) 可以不一样，比如发件箱用 `xxx@qq.com`，发到 `you@gmail.com`。所以这字段是必需的。