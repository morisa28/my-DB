SET NAMES utf8mb4;

USE mall_db;

INSERT INTO `user` (id, username, password, phone, email, role, status) VALUES
(1, 'admin', '$2b$10$3wbeAJ45zZxPYxX9hflyC.c2X2RdaIPU5LOVjhsvn8PoOs4DieAyq', '13800000000', 'admin@example.com', 1, 1),
(2, 'user', '$2b$10$I2H.L0Aze3ocrGLq7zsCG.8XMHuRWm6vcAvrmDMnlQRqOPWHRtpfC', '13900000000', 'user@example.com', 0, 1),
(3, 'alice', '$2b$10$I2H.L0Aze3ocrGLq7zsCG.8XMHuRWm6vcAvrmDMnlQRqOPWHRtpfC', '13700000000', 'alice@example.com', 0, 1),
(4, 'disabled_demo', '$2b$10$I2H.L0Aze3ocrGLq7zsCG.8XMHuRWm6vcAvrmDMnlQRqOPWHRtpfC', '13500000000', 'disabled@example.com', 0, 0);

INSERT INTO category (id, name, sort_order, status) VALUES
(1, '数码家电', 1, 1),
(2, '家居日用', 2, 1),
(3, '食品饮料', 3, 1),
(4, '个护清洁', 4, 1),
(5, '美妆护肤', 5, 1),
(6, '服饰鞋包', 6, 1),
(7, '母婴宠物', 7, 1),
(8, '运动户外', 8, 1),
(9, '图书文具', 9, 1),
(10, '厨房餐厨', 10, 1);

INSERT INTO product (id, category_id, name, price, stock, sales, image_url, description, status) VALUES
(1, 1, '无线降噪蓝牙耳机', 299.00, 36, 8, '/images/products/001-wireless-noise-cancelling-earbuds.webp', '入耳式无线蓝牙耳机，支持主动降噪和通透模式，单次续航约 7 小时，充电盒可多次补电。适合通勤、居家办公和运动散步使用，硅胶耳帽贴合耳道，语音通话更清晰。', 1),
(2, 1, '便携机械键盘', 239.00, 24, 5, '/images/products/002-portable-mechanical-keyboard.webp', '紧凑配列机械键盘，保留常用功能键，支持多设备切换和可拆卸连接线。键帽触感干爽，回弹明确，适合桌面办公、轻度游戏和移动办公场景。', 1),
(3, 1, '智能运动手环', 169.00, 52, 4, '/images/products/003-smart-fitness-band.webp', '轻量智能手环，支持心率、睡眠、步数和运动模式记录，彩色屏幕可查看消息提醒。表带亲肤耐汗，日常佩戴负担低，适合健康管理和运动打卡。', 1),
(4, 1, '护眼阅读台灯', 129.00, 18, 7, '/images/products/004-eye-care-reading-lamp.webp', '桌面护眼台灯，提供多档亮度和色温调节，灯臂可按阅读、书写和电脑办公角度调整。光线柔和不刺眼，适合卧室书桌、儿童学习桌和夜间阅读。', 1),
(5, 1, '智能显温保温杯', 59.90, 80, 13, '/images/products/005-smart-temperature-thermos.webp', '便携保温杯，杯盖带触控温度显示，内胆采用食品接触级不锈钢，冷热饮均可长时间保温。杯身防滑易握，适合通勤、会议、旅行和日常饮水。', 1),
(6, 1, '桌面无线充电器', 89.00, 65, 9, '/images/products/006-desktop-wireless-charger.webp', '立式桌面无线充电器，手机竖放横放均可充电，底座带防滑垫并预留散热空间。适合放在床头、办公桌或玄关，随手补电并保持桌面整洁。', 1),
(7, 1, 'USB-C 快充数据线', 19.90, 120, 15, '/images/products/007-usb-c-fast-charging-cable.webp', '加粗线芯 USB-C 数据线，支持日常快充和稳定传输，接口处做加固防折弯处理。线材柔韧不易缠绕，适合手机、平板、移动电源和车载充电使用。', 1),
(8, 1, '高速 U 盘 64GB', 49.90, 40, 6, '/images/products/008-high-speed-64gb-usb-drive.webp', '64GB 高速 U 盘，金属外壳耐磨便携，适合保存文档、照片、课件和常用安装包。推拉式接口减少丢盖烦恼，钥匙孔设计方便随身携带。', 1),
(9, 1, '便携手机支架', 12.90, 95, 11, '/images/products/009-foldable-phone-stand.webp', '可折叠手机支架，支持多角度调节，底部硅胶防滑，放置手机或小尺寸平板更稳定。适合追剧、视频会议、厨房看菜谱和桌面充电。', 1),
(10, 1, '迷你蓝牙音箱', 45.00, 45, 10, '/images/products/010-mini-bluetooth-speaker.webp', '小体积蓝牙音箱，支持无线连接和便携挂绳，声音清亮，低音适中。适合卧室、厨房、露台和短途出行使用，日常听音乐和播客更方便。', 1),
(11, 1, '桌面空气循环扇', 35.00, 6, 14, '/images/products/011-desktop-air-circulation-fan.webp', '桌面小风扇，支持多档风量和上下角度调节，低噪运行不打扰办公休息。机身轻巧，适合书桌、床头和厨房台面使用，低库存便于后台预警展示。', 1),
(12, 1, '智能定时插座', 39.90, 72, 3, '/images/products/012-smart-timer-plug.webp', '智能定时插座，可为台灯、风扇、加湿器等小家电设置定时开关，支持过载保护。外壳阻燃耐热，适合日常节能管理和居家电器自动化。', 1),
(13, 1, '家用空气净化器', 699.00, 22, 5, '/images/products/013-home-air-purifier.webp', '家用空气净化器，适合卧室和客厅使用，支持多档风量与滤芯寿命提醒。简洁机身便于融入家居环境，适合换季、宠物家庭和新家具入户后的空气管理。', 1),
(14, 1, '手持蒸汽挂烫机', 159.00, 34, 8, '/images/products/014-handheld-garment-steamer.webp', '手持蒸汽挂烫机，预热快，适合衬衫、连衣裙、外套和窗帘局部熨烫。水箱可拆卸加水，机身握持轻便，出差和日常衣物护理都能使用。', 1),
(15, 1, '家用迷你投影仪', 899.00, 16, 2, '/images/products/015-compact-home-projector.webp', '家用迷你投影仪，适合卧室观影和客厅临时投屏，支持常见视频设备连接。机身小巧易收纳，画面亮度适合夜间环境，搭配幕布或白墙即可使用。', 1),
(16, 1, '车载无线吸尘器', 199.00, 28, 6, '/images/products/016-cordless-car-vacuum.webp', '无线手持吸尘器，配备缝隙吸头和毛刷吸头，适合清理车内座椅、脚垫、沙发缝隙和桌面碎屑。可水洗滤芯便于维护，车家两用更灵活。', 1),
(17, 2, '纯棉四件套床品', 259.00, 38, 9, '/images/products/017-cotton-bedding-set.webp', '纯棉床品四件套，包含被套、床单和枕套，面料柔软透气，适合四季日常使用。简洁配色便于搭配卧室风格，亲肤触感适合敏感肌人群。', 1),
(18, 2, '记忆棉护颈枕', 129.00, 42, 7, '/images/products/018-memory-foam-neck-pillow.webp', '慢回弹记忆棉枕，弧形承托颈部和肩部，枕套可拆洗。适合仰睡和侧睡人群，帮助保持自然睡姿，提升卧室睡眠舒适度。', 1),
(19, 2, '加厚吸水浴巾套装', 89.00, 55, 12, '/images/products/019-thick-absorbent-towel-set.webp', '加厚浴巾套装，包含大浴巾和面巾，织物蓬松吸水，触感柔软。适合家庭浴室、健身后擦拭和旅行备用，边缘包边更耐洗。', 1),
(20, 2, '抽屉分格收纳盒', 39.90, 88, 16, '/images/products/020-drawer-organizer-boxes.webp', '多规格抽屉分格收纳盒，可分类放置袜子、内衣、化妆小物、数据线和办公用品。盒体轻便可组合，帮助衣柜、书桌和梳妆台保持整齐。', 1),
(21, 2, '免打孔挂钩组合', 19.90, 110, 20, '/images/products/021-adhesive-wall-hooks-set.webp', '免打孔挂钩组合，背胶粘贴牢固，适合瓷砖、玻璃和光滑柜门表面。可挂钥匙、毛巾、厨房工具和小包，不破坏墙面，租房家庭也方便使用。', 1),
(22, 2, '可折叠脏衣篮', 49.90, 63, 10, '/images/products/022-foldable-laundry-basket.webp', '可折叠脏衣篮，容量适合日常换洗衣物，侧边提手便于搬运。不用时可压扁收纳，适合浴室、阳台、衣帽间和小户型空间。', 1),
(23, 2, '衣柜防潮除湿盒', 16.90, 96, 18, '/images/products/023-wardrobe-dehumidifier-box.webp', '衣柜除湿盒，适合放在衣柜、鞋柜、储物间和卫生间角落，帮助吸收潮气并减少异味。透明盒体可观察吸湿状态，换季收纳更安心。', 1),
(24, 2, '北欧陶瓷花瓶', 59.90, 47, 6, '/images/products/024-nordic-ceramic-vase.webp', '简约陶瓷花瓶，哑光釉面和柔和线条适合客厅、餐桌、玄关和书房陈列。可搭配鲜花、干花或单独摆放，为家居空间增加装饰层次。', 1),
(25, 2, '客厅短绒地垫', 79.00, 50, 9, '/images/products/025-soft-living-room-rug.webp', '短绒地垫，脚感柔软，底部防滑，适合客厅茶几、床边和儿童活动区域。低绒面更易清理，日常吸尘即可保持整洁。', 1),
(26, 2, '防滑衣架套装', 29.90, 130, 24, '/images/products/026-non-slip-hanger-set.webp', '防滑衣架套装，肩部弧线贴合衣物版型，可减少衣肩鼓包。表面细纹增加摩擦力，适合挂衬衫、外套、连衣裙和轻薄针织衫。', 1),
(27, 2, '桌面纸巾收纳盒', 24.90, 76, 13, '/images/products/027-desktop-tissue-storage-box.webp', '桌面纸巾收纳盒，顶部抽纸顺畅，侧边可放遥控器、笔或小物。适合客厅茶几、餐桌、床头柜和办公桌，减少零散物品堆放。', 1),
(28, 2, '无火藤条香薰', 46.00, 58, 8, '/images/products/028-reed-diffuser-set.webp', '无火藤条香薰，扩香自然柔和，不需要明火或插电。适合卧室、玄关、卫生间和书房使用，玻璃瓶外观简洁，可同时作为小型装饰。', 1),
(29, 2, '可水洗沙发盖毯', 99.00, 44, 7, '/images/products/029-washable-sofa-throw.webp', '可水洗沙发盖毯，触感柔软，适合盖在沙发、休闲椅或床尾。既能防尘防磨，也能在午休或观影时保暖，机洗后晾干即可重复使用。', 1),
(30, 2, '加厚抽绳垃圾袋', 18.90, 140, 30, '/images/products/030-drawstring-trash-bags.webp', '加厚抽绳垃圾袋，袋身韧性好不易破，抽绳封口便于提拿和防止异味外溢。适合厨房、卫生间、卧室和办公室垃圾桶日常使用。', 1);

INSERT INTO address (id, user_id, receiver_name, receiver_phone, province, city, detail_address, is_default) VALUES
(1, 2, '张同学', '13900000000', '江苏省', '南京市', '江宁区大学城 1 号宿舍楼 502', 1),
(2, 2, '张同学', '13900000000', '江苏省', '南京市', '教学楼 B 区 101', 0),
(3, 3, 'Alice', '13700000000', '上海市', '上海市', '浦东新区课程实验楼 306', 1);

INSERT INTO cart_item (id, user_id, product_id, quantity) VALUES
(1, 2, 1, 1),
(2, 2, 7, 2),
(3, 3, 10, 1);

INSERT INTO order_info (id, order_no, user_id, total_amount, status, receiver_name, receiver_phone, receiver_address, payment_note, admin_remark, shipping_no, pay_time, ship_time, finish_time, cancel_time, confirm_time) VALUES
(1, 'M202605150900000001', 2, 358.80, 2, '张同学', '13900000000', '江苏省南京市江宁区大学城 1 号宿舍楼 502', '微信转账尾号 1024', '已确认收款', 'SF202605150001', '2026-05-15 09:05:00', '2026-05-15 10:10:00', NULL, NULL, NULL),
(2, 'M202605150910000002', 3, 172.90, 1, 'Alice', '13700000000', '上海市上海市浦东新区课程实验楼 306', '支付宝转账尾号 2048', '已确认收款，待打包', NULL, '2026-05-15 09:15:00', NULL, NULL, NULL, NULL),
(3, 'M202605150920000003', 2, 338.80, 0, '张同学', '13900000000', '江苏省南京市教学楼 B 区 101', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(4, 'M202605150930000004', 2, 174.00, 3, '张同学', '13900000000', '江苏省南京市江宁区大学城 1 号宿舍楼 502', '线下现金支付', '已完成', 'ZTO202605150004', '2026-05-15 09:35:00', '2026-05-15 10:20:00', '2026-05-15 12:00:00', NULL, '2026-05-15 12:00:00'),
(5, 'M202605150940000005', 3, 35.00, 4, 'Alice', '13700000000', '上海市上海市浦东新区课程实验楼 306', NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15 09:50:00', NULL);

INSERT INTO order_item (order_id, product_id, product_name, product_price, quantity, total_price, product_image) VALUES
(1, 2, '便携机械键盘', 239.00, 1, 239.00, '/images/products/002-portable-mechanical-keyboard.webp'),
(1, 5, '智能显温保温杯', 59.90, 2, 119.80, '/images/products/005-smart-temperature-thermos.webp'),
(2, 10, '迷你蓝牙音箱', 45.00, 2, 90.00, '/images/products/010-mini-bluetooth-speaker.webp'),
(2, 11, '桌面空气循环扇', 35.00, 2, 70.00, '/images/products/011-desktop-air-circulation-fan.webp'),
(2, 9, '便携手机支架', 12.90, 1, 12.90, '/images/products/009-foldable-phone-stand.webp'),
(3, 1, '无线降噪蓝牙耳机', 299.00, 1, 299.00, '/images/products/001-wireless-noise-cancelling-earbuds.webp'),
(3, 7, 'USB-C 快充数据线', 19.90, 2, 39.80, '/images/products/007-usb-c-fast-charging-cable.webp'),
(4, 4, '护眼阅读台灯', 129.00, 1, 129.00, '/images/products/004-eye-care-reading-lamp.webp'),
(4, 10, '迷你蓝牙音箱', 45.00, 1, 45.00, '/images/products/010-mini-bluetooth-speaker.webp'),
(5, 11, '桌面空气循环扇', 35.00, 1, 35.00, '/images/products/011-desktop-air-circulation-fan.webp');
