-- Add admin operation audit records.
-- Run once after backing up data:
--   mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260520_02__admin_operation_log.sql

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS schema_migration_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version VARCHAR(64) NOT NULL,
    description VARCHAR(200) NOT NULL,
    script_name VARCHAR(200) NOT NULL,
    executed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_schema_migration_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据库增量脚本执行记录表';

CREATE TABLE IF NOT EXISTS admin_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_id BIGINT NOT NULL,
    operator_username VARCHAR(50) NOT NULL,
    module VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    target_id BIGINT,
    target_name VARCHAR(100),
    remark VARCHAR(255),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_admin_operation_time (create_time),
    KEY idx_admin_operation_operator (operator_id, create_time),
    KEY idx_admin_operation_module_action (module, action, create_time),
    CONSTRAINT fk_admin_operation_user FOREIGN KEY (operator_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台管理操作审计日志表';

INSERT INTO schema_migration_history (version, description, script_name)
VALUES ('20260520_02', 'admin operation audit log', 'V20260520_02__admin_operation_log.sql')
ON DUPLICATE KEY UPDATE version = version;
