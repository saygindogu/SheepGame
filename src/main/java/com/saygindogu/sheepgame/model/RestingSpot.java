package com.saygindogu.sheepgame.model;

import lombok.Getter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;

@Getter
public class RestingSpot extends LocatableShape implements Drawable {

    // Color palette constants
    private static final Color EARTH_COLOR = new Color(180, 150, 100);
    private static final Color SHADOW_COLOR = new Color(60, 40, 20, 60);
    private static final Color PEBBLE_COLOR = new Color(140, 110, 70);
    private static final Color OUTLINE_COLOR = new Color(120, 90, 50);

    private final double restPower = 2.0;
    private final int locationX;
    private final int locationY;
    private final int width;
    private final int height;

    public RestingSpot(int x, int y, int width, int height) {
        this.locationX = x;
        this.locationY = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill earth/sand oval
        g2.setColor(EARTH_COLOR);
        g2.fillOval(locationX, locationY, width, height);

        // Semi-transparent shadow overlay
        g2.setColor(SHADOW_COLOR);
        int inset = width / 6;
        g2.fillOval(locationX + inset, locationY + inset, width - 2 * inset, height - 2 * inset);

        // Pebble texture (3-5 small dots)
        g2.setColor(PEBBLE_COLOR);
        Random rand = new Random(locationX * 31 + locationY);
        int pebbleCount = 3 + rand.nextInt(3);
        for (int i = 0; i < pebbleCount; i++) {
            int px = locationX + width / 5 + rand.nextInt(width * 3 / 5);
            int py = locationY + height / 5 + rand.nextInt(height * 3 / 5);
            int pSize = 2 + rand.nextInt(4);
            g2.fillOval(px, py, pSize, pSize);
        }

        // Thin outline
        g2.setColor(OUTLINE_COLOR);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(locationX, locationY, width, height);

        g2.dispose();
    }
}
