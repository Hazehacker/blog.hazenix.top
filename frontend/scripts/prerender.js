/**
 * 预渲染脚本 — 构建时生成静态 HTML，供 Nginx 直接返回给爬虫
 *
 * 用法：
 *   node scripts/prerender.js [baseUrl]
 *
 *   默认 baseUrl 为 https://blog.hazenix.top
 *   本地测试：node scripts/prerender.js http://localhost:5173
 *
 * 流程：
 *   1. 从 API 获取所有 slugs / categories / tags
 *   2. Puppeteer 渲染每个页面，等待 networkidle0
 *   3. 移除 <script> 标签（爬虫不需要 Vue 引导）
 *   4. 保存到 dist/prerender/，目录结构对应 URL 路径
 */

let puppeteer
try {
  puppeteer = (await import('puppeteer')).default
} catch {
  console.log('[prerender] puppeteer 未安装，跳过预渲染。npm install puppeteer 后重试。')
  process.exit(0)
}

// 优先用系统 Chrome（可用 puppeteer 安装的 Chrome 常因网络问题安装失败）
const CHROME_PATHS = [
  '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  '/usr/bin/google-chrome',
  '/usr/bin/chromium-browser',
  '/usr/bin/chromium',
]
import { execSync } from 'node:child_process'
function findChrome() {
  for (const p of CHROME_PATHS) {
    try { execSync(`test -x "${p}"`); return p } catch {}
  }
  return null
}

import { writeFile, mkdir } from 'node:fs/promises'
import { join, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const DIST_DIR = join(__dirname, '..', 'dist')
const PRERENDER_DIR = join(DIST_DIR, 'prerender')

const BASE_URL = process.argv[2] || process.env.PRERENDER_BASE_URL || 'https://blog.hazenix.top'
const API_BASE = BASE_URL + '/api'

// ========== 页面列表 ==========

const STATIC_PAGES = [
  // `/` 不预渲染 —— 着陆页走独立的 landing.html 入口（已是真静态 HTML）
  { path: '/home', file: 'home.html' },
  { path: '/articles', file: 'articles.html' },
  { path: '/categories', file: 'categories.html' },
  { path: '/tags', file: 'tags.html' },
  { path: '/friend-links', file: 'friend-links.html' },
  { path: '/tree-hole', file: 'tree-hole.html' },
  { path: '/messageboard', file: 'messageboard.html' },
  { path: '/about', file: 'about.html' },
  { path: '/about-project', file: 'about-project.html' },
  { path: '/history', file: 'history.html' },
]

// ========== 数据获取 ==========

async function fetchJSON(path) {
  const url = API_BASE + path
  const res = await fetch(url)
  if (!res.ok) throw new Error(`${url} → ${res.status}`)
  const json = await res.json()
  return json.data || json
}

async function collectPages() {
  const pages = [...STATIC_PAGES]

  // 文章页
  try {
    const slugs = await fetchJSON('/user/articles/slugs')
    for (const { slug, id } of slugs) {
      const identifier = slug || String(id)
      pages.push({ path: `/article/${identifier}`, file: `article/${identifier}.html` })
    }
    console.log(`  [slugs] ${slugs.length} 篇文章`)
  } catch (e) {
    console.warn(`  [slugs] 获取失败: ${e.message}`)
  }

  // 分类页
  try {
    const categories = await fetchJSON('/user/categories')
    for (const { id } of categories) {
      pages.push({ path: `/category/${id}`, file: `category/${id}.html` })
    }
    console.log(`  [categories] ${categories.length} 个分类`)
  } catch (e) {
    console.warn(`  [categories] 获取失败: ${e.message}`)
  }

  // 标签页
  try {
    const tags = await fetchJSON('/user/tags')
    for (const { id } of tags) {
      pages.push({ path: `/tag/${id}`, file: `tag/${id}.html` })
    }
    console.log(`  [tags] ${tags.length} 个标签`)
  } catch (e) {
    console.warn(`  [tags] 获取失败: ${e.message}`)
  }

  return pages
}

// ========== HTML 处理 ==========

function cleanHTML(html) {
  return html
    // 只移除非 module 脚本（GA4、Umami 等），保留 Vue 主模块脚本用于 hydration
    // 正则：匹配 <script> 标签，但排除 type="module" 的（Vue 入口）
    .replace(/<script\b(?!\s[^>]*type="module")[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
    .replace(/<script\b(?!\s[^>]*type="module")[^>]*\/>/gi, '')
    // 移除 Vite 的 modulepreload 链接
    .replace(/<link\s+rel="modulepreload"[^>]*>/gi, '')
    // 移除空的 <style> 动画定义（骨架屏 keyframes，如果存在）
    // 保留其余 CSS
}

// ========== 渲染 ==========

async function renderPage(browser, { path, file }) {
  const page = await browser.newPage()
  try {
    const url = BASE_URL + path
    await page.goto(url, { waitUntil: 'networkidle0', timeout: 30000 })
    await page.waitForSelector('#app', { timeout: 5000 }).catch(() => {})

    let html = await page.content()
    html = cleanHTML(html)

    const outPath = join(PRERENDER_DIR, file)
    await mkdir(dirname(outPath), { recursive: true })
    await writeFile(outPath, html)

    return { path, file, ok: true }
  } catch (err) {
    return { path, file, ok: false, error: err.message }
  } finally {
    await page.close()
  }
}

// ========== 主流程 ==========

async function main() {
  console.log('预渲染开始...')
  console.log(`  Base URL: ${BASE_URL}`)
  console.log(`  输出目录: ${PRERENDER_DIR}`)

  console.log('\n收集页面列表...')
  const pages = await collectPages()
  console.log(`共 ${pages.length} 个页面待渲染`)

  console.log('\n启动浏览器...')
  const chromePath = findChrome()
  const launchOpts = {
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox'],
  }
  if (chromePath) {
    launchOpts.executablePath = chromePath
    console.log(`  使用系统 Chrome: ${chromePath}`)
  }
  const browser = await puppeteer.launch(launchOpts)

  let done = 0
  let failed = 0
  const total = pages.length

  // 并发渲染，控制并发数避免压垮服务器
  const CONCURRENCY = 4
  for (let i = 0; i < pages.length; i += CONCURRENCY) {
    const batch = pages.slice(i, i + CONCURRENCY)
    const results = await Promise.all(batch.map(p => renderPage(browser, p)))

    for (const r of results) {
      if (r.ok) {
        done++
      } else {
        failed++
        console.warn(`  ✗ ${r.path}: ${r.error}`)
      }
      process.stdout.write(`\r  进度: ${done + failed}/${total} (${done} 成功, ${failed} 失败)`)
    }
  }

  await browser.close()

  console.log(`\n\n预渲染完成: ${done} 成功, ${failed} 失败`)

  // 关键修复：BASE_URL 指向生产站点时，抓回的 HTML 引用的是 *旧* 构建的资源 hash。
  // 部署后旧 hash 文件已被 --delete 清除 → 全站 JS/CSS 404 → 无法 hydration。
  // 用本地 dist/index.html 的标签覆盖所有 prerender HTML 里的资源引用。
  await rewriteAssetRefs()

  if (failed > 0) process.exitCode = 1
}

async function rewriteAssetRefs() {
  const { readFile } = await import('node:fs/promises')
  const { readdirSync, statSync } = await import('node:fs')
  const indexPath = join(DIST_DIR, 'index.html')
  const realHtml = await readFile(indexPath, 'utf8')
  const scriptTag = (realHtml.match(/<script[^>]+src="\/assets\/[^"]+\.js"[^>]*><\/script>/) || [])[0]
  const cssTag = (realHtml.match(/<link[^>]+rel="stylesheet"[^>]+href="\/assets\/[^"]+\.css"[^>]*>/) || [])[0]
  if (!scriptTag || !cssTag) {
    console.warn('  [rewrite] 未能从 dist/index.html 提取 script/css 标签，跳过')
    return
  }
  const files = []
  ;(function walk(d) {
    for (const name of readdirSync(d)) {
      const p = join(d, name)
      if (statSync(p).isDirectory()) walk(p)
      else if (name.endsWith('.html')) files.push(p)
    }
  })(PRERENDER_DIR)
  let patched = 0
  for (const f of files) {
    let html = await readFile(f, 'utf8')
    const before = html
    html = html
      .replace(/<script[^>]+src="\/assets\/[^"]+\.js"[^>]*><\/script>/g, scriptTag)
      .replace(/<link[^>]+rel="stylesheet"[^>]+href="\/assets\/[^"]+\.css"[^>]*>/g, cssTag)
    if (html !== before) {
      await writeFile(f, html)
      patched++
    }
  }
  console.log(`  [rewrite] 已用本地 dist 资源引用重写 ${patched}/${files.length} 个预渲染页面`)
}

main()
