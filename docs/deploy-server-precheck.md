# 服务器部署前检查清单

本文档用于在正式上云前检查应用侧准备情况。云服务器购买、域名解析、HTTPS 证书、安全组和云厂商控制台配置由用户完成；本项目提供可部署的 Docker Compose 应用包、配置说明和验收步骤。

## 1. 当前交付边界

已具备：

- Spring Boot 3 后端、Vue3 前端、MySQL 8 和 Nginx 前端容器。
- Docker Compose 一键构建和启动。
- MySQL 数据卷持久化。
- 商品图片上传目录持久化。
- `/api/health` 健康检查。
- `/api/ready` 数据库就绪检查。
- `docker-compose.prod.yml` 生产部署模板。
- 线下付款备注、管理员确认收款、发货、用户确认收货和待支付订单取消。
- JWT 鉴权、管理员接口权限、禁用用户拦截、生产环境危险配置启动拦截。
- 上线前业务验收脚本 `scripts/practical-flow-check.mjs`。

未内置：

- HTTPS 证书自动申请。
- 云服务器安全组配置。
- 真实支付、短信、邮件、物流 API。
- 对象存储。
- 云数据库。
- WAF、CDN、专业监控告警。

## 2. 服务器最低配置建议

小规模实用部署建议：

| 项目 | 最低建议 | 更稳妥建议 |
|---|---:|---:|
| CPU | 2 核 | 2-4 核 |
| 内存 | 2 GB | 4 GB |
| 系统盘 | 40 GB | 60 GB 以上 |
| 带宽 | 3 Mbps | 5 Mbps 以上 |
| 操作系统 | Ubuntu 22.04 LTS / Debian 12 / Rocky Linux 9 | Ubuntu 22.04 LTS |

说明：

- 2 GB 内存可运行小流量商城，但构建镜像时可能较紧张；如在服务器上直接 `docker compose build`，建议 4 GB 内存。
- 商品图片会持续占用磁盘，需要按实际商品数量和备份策略预留空间。
- 如果后续接入真实支付、对象存储或云数据库，应重新评估网络和安全配置。

## 3. Docker / Compose 要求

服务器需安装：

```bash
docker --version
docker compose version
```

建议：

- Docker Engine 24+。
- Docker Compose v2。
- 当前用户可执行 Docker 命令，或使用 `sudo docker`。
- 服务器时间和时区正确，建议使用 Asia/Shanghai。

## 4. 端口清单

Compose 默认端口：

| 端口 | 服务 | 是否建议公网开放 | 说明 |
|---:|---|---|---|
| 8088 | `frontend` | 是，或由外层 Nginx/Caddy 反代 | 用户访问入口 |
| 8080 | `backend` | 否 | 后端直连调试端口，公网部署建议只允许本机或内网访问 |
| 3307 | `mysql` | 否 | 仅本地演示 Compose 默认映射；生产 Compose 不映射 MySQL |
| 80 | 外层反代 | 是 | 如用户使用服务器级 Nginx/Caddy |
| 443 | 外层反代 | 是 | HTTPS 入口，由用户配置证书 |

小规模上线推荐：

- 公网只开放 80/443，外层反代到 `127.0.0.1:8088`。
- 如果暂时没有 HTTPS，可短期开放 `FRONTEND_PORT=8088` 进行验收，但不建议长期裸 HTTP 运行。
- 不要把 MySQL 端口开放到公网。

## 5. `.env` 必填项

本地演示从模板复制：

```bash
cp .env.example .env
```

生产部署应使用生产模板：

```bash
cp .env.prod.example .env
```

生产部署必须替换所有 `change-me` 占位值：

```text
MYSQL_ROOT_PASSWORD=替换为强密码
MYSQL_DATABASE=mall_db
MYSQL_USER=mall_app
MYSQL_PASSWORD=替换为应用数据库用户强密码

FRONTEND_PORT=8088

JWT_SECRET=替换为至少32字节的强随机密钥
JWT_EXPIRATION_MINUTES=10080
CORS_ALLOWED_ORIGINS=https://你的域名

UPLOAD_DIR=/app/uploads
UPLOAD_BASE_URL=/uploads
UPLOAD_MAX_FILE_SIZE=2MB
UPLOAD_MAX_REQUEST_SIZE=3MB
UPLOAD_MAX_SIZE_BYTES=2097152
```

注意：

- `.env` 已被 `.gitignore` 忽略，不要提交到 Git。
- 生产 Compose 固定 `SPRING_PROFILES_ACTIVE=prod`，后端使用 `MYSQL_USER` 和 `MYSQL_PASSWORD` 连接数据库。
- 生产环境后端禁止 root 数据库账号、demo JWT 密钥和 `CORS_ALLOWED_ORIGINS=*`。
- 如果外层反代使用 HTTPS，`CORS_ALLOWED_ORIGINS` 应填写 HTTPS 域名。
- 若存在多个前端域名，用逗号分隔，例如 `https://example.com,https://www.example.com`。

## 6. 上传目录挂载

当前 Compose 使用命名卷：

```text
mall-upload-data:/app/uploads
```

这能保证容器重启或重建后图片不丢失。服务器上线前需要确认：

```bash
docker volume ls
docker compose exec backend ls -la /app/uploads
```

如用户希望把图片目录直接落到服务器指定目录，可把 `docker-compose.yml` 中的上传卷改为绑定挂载，例如：

```yaml
volumes:
  - /data/mall/uploads:/app/uploads
```

绑定挂载时由用户负责创建目录和设置权限：

```bash
mkdir -p /data/mall/uploads
```

## 7. 数据库初始化

首次启动时，MySQL 数据卷为空，Compose 会自动执行内置在 `mall-platform-mysql` 镜像中的初始化 SQL：

```text
mall-backend/src/main/resources/sql/schema.sql
mall-backend/src/main/resources/sql/data.sql
```

本地演示启动：

```bash
docker compose up -d --build
docker compose ps
```

生产部署启动：

```bash
docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml ps
```

检查：

```bash
curl http://localhost:8088/api/health
curl http://localhost:8088/api/ready
```

已有数据库升级时不要执行：

```bash
docker compose down -v
```

原因：`down -v` 会删除 MySQL 和上传文件卷。已有数据升级应先备份，再按 `docs/database-migration.md` 执行 migration。

## 8. 数据库备份建议

上线前至少建立手动备份流程：

```bash
mkdir -p backups
docker compose exec -T mysql sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --routines --triggers "$MYSQL_DATABASE"' > backups/mall_db_$(date +%Y%m%d_%H%M%S).sql
```

该命令从 MySQL 容器环境变量读取数据库名和 root 密码，不会把真实密码写入文档或 Git。

建议频率：

- 小规模上线初期：每天一次数据库备份。
- 有订单期间：重大更新前手动备份。
- 图片上传目录：与数据库一起备份，避免订单或商品引用的图片丢失。

图片卷备份可参考：

```bash
docker run --rm -v mall-platform_mall-upload-data:/data -v "$PWD/backups":/backup alpine tar czf /backup/mall_uploads.tar.gz -C /data .
```

卷名前缀可能随项目目录变化，可用 `docker volume ls` 确认实际卷名。

## 9. 部署前验收步骤

在服务器或与服务器一致的本地环境执行：

```bash
docker compose down
docker compose up -d --build
docker compose ps
curl http://localhost:8088/api/health
curl http://localhost:8088/api/ready
node scripts/practical-flow-check.mjs
```

验收项：

- 三个容器运行正常。
- `/api/health` 返回 `UP`。
- `/api/ready` 返回 `UP` 且 `database=UP`。
- 默认管理员可登录。
- 普通用户可下单、提交付款备注、取消待支付订单。
- 管理员可确认收款、发货、上传商品图片。
- 用户可确认收货。
- 普通用户访问后台接口返回 403。
- 禁用用户 Token 继续请求会失败。
- 商品图片通过 `/uploads/**` 可访问。

## 10. 用户上线前必须完成

用户必须自行完成：

- 购买云服务器。
- 安装 Docker 和 Docker Compose。
- 配置服务器防火墙和云厂商安全组。
- 准备域名或决定使用公网 IP。
- 配置 HTTPS 证书和外层反向代理。
- 修改 `.env` 中所有生产模板占位密码和密钥。
- 修改默认管理员密码。
- 准备正式商品资料、分类、库存、价格和图片。
- 决定是否继续使用同机 MySQL，或迁移到云数据库。
- 建立数据库和上传图片备份机制。
- 确认线下付款方式、收款账户展示内容和订单处理规则。

## 11. 上线前风险提示

必须明确：

- 当前支付方式是线下付款加管理员确认收款，不是自动支付。
- 当前没有自动退款、退货、售后系统。
- 当前图片存储默认在服务器本地卷，不是对象存储。
- 当前不包含 HTTPS 自动配置；没有 HTTPS 时不要收集敏感支付信息。
- 当前适合小规模自营商城，不适合高并发、多商户或复杂促销场景。
- 默认演示账号和演示数据必须在正式使用前调整。

## 12. 最终检查清单

上线前逐项确认：

- [ ] `.env` 已创建且未提交 Git。
- [ ] 使用 `docker-compose.prod.yml` 启动，后端 profile 为 `prod`。
- [ ] `JWT_SECRET` 已替换为强随机密钥。
- [ ] `MYSQL_ROOT_PASSWORD` 和 `MYSQL_PASSWORD` 已替换为强密码。
- [ ] `MYSQL_USER` 不是 `root`。
- [ ] `CORS_ALLOWED_ORIGINS` 已设置为正式域名。
- [ ] 公网未开放 MySQL 端口。
- [ ] 上传目录卷或绑定挂载已确认可持久化。
- [ ] 数据库备份命令已测试。
- [ ] 上传文件备份方式已测试。
- [ ] 默认管理员密码已修改。
- [ ] HTTPS 证书和反代规则已配置。
- [ ] `node scripts/practical-flow-check.mjs` 已通过。
- [ ] 正式商品、库存、价格和收款说明已核对。
