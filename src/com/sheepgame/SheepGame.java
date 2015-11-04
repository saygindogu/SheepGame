package com.sheepgame;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class SheepGame {

	public static final int GAME_SIZE_X = 1200;
	public static final int GAME_SIZE_Y = 800;
	public static final int TIMER_CONSTANT = 10000;
	
	Random random;
	Sheep sheep;
	ArrayList<LocatableShape> otherObjects;
	ArrayList<SheepGameView> views;
	Timer timer;
	int hardness;
	
	public SheepGame( int hardness){
		sheep = new Sheep();
		views = new ArrayList<SheepGameView>();
		
		if( hardness > 10){
			this.hardness = 10;
		}
		else if( hardness <= 0)
		{
			this.hardness = 1;
		}
		else
			this.hardness = hardness;
		
		timer = new Timer( TIMER_CONSTANT / hardness, new SheepGameTimerListener() );
		initilize( hardness);
		timer.start();
	}
	
	public boolean isGameOver(){
		if( !sheep.isAlive() )
		{
			timer.stop();
			return true;
		}
		return false;
	}
	
	public void addView( SheepGameView view ){
		views.add( view);
		updateViews();
	}
	
	public void updateViews(){
		for( int i = 0; i < views.size(); i++)
		{
			views.get( i).update( this);
		}
	}
	
	private void initilize( int hardness ){
		otherObjects = new ArrayList<LocatableShape>();
		
		for( int i = 0; i < hardness; i++){
			createNewObject();
		}
	}
	
	private void createNewObject() {
		random = new Random();
		
		int oType = random.nextInt( 2);
		int height = random.nextInt( 70) + 20;
		int width = random.nextInt( 70) + 20;
		int locX = random.nextInt( GAME_SIZE_X);
		int locY = random.nextInt( GAME_SIZE_Y);
		int value = random.nextInt(300);
		
//		System.out.println( "otype:\t" + oType
//							+ "\nheigt\t" + height
//							+ "\nwidth\t" + width
//							+ "\nlocX\t" + locX
//							+ "\nLocY\t" + locY);
		
		if( oType == 0){
			Grass grass = new Grass( value, locX, locY, width, height);
			if( !isOverlaping( grass) )
			{
				otherObjects.add( grass);
			}
		}
		else if( oType == 1){
			Water water = new Water( value, locX, locY, width, height);
			if( !isOverlaping( water) )
			{
				otherObjects.add( water);
			}
			
		}
		
		updateViews();
		
	}

	private boolean isOverlaping( LocatableShape shape) {
		
		for( int i = 0; i < otherObjects.size(); i++)
		{
			if( shape.overlaps( otherObjects.get(i)))
			{
				return true;
			}
		}
		
		return false;
	}

	private static void createAndShowGUI(){
		JFrame frame;
		SheepGame sheepGame;
		
		sheepGame = new SheepGame(10);
		SheepGameVisualViewPanel sgvp = new SheepGameVisualViewPanel( sheepGame);
		SheepHungerPanel hunger = new SheepHungerPanel( sheepGame);
		sheepGame.addView( sgvp);
		sheepGame.addView( hunger);
		
		frame = new JFrame("Sheep Game");
		frame.add( sgvp );
		frame.addKeyListener( new SheepGameKeyListener( sheepGame) );
		frame.add( hunger, BorderLayout.EAST );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.pack();
		frame.setVisible( true);
		
	}
	
	public static void main( String[] args){
		
		SwingUtilities.invokeLater( new Runnable(){
			public void run(){
				createAndShowGUI();
			}
		});
	}
	
	private class SheepGameTimerListener implements ActionListener{
		
		public void actionPerformed( ActionEvent e){
			createNewObject();
			updateViews();
		}
	}
	
	

	public Drawable[] getDrawables() {
		Drawable[] drawables = new Drawable[ otherObjects.size() + 1];
		
		for( int i = 0; i < otherObjects.size(); i++)
		{
			drawables[i] = otherObjects.get(i);
		}
		drawables[ drawables.length - 1] = sheep;
		return drawables;
	}

	public Sheep getSheep() {
		return sheep;
	}

}
