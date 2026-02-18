# Wolf Predator Review Fixes

**Date:** 2026-02-18
**Review:** out/review-wolf-predator.md

## Fixes Applied

### Issue 6 (MEDIUM) — `wolves` field null before `initilize()`
**File:** `SheepGame.java`
Changed `List<Wolf> wolves;` to `List<Wolf> wolves = new ArrayList<>();` at the field declaration. This eliminates the NPE window between timer creation and `initilize()`.

### Issue 7 (MEDIUM) — `Random` re-instantiated every `createNewObject()` call
**File:** `SheepGame.java`
Changed `Random random;` to `Random random = new Random();` at the field declaration. Removed the `random = new Random();` line inside `createNewObject()`. The single instance is now reused across all calls, consistent with how `Wolf.rng` is handled.

### Issue 4 (BUG) — Wolf warning thresholds disconnected from detection radius
**File:** `SheepHungerPanel.java`
Replaced magic-number thresholds (150px red, 400px orange) with values derived from the nearest wolf's actual `detectionRadius`. Red warning now fires when the sheep is within the wolf's detection radius (i.e., when the wolf is chasing). Orange warning fires at 1.5x the detection radius as an early alert. The panel now tracks `nearestDetectRadius` alongside `nearest` distance.

### Issue 13 (STYLE) — `new Color(...)` allocated on every `draw()` call
**File:** `Wolf.java`
Extracted 8 Color objects as `private static final` constants: `LEG_COLOR`, `BODY_COLOR`, `BODY_OUTLINE_COLOR`, `TAIL_COLOR`, `HEAD_COLOR`, `SNOUT_COLOR`, `EYE_CHASING_COLOR`, `EYE_ROAMING_COLOR`. The `draw()` method now references these constants instead of allocating new Color instances per frame.

### Issue 12 (LOW) — `difficultyLevel` clamped after `new Sheep()` consumed raw value
**File:** `SheepGame.java`
Moved the clamping block (`if > 10 ... else if <= 0 ...`) to the top of the constructor, before `new Sheep(this.difficultyLevel)`. Also ensured the `timer` interval and `initilize()` call use `this.difficultyLevel` (the clamped value) rather than the raw parameter.

## Issues Not Changed (per instructions)
- Issue 1/5: Lombok `@Getter` redundancy with `getLocationX/Y` — matches pre-existing Sheep.java pattern
- Issue 2: `getDrawables()` snapshot safety — safe today on EDT
- Issue 9: `int` precision in `tick()` — negligible
- Issue 10: `isGameOver()` side-effects — pre-existing, out of scope

## Build Verification
`./gradlew build` passed with BUILD SUCCESSFUL, no compilation errors or test failures.
