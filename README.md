# SpringBoot3 + Vue3 小型商城平台

这是一个数据库课程大作业项目，包含 SpringBoot3 后端、Vue3 前端、MySQL 建表和初始化数据脚本、接口测试文件以及实验报告素材。

## 项目结构

```text
mall-project/
├── mall-backend/      # SpringBoot3 + MyBatis-Plus 后端
├── mall-frontend/     # Vue3 + Vite + Element Plus 前端
├── docs/              # 实验报告素材
├── PLAN.md
├── AGENTS.md
└── README.md
```

## 技术栈

- 后端：Java 17、Spring Boot 3、Spring Web、Validation、MyBatis-Plus、JWT、BCrypt、MySQL 8
- 前端：Vue 3、Vite、Vue Router、Pinia、Axios、Element Plus
- 数据库：MySQL 8，使用外键、唯一约束、检查约束和索引

## 初始化数据库

```bash
mysql -u root -p < mall-backend/src/main/resources/sql/schema.sql
mysql -u root -p mall_db < mall-backend/src/main/resources/sql/data.sql
```

如需修改数据库连接，设置环境变量：

```bash
export DB_URL="jdbc:mysql://localhost:3306/mall_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
export DB_USERNAME="root"
export DB_PASSWORD="你的密码"
export SPRING_PROFILES_ACTIVE="dev"
export JWT_SECRET="至少32字节的JWT密钥"
export CORS_ALLOWED_ORIGINS="http://localhost:5173"
export UPLOAD_DIR="./uploads"
export UPLOAD_BASE_URL="/uploads"
```

## 演示模式启动

推荐使用 Docker Compose 启动可演示级环境，包含 MySQL、后端和 Nginx 前端。

```bash
cp .env.example .env
docker compose up -d --build
```

访问地址：

```text
前端入口：http://localhost:8088
后端健康检查：http://localhost:8088/api/health
后端就绪检查：http://localhost:8088/api/ready
MySQL：localhost:3307
```

重置演示数据：

```bash
docker compose down -v
docker compose up -d --build
```

详细说明见：

```text
docs/deploy-demo.md
docs/database-migration.md
docs/deploy-server-precheck.md
docs/ops-runbook.md
```

## 生产模式部署

`docker-compose.yml` 仅用于本地演示/开发，内置了 demo 数据库密码、demo JWT 密钥、开发 profile 和 MySQL 端口映射，不应直接用于公网服务器。

生产或服务器内测请使用生产模板，并替换所有占位值：

```bash
cp .env.prod.example .env
docker compose -f docker-compose.prod.yml up -d --build
```

生产 Compose 的安全约束：

- 强制要求 `MYSQL_ROOT_PASSWORD`、`MYSQL_USER`、`MYSQL_PASSWORD`、`JWT_SECRET`、`CORS_ALLOWED_ORIGINS`。
- 后端固定使用 `SPRING_PROFILES_ACTIVE=prod`。
- 后端使用 `MYSQL_USER` 连接数据库，禁止使用 root 账号。
- MySQL 不映射到宿主机端口，避免误暴露到公网。
- 后端容器健康检查使用 `/api/ready`，必须能连接数据库才算就绪。

后端在 prod profile 下会启动自检，发现以下危险配置会拒绝启动：

- `JWT_SECRET` 为空、少于 32 字节、使用 demo 值或仍包含 `change-me`。
- `CORS_ALLOWED_ORIGINS` 为空或包含 `*`。
- `DB_USERNAME=root`。
- `DB_PASSWORD` 为空、过短、使用 demo 值或仍包含 `change-me`。

## 启动后端

```bash
cd mall-backend
mvn clean package
mvn spring-boot:run
```

默认端口：`8080`。

## 启动前端

```bash
cd mall-frontend
npm install
npm run dev
```

默认端口：`5173`。Vite 已配置 `/api` 代理到 `http://localhost:8080`。

## 演示账号

| 角色 | 用户名 | 密码 |
|---|---|---|
| 管理员 | admin | admin123456 |
| 普通用户 | user | user123456 |
| 普通用户 | alice | user123456 |

初始化数据中的密码均使用 BCrypt 加密存储。

## 核心接口测试

接口测试文件：

```text
mall-backend/api-test.http
```

该文件包含注册、登录、登录失败限流、主动退出、分类、商品、购物车、地址、下单、付款备注、管理员确认收款、发货、确认收货、取消订单和统计接口示例。

上线前业务验收脚本：

```bash
node scripts/practical-flow-check.mjs
```

脚本默认访问 `http://localhost:8088/api`，可通过 `BASE_URL` 覆盖。

## 重点展示功能

- 用户注册登录和 JWT 鉴权。
- JWT 内含会话版本，修改密码、主动退出登录或管理员禁用用户后旧 Token 会失效。
- 登录失败有基础内存限流，注册和修改密码要求至少 8 位且包含字母和数字。
- 禁用用户 Token 会被后端拒绝，生产环境禁止使用默认 JWT 密钥。
- CORS 允许来源可通过 `CORS_ALLOWED_ORIGINS` 配置。
- 生产环境启动会校验 JWT、CORS 和数据库账号密码，避免 demo 配置误上线。
- 商品分页、搜索、分类筛选。
- 管理员上传商品主图，图片保存到持久化上传目录并通过 `/uploads/**` 访问。
- 管理后台支持低库存筛选和用户订单概要查看。
- 购物车同用户同商品唯一记录。
- 地址默认值互斥。
- 订单创建事务：订单主表、订单明细、库存扣减、销量增加、购物车清理同事务完成。
- 下单接口要求 `requestId`，同一用户同一 `requestId` 重复提交会返回同一订单，避免网络重试生成重复订单。
- 同一购物车项在事务内先声明清理，不同 `requestId` 重复结算会失败，降低并发重复下单风险。
- 库存流水记录下单扣库存、取消待支付订单恢复库存和管理员手动调库存，便于审计追踪。
- 管理员确认线下收款时会生成 `OFFLINE` 支付单；支付回调日志表已预留，便于后续接入真实支付。
- 实用级订单流程：用户提交付款备注，管理员确认收款并发货，用户确认收货；取消待支付订单会恢复库存并回退销量。
- 订单明细保存商品名称、价格、图片快照。
- 管理员后台管理商品、分类、订单、用户和统计数据。

## 实验报告素材

`docs/` 目录中包含：

- `01-需求分析.md`
- `02-系统设计.md`
- `03-数据库设计.md`
- `04-系统实现.md`
- `05-测试报告.md`
- `06-实验总结.md`
- `07-后续开发方案.md`
- `08-接续开发说明.md`
- `09-Codex后续开发Prompt.md`
- `10-阶段四-商品图片上传.md`
- `11-阶段五-账户安全配置加固.md`
- `12-阶段六-配置与数据库演进.md`
- `13-阶段七-管理后台实用化.md`
- `14-阶段八-前端实用体验.md`
- `15-阶段九-测试和上线前验证.md`
- `16-阶段十-服务器部署前交付包.md`
- `17-整体隐患审查报告.md`
- `18-阶段十一-生产安全与运行稳定性加固.md`
- `19-阶段十二-登录会话安全加固.md`
- `20-阶段十三-下单幂等与重复结算防护.md`
- `21-阶段十四-库存流水审计.md`
- `22-阶段十五-支付单与回调日志预留.md`
- `deploy-demo.md`
- `demo-script.md`
- `database-migration.md`
- `deploy-server-precheck.md`
- `ops-runbook.md`
- `presentation-outline.md`

## 本地验收建议

1. 执行数据库脚本。
2. 启动后端。
3. 启动前端。
4. 使用 `user / user123456` 完成浏览商品、加入购物车、新增地址、提交订单、填写付款备注和确认收货。
5. 使用 `admin / admin123456` 上传商品图片、查看统计、筛选订单、确认收款并发货。
6. 在 MySQL 中观察 `order_info`、`order_item`、`product`、`cart_item` 表的数据变化。
