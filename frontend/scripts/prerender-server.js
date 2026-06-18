/**
 * 预渲染服务 — 独立 HTTP 服务，接收 webhook 调用进行增量页面重渲染
 *
 * 启动：
 *   PRERENDER_BASE_URL=https://blog.hazenix.top \
 *   PRERENDER_OUT_DIR=/path/to/frontend/dist \
 *   node scripts/prerender-server.js
 *
 * 端口：默认 3001（通过 PORT 环境变量配置）
 *
 * API：
 *   POST /render?path=/article/slug      — 渲染单个页面
 *   POST /render-all                     — 全量重新渲染
 *   POST /render-affected?type=article&id=1  — 渲染受影响页面（文章→首页+分类页+文章页）
 *   GET  /health                         — 健康检查
 */

import http from 'node:http'
import { writeFile, mkdir } from 'node:fs/promises'
import { join, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))

const PORT = process.env.PORT || 3001
const BASE_URL = process.env.PRERENDER_BASE_URL || 'https://blog.hazenix.top'
const OUT_DIR = process.env.PRERENDER_OUT_DIR || join(__dirname, '..', 'dist')
const PRERENDER_DIR = join(OUT_DIR, 'prerender')
const API_BASE = BASE_URL + '/api'

// ========== 浏览器单例 ==========

import { execSync } from 'node:child_process'

const CHROME_PATHS = [
  '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  '/usr/bin/google-chrome',
  '/usr/bin/chromium-browser',
  '/usr/bin/chromium',
]

function findChrome() {
  for (const p of CHROME_PATHS) {
    try { execSync(`test -x "${p}"`); return p } catch {}
  }
  return null
}

let browserPromise = null

function getBrowser() {
  if (!browserPromise) {
    browserPromise = import('puppeteer').then(({ default: puppeteer }) => {
      const opts = { headless: true, args: ['--no-sandbox', '--disable-setuid-sandbox'] }
      const chromePath = findChrome()
      if (chromePath) {
        opts.executablePath = chromePath
        console.log(`[prerender-server] 使用系统 Chrome: ${chromePath}`)
      }
      return puppeteer.launch(opts)
    })
  }
  return browserPromise
}

// ========== HTML 清理 ==========

function cleanHTML(html) {
  return html
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
    .replace(/<script\b[^>]*\/>/gi, '')
    .replace(/<link\s+rel="modulepreload"[^>]*>/gi, '')
}

// ========== 渲染 ==========

async function renderPage(browser, path, file) {
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
    return { path, ok: true }
  } finally {
    await page.close()
  }
}

// ========== 受影响的页面计算 ==========

async function getAffectedPaths(type, id) {
  const paths = []

  // 首页总是受影响（文章列表、热门文章变化）
  paths.push({ path: '/', file: 'index.html' })
  paths.push({ path: '/home', file: 'home.html' })
  paths.push({ path: '/articles', file: 'articles.html' })

  if (type === 'article') {
    try {
      const res = await fetch(`${API_BASE}/user/articles/${id}`)
      const json = await res.json()
      const article = json.data || json
      if (article) {
        const identifier = article.slug || String(article.id)
        paths.push({ path: `/article/${identifier}`, file: `article/${identifier}.html` })
        if (article.categoryId || article.category?.id) {
          const cid = article.categoryId || article.category?.id
          paths.push({ path: `/category/${cid}`, file: `category/${cid}.html` })
        }
        if (article.tags) {
          for (const tag of article.tags) {
            const tid = typeof tag === 'object' ? tag.id : tag
            if (tid) paths.push({ path: `/tag/${tid}`, file: `tag/${tid}.html` })
          }
        }
      }
    } catch (e) {
      console.error(`获取文章 ${id} 失败:`, e.message)
    }
  } else if (type === 'category') {
    paths.push({ path: `/category/${id}`, file: `category/${id}.html` })
    paths.push({ path: '/categories', file: 'categories.html' })
  } else if (type === 'tag') {
    paths.push({ path: `/tag/${id}`, file: `tag/${id}.html` })
    paths.push({ path: '/tags', file: 'tags.html' })
  }

  return paths
}

// ========== HTTP 服务器 ==========

const STATIC_PAGES = [
  { path: '/', file: 'index.html' },
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

async function collectAllPages() {
  const pages = [...STATIC_PAGES]
  try {
    const res = await fetch(`${API_BASE}/user/articles/slugs`)
    const data = await res.json()
    const slugs = data.data || data || []
    for (const { slug, id } of slugs) {
      const identifier = slug || String(id)
      pages.push({ path: `/article/${identifier}`, file: `article/${identifier}.html` })
    }
  } catch (e) { console.error('获取 slugs 失败:', e.message) }
  try {
    const res = await fetch(`${API_BASE}/user/categories`)
    const data = await res.json()
    const cats = data.data || data || []
    for (const { id } of cats) pages.push({ path: `/category/${id}`, file: `category/${id}.html` })
  } catch (e) { console.error('获取 categories 失败:', e.message) }
  try {
    const res = await fetch(`${API_BASE}/user/tags`)
    const data = await res.json()
    const tags = data.data || data || []
    for (const { id } of tags) pages.push({ path: `/tag/${id}`, file: `tag/${id}.html` })
  } catch (e) { console.error('获取 tags 失败:', e.message) }
  return pages
}

async function handleRequest(req, res) {
  const url = new URL(req.url, `http://localhost:${PORT}`)

  // 健康检查
  if (req.method === 'GET' && url.pathname === '/health') {
    res.writeHead(200, { 'Content-Type': 'application/json' })
    res.end(JSON.stringify({ status: 'ok' }))
    return
  }

  // 需要 POST 的端点
  if (req.method !== 'POST') {
    res.writeHead(405)
    res.end('Method Not Allowed')
    return
  }

  const startTime = Date.now()
  const browser = await getBrowser()

  try {
    if (url.pathname === '/render-all') {
      console.log('[prerender-server] 全量渲染开始...')
      const pages = await collectAllPages()
      let ok = 0, fail = 0
      for (const p of pages) {
        const result = await renderPage(browser, p.path, p.file)
        if (result.ok) ok++; else fail++
      }
      const elapsed = Date.now() - startTime
      console.log(`[prerender-server] 全量完成: ${ok} 成功, ${fail} 失败 (${elapsed}ms)`)
      res.writeHead(200, { 'Content-Type': 'application/json' })
      res.end(JSON.stringify({ ok, fail, elapsed }))

    } else if (url.pathname === '/render') {
      const path = url.searchParams.get('path')
      if (!path) {
        res.writeHead(400)
        res.end('Missing ?path= parameter')
        return
      }
      console.log(`[prerender-server] 渲染: ${path}`)
      const file = path === '/' ? 'index.html' : path.replace(/^\//, '') + '.html'
      const result = await renderPage(browser, path, file)
      const elapsed = Date.now() - startTime
      if (result.ok) {
        console.log(`[prerender-server] 完成: ${path} (${elapsed}ms)`)
        res.writeHead(200, { 'Content-Type': 'application/json' })
        res.end(JSON.stringify({ ok: true, elapsed }))
      } else {
        res.writeHead(500)
        res.end('Render failed')
      }

    } else if (url.pathname === '/render-affected') {
      const type = url.searchParams.get('type')
      const id = url.searchParams.get('id')
      if (!type || !id) {
        res.writeHead(400)
        res.end('Missing ?type= or ?id=')
        return
      }
      console.log(`[prerender-server] 增量渲染: ${type}/${id}`)
      const pages = await getAffectedPaths(type, id)
      let ok = 0, fail = 0
      for (const p of pages) {
        const result = await renderPage(browser, p.path, p.file)
        if (result.ok) ok++; else fail++
      }
      const elapsed = Date.now() - startTime
      console.log(`[prerender-server] 增量完成: ${ok} 成功, ${fail} 失败 (${elapsed}ms)`)
      res.writeHead(200, { 'Content-Type': 'application/json' })
      res.end(JSON.stringify({ ok, fail, elapsed }))

    } else {
      res.writeHead(404)
      res.end('Not Found')
    }
  } catch (err) {
    console.error('[prerender-server] 错误:', err)
    res.writeHead(500)
    res.end(err.message)
  }
}

const server = http.createServer(handleRequest)
server.listen(PORT, () => {
  console.log(`[prerender-server] 运行在 http://localhost:${PORT}`)
  console.log(`[prerender-server] 目标 URL: ${BASE_URL}`)
  console.log(`[prerender-server] 输出目录: ${PRERENDER_DIR}`)
})
