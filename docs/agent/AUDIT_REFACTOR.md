# Repository-wide audit and deep-refactoring protocol

This protocol is mandatory whenever a task requests a full repository audit, cleanup, optimization, simplification, codebase tuning, or deep refactoring.

RERUNA-specific invariants in `AGENTS.md`, `docs/ARCHITECTURE.md`, and `docs/GAME_DESIGN.md` remain mandatory throughout the process and take precedence over generic simplification goals.

A repository-wide audit is an implementation task, not merely a request for recommendations.

## Objective

Audit the entire repository and reduce the codebase to the minimum necessary complexity while preserving 100% of current functionality and externally observable behavior unless an explicitly requested bug fix or product change requires otherwise.

Preserve:

- user-facing functionality;
- game rules and tuning unless tuning is explicitly in scope;
- deterministic seeded behavior;
- UI/UX and input semantics;
- score, Combo, Sync, Resonance, Entropy, and Rerun semantics;
- persistence behavior and stored-value compatibility;
- public and internal contracts that have active consumers;
- Android/platform behavior;
- accessibility behavior;
- documented capabilities;
- edge-case semantics.

The goal is minimum **necessary complexity**, not minimum line count.

Do not perform code golf.

A change is justified only when it objectively reduces one or more of:

- code volume;
- duplication;
- conceptual complexity;
- branching or mutable state;
- coupling;
- maintenance burden;
- dependency surface;
- runtime cost;
- memory/allocation cost;
- regression risk.

If a change merely makes the implementation different without making it demonstrably smaller, simpler, safer, clearer, or more efficient, leave the code unchanged.

Default decision rule:

- if code is proven unnecessary, delete it;
- if code can be objectively simplified, simplify it;
- if duplicate responsibilities can be safely consolidated, consolidate them;
- if an abstraction no longer provides value, remove it;
- if the benefit is uncertain, preserve the existing working behavior.

## Required audit scope

Before making repository-wide refactoring changes, inspect the complete repository rather than only recently changed files or obvious hotspots.

Review all relevant areas:

- production Kotlin source;
- pure game-engine code;
- Compose UI;
- Android lifecycle/state holders;
- tests and test utilities;
- resources and assets;
- AndroidManifest;
- Gradle files and properties;
- CI/CD workflows;
- release and packaging configuration;
- dependency declarations;
- lint/static-analysis/security configuration;
- documentation;
- persistence;
- generated-code integration points;
- directory/package/module structure.

First establish:

1. the actual architecture;
2. authoritative sources of state and game logic;
3. all current user-visible functionality;
4. important internal contracts and invariants;
5. persistence and compatibility requirements;
6. Android/framework/convention-driven entry points;
7. deterministic behavior and random-state flow;
8. the baseline verification status.

Only then begin removing, merging, or reorganizing code.

## Baseline before editing

Record reliable baseline information when practical:

- current commit SHA;
- clean/dirty repository state;
- build/test/lint status;
- relevant file count;
- relevant source line count;
- production and development dependency count;
- unit-test count;
- debug/release artifact size when meaningful;
- known warnings or failing checks.

Never invent baseline statistics.

Use the same counting method for before and after values.

If the baseline already fails, distinguish pre-existing failures from failures introduced by the audit.

## Required removal candidates

Actively search for and remove, when proven safe:

- dead and unreachable code;
- unused functions, classes, methods, variables, constants, types, and interfaces;
- unused imports;
- unused files;
- unused resources and assets;
- obsolete legacy code;
- temporary workarounds whose original constraint no longer exists;
- duplicate or near-duplicate implementations;
- unnecessary abstraction layers;
- unnecessary wrappers and forwarding helpers;
- helper functions with only a trivial single caller;
- adapter chains that add no meaningful semantics;
- redundant data conversions;
- redundant intermediate objects;
- redundant intermediate state;
- redundant copies and transformations;
- defensive checks for states already guaranteed by types, architecture, invariants, or earlier validation;
- repeated validation of already validated data;
- repeated checks of the same condition;
- redundant fallbacks;
- obsolete compatibility branches;
- unused feature flags and configuration;
- unused dependencies and development dependencies;
- multiple dependencies serving the same purpose;
- commented-out historical code;
- stale TODO/FIXME items;
- leftovers from previous migrations or refactors;
- speculative architecture created only for hypothetical future features.

Pay particular attention to:

- multiple representations of the same authoritative game state;
- UI state that duplicates engine state;
- rule calculations repeated outside `com.sl.reruna.game`;
- repeated set/list construction on each move;
- repeated conversions on the input/render path;
- duplicated constants or tuning values;
- wrapper -> wrapper -> wrapper call chains;
- unnecessary Android lifecycle wrappers;
- state copied only to satisfy an abstraction that no longer provides value.

## Proving code is unused

Do not classify code as unused solely because textual search finds no direct call.

Before removing anything, check for indirect or convention-driven use through:

- callbacks and lambdas;
- Compose composition;
- Android lifecycle hooks;
- Activities and manifest declarations;
- resources and generated `R` references;
- reflection;
- serialization/deserialization;
- dependency injection if introduced;
- navigation/routing if introduced;
- Gradle configuration;
- ProGuard/R8 rules;
- generated code;
- CI/CD;
- release tooling;
- tests and debug tooling;
- external/public contracts.

When uncertainty remains, preserve the code until lack of use can be demonstrated.

## Required simplification review

Look for opportunities to safely:

- simplify control flow;
- reduce nesting;
- reduce mutable state;
- reduce branches;
- merge equivalent paths;
- merge components with the same responsibility;
- remove temporary values and objects;
- remove avoidable allocations and copies;
- remove repeated transformations;
- remove redundant loops or passes;
- compute invariant values once;
- replace custom code with standard Kotlin/Android/Compose facilities when clearly simpler;
- eliminate repeated business/game logic;
- centralize genuinely shared logic when this reduces total complexity;
- reduce coupling;
- remove premature generalization;
- remove architecture that exists only for hypothetical future requirements.

Do not introduce an abstraction merely to satisfy DRY.

Similar-looking code should be unified only when the resulting abstraction reduces real duplication and overall conceptual complexity.

Do not move game logic into Compose merely because it shortens a file.

## Architecture review

Explicitly determine whether:

- every current layer is still necessary;
- every class/module has a meaningful responsibility;
- interfaces with one implementation provide a real boundary benefit;
- classes or files can be safely merged;
- classes or files can be safely deleted;
- responsibilities are unnecessarily fragmented;
- there is speculative extensibility;
- duplicated state or logic creates coupling;
- Android/platform concerns leak into the pure engine;
- UI code has become an authority for game semantics;
- persistence duplicates authoritative run state unnecessarily;
- architecture complexity is proportional to the actual product.

Prefer the simplest architecture that fully supports the current product.

Do not perform a large rewrite merely because another architecture is newer, fashionable, or theoretically cleaner.

Do not introduce DI frameworks, databases, repositories, use-case layers, navigation frameworks, multiplatform structure, or other infrastructure unless the current product has a concrete need.

## Determinism audit

RERUNA's deterministic core is a first-class invariant.

Verify that:

- equivalent `GameState` plus equivalent `Direction` produces equivalent output;
- random state remains explicit;
- no global/random wall-clock source enters authoritative rule transitions;
- animation time, frame rate, haptics, sound, and UI timing cannot change game outcomes;
- seeded tests remain reproducible;
- a refactor does not silently change ordering semantics of collections where ordering affects a result;
- score and event derivation remain deterministic.

Do not update expected deterministic results merely to silence a regression without understanding and documenting the semantic change.

## Performance and resource review

Review performance where there is practical value.

Look for:

- repeated calculations;
- repeated reads;
- avoidable allocations;
- unnecessary copies;
- redundant loops;
- multiple passes that can safely become one;
- unnecessary per-move collection construction;
- unnecessary recomposition;
- expensive work in Compose drawing or input callbacks;
- persistence writes that occur more often than necessary;
- invariant operations performed repeatedly;
- data structures mismatched to actual access patterns;
- unbounded work introduced by future gameplay expansion.

Measure or otherwise demonstrate meaningful hot paths before making readability-reducing micro-optimizations.

Do not use device-dependent wall-clock thresholds as correctness tests.

Performance caches, if ever introduced, must remain derived, bounded, correctly keyed, and non-authoritative.

## Dependency review

Review every production and development dependency.

For each dependency:

- verify it is actually used;
- verify its purpose is still required;
- remove unused or obsolete dependencies;
- identify libraries serving overlapping purposes;
- consolidate overlapping dependencies when safe;
- prefer Kotlin, Android SDK, Compose, or existing-stack functionality when it clearly reduces total complexity;
- review native/JNI implications;
- review artifact-size impact where meaningful;
- review compatibility with the declared SDK/toolchain.

A dependency may be replaced by a small local implementation only when that clearly reduces total complexity, risk, maintenance cost, or artifact size.

Do not replace a mature, well-maintained library with custom code merely to reduce dependency count.

If native code is introduced or retained, verify required 64-bit ABI support and 16 KB memory page-size compatibility.

## Security and privacy review

Even an offline game can accumulate unnecessary privacy/security surface.

Verify that the refactor does not accidentally add or retain:

- unnecessary INTERNET or other permissions;
- analytics;
- trackers;
- advertising SDKs;
- remote configuration;
- identifiers;
- background services;
- exported Android components without a concrete reason;
- secrets or signing material;
- logging of unnecessary user/device data.

Review manifest exports and permissions.

Simplification must never weaken privacy or platform security merely to remove code.

## Non-negotiable behavior-preservation rules

Unless explicitly requested, repository-wide cleanup/refactoring must not intentionally:

- remove user-facing functionality;
- change current UX;
- change visual semantics;
- change input semantics;
- change game rules;
- change tuning values;
- change score behavior;
- change Rerun, Spark, Sync, Resonance, Combo, or Entropy behavior;
- change seeded determinism;
- change persisted data behavior;
- change Android/platform behavior;
- weaken compatibility;
- weaken accessibility;
- weaken security or privacy controls;
- reduce functionality solely to reduce code size;
- add unrelated functionality;
- introduce new architectural layers only to satisfy generic best practices;
- perform a broad rewrite without demonstrated need.

If a contemplated refactor would intentionally change gameplay, split that change into a separately reviewed product/behavior change.

## Required execution workflow

For a repository-wide audit or deep refactor:

1. Inspect the entire repository.
2. Establish architecture, feature set, user-visible behavior, internal contracts, authoritative state, persistence, deterministic rules, and platform constraints.
3. Run and record the available baseline verification.
4. Collect reliable baseline statistics where practical.
5. Build an internal candidate list grouped into:
   - deletion;
   - consolidation;
   - simplification;
   - architecture reduction;
   - dependency cleanup;
   - practical performance optimization.
6. Validate every removal against both direct and indirect usage.
7. Rank candidates by:
   - confidence;
   - regression risk;
   - complexity reduction;
   - maintenance benefit.
8. If critical behavior lacks coverage for a risky candidate, first add the smallest deterministic regression test that captures existing behavior.
9. Apply changes in small, logically coherent groups rather than one uncontrolled rewrite.
10. After every meaningful group, run the relevant checks.
11. Keep behavior-preserving refactoring separate from unrelated feature or tuning work.
12. When a candidate cannot be proven safe, leave it unchanged and record the reason.
13. Complete the first refactoring pass.
14. Perform a mandatory second full pass over the already-refactored repository.
15. During the second pass, look again for:
    - newly exposed simplifications;
    - remaining dead code;
    - residual duplication;
    - unnecessary abstractions;
    - unnecessary wrappers;
    - redundant checks;
    - unused dependencies;
    - residual legacy;
    - temporary structures made obsolete by the first pass.
16. Review repository hygiene relevant to the audit, including obsolete PRs/branches only when their status can be established safely.
17. Finish with the complete available verification suite.
18. Compare before/after statistics using the same method.

## Validation requirements

The final verification should include every applicable repository check.

Current baseline commands:

```text
gradle --no-daemon :app:testDebugUnitTest
gradle --no-daemon :app:lintDebug
gradle --no-daemon :app:assembleDebug
gradle --no-daemon :app:bundleDebug
```

Also run, when applicable:

- release build/bundle verification;
- manifest/permission review;
- dependency/security review;
- SDK/toolchain compatibility checks;
- native ABI/16 KB checks;
- device/emulator smoke test;
- accessibility checks;
- deterministic regression/stress tests;
- project-specific CI validation.

Do not claim a check passed unless it actually executed successfully.

If a check cannot be run because of environment, SDK availability, credentials, signing material, hardware, platform, or another limitation, state that explicitly.

## Comments during refactoring

Keep comments to the minimum necessary and English-only.

Remove stale, misleading, redundant, and commented-out historical code.

Do not add comments that merely narrate obvious code.

Retain comments that explain a non-obvious reason, invariant, platform constraint, compatibility requirement, performance bound, or architectural contract.

Do not replace clear code with explanatory comments when the code itself can be made self-explanatory.

## Completion standard

A repository-wide audit/refactoring task is not complete merely because:

- code was formatted;
- static analysis was run;
- recommendations were listed;
- potential improvements were described;
- only recently modified files were reviewed.

Safe and justified improvements must be implemented directly.

The final codebase should be objectively smaller, simpler, less duplicated, less coupled, easier to reason about, safer, or more efficient while preserving behavior.

## Required final report

At completion, report:

1. **Removed**
   - dead code;
   - unused files/resources;
   - obsolete compatibility code;
   - unnecessary abstractions;
   - other removed components.

2. **Consolidated**
   - duplicated logic;
   - equivalent components;
   - repeated validation;
   - shared responsibilities.

3. **Simplified**
   - control flow;
   - state;
   - architecture;
   - transformations;
   - hot paths.

4. **Dependencies**
   - dependencies removed;
   - overlapping dependencies consolidated;
   - dependency changes intentionally not made and why.

5. **Legacy**
   - legacy components found;
   - which were removed;
   - which remain and why.

6. **Intentionally unchanged**
   - candidates reviewed but preserved;
   - why removal or simplification could not be proven safe.

7. **Determinism**
   - deterministic invariants checked;
   - relevant regression coverage.

8. **Verification**
   - tests;
   - lint;
   - builds;
   - static/dependency/security checks;
   - device/platform checks;
   - results.

9. **Limitations**
   - areas that could not be safely optimized;
   - missing coverage;
   - unavailable environment/tooling;
   - external constraints.

10. **Before/after statistics**, when measurable:
    - file count;
    - source line count;
    - dependency count;
    - test count;
    - artifact size;
    - other relevant metrics.

Never invent statistics.

Use the same counting method for before and after values.

## Final principle

When choosing between preserving working code whose necessity is uncertain and deleting code because it appears unnecessary, preserve the working behavior until the code is proven unnecessary.

The final goal is a codebase containing only the complexity required to implement RERUNA's current functionality: as small, clean, understandable, maintainable, deterministic, and efficient as reasonably possible, without functional regressions.
