# AGENTS.md

These rules apply to all automated coding agents, maintenance work, and human changes in this repository.

## Project identity and priorities

RERUNA is a minimalist strategy arcade game. Complexity belongs in emergent interactions, not in the input scheme, menus, or infrastructure.

Preserve these priorities, in order:

1. Correct and deterministic game rules.
2. Immediate, readable, responsive gameplay.
3. Data integrity and reproducible runs.
4. Accessibility and predictable Android behavior.
5. Resource efficiency.
6. Minimal necessary architecture and dependency surface.
7. Future portability of the game rules.

The core loop is:

**MOVE → RECORD → RERUN → SYNC → SURVIVE → IMPROVE**

Do not turn RERUNA into a generic runner, match game, shooter, idle game, or content-heavy puzzle collection unless the product direction is explicitly changed.

Do not claim release readiness, retention quality, performance, accessibility completeness, or platform support beyond what current repository evidence demonstrates.

## Read before changing architecture

Before changing durable behavior or architecture, inspect the relevant parts of:

- `README.md`;
- `ROADMAP.md`;
- `docs/ARCHITECTURE.md`;
- `docs/GAME_DESIGN.md`;
- this file.

Architecture and game-design documentation are part of the repository contract. Update them in the same change when durable behavior, ownership, terminology, platform requirements, or subsystem boundaries change.

For a full repository audit, cleanup, optimization, simplification, or deep refactor, read and follow `docs/agent/AUDIT_REFACTOR.md` in full before editing.

## Authoritative game architecture

Keep the authoritative game rules in `com.sl.reruna.game`.

The game package must remain free of Android framework and Compose dependencies.

The central rule boundary is conceptually:

`GameState + Direction -> GameState`

Preserve these invariants:

- A move is deterministic for a given `GameState` and `Direction`.
- Randomness flows through explicit engine state so seeded runs are reproducible.
- Game state, scoring, Spark collection, Reruns, Sync, Resonance, Entropy, and game-over decisions are engine-owned.
- UI code renders state and translates user input into commands. It must not duplicate or override game rules.
- Presentation state must not become a second source of game truth.
- Persisted values must be explicitly owned. In the current MVP, local best score is persistence; the active run remains authoritative in memory.
- A future replay, daily seed, challenge, or save format must rebuild or validate authoritative state through the game rules rather than trusting duplicated derived score or geometry.

Do not introduce parallel rule implementations for touch, accessibility, AI, tests, replay, networking, or another platform.

Every non-trivial new or changed rule needs focused deterministic regression coverage.

## Gameplay invariants

Unless explicitly changed as a product decision:

- Board topology is toroidal.
- The recording length is 8 moves.
- A Rerun is a recorded past self that replays synchronously.
- At most 6 Reruns are active.
- Past selves are programmable assets, not collision enemies.
- Sparks may be collected by the player or a Rerun.
- Sync is desirable and must be derived from authoritative post-move positions.
- Resonance is charged by Syncs.
- Entropy is the run-ending pressure system.
- Restart friction must remain minimal.

Use terminology consistently:

- **Rerun** — an 8-move recorded past self.
- **Spark** — collectible scoring/entropy resource.
- **Sync** — two or more selves occupying the same cell on a turn.
- **Resonance** — temporary high-value state charged by Syncs.
- **Entropy** — run-ending pressure meter.

When tuning numeric values, preserve the underlying rules unless the task explicitly changes them. Treat tuning as gameplay behavior, not as a harmless refactor.

## Android baseline

- Application ID: `com.sl.reruna`.
- minSdk: 26.
- targetSdk: 37 or newer only after explicit compatibility validation.
- compileSdk: 37 or the latest stable SDK supported by the selected stable toolchain.
- JDK: 17 unless a justified toolchain requirement changes it.
- Primary production artifact: signed AAB.
- APK is a development/direct-install artifact, not the primary store format.
- Avoid native/JNI/NDK dependencies unless they provide a concrete product benefit.
- Any native dependency must be verified for required 64-bit ABIs and 16 KB memory page-size compatibility.

Do not raise platform requirements without a technical reason.

Keep Android/platform APIs outside the pure game-rule layer.

## UI, input, and accessibility

- One-finger directional input is a product invariant unless explicitly changed.
- Different input paths must submit the same authoritative `Direction` commands; they must not implement separate rule logic.
- Essential game information must not rely on color alone.
- Preserve usability with Android safe areas, different phone sizes, font scaling, touch input, and reduced-motion preferences where motion is introduced.
- Animations, haptics, sound, particles, trails, and event labels are presentation. They must not decide legality, score, Sync, Resonance, Entropy, or persistence.
- Avoid frame-rate-dependent game semantics. The world advances by player moves, not rendering time.

## Determinism and performance

Equivalent state plus equivalent input must produce equivalent rule output.

Timing, frame rate, animation clocks, benchmark data, and device performance must not feed authoritative game state.

Performance work must target practical or measured cost. Review hot paths such as:

- per-move allocations;
- repeated collections or conversions;
- recomposition breadth;
- Canvas work;
- repeated derived calculations;
- persistence on the input path.

Do not trade correctness, readability, determinism, accessibility, or architecture boundaries for an unmeasured micro-optimization.

Deterministic stress tests must remain hardware-independent. Do not use fragile wall-clock thresholds as correctness gates.

## Dependencies and generated code

Add a production or development dependency only for a concrete current need.

Before adding a dependency, check whether the language, Android SDK, Compose, or the existing stack already provides the capability adequately.

Do not add overlapping libraries for the same purpose.

Do not replace a mature dependency with custom code solely to reduce dependency count. A local replacement is justified only when it clearly reduces total complexity, risk, maintenance cost, or artifact size.

Keep dependency versions and CI configuration reproducible and synchronized.

Keep Gradle dependency locks committed and update them only with an intentional dependency change. Keep the CI Gradle distribution version and SHA-256 pin synchronized; never remove checksum verification.

Prefer maintained GitHub Actions versions; for release-critical workflows, pin third-party actions to immutable commit SHAs where practical.

Do not weaken tests, lint, build, compatibility, or security checks merely to make a change pass.

Generated artifacts are not authoritative source when a generator or source representation exists. Change the source of truth and regenerate instead.

## Privacy, security, and monetization

Do not add analytics, advertising SDKs, account requirements, backend services, network permissions, remote configuration, device identifiers, tracking, or telemetry by default.

Any such change requires an explicit product decision and a privacy/security review.

Do not add pay-to-win mechanics, artificial wait timers, forced interstitial friction between runs, or other dark-pattern retention mechanics without an explicit product decision.

Never commit credentials, tokens, private keys, signing material, keystores, local SDK paths, personal data, generated secrets, or production service configuration.

## Comments and documentation

Keep source-code comments minimal, necessary, current, and English-only.

Do not add comments that narrate obvious code. Prefer names, types, and structure that make the code self-explanatory.

Keep comments only when they explain a non-obvious reason, constraint, workaround, invariant, ownership boundary, compatibility requirement, lifecycle or safety rule, resource bound, architectural reason, or important contract.

Do not replace clear code with explanatory comments when the code itself can be made self-explanatory.

Remove stale, misleading, redundant, commented-out historical code and obsolete TODO/FIXME items when the surrounding work proves they are no longer needed.

Review `README.md` after implementation changes. Update `README.md`, `ROADMAP.md`, and relevant `docs/` files in the same change when behavior, architecture, dependencies, commands, platform requirements, or project status change.

## Verification

Run checks appropriate to every change before considering it complete.

The baseline repository verification is:

```text
gradle --no-daemon :app:testDebugUnitTest
gradle --no-daemon :app:lintDebug
gradle --no-daemon :app:assembleDebug
gradle --no-daemon :app:bundleDebug
python3 scripts/verify_android_manifest_security.py
python3 scripts/verify_ci_supply_chain.py
```

For release, packaging, dependency, manifest, SDK, signing, or shrinker changes, also verify the applicable release AAB path in CI or an equivalent controlled environment.

Changes to the pure game engine must run the full game-engine unit suite.

Changes to UI/input must compile the Android app and preserve the authoritative engine boundary.

Changes to persistence must test existing-value, missing-value, malformed-value, and migration/recovery behavior as applicable.

Never claim a check passed unless it actually ran successfully. State unavailable platforms, hardware, credentials, signing material, SDKs, or other verification limitations explicitly.

## Change and Git discipline

Inspect the repository state before editing and preserve unrelated user changes.

Keep changes and commits focused on one coherent purpose. Separate behavior-preserving refactoring from unrelated feature development.

Keep `main` buildable. For non-trivial work, use short-lived topic branches and pull requests rather than using `main` as a scratch branch.

Do not force-push shared history, discard unrelated changes, weaken repository protections, or delete active branches without explicit authorization.

Merge only after the required checks pass.

Repository cleanup should also review obsolete pull requests and stale topic branches when relevant; remove them only when their obsolescence is clear.

## Repository-wide audit and deep refactoring

A repository-wide audit is an implementation task, not merely a request for recommendations.

For any request for a full audit, cleanup, optimization, simplification, or deep refactor:

1. Read `docs/agent/AUDIT_REFACTOR.md` in full before editing.
2. Preserve every RERUNA-specific invariant in this file.
3. Establish a verified baseline before changing behavior-preserving code.
4. Implement safe, justified simplifications rather than only listing them.
5. Perform the mandatory second full pass.
6. Finish with the complete available verification suite and evidence-based final report.

The goal is minimum **necessary complexity**, not minimum line count. Do not perform code golf or speculative rewrites.



## GitHub security baseline

Security controls are part of the repository contract, not optional CI decoration.

- Before a major milestone or public release, perform a repository security review covering source, dependencies, Android manifest/permissions, CI/CD, release integrity, and supply chain.
- Keep every non-local GitHub Action pinned to an immutable full 40-character commit SHA. Keep a nearby version comment when useful for maintainability.
- Pin workflow container images by SHA-256 digest.
- Do not use `pull_request_target` for normal validation. Never execute untrusted pull-request code with repository secrets, signing material, write tokens, or privileged runners.
- Default workflow permissions to `permissions: {}` and grant only the minimum job-level permissions required.
- Keep `security-events: write` limited to jobs that upload code-scanning results.
- Do not grant `id-token: write`, `contents: write`, `pull-requests: write`, or similar write scopes unless a concrete job requires them.
- Keep Android verification, CodeQL, Semgrep, Gitleaks, and the resolved Maven Dependency Audit healthy and merge-blocking once repository rulesets are configured.
- Maintain Dependabot coverage for every package ecosystem actually used by the repository, including GitHub Actions. Audit resolved Maven dependencies against the GitHub Advisory Database.
- Run `python3 scripts/verify_ci_supply_chain.py` whenever `.github/workflows/**` or `.github/actions/**` changes.
- Never commit signing keys, keystores, passwords, API keys, tokens, `.env` files, `local.properties`, service-account credentials, or generated secrets.
- Keep secret material outside Git and supply it only through an appropriate protected secret mechanism.
- A production release workflow must build from an unambiguous commit/tag, use signing material outside the repository, minimize write permissions, publish checksums, and add provenance/attestation when technically supported.
- When versioned `v*` releases are introduced, release tags must become immutable before the first production release.
- If native/JNI/NDK code or native libraries are introduced, perform a dedicated native supply-chain, ABI, memory-safety, and 16 KB page-size review.
- Do not weaken a security gate merely to make CI pass. Fix the cause or document a narrowly justified exception.

## App icon source artwork

- The current approved canonical RERUNA app icon is `assets/branding/reruna-icon.png`.
- Its SHA-256 is `5ecf527eb8a01550cce8949a66c3d68d77e716acdd12f2d887f09eee677f579b`.
- The Android launcher copy at `app/src/main/res/mipmap-nodpi/ic_launcher.png` must remain byte-identical to the canonical source.
- When the project owner provides a new app icon as a PNG and identifies it as the app icon, treat that exact PNG as the canonical source artwork.
- Keep that source as the original raster PNG. Do not trace, vectorize, redraw, restyle, recreate, or convert it to SVG, vector PDF, Android VectorDrawable, SF Symbol, or any other vector representation unless the project owner explicitly requests it.
- Do not overwrite, recompress, optimize in place, or otherwise rewrite the canonical PNG. Keep the uploaded source unchanged.
- Platform-required derivatives may be generated only as raster derivatives of that PNG. Resizing and required raster packaging/container formats such as PNG size variants, ICO, or ICNS are allowed, but the visible artwork must remain unchanged: no cropping, padding, color changes, removed details, or other design edits unless explicitly requested.
- If an older icon in another format is currently canonical, keep it until the project owner explicitly supplies a replacement PNG as the new app icon. Once supplied, that PNG becomes the canonical source and the asset pipeline should derive required icons from it rather than converting it to a vector source.
