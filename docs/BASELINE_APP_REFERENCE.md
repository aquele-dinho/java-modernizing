# Phase 0 Baseline Application – Technical Reference

This document centralizes the **implementation and configuration details** of the Phase 0 baseline application.

Use this file when you need to:
- Understand how the baseline Task Management API behaves
- Look up concrete HTTP examples (cURL) for the REST endpoints
- See how tests, security scanning, and project structure are organized

For the **modernization journey itself**, focus on the guides:
- `docs/GUIDE_PHASE1.md` – Java 11 → 17 & Spring Boot 2.4 → 3.0
- `docs/GUIDE_PHASE2.md` – Java 17 → 21 & optimizations
- `docs/GUIDE_SECURITY.md` – OWASP SCA / security workflow

---

## 1. Baseline Quick Start

### 1.1 Build and Run

```bash
# Navigate to project directory
cd java-modernizing

# Set Java 11 (macOS example)
export JAVA_HOME=$(/usr/libexec/java_home -v 11)

# Clean and build the baseline
mvn clean install

# Run the application
mvn spring-boot:run
```

The application starts on:
- URL: `http://localhost:8080`

### 1.2 Accessing Developer Tools

- **Swagger UI (API docs & testing)**  
  URL: `http://localhost:8080/swagger-ui.html`

- **H2 Console (in-memory DB)**  
  URL: `http://localhost:8080/h2-console`  
  JDBC URL: `jdbc:h2:mem:taskdb`  
  Username: `sa`  
  Password: _(empty)_

---

## 2. Default Credentials

Phase 0 ships with sample users for testing:

| Username | Password  | Roles       |
|----------|-----------|-------------|
| `admin`  | `password`| USER, ADMIN |
| `user`   | `password`| USER        |

These are used heavily by the integration tests and by the examples below.

---

## 3. API Endpoint Reference

### 3.1 Swagger UI Workflow

The easiest way to explore the API is through Swagger UI:

1. Start the application
2. Open `http://localhost:8080/swagger-ui.html`
3. Call `/api/auth/login` to get a JWT
4. Click the **Authorize** button
5. Enter `Bearer YOUR_TOKEN_HERE`
6. Use the UI to trigger authenticated endpoints

### 3.2 Authentication Endpoints (Public)

#### Register New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@demo.com",
    "password": "password123"
  }'
```

#### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'
```

Typical response:

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "username": "admin",
  "email": "admin@demo.com"
}
```

### 3.3 Task Endpoints (Authenticated)

Export the JWT token into an environment variable:

```bash
export TOKEN="your_jwt_token_here"
```

#### Get All Tasks

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/tasks
```

#### Get Task by ID

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/tasks/1
```

#### Create New Task

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "New Task",
    "description": "Task description",
    "status": "OPEN",
    "priority": "HIGH",
    "assignedToId": 1
  }'
```

#### Update Task

```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Task",
    "description": "Updated description",
    "status": "IN_PROGRESS",
    "priority": "MEDIUM",
    "assignedToId": 1
  }'
```

#### Delete Task (Admin Only)

```bash
curl -X DELETE http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 3.4 User Endpoints

#### Get All Users (Admin Only)

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/users
```

#### Get User by ID

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/users/1
```

---

## 4. Tests in Phase 0

The baseline includes unit and integration tests to validate behavior before migration.

### 4.1 Run All Tests

```bash
mvn test
```

This executes:
- Unit tests: `TaskServiceTest`, `UserServiceTest`, `AuthenticationServiceTest`
- Integration tests: `TaskControllerIntegrationTest`, `SecurityIntegrationTest`

### 4.2 Full Verification Build

```bash
mvn clean verify
```

This command cleans the project, runs all tests, and builds the executable JAR.

---

## 5. Security Scanning & Migration (High-Level)

> Important: In the pure Phase 0 baseline, the OWASP Dependency-Check and OpenRewrite plugins are **not** configured in `pom.xml`. They are introduced as part of the guides.

This section captures the **target behavior** once you follow the security and migration guides.

### 5.1 OWASP Dependency-Check

Typical commands after enabling the plugin (see `docs/GUIDE_SECURITY.md`):

```bash
# Run dependency vulnerability check
mvn dependency-check:check

# View the report
open target/dependency-check-report.html
```

To establish a security baseline:

```bash
# 1. Run initial vulnerability scan
mvn clean dependency-check:check

# 2. Review the HTML report
open target/dependency-check-report.html

# 3. Document baseline CVE count
# 4. Create/update dependency-suppression.xml for false positives
```

### 5.2 Migration Path with Security Validation

The guides orchestrate OpenRewrite + OWASP:

- `docs/GUIDE_PHASE1.md` – Spring Boot 3.0 + Java 17
- `docs/GUIDE_PHASE2.md` – Java 21 optimization

Example commands (see guides for context):

```bash
# Preview changes
mvn rewrite:dryRun

# Apply migration
mvn rewrite:run

# Example gated check
mvn clean install dependency-check:check
```

---

## 6. Project Structure (Baseline View)

```text
java-modernizing/
├── src/main/java/dev/tiodati/demo/modernization/
│   ├── ModernizationDemoApplication.java
│   ├── config/          # Security and RestTemplate configuration
│   ├── controller/      # REST API controllers
│   ├── domain/          # JPA entities (User, Task)
│   ├── dto/             # Data Transfer Objects
│   ├── exception/       # Custom exceptions and handlers
│   ├── repository/      # Spring Data JPA repositories
│   ├── security/        # JWT authentication components
│   └── service/         # Business logic layer
├── src/main/resources/
│   ├── application.properties  # App configuration
│   └── data.sql                # Sample data
├── src/test/java/dev/tiodati/demo/modernization/
│   ├── integration/            # Integration tests
│   └── unit/                   # Unit tests
├── docs/
│   ├── RESEARCH.md             # Comprehensive migration research
│   ├── OWASP.md                # OWASP SCA methodology and integration
│   ├── GUIDE_PHASE1.md         # Phase 1 modernization guide
│   ├── GUIDE_PHASE2.md         # Phase 2 modernization guide
│   └── GUIDE_SECURITY.md       # Security & SCA guide
├── pom.xml                     # Maven build (baseline config)
├── WARP.md                     # Technical specification
└── README.md                   # High-level overview & guide entrypoint
```

---

## 7. Intentional Legacy Patterns

The baseline intentionally includes patterns that will be migrated in later phases:

1. `javax.*` imports instead of `jakarta.*`
2. Spring Security method chaining with `.antMatchers()` / `.authorizeRequests()` and `WebSecurityConfigurerAdapter`
3. Apache HttpClient 4.x customization in `RestClientConfig`
4. Deprecated configuration properties (e.g., `server.max.http.header.size`)

These are annotated with `MIGRATION NOTE` comments in the code to make them easy to find during the guides.

---

## 8. Known Issues & Security Warnings

- ⚠️ `TrustAllStrategy` in `RestClientConfig` is **insecure** and for demonstration only
- ⚠️ H2 console should be **disabled in production**
- ⚠️ JWT secret must be **externalized** and secured in real deployments
- ⚠️ The baseline may contain known CVEs by design, to showcase security improvements through modernization
