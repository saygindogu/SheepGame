# Wolf Predator Implementation Summary

## Overview
Added wolf predators that roam the game canvas and chase the sheep when within detection range. Wolves spawn on random canvas edges and scale with difficulty level.

## Files Changed

### New: `src/main/java/com/saygindogu/sheepgame/model/Wolf.java`
- Extends `LocatableShape`, implements `Drawable`
- Two states: `ROAMING` (random waypoints) and `CHASING` (pursues sheep)
- 40% hysteresis on detection radius transition (enters chase at `detectionRadius`, exits at `detectionRadius * 1.4`)
- Spawns on a random canvas edge (top/bottom/left/right)
- `tick(int sheepX, int sheepY)` called each physics frame from existing `physicsTimer`
- Eyes: amber (255,165,0) when chasing, yellow (255,255,0) when roaming
- No new `javax.swing.Timer` instances; driven entirely by existing physics timer

### Modified: `src/main/java/com/saygindogu/sheepgame/SheepGame.java`
- Added `@Getter List<Wolf> wolves` field
- `initilize()`: creates wolves after resources using `wolfCount = Math.max(0, (difficultyLevel - 1) / 2)`
- `physicsTimer` callback: ticks all wolves after `sheep.tick()`
- `checkCollisions()`: checks `wolf.overlaps(sheep)` first; calls `sheep.die()` and returns immediately on hit
- `getDrawables()`: includes wolves in the returned array (between resources and sheep)

### Modified: `src/main/java/com/saygindogu/sheepgame/ui/SheepHungerPanel.java`
- Added `wolfWarning` JLabel positioned at SOUTH of the panel
- `update()`: calculates nearest wolf distance via `Math.hypot`
  - Within 150px: red text "WOLF! Xpx"
  - Within 400px: orange text "WOLF! Xpx"
  - Otherwise: hidden

## Difficulty Scaling Formulas
| Parameter      | Formula                            | Diff 1 | Diff 5 | Diff 10 |
|----------------|-------------------------------------|--------|--------|---------|
| wolfCount      | `max(0, (level - 1) / 2)`          | 0      | 2      | 4       |
| chaseSpeed     | `2.0 + level * 0.25`               | 2.25   | 3.25   | 4.5     |
| roamSpeed      | `0.8 + level * 0.15`               | 0.95   | 1.55   | 2.3     |
| detectionRadius| `150 + level * 25`                  | 175    | 275    | 400     |

## Acceptance Criteria Status
- [x] Difficulty 1 -> 0 wolves
- [x] Difficulty 5 -> 2 wolves
- [x] Difficulty 10 -> 4 wolves
- [x] Wolf renders each frame via getDrawables()
- [x] Wolf-sheep collision calls sheep.die()
- [x] Eyes change colour on state transition
- [x] No new javax.swing.Timer instances
- [x] ./gradlew build passes
