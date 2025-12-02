# 🧩 **API-Spezifikation – Bodybuilding Coaching Plattform (MVP)**

## 🔐 Authentifizierung

**JWT-basierte Authentifizierung**

* Alle Endpunkte (außer `/auth/register` und `/auth/login`) erfordern ein gültiges Token.
* Coach und Athlet teilen sich die Auth-Logik, unterscheiden sich aber durch ihre `role`.

---

## 🌍 **Base URL**

```
/api/v1
```

---

## 🧠 **1. Authentifizierung**

### **POST /auth/register**

> Nur für den Coach (einmalige Registrierung)

**Request**

```json
{
  "name": "Max Mustermann",
  "email": "max@coach.com",
  "password": "supersecret"
}
```

**Response**

```json
{
  "message": "Coach registered successfully",
  "token": "<jwt_token>"
}
```

---

### **POST /auth/login**

> Für Coach **oder** Athlet

**Request**

```json
{
  "email": "anna@athlete.com",
  "password": "123456"
}
```

**Response**

```json
{
  "token": "<jwt_token>",
  "user": {
    "id": "a1",
    "role": "athlete",
    "name": "Anna Beispiel"
  }
}
```

---

## 👨‍🏫 **2. Coach-Routen**

Nur zugänglich, wenn `role = coach`.

---

### **GET /coach/athletes**

> Liste aller Athleten des Coaches

**Response**

```json
[
  {
    "id": "a1",
    "name": "Anna Beispiel",
    "email": "anna@athlete.com",
    "created_at": "2025-11-09T12:00:00Z"
  }
]
```

---

### **POST /coach/athletes**

> Neuen Athleten anlegen

**Request**

```json
{
  "name": "Lukas Test",
  "email": "lukas@athlete.com",
  "password": "test123",
  "gender": "male",
  "birth_date": "1998-06-15"
}
```

**Response**

```json
{
  "message": "Athlete created successfully",
  "athlete_id": "a2"
}
```

---

### **PATCH /coach/athletes/:athleteId**

> Daten eines Athleten bearbeiten

**Request**

```json
{
  "name": "Lukas Müller",
  "email": "lukas.m@athlete.com"
}
```

**Response**

```json
{
  "message": "Athlete updated successfully"
}
```

---

### **DELETE /coach/athletes/:athleteId**

> Athleten löschen

**Response**

```json
{
  "message": "Athlete deleted successfully"
}
```

---

## 🍽️ **3. Ernährungsvorgabe**

### **GET /coach/athletes/:athleteId/nutrition-plan**

> Aktive Ernährungsvorgabe eines Athleten abrufen

**Response**

```json
{
  "athlete_id": "a1",
  "calories": 2200,
  "protein": 160,
  "carbs": 250,
  "fat": 70,
  "water": 3.0,
  "salt": 5.0,
  "updated_at": "2025-11-09T12:00:00Z"
}
```

---

### **POST /coach/athletes/:athleteId/nutrition-plan**

> Neue Ernährungsvorgabe erstellen oder bestehende aktualisieren

**Request**

```json
{
  "calories": 2300,
  "protein": 165,
  "carbs": 260,
  "fat": 72,
  "water": 3.2,
  "salt": 5.5
}
```

**Response**

```json
{
  "message": "Nutrition plan updated successfully"
}
```

---

## 📆 **4. Check-ins**

### **GET /coach/checkins**

> Liste aller Check-ins aller Athleten

**Response**

```json
[
  {
    "athlete_id": "a1",
    "athlete_name": "Anna Beispiel",
    "date": "2025-11-09",
    "weight": 63.5,
    "calories": 2150
  },
  {
    "athlete_id": "a2",
    "athlete_name": "Lukas Müller",
    "date": "2025-11-09",
    "weight": 82.1,
    "calories": 2450
  }
]
```

---

### **GET /coach/athletes/:athleteId/checkins**

> Alle Check-ins eines bestimmten Athleten abrufen

**Response**

```json
[
  {
    "id": "chk1",
    "date": "2025-11-09",
    "weight": 63.5,
    "calories": 2150,
    "protein": 158,
    "comment": "Alles gut, etwas weniger Appetit heute."
  }
]
```

---

## 💪 **5. Athleten-Routen**

Nur zugänglich, wenn `role = athlete`.

---

### **GET /athlete/me**

> Profildaten des eingeloggten Athleten

**Response**

```json
{
  "id": "a1",
  "name": "Anna Beispiel",
  "email": "anna@athlete.com",
  "gender": "female",
  "birth_date": "2000-03-12"
}
```

---

### **GET /athlete/nutrition-plan**

> Aktuelle Ernährungsvorgabe anzeigen

**Response**

```json
{
  "calories": 2200,
  "protein": 160,
  "carbs": 250,
  "fat": 70,
  "water": 3.0,
  "salt": 5.0
}
```

---

### **GET /athlete/checkins**

> Liste der bisherigen Check-ins

**Response**

```json
[
  {
    "id": "chk1",
    "date": "2025-11-09",
    "weight": 63.5,
    "calories": 2150
  },
  {
    "id": "chk2",
    "date": "2025-11-02",
    "weight": 64.1,
    "calories": 2200
  }
]
```

---

### **POST /athlete/checkins**

> Neuen Check-in erstellen
> Werte aus `nutrition_plan` werden automatisch vorausgefüllt, können aber angepasst werden.

**Request**

```json
{
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

**Response**

```json
{
  "message": "Check-in created successfully",
  "checkin_id": "chk1"
}
```

---

## ⚠️ **Fehlerbehandlung (Standard)**

| Code  | Bedeutung             | Beispiel                         |
|-------|-----------------------|----------------------------------|
| `400` | Ungültige Anfrage     | Fehlende Pflichtfelder           |
| `401` | Nicht authentifiziert | Fehlendes oder ungültiges Token  |
| `403` | Keine Berechtigung    | Athlet ruft Coach-Endpunkt auf   |
| `404` | Nicht gefunden        | Athlet oder Plan existiert nicht |
| `500` | Serverfehler          | Unerwartete Ausnahme             |

---

## 🔒 **Sicherheitsaspekte**

* JWT-Auth-Header:

  ```
  Authorization: Bearer <token>
  ```
* Passwörter mit **bcrypt/argon2** gehasht
* Input-Validierung serverseitig (z. B. min/max Werte für Kalorien, Wasser, Gewicht)
