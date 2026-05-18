-- Upgrade existing demo databases for the practical order workflow.
-- Run once after backing up data:
--   mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260519__practical_order_workflow.sql

SET NAMES utf8mb4;

ALTER TABLE order_info
    ADD COLUMN payment_note VARCHAR(255) NULL COMMENT '用户付款备注，例如付款渠道、转账尾号或流水号' AFTER receiver_address,
    ADD COLUMN admin_remark VARCHAR(255) NULL COMMENT '管理员收款、发货或售后备注' AFTER payment_note,
    ADD COLUMN shipping_no VARCHAR(64) NULL COMMENT '物流单号或配送编号' AFTER admin_remark,
    ADD COLUMN cancel_time DATETIME NULL AFTER finish_time,
    ADD COLUMN confirm_time DATETIME NULL AFTER cancel_time;

UPDATE order_info
SET confirm_time = finish_time
WHERE status = 3 AND finish_time IS NOT NULL AND confirm_time IS NULL;
