-- Add payment order and payment callback log tables for existing databases.
-- Run once after backing up data:
--   mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260520_4__payment_models.sql

SET NAMES utf8mb4;

CREATE TABLE payment_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    payment_no VARCHAR(64) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付 1成功 2失败 3关闭 4退款中 5已退款',
    paid_time DATETIME,
    third_party_trade_no VARCHAR(128),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_payment_no (payment_no),
    KEY idx_payment_order_id (order_id),
    KEY idx_payment_order_no (order_no),
    KEY idx_payment_channel_status (channel, status),
    CONSTRAINT fk_payment_order_order FOREIGN KEY (order_id) REFERENCES order_info(id),
    CONSTRAINT ck_payment_amount CHECK (amount >= 0),
    CONSTRAINT ck_payment_status CHECK (status IN (0, 1, 2, 3, 4, 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付单表';

CREATE TABLE payment_callback_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_no VARCHAR(64) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    event_type VARCHAR(64),
    raw_payload TEXT,
    verify_result TINYINT NOT NULL DEFAULT 0,
    process_result TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_callback_payment_no (payment_no),
    KEY idx_callback_channel_event (channel, event_type),
    CONSTRAINT ck_callback_verify_result CHECK (verify_result IN (0, 1)),
    CONSTRAINT ck_callback_process_result CHECK (process_result IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付回调日志表';
