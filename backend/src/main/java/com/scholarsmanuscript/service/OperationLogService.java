package com.scholarsmanuscript.service;

import com.scholarsmanuscript.dto.response.OperationLogResponse;
import com.scholarsmanuscript.entity.mongo.OperationLog;
import com.scholarsmanuscript.repository.mongo.OperationLogRepository;
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
        if (username != null && !username.isEmpty()) {
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
                .username(log.getUsername())
                .methodName(log.getMethodName())
                .className(log.getClassName())
                .httpMethod(log.getHttpMethod())
                .requestUri(log.getRequestUri())
                .parameters(log.getParameters())
                .result(log.getResult() != null && log.getResult().length() > 200
                    ? log.getResult().substring(0, 200) + "..." : log.getResult())
                .executionTime(log.getExecutionTime())
                .success(log.getSuccess())
                .errorMessage(log.getErrorMessage())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
