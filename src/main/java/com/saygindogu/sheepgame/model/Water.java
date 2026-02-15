package com.saygindogu.sheepgame.model;

import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;

public class Water extends LocatableShape {

	@Getter
	private int volume;
	@Getter @Setter
	private int height;
	@Getter @Setter
	private int width;
	@Getter
	private int locationX;
	@Getter
	private int locationY;

	public Water( int volume, int x, int y, int width , int height){
		this.volume = volume;

		locationX = x;
		locationY = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Puddle body with gradient
		GradientPaint waterGrad = new GradientPaint(
			locationX, locationY, new Color( 100, 160, 220),
			locationX, locationY + height, new Color( 30, 80, 160));
		g2.setPaint( waterGrad);
		g2.fillOval( locationX, locationY, width, height);

		// Lighter rim / highlight
		g2.setColor( new Color( 150, 200, 255, 100));
		g2.fillOval( locationX + width / 6, locationY + height / 6, width * 2 / 3, height / 3);

		// Small wave/ripple lines
		g2.setColor( new Color( 200, 230, 255, 160));
		Random rand = new Random( locationX * 31 + locationY);
		int rippleCount = Math.max( 2, width / 20);
		for( int i = 0; i < rippleCount; i++){
			int rx = locationX + width / 5 + rand.nextInt( width * 3 / 5);
			int ry = locationY + height / 3 + rand.nextInt( height / 3);
			int rw = 6 + rand.nextInt( 10);
			g2.drawArc( rx, ry, rw, 4, 0, 180);
		}

		g2.dispose();
	}

}
