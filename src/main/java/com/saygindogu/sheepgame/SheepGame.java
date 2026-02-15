package com.saygindogu.sheepgame;

import com.saygindogu.sheepgame.ui.*;
import lombok.Getter;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class SheepGame {

	public static final int GAME_SIZE_X = 1200;
	public static final int GAME_SIZE_Y = 800;
	public static final int TIMER_CONSTANT = 10000;

	Random random;
	@Getter
	Sheep sheep;
	List<LocatableShape> otherObjects;
	List<SheepGameView> views;
	Timer timer;
	int hardness;

	public SheepGame( int hardness){
		sheep = new Sheep();
		views = new ArrayList<>();

		if( hardness > 10){
			this.hardness = 10;
		}
		else if( hardness <= 0)
		{
			this.hardness = 1;
		}
		else
			this.hardness = hardness;

		timer = new Timer( TIMER_CONSTANT / hardness, e -> {
			createNewObject();
			updateViews();
		});
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
		checkCollisions();
		for( SheepGameView view : views )
		{
			view.update( this);
		}
	}

	private void checkCollisions(){
		Iterator<LocatableShape> it = otherObjects.iterator();
		while( it.hasNext() ){
			LocatableShape obj = it.next();
			if( sheep.overlaps( obj) ){
				if( obj instanceof Grass ){
					sheep.eat( (Grass) obj);
					it.remove();
				}
				else if( obj instanceof Water ){
					sheep.drink( (Water) obj);
					it.remove();
				}
			}
		}
	}

	private void initilize( int hardness ){
		otherObjects = new ArrayList<>();

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

		for( LocatableShape other : otherObjects )
		{
			if( shape.overlaps( other))
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

		SwingUtilities.invokeLater( () -> createAndShowGUI() );
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

}
