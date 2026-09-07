# Game Design

## One-sentence pitch

**Build your past, then use it to keep the future alive.**

## Core fantasy

The player gradually turns previous actions into a living machine. Past selves are not enemies. They are programmable assets.

The ideal emotional sequence is:

1. "This is simple."
2. "Oh — that copy is replaying what I did."
3. "I can make it collect for me."
4. "I can make them meet on purpose."
5. "I can build a better eight-move route."
6. Game over.
7. Immediate retry.

## Rules in the first playable build

- Board: 7×9.
- Topology: wraps horizontally and vertically.
- Input: four directional swipes.
- Time: advances once per player move.
- Recording: exactly 8 moves.
- Active Reruns: maximum 6.
- Reruns replay synchronously.
- Sparks can be collected by the player or any Rerun.
- A Sync occurs when two or more active selves occupy one cell after a move.
- Syncs award score, reduce Entropy and charge Resonance.
- Resonance lasts 8 turns and doubles scoring while active.
- Entropy reaching 100 ends the run.
- Missing Sparks long enough breaks Combo.

## Current tuning values

These values are prototypes, not promises.

| Parameter | Initial value |
| --- | ---: |
| Starting Sparks | 3 |
| Maximum Sparks | 4 |
| Recording length | 8 turns |
| Maximum Reruns | 6 |
| Starting Entropy gain | 2/turn |
| Spark Entropy reduction | 7 |
| Sync Entropy reduction | 4 × participants |
| Resonance charge | 12 × Sync participants |
| Resonance duration | 8 turns |
| Spark base score | 100 |
| Sync base score | 250 × participants² |
| Combo grace | 4 turns |

## Design constraints

### The game must remain readable

More entities do not automatically mean more fun. Six Reruns is a deliberate readability cap and should only change after playtesting.

### Failure should be legible

The player should usually understand which weak route or missed collection caused Entropy to escape control.

### Restarts should be frictionless

No modal promotions, currency screens, forced ads or long animations between game over and AGAIN.

### Skill should compound

A stronger player should not merely swipe faster. They should:

- understand eight-turn geometry;
- distribute coverage;
- anticipate future intersections;
- preserve Combo;
- manufacture Syncs;
- replace aging Reruns intentionally.

## What RERUNA is not

RERUNA is not about avoiding contact with replay ghosts. Contact is desirable when deliberately engineered.

RERUNA is also not a level-by-level pathfinding puzzle in the MVP. Endless systemic play is the first test.

## MVP success criterion

The MVP succeeds when testers voluntarily restart after failure and can explain a concrete plan for what they want to do better in the next run.
