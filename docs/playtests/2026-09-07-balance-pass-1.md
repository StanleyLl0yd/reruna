# R1 Balance Pass 1 — 2026-09-07

This is an exploratory balance pass based on four completed runs from the first instrumented R1 build at commit `a3491d85f8839439b95c883f5b94c5d1cdbc6730`.

It is not enough evidence to close any R1 tuning item. It is enough to fix one clear dominant-strategy exploit and one clear early-run pressure problem before the larger measured batch.

## Observed runs

| Turns | Score | Sparks | Sync events | Reruns | Max Combo | Resonance activations |
| ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 130 | 47,100 | 43 | 26 | 16 | 25 | 3 |
| 390 | 853,400 | 19 | 342 | 48 | 4 | 29 |
| 39 | 100 | 1 | 0 | 4 | 1 | 0 |
| 41 | 200 | 2 | 0 | 5 | 1 | 0 |

## Findings

### Sustained-overlap Sync farming dominates

The 390-turn run produced 342 Sync events, 29 Resonance activations, and 853,400 score while collecting only 19 Sparks.

That means a pair or group of selves that stays stacked can repeatedly earn Sync score, Entropy relief, and Resonance every turn. The optimal machine collapses toward persistent overlap instead of repeated planned convergence.

### Early failure pressure is too abrupt without a working route

The two runs without Syncs ended after 39 and 41 turns with only one or two Sparks collected. That is enough time to reveal Reruns, but not enough room for a new player to understand and then recover from a weak first route.

### Spark/Combo play can support a healthier run

The 130-turn run collected 43 Sparks, reached Combo 25, created 26 Sync events, and activated Resonance three times. This is closer to the intended mixed loop and is the reference shape for the next playtest batch, not a final target.

## Balance pass

- A rewarded Sync now requires a participant group to newly converge. Staying overlapped remains visually stacked but does not repeatedly award score, Entropy relief, or Resonance until the group separates and meets again.
- Starting/maintained Sparks increase from 3/4 to 4/4.
- Starting Entropy gain drops from 2 to 1.
- Entropy time ramp moves from every 64 turns to every 96 turns.
- Rerun-count Entropy pressure changes from one step per 2 active Reruns to one step per 4.
- Sync base score drops from 250 to 150 while retaining the participant-squared reward shape.

## Next evidence required

Run the normal 3 warm-ups plus at least 10 measured runs on the new build. Compare:

- median and spread of turns survived;
- Sparks per 100 turns;
- rewarded Syncs per 100 turns;
- Resonance activations per 100 turns;
- score composition in practice;
- maximum Combo;
- immediate-AGAIN response and failure note.

Do not mark the three R1 tuning tasks complete until the larger before/after evidence supports the new values.
