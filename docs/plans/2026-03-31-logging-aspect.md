# Logging Aspect Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add an AOP-based logging aspect that records method invocations with parameters, execution time, and results, store logs in MongoDB, and provide a frontend interface to view logs.

**Architecture:** Use Spring AOP to intercept controller methods via `@Around` advice, capture request/response data, and store in MongoDB via a dedicated `OperationLog` document. Expose logs via REST API and create a Vue component for viewing.

**Tech Stack:** Spring Boot AOP, Spring Data MongoDB, Vue 3 + TypeScript

---

## Task 1: Add AOP Dependency

**Files:**
- Modify: `backend/pom.xml`

**Step 1: Add spring-boot-starter-aop dependency**

Add after the MongoDB dependency:
```xml
<!-- AOP -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

**Step 2: Verify dependency resolves**

Run: `cd backend && mvn dependency:tree | grep aop`
Expected: `spring-boot-starter-aop` in output

---

## Task 2: Create OperationLog MongoDB Entity

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/entity/mongo/OperationLog.java`

**Step 1: Create the entity**

```java
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
```

**Step 2: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: No errors

---

## Task 3: Create OperationLogRepository

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/repository/mongo/OperationLogRepository.java`

**Step 1: Create the repository**

```java
package com.scholarsmanuscript.repository.mongo;

import com.scholarsmanuscript.entity.mongo.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationLogRepository extends MongoRepository<OperationLog, String> {

    Page<OperationLog> findByUsername(String username, Pageable pageable);

    Page<OperationLog> findBySuccess(Boolean success, Pageable pageable);

    Page<OperationLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
```

---

## Task 4: Create OperationLogService

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/service/OperationLogService.java`

**Step 1: Create the service**

```java
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
```

---

## Task 5: Create OperationLogResponse DTO

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/dto/response/OperationLogResponse.java`

**Step 1: Create the DTO**

```java
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
```

---

## Task 6: Create LoggingAspect

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/aspect/LoggingAspect.java`

**Step 1: Create the aspect**

```java
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
```

**Step 2: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: No errors

---

## Task 7: Create LogController

**Files:**
- Create: `backend/src/main/java/com/scholarsmanuscript/controller/LogController.java`

**Step 1: Create the controller**

```java
package com.scholarsmanuscript.controller;

import com.scholarsmanuscript.dto.response.ApiResponse;
import com.scholarsmanuscript.dto.response.OperationLogResponse;
import com.scholarsmanuscript.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final OperationLogService operationLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OperationLogResponse>>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Boolean success) {
        Page<OperationLogResponse> logs = operationLogService.getLogs(page, size, username, success);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
```

---

## Task 8: Add CORS Configuration for Logs

**Files:**
- Modify: `backend/src/main/java/com/scholarsmanuscript/config/SecurityConfig.java`

**Step 1: Add /api/logs to permitted endpoints**

Find the security configuration and ensure `/api/logs` is accessible. If using JWT auth, logs endpoint should require authentication.

---

## Task 9: Create Frontend LogViewer View

**Files:**
- Create: `frontend/src/views/LogViewerView.vue`

**Step 1: Create the Vue view**

```vue
<template>
  <div class="log-viewer">
    <h1>操作日志</h1>

    <div class="filters">
      <input v-model="filters.username" placeholder="用户名" @keyup.enter="fetchLogs" />
      <select v-model="filters.success">
        <option :value="null">全部</option>
        <option :value="true">成功</option>
        <option :value="false">失败</option>
      </select>
      <button @click="fetchLogs">搜索</button>
    </div>

    <table v-if="logs.length > 0">
      <thead>
        <tr>
          <th>时间</th>
          <th>用户</th>
          <th>方法</th>
          <th>URI</th>
          <th>耗时</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="log in logs" :key="log.id">
          <td>{{ formatDate(log.createdAt) }}</td>
          <td>{{ log.username }}</td>
          <td>{{ log.httpMethod }} {{ log.methodName }}</td>
          <td>{{ log.requestUri }}</td>
          <td>{{ log.executionTime }}ms</td>
          <td :class="log.success ? 'success' : 'error'">
            {{ log.success ? '成功' : '失败' }}
          </td>
          <td>
            <button @click="showDetail(log)">详情</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-else class="no-data">暂无数据</div>

    <div class="pagination">
      <button :disabled="page === 0" @click="page--; fetchLogs()">上一页</button>
      <span>第 {{ page + 1 }} 页</span>
      <button @click="page++; fetchLogs()">下一页</button>
    </div>

    <div v-if="selectedLog" class="modal" @click.self="selectedLog = null">
      <div class="modal-content">
        <h2>日志详情</h2>
        <div class="detail-grid">
          <div><strong>用户:</strong> {{ selectedLog.username }}</div>
          <div><strong>类名:</strong> {{ selectedLog.className }}</div>
          <div><strong>方法:</strong> {{ selectedLog.methodName }}</div>
          <div><strong>HTTP:</strong> {{ selectedLog.httpMethod }}</div>
          <div><strong>URI:</strong> {{ selectedLog.requestUri }}</div>
          <div><strong>IP:</strong> {{ selectedLog.ip }}</div>
          <div><strong>耗时:</strong> {{ selectedLog.executionTime }}ms</div>
          <div><strong>状态:</strong> {{ selectedLog.success ? '成功' : '失败' }}</div>
        </div>
        <div class="detail-section">
          <strong>参数:</strong>
          <pre>{{ formatJson(selectedLog.parameters) }}</pre>
        </div>
        <div v-if="selectedLog.result" class="detail-section">
          <strong>结果:</strong>
          <pre>{{ formatJson(selectedLog.result) }}</pre>
        </div>
        <div v-if="selectedLog.errorMessage" class="detail-section error">
          <strong>错误:</strong>
          <pre>{{ selectedLog.errorMessage }}</pre>
        </div>
        <button @click="selectedLog = null">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

interface OperationLog {
  id: string
  username: string
  methodName: string
  className: string
  httpMethod: string
  requestUri: string
  parameters: string
  result: string
  executionTime: number
  success: boolean
  errorMessage: string
  createdAt: string
}

const logs = ref<OperationLog[]>([])
const page = ref(0)
const size = ref(20)
const filters = ref({
  username: '',
  success: null as boolean | null
})
const selectedLog = ref<OperationLog | null>(null)

const fetchLogs = async () => {
  try {
    const params = new URLSearchParams({
      page: page.value.toString(),
      size: size.value.toString()
    })
    if (filters.value.username) params.append('username', filters.value.username)
    if (filters.value.success !== null) params.append('success', filters.value.success.toString())

    const response = await axios.get(`/api/logs?${params}`)
    logs.value = response.data.data.content
  } catch (error) {
    console.error('Failed to fetch logs:', error)
  }
}

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

const formatJson = (str: string) => {
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

const showDetail = (log: OperationLog) => {
  selectedLog.value = log
}

onMounted(fetchLogs)
</script>

<style scoped>
.log-viewer {
  padding: 20px;
}
.filters {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.filters input, .filters select {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
.filters button {
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
table {
  width: 100%;
  border-collapse: collapse;
}
th, td {
  border: 1px solid #ddd;
  padding: 10px;
  text-align: left;
}
th {
  background: #f5f5f5;
}
.success { color: green; }
.error { color: red; }
.pagination {
  display: flex;
  gap: 10px;
  margin-top: 20px;
  align-items: center;
}
.pagination button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  cursor: pointer;
}
.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.no-data {
  text-align: center;
  padding: 40px;
  color: #999;
}
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  max-width: 800px;
  max-height: 80vh;
  overflow-y: auto;
  width: 90%;
}
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin: 15px 0;
}
.detail-section {
  margin: 15px 0;
}
.detail-section pre {
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
}
.detail-section.error pre {
  background: #fee;
  color: #c00;
}
.modal-content button {
  margin-top: 15px;
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>
```

---

## Task 10: Add LogViewer Route

**Files:**
- Modify: `frontend/src/router/index.ts`

**Step 1: Add route for LogViewerView**

Add to router configuration:
```typescript
{
  path: '/logs',
  name: 'LogViewer',
  component: () => import('../views/LogViewerView.vue'),
  meta: { requiresAuth: true }
}
```

---

## Task 11: Verify Build

**Step 1: Compile backend**

Run: `cd backend && mvn compile -q`
Expected: No errors

**Step 2: Build frontend**

Run: `cd frontend && npm run build`
Expected: No errors

---

## Task 12: Integration Test

**Step 1: Start backend**

Run: `cd backend && mvn spring-boot:run`

**Step 2: Make some API requests**

Call any `/api/articles/**` endpoint to generate logs

**Step 3: Verify logs API works**

Run: `curl http://localhost:8080/api/logs`
Expected: JSON response with operation logs

---

## Summary

| Task | Description |
|------|-------------|
| 1 | Add AOP dependency |
| 2 | Create OperationLog MongoDB entity |
| 3 | Create OperationLogRepository |
| 4 | Create OperationLogService |
| 5 | Create OperationLogResponse DTO |
| 6 | Create LoggingAspect |
| 7 | Create LogController |
| 8 | Update SecurityConfig for logs endpoint |
| 9 | Create Frontend LogViewerView |
| 10 | Add route to router |
| 11 | Verify build |
| 12 | Integration test |

**Plan complete.** Two execution options:

**1. Subagent-Driven (this session)** - I dispatch fresh subagent per task, review between tasks, fast iteration

**2. Parallel Session (separate)** - Open new session with executing-plans, batch execution with checkpoints

Which approach?
