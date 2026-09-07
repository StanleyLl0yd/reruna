# R1 Playtest Protocol

R1 tuning is evidence-driven. Do not mark Entropy, Spark pressure, scoring/Combo, device coverage, or performance complete from code inspection alone.

## Build under test

Use a debug APK produced from the exact `main` commit being evaluated. Record the commit SHA with every playtest batch.

The game-over screen exposes deterministic run metrics:

- turns survived;
- Sparks collected;
- rewarded fresh-convergence Sync events;
- total Reruns created;
- maximum Combo;
- Resonance activations.

These metrics are intentionally local and are not transmitted anywhere.

## Balance session

For each tuning candidate:

1. Play 3 warm-up runs and discard them.
2. Play at least 10 measured runs without changing the build.
3. After each run, record the run stats and whether you immediately wanted to press **AGAIN**.
4. Add one short failure note: missed Spark coverage, weak Rerun route, failed Sync planning, Combo loss, or another concrete cause.
5. Change one tuning dimension at a time when practical.
6. Repeat the measured batch before accepting a new value.

Do not tune only to average score. R1 is successful when failure remains legible and repeated runs create a clear plan for the next attempt.

## Device coverage

Before closing R1, exercise at least:

- a compact phone-sized viewport;
- a common modern phone viewport;
- a tall/large phone viewport;
- 60 Hz;
- a higher refresh rate when available.

Verify:

- board, tutorial, controls, footer, and game-over stats remain visible without overlap;
- direction buttons remain comfortably tappable;
- swipe input behaves consistently;
- animations do not alter game timing;
- pause/resume does not advance the game;
- haptics and sound do not block input.

Record device model, Android version, logical display size, density, and refresh rate.

## Frame-time and allocation capture

On macOS with Android platform-tools installed, connect the test device and run:

```text
scripts/capture_playtest_profile.sh
```

The script captures display metadata, `dumpsys gfxinfo ... framestats`, and `dumpsys meminfo` before and after a manual play interval.

Use Android Studio Profiler for deeper allocation investigation only after the command-line capture shows a practical reason. Avoid speculative optimization.

## R1 exit evidence

R1 can close only when:

- the three tuning items have measured before/after playtest evidence;
- the device matrix has been exercised;
- frame-time/allocation review found no practical blocker or the blockers were fixed;
- repeated internal playtests consistently produce voluntary immediate restarts.
