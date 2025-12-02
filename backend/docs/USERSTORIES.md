# 🧩 **User Stories – MVP Bodybuilding Coaching Plattform**

---

## 👨‍🏫 **Rolle: Coach**

### 🧠 **Athletenverwaltung**

| ID  | User Story                                                                                           | Priorität |
|-----|------------------------------------------------------------------------------------------------------|-----------|
| C-1 | Als Coach möchte ich neue Athleten anlegen, um sie in meinem System zu verwalten.                    | M         |
| C-2 | Als Coach möchte ich Athleten bearbeiten (z. B. Name, E-Mail), um Änderungen zu aktualisieren.       | M         |
| C-3 | Als Coach möchte ich Athleten löschen, um inaktive oder falsche Einträge zu entfernen.               | S         |
| C-4 | Als Coach möchte ich alle meine Athleten in einer Liste sehen, um schnell den Überblick zu behalten. | M         |

---

### 🍽️ **Ernährungsvorgaben**

| ID  | User Story                                                                                                                                           | Priorität |
|-----|------------------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| C-5 | Als Coach möchte ich für jeden Athleten eine Ernährungsvorgabe festlegen, damit er weiß, wie viele Kalorien und Makronährstoffe er konsumieren soll. | M         |
| C-6 | Als Coach möchte ich bestehende Ernährungsvorgaben bearbeiten können, wenn sich die Ziele oder Bedingungen eines Athleten ändern.                    | M         |
| C-7 | Als Coach möchte ich sehen, wann eine Ernährungsvorgabe zuletzt geändert wurde, um den Verlauf nachvollziehen zu können.                             | S         |

---

### 📊 **Check-in Übersicht**

| ID   | User Story                                                                                                                         | Priorität |
|------|------------------------------------------------------------------------------------------------------------------------------------|-----------|
| C-8  | Als Coach möchte ich alle Check-ins meiner Athleten sehen, um deren Fortschritt zu überprüfen.                                     | M         |
| C-9  | Als Coach möchte ich den Verlauf der Check-ins eines bestimmten Athleten sehen, um Trends (z. B. Gewichtsentwicklung) zu erkennen. | M         |
| C-10 | Als Coach möchte ich Check-ins nach Datum sortieren oder filtern können, um schnell die letzten Einträge zu finden.                | S         |

---

## 💪 **Rolle: Athlet**

### 🔐 **Authentifizierung**

| ID  | User Story                                                                                                        | Priorität |
|-----|-------------------------------------------------------------------------------------------------------------------|-----------|
| A-1 | Als Athlet möchte ich mich mit E-Mail und Passwort anmelden, um Zugriff auf meine persönlichen Daten zu erhalten. | M         |
| A-2 | Als Athlet möchte ich mein Passwort ändern können, um meine Sicherheit zu gewährleisten.                          | S         |
| A-3 | Als Athlet möchte ich mich ausloggen können, um mein Konto zu schützen.                                           | M         |

---

### 🍱 **Ernährungsvorgaben**

| ID  | User Story                                                                                                                                                            | Priorität |
|-----|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| A-4 | Als Athlet möchte ich meine aktuelle Ernährungsvorgabe sehen, um zu wissen, wie viele Kalorien, Proteine, Kohlenhydrate, Fette, Wasser und Salz ich konsumieren soll. | M         |
| A-5 | Als Athlet möchte ich sehen, wann mein Coach meine Ernährungsvorgabe zuletzt geändert hat.                                                                            | S         |

---

### 📆 **Check-ins**

| ID   | User Story                                                                                                                                                  | Priorität |
|------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| A-6  | Als Athlet möchte ich einen neuen Check-in erstellen, um meinen aktuellen Zustand zu dokumentieren.                                                         | M         |
| A-7  | Als Athlet möchte ich, dass meine Ernährungsvorgabe beim Check-in automatisch vorausgefüllt ist, damit ich sie nur anpassen muss, falls ich abgewichen bin. | M         |
| A-8  | Als Athlet möchte ich mein Gewicht beim Check-in angeben, damit mein Coach meinen Fortschritt sehen kann.                                                   | M         |
| A-9  | Als Athlet möchte ich optional einen Kommentar hinzufügen (z. B. „heute viel Wasser getrunken“), um Kontext zu geben.                                       | S         |
| A-10 | Als Athlet möchte ich meine bisherigen Check-ins ansehen, um meinen eigenen Fortschritt nachvollziehen zu können.                                           | M         |

---

## ⚙️ **Systemweite Stories**

| ID    | User Story                                                                                                                                      | Priorität |
|-------|-------------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| SYS-1 | Als System möchte ich alle Aktionen (Check-ins, Änderungen) mit Datum speichern, um eine Chronologie zu gewährleisten.                          | M         |
| SYS-2 | Als System möchte ich Benutzerauthentifizierung über sichere Tokens (z. B. JWT) durchführen.                                                    | M         |
| SYS-3 | Als System möchte ich Eingaben validieren (z. B. keine negativen Werte), um Datenqualität sicherzustellen.                                      | M         |
| SYS-4 | Als System möchte ich eine einfache, mobile-optimierte Web-Oberfläche bereitstellen, damit Athleten und Coach das System überall nutzen können. | M         |
