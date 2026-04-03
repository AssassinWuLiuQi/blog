# Image History API Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** History Drawer 对接真实 API，支持分页下拉加载、Reuse 回填、Details 弹窗

**Tech Stack:** Vue 3, TypeScript, Element Plus, Spring Boot

---

## Task 1: 前端 HistoryItem 接口改造 + 分页加载

**Files:**
- Modify: `frontend/src/views/ImageGenerationView.vue`

---

- [ ] **Step 1: 更新 HistoryItem 接口**

文件: `frontend/src/views/ImageGenerationView.vue`

将原来的 `HistoryItem` interface 替换为:
```typescript
interface HistoryItem {
  id: number
  title: string
  date: string
  description: string
  thumbnail: string
  imageUrls: string[]
  model: string
  aspectRatio: string
  style: string
  successCount: number
  failedCount: number
  createdAt: string
}
```

---

- [ ] **Step 2: 添加分页状态和 API 方法**

在 `selectedIds` ref 下方添加:
```typescript
const currentPage = ref(0)
const pageSize = ref(10)
const hasMore = ref(true)
const loadingMore = ref(false)

// API call to fetch history
const fetchHistory = async (page: number, size: number): Promise<HistoryItem[]> => {
  const res = await fetch(`/api/image/history?page=${page}&size=${size}`)
  const json = await res.json()
  if (json.code === 200) {
    return json.data.content.map((item: any) => ({
      id: item.id,
      title: item.prompt.length > 30 ? item.prompt.substring(0, 30) + '...' : item.prompt,
      date: formatDate(item.createdAt),
      description: item.prompt,
      thumbnail: item.imageUrls && item.imageUrls.length > 0 ? item.imageUrls[0] : '',
      imageUrls: item.imageUrls || [],
      model: item.model,
      aspectRatio: item.aspectRatio,
      style: item.style,
      successCount: item.successCount,
      failedCount: item.failedCount,
      createdAt: item.createdAt
    }))
  }
  return []
}

// Date formatter: yyyy.MM.dd HH:mm
const formatDate = (dateStr: string): string => {
  const d = new Date(dateStr)
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
```

---

- [ ] **Step 3: 替换 historyList 为空数组，添加加载方法**

将 `historyList` 的 mock 数据替换为:
```typescript
const historyList = ref<HistoryItem[]>([])
```

添加加载方法:
```typescript
const loadHistory = async (): Promise<void> => {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  const items = await fetchHistory(currentPage.value, pageSize.value)
  if (items.length < pageSize.value) {
    hasMore.value = false
  }
  historyList.value.push(...items)
  currentPage.value++
  loadingMore.value = false
}
```

---

- [ ] **Step 4: 在 Drawer 打开时加载历史记录**

添加 watcher 在 drawer 打开时加载:
```typescript
import { watch } from 'vue'

watch(historyDrawerVisible, (visible) => {
  if (visible && historyList.value.length === 0) {
    currentPage.value = 0
    hasMore.value = true
    historyList.value = []
    loadHistory()
  }
})
```

---

- [ ] **Step 5: 添加下拉加载更多**

在 History List 的外层 div 上添加 `@scroll` 事件检测到底部:
```typescript
const handleHistoryScroll = (event: Event): void => {
  const el = event.target as HTMLElement
  const { scrollTop, scrollHeight, clientHeight } = el
  if (scrollHeight - scrollTop - clientHeight < 50 && !loadingMore.value && hasMore.value) {
    loadHistory()
  }
}
```

模板中 History List 外层 div (line ~220) 添加:
```html
<div class="flex-1 overflow-auto p-6" @scroll="handleHistoryScroll">
```

添加 loading 提示在列表底部:
```html
<div v-if="loadingMore" class="text-center py-4 text-[#424752] text-sm">
  加载中...
</div>
<div v-if="!hasMore && historyList.length > 0" class="text-center py-4 text-[#424752] text-sm">
  没有更多了
</div>
```

---

- [ ] **Step 6: 更新缩略图显示**

找到 Thumbnail div (line ~231-234)，修改为:
```html
<div class="w-24 h-24 rounded bg-[#e6e8ea] flex items-center justify-center shrink-0 overflow-hidden">
  <img
    v-if="item.thumbnail"
    :src="item.thumbnail"
    class="w-full h-full object-cover"
    alt="thumbnail"
  />
  <span v-else class="material-symbols-outlined text-4xl text-[#c2c6d4]">image</span>
</div>
```

---

- [ ] **Step 7: 更新右侧 Actions 按钮**

将原来的 Reuse/Details 按钮保持不变，但需要更新 item 参数类型以匹配新接口（实际上还是 HistoryItem 所以不需要改）。

---

- [ ] **Step 8: 编译验证**

```bash
cd frontend
npm run dev
```

Expected: 无编译错误

---

- [ ] **Step 9: 提交**

```bash
git add frontend/src/views/ImageGenerationView.vue
git commit -m "feat: integrate history API with pagination in ImageGenerationView"
```

---

## Task 2: Reuse 和 Details 功能实现

**Files:**
- Modify: `frontend/src/views/ImageGenerationView.vue`
- Create: 可能有新组件用于 Details Dialog（也可以直接在 Vue 里用 el-dialog）

---

- [ ] **Step 1: 添加 Details Dialog 状态**

在 `contextmenuVisible` 附近添加:
```typescript
const detailsDialogVisible = ref(false)
const currentDetailsItem = ref<HistoryItem | null>(null)

const openDetails = (item: HistoryItem): void => {
  currentDetailsItem.value = item
  detailsDialogVisible.value = true
}
```

---

- [ ] **Step 2: 修改 handleDetails 调用**

将 `@click="handleDetails(item)"` 改为 `@click="openDetails(item)"`

删除原来的 `handleDetails` 方法（如果不再需要）。

---

- [ ] **Step 3: 添加 Details el-dialog**

在 `</el-drawer>` 之后、`</AppLayout>` 之前添加:
```html
<!-- Details Dialog -->
<el-dialog
  v-model="detailsDialogVisible"
  title="生成详情"
  width="600px"
  :close-on-click-modal="true"
>
  <div v-if="currentDetailsItem" class="space-y-4">
    <!-- Prompt -->
    <div>
      <h4 class="text-sm font-semibold text-[#424752] mb-1">Prompt</h4>
      <p class="text-sm text-[#191c1e] bg-[#f7f9fb] p-3 rounded">{{ currentDetailsItem.description }}</p>
    </div>

    <!-- Images Grid -->
    <div v-if="currentDetailsItem.imageUrls && currentDetailsItem.imageUrls.length > 0">
      <h4 class="text-sm font-semibold text-[#424752] mb-2">生成图片</h4>
      <div class="grid grid-cols-2 gap-2">
        <img
          v-for="(url, idx) in currentDetailsItem.imageUrls"
          :key="idx"
          :src="url"
          class="w-full h-auto rounded object-contain bg-[#e6e8ea]"
          alt=""
        />
      </div>
    </div>

    <!-- Meta Info -->
    <div class="grid grid-cols-2 gap-4 text-sm">
      <div>
        <span class="text-[#424752]">模型：</span>
        <span class="text-[#191c1e]">{{ currentDetailsItem.model }}</span>
      </div>
      <div>
        <span class="text-[#424752]">比例：</span>
        <span class="text-[#191c1e]">{{ currentDetailsItem.aspectRatio }}</span>
      </div>
      <div>
        <span class="text-[#424752]">风格：</span>
        <span class="text-[#191c1e]">{{ currentDetailsItem.style || '-' }}</span>
      </div>
      <div>
        <span class="text-[#424752]">时间：</span>
        <span class="text-[#191c1e]">{{ currentDetailsItem.date }}</span>
      </div>
      <div>
        <span class="text-[#424752]">成功：</span>
        <span class="text-[#16a34a]">{{ currentDetailsItem.successCount }}</span>
      </div>
      <div>
        <span class="text-[#424752]">失败：</span>
        <span class="text-[#dc2626]">{{ currentDetailsItem.failedCount }}</span>
      </div>
    </div>
  </div>
</el-dialog>
```

---

- [ ] **Step 4: Reuse 功能 — 回填 prompt 到 ImagePanel**

需要暴露 ImagePanel 的 prompt 输入框 ref。但看当前架构，ImagePanel 是通过 `v-model="generatedImages"` 双向绑定的。

最简单的方式：在 ImageGenerationView 中定义一个共享的 prompt ref，通过 provide/inject 传递给 ImagePanel。或者直接在 ImagePanel 中通过 props 接收初始值。

实际上更简单的方式是：让 ImagePanel 暴露一个 `setPrompt` 方法或者用 Pinia store 来共享状态。

检查一下 ImagePanel 的实现：
```bash
cat frontend/src/components/image/ImagePanel.vue
```

如果 ImagePanel 已经有 prompt 相关的 ref，可以直接通过 `defineExpose` 暴露出来。

假设 ImagePanel 支持通过 props 设置初始 prompt，在 handleReuse 中关闭 drawer 并跳转即可:
```typescript
const handleReuse = (item: HistoryItem): void => {
  historyDrawerVisible.value = false
  // Navigate back to image panel with prompt - this depends on ImagePanel implementation
  console.log('Reuse prompt:', item.description)
}
```

用户说先用简单方式实现，Details 先按上面方式做。Reuse 功能如果 ImagePanel 有现成的 prompt 输入框则对接，否则先 console.log。

先按 console.log 方式实现，等 ImagePanel 改造后再对接。

---

- [ ] **Step 5: 编译验证**

```bash
cd frontend
npm run dev
```

Expected: 无编译错误

---

- [ ] **Step 6: 提交**

```bash
git add frontend/src/views/ImageGenerationView.vue
git commit -m "feat: add details dialog and reuse functionality to history drawer"
```

---

## Verification

- [ ] 打开 History Drawer 自动加载第一页数据
- [ ] 下拉到底部自动加载更多
- [ ] 缩略图显示第一张生成的图片
- [ ] 标题显示 prompt 前 30 字符
- [ ] 时间格式为 yyyy.MM.dd HH:mm
- [ ] 点击 Details 显示弹窗，包含完整信息
- [ ] 点击 Reuse 输出 prompt（后续对接 ImagePanel）
- [ ] 右键多选删除功能正常工作
- [ ] `npm run dev` 前端编译通过
