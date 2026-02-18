# Feature Spec: Stamina & Rest Spots

**Authored by:** game-designer agent
**Date:** 2026-02-18
**Status:** Proposed

---

## 1. Goal

The player feels a genuine tension between moving fast (to reach food or escape the wolf) and burning out — creating a layered decision loop where *how* you move is as important as *where* you go.

---

## 2. Design Rationale

### The Problem With the Current Loop

Every decision in the game right now is reactive and first-order:
- Hunger low → move toward nearest grass.
- Thirst low → move toward nearest water.
- Wolf nearby → flee.

There is no planning horizon. The player never has to think more than one step ahead, and the physics-based movement (momentum, friction) is the only dimension that makes navigation feel skillful.

### The Dimension Being Added

Fatigue is a resource that the *player's own choices* generate. Moving at speed costs stamina; standing still or resting on a RestingSpot recovers it. When fatigue reaches its cap, the sheep's maximum speed drops sharply — a meaningful but survivable penalty that forces the player to stop sprinting and manage their movement budget.

This changes spatial reasoning from "path to nearest resource" into "path to nearest resource *via* the cheapest route in terms of stamina cost." Near a wolf, sprinting is correct. Without a wolf, a slightly longer path through shade spots may be better to preserve speed for the next emergency.

The mechanic is also inherently self-balancing: a player who moves carelessly exhausts themselves; a player who is very scared of the wolf and keeps sprinting will paradoxically become slower and more vulnerable. This is satisfying counterintuitive design.

---

## 3. New Model Object: `RestingSpot`

### Location

New file: `src/main/java/com/saygindogu/sheepgame/model/RestingSpot.java`
Package: `com.saygindogu.sheepgame.model`
Extends: `LocatableShape`
Implements: `Drawable` (via `Shape` → `Drawable` inheritance chain, same as `Grass` and `Water`)

### Stats

| Field | Value / Formula |
|-------|----------------|
| `restPower` | Recovery rate in stamina points per physics tick while sheep overlaps the spot |
| Size (width) | `random in [max(30, (int)(60 * scale)), max(30, (int)(60 * scale)) + max(20, (int)(40 * scale))]` — deliberately larger than grass/water so they feel welcoming |
| Size (height) | Same range as width |
| Spawn position | Fully random across 1200×800, same as other resources |

`restPower` should be fixed at **2.0 stamina/tick** regardless of difficulty (see difficulty interaction below for why).

### Visual Design

Draw as a rounded oval patch of soft brown/tan earth with dappled shadow. Suggestion for `draw(Graphics g)`:

- Fill an oval with `new Color(180, 150, 100)` (warm sand/earth tone).
- Overlay a semi-transparent dark ellipse `new Color(60, 40, 20, 60)` for a shadow feel.
- Draw 3–5 small irregular dots (pebbles) in `new Color(140, 110, 70)` for texture.
- Add a thin outline stroke in `new Color(120, 90, 50)`.

The spot must be visually distinct from both grass (green) and water (blue). Tan/brown earth works well and is thematically coherent (the sheep rests in a shady patch of bare earth under an implied tree).

---

## 4. New Sheep Stat: `fatigue`

### Location

All changes in `src/main/java/com/saygindogu/sheepgame/model/Sheep.java`

### Constants to Add

```java
public static final int MAX_FATIGUE = 100;          // die threshold — NOT used for death; see penalty below
private static final double BASE_FATIGUE_RATE = 0.08;  // stamina lost per physics tick while moving at full speed
private static final double FATIGUE_RECOVERY_IDLE = 0.03; // stamina recovered per tick while still (not on rest spot)
```

### Field to Add

```java
@Getter
private double fatigue;   // 0.0 = fresh, 100.0 = fully exhausted
```

Initialize to `0.0` in the constructor.

### Fatigue Tick Logic (inside `tick()`)

Fatigue accumulates based on current speed magnitude. Formula:

```
currentSpeed = Math.hypot(vx, vy)
speedRatio = currentSpeed / maxSpeed        // 0.0 when still, 1.0 at top speed

if speedRatio > 0.1:
    fatigue += BASE_FATIGUE_RATE * speedRatio * difficultyScale
else:
    fatigue -= FATIGUE_RECOVERY_IDLE * difficultyScale   // passive idle recovery
fatigue = clamp(fatigue, 0.0, MAX_FATIGUE)
```

Where `difficultyScale` for fatigue is **`1.0 + (difficulty - 1) * 0.06`** (inverted compared to speed scale — harder difficulties mean fatigue accumulates faster and recovers slower). At difficulty 1 the scale is 1.0; at difficulty 10 it is 1.54.

### Speed Penalty When Exhausted

**Do not kill the sheep from fatigue.** Instead, apply a progressive speed cap reduction:

```
exhaustionRatio = fatigue / MAX_FATIGUE    // 0.0 to 1.0
effectiveMaxSpeed = maxSpeed * (1.0 - 0.60 * exhaustionRatio)
```

At full exhaustion (fatigue = 100), the sheep's effective top speed is reduced to **40% of `maxSpeed`**. This makes the sheep feel sluggish and vulnerable, but does not end the run. The player must find a rest spot or stop moving to recover.

The penalty is applied inside `tick()` when clamping velocity — replace the direct use of `maxSpeed` as the velocity cap with `effectiveMaxSpeed`.

### Resting on a RestingSpot

Add a new method to `Sheep`:

```java
public void rest(RestingSpot rs) {
    fatigue = Math.max(0.0, fatigue - rs.getRestPower());
}
```

This is called from `SheepGame.checkCollisions()` (see Section 6) on every physics tick while the sheep overlaps the spot. Unlike grass/water, the rest spot is **not removed on contact** — it is a persistent environmental feature. The sheep must remain on it to keep recovering.

---

## 5. New UI Elements

### Fatigue Bar in `SheepHungerPanel`

Add a third `JProgressBar` (vertical, same style as hunger/thirst) labeled "Stamina" or "Energy."

- Bar maximum: `MAX_FATIGUE` (100)
- Bar value: `MAX_FATIGUE - (int) sheep.getFatigue()` (so full bar = fresh, empty bar = exhausted)
- Color scheme: when fatigue > 70, bar foreground turns **orange**; when fatigue > 90, it turns **red**. This can be set dynamically in `SheepHungerPanel.update()` using `progressBar.setForeground(Color)`.

No additional warning label is needed — the bar color change is sufficient warning.

### On-Canvas Visual Feedback (Optional Enhancement)

When the sheep's fatigue exceeds 80, briefly tint the sheep sprite with a reddish overlay or add a small "panting" animation (e.g., the white body pulses slightly lighter and darker). This is a lower-priority polish item — the developer should implement the bar first and treat this as a stretch goal.

---

## 6. Changes to Existing Logic

### `SheepGame.java` — `initilize()`

1. Add a `List<RestingSpot> restingSpots = new ArrayList<>()` field alongside `wolves` and `otherObjects`.
2. In `initilize()`, after creating wolves, spawn an initial set of rest spots:
   ```
   int restCount = Math.max(1, 4 - difficultyLevel / 3);
   ```
   This yields:
   | Difficulty | Initial rest spots |
   |---|---|
   | 1–3 | 3 |
   | 4–6 | 2 |
   | 7–9 | 1 |
   | 10 | 1 |

3. Spawn each rest spot the same way as grass/water: random position, check `isOverlaping()` before adding.

### `SheepGame.java` — `checkCollisions()`

After the wolf collision check and before/after the resource check, add:

```java
for (RestingSpot rs : restingSpots) {
    if (sheep.overlaps(rs)) {
        sheep.rest(rs);
    }
}
```

RestingSpots are never removed from the list — they are permanent fixtures for the whole game.

### `SheepGame.java` — `getDrawables()`

Extend the drawables array to include rest spots. Rest spots should be drawn **before** (underneath) resources and the sheep, so they appear as ground-level patches. Adjust array allocation accordingly:

```java
Drawable[] drawables = new Drawable[restingSpots.size() + otherObjects.size() + wolves.size() + 1];
```

Draw order (index 0 = bottom, painted first):
1. RestingSpots
2. Grass / Water (otherObjects)
3. Wolves
4. Sheep (always on top)

### `SheepGame.java` — Spawning Timer (`createNewObject`)

Rest spots are **not** spawned by the periodic timer — they are fixed for the session. This keeps them as stable landmarks the player can memorize and route around, rather than transient pickups. Do not add rest spot spawning to `createNewObject()`.

### `SheepGame.java` — `isGameOver()`

No changes needed. Fatigue does not kill the sheep.

### `Sheep.java` — Constructor

Sheep must store `difficultyLevel` (or the derived scale) to use in the fatigue tick formula. Either:
- Pass the raw `int hardness` to a field `private final int difficulty`, or
- Compute the fatigue scale once: `private final double fatigueScale = 1.0 + (hardness - 1) * 0.06`

The existing `scale` variable (used for acceleration/speed) is computed locally. Add `fatigueScale` alongside it and store it as a field.

### `Sheep.java` — `tick()`

Inside the physics tick method, after updating velocity and position, append the fatigue logic described in Section 4.

The velocity clamping block that currently uses `maxSpeed` must be updated to use `effectiveMaxSpeed`. Be precise: only the **clamping** step changes. The acceleration addend still uses `acceleration` unchanged.

---

## 7. Difficulty Interaction

| Difficulty | Fatigue accumulation multiplier | Initial rest spots | Effective feel |
|---|---|---|---|
| 1 | ×1.00 | 3 | Fatigue barely noticeable; rest spots plentiful |
| 5 | ×1.24 | 2 | Sprint budget is real; one rest spot per half of canvas |
| 10 | ×1.54 | 1 | Chronic fatigue pressure; one rest spot becomes a contested resource to protect access to |

The combination of slower sheep speed (from the existing scale), faster fatigue accumulation, and fewer rest spots at high difficulty creates compounding pressure without any single punishing spike. The player is not surprised by an instant death; they are gradually squeezed.

---

## 8. Balance Notes

### Why Not Kill From Fatigue?

Death from fatigue would frustrate players who feel they are doing everything right. The speed penalty is more interesting: it creates a window of vulnerability (slow sheep near wolves) that the player understands and can plan around. Death is always traceable to hunger, thirst, or a wolf — the original, legible causes.

### Rest Spots as Strategic Anchors

Because rest spots are permanent, skilled players will learn their positions and incorporate them into routing. At high difficulty with one rest spot and multiple wolves, the player may need to kite the wolves away from the rest spot before using it — an emergent tactical interaction that the design does not need to explicitly engineer.

### Recovery Rate Tuning

`restPower = 2.0/tick` and `FATIGUE_RECOVERY_IDLE = 0.03/tick` mean:
- At rest spot: 100 stamina recovered in 50 ticks (~0.8 seconds). Fast enough to feel responsive.
- Idle (not moving): 100 stamina recovered in ~3333 ticks (~53 seconds). Painfully slow — standing still is not a good strategy but it eventually works.
- At full sprint (speedRatio = 1.0, diff 1): 0.08/tick; from 0 to 100 in 1250 ticks (~20 seconds). Sprint budget is meaningful but not punishing.
- At full sprint (diff 10): 0.08 × 1.54 ≈ 0.123/tick; 0 to 100 in ~810 ticks (~13 seconds). Tight.

These numbers should be considered starting-point estimates. The developer should tune after playtesting — in particular, `BASE_FATIGUE_RATE` and `FATIGUE_RECOVERY_IDLE` are the two primary levers.

---

## 9. Architectural Notes

This feature requires no new Timer, no new packages, and no changes to the collision detection architecture. All touchpoints are confined to:

1. One new class: `RestingSpot.java`
2. Two modified fields + one new method in `Sheep.java`
3. One new field + three method changes in `SheepGame.java`
4. One new progress bar + one color-update in `SheepHungerPanel.java`

No Swing layout changes beyond adding one `JProgressBar` to the existing `SheepHungerPanel`. The developer may need to adjust the `BorderLayout` in that panel (e.g., use a `BoxLayout` or `GridLayout` instead) to accommodate three vertical bars cleanly.

---

## 10. Open Questions for the Developer

1. **Panel layout:** `SheepHungerPanel` currently uses `BorderLayout` with bars in WEST and EAST. Adding a third bar will require switching to a `BoxLayout(Y_AXIS)` or a horizontal `GridLayout(1, 3)`. The developer should choose based on visual preference.

2. **Fatigue field type:** `double` vs `int`. This spec uses `double` for smooth accumulation. An `int` with fractional remainder tracking would also work. Developer's discretion.

3. **Rest spot overlap with resources:** The `isOverlaping()` check in `SheepGame` only checks `otherObjects`. When placing rest spots in `initilize()`, the developer should also check against already-placed rest spots to avoid stacking. Pass rest spots through a separate overlap check or extend `isOverlaping()` to accept a secondary list.

4. **Pause behavior:** If a pause feature is ever added, fatigue should freeze (same as hunger/thirst). This is already implied by tying all logic to the same timers, but worth noting.

5. **Game-over screen copy:** Currently reads "The Sheep Died!" — no change needed, since fatigue does not cause death.
