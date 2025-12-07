# Java Modernization Demo - Project Specification

## 1. Project Overview

### 1.1. Purpose
This project serves as a **demonstration and reference implementation** for the strategic modernization of Java applications from Java 11 + Spring Boot 2.4 to modern Java LTS versions (17, 21) and Spring Boot 3.x, following the comprehensive migration methodology documented in `docs/RESEARCH.md`.

### 1.2. Objectives
- Create a realistic Spring Boot 2.4 application running on Java 11 that exhibits common patterns and dependencies found in legacy enterprise applications
- Demonstrate the phased migration approach using OpenRewrite automation
- Illustrate breaking changes at each migration phase (Java 11→17, Spring Boot 2.x→3.x, Java 17→21)
- **Integrate OWASP dependency vulnerability checking (SCA) throughout the migration lifecycle**
- Provide before/after code examples for critical refactoring areas
- Document manual intervention points that cannot be automated
- Serve as a training resource and blueprint for real-world modernization projects

### 1.3. Strategic Value
This demonstration application will enable development teams to:
- Understand the scope and complexity of Java/Spring Boot modernization
- Evaluate OpenRewrite's capabilities and limitations
- **Implement DevSecOps practices with automated dependency vulnerability scanning**
- Plan resource allocation for production migrations
- Establish testing and validation strategies
- Create phased rollout plans with measurable milestones
- **Generate compliance artifacts (SBOM, VDR, VEX) for supply chain security**

## 2. Technical Context

### 2.1. Current State (Baseline)
- **Java Version:** Java 11 (LTS)
- **Spring Boot Version:** 2.4.x
- **Build Tool:** Maven 3.8+
- **Dependencies:** Spring Web, Spring Data JPA, Spring Security, H2 Database
- **Architecture:** Monolithic REST API with layered architecture

### 2.2. Target States
The project will demonstrate migration through three discrete phases:

**Phase 1: Java 11 + Spring Boot 2.4** (Baseline)
- Establish foundation with common patterns and anti-patterns

**Phase 2: Java 17 + Spring Boot 3.0** (Primary Migration)
- Address Jakarta EE namespace migration (javax.* → jakarta.*)
- Refactor Spring Security 5.x → 6.0 (Lambda DSL)
- Update HTTP Client stack (HttpClient 4.x → 5.x)
- Remove Java EE module dependencies

**Phase 3: Java 21 + Spring Boot 3.x** (Optimization)
- Enable Virtual Threads
- Adopt Java 21 language features (Sequenced Collections, Pattern Matching enhancements)
- Demonstrate performance improvements

## 3. Application Requirements

### 3.1. Domain Model
The application will implement a simplified **Task Management System** with the following entities:

#### 3.1.1. User Entity
- Properties: `id`, `username`, `email`, `password`, `roles`, `createdAt`
- Annotations: `@Entity`, JPA mappings, validation constraints
- Security: Password encoding with BCrypt
- Relationships: One-to-many with Tasks

#### 3.1.2. Task Entity
- Properties: `id`, `title`, `description`, `status`, `priority`, `assignedTo`, `createdAt`, `updatedAt`
- Enums: `TaskStatus` (OPEN, IN_PROGRESS, COMPLETED), `Priority` (LOW, MEDIUM, HIGH)
- Relationships: Many-to-one with User

### 3.2. API Endpoints
The application will expose the following REST endpoints:

#### 3.2.1. Authentication Endpoints
```
POST   /api/auth/register       - Register new user
POST   /api/auth/login          - Authenticate and receive JWT token
```

#### 3.2.2. Task Management Endpoints
```
GET    /api/tasks               - List all tasks (paginated)
GET    /api/tasks/{id}          - Get task by ID
POST   /api/tasks               - Create new task
PUT    /api/tasks/{id}          - Update existing task
DELETE /api/tasks/{id}          - Delete task
GET    /api/tasks/user/{userId} - Get tasks by assigned user
```

#### 3.2.3. User Management Endpoints
```
GET    /api/users               - List all users (admin only)
GET    /api/users/{id}          - Get user by ID
PUT    /api/users/{id}          - Update user profile
```

### 3.3. Security Configuration
- **Authentication:** JWT-based stateless authentication
- **Authorization:** Role-based access control (ADMIN, USER)
- **Security Rules:**
  - Public access: `/api/auth/**`, `/h2-console/**` (dev only)
  - Authenticated access: All other endpoints
  - Admin-only access: User management endpoints

### 3.4. Data Access Layer
- **ORM:** Spring Data JPA with Hibernate
- **Database:** H2 in-memory database (for demo purposes)
- **Repository Pattern:** Spring Data JPA repositories with custom query methods
- **Transaction Management:** Declarative with `@Transactional`

### 3.5. HTTP Client Configuration
The application will include REST client examples demonstrating:
- Custom `RestTemplate` configuration with `HttpComponentsClientHttpRequestFactory`
- Connection pooling and timeout configuration
- SSL/TLS trust store customization (to demonstrate breaking changes in migration)

### 3.6. Configuration Properties
Custom properties that will require migration:
```properties
# Server configuration
server.port=8080
server.max.http.header.size=16KB

# Security configuration
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration=86400000

# Database configuration
spring.datasource.url=jdbc:h2:mem:taskdb
spring.h2.console.enabled=true
```

## 4. Code Patterns to Demonstrate

### 4.1. Breaking Changes in Java 17
The baseline (Java 11) code will intentionally use:
- JAXB dependencies (javax.xml.bind) without explicit Jakarta dependencies
- Reflection access to internal APIs (to demonstrate restrictions)
- Deprecated APIs scheduled for removal

### 4.2. Breaking Changes in Spring Boot 3.x
The Spring Boot 2.4 code will include:
- Spring Security configuration using method chaining (`.antMatchers()`, `.authorizeRequests()`)
- `RestTemplate` with Apache HttpClient 4.x customization
- Configuration properties using deprecated naming conventions
- Servlet-based web configuration with `javax.servlet.*` imports

### 4.3. Manual Intervention Points
Document areas requiring human validation:
- Complex Spring Security filter chains with multiple matchers
- Custom HTTP client SSL configurations
- Business logic that depends on framework behavior changes
- Test assertions that validate security defaults

## 5. Project Structure

```
java-modernizing/
├── docs/
│   ├── RESEARCH.md           (existing comprehensive research)
│   ├── OWASP.md              (dependency vulnerability checking strategy)
│   ├── MIGRATION_PHASE1.md   (Java 11→17 + SB 2.4→3.0)
│   ├── MIGRATION_PHASE2.md   (Java 17→21 + optimizations)
│   └── LESSONS_LEARNED.md    (post-migration insights)
├── src/main/java/com/demo/modernization/
│   ├── ModernizationDemoApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── RestClientConfig.java
│   │   └── JpaConfig.java
│   ├── domain/
│   │   ├── User.java
│   │   └── Task.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── TaskRepository.java
│   ├── service/
│   │   ├── AuthenticationService.java
│   │   ├── UserService.java
│   │   └── TaskService.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── TaskController.java
│   │   └── UserController.java
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── TaskDto.java
│   │   └── UserDto.java
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java
│   │   └── JwtTokenProvider.java
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       └── ResourceNotFoundException.java
├── src/main/resources/
│   ├── application.properties
│   ├── application-dev.properties
│   └── data.sql (sample data)
├── src/test/java/com/demo/modernization/
│   ├── integration/
│   │   ├── TaskControllerIntegrationTest.java
│   │   └── SecurityIntegrationTest.java
│   └── unit/
│       ├── TaskServiceTest.java
│       └── UserServiceTest.java
├── pom.xml
├── dependency-suppression.xml (OWASP false positive suppression)
├── WARP.md (this file)
└── README.md
```

## 6. Security-First Migration Strategy (DevSecOps)

This project follows the comprehensive OWASP-based Software Composition Analysis (SCA) methodology documented in `docs/OWASP.md`. Security verification is integrated into every phase of the migration lifecycle.

### 6.1. Phase 0: Baseline Stabilization & Security Assessment
- Ensure Spring Boot 2.4 application is fully functional
- Write comprehensive test suite (unit + integration)
- Document all deprecation warnings
- Establish performance baseline metrics

**Security Actions (Pre-Migration):**
1. **Baseline Vulnerability Scan:** Run OWASP Dependency-Check to establish security baseline
   - Quantify inherited security debt (critical/high CVEs)
   - Generate initial SBOM using OWASP Dep-Scan
2. **NVD API Configuration:** Obtain and configure NVD API Key for efficient scanning
   - Set up H2 database cache to prevent rate limiting in CI/CD
3. **False Positive Management:** Create `dependency-suppression.xml` for known false positives
   - Document and version suppress file in repository
   - Review and categorize each suppression with rationale
4. **Security Target Definition:** Migration must result in CVE count ≤ baseline

### 6.2. Phase 1: Primary Migration (Java 17 + Spring Boot 3.0)
**OpenRewrite Recipe:** `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0`

**Automated Changes:**
- Update Maven `java.version` to 17
- Update Spring Boot parent to 3.0.x
- Migrate all `javax.*` imports to `jakarta.*`
- Add explicit Jakarta dependencies (JAXB, Validation)
- Transform Spring Security to Lambda DSL
- Update configuration property names

**Automated Security Remediation:**
- **OpenRewrite DependencyVulnerabilityCheck:** Apply automated patch upgrades for known CVEs
  - Use `maximumUpgradeDelta=PATCH` for safe, non-breaking updates
  - Flag minor/major version upgrades for manual review
- **Taint Analysis:** Run `FindXssVulnerability` recipe to detect XSS risks introduced by refactoring

**Manual Tasks:**
1. Review and test SecurityConfig Lambda DSL transformations
2. Refactor RestTemplate SSL/TLS configuration for HttpClient 5.x
3. Validate connection pooling behavior
4. Update test assertions for new Spring Security defaults
5. Address any custom filter chain ordering issues
6. **Gated Check (Critical):** Run Dependency-Check after Spring Boot upgrade
   - Fail build if critical/high CVEs introduced by transitive dependencies
   - Special focus on javax→jakarta namespace migration risks
7. Run full regression test suite

### 6.3. Phase 2: Optimization (Java 21)
**OpenRewrite Recipe:** `org.openrewrite.java.migrate.UpgradeToJava21`

**Automated Changes:**
- Update Maven `java.version` to 21
- Migrate to Sequenced Collections where applicable
- Apply pattern matching enhancements

**Manual Tasks:**
1. Enable Virtual Threads in Tomcat configuration
2. Benchmark application throughput and latency
3. Adopt Java 21 language features in appropriate locations
4. **Final Security Validation:** Execute zero-tolerance vulnerability scan
   - Run OWASP Dep-Scan with reachability analysis to prioritize exploitable CVEs
   - Audit and purge obsolete suppression entries from `dependency-suppression.xml`
5. Document performance improvements

### 6.4. Validation Requirements
After each phase:
- All tests must pass (zero regressions)
- Application must start successfully
- All API endpoints must respond correctly
- Security rules must be enforced identically
- **Security validation:** No new critical/high CVEs introduced (compared to baseline)
- Performance metrics must meet or exceed baseline

### 6.5. Post-Migration: Compliance Artifacts
Generate and deliver the following security artifacts:
1. **SBOM (Software Bill-of-Materials):** Complete inventory of all dependencies and versions
2. **VDR (Vulnerability Disclosure Report):** Comprehensive vulnerability assessment
3. **CSAF 2.0 VEX (Vulnerability Exploitability eXchange):** Document non-exploitable CVEs with reachability analysis
4. **Updated Suppression File:** Clean, audited list of accepted risks with rationale

## 7. Build Configuration

### 7.1. Maven Plugins Required
- `maven-compiler-plugin` (version management per phase)
- `spring-boot-maven-plugin`
- `maven-surefire-plugin` (test execution)
- `rewrite-maven-plugin` (OpenRewrite integration)
- **`dependency-check-maven` (OWASP Dependency-Check for SCA)**

### 7.2. OWASP Dependency-Check Configuration
```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>11.0.0+</version>
    <configuration>
        <!-- NVD API Key for efficient scanning (highly recommended) -->
        <nvdApiKey>${env.NVD_API_KEY}</nvdApiKey>
        <!-- Fail build on CVSS score >= 7 (High/Critical) -->
        <failBuildOnCVSS>7</failBuildOnCVSS>
        <!-- Include transitive dependencies (critical for detecting inherited risks) -->
        <skipTestScope>false</skipTestScope>
        <!-- False positive suppression file -->
        <suppressionFile>dependency-suppression.xml</suppressionFile>
        <!-- Report formats -->
        <formats>
            <format>HTML</format>
            <format>JSON</format>
        </formats>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 7.3. OpenRewrite Configuration
```xml
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>5.x.x</version>
    <configuration>
        <activeRecipes>
            <!-- Phase 1: Migration -->
            <recipe>org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0</recipe>
            <!-- Security: Automated vulnerability patching -->
            <recipe>org.openrewrite.java.dependencies.DependencyVulnerabilityCheck</recipe>
            <!-- Security: XSS detection for refactored code -->
            <recipe>org.openrewrite.analysis.java.security.FindXssVulnerability</recipe>
        </activeRecipes>
        <recipeConfig>
            <org.openrewrite.java.dependencies.DependencyVulnerabilityCheck>
                <!-- Safe default: only patch upgrades, flag minor/major for review -->
                <maximumUpgradeDelta>PATCH</maximumUpgradeDelta>
            </org.openrewrite.java.dependencies.DependencyVulnerabilityCheck>
        </recipeConfig>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.openrewrite.recipe</groupId>
            <artifactId>rewrite-spring</artifactId>
            <version>5.x.x</version>
        </dependency>
        <dependency>
            <groupId>org.openrewrite.recipe</groupId>
            <artifactId>rewrite-java-dependencies</artifactId>
            <version>1.x.x</version>
        </dependency>
    </dependencies>
</plugin>
```

### 7.4. Testing Strategy
- **Unit Tests:** Mockito-based service layer tests
- **Integration Tests:** `@SpringBootTest` with TestRestTemplate
- **Security Tests:** `@WithMockUser` for authorization testing
- **Dependency Security Tests:** OWASP Dependency-Check integration in CI/CD pipeline
- **Test Coverage Target:** Minimum 80% line coverage

## 8. Documentation Requirements

### 8.1. README.md
- Project overview and purpose
- Quick start guide for each phase
- How to run OpenRewrite dry-run and apply migrations
- API documentation (endpoints, request/response examples)
- Known issues and troubleshooting

### 8.2. Migration Phase Documents
Each migration phase will have detailed documentation:
- Prerequisites and dependency versions
- Step-by-step OpenRewrite execution
- Automated changes summary
- **Security scan results:** Baseline vs. post-migration CVE comparison
- Manual intervention checklist
- Validation test results
- Before/after code comparisons for critical changes

### 8.3. Code Comments
- Inline comments highlighting migration-relevant patterns
- JavaDoc for public APIs
- Migration notes using `@deprecated` tags where applicable

## 9. Development Guidelines

### 9.1. Code Style
- Follow standard Java conventions
- Use Spring Boot best practices
- Maintain consistent formatting (Google Java Style Guide)
- Organize imports consistently

### 9.2. Git Workflow
- Branch per migration phase: `feature/phase-1-sb3-java17`, `feature/phase-2-java21`
- Commit messages: Use conventional commits format
  - `feat:` for new features
  - `refactor:` for migration changes
  - `docs:` for documentation
  - `test:` for test additions/changes
- Tag releases: `v1.0.0-java11-sb2.4`, `v2.0.0-java17-sb3.0`, `v3.0.0-java21-sb3.x`

### 9.3. No Direct Commits to Main
- All changes through pull requests
- Self-review before merging
- Ensure clean build and passing tests

## 10. Success Criteria

The demonstration project will be considered successful when:

✅ **Functionality:** All three phases build, run, and pass tests
✅ **Security:** Zero critical CVEs introduced; vulnerability count ≤ baseline
✅ **Compliance:** SBOM, VDR, and VEX artifacts generated and validated
✅ **Documentation:** Comprehensive migration guides are complete
✅ **Automation:** OpenRewrite recipes successfully handle bulk changes
✅ **DevSecOps:** OWASP SCA integrated into CI/CD as gated checks
✅ **Clarity:** Manual intervention points are clearly documented
✅ **Realism:** Code patterns reflect real-world enterprise applications
✅ **Education:** Project can be used as training material

## 11. Out of Scope

This demonstration will NOT include:
- Production-grade security (e.g., OAuth2, external identity providers)
- Distributed tracing or observability frameworks
- Container/Kubernetes deployment configurations
- CI/CD pipeline setup
- Multi-module Maven project structure
- Database migrations with Flyway/Liquibase
- Performance optimization beyond Virtual Threads demonstration

## 12. Dependencies Summary

### Phase 1 (Java 11 + Spring Boot 2.4)
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.4.13</version>
</parent>
<properties>
    <java.version>11</java.version>
</properties>
<dependencies>
    <!-- Web & REST -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Data Access -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt</artifactId>
        <version>0.9.1</version>
    </dependency>
    
    <!-- HTTP Client (4.x) -->
    <dependency>
        <groupId>org.apache.httpcomponents</groupId>
        <artifactId>httpclient</artifactId>
        <version>4.5.13</version>
    </dependency>
    
    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Phase 2 (Java 17 + Spring Boot 3.0)
Key changes:
- Parent version: `3.0.x`
- `java.version`: `17`
- All `javax.*` dependencies become `jakarta.*`
- Apache HttpClient: `5.x`
- JJWT: `0.12.x` (for Jakarta compatibility)

### Phase 3 (Java 21 + Spring Boot 3.x)
Key changes:
- `java.version`: `21`
- Spring Boot: `3.2.x` or latest stable
- Enable Virtual Threads configuration

## 13. Risk Mitigation

### 13.1. Technical Risks
- **Risk:** OpenRewrite may not handle all custom code patterns
  - **Mitigation:** Comprehensive manual review checklist, pair programming for validation
  
- **Risk:** Breaking behavioral changes in Spring Security may affect authorization logic
  - **Mitigation:** Extensive integration tests, security audit post-migration
  
- **Risk:** Performance degradation in new runtime
  - **Mitigation:** Establish baseline metrics, benchmark after each phase

- **Risk:** NVD API rate limiting causing CI/CD build failures
  - **Mitigation:** Configure NVD API Key, implement H2 cache sharing, monitor API usage

- **Risk:** Transitive dependencies introducing new CVEs during framework upgrades
  - **Mitigation:** Gated checks after each major upgrade, automated patch application with OpenRewrite

### 13.2. Scope Risks
- **Risk:** Feature creep beyond demonstration scope
  - **Mitigation:** Strict adherence to "out of scope" section, timeboxed implementation

## 14. Timeline Estimate

- **Phase 0 (Baseline):** 2-3 days (application + tests + security baseline)
- **Phase 1 (SB 3.0 + Java 17):** 2-3 days (migration + gated security checks + validation + docs)
- **Phase 2 (Java 21):** 1 day (migration + final security audit + benchmarking)
- **Compliance Artifacts:** 0.5 day (SBOM/VDR/VEX generation)
- **Documentation Polish:** 1 day
- **Total:** 6.5-8.5 development days

## 15. Maintenance Notes

### 15.1. Version Management
- Keep OpenRewrite plugin and recipes up to date
- Use Spring Boot BOM for consistent dependency versions
- Document all manual version overrides with rationale

### 15.2. Future Enhancements
Potential additions for extended learning:
- GraalVM native image compilation
- Spring Boot 3.x Observability features
- Project Loom structured concurrency patterns
- Migration to Spring Boot 4.x when available
- **Continuous vulnerability scanning in production (Shift Right)**
- **Container image scanning integration (OS-level CVEs)**
- **Automated dependency upgrade scheduling (monthly/quarterly)**

---

**Document Version:** 2.0  
**Last Updated:** 2025-12-07  
**Author:** System Architect  
**Status:** Enhanced with OWASP SCA Integration  
**See Also:** `docs/OWASP.md` for detailed security methodology
