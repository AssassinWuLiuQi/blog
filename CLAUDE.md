# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**The Scholar's Manuscript** is a Vue 3 blog platform with a Spring Boot backend. It features a rich text editor (Tiptap), text-to-speech functionality, and article management with categories.

## Architecture

```
blog/
├── frontend/          # Vue 3 SPA (dev server: http://localhost:5173)
├── backend/           # Spring Boot 3.3 REST API (port 8080)
├── ui/                # Static HTML design reference
├── deploy/            # Deployment configurations
└── docs/              # Technical documentation and plans
```

## Development Commands

### Frontend
```bash
cd frontend
npm install          # Install dependencies
npm run dev         # Start dev server (http://localhost:5173)
npm run build       # Production build
npm run preview     # Preview production build
```

### Backend
```bash
cd backend
mvn clean compile   # Compile project
mvn spring-boot:run # Start Spring Boot application
mvn clean package   # Build JAR
```

## Frontend Stack

- **Vue 3** + **Vite** + **Tailwind CSS** + **Pinia** + **Vue Router**
- **Tiptap** for rich text editing
- **Heroicons** for icons
- Routes: `/` (home), `/login`, `/tech-preview`, `/article/:id`, `/archive`, `/settings`
- Auth: JWT stored in localStorage, routes require authentication except `/login`

## Backend Stack

- **Spring Boot 3.3** with **Java 21**
- **Spring Security 6.x** with JWT (access + refresh tokens)
- **Spring Data JPA** with **MySQL** (H2 for dev)
- **Lombok** + **MapStruct** for boilerplate reduction
- Layered architecture: Controller → Service → Repository

## Key API Endpoints

| Module | Endpoints |
|--------|-----------|
| Auth | `POST /api/auth/login`, `POST /api/auth/register`, `GET /api/auth/me` |
| Articles | `GET /api/articles`, `GET /api/articles/{id}`, `POST /api/articles`, `PUT /api/articles/{id}`, `DELETE /api/articles/{id}` |
| Categories | `GET /api/categories`, `GET /api/categories/{slug}/articles` |
| Users | `GET /api/users/settings`, `PUT /api/users/settings` |

## Design System (Frontend)

Colors use CSS custom properties: `--primary` (#003f87), `--surface` (#f7f9fb), `--on-surface` (#191c1e)
- No 1px borders — use background shifts instead
- Gradient buttons: `from-primary to-primary-container` at 135deg
- Chinese text uses `.chinese-manuscript` class with `line-height: 1.8`
- Glassmorphism: `bg-white/80 backdrop-blur-md` for navigation

## Important Notes

- Backend is on `backend-implementation` branch — most backend code doesn't exist yet (implementation plan is in `docs/plans/`)
- Frontend has mock data/stores; full API integration is pending
- TTS (text-to-speech) UI exists but actual voice synthesis is not yet connected
- Frontend uses Material Symbols: `<span class="material-symbols-outlined">icon_name</span>`
