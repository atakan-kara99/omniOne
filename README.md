# 📌 API Endpoints Overview

## 🔐 Authentication & Account

| Method   | Endpoint                         | Description                       |
| -------- | -------------------------------- | --------------------------------- |
| **POST** | `/auth/account/register`         | Register user (CLIENT or COACH)   |
| **GET**  | `/auth/account/resend?email=`    | Resend activation email           |
| **GET**  | `/auth/account/activate?token=`  | Activate account                  |
| **GET**  | `/auth/invitation/accept?token=` | Accept invitation                 |
| **GET**  | `/auth/password/forgot?email=`   | Send email for forgotten password |
| **POST** | `/auth/password/reset`           | Reset password                    |

---

## 👤 User

| Method   | Endpoint         | Description                |
| -------- | ---------------- | -------------------------- |
| **GET**  | `/user`          | Get user data              |
| **GET**  | `/user/profile`  | Get user profile           |
| **PUT**  | `/user/profile`  | Set or update user profile |
| **POST** | `/user/password` | Change password            |

---

## 🧑‍🏫 Coach

| Method     | Endpoint                                    | Description                          |
| ---------- | ------------------------------------------- | ------------------------------------ |
| **GET**    | `/coach`                                    | Get coach                            |
| **PATCH**  | `/coach`                                    | Update coach                         |
| **DELETE** | `/coach`                                    | Remove coach                         |
| **GET**    | `/coach/clients`                            | Get all clients from a coach         |
| **GET**    | `/coach/clients/{clientId}`                 | Get a specific client                |
| **POST**   | `/coach/clients/invite?email=`              | Send client invitation               |
| **GET**    | `/coach/clients/{clientId}/nutrition-plan`  | Get client's nutrition plan          |
| **POST**   | `/coach/clients/{clientId}/nutrition-plan`  | Add nutrition plan for client        |
| **GET**    | `/coach/clients/{clientId}/nutrition-plans` | Get all nutrition plans for a client |

---

## 🧑‍💼 Client

| Method    | Endpoint                  | Description                      |
| --------- | ------------------------- | -------------------------------- |
| **GET**   | `/client`                 | Get client                       |
| **PATCH** | `/client`                 | Update client                    |
| **GET**   | `/client/nutrition-plan`  | Get client's nutrition plan      |
| **GET**   | `/client/nutrition-plans` | Get all client's nutrition plans |
