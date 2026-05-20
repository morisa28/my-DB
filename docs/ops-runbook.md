# 运维手册

本文档面向小规模上线后的日常运维。所有命令默认在项目根目录执行。

## 1. 服务组成

| Compose 服务 | 容器名 | 作用 |
|---|---|---|
| `mysql` | Compose 自动生成，例如 `mall-platform-mysql-1` | MySQL 8 数据库 |
| `backend` | Compose 自动生成，例如 `mall-platform-backend-1` | Spring Boot 3 API 服务 |
| `frontend` | Compose 自动生成，例如 `mall-platform-frontend-1` | Nginx 托管 Vue 前端并代理 `/api`、`/uploads` |

持久化数据：

| 数据 | 默认位置 |
|---|---|
| MySQL 数据 | Docker 命名卷 `mall-platform_mall-mysql-data` |
| 商品图片 | Docker 命名卷 `mall-platform_mall-upload-data`，容器内 `/app/uploads` |

说明：生产环境通过 `.env` 中的 `COMPOSE_PROJECT_NAME=mall-platform` 固定资源前缀。不要随意修改该值，否则 Compose 会创建新的容器和命名卷，看起来像“数据丢失”。

## 2. 启动、停止和重启

首次或更新后构建启动：

```bash
docker compose up -d --build
```

查看状态：

```bash
docker compose ps
```

停止服务但保留数据：

```bash
docker compose down
```

重启全部服务：

```bash
docker compose restart
```

只重启后端：

```bash
docker compose restart backend
```

不要在生产数据环境随意执行：

```bash
docker compose down -v
```

这会删除数据库卷和上传文件卷。

## 3. 健康检查

从服务器本机检查：

```bash
curl http://localhost:8088/api/health
curl http://localhost:8088/api/ready
```

从外网检查：

```bash
curl https://你的域名/api/health
curl https://你的域名/api/ready
```

正常结果应包含：

```text
UP
```

如果外网失败、本机成功，优先检查安全组、服务器防火墙、HTTPS 证书和外层反向代理。

说明：

- `/api/health` 只表示后端进程存活。
- `/api/ready` 会检查数据库连接，部署、发布和监控应优先使用该接口判断商城是否真正可用。

## 4. 日志查看

查看全部服务日志：

```bash
docker compose logs --tail=200
```

查看后端日志：

```bash
docker compose logs --tail=200 backend
```

持续跟踪后端日志：

```bash
docker compose logs -f backend
```

查看 MySQL 日志：

```bash
docker compose logs --tail=200 mysql
```

查看前端 Nginx 日志：

```bash
docker compose logs --tail=200 frontend
```

## 5. 数据库备份

创建备份目录：

```bash
mkdir -p backups
```

备份数据库：

```bash
docker compose exec -T mysql sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --routines --triggers "$MYSQL_DATABASE"' > backups/mall_db_$(date +%Y%m%d_%H%M%S).sql
```

备份后检查文件大小：

```bash
ls -lh backups
```

建议：

- 每日备份数据库。
- 每次上线或执行 migration 前先备份。
- 定期把备份复制到服务器外部位置。
- 不要把备份文件提交到 Git。

## 6. 上传图片备份

确认上传卷名称：

```bash
docker volume ls
```

备份默认上传卷示例：

```bash
docker run --rm -v mall-platform_mall-upload-data:/data -v "$PWD/backups":/backup alpine tar czf /backup/mall_uploads_$(date +%Y%m%d_%H%M%S).tar.gz -C /data .
```

如果实际卷名不是 `mall-platform_mall-upload-data`，用 `docker volume ls` 中的实际名称替换。

## 7. 数据恢复

恢复前先停止会写入数据库的服务：

```bash
docker compose stop backend frontend
```

恢复数据库：

```bash
docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"' < backups/mall_db_backup.sql
```

恢复上传图片时，先确认目标卷名，再执行：

```bash
docker run --rm -v mall-platform_mall-upload-data:/data -v "$PWD/backups":/backup alpine sh -c "cd /data && tar xzf /backup/mall_uploads.tar.gz"
```

恢复后启动服务：

```bash
docker compose up -d
```

恢复完成后检查：

```bash
curl http://localhost:8088/api/health
curl http://localhost:8088/api/ready
```

## 8. 更新发布流程

推荐流程：

1. 记录当前提交号：

```bash
git rev-parse --short HEAD
```

2. 备份数据库和上传图片。

3. 拉取或切换到新版本代码。

4. 如果有数据库 migration，按 `docs/database-migration.md` 先备份后执行。

5. 构建并启动：

```bash
docker compose up -d --build
```

6. 查看服务状态和健康检查：

```bash
docker compose ps
curl http://localhost:8088/api/health
curl http://localhost:8088/api/ready
```

7. 执行业务验收：

```bash
node scripts/practical-flow-check.mjs
```

注意：业务验收脚本会写入测试订单、地址和上传图片，正式生产环境执行前应确认是否接受这些测试数据。正式环境更推荐在预发布环境执行完整脚本，在生产环境只做健康检查和人工抽查。

## 9. 回滚方式

回滚前提：

- 记录了上一个可用 Git 提交号。
- 已有上线前数据库和上传图片备份。

应用代码回滚：

```bash
git checkout <上一个可用提交号>
docker compose up -d --build
```

如果本次发布执行了数据库 migration 且需要回滚数据库：

```bash
docker compose stop backend frontend
docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"' < backups/mall_db_backup.sql
docker compose up -d
```

说明：

- 当前项目没有自动 migration 回滚工具。
- 数据库结构变更上线前必须备份。
- 如果上线后已有新订单，直接恢复旧备份会丢失这些订单，需要先人工导出或评估。

## 10. 常见故障排查

### 前端打不开

检查：

```bash
docker compose ps
docker compose logs --tail=100 frontend
```

重点确认：

- `frontend` 容器是否运行。
- `FRONTEND_PORT` 是否被占用。
- 安全组是否开放了入口端口。
- 外层 Nginx/Caddy 是否反代到正确端口。

### 前端能打开但接口失败

检查：

```bash
curl http://localhost:8088/api/health
curl http://localhost:8088/api/ready
docker compose logs --tail=200 backend
```

重点确认：

- 后端是否运行。
- 后端是否连接上 MySQL。
- `CORS_ALLOWED_ORIGINS` 是否包含正式前端域名。
- 前端 Nginx `/api` 代理是否生效。

### 后端启动失败

检查：

```bash
docker compose logs --tail=200 backend
```

常见原因：

- `SPRING_PROFILES_ACTIVE=prod` 但未设置必要环境变量。
- `JWT_SECRET` 仍是演示值或长度不足。
- `DB_PASSWORD` 与 MySQL 密码不一致。
- MySQL 还未健康。
- 上传目录权限异常。

### MySQL 启动失败

检查：

```bash
docker compose logs --tail=200 mysql
docker volume ls
```

常见原因：

- 数据卷已有旧数据库，修改 `MYSQL_ROOT_PASSWORD` 不会自动改变旧 root 密码。
- 磁盘空间不足。
- 数据卷损坏。

处理原则：

- 先备份能备份的数据。
- 不要直接 `down -v` 删除生产卷。
- 如果只是密码不一致，优先把 `.env` 中的 `DB_PASSWORD` 改回实际数据库密码。

### 图片上传失败

检查：

```bash
docker compose logs --tail=200 backend
docker compose exec backend ls -la /app/uploads
```

重点确认：

- 文件扩展名是否在 `jpg,jpeg,png,webp` 内。
- 文件大小是否超过限制。
- `mall-upload-data` 是否挂载。
- `/uploads/**` 是否能通过前端域名访问。

### 登录失败或 Token 异常

检查：

- 用户是否被管理员禁用。
- `.env` 中 `JWT_SECRET` 是否在重启前后发生变化。
- 浏览器是否保留了旧 Token。
- 后端时间是否正确。

如果修改了 `JWT_SECRET`，所有旧 Token 都会失效，用户需要重新登录。

## 11. 日常运营建议

- 管理员账号不要多人共用，正式使用前修改默认密码。
- 上架商品前核对价格、库存、分类和图片。
- 每日查看待发货订单。
- 确认线下收款后再点击确认收款。
- 发货时填写物流单号或发货备注。
- 定期检查低库存商品。
- 每次发布前备份数据库和上传图片。
- 保留最近多个版本的数据库备份。

## 12. 需要用户长期维护的事项

- 云服务器续费、系统更新和安全组维护。
- 域名、HTTPS 证书和反向代理维护。
- 数据库和上传文件备份。
- 正式商品资料维护。
- 收款账户和线下付款规则维护。
- 是否接入真实支付、短信、邮件、对象存储和物流 API 的后续决策。
