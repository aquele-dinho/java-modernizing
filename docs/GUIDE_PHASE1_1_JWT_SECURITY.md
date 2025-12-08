# Phase 1.1 Appendix – JWT & Security Alignment (Post SB 3 Migration)

## 0. Purpose & Scope

After completing **Phase 1** (Java 17 + Spring Boot 3.0 structural migration), the code compiles and unit tests pass, but integration tests may still fail due to:

- Legacy **JJWT 0.9.x** incompatibilities on Java 17 / Jakarta.
- Changed **Spring Security 6** behavior (authorization and error handling).

This appendix (Phase 1.1) focuses on aligning **authentication and security behavior** with the new platform so that:

- JWT creation and parsing work correctly on Java 17.
- Integration tests around authentication, authorization, and public endpoints are made green again.

> **Prerequisite:** You have completed Phase 1 on a feature branch (for example, `feature/phase1-migration`) and are now working on a follow-up branch (for example, `feature/phase1.1-jwt-security`).

---

## Section 1 – Understand the Current JWT & Security Issues

### 1.1 JJWT 0.9.x vs Java 17

Symptoms you are likely to see in integration tests:

- `ClassNotFoundException: javax.xml.bind.DatatypeConverter` originating from:
  - `io.jsonwebtoken.impl.Base64Codec.decode(...)`
  - `io.jsonwebtoken.impl.DefaultJwtParser.setSigningKey(...)`

Root cause:

- **JJWT 0.9.x** relies on `javax.xml.bind` APIs that are not present on the Java 17 runtime.
- Spring Boot 3 and Java 17 remove the old Java EE/JAXB modules that JJWT 0.9.x expects.

### 1.2 Security Behavior Changes

Common test failures after Phase 1:

- Expected `200 OK` but got `403 FORBIDDEN` for endpoints that used to be public or authenticated differently.
- Expected `200/201` but got `500 INTERNAL_SERVER_ERROR` due to JWT parsing failures.
- NullPointerExceptions in tests where `ResponseEntity.getBody()` is `null` because the controller returned an error status.

Root causes:

- **Spring Security 6** tightens or changes some defaults (for example, request matcher behavior, filter chains, and error handling).
- Until JWT is working and security rules are revalidated, integration tests will justifiably fail.

> **Goal of Phase 1.1:** Fix these authentication/security issues without introducing new application features.

---

## Section 2 – Plan of Attack for Phase 1.1

At a high level, Phase 1.1 consists of four steps:

1. **Upgrade JJWT** to a Java 17–compatible version and modernize `JwtTokenProvider`.
2. **Verify and adjust SecurityConfig** for Spring Security 6 behavior.
3. **Refine integration tests** to reflect the new behavior (both positive and negative cases).
4. **Re-run tests and security checks**, and document remaining gaps.

You should perform these steps on a dedicated branch (for example, `feature/phase1.1-jwt-security`), not directly on `main`.

---

## Section 3 – Upgrade JJWT to a Java 17–Compatible Version

### 3.1 Update Dependency in pom.xml

**What you change:**

- Replace the existing `jjwt` 0.9.x dependency with a modern JJWT release that supports Java 17 and Jakarta. A common pattern (check latest docs) is:

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

> **Note:** Adjust versions if you standardize on a newer JJWT release; update this guide accordingly.

### 3.2 Refactor JwtTokenProvider

With newer JJWT versions, you typically:

- Use `Jwts.builder()` with a `Key` (for example, `SecretKey`) rather than a raw `String` and `DatatypeConverter`.
- Use `Jwts.parserBuilder().setSigningKey(key).build()` instead of the older `Jwts.parser().setSigningKey(...)`.

**Refactor steps (conceptual):**

1. Derive a signing key from `app.jwt.secret` using a supported algorithm (for example, HS256):

   - Convert the secret string to bytes (UTF-8) and use `Keys.hmacShaKeyFor(...)` (from JJWT).
   - Ensure the secret length is sufficient for the chosen algorithm.

2. Update **token creation** to use the `Key` object and builder API.
3. Update **token parsing** to use `Jwts.parserBuilder()`, attach the `Key`, and call `build().parseClaimsJws(token)`.
4. Make sure all exceptions are caught and translated consistently into 401/403 responses, not 500s.

> **Deliverable:** `JwtTokenProvider` no longer references `DatatypeConverter`, compiles on Java 17, and passes basic smoke tests (generating and parsing a token).

---

## Section 4 – Align Spring Security Behavior with Tests

After fixing JWT, revisit your **SecurityConfig** and integration tests together.

### 4.1 Confirm Security Rules

- Check your `SecurityFilterChain` to ensure it expresses the intended rules:
  - `/api/auth/**` endpoints should remain public (login/register).
  - `/h2-console/**` may be public in dev.
  - Swagger/OpenAPI endpoints should match your documentation policy.
  - All other API endpoints should require authentication.

> If tests expect an endpoint to be public, make sure it is covered in `requestMatchers(...).permitAll()`.

### 4.2 Normalize Error Handling

- Ensure exceptions from JWT parsing and authentication failures lead to:
  - **401 UNAUTHORIZED** for invalid/missing tokens, or
  - **403 FORBIDDEN** for insufficient privileges,
  - Not **500 INTERNAL_SERVER_ERROR**.

If necessary, introduce or update:

- An `AuthenticationEntryPoint` for unauthenticated requests.
- An `AccessDeniedHandler` for forbidden requests.

Align these behaviors with what your integration tests assert.

---

## Section 5 – Update Integration Tests

### 5.1 Re-baseline Expected Status Codes

For each integration test (for example, `TaskControllerIntegrationTest`, `SecurityIntegrationTest`):

1. Decide whether the target endpoint should be:
   - Public (no token required),
   - Protected (valid token required), or
   - Admin-only.
2. Adjust the test to:
   - Include or omit JWTs appropriately.
   - Expect `200/201` only when the request is valid and authorized.
   - Expect `401/403` where authentication or authorization should fail.

### 5.2 Use Realistic JWTs in Tests

- Replace any hard-coded or legacy tokens with ones generated via the updated `JwtTokenProvider` (or via a helper method in tests).
- Make sure test secrets match `app.jwt.secret` and the algorithm used in production code.

### 5.3 Stabilize H2/Test Data

- If data uniqueness or ordering issues arise (for example, duplicate key insert failures), ensure:
  - The test profile (`application-test.properties`) uses an in-memory H2 database (for example, `jdbc:h2:mem:testdb`).
  - `spring.jpa.hibernate.ddl-auto` is set appropriately for tests (in this demo, `create-drop`).
  - `spring.jpa.defer-datasource-initialization=true` and `spring.sql.init.mode=always` are set so that the JPA schema is created before `data.sql` runs and `data.sql` is always applied when the context starts.
  - `data.sql` is both **idempotent** and **identity-aware** within the lifecycle of your tests. In this reference project we use:

  ```sql path=null start=null
  -- Idempotent H2 seeding
  MERGE INTO users (id, username, email, password, roles, created_at) KEY(id) VALUES (...);
  MERGE INTO tasks (id, title, description, status, priority, assigned_to_id, created_at, updated_at) KEY(id) VALUES (...);

  -- Ensure identity sequences continue after seeded ids
  ALTER TABLE users ALTER COLUMN id RESTART WITH 3;
  ALTER TABLE tasks ALTER COLUMN id RESTART WITH 6;
  ```

  - Adjust the `RESTART WITH` values so that JPA-generated ids start at `max(seed_id) + 1` for your own data set.

> **Deliverable:** All security- and auth-related integration tests pass under Java 17 + Spring Boot 3.

---

## Section 6 – Re-run Security Checks and Document Outcomes

### 6.1 Full Build

- Run:

  ```bash
  mvn clean verify
  ```

- Record:
  - All tests passing (unit + integration).
  - Any remaining flaky or context-specific tests as TODOs.

### 6.2 Dependency-Check

- Run:

  ```bash
  mvn dependency-check:check
  ```

- Compare reports (baseline vs post–Phase 1.1):
  - Highlight whether JWT-related libraries and security stack introduced or removed CVEs.
  - Update any suppression entries for vetted false positives.

### 6.3 Document Changes

- In your migration documentation (for example, Phase 1 guide and this appendix):
  - Summarize JJWT version changes and rationale.
  - Summarize any security rule changes (for example, endpoints that are now protected).
  - Summarize test suite improvements (new/updated integration tests).

---

## 7. Success Criteria for Phase 1.1

Phase 1.1 is complete when:

- ✅ JJWT has been upgraded and `JwtTokenProvider` works on Java 17 without `DatatypeConverter`.
- ✅ Spring Security configuration is aligned with the intended authorization model and uses modern patterns.
- ✅ All **unit and integration tests** pass under Java 17 + Spring Boot 3.
- ✅ Dependency-Check continues to run and any new CVEs introduced by security stack changes are understood and documented (ideally reduced or eliminated).

At this point, the application is not only structurally migrated to Spring Boot 3 but also **security-aligned** with the modern stack, and ready for further optimization phases (Java 21, performance tuning, additional security hardening, etc.).
