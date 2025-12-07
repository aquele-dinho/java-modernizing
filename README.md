# Java Modernization Demo - Phase 0 (Baseline)

A demonstration application showcasing the migration path from Java 11 + Spring Boot 2.4 to Java 21 + Spring Boot 3.x using OpenRewrite automation.

## 📋 Project Overview

This project serves as a **baseline implementation** for demonstrating Java and Spring Boot modernization. It intentionally uses legacy patterns and deprecated APIs that will be migrated in subsequent phases.

**Current State (Phase 0):**
- ☕ Java 11 (LTS)
- 🍃 Spring Boot 2.4.13
- 🔐 Spring Security 5.x (method chaining patterns)
- 📦 javax.* namespace (not jakarta.*)
- 🌐 Apache HttpClient 4.x

**Target States:**
- **Phase 1:** Java 17 + Spring Boot 3.0 (Jakarta EE, Security 6.0 Lambda DSL)
- **Phase 2:** Java 21 + Spring Boot 3.x (Virtual Threads, modern language features)

## 🏗️ Architecture

**Task Management REST API** with the following components:
- **Domain:** User and Task entities with JPA relationships
- **Security:** JWT-based stateless authentication with role-based access control
- **API:** RESTful endpoints for authentication, task CRUD, and user management
- **Database:** H2 in-memory database with sample data

## 📝 Prerequisites

- **Java 11** - LTS version required for baseline
- **Maven 3.8+** - Build tool
- **Git** - Version control

### Setting Up Java 11

On macOS with multiple Java versions installed:

```bash
# Set JAVA_HOME to Java 11
export JAVA_HOME=$(/usr/libexec/java_home -v 11)

# Verify Java version
java -version
# Should output: openjdk version "11.x.x"
```

Add this to your `~/.zshrc` or `~/.bashrc` to make it persistent for this project.

## 🚀 Quick Start

### 1. Clone and Build

```bash
# Navigate to project directory
cd java-modernizing

# Set Java 11
export JAVA_HOME=$(/usr/libexec/java_home -v 11)

# Clean and build
mvn clean install
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 3. Access H2 Console (Development)

- **URL:** http://localhost:8080/h2-console
- **JDBC URL:** `jdbc:h2:mem:taskdb`
- **Username:** `sa`
- **Password:** _(leave empty)_

## 🔑 Default Credentials

The application comes with pre-configured test users:

| Username | Password | Roles       |
|----------|----------|-------------|
| `admin`  | `password` | USER, ADMIN |
| `user`   | `password` | USER        |

## 📡 API Endpoints

### Authentication Endpoints (Public)

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

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "username": "admin",
  "email": "admin@demo.com"
}
```

### Task Endpoints (Authenticated)

Save the JWT token from login response and use it in subsequent requests:

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

### User Endpoints

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

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run with coverage
mvn clean test jacoco:report
```

## 🔄 Migration Path

### Phase 1: Spring Boot 3.0 + Java 17

Use OpenRewrite to automate the migration:

```bash
# Preview changes (dry-run)
mvn rewrite:dryRun

# Apply migration
mvn rewrite:run

# Update Java version
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

**Key Changes:**
- `javax.*` → `jakarta.*` namespace migration
- Spring Security method chaining → Lambda DSL
- Apache HttpClient 4.x → 5.x
- WebSecurityConfigurerAdapter → SecurityFilterChain

### Phase 2: Java 21 Optimization

```bash
# Set Java 21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Apply Java 21 migration recipe
mvn rewrite:run -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava21
```

## 📦 Project Structure

```
java-modernizing/
├── src/main/java/dev/tiodati/demo/modernization/
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
├── src/test/java/       # Unit and integration tests
├── docs/
│   └── RESEARCH.md      # Comprehensive migration research
├── pom.xml              # Maven configuration with OpenRewrite
├── WARP.md              # Technical specification
└── README.md            # This file
```

## 🔍 Legacy Patterns (Intentional)

This baseline intentionally demonstrates patterns that will be migrated:

1. **javax.* imports** - Will migrate to jakarta.*
2. **Spring Security method chaining** - `.antMatchers()`, `.authorizeRequests()`
3. **WebSecurityConfigurerAdapter** - Deprecated in Spring Security 5.7
4. **Apache HttpClient 4.x** - Package structure changes in 5.x
5. **Deprecated property names** - `server.max.http.header.size`

These patterns are **documented as migration targets** in the codebase.

## 📚 Additional Resources

- **Migration Research:** See `docs/RESEARCH.md` for comprehensive analysis
- **Technical Spec:** See `WARP.md` for detailed specifications
- **OpenRewrite Docs:** https://docs.openrewrite.org

## 🐛 Known Issues

- ⚠️ TrustAllStrategy in RestClientConfig is **insecure** and for demonstration only
- ⚠️ H2 console should be **disabled in production**
- ⚠️ JWT secret should be **externalized** and secured in production

## 📝 License

This is a demonstration project for educational purposes.

## 🤝 Contributing

This project follows the Specification-Driven Development (SDD) methodology. See `WARP.md` for implementation guidelines.

---

**Version:** 1.0.0-SNAPSHOT (Phase 0 - Baseline)  
**Java:** 11  
**Spring Boot:** 2.4.13  
**Status:** ✅ Ready for Migration
