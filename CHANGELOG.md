# Changelog

## The Epic Revival of Sheep Game

### November 4, 2015 — The Beginning

A young developer sat down and wrote a small Java game about a sheep trying to survive. The sheep was a red square. Grass was neon green ovals. Water was dark blue blobs. It was beautiful in the way only a first project can be — raw, honest, and full of heart.

Two commits were made that day. Then silence.

![The original Sheep Game, circa 2015](screenshots/before/gameplay.png)

The repository sat untouched on GitHub. Through the rise of React, the fall of Java applets, the mass adoption of TypeScript, the arrival of GPT, and the entire pandemic — the red square sheep waited patiently in its field of green ovals.

For **eleven years**.

---

### February 15, 2026 — Claude Enters the Pasture

Claude, an AI by Anthropic, was invited to breathe life back into the project. What followed was a single afternoon of intense collaboration.

---

### v2.0 — The Modernization (Feb 15, 2026)

The ancient Eclipse project was dragged into the modern era:

- Migrated from raw javac to **Gradle** with the application plugin
- Upgraded to **Java 21**
- Added **Lombok** to tame the boilerplate
- Reorganized into proper `model` and `ui` packages

### v2.1 — The Glow-Up (Feb 15, 2026)

The red square sheep deserved better:

- The sheep became a **cartoon sprite** with a woolly white body, gray legs, a dark head with ears, and an actual eye
- Grass transformed from flat green ovals into **clumps of individual blades** growing from dirt patches
- Water became **gradient puddles** with highlights and ripple animations
- Game over screen now reads "The Sheep Died!" in bold red, because the player should feel something

### v2.2 — The Main Menu & Physics (Feb 15, 2026)

The game stopped being a hardcoded difficulty-10 instant-launch experience:

- Added a **main menu** with a difficulty slider (1–10) and a start button
- Replaced stiff grid-like movement with **velocity-based physics** — the sheep now accelerates, coasts, and slides with friction
- Added **WASD controls** alongside arrow keys
- Enabled **diagonal movement** by tracking held keys
- Added a **60fps physics timer** for smooth continuous motion

### v2.3 — Difficulty Matters (Feb 15, 2026)

Difficulty stopped being just "how fast do things spawn" and started meaning something:

- Higher difficulty **slows the sheep** (reduced acceleration and max speed)
- Resources **spawn less frequently** at higher difficulty
- Resources are **smaller and less nutritious** at higher difficulty
- The game is now genuinely harder at 10 and genuinely relaxing at 1

---

### February 18, 2026 — The Agents Take Over

Three days after the revival, the project crossed a new threshold: features were no longer written by hand. A pipeline of specialised AI subagents — each with its own role, memory, and toolset — designed, implemented, reviewed, and fixed the next major update entirely autonomously.

The human's job was to say *yes*.

---

### v2.4 — Wolf Predator (Feb 18, 2026)

*Designed by the **game-designer** agent. Implemented by the **developer** agent. Reviewed and fixed through two automated passes by the **code-reviewer** and **developer** agents.*

The sheep now has a predator:

- **Wolves** spawn on the canvas edges and roam toward random waypoints
- When a sheep comes within detection range, the wolf locks on and **chases in a straight line** — eyes turning amber to signal the hunt
- Contact is an **instant kill**, bypassing hunger and thirst entirely
- Wolves scale with difficulty: none at level 1, up to 4 at level 10
- The **sidebar warns** the player as a wolf closes in — orange beyond detection range, red when inside it
- The existing `physicsTimer` drives wolf movement at 60fps — no new timers

#### How It Was Built

The `game-designer` agent read the codebase, studied the balance numbers, and produced a full feature spec in `out/feature-wolf-predator.md` — including difficulty tables, architecture diagrams, and acceptance criteria.

The `developer` agent read the spec, implemented `Wolf.java`, updated `SheepGame.java` and `SheepHungerPanel.java`, ran the build, then automatically invoked the `code-reviewer` agent on its own output.

The `code-reviewer` agent filed a structured report in `out/review-wolf-predator.md` identifying five issues — a null-reference window, a random instance re-seeded on every spawn, a difficulty clamp applied after the `Sheep` constructor already consumed the raw value, hardcoded warning thresholds disconnected from the wolf's actual detection formula, and per-frame color allocations in `draw()`.

The `developer` agent read the report, fixed all five issues, verified the build passed, and logged the session to `memory/developer.jsonl` for future reference.

No human wrote a line of Java.

---

### v2.5 — Stamina & Rest Spots (Feb 18, 2026)

*Designed by the **game-designer** agent. Implemented, reviewed twice, and fixed by the **developer** and **code-reviewer** agents in two automated passes.*

The sheep now has a stamina budget:

- **Sprinting drains fatigue** — moving fast costs stamina proportional to current speed; standing still recovers it slowly
- **Rest spots** — permanent tan earth patches scattered across the canvas; standing on one restores stamina quickly (~0.8 seconds to full)
- **Speed penalty** — at full exhaustion the sheep's effective top speed drops to 40% of normal, making a tired sheep dramatically more vulnerable to wolves
- **Stamina bar** added to the sidebar — turns orange above 70% fatigue, red above 90%
- Rest spots and fatigue accumulation both scale with difficulty: fewer spots (3 → 1) and faster drain (×1.54 at level 10) compound the existing wolf and resource pressure

The new decision the mechanic creates: *"Do I sprint to that grass patch and arrive exhausted near a wolf, or pace myself?"*

#### How It Was Built

The `game-designer` agent identified that the current loop had no planning horizon — every move was reactive. Stamina makes *how you move* a resource the player manages, not just *where you go*.

The `developer` agent implemented `RestingSpot.java`, extended `Sheep.java` with fatigue tick logic and a speed penalty, updated `SheepGame.java`, and reworked `SheepHungerPanel` to a three-bar layout. It then invoked the `code-reviewer` automatically.

The `code-reviewer` found one medium bug (idle recovery was scaling in the wrong direction — faster at higher difficulty instead of slower), three low issues (runtime grass spawner could overlap rest spots, `restPower` was not difficulty-scaled, each rest spot lacked its own retry budget in placement), and one compounding issue (double recovery when idle on a rest spot). It also caught two safe nitpicks.

The `developer` read the full review, applied all seven fixes across four files, and verified the build.

---

### What Changed in Numbers

| Aspect | 2015 | 2026 |
|---|---|---|
| Sheep appearance | Red square | Cartoon sprite with wool, legs, and eyes |
| Grass | Neon green oval | Blades growing from dirt |
| Water | Blue oval | Gradient puddle with ripples |
| Movement | Instant 10px jumps | Physics with acceleration and friction |
| Controls | Arrow keys only | Arrow keys + WASD, diagonal support |
| Menu | None (hardcoded start) | Difficulty selection screen |
| Build system | Eclipse project | Gradle + Java 21 + Lombok |
| Time between updates | — | 11 years |
| Wolf predator | None | Roaming/chasing enemy, scales with difficulty |
| Feature authorship | Human | AI subagent pipeline (designer → developer → reviewer) |
| Code review | Manual | Automated, structured, with memory across sessions |
| Stamina system | None | Fatigue drains on movement, rest spots restore it |
| Rest spots | None | Permanent earth patches, difficulty-scaled count and recovery rate |
