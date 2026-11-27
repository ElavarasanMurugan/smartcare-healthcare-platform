# SmartCare User Service API (`v1`)

The **User Service** is a Spring Boot microservice (Java 17, Spring Data JPA, Hibernate, Spring Security, PostgreSQL/Supabase) running on port `8081` by default, using the `user_service` database schema.

Base URL (local): `http://localhost:8081`

---

## 1. Endpoints Overview

| HTTP Method | Endpoint | Description | Request DTO | Success Status |
| --- | --- | --- | --- | --- |
| `POST` | `/api/v1/auth/login` | Authenticate user and issue JWT | `LoginRequest` | `200 OK` |
| `POST` | `/api/v1/users` | Register a new user with a password | `UserRegistrationRequest` | `201 Created` |
| `GET` | `/api/v1/users` | Retrieve all users | — | `200 OK` |
| `GET` | `/api/v1/users/{id}` | Retrieve a user by ID | — | `200 OK` |
| `PUT` | `/api/v1/users/{id}` | Update user profile details | `UserRequest` | `200 OK` |
| `DELETE` | `/api/v1/users/{id}` | Delete a user by ID | — | `204 No Content` |

---

## 2. Authentication & JWT (Phase 4)

### Login Endpoint (`POST /api/v1/auth/login`)

**Request JSON:**
```json
{
  "email": "arun.phase3@example.com",
  "password": "SmartCare@123"
}
```
- `email`: Required (`@NotBlank`), valid email format (`@Email`), max 254 characters, normalized to lowercase and trimmed before lookup.
- `password`: Required (`@NotBlank`), verified against the stored BCrypt hash using `PasswordEncoder.matches(...)`.

**Successful Login Response (`200 OK`):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJhcnVuLnBoYXNlM0BleGFtcGxlLmNvbSIsImlhdCI6MTc5NTc2MDAwMCwiZXhwIjoxNzk1NzYzNjAwfQ.signature",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "fullName": "Arun Kumar",
    "email": "arun.phase3@example.com"
  }
}
```

**Invalid Credentials Response (`401 Unauthorized`):**
Returned for both an unregistered email and an incorrect password so attackers cannot enumerate registered accounts:
```json
{
  "timestamp": "2026-11-27T09:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password.",
  "path": "/api/v1/auth/login"
}
```

### JWT Structure & Purpose
A JSON Web Token (JWT) has three Base64URL-encoded parts separated by dots (`header.payload.signature`):
1. **Header**: Signing algorithm (`HS256`).
2. **Payload (Claims)**:
   - `sub` (Subject): User's unique ID (e.g. `"1"`).
   - `email`: Normalized user email.
   - `iat`: Issued-at timestamp.
   - `exp`: Expiration timestamp.
   - *Passwords and password hashes are never included in JWT claims.*
3. **Signature**: HMAC-SHA256 signature computed with `JWT_SECRET` so any tampering is detected.

### Sending the Token in Future Requests
Clients should include the token in the HTTP `Authorization` header:
```http
Authorization: Bearer <accessToken>
```
*(Note: In Phase 4, Spring Security is configured as stateless with CSRF, HTTP Basic, and form login disabled, permitting `/api/v1/auth/login` and `/api/v1/users/**`. Endpoint-level JWT enforcement and role-based access control will be enabled in Phase 5.)*

---

## 3. Password Security & BCrypt Hashing (Phase 3)

### Registration Flow
1. The client submits `fullName`, `email`, optional `phoneNumber`, and `password` to `POST /api/v1/users`.
2. `UserRegistrationRequest` validates all fields using Jakarta Bean Validation and strict JSON type checking (`StrictStringDeserializer`).
3. `UserService` normalizes the email (trimmed and lowercased) and checks for duplicates.
4. `UserService` hashes the raw password using Spring Security's `BCryptPasswordEncoder` (`PasswordEncoder.encode(...)`).
5. Only the BCrypt hash is saved in the `password_hash` (`VARCHAR(255)`) column in PostgreSQL.
6. The controller returns `UserResponse`, which **never** includes `password` or `passwordHash`.

### Why Plaintext Passwords Are Never Stored & Hashes Are Excluded
- Storing only salted BCrypt hashes protects user credentials if the database is ever exposed.
- Keeping `UserResponse` and `LoginResponse` strictly limited to non-sensitive profile fields ensures credentials never leave the backend.

---

## 4. Implemented Input Validations

Validation is enforced on the request DTOs using Jakarta Bean Validation (`@Valid`) and `StrictStringDeserializer` (which rejects numbers, booleans, arrays, and objects for string fields with `400 Bad Request`):

| Field | DTOs | Rules |
| --- | --- | --- |
| `fullName` | `UserRegistrationRequest`, `UserRequest` | Required (`@NotBlank`), max `100` characters, JSON string only. |
| `email` | `UserRegistrationRequest`, `UserRequest`, `LoginRequest` | Required (`@NotBlank`), valid email (`@Email`), max `254` characters, JSON string only. |
| `phoneNumber` | `UserRegistrationRequest`, `UserRequest` | Optional, max `20` characters, matches international phone `@Pattern`, JSON string only. |
| `password` | `UserRegistrationRequest` | Required (`@NotBlank`), min `8` characters, max `72` UTF-8 bytes (`@BcryptMaxBytes`), JSON string only. |
| `password` | `LoginRequest` | Required (`@NotBlank`), JSON string only. |

---

## 5. Error Response Format & HTTP Status Codes

| HTTP Status | Condition | Description |
| --- | --- | --- |
| `200 OK` | Successful `GET`, `PUT`, or `POST /api/v1/auth/login` | Request succeeded |
| `201 Created` | Successful `POST /api/v1/users` | User registered |
| `204 No Content` | Successful `DELETE` | User deleted |
| `400 Bad Request` | Validation error, non-string JSON field, malformed JSON | Invalid request data |
| `401 Unauthorized` | `InvalidCredentialsException` | Invalid email or password |
| `404 Not Found` | `ResourceNotFoundException` / `UserNotFoundException` | Requested user does not exist |
| `409 Conflict` | `DuplicateEmailException` or unique email constraint violation | Email address is already in use |
| `500 Internal Server Error` | Unhandled `Exception` | Generic unexpected server error |

---

## 6. Required Environment Variables

| Variable | Description | Example |
| --- | --- | --- |
| `DB_URL` | Supabase PostgreSQL JDBC URL with SSL enabled | `jdbc:postgresql://<host>:<port>/<database>?sslmode=require` |
| `DB_USERNAME` | Supabase PostgreSQL username | `<your-db-username>` |
| `DB_PASSWORD` | Supabase PostgreSQL password | `<your-db-password>` |
| `JWT_SECRET` | **Required** HS256 signing secret (at least 32 characters / 256 bits; **never** reuse `DB_PASSWORD` or commit to Git) | `<32+-char-random-secret>` |
| `JWT_EXPIRATION_MS` | Optional token lifetime in milliseconds (defaults to `3600000` = 1 hour) | `3600000` |
| `SPRING_PROFILES_ACTIVE` | Optional active Spring profile (`dev` by default, `prod` for production) | `dev` |
| `SERVER_PORT` | Optional server port (defaults to `8081`) | `8081` |

---

## 7. Running Locally (Windows PowerShell)

1. Navigate to the service directory:
   ```powershell
   cd backend\user-service
   ```
2. Run automated tests (does not require a live Supabase connection or real `JWT_SECRET`):
   ```powershell
   .\mvnw.cmd clean test
   ```
3. Generate a strong local `JWT_SECRET` (at least 32 characters) and set environment variables for the current PowerShell session:
   ```powershell
   $env:DB_URL="jdbc:postgresql://<your-supabase-host>:<port>/<db>?sslmode=require"
   $env:DB_USERNAME="<your-supabase-username>"
   $env:DB_PASSWORD="<your-supabase-password>"
   $env:JWT_SECRET="replace-with-your-own-32-plus-character-random-secret-key"
   $env:JWT_EXPIRATION_MS="3600000"
   ```
4. Start the service:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
