package com.sheepgame;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class SheepGameVisualViewPanel extends JPanel implements SheepGameView {

	SheepGame game;
	
	public SheepGameVisualViewPanel( SheepGame game){
		this.game = game;
		setPreferredSize( new Dimension( SheepGame.GAME_SIZE_X, SheepGame.GAME_SIZE_Y));
		grabFocus();
	}
	
	@Override
	public void update(SheepGame game) {
		repaint();
	}
	
	@Override
	public void paintComponent( Graphics g){
		super.paintComponent(g);
		
		if( game.isGameOver() ){
			g.drawString( "Game Over!", getBounds().width / 2, getBounds().height / 2);
			//g.fillRect( getBounds().width / 2, getBounds().height / 2, 80, 80);
		}
		else{
			Drawable[] drawableList = game.getDrawables();
			
			for( int i = 0; i < drawableList.length; i++ )
			{
				drawableList[i].draw(g);
			}
		}
	}
	
	

}
