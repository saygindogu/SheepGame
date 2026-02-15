package com.saygindogu.sheepgame;

import com.saygindogu.sheepgame.model.*;
import com.saygindogu.sheepgame.ui.*;
import lombok.Getter;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class SheepGame {

	public static final int GAME_SIZE_X = 1200;
	public static final int GAME_SIZE_Y = 800;
	public static final int TIMER_CONSTANT = 10000;

	private static final String MENU_CARD = "menu";
	private static final String GAME_CARD = "game";

	Random random;
	@Getter
	Sheep sheep;
	List<LocatableShape> otherObjects;
	List<SheepGameView> views;
	Timer timer;
	Timer physicsTimer;
	int difficultyLevel;

	public SheepGame( int difficultyLevel){
		sheep = new Sheep(difficultyLevel);
		views = new ArrayList<>();

		if( difficultyLevel > 10){
			this.difficultyLevel = 10;
		}
		else if( difficultyLevel <= 0)
		{
			this.difficultyLevel = 1;
		}
		else
			this.difficultyLevel = difficultyLevel;

		timer = new Timer( TIMER_CONSTANT / 10 * (difficultyLevel / 2 + 1), e -> {
			createNewObject();
			updateViews();
		});
		physicsTimer = new Timer( 16, e -> {
			sheep.tick();
			updateViews();
		});
		initilize( 11 - difficultyLevel);
		timer.start();
		physicsTimer.start();
	}

	public boolean isGameOver(){
		if( !sheep.isAlive() )
		{
			timer.stop();
			physicsTimer.stop();
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

		// Scale size and value: difficulty 1 → full, difficulty 10 → 55%
		double scale = 1.0 - (difficultyLevel - 1) * 0.05;

		int oType = random.nextInt( 2);
		int maxSize = Math.max(10, (int)(70 * scale));
		int minSize = Math.max(5, (int)(20 * scale));
		int height = random.nextInt( maxSize) + minSize;
		int width = random.nextInt( maxSize) + minSize;
		int locX = random.nextInt( GAME_SIZE_X);
		int locY = random.nextInt( GAME_SIZE_Y);
		int value = random.nextInt( Math.max(1, (int)(300 * scale)));

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

	private static JFrame frame;
	private static CardLayout cardLayout;
	private static JPanel cards;
	private static JPanel gameContainer;
	private static SheepGame currentGame;

	private static void createAndShowGUI(){
		cardLayout = new CardLayout();
		cards = new JPanel( cardLayout);

		MainMenuPanel menuPanel = new MainMenuPanel( difficulty -> startGame( difficulty));
		cards.add( menuPanel, MENU_CARD);

		gameContainer = new JPanel( new BorderLayout());
		cards.add( gameContainer, GAME_CARD);

		frame = new JFrame("Sheep Game");
		frame.add( cards);
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible( true);

		cardLayout.show( cards, MENU_CARD);
	}

	private static void startGame( int difficulty ){
		gameContainer.removeAll();

		currentGame = new SheepGame( difficulty);
		SheepGameVisualViewPanel sgvp = new SheepGameVisualViewPanel( currentGame);
		SheepHungerPanel hunger = new SheepHungerPanel( currentGame);
		currentGame.addView( sgvp);
		currentGame.addView( hunger);

		gameContainer.add( sgvp, BorderLayout.CENTER);
		gameContainer.add( hunger, BorderLayout.EAST);

		// Remove old key listeners and add new one
		for( java.awt.event.KeyListener kl : frame.getKeyListeners() ){
			frame.removeKeyListener( kl);
		}
		frame.addKeyListener( new SheepGameKeyListener( currentGame, SheepGame::returnToMenu));

		cardLayout.show( cards, GAME_CARD);
		frame.pack();
		frame.requestFocus();
	}

	static void returnToMenu(){
		if( currentGame != null){
			currentGame.timer.stop();
			currentGame.physicsTimer.stop();
			currentGame = null;
		}
		gameContainer.removeAll();
		cardLayout.show( cards, MENU_CARD);
		frame.pack();
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
