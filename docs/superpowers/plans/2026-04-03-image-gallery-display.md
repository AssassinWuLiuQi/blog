# Image Gallery Display Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `ImageGenerationView.vue` 图片从 `object-cover`（裁剪）改为 `object-contain`（完整显示），同时移除后端批量删除全部历史记录的功能。

**Architecture:**
- 前端改动仅涉及 `ImageGenerationView.vue` 中图片容器的 CSS 类名
- 后端移除 `ImageController.deleteAllHistory()` 方法

**Tech Stack:** Vue 3, Tailwind CSS, Spring Boot

---

## Task 1: 修改前端图片展示样式

**Files:**
- Modify: `frontend/src/views/ImageGenerationView.vue:126-153`

- [ ] **Step 1: 添加 aspect-square 工具类到 tailwind config**

文件: `frontend/tailwind.config.js`

在 `borderRadius` 节点下添加:
```js
aspectRatio: {
  'square': '1 / 1',
},
```

- [ ] **Step 2: 修改图片容器结构**

文件: `frontend/src/views/ImageGenerationView.vue:126-153`

找到当前代码:
```html
<div
  v-for="(url, index) in generatedImages"
  :key="index"
  class="relative group rounded-lg overflow-hidden"
>
  <img
    :src="url"
    :alt="`Generated image ${index + 1}`"
    class="w-full h-auto object-cover"
  />
```

改为:
```html
<div
  v-for="(url, index) in generatedImages"
  :key="index"
  class="relative group rounded-lg overflow-hidden aspect-square bg-[#f2f4f6]"
>
  <img
    :src="url"
    :alt="`Generated image ${index + 1}`"
    class="w-full h-full object-contain"
  />
```

**改动说明:**
- `aspect-square` = `aspect-ratio: 1/1` 正方形容器
- `bg-[#f2f4f6]` = 留白区域背景色
- `w-full h-full object-contain` = 图片完整显示，保持宽高比，居中

- [ ] **Step 3: 提交前端改动**

```bash
git add frontend/src/views/ImageGenerationView.vue frontend/tailwind.config.js
git commit -m "feat: change image display from cover to contain with square aspect ratio"
```

---

## Task 2: 移除后端批量删除历史记录功能

**Files:**
- Modify: `backend/src/main/java/com/scholarsmanuscript/controller/ImageController.java`

> **Note:** 用户确认不需要批量删除全部历史记录功能，移除此 endpoint。

- [ ] **Step 1: 移除 deleteAllHistory endpoint**

文件: `backend/src/main/java/com/scholarsmanuscript/controller/ImageController.java:46-50`

删除以下代码:
```java
@DeleteMapping("/history")
public ResponseEntity<ApiResponse<Void>> deleteAllHistory() {
    historyService.deleteAllHistory();
    return ResponseEntity.ok(ApiResponse.success(null));
}
```

- [ ] **Step 2: 移除 ImageGenerationHistoryService.deleteAllHistory 方法**

文件: `backend/src/main/java/com/scholarsmanuscript/service/ImageGenerationHistoryService.java`

删除以下方法:
```java
@Transactional
public void deleteAllHistory() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    Pageable pageable = PageRequest.of(0, 100);
    Page<ImageGenerationHistory> histories = historyRepository.findByUserIdAndNotDeleted(user.getId(), pageable);

    for (ImageGenerationHistory history : histories) {
        history.softDelete();
        historyRepository.save(history);
    }
    log.info("Soft deleted all image generation histories for user: {}", username);
}
```

- [ ] **Step 3: 提交后端改动**

```bash
git add backend/src/main/java/com/scholarsmanuscript/controller/ImageController.java backend/src/main/java/com/scholarsmanuscript/service/ImageGenerationHistoryService.java
git commit -m "feat: remove batch delete all history endpoint per user request"
```

---

## Verification

- [ ] 确认 `ImageGenerationView.vue` 中图片容器已添加 `aspect-square` 和 `bg-[#f2f4f6]`
- [ ] 确认图片元素已改为 `object-contain`
- [ ] 确认 `DELETE /api/image/history` 批量删除接口已移除
- [ ] 确认 `ImageGenerationHistoryService.deleteAllHistory()` 方法已移除
- [ ] 运行 `mvn clean compile` 确认后端编译通过
- [ ] 运行 `npm run dev` 确认前端编译通过
