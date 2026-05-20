# 可演示级部署说明

本文档用于把项目启动为可演示级商城系统：MySQL、Spring Boot 后端、Nginx + Vue 前端统一由 Docker Compose 管理。

## 1. 环境要求

- Docker Desktop 或 Docker Engine。
- Docker Compose v2。
- 当前目录为项目根目录。

检查命令：

```bash
docker --version
docker compose version
```

## 2. 首次启动

复制环境变量模板：

```bash
cp .env.example .env
```

启动全部服务：

```bash
docker compose up -d --build
```

查看服务状态：

```bash
docker compose ps
```

访问地址：

```text
前端入口：http://localhost:8088
后端健康检查：http://localhost:8088/api/health
后端就绪检查：http://localhost:8088/api/ready
后端直连：http://localhost:8080/api/health
MySQL：localhost:3307
```

默认演示账号：

| 角色 | 用户名 | 密码 |
|---|---|---|
| 管理员 | admin | admin123456 |
| 普通用户 | user | user123456 |
| 普通用户 | alice | user123456 |

## 3. 服务结构

```text
Browser
  ↓ http://localhost:8088
Nginx / Vue
  ├── /      -> 前端静态页面
  ├── /api   -> Spring Boot 后端
  └── /uploads -> Spring Boot 上传文件访问
                  ↓
                MySQL 8
```

Compose 服务：

| 服务 | 说明 | 默认端口 |
|---|---|---:|
| `mysql` | MySQL 8，自动执行初始化 SQL | `3307 -> 3306` |
| `backend` | Spring Boot 3 后端 | `8080 -> 8080` |
| `frontend` | Nginx 托管 Vue 构建产物 | `8088 -> 80` |

持久化卷：

| 卷 | 用途 |
|---|---|
| `mall-mysql-data` | MySQL 数据 |
| `mall-upload-data` | 商品图片上传文件，默认挂载到后端容器 `/app/uploads` |

## 4. 演示前检查

执行：

```bash
docker compose ps
```

确认三个服务都处于运行状态。

检查后端：

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/api/ready
```

检查前端代理：

```bash
curl http://localhost:8088/api/health
curl http://localhost:8088/api/ready
```

如果 `/api/health` 返回 `status=UP`，说明后端进程可响应；如果 `/api/ready` 也返回 `status=UP` 且 `database=UP`，说明数据库依赖也可用。

## 5. 数据库连接

默认连接信息：

```text
host: localhost
port: 3307
user: root
password: mall_demo_root_2026
database: mall_db
```

如果修改过 `.env`，以 `.env` 中的值为准。

演示时建议重点观察：

```sql
SELECT * FROM cart_item;
SELECT * FROM order_info ORDER BY id DESC;
SELECT * FROM order_item ORDER BY id DESC;
SELECT id, name, stock, sales FROM product ORDER BY id;
```

上传图片访问检查：

```bash
curl -I http://localhost:8088/uploads/<上传返回路径>
```

## 6. 重置演示数据

如果需要恢复初始化数据，删除 MySQL 数据卷并重启：

```bash
docker compose down -v
docker compose up -d --build
```

注意：`down -v` 会删除容器数据库数据和上传图片卷，只适合演示环境重置。

## 7. 停止服务

停止但保留数据库数据：

```bash
docker compose down
```

停止并清空数据库数据：

```bash
docker compose down -v
```

## 8. 常见问题

### 端口被占用

修改 `.env`：

```text
MYSQL_ROOT_PASSWORD=mall_demo_root_2026
MYSQL_DATABASE=mall_db
MYSQL_PORT=3307
DB_USERNAME=root
DB_PASSWORD=mall_demo_root_2026
BACKEND_PORT=8080
FRONTEND_PORT=8088
SPRING_PROFILES_ACTIVE=dev
CORS_ALLOWED_ORIGINS=*
UPLOAD_DIR=/app/uploads
UPLOAD_BASE_URL=/uploads
```

`DB_PASSWORD` 必须与后端实际连接 MySQL 的密码一致。默认同 `MYSQL_ROOT_PASSWORD`；如果修改 MySQL root 密码，也要同步修改 `DB_PASSWORD`。

修改后重新启动：

```bash
docker compose up -d --build
```

### 前端能打开但接口失败

检查：

```bash
docker compose logs backend
docker compose logs frontend
curl http://localhost:8088/api/health
curl http://localhost:8088/api/ready
```

重点确认后端是否连接上 MySQL，以及 Nginx 是否把 `/api` 代理到 `backend:8080`。

### 默认账号无法登录

通常是数据库未初始化或使用了旧数据卷。执行：

```bash
docker compose down -v
docker compose up -d --build
```

### 商品图片上传失败

检查：

```bash
docker compose logs backend
docker compose exec backend ls -la /app/uploads
```

重点确认：

- 图片格式是否为 `jpg`、`jpeg`、`png`、`webp`。
- 图片大小是否不超过 2MB。
- `mall-upload-data` 卷是否挂载到后端容器 `/app/uploads`。
- 前端 Nginx 是否把 `/uploads/` 代理到后端。

### 后端启动失败

查看日志：

```bash
docker compose logs backend
```

重点检查：

- `DB_URL` 是否指向 `mysql:3306`。
- `DB_PASSWORD` 是否和 MySQL root 密码一致。
- `JWT_SECRET` 是否至少 32 字节。
- 如果设置 `SPRING_PROFILES_ACTIVE=prod`，必须使用非默认 `JWT_SECRET`。
- `SPRING_PROFILES_ACTIVE=prod` 时，`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`JWT_SECRET`、`CORS_ALLOWED_ORIGINS` 都必须显式配置。

## 9. 手动开发模式

如果 Docker 不可用，可以手动启动。

数据库：

```bash
mysql -u root -p < mall-backend/src/main/resources/sql/schema.sql
mysql -u root -p mall_db < mall-backend/src/main/resources/sql/data.sql
```

后端：

```bash
cd mall-backend
mvn spring-boot:run
```

前端：

```bash
cd mall-frontend
npm install
npm run dev
```

在 Windows/WSL 挂载盘中，如果 `npm install` 因符号链接或权限失败，可使用：

```bash
npm install --no-bin-links
```

## 10. 演示级边界

当前部署用于课程演示，不等同于生产部署：

- 使用本地 HTTP，不配置 HTTPS。
- 支付为线下付款备注 + 管理员确认收款，当前未接入真实支付网关。
- 数据库密码和 JWT 密钥可通过 `.env` 修改，但未接入密钥管理系统。
- 未配置监控告警、备份恢复、CI/CD。
- 初始化商品仍使用外部示例图片 URL；后台新增或编辑商品时可上传本地持久化图片。
