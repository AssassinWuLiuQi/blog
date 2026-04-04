package com.scholarsmanuscript.controller;

import com.scholarsmanuscript.dto.response.ApiResponse;
import com.scholarsmanuscript.dto.response.OperationLogResponse;
import com.scholarsmanuscript.entity.User;
import com.scholarsmanuscript.repository.UserRepository;
import com.scholarsmanuscript.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final OperationLogService operationLogService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OperationLogResponse>>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean success,
            Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        boolean isAdmin = user != null && user.getId() == 1L;
        Page<OperationLogResponse> logs = operationLogService.getLogs(page, size, isAdmin ? null : username, success);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
