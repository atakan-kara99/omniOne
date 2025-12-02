# 🧩 MVP-Spezifikation – Bodybuilding Coaching Plattform (Single-Coach Version)

## 1. 🎯 Zielsetzung

Der MVP ist eine **Webplattform für einen Coach**, der **mehrere Athleten** betreut.
Ziel: Der Coach kann **Ernährungsvorgaben** individuell für jeden Athleten erstellen.
Athleten können diese Vorgaben **einsehen** und regelmäßige **Check-ins** durchführen, um ihren Fortschritt zu dokumentieren (inkl. Gewicht und tatsächlicher Ernährung).

---

## 2. 👥 Benutzerrollen

### 👨‍🏫 Coach

* Kann Athleten anlegen, bearbeiten und löschen
* Kann Ernährungsvorgaben pro Athlet erstellen und anpassen
* Kann Check-ins seiner Athleten einsehen

### 💪 Athlet

* Kann sich einloggen
* Kann seine aktuelle Ernährungsvorgabe einsehen
* Kann einen Check-in erstellen (mit aktuellem Gewicht & tatsächlicher Ernährung)

---

## 3. ⚙️ Hauptfunktionen (funktionale Anforderungen)

### **Coach-Funktionen**

| Funktion                                   | Beschreibung                                                                                                                                                       |
|--------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Athletenverwaltung**                     | Coach kann neue Athleten anlegen (Name, E-Mail, Geschlecht, Geburtsdatum optional)                                                                                 |
| **Ernährungsvorgabe erstellen/bearbeiten** | Pro Athlet werden tägliche Zielwerte gespeichert:<br>• Kalorien (kcal)<br>• Protein (g)<br>• Kohlenhydrate (g)<br>• Fett (g)<br>• Wasser (ml oder L)<br>• Salz (g) |
| **Check-ins einsehen**                     | Coach sieht eine Liste aller Check-ins seiner Athleten (Datum, Gewicht, Abweichungen)                                                                              |

---

### **Athlet-Funktionen**

| Funktion                       | Beschreibung                                                                                                                                                                                                                                                  |
|--------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Login / Authentifizierung**  | Athlet kann sich mit Zugangsdaten anmelden (vielleicht initial vom Coach erstellt)                                                                                                                                                                            |
| **Ernährungsvorgabe einsehen** | Athlet sieht aktuelle Zielwerte, die der Coach festgelegt hat                                                                                                                                                                                                 |
| **Check-in erstellen**         | Athlet erstellt einen neuen Check-in mit folgenden Feldern:<br>• Datum (automatisch)<br>• Gewicht (in kg)<br>• Kalorien, Protein, Kohlenhydrate, Fett, Wasser, Salz (vorausgefüllt aus Vorgabe, aber änderbar)<br>• Optional: Kommentar oder Bemerkung (frei) |
| **Check-in Verlauf ansehen**   | Athlet sieht eine Liste seiner bisherigen Check-ins (Datum, Gewicht, ggf. Abweichungen)                                                                                                                                                                       |

---

## 4. 🧱 Systemfunktionen

| Bereich               | Beschreibung                                               |
|-----------------------|------------------------------------------------------------|
| **Authentifizierung** | Einfache E-Mail + Passwort Authentifizierung (JWT-basiert) |
| **Rollenmanagement**  | Unterscheidung Coach / Athlet                              |
| **Datenspeicherung**  | PostgreSQL                                                 |
| **Frontend**          | Thymeleaf                                                  |
| **Backend**           | Spring Boot                                                |

---

## 5. 💾 Vorschlag für Datenmodell (vereinfachtes ERD)

```
COACH (1)
│
└── ATHLETE (n)
      ├── nutrition_plan (1)
      └── checkin (n)
```

### Tabellen (vereinfachte Struktur)

**`coach`**

* id
* name
* email
* password_hash

**`athlete`**

* id
* coach_id
* name
* email
* password_hash (optional, wenn Athlet Login braucht)
* gender
* birth_date

**`nutrition_plan`**

* id
* athlete_id
* calories
* protein
* carbs
* fat
* water
* salt
* created_at
* updated_at

**`checkin`**

* id
* athlete_id
* weight
* calories
* protein
* carbs
* fat
* water
* salt
* comment
* created_at

---

## 6. 🧩 Nicht-funktionale Anforderungen

| Kategorie       | Beschreibung                                                              |
|-----------------|---------------------------------------------------------------------------|
| **Performance** | < 1 Sekunde Antwortzeit pro API-Call im lokalen Betrieb                   |
| **Sicherheit**  | Passwort-Hashing (bcrypt/argon2), JWT-Token für Auth                      |
| **Datenschutz** | Keine sensiblen Gesundheitsdaten außer Gewicht & Ernährung, DSGVO-konform |
| **Usability**   | Einfaches Dashboard für Coach & Athlet, minimalistische UI                |
| **Deployment**  | Single-instance Anwendung (lokal oder Cloud), kein Mandantenkonzept       |

---

## 7. 🚀 MVP-Roadmap

| Phase       | Ziel              | Hauptfeatures                                              |
|-------------|-------------------|------------------------------------------------------------|
| **Phase 1** | Auth & Basisdaten | Login-System, Coach kann Athleten anlegen                  |
| **Phase 2** | Ernährung         | Ernährungsvorgaben pro Athlet speichern & anzeigen         |
| **Phase 3** | Check-ins         | Athlet kann Check-ins erstellen & Verlauf ansehen          |
| **Phase 4** | Coach-Dashboard   | Coach sieht Check-ins aller Athleten (Tabelle oder Grafik) |
