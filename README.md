<h1>
  <img src="src/main/resources/omniOne.svg" width="22" alt=""/>
  omniOne Bodybuilding Coaching Platform
</h1>

## Key Features

### 🔐 Security & Authentication

* **JWT-based authentication** for protected endpoints
* Full auth flow support: **registration**, **login**, and common account lifecycle actions
* **Email-driven flows** for: Account activation, Invitations, Password reset

---

### 🧱 Architecture & Code Quality

* Clear separation using **DTOs** to isolate internal domain models from API payloads
* DTO-level **validation** to enforce input constraints consistently
* **MapStruct** for fast, maintainable mapping between **Entities ↔ DTOs**
* **Lombok** to reduce boilerplate (getters/setters/builders, etc.)
* **Global exception handling** with a centralized handler and **custom exceptions**

---

### 🗄️ Data & Persistence

* **PostgreSQL** integration
* Persistence layer built with **Spring Data JPA**
* **Hibernate** for ORM and entity management

---

### 🌐 API & Documentation

* **REST API controllers** running on the embedded **Tomcat** web server
* **OpenAPI specification** included for interactive **Swagger UI** documentation

---

### 📝 Logging & Observability

* Logging configured via **custom Logback** setup to keep logs structured and useful across environments

---

### 🧪 Testing

* Unit and service testing with **JUnit**
* Mocking and behavioral testing with **Mockito**

---

### ⚙️ Configuration & Build

* **Maven** as the build tool
* Multiple Spring profile configurations: `dev`, `test`, `prod`

---

### 🐳 Local Development (Docker)

* **Docker Compose** setup to run the whole stack locally, including:

  * **PostgreSQL** as database
  * **Adminer** for DB inspection/management
  * **MailHog** for capturing and previewing outgoing emails during development
