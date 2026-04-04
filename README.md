# The Scholar's Manuscript (学者手稿)

<p align="center">
  <img src="https://img.shields.io/badge/Java-21+-blue.svg" alt="Java">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.3-green.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3.4-brightgreen.svg" alt="Vue">
  <img src="https://img.shields.io/badge/TypeScript-6.0-blue.svg" alt="TypeScript">
  <img src="https://img.shields.io/badge/License-Private-red.svg" alt="License">
</p>

> 一个现代化的学术博客系统，支持富文本编辑、文本转语音、优雅的中文排版，专为学者和写作者打造。

## 特性

- **富文本编辑** — 基于 Tiptap 的专业编辑器，支持标题、列表、引用、代码块等格式
- **文本转语音 (TTS)** — 集成 MiniMax API，将文章内容转换为自然语音
- **用户认证** — JWT + RSA 加密的安全认证机制
- **分类管理** — 灵活的文章分类体系
- **响应式设计** — 适配桌面端和移动端
- **中文优化** — 优雅的中文排版，适合学术文章阅读

## 技术栈

| 分类 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Vite + Tailwind CSS + Pinia + Vue Router |
| 后端 | Spring Boot 3.3 + Spring Security + Spring Data JPA |
| 数据库 | MySQL (关系数据) + MongoDB (文章内容) |
| 其他 | Lombok + MapStruct + JWT + MiniMax TTS API |

## 项目结构

```
blog/
├── frontend/                 # Vue 3 单页应用
│   ├── src/
│   │   ├── components/      # 复用组件
│   │   ├── views/           # 页面视图
│   │   ├── stores/          # Pinia 状态管理
│   │   ├── router/          # 路由配置
│   │   └── utils/           # 工具函数
│   └── tailwind.config.js   # Tailwind 配置
│
├── backend/                  # Spring Boot 后端
│   └── src/main/java/com/scholarsmanuscript/
│       ├── config/          # 配置类
│       ├── controller/      # REST 控制器
│       ├── dto/             # 数据传输对象
│       ├── entity/          # JPA 实体
│       ├── repository/      # 数据访问层
│       ├── security/        # 安全认证
│       └── service/         # 业务逻辑
│
├── ui/                       # 设计稿参考
├── deploy/                   # 部署配置
└── docs/                     # 技术文档
```

## 快速开始

### 前置要求

- JDK 21+
- Node.js 18+
- Maven 3.8+
- MySQL 8.0+
- MongoDB 5.0+

### 克隆项目

```bash
git clone <repository-url>
cd blog
```

### 启动后端

```bash
cd backend

# 配置数据库连接 (application.yml 或环境变量)
# DB_HOST, DB_PORT, DB_USERNAME, DB_PASSWORD
# MONGO_HOST, MONGO_PORT

mvn spring-boot:run
```

### 启动前端

```bash
cd frontend

npm install
npm run dev
```

访问 `http://localhost:5173` 即可看到应用。

## API 概览

### 认证
| 方法 | 端点 | 描述 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |
| GET | `/api/auth/me` | 获取当前用户 |

### 文章
| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/articles` | 文章列表 |
| GET | `/api/articles/{id}` | 文章详情 |
| POST | `/api/articles` | 创建文章 |
| PUT | `/api/articles/{id}` | 更新文章 |
| DELETE | `/api/articles/{id}` | 删除文章 |

### 分类
| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/categories` | 分类列表 |
| GET | `/api/categories/{slug}/articles` | 按分类获取文章 |

### TTS
| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/tts/voices` | 可用音色列表 |
| POST | `/api/tts/speech` | 文本转语音 |

## 路由

| 路径 | 页面 |
|------|------|
| `/` | 首页 |
| `/login` | 登录 |
| `/article/:id` | 文章详情 |
| `/archive` | 文章归档 |
| `/settings` | 设置 |
| `/tech-preview` | 技术预览 |

## License

Private Project - All Rights Reserved
