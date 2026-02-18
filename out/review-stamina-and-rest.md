# Code Review: Stamina & Rest Spots Feature

**Date:** 2026-02-18
**Reviewer:** code-reviewer agent
**Files reviewed:**
- `src/main/java/com/saygindogu/sheepgame/model/RestingSpot.java` (new)
- `src/main/java/com/saygindogu/sheepgame/model/Sheep.java` (modified)
- `src/main/java/com/saygindogu/sheepgame/SheepGame.java` (modified)
- `src/main/java/com/saygindogu/sheepgame/ui/SheepHungerPanel.java` (modified)

**Supporting files read for context:** `LocatableShape.java`, `Wolf.java`, `Grass.java`, `Water.java`, `Shape.java`, `Locatable.java`

**Prior session patterns applied:** Class-level `@Getter` generating dead Lombok getters; model-layer coupling to `SheepGame` constants; `isGameOver()` side-effect design.

---

## Verdict: Mostly Correct — 1 Medium Bug, 4 Low Issues, 4 Nitpicks

---

## Bugs

### BUG-1 (Medium) — `fatigueScale` makes idle recovery FASTER at higher difficulty

**File:** `Sheep.java`, line 231

```java
fatigue -= FATIGUE_RECOVERY_IDLE * fatigueScale;
```

`fatigueScale` is `1.0 + (hardness - 1) * 0.06`. At difficulty 10, `fatigueScale = 1.54`. This means idle recovery is 54% faster at maximum difficulty, which is the opposite of the intended effect. Fatigue accumulation is correctly scaled up by `fatigueScale` (line 229), but recovery is also scaled up, partially negating the difficulty increase. At difficulty 1 the idle recovery rate is `0.03`; at difficulty 10 it is `0.0462`. The player at harder difficulty recovers fatigue faster when idle, which undermines the design.

The fix is to divide rather than multiply by `fatigueScale` for the recovery branch:

```java
fatigue -= FATIGUE_RECOVERY_IDLE / fatigueScale;
```

---

## Low Issues

### LOW-1 — `createNewObject()` does not exclude resting spot areas

**File:** `SheepGame.java`, `createNewObject()` / `isOverlaping()`, lines 165–196, 198–209

`isOverlaping(shape)` checks only `otherObjects`. `restingSpots` is a separate list that is never consulted during `createNewObject()`. This means Grass and Water objects spawned during gameplay (from the `timer` callback) can overlap existing resting spots. The sheep can then stand on a rest spot, have a food/water item underneath it, and consume that item through the rest spot visually. The fix is to also check `isOverlapingRestingSpots(shape)` inside `createNewObject()`, mirroring the placement guard used in `initilize()`.

### LOW-2 — `restPower` is not difficulty-scaled

**File:** `RestingSpot.java`, line 21; `SheepGame.java` `initilize()`, line 145

```java
private final double restPower = 2.0;
```

Rest spot count and size are both reduced at higher difficulty (lines 143–147 of `SheepGame.java`), but `restPower` is a fixed constant. Recovery speed per tick does not decrease with difficulty. At difficulty 10 the player has fewer, smaller rest spots but they recover stamina at the same per-tick rate as difficulty 1. This is a design gap. If the intent is that higher difficulty is more punishing, `restPower` should either be passed as a constructor parameter scaled by difficulty, or acknowledged as an intentional floor.

### LOW-3 — Rest spot placement loop has a shared attempt budget, not per-spot

**File:** `SheepGame.java`, `initilize()`, lines 148–162

```java
int maxAttempts = restCount * 20;
int attempts = 0;
for (int i = 0; i < restCount && attempts < maxAttempts; i++) {
    ...
    attempts++;
    if (!isOverlaping(rs) && !isOverlapingRestingSpots(rs)) {
        restingSpots.add(rs);
    } else {
        i--; // retry placement
    }
}
```

`maxAttempts` is a pool shared across all spots. If early spots happen to require many retries, later spots receive fewer chances than the 20 implied by the constant name. This can silently produce fewer rest spots than `restCount` without any log or fallback. For `restCount = 4` and a cluttered board, the game might silently produce only 2 or 3 spots. The minimum of 1 from `Math.max(1, ...)` only guarantees `restCount >= 1`, not that at least 1 spot is successfully placed. Recommend tracking successful placements or using a per-spot attempt counter.

### LOW-4 — Double fatigue recovery when sheep is idle on a rest spot

**File:** `Sheep.java` `tick()` line 231; `SheepGame.java` `checkCollisions()` lines 103–107; `SheepGame.java` `physicsTimer` lambda lines 58–64

The physics timer fires `sheep.tick()` first, then `updateViews()` which calls `checkCollisions()` which calls `sheep.rest(rs)`. On a tick where the sheep is both idle (speed < 0.1) and overlapping a rest spot, both recovery paths fire:

1. `fatigue -= FATIGUE_RECOVERY_IDLE * fatigueScale` (inside `tick()`)
2. `fatigue -= rs.getRestPower()` (2.0) (inside `rest()` called from `checkCollisions()`)

The combined recovery per tick while idle on a rest spot is `FATIGUE_RECOVERY_IDLE * fatigueScale + restPower`. For difficulty 1 that is `0.03 + 2.0 = 2.03` per tick, versus 2.0 when actively moving onto the spot. This is negligible in practice and arguably intended, but it is not documented and not obvious from reading either method in isolation.

---

## Nitpicks

### NIT-1 — Redundant `implements Drawable` on `RestingSpot`

**File:** `RestingSpot.java`, line 13

```java
public class RestingSpot extends LocatableShape implements Drawable {
```

`LocatableShape` implements `Shape`, and `Shape extends Drawable`. `RestingSpot` inherits `Drawable` through the superclass hierarchy. The explicit `implements Drawable` declaration is harmless but redundant, and is inconsistent with `Wolf` (which also has it) versus `Grass` and `Water` (which do not). Pre-existing project inconsistency — worth unifying.

### NIT-2 — Recurring pattern: class-level `@Getter` generates dead getters alongside manual overrides

**File:** `RestingSpot.java`, lines 12, 23–26

`RestingSpot` uses `@Getter` at the class level, which generates `getLocationX()`, `getLocationY()`, `getWidth()`, `getHeight()`. These are required to satisfy `Locatable` and `Shape`. However, they are backed by fields named `locationX`, `locationY`, so Lombok generates `getLocationX()` / `getLocationY()` which match the `Locatable` contract. This works correctly, but note that `LocatableShape` provides abstract `getLocationX()` / `getLocationY()` via the `Locatable` interface — Lombok satisfies these without any manual override here. Unlike `Sheep` and `Wolf` which declare manual `getLocationX()` / `getLocationY()` overrides (conflicting with class-level `@Getter`), `RestingSpot` is cleaner. The project-wide inconsistency remains, but this file does not add new dead getter pairs.

### NIT-3 — `stamina.setValue()` truncates rather than rounds

**File:** `SheepHungerPanel.java`, line 59

```java
stamina.setValue(Sheep.MAX_FATIGUE - (int) fatigue);
```

`(int) fatigue` truncates toward zero. At `fatigue = 99.9`, this gives `100 - 99 = 1` (bar almost empty) rather than `100 - 100 = 0`. `Math.round()` or `(int) Math.round(fatigue)` would be more accurate. The effect is one display unit of error at most and does not affect game logic.

### NIT-4 — `SheepHungerPanel` label spacing does not align with bar columns

**File:** `SheepHungerPanel.java`, line 27

```java
label = new JLabel("Hunger  Thirst  Energy");
```

The label uses manual double-space padding to attempt visual alignment with the three bars in the `GridLayout(1,3)` below it. Because the label is a single `JLabel` in `BorderLayout.NORTH` and the bars are in a `GridLayout`, pixel alignment depends on font metrics and is not guaranteed. Using three separate `JLabel` headers inside the `GridLayout` alongside the bars (or a second `GridLayout` row for headers) would produce reliable alignment.

---

## What Is Correct

- **RestingSpot never removed from list in `checkCollisions()`**: Confirmed. Lines 103–107 of `SheepGame.java` iterate `restingSpots` and call `sheep.rest()` but never call `it.remove()` or `restingSpots.remove()`. Correct.

- **Draw order in `getDrawables()`**: Rest spots are placed at indices 0..N-1, before `otherObjects`, wolves, and sheep. The visual panel draws them in array order, so rest spots are rendered first (underneath all other game objects). Correct.

- **No new `javax.swing.Timer` instances**: This PR introduces no new Timer. The existing `Sheep.timer` (hunger/thirst countdown) was pre-existing. `SheepGame` uses the two pre-existing timers (`timer`, `physicsTimer`). Correct.

- **EDT threading safety**: Both `timer` and `physicsTimer` are `javax.swing.Timer`, which fires callbacks on the Event Dispatch Thread. `checkCollisions()`, `sheep.tick()`, wolf ticks, and all UI updates run on the EDT. No background threads are introduced. Correct.

- **ConcurrentModificationException safety**:
  - `wolves` loop in `checkCollisions()` (line 95): for-each on a list that is never modified mid-iteration. Safe.
  - `restingSpots` loop in `checkCollisions()` (line 103): for-each on a list that is never modified. Safe.
  - `otherObjects` loop in `checkCollisions()` (line 109): uses explicit `Iterator` with `it.remove()`. Safe.
  - `restingSpots` loop in `isOverlapingRestingSpots()`: read-only iteration. Safe.

- **Overlap check for rest spots covers both `otherObjects` and other rest spots**: `initilize()` calls `isOverlaping(rs)` (checks `otherObjects`) AND `isOverlapingRestingSpots(rs)` (checks already-placed rest spots) before adding each new rest spot. Both lists are checked. Correct.

- **Fatigue tick logic — effectiveMaxSpeed only caps velocity, not acceleration**: Lines 186–189 of `Sheep.java` apply acceleration unconditionally (not reduced by fatigue). The velocity cap at lines 200–205 applies `effectiveMaxSpeed`. Fatigue makes the sheep slower but does not reduce responsiveness or acceleration. This matches the stated design intent.

- **Fatigue clamped to `[0, MAX_FATIGUE]`**: Line 233 of `Sheep.java` clamps fatigue after every tick. Correct.

- **SheepHungerPanel layout handles 3 bars**: `GridLayout(1, 3)` with hunger, thirst, stamina bars. Correct number of cells, correct bar orientations (`SwingConstants.VERTICAL`).

- **Static final colors in `RestingSpot`**: All four colors are `private static final Color` constants. Correct, consistent with the `Wolf` pattern established in the prior review.

- **`g2.dispose()` always called**: `RestingSpot.draw()` calls `g2.dispose()` at line 64. Correct.

- **`Lombok @Getter` on `RestingSpot`**: Generates `getRestPower()`, `getLocationX()`, `getLocationY()`, `getWidth()`, `getHeight()` — all required by the type hierarchy. No dead getters are generated by this class (unlike `Wolf` and `Sheep` which have redundant manual overrides).

- **`rest()` method in `Sheep`**: Correctly delegates to `Math.max(0.0, fatigue - rs.getRestPower())` so fatigue never goes negative via rest. Consistent with the `Math.max(0, hunger - ...)` pattern in `eat()`.

---

## Summary Table

| Severity | ID | Description |
|---|---|---|
| Medium | BUG-1 | `fatigueScale` makes idle recovery faster at higher difficulty (should divide, not multiply) |
| Low | LOW-1 | `createNewObject()` does not check rest spot overlap — food/water can spawn on rest spots |
| Low | LOW-2 | `restPower` is not difficulty-scaled despite size and count being scaled |
| Low | LOW-3 | Shared attempt budget for rest spot placement may silently produce fewer spots than `restCount` |
| Low | LOW-4 | Double recovery (tick idle + rest spot) per tick when sheep is standing still on a rest spot |
| Nitpick | NIT-1 | Redundant `implements Drawable` on `RestingSpot` |
| Nitpick | NIT-2 | Recurring project pattern: class-level `@Getter` inconsistency |
| Nitpick | NIT-3 | `(int) fatigue` truncates; `Math.round()` would be more accurate for stamina bar |
| Nitpick | NIT-4 | Single `JLabel` header does not align with `GridLayout` bar columns |
