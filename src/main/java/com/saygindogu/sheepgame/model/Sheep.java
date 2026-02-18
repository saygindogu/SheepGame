package com.saygindogu.sheepgame.model;

import com.saygindogu.sheepgame.SheepGame;
import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Timer;

import static java.lang.Integer.max;

@Getter
@Setter
public class Sheep extends LocatableShape implements Moveable {

    public static final int MAX_HUNGER = 10;
    public static final int MAX_THIRST = 50;
    public static final int MAX_FATIGUE = 100;

    private static final double BASE_ACCELERATION = 1.0;
    private static final double FRICTION = 0.85;
    private static final double BASE_MAX_SPEED = 8.0;
    private static final double BASE_FATIGUE_RATE = 0.08;
    private static final double FATIGUE_RECOVERY_IDLE = 0.03;

    //Properties
    @Getter
    private int hunger;
    @Getter
    private int thirst;
    @Getter
    private double fatigue;
    private final double fatigueScale;
    private double xPos;
    private double yPos;
    private int xLocation;
    private int yLocation;
    private int speed;
    private int height;
    private int width;
    private boolean isAlive;
    private Timer timer;
    private double vx;
    private double vy;
    private double acceleration;
    private double maxSpeed;
    private boolean movingUp;
    private boolean movingDown;
    private boolean movingLeft;
    private boolean movingRight;

    //Constructor
    public Sheep(int hardness) {
        hunger = 0;
        thirst = 0;
        fatigue = 0.0;
        xPos = 0;
        yPos = 0;
        xLocation = 0;
        yLocation = 0;
        speed = 10;
        height = 40;
        width = 50;
        isAlive = true;
        vx = 0;
        vy = 0;
        // Scale: difficulty 1 → full speed, difficulty 10 → 55% speed
        double scale = 1.0 - (hardness - 1) * 0.05;
        acceleration = BASE_ACCELERATION * scale;
        maxSpeed = BASE_MAX_SPEED * scale;
        fatigueScale = 1.0 + (hardness - 1) * 0.06;

        timer = new Timer(1000, e -> {
            hunger++;
            thirst++;

            if (hunger >= MAX_HUNGER || thirst >= MAX_THIRST) {
                die();
            }
        });
        timer.start();
    }

    public boolean isAlive() {
        return isAlive;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = xLocation + width / 2;
        int cy = yLocation + height / 2;

        // Legs (dark gray)
        g2.setColor(new Color(80, 80, 80));
        int legW = width / 6;
        int legH = height / 3;
        g2.fillRoundRect(cx - width / 3, cy + height / 4, legW, legH, 2, 2);
        g2.fillRoundRect(cx - width / 8, cy + height / 4, legW, legH, 2, 2);
        g2.fillRoundRect(cx + width / 8 - legW / 2, cy + height / 4, legW, legH, 2, 2);
        g2.fillRoundRect(cx + width / 3 - legW, cy + height / 4, legW, legH, 2, 2);

        // Woolly body (white oval)
        g2.setColor(Color.WHITE);
        g2.fillOval(cx - width / 2, cy - height / 4, width, height / 2);

        // Wool puff texture (light gray circles on body)
        g2.setColor(new Color(220, 220, 220));
        int puff = width / 5;
        g2.fillOval(cx - width / 3, cy - height / 5, puff, puff);
        g2.fillOval(cx - width / 8, cy - height / 4, puff, puff);
        g2.fillOval(cx + width / 8, cy - height / 5, puff, puff);
        g2.fillOval(cx - width / 4, cy, puff, puff);
        g2.fillOval(cx, cy - height / 8, puff, puff);
        g2.fillOval(cx + width / 5, cy, puff, puff);

        // Body outline
        g2.setColor(new Color(180, 180, 180));
        g2.drawOval(cx - width / 2, cy - height / 4, width, height / 2);

        // Head (black oval, to the right)
        g2.setColor(new Color(50, 50, 50));
        int headW = width / 3;
        int headH = height / 3;
        g2.fillOval(cx + width / 3, cy - height / 4, headW, headH);

        // Ears
        g2.fillOval(cx + width / 3 + headW / 4 - 3, cy - height / 4 - headH / 4, headW / 3, headH / 3);
        g2.fillOval(cx + width / 3 + headW * 2 / 3 - 1, cy - height / 4 - headH / 4, headW / 3, headH / 3);

        // Eye (white dot)
        g2.setColor(Color.WHITE);
        int eyeSize = Math.max(2, width / 10);
        g2.fillOval(cx + width / 3 + headW / 2, cy - height / 6, eyeSize, eyeSize);
        // Pupil
        g2.setColor(Color.BLACK);
        int pupilSize = Math.max(1, eyeSize / 2);
        g2.fillOval(cx + width / 3 + headW / 2 + pupilSize / 2, cy - height / 6, pupilSize, pupilSize);

        g2.dispose();
    }

    @Override
    public int getLocationX() {
        return xLocation;
    }

    @Override
    public int getLocationY() {
        return yLocation;
    }

    @Override
    public void goUp() {
        movingUp = true;
    }

    @Override
    public void goDown() {
        movingDown = true;
    }

    @Override
    public void goLeft() {
        movingLeft = true;
    }

    @Override
    public void goRight() {
        movingRight = true;
    }

    public void stopUp() { movingUp = false; }
    public void stopDown() { movingDown = false; }
    public void stopLeft() { movingLeft = false; }
    public void stopRight() { movingRight = false; }

    public void tick() {
        // Apply acceleration for held directions (unchanged by fatigue)
        if (movingUp) vy -= acceleration;
        if (movingDown) vy += acceleration;
        if (movingLeft) vx -= acceleration;
        if (movingRight) vx += acceleration;

        // Apply friction
        vx *= FRICTION;
        vy *= FRICTION;

        // Compute effective max speed based on fatigue
        double exhaustionRatio = fatigue / MAX_FATIGUE;
        double effectiveMaxSpeed = maxSpeed * (1.0 - 0.60 * exhaustionRatio);

        // Clamp velocity to effectiveMaxSpeed
        double speed = Math.sqrt(vx * vx + vy * vy);
        if (speed > effectiveMaxSpeed) {
            double scale = effectiveMaxSpeed / speed;
            vx *= scale;
            vy *= scale;
        }

        // Stop tiny drift
        if (Math.abs(vx) < 0.1) vx = 0;
        if (Math.abs(vy) < 0.1) vy = 0;

        // Update precise position
        xPos += vx;
        yPos += vy;

        // Clamp position to game bounds
        if (xPos < 0) { xPos = 0; vx = 0; }
        if (yPos < 0) { yPos = 0; vy = 0; }
        if (xPos + width > SheepGame.GAME_SIZE_X) { xPos = SheepGame.GAME_SIZE_X - width; vx = 0; }
        if (yPos + height > SheepGame.GAME_SIZE_Y) { yPos = SheepGame.GAME_SIZE_Y - height; vy = 0; }

        // Sync int locations for rendering and collision
        xLocation = (int) Math.round(xPos);
        yLocation = (int) Math.round(yPos);

        // Fatigue accumulation / recovery
        double currentSpeed = Math.hypot(vx, vy);
        double speedRatio = currentSpeed / maxSpeed;
        if (speedRatio > 0.1) {
            fatigue += BASE_FATIGUE_RATE * speedRatio * fatigueScale;
        } else {
            fatigue -= FATIGUE_RECOVERY_IDLE * fatigueScale;
        }
        fatigue = Math.max(0.0, Math.min(MAX_FATIGUE, fatigue));
    }

    public void eat(Grass g) {
        if (g.getEaten()) {
            hunger = max(0, hunger - g.getNutritiousness());
        }
    }

    public void drink(Water w) {
        thirst = max(0, thirst - w.getVolume());
    }

    public void rest(RestingSpot rs) {
        fatigue = Math.max(0.0, fatigue - rs.getRestPower());
    }

    public void die() {
        isAlive = false;
        timer.stop();
    }

}
