SET NAMES utf8mb4;

DROP DATABASE IF EXISTS mall_db;
CREATE DATABASE mall_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mall_db;

CREATE TABLE `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    role TINYINT NOT NULL DEFAULT 0 COMMENT '0 普通用户，1 管理员',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0 禁用，1 正常',
    session_version INT NOT NULL DEFAULT 0 COMMENT '会话版本，变更后旧 Token 失效',
    last_login_time DATETIME,
    last_password_update_time DATETIME,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_username (username),
    CONSTRAINT ck_user_role CHECK (role IN (0, 1)),
    CONSTRAINT ck_user_status CHECK (status IN (0, 1)),
    CONSTRAINT ck_user_session_version CHECK (session_version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_category_status_sort (status, sort_order),
    CONSTRAINT ck_category_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    sales INT NOT NULL DEFAULT 0,
    image_url VARCHAR(255),
    description TEXT,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_product_category (category_id),
    KEY idx_product_name (name),
    KEY idx_product_status (status),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT ck_product_price CHECK (price >= 0),
    CONSTRAINT ck_product_stock CHECK (stock >= 0),
    CONSTRAINT ck_product_sales CHECK (sales >= 0),
    CONSTRAINT ck_product_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE cart_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cart_user_product (user_id, product_id),
    KEY idx_cart_user (user_id),
    KEY idx_cart_product (product_id),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES `user`(id),
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT ck_cart_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

CREATE TABLE address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    province VARCHAR(50),
    city VARCHAR(50),
    detail_address VARCHAR(255) NOT NULL,
    is_default TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_address_user (user_id),
    KEY idx_address_user_default (user_id, is_default),
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES `user`(id),
    CONSTRAINT ck_address_default CHECK (is_default IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

CREATE TABLE order_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0 待支付，1 已支付待发货，2 已发货，3 已完成，4 已取消',
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    receiver_address VARCHAR(255) NOT NULL,
    payment_note VARCHAR(255) COMMENT '用户付款备注，例如付款渠道、转账尾号或流水号',
    admin_remark VARCHAR(255) COMMENT '管理员收款、发货或售后备注',
    shipping_no VARCHAR(64) COMMENT '物流单号或配送编号',
    pay_time DATETIME,
    ship_time DATETIME,
    finish_time DATETIME,
    cancel_time DATETIME,
    confirm_time DATETIME,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_order_user (user_id),
    KEY idx_order_status (status),
    KEY idx_order_create_time (create_time),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES `user`(id),
    CONSTRAINT ck_order_amount CHECK (total_amount >= 0),
    CONSTRAINT ck_order_status CHECK (status IN (0, 1, 2, 3, 4))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

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

CREATE TABLE refund_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    refund_no VARCHAR(64) NOT NULL,
    order_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    payment_no VARCHAR(64),
    apply_type TINYINT NOT NULL DEFAULT 0 COMMENT '0仅退款 1退货退款',
    refund_amount DECIMAL(10,2) NOT NULL,
    reason VARCHAR(255),
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0申请中 1审核通过 2审核拒绝 3退款中 4退款成功 5退款失败 6已取消',
    refund_channel VARCHAR(32),
    third_party_refund_no VARCHAR(128),
    stock_restore_required TINYINT NOT NULL DEFAULT 0 COMMENT '0不需要 1需要恢复库存',
    stock_restored TINYINT NOT NULL DEFAULT 0 COMMENT '0未恢复 1已恢复',
    admin_remark VARCHAR(255),
    apply_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    audit_time DATETIME,
    refund_time DATETIME,
    finish_time DATETIME,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_refund_no (refund_no),
    KEY idx_refund_order_id (order_id),
    KEY idx_refund_order_no (order_no),
    KEY idx_refund_user_status (user_id, status),
    KEY idx_refund_payment_no (payment_no),
    CONSTRAINT fk_refund_order_order FOREIGN KEY (order_id) REFERENCES order_info(id),
    CONSTRAINT fk_refund_order_user FOREIGN KEY (user_id) REFERENCES `user`(id),
    CONSTRAINT fk_refund_order_payment FOREIGN KEY (payment_no) REFERENCES payment_order(payment_no),
    CONSTRAINT ck_refund_apply_type CHECK (apply_type IN (0, 1)),
    CONSTRAINT ck_refund_amount CHECK (refund_amount > 0),
    CONSTRAINT ck_refund_status CHECK (status IN (0, 1, 2, 3, 4, 5, 6)),
    CONSTRAINT ck_refund_stock_required CHECK (stock_restore_required IN (0, 1)),
    CONSTRAINT ck_refund_stock_restored CHECK (stock_restored IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款售后单表';

CREATE TABLE refund_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    refund_id BIGINT NOT NULL,
    refund_no VARCHAR(64) NOT NULL,
    operator_id BIGINT,
    operator_role VARCHAR(32) NOT NULL,
    from_status TINYINT,
    to_status TINYINT NOT NULL,
    action VARCHAR(64) NOT NULL,
    remark VARCHAR(255),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_refund_log_refund_id (refund_id),
    KEY idx_refund_log_refund_no (refund_no),
    KEY idx_refund_log_operator (operator_id, create_time),
    CONSTRAINT fk_refund_log_refund FOREIGN KEY (refund_id) REFERENCES refund_order(id),
    CONSTRAINT fk_refund_log_operator FOREIGN KEY (operator_id) REFERENCES `user`(id),
    CONSTRAINT ck_refund_log_operator_role CHECK (operator_role IN ('USER', 'ADMIN', 'SYSTEM')),
    CONSTRAINT ck_refund_log_from_status CHECK (from_status IS NULL OR from_status IN (0, 1, 2, 3, 4, 5, 6)),
    CONSTRAINT ck_refund_log_to_status CHECK (to_status IN (0, 1, 2, 3, 4, 5, 6))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款售后操作日志表';

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

CREATE TABLE order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    product_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    product_image VARCHAR(255),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_order_item_order (order_id),
    KEY idx_order_item_product (product_id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES order_info(id),
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT ck_order_item_price CHECK (product_price >= 0),
    CONSTRAINT ck_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT ck_order_item_total CHECK (total_price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';
