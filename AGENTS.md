# AGENTS.md

These rules apply to all automated and human changes in this repository.

## Product invariant

RERUNA is a minimalist strategy arcade game. Complexity belongs in emergent interactions, not in the input scheme or menus.

The core loop is:

**MOVE → RECORD → RERUN → SYNC → SURVIVE → IMPROVE**

Do not turn the game into a generic runner, match game, shooter, idle game, or content-heavy puzzle collection.

## Architecture rules

1. Keep the game rules in `com.sl.reruna.game`.
2. The game package must remain free of Android framework and Compose dependencies.
3. A move must be deterministic for a given `GameState` and `Direction`.
4. Randomness must flow through explicit engine state so seeded runs are reproducible.
5. UI code renders state and translates input into directions. It must not duplicate game rules.
6. Every new rule needs focused unit tests before or with its implementation.
7. Prefer deleting complexity over adding abstraction without a demonstrated need.

## Android baseline

- Application ID: `com.sl.reruna`
- minSdk: 26
- targetSdk: 37 or newer only after explicit compatibility validation
- compileSdk: 37 or the latest stable SDK supported by the selected stable toolchain
- JDK: 17 unless the toolchain requires a justified change
- Primary production artifact: signed AAB
- Avoid native/JNI/NDK dependencies unless they have a concrete product benefit
- Any native dependency must be verified for 64-bit and 16 KB memory page-size compatibility

Do not raise minimum requirements without a technical reason.

## Privacy and monetization

Do not add analytics, advertising SDKs, account requirements, network permissions, device identifiers, tracking, or telemetry by default.

Any such change requires an explicit product decision and a privacy review.

Do not add pay-to-win mechanics or delays designed solely to pressure a purchase.

## Gameplay terminology

Use these names consistently:

- **Rerun** — an 8-move recorded past self
- **Spark** — collectible scoring/entropy resource
- **Sync** — two or more selves occupying the same cell on a turn
- **Resonance** — temporary high-value state charged by Syncs
- **Entropy** — run-ending pressure meter

## Change discipline

- Keep commits focused.
- Preserve working behavior unless the task explicitly changes it.
- Run unit tests and lint for game/UI changes.
- Update `README.md`, `ROADMAP.md`, or `docs/` when a change affects documented behavior.
- Do not commit generated APK/AAB artifacts, keystores, secrets, local SDK paths, or IDE state.
