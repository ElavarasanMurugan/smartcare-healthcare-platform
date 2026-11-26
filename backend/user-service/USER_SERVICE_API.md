# SmartCare User Service API (`v1`)

The **User Service** is a Spring Boot microservice (Java 17, Spring Data JPA, Hibernate, PostgreSQL/Supabase) running on port `8081` by default, using the `user_service` database schema.

Base URL (local): `http://localhost:8081/api/v1/users`

---

## 1. Endpoints Overview

| HTTP Method | Endpoint | Description | Request DTO | Success Status |
| --- | --- | --- | --- | --- |
| `POST` | `/api/v1/users` | Register a new user with a password | `UserRegistrationRequest` | `201 Created` |
| `GET` | `/api/v1/users` | Retrieve all users | — | `200 OK` |
| `GET` | `/api/v1/users/{id}` | Retrieve a user by ID | — | `200 OK` |
| `PUT` | `/api/v1/users/{id}` | Update user profile details | `UserRequest` | `200 OK` |
| `DELETE` | `/api/v1/users/{id}` | Delete a user by ID | — | `204 No Content` |

---

## 2. Password Security & BCrypt Hashing (Phase 3)

### Registration Flow
1. The client submits `fullName`, `email`, optional `phoneNumber`, and `password` to `POST /api/v1/users`.
2. `UserRegistrationRequest` validates all fields using Jakarta Bean Validation and strict JSON type checking (`StrictStringDeserializer`).
3. `UserService` normalizes the email (trimmed and lowercased) and checks for duplicates.
4. `UserService` hashes the raw password using Spring Security's `BCryptPasswordEncoder` (`PasswordEncoder.encode(...)`).
5. Only the BCrypt hash is saved in the `password_hash` (`VARCHAR(255)`) column in PostgreSQL.
6. The controller returns `UserResponse`, which **never** includes `password` or `passwordHash`.

### Why Plaintext Passwords Are Never Stored
- If a database backup, query log, or table is ever exposed, plaintext passwords would immediately compromise user accounts across SmartCare and any other services where users reuse passwords.
- BCrypt is a one-way adaptive hashing algorithm with a built-in random salt per password, making brute-force and rainbow-table attacks computationally expensive.

### Why Password Hashes Are Excluded from API Responses
- Exposing even a password hash in `POST`, `GET`, or `PUT` responses leaks sensitive credential material to clients, logs, and browser/proxy caches.
- Keeping `UserResponse` strictly limited to non-sensitive profile fields (`id`, `fullName`, `email`, `phoneNumber`, `createdAt`, `updatedAt`) ensures credentials never leave the backend.

---

## 3. Implemented Input Validations

Validation is enforced on the request DTOs using Jakarta Bean Validation (`@Valid` in `UserController`) and `StrictStringDeserializer` (which rejects numbers, booleans, arrays, and objects for string fields with `400 Bad Request`):

| Field | DTOs | Rules |
| --- | --- | --- |
| `fullName` | `UserRegistrationRequest`, `UserRequest` | Required (`@NotBlank`), max `100` characters (`@Size(max = 100)`), must be a JSON string. Trimmed before saving. |
| `email` | `UserRegistrationRequest`, `UserRequest` | Required (`@NotBlank`), valid email format (`@Email`), max `254` characters (`@Size(max = 254)`), unique across all users, must be a JSON string. Normalized to lowercase and trimmed. |
| `phoneNumber` | `UserRegistrationRequest`, `UserRequest` | Optional. If provided, max `20` characters (`@Size(max = 20)`), must match `^$\|^[+]?[0-9][0-9\s\-()]{6,19}$` (`@Pattern`), must be a JSON string. Blank values are stored as `null`. |
| `password` | `UserRegistrationRequest` only | Required (`@NotBlank`), minimum `8` characters (`@Size(min = 8)`), maximum `72` UTF-8 bytes (`@BcryptMaxBytes` for BCrypt compatibility), must be a JSON string. |

---

## 4. Request & Response Examples

### Valid Registration Request (`POST /api/v1/users`)

**Request JSON:**
```json
{
  "fullName": "Aarav Sharma",
  "email": "aarav.sharma@example.com",
  "phoneNumber": "+91-9876543210",
  "password": "StrongPassword123!"
}
```

**Response JSON (`201 Created`):**
*(Note: `password` and `passwordHash` are never returned.)*
```json
{
  "id": 1,
  "fullName": "Aarav Sharma",
  "email": "aarav.sharma@example.com",
  "phoneNumber": "+91-9876543210",
  "createdAt": "2026-11-26T13:30:00Z",
  "updatedAt": "2026-11-26T13:30:00Z"
}
```

### Get All Users (`GET /api/v1/users`)

**Response JSON (`200 OK`):**
```json
[
  {
    "id": 1,
    "fullName": "Aarav Sharma",
    "email": "aarav.sharma@example.com",
    "phoneNumber": "+91-9876543210",
    "createdAt": "2026-11-26T13:30:00Z",
    "updatedAt": "2026-11-26T13:30:00Z"
  }
]
```

### Get User by ID (`GET /api/v1/users/{id}`)

**Response JSON (`200 OK`):**
```json
{
  "id": 1,
  "fullName": "Aarav Sharma",
  "email": "aarav.sharma@example.com",
  "phoneNumber": "+91-9876543210",
  "createdAt": "2026-11-26T13:30:00Z",
  "updatedAt": "2026-11-26T13:30:00Z"
}
```

### Update User (`PUT /api/v1/users/{id}`)

**Request JSON:**
```json
{
  "fullName": "Aarav R. Sharma",
  "email": "aarav.sharma@example.com",
  "phoneNumber": "+91-9876500000"
}
```

**Response JSON (`200 OK`):**
```json
{
  "id": 1,
  "fullName": "Aarav R. Sharma",
  "email": "aarav.sharma@example.com",
  "phoneNumber": "+91-9876500000",
  "createdAt": "2026-11-26T13:30:00Z",
  "updatedAt": "2026-11-26T13:35:00Z"
}
```

### Delete User (`DELETE /api/v1/users/{id}`)

- Returns `204 No Content` with an empty response body.

---

## 5. Error Response Format & HTTP Status Codes

All errors are handled centrally by `GlobalExceptionHandler` (`@RestControllerAdvice`) and never expose stack traces, SQL queries, passwords, or credentials.

| HTTP Status | Condition | Description |
| --- | --- | --- |
| `200 OK` | Successful `GET` or `PUT` | Request succeeded |
| `201 Created` | Successful `POST` | User registered |
| `204 No Content` | Successful `DELETE` | User deleted |
| `400 Bad Request` | `MethodArgumentNotValidException`, `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException` | Invalid request data, non-string JSON field type, malformed JSON, or invalid path parameter |
| `404 Not Found` | `ResourceNotFoundException` / `UserNotFoundException` | Requested user does not exist |
| `409 Conflict` | `DuplicateEmailException` or unique email `DataIntegrityViolationException` | Email address is already in use |
| `500 Internal Server Error` | Unhandled `Exception` | Generic unexpected server error |

### Example Invalid Registration Request (`400 Bad Request`)

**Request JSON:**
```json
{
  "fullName": "   ",
  "email": "not-an-email",
  "password": "short"
}
```

**Response JSON (`400 Bad Request`):**
```json
{
  "timestamp": "2026-11-26T13:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields.",
  "path": "/api/v1/users",
  "fieldErrors": {
    "fullName": "Full name is required",
    "email": "Email must be a valid email address",
    "password": "Password must be at least 8 characters"
  }
}
```

### Example Duplicate Email Response (`409 Conflict`)

**Response JSON (`409 Conflict`):**
```json
{
  "timestamp": "2026-11-26T13:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "A user with email 'aarav.sharma@example.com' already exists.",
  "path": "/api/v1/users"
}
```

---

## 6. Required Environment Variables

| Variable | Description | Example |
| --- | --- | --- |
| `DB_URL` | Supabase PostgreSQL JDBC URL with SSL enabled | `jdbc:postgresql://<host>:<port>/<database>?sslmode=require` |
| `DB_USERNAME` | Supabase PostgreSQL username | `<your-db-username>` |
| `DB_PASSWORD` | Supabase PostgreSQL password | `<your-db-password>` |
| `SPRING_PROFILES_ACTIVE` | Optional active Spring profile (`dev` by default, `prod` for production) | `dev` |
| `SERVER_PORT` | Optional server port (defaults to `8081`) | `8081` |

---

## 7. Running Locally (Windows PowerShell)

1. Navigate to the service directory:
   ```powershell
   cd backend\user-service
   ```
2. Run automated tests (does not require a live Supabase connection):
   ```powershell
   .\mvnw.cmd clean test
   ```
3. Set your Supabase PostgreSQL environment variables for the current PowerShell session:
   ```powershell
   $env:DB_URL="jdbc:postgresql://<your-supabase-host>:<port>/<db>?sslmode=require"
   $env:DB_USERNAME="<your-supabase-username>"
   $env:DB_PASSWORD="<your-supabase-password>"
   ```
4. Start the service:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
