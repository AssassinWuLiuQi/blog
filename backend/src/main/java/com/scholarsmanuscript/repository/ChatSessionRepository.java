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
