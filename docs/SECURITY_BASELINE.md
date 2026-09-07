# Security baseline

RERUNA applies security controls appropriate to its current Android/Kotlin, offline, no-backend, no-native-code architecture.

## Repository protection

Target GitHub repository rules remain:

- protect the default branch;
- forbid deletion and non-fast-forward updates;
- require pull requests, squash merging, linear history, conversation resolution, and verified signed commits on `main`;
- require successful `Verify`, `Analyze Java and Kotlin`, `Semgrep`, `Gitleaks`, and `Dependency Review` checks;
- keep production version tags immutable once versioned releases begin;
- enforce CodeQL findings at an appropriate security-severity threshold.

These account-side rules require repository administration capabilities and are not replaced by CI files.

## CI/CD supply chain

- External GitHub Actions use immutable full commit SHAs.
- Workflow containers use SHA-256 digests.
- Workflow permissions default to none and are granted per job.
- `pull_request_target`, persisted checkout credentials, and inherited reusable-workflow secrets are rejected by policy.
- The official Gradle 9.6.0 wrapper is committed, its wrapper JAR SHA-256 is validated, and the distribution ZIP is pinned by SHA-256 in `gradle-wrapper.properties`.
- Dependabot covers Gradle and GitHub Actions.

## Dependency security

- Gradle dependency locking is enabled for the app module.
- CI regenerates the lock state and fails on drift.
- Resolved Maven dependencies are checked against the GitHub Advisory Database for high/critical advisories.
- Dependabot provides update pull requests.
- GitHub-native Dependency Review is enabled on pull requests and is required by the protected `main` ruleset.

## Static analysis and secrets

- CodeQL Java/Kotlin with `security-extended` queries.
- Semgrep `security-audit` and `secrets` rules.
- Gitleaks full-history secret scanning.
- Qodana JVM analysis on scheduled/manual runs as a supplementary non-merge-blocking analyzer.

## Android attack surface

CI requires:

- no Android permissions under the current offline baseline;
- `android:usesCleartextTraffic="false"`;
- no manifest-level `android:debuggable="true"`;
- only `.MainActivity` may be exported, and it must remain the launcher activity.

Any future network, exported-component, analytics, advertising, background-service, or runtime-permission requirement needs an explicit security/privacy review before the baseline changes.

## Release integrity

R0 is a pre-alpha milestone release built with development signing.

Before the first production/store release, add:

- owner-controlled release signing material stored outside Git;
- release certificate fingerprint validation;
- immutable version tags;
- verified release source from protected `main`;
- SHA-256 checksums;
- OIDC-backed artifact attestations;
- minimal release-job write permissions;
- validation that pull-request code cannot access signing material.
