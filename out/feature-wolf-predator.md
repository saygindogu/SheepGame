# Feature Spec: Wolf Predator

**Author:** game-designer agent
**Date:** 2026-02-18
**Status:** Proposed

---

## Overview

Introduce a **Wolf** — a roaming predator that hunts the sheep across the canvas. The wolf
applies constant pressure that forces the player to balance two competing priorities:
reaching food/water quickly versus staying safe. On higher difficulties wolves are
faster, more numerous, and spawn sooner, deepening the existing difficulty curve without
any changes to the core hunger/thirst constants.

---

## Motivation and Design Goals

The current game is a pure resource-collection loop. Hunger and thirst create urgency, but
the sheep is never threatened by anything other than time. A predator adds:

1. **Spatial tension** — no path to a resource patch is ever trivially safe.
2. **Risk/reward decisions** — a large grass patch close to a wolf may still be worth
   chasing at low hunger.
3. **Difficulty expression** — wolf count and speed are natural knobs that make higher
   difficulties feel qualitatively different, not just "resources are smaller."
4. **Emergent drama** — a near-miss escape is an experience the player will remember.

---

## Gameplay Behaviour

### Wolf state machine

```
ROAMING  -->  (sheep within DETECTION_RADIUS)  -->  CHASING
CHASING  -->  (sheep outside FLEE_RADIUS)       -->  ROAMING
CHASING  -->  (wolf overlaps sheep)             -->  GAME OVER (instant kill)
```

- **ROAMING**: the wolf wanders toward a random waypoint on the canvas. When it reaches
  the waypoint it picks a new one. Movement uses the same AABB physics pattern as the
  sheep (direct position update, no Timer of its own — driven by the existing
  `physicsTimer`).
- **CHASING**: the wolf moves directly toward the sheep's current position each tick at
  `chaseSpeed`. There is no pathfinding; the wolf moves in a straight line, which keeps
  implementation trivial and allows the player to "corner-dodge" around resource patches.

### Death condition

When `wolf.overlaps(sheep)` returns `true` (same AABB check already in
`LocatableShape`), `sheep.die()` is called. The existing game-over flow handles the rest.

---

## Difficulty Scaling

| Difficulty | Wolves spawned at start | Wolf chase speed (px/tick) | Detection radius (px) | Roam speed (px/tick) |
|------------|------------------------|---------------------------|-----------------------|----------------------|
| 1          | 0                      | —                         | —                     | —                    |
| 2–3        | 1                      | 2.5                       | 200                   | 1.2                  |
| 4–5        | 1                      | 3.0                       | 250                   | 1.5                  |
| 6–7        | 2                      | 3.5                       | 300                   | 1.8                  |
| 8–9        | 2                      | 4.0                       | 350                   | 2.0                  |
| 10         | 3                      | 4.5                       | 400                   | 2.2                  |

Wolves are NOT spawned by the resource-spawner timer; they are created once during
`SheepGame.initilize()` so their count is fixed for the session.

Formula helpers (to avoid a lookup table in code):

```java
int wolfCount   = Math.max(0, (difficultyLevel - 1) / 2);   // 0,1,1,2,2,3 for levels 1-10 clamped
double chaseSpd = 2.0 + difficultyLevel * 0.25;              // 2.25 → 4.75 across levels 1-10
double roamSpd  = 0.8 + difficultyLevel * 0.15;              // 0.95 → 2.30 across levels 1-10
int detectR     = 150 + difficultyLevel * 25;                // 175 → 400 px
```

Difficulty 1 produces `wolfCount = 0`, so beginners are never threatened.

---

## Architecture

### New model class: `Wolf`

File: `src/main/java/com/saygindogu/sheepgame/model/Wolf.java`

```java
package com.saygindogu.sheepgame.model;

import com.saygindogu.sheepgame.SheepGame;
import lombok.Getter;
import java.awt.*;
import java.util.Random;

@Getter
public class Wolf extends LocatableShape {

    public enum State { ROAMING, CHASING }

    private static final int SIZE = 36;          // square bounding box used for AABB

    private double xPos, yPos;
    private int xLocation, yLocation;
    private final int width  = SIZE;
    private final int height = SIZE;

    private final double chaseSpeed;
    private final double roamSpeed;
    private final int detectionRadius;

    private State state = State.ROAMING;

    // Roam waypoint
    private double waypointX, waypointY;
    private final Random rng = new Random();

    public Wolf(int difficultyLevel) {
        chaseSpeed      = 2.0 + difficultyLevel * 0.25;
        roamSpeed       = 0.8 + difficultyLevel * 0.15;
        detectionRadius = 150 + difficultyLevel * 25;

        // Spawn on a random canvas edge so the wolf enters from outside
        spawnOnEdge();
        pickNewWaypoint();
    }

    private void spawnOnEdge() {
        int edge = rng.nextInt(4);
        switch (edge) {
            case 0 -> { xPos = rng.nextInt(SheepGame.GAME_SIZE_X); yPos = 0; }
            case 1 -> { xPos = rng.nextInt(SheepGame.GAME_SIZE_X); yPos = SheepGame.GAME_SIZE_Y - SIZE; }
            case 2 -> { xPos = 0;                                   yPos = rng.nextInt(SheepGame.GAME_SIZE_Y); }
            default -> { xPos = SheepGame.GAME_SIZE_X - SIZE;       yPos = rng.nextInt(SheepGame.GAME_SIZE_Y); }
        }
        syncLocation();
    }

    private void pickNewWaypoint() {
        waypointX = rng.nextInt(SheepGame.GAME_SIZE_X);
        waypointY = rng.nextInt(SheepGame.GAME_SIZE_Y);
    }

    /** Called every physicsTimer tick (16 ms). sheepX/Y are the sheep's current pixel coords. */
    public void tick(int sheepX, int sheepY) {
        double dx = sheepX - xPos;
        double dy = sheepY - yPos;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < detectionRadius) {
            state = State.CHASING;
        } else if (dist > detectionRadius * 1.4) {   // 40% hysteresis to prevent flicker
            state = State.ROAMING;
        }

        if (state == State.CHASING) {
            if (dist > 0) {
                xPos += (dx / dist) * chaseSpeed;
                yPos += (dy / dist) * chaseSpeed;
            }
        } else {
            // Move toward waypoint
            double wx = waypointX - xPos;
            double wy = waypointY - yPos;
            double wdist = Math.sqrt(wx * wx + wy * wy);
            if (wdist < 10) {
                pickNewWaypoint();
            } else {
                xPos += (wx / wdist) * roamSpeed;
                yPos += (wy / wdist) * roamSpeed;
            }
        }

        // Clamp to canvas
        xPos = Math.max(0, Math.min(SheepGame.GAME_SIZE_X - SIZE, xPos));
        yPos = Math.max(0, Math.min(SheepGame.GAME_SIZE_Y - SIZE, yPos));
        syncLocation();
    }

    private void syncLocation() {
        xLocation = (int) Math.round(xPos);
        yLocation = (int) Math.round(yPos);
    }

    @Override
    public int getLocationX() { return xLocation; }

    @Override
    public int getLocationY() { return yLocation; }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = xLocation + SIZE / 2;
        int cy = yLocation + SIZE / 2;

        // Body — dark grey ellipse
        g2.setColor(new Color(60, 60, 60));
        g2.fillOval(xLocation, yLocation + SIZE / 4, SIZE, SIZE / 2);

        // Head — slightly lighter
        g2.setColor(new Color(80, 80, 80));
        int headW = SIZE / 2, headH = SIZE / 2;
        g2.fillOval(xLocation + SIZE - headW / 2, yLocation + SIZE / 4 - headH / 4, headW, headH);

        // Ears — triangular look via filled arcs
        g2.setColor(new Color(50, 50, 50));
        g2.fillOval(xLocation + SIZE - headW / 4, yLocation, headW / 3, headH / 3);
        g2.fillOval(xLocation + SIZE + headW / 4, yLocation, headW / 3, headH / 3);

        // Eyes — glowing amber when chasing
        Color eyeColor = (state == State.CHASING) ? new Color(230, 140, 0) : Color.YELLOW;
        g2.setColor(eyeColor);
        int eyeSize = Math.max(3, SIZE / 8);
        g2.fillOval(xLocation + SIZE - headW / 4, yLocation + SIZE / 4, eyeSize, eyeSize);

        // Detection radius ring (faint, only drawn in debug — remove for production)
        // g2.setColor(new Color(255, 0, 0, 40));
        // g2.drawOval(cx - detectionRadius, cy - detectionRadius, detectionRadius*2, detectionRadius*2);

        g2.dispose();
    }
}
```

### Changes to `SheepGame`

1. Add `List<Wolf> wolves` field alongside `List<LocatableShape> otherObjects`.

2. In `initilize(int hardness)`, after creating the initial resources, create wolves:

```java
private void initilize(int hardness) {
    otherObjects = new ArrayList<>();
    wolves = new ArrayList<>();

    for (int i = 0; i < hardness; i++) {
        createNewObject();
    }

    int wolfCount = Math.max(0, (difficultyLevel - 1) / 2);
    for (int i = 0; i < wolfCount; i++) {
        wolves.add(new Wolf(difficultyLevel));
    }
}
```

3. In `physicsTimer` callback, tick all wolves after ticking the sheep:

```java
physicsTimer = new Timer(16, e -> {
    sheep.tick();
    for (Wolf wolf : wolves) {
        wolf.tick(sheep.getLocationX(), sheep.getLocationY());
    }
    updateViews();
});
```

4. In `checkCollisions()`, add wolf-sheep overlap check:

```java
for (Wolf wolf : wolves) {
    if (wolf.overlaps(sheep)) {
        sheep.die();
        return;   // no point checking further
    }
}
```

5. In `getDrawables()` (or wherever the drawable list is assembled), include wolves
   so `SheepGameVisualViewPanel` renders them automatically. Since `Wolf` extends
   `LocatableShape` which implements `Shape` which extends `Drawable`, no interface
   changes are needed — just add them to the returned array.

   If `getDrawables()` currently only returns the contents of `otherObjects` plus the
   sheep, change it to also include `wolves`:

```java
public Drawable[] getDrawables() {
    List<Drawable> list = new ArrayList<>();
    list.add(sheep);
    list.addAll(otherObjects);
    list.addAll(wolves);    // <-- new line
    return list.toArray(new Drawable[0]);
}
```

6. Expose `getWolves()` via a `@Getter`-annotated field or a hand-written accessor so
   views or tests can query wolf state if needed.

### Changes to `SheepHungerPanel` (optional enhancement)

Add a small "WOLF!" warning label that turns red and shows the distance to the nearest
wolf when any wolf is within `detectionRadius`. This gives audio-game-feel even without
sound. The label is hidden otherwise.

```java
// In update():
wolves = game.getWolves();
int minDist = wolves.stream()
    .mapToInt(w -> (int) Math.hypot(
        w.getLocationX() - game.getSheep().getLocationX(),
        w.getLocationY() - game.getSheep().getLocationY()))
    .min().orElse(Integer.MAX_VALUE);

if (minDist < 400) {
    wolfWarning.setText("WOLF! " + minDist + " px");
    wolfWarning.setForeground(minDist < 150 ? Color.RED : Color.ORANGE);
    wolfWarning.setVisible(true);
} else {
    wolfWarning.setVisible(false);
}
```

---

## Visual Design

| Element | Colour / Style |
|---------|---------------|
| Body | Dark grey ellipse (`60,60,60`) |
| Head | Slightly lighter (`80,80,80`) |
| Eyes (roaming) | Yellow dots |
| Eyes (chasing) | Amber/orange (`230,140,0`) — signals danger to the player |
| Ears | Two small dark ovals |

The amber eye transition is the player's primary visual cue that a wolf has locked on.

---

## Game Feel Notes

- Because wolves use straight-line tracking (no pathfinding), a skilled player can use
  resource patches as soft obstacles to break line of sight by running past them —
  introducing an advanced technique that rewards experienced players.
- Wolves do NOT consume or destroy resources. They are pure hazards.
- Multiple wolves will naturally spread out during ROAMING (different random waypoints),
  then converge during a chase — creating a pincer dynamic at high difficulty.
- The sheep's existing FRICTION mechanic means a panicking player who holds all four
  arrow keys at once loses control. This synergises well with the wolf; staying calm and
  choosing a clean escape vector matters.

---

## Acceptance Criteria

- [ ] `Wolf` class compiles, extends `LocatableShape`, implements `Drawable`.
- [ ] Difficulty 1 game starts with zero wolves.
- [ ] Difficulty 5 game starts with exactly 2 wolves (formula: `(5-1)/2 = 2`).
- [ ] Difficulty 10 game starts with exactly 4 wolves (formula: `(10-1)/2 = 4`).
- [ ] Wolf rendered on canvas every paint cycle via `getDrawables()`.
- [ ] Collision between wolf and sheep triggers `sheep.die()` and shows game-over overlay.
- [ ] Wolf eyes change colour when entering CHASING state.
- [ ] `SheepHungerPanel` wolf warning label appears when any wolf is within 400 px and
      turns red inside 150 px.
- [ ] No new `javax.swing.Timer` instances are created; wolves tick inside the existing
      `physicsTimer` callback.
- [ ] `./gradlew build` passes with no new warnings.

---

## Out of Scope (future iterations)

- Sound effects (growl on detection, heartbeat when close).
- Wolf pathfinding around obstacles.
- Wolf pack coordination (shared target locking).
- Wolf visual animation (running leg cycle).
- Wolf leaving the canvas and re-entering from a different edge.
