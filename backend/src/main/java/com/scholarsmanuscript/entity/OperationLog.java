package com.scholarsmanuscript.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "operation_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 50)
    private String username;

    @Column(length = 100)
    private String operation;

    @Column(length = 50)
    private String methodName;

    @Column(length = 100)
    private String className;

    @Column(length = 10)
    private String httpMethod;

    @Column(length = 255)
    private String requestUri;

    @Column(columnDefinition = "TEXT")
    private String parameters;

    private Long executionTime;

    @Column(length = 50)
    private String ip;

    private Boolean success;

    @Column(length = 500)
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String errorTrace;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
