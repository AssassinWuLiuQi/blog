package com.scholarsmanuscript.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationLogResponse {

    private String id;
    private String username;
    private String methodName;
    private String className;
    private String httpMethod;
    private String requestUri;
    private String parameters;
    private String result;
    private Long executionTime;
    private Boolean success;
    private String errorMessage;
    private LocalDateTime createdAt;
}