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

New issues should be appended below following the same structure:

- **Issue N – Title**
- **Guide section**
- **What the guide says**
- **What actually happened**
- **Root cause**
- **Why this breaks bulletproof-ness**
- **Proposed fixes to the guide**
- **Optional project-side mitigation**
