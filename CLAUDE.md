# omniOne — Claude Code Guide

## Project Overview

**omniOne** is a bodybuilding coaching platform connecting coaches and clients. It handles coach-client relationships, nutrition plans, questionnaires, real-time chat, and (upcoming) check-ins and training plans.

## Monorepo Structure

```
omniOne/
├── frontend/    # React 19 + Vite SPA
├── backend/     # Spring Boot 3 REST API
└── CLAUDE.md
```

---

## Frontend

**Stack:** React 19, React Router 7, Vite 7, STOMP.js (WebSockets), Phosphor Icons

**Run dev server:**
```bash
cd frontend
npm run dev
```

**Build:**
```bash
npm run build
```

**Source layout (`src/`):**
```
pages/          # One file per route/screen
components/     # Shared UI components (e.g. ChatDock.jsx)
utils/          # api.js, auth.js, authContext.js, errorUtils.js, etc.
assets/         # Static files
App.jsx         # Router setup
main.jsx        # Entry point
```

**Page groups:**
- Auth: `Login`, `Register`, `ForgotPassword`, `ResetPassword`, `ActivateAccount`, `AcceptInvitation`
- Coach: `CoachDashboard`, `CoachClients`, `CoachClientDetail`, `CoachQuestionnaire`, `CoachClientQuestionnaireResponses`, `CoachClientNutritionPlans`
- Client: `ClientDashboard`, `ClientCoach`, `ClientQuestionnaire`, `ClientNutritionPlans`
- Shared: `CompleteProfile`, `Profile`

**Conventions:**
- Functional components with hooks only — no class components
- Use `authContext.js` for auth state; never duplicate auth logic in components
- API calls go through `api.js` — do not use `fetch` directly in components
- Use `errorUtils.js` for consistent error handling/display
- Icons: use `phosphor-react` (already installed)
- No CSS frameworks — styles live in `App.css`/`index.css` or component-scoped CSS
- Keep pages lean; extract reusable logic into `utils/` or `components/`

---

## Backend

**Stack:** Spring Boot 3.5, Java 21, PostgreSQL, Spring Security + JWT, WebSockets (STOMP), MapStruct, Lombok, Swagger/OpenAPI, MailHog (dev), Maven

**Run (dev profile):**
```bash
cd backend
# Start infra first
docker compose up -d
# Then run Spring Boot
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Test:**
```bash
./mvnw test
```

**Build:**
```bash
./mvnw package -DskipTests
```

**Profiles:**
- `dev` — local PostgreSQL (Docker), MailHog, hot reload
- `test` — test DB, isolated config
- `prod` — production settings

**Docker services (docker-compose.yml):**
- PostgreSQL on `5432`
- Adminer on `8080` (DB UI)
- MailHog on `1025` (SMTP) / `8025` (web UI)

**API docs:** `http://localhost:8080/swagger-ui.html` (when running)

### Backend Package Structure

```
src/main/java/com/omniOne/
├── authentication/         # JWT auth, refresh tokens, login/register endpoints
│   ├── controller/
│   ├── service/
│   ├── dto/
│   ├── token/              # JWT + refresh token logic
│   └── validation/
├── controller/             # Business controllers (split by role: Coach/Client)
├── service/                # Business logic services
├── model/
│   ├── entity/             # JPA entities
│   ├── dto/                # Input/Output DTOs
│   ├── enums/              # Role, status enums
│   └── mapper/             # MapStruct mappers (entity ↔ DTO)
├── repository/             # Spring Data JPA repositories
├── chatting/               # WebSocket chat (controller, service, entity, DTOs)
├── configuration/          # SecurityConfig, CorsConfig, WebSocketConfig
├── email/                  # EmailService, templates (HTML)
├── exception/              # GlobalExceptionHandler, custom exceptions
└── logging/                # Logback config, request logging
```

### Backend Conventions

- **Controllers** are thin — only handle HTTP concerns, delegate to services
- **Services** contain all business logic
- **DTOs** are used for all API input/output — never expose entities directly
- **MapStruct mappers** handle entity ↔ DTO conversion — do not map manually
- **Lombok** is used on entities and DTOs (`@Data`, `@Builder`, `@NoArgsConstructor`, etc.)
- **Validation** via Jakarta Bean Validation (`@NotNull`, `@Email`, etc.) on DTOs
- **Exceptions**: throw custom exceptions; `GlobalExceptionHandler` formats error responses
- **Security**: JWT for stateless auth; routes are role-gated (`COACH` / `CLIENT`)
- **Token TTLs**: access token = 15 min, refresh = 30 days (see `application.properties`)
- Controller URL convention: `/api/coach/**` for coach routes, `/api/client/**` for client routes
- Always add tests for new services in `src/test/`

---

## Key Domain Concepts

| Concept | Description |
|---|---|
| **Coach** | A trainer who manages multiple clients |
| **Client** | An athlete assigned to a coach |
| **Coaching relationship** | Coach invites client via email; client accepts |
| **Questionnaire** | Coach creates intake forms; client fills them in |
| **NutritionPlan** | Coach assigns meal/macro plans to clients |
| **Check-In** | (MVP) Weekly client progress reports (weight, photos, notes) |
| **Chat** | Real-time WebSocket messaging between coach and client |

---

## MVP TODO (from TODO.md)

### In progress / Pending
- Client location + join date fields
- Questionnaire templating (Q&A after activation)
- **Check-Ins** — weight, nutrition adherence, photos, sleep, steps, energy level
- **Reminder/Notification system** — check-in reminders, chat message alerts
- **SupplementPlan**
- **Statistics/Analytics** — progress graphs over time
- **Training plan** creation and management
- **Profile pictures** — determine best storage approach for dev/prod
- **Peakweek planning**

### DevOps / Cloud
- CI/CD with GitHub Actions
- Flyway for DB migrations
- JSON logs → Promtail → Loki
- Prometheus + Grafana via Spring Actuator

### Beyond MVP
- Upcoming competitions tracking
- User settings (notifications, chat dock reset)
- Coach dashboard widgets (client count, pending check-ins, new messages)
- CSRF protection audit for production

---

## Development Notes

- CORS is configured in `SecurityConfig` — frontend runs on `http://localhost:5173` in dev
- WebSocket endpoint is `/ws`; chat uses STOMP over SockJS
- Email templates are HTML files in `src/main/resources/email/`
- Application properties are profile-specific — edit `application-dev.properties` for local changes, never commit secrets to `application-prod.properties`
- The frontend `api.js` utility handles auth headers and token refresh — keep all API logic there
