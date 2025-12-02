# 🧩 **Datenmodell – Bodybuilding Coaching Plattform (Single-Coach MVP)**

## 🧠 Überblick (Entitäten & Beziehungen)

```
COACH (1)
│
└── ATHLETE (n)
      ├── NUTRITION_PLAN (1)
      └── CHECKIN (n)
```

👉 Ein Coach hat mehrere Athleten.
👉 Jeder Athlet hat genau **eine aktive Ernährungsvorgabe**.
👉 Jeder Athlet kann **mehrere Check-ins** erstellen.
👉 Ein Check-in speichert die aktuellen Ist-Werte (meist basierend auf der Vorgabe).

---

## 📋 Tabellen im Detail

### **1. coach**

Der einzige Coach der Plattform.

| Feld            | Typ                  | Beschreibung              |
|-----------------|----------------------|---------------------------|
| `id`            | UUID (PK)            | Eindeutige ID des Coaches |
| `name`          | VARCHAR(100)         | Anzeigename               |
| `email`         | VARCHAR(255), unique | Login-E-Mail              |
| `password_hash` | VARCHAR(255)         | Gehashter Passwortwert    |
| `created_at`    | TIMESTAMP            | Erstellungsdatum          |
| `updated_at`    | TIMESTAMP            | Letzte Änderung           |

---

### **2. athlete**

Ein Athlet, der vom Coach betreut wird.

| Feld            | Typ                           | Beschreibung                       |
|-----------------|-------------------------------|------------------------------------|
| `id`            | UUID (PK)                     | Eindeutige ID des Athleten         |
| `coach_id`      | UUID (FK → coach.id)          | Zu welchem Coach gehört der Athlet |
| `name`          | VARCHAR(100)                  | Vollständiger Name                 |
| `email`         | VARCHAR(255), unique          | Login-E-Mail                       |
| `password_hash` | VARCHAR(255)                  | Gehashter Passwortwert             |
| `gender`        | ENUM('male','female','other') | Geschlecht (optional)              |
| `birth_date`    | DATE                          | Geburtsdatum (optional)            |
| `created_at`    | TIMESTAMP                     | Erstellungsdatum                   |
| `updated_at`    | TIMESTAMP                     | Letzte Änderung                    |

---

### **3. nutrition_plan**

Die aktuelle Ernährungsvorgabe eines Athleten.
(optional kann man hier auch eine Historie abbilden, aber für den MVP reicht **eine aktive Vorgabe**)

| Feld         | Typ                    | Beschreibung                        |
|--------------|------------------------|-------------------------------------|
| `id`         | UUID (PK)              | Eindeutige ID                       |
| `athlete_id` | UUID (FK → athlete.id) | Zu welchem Athleten gehört der Plan |
| `calories`   | INT                    | Zielkalorien pro Tag                |
| `protein`    | DECIMAL(5,1)           | Protein in Gramm                    |
| `carbs`      | DECIMAL(5,1)           | Kohlenhydrate in Gramm              |
| `fat`        | DECIMAL(5,1)           | Fett in Gramm                       |
| `water`      | DECIMAL(5,1)           | Wasser in Litern                    |
| `salt`       | DECIMAL(5,1)           | Salz in Gramm                       |
| `created_at` | TIMESTAMP              | Datum der Erstellung                |
| `updated_at` | TIMESTAMP              | Datum der letzten Änderung          |

---

### **4. checkin**

Ein Check-in, den der Athlet manuell erstellt.
Die Werte sind **voreingestellt** aus seinem aktuellen `nutrition_plan`, können aber angepasst werden.

| Feld         | Typ                    | Beschreibung                       |
|--------------|------------------------|------------------------------------|
| `id`         | UUID (PK)              | Eindeutige ID                      |
| `athlete_id` | UUID (FK → athlete.id) | Zugehöriger Athlet                 |
| `date`       | DATE                   | Check-in-Datum (Standard: `NOW()`) |
| `weight`     | DECIMAL(5,2)           | Gewicht in kg                      |
| `calories`   | INT                    | Tatsächlich konsumierte Kalorien   |
| `protein`    | DECIMAL(5,1)           | Tatsächliches Protein (g)          |
| `carbs`      | DECIMAL(5,1)           | Tatsächliche Kohlenhydrate (g)     |
| `fat`        | DECIMAL(5,1)           | Tatsächliches Fett (g)             |
| `water`      | DECIMAL(5,1)           | Tatsächliches Wasser (L)           |
| `salt`       | DECIMAL(5,1)           | Tatsächliches Salz (g)             |
| `comment`    | TEXT                   | Optionaler Kommentar               |
| `created_at` | TIMESTAMP              | Erstellungszeitpunkt               |

---

### **5. optional: session_token (wenn JWT-Refresh verwendet wird)**

Nicht unbedingt nötig im MVP, aber falls du JWT-Refresh-Tokens oder Session-Invalidierung willst:

| Feld         | Typ                     | Beschreibung                       |
|--------------|-------------------------|------------------------------------|
| `id`         | UUID (PK)               | Eindeutige ID                      |
| `user_id`    | UUID                    | ID des Nutzers (Coach oder Athlet) |
| `role`       | ENUM('coach','athlete') | Rolle des Nutzers                  |
| `token`      | TEXT                    | Refresh-Token                      |
| `expires_at` | TIMESTAMP               | Ablaufdatum                        |

---

## 🔗 Relationen (Formal beschrieben)

| Beziehung                    | Typ   | Beschreibung                                         |
|------------------------------|-------|------------------------------------------------------|
| `coach` → `athlete`          | 1 : n | Ein Coach betreut mehrere Athleten                   |
| `athlete` → `nutrition_plan` | 1 : 1 | Jeder Athlet hat genau eine aktive Ernährungsvorgabe |
| `athlete` → `checkin`        | 1 : n | Jeder Athlet hat mehrere Check-ins                   |

---

## ⚙️ Integritätsbedingungen

* `nutrition_plan.athlete_id` → `athlete.id` (ON DELETE CASCADE)
* `checkin.athlete_id` → `athlete.id` (ON DELETE CASCADE)
* `athlete.coach_id` → `coach.id` (ON DELETE CASCADE)
* E-Mails (`athlete.email`, `coach.email`) sind **unique**
* Wertebereiche validieren:

    * `calories`, `protein`, `carbs`, `fat`, `water`, `salt` ≥ 0
    * `weight` > 0

---

## 🧩 Bonus: Beispiel-Datensatz (zum Testen)

**Coach**

```json
{
  "id": "c1",
  "name": "Max Mustermann",
  "email": "max@coach.com"
}
```

**Athlete**

```json
{
  "id": "a1",
  "coach_id": "c1",
  "name": "Anna Beispiel",
  "email": "anna@athlete.com"
}
```

**Nutrition Plan**

```json
{
  "athlete_id": "a1",
  "calories": 2200,
  "protein": 160,
  "carbs": 250,
  "fat": 70,
  "water": 3,
  "salt": 5
}
```

**Check-in**

```json
{
  "athlete_id": "a1",
  "date": "2025-11-09",
  "weight": 63.5,
  "calories": 2150,
  "protein": 158,
  "carbs": 245,
  "fat": 68,
  "water": 3.2,
  "salt": 4.8,
  "comment": "Alles gut, etwas weniger Appetit heute."
}
```
