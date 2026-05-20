-- Add session security fields for existing databases.
-- Run once after backing up data:
--   mysql -u root -p mall_db < mall-backend/src/main/resources/sql/migration/V20260520__session_security.sql

SET NAMES utf8mb4;

ALTER TABLE `user`
    ADD COLUMN session_version INT NOT NULL DEFAULT 0 COMMENT '会话版本，变更后旧 Token 失效' AFTER status,
    ADD COLUMN last_login_time DATETIME NULL AFTER session_version,
    ADD COLUMN last_password_update_time DATETIME NULL AFTER last_login_time,
    ADD CONSTRAINT ck_user_session_version CHECK (session_version >= 0);
