# Security Guide – OWASP SCA & Dependency Management

> Brazilian Portuguese version: this guide must have a translated counterpart in `GUIDE_SECURITY_pt-BR.md`.

## 0. Overview & Objectives

This guide focuses on **Software Composition Analysis (SCA)** using **OWASP Dependency-Check** and related tools. It explains:
- How to configure and run scans.
- How to interpret reports.
- How to manage false positives responsibly.
- How to integrate SCA into CI/CD and compliance workflows.

Each section includes:
- **Command** (when applicable).
- **Expected result** (reports, file changes, or decisions).

---

## Section 1 – NVD API Key: Benefits and Setup

### Step 1.1 – Why an NVD API key matters
- **What you describe:**
  - Rate limiting and performance benefits of using an API key.
  - More reliable and faster vulnerability database synchronization.
- **Expected result:**
  - Readers understand that an API key is strongly recommended for CI/CD.

### Step 1.2 – Requesting an API key
- **What you document:**
  - URL for requesting an NVD API key.
  - Expected email/approval process at a high level.
- **Expected result:**
  - Readers know where and how to obtain an API key.

### Step 1.3 – Configuring the API key
- **What you show:**
  - Example environment variable configuration (e.g., `NVD_API_KEY`).
  - How the key is referenced in `pom.xml` through `${env.NVD_API_KEY}`.
- **Expected result:**
  - When the user runs a scan, Dependency-Check connects successfully to NVD using the key.

---

## Section 2 – CVSS Scoring and Threshold Configuration

### Step 2.1 – Explain CVSS basics
- **What you describe:**
  - Severity levels (Critical, High, Medium, Low).
  - Typical CVSS score ranges.
- **Expected result:**
  - Readers understand how scores map to risk.

### Step 2.2 – Configure failBuildOnCVSS
- **What you show:**
  - Example configuration: `<failBuildOnCVSS>7</failBuildOnCVSS>`.
- **Expected result:**
  - Builds fail when vulnerabilities with CVSS ≥ 7 are detected (unless suppressed).

### Step 2.3 – Define policy in this project
- **What you record:**
  - Project-specific rules (e.g., no net-new Critical/High CVEs after migration).
- **Expected result:**
  - Clear, documented security gate for all contributors.

---

## Section 3 – Interpreting dependency-check-report.html

### Step 3.1 – Run a scan and open the report
- **Command:**
  - `mvn dependency-check:check`
- **Expected result:**
  - HTML report available under `target/` that can be opened in a browser.

### Step 3.2 – Understand key sections of the report
- **What you explain:**
  - Summary view (counts by severity).
  - Dependency list with associated CVEs.
  - Individual CVE details (description, CVSS score, references).
- **Expected result:**
  - Readers can navigate the report and identify high-risk components.

---

## Section 4 – Suppression File Management

### Step 4.1 – Purpose of dependency-suppression.xml
- **What you describe:**
  - When and why to suppress a vulnerability (e.g., proven false positive, non-exploitable in context).
- **Expected result:**
  - Readers understand suppression is an exception, not a default.

### Step 4.2 – Creating and editing the suppression file
- **What you show:**
  - Example of adding a suppression entry.
  - Referencing the file from the Dependency-Check plugin.
- **Expected result (code):**
  - `dependency-suppression.xml` exists and is under version control.

### Step 4.3 – Documenting rationale for each suppression
- **What you require:**
  - Each suppression entry must have a comment or field explaining **why** it is safe.
- **Expected result:**
  - Auditable trace of suppression decisions.

### Step 4.4 – Periodic review and cleanup
- **What you recommend:**
  - Regular audits to remove obsolete suppressions.
- **Expected result:**
  - The file remains small, current, and meaningful.

---

## Section 5 – H2 Database Cache for NVD Data

### Step 5.1 – Why caching matters
- **What you explain:**
  - Local NVD cache improves scan performance and avoids repeated large downloads.
- **Expected result:**
  - Readers see the benefit of persistent NVD data, especially in CI.

### Step 5.2 – Configuring H2 cache
- **What you describe:**
  - High-level configuration options in Dependency-Check for using H2 as a cache.
- **Expected result:**
  - Repeated scans run faster once cache is populated.

---

## Section 6 – CI/CD Integration Patterns (Gated Checks)

### Step 6.1 – Adding SCA to the build pipeline
- **What you show:**
  - Example CI job that runs `mvn dependency-check:check`.
- **Expected result:**
  - Pipelines fail when new high-severity vulnerabilities appear.

### Step 6.2 – Handling failures in CI
- **What you describe:**
  - Recommended triage workflow when the pipeline fails (review report, decide to fix vs. suppress vs. accept risk).
- **Expected result:**
  - Teams have a clear response playbook.

---

## Section 7 – Reachability Analysis with OWASP Dep-Scan

### Step 7.1 – Purpose of reachability analysis
- **What you explain:**
  - Difference between "present" vulnerabilities and actually **reachable/exploitable** ones.
- **Expected result:**
  - Readers understand why reachability matters for prioritization.

### Step 7.2 – Running Dep-Scan (high-level)
- **What you show:**
  - Example of invoking Dep-Scan (or similar tool) against the project.
- **Expected result:**
  - Additional report identifying which CVEs are likely exploitable.

---

## Section 8 – Compliance Artifact Workflows

### Step 8.1 – Generating and using SBOM
- **What you explain:**
  - How SBOMs relate to supply chain visibility.
- **Expected result:**
  - Teams know when and how to regenerate SBOMs (e.g., on each release).

### Step 8.2 – Creating VDR and VEX documents
- **What you describe:**
  - How vulnerability findings are converted into VDR and then refined by VEX to mark non-exploitable issues.
- **Expected result:**
  - Clear flow from scan → report → risk decision → documented artifact.

### Step 8.3 – Real-world CVE remediation examples
- **What you provide:**
  - At least one example where a vulnerable dependency was:
    - Upgraded.
    - Suppressed with rationale.
    - Left as-is with strong justification and compensating controls.
- **Expected result:**
  - Readers see concrete, realistic remediation patterns.
