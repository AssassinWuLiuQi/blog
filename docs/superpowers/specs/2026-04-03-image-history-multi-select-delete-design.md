# Image History Multi-Select Delete Design

## Date
2026-04-03

## Topic
ImageGenerationView.vue 历史记录多选删除功能

## Problem Statement
当前历史记录只能单条删除，用户需要批量删除多条记录的功能。

## Design Decision

### Interaction Flow
1. **右键点击**任意历史记录 → 该记录变为选中状态（整行背景高亮）
2. **右键菜单**（el-contextmenu）显示两个选项：
   - 全选
   - 删除选中
3. 选中后点击「删除选中」→ 调用后端批量删除接口

### Visual Design

**正常状态:**
```
┌─────────────────────────────────────┐
│  [缩略图]  标题        日期          │
│           描述描述...               │
└─────────────────────────────────────┘
```

**选中状态（背景高亮）:**
```
┌─────────────────────────────────────┐
│  [缩略图]  标题        日期          │  ← 整行 bg-[#003f87]/10
│           描述描述...               │
└─────────────────────────────────────┘
```

### Components

| Component | Library | Usage |
|-----------|---------|-------|
| Drawer | el-drawer | 历史记录侧边栏 |
| Contextmenu | el-contextmenu | 右键菜单 |
| Button | el-button | 菜单操作按钮 |

### Backend API

**New endpoint:**
```
POST /api/image/history/batch-delete
Content-Type: application/json
Body: { "ids": [1, 2, 3] }

Response: 200 OK
{ "code": 200, "message": "success", "data": null }
```

**Implementation:**
- `ImageController.java`: Add `POST /api/image/history/batch-delete` endpoint
- `ImageGenerationHistoryService.java`: Add `deleteHistories(List<Long> ids)` method
- `ImageGenerationHistoryRepository.java`: Add `findByIdInAndUserId(List<Long> ids, Long userId)` query

### Frontend Changes

**Files:**
- `frontend/src/views/ImageGenerationView.vue`

**State:**
- `selectedIds: ref<Set<number>>(new Set())` — 选中记录 ID 集合

**Contextmenu:**
- `@contextmenu.prevent` 阻止默认菜单
- `el-contextmenu` 条件渲染，绑定当前右键项
- 菜单选项：`全选`、`删除选中`

**Selected style:**
- 选中项添加 `bg-[#003f87]/10` 背景色
- 通过 `:class` 条件绑定

### DTO (New)

**Request DTO:**
```java
// backend/src/main/java/com/scholarsmanuscript/dto/request/BatchDeleteRequest.java
public class BatchDeleteRequest {
    private List<Long> ids;
}
```

### Soft Delete
- 批量删除同样是软删除，复用 `softDelete()` 逻辑

## Status
Approved by user on 2026-04-03
