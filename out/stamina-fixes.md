# Stamina & Rest Spots — Review Fixes Applied

**Date:** 2026-02-18

## Fixes Applied

### BUG-1 (Medium) — Fixed idle recovery scaling
**File:** `Sheep.java`, line 231
Changed `fatigue -= FATIGUE_RECOVERY_IDLE * fatigueScale` to `fatigue -= FATIGUE_RECOVERY_IDLE / fatigueScale`. Idle recovery now correctly slows at higher difficulty instead of speeding up.

### LOW-1 — Excluded rest spots from runtime spawning
**File:** `SheepGame.java`, `createNewObject()`
Added `!isOverlapingRestingSpots(grass)` and `!isOverlapingRestingSpots(water)` checks before adding new Grass/Water objects. Mirrors the guard already present in `initilize()`.

### LOW-2 — Scaled restPower with difficulty
**Files:** `RestingSpot.java`, `SheepGame.java`
`restPower` is now computed as `Math.max(0.5, 2.0 / (1.0 + (difficultyLevel - 1) * 0.08))` via a new `difficultyLevel` constructor parameter. Yields ~2.0 at difficulty 1, ~1.1 at difficulty 10.

### LOW-3 — Fixed retry budget in initilize()
**File:** `SheepGame.java`, `initilize()`
Replaced shared attempt pool with per-spot 20-attempt budget using nested loops. Each spot now gets its own full retry budget.

### LOW-4 — Prevented double recovery when idle on rest spot
**Files:** `Sheep.java`, `SheepGame.java`
Added `private boolean onRestSpot` field (exposed via Lombok `@Setter`). `SheepGame.checkCollisions()` sets the flag before iterating rest spots. `Sheep.tick()` skips idle recovery when `onRestSpot` is true.

### NIT-1 — Removed redundant `implements Drawable`
**File:** `RestingSpot.java`
Removed `implements Drawable` from class declaration since it is already inherited through `LocatableShape -> Shape -> Drawable`.

### NIT-3 — Fixed stamina bar truncation
**File:** `SheepHungerPanel.java`
Changed `(int) fatigue` to `(int) Math.round(fatigue)` for accurate stamina bar display.

## Skipped (as directed)
- NIT-2: Lombok inconsistency — pre-existing project-wide pattern
- NIT-4: Label alignment — cosmetic, out of scope

## Build
`./gradlew build` — BUILD SUCCESSFUL
