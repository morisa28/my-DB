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
export JWT_SECRET="至少32字节的JWT密钥"
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
```

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

该文件包含注册、登录、分类、商品、购物车、地址、下单、付款备注、管理员确认收款、发货、确认收货、取消订单和统计接口示例。

## 重点展示功能

- 用户注册登录和 JWT 鉴权。
- 商品分页、搜索、分类筛选。
- 管理员上传商品主图，图片保存到持久化上传目录并通过 `/uploads/**` 访问。
- 购物车同用户同商品唯一记录。
- 地址默认值互斥。
- 订单创建事务：订单主表、订单明细、库存扣减、销量增加、购物车清理同事务完成。
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
- `deploy-demo.md`
- `demo-script.md`
- `presentation-outline.md`

## 本地验收建议

1. 执行数据库脚本。
2. 启动后端。
3. 启动前端。
4. 使用 `user / user123456` 完成浏览商品、加入购物车、新增地址、提交订单、填写付款备注和确认收货。
5. 使用 `admin / admin123456` 上传商品图片、查看统计、筛选订单、确认收款并发货。
6. 在 MySQL 中观察 `order_info`、`order_item`、`product`、`cart_item` 表的数据变化。
