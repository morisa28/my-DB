-- Add auditable order operation records.
-- Run once after backing up data:
--   mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260520__order_operation_log.sql

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS order_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    operator_id BIGINT NOT NULL,
    operator_username VARCHAR(50) NOT NULL,
    operator_role TINYINT NOT NULL COMMENT '0 普通用户，1 管理员',
    action VARCHAR(50) NOT NULL,
    from_status TINYINT,
    to_status TINYINT,
    remark VARCHAR(255),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_order_operation_order (order_id, create_time),
    KEY idx_order_operation_operator (operator_id, create_time),
    KEY idx_order_operation_action (action),
    CONSTRAINT fk_order_operation_order FOREIGN KEY (order_id) REFERENCES order_info(id),
    CONSTRAINT fk_order_operation_user FOREIGN KEY (operator_id) REFERENCES `user`(id),
    CONSTRAINT ck_order_operation_role CHECK (operator_role IN (0, 1)),
    CONSTRAINT ck_order_operation_from_status CHECK (from_status IS NULL OR from_status IN (0, 1, 2, 3, 4)),
    CONSTRAINT ck_order_operation_to_status CHECK (to_status IS NULL OR to_status IN (0, 1, 2, 3, 4))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单操作审计日志表';
