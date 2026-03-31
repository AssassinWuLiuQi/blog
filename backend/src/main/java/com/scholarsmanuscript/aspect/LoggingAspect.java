package com.scholarsmanuscript.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarsmanuscript.entity.mongo.OperationLog;
import com.scholarsmanuscript.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerPointcut() {}

    @Around("restControllerPointcut()")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        HttpServletRequest request = getHttpServletRequest();
        String username = getCurrentUsername();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String httpMethod = request != null ? request.getMethod() : "UNKNOWN";
        String requestUri = request != null ? request.getRequestURI() : "UNKNOWN";
        String parameters = getParameters(joinPoint);
        String ip = request != null ? getClientIp(request) : "UNKNOWN";

        OperationLog.OperationLogBuilder logBuilder = OperationLog.builder()
                .username(username)
                .methodName(methodName)
                .className(className)
                .httpMethod(httpMethod)
                .requestUri(requestUri)
                .parameters(parameters)
                .ip(ip)
                .createdAt(LocalDateTime.now());

        Object result = null;
        Boolean success = true;
        String errorMessage = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            success = false;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            try {
                String resultStr = result != null ? objectMapper.writeValueAsString(result) : null;
                operationLogService.saveLog(logBuilder
                        .result(resultStr)
                        .executionTime(executionTime)
                        .success(success)
                        .errorMessage(errorMessage)
                        .build());
            } catch (Exception e) {
                log.error("Failed to save operation log", e);
            }
        }
    }

    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "anonymous";
    }

    private String getParameters(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            return objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            return "[]";
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
