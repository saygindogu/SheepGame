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

	Random random = new Random();
	@Getter
	Sheep sheep;
	@Getter
	List<Wolf> wolves = new ArrayList<>();
	List<RestingSpot> restingSpots = new ArrayList<>();
	List<LocatableShape> otherObjects;
	List<SheepGameView> views;
	Timer timer;
	Timer physicsTimer;
	int difficultyLevel;

	public SheepGame( int difficultyLevel){
		if( difficultyLevel > 10){
			this.difficultyLevel = 10;
		}
		else if( difficultyLevel <= 0)
		{
			this.difficultyLevel = 1;
		}
		else
			this.difficultyLevel = difficultyLevel;

		sheep = new Sheep(this.difficultyLevel);
		views = new ArrayList<>();

		timer = new Timer( TIMER_CONSTANT / 10 * (this.difficultyLevel / 2 + 1), e -> {
			createNewObject();
			updateViews();
		});
		physicsTimer = new Timer( 16, e -> {
			sheep.tick();
			for( Wolf w : wolves ){
				w.tick( sheep.getLocationX(), sheep.getLocationY());
			}
			updateViews();
		});
		initilize( 11 - this.difficultyLevel);
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
		// Check wolf-sheep collisions first
		for( Wolf w : wolves ){
			if( w.overlaps( sheep) ){
				sheep.die();
				return;
			}
		}

		// Check resting spot collisions (never removed)
		sheep.setOnRestSpot( false);
		for( RestingSpot rs : restingSpots ){
			if( sheep.overlaps( rs) ){
				sheep.setOnRestSpot( true);
				sheep.rest( rs);
			}
		}

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

		// Create wolves based on difficulty
		int wolfCount = Math.max(0, (difficultyLevel - 1) / 2);
		double chaseSpeed = 2.0 + difficultyLevel * 0.25;
		double roamSpeed = 0.8 + difficultyLevel * 0.15;
		double detectRadius = 150 + difficultyLevel * 25;
		wolves = new ArrayList<>();
		for( int i = 0; i < wolfCount; i++){
			wolves.add( new Wolf( chaseSpeed, roamSpeed, detectRadius));
		}

		// Create resting spots — fewer at higher difficulty
		int restCount = Math.max(1, 4 - difficultyLevel / 3);
		restingSpots = new ArrayList<>();
		double scale = 1.0 - (difficultyLevel - 1) * 0.05;
		int minRestSize = Math.max(30, (int)(60 * scale));
		int restSizeRange = Math.max(20, (int)(40 * scale));
		for( int i = 0; i < restCount; i++){
			for( int attempt = 0; attempt < 20; attempt++){
				int w = minRestSize + random.nextInt(restSizeRange);
				int h = minRestSize + random.nextInt(restSizeRange);
				int locX = random.nextInt(GAME_SIZE_X - w);
				int locY = random.nextInt(GAME_SIZE_Y - h);
				RestingSpot rs = new RestingSpot(locX, locY, w, h, difficultyLevel);
				if( !isOverlaping(rs) && !isOverlapingRestingSpots(rs) ){
					restingSpots.add(rs);
					break;
				}
			}
		}
	}

	private void createNewObject() {
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
			if( !isOverlaping( grass) && !isOverlapingRestingSpots( grass) )
			{
				otherObjects.add( grass);
			}
		}
		else if( oType == 1){
			Water water = new Water( value, locX, locY, width, height);
			if( !isOverlaping( water) && !isOverlapingRestingSpots( water) )
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

	private boolean isOverlapingRestingSpots( LocatableShape shape) {
		for( RestingSpot rs : restingSpots ){
			if( shape.overlaps( rs) ){
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
		Drawable[] drawables = new Drawable[ restingSpots.size() + otherObjects.size() + wolves.size() + 1];

		int idx = 0;
		for( int i = 0; i < restingSpots.size(); i++){
			drawables[idx++] = restingSpots.get(i);
		}
		for( int i = 0; i < otherObjects.size(); i++){
			drawables[idx++] = otherObjects.get(i);
		}
		for( int i = 0; i < wolves.size(); i++){
			drawables[idx++] = wolves.get(i);
		}
		drawables[idx] = sheep;
		return drawables;
	}

}
