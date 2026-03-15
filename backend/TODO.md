# TODO's for MVP

## Frontend

## Backend

* Client
  * Wohnort (Austin, TX), joined at
* Q&A after client activation (my version is not complete, need templating)
* Check-Ins
  * Weight, NutriPlan, Comment, Date, Photos, (Videos), Satiation/Hunger, Energielevel, Sleep, Steps
  * (PENDING, ...)
* Reminder System / Notifications
  * for Check-In
  * for Chat Messages
* SupplementPlan
* Statistics / Analytics 
  * graphs over time
* Training plan creation & management
* Profile picture for users
  * Check which is best approach for dev and prod
* Peakweek planning

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

## Features

* Upcoming Competitions
* Settings
  * Chat Dock reset
  * Notification (Mail)

## Frontend

* Chat
  * Chat thread scroll position saved
  * If a lot of msg are loaded, the typing is slow
* Clients
  * total clients
* Dashboard
  * Total Clients (count)
  * Pending Check-Ins (count)
  * New messages (count)
  * Active Client Status
    * Name, Status, Last Check-In, Goal (Hypertrophy, Fat Loss, Comp Prep, Maintenance)

## Backend

* CSRF
  * Check if it is configured well in prod