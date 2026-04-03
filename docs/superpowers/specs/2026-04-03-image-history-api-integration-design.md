# Image History API Integration Design

## Date
2026-04-03

## Topic
ImageGenerationView.vue History Drawer 对接真实后端 API

## Problem Statement
History Drawer 目前使用 mock 数据，需要对接 `GET /api/image/history` 分页查询接口，实现下拉加载更多。

## Design Decision

### Data Mapping

| Frontend Field | Backend Source |
|---------------|----------------|
| `id` | `id` |
| `title` | `prompt.substring(0, 30) + "..."` |
| `date` | `createdAt` formatted as `yyyy.MM.dd HH:mm` |
| `description` | Full `prompt` |
| `thumbnail` | `imageUrls[0]` (first image) |
| `imageUrls` | Full `imageUrls` array |

### Frontend Interface (HistoryItem)

```typescript
interface HistoryItem {
  id: number
  title: string      // prompt.substring(0, 30) + "..."
  date: string       // yyyy.MM.dd HH:mm
  description: string // full prompt
  thumbnail: string   // imageUrls[0] or ''
  imageUrls: string[] // full array for details view
  model: string
  aspectRatio: string
  style: string
  successCount: number
  failedCount: number
  createdAt: string
}
```

### API Integration

**Endpoint:** `GET /api/image/history?page=0&size=10`

**Response:** `{ code: 200, data: { content: HistoryItem[], totalPages, totalElements } }`

### Loading Behavior

| Scenario | Behavior |
|----------|----------|
| First open Drawer | Fetch `GET /api/image/history?page=0&size=10` |
| Scroll to bottom | Fetch `page+1`, append to list |
| All loaded | Stop loading more |

### Feature: Reuse

Click "Reuse" → Fill `prompt` into ImagePanel input field

### Feature: Details

Click "Details" → Show `el-dialog` with full info:
- Prompt (full text)
- Image URLs (all images in grid)
- Model, Aspect Ratio, Style
- Success/Failed counts
- Created time

## Status
Approved by user on 2026-04-03
