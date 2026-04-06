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
                        .messageCount((int) sessionRepository.countMessagesBySessionId(session.getId()))
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
