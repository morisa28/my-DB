-- Add stock movement audit table for existing databases.
-- Run once after backing up data:
--   mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260520_3__stock_movement.sql

SET NAMES utf8mb4;

CREATE TABLE stock_movement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    order_id BIGINT,
    movement_type VARCHAR(32) NOT NULL,
    quantity INT NOT NULL,
    before_stock INT,
    after_stock INT,
    operator_id BIGINT,
    remark VARCHAR(255),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_stock_movement_product_time (product_id, create_time),
    KEY idx_stock_movement_order (order_id),
    KEY idx_stock_movement_operator (operator_id, create_time),
    CONSTRAINT fk_stock_movement_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT fk_stock_movement_order FOREIGN KEY (order_id) REFERENCES order_info(id),
    CONSTRAINT fk_stock_movement_operator FOREIGN KEY (operator_id) REFERENCES `user`(id),
    CONSTRAINT ck_stock_movement_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';
