package com.scholarsmanuscript.service;

import com.scholarsmanuscript.dto.response.OperationLogResponse;
import com.scholarsmanuscript.entity.OperationLog;
import com.scholarsmanuscript.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;

    public void saveLog(OperationLog log) {
        operationLogRepository.save(log);
    }

    public Page<OperationLogResponse> getLogs(int page, int size, String username, Boolean success) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<OperationLog> logs;
        if (username != null && !username.isEmpty() && success != null) {
            logs = operationLogRepository.findByUsernameAndSuccess(username, success, pageable);
        } else if (username != null && !username.isEmpty()) {
            logs = operationLogRepository.findByUsername(username, pageable);
        } else if (success != null) {
            logs = operationLogRepository.findBySuccess(success, pageable);
        } else {
            logs = operationLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return logs.map(this::mapToResponse);
    }

    private OperationLogResponse mapToResponse(OperationLog log) {
        return OperationLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .username(log.getUsername())
                .operation(log.getOperation())
                .methodName(log.getMethodName())
                .className(log.getClassName())
                .httpMethod(log.getHttpMethod())
                .requestUri(log.getRequestUri())
                .parameters(truncate(log.getParameters(), 500))
                .executionTime(log.getExecutionTime())
                .ip(log.getIp())
                .success(log.getSuccess())
                .errorMessage(log.getErrorMessage())
                .errorTrace(log.getErrorTrace())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength);
    }
}
