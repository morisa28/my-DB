# Codex 后续开发 Prompt：将 SpringBoot3 + Vue3 小型商城升级为可小规模实用项目

你现在是一名资深全栈工程师、DevOps 工程师和代码审查专家。请在当前仓库中继续开发一个 **SpringBoot3 + Vue3 + MySQL 小型商城项目**，目标不是停留在演示级，而是升级为 **可小规模投入实用的小型实战商城项目**。

请始终使用简体中文回复。

---

## 当前进度更新

截至 2026-05-19，第一阶段、第二阶段和第三阶段已完成：

- 文档收口和远程推送已完成。
- Docker Compose 环境已通过真实启动验证，前端、后端、MySQL 和 Nginx `/api` 代理可用。
- 原“模拟支付”已升级为线下付款备注、管理员确认收款、管理员发货、用户确认收货、待支付订单取消并恢复库存的实用级订单流程。

后续继续开发时，请从 **第四阶段：商品图片上传** 开始推进；若发现第三阶段相关回归，再按缺陷修复处理。

---

## 一、当前项目环境

当前项目路径：

```text
/home/just_monika/win_share/study/DB-sql/.worktrees/mall-platform
```

Windows 对应路径：

```text
F:\file\wsl_shared_files\study\DB-sql\.worktrees\mall-platform
```

当前分支：

```text
codex/mall-platform
```

远程仓库：

```text
https://github.com/morisa28/my-DB
```

远程分支：

```text
origin/codex/mall-platform
```

最近已推送提交：

```text
0a69e6e test: add demo flow api checks
46dcbdf feat: add docker compose demo environment
60a1964 feat: implement mall platform
```

当前可能存在尚未提交的文档改动：

```text
README.md
docs/08-接续开发说明.md
```

开始开发前请先执行只读检查：

```bash
git status -sb
git log --oneline -5
git remote -v
```

如果存在未提交改动，先确认其内容；不要覆盖、删除或回滚用户已有改动。

---

## 二、开发目标

将当前项目从“可演示级课程商城”升级为：

```text
可本地 Docker Compose 启动
可由用户后续部署到云服务器
可处理小规模真实订单
可由管理员完成基础运营
具备基本安全、配置、上传、测试和运维文档
```

服务器购买、域名解析、HTTPS 证书、安全组、云厂商配置由用户负责。你的任务是完成部署到服务器之前的应用侧开发准备。

---

## 三、当前已具备能力

项目已完成：

- Spring Boot 3 后端
- Vue3 + Vite + Element Plus 前端
- MySQL 建表和初始化数据
- JWT 登录鉴权
- BCrypt 密码加密
- 用户、商品、分类、购物车、地址、订单、后台管理
- 线下付款备注 + 管理员确认收款
- 管理员发货
- 后台统计
- Docker Compose 演示环境
- Nginx 托管前端并代理 `/api`
- `/api/health` 健康检查
- `api-test.http`
- `docs/demo-script.md`
- `docs/deploy-demo.md`

但当前还不是实用级项目，仍需补齐订单实用流程、文件上传、安全配置、部署前检查和测试。

---

## 四、重要开发原则

1. 不要为了绕过权限问题降低实现质量。
2. 需要权限、网络、Docker、推送、安装依赖时，直接申请提权。
3. 不要提交 `.env`、密码、token、证书、私钥。
4. 不要破坏已有 worktree 和远程分支。
5. 先读代码和文档，再改代码。
6. 每个阶段尽量小步提交。
7. 后端必须作为最终权限防线，不能只依赖前端隐藏按钮。
8. 订单金额、库存、订单状态必须以后端和数据库为准。
9. 所有部署相关配置必须可通过环境变量覆盖。
10. 当前目标是“小规模实用”，不是高并发、分布式、完整生产电商。

---

## 五、用户负责或需要确认的事项

这些不是代码可自动解决的问题，请不要假装已经完成。

用户负责：

- 准备云服务器
- 准备域名或公网 IP
- 配置安全组和端口开放
- 配置 HTTPS 证书
- 提供正式商品资料和图片
- 提供正式管理员密码
- 决定是否使用云数据库
- 决定是否接真实支付
- 决定是否接对象存储

默认策略：

- 支付：线下付款 + 管理员确认收款
- 图片：服务器本地持久化目录
- 数据库：MySQL 与应用同机 Docker Compose 部署
- 物流：管理员填写物流单号或发货备注
- 售后：v1 不做复杂退款退货系统

如果用户要接真实支付、短信、邮件、对象存储、物流 API，必须先让用户提供对应服务账号、密钥、回调域名、测试环境和接口文档。

---

## 六、第一阶段：收口当前文档改动

如果 `README.md` 和 `docs/08-接续开发说明.md` 未提交，请先提交并推送。

建议命令：

```bash
git add README.md docs/08-接续开发说明.md
git commit -m "docs: add continuation guide"
git push
```

验收：

```bash
git status -sb
git log --oneline -5
```

---

## 七、第二阶段：Docker 真实启动验证

在具备 Docker 的环境中验证：

```bash
cp .env.example .env
docker compose up -d --build
docker compose ps
```

检查：

```bash
curl http://localhost:8088/api/health
curl http://localhost:8080/api/health
```

浏览器访问：

```text
http://localhost:8088
```

如果失败，查看：

```bash
docker compose logs mysql
docker compose logs backend
docker compose logs frontend
```

必须确认：

- MySQL 初始化成功
- 后端可连接 MySQL
- 前端可访问
- `/api` 代理正常
- 默认账号可登录
- `docs/demo-script.md` 可完整执行

如需修复，建议提交：

```text
fix: stabilize docker demo startup
```

---

## 八、第三阶段：实用级订单流程

将订单流程从模拟演示升级为小规模实用。

订单状态保持：

```text
0 待支付
1 已支付待发货
2 已发货
3 已完成
4 已取消
```

新增或调整能力：

- 用户提交订单后进入待支付
- 用户查看付款说明
- 管理员确认收款
- 管理员发货
- 用户确认收货
- 用户取消待支付订单
- 取消待支付订单时释放库存
- 已确认收款后不能由用户直接取消
- 非法状态流转必须返回业务错误

建议新增接口：

```text
POST /api/admin/orders/{id}/confirm-payment
PUT  /api/orders/{id}/confirm-receipt
```

数据库可按需新增字段：

```text
payment_note      付款备注
admin_remark      管理员备注
shipping_no       物流单号
cancel_time       取消时间
confirm_time      确认收货时间
```

验收：

- 下单扣库存
- 取消待支付订单释放库存
- 管理员确认收款后状态变为待发货
- 管理员发货后状态变为已发货
- 用户确认收货后状态变为已完成
- 非法状态流转失败

---

## 九、第四阶段：商品图片上传

新增商品图片上传能力，v1 使用服务器本地持久化目录。

后端要求：

- 管理员上传商品主图
- 校验文件类型
- 校验文件大小
- 保存文件到 `UPLOAD_DIR`
- 返回可访问 URL
- 商品表保存图片 URL

默认限制：

```text
允许类型：jpg、jpeg、png、webp
最大大小：2MB
```

建议新增接口：

```text
POST /api/admin/upload/product-image
```

新增配置：

```text
UPLOAD_DIR=/app/uploads
UPLOAD_BASE_URL=/uploads
```

Docker 要求：

- 增加上传目录 volume
- 容器重启后图片不丢失

前端要求：

- 商品管理页支持上传图片
- 上传后可预览
- 商品列表和商品详情显示上传图片

验收：

- 管理员可上传图片
- 非图片被拒绝
- 超大图片被拒绝
- 容器重启后图片仍可访问

---

## 十、第五阶段：账户、安全和配置加固

必须完成：

- 生产配置禁止默认 JWT secret
- 禁用用户已有 token 继续请求时必须被拒绝
- 登录失败不泄露用户是否存在
- 普通用户不能访问 `/api/admin/**`
- 用户只能操作自己的订单、地址、购物车
- 管理员不能禁用自己
- 管理员可修改密码
- CORS 通过环境变量配置

新增配置建议：

```text
SPRING_PROFILES_ACTIVE=prod
CORS_ALLOWED_ORIGINS=https://your-domain.com
JWT_SECRET=强随机密钥
```

验收：

- 普通用户访问后台返回 403
- 未登录访问受保护接口返回 401
- 禁用用户 token 请求失败
- 他人订单和地址无法访问
- 默认 JWT secret 在 prod 环境不可用

---

## 十一、第六阶段：配置与数据库演进

新增配置文件：

```text
application-dev.yml
application-prod.yml
```

保留：

```text
application.yml
```

扩展 `.env.example`：

```text
MYSQL_ROOT_PASSWORD=
MYSQL_DATABASE=
MYSQL_PORT=
BACKEND_PORT=
FRONTEND_PORT=
JWT_SECRET=
JWT_EXPIRATION_MINUTES=
UPLOAD_DIR=
UPLOAD_BASE_URL=
CORS_ALLOWED_ORIGINS=
SPRING_PROFILES_ACTIVE=
```

数据库管理：

- 保留 `schema.sql` 作为全量初始化
- 新增 `migration/` 保存增量 SQL
- 暂不强制引入 Flyway，除非表结构继续频繁变化

验收：

- 新环境可全量初始化
- 旧环境可按 migration 升级
- README 说明初始化和升级区别

---

## 十二、第七阶段：管理后台实用化

后台需要满足小规模运营：

商品管理：

- 图片上传
- 上架/下架
- 库存修改
- 低库存筛选

订单管理：

- 状态筛选
- 查看详情
- 确认收款
- 发货
- 物流单号
- 管理员备注

用户管理：

- 搜索用户
- 启用/禁用用户
- 查看用户订单概要

统计：

- 总订单数
- 已收款金额
- 待发货订单
- 低库存商品
- 热销商品

验收：

- 管理员无需直接操作数据库即可完成日常小店运营

---

## 十三、第八阶段：前端实用体验

用户端：

- 商品详情展示库存、销量
- 结算页展示付款说明
- 订单页支持取消、确认收货
- 订单状态清晰
- 错误状态友好展示

管理端：

- 操作确认弹窗
- loading 状态
- 表单校验
- 操作成功后刷新列表
- 上传图片预览

全局：

- 401 自动退出登录
- 403 显示无权限
- 500 显示通用错误
- 移动端可完成基本浏览和下单

---

## 十四、第九阶段：测试和上线前验证

后端测试：

```bash
mvn test
mvn -DskipTests package
```

前端测试：

```bash
npm run build
```

Docker 测试：

```bash
docker compose down -v
docker compose up -d --build
curl http://localhost:8088/api/health
```

业务测试：

- 用户下单
- 用户取消待支付订单
- 管理员确认收款
- 管理员发货
- 用户确认收货
- 上传商品图片
- 禁用用户无法继续操作
- 普通用户无法访问后台
- 超库存下单失败
- 容器重启后数据和图片不丢失

更新：

```text
mall-backend/api-test.http
docs/05-测试报告.md
```

---

## 十五、第十阶段：服务器部署前交付包

新增文档：

```text
docs/deploy-server-precheck.md
docs/ops-runbook.md
```

必须包含：

- 服务器最低配置建议
- Docker / Compose 要求
- 端口清单
- `.env` 必填项
- 上传目录挂载
- 数据库初始化
- 数据库备份建议
- 日志查看
- 重启命令
- 回滚方式
- 常见故障排查
- 哪些内容必须由用户在服务器上完成

最终交付标准：

- Docker 可构建
- Compose 可运行
- 数据库可初始化
- 商品图片可上传
- 订单可小规模实用处理
- 配置不含敏感信息
- 用户可接手部署到云服务器

---

## 十六、不要埋下的隐患

禁止：

- 把 `.env` 提交到 Git
- 把真实密码写进 README
- 使用默认 JWT secret 作为生产配置
- 商品图片只存在容器内部而没有 volume
- 订单取消不释放库存
- 前端计算订单金额并作为最终金额
- 只在前端限制管理员权限
- 用户能访问他人订单或地址
- 不说明当前未接真实支付
- 不说明当前未配置 HTTPS
- 不说明服务器和域名由用户负责

凡是当前条件无法解决的事项，必须写进文档的“用户需处理事项”或“上线前检查清单”。

---

## 十七、提交规范

建议小步提交：

```text
docs: add continuation guide
fix: stabilize docker demo startup
feat: add practical order workflow
feat: add product image upload
fix: harden auth and user status checks
feat: add production profile configuration
feat: improve admin order operations
docs: add server deployment precheck
test: add practical mall flow checks
```

每次提交前执行：

```bash
git status --short
git diff --stat
```

能运行时执行：

```bash
cd mall-backend && mvn test
cd mall-frontend && npm run build
```

推送：

```bash
git push
```

---

## 十八、最终目标定义

当以下条件全部满足，才算完成服务器部署前开发：

- 本地 Docker clean 启动成功
- 前端、后端、MySQL 联通
- 管理员可上传商品图片
- 用户可真实下单
- 管理员可确认收款和发货
- 用户可确认收货
- 取消订单能恢复库存
- 权限和禁用用户逻辑正确
- 配置全部环境变量化
- 数据和图片持久化
- 测试报告更新
- 部署前检查文档完成
- 运维手册完成
- 不含密钥和真实敏感信息
