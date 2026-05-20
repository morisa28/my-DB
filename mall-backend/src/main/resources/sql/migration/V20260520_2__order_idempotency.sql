-- Add order creation idempotency table for existing databases.
-- Run once after backing up data:
--   mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260520_2__order_idempotency.sql

SET NAMES utf8mb4;

CREATE TABLE order_idempotency (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    request_id VARCHAR(64) NOT NULL,
    order_id BIGINT,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0 处理中，1 已成功',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_idempotency_user_request (user_id, request_id),
    KEY idx_order_idempotency_order (order_id),
    CONSTRAINT fk_order_idempotency_user FOREIGN KEY (user_id) REFERENCES `user`(id),
    CONSTRAINT fk_order_idempotency_order FOREIGN KEY (order_id) REFERENCES order_info(id),
    CONSTRAINT ck_order_idempotency_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单创建幂等表';
