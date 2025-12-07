# Phase 2 Modernization Guide – Java 17→21 & Optimizations

> Brazilian Portuguese version: this guide must have a translated counterpart in `GUIDE_PHASE2_pt-BR.md`.

## 0. Overview & Objectives

This guide covers the upgrade from **Java 17** to **Java 21** on top of **Spring Boot 3.x**, focusing on:
- Java language/runtime migrations via OpenRewrite.
- Enabling **Virtual Threads**.
- Benchmarking performance.
- Performing a **final security audit** and generating **compliance artifacts**.

For each section we describe:
- **Command** – what to run.
- **Expected result** – runtime behavior, performance metrics, or code changes.

The goal is that, after following this guide:
- The project builds and runs on Java 21.
- Virtual threads are correctly configured and observable.
- Performance impact is measured and documented.
- The security posture is at least as strong as before (no new Critical/High CVEs).
- SBOM/VDR/VEX artifacts are generated and understood.

---

## Section 1 – Java 21 Migration

Goal: Upgrade the project to build and run on Java 21.

### Step 1.1 – Update pom.xml to Java 21
- **What you change:**
  - Set `<java.version>21</java.version>` (or the equivalent property used in the build).
  - If you use a Maven compiler plugin, ensure `<release>21</release>` (or `<source>`/`<target>` 21) is set.
- **Expected result (code):**
  - `pom.xml` clearly indicates Java 21 as the target release.

Example (snippet):

```xml path=null start=null
<properties>
    <java.version>21</java.version>
</properties>
```

You do **not** need to run a build yet, but you can verify the configuration with:

- **Command:** `mvn -q help:evaluate -Dexpression=java.version -DforceStdout`
- **Expected result:** the output is `21`.

### Step 1.2 – Run OpenRewrite recipe UpgradeToJava21
- **Command:**
  - `mvn rewrite:run -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava21`
- **Expected result (code):**
  - Source files are updated to use Java 21-friendly APIs and patterns where appropriate.
  - Any deprecated constructs handled by the recipe are migrated.

If you want to preview changes first, run instead:

- **Command:** `mvn rewrite:dryRun -Drewrite.activeRecipes=org.openrewrite.java.migrate.UpgradeToJava21`
- **Expected result:**
  - A `rewrite.patch` file is created under the target rewrite directory, showing proposed changes.

### Step 1.3 – Document automated changes
- **What you document in this guide:**
  - Examples of collections updated to **Sequenced Collections** (e.g., `List` → `SequencedCollection` usage where appropriate).
  - Examples where pattern matching was improved or simplified.
  - Any APIs that were replaced or upgraded by the recipe.
- **Expected result:**
  - Readers see concrete before/after snippets illustrating Java 21 migrations.

---

## Section 2 – Virtual Threads Configuration

Goal: Enable and validate **Virtual Threads** support in the application.

### Step 2.1 – Enable Virtual Threads in configuration
- **What you change:**
  - Application properties or configuration beans to use executors backed by virtual threads (e.g., for Tomcat or task execution).
- **Expected result (code):**
  - Configuration file(s) clearly indicate the use of virtual threads.

Example (conceptual Spring configuration – adapt to your project):

```java path=null start=null
@Bean
Executor taskExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
}
```

You might also configure Spring Boot’s application properties to use this executor where appropriate.

### Step 2.2 – Configure Tomcat for Virtual Threads
- **What you change:**
  - Spring Boot server configuration or a dedicated configuration class that:
    - Switches the executor to a virtual-thread-based executor.
- **Expected result (behavior):**
  - Application still starts successfully.
  - Thread dumps (if inspected) show virtual threads being used to serve requests.

Example (illustrative only):

```java path=null start=null
@Bean
WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatVirtualThreadsCustomizer() {
    return factory -> factory.addConnectorCustomizers(connector -> {
        connector.getProtocolHandler().setExecutor(
            Executors.newVirtualThreadPerTaskExecutor()
        );
    });
}
```

Use thread dumps (`jstack` or IDE tools) to verify that requests are handled by virtual threads (often visible as `VirtualThread` in the stack traces).

### Step 2.3 – Document configuration changes
- **What you describe in this guide:**
  - The exact property names and configuration beans used.
  - Any caveats or limitations (e.g., blocking calls that might negate virtual thread benefits).
- **Expected result:**
  - Readers can reproduce virtual thread configuration in their own applications.

---

## Section 3 – Performance Benchmarking

Goal: Quantify the performance impact of Java 21 + Virtual Threads.

### Step 3.1 – Establish Java 17 baseline
- **What you do:**
  - Run a simple load test or benchmarking scenario on the Java 17 version.
  - Capture at least throughput (requests/second) and key latency percentiles (P50, P95, P99).
- **Expected result:**
  - Baseline metrics recorded in this guide (e.g., requests/second, average latency, P95 latency).

Example (pseudo-metrics):

```text path=null start=null
Baseline (Java 17)
- RPS: 1,000
- Avg latency: 45 ms
- P95 latency: 80 ms
- P99 latency: 120 ms
```

### Step 3.2 – Benchmark Java 21 + Virtual Threads
- **What you do:**
  - Run the **same** load test scenario against the Java 21 + Virtual Threads configuration.
- **Expected result:**
  - New metrics captured and documented.

Example (pseudo-metrics):

```text path=null start=null
Java 21 + Virtual Threads
- RPS: 1,400
- Avg latency: 38 ms
- P95 latency: 65 ms
- P99 latency: 100 ms
```

### Step 3.3 – Document throughput and latency improvements
- **What you present in this guide:**
  - A side-by-side summary comparing baseline vs. Java 21 + Virtual Threads.
- **Expected result:**
  - Clear narrative about performance impact, including cases where it improves or stays similar.

Example narrative:

```text path=null start=null
Compared to Java 17, Java 21 + Virtual Threads increased throughput by ~40% (1,000 → 1,400 RPS)
while reducing P95 latency from 80 ms to 65 ms. P99 latency also improved from 120 ms to 100 ms.
```

---

## Section 4 – Final Security Audit

Goal: Perform a **zero-tolerance** security scan at the end of the modernization.

### Step 4.1 – Run OWASP Dependency-Check
- **Command:**
  - `mvn dependency-check:check`
- **Expected result:**
  - Scan completes with HTML/JSON reports generated under `target/`.
  - The build passes only if no vulnerabilities are found above the configured CVSS threshold.

### Step 4.2 – Enforce zero-tolerance for Critical/High CVEs
- **What you verify:**
  - Reports show **no Critical or High** vulnerabilities that are not explicitly suppressed with documented rationale.
- **Expected result:**
  - Either:
    - Build succeeds with **0 Critical/High** un-suppressed CVEs, or
    - Build fails, prompting remediation/upgrade of vulnerable dependencies.

### Step 4.3 – Audit and purge obsolete suppressions
- **What you do:**
  - Review `dependency-suppression.xml` and remove any entries that no longer apply.
- **Expected result (code):**
  - Suppression file only contains **current, justified** suppressions.

### Step 4.4 – Document final security posture
- **What you record in this guide:**
  - Final counts of vulnerabilities by severity.
  - Explanation of any remaining, accepted risks.
- **Expected result:**
  - Clear, auditable record of the project’s security status after modernization.

---

## Section 5 – Compliance Artifacts

Goal: Produce artifacts that support supply chain and vulnerability management.

### Step 5.1 – Generate SBOM (Software Bill of Materials)
- **What you do:**
  - Use your chosen tool (e.g., CycloneDX Maven plugin or OWASP tools) to generate an SBOM.
- **Expected result:**
  - SBOM file (e.g., JSON or XML) present in `target/` or a dedicated output directory.

Example (CycloneDX Maven plugin):

```bash path=null start=null
mvn -q org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom
```

### Step 5.2 – Generate VDR (Vulnerability Disclosure Report)
- **What you produce:**
  - A report summarizing identified vulnerabilities, their status, and remediation.
- **Expected result:**
  - Document stored under `docs/` that can be shared with stakeholders.

Suggested contents:
- Scope (application version, date, tooling used).
- Vulnerability summary by severity.
- Remediation status (fixed, suppressed with rationale, accepted risk with compensating controls).

### Step 5.3 – Generate VEX (Vulnerability Exploitability eXchange)
- **What you produce:**
  - A VEX document describing which known vulnerabilities are **not exploitable** in this context.
- **Expected result:**
  - Machine-readable VEX file aligned with CSAF or similar format.

Typical workflow:
- Start from the SBOM and VDR.
- For each vulnerability, determine exploitability in the context of this application.
- Mark non-exploitable cases in the VEX document with justification.

### Step 5.4 – Document compliance artifact usage
- **What you describe in this guide:**
  - How to read and maintain SBOM, VDR, and VEX over time.
- **Expected result:**
  - Readers understand how these artifacts fit into DevSecOps and audits.

---

## Additional Materials

At the end of this guide, include:
- A **Before/After Performance** section with charts or tables.
- A **Java 21 Feature Adoption** section with concrete examples (e.g., pattern matching, record patterns, sequenced collections) and expected code simplifications.

### Example: Before/After Performance Summary

```text path=null start=null
Before (Java 17)
- RPS: 1,000
- Avg latency: 45 ms
- P95 latency: 80 ms

After (Java 21 + Virtual Threads)
- RPS: 1,400
- Avg latency: 38 ms
- P95 latency: 65 ms
```

### Example: Java 21 Feature Adoption Opportunities

Some ideas for where to adopt Java 21 features in this project:

- **Pattern Matching for `instanceof`**
  - Replace manual casts with pattern matching to make code more concise and safer.

```java path=null start=null
if (obj instanceof User user) {
    // use 'user' directly
}
```

- **Sequenced Collections**
  - Where the order of elements matters and is frequently traversed from both ends, consider `SequencedCollection` / `SequencedSet`.

- **Record Patterns and Switch Enhancements**
  - In DTO mapping or simple state machines, consider using enhanced `switch` expressions and record patterns to simplify branching logic.

Document any actual adoptions you make so that future readers can see tangible benefits of the Java 21 upgrade.
