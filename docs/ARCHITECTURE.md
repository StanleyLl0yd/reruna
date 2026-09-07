# Architecture

RERUNA starts deliberately small.

## Layers

### Game engine

Location: `app/src/main/java/com/sl/reruna/game`

The engine owns all rules and state transitions:

- board topology;
- movement;
- recording;
- Rerun playback;
- Spark placement and collection;
- Combo;
- Sync;
- Resonance;
- Entropy;
- scoring;
- game-over state;
- deterministic random state;
- deterministic run-summary counters used by R1 playtests.

The engine has no Android dependency. Its central operation is conceptually:

`GameState + Direction → GameState`

That constraint makes rule changes cheap to test and keeps a future non-Android client possible.

### Android state holder

`RerunaViewModel` connects the pure engine to Android lifecycle/UI state. It also owns the only persistent value in the MVP: local best score.

Persistence is intentionally minimal. SharedPreferences is enough for one integer and avoids introducing a database or additional architecture before it is needed.

### Compose UI

The UI:

- renders state;
- renders the board with a Compose Canvas;
- converts a swipe or accessibility direction-button press into the same Direction command;
- displays game events and meters;
- animates deterministic event metadata such as Sync locations and Rerun creation;
- maps engine event metadata to fixed sound/haptic cues;
- displays first-run guidance and paused state;
- requests restart.

Run-summary counters are engine-owned derived state and remain local to the active run; they are not analytics and are not transmitted.

The UI must not calculate scoring, movement results, Entropy, Reruns or Syncs. Presentation feedback may derive visuals from engine-owned event metadata, but animation clocks, audio playback, haptics, and lifecycle state never feed back into the engine.

## Determinism

The random generator state is stored inside `GameState`. This is required for:

- repeatable unit tests;
- future Daily seeds;
- reproducible bug reports;
- asynchronous challenge runs;
- potential replay verification.

Do not replace it with ad-hoc calls to global random APIs inside engine steps.

## Board topology

The board is 7×9 and toroidal. Coordinates wrap on both axes. There are no wall deaths.

Each recording contains exactly 8 moves. At the end of the eighth move a new Rerun is created from the recording origin and all Reruns restart their recorded sequence together.

Only the six newest Reruns remain active.

## Dependency policy

The MVP uses platform/Jetpack code only and has no native library. This keeps startup, artifact size, maintenance and 16 KB native page-size risk low.

New dependencies should be added only for a concrete need that cannot reasonably be met by the existing stack.

## Future portability

Do not interpret "platform-independent game rules" as a requirement to introduce Kotlin Multiplatform now. The current pure-Kotlin boundary is sufficient. KMP is a later decision if a second platform is actually being built.
