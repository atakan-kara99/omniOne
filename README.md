# omniOne

A bodybuilding coaching platform that connects coaches and their clients. Coaches manage nutrition plans, questionnaires, and client progress. Clients track their plans and communicate with their coach in real time.

## Monorepo Structure

```
omniOne/
├── frontend/    # React 19 + Vite SPA
├── backend/     # Spring Boot 3 REST API
└── CLAUDE.md    # Claude Code context
```

## Features

- **Authentication** — JWT-based login, registration, email activation, password reset
- **Coach–Client relationship** — coaches invite clients via email; clients accept and get onboarded
- **Nutrition plans** — coaches create and update macro plans per client; clients view their active plan and history
- **Questionnaires** — coaches build intake forms; clients fill them in after activation
- **Real-time chat** — WebSocket messaging between coach and client via STOMP
- **Check-ins** *(in progress)* — weekly client progress reports with weight, photos, and wellness metrics
- **Training plans** *(planned)*
- **Supplement plans** *(planned)*
- **Analytics** *(planned)*

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 19, React Router 7, Vite 7, STOMP.js |
| Backend | Spring Boot 3.5, Java 21, Spring Security |
| Database | PostgreSQL |
| Auth | JWT (access) + Refresh tokens |
| Messaging | WebSockets (STOMP over SockJS) |
| Email | JavaMail + MailHog (dev) |
| API Docs | Swagger / OpenAPI |
| Build | Maven (backend), Vite (frontend) |
| Dev infra | Docker Compose (PostgreSQL, Adminer, MailHog) |

## Quick Start

### Prerequisites

- Java 21
- Node.js 18+
- Docker

### Backend

```bash
cd backend

# Start PostgreSQL, Adminer, and MailHog
docker compose up -d

# Run with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

API available at `http://localhost:8080`
Swagger UI at `http://localhost:8080/swagger-ui.html`
MailHog UI at `http://localhost:8025`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

App available at `http://localhost:5173`

## Development

See [`CLAUDE.md`](./CLAUDE.md) for full architecture notes, conventions, and MVP roadmap.
See [`frontend/README.md`](./frontend/README.md) for frontend-specific details.
See [`backend/README.md`](./backend/README.md) for backend-specific details.
