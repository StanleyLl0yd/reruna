# RERUNA Roadmap

The roadmap is milestone-based. A milestone closes only after its exit criteria are met.

## R0 — Foundation

Status: **Released as R0 on 2026-09-07.**

Goal: prove the game can exist as a small, deterministic, testable Android project.

- [x] Android project baseline
- [x] `com.sl.reruna`
- [x] compileSdk/targetSdk 37, minSdk 26
- [x] Pure-Kotlin deterministic game engine
- [x] 7×9 toroidal board
- [x] 8-step recordings
- [x] Maximum 6 active Reruns
- [x] Sparks
- [x] Combo
- [x] Sync
- [x] Resonance
- [x] Entropy
- [x] Persistent local best score
- [x] Compose board and swipe controls
- [x] Core unit tests
- [x] CI baseline

Exit criterion: a clean build launches into a complete playable loop.

## R1 — Feel

Goal: make the basic loop genuinely hard to put down before adding metagame systems.

- [ ] Tune Entropy curve from real play sessions
- [ ] Tune Spark spawn pressure
- [ ] Tune scoring and Combo decay
- [ ] Improve Sync readability
- [ ] Add RERUN transition animation
- [ ] Add Resonance audiovisual state
- [ ] Add subtle haptics
- [ ] Add deterministic sound layer
- [ ] Add pause/background handling
- [ ] Add lightweight first-run tutorial inside gameplay
- [ ] Add accessibility alternatives to swipe input
- [ ] Device-size and refresh-rate testing
- [ ] Frame-time and allocation profiling

Exit criterion: repeated internal playtests consistently produce voluntary immediate restarts.

## R2 — Depth

Goal: add variety without making the controls more complicated.

Candidates must be prototyped and retained only if they improve the core loop.

- [ ] Daily deterministic seed
- [ ] Alternative board laws/modifiers
- [ ] Mirror Rerun experiment
- [ ] Reverse Rerun experiment
- [ ] Slow/offset Rerun experiment
- [ ] Challenge seeds
- [ ] Local run history
- [ ] Achievement model
- [ ] Cosmetic progression model

Exit criterion: meaningful variety without weakening the eight-move Rerun identity.

## R3 — Retention and competition

- [ ] Daily challenge
- [ ] Leaderboard design
- [ ] Anti-cheat model
- [ ] Shareable run/seed format
- [ ] Asynchronous ghost challenges
- [ ] Optional cloud identity, only if required
- [ ] Privacy review before any network feature ships

Exit criterion: players have a reason to return without coercive retention mechanics.

## R4 — Release candidate

- [ ] Production icon and store artwork
- [ ] Localization
- [ ] Signed AAB
- [ ] Release shrinking/optimization
- [ ] Baseline Profiles
- [ ] 16 KB page-size check for every dependency/artifact
- [ ] Android 8.0 through current Android compatibility pass
- [ ] Phone/tablet/foldable layout pass
- [ ] Crash/ANR review
- [ ] Privacy/data-safety declarations
- [ ] Store listing/ASO
- [ ] Closed testing
- [ ] 1.0.0 release decision

No feature is promoted merely because it is on this list. RERUNA stays small unless added complexity measurably improves play.
