# RERUNA

> **Build your past.**

RERUNA is a minimalist strategy arcade game where every move becomes part of a repeating past. You do not run away from your previous selves — you build a system out of them.

The project is **Android-first**, while the game rules are intentionally kept platform-independent so the core can later move to other platforms without being redesigned.

## Core loop

**MOVE → RECORD → RERUN → SYNC → SURVIVE → IMPROVE**

- Swipe in four directions.
- The world advances only when you move.
- Every 8 moves become a repeating **Rerun**.
- Up to 6 Reruns remain active at once.
- Any version of you can collect **Sparks**.
- Deliberately overlap timelines to create **Syncs**.
- Syncs charge **Resonance**, an 8-turn scoring state.
- Keep **Entropy** below 100%.
- When the run ends, press **AGAIN** and improve the machine.

The board wraps at every edge, so moving off one side brings you back on the opposite side.

## Current status

**Pre-alpha / R0 foundation.**

The current build contains the first playable vertical slice:

- deterministic pure-Kotlin game engine;
- toroidal 7×9 board;
- 8-move recordings;
- up to 6 repeating Reruns;
- Sparks, Combo, Sync, Resonance and Entropy;
- persistent local best score;
- one-finger swipe input;
- Compose-rendered game board;
- instant run restart;
- unit tests for the core rules.

The immediate goal is not content volume. It is to answer one question: **does losing make the player want to tap AGAIN immediately?**

## Android baseline

| Setting | Value |
| --- | --- |
| Application ID | `com.sl.reruna` |
| minSdk | 26 |
| targetSdk | 37 |
| compileSdk | 37 |
| Android Gradle Plugin | 9.4.0 |
| Gradle | 9.6.0 in CI |
| Compose BOM | 2026.08.00 |
| JDK | 17 |
| Release format | AAB |
| Native/JNI/NDK | None in MVP |

With no native code in the MVP, the app does not introduce native-library 16 KB page-size compatibility risk.

## Build

Open the project in a current Android Studio version that supports AGP 9.4/API 37, or build with Gradle 9.6.0 and JDK 17.

The CI workflow installs API 37 and runs unit tests, lint and a debug build.

## Project docs

- [Roadmap](ROADMAP.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Game design](docs/GAME_DESIGN.md)
- [Contributor/agent rules](AGENTS.md)
- [Audit/refactor protocol](docs/agent/AUDIT_REFACTOR.md)

## Product principles

- Gameplay before content.
- A run starts in seconds.
- One-handed input first.
- No forced account.
- No network dependency for the core game.
- No pay-to-win.
- No dark-pattern interruption between attempts.
- Minimal permissions and minimal data collection.
- Game rules stay deterministic and testable.

## License

RERUNA source code is available under the **PolyForm Noncommercial License 1.0.0**. Commercial use requires separate permission from the copyright holder. See [LICENSE.md](LICENSE.md).
