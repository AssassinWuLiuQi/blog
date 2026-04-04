package com.scholarsmanuscript.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationLogResponse {

    private Long id;
    private Long userId;
    private String username;
    private String operation;
    private String methodName;
    private String className;
    private String httpMethod;
    private String requestUri;
    private String parameters;
    private Long executionTime;
    private String ip;
    private Boolean success;
    private String errorMessage;
    private String errorTrace;
    private LocalDateTime createdAt;
}
