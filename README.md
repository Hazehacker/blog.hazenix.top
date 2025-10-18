> 网站设计

# 网站类型

* 个人博客
  * 动态网站





#### 什么是静态网站

> 这是一个非常好的问题，我们来一步一步解释清楚。
>
> ---
>
> ### ✅ 一、什么是**静态网站（Static Website）**？
>
> #### 📌 定义：
>
> 静态网站是指：**所有网页内容在服务器上是预先写好、固定不变的 HTML、CSS、JavaScript 文件**，用户访问时，服务器直接把这些文件发给浏览器，**不需要在服务器端动态生成或查询数据库**。
>
> #### 🧱 核心特点：
>
> | 特点         | 说明                                                |
> | ------------ | --------------------------------------------------- |
> | 📄 内容固定   | 每个页面都是 `.html` 文件，内容不会因用户不同而变化 |
> | ⚙️ 无后端逻辑 | 不需要 PHP、Python、Node.js 等服务器端语言          |
> | 💾 无数据库   | 不依赖 MySQL、MongoDB 等存储数据                    |
> | 🚀 加载快     | 可以用 CDN 全球加速，性能高                         |
> | ✅ 易部署     | 可以托管在 GitHub Pages、Vercel、Netlify 等免费平台 |
>
> 📌 **常见例子**：
>
> - 个人博客（如用 Hugo、Jekyll 生成）
> - 企业官网（公司介绍、联系方式）
> - 项目展示页
> - 技术文档网站（如 Vue 官方文档）
>
> ---
>
> ### ❓ 二、有轮播图的网站是静态网站吗？
>
> ✅ **可以是！**
>
> > **轮播图本身是前端功能，由 JavaScript 实现，不涉及服务器处理。**
>
> #### 🔍 举个例子：
>
> 你用 HTML + CSS + JavaScript 写了一个轮播图，图片都放在 `images/` 文件夹里：
>
> ```html
> <div class="carousel">
>   <img src="images/banner1.jpg">
>   <img src="images/banner2.jpg">
>   <img src="images/banner3.jpg">
> </div>
> <script>
>   // 用 JS 控制图片自动切换
>   let index = 0;
>   setInterval(() => {
>     index = (index + 1) % 3;
>     // 切换图片
>   }, 3000);
> </script>
> ```
>
> 👉 这个网站依然是**静态网站**，因为：
>
> - 页面是预先写好的 `.html` 文件
> - 轮播是浏览器自己完成的（客户端 JavaScript）
> - 没有向服务器请求“生成”内容
>
> ---
>
> ### ❓ 三、能向后端发送请求的网站，还是静态网站吗？
>
> 这个问题要分情况看：
>
> ---
>
> #### ✅ 情况 1：**前端 JS 调用第三方 API（比如获取天气、评论）**
>
> 👉 **仍然可以算静态网站**，这叫 **“伪动态” 或 “Jamstack” 架构**。
>
> ##### 🔍 例子：
>
> 你在静态网站中加了这段代码：
>
> ```javascript
> fetch('https://api.weather.com/today')
>   .then(res => res.json())
>   .then(data => {
>     document.getElementById('weather').innerText = data.temp;
>   });
> ```
>
> 👉 虽然发了请求，但：
>
> - 网站本身还是 `.html` 文件
> - 请求是**浏览器发起的**，不是服务器
> - 服务器只是“发文件”，不参与逻辑
>
> 📌 这种模式非常流行，叫 **Jamstack**（JavaScript + APIs + Markup），比如：
>
> - 用 Disqus 加评论
> - 用 LeanCloud 存评论
> - 用 Stripe 支付
>
> ✅ **结论：仍然是静态网站**。
>
> ---
>
> #### ❌ 情况 2：**服务器端动态生成页面（如 PHP、Python）**
>
> 比如用户访问 `/news.php?id=123`，服务器才去数据库查新闻，生成 HTML 返回。
>
> 👉 这就是典型的**动态网站**，不是静态网站。
>
> ---
>
> #### ❌ 情况 3：**用户提交表单，数据存到服务器数据库**
>
> 比如：
>
> ```html
> <form action="/submit.php" method="post">
>   <input type="text" name="name">
>   <button type="submit">提交</button>
> </form>
> ```
>
> 👉 如果 `/submit.php` 是一个 PHP 脚本，把数据存进数据库，那就是**动态网站**。
>
> 但如果用第三方服务（如 Google Form、Airtable、Formspree），表单提交到外部，**你的服务器不处理**，那也可以算静态网站。
>
> ---
>
> ### ✅ 总结：判断一个网站是不是静态网站，看这几点：
>
> | 问题                                     | 是静态网站？                 |
> | ---------------------------------------- | ---------------------------- |
> | 页面是 `.html` 文件，内容固定            | ✅ 是                         |
> | 有轮播图、动画、JS 交互                  | ✅ 可以是                     |
> | 用 JavaScript 调用 API 显示数据          | ✅ 可以是（现代静态网站常见） |
> | 服务器端生成页面（如 PHP、JSP）          | ❌ 不是                       |
> | 依赖数据库动态生成内容                   | ❌ 不是                       |
> | 用户登录、评论、购物车（在服务器端处理） | ❌ 不是                       |
>
> ---
>
> ### 🎯 回到你的问题：
>
> > “有轮播图，能向后端发送请求的算静态网站吗？”
>
> ✅ **答案是：有可能是！**
>
> 只要：
>
> 1. 页面是预先生成的 HTML 文件（不是服务器实时生成）
> 2. 轮播图是前端 JS 实现
> 3. “向后端发送请求” 是浏览器通过 JavaScript 调用**外部 API**（比如获取评论、天气、搜索），而不是你的服务器在处理逻辑
>
> 👉 那它就是一个**现代静态网站**，非常常见且高效！
>
> ---
>
> 如果你告诉我你用的是什么技术（比如 WordPress？Vue？GitHub Pages？），我可以更具体地判断 😊



# 技术选型



* 方案1

  springboot+vue高度定制的博客

  

* **方案2**

  直接用现成的网站系统（框架），比如WordPress，
  已经编写好了，局限性比较大、有些地方不能自定义





一些手段

比文云官网建站

![image-20251011163753647](C:\Users\毕哲晖\AppData\Roaming\Typora\typora-user-images\image-20251011163753647.png)







# 数据库设计





#### user

用户表，用于存储用户信息。具体表结构如下：

| 字段名          | 数据类型     | 说明         | 备注                                                         |      |
| --------------- | ------------ | ------------ | ------------------------------------------------------------ | ---- |
| id              | bigint       | 主键         | 自增                                                         |      |
| nickname        | varchar(32)  | 用户名       | 本博客项目不设置唯一的username字段                           |      |
| password        | varchar(64)  | 密码         | 不要存明文密码! 存哈希值                                     |      |
| phone           | varchar(11)  | 手机号       |                                                              |      |
| email           | varchar(100) |              | 邮箱，可用于订阅文章，必须唯一                               |      |
| avatar          | varchar(500) | 头像图片路径 |                                                              | 可选 |
| gender          | int          | 性别         | 约定：0保密/未知 1男 2女                                     | 可选 |
| social_links    | varchar(200) | 用户联系方式 | 如果希望作者通过联系方式联系到自己，可以选择填写(50个字以内) | 可选 |
| status          | int          | 账号状态     | 1正常 0锁定                                                  | 必须 |
| last_login_time | datetime     | 最后登录时间 | 记录最后登录时间，用于安全审计                               |      |
| role            | int          | 角色         | 约定：0管理员  1作者 2用户（默认设为2）                      | 必须 |
| create_time     | datetime     | 创建时间     |                                                              |      |
| update_time     | datetime     | 最后修改时间 |                                                              |      |



#### article

文章信息表。具体表结构如下：

| 字段名         | 数据类型      | 说明         | 备注                                                     |      |
| -------------- | ------------- | ------------ | -------------------------------------------------------- | ---- |
| id             | bigint        | 主键         | 自增                                                     |      |
| user_id        | bigint        | 用户(作者)id |                                                          |      |
| title          |               | 标题         |                                                          | 必须 |
| content        | text          | 文章内容     |                                                          | 必须 |
| cover_image    | varchar(500 ) | 封面图片路径 |                                                          |      |
| tags_id        | bigint        | 文章分类id   |                                                          |      |
| tags_name      |               | 文章分类名   | 冗余字段                                                 |      |
| like_count     | bigint        | 点赞数       | 默认0                                                    |      |
| favorite_count | bigint        | 收藏数       | 默认0                                                    |      |
| view_count     | bigint        | 评论数       | 默认0                                                    |      |
| slug           |               | URL标识符    |                                                          |      |
| status         | int           | 状态         | 0正常 1待审核  2草稿<br />（需要管理员审核之后才能展示） |      |
| create_time    | datetime      | 创建时间     |                                                          |      |
| update_time    | datetime      | 最后修改时间 |                                                          |      |

> 博客作者很少，暂时不添加冗余字段提升性能，直接多表联查
>
> 图片(文章内图片和封面图片)存在OSS，不存数据库

> title



> #### 什么是slug
>
>
> ![image-20251011184918402](C:\Users\毕哲晖\AppData\Roaming\Typora\typora-user-images\image-20251011184918402.png)
>
> ![image-20251011184904213](C:\Users\毕哲晖\AppData\Roaming\Typora\typora-user-images\image-20251011184904213.png)
>
> ![image-20251011184954617](C:\Users\毕哲晖\AppData\Roaming\Typora\typora-user-images\image-20251011184954617.png)
>
> 

#### article_tags

| 字段名     | 数据类型 | 说明 | 备注     |
| ---------- | -------- | ---- | -------- |
| tags_id    |          |      |          |
| tags_name  |          |      | 冗余字段 |
| article_id |          |      |          |



#### tags

文章分类表。具体表结构如下：

| 字段名      | 数据类型 | 说明         | 备注 |
| ----------- | -------- | ------------ | ---- |
| id          | bigint   | 主键         | 自增 |
| name        |          |              |      |
| slug        |          | URL标识符    |      |
| create_time | datetime | 创建时间     |      |
| update_time | datetime | 最后修改时间 |      |





#### comments

存储评论信息。具体表结构如下：

| 字段名         | 数据类型 | 说明               | 备注                         |
| -------------- | -------- | ------------------ | ---------------------------- |
| id             | bigint   | 主键               | 自增                         |
| article_id     | bigint   | 文章id             | [关联]这个评论所属的文章     |
| user_id        | bigint   | 用户id             | [关联]发出这个评论的用户     |
| username       |          | 冗余字段           |                              |
| reply_id       |          | 该评论回复的评论id | 如果是第一级评论该值就为null |
| reply_username |          | 冗余字段           |                              |
| content        | text     | 评论内容           |                              |
| like_count     | bigint   | 点赞数             | 默认0                        |
| status         | int      | 评论状态           | 1正常  0不展示(软删除)       |
| create_time    | datetime | 创建时间           |                              |



#### (links)

友情链接

| 字段名      | 数据类型 | 说明     | 备注 |
| ----------- | -------- | -------- | ---- |
| id          |          |          |      |
| name        |          |          |      |
| url         |          |          |      |
| avatar      |          |          |      |
| sort_order  |          | 用于排序 |      |
| create_time |          |          |      |
|             |          |          |      |
|             |          |          |      |





# 功能设计

## 游客模式

### 文章相关

* 可以转发文章
* 







## 用户模式





### 注册/登录

* 手机号作为账号
  手机号+密码  手机号+验证码 登录
* 微信直接扫码登录
* 使用google或者github或gitee登录

> 参考其他网站的登录方式，比如leetcode/csdn

### 文章相关

* 可以转发文章

* 个性化推荐

* 文章开头提供  AI Summary

* 用户可以收藏文章、可以发表评论

  > 提供基础的小表情（参考qq）

* 









# 接口概览

> 注意使用redis缓存和jwt校验







































