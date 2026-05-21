# 阶段十三：Docker 运行态复验与 Compose 隔离修复

## 阶段目标

在 Docker Desktop 恢复后，对阶段十一和阶段十二的修复进行真实容器运行态验收，并继续修复验收过程中暴露出的部署隐患。

## 本轮发现的问题

### 1. 固定容器名导致环境冲突

执行当前修复分支的 Docker Compose 启动时，构建已成功，但启动失败：

```text
Conflict. The container name "/mall-mysql" is already in use
```

原因是 `docker-compose.yml` 和 `docker-compose.prod.yml` 固定了：

```yaml
container_name: mall-mysql
container_name: mall-backend
container_name: mall-frontend
```

这会导致以下问题：

- 多个 worktree 无法同时启动同一项目。
- 同一服务器上无法部署测试环境和生产环境。
- 旧容器未清理时，新版本启动会失败。
- 容器名绕过 Compose 默认项目隔离机制。

### 2. 默认 Docker 演示栈输出 SQL 参数

默认 `docker-compose.yml` 使用 `SPRING_PROFILES_ACTIVE=dev`，而 `application-dev.yml` 开启 MyBatis SQL stdout 日志。运行业务验收后，后端日志会输出手机号、邮箱和密码哈希等字段。

生产配置 `application-prod.yml` 已使用 `NoLoggingImpl`，但默认演示栈也不应把敏感业务字段直接写入容器日志。

## 完成内容

- 移除 `docker-compose.yml` 和 `docker-compose.prod.yml` 中固定的 `container_name`。
- 在 `.env.prod.example` 中增加：

```text
COMPOSE_PROJECT_NAME=mall-platform
```

- 更新部署文档，说明 `COMPOSE_PROJECT_NAME` 用于固定生产资源前缀，部署后不要随意修改。
- 更新运维手册，容器名改为 Compose 自动生成，例如：

```text
mall-platform-mysql-1
mall-platform-backend-1
mall-platform-frontend-1
```

- 将 `application-dev.yml` 中 MyBatis 日志实现改为可配置：

```yaml
log-impl: ${MYBATIS_LOG_IMPL:org.apache.ibatis.logging.stdout.StdOutImpl}
```

- 在默认 Docker Compose 演示栈中设置：

```text
MYBATIS_LOG_IMPL=org.apache.ibatis.logging.nologging.NoLoggingImpl
```

本地直接运行 dev profile 仍保留 SQL 调试默认值；Docker 演示环境默认关闭 SQL 参数输出。

## 自我审计

| 审计项 | 结论 |
|---|---|
| 是否依赖手动删除旧容器解决冲突 | 否，已移除固定容器名 |
| 多个 worktree 是否可以通过不同端口并行启动 | 是 |
| 生产环境资源前缀是否稳定 | 是，通过 `COMPOSE_PROJECT_NAME=mall-platform` 固定 |
| 生产环境 SQL 日志是否关闭 | 是，`application-prod.yml` 仍使用 `NoLoggingImpl` |
| 默认 Docker 演示栈是否仍输出 SQL 参数 | 否，已通过日志复核 |
| 是否删除数据卷或清理用户容器 | 否 |

## 验证结果

已执行：

| 验证项 | 结果 | 说明 |
|---|---|---|
| `docker compose config --quiet` | 通过 | 默认 Compose 可解析 |
| `docker compose --env-file .env.prod.example -f docker-compose.prod.yml config --quiet` | 通过 | 生产 Compose 可解析 |
| `MYSQL_PORT=3317 BACKEND_PORT=8090 FRONTEND_PORT=8098 docker compose up -d --build` | 通过 | 当前 worktree 使用隔离端口启动 |
| `docker compose ps` | 通过 | MySQL、backend 均 healthy，frontend 正常运行 |
| `curl http://localhost:8098/api/health` | 通过 | 后端存活 |
| `curl http://localhost:8098/api/ready` | 通过 | 后端和数据库均就绪 |
| `curl "http://localhost:8098/api/products?page=1&size=1"` | 通过 | 商品接口可用 |
| `BASE_URL=http://localhost:8098/api node scripts/practical-flow-check.mjs` | 通过 | 业务验收脚本全部通过 |
| `npm run build` | 通过 | 前端生产构建通过，仍存在 Vite 大 chunk 警告 |
| `npm audit --omit=dev` | 通过 | 生产依赖无已知漏洞 |
| `git diff --check` | 通过 | 无空白错误 |
| `docker compose logs --tail=120 backend` | 通过 | 未再出现 SQL 参数输出 |

业务验收脚本最后一次输出摘要：

```json
{
  "base": "http://localhost:8098/api",
  "orderId": 8,
  "cancelOrderId": 9,
  "checks": "passed"
}
```

## 当前运行状态

当前隔离修复栈仍在运行，便于继续人工检查：

```text
前端入口：http://localhost:8098
后端直连：http://localhost:8090
MySQL 端口：3317
```

同时旧的 `mall-platform` 栈仍可继续占用默认演示端口 `8088`，两者互不冲突。

## 未完成 / 风险

- 本轮没有引入后端自动化集成测试，订单并发仍主要依赖脚本和数据库条件更新验证。
- 前端构建仍有大 chunk 警告，后续应进行代码分包和 Element Plus 按需优化。
- 当前默认 Docker Compose 仍是演示配置，生产部署必须使用 `.env.prod.example` 派生的 `.env` 和 `docker-compose.prod.yml`。
- 服务器部署后不得随意修改 `COMPOSE_PROJECT_NAME`，否则会创建新的命名卷，表现为旧数据不可见。

## 建议下一步

- 推送 `codex/mall-risk-fixes` 分支到远端，保留隔离修复成果。
- 下一阶段补充后端集成测试，优先覆盖订单状态流转、权限边界、参数校验和上传限制。
- 引入订单操作审计日志，记录管理员确认收款、发货、用户取消和确认收货行为。
- 优化前端构建分包，降低首屏资源体积。
