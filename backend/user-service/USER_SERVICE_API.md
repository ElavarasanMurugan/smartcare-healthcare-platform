# SmartCare User Service API (`v1`)

The **User Service** is a Spring Boot microservice (Java 17, Spring Data JPA, Hibernate, PostgreSQL/Supabase) running on port `8081` by default, using the `user_service` database schema.

Base URL (local): `http://localhost:8081/api/v1/users`

---

## 1. Endpoints Overview

| HTTP Method | Endpoint | Description | Success Status |
| --- | --- | --- | --- |
| `POST` | `/api/v1/users` | Create a new user | `201 Created` |
| `GET` | `/api/v1/users` | Retrieve all users | `200 OK` |
| `GET` | `/api/v1/users/{id}` | Retrieve a user by ID | `200 OK` |
| `PUT` | `/api/v1/users/{id}` | Update an existing user | `200 OK` |
| `DELETE` | `/api/v1/users/{id}` | Delete a user by ID | `204 No Content` |

---

## 2. Implemented Input Validations (`UserRequest`)

Validation is enforced on the `UserRequest` DTO using Jakarta Bean Validation (`@Valid` in `UserController`):

| Field | Rules |
| --- | --- |
| `fullName` | Required (`@NotBlank`), rejects null/empty/whitespace-only values, max `100` characters (`@Size(max = 100)`). Trimmed before saving. |
| `email` | Required (`@NotBlank`), valid email format (`@Email`), max `254` characters (`@Size(max = 254)`), unique across all users. Normalized to lowercase and trimmed before saving. |
| `phoneNumber` | Optional. If provided, max `20` characters (`@Size(max = 20)`) and must match `^$\|^[+]?[0-9][0-9\s\-()]{6,19}$` (`@Pattern`), supporting international formats such as `+91-9876543210` or `+1 (555) 123-4567`. Blank values are stored as `null`. |

---

## 3. Request & Response Examples

### Valid Registration Request (`POST /api/v1/users`)

**Request JSON:**
```json
{
  "fullName": "Aarav Sharma",
  "email": "aarav.sharma@example.com",
  "phoneNumber": "+91-9876543210"
}
```

**Response JSON (`201 Created`):**
```json
{
  "id": 1,
  "fullName": "Aarav Sharma",
  "email": "aarav.sharma@example.com",
  "phoneNumber": "+91-9876543210",
  "createdAt": "2026-11-25T18:30:00Z",
  "updatedAt": "2026-11-25T18:30:00Z"
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
    "createdAt": "2026-11-25T18:30:00Z",
    "updatedAt": "2026-11-25T18:30:00Z"
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
  "createdAt": "2026-11-25T18:30:00Z",
  "updatedAt": "2026-11-25T18:30:00Z"
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
  "createdAt": "2026-11-25T18:30:00Z",
  "updatedAt": "2026-11-25T18:35:00Z"
}
```

### Delete User (`DELETE /api/v1/users/{id}`)

- Returns `204 No Content` with an empty response body.

---

## 4. Error Response Format & HTTP Status Codes

All errors are handled centrally by `GlobalExceptionHandler` (`@RestControllerAdvice`) and never expose stack traces, SQL queries, or credentials.

| HTTP Status | Condition | Description |
| --- | --- | --- |
| `200 OK` | Successful `GET` or `PUT` | Request succeeded |
| `201 Created` | Successful `POST` | User created |
| `204 No Content` | Successful `DELETE` | User deleted |
| `400 Bad Request` | `MethodArgumentNotValidException`, `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException` | Invalid request data, malformed JSON, or invalid path parameter |
| `404 Not Found` | `ResourceNotFoundException` / `UserNotFoundException` | Requested user does not exist |
| `409 Conflict` | `DuplicateEmailException` or unique email `DataIntegrityViolationException` | Email address is already in use |
| `500 Internal Server Error` | Unhandled `Exception` | Generic unexpected server error |

### Example Invalid Request (`400 Bad Request`)

**Request JSON:**
```json
{
  "fullName": "   ",
  "email": "not-an-email",
  "phoneNumber": "abc123"
}
```

**Response JSON (`400 Bad Request`):**
```json
{
  "timestamp": "2026-11-25T18:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields.",
  "path": "/api/v1/users",
  "fieldErrors": {
    "fullName": "Full name is required",
    "email": "Email must be a valid email address",
    "phoneNumber": "Phone number must be 7 to 20 characters and contain only digits, spaces, hyphens, parentheses, or a leading +"
  }
}
```

### Example Duplicate Email Response (`409 Conflict`)

**Response JSON (`409 Conflict`):**
```json
{
  "timestamp": "2026-11-25T18:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "A user with email 'aarav.sharma@example.com' already exists.",
  "path": "/api/v1/users"
}
```

---

## 5. Required Environment Variables

Set these environment variables before running the service against Supabase PostgreSQL:

| Variable | Description | Example |
| --- | --- | --- |
| `DB_URL` | Supabase PostgreSQL JDBC URL with SSL enabled | `jdbc:postgresql://<host>:<port>/<database>?sslmode=require` |
| `DB_USERNAME` | Supabase PostgreSQL username | `<your-db-username>` |
| `DB_PASSWORD` | Supabase PostgreSQL password | `<your-db-password>` |
| `SPRING_PROFILES_ACTIVE` | Optional active Spring profile (`dev` by default, `prod` for production) | `dev` |
| `SERVER_PORT` | Optional server port (defaults to `8081`) | `8081` |

---

## 6. Running Locally (Windows PowerShell)

1. Navigate to the service directory:
   ```powershell
   cd backend\user-service
   ```
2. Run automated tests (does not require a live Supabase connection):
   ```powershell
   mvn test
   ```
3. Set your Supabase PostgreSQL environment variables for the current PowerShell session:
   ```powershell
   $env:DB_URL="jdbc:postgresql://<your-supabase-host>:<port>/<db>?sslmode=require"
   $env:DB_USERNAME="<your-supabase-username>"
   $env:DB_PASSWORD="<your-supabase-password>"
   ```
4. Start the service:
   ```powershell
   mvn spring-boot:run
   ```
