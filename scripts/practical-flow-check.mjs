const base = process.env.BASE_URL || 'http://localhost:8088/api'
const origin = new URL(base).origin

async function request(method, path, body, token, expectOk = true) {
  const headers = {}
  const options = { method, headers }
  if (token) headers.Authorization = `Bearer ${token}`
  if (body instanceof FormData) {
    options.body = body
  } else if (body !== undefined && body !== null) {
    headers['Content-Type'] = 'application/json'
    options.body = JSON.stringify(body)
  }

  const response = await fetch(base + path, options)
  const text = await response.text()
  let payload
  try {
    payload = text ? JSON.parse(text) : null
  } catch {
    payload = { raw: text }
  }

  if (expectOk && (!response.ok || payload?.code !== 200)) {
    throw new Error(`${method} ${path} failed: HTTP ${response.status} ${text}`)
  }
  return { http: response.status, body: payload }
}

async function login(username, password) {
  const response = await request('POST', '/user/login', { username, password })
  return response.body.data.token
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message)
  }
}

async function uploadImage(adminToken) {
  const png = Buffer.from(
    'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=',
    'base64'
  )
  const form = new FormData()
  form.append('file', new Blob([png], { type: 'image/png' }), 'practical-check.png')
  const uploaded = await request('POST', '/admin/upload/product-image', form, adminToken)
  const url = uploaded.body.data.url
  const image = await fetch(origin + url, { method: 'HEAD' })
  assert(image.ok, '上传图片不能通过前端入口访问')
  return url
}

const health = await request('GET', '/health')
assert(health.body.data.status === 'UP', '健康检查失败')
const ready = await request('GET', '/ready')
assert(ready.body.data.status === 'UP' && ready.body.data.database === 'UP', '数据库就绪检查失败')

const userToken = await login('user', 'user123456')
const adminToken = await login('admin', 'admin123456')
const aliceToken = await login('alice', 'user123456')

const unauthorized = await request('GET', '/cart', null, null, false)
assert(unauthorized.http === 401, '未登录访问购物车应返回 401')

const forbidden = await request('GET', '/admin/statistics', null, userToken, false)
assert(forbidden.http === 403, '普通用户访问后台应返回 403')

const overStock = await request('POST', '/cart', { productId: 11, quantity: 999 }, userToken, false)
assert(overStock.http === 400, '超库存加入购物车应返回 400')

const lowStock = await request('GET', '/admin/products?page=1&size=20&lowStock=true', null, adminToken)
assert((lowStock.body.data.records || []).every((item) => item.stock < 10), '低库存筛选结果异常')

const imageUrl = await uploadImage(adminToken)

await request('DELETE', '/cart/clear', null, userToken)
const address = await request('POST', '/address', {
  receiverName: '验收用户',
  receiverPhone: '13600000002',
  province: '江苏省',
  city: '南京市',
  detailAddress: '上线前验收地址',
  isDefault: 1
}, userToken)
const addressId = address.body.data.id

await request('POST', '/cart', { productId: 3, quantity: 1 }, userToken)
let cart = (await request('GET', '/cart', null, userToken)).body.data
let cartItem = cart.find((item) => item.productId === 3)
const order = await request('POST', '/orders', { addressId, cartItemIds: [cartItem.id] }, userToken)
const orderId = order.body.data.orderId

await request('PUT', `/orders/${orderId}/payment-note`, { paymentNote: '上线前验收付款备注' }, userToken)
await request('POST', `/admin/orders/${orderId}/confirm-payment`, { adminRemark: '上线前验收已收款' }, adminToken)
await request('PUT', `/admin/orders/${orderId}/ship`, { shippingNo: `CHECK${Date.now()}` }, adminToken)
await request('PUT', `/orders/${orderId}/confirm-receipt`, null, userToken)
const finished = (await request('GET', `/orders/${orderId}`, null, userToken)).body.data
assert(finished.status === 3 && finished.confirmTime, '订单确认收货失败')

const productBefore = (await request('GET', '/products/4')).body.data
await request('POST', '/cart', { productId: 4, quantity: 1 }, userToken)
cart = (await request('GET', '/cart', null, userToken)).body.data
cartItem = cart.find((item) => item.productId === 4)
const cancelOrder = await request('POST', '/orders', { addressId, cartItemIds: [cartItem.id] }, userToken)
const cancelOrderId = cancelOrder.body.data.orderId
await request('PUT', `/orders/${cancelOrderId}/cancel`, null, userToken)
const canceled = (await request('GET', `/orders/${cancelOrderId}`, null, userToken)).body.data
const productAfter = (await request('GET', '/products/4')).body.data
assert(canceled.status === 4 && canceled.cancelTime, '取消订单状态异常')
assert(productAfter.stock === productBefore.stock && productAfter.sales === productBefore.sales, '取消订单未恢复库存或销量')

await request('PUT', '/admin/users/3/status', { status: 0 }, adminToken)
const disabledToken = await request('GET', '/cart', null, aliceToken, false)
assert(disabledToken.http === 403, '禁用用户旧 Token 应返回 403')
await request('PUT', '/admin/users/3/status', { status: 1 }, adminToken)

const selfDisable = await request('PUT', '/admin/users/1/status', { status: 0 }, adminToken, false)
assert(selfDisable.http === 400, '管理员禁用自己应失败')

const summary = await request('GET', '/admin/users/2/order-summary', null, adminToken)
assert(summary.body.data.userId === 2, '用户订单概要异常')

console.log(JSON.stringify({
  base,
  orderId,
  cancelOrderId,
  imageUrl,
  lowStockCount: lowStock.body.data.records.length,
  userTotalOrders: summary.body.data.totalOrders,
  checks: 'passed'
}, null, 2))
