# Spring Boot 后端技术方案

> **项目**: The Scholar's Manuscript（学者手稿）- Vue 3 博客平台后端
> **日期**: 2026-03-29

## 1. 架构概述

```
┌─────────────────────────────────────────────────────┐
│                    Controller 层                     │
│        (Auth, Article, Category, User 控制器)       │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│                    Service 层                        │
│          (业务逻辑、验证、事务管理)                    │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│                  Repository 层                       │
│              (JPA、Spring Data)                      │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│                     数据库层                          │
│              (开发/生产：MySQL 8.x)                  │
└─────────────────────────────────────────────────────┘
```

## 2. 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.3.x | 核心框架 |
| Java | 21 | 运行环境 |
| Spring Security | 6.x | 认证授权 |
| Spring Data JPA | 3.x | 数据访问 |
| MySQL | 8.x | 数据库 |
| JWT | 0.12.x | Token 认证 |
| Lombok | 1.18.x | 简化代码 |
| MapStruct | 3.x | 对象映射 |

## 3. 项目结构

```
backend/
├── src/main/java/com/scholarsmanuscript/
│   ├── ScholarsManuscriptApplication.java    # 启动类
│   │
│   ├── config/                               # 配置类
│   │   ├── SecurityConfig.java               # Spring Security 配置
│   │   ├── JwtConfig.java                    # JWT 相关配置
│   │   └── CorsConfig.java                   # 跨域配置
│   │
│   ├── controller/                           # 控制器层
│   │   ├── AuthController.java               # 认证：登录/注册/登出
│   │   ├── ArticleController.java            # 文章：CRUD + 归档 + 搜索
│   │   ├── CategoryController.java           # 分类：列表 + 详情
│   │   └── UserController.java               # 用户：设置 + TTS 配置
│   │
│   ├── service/                              # 服务层
│   │   ├── AuthService.java
│   │   ├── ArticleService.java
│   │   ├── CategoryService.java
│   │   └── UserService.java
│   │
│   ├── repository/                           # 数据访问层
│   │   ├── UserRepository.java
│   │   ├── ArticleRepository.java
│   │   └── CategoryRepository.java
│   │
│   ├── entity/                               # 实体类
│   │   ├── User.java
│   │   ├── Article.java
│   │   └── Category.java
│   │
│   ├── dto/                                  # 数据传输对象
│   │   ├── request/                          # 请求 DTO
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── ArticleRequest.java
│   │   │   └── UserSettingsRequest.java
│   │   └── response/                         # 响应 DTO
│   │       ├── AuthResponse.java
│   │       ├── ArticleResponse.java
│   │       └── UserSettingsResponse.java
│   │
│   ├── security/                             # 安全相关
│   │   ├── JwtTokenProvider.java             # JWT 生成/验证
│   │   ├── JwtAuthenticationFilter.java      # 请求拦截过滤器
│   │   └── UserDetailsServiceImpl.java       # 用户加载服务
│   │
│   └── exception/                            # 异常处理
│       ├── GlobalExceptionHandler.java        # 统一异常处理
│       └── BusinessException.java            # 业务异常
│
├── src/main/resources/
│   ├── application.yml                       # 主配置文件
│   └── application-dev.yml                   # 开发环境配置
│
├── src/test/java/                           # 测试代码
│
└── pom.xml                                  # Maven 依赖
```

## 4. 数据库设计

### 4.1 用户表 (users)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| email | VARCHAR(100) | 邮箱，唯一 |
| password | VARCHAR(255) | BCrypt 加密密码 |
| theme | VARCHAR(20) | 主题偏好（light/dark） |
| font | VARCHAR(50) | 字体偏好 |
| voice_setting | VARCHAR(50) | 语音设置 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 4.2 文章表 (articles)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| title | VARCHAR(200) | 标题 |
| content | TEXT | 富文本内容（HTML） |
| excerpt | VARCHAR(500) | 摘要 |
| status | VARCHAR(20) | 状态：DRAFT / PUBLISHED |
| author_id | BIGINT | 作者 ID（外键 → users.id） |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| published_at | TIMESTAMP | 发布时间 |

### 4.3 分类表 (categories)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| name | VARCHAR(50) | 分类名称 |
| slug | VARCHAR(50) | URL 友好标识，唯一 |

### 4.4 文章_分类关联表 (article_categories)

| 字段 | 类型 | 说明 |
|------|------|------|
| article_id | BIGINT | 文章 ID |
| category_id | BIGINT | 分类 ID |
| → 联合主键 (article_id, category_id) |

**表关系：**
- User → Article：一对多（一个用户可以写多篇文章）
- Article ↔ Category：多对多（通过 article_categories 关联）

## 5. API 设计

### 5.1 认证模块 `/api/auth`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/auth/login | 用户登录 | 公开 |
| POST | /api/auth/register | 用户注册 | 公开 |
| POST | /api/auth/logout | 用户登出 | 登录 |
| GET | /api/auth/me | 获取当前用户信息 | 登录 |
| POST | /api/auth/refresh | 刷新访问令牌 | 登录 |

**登录请求：**
```json
{
  "username": "string",
  "password": "string"
}
```

**登录响应：**
```json
{
  "token": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "user": {
    "id": 1,
    "username": "string",
    "email": "string"
  }
}
```

**注册请求：**
```json
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```

### 5.2 文章模块 `/api/articles`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/articles | 获取文章列表（分页） | 登录 |
| GET | /api/articles/{id} | 获取文章详情 | 登录 |
| POST | /api/articles | 创建文章 | 登录 |
| PUT | /api/articles/{id} | 更新文章 | 作者 |
| DELETE | /api/articles/{id} | 删除文章 | 作者 |
| GET | /api/articles/archive | 按年份归档 | 登录 |
| GET | /api/articles/search | 搜索文章 | 登录 |

**查询参数（GET /api/articles）：**
- `page` — 页码（默认 0）
- `size` — 每页数量（默认 10）
- `status` — 状态筛选（DRAFT / PUBLISHED）
- `category` — 分类筛选

**创建文章请求：**
```json
{
  "title": "string",
  "content": "string (HTML)",
  "excerpt": "string",
  "status": "DRAFT | PUBLISHED",
  "categoryIds": [1, 2]
}
```

### 5.3 分类模块 `/api/categories`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/categories | 获取所有分类 | 登录 |
| GET | /api/categories/{slug} | 获取分类详情 | 登录 |
| GET | /api/categories/{slug}/articles | 获取该分类下的文章 | 登录 |

### 5.4 用户设置 `/api/users`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/users/settings | 获取当前用户设置 | 登录 |
| PUT | /api/users/settings | 更新用户设置 | 登录 |

**用户设置请求：**
```json
{
  "theme": "light",
  "font": "Inter",
  "voiceSetting": "default"
}
```

### 5.5 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

**错误响应：**
```json
{
  "code": 401,
  "message": "Unauthorized",
  "data": null
}
```

## 6. 安全设计

### 6.1 JWT 认证流程

1. 用户登录，服务器验证用户名密码
2. 生成 Access Token（15 分钟有效期）
3. 生成 Refresh Token（7 天有效期）
4. 客户端请求时携带 Header: `Authorization: Bearer <access_token>`
5. JwtAuthenticationFilter 拦截请求，验证 Token
6. Access Token 过期后用 Refresh Token 调用 `/api/auth/refresh` 获取新 Access Token

### 6.2 Token 存储

- **Access Token** — 短期令牌，存前端内存或 localStorage
- **Refresh Token** — 长期令牌，建议存 httpOnly Cookie（防 XSS）

### 6.3 密码安全

- BCrypt 加密，强度因子 12
- 密码不返回给客户端

### 6.4 接口权限矩阵

| 接口 | 需要登录 | 需要是作者 |
|------|---------|-----------|
| POST /api/auth/login | ❌ | — |
| POST /api/auth/register | ❌ | — |
| POST /api/auth/logout | ✅ | — |
| GET /api/auth/me | ✅ | — |
| GET /api/articles | ✅ | — |
| GET /api/articles/{id} | ✅ | — |
| POST /api/articles | ✅ | — |
| PUT /api/articles/{id} | ✅ | ✅ (文章作者本人) |
| DELETE /api/articles/{id} | ✅ | ✅ (文章作者本人) |
| GET /api/categories | ✅ | — |
| GET /api/users/settings | ✅ | — |
| PUT /api/users/settings | ✅ | — |

## 7. 配置

### 7.1 application.yml 主要配置

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/scholars_manuscript?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true

jwt:
  secret: ${JWT_SECRET}
  expiration: 900000        # 15 分钟
  refresh-expiration: 604800000  # 7 天

app:
  cors:
    allowed-origins: http://localhost:5173
```

## 8. 开发计划

**第一阶段：基础框架**
1. 创建 Spring Boot 项目，配置 pom.xml 依赖
2. 配置 Security、JWT、CORS
3. 编写 Entity 实体类
4. 编写 Repository 层

**第二阶段：认证模块**
1. 实现 AuthController（登录/注册/登出/刷新）
2. 实现 JWT Token 生成和验证
3. 实现注册时密码 BCrypt 加密
4. 编写单元测试

**第三阶段：文章模块**
1. 实现 ArticleController（CRUD + 归档 + 搜索）
2. 实现分页和分类筛选
3. 实现文章权限控制（只有作者能修改/删除）
4. 编写单元测试

**第四阶段：分类和用户设置**
1. 实现 CategoryController
2. 实现 UserController（设置 + TTS 配置）
3. 编写单元测试
