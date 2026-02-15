package com.saygindogu.sheepgame.model;

import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
import java.awt.Graphics;

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
		Graphics g1 = g.create();

		g1.setColor( Color.BLUE);
		g1.fillOval( locationX, locationY, width, height);

		g1.dispose();

	}

}
