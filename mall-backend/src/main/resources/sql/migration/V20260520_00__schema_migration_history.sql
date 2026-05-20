-- Add lightweight migration execution history.
-- Run once after backing up data:
--   mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260520_00__schema_migration_history.sql

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS schema_migration_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version VARCHAR(64) NOT NULL,
    description VARCHAR(200) NOT NULL,
    script_name VARCHAR(200) NOT NULL,
    executed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_schema_migration_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据库增量脚本执行记录表';

INSERT INTO schema_migration_history (version, description, script_name)
VALUES ('20260520_00', 'schema migration history', 'V20260520_00__schema_migration_history.sql')
ON DUPLICATE KEY UPDATE version = version;
