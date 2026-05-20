# 数据库初始化与迁移说明

## 适用范围

本文档说明本项目在部署到服务器前如何管理 MySQL 数据库结构。当前项目暂不强制引入 Flyway，采用“全量初始化 SQL + 增量 migration SQL”的轻量方案。

## 新环境全量初始化

新环境或演示环境首次启动时执行：

```bash
mysql -u root -p < mall-backend/src/main/resources/sql/schema.sql
mysql -u root -p mall_db < mall-backend/src/main/resources/sql/data.sql
```

Docker Compose 首次创建 `mall-mysql-data` 数据卷时，会自动执行：

```text
mall-backend/src/main/resources/sql/schema.sql
mall-backend/src/main/resources/sql/data.sql
```

## 已有环境增量升级

已有数据库不能直接重新执行 `schema.sql`，否则会删除库并重建。上线后如需升级结构，应先备份，再按顺序执行 `migration/` 目录下的增量脚本。

当前增量脚本：

```text
mall-backend/src/main/resources/sql/migration/V20260519__practical_order_workflow.sql
mall-backend/src/main/resources/sql/migration/V20260520__session_security.sql
mall-backend/src/main/resources/sql/migration/V20260520_2__order_idempotency.sql
mall-backend/src/main/resources/sql/migration/V20260520_3__stock_movement.sql
```

执行示例：

```bash
mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260519__practical_order_workflow.sql
mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260520__session_security.sql
mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260520_2__order_idempotency.sql
mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260520_3__stock_movement.sql
```

## 备份建议

执行任何 migration 前先备份：

```bash
mysqldump -u root -p --single-transaction --routines --triggers mall_db > mall_db_backup.sql
```

恢复示例：

```bash
mysql -u root -p mall_db < mall_db_backup.sql
```

## 开发约定

- `schema.sql` 保存最新全量结构，用于新环境和演示环境。
- `data.sql` 保存演示种子数据，不应包含真实用户隐私或密钥。
- `migration/` 保存增量升级脚本，文件名格式建议：`VYYYYMMDD__description.sql`。
- 修改表结构后必须同步更新 `docs/03-数据库设计.md` 和 `docs/05-测试报告.md`。
- 服务器已有数据时，禁止执行 `docker compose down -v` 来“升级数据库”。
