# TODO's for MVP

## Frontend

## Backend

* Profile picture for users
  * Check which is best approach for dev and prod
* Check-Ins
  * Weight, NutriPlan, Comment, Date, Photos, Videos, Satiation/Hunger, Energielevel, Sleep, Steps
* Reminder System / Notifications
  * for Check-In
  * for Chat Messages
* SupplementPlan
* Statistics / Analytics 
  * graphs over time
* Peakweek planning
* Training plan creation & management
* Q&A after client activation (my version is not complete, need templating)
* ws tries to reconnect and jwt is expired -> error overload

## DevOps

* Setup CI/CD with GitHub Actions

## Cloud

* Adopt flyway for DB
* JSON logs + Promtail → Loki
* Prometheus and Grafana with Actuator
* Check out how this should work

## CV & GIT

* Test (JUnit, Mockito), JaCoCo
* STOMP over WebSockets


# Beyond MVP

## Frontend

* Chat
  * Chat thread scroll position saved
  * If a lot of msg are loaded, the typing is slow

## Backend

* CSRF
  * Check if it is configured well in prod