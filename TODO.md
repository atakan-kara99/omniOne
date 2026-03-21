# omniOne — MVP TODO

## Frontend

- [ ] Questionnaire templating — Q&A flow after client activation
- [ ] Check-In UI — submission form (weight, nutrition adherence, photos, sleep, steps, energy level, comment)
- [ ] Check-In history view — list of past check-ins per client
- [ ] Notification badges — unread chat messages, pending check-ins
- [ ] Profile picture — upload + display for coaches and clients
- [ ] Supplement plan view (client + coach side)
- [ ] Statistics / Analytics — progress graphs over time (weight, macros, etc.)
- [ ] Training plan view — display assigned training plan
- [ ] Peak week planning page

## Backend

- [ ] Client entity — add location field and joinedAt field
- [ ] Questionnaire templating — complete templating logic after activation
- [ ] Check-In entity + endpoints
  - Fields: weight, nutrition adherence, photos, comment, date, satiation/hunger, energy level, sleep, steps
  - `POST /api/client/check-ins`
  - `GET /api/coach/clients/{id}/check-ins`
- [ ] Reminder / Notification system
  - Check-in reminders (scheduled)
  - Chat message alerts
- [ ] SupplementPlan entity + CRUD endpoints
- [ ] Statistics / Analytics endpoints — aggregated progress data per client
- [ ] Training plan entity + CRUD endpoints
- [ ] Profile picture — file storage (decide: local disk vs S3 for dev/prod)
- [ ] Peak week planning entity + endpoints

## DevOps

- [ ] CI/CD pipeline with GitHub Actions (build + test on push)
- [ ] Flyway for DB migrations
- [ ] JSON structured logging → Promtail → Loki
- [ ] Prometheus metrics + Grafana dashboard via Spring Actuator
- [ ] Test coverage with JUnit, Mockito, JaCoCo

---

# Beyond MVP

## Features

- [ ] Upcoming competitions tracking
- [ ] User settings
  - [ ] Chat dock scroll position reset
  - [ ] Email notification preferences

## Frontend

- [ ] Chat — save thread scroll position per conversation
- [ ] Chat — investigate slow typing with large message loads
- [ ] Clients page — show total client count
- [ ] Coach dashboard widgets
  - [ ] Total clients (count)
  - [ ] Pending check-ins (count)
  - [ ] New messages (count)
  - [ ] Active client status table (name, status, last check-in, goal)

## Backend

- [ ] CSRF — audit and verify configuration is correct for production
