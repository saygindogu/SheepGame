package com.saygindogu.sheepgame.ui;

import com.saygindogu.sheepgame.SheepGame;
import lombok.Getter;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.Timer;

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
	public Sheep(){
		hunger = 0;
		thirst = 0;
		xLocation = 0;
		yLocation = 0;
		speed = 10;
		height = 30;
		width = 30;
		isAlive = true;

		timer = new Timer( 1000, e -> {
			hunger++;
			thirst++;

			if( hunger >= MAX_HUNGER || thirst >= MAX_THIRST ){
				die();
			}
		});
		timer.start();
	}

	public boolean isAlive(){
		return isAlive;
	}

	@Override
	public void draw(Graphics g) {
		Graphics g1 = g.create();

		g1.setColor( Color.RED);
		g1.fillRect( xLocation, yLocation, width, height);

		g1.dispose();

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
	public void setLocation(int x, int y) {
		xLocation = x;
		yLocation = y;

	}

	@Override
	public void goUp() {

		if( yLocation - speed >= 0)
		{
			yLocation = yLocation - speed;
		}

	}

	@Override
	public void goDown() {

		if( yLocation + speed + height <= SheepGame.GAME_SIZE_Y )
		{
			yLocation = yLocation + speed;
		}

	}

	@Override
	public void goLeft() {

		if( xLocation - speed >= 0){
			xLocation = xLocation - speed;
		}

	}

	@Override
	public void goRight() {
		if( xLocation + speed + width <= SheepGame.GAME_SIZE_X )
		{
			xLocation = xLocation + speed;
		}

	}

	public void eat( Grass g){
		if( g.getEaten() )
		{
			hunger = hunger - g.getNutritiousness();
		}
	}

	public void drink( Water w){
		thirst = thirst - w.getVolume();
	}

	public void die(){
		isAlive = false;
		timer.stop();
	}

	@Override
	public void setHeight(int h) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setWidth(int w) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

}
