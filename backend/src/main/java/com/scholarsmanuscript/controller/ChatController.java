package com.scholarsmanuscript.controller;

import com.scholarsmanuscript.dto.request.ChatRequest;
import com.scholarsmanuscript.dto.response.ApiResponse;
import com.scholarsmanuscript.dto.response.ChatResponse;
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
import java.util.concurrent.Executor;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final Executor sseExecutor;

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

        sseExecutor.execute(() -> chatService.streamChat(userId, request, emitter));

        return emitter;
    }

    private Long getUserId(Authentication authentication) {
        return 1L;
    }
}
