# Code Review: Wolf Predator Feature

**Reviewer:** code-reviewer agent
**Date:** 2026-02-18
**Files reviewed:**
- `src/main/java/com/saygindogu/sheepgame/model/Wolf.java` (new)
- `src/main/java/com/saygindogu/sheepgame/SheepGame.java` (modified)
- `src/main/java/com/saygindogu/sheepgame/ui/SheepHungerPanel.java` (modified)

**Supporting context read:**
- `LocatableShape.java`, `Shape.java`, `Locatable.java`, `Drawable.java`
- `Sheep.java`, `Grass.java`, `Water.java`
- `SheepGameVisualViewPanel.java`, `SheepGameKeyListener.java`, `SheepGameView.java`
- `memory/developer.jsonl`, `memory/game-designer.jsonl`

---

## Summary Verdict

The implementation is **mostly correct and well-structured**. Timer lifecycle is safe, threading stays on the EDT throughout, and the overall architecture matches the existing project patterns. There are several issues worth addressing, ranging from a genuine bug to minor style inconsistencies.

---

## Issue Index

| # | Severity | File | Topic |
|---|----------|------|-------|
| 1 | BUG | `Wolf.java` | Duplicate `@Getter` annotation redundancy with explicit accessor overrides |
| 2 | BUG | `SheepGame.java` | `getDrawables()` is not safe against concurrent list modification |
| 3 | BUG | `SheepGame.java` | `physicsTimer` callback iterates `wolves` while `checkCollisions()` may return early – ordering is fragile |
| 4 | BUG | `SheepHungerPanel.java` | Warning threshold (150 px) does not match detection radius used by Wolf |
| 5 | MEDIUM | `Wolf.java` | Lombok `@Getter` on class conflicts with hand-written `getLocationX()` / `getLocationY()` |
| 6 | MEDIUM | `SheepGame.java` | `wolves` list is not initialized before `initilize()` is called, but `physicsTimer` references it |
| 7 | MEDIUM | `SheepGame.java` | `createNewObject()` re-instantiates `random` on every call |
| 8 | MEDIUM | `Wolf.java` | `Wolf` does not stop the sheep's internal timer on kill; only `SheepGame.checkCollisions()` calls `sheep.die()` |
| 9 | LOW | `Wolf.java` | `tick(int sheepX, int sheepY)` accepts int coordinates while wolf positions are `double` |
| 10 | LOW | `SheepGame.java` | `isGameOver()` has side-effects (stops timers) inside what reads like a pure query |
| 11 | LOW | `Wolf.java` | `Wolf` spawns using `SheepGame.GAME_SIZE_*` constants via static coupling in a model class |
| 12 | LOW | `SheepGame.java` | `difficultyLevel` clamp runs after `new Sheep(difficultyLevel)` uses the raw unclamped value |
| 13 | STYLE | `Wolf.java` | `new Color(...)` objects are allocated on every paint call |
| 14 | STYLE | `SheepGame.java` | `wolves` field is package-private; all other state-bearing fields follow the same pattern, but `wolves` is also exposed via `@Getter` on a mutable `ArrayList` |

---

## Detailed Findings

### Issue 1 — BUG: `@Getter` on `Wolf` class conflicts with hand-written accessors

**File:** `Wolf.java`, lines 12, 187-195

`Wolf` is annotated `@Getter` at class level. This generates getters for **all** fields, including `xPos`, `yPos`, `xLocation`, `yLocation`. However, `getLocationX()` and `getLocationY()` are also hand-written to satisfy the `Locatable` interface:

```java
@Override
public int getLocationX() { return xLocation; }

@Override
public int getLocationY() { return yLocation; }
```

Lombok will also generate `getXLocation()` and `getYLocation()` (camel-cased from field names `xLocation`/`yLocation`), which are **not** the same method names as the interface requires. The hand-written methods satisfy `Locatable` correctly, but the class now has **both** `getXLocation()` (Lombok) and `getLocationX()` (manual). This is the same pattern that `Sheep.java` uses (it also has `@Getter` at class level plus manual `getLocationX()` / `getLocationY()`), so it is consistent with the existing project convention — but it is confusing and generates dead generated getters.

**Comparison with `Sheep.java`:** `Sheep` has the same redundancy. Both classes should either drop the class-level `@Getter` and annotate fields individually, or make `Locatable` use `getXLocation()` naming. As-is it is not a compilation error but produces unnecessary API surface.

**Recommendation:** Annotate only the fields that truly need Lombok getters (e.g., `chaseSpeed`, `roamSpeed`, `detectionRadius`, `state`). The coordinate fields should keep their manual overrides only.

---

### Issue 2 — BUG: `getDrawables()` is not safe against concurrent modification

**File:** `SheepGame.java`, lines 247-259

```java
public Drawable[] getDrawables() {
    Drawable[] drawables = new Drawable[ otherObjects.size() + wolves.size() + 1];

    int idx = 0;
    for( int i = 0; i < otherObjects.size(); i++){
        drawables[idx++] = otherObjects.get(i);
    }
    for( int i = 0; i < wolves.size(); i++){
        drawables[idx++] = wolves.get(i);
    }
    drawables[idx] = sheep;
    return drawables;
}
```

This method is called from `paintComponent()` on the EDT. The `physicsTimer` fires on the EDT too (all `javax.swing.Timer` callbacks run on the EDT), so in practice there is **no concurrent access from a background thread**. However:

1. `createNewObject()` adds to `otherObjects` mid-game. The `timer` callback calls `createNewObject()` then `updateViews()`. Since both `timer` and `physicsTimer` fire on the EDT, true concurrent modification cannot occur. But `updateViews()` is called from **inside** `createNewObject()` (line 166) **before** `timer`'s own `updateViews()` call at line 55. This means one `updateViews()` call per timer tick will occur at a point when `otherObjects` has already been mutated but the caller (`timer` action listener) has not finished. This double `updateViews()` in one timer cycle is harmless but wasteful.

2. A more serious latent risk: if any future code ever calls `updateViews()` or `getDrawables()` off the EDT (e.g., from a worker thread), this becomes a `ConcurrentModificationException` waiting to happen because `otherObjects` is a plain `ArrayList`. The `wolves` list shares the same risk. Defensive practice would be to copy the list snapshot at the start of `getDrawables()` or to use `CopyOnWriteArrayList`.

3. The array is sized by `otherObjects.size() + wolves.size() + 1` at the top of the method, but the loop iterates up to the **current** size. Because everything is on the EDT, the size cannot change mid-call, but the snapshot approach (sizing the array then iterating) would be safer to make this reasoning obvious.

**Recommendation:** No immediate fix is required for a single-threaded Swing application, but add a comment making the EDT-only invariant explicit. If `wolves` or `otherObjects` ever grow to be shared, switch to `CopyOnWriteArrayList`.

---

### Issue 3 — BUG: ordering of `wolf.tick()` and `checkCollisions()` allows a one-frame death delay

**File:** `SheepGame.java`, lines 57-63 and 92-99

The `physicsTimer` callback:

```java
physicsTimer = new Timer( 16, e -> {
    sheep.tick();
    for( Wolf w : wolves ){
        w.tick( sheep.getLocationX(), sheep.getLocationY());
    }
    updateViews();
});
```

`updateViews()` calls `checkCollisions()`. So the sequence per tick is:
1. Sheep moves.
2. All wolves move.
3. Collision check runs.

This is the correct order: wolf positions are updated before the collision check runs, so kills are detected in the same frame the wolf walks onto the sheep. This is fine.

However, `checkCollisions()` is **also** called from `updateViews()`, which is called from `createNewObject()` (the `timer` callback). That means a wolf-sheep collision is checked after every grass/water spawn event as well, not just every physics tick. This is redundant (wolves do not move between those calls) but harmless. If the wolf-kill check were expensive it would be worth noting as a performance issue.

The only genuine concern is that on difficulty 1, `wolfCount = max(0, (1-1)/2) = 0`, so the `wolves` list is empty — the for-loop is a no-op and there is no issue. On difficulty 2, `wolfCount = max(0, (2-1)/2) = 0` as well (integer division). The first wolf does not appear until difficulty 3. This matches the designer spec `(difficultyLevel-1)/2` but differs from the designer's stated maximum of 4 wolves at difficulty 10 (`(10-1)/2 = 4`). This is intentional and correct.

---

### Issue 4 — BUG: Wolf warning threshold inconsistency

**File:** `SheepHungerPanel.java`, lines 60-70

```java
if( nearest <= 150 ){
    wolfWarning.setText( "WOLF! " + (int) nearest + "px");
    wolfWarning.setForeground( Color.RED);
    wolfWarning.setVisible( true);
} else if( nearest <= 400 ){
    ...
}
```

The red threshold is hardcoded at 150 px. However, the minimum detection radius (difficulty 1) is:

```java
double detectRadius = 150 + difficultyLevel * 25;  // difficulty 1 → 175 px
```

At difficulty 1 the wolf starts chasing at 175 px. The panel only shows red at 150 px — meaning the wolf is already chasing the sheep before the red warning fires. At difficulty 10 the detection radius is 400 px, which matches the orange threshold but not the red one.

More importantly, the warning thresholds are **magic numbers** that are disconnected from the actual `Wolf.detectionRadius` values. If the radius formula changes, the warning will silently become wrong again.

**Recommendation:** Pass the detection radius (or the minimum among all wolves' radii) into the warning logic, or expose a constant. At minimum, document why 150 and 400 were chosen and note they must be updated alongside the Wolf constructor formula.

---

### Issue 5 — MEDIUM: Lombok `@Getter` on class combined with manual accessor methods

Already detailed in Issue 1. This is explicitly flagged as a MEDIUM because it is a pattern already present in `Sheep.java` and therefore is not a new problem introduced by this PR, but it is worth consolidating.

---

### Issue 6 — MEDIUM: `wolves` initialization order

**File:** `SheepGame.java`, lines 39-67

Constructor flow:

```java
public SheepGame( int difficultyLevel){
    sheep = new Sheep(difficultyLevel);
    // ...
    physicsTimer = new Timer( 16, e -> {
        sheep.tick();
        for( Wolf w : wolves ){   // <-- references this.wolves
            w.tick(...);
        }
        updateViews();
    });
    initilize( 11 - difficultyLevel);   // sets this.wolves = new ArrayList<>()
    timer.start();
    physicsTimer.start();
}
```

The `physicsTimer` lambda captures `this.wolves` by reference. The lambda is created before `initilize()` sets `wolves = new ArrayList<>()`. This is safe in Java because the lambda reads the instance field `wolves` at the time the callback fires, not at construction time, and `physicsTimer.start()` is called after `initilize()`. So by the time any tick fires, `wolves` is non-null.

However, this is easy to misread as a null-pointer risk. If someone reorders the two lines `timer.start()` / `physicsTimer.start()` above `initilize()`, or if `Sheep`'s internal timer fires before `wolves` is set (it starts immediately in `new Sheep()`), there could be trouble. `Sheep`'s internal timer does not call `wolves` directly so there is no actual null path today, but the fragility should be noted.

**Recommendation:** Initialize `wolves = new ArrayList<>()` at the field declaration site, then populate it inside `initilize()`. This eliminates the window entirely and makes the code easier to read.

---

### Issue 7 — MEDIUM: `Random` re-instantiated every object creation

**File:** `SheepGame.java`, line 136

```java
private void createNewObject() {
    random = new Random();
    ...
}
```

`random` is a field, but it is replaced with a brand-new `Random()` instance on every call. This is wasteful (a `Random` instance is not free) and statistically suboptimal (repeatedly seeding from system time in fast succession is less random than reusing a single instance). It also masks that the field is effectively only used as a local variable.

This is a pre-existing issue unrelated to the wolf feature, but the wolf feature did not fix it either. The `Wolf` constructor correctly instantiates its own `this.rng = new Random()` once, which is the right pattern.

**Recommendation:** Initialize `random` once in the constructor or at field declaration, remove the re-instantiation from `createNewObject()`.

---

### Issue 8 — MEDIUM: Sheep's internal timer and `die()` lifecycle

**File:** `Wolf.java` / `SheepGame.java`

When a wolf reaches the sheep, `SheepGame.checkCollisions()` calls `sheep.die()`. `Sheep.die()` sets `isAlive = false` and calls `timer.stop()` on the sheep's internal hunger timer. This is correct.

`SheepGame.isGameOver()` then calls `timer.stop()` and `physicsTimer.stop()` on the game timers. `isGameOver()` is called from `paintComponent()` in `SheepGameVisualViewPanel`. This means the game timers do not stop until the next repaint after death — a potential extra tick could fire. The wolf timer tick calling `sheep.die()` again is harmless because `die()` is idempotent (setting `isAlive = false` twice and calling `timer.stop()` on an already-stopped timer is safe in Swing). But:

1. `physicsTimer` may fire one more tick after death and before `isGameOver()` is called in `paintComponent()`. In that tick, `checkCollisions()` will call `sheep.die()` again (idempotent, fine), and wolves will `tick()` on an already-dead sheep (also harmless but unnecessary).
2. The `isGameOver()` side-effect design (stopping timers inside what looks like a query) is the root cause of this mild race window (see Issue 10).

No actual bug results today, but the architecture creates a fragile dependency between the rendering path and the game-logic lifecycle.

---

### Issue 9 — LOW: `tick()` accepts `int` coordinates, losing precision

**File:** `Wolf.java`, line 68

```java
public void tick(int sheepX, int sheepY) {
```

Called as:

```java
w.tick( sheep.getLocationX(), sheep.getLocationY());
```

`getLocationX()` / `getLocationY()` return `int` values that are the rounded versions of `sheep.xPos` / `sheep.yPos` (the precise `double` positions). The wolf's direction computation therefore has up to ±0.5 px of rounding error in the target. At the speeds involved (chaseSpeed up to ~4.5 px/tick) this is negligible, but passing the precise `double` positions (which are accessible via `sheep.getXPos()` generated by Lombok) would be more accurate.

**Recommendation:** Change the signature to `tick(double sheepX, double sheepY)` and call `sheep.getXPos()` / `sheep.getYPos()`. This is a minor accuracy improvement but consistent with the double-precision movement model used throughout.

---

### Issue 10 — LOW: `isGameOver()` has side-effects

**File:** `SheepGame.java`, lines 69-77

```java
public boolean isGameOver(){
    if( !sheep.isAlive() )
    {
        timer.stop();
        physicsTimer.stop();
        return true;
    }
    return false;
}
```

This method is called from `SheepGameVisualViewPanel.paintComponent()` (on every repaint) and from `SheepGameKeyListener.keyPressed()`. A method named `isGameOver()` strongly implies a pure predicate — callers do not expect it to stop timers. This is the same pattern that existed before the wolf feature, but the wolf feature added a second call path (wolf kills trigger a repaint which triggers `paintComponent` which calls `isGameOver`).

Calling `timer.stop()` on an already-stopped timer is safe in Swing, so there is no double-stop bug, but the side-effect design is surprising and should be documented.

**Recommendation:** Rename or split: introduce a `stopGame()` method and have `isGameOver()` be a pure check. Call `stopGame()` explicitly when death is detected (in `checkCollisions()`), then `isGameOver()` can simply return `!sheep.isAlive()` with no side-effects.

---

### Issue 11 — LOW: `Wolf` constructor couples model to `SheepGame` constants

**File:** `Wolf.java`, lines 42-56

```java
xPos = rng.nextInt(SheepGame.GAME_SIZE_X);
```

This is the same pattern used in `Sheep.java` (`SheepGame.GAME_SIZE_X` referenced from a model class). It is an existing project-wide convention, so the wolf is consistent. Noted only to flag that if GAME_SIZE_* are ever made configurable, all model classes will need updating.

---

### Issue 12 — LOW: `difficultyLevel` clamping happens after `new Sheep(difficultyLevel)`

**File:** `SheepGame.java`, lines 39-51

```java
public SheepGame( int difficultyLevel){
    sheep = new Sheep(difficultyLevel);   // raw, unclamped value used here
    // ...
    if( difficultyLevel > 10){ this.difficultyLevel = 10; }
    else if( difficultyLevel <= 0){ this.difficultyLevel = 1; }
    else this.difficultyLevel = difficultyLevel;
```

If a caller passes `difficultyLevel = 0` or `difficultyLevel = 15`, `Sheep` receives the out-of-range value. The `Sheep` constructor uses `difficultyLevel` to compute `acceleration` and `maxSpeed` via `1.0 - (hardness - 1) * 0.05` — a value of 15 would yield `scale = 0.3` which is odd but not a crash. This was a pre-existing issue but should be fixed: clamp `difficultyLevel` first, then pass the clamped value to `new Sheep(...)`.

---

### Issue 13 — STYLE: `new Color(...)` in `draw()` allocates on every repaint

**File:** `Wolf.java`, lines 129-180

Every call to `Wolf.draw()` allocates approximately 14 `Color` objects. At 60 fps with 4 wolves this is 3360 `Color` allocations per second. These are short-lived objects and will be collected quickly, but the same pattern exists in `Sheep.draw()` and `Grass.draw()`. The `Sheep` class allocates even more per frame. For a hobby project this is acceptable; a production Swing game would make these `static final` constants.

**Recommendation (optional):** Declare the palette as `private static final Color BODY_COLOR = new Color(90, 90, 100)` etc. at the top of each model class.

---

### Issue 14 — STYLE: `wolves` field visibility and mutability exposure

**File:** `SheepGame.java`, line 32-33

```java
@Getter
List<Wolf> wolves;
```

The `@Getter` annotation generates a `getWolves()` returning the live `ArrayList`. Callers (e.g., `SheepHungerPanel.update()`) iterate over it:

```java
for( Wolf w : game.getWolves() ){
```

Returning the internal list directly allows callers to accidentally mutate it (add/remove wolves) outside `SheepGame`. Since all callers are currently read-only, this is not a bug today. Consistent with other `@Getter` usage in the project (e.g., `getSheep()` also returns the mutable `Sheep` reference).

**Recommendation (optional):** Return `Collections.unmodifiableList(wolves)` from the getter, or document that the returned list must not be modified.

---

## Positive Observations

1. **No new `javax.swing.Timer` introduced.** The wolf is ticked inside the existing `physicsTimer` (16 ms), exactly as the game designer specified. This is the correct approach — adding a new timer would complicate lifecycle management.

2. **EDT safety is maintained throughout.** Both `timer` and `physicsTimer` are `javax.swing.Timer` instances. All callbacks fire on the EDT. `Wolf.tick()`, `checkCollisions()`, and `SheepHungerPanel.update()` all run on the EDT. There are no background threads accessing shared state.

3. **State-machine hysteresis.** The 40% hysteresis on the ROAMING → CHASING transition (`detectionRadius * 1.4` for the exit) prevents rapid state oscillation when the sheep is near the boundary. This is a good design decision.

4. **`g2.dispose()` is called.** The `draw()` method correctly disposes the derived `Graphics2D` context. This matches the pattern in `Sheep`, `Grass`, and `Water`.

5. **Waypoint roaming.** The random waypoint system in `ROAMING` state adds interesting non-linear wolf movement without requiring complex pathfinding. Waypoint refresh when the wolf is within `speed * 2` pixels is a reasonable stopping condition.

6. **Collision detection reuses `LocatableShape.overlaps()`.** The wolf-sheep collision uses the same AABB `overlaps()` method already defined on `LocatableShape`, which is correct and avoids duplicating geometry logic.

7. **Visual differentiation by state.** The eye color changes from yellow (roaming) to amber/orange (chasing), giving the player a visual cue about wolf intent during gameplay. This is a nice UX touch.

8. **`returnToMenu()` cleanly stops both timers.** `SheepGame.returnToMenu()` calls `timer.stop()` and `physicsTimer.stop()` before nulling `currentGame`. This prevents ghost timer callbacks referencing a garbage-collected game instance.

---

## Priority Fix List

1. **(BUG, Issue 4)** Fix wolf warning thresholds in `SheepHungerPanel` — the red warning fires at 150 px but detection starts at 175 px minimum. The warning should trigger at or before the detection radius.

2. **(MEDIUM, Issue 6)** Initialize `wolves = new ArrayList<>()` at the field declaration to close the null-reference window between timer creation and `initilize()`.

3. **(LOW, Issue 12)** Clamp `difficultyLevel` before passing to `new Sheep()`.

4. **(LOW, Issue 10)** Separate `isGameOver()` (pure predicate) from timer-stopping side effects.

5. **(MEDIUM, Issue 7)** Remove `random = new Random()` from inside `createNewObject()`.

6. **(LOW, Issue 9)** Change `tick(int, int)` to `tick(double, double)` and use precise sheep position.

---
