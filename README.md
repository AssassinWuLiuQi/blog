# The Scholar's Manuscript (学者手稿)

一个现代化的学术博客系统，支持富文本编辑、文本转语音、用户认证等功能。

## 技术栈

### 后端
- **Spring Boot 3.3.0** (Java 21)
- **Spring Security** - JWT 认证 + RSA 加密
- **Spring Data JPA** + MySQL - 关系数据存储
- **Spring Data MongoDB** - 文章内容存储
- **OkHttp** - HTTP 客户端
- **Lombok** + **MapStruct** - 简化开发

### 前端
- **Vue 3** + TypeScript (严格模式)
- **Vite** - 构建工具
- **Tailwind CSS** - 样式框架
- **Element Plus** - UI 组件库
- **Tiptap** - 富文本编辑器
- **Pinia** - 状态管理
- **Vue Router** - 路由管理

## 项目结构

```
blog/
├── backend/                    # Spring Boot 后端
│   └── src/main/java/com/scholarsmanuscript/
│       ├── config/             # 配置类
│       ├── controller/         # REST 控制器
│       ├── dto/                # 数据传输对象
│       ├── entity/             # JPA 实体
│       ├── repository/         # 数据访问层
│       │   ├── jpa/            # JPA 仓库 (MySQL)
│       │   └── mongo/          # MongoDB 仓库
│       ├── security/           # 安全相关
│       ├── service/           # 业务逻辑
│       └── utils/              # 工具类
│
└── frontend/                   # Vue 3 前端
    └── src/
        ├── assets/             # 静态资源
        ├── components/         # Vue 组件
        ├── router/             # 路由配置
        ├── stores/             # Pinia 状态管理
        ├── types/              # TypeScript 类型定义
        ├── utils/              # 工具函数
        └── views/              # 页面视图
```

## 功能特性

- [x] 用户注册/登录 (JWT + RSA 加密)
- [x] 文章管理 (富文本编辑)
- [x] 分类管理
- [x] 文本转语音 (TTS) - MiniMax API
- [x] 操作日志 (MongoDB 存储)
- [x] 响应式布局

## 快速开始

### 环境要求

- JDK 21+
- Node.js 18+
- Maven 3.8+
- MySQL 8.0+
- MongoDB 5.0+

### 后端启动

```bash
cd backend

# 配置环境变量或修改 application.yml
# 设置数据库连接、MongoDB 连接、JWT 密钥等

# 启动
mvn spring-boot:run
```

### 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 开发模式
npm run dev

# 构建生产版本
npm run build
```

### 环境变量配置

后端 `application.yml` 或系统环境变量：

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DB_HOST` | MySQL 主机 | localhost |
| `DB_PORT` | MySQL 端口 | 3306 |
| `DB_USERNAME` | 数据库用户名 | root |
| `DB_PASSWORD` | 数据库密码 | (空) |
| `MONGO_HOST` | MongoDB 主机 | localhost |
| `MONGO_PORT` | MongoDB 端口 | 27017 |
| `JWT_SECRET` | JWT 密钥 | (自动生成) |
| `CORS_ORIGINS` | CORS 允许的源 | http://localhost:5173 |
| `MINI_MAX_API_KEY` | MiniMax API 密钥 | (空) |

## API 端点

### 认证
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/public-key` - 获取 RSA 公钥
- `GET /api/auth/me` - 获取当前用户信息

### 文章
- `GET /api/articles` - 获取文章列表
- `GET /api/articles/{id}` - 获取文章详情
- `POST /api/articles` - 创建文章
- `PUT /api/articles/{id}` - 更新文章
- `DELETE /api/articles/{id}` - 删除文章

### 分类
- `GET /api/categories` - 获取分类列表
- `POST /api/categories` - 创建分类
- `PUT /api/categories/{id}` - 更新分类
- `DELETE /api/categories/{id}` - 删除分类

### TTS
- `GET /api/tts/voices` - 获取可用音色列表
- `POST /api/tts/speech` - 文本转语音 (流式响应)

## License

Private Project - All Rights Reserved
