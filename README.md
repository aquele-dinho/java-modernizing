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

## 🏗️ Architecture (Baseline)

Phase 0 is a **Task Management REST API** that intentionally represents a typical legacy Spring Boot 2.4 application:
- **Domain:** User and Task entities with JPA relationships
- **Security:** JWT-based stateless authentication with role-based access control
- **API:** RESTful endpoints for authentication, task CRUD, and user management
- **Database:** H2 in-memory database with sample data

If you need detailed endpoint examples, test behavior, or project layout, see:
- [`docs/BASELINE_APP_REFERENCE.md`](docs/BASELINE_APP_REFERENCE.md)

## 🧭 Using the Modernization Guides

The **main goal** of this repository is to serve as a playground for the step-by-step modernization guides under `docs/`.

Recommended reading/execution order:

1. **Security & SCA workflow**  
   - [`docs/GUIDE_SECURITY.md`](docs/GUIDE_SECURITY.md) – how to configure OWASP Dependency-Check, NVD API key, suppression management, and security gates.
2. **Phase 1 – Java 11 → 17 & Spring Boot 2.4 → 3.0**  
   - [`docs/GUIDE_PHASE1.md`](docs/GUIDE_PHASE1.md) – run OpenRewrite, migrate `javax.*` → `jakarta.*`, upgrade Spring Security, and apply security checks.  
3. **Phase 2 – Java 17 → 21 & optimizations**  
   - [`docs/GUIDE_PHASE2.md`](docs/GUIDE_PHASE2.md) – migrate to Java 21, enable Virtual Threads, and validate performance and security.

> 🇧🇷 Brazilian Portuguese versions of the guides are also available alongside the English files (same names with language suffix).

Each guide assumes you can build the Phase 0 baseline and then walks you through the changes, including the security validation steps.

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

## 📡 API & Baseline Reference

The baseline is a simple Task Management API with JWT-based security.

- For **interactive exploration**, use Swagger UI at `http://localhost:8080/swagger-ui.html`.
- For **detailed cURL examples** of authentication, task, and user endpoints, see:
-  - [`docs/BASELINE_APP_REFERENCE.md`](docs/BASELINE_APP_REFERENCE.md)

## 🧪 Running Tests (Baseline)

Phase 0 includes unit and integration tests to validate baseline behavior before you start migrating.

Typical commands:

```bash
mvn test          # run tests
mvn clean verify  # full verification build
```

For a breakdown of which tests exist and what they cover, see [`docs/BASELINE_APP_REFERENCE.md`](docs/BASELINE_APP_REFERENCE.md).

## 🔒 Security & OWASP SCA

Security is not just an add-on; it is a **first-class goal** of this modernization demo.

- The **baseline** intentionally behaves like a typical legacy app (no SCA tooling wired into `pom.xml`).
- The **guides** show you how to introduce OWASP Dependency-Check and integrate it with OpenRewrite recipes.

To learn how to:
- Configure NVD API keys
- Run gated security checks
- Manage suppression files and generate SBOM/VDR/VEX

refer to **[`docs/GUIDE_SECURITY.md`](docs/GUIDE_SECURITY.md)** and [`docs/OWASP.md`](docs/OWASP.md).

## 🔄 Migration Path (High-Level)

The modernization journey is intentionally **guide-driven**:

- **Phase 0:** Establish and understand the legacy baseline (this README + [`docs/BASELINE_APP_REFERENCE.md`](docs/BASELINE_APP_REFERENCE.md)).
- **Phase 1:** Follow [`docs/GUIDE_PHASE1.md`](docs/GUIDE_PHASE1.md) to migrate to Java 17 + Spring Boot 3.0 using OpenRewrite and gated security checks.
- **Phase 2:** Follow [`docs/GUIDE_PHASE2.md`](docs/GUIDE_PHASE2.md) to move to Java 21, enable Virtual Threads, and perform final security/performance validation.

The guides contain the precise commands and OpenRewrite recipes; this README only summarizes the path.

## 📦 Project Structure (High-Level)

For a detailed, baseline-focused view of the project layout, use [`docs/BASELINE_APP_REFERENCE.md`](docs/BASELINE_APP_REFERENCE.md).

At a high level:

```text
java-modernizing/
├── src/main/java/dev/tiodati/demo/modernization/   # Application code
├── src/test/java/dev/tiodati/demo/modernization/   # Unit & integration tests
├── src/main/resources/                             # Configuration & data
├── docs/                                           # Research + step-by-step guides
├── pom.xml                                         # Maven build (baseline; tooling added via guides)
├── WARP.md                                         # Specification
└── README.md                                       # High-level overview & entrypoint
```

## 🔍 Legacy Patterns (Intentional)

The baseline intentionally demonstrates patterns that the guides will migrate:

1. **`javax.*` imports** instead of `jakarta.*`
2. **Spring Security method chaining** using `.antMatchers()` / `.authorizeRequests()` and `WebSecurityConfigurerAdapter`
3. **Apache HttpClient 4.x** customization in `RestClientConfig`
4. **Deprecated configuration properties** such as `server.max.http.header.size`

These patterns are marked with `MIGRATION NOTE` comments throughout the code. The guides call them out explicitly when it is time to change them.

## 📚 Additional Resources

- **Migration Research:** See [`docs/RESEARCH.md`](docs/RESEARCH.md) for comprehensive analysis
- **OWASP SCA Strategy:** See [`docs/OWASP.md`](docs/OWASP.md) for security methodology
- **Technical Spec:** See [`WARP.md`](WARP.md) for detailed specifications
- **OpenRewrite Docs:** https://docs.openrewrite.org
- **OWASP Dependency-Check:** https://owasp.org/www-project-dependency-check/
- **OWASP Dep-Scan:** https://owasp.org/www-project-dep-scan/

## 🐛 Known Issues & Security Warnings

- ⚠️ `TrustAllStrategy` in `RestClientConfig` is **insecure** and for demonstration only
- ⚠️ H2 console should be **disabled in production**
- ⚠️ JWT secret should be **externalized** and secured in production
- ⚠️ The baseline may contain known CVEs by design, to demonstrate improvement through modernization
- ⚠️ Without an NVD API Key, OWASP Dependency-Check scans (once enabled) can be very slow

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

**Version:** 2.0.0-SNAPSHOT (Phase 0 - Baseline)  
**Java:** 11  
**Spring Boot:** 2.4.13  
**Security:** Legacy baseline (OWASP + OpenRewrite added via guides)  
**Status:** ✅ Ready for Security-First Migration (start with `docs/GUIDE_SECURITY.md`)
