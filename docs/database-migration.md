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
mall-backend/src/main/resources/sql/migration/V20260520_00__schema_migration_history.sql
mall-backend/src/main/resources/sql/migration/V20260520__session_security.sql
mall-backend/src/main/resources/sql/migration/V20260520__order_operation_log.sql
mall-backend/src/main/resources/sql/migration/V20260520_2__order_idempotency.sql
mall-backend/src/main/resources/sql/migration/V20260520_02__admin_operation_log.sql
mall-backend/src/main/resources/sql/migration/V20260520_3__stock_movement.sql
mall-backend/src/main/resources/sql/migration/V20260520_4__payment_models.sql
mall-backend/src/main/resources/sql/migration/V20260520_5__refund_models.sql
```

执行示例：

```bash
mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260519__practical_order_workflow.sql
mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260520_00__schema_migration_history.sql
mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260520__session_security.sql
mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260520__order_operation_log.sql
mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260520_2__order_idempotency.sql
mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260520_02__admin_operation_log.sql
mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260520_3__stock_movement.sql
mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260520_4__payment_models.sql
mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260520_5__refund_models.sql
```

`V20260520_00` 之后的脚本会向 `schema_migration_history` 写入执行记录。执行完成后可检查：

```sql
SELECT version, description, script_name, executed_at
FROM schema_migration_history
ORDER BY executed_at;
```

## 备份建议

执行任何 migration 前先备份数据库和上传目录：

```bash
bash scripts/backup-db.sh
bash scripts/backup-uploads.sh
```

恢复演练优先恢复到临时库：

```bash
RESTORE_DATABASE=mall_db_restore bash scripts/restore-db.sh backups/db/<backup-file>.sql.gz
```

详细流程见 `docs/backup-and-restore.md`。

## 开发约定

- `schema.sql` 保存最新全量结构，用于新环境和演示环境。
- `data.sql` 保存演示种子数据，不应包含真实用户隐私或密钥。
- `migration/` 保存增量升级脚本，文件名格式建议：`VYYYYMMDD__description.sql`。
- 新增 migration 时应同步写入 `schema_migration_history` 记录，便于服务器手动迁移追踪。
- 修改表结构后必须同步更新 `docs/03-数据库设计.md` 和 `docs/05-测试报告.md`。
- 服务器已有数据时，禁止执行 `docker compose down -v` 来“升级数据库”。
- 生产环境恢复前必须先恢复到临时库验证，不要直接覆盖正式库。
