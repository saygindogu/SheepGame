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

    //Properties
    @Getter
    private int hunger;
    @Getter
    private int thirst;
    private int xLocation;
    private int yLocation;
    private int speed;
    private int height;
    private int width;
    private boolean isAlive;
    private Timer timer;

    //Constructor
    public Sheep() {
        hunger = 0;
        thirst = 0;
        xLocation = 0;
        yLocation = 0;
        speed = 10;
        height = 40;
        width = 50;
        isAlive = true;

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

        if (yLocation - speed >= 0) {
            yLocation = yLocation - speed;
        }

    }

    @Override
    public void goDown() {

        if (yLocation + speed + height <= SheepGame.GAME_SIZE_Y) {
            yLocation = yLocation + speed;
        }

    }

    @Override
    public void goLeft() {

        if (xLocation - speed >= 0) {
            xLocation = xLocation - speed;
        }

    }

    @Override
    public void goRight() {
        if (xLocation + speed + width <= SheepGame.GAME_SIZE_X) {
            xLocation = xLocation + speed;
        }

    }

    public void eat(Grass g) {
        if (g.getEaten()) {
            hunger = max(0, hunger - g.getNutritiousness());
        }
    }

    public void drink(Water w) {
        thirst = max(0, thirst - w.getVolume());
    }

    public void die() {
        isAlive = false;
        timer.stop();
    }

}
