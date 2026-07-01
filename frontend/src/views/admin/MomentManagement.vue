<!-- frontend/src/views/admin/MomentManagement.vue -->
<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">手记管理</h1>
      <el-button type="primary" @click="editorRef.open()">
        <el-icon class="mr-2"><Plus /></el-icon>
        新建手记
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-4">
      <div class="flex gap-4">
        <el-input
          v-model="keyword"
          placeholder="搜索标题或内容..."
          clearable
          style="max-width: 320px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 批量操作 -->
    <div v-if="selected.length > 0" class="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-3 flex items-center justify-between">
      <span class="text-blue-700 dark:text-blue-300 text-sm">已选 {{ selected.length }} 条</span>
      <el-button size="small" type="danger" @click="batchRemove(selected)">批量删除</el-button>
    </div>

    <!-- 列表 -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
      <el-table v-loading="loading" :data="list" @selection-change="selected = $event" stripe row-key="id">
        <el-table-column type="selection" width="55" />

        <el-table-column label="内容" min-width="240">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <span class="font-medium text-gray-900 dark:text-gray-100 truncate">
                {{ row.title || row.content?.slice(0, 30) || '无标题' }}
              </span>
              <el-tag v-if="row.isTop" type="warning" size="small">置顶</el-tag>
            </div>
            <div class="text-xs text-gray-400 mt-0.5">
              {{ formatDate(row.createTime) }} · {{ row.images?.length || 0 }} 张图
              · {{ row.content?.length || 0 }} 字
            </div>
          </template>
        </el-table-column>

        <el-table-column label="标签" width="180">
          <template #default="{ row }">
            <el-tag v-for="tag in (row.tags || [])" :key="tag" size="small" class="mr-1 mb-1">{{ tag }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="推荐度" width="140" align="center">
          <template #default="{ row }">
            <el-dropdown trigger="click" @command="(lv) => changeLevel(row, lv)">
              <el-tag :type="levelTagType(row.recommendLevel)" class="cursor-pointer">
                {{ levelLabel(row.recommendLevel) }}
              </el-tag>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="lv in [0,1,2,3,4,5]"
                    :key="lv"
                    :command="lv"
                    :disabled="lv === row.recommendLevel"
                  >
                    {{ levelLabel(lv) }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>

        <el-table-column prop="viewCount" label="浏览" width="70" align="center" />
        <el-table-column prop="likeCount" label="点赞" width="70" align="center" />

        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'info'" size="small">
              {{ row.status === 0 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <div class="flex space-x-1">
              <el-button link type="primary" @click="handleView(row)">查看</el-button>
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button link @click="toggleTop(row)">
                {{ row.isTop ? '取消置顶' : '置顶' }}
              </el-button>
              <el-button link @click="toggleStatus(row)" :type="row.status === 0 ? 'warning' : 'success'">
                {{ row.status === 0 ? '下架' : '发布' }}
              </el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="p-4 flex justify-end">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>

    <MomentEditor ref="editorRef" @saved="loadData" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import MomentEditor from '@/components/admin/MomentEditor.vue'
import { momentApi } from '@/api/moment'
import { useArticleAdminActions } from '@/composables/useArticleAdminActions'

const router = useRouter()

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const selected = ref([])
const editorRef = ref()

const { levelLabel, levelTagType, changeLevel, toggleStatus, toggleTop, remove, batchRemove } =
  useArticleAdminActions({ reload: loadData, entityLabel: '手记' })

async function loadData() {
  loading.value = true
  try {
    const res = await momentApi.adminPage({
      page: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined
    })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  loadData()
}

function handleReset() {
  keyword.value = ''
  page.value = 1
  loadData()
}

function handleEdit(row) {
  editorRef.value.open(row)
}

function handleView(row) {
  router.push(`/article/${row.id}`)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

onMounted(loadData)
</script>
