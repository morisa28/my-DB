USE mall_db;

INSERT INTO `user` (id, username, password, phone, email, role, status) VALUES
(1, 'admin', '$2b$10$3wbeAJ45zZxPYxX9hflyC.c2X2RdaIPU5LOVjhsvn8PoOs4DieAyq', '13800000000', 'admin@example.com', 1, 1),
(2, 'user', '$2b$10$I2H.L0Aze3ocrGLq7zsCG.8XMHuRWm6vcAvrmDMnlQRqOPWHRtpfC', '13900000000', 'user@example.com', 0, 1),
(3, 'alice', '$2b$10$I2H.L0Aze3ocrGLq7zsCG.8XMHuRWm6vcAvrmDMnlQRqOPWHRtpfC', '13700000000', 'alice@example.com', 0, 1),
(4, 'disabled_demo', '$2b$10$I2H.L0Aze3ocrGLq7zsCG.8XMHuRWm6vcAvrmDMnlQRqOPWHRtpfC', '13500000000', 'disabled@example.com', 0, 0);

INSERT INTO category (id, name, sort_order, status) VALUES
(1, '数码电子', 1, 1),
(2, '居家生活', 2, 1),
(3, '学习办公', 3, 1),
(4, '食品饮品', 4, 1);

INSERT INTO product (id, category_id, name, price, stock, sales, image_url, description, status) VALUES
(1, 1, '蓝牙降噪耳机', 299.00, 36, 8, 'https://picsum.photos/seed/mall-earphone/640/480', '适合通勤和自习室使用的轻量降噪耳机。', 1),
(2, 1, '便携机械键盘', 239.00, 24, 5, 'https://picsum.photos/seed/mall-keyboard/640/480', '紧凑配列，适合宿舍和实验室桌面。', 1),
(3, 1, '智能手环', 169.00, 52, 4, 'https://picsum.photos/seed/mall-band/640/480', '记录运动、睡眠和课程日程提醒。', 1),
(4, 2, '护眼台灯', 129.00, 18, 7, 'https://picsum.photos/seed/mall-lamp/640/480', '三档亮度，适合夜间阅读和编程。', 1),
(5, 2, '保温杯', 59.90, 80, 13, 'https://picsum.photos/seed/mall-cup/640/480', '316 不锈钢内胆，适合课堂和图书馆。', 1),
(6, 2, '桌面收纳盒', 39.90, 65, 9, 'https://picsum.photos/seed/mall-storage/640/480', '分类整理文具、数据线和小物件。', 1),
(7, 3, '数据库课程笔记本', 19.90, 120, 15, 'https://picsum.photos/seed/mall-notebook/640/480', '方格内页，适合记录 E-R 图和 SQL。', 1),
(8, 3, 'U 盘 64GB', 49.90, 40, 6, 'https://picsum.photos/seed/mall-usb/640/480', '课程资料、报告和演示文件备份。', 1),
(9, 3, '考试资料文件夹', 12.90, 95, 11, 'https://picsum.photos/seed/mall-folder/640/480', '透明分页，整理试卷和课程讲义。', 1),
(10, 4, '挂耳咖啡', 45.00, 45, 10, 'https://picsum.photos/seed/mall-coffee/640/480', '深夜写报告时的提神补给。', 1),
(11, 4, '每日坚果', 35.00, 6, 14, 'https://picsum.photos/seed/mall-nuts/640/480', '低库存商品，用于后台库存预警展示。', 1),
(12, 4, '低糖饼干', 22.80, 72, 3, 'https://picsum.photos/seed/mall-cookie/640/480', '课间补充能量的小包装饼干。', 1);

INSERT INTO address (id, user_id, receiver_name, receiver_phone, province, city, detail_address, is_default) VALUES
(1, 2, '张同学', '13900000000', '江苏省', '南京市', '江宁区大学城 1 号宿舍楼 502', 1),
(2, 2, '张同学', '13900000000', '江苏省', '南京市', '教学楼 B 区 101', 0),
(3, 3, 'Alice', '13700000000', '上海市', '上海市', '浦东新区课程实验楼 306', 1);

INSERT INTO cart_item (id, user_id, product_id, quantity) VALUES
(1, 2, 1, 1),
(2, 2, 7, 2),
(3, 3, 10, 1);

INSERT INTO order_info (id, order_no, user_id, total_amount, status, receiver_name, receiver_phone, receiver_address, pay_time, ship_time, finish_time) VALUES
(1, 'M202605150900000001', 2, 358.90, 2, '张同学', '13900000000', '江苏省南京市江宁区大学城 1 号宿舍楼 502', '2026-05-15 09:05:00', '2026-05-15 10:10:00', NULL),
(2, 'M202605150910000002', 3, 172.90, 1, 'Alice', '13700000000', '上海市上海市浦东新区课程实验楼 306', '2026-05-15 09:15:00', NULL, NULL),
(3, 'M202605150920000003', 2, 338.80, 0, '张同学', '13900000000', '江苏省南京市教学楼 B 区 101', NULL, NULL, NULL),
(4, 'M202605150930000004', 2, 174.00, 3, '张同学', '13900000000', '江苏省南京市江宁区大学城 1 号宿舍楼 502', '2026-05-15 09:35:00', '2026-05-15 10:20:00', '2026-05-15 12:00:00'),
(5, 'M202605150940000005', 3, 35.00, 4, 'Alice', '13700000000', '上海市上海市浦东新区课程实验楼 306', NULL, NULL, NULL);

INSERT INTO order_item (order_id, product_id, product_name, product_price, quantity, total_price, product_image) VALUES
(1, 2, '便携机械键盘', 239.00, 1, 239.00, 'https://picsum.photos/seed/mall-keyboard/640/480'),
(1, 5, '保温杯', 59.90, 2, 119.80, 'https://picsum.photos/seed/mall-cup/640/480'),
(2, 10, '挂耳咖啡', 45.00, 2, 90.00, 'https://picsum.photos/seed/mall-coffee/640/480'),
(2, 11, '每日坚果', 35.00, 2, 70.00, 'https://picsum.photos/seed/mall-nuts/640/480'),
(2, 9, '考试资料文件夹', 12.90, 1, 12.90, 'https://picsum.photos/seed/mall-folder/640/480'),
(3, 1, '蓝牙降噪耳机', 299.00, 1, 299.00, 'https://picsum.photos/seed/mall-earphone/640/480'),
(3, 7, '数据库课程笔记本', 19.90, 2, 39.80, 'https://picsum.photos/seed/mall-notebook/640/480'),
(4, 4, '护眼台灯', 129.00, 1, 129.00, 'https://picsum.photos/seed/mall-lamp/640/480'),
(4, 10, '挂耳咖啡', 45.00, 1, 45.00, 'https://picsum.photos/seed/mall-coffee/640/480'),
(5, 11, '每日坚果', 35.00, 1, 35.00, 'https://picsum.photos/seed/mall-nuts/640/480');
