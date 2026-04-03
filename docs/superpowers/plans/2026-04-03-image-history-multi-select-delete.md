# Image History Multi-Select Delete Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现历史记录多选删除功能，使用 Element Plus el-contextmenu 实现右键菜单

**Architecture:**
- 前端：在 ImageGenerationView.vue 中添加选中状态和右键菜单
- 后端：新增批量删除接口，复用软删除逻辑

**Tech Stack:** Vue 3, Element Plus, Spring Boot

---

## Task 1: 后端新增批量删除接口

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/dto/request/BatchDeleteRequest.java`
- Modify: `backend/src/main/java/com/scholarsmanuscript/controller/ImageController.java`
- Modify: `backend/src/main/java/com/scholarsmanuscript/service/ImageGenerationHistoryService.java`
- Modify: `backend/src/main/java/com/scholarsmanuscript/repository/ImageGenerationHistoryRepository.java`

---

- [ ] **Step 1: 创建 BatchDeleteRequest DTO**

文件: `backend/src/main/java/com/scholarsmanuscript/dto/request/BatchDeleteRequest.java`

```java
package com.scholarsmanuscript.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class BatchDeleteRequest {
    @NotEmpty(message = "ids cannot be empty")
    private List<Long> ids;
}
```

---

- [ ] **Step 2: Repository 添加批量查询方法**

文件: `backend/src/main/java/com/scholarsmanuscript/repository/ImageGenerationHistoryRepository.java`

在接口中添加:
```java
List<ImageGenerationHistory> findByIdInAndUserIdAndNotDeleted(List<Long> ids, Long userId);
```

---

- [ ] **Step 3: Service 添加批量删除方法**

文件: `backend/src/main/java/com/scholarsmanuscript/service/ImageGenerationHistoryService.java`

添加方法:
```java
@Transactional
public void deleteHistories(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
        return;
    }
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    List<ImageGenerationHistory> histories = historyRepository.findByIdInAndUserIdAndNotDeleted(ids, user.getId());
    for (ImageGenerationHistory history : histories) {
        history.softDelete();
        historyRepository.save(history);
    }
    log.info("Batch soft deleted {} image generation histories for user: {}", histories.size(), username);
}
```

---

- [ ] **Step 4: Controller 添加批量删除接口**

文件: `backend/src/main/java/com/scholarsmanuscript/controller/ImageController.java`

添加 import:
```java
import com.scholarsmanuscript.dto.request.BatchDeleteRequest;
```

添加方法:
```java
@PostMapping("/history/batch-delete")
public ResponseEntity<ApiResponse<Void>> batchDeleteHistory(
        @Valid @RequestBody BatchDeleteRequest request) {
    historyService.deleteHistories(request.getIds());
    return ResponseEntity.ok(ApiResponse.success(null));
}
```

---

- [ ] **Step 5: 编译验证后端**

```bash
cd backend
mvn clean compile
```

Expected: BUILD SUCCESS

---

- [ ] **Step 6: 提交后端改动**

```bash
git add backend/src/main/java/com/scholarsmanuscript/
git commit -m "feat: add batch delete history endpoint"
```

---

## Task 2: 前端多选删除功能

**Files:**
- Modify: `frontend/src/views/ImageGenerationView.vue`

---

- [ ] **Step 1: 添加选中状态 ref**

文件: `frontend/src/views/ImageGenerationView.vue`

在 `historyList` ref 下方添加:
```typescript
const selectedIds = ref<Set<number>>(new Set())
```

---

- [ ] **Step 2: 添加右键菜单状态和方法**

在 `handleDetails` 方法下方添加:
```typescript
const contextmenuVisible = ref(false)
const contextmenuPosition = ref({ x: 0, y: 0 })

const handleContextMenu = (event: MouseEvent, item: HistoryItem): void => {
  event.preventDefault()
  selectedIds.value.clear()
  selectedIds.value.add(item.id)
  contextmenuPosition.value = { x: event.clientX, y: event.clientY }
  contextmenuVisible.value = true
}

const handleSelectAll = (): void => {
  selectedIds.value = new Set(historyList.value.map(item => item.id))
  contextmenuVisible.value = false
}

const handleDeleteSelected = async (): Promise<void> => {
  if (selectedIds.value.size === 0) return
  try {
    await fetch('/api/image/history/batch-delete', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ids: Array.from(selectedIds.value) })
    })
    // Remove deleted items from list
    historyList.value = historyList.value.filter(item => !selectedIds.value.has(item.id))
    selectedIds.value.clear()
    contextmenuVisible.value = false
  } catch (error) {
    console.error('Delete failed:', error)
  }
}

const isSelected = (id: number): boolean => selectedIds.value.has(id)
```

---

- [ ] **Step 3: 给历史记录列表添加右键事件和选中样式**

找到 history list 的 div，添加 `@contextmenu` 和 `:class`:

```html
<div
  v-for="item in historyList"
  :key="item.id"
  class="bg-white rounded-lg border border-[#c2c6d4]/10 p-4 cursor-pointer transition-colors"
  :class="{ 'bg-[#003f87]/10 border-[#003f87]/30': isSelected(item.id) }"
  @contextmenu="handleContextMenu($event, item)"
>
```

---

- [ ] **Step 4: 添加 el-contextmenu 右键菜单**

在 `</el-drawer>` 之前添加:

```html
<!-- Right-click Context Menu -->
<el-contextmenu
  v-model:visible="contextmenuVisible"
  :x="contextmenuPosition.x"
  :y="contextmenuPosition.y"
  :key="`${contextmenuPosition.x}-${contextmenuPosition.y}`"
>
  <el-contextmenu-item @click="handleSelectAll">
    全选
  </el-contextmenu-item>
  <el-contextmenu-item @click="handleDeleteSelected" :disabled="selectedIds.size === 0">
    删除选中 ({{ selectedIds.size }})
  </el-contextmenu-item>
</el-contextmenu>
```

---

- [ ] **Step 5: 确保 el-contextmenu 已全局注册**

检查 `frontend/src/main.ts` 是否已注册 Element Plus:

```typescript
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

app.use(ElementPlus)
```

el-contextmenu 是 Element Plus 内置组件，全局注册后可直接使用。

---

- [ ] **Step 6: 验证前端编译**

```bash
cd frontend
npm run dev
```

Expected: 无编译错误

---

- [ ] **Step 7: 提交前端改动**

```bash
git add frontend/src/views/ImageGenerationView.vue
git commit -m "feat: add multi-select delete for image history with right-click context menu"
```

---

## Verification

- [ ] `POST /api/image/history/batch-delete` 接口可正常调用
- [ ] 右键历史记录显示 el-contextmenu 菜单
- [ ] 「全选」选项可选中列表所有记录
- [ ] 「删除选中」选项调用批量删除接口并从列表移除
- [ ] 选中项整行背景变为 `bg-[#003f87]/10`
- [ ] `mvn clean compile` 后端编译通过
- [ ] `npm run dev` 前端编译通过
