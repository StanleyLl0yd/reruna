# Changelog

All notable milestone changes are recorded here.

## R1 — Feel — Unreleased

### Added

- Engine-owned per-turn Spark and Sync-location event metadata for deterministic presentation feedback.
- Sync location pulse, Rerun transition pulse, and active Resonance board treatment.
- Fixed event-to-tone mapping with event haptics, isolated from game rules.
- Lifecycle-aware pause/input suppression.
- Lightweight first-run in-game tutorial with persistent dismissal.
- Four accessible on-screen direction buttons that submit the same Direction commands as swipes.

### Changed

- Gameplay tuning constants are centralized in GameRules without claiming playtest-driven value changes.
- R1 remains open pending real-play tuning, device-size/refresh-rate testing, and frame-time/allocation profiling.

## R0 — Foundation — 2026-09-07

R0 establishes the first complete playable RERUNA loop on Android.

### Added

- Deterministic pure-Kotlin game engine.
- 7×9 toroidal board and one-finger four-direction swipe input.
- Eight-move recording cycles with up to six active Reruns.
- Sparks, Combo, Sync, Resonance, Entropy, scoring, game over, and instant restart.
- Persistent local best score.
- Jetpack Compose gameplay UI.
- Core deterministic unit tests and Android CI.
- CodeQL, Semgrep, Gitleaks, resolved Maven dependency auditing, Dependabot, and CI supply-chain policy checks.
- RERUNA branding assets and project documentation.

### Release status

R0 is a **pre-alpha milestone release**, not a production/store release. Its downloadable Android artifacts are development builds intended for evaluation and internal playtesting.
