# 数据库与上传文件备份恢复

## 适用范围

本文档用于本地演示环境、服务器内测环境和生产环境的备份恢复演练。备份脚本默认读取项目根目录 `.env`，也可以通过 `ENV_FILE` 指定其他环境文件。

生产环境操作前必须确认：

- 已进入正确服务器和正确项目目录。
- `.env` 中的数据库账号、库名和上传目录是目标环境。
- 恢复前已经完成一次新的数据库备份。
- 不把备份文件提交到 Git。

## 备份数据库

Docker Compose 服务运行时，脚本优先进入 `mysql` 服务执行 `mysqldump`：

```bash
bash scripts/backup-db.sh
```

指定环境文件：

```bash
ENV_FILE=.env.prod bash scripts/backup-db.sh
```

备份文件默认输出到：

```text
backups/db/
```

可以通过环境变量调整：

```bash
BACKUP_DIR=/data/mall-backups/db RETENTION_DAYS=30 bash scripts/backup-db.sh
```

如果 Docker Compose 服务未运行，脚本会尝试使用本机 `mysqldump`，读取 `DB_HOST`、`MYSQL_PORT`、`DB_USERNAME`、`DB_PASSWORD` 和 `MYSQL_DATABASE`。

## 备份上传目录

Docker Compose 后端服务运行时，脚本优先进入 `backend` 服务打包 `UPLOAD_DIR`：

```bash
bash scripts/backup-uploads.sh
```

备份文件默认输出到：

```text
backups/uploads/
```

如果后端容器未运行，但上传目录在本机可见，可以指定：

```bash
UPLOAD_SOURCE_DIR=/data/mall/uploads bash scripts/backup-uploads.sh
```

## 恢复数据库到临时库

恢复脚本默认不会覆盖正式库，而是恢复到 `${MYSQL_DATABASE}_restore`，用于验证备份是否可用：

```bash
bash scripts/restore-db.sh backups/db/mall_db_20260520_120000.sql.gz
```

指定临时库名：

```bash
RESTORE_DATABASE=mall_db_restore_20260520 bash scripts/restore-db.sh backups/db/mall_db_20260520_120000.sql.gz
```

恢复完成后可以连接数据库检查核心表：

```sql
USE mall_db_restore;
SHOW TABLES;
SELECT COUNT(*) FROM user;
SELECT COUNT(*) FROM order_info;
```

## 覆盖正式库的限制

恢复脚本会拒绝直接覆盖 `MYSQL_DATABASE`。只有在确认已经备份、确认目标正确、确认可以覆盖时，才允许显式执行：

```bash
RESTORE_DATABASE=mall_db CONFIRM_RESTORE=I_UNDERSTAND bash scripts/restore-db.sh backups/db/mall_db_20260520_120000.sql.gz
```

生产环境不建议直接覆盖正式库。更安全的流程是：

1. 先恢复到临时库。
2. 验证表结构和关键数据。
3. 停止业务写入。
4. 再由管理员执行正式恢复或数据迁移。

## 不能在生产环境执行的操作

- 不要执行 `docker compose down -v` 清空生产数据卷。
- 不要重新执行包含 `DROP DATABASE` 的 `schema.sql`。
- 不要把 `backups/` 目录、数据库导出文件或上传文件包提交到 Git。
- 不要把真实 `.env`、数据库密码或服务器地址写入文档和提交信息。

## 恢复演练验收

建议每次上线前至少做一次：

```bash
bash scripts/backup-db.sh
bash scripts/backup-uploads.sh
RESTORE_DATABASE=mall_db_restore bash scripts/restore-db.sh backups/db/<backup-file>.sql.gz
```

验收标准：

- 数据库备份文件成功生成。
- 上传文件备份包成功生成。
- 数据库备份可以恢复到临时库。
- 临时库中 `user`、`product`、`order_info`、`order_item` 等核心表可查询。
