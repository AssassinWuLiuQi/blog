package com.scholarsmanuscript.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarsmanuscript.entity.OperationLog;
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private static final List<String> SENSITIVE_KEYS = Arrays.asList(
            "password", "secret", "token", "accessKey", "secretKey",
            "encryptedPassword", "privateKey", "publicKey", "authorization"
    );

    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "\"(" + String.join("|", SENSITIVE_KEYS) + ")\"\\s*:",
            Pattern.CASE_INSENSITIVE
    );

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerPointcut() {}

    @Around("restControllerPointcut()")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        HttpServletRequest request = getHttpServletRequest();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";
        Long userId = getUserId(authentication);
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String httpMethod = request != null ? request.getMethod() : "UNKNOWN";
        String requestUri = request != null ? request.getRequestURI() : "UNKNOWN";
        String parameters = filterSensitiveData(getParameters(joinPoint));
        String ip = request != null ? getClientIp(request) : "UNKNOWN";

        OperationLog.OperationLogBuilder logBuilder = OperationLog.builder()
                .userId(userId)
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
        String errorTrace = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            success = false;
            errorMessage = e.getMessage();
            errorTrace = getStackTrace(e);
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            try {
                operationLogService.saveLog(logBuilder
                        .executionTime(executionTime)
                        .success(success)
                        .errorMessage(errorMessage)
                        .errorTrace(truncate(errorTrace, 1000))
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

    private Long getUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            return null;
        }
        return null;
    }

    private String getParameters(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            return objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            return "[]";
        }
    }

    private String filterSensitiveData(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        return SENSITIVE_PATTERN.matcher(json).replaceAll("\"***\"");
    }

    private String getStackTrace(Throwable t) {
        if (t == null) {
            return null;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    private String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength);
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "UNKNOWN";
        }
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
