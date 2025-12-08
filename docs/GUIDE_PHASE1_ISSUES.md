# GUIDE_PHASE1 Issues & Fix Proposals

## Issue 1 – OWASP Dependency-Check v11.0.0 fails on initial baseline scan

**Guide section:** Section 2 – Run Pre-Migration Security Baseline (Step 2.1)

**What the guide says:**
- Command: `mvn dependency-check:check`
- Expected result: Maven build completes and generates `dependency-check-report.html` and `dependency-check-report.json` under `target/`.

**What actually happened:**
- Running `mvn dependency-check:check` with plugin version `11.0.0` caused the build to fail.
- The scan started, downloaded NVD data, and then aborted with a Jackson deserialization error related to CVSS v4.0 metrics.
- The error message (paraphrased) indicated an unexpected enum value like `SAFETY` for a `ModifiedCiaType` field when parsing NVD JSON.
- As a result, **no `dependency-check-report.html` or `.json` files were generated under `target/`**, and the build ended with `BUILD FAILURE`.

**Root cause (analysis):**
- The configured plugin version (`11.0.0`) is not compatible with the current NVD API JSON schema, specifically for CVSS v4.0 data.
- This is external to the project code but breaks the “baseline scan” step exactly as described in the guide.

**Why this means the guide is not bulletproof:**
- The guide assumes that `mvn dependency-check:check` will succeed with `11.0.0+` but does not:
  - Pin a concrete, verified plugin version known to work with the current NVD schema; or
  - Provide troubleshooting/fallback instructions when the plugin fails due to NVD schema changes.
- In a fresh environment following the guide literally, Section 2.1 cannot be completed successfully.

**Proposed fixes to the guide:**

1. **Pin a tested plugin version and document it explicitly**

   Instead of `11.0.0+`, specify and validate a concrete version, for example:

   ```xml
   <plugin>
       <groupId>org.owasp</groupId>
       <artifactId>dependency-check-maven</artifactId>
       <version>11.0.x</version> <!-- replace with a version verified against current NVD schema -->
       ...
   </plugin>
   ```

   And add text like:

   > NOTE: Always check the OWASP Dependency-Check release notes for compatibility with the current NVD schema. If scans start failing with JSON parsing errors, update the plugin version here to a release that explicitly supports the latest NVD format.

2. **Add a troubleshooting subsection under Section 2 for NVD schema / JSON parsing errors**

   Under “Section 2 – Run Pre-Migration Security Baseline”, add a Troubleshooting bullet similar to:

   > **Problem:** `mvn dependency-check:check` fails with Jackson deserialization errors mentioning CVSS v4.0 fields (e.g., unexpected enum values like `SAFETY` or `ModifiedCiaType`).  
   > **Cause:** The configured Dependency-Check plugin version is not compatible with the current NVD JSON schema.  
   > **Resolution:**
   > - Update the `dependency-check-maven` plugin version in `pom.xml` to the latest stable release.
   > - Re-run `mvn dependency-check:check`.  
   > - If the error persists, consult the official OWASP Dependency-Check GitHub issues for known NVD schema changes.

3. **Clarify expectations around report generation when the build fails**

   Explicitly state that the expected HTML/JSON reports are only generated when the scan completes successfully. If the build fails during NVD processing, users should:

   - Not expect `dependency-check-report.html` / `.json` to exist under `target/`.
   - Treat this as a dependency-check configuration or ecosystem issue, not as a project code problem.

**Optional project-side mitigation (code change suggestion):**

In addition to guide changes, you can mitigate failures by:

- Keeping the plugin version in a dedicated property so it is easy to bump:

  ```xml
  <properties>
      ...
      <dependency-check.maven.version>11.0.0</dependency-check.maven.version>
  </properties>

  <plugin>
      <groupId>org.owasp</groupId>
      <artifactId>dependency-check-maven</artifactId>
      <version>${dependency-check.maven.version}</version>
      ...
  </plugin>
  ```

- This makes future updates (driven by NVD schema changes) a single-line change instead of hunting through plugin blocks.

---

## Issue 2 – Maven property interpolation fails for OpenRewrite plugin versions

**Guide section:** Section 3 – Configure OpenRewrite Plugin (Steps 3.1–3.2)

**What the guide says:**
- Add the OpenRewrite Maven plugin with `<version>` references to properties.
- Add recipe dependencies with versioned properties like `${rewrite.spring.version}`.

**What actually happened:**
- After defining properties in `<properties>` and referencing them in the plugin block:
  ```xml
  <properties>
      <rewrite.maven.plugin.version>6.22.0</rewrite.maven.plugin.version>
      <rewrite.spring.version>5.21.1</rewrite.spring.version>
      ...
  </properties>
  <plugin>
      <version>${rewrite.maven.plugin.version}</version>
      ...
  ```
- Running `mvn rewrite:dryRun` fails with errors like:
  ```
  'build.plugins.plugin.version' for org.openrewrite.maven:rewrite-maven-plugin
  must be a valid version but is '${rewrite.maven.plugin.version}'.
  ```
- Maven is not interpolating the properties. The build fails before the plugin can execute.

**Root cause (analysis):**
- The Spring Boot parent POM (`spring-boot-starter-parent:2.4.13`) does not include the property definitions added to the child `pom.xml` in scope for plugin version resolution **at the point Maven parses the POM model**.
- This appears to be a Maven POM processing order issue where properties defined in the child are not visible during plugin version resolution when they are scoped in certain parent/child contexts.

**Why this means the guide is not bulletproof:**
- The guide (Section 3.1) suggests adding properties and referencing them via `${...}`, following standard Maven patterns (and mirroring what was done successfully for `dependency-check.maven.version`).
- However, this approach fails in this specific POM context, meaning the reader cannot proceed with Section 4 (dry-run) as described.
- The guide does not provide a fallback or troubleshooting step for this case.

**Proposed fixes to the guide:**

1. **Use hardcoded plugin versions in Section 3 (pragmatic workaround)**

   Until the root cause is better understood, recommend hardcoding the plugin and recipe versions directly:

   ```xml
   <plugin>
       <groupId>org.openrewrite.maven</groupId>
       <artifactId>rewrite-maven-plugin</artifactId>
       <version>6.22.0</version> <!-- Hardcoded for compatibility with Spring Boot parent -->
       ...
       <dependencies>
           <dependency>
               <groupId>org.openrewrite.recipe</groupId>
               <artifactId>rewrite-spring</artifactId>
               <version>5.21.1</version> <!-- Hardcoded -->
           </dependency>
           ...
       </dependencies>
   </plugin>
   ```

   Add a note in Section 3.1 like:

   > **Note:** Unlike the Dependency-Check plugin, OpenRewrite plugin versions must be hardcoded in this POM due to property interpolation limitations when using `spring-boot-starter-parent:2.4.13`. If you need to update versions later, edit the `<version>` tags directly in the plugin block.

2. **Add troubleshooting subsection under Section 3 for property interpolation errors**

   Under "Section 3 – Configure OpenRewrite Plugin", add:

   > **Problem:** Running `mvn rewrite:dryRun` fails with error: `'build.plugins.plugin.version' for org.openrewrite.maven:rewrite-maven-plugin must be a valid version but is '${...}'`.  
   > **Cause:** Maven property interpolation is not resolving `${...}` references in plugin versions for this POM structure.  
   > **Resolution:**
   > - Replace all `${rewrite.*}` property references with hardcoded version strings in the plugin block.
   > - Re-run the command.

3. **Clarify that the property-based approach shown in Section 1 (Dependency-Check) may not work for all plugins**

   In Section 3.1, before the code example, add:

   > While we successfully used a `${dependency-check.maven.version}` property for the OWASP plugin, **some plugins (like OpenRewrite) may require hardcoded versions** depending on your parent POM configuration. If you encounter property interpolation errors, hardcode the versions as shown below.

**Optional project-side mitigation (code change suggestion):**

Hardcode all OpenRewrite plugin and recipe versions directly in `pom.xml`, removing the property definitions:

```xml
<!-- Remove these from <properties>: -->
<!-- <rewrite.maven.plugin.version>6.22.0</rewrite.maven.plugin.version> -->
<!-- <rewrite.spring.version>5.21.1</rewrite.spring.version> -->
<!-- ... -->

<!-- Use this in the plugin block: -->
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>6.22.0</version>
    ...
    <dependencies>
        <dependency>
            <groupId>org.openrewrite.recipe</groupId>
            <artifactId>rewrite-spring</artifactId>
            <version>5.21.1</version>
        </dependency>
        ...
    </dependencies>
</plugin>
```

This makes the POM explicit and unambiguous, at the cost of requiring direct edits for version bumps.

---

## Issue 3 – OpenRewrite UpgradeSpringBoot_3_0 recipe fails with NoSuchMethodError

**Guide section:** Section 4 – OpenRewrite Dry-Run (Steps 4.1–4.3)

**What the guide says:**
- Run `mvn rewrite:dryRun -Drewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0`.
- Expect Maven to complete successfully and generate `target/rewrite/rewrite.patch` for review.

**What actually happened:**
- With plugin configuration:
  - `rewrite-maven-plugin` version: `6.22.0`.
  - Recipe bundles: `rewrite-spring:5.21.1`, `rewrite-migrate-java:3.22.0`, `rewrite-java-dependencies:1.45.0`.
- Maven resolved the plugin and recipes, started executing `UpgradeSpringBoot_3_0`, and then failed with:
  ```
  java.lang.NoSuchMethodError:
    'void org.openrewrite.java.tree.JavaType$Method.<init>(..., java.util.List, java.util.List, java.util.List, java.util.List)'
    at org.openrewrite.java.spring.boot2.ConvertToSecurityDslVisitor.createDefaultsCall(ConvertToSecurityDslVisitor.java:280)
    ...
  ```
- The error occurs while visiting `SecurityConfig.java` during the Spring Security DSL migration.
- No `rewrite.patch` file is generated; the dry run aborts.

**Root cause (analysis):**
- The `NoSuchMethodError` indicates a **binary incompatibility** between the version of `org.openrewrite.java` (core JavaType model) used to compile the Spring Boot/Security recipes and the version present at runtime.
- The combination of `rewrite-maven-plugin:6.22.0` with the selected recipe bundle versions (`rewrite-spring:5.21.1`, `rewrite-migrate-java:3.22.0`, `rewrite-java-dependencies:1.45.0`) results in multiple or mismatched `rewrite-java` versions on the plugin classpath.
- This is external to the demo application; it is a library-version alignment issue inside the OpenRewrite stack.

**Why this means the guide is not bulletproof:**
- The guide assumes that using the documented plugin + recipe configuration will produce a successful dry run and a `rewrite.patch` file.
- In practice, a reader who uses the same (or similar) version set can hit a hard runtime failure before any patch is produced.
- There is no troubleshooting note in the guide covering this class of OpenRewrite version incompatibility.

**Proposed fixes to the guide:**

1. **Pin and publish a known-good OpenRewrite version matrix**

   Instead of loosely specifying plugin and recipe versions, publish a tested combination, e.g.:

   ```xml
   <plugin>
       <groupId>org.openrewrite.maven</groupId>
       <artifactId>rewrite-maven-plugin</artifactId>
       <version>X.Y.Z</version>
       <configuration>
           <activeRecipes>
               <recipe>org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0</recipe>
           </activeRecipes>
           <recipeArtifactCoordinates>
               org.openrewrite.recipe:rewrite-migrate-java:A.B.C,
               org.openrewrite.recipe:rewrite-spring:D.E.F,
               org.openrewrite.recipe:rewrite-java-dependencies:G.H.I
           </recipeArtifactCoordinates>
       </configuration>
   </plugin>
   ```

   And explicitly state in the guide:

   > These exact versions have been tested together against the baseline `SecurityConfig` in this repo. If you change any of them, consult the OpenRewrite release notes and be prepared to adjust versions if you encounter `NoSuchMethodError` or other linkage problems.

2. **Prefer `recipeArtifactCoordinates` over direct plugin `<dependencies>` in the guide examples**

   Update Section 3 to recommend `recipeArtifactCoordinates` instead of `<dependencies>` for recipe bundles, aligning with the official OpenRewrite documentation. This reduces the risk of accidentally pulling conflicting core libraries onto the plugin classpath.

3. **Add a troubleshooting subsection for OpenRewrite runtime linkage errors**

   Under Section 4 (Dry-Run), add a troubleshooting note:

   > **Problem:** `mvn rewrite:dryRun` fails with `NoSuchMethodError` mentioning `org.openrewrite.java.tree.JavaType$Method` or other OpenRewrite internals.  
   > **Cause:** Incompatible versions of the OpenRewrite core and recipe bundles are on the plugin classpath.  
   > **Resolution:**
   > - Align `rewrite-maven-plugin` and recipe bundle versions according to the official OpenRewrite guidance.
   > - Use the known-good version matrix documented earlier in this guide.
   > - If issues persist, temporarily skip the automated SecurityConfig migration and proceed with manual refactoring (Section 6), using the code examples as a reference.

**Optional project-side mitigation (code change suggestion):**

- Reconfigure the plugin to use `recipeArtifactCoordinates` instead of `<dependencies>` and, if necessary, update to a plugin version / recipe bundle set that is confirmed to work with this project (once identified).  
- Keep this configuration minimal and well-documented so it can be easily updated when OpenRewrite publishes new breaking changes.

---

## Issue 4 – DependencyVulnerabilityCheck recipe removed/renamed in newer OpenRewrite versions

**Guide section:** Section 3.2 – Configure active recipes and security checks

**What the guide says:**
- Configure `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0` as an active recipe.
- Configure `org.openrewrite.java.dependencies.DependencyVulnerabilityCheck` as an active recipe with `<maximumUpgradeDelta>PATCH</maximumUpgradeDelta>`.

**What actually happened:**
- With `rewrite-maven-plugin:6.24.0` and modern recipe bundles resolved via `recipeArtifactCoordinates`, running:
  ```bash
  mvn rewrite:run
  ```
  failed immediately with:
  ```
  Recipe(s) not found: org.openrewrite.java.dependencies.DependencyVulnerabilityCheck
  Did you mean: org.openrewrite.java.dependencies.DependencyInsight
  ```
- As long as `DependencyVulnerabilityCheck` remains in `<activeRecipes>`, the migration cannot be applied; `rewrite:run` aborts before modifying any files.

**Root cause (analysis):**
- In the current OpenRewrite recipe bundles, `org.openrewrite.java.dependencies.DependencyVulnerabilityCheck` no longer exists under that ID (it has been removed or renamed).
- The guide hardcodes this recipe name, which is **version-sensitive** and not stable across releases.

**Why this means the guide is not bulletproof:**
- Following the guide literally with up-to-date OpenRewrite versions prevents `rewrite:run` from completing, blocking the entire automated migration.
- The guide does not mention that the dependency vulnerability recipe is optional (since OWASP Dependency-Check already handles SCA) or provide a fallback when the recipe is missing.

**Proposed fixes to the guide:**

1. **Separate structural migration from dependency vulnerability checks**

   - Make `UpgradeSpringBoot_3_0` the only required active recipe for the structural migration in this guide.
   - Treat any dependency-vulnerability-related recipes as **optional enhancements**, e.g.:

   > Optionally, if you are using an OpenRewrite recipe bundle that still provides `org.openrewrite.java.dependencies.DependencyVulnerabilityCheck`, you can add it as an additional active recipe. If the recipe is missing or renamed in your version, skip it and rely on OWASP Dependency-Check for SCA.

2. **Do not hardcode `DependencyVulnerabilityCheck` in the main configuration example**

   - In Section 3.2, adjust the main example to only include:

   ```xml
   <activeRecipes>
       <recipe>org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0</recipe>
   </activeRecipes>
   ```

   - Move the vulnerability recipe discussion into a separate, clearly marked subsection that:
     - Explains that recipe IDs are version-dependent.
     - Instructs readers to consult the OpenRewrite documentation or `rewrite:discover` output for the appropriate recipe name in their environment.

3. **Clarify that OWASP Dependency-Check remains the primary SCA mechanism**

   - Emphasize in the guide that OWASP Dependency-Check (Section 1–2) is the **authoritative** SCA tool in this blueprint, and that OpenRewrite’s dependency recipes are optional, best-effort helpers.

**Optional project-side mitigation (code change suggestion):**

- Keep the POM’s OpenRewrite `<activeRecipes>` limited to `UpgradeSpringBoot_3_0` and rely on OWASP Dependency-Check for vulnerability enforcement.
- If, in the future, a stable replacement recipe for `DependencyVulnerabilityCheck` is identified, add it back as an optional step with explicit version constraints.

---

New issues should be appended below following the same structure:

- **Issue N – Title**
- **Guide section**
- **What the guide says**
- **What actually happened**
- **Root cause**
- **Why this breaks bulletproof-ness**
- **Proposed fixes to the guide**
- **Optional project-side mitigation**
