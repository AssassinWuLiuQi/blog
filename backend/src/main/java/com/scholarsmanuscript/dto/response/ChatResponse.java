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
