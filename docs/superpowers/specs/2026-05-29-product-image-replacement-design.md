# 当前 12 个商品图片替换设计

## 背景

当前初始化数据位于 `mall-backend/src/main/resources/sql/data.sql`，包含 4 个品类和 12 个商品。商品图和历史订单快照图仍使用 `https://picsum.photos/...` 占位图，和商品名称、描述没有稳定对应关系，影响课程演示效果。

## 目标

- 只处理当前 `data.sql` 中已有的 12 个商品。
- 不改商品 ID、名称、分类、价格、库存、销量、描述、订单、购物车或业务逻辑。
- 为每个商品按名称和描述生成一张匹配的本地商品图。
- 替换 `product.image_url` 和已有 `order_item.product_image` 中对应商品的旧占位图。
- 最终项目中不再出现 `picsum.photos` 商品图引用。

## 图片风格

采用“干净商品摄影”风格：

- 正方形构图，商品主体居中且完整可见。
- 浅色干净背景，最多使用少量弱化道具，不使用复杂生活场景。
- 适合商品列表卡片、商品详情页和后台表格缩略图。
- 不出现文字、水印、品牌标识、真实商标、人物或手部。
- 不使用包装上的可读文字。

## 输出位置与命名

图片保存到：

```text
mall-frontend/public/images/products/
```

图片路径写入 SQL，格式为：

```text
/images/products/<三位商品ID>-<英文slug>.webp
```

示例：

```text
/images/products/001-bluetooth-noise-cancelling-earbuds.webp
```

## 商品与图片映射

| ID | 商品名 | 图片路径 |
|---:|---|---|
| 1 | 蓝牙降噪耳机 | `/images/products/001-bluetooth-noise-cancelling-earbuds.webp` |
| 2 | 便携机械键盘 | `/images/products/002-portable-mechanical-keyboard.webp` |
| 3 | 智能手环 | `/images/products/003-smart-fitness-band.webp` |
| 4 | 护眼台灯 | `/images/products/004-eye-care-desk-lamp.webp` |
| 5 | 保温杯 | `/images/products/005-insulated-travel-tumbler.webp` |
| 6 | 桌面收纳盒 | `/images/products/006-desktop-organizer-box.webp` |
| 7 | 数据库课程笔记本 | `/images/products/007-database-course-notebook.webp` |
| 8 | U 盘 64GB | `/images/products/008-64gb-usb-flash-drive.webp` |
| 9 | 考试资料文件夹 | `/images/products/009-exam-document-folder.webp` |
| 10 | 挂耳咖啡 | `/images/products/010-drip-bag-coffee.webp` |
| 11 | 每日坚果 | `/images/products/011-daily-mixed-nuts.webp` |
| 12 | 低糖饼干 | `/images/products/012-low-sugar-biscuits.webp` |

## 实现范围

需要修改：

- `mall-backend/src/main/resources/sql/data.sql`
- 新增 `mall-frontend/public/images/products/*.webp`
- 新增 `docs/product-images/12-product-images.md`，记录商品、图片路径和生成提示词摘要

不修改：

- 后端 Java 业务代码
- 前端组件代码
- 数据库表结构
- 商品业务数据字段，除图片 URL 外

## 验证

实施后至少执行：

- 检查 `data.sql` 中不再包含 `picsum.photos`。
- 检查 12 个 `product.image_url` 均指向 `/images/products/*.webp`。
- 检查所有 SQL 引用的本地图片文件存在。
- 检查已有订单明细快照图同步替换为对应本地图片。
- 在可行时运行 `npm run build` 验证前端静态资源可打包。

## 风险与处理

- 图片生成可能出现文字、水印、品牌或不匹配主体：逐张检查，不合格的单张重生成。
- 生成图片默认不直接落在项目目录：先生成，再移动到 `mall-frontend/public/images/products/`。
- 只替换当前 12 个商品，不恢复此前 150 商品扩展计划，避免扩大数据变更范围。
