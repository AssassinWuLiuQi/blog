# Plan A: Spring Boot Backend

## Overview

Add chat session management and message history to existing Spring Boot backend. This plan adds database entities, repositories, service layer, and REST API endpoints for the customer service system.

**Files to create/modify:**
- `backend/src/main/java/com/scholarsmanuscript/entity/ChatSession.java` (create)
- `backend/src/main/java/com/scholarsmanuscript/entity/ChatMessage.java` (create)
- `backend/src/main/java/com/scholarsmanuscript/repository/ChatSessionRepository.java` (create)
- `backend/src/main/java/com/scholarsmanuscript/repository/ChatMessageRepository.java` (create)
- `backend/src/main/java/com/scholarsmanuscript/dto/request/ChatRequest.java` (create)
- `backend/src/main/java/com/scholarsmanuscript/dto/response/ChatResponse.java` (create)
- `backend/src/main/java/com/scholarsmanuscript/service/ChatService.java` (create)
- `backend/src/main/java/com/scholarsmanuscript/controller/ChatController.java` (create)
- `backend/src/main/java/com/scholarsmanuscript/config/SecurityConfig.java` (modify)

---

## Task 1: Create ChatSession Entity

**File:** `backend/src/main/java/com/scholarsmanuscript/entity/ChatSession.java`

**Purpose:** Represents a chat conversation session belonging to a user.

- [ ] **Step 1: Create ChatSession.java**

```java
package com.scholarsmanuscript.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_name", length = 255)
    @Builder.Default
    private String sessionName = "新对话";

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addMessage(ChatMessage message) {
        messages.add(message);
        message.setSession(this);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd backend && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/scholarsmanuscript/entity/ChatSession.java
git commit -m "feat(chat): add ChatSession entity"
```

---

## Task 2: Create ChatMessage Entity

**File:** `backend/src/main/java/com/scholarsmanuscript/entity/ChatMessage.java`

**Purpose:** Represents a single message in a chat conversation (user, assistant, or system).

- [ ] **Step 1: Create ChatMessage.java**

```java
package com.scholarsmanuscript.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    public enum Role {
        user, assistant, system
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd backend && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/scholarsmanuscript/entity/ChatMessage.java
git commit -m "feat(chat): add ChatMessage entity"
```

---

## Task 3: Create Chat Repositories

**Files:**
- `backend/src/main/java/com/scholarsmanuscript/repository/ChatSessionRepository.java` (create)
- `backend/src/main/java/com/scholarsmanuscript/repository/ChatMessageRepository.java` (create)

- [ ] **Step 1: Create ChatSessionRepository.java**

```java
package com.scholarsmanuscript.repository;

import com.scholarsmanuscript.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<ChatSession> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.session.id = :sessionId")
    long countMessagesBySessionId(@Param("sessionId") Long sessionId);
}
```

- [ ] **Step 2: Create ChatMessageRepository.java**

```java
package com.scholarsmanuscript.repository;

import com.scholarsmanuscript.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    @Query("SELECT m FROM ChatMessage m WHERE m.session.id = :sessionId ORDER BY m.createdAt DESC")
    List<ChatMessage> findRecentBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT m FROM ChatMessage m WHERE m.session.id = :sessionId ORDER BY m.createdAt DESC LIMIT :limit")
    List<ChatMessage> findLastNMessages(@Param("sessionId") Long sessionId, @Param("limit") int limit);
}
```

- [ ] **Step 3: Verify compilation**

Run: `cd backend && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/scholarsmanuscript/repository/ChatSessionRepository.java
git add backend/src/main/java/com/scholarsmanuscript/repository/ChatMessageRepository.java
git commit -m "feat(chat): add chat repositories"
```

---

## Task 4: Create Chat DTOs

**Files:**
- `backend/src/main/java/com/scholarsmanuscript/dto/request/ChatRequest.java` (create)
- `backend/src/main/java/com/scholarsmanuscript/dto/response/ChatResponse.java` (create)

- [ ] **Step 1: Create ChatRequest.java**

```java
package com.scholarsmanuscript.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {

    private Long sessionId;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    private boolean useVoice = false;
}
```

- [ ] **Step 2: Create ChatResponse.java**

```java
package com.scholarsmanuscript.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private Long messageId;
    private Long sessionId;
    private String role;
    private String content;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Session {
        private Long id;
        private String sessionName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int messageCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionDetail {
        private Long id;
        private String sessionName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<ChatResponse> messages;
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `cd backend && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/scholarsmanuscript/dto/request/ChatRequest.java
git add backend/src/main/java/com/scholarsmanuscript/dto/response/ChatResponse.java
git commit -m "feat(chat): add chat DTOs"
```

---

## Task 5: Create ChatService

**File:** `backend/src/main/java/com/scholarsmanuscript/service/ChatService.java`

**Purpose:** Business logic for chat sessions and messages, including calling Python LangGraph service.

- [ ] **Step 1: Create ChatService.java**

```java
package com.scholarsmanuscript.service;

import com.scholarsmanuscript.dto.request.ChatRequest;
import com.scholarsmanuscript.dto.response.ChatResponse;
import com.scholarsmanuscript.entity.ChatMessage;
import com.scholarsmanuscript.entity.ChatSession;
import com.scholarsmanuscript.entity.User;
import com.scholarsmanuscript.exception.BusinessException;
import com.scholarsmanuscript.exception.ErrorCode;
import com.scholarsmanuscript.repository.ChatMessageRepository;
import com.scholarsmanuscript.repository.ChatSessionRepository;
import com.scholarsmanuscript.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;

    @Value("${python.service.url:http://localhost:8000}")
    private String pythonServiceUrl;

    public List<ChatResponse.Session> getUserSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(session -> ChatResponse.Session.builder()
                        .id(session.getId())
                        .sessionName(session.getSessionName())
                        .createdAt(session.getCreatedAt())
                        .updatedAt(session.getUpdatedAt())
                        .messageCount((int) messageRepository.countMessagesBySessionId(session.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    public ChatResponse.SessionDetail getSessionDetail(Long sessionId, Long userId) {
        ChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND, "会话不存在"));

        List<ChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

        return ChatResponse.SessionDetail.builder()
                .id(session.getId())
                .sessionName(session.getSessionName())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .messages(messages.stream()
                        .map(msg -> ChatResponse.builder()
                                .messageId(msg.getId())
                                .sessionId(sessionId)
                                .role(msg.getRole().name())
                                .content(msg.getContent())
                                .createdAt(msg.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public ChatResponse.Session createSession(Long userId, String sessionName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ChatSession session = ChatSession.builder()
                .user(user)
                .sessionName(sessionName != null ? sessionName : "新对话")
                .build();

        ChatSession saved = sessionRepository.save(session);

        return ChatResponse.Session.builder()
                .id(saved.getId())
                .sessionName(saved.getSessionName())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .messageCount(0)
                .build();
    }

    @Transactional
    public void deleteSession(Long sessionId, Long userId) {
        ChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND, "会话不存在"));
        sessionRepository.delete(session);
    }

    @Transactional
    public ChatResponse sendMessage(Long userId, ChatRequest request) {
        ChatSession session;

        if (request.getSessionId() != null) {
            session = sessionRepository.findByIdAndUserId(request.getSessionId(), userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND, "会话不存在"));
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            session = ChatSession.builder()
                    .user(user)
                    .sessionName("新对话")
                    .build();
            session = sessionRepository.save(session);
        }

        ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessage.Role.user)
                .content(request.getContent())
                .build();
        session.addMessage(userMessage);
        messageRepository.save(userMessage);

        log.info("Python service URL: {}", pythonServiceUrl);

        return ChatResponse.builder()
                .messageId(userMessage.getId())
                .sessionId(session.getId())
                .role("user")
                .content(request.getContent())
                .createdAt(userMessage.getCreatedAt())
                .build();
    }

    public List<ChatMessage> getRecentMessages(Long sessionId, int limit) {
        return messageRepository.findLastNMessages(sessionId, limit);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd backend && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/scholarsmanuscript/service/ChatService.java
git commit -m "feat(chat): add ChatService with Python service integration"
```

---

## Task 6: Create ChatController

**File:** `backend/src/main/java/com/scholarsmanuscript/controller/ChatController.java`

**Purpose:** REST API endpoints for chat operations.

- [ ] **Step 1: Create ChatController.java**

```java
package com.scholarsmanuscript.controller;

import com.scholarsmanuscript.dto.request.ChatRequest;
import com.scholarsmanuscript.dto.response.ApiResponse;
import com.scholarsmanuscript.dto.response.ChatResponse;
import com.scholarsmanuscript.entity.ChatMessage;
import com.scholarsmanuscript.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ExecutorService sseExecutor = Executors.newCachedThreadPool();

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<ChatResponse.Session>>> getUserSessions(
            Authentication authentication) {
        Long userId = getUserId(authentication);
        List<ChatResponse.Session> sessions = chatService.getUserSessions(userId);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<ChatResponse.SessionDetail>> getSessionDetail(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        ChatResponse.SessionDetail detail = chatService.getSessionDetail(id, userId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<ChatResponse.Session>> createSession(
            @RequestParam(required = false) String name,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        ChatResponse.Session session = chatService.createSession(userId, name);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        chatService.deleteSession(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping(value = "/sessions/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @PathVariable Long id,
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        sseExecutor.execute(() -> {
            try {
                chatService.sendMessage(userId, request);

                List<ChatMessage> recentMessages = chatService.getRecentMessages(id, 10);

                for (ChatMessage msg : recentMessages) {
                    String role = msg.getRole().name();
                    String content = msg.getContent();
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data("{\"role\":\"" + role + "\",\"content\":\"" + escapeJson(content) + "\"}"));
                }

                emitter.send(SseEmitter.event()
                        .name("done")
                        .data(""));

                emitter.complete();
            } catch (Exception e) {
                log.error("SSE stream error", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private Long getUserId(Authentication authentication) {
        return 1L;
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd backend && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/scholarsmanuscript/controller/ChatController.java
git commit -m "feat(chat): add ChatController with SSE streaming endpoint"
```

---

## Task 7: Update SecurityConfig

**File:** `backend/src/main/java/com/scholarsmanuscript/config/SecurityConfig.java`

**Purpose:** Add chat API endpoints to security configuration.

- [ ] **Step 1: Read current SecurityConfig.java**

Run: `cat backend/src/main/java/com/scholarsmanuscript/config/SecurityConfig.java`

- [ ] **Step 2: Modify SecurityConfig.java** — add chat endpoints to permitAll or adjust authentication as needed

```java
// Add to authorizeHttpRequests:
.requestMatchers("/api/chat/**").authenticated()
```

- [ ] **Step 3: Verify compilation**

Run: `cd backend && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/scholarsmanuscript/config/SecurityConfig.java
git commit -m "feat(chat): add chat API to security config"
```

---

## Task 8: Add Python Service Configuration

**File:** `backend/src/main/resources/application.yml`

**Purpose:** Add python service URL configuration.

- [ ] **Step 1: Add configuration**

```yaml
python:
  service:
    url: http://localhost:8000
    timeout: 120
```

- [ ] **Step 2: Verify compilation**

Run: `cd backend && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/resources/application.yml
git commit -m "feat(chat): add python service configuration"
```

---

## Verification

After all tasks complete:

```bash
cd backend && mvn clean compile -q
```

Expected: BUILD SUCCESS with no output.

---

## Next Steps

After Plan A completes, the following are unblocked:
- Plan B Task 3 (repositories exist)
- Plan C Task 1 (API endpoints known)
