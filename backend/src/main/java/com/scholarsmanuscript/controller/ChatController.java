package com.scholarsmanuscript.controller;

import com.scholarsmanuscript.dto.request.ChatRequest;
import com.scholarsmanuscript.dto.response.ApiResponse;
import com.scholarsmanuscript.dto.response.ChatResponse;
import com.scholarsmanuscript.entity.ChatMessage;
import com.scholarsmanuscript.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
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
