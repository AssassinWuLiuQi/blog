package com.scholarsmanuscript.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "image_generation_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageGenerationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    @Column(name = "image_urls", columnDefinition = "JSON")
    private String imageUrls;

    @Column(length = 50, nullable = false)
    private String model;

    @Column(name = "aspect_ratio", length = 10, nullable = false)
    private String aspectRatio;

    @Column(length = 50)
    private String style;

    @Column(name = "success_count")
    private Integer successCount;

    @Column(name = "failed_count")
    private Integer failedCount;

    @Column(name = "task_id", length = 100)
    private String taskId;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "status_msg", length = 255)
    private String statusMsg;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
