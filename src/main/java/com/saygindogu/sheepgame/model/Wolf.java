package com.saygindogu.sheepgame.model;

import com.saygindogu.sheepgame.SheepGame;
import lombok.Getter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;

@Getter
public class Wolf extends LocatableShape implements Drawable {

    public enum State { ROAMING, CHASING }

    // Color palette constants
    private static final Color LEG_COLOR = new Color(60, 40, 20);
    private static final Color BODY_COLOR = new Color(90, 90, 100);
    private static final Color BODY_OUTLINE_COLOR = new Color(60, 60, 70);
    private static final Color TAIL_COLOR = new Color(80, 80, 90);
    private static final Color HEAD_COLOR = new Color(70, 70, 80);
    private static final Color SNOUT_COLOR = new Color(55, 55, 65);
    private static final Color EYE_CHASING_COLOR = new Color(255, 165, 0);
    private static final Color EYE_ROAMING_COLOR = new Color(255, 255, 0);

    private double xPos;
    private double yPos;
    private int xLocation;
    private int yLocation;
    private final int width = 36;
    private final int height = 36;
    private final double chaseSpeed;
    private final double roamSpeed;
    private final double detectionRadius;
    private State state;
    private double waypointX;
    private double waypointY;
    private final Random rng;

    public Wolf(double chaseSpeed, double roamSpeed, double detectionRadius) {
        this.chaseSpeed = chaseSpeed;
        this.roamSpeed = roamSpeed;
        this.detectionRadius = detectionRadius;
        this.state = State.ROAMING;
        this.rng = new Random();

        // Spawn on a random canvas edge
        int edge = rng.nextInt(4);
        switch (edge) {
            case 0: // top
                xPos = rng.nextInt(SheepGame.GAME_SIZE_X);
                yPos = 0;
                break;
            case 1: // bottom
                xPos = rng.nextInt(SheepGame.GAME_SIZE_X);
                yPos = SheepGame.GAME_SIZE_Y - height;
                break;
            case 2: // left
                xPos = 0;
                yPos = rng.nextInt(SheepGame.GAME_SIZE_Y);
                break;
            case 3: // right
                xPos = SheepGame.GAME_SIZE_X - width;
                yPos = rng.nextInt(SheepGame.GAME_SIZE_Y);
                break;
        }
        xLocation = (int) Math.round(xPos);
        yLocation = (int) Math.round(yPos);
        pickNewWaypoint();
    }

    private void pickNewWaypoint() {
        waypointX = rng.nextInt(SheepGame.GAME_SIZE_X - width);
        waypointY = rng.nextInt(SheepGame.GAME_SIZE_Y - height);
    }

    public void tick(int sheepX, int sheepY) {
        double dx = sheepX - xPos;
        double dy = sheepY - yPos;
        double distToSheep = Math.hypot(dx, dy);

        // State transition with 40% hysteresis
        if (state == State.ROAMING && distToSheep < detectionRadius) {
            state = State.CHASING;
        } else if (state == State.CHASING && distToSheep > detectionRadius * 1.4) {
            state = State.ROAMING;
            pickNewWaypoint();
        }

        double speed;
        double targetX;
        double targetY;

        if (state == State.CHASING) {
            speed = chaseSpeed;
            targetX = sheepX;
            targetY = sheepY;
        } else {
            speed = roamSpeed;
            targetX = waypointX;
            targetY = waypointY;

            // Pick new waypoint if close enough to current one
            double distToWaypoint = Math.hypot(waypointX - xPos, waypointY - yPos);
            if (distToWaypoint < speed * 2) {
                pickNewWaypoint();
            }
        }

        // Move toward target
        double tdx = targetX - xPos;
        double tdy = targetY - yPos;
        double dist = Math.hypot(tdx, tdy);
        if (dist > 0) {
            xPos += (tdx / dist) * speed;
            yPos += (tdy / dist) * speed;
        }

        // Clamp to bounds
        if (xPos < 0) xPos = 0;
        if (yPos < 0) yPos = 0;
        if (xPos + width > SheepGame.GAME_SIZE_X) xPos = SheepGame.GAME_SIZE_X - width;
        if (yPos + height > SheepGame.GAME_SIZE_Y) yPos = SheepGame.GAME_SIZE_Y - height;

        xLocation = (int) Math.round(xPos);
        yLocation = (int) Math.round(yPos);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = xLocation + width / 2;
        int cy = yLocation + height / 2;

        // Legs (dark brown)
        g2.setColor(LEG_COLOR);
        int legW = width / 7;
        int legH = height / 3;
        g2.fillRoundRect(cx - width / 3, cy + height / 5, legW, legH, 2, 2);
        g2.fillRoundRect(cx - width / 8, cy + height / 5, legW, legH, 2, 2);
        g2.fillRoundRect(cx + width / 10, cy + height / 5, legW, legH, 2, 2);
        g2.fillRoundRect(cx + width / 4, cy + height / 5, legW, legH, 2, 2);

        // Body (dark gray)
        g2.setColor(BODY_COLOR);
        g2.fillOval(cx - width / 2, cy - height / 4, width, height / 2);

        // Body outline
        g2.setColor(BODY_OUTLINE_COLOR);
        g2.drawOval(cx - width / 2, cy - height / 4, width, height / 2);

        // Tail (dark gray triangle behind body)
        g2.setColor(TAIL_COLOR);
        int[] tailX = { cx - width / 2 - 4, cx - width / 2, cx - width / 2 };
        int[] tailY = { cy - height / 8, cy - height / 6, cy + height / 8 };
        g2.fillPolygon(tailX, tailY, 3);

        // Head (darker, pointy snout to the right)
        g2.setColor(HEAD_COLOR);
        int headW = width / 3;
        int headH = height / 3;
        g2.fillOval(cx + width / 4, cy - height / 3, headW, headH);

        // Snout
        g2.setColor(SNOUT_COLOR);
        g2.fillOval(cx + width / 4 + headW / 2, cy - height / 4, headW / 2, headH / 2);

        // Ears (pointed triangles)
        g2.setColor(HEAD_COLOR);
        int earBase = headW / 3;
        // Left ear
        int[] ear1X = { cx + width / 4 + headW / 4, cx + width / 4 + headW / 4 + earBase / 2, cx + width / 4 + headW / 4 + earBase };
        int[] ear1Y = { cy - height / 3, cy - height / 3 - headH / 2, cy - height / 3 };
        g2.fillPolygon(ear1X, ear1Y, 3);
        // Right ear
        int[] ear2X = { cx + width / 4 + headW / 2, cx + width / 4 + headW / 2 + earBase / 2, cx + width / 4 + headW / 2 + earBase };
        int[] ear2Y = { cy - height / 3, cy - height / 3 - headH / 2, cy - height / 3 };
        g2.fillPolygon(ear2X, ear2Y, 3);

        // Eyes - amber when chasing, yellow when roaming
        Color eyeColor = (state == State.CHASING) ? EYE_CHASING_COLOR : EYE_ROAMING_COLOR;
        g2.setColor(eyeColor);
        int eyeSize = Math.max(3, width / 9);
        g2.fillOval(cx + width / 4 + headW / 3, cy - height / 4, eyeSize, eyeSize);

        // Pupil
        g2.setColor(Color.BLACK);
        int pupilSize = Math.max(1, eyeSize / 2);
        g2.fillOval(cx + width / 4 + headW / 3 + pupilSize / 2, cy - height / 4 + pupilSize / 2, pupilSize, pupilSize);

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
}
