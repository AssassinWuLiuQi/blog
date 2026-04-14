# Music Generation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a `/music-generation` page with two independent tabs — lyrics generation (Tab 1) and one-click music generation (Tab 2) — backed by MiniMax's APIs.

**Architecture:** Backend follows the existing Image module pattern (Controller → Service → Repository). Music generation uses `output_format: "url"` then uploads the MP3 to MinIO. Tab 2 uses `lyrics_optimizer: true` when lyrics is blank instead of calling the lyrics API separately.

**Tech Stack:** Spring Boot 3.3 / Java 21 / JPA / OkHttp / FastJSON2 / MinIO (backend) · Vue 3 / TypeScript / Tailwind CSS / Element Plus (frontend)

---

## File Map

### Backend — New Files

| Path | Responsibility |
|------|----------------|
| `backend/src/main/java/com/scholarsmanuscript/dto/request/LyricsGenerationRequest.java` | DTO for POST /api/music/lyrics |
| `backend/src/main/java/com/scholarsmanuscript/dto/request/MusicGenerationRequest.java` | DTO for POST /api/music/generate |
| `backend/src/main/java/com/scholarsmanuscript/dto/response/LyricsGenerationResponse.java` | Lyrics result DTO |
| `backend/src/main/java/com/scholarsmanuscript/dto/response/MusicGenerationResponse.java` | Music result DTO (audioUrl, statusCode, statusMsg) |
| `backend/src/main/java/com/scholarsmanuscript/dto/response/MusicHistoryResponse.java` | History list item DTO |
| `backend/src/main/java/com/scholarsmanuscript/entity/MusicGenerationHistory.java` | JPA entity |
| `backend/src/main/java/com/scholarsmanuscript/repository/MusicGenerationHistoryRepository.java` | JPA repository |
| `backend/src/main/java/com/scholarsmanuscript/service/MusicService.java` | MiniMax calls + MinIO + history |
| `backend/src/main/java/com/scholarsmanuscript/controller/MusicController.java` | REST endpoints |

### Backend — Modified Files

| Path | Change |
|------|--------|
| `backend/src/main/java/com/scholarsmanuscript/service/MinioService.java` | Add `uploadAudioFromUrl(String url)` method |

### Frontend — New Files

| Path | Responsibility |
|------|----------------|
| `frontend/src/types/music.ts` | TypeScript types |
| `frontend/src/utils/musicApi.ts` | API call utilities |
| `frontend/src/components/music/MusicPanel.vue` | Left panel with two tabs |
| `frontend/src/views/MusicGenerationView.vue` | Page view |

### Frontend — Modified Files

| Path | Change |
|------|--------|
| `frontend/src/router/index.ts` | Add `/music-generation` route |
| `frontend/src/components/layout/SideNavBar.vue` | Add "音乐生成" nav item |

---

## Task 1: Backend DTOs

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/dto/request/LyricsGenerationRequest.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/dto/request/MusicGenerationRequest.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/dto/response/LyricsGenerationResponse.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/dto/response/MusicGenerationResponse.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/dto/response/MusicHistoryResponse.java`

- [ ] **Step 1: Create LyricsGenerationRequest**

```java
// backend/src/main/java/com/scholarsmanuscript/dto/request/LyricsGenerationRequest.java
package com.scholarsmanuscript.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LyricsGenerationRequest {

    @NotBlank(message = "prompt is required")
    private String prompt;

    private String mode = "write_full_song";
}
```

- [ ] **Step 2: Create MusicGenerationRequest**

```java
// backend/src/main/java/com/scholarsmanuscript/dto/request/MusicGenerationRequest.java
package com.scholarsmanuscript.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MusicGenerationRequest {

    private String model = "music-2.6";

    @NotBlank(message = "prompt is required")
    private String prompt;

    private String lyrics;

    private boolean isInstrumental = false;
}
```

- [ ] **Step 3: Create LyricsGenerationResponse**

```java
// backend/src/main/java/com/scholarsmanuscript/dto/response/LyricsGenerationResponse.java
package com.scholarsmanuscript.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LyricsGenerationResponse {

    private String lyrics;
    private Integer statusCode;
    private String statusMsg;
}
```

- [ ] **Step 4: Create MusicGenerationResponse**

```java
// backend/src/main/java/com/scholarsmanuscript/dto/response/MusicGenerationResponse.java
package com.scholarsmanuscript.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MusicGenerationResponse {

    private String audioUrl;
    private Integer statusCode;
    private String statusMsg;
    private String traceId;
}
```

- [ ] **Step 5: Create MusicHistoryResponse**

```java
// backend/src/main/java/com/scholarsmanuscript/dto/response/MusicHistoryResponse.java
package com.scholarsmanuscript.dto.response;

import com.scholarsmanuscript.entity.MusicGenerationHistory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MusicHistoryResponse {

    private Long id;
    private String prompt;
    private String lyrics;
    private String audioUrl;
    private String model;
    private boolean isInstrumental;
    private Integer statusCode;
    private String statusMsg;
    private LocalDateTime createdAt;

    public static MusicHistoryResponse fromEntity(MusicGenerationHistory entity) {
        return MusicHistoryResponse.builder()
                .id(entity.getId())
                .prompt(entity.getPrompt())
                .lyrics(entity.getLyrics())
                .audioUrl(entity.getAudioUrl())
                .model(entity.getModel())
                .isInstrumental(entity.isInstrumental())
                .statusCode(entity.getStatusCode())
                .statusMsg(entity.getStatusMsg())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
```

- [ ] **Step 6: Compile to verify**

```bash
cd backend && mvn clean compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/scholarsmanuscript/dto/
git commit -m "feat(music): add music generation DTOs"
```

---

## Task 2: Backend Entity + Repository

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/entity/MusicGenerationHistory.java`
- Create: `backend/src/main/java/com/scholarsmanuscript/repository/MusicGenerationHistoryRepository.java`

- [ ] **Step 1: Create MusicGenerationHistory entity**

```java
// backend/src/main/java/com/scholarsmanuscript/entity/MusicGenerationHistory.java
package com.scholarsmanuscript.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "music_generation_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MusicGenerationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    @Column(columnDefinition = "TEXT")
    private String lyrics;

    @Column(name = "audio_url", length = 512)
    private String audioUrl;

    @Column(length = 50, nullable = false)
    private String model;

    @Column(name = "is_instrumental", nullable = false)
    private boolean isInstrumental = false;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "status_msg", length = 255)
    private String statusMsg;

    @Column(name = "trace_id", length = 100)
    private String traceId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: Create MusicGenerationHistoryRepository**

```java
// backend/src/main/java/com/scholarsmanuscript/repository/MusicGenerationHistoryRepository.java
package com.scholarsmanuscript.repository;

import com.scholarsmanuscript.entity.MusicGenerationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MusicGenerationHistoryRepository extends JpaRepository<MusicGenerationHistory, Long> {

    @Query("SELECT h FROM MusicGenerationHistory h WHERE h.user.id = :userId AND h.deletedAt IS NULL ORDER BY h.createdAt DESC")
    Page<MusicGenerationHistory> findByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT h FROM MusicGenerationHistory h WHERE h.id = :id AND h.user.id = :userId AND h.deletedAt IS NULL")
    Optional<MusicGenerationHistory> findByIdAndUserIdAndNotDeleted(@Param("id") Long id, @Param("userId") Long userId);
}
```

- [ ] **Step 3: Compile to verify**

```bash
cd backend && mvn clean compile -q
```

Expected: BUILD SUCCESS

Note: Spring Boot with JPA `spring.jpa.hibernate.ddl-auto=update` (or `create-drop` for dev/H2) will auto-create the `music_generation_history` table on startup.

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/scholarsmanuscript/entity/MusicGenerationHistory.java
git add backend/src/main/java/com/scholarsmanuscript/repository/MusicGenerationHistoryRepository.java
git commit -m "feat(music): add MusicGenerationHistory entity and repository"
```

---

## Task 3: MinioService — Add uploadAudioFromUrl

**Files:**
- Modify: `backend/src/main/java/com/scholarsmanuscript/service/MinioService.java`

- [ ] **Step 1: Add uploadAudioFromUrl method**

Open `MinioService.java` and add this method after the existing `uploadFromUrl` method (around line 95):

```java
public String uploadAudioFromUrl(String audioUrl) {
    try {
        log.info("Downloading audio from: {}", audioUrl);
        URL url = new URL(audioUrl);
        byte[] audioBytes;

        try (InputStream in = url.openStream()) {
            audioBytes = in.readAllBytes();
        }

        log.info("Downloaded {} bytes, uploading audio to MinIO", audioBytes.length);

        String objectName = "audio/" + UUID.randomUUID() + ".mp3";
        try (ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes)) {
            upload(bais, objectName, "audio/mpeg", audioBytes.length);
        }

        String presignedUrl = getPresignedUrl(objectName);
        log.info("Successfully uploaded audio to MinIO: {}", objectName);
        return presignedUrl;
    } catch (IOException e) {
        log.error("Failed to upload audio from URL: {}", audioUrl, e);
        throw new RuntimeException("Failed to upload audio: " + e.getMessage(), e);
    }
}
```

- [ ] **Step 2: Compile to verify**

```bash
cd backend && mvn clean compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/scholarsmanuscript/service/MinioService.java
git commit -m "feat(music): add uploadAudioFromUrl to MinioService"
```

---

## Task 4: MusicService

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/service/MusicService.java`

- [ ] **Step 1: Create MusicService**

```java
// backend/src/main/java/com/scholarsmanuscript/service/MusicService.java
package com.scholarsmanuscript.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.scholarsmanuscript.dto.request.LyricsGenerationRequest;
import com.scholarsmanuscript.dto.request.MusicGenerationRequest;
import com.scholarsmanuscript.dto.response.LyricsGenerationResponse;
import com.scholarsmanuscript.dto.response.MusicGenerationResponse;
import com.scholarsmanuscript.dto.response.MusicHistoryResponse;
import com.scholarsmanuscript.entity.MusicGenerationHistory;
import com.scholarsmanuscript.entity.User;
import com.scholarsmanuscript.exception.BusinessException;
import com.scholarsmanuscript.exception.ErrorCode;
import com.scholarsmanuscript.repository.MusicGenerationHistoryRepository;
import com.scholarsmanuscript.repository.UserRepository;
import com.scholarsmanuscript.utils.OkHttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicService {

    private static final String MINIMAX_API_BASE = "https://api.minimaxi.com/v1";

    @Value("${miniMax.api-key:}")
    private String apiKey;

    private final MinioService minioService;
    private final MusicGenerationHistoryRepository historyRepository;
    private final UserRepository userRepository;

    // ==================== Lyrics Generation ====================

    public LyricsGenerationResponse generateLyrics(LyricsGenerationRequest request) {
        log.info("Calling MiniMax Lyrics Generation API, prompt: {}", request.getPrompt());

        Map<String, Object> body = new HashMap<>();
        body.put("mode", request.getMode());
        body.put("prompt", request.getPrompt());

        Request httpRequest = new Request.Builder()
                .url(MINIMAX_API_BASE + "/lyrics_generation")
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(
                        JSON.toJSONString(body),
                        okhttp3.MediaType.parse("application/json; charset=utf-8")))
                .build();

        try {
            Map<String, Object> responseMap = OkHttpUtil.execute(httpRequest, Map.class);
            return parseLyricsResponse(responseMap);
        } catch (IOException e) {
            log.error("Error calling MiniMax Lyrics Generation API", e);
            return LyricsGenerationResponse.builder()
                    .statusCode(500)
                    .statusMsg("API call failed: " + e.getMessage())
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    private LyricsGenerationResponse parseLyricsResponse(Map<String, Object> responseMap) {
        LyricsGenerationResponse.LyricsGenerationResponseBuilder builder = LyricsGenerationResponse.builder();

        Map<String, Object> baseResp = (Map<String, Object>) responseMap.get("base_resp");
        if (baseResp != null) {
            builder.statusCode((Integer) baseResp.get("status_code"));
            builder.statusMsg((String) baseResp.get("status_msg"));
        }

        // MiniMax lyrics API returns lyrics in data.text
        Object data = responseMap.get("data");
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            String lyrics = (String) dataMap.get("text");
            if (lyrics == null) {
                lyrics = (String) dataMap.get("lyrics");
            }
            builder.lyrics(lyrics);
        }

        return builder.build();
    }

    // ==================== Music Generation ====================

    public MusicGenerationResponse generateMusic(MusicGenerationRequest request) {
        log.info("Calling MiniMax Music Generation API, model: {}, prompt: {}",
                request.getModel(), request.getPrompt());

        Map<String, Object> body = new HashMap<>();
        body.put("model", request.getModel());
        body.put("prompt", request.getPrompt());
        body.put("output_format", "url");
        body.put("is_instrumental", request.isInstrumental());

        Map<String, Object> audioSetting = new HashMap<>();
        audioSetting.put("sample_rate", 44100);
        audioSetting.put("bitrate", 256000);
        audioSetting.put("format", "mp3");
        body.put("audio_setting", audioSetting);

        String lyrics = request.getLyrics();
        if (lyrics != null && !lyrics.isBlank()) {
            body.put("lyrics", lyrics);
        } else if (!request.isInstrumental()) {
            // no lyrics provided and not instrumental → let MiniMax auto-generate
            body.put("lyrics_optimizer", true);
        }

        Request httpRequest = new Request.Builder()
                .url(MINIMAX_API_BASE + "/music_generation")
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(
                        JSON.toJSONString(body),
                        okhttp3.MediaType.parse("application/json; charset=utf-8")))
                .build();

        MusicGenerationResponse response;
        try {
            Map<String, Object> responseMap = OkHttpUtil.execute(httpRequest, Map.class);
            response = parseMusicResponse(responseMap);
        } catch (IOException e) {
            log.error("Error calling MiniMax Music Generation API", e);
            response = MusicGenerationResponse.builder()
                    .statusCode(500)
                    .statusMsg("API call failed: " + e.getMessage())
                    .build();
        }

        // Upload audio to MinIO
        if (response.getStatusCode() != null && response.getStatusCode() == 0
                && response.getAudioUrl() != null && !response.getAudioUrl().isBlank()) {
            try {
                String minioUrl = minioService.uploadAudioFromUrl(response.getAudioUrl());
                response.setAudioUrl(minioUrl);
                log.info("Uploaded audio to MinIO: {}", minioUrl);
            } catch (Exception e) {
                log.error("Failed to upload audio to MinIO", e);
                response.setStatusCode(500);
                response.setStatusMsg("Failed to upload audio: " + e.getMessage());
            }
        }

        saveHistory(request, response);
        return response;
    }

    @SuppressWarnings("unchecked")
    private MusicGenerationResponse parseMusicResponse(Map<String, Object> responseMap) {
        MusicGenerationResponse.MusicGenerationResponseBuilder builder = MusicGenerationResponse.builder();

        Map<String, Object> baseResp = (Map<String, Object>) responseMap.get("base_resp");
        if (baseResp != null) {
            builder.statusCode((Integer) baseResp.get("status_code"));
            builder.statusMsg((String) baseResp.get("status_msg"));
        }

        builder.traceId((String) responseMap.get("trace_id"));

        // data.audio contains the URL when output_format is "url"
        Object data = responseMap.get("data");
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            builder.audioUrl((String) dataMap.get("audio"));
        }

        return builder.build();
    }

    // ==================== History ====================

    @Transactional
    public void saveHistory(MusicGenerationRequest request, MusicGenerationResponse response) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        MusicGenerationHistory history = MusicGenerationHistory.builder()
                .user(user)
                .prompt(request.getPrompt())
                .lyrics(request.getLyrics())
                .audioUrl(response.getAudioUrl())
                .model(request.getModel())
                .isInstrumental(request.isInstrumental())
                .statusCode(response.getStatusCode())
                .statusMsg(response.getStatusMsg())
                .traceId(response.getTraceId())
                .build();

        historyRepository.save(history);
        log.info("Saved music generation history for user: {}", username);
    }

    public Page<MusicHistoryResponse> getHistory(int page, int size) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);
        return historyRepository.findByUserIdAndNotDeleted(user.getId(), pageable)
                .map(MusicHistoryResponse::fromEntity);
    }

    @Transactional
    public void deleteHistory(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        MusicGenerationHistory history = historyRepository
                .findByIdAndUserIdAndNotDeleted(id, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "History not found"));

        history.softDelete();
        historyRepository.save(history);
        log.info("Soft deleted music history id: {} for user: {}", id, username);
    }
}
```

- [ ] **Step 2: Compile to verify**

```bash
cd backend && mvn clean compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/scholarsmanuscript/service/MusicService.java
git commit -m "feat(music): add MusicService with lyrics and music generation"
```

---

## Task 5: MusicController

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/controller/MusicController.java`

- [ ] **Step 1: Create MusicController**

```java
// backend/src/main/java/com/scholarsmanuscript/controller/MusicController.java
package com.scholarsmanuscript.controller;

import com.scholarsmanuscript.dto.request.LyricsGenerationRequest;
import com.scholarsmanuscript.dto.request.MusicGenerationRequest;
import com.scholarsmanuscript.dto.response.ApiResponse;
import com.scholarsmanuscript.dto.response.LyricsGenerationResponse;
import com.scholarsmanuscript.dto.response.MusicGenerationResponse;
import com.scholarsmanuscript.dto.response.MusicHistoryResponse;
import com.scholarsmanuscript.service.MusicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {

    private final MusicService musicService;

    @PostMapping("/lyrics")
    public ResponseEntity<ApiResponse<LyricsGenerationResponse>> generateLyrics(
            @Valid @RequestBody LyricsGenerationRequest request) {
        LyricsGenerationResponse response = musicService.generateLyrics(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<MusicGenerationResponse>> generateMusic(
            @Valid @RequestBody MusicGenerationRequest request) {
        MusicGenerationResponse response = musicService.generateMusic(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<MusicHistoryResponse>>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MusicHistoryResponse> histories = musicService.getHistory(page, size);
        return ResponseEntity.ok(ApiResponse.success(histories));
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHistory(@PathVariable Long id) {
        musicService.deleteHistory(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
```

- [ ] **Step 2: Compile to verify**

```bash
cd backend && mvn clean compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Start the backend and verify endpoints exist**

```bash
cd backend && mvn spring-boot:run &
sleep 10
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/music/history \
  -H "Authorization: Bearer invalid"
```

Expected: `401` (endpoint exists but returns 401 without valid token)

Kill the background process after verification.

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/scholarsmanuscript/controller/MusicController.java
git commit -m "feat(music): add MusicController with lyrics/generate/history endpoints"
```

---

## Task 6: Frontend Types + API Utils

**Files:**
- Create: `frontend/src/types/music.ts`
- Create: `frontend/src/utils/musicApi.ts`

- [ ] **Step 1: Create music.ts types**

```typescript
// frontend/src/types/music.ts

export interface LyricsGenerationRequest {
  prompt: string
  mode?: string
}

export interface LyricsGenerationResponse {
  lyrics: string | null
  statusCode: number
  statusMsg: string
}

export interface MusicGenerationRequest {
  model: string
  prompt: string
  lyrics?: string
  isInstrumental: boolean
}

export interface MusicGenerationResponse {
  audioUrl: string | null
  statusCode: number
  statusMsg: string
  traceId: string | null
}

export interface MusicHistoryItem {
  id: number
  prompt: string
  lyrics: string | null
  audioUrl: string | null
  model: string
  isInstrumental: boolean
  statusCode: number
  statusMsg: string
  createdAt: string
  // computed display fields
  displayDate: string
  displayTitle: string
}
```

- [ ] **Step 2: Create musicApi.ts**

```typescript
// frontend/src/utils/musicApi.ts
import { fetchWithAuth } from './api'
import type {
  LyricsGenerationRequest,
  LyricsGenerationResponse,
  MusicGenerationRequest,
  MusicGenerationResponse,
  MusicHistoryItem
} from '@/types/music'

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api'

export async function generateLyrics(
  request: LyricsGenerationRequest
): Promise<LyricsGenerationResponse> {
  const response = await fetchWithAuth(`${API_BASE}/music/lyrics`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request)
  })
  const result = await response.json()
  if (!response.ok) {
    throw new Error(result.message || 'Lyrics generation failed')
  }
  return result.data
}

export async function generateMusic(
  request: MusicGenerationRequest
): Promise<MusicGenerationResponse> {
  const response = await fetchWithAuth(`${API_BASE}/music/generate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request)
  })
  const result = await response.json()
  if (!response.ok) {
    throw new Error(result.message || 'Music generation failed')
  }
  return result.data
}

const formatDate = (dateStr: string): string => {
  const d = new Date(dateStr)
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

export async function fetchMusicHistory(page: number, size: number): Promise<MusicHistoryItem[]> {
  try {
    const res = await fetchWithAuth(`${API_BASE}/music/history?page=${page}&size=${size}`)
    if (!res.ok) return []
    const json = await res.json()
    if (json.code !== 200) return []
    return json.data.content.map((item: any): MusicHistoryItem => ({
      id: item.id,
      prompt: item.prompt,
      lyrics: item.lyrics,
      audioUrl: item.audioUrl,
      model: item.model,
      isInstrumental: item.isInstrumental,
      statusCode: item.statusCode,
      statusMsg: item.statusMsg,
      createdAt: item.createdAt,
      displayDate: formatDate(item.createdAt),
      displayTitle: item.prompt.length > 30 ? item.prompt.substring(0, 30) + '...' : item.prompt
    }))
  } catch {
    return []
  }
}

export async function deleteMusicHistory(id: number): Promise<void> {
  await fetchWithAuth(`${API_BASE}/music/history/${id}`, { method: 'DELETE' })
}
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/types/music.ts frontend/src/utils/musicApi.ts
git commit -m "feat(music): add frontend types and API utilities"
```

---

## Task 7: MusicPanel Component

**Files:**
- Create: `frontend/src/components/music/MusicPanel.vue`

- [ ] **Step 1: Create MusicPanel.vue**

```vue
<!-- frontend/src/components/music/MusicPanel.vue -->
<script setup lang="ts">
import { ref } from 'vue'
import { generateLyrics, generateMusic } from '@/utils/musicApi'
import type { LyricsGenerationResponse, MusicGenerationResponse } from '@/types/music'

const emit = defineEmits<{
  'lyrics-result': [lyrics: string]
  'music-result': [audioUrl: string]
  'loading': [loading: boolean]
}>()

const activeTab = ref<'lyrics' | 'music'>('lyrics')

// --- Tab 1: Lyrics ---
const lyricsPrompt = ref('')
const lyricsLoading = ref(false)
const lyricsError = ref('')

const handleGenerateLyrics = async () => {
  if (!lyricsPrompt.value.trim()) return
  lyricsLoading.value = true
  lyricsError.value = ''
  emit('loading', true)
  try {
    const res: LyricsGenerationResponse = await generateLyrics({
      prompt: lyricsPrompt.value.trim(),
      mode: 'write_full_song'
    })
    if (res.statusCode === 0 && res.lyrics) {
      emit('lyrics-result', res.lyrics)
    } else {
      lyricsError.value = res.statusMsg || '生成失败'
    }
  } catch (e: any) {
    lyricsError.value = e.message || '请求失败'
  } finally {
    lyricsLoading.value = false
    emit('loading', false)
  }
}

// --- Tab 2: Music ---
const musicPrompt = ref('')
const musicLyrics = ref('')
const musicModel = ref('music-2.6')
const isInstrumental = ref(false)
const musicLoading = ref(false)
const musicError = ref('')

const handleGenerateMusic = async () => {
  if (!musicPrompt.value.trim()) return
  musicLoading.value = true
  musicError.value = ''
  emit('loading', true)
  try {
    const res: MusicGenerationResponse = await generateMusic({
      model: musicModel.value,
      prompt: musicPrompt.value.trim(),
      lyrics: musicLyrics.value.trim() || undefined,
      isInstrumental: isInstrumental.value
    })
    if (res.statusCode === 0 && res.audioUrl) {
      emit('music-result', res.audioUrl)
    } else {
      musicError.value = res.statusMsg || '生成失败'
    }
  } catch (e: any) {
    musicError.value = e.message || '请求失败'
  } finally {
    musicLoading.value = false
    emit('loading', false)
  }
}
</script>

<template>
  <div class="h-full flex flex-col border-r border-[#c2c6d4]/10 bg-white">
    <!-- Tab Header -->
    <div class="shrink-0 flex border-b border-[#c2c6d4]/10">
      <button
        v-for="tab in [{ key: 'lyrics', label: '歌词生成' }, { key: 'music', label: '音乐生成' }]"
        :key="tab.key"
        class="flex-1 py-3 text-sm font-medium transition-colors"
        :class="activeTab === tab.key
          ? 'text-[#003f87] border-b-2 border-[#003f87]'
          : 'text-[#424752] hover:text-[#191c1e]'"
        @click="activeTab = tab.key as 'lyrics' | 'music'"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- Tab 1: Lyrics Generation -->
    <div v-if="activeTab === 'lyrics'" class="flex-1 overflow-auto p-6 flex flex-col gap-4">
      <div>
        <label class="block text-xs font-semibold text-[#424752] mb-2 tracking-wide uppercase">
          主题描述
        </label>
        <textarea
          v-model="lyricsPrompt"
          rows="4"
          placeholder="例如：一首欢乐的新年歌曲"
          class="w-full px-3 py-2 text-sm text-[#191c1e] bg-[#f7f9fb] border border-[#c2c6d4]/20 rounded-lg resize-none focus:outline-none focus:border-[#003f87]/50 transition-colors"
        />
      </div>

      <p v-if="lyricsError" class="text-xs text-red-500">{{ lyricsError }}</p>

      <button
        class="w-full py-2.5 rounded-xl text-sm font-semibold text-white transition-all"
        :class="lyricsLoading || !lyricsPrompt.trim()
          ? 'bg-[#003f87]/40 cursor-not-allowed'
          : 'bg-gradient-to-r from-[#003f87] to-[#0056b3] hover:opacity-90'"
        :disabled="lyricsLoading || !lyricsPrompt.trim()"
        @click="handleGenerateLyrics"
      >
        <span v-if="lyricsLoading" class="flex items-center justify-center gap-2">
          <span class="material-symbols-outlined text-base animate-spin">progress_activity</span>
          生成中...
        </span>
        <span v-else>生成歌词</span>
      </button>
    </div>

    <!-- Tab 2: Music Generation -->
    <div v-if="activeTab === 'music'" class="flex-1 overflow-auto p-6 flex flex-col gap-4">
      <div>
        <label class="block text-xs font-semibold text-[#424752] mb-2 tracking-wide uppercase">
          风格描述
        </label>
        <textarea
          v-model="musicPrompt"
          rows="3"
          placeholder="例如：Mandopop, Festive, Upbeat"
          class="w-full px-3 py-2 text-sm text-[#191c1e] bg-[#f7f9fb] border border-[#c2c6d4]/20 rounded-lg resize-none focus:outline-none focus:border-[#003f87]/50 transition-colors"
        />
      </div>

      <div>
        <label class="block text-xs font-semibold text-[#424752] mb-2 tracking-wide uppercase">
          歌词（可选，留空自动生成）
        </label>
        <textarea
          v-model="musicLyrics"
          rows="5"
          placeholder="[Verse]\n..."
          class="w-full px-3 py-2 text-sm text-[#191c1e] bg-[#f7f9fb] border border-[#c2c6d4]/20 rounded-lg resize-none focus:outline-none focus:border-[#003f87]/50 transition-colors"
        />
      </div>

      <div>
        <label class="block text-xs font-semibold text-[#424752] mb-2 tracking-wide uppercase">
          模型
        </label>
        <select
          v-model="musicModel"
          class="w-full px-3 py-2 text-sm text-[#191c1e] bg-[#f7f9fb] border border-[#c2c6d4]/20 rounded-lg focus:outline-none focus:border-[#003f87]/50"
        >
          <option value="music-2.6">music-2.6（推荐）</option>
          <option value="music-2.6-free">music-2.6-free（免费）</option>
        </select>
      </div>

      <label class="flex items-center gap-2 cursor-pointer select-none">
        <div
          class="w-10 h-5 rounded-full transition-colors relative"
          :class="isInstrumental ? 'bg-[#003f87]' : 'bg-[#c2c6d4]/40'"
          @click="isInstrumental = !isInstrumental"
        >
          <div
            class="absolute top-0.5 w-4 h-4 bg-white rounded-full shadow transition-transform"
            :class="isInstrumental ? 'translate-x-5' : 'translate-x-0.5'"
          />
        </div>
        <span class="text-sm text-[#424752]">纯音乐（无人声）</span>
      </label>

      <p v-if="musicError" class="text-xs text-red-500">{{ musicError }}</p>

      <button
        class="w-full py-2.5 rounded-xl text-sm font-semibold text-white transition-all"
        :class="musicLoading || !musicPrompt.trim()
          ? 'bg-[#003f87]/40 cursor-not-allowed'
          : 'bg-gradient-to-r from-[#003f87] to-[#0056b3] hover:opacity-90'"
        :disabled="musicLoading || !musicPrompt.trim()"
        @click="handleGenerateMusic"
      >
        <span v-if="musicLoading" class="flex items-center justify-center gap-2">
          <span class="material-symbols-outlined text-base animate-spin">progress_activity</span>
          生成中...
        </span>
        <span v-else>开始生成</span>
      </button>
    </div>
  </div>
</template>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/music/MusicPanel.vue
git commit -m "feat(music): add MusicPanel component with lyrics and music tabs"
```

---

## Task 8: MusicGenerationView + Router + Nav

**Files:**
- Create: `frontend/src/views/MusicGenerationView.vue`
- Modify: `frontend/src/router/index.ts`
- Modify: `frontend/src/components/layout/SideNavBar.vue`

- [ ] **Step 1: Create MusicGenerationView.vue**

```vue
<!-- frontend/src/views/MusicGenerationView.vue -->
<script setup lang="ts">
import { ref, watch } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import MusicPanel from '@/components/music/MusicPanel.vue'
import { fetchMusicHistory, deleteMusicHistory } from '@/utils/musicApi'
import type { MusicHistoryItem } from '@/types/music'

// Result state
type ResultMode = 'empty' | 'lyrics' | 'audio'
const resultMode = ref<ResultMode>('empty')
const currentLyrics = ref('')
const currentAudioUrl = ref('')
const isLoading = ref(false)

// History drawer
const historyDrawerVisible = ref(false)
const historyList = ref<MusicHistoryItem[]>([])
const currentPage = ref(0)
const pageSize = ref(10)
const hasMore = ref(true)
const loadingMore = ref(false)
const refreshing = ref(false)

const handleLyricsResult = (lyrics: string) => {
  currentLyrics.value = lyrics
  resultMode.value = 'lyrics'
}

const handleMusicResult = (audioUrl: string) => {
  currentAudioUrl.value = audioUrl
  resultMode.value = 'audio'
}

const copyLyrics = async () => {
  await navigator.clipboard.writeText(currentLyrics.value)
}

const downloadAudio = () => {
  const a = document.createElement('a')
  a.href = currentAudioUrl.value
  a.download = 'generated-music.mp3'
  a.target = '_blank'
  a.click()
}

const loadHistory = async () => {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    const items = await fetchMusicHistory(currentPage.value, pageSize.value)
    if (items.length < pageSize.value) hasMore.value = false
    historyList.value.push(...items)
    currentPage.value++
  } finally {
    loadingMore.value = false
  }
}

const handleRefresh = async () => {
  if (refreshing.value) return
  refreshing.value = true
  currentPage.value = 0
  hasMore.value = true
  historyList.value = []
  try {
    const items = await fetchMusicHistory(0, pageSize.value)
    if (items.length < pageSize.value) hasMore.value = false
    historyList.value = items
    currentPage.value = 1
  } finally {
    refreshing.value = false
  }
}

const handleDeleteHistory = async (id: number) => {
  await deleteMusicHistory(id)
  historyList.value = historyList.value.filter(item => item.id !== id)
}

const handleHistoryScroll = (event: Event) => {
  const el = event.target as HTMLElement
  const { scrollTop, scrollHeight, clientHeight } = el
  if (scrollHeight - scrollTop - clientHeight < 50 && !loadingMore.value && hasMore.value) {
    loadHistory()
  }
}

const playFromHistory = (item: MusicHistoryItem) => {
  if (item.audioUrl) {
    currentAudioUrl.value = item.audioUrl
    resultMode.value = 'audio'
    historyDrawerVisible.value = false
  }
}

watch(historyDrawerVisible, (visible) => {
  if (visible) {
    currentPage.value = 0
    hasMore.value = true
    historyList.value = []
    loadHistory()
  }
})
</script>

<template>
  <AppLayout section-title="音乐生成">
    <div class="flex flex-1 h-[calc(100vh-4rem)]">
      <!-- Left: MusicPanel (1/3) -->
      <MusicPanel
        class="w-1/3"
        @lyrics-result="handleLyricsResult"
        @music-result="handleMusicResult"
        @loading="isLoading = $event"
      />

      <!-- Right: Results (2/3) -->
      <div class="flex-1 w-2/3 bg-white p-8 flex flex-col">
        <!-- Header -->
        <div class="flex items-center justify-between mb-6 shrink-0">
          <div class="flex items-center gap-3">
            <div class="w-2 h-8 rounded-full bg-gradient-to-b from-[#003f87] to-[#0056b3]"></div>
            <h2 class="text-2xl font-semibold text-[#003f87]">生成结果</h2>
          </div>
          <button
            class="flex items-center gap-1 text-[#003f87] text-sm font-medium hover:opacity-80 transition-opacity"
            @click="historyDrawerVisible = true"
          >
            <span>View All History</span>
            <span class="material-symbols-outlined text-base">chevron_right</span>
          </button>
        </div>

        <!-- Result Area -->
        <div class="flex-1 overflow-auto relative">
          <!-- Loading overlay -->
          <div
            v-if="isLoading"
            class="absolute inset-0 flex items-center justify-center bg-white/70 z-10 rounded-2xl"
          >
            <div class="flex flex-col items-center gap-3">
              <span class="material-symbols-outlined text-5xl text-[#003f87] animate-spin">progress_activity</span>
              <p class="text-sm text-[#424752]">正在生成，请稍候...</p>
            </div>
          </div>

          <!-- Empty state -->
          <div
            v-if="resultMode === 'empty' && !isLoading"
            class="h-full flex flex-col items-center justify-center rounded-2xl bg-[#f2f4f6]/5 border-2 border-[#c2c6d4]/10"
          >
            <div class="relative mb-6">
              <div class="w-24 h-24 rounded-2xl bg-gradient-to-br from-[#003f87]/10 to-[#0056b3]/10 flex items-center justify-center">
                <span class="material-symbols-outlined text-6xl text-[#003f87]/20">music_note</span>
              </div>
              <div class="absolute -top-2 -left-2 w-4 h-4 border-l-2 border-t-2 border-[#003f87]/20 rounded-tl-lg"></div>
              <div class="absolute -bottom-2 -right-2 w-4 h-4 border-r-2 border-b-2 border-[#003f87]/20 rounded-br-lg"></div>
            </div>
            <h3 class="text-2xl font-medium text-[#191c1e] mb-3">No music yet</h3>
            <p class="text-base text-[#424752] max-w-md text-center mb-8">
              使用左侧面板生成歌词或音乐。
            </p>
            <div class="flex items-center gap-3">
              <div class="w-16 h-1 rounded-full bg-[#c2c6d4]/30"></div>
              <div class="w-8 h-1 rounded-full bg-[#003f87]/20"></div>
              <div class="w-16 h-1 rounded-full bg-[#c2c6d4]/30"></div>
            </div>
          </div>

          <!-- Lyrics result -->
          <div v-if="resultMode === 'lyrics'" class="h-full flex flex-col">
            <div class="flex items-center justify-between mb-4">
              <span class="text-sm font-semibold text-[#424752]">Generated Lyrics</span>
              <button
                class="flex items-center gap-1 text-xs font-semibold text-[#003f87] hover:bg-[#003f87]/5 px-3 py-1.5 rounded transition-colors"
                @click="copyLyrics"
              >
                <span class="material-symbols-outlined text-base">content_copy</span>
                复制
              </button>
            </div>
            <div class="flex-1 overflow-auto bg-[#f7f9fb] rounded-xl p-6">
              <pre class="text-sm text-[#191c1e] whitespace-pre-wrap leading-relaxed font-sans">{{ currentLyrics }}</pre>
            </div>
          </div>

          <!-- Audio result -->
          <div v-if="resultMode === 'audio'" class="h-full flex flex-col items-center justify-center gap-6">
            <div class="w-32 h-32 rounded-2xl bg-gradient-to-br from-[#003f87]/10 to-[#0056b3]/10 flex items-center justify-center">
              <span class="material-symbols-outlined text-6xl text-[#003f87]/60">music_note</span>
            </div>
            <p class="text-sm text-[#424752]">音乐生成完成</p>
            <audio
              :src="currentAudioUrl"
              controls
              class="w-full max-w-lg"
            />
            <button
              class="flex items-center gap-2 px-4 py-2 text-sm font-semibold text-[#003f87] border border-[#003f87]/20 rounded-lg hover:bg-[#003f87]/5 transition-colors"
              @click="downloadAudio"
            >
              <span class="material-symbols-outlined text-base">download</span>
              下载 MP3
            </button>
          </div>
        </div>
      </div>

      <!-- History Drawer -->
      <el-drawer
        v-model="historyDrawerVisible"
        title=""
        direction="rtl"
        size="480px"
        :with-header="false"
      >
        <div class="h-full flex flex-col overflow-hidden">
          <div class="shrink-0 flex items-center justify-between px-6 py-5 border-b border-[#c2c6d4]/10">
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded-lg bg-[#003f87]/10 flex items-center justify-center">
                <span class="material-symbols-outlined text-[#003f87]">history</span>
              </div>
              <h2 class="text-xl font-semibold text-[#191c1e]">Generation History</h2>
            </div>
            <div class="flex items-center gap-1">
              <button
                class="w-8 h-8 rounded-full hover:bg-[#c2c6d4]/10 flex items-center justify-center transition-colors disabled:opacity-50"
                :disabled="refreshing"
                @click="handleRefresh"
              >
                <span class="material-symbols-outlined text-lg text-[#424752]" :class="{ 'animate-spin': refreshing }">refresh</span>
              </button>
              <button
                class="w-8 h-8 rounded-full hover:bg-[#c2c6d4]/10 flex items-center justify-center transition-colors"
                @click="historyDrawerVisible = false"
              >
                <span class="material-symbols-outlined text-lg text-[#424752]">close</span>
              </button>
            </div>
          </div>

          <div class="flex-1 overflow-auto p-6" @scroll="handleHistoryScroll">
            <div class="flex flex-col gap-4">
              <div
                v-for="item in historyList"
                :key="item.id"
                class="bg-white rounded-lg border border-[#c2c6d4]/10 p-4"
              >
                <div class="flex items-start justify-between gap-2 mb-2">
                  <span class="text-xs font-bold text-[#003f87] tracking-wide truncate">{{ item.displayTitle }}</span>
                  <span class="text-xs text-[#424752] shrink-0">{{ item.displayDate }}</span>
                </div>
                <div class="flex items-center gap-2 text-xs text-[#424752] mb-3">
                  <span>{{ item.model }}</span>
                  <span v-if="item.isInstrumental" class="text-[#003f87]">· 纯音乐</span>
                </div>
                <div class="flex items-center gap-2">
                  <button
                    v-if="item.audioUrl"
                    class="px-3 py-1 text-xs font-semibold text-[#003f87] hover:bg-[#003f87]/5 rounded transition-colors"
                    @click="playFromHistory(item)"
                  >
                    播放
                  </button>
                  <button
                    class="px-3 py-1 text-xs font-semibold text-red-500 hover:bg-red-50 rounded transition-colors"
                    @click="handleDeleteHistory(item.id)"
                  >
                    删除
                  </button>
                </div>
              </div>

              <div v-if="loadingMore" class="text-center py-4 text-[#424752] text-sm">加载中...</div>
              <div v-if="!hasMore && historyList.length > 0" class="text-center py-4 text-[#424752] text-sm">没有更多了</div>
              <div v-if="!loadingMore && historyList.length === 0" class="text-center py-8 text-[#424752] text-sm">暂无历史记录</div>
            </div>
          </div>
        </div>
      </el-drawer>
    </div>
  </AppLayout>
</template>
```

- [ ] **Step 2: Add route to router/index.ts**

Open `frontend/src/router/index.ts`. After the `/chat` route entry (around line 80), add:

```typescript
  {
    path: '/music-generation',
    name: 'music-generation',
    component: () => import('@/views/MusicGenerationView.vue'),
    meta: { requiresAuth: true }
  },
```

- [ ] **Step 3: Add nav item to SideNavBar.vue**

Open `frontend/src/components/layout/SideNavBar.vue`. In the `navItems` array (around line 33), add after the `/image-generation` entry:

```typescript
  { path: '/music-generation', icon: 'music_note', label: '音乐生成', exact: false },
```

- [ ] **Step 4: Start frontend dev server and verify page loads**

```bash
cd frontend && npm run dev
```

Open `http://localhost:5173/music-generation` in browser.

Expected:
- Page loads with "音乐生成" title
- Two tabs visible: "歌词生成" and "音乐生成"
- Nav shows "音乐生成" entry
- Empty state shows music note icon

- [ ] **Step 5: Commit**

```bash
git add frontend/src/views/MusicGenerationView.vue
git add frontend/src/router/index.ts
git add frontend/src/components/layout/SideNavBar.vue
git commit -m "feat(music): add MusicGenerationView, route, and nav entry"
```

---

## Self-Review

**Spec coverage check:**
- ✅ `/music-generation` page with AppLayout — Task 8
- ✅ Two independent tabs — Task 7 (MusicPanel)
- ✅ Tab 1: theme prompt → `/api/music/lyrics` — Tasks 1, 4, 6, 7
- ✅ Tab 2: one-click with optional lyrics, model selector, instrumental toggle → Tasks 1, 4, 6, 7
- ✅ Right panel: empty state, lyrics result, audio player — Task 8
- ✅ History drawer with pagination and delete — Task 8
- ✅ Backend: Controller, Service, Entity, Repository, DTOs — Tasks 1–5
- ✅ MinIO audio upload — Task 3
- ✅ `lyrics_optimizer: true` when no lyrics provided — Task 4 (MusicService)
- ✅ Nav entry with `music_note` icon — Task 8
- ✅ Soft delete — Task 2 (entity), Task 4 (service)

**Placeholder scan:** No TBD, TODO, or vague instructions. All code is complete.

**Type consistency:**
- `MusicGenerationRequest.isInstrumental` → used in MusicService as `request.isInstrumental()` ✅
- `MusicHistoryResponse.fromEntity(entity)` → entity fields match MusicGenerationHistory ✅
- `musicApi.ts` maps `item.audioUrl`, `item.isInstrumental` from backend JSON camelCase ✅
- `MusicPanel` emits `lyrics-result` and `music-result` → `MusicGenerationView` listens to both ✅
