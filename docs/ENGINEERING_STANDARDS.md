# Engineering Coding Standards & Git Workflow Guidelines

> **Scope:** Applies to all contributors on the Gilded Rose Inventory API project.
> All new code, PRs, and branches must comply with these standards before merging.

---

## Table of Contents

1. [General Coding Standards](#1-general-coding-standards)
2. [Unit Testing Standards](#2-unit-testing-standards)
3. [Branching Strategy](#3-branching-strategy)
4. [Commit Message Standards](#4-commit-message-standards)
5. [Pull Request Guidelines](#5-pull-request-guidelines)
6. [Pull Request Review Standards](#6-pull-request-review-standards)
7. [CI/CD Quality Gates](#7-cicd-quality-gates)
8. [IDE Static Analysis Setup](#8-ide-static-analysis-setup)
9. [SonarQube Quality Gate Policy](#9-sonarqube-quality-gate-policy)
10. [Fortify Security Scanning](#10-fortify-security-scanning)
11. [Technical Debt Management](#11-technical-debt-management)
12. [Dependency Management](#12-dependency-management)
13. [Security Practices](#13-security-practices)
14. [Code Review Etiquette](#14-code-review-etiquette)
15. [Definition of Done](#15-definition-of-done)

---

## 1. General Coding Standards

### 1.1 Code Readability

- **Write self-documenting code.** Variable, method, and class names must clearly
  express intent without requiring a comment to explain them.

  ```java
  // WRONG — what does 'd' mean?
  int d = item.sellIn - 1;

  // CORRECT — intent is clear
  int updatedSellIn = item.sellIn - 1;
  ```

- **Single Responsibility Principle (SRP).** Every class and method has exactly one
  reason to change. In this project:
  - `GildedRoseController` — HTTP translation only
  - `GildedRoseInventoryService` — orchestration only
  - `AgedBrieUpdateStrategy` — Aged Brie quality rules only

- **Avoid deep nesting (> 3 levels).** Refactor using guard clauses or helper methods.

  ```java
  // WRONG — deeply nested
  public void updateSingleItem(Item item) {
      if (item != null) {
          if (item.name != null) {
              if (!item.name.isBlank()) {
                  // actual logic
              }
          }
      }
  }

  // CORRECT — guard clauses exit early
  public void updateSingleItem(Item item) {
      if (item == null) throw new InvalidItemException("Item must not be null");
      if (item.name == null || item.name.isBlank()) throw new InvalidItemException("...");
      // actual logic
  }
  ```

- **Prefer composition over inheritance** unless inheritance models a true
  is-a relationship. Strategies use inheritance only where the Template Method
  skeleton is genuinely shared.

- **Limit method length:**
  - Recommended: ≤ 30 lines
  - Absolute maximum: ≤ 50 lines

- **No magic numbers or magic strings.** Every business-meaningful literal belongs in
  `constants/QualityConstants.java` or `constants/ItemNames.java`.

  ```java
  // WRONG
  if (item.quality > 50) { ... }
  if (item.name.equals("Sulfuras, Hand of Ragnaros")) { ... }

  // CORRECT
  if (item.quality > QualityConstants.MAX_QUALITY) { ... }
  if (item.name.equals(ItemNames.SULFURAS)) { ... }
  ```

- **Code must compile and build without warnings.**

### 1.2 Naming Conventions

| Construct | Convention | Example |
|---|---|---|
| Variables / parameters | `camelCase` | `itemName`, `sellIn` |
| Methods | `camelCase` verb phrase | `updateQuality()`, `getStrategy()` |
| Classes / interfaces | `PascalCase` | `GildedRoseController`, `ItemUpdateStrategy` |
| Constants | `UPPER_SNAKE_CASE` | `MAX_QUALITY`, `AGED_BRIE` |
| Packages | `lowercase.dotted` | `com.vinods.gildedrose.domain.strategy` |
| Test methods | `methodName_condition_expectedResult` | `updateQuality_decreasesQualityByOne_forNormalItem` |

For full naming rules see [CODING_CONVENTIONS.md — Section 2](CODING_CONVENTIONS.md).

### 1.3 Code Structure

- Organise code by **domain concern**, not by technical layer where possible.
  All inventory-update behaviour lives under `domain/strategy/` regardless of how
  many classes it spans.
- Enforce the **hexagonal dependency rule**: `adapter → application → domain`.
  No class in `domain/` or `application/` may import from `adapter/`.
- Avoid circular dependencies between packages.
- Each module exposes a clear public API through port interfaces
  (`application/port/InventoryUpdateService.java`).

### 1.4 Documentation

- All **public API methods** (controllers, port interfaces) must have Javadoc.
- Complex domain logic (e.g., `BackstagePassUpdateStrategy`) must include inline
  explanation comments for thresholds and branching decisions.
- Maintain `docs/` for architecture, flow, and convention documentation.
- Update docs as part of the same PR that changes the behaviour.

### 1.5 Error Handling

- **Never swallow exceptions silently.**

  ```java
  // WRONG
  try {
      service.updateInventory(items);
  } catch (Exception e) { /* ignored */ }

  // CORRECT — let GlobalExceptionHandler handle it, or rethrow with context
  service.updateInventory(items);
  ```

- Use structured logging instead of `System.out.println` or `e.printStackTrace()`.
- Provide meaningful, user-facing error messages in `InvalidItemException` and
  `ErrorResponse`. Do not expose stack traces in API responses.

### 1.6 Logging

Use SLF4J (`org.slf4j.Logger`) throughout.
Never use `System.out` or `java.util.logging`.

| Level | When to use | Example in this project |
|---|---|---|
| `ERROR` | System failure, requires immediate attention | Uncaught exception in `handleGenericException` |
| `WARN` | Bad input, expected-but-wrong state | Null item, blank item name, malformed JSON |
| `INFO` | Major business events, entry/exit of use case | "Received update-quality request for N item(s)" |
| `DEBUG` | Per-item detail; useful in development | "Updating item: 'name'", "Selected strategy 'X'" |

```java
// CORRECT — SLF4J placeholder syntax (lazy evaluation)
log.info("Starting inventory update for {} items", items.length);

// WRONG — string concatenation always evaluates even if log level is OFF
log.info("Starting inventory update for " + items.length + " items");
```

**Never log sensitive data** (passwords, tokens, personal information).
`ERROR` level calls must include the exception as the last argument to capture the
stack trace:

```java
log.error("Unexpected error at {}: {}", uri, ex.getMessage(), ex);
```

### 1.7 Code Quality Tools

Every repository must include and enforce the following tools. Configuration must be
committed to the repository so all developers use identical rules.

| Tool | Purpose | Where configured |
|---|---|---|
| **SonarLint** (IDE) | Real-time issue detection before commit | Developer IDE — mandatory setup |
| **SonarQube** (CI) | Centralised quality gate — code smells, bugs, coverage | CI pipeline + server |
| **Fortify** (CI) | Enterprise security scanning (SAST) | CI pipeline |
| **Checkstyle** | Java code style enforcement | `pom.xml` |
| **SpotBugs** | Bytecode-level bug pattern detection | `pom.xml` |
| **JaCoCo** | Test coverage measurement and reporting | `pom.xml` |
| **OWASP Dependency-Check** | Known CVE detection in dependencies | `pom.xml` / CI |

CI checks **must fail** if any lint, quality, or security rule is violated.

---

## 2. Unit Testing Standards

### 2.1 Coverage Targets

| Scope | Target |
|---|---|
| Overall unit test coverage | ≥ 80 % |
| Critical business logic (domain strategies, service) | ≥ 90 % |

Every new feature PR must include tests. A PR that adds production code without
corresponding tests will not be approved.

### 2.2 Test Quality Rules

- Follow the **AAA pattern** with explicit comments when the setup is non-trivial:

  ```java
  @Test
  void updateInventory_updatesSingleItem() {
      // Arrange
      Item[] items = {new Item("Normal Item", 10, 20)};

      // Act
      service.updateInventory(items);

      // Assert
      assertEquals(19, items[0].quality);
      assertEquals(9,  items[0].sellIn);
  }
  ```

- Tests must be:
  - **Deterministic** — same result on every run
  - **Isolated** — no shared mutable state between tests
  - **Repeatable** — no dependency on environment, clock, or network

- **Avoid testing implementation details.** Test observable behaviour (outputs, side
  effects, exceptions), not internal method calls.

- **Mock external dependencies.** In `@WebMvcTest`, always `@MockBean` the service
  and mapper so tests are fast and scoped to the web layer only.

### 2.3 Test Method Naming

```
methodName_condition_expectedResult
```

Examples from this project:

```java
updateQuality_decreasesQualityByOne_forNormalItem()
updateInventory_throwsInvalidItemException_forNullItem()
returns400_whenInvalidItemExceptionThrown()
getStrategy_returnsNormalStrategy_forUnknownItem()
```

### 2.4 Types of Tests Required

| Type | Tool | When required |
|---|---|---|
| Unit tests | JUnit 5 + Mockito | Every class |
| Web layer slice tests | `@WebMvcTest` + MockMvc | Every controller |
| Integration tests | `@SpringBootTest` | Full request flows (smoke) |
| Regression tests | JUnit 5 | After every bug fix |

### 2.5 Test Scope by Layer

| Layer | Spring context | What to mock |
|---|---|---|
| Domain strategies | None | Nothing — plain Java instantiation |
| Domain services (`QualityAdjuster`, `SellInAdjuster`) | None | Nothing |
| Application service | None | Nothing (use real `ItemUpdateStrategyFactory`) |
| Controller | `@WebMvcTest` (web slice only) | `InventoryUpdateService`, `ItemMapper` |

---

## 3. Branching Strategy

This project follows **GitFlow-lite**.

### 3.1 Branch Types

```
main
 └── develop
       ├── feature/*
       ├── bugfix/*
       └── release/*
hotfix/* (branches from main)
```

### 3.2 Branch Rules

| Branch | Branches from | Merges into | Direct commits |
|---|---|---|---|
| `main` | — | — | ❌ Never |
| `develop` | `main` | — | ❌ Never (PR only) |
| `feature/*` | `develop` | `develop` | ✅ Yes |
| `bugfix/*` | `develop` | `develop` | ✅ Yes |
| `release/*` | `develop` | `main` + `develop` | Bug fixes only |
| `hotfix/*` | `main` | `main` + `develop` | ✅ Yes |

### 3.3 Branch Naming

```
feature/conjured-item-strategy
feature/add-swagger-documentation
bugfix/backstage-pass-quality-cap
hotfix/null-pointer-in-quality-update
release/v1.2.0
```

- Use lowercase with hyphens.
- Include the ticket number where applicable: `feature/JIRA-42-conjured-items`.
- Keep names concise but descriptive.

---

## 4. Commit Message Standards

Follow the **Conventional Commits** specification.

### 4.1 Format

```
<type>(<scope>): <short description>

[optional body]

[optional footer: Co-Authored-By, Closes #issue]
```

### 4.2 Types

| Type | Purpose | Example |
|---|---|---|
| `feat` | New feature | `feat(strategy): add ConjuredItemUpdateStrategy` |
| `fix` | Bug fix | `fix(quality): cap quality at 50 for aged brie` |
| `refactor` | Code restructuring (no behaviour change) | `refactor(factory): extract createStrategies helper` |
| `test` | Test additions or fixes | `test(controller): add 400 handler tests` |
| `docs` | Documentation only | `docs(arch): add hexagonal architecture diagram` |
| `style` | Formatting, no logic change | `style(service): reorder imports` |
| `chore` | Build, deps, tooling | `chore(pom): upgrade mapstruct to 1.6.0` |
| `perf` | Performance improvement | `perf(factory): cache strategy lookup` |

### 4.3 Rules

- **Atomic commits:** one logical change per commit.
- Write in **present tense, imperative mood**: "add feature" not "added feature".
- Keep the subject line ≤ 72 characters.
- Use the body to explain *why*, not *what* (the diff already shows what).

```
# WRONG — vague
fix stuff
update
wip

# CORRECT — specific and atomic
fix(strategy): prevent quality exceeding 50 for backstage passes

Backstage pass logic was not applying the MAX_QUALITY cap when
the base increase pushed quality above 50 in the ≤5 day window.
Added QualityAdjuster.increaseQuality() call to enforce the cap.

Closes #47
```

---

## 5. Pull Request Guidelines

### 5.1 Developer Pre-Submission Checklist

Every developer must verify **all** of the following before raising a PR.
A PR must not be opened until every item below is ticked.

#### Tests

- [ ] **Unit tests written for all newly introduced logic.**
  Every new method, branch, and class must have a corresponding test.
- [ ] **Existing tests still pass.** No previously passing test may be broken
  by the change (`./mvnw test` — zero failures).
- [ ] **Coverage is 100 % for newly added or modified code.**
  All new lines, branches, and edge cases — including error paths — must be
  covered by automated tests.
- [ ] **Tests executed locally with coverage enabled.**
  Run `./mvnw test` and review the JaCoCo report before pushing.
  Verify that coverage reports meet the required thresholds.

#### Code Quality

- [ ] **No new SonarQube / SonarLint violations introduced.**
  SonarLint in the IDE must show no critical or blocker issues on changed files.
  Fix all issues detected before committing — not after the PR is opened.
- [ ] **Static analysis issues resolved.**
  No new code smells, bugs, or vulnerabilities flagged by Checkstyle, SpotBugs,
  or SonarLint remain outstanding.
- [ ] **Code is properly formatted** and all lint checks pass locally.
- [ ] **No commented-out code or debug statements** in the diff.

#### Commits & Branch

- [ ] **Commit messages follow Conventional Commits format.**
  Each commit message must clearly describe the purpose of the change.

  ```
  type(scope): short description

  feat(payment): add retry mechanism for payment service
  fix(auth): resolve token validation bug
  ```

- [ ] **Targets the correct branch** (usually `develop`; hotfixes target `main`).

#### Documentation & Pipeline

- [ ] **Documentation updated** if behaviour, architecture, or API contracts changed.
- [ ] **All CI pipeline checks are expected to pass.** The developer must have
  verified locally that no gate will fail before opening the PR.

### 5.2 PR Size

| Metric | Target |
|---|---|
| Lines changed | < 400 |
| Files changed | < 15 |
| Commits | ≤ 5 focused commits |

Large PRs must be split into smaller, independently-reviewable units. If a feature
is too large for one PR, use a feature branch as the target and submit sub-PRs against it.

### 5.3 PR Description Template

```markdown
## Problem
<!-- What problem does this PR solve? Link to ticket if applicable. -->

## Solution
<!-- How does this implementation solve it? -->

## Changes
<!-- Bullet list of key changes -->
- Added `ConjuredItemUpdateStrategy` implementing 2× degradation
- Registered strategy in `ItemUpdateStrategyFactory`
- Added constant `ItemNames.CONJURED_PREFIX`

## Tests
<!-- What was tested? What was not tested and why? -->
- Unit tests for `ConjuredItemUpdateStrategy` (8 test cases)
- Edge cases: quality at 0, quality at 50, before/after sell date

## Pre-Submission Checklist Confirmation
<!-- Confirm every item is ticked before opening the PR -->
- [ ] All unit tests written and passing locally
- [ ] Coverage 100 % on new/modified code — JaCoCo report verified
- [ ] SonarLint: no critical or blocker issues on changed files
- [ ] Static analysis (Checkstyle / SpotBugs): no new violations
- [ ] Commit messages follow Conventional Commits format
- [ ] Code formatted; lint checks pass locally
- [ ] All CI pipeline checks expected to pass

## Coverage Numbers
<!-- Paste actual numbers from local JaCoCo run -->
- New/modified code coverage: XX %
- Domain strategies: XX % | Application service: XX %

## Screenshots
<!-- If UI or Swagger changes, include screenshots -->

## Related
<!-- Ticket / issue number -->
Closes JIRA-99
```

---

## 6. Pull Request Review Standards

### 6.1 Approval Requirements

- Minimum **1 approval** required before merging.
- For changes touching domain business logic: **2 approvals**.
- The author must not self-approve.

### 6.2 Review Checklist

Reviewers verify each of the following before approving:

#### Code Quality
- [ ] Code is readable; names clearly express intent
- [ ] Naming conventions followed (see Section 1.2)
- [ ] No unnecessary complexity or over-engineering
- [ ] Method length within limits (≤ 30 lines recommended)
- [ ] No magic numbers or magic strings
- [ ] SonarLint / SonarQube gate passes — no new blockers or critical issues

#### Architecture
- [ ] New class is in the correct package/layer
- [ ] Dependency direction respected (`adapter → application → domain`)
- [ ] No Spring annotations in domain or application classes
- [ ] No business logic in the adapter layer

#### Testing
- [ ] All necessary unit tests are present and **meaningful** (not just coverage-filling stubs)
- [ ] Coverage requirement met — **100 % on new/modified code**; ≥ 80 % overall, ≥ 90 % domain
- [ ] Edge cases covered (null, empty, boundary values, error paths)
- [ ] Tests are deterministic and isolated — no shared mutable state

#### Security
- [ ] No secrets, tokens, or passwords in code or logs
- [ ] User input validated at the application service boundary
- [ ] No internal stack traces exposed in API responses
- [ ] Fortify scan passes — no unresolved critical or high vulnerabilities

#### Performance
- [ ] No unnecessary loops, repeated DB/API calls, or eager loading
- [ ] No blocking calls on the main thread
- [ ] Efficient use of collections and streams

### 6.3 Reviewer Responsibilities

During the PR review process, reviewers must verify that the developer's
pre-submission checklist (Section 5.1) has been satisfied. Specifically:

- [ ] All necessary unit tests are present and meaningful.
- [ ] Coverage requirements are met for new/modified code (100 % on new code).
- [ ] No new SonarQube violations are introduced.
- [ ] Code follows coding standards and best practices defined in this document.
- [ ] Commit messages follow the Conventional Commits format.
- [ ] CI pipeline checks pass successfully.

> **If any of the above checks are not satisfied, reviewers must request changes
> before approving. A PR must not be approved with outstanding checklist failures.**

Additional reviewer responsibilities:

- Complete review within **24 hours** of PR assignment.
- Be **constructive and specific**: point to the line, explain why it matters,
  suggest an alternative.
- Distinguish between **blocking** (must fix) and **non-blocking** (suggestion)
  comments using labels:
  - `[blocking]` — must be resolved before merge
  - `[nit]` — minor style preference, author's discretion
  - `[question]` — seeking clarification, not requesting a change

- Do not block a PR for purely stylistic preferences that are not covered by a
  written convention.

---

## 7. CI/CD Quality Gates

The CI pipeline enforces the following gates in order. **All must pass** before a PR
can be merged.

```
┌──────────────────────────────────────────────────────┐
│  1. Lint & Style                                      │
│     Checkstyle, import order, formatting              │
├──────────────────────────────────────────────────────┤
│  2. Build                                             │
│     ./mvnw clean compile  —  zero warnings            │
├──────────────────────────────────────────────────────┤
│  3. Unit Tests                                        │
│     ./mvnw test                                      │
├──────────────────────────────────────────────────────┤
│  4. Coverage Threshold (JaCoCo)                       │
│     ≥ 80 % overall  |  ≥ 90 % domain/critical logic  │
├──────────────────────────────────────────────────────┤
│  5. SonarQube Quality Gate                            │
│     Code smells · Bug detection · Coverage validation │
│     Maintainability rating · No new blockers/critical │
├──────────────────────────────────────────────────────┤
│  6. Fortify Security Scan (SAST)                      │
│     SQL injection · XSS · Insecure data handling      │
│     Auth vulnerabilities · Insecure API usage         │
│     No unresolved critical or high severity findings  │
├──────────────────────────────────────────────────────┤
│  7. OWASP Dependency-Check                            │
│     CVE scan on all declared dependencies             │
│     Fails on CVSS ≥ 7.0                              │
└──────────────────────────────────────────────────────┘
```

A PR **cannot be merged** if any gate fails.

| Gate | Tool | Failure condition |
|---|---|---|
| Code Quality | SonarQube | New blocker or critical issue introduced |
| Security Vulnerabilities | Fortify | Critical or high severity finding unresolved |
| Unit Test Coverage | JaCoCo | Below threshold |
| Dependency CVEs | OWASP Dependency-Check | CVSS ≥ 7.0 |
| Linting | Checkstyle / SpotBugs | Any rule violation |
| Build | Maven | Compilation error or warning |

---

## 8. IDE Static Analysis Setup

> **This section is mandatory for all engineers.**
> Static analysis issues must be resolved during development, not discovered at PR time.

### 8.1 Required Tool: SonarLint

Every developer must install and configure **SonarLint** in their IDE.

| IDE | Installation |
|---|---|
| IntelliJ IDEA | Settings → Plugins → Marketplace → search "SonarLint" |
| Eclipse | Help → Eclipse Marketplace → search "SonarLint" |
| VS Code | Extensions → search "SonarLint" |
| Visual Studio | Extensions → Manage Extensions → search "SonarLint" |

### 8.2 SonarQube Server Connection (if applicable)

If the team runs a shared SonarQube server:

1. Open SonarLint settings in the IDE.
2. Add a new SonarQube connection using the server URL and an access token.
3. Bind the local project to the corresponding SonarQube project.
4. Synchronise rules so local analysis matches the server's quality profile.

This ensures that the rules checked locally are identical to those enforced in CI.

### 8.3 Developer Responsibilities

| Responsibility | Standard |
|---|---|
| Install SonarLint | Mandatory before first commit |
| Connect to SonarQube server | Required if server is available |
| Resolve SonarLint issues | **Before committing** — not at PR review time |
| Follow SonarLint recommendations | Treat suggestions as team coding standards |
| Keep plugin updated | Update SonarLint within one week of a new release |

### 8.4 Workflow

```
Write code
    │
    ▼
SonarLint highlights issue in IDE in real time
    │
    ▼
Developer fixes the issue immediately
    │
    ▼
Commit — code is clean before it enters the repository
    │
    ▼
CI SonarQube gate validates (should pass with no surprises)
```

---

## 9. SonarQube Quality Gate Policy

All repositories are integrated with SonarQube for centralised, automated code analysis.

### 9.1 What SonarQube Checks

| Check | Description |
|---|---|
| **Code smells** | Maintainability issues — duplication, long methods, dead code |
| **Bug detection** | Logic errors, null dereference risk, resource leaks |
| **Security vulnerabilities** | OWASP Top 10, injection risks, insecure configurations |
| **Coverage validation** | Enforces the JaCoCo coverage thresholds from Section 2.1 |
| **Maintainability rating** | A–E rating; new code must not drop the project below the agreed rating |
| **Duplication** | Code clone detection across the codebase |

### 9.2 Quality Gate Rules

A build **automatically fails** if any of the following are introduced on new code:

- Any new **Blocker** or **Critical** issue
- Coverage on new code drops below the defined threshold
- Any new **Security Vulnerability** of any severity
- Maintainability rating for new code is below **A**
- Code duplication on new code exceeds the defined percentage

### 9.3 Developer Obligations

- Fix all SonarQube issues **before the PR is opened**, not after review begins.
- Do not mark issues as "Won't Fix" or "False Positive" without a team discussion
  and written justification in the SonarQube issue comment.
- New **technical debt** introduced intentionally must be logged and scheduled for
  resolution (see Section 11).

---

## 10. Fortify Security Scanning

All code must pass a security scan using **OpenText Fortify** (SAST — Static Application
Security Testing) before it can be merged.

### 10.1 What Fortify Detects

| Vulnerability Category | Examples |
|---|---|
| **Injection** | SQL injection, command injection, LDAP injection |
| **Cross-site scripting (XSS)** | Reflected, stored, and DOM-based XSS |
| **Insecure data handling** | Plaintext secrets, unencrypted sensitive fields |
| **Authentication vulnerabilities** | Weak credential handling, session fixation |
| **Insecure API usage** | Unsafe deserialization, missing input validation |
| **Security misconfigurations** | Debug mode in production, default credentials |

### 10.2 Pipeline Integration

Fortify runs automatically in the CI/CD pipeline as Gate 6 (see Section 7).
The scan analyses all source code and reports findings categorised by severity:

| Severity | Action required |
|---|---|
| **Critical** | ❌ PR blocked — must be fixed before merge |
| **High** | ❌ PR blocked — must be fixed before merge |
| **Medium** | ⚠️ Must be reviewed; remediation required within the sprint |
| **Low** | ℹ️ Logged; addressed in next available sprint |

### 10.3 Developer Responsibilities

- Developers must **not introduce new vulnerabilities**. Fortify findings that did
  not exist before the PR are the author's responsibility to fix.
- No PR may be merged with an unresolved Critical or High finding.
- Medium and Low findings must be triaged and ticketed within the sprint.
- If a finding is a confirmed false positive, it must be marked as such in the
  Fortify issue tracker with a written justification. This requires a second engineer
  to confirm.

---

## 11. Technical Debt Management

SonarQube is the authoritative source for tracking technical debt in this project.

### 11.1 Rules

| Rule | Detail |
|---|---|
| No new Blocker or Critical issues | Enforced automatically by the SonarQube quality gate |
| New code must meet the quality gate | All new files and methods are assessed independently |
| Existing debt must be prioritised | All logged debt items must have a sprint assignment |
| Sprint debt target | Technical debt items should be addressed within the sprint they are created, where possible |

### 11.2 Debt Triage Process

1. SonarQube raises a new issue on the main branch.
2. The team reviews the issue in the next sprint planning.
3. The issue is assigned a priority (`Blocker` → `Critical` → `Major` → `Minor`).
4. A ticket is created and placed in the backlog with a target sprint.
5. When resolved, the fix is verified by re-running the SonarQube analysis.

### 11.3 What Does Not Constitute Acceptable Debt

- Skipping tests to hit a deadline.
- Disabling SonarQube or Fortify rules without team approval.
- Marking genuine issues as "Won't Fix" to make the gate pass.
- Leaving TODO comments in production code without a corresponding ticket.

---

## 12. Dependency Management

- **Avoid unnecessary dependencies.** Every new library must be justified by a clear
  need that cannot be met by the existing stack.
- **Version management:** Prefer Spring Boot's managed versions (via `spring-boot-starter-parent`)
  over explicit versions where possible.
- Review and update dependencies as part of regular maintenance (`chore` commits).
- **Critical vulnerabilities** (CVSS ≥ 7.0) must be patched within 48 hours of discovery.
- Annotation processors (e.g., MapStruct) must be declared in
  `<annotationProcessorPaths>`, not as regular compile dependencies.

---

## 13. Security Practices

- **Never commit secrets.** No API keys, database passwords, tokens, or credentials
  in source code or committed files. Use environment variables or a secrets manager.
- Add `.env`, `*.key`, `*.pem` to `.gitignore`.
- **Validate all user input** at the application service boundary before it reaches
  the domain. In this project, validation lives in `GildedRoseInventoryService.updateSingleItem()`.
- Do not log request bodies at INFO level in production. Use DEBUG.
- Never expose internal stack traces in API error responses — return only `ErrorResponse`
  with a user-safe message.
- Run Fortify (SAST) and OWASP Dependency-Check as part of CI to detect both
  code-level vulnerabilities and known CVEs in third-party libraries.

---

## 14. Code Review Etiquette

### Author Responsibilities

| Responsibility | Guideline |
|---|---|
| PR description | Clear summary, problem, solution, test coverage, SonarLint status |
| PR size | Keep it small and focused; split if > 400 lines |
| Response time | Respond to review comments within **24 hours** |
| Tone | Accept feedback professionally; ask for clarification if unclear |
| Incomplete work | Use draft PRs for in-progress work; never open a ready PR with TODO items |

### Reviewer Responsibilities

| Responsibility | Guideline |
|---|---|
| Review time | Complete within **24 hours** |
| Tone | Be respectful and constructive at all times |
| Feedback | Be specific — reference the line, explain the issue, suggest a fix |
| Blocking vs. non-blocking | Clearly label `[blocking]` vs. `[nit]` vs. `[question]` |
| Approvals | Only approve code you would be comfortable maintaining |
| Focus | Review the code; do not rewrite the whole feature in comments |

### Examples

```
# WRONG — vague and unhelpful
"This is bad."
"I don't like this."

# CORRECT — specific and actionable
[blocking] This method is 62 lines. Per our standards, the max is 50.
Consider extracting the validation block (lines 23–41) into a private
`validateItem(item)` method to bring it under the limit.

[nit] Minor preference: I'd use `item.name.isBlank()` instead of
`item.name.isEmpty() || item.name.trim().isEmpty()` — they're equivalent
but `isBlank()` is more readable. Up to you.

[blocking] Fortify flagged potential SQL injection at this line. Input
must be sanitised or parameterised before being passed to the query.
```

---

## 15. Definition of Done

A task or story is **Done** only when **all** of the following are true:

| # | Criterion | Verification |
|---|---|---|
| 1 | ✅ Code implemented | Feature works as specified |
| 2 | ✅ Unit tests written for all new logic | Every new method and branch has a corresponding test |
| 3 | ✅ Coverage is 100 % on new/modified code | All new lines, branches, and error paths covered |
| 4 | ✅ Tests run locally with coverage enabled | JaCoCo report reviewed; overall ≥ 80 %, domain ≥ 90 % |
| 5 | ✅ SonarLint clean | No critical or blocker issues in IDE before committing |
| 6 | ✅ SonarQube quality gate passes | No new blockers, criticals, or security vulnerabilities |
| 7 | ✅ Fortify security scan passes | No unresolved critical or high severity findings |
| 8 | ✅ Developer pre-submission checklist complete | All items in Section 5.1 ticked before PR was opened |
| 9 | ✅ Code reviewed | Minimum approvals received; reviewer checklist (Section 6.3) satisfied; all `[blocking]` comments resolved |
| 10 | ✅ CI pipeline passes | All 7 quality gates green |
| 11 | ✅ Documentation updated | `docs/` updated if architecture, flow, or conventions changed; Javadoc added for new public APIs |
| 12 | ✅ Merged into correct branch | `develop` for features/bugfixes; `main` only via release or hotfix |
| 13 | ✅ Branch deleted | Feature/bugfix branch removed after merge |

### Definition of Clean Code

Code is considered **clean and merge-ready** when all of the following conditions are satisfied:

```
┌──────────────────────────────────────────────────────────────────┐
│  CLEAN CODE DEFINITION                                           │
│                                                                  │
│  Developer pre-submission checklist (Section 5.1) complete ✅    │
│                                                                  │
│  ✅  Tests       — all new logic covered; coverage 100 % on     │
│                    new/modified code; run locally before push    │
│  ✅  SonarLint   — no critical or blocker issues in IDE         │
│  ✅  SonarQube   — quality gate passes; no new violations       │
│  ✅  Fortify     — security scan passes; no critical/high       │
│  ✅  CI pipeline — all 7 gates green                            │
│                                                                  │
│  + Reviewer checklist (Section 6.3) verified by approver(s)    │
│  + Required approvals obtained                                  │
│                                                                  │
│  A PR cannot be merged until every line above is satisfied.     │
└──────────────────────────────────────────────────────────────────┘
```

---

## Appendix — Quick Reference

### Branch Cheat Sheet

```
New feature     →  git checkout -b feature/<name> develop
Bug fix         →  git checkout -b bugfix/<name> develop
Production fix  →  git checkout -b hotfix/<name> main
Release prep    →  git checkout -b release/vX.Y.Z develop
```

### Commit Type Cheat Sheet

```
feat     — new feature
fix      — bug fix
refactor — restructuring, no behaviour change
test     — tests only
docs     — documentation only
style    — formatting only
chore    — build / dependencies / tooling
perf     — performance
```

### PR Merge Conditions

```
── Developer must verify before opening PR ──────────────────
✅ All unit tests written for new logic
✅ Coverage 100 % on new/modified code — JaCoCo report checked
✅ Full test suite passes locally (./mvnw test)
✅ SonarLint: no critical/blocker issues in IDE
✅ Static analysis: no new violations
✅ Commit messages follow Conventional Commits format
✅ Code formatted; lint checks pass locally
✅ Targets correct branch
✅ Docs updated

── CI pipeline must pass ─────────────────────────────────────
✅ Lint & Style gate green
✅ Build gate green (zero warnings)
✅ Unit tests gate green
✅ Coverage threshold gate green
✅ SonarQube quality gate green
✅ Fortify security scan green
✅ OWASP Dependency-Check green

── Reviewer must confirm ─────────────────────────────────────
✅ All unit tests present and meaningful
✅ Coverage 100 % on new/modified code verified
✅ No new SonarQube violations
✅ Code follows standards
✅ Commit messages follow convention
✅ CI pipeline green
✅ Min. 1–2 approvals obtained
✅ No unresolved [blocking] comments
```

### Severity Response Times

```
Fortify / SonarQube finding:
  Critical  →  Fix before PR merge (blocks merge)
  High      →  Fix before PR merge (blocks merge)
  Medium    →  Ticket + fix within current sprint
  Low       →  Ticket + fix in next available sprint

CVE (OWASP Dependency-Check):
  CVSS ≥ 7.0  →  Patch within 48 hours
  CVSS < 7.0  →  Patch in next maintenance cycle
```

### Definition of Done Summary

```
Code ✅  Tests (100% new code) ✅  Coverage verified locally ✅
SonarLint ✅  SonarQube ✅  Fortify ✅  CI (7 gates) ✅
Pre-submission checklist ✅  Reviewer checklist ✅
Approvals ✅  Docs ✅  Merged ✅  Branch deleted ✅
```
