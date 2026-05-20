import { createRequire } from 'node:module'
import { mkdir } from 'node:fs/promises'
import path from 'node:path'

const require = createRequire(import.meta.url)
const baseUrl = process.env.BASE_URL || 'http://localhost:8088'
const adminUsername = process.env.ADMIN_USERNAME || 'admin'
const adminPassword = process.env.ADMIN_PASSWORD || 'admin123456'
const screenshotDir = process.env.SCREENSHOT_DIR || '/tmp/mall-frontend-smoke'
const playwrightModule = process.env.PLAYWRIGHT_MODULE || 'playwright'

let chromium
try {
  ;({ chromium } = require(playwrightModule))
} catch (error) {
  throw new Error(`无法加载 Playwright 模块。可先安装 playwright，或设置 PLAYWRIGHT_MODULE 指向模块路径。原始错误：${error.message}`)
}

const messages = []
const pageErrors = []

function attachConsole(page, label) {
  page.on('console', (msg) => {
    if (['error', 'warning'].includes(msg.type())) {
      messages.push({ label, type: msg.type(), text: msg.text() })
    }
  })
  page.on('pageerror', (error) => {
    pageErrors.push({ label, text: error.message })
  })
}

async function assertVisibleText(page, text) {
  await page.getByText(text, { exact: false }).first().waitFor({ timeout: 8000 })
}

async function screenshot(page, name) {
  const file = path.join(screenshotDir, name)
  await page.screenshot({ path: file, fullPage: false })
  return file
}

await mkdir(screenshotDir, { recursive: true })

const browser = await chromium.launch({ headless: true })
try {
  const context = await browser.newContext({ viewport: { width: 1440, height: 900 } })
  const page = await context.newPage()
  attachConsole(page, 'desktop')

  await page.goto(baseUrl, { waitUntil: 'networkidle' })
  const title = await page.title()
  const homeText = await page.locator('#app').innerText({ timeout: 8000 })
  if (homeText.trim().length < 20) {
    throw new Error('首页内容过少，可能为空白页')
  }
  if (/vite|webpack|error overlay/i.test(homeText)) {
    throw new Error('页面疑似出现框架错误覆盖层')
  }
  const homeDesktop = await screenshot(page, 'home-desktop.png')

  await page.goto(`${baseUrl}/login`, { waitUntil: 'networkidle' })
  await page.locator('input').nth(0).fill(adminUsername)
  await page.locator('input').nth(1).fill(adminPassword)
  await page.getByRole('button', { name: '登录' }).click()
  await page.waitForURL(`${baseUrl}/`, { timeout: 8000 })

  await page.goto(`${baseUrl}/admin/orders`, { waitUntil: 'networkidle' })
  await assertVisibleText(page, '订单管理')
  await page.getByRole('button', { name: '详情' }).first().click()
  await page.locator('.el-overlay').waitFor({ state: 'visible', timeout: 8000 })
  await page.locator('.el-drawer').waitFor({ state: 'visible', timeout: 8000 })
  await assertVisibleText(page, '操作记录')
  const drawerText = await page.locator('.el-drawer').innerText({ timeout: 8000 })
  if (drawerText.includes('未知 ->')) {
    throw new Error('审计状态流仍显示未知状态')
  }
  await page.waitForTimeout(400)
  const orderAudit = await screenshot(page, 'admin-order-audit.png')

  await page.goto(`${baseUrl}/admin/operation-logs`, { waitUntil: 'networkidle' })
  await assertVisibleText(page, '操作日志')
  const adminOperation = await screenshot(page, 'admin-operation-log.png')

  const mobile = await context.newPage()
  attachConsole(mobile, 'mobile')
  await mobile.setViewportSize({ width: 390, height: 844 })
  await mobile.goto(baseUrl, { waitUntil: 'networkidle' })
  const mobileText = await mobile.locator('#app').innerText({ timeout: 8000 })
  if (mobileText.trim().length < 20) {
    throw new Error('移动端首页内容过少，可能为空白页')
  }
  const homeMobile = await screenshot(mobile, 'home-mobile.png')

  const relevantErrors = messages.filter((item) => item.type === 'error')
  if (pageErrors.length || relevantErrors.length) {
    throw new Error(JSON.stringify({ pageErrors, relevantErrors }, null, 2))
  }

  console.log(JSON.stringify({
    baseUrl,
    title,
    homeTextLength: homeText.length,
    drawerHasAuditLogs: drawerText.includes('操作记录'),
    consoleWarnings: messages.filter((item) => item.type === 'warning').length,
    screenshots: [homeDesktop, orderAudit, adminOperation, homeMobile],
    checks: 'passed'
  }, null, 2))
} finally {
  await browser.close()
}
