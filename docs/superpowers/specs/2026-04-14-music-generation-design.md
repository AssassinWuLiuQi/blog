# Music Generation Feature — Design Spec

**Date:** 2026-04-14
**Branch:** backend-implementation

---

## Overview

Add a dedicated Music Generation page (`/music-generation`) that integrates MiniMax's lyrics and music generation APIs. The page follows the same layout pattern as `ImageGenerationView.vue`.

---

## Page Layout

Route: `/music-generation` (requires auth)
Side nav: new entry with `music_note` icon, label "音乐生成"

```
AppLayout (section-title="音乐生成")
├── MusicPanel (w-1/3)        — left: control panel with two tabs
└── Results Area (w-2/3)      — right: audio player or lyrics display
    └── History Drawer        — slide-in panel, same as image history
```

---

## Frontend

### New Files

| File | Purpose |
|------|---------|
| `frontend/src/views/MusicGenerationView.vue` | Page view, mirrors ImageGenerationView layout |
| `frontend/src/components/music/MusicPanel.vue` | Left panel with Tab 1 / Tab 2 |
| `frontend/src/types/music.ts` | TypeScript types |
| `frontend/src/utils/musicApi.ts` | API call utilities |

### Tab 1 — 歌词生成

- Input: theme textarea (e.g. "一首欢乐的新年歌曲")
- Submit → `POST /api/music/lyrics`
- Result rendered in right panel as formatted lyrics text with a copy button
- No linkage to Tab 2 (independent)

### Tab 2 — 音乐生成（一键）

- Input: style description textarea (e.g. `Mandopop, Festive, Upbeat`)
- Optional: lyrics textarea — leave blank to let the backend auto-generate lyrics first
- Model selector: `music-2.6` (default) / `music-2.5`
- Instrumental toggle: maps to `is_instrumental` parameter
- Submit → `POST /api/music/generate`
- Result: HTML5 audio player appears in the right panel with the MinIO-hosted MP3 URL

### Right Panel States

- **Empty**: decorative empty state (same style as image module)
- **Lyrics result**: formatted text block, copy button
- **Audio result**: HTML5 `<audio>` element with controls, download button
- **Loading**: spinner overlay

### History Drawer

- Triggered by "View All History" button (top-right of results area)
- Lists past generations with: style prompt, model, date, play button
- Infinite scroll pagination (same pattern as image history)
- Context menu: delete selected

---

## Backend

### New Files

| File | Purpose |
|------|---------|
| `MusicController.java` | REST endpoints |
| `MusicService.java` | MiniMax API calls + MinIO upload + history |
| `MusicGenerationHistory.java` | JPA entity |
| `MusicGenerationHistoryRepository.java` | JPA repository |
| `LyricsGenerationRequest.java` | DTO |
| `LyricsGenerationResponse.java` | DTO |
| `MusicGenerationRequest.java` | DTO |
| `MusicGenerationResponse.java` | DTO |
| `MusicHistoryResponse.java` | DTO |

### API Endpoints

```
POST /api/music/lyrics          # Generate lyrics from a theme prompt
POST /api/music/generate        # Generate music (auto-lyrics if none provided)
GET  /api/music/history         # Paginated history (page, size params)
DELETE /api/music/history/{id}  # Soft delete a history record
```

Security: all endpoints require JWT auth (covered by existing `anyRequest().authenticated()`).

### MusicService Logic

**`generateLyrics(request)`**
1. Call `POST https://api.minimaxi.com/v1/lyrics_generation` with `{ mode, prompt }`
2. Return lyrics string

**`generateMusic(request)`**
1. If `lyrics` is blank: call lyrics generation first with `prompt` as theme
2. Call `POST https://api.minimaxi.com/v1/music_generation` with `{ model, prompt, lyrics, is_instrumental, audio_setting, output_format: "url" }`
3. MiniMax returns an audio URL → upload to MinIO → replace with MinIO URL
4. Save to `MusicGenerationHistory`
5. Return `MusicGenerationResponse`

### MusicGenerationHistory Entity

```
id            BIGINT PK
user_id       FK → users
prompt        TEXT          (style description)
lyrics        TEXT
audio_url     VARCHAR(512)  (MinIO URL)
model         VARCHAR(50)
is_instrumental BOOLEAN
status_code   INT
status_msg    VARCHAR(255)
task_id       VARCHAR(100)
deleted_at    DATETIME      (soft delete)
created_at    DATETIME
```

### MiniMax API Parameters

**Lyrics generation** (`/v1/lyrics_generation`):
```json
{ "mode": "write_full_song", "prompt": "<theme>" }
```

**Music generation** (`/v1/music_generation`):
```json
{
  "model": "music-2.6",
  "prompt": "<style description>",
  "lyrics": "<structured lyrics>",
  "is_instrumental": false,
  "audio_setting": { "sample_rate": 44100, "bitrate": 256000, "format": "mp3" },
  "output_format": "url"
}
```

Uses existing `@Value("${miniMax.api-key}")` and `OkHttpUtil` — same pattern as `ImageService`.

---

## Data Flow

```
User (Tab 2) → MusicPanel → POST /api/music/generate
  → MusicService.generateMusic()
      → [if no lyrics] → MiniMax /v1/lyrics_generation
      → MiniMax /v1/music_generation
      → MinioService.uploadFromUrl()
      → MusicGenerationHistoryRepository.save()
  → MusicGenerationResponse { audioUrl, statusCode, statusMsg }
→ MusicPanel renders <audio> player
```

---

## Out of Scope

- Waveform visualization (plain HTML5 audio controls only)
- Batch music generation
- Sharing / public playback links
