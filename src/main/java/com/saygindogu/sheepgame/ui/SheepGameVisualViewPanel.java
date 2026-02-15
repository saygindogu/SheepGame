package com.saygindogu.sheepgame.ui;

import com.saygindogu.sheepgame.SheepGame;

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
		}
		else{
			Drawable[] drawableList = game.getDrawables();

			for( Drawable drawable : drawableList )
			{
				drawable.draw(g);
			}
		}
	}

}
