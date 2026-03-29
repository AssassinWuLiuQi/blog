# Spring Boot 后端技术方案

> **项目**: The Scholar's Manuscript（学者手稿）- Vue 3 博客平台后端

## 1. 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.3.x | 核心框架 |
| Java | 21 | 运行环境 |
| Spring Security | 6.x | 认证授权 |
| Spring Data JPA | 3.x | 数据访问 |
| MySQL | 8.x | 生产数据库 |
| H2 | 2.x | 开发数据库 |
| JWT | 0.12.x | Token 认证 |
| Lombok | 1.18.x | 简化代码 |
| MapStruct | 3.x | 对象映射 |

## 2. 项目结构

```
backend/
├── src/main/java/com/scholars manuscript/
│   ├── config/                 # 配置类
│   │   ├── SecurityConfig.java
│   │   ├── JwtConfig.java
│   │   └── CorsConfig.java
│   ├── controller/             # 控制器
│   │   ├── AuthController.java
│   │   ├── ArticleController.java
│   │   ├── CategoryController.java
│   │   └── UserController.java
│   ├── service/                # 业务逻辑
│   │   ├── AuthService.java
│   │   ├── ArticleService.java
│   │   └── UserService.java
│   ├── repository/             # 数据访问
│   │   ├── UserRepository.java
│   │   ├── ArticleRepository.java
│   │   └── CategoryRepository.java
│   ├── entity/                # 实体类
│   │   ├── User.java
│   │   ├── Article.java
│   │   └── Category.java
│   ├── dto/                   # 数据传输对象
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── ArticleRequest.java
│   │   └── ArticleResponse.java
│   ├── security/              # 安全相关
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── UserDetailsServiceImpl.java
│   └── exception/             # 异常处理
│       ├── GlobalExceptionHandler.java
│       └── BusinessException.java
├── src/main/resources/
│   ├── application.yml
│   └── data.sql
├── pom.xml
└── README.md
```

## 3. 数据库设计

### 3.1 用户表 (users)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名，唯一 |
| email | VARCHAR(100) | 邮箱，唯一 |
| password | VARCHAR(255) | 密码（加密存储） |
| theme | VARCHAR(20) | 主题偏好 |
| font | VARCHAR(50) | 字体偏好 |
| voice_setting | VARCHAR(50) | 语音设置 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 3.2 文章表 (articles)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| title | VARCHAR(200) | 标题 |
| content | TEXT | 富文本内容 |
| excerpt | VARCHAR(500) | 摘要 |
| status | VARCHAR(20) | 状态：DRAFT/PUBLISHED |
| author_id | BIGINT | 作者ID（外键） |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| published_at | TIMESTAMP | 发布时间 |

### 3.3 分类表 (categories)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 分类名称 |
| slug | VARCHAR(50) | URL友好标识 |

### 3.4 文章_分类关联表 (article_categories)

| 字段 | 类型 | 说明 |
|------|------|------|
| article_id | BIGINT | 文章ID |
| category_id | BIGINT | 分类ID |

## 4. API 设计

### 4.1 认证模块 `/api/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/logout | 用户登出 |
| GET | /api/auth/me | 获取当前用户 |

**登录请求:**
```json
{
  "username": "string",
  "password": "string"
}
```

**登录响应:**
```json
{
  "token": "jwt-token-string",
  "user": {
    "id": 1,
    "username": "string",
    "email": "string"
  }
}
```

### 4.2 文章模块 `/api/articles`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/articles | 获取文章列表（分页、筛选） |
| GET | /api/articles/{id} | 获取文章详情 |
| POST | /api/articles | 创建文章 |
| PUT | /api/articles/{id} | 更新文章 |
| DELETE | /api/articles/{id} | 删除文章 |
| GET | /api/articles/archive | 按年份归档 |
| GET | /api/articles/search | 搜索文章 |

**查询参数:**
- `page`: 页码（默认0）
- `size`: 每页数量（默认10）
- `status`: 状态筛选
- `category`: 分类筛选
- `year`: 年份筛选（归档用）
- `keyword`: 关键词搜索

### 4.3 分类模块 `/api/categories`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/categories | 获取所有分类 |
| POST | /api/categories | 创建分类（管理员） |
| GET | /api/categories/{slug}/articles | 获取分类下的文章 |

### 4.4 用户设置 `/api/users/settings`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/users/settings | 获取当前用户设置 |
| PUT | /api/users/settings | 更新用户设置 |

**设置请求:**
```json
{
  "theme": "light",
  "font": "Inter",
  "voiceSetting": "default"
}
```

## 5. 安全设计

### 5.1 JWT Token

- 访问令牌（Access Token）: 15分钟有效期
- 刷新令牌（Refresh Token）: 7天有效期
- 存储在 Authorization Header: `Bearer <token>`

### 5.2 密码加密

使用 BCrypt 加密，强度 12

### 5.3 接口权限

| 接口 | 权限 |
|------|------|
| POST /api/auth/login | 公开 |
| POST /api/auth/register | 公开 |
| GET /api/articles | 登录用户 |
| POST /api/articles | 登录用户 |
| PUT /api/articles/{id} | 文章作者 |
| DELETE /api/articles/{id} | 文章作者 |

## 6. 配置

### 6.1 application.yml 关键配置

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:scholarship
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-here-must-be-long-enough}
  expiration: 900000  # 15 minutes
  refresh-expiration: 604800000  # 7 days
```

## 7. 待实现 vs 前端对应

| 前端待完善功能 | 后端需要支持 |
|--------------|-------------|
| 用户注册功能 | POST /api/auth/register |
| 文章增删改查 API 对接 | Full CRUD + 状态管理 |
| 真实 TTS 语音合成对接 | 预留 voice_setting 字段 |
| 深色模式支持 | theme 字段支持 |

## 8. 开发计划

**Phase 1: 基础框架**
1. 项目初始化（Spring Boot 3.3 + JDK 21）
2. 实体类设计（JPA Entity）
3. Repository 层实现
4. 基础配置（Security, JWT, CORS）

**Phase 2: 认证模块**
1. 用户注册/登录
2. JWT Token 生成验证
3. 密码加密存储

**Phase 3: 文章模块**
1. 文章 CRUD
2. 分页查询
3. 分类管理
4. 归档查询

**Phase 4: 用户设置**
1. 设置 CRUD
2. 偏好设置持久化

---

**确认后我将创建详细的实现计划文档。**
