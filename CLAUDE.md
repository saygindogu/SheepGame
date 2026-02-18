# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

Requires Java 21. Uses Gradle wrapper (Gradle 9.3.1).

```sh
./gradlew run          # Build and launch the game
./gradlew build        # Compile only
./gradlew clean build  # Clean rebuild
```

There are no tests in this project.

## Architecture

`SheepGame.java` is both the game controller and the application entry point (`main()`). It also owns the static Swing frame and `CardLayout` navigation between the main menu and game screens.

### Packages

**`com.saygindogu.sheepgame`** — top-level, contains `SheepGame` only.

**`com.saygindogu.sheepgame.model`** — pure game logic and entities:
- `LocatableShape` — abstract base for all positioned objects; provides AABB collision via `overlaps()`
- `Sheep` — the player entity; extends `LocatableShape` and implements `Moveable`. Holds physics state (`vx`, `vy`, `acceleration`, `maxSpeed`) and runs its own 1-second `Timer` to tick hunger/thirst. Physics constants `BASE_ACCELERATION`, `FRICTION`, `BASE_MAX_SPEED` live here and are scaled by difficulty.
- `Grass`, `Water` — collectible resource objects extending `LocatableShape`
- Interfaces: `Drawable` (has `draw(Graphics)`), `Locatable`, `Moveable`, `Shape`

**`com.saygindogu.sheepgame.ui`** — all Swing UI:
- `SheepGameView` — interface; views implement `update(SheepGame)` and are registered on the game
- `SheepGameVisualViewPanel` — main game canvas (`JPanel`), calls `game.getDrawables()` and paints each; also renders the game-over overlay
- `SheepHungerPanel` — sidebar panel showing hunger/thirst bars
- `MainMenuPanel` — difficulty selection screen; fires a `Consumer<Integer>` callback to start the game
- `SheepGameKeyListener` — maps arrow/WASD key press/release to `Sheep.goUp/stopUp` etc.

### Game Loop

`SheepGame` runs two `javax.swing.Timer`s:
1. **`physicsTimer`** (16 ms) — calls `sheep.tick()` then `updateViews()`. This drives ~60 fps physics.
2. **`timer`** (rate = `TIMER_CONSTANT / 10 * (difficultyLevel / 2 + 1)`) — spawns new `Grass` or `Water` objects and calls `updateViews()`.

`updateViews()` runs collision detection first, then notifies all registered `SheepGameView`s.

### Difficulty Scaling

Difficulty (1–10) is passed into `SheepGame` and `Sheep` constructors. A `scale` factor (`1.0 - (difficulty - 1) * 0.05`) linearly reduces sheep acceleration/speed, resource spawn size, and resource nutritional value at higher difficulties. Initial resource count is `11 - difficultyLevel`.

### Lombok

The project uses Lombok (`@Getter`, `@Setter`) for boilerplate reduction. Annotation processing is configured in `build.gradle`.

### Future Development

Claude will be used for any future development. Utilize the subagents as necessary.