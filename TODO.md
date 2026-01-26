# TODO's for MVP

## Frontend


## Backend

* POST /auth/token/refresh, POST /auth/logout? 
  * Detect reuse (if an old refresh token is presented after rotation → revoke the whole session)
* Profile picture for users
    * Check which is best approach for dev and prod
* Check-Ins
    * Weight, NutriPlan, Comment, Date, Photos, Videos, Satiation/Hunger, Energielevel, Sleep, Steps
* Reminder system (for Check-In)
* Supplements
* Statistics (w graphs over time)
* Peakweek planning
* Training
* Q&A after client activation (my version is not complete, need templating)

## DevOps

* Setup CI/CD with GitHub Actions

## Cloud

* Adopt flyway for DB
* JSON logs + Promtail → Loki
* Prometheus and Grafana with Actuator
* Check out how this should work

## CV

* Test (JUnit, Mockito), JaCoCo
* STOMP over WebSockets


# Beyond MVP

## Frontend

* Chat
  * Chat thread scroll position saved

## Backend

* Add deviceId to RefreshTokens
  * 1 refreshToken per device and user.
* Check 