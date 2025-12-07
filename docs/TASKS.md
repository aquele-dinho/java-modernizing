# Java Modernization Demo - Phase 0 Implementation Tasks

This task list breaks down the implementation plan into minimal, independently implementable units of work. Tasks are organized by implementation phase and can be completed sequentially or in parallel where dependencies allow.

## Legend
- ✅ Completed
- ⬜ Not started
- 🔄 In progress

---

## Phase 1: Project Initialization

### 1.1 Git Repository Setup
- [x] Create `.gitignore` for Java/Maven/IDE files
- [x] Initialize git repository
- [x] Create initial commit
- [x] Create main branch

### 1.2 Maven Project Structure
- [x] Create `pom.xml` with Spring Boot 2.4.13 parent
- [x] Configure Java 11 as source/target version (maven.compiler.source/target = 11)
- [x] Add Spring Boot Starter Web dependency
- [x] Add Spring Boot Starter Data JPA dependency
- [x] Add H2 Database dependency (runtime scope)
- [x] Add Spring Boot Starter Security dependency
- [x] Add JJWT 0.9.1 dependency (intentional legacy version)
- [x] Add Apache HttpClient 4.5.13 dependency (intentional legacy version)
- [x] Add Spring Boot Starter Validation dependency
- [x] Add Springdoc OpenAPI UI dependency
- [x] Add Spring Boot Starter Test dependency (test scope)
- [x] Add Spring Security Test dependency (test scope)
- [x] Configure Maven Compiler Plugin for Java 11
- [x] Configure Spring Boot Maven Plugin
- [x] Configure Maven Surefire Plugin for test execution
- [x] Add OpenRewrite Maven Plugin (version 5.42.0+)
- [x] Add rewrite-spring dependency to OpenRewrite plugin
- [x] Add rewrite-migrate-java dependency to OpenRewrite plugin
- [x] Configure OpenRewrite recipe: UpgradeSpringBoot_3_0
- [x] **Add OWASP Dependency-Check Maven Plugin (version 11.0.0+)**
- [x] **Configure NVD API Key: ${env.NVD_API_KEY}**
- [x] **Set failBuildOnCVSS=7 (High/Critical threshold)**
- [x] **Configure suppression file: dependency-suppression.xml**
- [x] **Enable HTML and JSON report formats**
- [x] **Add OpenRewrite security recipe: DependencyVulnerabilityCheck**
- [x] **Add OpenRewrite security recipe: FindXssVulnerability**
- [x] **Configure maximumUpgradeDelta=PATCH for safe patching**
- [x] **Add rewrite-java-dependencies artifact to OpenRewrite plugin**

### 1.3 Directory Structure
- [x] Create src/main/java/dev/tiodati/demo/modernization/ (base package)
- [x] Create config/ subpackage
- [x] Create domain/ subpackage
- [x] Create repository/ subpackage
- [x] Create service/ subpackage
- [x] Create controller/ subpackage
- [x] Create dto/ subpackage
- [x] Create security/ subpackage
- [x] Create exception/ subpackage
- [x] Create src/main/resources/ directory
- [x] Create src/test/java/dev/tiodati/demo/modernization/ directory
- [ ] Create src/test/java/dev/tiodati/demo/modernization/integration/ directory
- [ ] Create src/test/java/dev/tiodati/demo/modernization/unit/ directory
- [x] **Create dependency-suppression.xml in project root**

---

## Phase 2: Core Domain Implementation

### 2.1 Enums and Value Objects
- [x] Create TaskStatus enum (OPEN, IN_PROGRESS, COMPLETED)
- [x] Create Priority enum (LOW, MEDIUM, HIGH)

### 2.2 Entity Classes
- [x] Create User.java entity with javax.persistence annotations
- [x] Add User fields: id, username, email, password, roles, createdAt
- [x] Add @Entity, @Table, @Id, @GeneratedValue annotations to User
- [x] Add validation constraints to User fields
- [x] Add OneToMany relationship from User to Task
- [x] Create Task.java entity with javax.persistence annotations
- [x] Add Task fields: id, title, description, status, priority, assignedTo, createdAt, updatedAt
- [x] Add @Entity, @Table, @Id, @GeneratedValue annotations to Task
- [x] Add validation constraints to Task fields
- [x] Add ManyToOne relationship from Task to User
- [x] Add audit timestamps (@CreatedDate, @LastModifiedDate)

### 2.3 Repository Layer
- [x] Create UserRepository extending JpaRepository<User, Long>
- [x] Add findByUsername() method to UserRepository
- [x] Add findByEmail() method to UserRepository
- [x] Create TaskRepository extending JpaRepository<Task, Long>
- [x] Add findByAssignedToId() method with Pageable to TaskRepository

---

## Phase 3: Security Infrastructure

### 3.1 JWT Components
- [x] Create JwtTokenProvider class
- [x] Implement generateToken() method in JwtTokenProvider
- [x] Implement validateToken() method in JwtTokenProvider
- [x] Implement getUsernameFromToken() method in JwtTokenProvider
- [x] Create JwtAuthenticationFilter extending OncePerRequestFilter
- [x] Implement doFilterInternal() to intercept and validate JWT tokens
- [x] Extract JWT from Authorization header
- [x] Validate token and set SecurityContext authentication

### 3.2 Security Configuration
- [x] Create SecurityConfig class with @Configuration and @EnableWebSecurity
- [x] Use method chaining pattern (.antMatchers(), .authorizeRequests()) - LEGACY PATTERN
- [x] Configure HttpSecurity to disable CSRF
- [x] Configure stateless session management
- [x] Permit public access to /api/auth/** endpoints
- [x] Permit public access to /h2-console/** (dev only)
- [x] Permit public access to /swagger-ui/**, /v3/api-docs/** (API docs)
- [x] Require authentication for all other endpoints
- [x] Add JWT filter before UsernamePasswordAuthenticationFilter
- [x] Create BCrypt password encoder bean
- [x] Configure AuthenticationManager bean
- [x] Enable H2 console for dev environment

### 3.3 User Details Service
- [x] Create CustomUserDetailsService implementing UserDetailsService
- [x] Implement loadUserByUsername() method
- [x] Map User entity to Spring Security UserDetails

---

## Phase 4: Business Logic Layer

### 4.1 DTOs
- [x] Create LoginRequest DTO with username and password fields
- [x] Create RegisterRequest DTO with username, email, password fields
- [x] Add validation annotations to RegisterRequest
- [x] Create JwtResponse DTO with token, type, username, email fields
- [x] Create TaskDto with all task fields
- [x] Add validation annotations to TaskDto
- [x] Create UserDto with safe user fields (no password)

### 4.2 Service Classes
- [x] Create AuthenticationService class
- [x] Implement register() method in AuthenticationService
- [x] Implement login() method in AuthenticationService
- [x] Validate credentials and return JWT token
- [x] Create UserService class
- [x] Implement CRUD operations in UserService
- [x] Implement findByUsername() in UserService
- [x] Implement role management in UserService
- [x] Add @Transactional annotations to UserService
- [x] Create TaskService class
- [x] Implement CRUD operations in TaskService
- [x] Implement findByUser() with pagination in TaskService
- [x] Add @Transactional annotations to TaskService

---

## Phase 5: REST API Layer

### 5.1 Exception Handling
- [x] Create ResourceNotFoundException custom exception
- [x] Create GlobalExceptionHandler with @ControllerAdvice
- [x] Add @ExceptionHandler for ResourceNotFoundException
- [x] Add @ExceptionHandler for validation errors
- [x] Return proper HTTP status codes (404, 400, etc.)
- [x] Return consistent error response format

### 5.2 Controllers
- [x] Create AuthController with @RestController and @RequestMapping("/api/auth")
- [x] Implement POST /api/auth/register endpoint
- [x] Implement POST /api/auth/login endpoint
- [x] Use javax.servlet.http.* imports (intentional for migration demo)
- [x] Create TaskController with @RestController and @RequestMapping("/api/tasks")
- [x] Implement GET /api/tasks endpoint (list all tasks)
- [x] Implement GET /api/tasks/{id} endpoint
- [x] Implement POST /api/tasks endpoint
- [x] Implement PUT /api/tasks/{id} endpoint
- [x] Implement DELETE /api/tasks/{id} endpoint (admin only)
- [x] Implement GET /api/tasks/user/{userId} endpoint
- [x] Add @PreAuthorize annotations for authorization
- [x] Create UserController with @RestController and @RequestMapping("/api/users")
- [x] Implement GET /api/users endpoint (admin only)
- [x] Implement GET /api/users/{id} endpoint
- [x] Implement PUT /api/users/{id} endpoint

---

## Phase 6: Configuration

### 6.1 Application Properties
- [x] Create application.properties file
- [x] Configure server.port=8080
- [x] Configure JWT secret and expiration settings
- [x] Configure H2 database: jdbc:h2:mem:taskdb
- [x] Configure spring.h2.console.enabled=true
- [x] Use deprecated property: server.max.http.header.size (intentional for migration demo)
- [x] Configure JPA settings (ddl-auto, show-sql)

### 6.2 HTTP Client Configuration
- [x] Create RestClientConfig class with @Configuration
- [x] Configure RestTemplate bean with HttpComponentsClientHttpRequestFactory
- [x] Use Apache HttpClient 4.x with connection pooling
- [x] Configure connection timeouts
- [x] Add SSL/TLS trust configuration with TrustAllStrategy (intentional insecure pattern)
- [x] Add migration comment explaining breaking changes in HttpClient 5.x

### 6.3 JPA Configuration and Sample Data
- [x] Create data.sql with sample users
- [x] Add admin user with BCrypt-encoded password
- [x] Add regular user with BCrypt-encoded password
- [x] Create sample tasks assigned to users

### 6.4 OpenAPI Configuration
- [x] Create OpenApiConfig class
- [x] Configure Swagger UI metadata
- [x] Configure JWT security scheme for Swagger

---

## Phase 7: Testing

### 7.1 Unit Tests
- [x] Create src/test/java/dev/tiodati/demo/modernization/unit/ directory
- [x] Create TaskServiceTest class
- [x] Test TaskService.createTask() with Mockito
- [x] Test TaskService.updateTask() with Mockito
- [x] Test TaskService.deleteTask() with Mockito
- [x] Test TaskService.findById() with Mockito
- [x] Test TaskService.findAll() with Mockito
- [x] Create UserServiceTest class
- [x] Test UserService.createUser() with Mockito
- [x] Test UserService.updateUser() with Mockito
- [x] Test UserService.findByUsername() with Mockito
- [x] Test UserService.findById() with Mockito
- [x] Verify minimum 80% code coverage for services

### 7.2 Integration Tests
- [x] Create src/test/java/dev/tiodati/demo/modernization/integration/ directory
- [x] Create TaskControllerIntegrationTest with @SpringBootTest
- [x] Test POST /api/auth/register endpoint
- [x] Test POST /api/auth/login endpoint
- [x] Test GET /api/tasks with valid JWT token
- [x] Test GET /api/tasks/{id} with valid JWT token
- [x] Test POST /api/tasks with valid JWT token
- [x] Test PUT /api/tasks/{id} with valid JWT token
- [x] Test DELETE /api/tasks/{id} with admin JWT token
- [x] Test DELETE /api/tasks/{id} rejects non-admin user
- [x] Create SecurityIntegrationTest with @SpringBootTest
- [x] Test public endpoints accessible without authentication
- [x] Test protected endpoints reject requests without JWT
- [x] Test admin-only endpoints reject regular users
- [x] Test invalid JWT tokens are rejected
- [x] Use TestRestTemplate for HTTP requests

---

## Phase 8: Documentation

### 8.1 README.md Updates
- [x] Add project overview and Phase 0 goals
- [x] Document prerequisites (Java 11, Maven 3.8+)
- [x] Add Java version setup instructions for macOS
- [x] Document build command: mvn clean install
- [x] Document run command: mvn spring-boot:run
- [x] Add default credentials table (admin/password, user/password)
- [x] Document API endpoints with curl examples
- [x] Add Swagger UI access instructions
- [x] Add H2 console access instructions
- [x] **Add OWASP Dependency-Check scanning instructions**
- [x] **Document NVD API Key setup process**
- [x] **Add security baseline assessment steps**
- [x] **Add security validation during migration section**
- [x] **Document compliance artifact generation**
- [x] Add migration to Phase 1 next steps

### 8.2 Code Documentation
- [x] Add JavaDoc to all public methods in service classes
- [x] Add JavaDoc to all public methods in controller classes
- [x] Add inline comments highlighting legacy patterns in SecurityConfig
- [x] Add inline comments explaining javax.* imports in entities
- [x] Add inline comments in RestClientConfig explaining HttpClient 4.x usage
- [x] Mark deprecated patterns with: // MIGRATION NOTE: This pattern will change in Spring Boot 3.x
- [x] Document intentional security vulnerabilities (TrustAllStrategy) with warnings

---

## Phase 9: Security Baseline Assessment

### 9.1 Establish Security Baseline
- [ ] **Run initial vulnerability scan: mvn dependency-check:check**
- [ ] **Review dependency-check-report.html in target/ directory**
- [ ] **Document baseline CVE count (critical, high, medium, low)**
- [ ] **Create docs/SECURITY_BASELINE.md with vulnerability metrics**
- [ ] **Create dependency-suppression.xml file in project root**
- [ ] **Review each CPE match in the HTML report**
- [ ] **Add suppression entries for known false positives**
- [ ] **Document rationale for each suppression**
- [ ] **Commit dependency-suppression.xml to version control**
- [ ] **Set security target: Future migrations CVE count ≤ baseline**

### 9.2 Obtain NVD API Key (Optional but Recommended)
- [ ] **Visit https://nvd.nist.gov/developers/request-an-api-key**
- [ ] **Request NVD API Key**
- [ ] **Export NVD_API_KEY environment variable**
- [ ] **Add export NVD_API_KEY="..." to ~/.zshrc**
- [ ] **Test API key with dependency-check scan**
- [ ] **Document scan performance improvement (with vs without key)**

---

## Phase 10: Validation

### 10.1 Build Verification
- [ ] Run: mvn clean compile (verify compilation succeeds)
- [ ] Run: mvn test (verify all tests pass)
- [ ] Run: mvn package (verify JAR creation succeeds)
- [ ] **Run: mvn dependency-check:check (verify security scan passes)**
- [ ] Verify target/modernization-demo-1.0.0-SNAPSHOT.jar exists
- [ ] **Verify target/dependency-check-report.html exists**

### 10.2 Runtime Verification
- [ ] Start application: mvn spring-boot:run
- [ ] Verify application starts on port 8080 without errors
- [ ] Test authentication: register new user via /api/auth/register
- [ ] Test authentication: login with admin credentials
- [ ] Verify JWT token received in login response
- [ ] Test task creation: POST /api/tasks with JWT token
- [ ] Test task retrieval: GET /api/tasks with JWT token
- [ ] Test task update: PUT /api/tasks/{id} with JWT token
- [ ] Test task deletion: DELETE /api/tasks/{id} with admin token
- [ ] Test authorization: verify DELETE rejects regular user
- [ ] Access H2 console at http://localhost:8080/h2-console
- [ ] Verify database tables and sample data
- [ ] Access Swagger UI at http://localhost:8080/swagger-ui.html
- [ ] Test all endpoints via Swagger UI

### 10.3 Deprecation Audit
- [ ] Run: mvn clean compile and capture all deprecation warnings
- [ ] Create docs/DEPRECATION_BASELINE.md
- [ ] Document each deprecation warning with file location
- [ ] Categorize warnings by migration impact (high/medium/low)
- [ ] Document which warnings will be resolved by OpenRewrite
- [ ] Document which warnings require manual intervention

### 10.4 Security Validation
- [ ] **Verify dependency-check-report.html contains complete vulnerability assessment**
- [ ] **Confirm all critical false positives are documented in suppression file**
- [ ] **Create docs/SECURITY_BASELINE.md with CVE metrics**
- [ ] **Document security baseline for Phase 1 comparison**
- [ ] **Verify NVD API Key is working (check scan duration)**
- [ ] **Test build failure with intentionally vulnerable dependency (validate CVSS threshold)**

---

## Phase 11: Final Deliverables

### 11.1 Documentation Artifacts
- [x] WARP.md (specification) - already exists
- [x] docs/RESEARCH.md (migration research) - already exists
- [x] docs/OWASP.md (security methodology) - already exists
- [x] README.md (updated with Phase 0 and security) - completed
- [ ] docs/TASKS.md (this file) - completed
- [ ] docs/SECURITY_BASELINE.md (CVE baseline metrics)
- [ ] docs/DEPRECATION_BASELINE.md (deprecation warnings)

### 11.2 Code Quality Checks
- [ ] Run: mvn clean verify (full build with tests)
- [ ] **Run: mvn dependency-check:check (security scan)**
- [ ] Verify no compiler errors
- [ ] Verify no test failures
- [ ] Verify test coverage ≥ 80%
- [ ] **Verify security scan passes or all CVEs are documented**
- [ ] Run: git status (verify no uncommitted changes)

### 11.3 Git Tagging and Release
- [ ] Create git commit with message: "feat: complete Phase 0 baseline with security integration"
- [ ] Create git tag: v1.0.0-phase0-baseline
- [ ] Push commit and tag to remote repository
- [ ] Create GitHub release with Phase 0 summary
- [ ] Include SECURITY_BASELINE.md in release notes

---

## Success Criteria Checklist

Verify all success criteria from the implementation plan:

- [ ] ✅ Application builds without errors
- [ ] ✅ All tests pass (minimum 80% coverage)
- [ ] ✅ Application starts and serves requests on port 8080
- [ ] ✅ Authentication flow works: register → login → receive JWT → access protected endpoints
- [ ] ✅ Authorization works: admin endpoints reject non-admin users
- [ ] ✅ All API endpoints respond with correct status codes and data
- [ ] ✅ H2 console accessible for database inspection
- [ ] ✅ **Security baseline established: OWASP Dependency-Check scan completed**
- [ ] ✅ **Baseline CVE count documented (critical/high/medium/low)**
- [ ] ✅ **Suppression file created for false positives with rationale**
- [ ] ✅ **NVD API Key configured (optional but recommended)**
- [ ] ✅ Deprecation warnings documented
- [ ] ✅ Code demonstrates legacy patterns intentionally (for migration demo value)
- [ ] ✅ README provides clear quick-start instructions with security scanning

---

## Estimated Time Breakdown

| Phase | Description | Estimated Time | Status |
|-------|-------------|----------------|--------|
| 1 | Project Initialization | 30 min (base) + 15 min (security) | ✅ Partially Complete |
| 2 | Core Domain Implementation | 1 hour | ✅ Complete |
| 3 | Security Infrastructure | 1.5 hours | ✅ Complete |
| 4 | Business Logic Layer | 2 hours | ✅ Complete |
| 5 | REST API Layer | 2 hours | ✅ Complete |
| 6 | Configuration | 1 hour | ✅ Complete |
| 7 | Testing | 2 hours | ⬜ Not Started |
| 8 | Documentation | 1 hour | ✅ Partially Complete |
| 9 | Security Baseline | 30 min (scan) + 20 min (suppression) + 10 min (API key) | ⬜ Not Started |
| 10 | Validation | 1 hour | ⬜ Not Started |
| 11 | Final Deliverables | 30 min | ⬜ Not Started |
| **Total** | | **~10.25 hours** | **~60% Complete** |

---

## Next Steps

**Priority 1 (Security Foundation):**
1. Add OWASP Dependency-Check Maven plugin to pom.xml
2. Configure security recipes in OpenRewrite plugin
3. Create dependency-suppression.xml file
4. Obtain NVD API Key
5. Run initial security baseline scan

**Priority 2 (Testing):**
1. Create unit test directory structure
2. Implement TaskServiceTest
3. Implement UserServiceTest
4. Create integration test directory structure
5. Implement TaskControllerIntegrationTest
6. Implement SecurityIntegrationTest

**Priority 3 (Documentation & Validation):**
1. Add JavaDoc to all public APIs
2. Document legacy patterns with migration notes
3. Run full validation suite
4. Document deprecation warnings
5. Document security baseline
6. Create final deliverables

---

**Document Version:** 1.0  
**Last Updated:** 2025-12-07  
**Status:** Phase 0 ~60% Complete - Security Integration Pending  
**Next Milestone:** Complete OWASP integration and establish security baseline
