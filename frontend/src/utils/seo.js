/**
 * SEO相关工具函数
 */

/**
 * 设置页面标题
 * @param {string} title - 页面标题
 * @param {string} siteName - 网站名称
 */
export function setPageTitle(title, siteName = 'Vue Blog') {
    if (title) {
        document.title = `${title} - ${siteName}`
    } else {
        document.title = siteName
    }
}

/**
 * 设置页面描述
 * @param {string} description - 页面描述
 */
export function setPageDescription(description) {
    let metaDescription = document.querySelector('meta[name="description"]')
    if (!metaDescription) {
        metaDescription = document.createElement('meta')
        metaDescription.name = 'description'
        document.head.appendChild(metaDescription)
    }
    metaDescription.content = description
}

/**
 * 设置页面关键词
 * @param {string|Array} keywords - 关键词
 */
export function setPageKeywords(keywords) {
    let metaKeywords = document.querySelector('meta[name="keywords"]')
    if (!metaKeywords) {
        metaKeywords = document.createElement('meta')
        metaKeywords.name = 'keywords'
        document.head.appendChild(metaKeywords)
    }

    const keywordsStr = Array.isArray(keywords) ? keywords.join(', ') : keywords
    metaKeywords.content = keywordsStr
}

/**
 * 设置页面作者
 * @param {string} author - 作者
 */
export function setPageAuthor(author) {
    let metaAuthor = document.querySelector('meta[name="author"]')
    if (!metaAuthor) {
        metaAuthor = document.createElement('meta')
        metaAuthor.name = 'author'
        document.head.appendChild(metaAuthor)
    }
    metaAuthor.content = author
}

/**
 * 设置Open Graph标签
 * @param {object} ogData - Open Graph数据
 */
export function setOpenGraph(ogData) {
    const { title, description, image, url, type = 'website', siteName } = ogData

    // 设置基本OG标签
    setMetaProperty('og:title', title)
    setMetaProperty('og:description', description)
    setMetaProperty('og:image', image)
    setMetaProperty('og:url', url)
    setMetaProperty('og:type', type)
    setMetaProperty('og:site_name', siteName)

    // 设置Twitter Card标签
    setMetaProperty('twitter:card', 'summary_large_image')
    setMetaProperty('twitter:title', title)
    setMetaProperty('twitter:description', description)
    setMetaProperty('twitter:image', image)
}

/**
 * 设置meta属性
 * @param {string} property - 属性名
 * @param {string} content - 属性值
 */
export function setMetaProperty(property, content) {
    if (!content) return

    let meta = document.querySelector(`meta[property="${property}"]`)
    if (!meta) {
        meta = document.createElement('meta')
        meta.setAttribute('property', property)
        document.head.appendChild(meta)
    }
    meta.content = content
}

/**
 * 设置canonical链接
 * @param {string} url - 规范URL
 */
export function setCanonicalUrl(url) {
    let canonical = document.querySelector('link[rel="canonical"]')
    if (!canonical) {
        canonical = document.createElement('link')
        canonical.rel = 'canonical'
        document.head.appendChild(canonical)
    }
    canonical.href = url
}

/**
 * 设置robots标签
 * @param {string} content - robots内容
 */
export function setRobots(content) {
    let metaRobots = document.querySelector('meta[name="robots"]')
    if (!metaRobots) {
        metaRobots = document.createElement('meta')
        metaRobots.name = 'robots'
        document.head.appendChild(metaRobots)
    }
    metaRobots.content = content
}

/**
 * 生成结构化数据（JSON-LD）
 * @param {object} data - 结构化数据
 */
export function setStructuredData(data) {
    // 移除现有的结构化数据
    const existingScript = document.querySelector('script[type="application/ld+json"]')
    if (existingScript) {
        existingScript.remove()
    }

    // 添加新的结构化数据
    const script = document.createElement('script')
    script.type = 'application/ld+json'
    script.textContent = JSON.stringify(data)
    document.head.appendChild(script)
}

/**
 * 生成文章结构化数据
 * @param {object} article - 文章数据
 * @returns {object} 结构化数据
 */
export function generateArticleStructuredData(article) {
    return {
        '@context': 'https://schema.org',
        '@type': 'Article',
        headline: article.title,
        description: article.summary || article.content?.substring(0, 160),
        image: article.coverImage || article.cover,
        author: {
            '@type': 'Person',
            name: article.authorName || article.author
        },
        publisher: {
            '@type': 'Organization',
            name: 'Vue Blog',
            logo: {
                '@type': 'ImageObject',
                url: '/logo.png'
            }
        },
        datePublished: article.createTime || article.createdAt,
        dateModified: article.updateTime || article.updatedAt,
        mainEntityOfPage: {
            '@type': 'WebPage',
            '@id': window.location.href
        },
        articleSection: article.categoryName || article.category?.name,
        keywords: article.tags?.map(tag => tag.name || tag).join(', '),
        wordCount: article.content?.length || 0,
        timeRequired: `PT${Math.ceil((article.content?.length || 0) / 500)}M`
    }
}

/**
 * 生成网站结构化数据
 * @param {object} siteData - 网站数据
 * @returns {object} 结构化数据
 */
export function generateWebsiteStructuredData(siteData) {
    return {
        '@context': 'https://schema.org',
        '@type': 'WebSite',
        name: siteData.name || 'Vue Blog',
        description: siteData.description || '一个基于Vue3的现代化博客系统',
        url: siteData.url || window.location.origin,
        potentialAction: {
            '@type': 'SearchAction',
            target: {
                '@type': 'EntryPoint',
                urlTemplate: `${siteData.url || window.location.origin}/search?q={search_term_string}`
            },
            'query-input': 'required name=search_term_string'
        }
    }
}

/**
 * 生成面包屑结构化数据
 * @param {Array} breadcrumbs - 面包屑数据
 * @returns {object} 结构化数据
 */
export function generateBreadcrumbStructuredData(breadcrumbs) {
    return {
        '@context': 'https://schema.org',
        '@type': 'BreadcrumbList',
        itemListElement: breadcrumbs.map((item, index) => ({
            '@type': 'ListItem',
            position: index + 1,
            name: item.name,
            item: item.url
        }))
    }
}

/**
 * 生成FAQ结构化数据
 * @param {Array} faqs - FAQ数据
 * @returns {object} 结构化数据
 */
export function generateFAQStructuredData(faqs) {
    return {
        '@context': 'https://schema.org',
        '@type': 'FAQPage',
        mainEntity: faqs.map(faq => ({
            '@type': 'Question',
            name: faq.question,
            acceptedAnswer: {
                '@type': 'Answer',
                text: faq.answer
            }
        }))
    }
}

/**
 * 设置完整的SEO信息
 * @param {object} seoData - SEO数据
 */
export function setSEO(seoData) {
    const {
        title,
        description,
        keywords,
        author,
        image,
        url,
        type = 'website',
        siteName = 'Vue Blog',
        robots = 'index, follow',
        canonical,
        structuredData
    } = seoData

    // 设置基本SEO标签
    if (title) setPageTitle(title, siteName)
    if (description) setPageDescription(description)
    if (keywords) setPageKeywords(keywords)
    if (author) setPageAuthor(author)
    if (robots) setRobots(robots)
    if (canonical) setCanonicalUrl(canonical)

    // 设置Open Graph标签
    if (title || description || image || url) {
        setOpenGraph({
            title,
            description,
            image,
            url,
            type,
            siteName
        })
    }

    // 设置结构化数据
    if (structuredData) {
        setStructuredData(structuredData)
    }
}

/**
 * 生成sitemap数据
 * @param {Array} pages - 页面数据
 * @returns {string} sitemap XML
 */
export function generateSitemap(pages) {
    const baseUrl = window.location.origin
    const currentDate = new Date().toISOString()

    let sitemap = '<?xml version="1.0" encoding="UTF-8"?>\n'
    sitemap += '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n'

    pages.forEach(page => {
        sitemap += '  <url>\n'
        sitemap += `    <loc>${baseUrl}${page.url}</loc>\n`
        sitemap += `    <lastmod>${page.lastmod || currentDate}</lastmod>\n`
        sitemap += `    <changefreq>${page.changefreq || 'weekly'}</changefreq>\n`
        sitemap += `    <priority>${page.priority || '0.5'}</priority>\n`
        sitemap += '  </url>\n'
    })

    sitemap += '</urlset>'

    return sitemap
}

/**
 * 生成robots.txt内容
 * @param {object} config - robots配置
 * @returns {string} robots.txt内容
 */
export function generateRobotsTxt(config = {}) {
    const {
        allow = ['/'],
        disallow = ['/admin/', '/api/'],
        sitemap = '/sitemap.xml',
        crawlDelay
    } = config

    let robots = ''

    // 允许的路径
    allow.forEach(path => {
        robots += `Allow: ${path}\n`
    })

    // 禁止的路径
    disallow.forEach(path => {
        robots += `Disallow: ${path}\n`
    })

    // 爬取延迟
    if (crawlDelay) {
        robots += `Crawl-delay: ${crawlDelay}\n`
    }

    // 站点地图
    robots += `Sitemap: ${window.location.origin}${sitemap}\n`

    return robots
}
