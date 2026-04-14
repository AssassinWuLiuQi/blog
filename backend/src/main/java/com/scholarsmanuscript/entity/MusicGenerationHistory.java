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
