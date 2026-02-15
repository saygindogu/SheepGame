package com.saygindogu.sheepgame.ui;

import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
import java.awt.Graphics;

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
	public void setLocation(int x, int y) {
		locationY = y;
		locationX = x;

	}

	@Override
	public void draw(Graphics g) {
		Graphics g1 = g.create();

		g1.setColor( Color.GREEN);
		g1.fillOval( locationX, locationY, width, height);

		g1.dispose();

	}

}
