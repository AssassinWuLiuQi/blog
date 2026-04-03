# Image Gallery Display Design

## Date
2026-04-03

## Topic
ImageGenerationView.vue 图片展示样式优化

## Problem Statement
当前 `ImageGenerationView.vue` 中图片使用 `object-fit: cover`，会导致图片被过度裁剪。用户希望看到完整的图片而不是被裁剪的部分。

## Design Decision

### Display Mode Change
| Item | Before | After |
|------|--------|-------|
| `object-fit` | `cover` (裁剪填满) | `contain` (完整显示) |
| Container | 无固定比例 | `aspect-ratio: 1/1` (正方形) |
| Layout | 图片决定容器大小 | 固定正方形容器 |
| Alignment | 各行高度不一 | 网格对齐整齐 |

### Visual Behavior
- 横向图片：左右留白，完整显示
- 竖向图片：上下留白，完整显示
- 正方形图片：刚好填满

### Implementation

**ImagePanel.vue 中的图片样式改动：**
```html
<!-- 容器添加 -->
<div class="aspect-square w-full overflow-hidden flex items-center justify-center bg-[#f2f4f6]">
  <img
    :src="url"
    :alt="`Generated image ${index + 1}`"
    class="w-full h-full object-contain"
  />
</div>
```

**关键 CSS：**
- `aspect-square` = `aspect-ratio: 1 / 1`
- `object-contain` = 图片完整显示，保持原始宽高比
- `flex items-center justify-center` = 图片在容器内居中

## Trade-offs
- **优点**：图片完整显示，网格整齐，无裁剪
- **缺点**：横向/竖向图片在正方形容器内会留白

## Status
Approved by user on 2026-04-03
