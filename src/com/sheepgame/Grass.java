package com.sheepgame;

import java.awt.Color;
import java.awt.Graphics;

public class Grass extends LocatableShape {
	
	private int nutritiousness;
	private int capacity;
	private int height;
	private int width;
	private int locationX;
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
	
	public int getNutritiousness(){
		return nutritiousness;
	}

	@Override
	public void setHeight(int h) {
		height = h;
		
	}

	@Override
	public void setWidth(int w) {
		width = w;
		
	}

	@Override
	public int getHeigth() {
		return height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getLocationX() {
		return locationX;
	}

	@Override
	public int getLocationY() {
		return locationY;
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
