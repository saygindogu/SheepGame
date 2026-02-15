package com.saygindogu.sheepgame.model;

import lombok.Getter;
import lombok.Setter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;

public class Grass extends LocatableShape {

	@Getter
	private int nutritiousness;
	private int capacity;
	@Getter @Setter
	private int height;
	@Getter @Setter
	private int width;
	@Getter
	private int locationX;
	@Getter
	private int locationY;

	public Grass( int capacity, int x, int y, int width , int height){
		this.capacity = capacity;
		nutritiousness = capacity / 10;

		locationX = x;
		locationY = y;
		this.width = width;
		this.height = height;
	}

	public boolean getEaten(){
		if( capacity > nutritiousness )
		{
			capacity = capacity - nutritiousness;
			return true;
		}
		else
			capacity = 0;

		return false;
	}

	@Override
	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Draw a dirt/ground patch
		g2.setColor( new Color( 120, 80, 40));
		g2.fillOval( locationX, locationY + height - height / 4, width, height / 4);

		// Draw grass blades
		g2.setStroke( new BasicStroke( 2));
		int bladeCount = Math.max( 5, width / 6);
		Random rand = new Random( locationX * 31 + locationY);

		for( int i = 0; i < bladeCount; i++){
			int baseX = locationX + rand.nextInt( width);
			int baseY = locationY + height - height / 6;
			int tipX = baseX + rand.nextInt( 11) - 5;
			int tipY = locationY + rand.nextInt( height / 3);
			int midX = (baseX + tipX) / 2 + rand.nextInt( 7) - 3;
			int midY = (baseY + tipY) / 2;

			// Vary the green shade per blade
			int green = 140 + rand.nextInt( 80);
			g2.setColor( new Color( 20, green, 20));

			g2.drawLine( baseX, baseY, midX, midY);
			g2.drawLine( midX, midY, tipX, tipY);
		}

		g2.dispose();
	}

}
