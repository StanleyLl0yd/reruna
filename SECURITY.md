# Security Policy

## Supported versions

RERUNA is currently pre-alpha and has no published production release.

| Version | Supported |
| --- | --- |
| Current `main` during pre-alpha | Yes |
| Historical commits | No |
| Published releases | None yet |

Until the first public release, security fixes land on `main`. When versioned releases begin, the project is expected to support the latest release unless a release note states otherwise.

## Reporting a vulnerability

Use GitHub private vulnerability reporting for this repository when it is available.

Do not disclose a suspected vulnerability, credential, signing material, token, private key, or other sensitive detail in a public issue, discussion, pull request, commit message, or log.

If private vulnerability reporting is unavailable, open only a minimal public issue asking for a private reporting channel. Do not include exploit details or secrets in that issue.

Include the affected commit or build, reproduction steps, expected impact, and relevant logs or screenshots with sensitive values removed. If testing may expose credentials or signing material, stop after collecting the minimum evidence required to report the issue safely.

## Triage process

- Initial acknowledgement target: within 3 business days.
- Initial severity and scope assessment target: within 7 business days.
- Valid reports are reproduced when practical and tracked privately until a fix or mitigation is ready.
- Critical and high-impact issues take priority over feature work.
- Disclosure timing should allow affected users a reasonable opportunity to update.

These are response targets, not guarantees of a specific remediation date.

## Security scope

In scope:

- Android application code and packaged resources;
- deterministic game-state and persistence integrity where a bug crosses a trust or data boundary;
- dependency and build-toolchain risks introduced by this repository;
- GitHub Actions, CI/CD, code scanning, secret scanning, dependency review, and artifact integrity;
- Android manifest, exported components, permissions, local data handling, and unintended network exposure;
- repository-secret exposure or supply-chain weaknesses caused by repository configuration.

Generally out of scope unless RERUNA directly causes or amplifies the issue:

- vulnerabilities in GitHub, Android, device firmware, app stores, or other third-party infrastructure;
- social engineering and phishing;
- denial-of-service requiring unrealistic local resource exhaustion;
- findings that require a rooted or otherwise compromised device and do not cross an additional RERUNA trust boundary.

## Current security model

RERUNA intentionally has a small runtime attack surface:

- no Android `INTERNET` permission;
- no backend or account system;
- no analytics or advertising SDKs;
- no native/JNI/NDK code in the current project;
- no dangerous Android runtime permissions;
- only a local best-score preference is persisted by the MVP.

Repository and CI controls include:

- Android build, unit-test, lint, and artifact verification;
- CodeQL for Java/Kotlin;
- Semgrep security and secret rules;
- Gitleaks full-history secret scanning;
- Dependency Review on pull requests;
- Dependabot for Gradle and GitHub Actions;
- least-privilege workflow permissions;
- full-SHA-pinned external GitHub Actions;
- digest-pinned workflow containers;
- a repository script that rejects common GitHub Actions supply-chain regressions.

## Release security

There is no production release pipeline yet.

Before the first production release, release hardening must explicitly cover:

- an immutable version tag;
- build provenance tied to the exact release commit/tag;
- Android signing material stored outside the repository;
- protected access to signing credentials;
- release certificate verification;
- SHA-256 checksums;
- GitHub artifact attestation/provenance when supported;
- minimal release-job permissions;
- validation that pull-request code cannot access signing credentials.

Never commit a keystore, `key.properties`, private key, token, `.env` file, service-account credential, or other credential material.
