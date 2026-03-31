package com.scholarsmanuscript.entity.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "operation_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationLog {

    @Id
    private String id;

    private String username;

    private String methodName;

    private String className;

    private String httpMethod;

    private String requestUri;

    private String parameters;

    private String result;

    private Long executionTime;

    private String ip;

    private Boolean success;

    private String errorMessage;

    private LocalDateTime createdAt;
}