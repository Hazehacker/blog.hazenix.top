<template>
  <div class="bs-page">
    <div class="bs-bg" aria-hidden="true">
      <div class="bs-blob bs-blob-1" />
      <div class="bs-blob bs-blob-2" />
      <div class="bs-blob bs-blob-3" />
    </div>

    <header class="bs-header">
      <span class="bs-eyebrow">T · A · G · S</span>
      <h1 class="bs-title">标签星球</h1>
      <p class="bs-sub">{{ sortedTags.length }} 个标签 · {{ totalArticles }} 篇文章</p>
    </header>

    <div v-if="loading" class="bs-loading">
      <div class="bs-spinner" />
    </div>

    <div v-else class="bs-stage" ref="stageRef">
      <div
        v-for="(tag, i) in sortedTags"
        :key="tag.id"
        class="bs-anchor"
        :style="anchorStyle(i)"
      >
        <div
          class="bs-bubble"
          :class="`drift-${i % 6}`"
          :style="bubbleInlineStyle(tag, i)"
          @click="goToTag(tag)"
          @mouseenter="hov = i"
          @mouseleave="hov = null"
        >
          <div class="bs-shine" />
          <div class="bs-shine-ring" />
          <span class="bs-name">{{ tag.name }}</span>
          <span class="bs-count">{{ tag.articleCount }} 篇</span>
        </div>
      </div>

      <div v-if="sortedTags.length === 0" class="bs-empty">暂无标签</div>
    </div>

    <div class="bs-legend">
      <div v-for="p in PALETTE.slice(0, 5)" :key="p.hex" class="bs-legend-dot" :style="{ background: p.hex, boxShadow: `0 0 6px ${p.hex}66` }" />
      <span class="bs-legend-text">大小 = 文章数</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getTagList } from '@/api/tag'
import { setSEO } from '@/utils/seo'
import { articleApi } from '@/api/article'

const router   = useRouter()
const stageRef = ref(null)
const hov      = ref(null)
const anchors  = ref([])
const tags     = ref([])
const loading  = ref(false)

// ─── Palette ─────────────────────────────────────────────────
const PALETTE = [
  { hex: '#6366f1', rgb: '99,102,241'  },
  { hex: '#ec4899', rgb: '236,72,153'  },
  { hex: '#10b981', rgb: '16,185,129'  },
  { hex: '#f59e0b', rgb: '245,158,11'  },
  { hex: '#0ea5e9', rgb: '14,165,233'  },
  { hex: '#8b5cf6', rgb: '139,92,246'  },
  { hex: '#f97316', rgb: '249,115,22'  },
  { hex: '#14b8a6', rgb: '20,184,166'  },
]

// ─── Derived ─────────────────────────────────────────────────
const sortedTags    = computed(() => [...tags.value].sort((a, b) => b.articleCount - a.articleCount))
const totalArticles = computed(() => tags.value.reduce((s, t) => s + t.articleCount, 0))
const maxCount      = computed(() => sortedTags.value[0]?.articleCount     || 1)
const minCount      = computed(() => sortedTags.value.at(-1)?.articleCount || 1)

// ─── Data fetching ───────────────────────────────────────────
const recalculateTagCounts = async (tagList) => {
  try {
    const response = await articleApi.getArticleList()
    if (response?.code === '200' && response.data) {
      let articles = Array.isArray(response.data)
        ? response.data
        : (response.data.list ?? response.data.records ?? [])

      const published = articles.filter(a =>
        a.id !== 1 && a.id !== '1' && (a.status === 0 || a.status === '0')
      )

      const countMap = new Map()
      published.forEach(a => {
        if (Array.isArray(a.tags)) {
          a.tags.forEach(t => {
            const id = typeof t === 'object' ? t.id : t
            if (id) countMap.set(id, (countMap.get(id) || 0) + 1)
          })
        }
      })

      return tagList
        .map(tag => ({ ...tag, articleCount: countMap.get(tag.id) || 0 }))
        .filter(tag => tag.articleCount > 0)
    }
  } catch {
    // fall through
  }
  return tagList
}

const loadTags = async () => {
  loading.value = true
  try {
    const res = await getTagList()
    let list  = res.data || []
    list      = await recalculateTagCounts(list)
    tags.value = list
  } catch {
    tags.value = []
  } finally {
    loading.value = false
    await nextTick()
    computeAnchors()
  }
}

// ─── Layout ──────────────────────────────────────────────────
const lcg = (seed) => {
  let s = seed >>> 0
  return () => { s = Math.imul(s, 1664525) + 1013904223 >>> 0; return s / 0xffffffff }
}

const bubbleSize = (count) => {
  const t = maxCount.value === minCount.value
    ? 0.5
    : (count - minCount.value) / (maxCount.value - minCount.value)
  return Math.round(62 + t * 92) // 62 px – 154 px
}

const computeAnchors = () => {
  const el = stageRef.value
  if (!el) return
  const W  = el.offsetWidth
  const H  = el.offsetHeight
  const cx = W / 2
  const cy = H / 2
  const rng = lcg(42)

  const goldenAngle  = 2.39996
  const baseSpread   = 60
  const gap          = 18
  const centerRadius = bubbleSize(sortedTags.value[0]?.articleCount || 1) / 2

  anchors.value = sortedTags.value.map((tag, i) => {
    const angle      = i * goldenAngle
    const currRadius = bubbleSize(tag.articleCount) / 2
    const minR       = i === 0 ? 0 : centerRadius + currRadius + gap
    const spiralR    = i === 0 ? 0 : baseSpread * Math.sqrt(i)
    const r          = Math.max(minR, spiralR)
    const jx         = (rng() - 0.5) * 20
    const jy         = (rng() - 0.5) * 20
    const half       = currRadius + 6
    return {
      x: Math.min(Math.max(cx + r * Math.cos(angle) + jx, half), W - half),
      y: Math.min(Math.max(cy + r * Math.sin(angle) + jy, half), H - half),
    }
  })
}

// ─── Styles ──────────────────────────────────────────────────
const anchorStyle = (i) => {
  const p = anchors.value[i]
  if (!p) return { opacity: 0 }
  return { left: `${p.x}px`, top: `${p.y}px`, opacity: 1 }
}

const bubbleInlineStyle = (tag, i) => {
  const size  = bubbleSize(tag.articleCount)
  const pal   = PALETTE[i % PALETTE.length]
  const isHov = hov.value === i
  const r     = pal.rgb
  const dur   = (5.5 + (i % 4) * 0.9).toFixed(1)
  const delay = ((i * 0.61) % 5).toFixed(2)

  return {
    width:   `${size}px`,
    height:  `${size}px`,
    background: `radial-gradient(circle at 32% 26%, rgba(255,255,255,0.88) 0%, rgba(${r},0.07) 52%, rgba(${r},0.18) 100%)`,
    border:  `1px solid rgba(${r},${isHov ? 0.32 : 0.16})`,
    boxShadow: isHov
      ? `0 12px 40px rgba(${r},0.28), 0 4px 16px rgba(${r},0.16), inset 0 1.5px 0 rgba(255,255,255,1), inset 0 -1px 0 rgba(${r},0.12)`
      : `0 6px 24px rgba(${r},0.14), 0 2px 8px rgba(${r},0.08), inset 0 1px 0 rgba(255,255,255,0.95)`,
    color:    pal.hex,
    fontSize: `${Math.max(10, Math.min(15, size * 0.115))}px`,
    transform: `scale(${isHov ? 1.11 : 1})`,
    animationDuration:  `${dur}s`,
    animationDelay:     `${delay}s`,
    animationPlayState: isHov ? 'paused' : 'running',
    transition: 'transform 0.35s cubic-bezier(0.34,1.56,0.64,1), box-shadow 0.3s, border-color 0.3s',
    cursor: 'pointer',
  }
}

const goToTag = (tag) => router.push(`/tag/${tag.id}`)

onMounted(() => {
  // Non-blocking font loading
  const fontLink = document.createElement('link')
  fontLink.rel = 'stylesheet'
  fontLink.href = 'https://fonts.googleapis.com/css2?family=Nunito:wght@400;500;600;700;800&family=DM+Serif+Display:ital@0;1&display=swap'
  document.head.appendChild(fontLink)

  setSEO({
    title: '标签列表',
    description: '浏览所有文章标签，按标签发现感兴趣的内容',
    type: 'website',
    siteName: 'Hazenix的后端札记',
  })
  loadTags()
  window.addEventListener('resize', computeAnchors)
})
onUnmounted(() => window.removeEventListener('resize', computeAnchors))
</script>

<style scoped>
.bs-page {
  position: relative;
  min-height: 80vh;
  overflow: hidden;
  padding: 0;
  font-family: 'Nunito', sans-serif;
  background: #ffffff;
}

.bs-bg { display: none; }

.bs-header {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 3rem 1.5rem 2rem;
}

.bs-eyebrow {
  display: block;
  font-size: 0.6rem;
  letter-spacing: 0.35em;
  color: #a0a8c0;
  margin-bottom: 0.75rem;
  font-weight: 600;
}

.bs-title {
  font-family: 'DM Serif Display', serif;
  font-size: clamp(2.5rem, 5vw, 4rem);
  font-weight: 400;
  color: #1a1d2e;
  margin: 0;
  letter-spacing: -0.02em;
  line-height: 1.05;
}

.bs-sub {
  font-size: 0.8rem;
  color: #8890a8;
  margin: 0.6rem 0 0;
  letter-spacing: 0.04em;
}

.bs-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 580px;
  position: relative;
  z-index: 1;
}
.bs-spinner {
  width: 36px; height: 36px;
  border: 2.5px solid #e2e8f0;
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.bs-stage {
  position: relative;
  width: 100%;
  height: 580px;
  z-index: 1;
}

.bs-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a0a8c0;
  font-size: 0.9rem;
}

.bs-anchor {
  position: absolute;
  width: 0; height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.5s;
}

.bs-bubble {
  position: relative;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  user-select: none;
  will-change: transform;
  animation-timing-function: ease-in-out;
  animation-iteration-count: infinite;
  animation-direction: alternate;
  flex-shrink: 0;
}

.bs-shine {
  position: absolute;
  top: 14%; left: 20%;
  width: 26%; height: 26%;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.92) 0%, rgba(255,255,255,0) 100%);
  pointer-events: none;
}
.bs-shine-ring {
  position: absolute;
  bottom: 18%; right: 14%;
  width: 14%; height: 14%;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.6) 0%, rgba(255,255,255,0) 100%);
  pointer-events: none;
}

.bs-name {
  position: relative;
  z-index: 1;
  font-weight: 700;
  line-height: 1.25;
  padding: 0 10%;
  color: inherit;
}
.bs-count {
  position: relative;
  z-index: 1;
  font-size: 0.72em;
  opacity: 0.5;
  margin-top: 2px;
  font-weight: 500;
  color: inherit;
}

.drift-0 { animation-name: drift-0; }
.drift-1 { animation-name: drift-1; }
.drift-2 { animation-name: drift-2; }
.drift-3 { animation-name: drift-3; }
.drift-4 { animation-name: drift-4; }
.drift-5 { animation-name: drift-5; }

@keyframes drift-0 {
  0%   { translate: 0px 0px; }
  20%  { translate: 9px -14px; }
  45%  { translate: 16px -5px; }
  70%  { translate: 7px 12px; }
  100% { translate: 0px 0px; }
}
@keyframes drift-1 {
  0%   { translate: 0px 0px; }
  25%  { translate: -12px -10px; }
  55%  { translate: -5px 15px; }
  80%  { translate: 8px 6px; }
  100% { translate: 0px 0px; }
}
@keyframes drift-2 {
  0%   { translate: 0px 0px; }
  30%  { translate: 14px 8px; }
  60%  { translate: 4px -16px; }
  85%  { translate: -10px -4px; }
  100% { translate: 0px 0px; }
}
@keyframes drift-3 {
  0%   { translate: 0px 0px; }
  15%  { translate: -8px 12px; }
  40%  { translate: -16px 2px; }
  70%  { translate: -5px -14px; }
  100% { translate: 0px 0px; }
}
@keyframes drift-4 {
  0%   { translate: 0px 0px; }
  35%  { translate: 11px -11px; }
  65%  { translate: -6px -8px; }
  90%  { translate: 4px 10px; }
  100% { translate: 0px 0px; }
}
@keyframes drift-5 {
  0%   { translate: 0px 0px; }
  20%  { translate: -10px -6px; }
  50%  { translate: 8px -14px; }
  80%  { translate: 12px 4px; }
  100% { translate: 0px 0px; }
}

.bs-legend {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  margin-top: 1.25rem;
  padding-bottom: 2rem;
}
.bs-legend-dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}
.bs-legend-text {
  font-size: 0.65rem;
  color: #aab0c8;
  margin-left: 0.3rem;
  letter-spacing: 0.04em;
}

:global(.dark) .bs-page  { background: #0f1117; }
:global(.dark) .bs-title { color: #e8eaf6; }
:global(.dark) .bs-sub   { color: #4a5070; }
</style>
