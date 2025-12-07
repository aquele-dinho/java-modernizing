# Phase 1 Modernization Guide – Java 11→17 & Spring Boot 2.4→3.0

> Brazilian Portuguese version: this guide must have a translated counterpart in `GUIDE_PHASE1_pt-BR.md`.

## 0. Overview & Objectives

This guide walks you through a **real-world migration** from **Java 11 + Spring Boot 2.4.13** to **Java 17 + Spring Boot 3.0** using **OpenRewrite** and **OWASP Dependency-Check**.

For **every step**, we describe:
- **Command** – what you run.
- **Expected result** – what you should see or which **code changes** should exist after the step.

### Prerequisites
- Java 11 toolchain installed and active.
- Maven 3.8+.
- This repository checked out and on the **Phase 0 baseline** state (no security plugins pre-configured).

---

## Section 0 – Verify Baseline Application (Real-World Starting Point)

Goal: Confirm the project looks like a typical legacy app **before** adding any security or modernization tooling.

### Step 0.1 – Verify pom.xml has no OWASP Dependency-Check plugin
- **What you check:** `pom.xml` build plugins.
- **Expected result:**
  - There is **no** `<plugin>` block with `<groupId>org.owasp</groupId>` and `<artifactId>dependency-check-maven</artifactId>`.
  - If such a plugin exists, you remove it and re-run `mvn clean compile` to ensure the build still passes.

### Step 0.2 – Verify pom.xml has no OpenRewrite plugin
- **What you check:** `pom.xml` build plugins.
- **Expected result:**
  - There is **no** `<plugin>` block with `<groupId>org.openrewrite.maven</groupId>` and `<artifactId>rewrite-maven-plugin</artifactId>`.
  - If the plugin exists, remove it and confirm `mvn clean compile` still succeeds.

### Step 0.3 – Document baseline technology stack
- **What you record in this guide:**
  - Java version: **11** (LTS).
  - Spring Boot version: **2.4.13**.
  - No SCA (Software Composition Analysis) tools such as OWASP Dependency-Check configured.
- **Expected result:**
  - This guide explicitly states the starting point so readers can compare later phases.

---

## Section 1 – Configure OWASP Dependency-Check Plugin

Goal: Integrate OWASP Dependency-Check in `pom.xml` to establish a **security baseline** for third-party dependencies.

### Step 1.1 – Add dependency-check-maven plugin to pom.xml
- **What you change:** Add a `<plugin>` entry for `org.owasp:dependency-check-maven` under `<build><plugins>`.
- **Expected result (code):**
  - `pom.xml` contains a plugin block for OWASP Dependency-Check.
  - The plugin is configured to run the `check` goal.

### Step 1.2 – Configure NVD API key and basic options
- **What you change:** In the OWASP plugin configuration, set:
  - `<nvdApiKey>${env.NVD_API_KEY}</nvdApiKey>`
  - `<failBuildOnCVSS>7</failBuildOnCVSS>`
  - `<suppressionFile>dependency-suppression.xml</suppressionFile>`
  - `<formats>` containing `HTML` and `JSON`.
- **Expected result (code):**
  - `pom.xml` references `${env.NVD_API_KEY}` for NVD access.
  - The build will **fail** on findings with CVSS score ≥ 7 once the plugin is executed.

### Step 1.3 – Document NVD API Key setup
- **What you write in the guide:**
  - Point to the NVD API key request page.
  - Show example of exporting `NVD_API_KEY` as an environment variable.
- **Expected result:**
  - Readers understand how to obtain an API key and configure it locally/CI.

---

## Section 2 – Run Pre-Migration Security Baseline

Goal: Capture the **initial vulnerability posture** of the legacy stack.

### Step 2.1 – Run initial vulnerability scan
- **Command:**
  - `mvn dependency-check:check`
- **Expected result:**
  - Maven build completes and generates `dependency-check-report.html` and `dependency-check-report.json` under `target/`.
  - The guide instructs readers where to find these files and how to open the HTML report.

### Step 2.2 – Document baseline CVE count
- **What you record in the guide:**
  - The number of Critical, High, Medium, and Low vulnerabilities found by the initial scan.
- **Expected result:**
  - A table or bullet list summarizing counts, to be reused later for comparison after migration.

### Step 2.3 – Create and document dependency-suppression.xml
- **What you change:**
  - Create `dependency-suppression.xml` with entries for known false positives.
- **Expected result (code):**
  - The file exists at the project root and is referenced by the OWASP plugin.
- **Expected result (behavior):**
  - Re-running `mvn dependency-check:check` no longer flags suppressed vulnerabilities.

### Step 2.4 – Define security target
- **What you state in the guide:**
  - "After migration, the total count of Critical/High CVEs must be **less than or equal** to this baseline; introducing new high-risk CVEs is not acceptable."
- **Expected result:**
  - Clear acceptance criteria for every future scan.

---

## Section 3 – Configure OpenRewrite Plugin

Goal: Prepare automated code and configuration transformations.

### Step 3.1 – Add rewrite-maven-plugin and recipe dependencies
- **What you change in `pom.xml`:**
  - Add `<plugin>` for `org.openrewrite.maven:rewrite-maven-plugin`.
  - Under that plugin, add dependencies for:
    - `rewrite-spring` (5.21.0+)
    - `rewrite-migrate-java` (2.26.0+)
    - `rewrite-java-dependencies`.
- **Expected result (code):**
  - `pom.xml` contains one OpenRewrite plugin with all required dependencies.

### Step 3.2 – Configure active recipes and security checks
- **What you configure:**
  - `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0` as an active recipe.
  - `org.openrewrite.java.dependencies.DependencyVulnerabilityCheck` with `<maximumUpgradeDelta>PATCH</maximumUpgradeDelta>`.
- **Expected result (code):**
  - The plugin configuration clearly lists both migration and security recipes.
- **Expected result (behavior):**
  - When running OpenRewrite, it suggests safe patch upgrades for vulnerable dependencies.

---

## Section 4 – OpenRewrite Dry-Run

Goal: Preview migration changes before applying them to the codebase.

### Step 4.1 – Run dry-run
- **Command:**
  - `mvn rewrite:dryRun -Drewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0`
- **Expected result:**
  - Maven completes successfully.
  - A `rewrite.patch` file is generated under `target/rewrite/` (or similar path depending on plugin version).

### Step 4.2 – Review rewrite.patch
- **What you inspect:**
  - Changes to imports (`javax.*` → `jakarta.*`).
  - Refactoring of Spring Security configuration (old `antMatchers` DSL → lambda-based DSL).
- **Expected result (code changes preview):**
  - You see proposed replacements for all JPA validation imports and security configuration.
- **What you record in the guide:**
  - Concrete before/after code snippets for:
    - A sample entity using `javax.persistence.*`.
    - The legacy `SecurityConfig` using `antMatchers`.

### Step 4.3 – Identify manual intervention points
- **What you list:**
  - Places where HttpClient 4.x usage may need manual refactoring.
  - Any security rules that look semantically different after migration.
- **Expected result:**
  - A checklist of files/sections that must be revisited after automated changes are applied.

---

## Section 5 – Apply Migration

Goal: Apply the OpenRewrite recipes and persist the transformed code.

### Step 5.1 – Run OpenRewrite migration
- **Command:**
  - `mvn rewrite:run`
- **Expected result (code):**
  - Actual Java and configuration files are modified, matching the earlier `rewrite.patch`.
  - `pom.xml` reflects Spring Boot 3.x and Java 17 settings, if recipes include them.

### Step 5.2 – Review and commit changes
- **What you do:**
  - Use `git diff` to inspect all modifications.
- **Expected result:**
  - Only expected files (entities, controllers, SecurityConfig, pom.xml, etc.) have been changed.
  - No unrelated files are modified.

### Step 5.3 – Document breaking changes
- **What you describe in this guide:**
  - Specific breaking changes in `SecurityConfig` (e.g., removed `WebSecurityConfigurerAdapter`, new `SecurityFilterChain` bean).
  - HttpClient 4.x to 5.x migration notes (interfaces changed, configuration moved).
- **Expected result:**
  - Readers understand which changes were automatic and which require manual validation.

---

## Section 6 – Manual Refactoring

Goal: Fix remaining incompatibilities and adjust to new defaults.

### Step 6.1 – Update RestClientConfig for HttpClient 5.x
- **Expected code changes:**
  - Rest client configuration uses HttpClient 5.x APIs.
  - Any deprecated or removed classes from 4.x are replaced.

### Step 6.2 – Fix Spring Security Lambda DSL issues
- **Expected code changes:**
  - Security configuration uses `http.authorizeHttpRequests`, `http.securityMatcher`, etc.
  - All endpoints have equivalent authorization rules compared to the legacy version.

### Step 6.3 – Update deprecated configuration properties
- **Expected code changes:**
  - Deprecated server/security properties updated to their Spring Boot 3.x equivalents.

### Step 6.4 – Update tests for new defaults
- **Expected behavior:**
  - All tests compile and pass.
  - Any changed defaults (e.g., CSRF behavior, password encoder) are reflected in test assertions.

---

## Section 7 – Security Validation (Gated Check)

Goal: Ensure migration **did not worsen** the security posture.

### Step 7.1 – Run post-migration dependency check
- **Command:**
  - `mvn dependency-check:check`
- **Expected result:**
  - Build succeeds or fails according to `<failBuildOnCVSS>7</failBuildOnCVSS>`.
  - New HTML/JSON reports overwrite the previous ones.

### Step 7.2 – Compare CVE counts to baseline
- **What you record in the guide:**
  - A before/after comparison of vulnerability counts.
- **Expected result:**
  - Critical/High CVE count is **≤ baseline**.

### Step 7.3 – Update suppression file if needed
- **Expected code changes:**
  - `dependency-suppression.xml` may gain new entries for proven false positives.

---

## Section 8 – Testing & Validation

Goal: Validate functional behavior, tests, and performance after migration.

### Step 8.1 – Run full build and tests
- **Command:**
  - `mvn clean verify`
- **Expected result:**
  - Build completes successfully.
  - All unit and integration tests pass.

### Step 8.2 – Run application and validate endpoints
- **Command:**
  - `mvn spring-boot:run`
- **Expected result:**
  - Application starts without errors on the configured port.
  - All authentication and task management endpoints behave as before (or better).

### Step 8.3 – Compare performance metrics
- **What you record:**
  - Simple before/after metrics (throughput, latency) gathered from your preferred tool.
- **Expected result:**
  - Performance is at least as good as baseline, ideally improved.

---

## Before/After Code Snippets & Troubleshooting

This section provides **concrete examples** from the baseline codebase and how they are expected to look after the migration. Use these as reference when reviewing `rewrite.patch` and the actual changes in your project.

### 1. Entities: javax → jakarta

**Before (current baseline – `User` entity using javax.*)**

```java path=/Users/aquele_dinho/Projects/java-modernizing/src/main/java/dev/tiodati/demo/modernization/domain/User.java start=3
// MIGRATION NOTE: These javax.* imports will be migrated to jakarta.* in Spring Boot 3.x:
// - javax.persistence.* → jakarta.persistence.*
// - javax.validation.* → jakarta.validation.*
// OpenRewrite will automatically handle this namespace migration.
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {
```

**After (expected, post-migration – using jakarta.*)**

```java path=null start=null
// After running OpenRewrite + Spring Boot 3 upgrade recipes
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {
    // class body remains essentially the same
}
```

You should see similar namespace changes for `Task` and other entities:
- `javax.persistence.*` → `jakarta.persistence.*`
- `javax.validation.*` → `jakarta.validation.*`

### 2. Security Configuration: Legacy DSL → Lambda DSL

**Before (current baseline – legacy `WebSecurityConfigurerAdapter`)**

```java path=/Users/aquele_dinho/Projects/java-modernizing/src/main/java/dev/tiodati/demo/modernization/config/SecurityConfig.java start=55
/**
 * MIGRATION NOTE: This method uses LEGACY METHOD CHAINING patterns:
 * - .antMatchers() will become .requestMatchers()
 * - .authorizeRequests() will become .authorizeHttpRequests()
 * - Method chaining will be replaced with Lambda DSL in Spring Security 6.0
 */
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
            .cors()
            .and()
            .csrf()
                .disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/h2-console/**").permitAll()
                // Allow Swagger UI and OpenAPI endpoints
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated();

    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    http.headers().frameOptions().sameOrigin();
}
```

**After (expected, post-migration – `SecurityFilterChain` + Lambda DSL)**

```java path=null start=null
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**", "/h2-console/**",
                             "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
                .permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
    return http.build();
}
```

When you inspect `rewrite.patch` and the actual changes, look for:
- Removal of `WebSecurityConfigurerAdapter`.
- Introduction of a `SecurityFilterChain` bean.
- Use of `.authorizeHttpRequests()` + `.requestMatchers()` with the Lambda DSL.

### 3. Rest Client: HttpClient 4.x → 5.x

**Before (current baseline – `RestClientConfig` with HttpClient 4.x)**

```java path=/Users/aquele_dinho/Projects/java-modernizing/src/main/java/dev/tiodati/demo/modernization/config/RestClientConfig.java start=15
/**
 * REST Client Configuration using Apache HttpClient 4.x.
 * 
 * MIGRATION NOTE: This configuration demonstrates legacy patterns that will need updates:
 * 1. Apache HttpClient 4.x will be replaced with 5.x in Spring Boot 3.x
 * 2. Package names will change: org.apache.http.* (4.x) → org.apache.hc.client5.* (5.x)
 * 3. TrustAllStrategy is insecure and used only for demonstration purposes
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate() throws Exception {
        SSLContext sslContext = SSLContextBuilder
                .create()
                .loadTrustMaterial(new TrustAllStrategy())  // INSECURE - for demo only!
                .build();

        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory)
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(20)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);

        return new RestTemplate(requestFactory);
    }
}
```

**After (expected, post-migration – HttpClient 5.x, org.apache.hc.client5 packages)**

```java path=null start=null
import org.apache.hc.client5.http.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.ssl.SSLContexts;

@Configuration
public class RestClientConfig {

    @Bean
    RestTemplate restTemplate() throws Exception {
        // In production, replace this with proper certificate validation
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, (chain, authType) -> true) // INSECURE – demo only
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManagerShared(true)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);

        return new RestTemplate(requestFactory);
    }
}
```

> NOTE: The "after" code above is illustrative. Use `rewrite.patch` and the official HttpClient 5.x documentation to fine-tune the details for your context.

---

## Troubleshooting

This subsection lists common issues that can occur during the migration and how to investigate them.

### 1. Build errors after running `mvn rewrite:run`

Typical symptoms:
- Classes not found (`ClassNotFoundException` or `cannot find symbol`) for `jakarta.*` imports.
- Compilation errors in `SecurityConfig` after removing `WebSecurityConfigurerAdapter`.

Recommended actions:
- Verify that all required Jakarta dependencies were added (for example, validation, JPA).
- Check that `SecurityConfig` was fully migrated to the `SecurityFilterChain` style.
- Use `git diff` to carefully review what OpenRewrite changed.

### 2. OWASP Dependency-Check issues

Typical symptoms:
- Build fails with connection errors to NVD.
- Very long runtime on the first plugin execution.

Recommended actions:
- Confirm that `NVD_API_KEY` is defined in the environment and that `pom.xml` references `${env.NVD_API_KEY}`.
- Check network connectivity / proxy configuration.
- If the local database is corrupted, clear the Dependency-Check cache and run again.

### 3. OpenRewrite recipes not applied

Typical symptoms:
- `mvn rewrite:dryRun` runs, but `rewrite.patch` is empty or nearly empty.

Recommended actions:
- Check that `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0` is listed in `<activeRecipes>` or passed via `-Drewrite.activeRecipes`.
- Verify that recipe dependencies (`rewrite-spring`, `rewrite-migrate-java`, `rewrite-java-dependencies`) are present in the plugin.
- Run `mvn -X rewrite:dryRun` to see more detailed logs.

### 4. Security behavior changes

Typical symptoms:
- Endpoints start requiring authentication where they were previously public, or vice-versa.

Recommended actions:
- Compare endpoint mappings before/after (especially `antMatchers` vs `requestMatchers`).
- Add security integration tests for critical endpoints (login, registration, admin operations).

Use this section as a checklist when validating the result of each execution (`dependency-check:check`, `rewrite:dryRun`, `rewrite:run`, `clean verify`).
