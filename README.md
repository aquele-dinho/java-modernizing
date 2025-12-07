# Java Modernization Demo - Phase 0 (Baseline)

A demonstration application showcasing the migration path from Java 11 + Spring Boot 2.4 to Java 21 + Spring Boot 3.x using OpenRewrite automation with integrated OWASP dependency vulnerability checking (SCA).

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
- **NVD API Key** (Optional but recommended) - For efficient OWASP Dependency-Check scanning

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

### Setting Up NVD API Key (Recommended)

For efficient vulnerability scanning with OWASP Dependency-Check:

1. **Obtain API Key:** Visit https://nvd.nist.gov/developers/request-an-api-key
2. **Set Environment Variable:**
   ```bash
   export NVD_API_KEY="your-api-key-here"
   ```
3. **Make it persistent:** Add to your `~/.zshrc` or `~/.bashrc`

Without an API key, vulnerability scans will be significantly slower.

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

### 3. Access Swagger UI (API Documentation)

- **URL:** http://localhost:8080/swagger-ui.html
- Interactive API documentation and testing interface
- Click **Authorize** button and enter JWT token from login

### 4. Access H2 Console (Development)

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

### 🌐 Interactive Documentation

The easiest way to explore and test the API is through **Swagger UI**:

👉 **http://localhost:8080/swagger-ui.html**

**Steps to use Swagger UI:**
1. Start the application
2. Open Swagger UI in your browser
3. Use the `/api/auth/login` endpoint to get a JWT token
4. Click the **Authorize** button at the top
5. Enter `Bearer YOUR_TOKEN_HERE` and click **Authorize**
6. Now you can test all authenticated endpoints!

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

## 🔒 Security Scanning (OWASP Dependency-Check)

This project integrates OWASP Dependency-Check for Software Composition Analysis (SCA) to detect vulnerable dependencies.

### Run Vulnerability Scan

```bash
# Run dependency vulnerability check
mvn dependency-check:check

# View the report
open target/dependency-check-report.html
```

### Baseline Security Assessment

Before starting migration, establish a security baseline:

```bash
# 1. Run initial vulnerability scan
mvn clean dependency-check:check

# 2. Review the HTML report
open target/dependency-check-report.html

# 3. Document baseline CVE count
# Note: Critical and High severity CVEs should be tracked

# 4. Create suppression file for false positives
# Edit dependency-suppression.xml to suppress known false positives
```

### Security Validation During Migration

After each migration phase, run the security check to ensure no new critical vulnerabilities were introduced:

```bash
# After Phase 1 (Spring Boot 3.0 + Java 17)
mvn dependency-check:check

# Build will FAIL if CVSS >= 7.0 (High/Critical)
# This is a GATED CHECK to prevent vulnerable code from being deployed
```

### Generate Compliance Artifacts

```bash
# Generate SBOM (Software Bill of Materials)
mvn cyclonedx:makeAggregateBom

# Generate comprehensive vulnerability report (JSON format)
mvn dependency-check:check -Dformat=JSON,HTML
```

**Note:** The `dependency-suppression.xml` file contains documented suppressions for known false positives. Review and update this file after each migration phase.

## 🔄 Migration Path with Security Validation

### Phase 1: Spring Boot 3.0 + Java 17

Use OpenRewrite to automate the migration with integrated security checks:

```bash
# 1. Preview changes (dry-run)
mvn rewrite:dryRun

# 2. Apply migration with security remediation
mvn rewrite:run

# 3. Update Java version
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# 4. CRITICAL: Run security scan after upgrade (Gated Check)
mvn clean install dependency-check:check
# Build will FAIL if critical CVEs are introduced

# 5. Review security report
open target/dependency-check-report.html
```

**Key Changes:**
- `javax.*` → `jakarta.*` namespace migration
- Spring Security method chaining → Lambda DSL
- Apache HttpClient 4.x → 5.x
- WebSecurityConfigurerAdapter → SecurityFilterChain
- **Automated CVE patching** via OpenRewrite `DependencyVulnerabilityCheck`
- **XSS vulnerability detection** via OpenRewrite `FindXssVulnerability`

### Phase 2: Java 21 Optimization

```bash
# 1. Set Java 21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# 2. Apply Java 21 migration recipe
mvn rewrite:run -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava21

# 3. Final security validation (Zero-tolerance scan)
mvn clean install dependency-check:check

# 4. Audit and clean suppression file
# Review dependency-suppression.xml and remove obsolete entries

# 5. Generate final compliance artifacts
mvn cyclonedx:makeAggregateBom
mvn dependency-check:check -Dformat=JSON,HTML
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
│   ├── RESEARCH.md      # Comprehensive migration research
│   └── OWASP.md         # OWASP SCA methodology and integration
├── target/
│   └── dependency-check-report.html  # Vulnerability scan report
├── pom.xml              # Maven with OpenRewrite + OWASP plugins
├── dependency-suppression.xml        # False positive suppressions
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
- **OWASP SCA Strategy:** See `docs/OWASP.md` for security methodology
- **Technical Spec:** See `WARP.md` for detailed specifications
- **OpenRewrite Docs:** https://docs.openrewrite.org
- **OWASP Dependency-Check:** https://owasp.org/www-project-dependency-check/
- **OWASP Dep-Scan:** https://owasp.org/www-project-dep-scan/

## 🐛 Known Issues & Security Warnings

- ⚠️ TrustAllStrategy in RestClientConfig is **insecure** and for demonstration only
- ⚠️ H2 console should be **disabled in production**
- ⚠️ JWT secret should be **externalized** and secured in production
- ⚠️ **Baseline may contain known CVEs** - This is intentional to demonstrate security improvement through modernization
- ⚠️ Without NVD API Key, dependency scans will be **extremely slow** (can take 30+ minutes)

## 📝 License

This is a demonstration project for educational purposes.

## 🤝 Contributing

This project follows the Specification-Driven Development (SDD) methodology. See `WARP.md` for implementation guidelines.

## 🎯 Success Criteria

The migration is considered successful when:

✅ All tests pass (zero regressions)  
✅ **Security:** Zero critical CVEs introduced (vulnerability count ≤ baseline)  
✅ **Compliance:** SBOM, VDR, and VEX artifacts generated  
✅ Application functionality maintained  
✅ Performance metrics meet or exceed baseline  

---

**Version:** 2.0.0-SNAPSHOT (Phase 0 - Baseline with OWASP Integration)  
**Java:** 11  
**Spring Boot:** 2.4.13  
**Security:** OWASP Dependency-Check + OpenRewrite Security Recipes  
**Status:** ✅ Ready for Security-First Migration
