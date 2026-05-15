# 演示脚本

本文档用于数据库课程答辩或本地验收演示。推荐配合 `docs/deploy-demo.md` 使用。

## 1. 演示目标

本次演示要证明系统具备完整小型商城闭环：

```text
游客浏览商品
  -> 用户登录
  -> 加入购物车
  -> 维护地址
  -> 提交订单
  -> 数据库生成订单和明细，扣减库存，清理购物车
  -> 用户模拟支付
  -> 管理员查看订单并发货
  -> 用户查看订单状态
```

同时展示数据库课程重点：

- E-R 关系清晰。
- 主键、外键、唯一约束、检查约束和索引设计合理。
- 订单创建过程使用事务。
- 订单明细保存商品名称、价格、图片快照。
- 支持分页、筛选和统计查询。

## 2. 演示前准备

启动服务：

```bash
cp .env.example .env
docker compose up -d --build
docker compose ps
```

检查接口：

```bash
curl http://localhost:8088/api/health
```

打开页面：

```text
前端入口：http://localhost:8088
```

准备数据库客户端：

```text
host: localhost
port: 3307
user: root
password: mall_demo_root_2026
database: mall_db
```

准备账号：

| 角色 | 用户名 | 密码 |
|---|---|---|
| 普通用户 | user | user123456 |
| 管理员 | admin | admin123456 |
| 禁用用户 | disabled_demo | user123456 |

## 3. 推荐演示顺序

### 3.1 游客浏览

1. 打开 `http://localhost:8088`。
2. 展示首页商品列表。
3. 使用分类筛选商品。
4. 搜索商品关键词，例如“咖啡”或“数据库”。
5. 打开一个商品详情页。

讲解点：

- 游客无需登录即可浏览商品。
- 商品支持分页、分类筛选和关键词搜索。
- 商品数据来自 `product` 和 `category` 表。

### 3.2 用户登录和购物车

1. 使用 `user / user123456` 登录。
2. 进入商品详情页。
3. 选择一个库存充足的商品，例如“智能手环”。
4. 加入购物车。
5. 打开购物车页面。

数据库观察：

```sql
SELECT * FROM cart_item WHERE user_id = 2;
```

讲解点：

- `cart_item(user_id, product_id)` 使用联合唯一约束。
- 同一个用户重复加入同一商品会合并数量。
- 后端校验库存，购物车数量不能超过库存。

### 3.3 地址维护

1. 进入地址管理页面。
2. 新增演示地址。
3. 设置为默认地址。

数据库观察：

```sql
SELECT * FROM address WHERE user_id = 2;
```

讲解点：

- 地址归属于用户。
- 默认地址互斥由业务逻辑保证。
- 下单时会把地址信息保存为订单快照。

### 3.4 提交订单

1. 回到购物车。
2. 选择刚加入的商品。
3. 进入结算页。
4. 选择地址并提交订单。

数据库观察：

```sql
SELECT * FROM order_info ORDER BY id DESC LIMIT 3;
SELECT * FROM order_item ORDER BY id DESC LIMIT 5;
SELECT id, name, stock, sales FROM product ORDER BY id;
SELECT * FROM cart_item WHERE user_id = 2;
```

讲解点：

- `order_info` 保存订单主信息和地址快照。
- `order_item` 保存商品名称、价格、图片快照。
- `product.stock` 扣减，`product.sales` 增加。
- 已结算购物车项被删除。
- 以上动作在同一个事务中完成。

### 3.5 模拟支付和订单状态

1. 打开订单列表。
2. 查看新订单状态为“待支付”。
3. 点击模拟支付。
4. 查看订单状态变化。

数据库观察：

```sql
SELECT id, order_no, status, pay_time, ship_time, finish_time
FROM order_info
ORDER BY id DESC;
```

状态说明：

| 状态 | 含义 |
|---:|---|
| 0 | 待支付 |
| 1 | 已支付待发货 |
| 2 | 已发货 |
| 3 | 已完成 |
| 4 | 已取消 |

### 3.6 管理员后台

1. 使用 `admin / admin123456` 登录。
2. 进入后台仪表盘。
3. 查看订单数、销售额、待发货订单、低库存商品和热销商品。
4. 进入订单管理。
5. 筛选待发货订单。
6. 对刚支付的订单执行发货。
7. 回到用户端查看订单状态。

数据库观察：

```sql
SELECT id, order_no, status, pay_time, ship_time
FROM order_info
ORDER BY id DESC;
```

讲解点：

- 普通用户不能访问管理员接口。
- 后端使用角色校验，前端菜单隐藏不是唯一防线。
- 统计数据通过订单表和订单明细表聚合得到。

## 4. API 演示辅助

接口测试文件：

```text
mall-backend/api-test.http
```

建议演示前按顺序执行：

1. 健康检查。
2. 普通用户登录。
3. 管理员登录。
4. 普通用户购物车、地址、下单、支付。
5. 管理员待发货订单、发货、统计。
6. 反向用例：禁用用户登录、未登录访问购物车、普通用户访问后台、超库存加入购物车。

## 5. 演示初始化数据

初始化数据已经覆盖多种订单状态：

| 订单 ID | 状态 | 用途 |
|---:|---|---|
| 1 | 已发货 | 展示历史发货订单 |
| 2 | 已支付待发货 | 展示管理员发货 |
| 3 | 待支付 | 展示用户支付前状态 |
| 4 | 已完成 | 展示已完成订单 |
| 5 | 已取消 | 展示取消订单 |

初始化数据还包含：

- 低库存商品：“每日坚果”。
- 热销商品：多条订单明细用于后台销量排行。
- 禁用用户：`disabled_demo`，用于登录失败测试。

## 6. 演示失败时的快速排查

服务状态：

```bash
docker compose ps
docker compose logs backend
docker compose logs frontend
docker compose logs mysql
```

重置数据库：

```bash
docker compose down -v
docker compose up -d --build
```

接口健康检查：

```bash
curl http://localhost:8088/api/health
curl http://localhost:8080/api/health
```

常见问题：

- 登录失败：检查数据库是否初始化成功。
- 前端接口失败：检查 Nginx `/api` 代理和后端日志。
- 下单失败：检查购物车、地址、商品库存和商品状态。
- 管理员进不去后台：确认使用 `admin / admin123456` 登录。

## 7. 收尾讲解

最后建议强调：

- 当前是可演示级部署，不是公网生产部署。
- 支付是模拟支付。
- Docker Compose 解决本地环境一致性问题。
- 真正生产使用还需要 HTTPS、真实支付、图片存储、备份、监控、CI/CD 和安全加固。

