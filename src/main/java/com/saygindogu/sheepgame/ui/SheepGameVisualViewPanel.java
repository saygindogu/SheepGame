package com.saygindogu.sheepgame.ui;

import com.saygindogu.sheepgame.SheepGame;
import com.saygindogu.sheepgame.model.Drawable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
			g.setFont( new Font( "SansSerif", Font.BOLD, 48));
			g.setColor( Color.RED);
			int cx = getBounds().width / 2;
			int cy = getBounds().height / 2;
			g.drawString( "The Sheep Died!", cx - 140, cy - 20);
			g.setFont( new Font( "SansSerif", Font.PLAIN, 20));
			g.setColor( Color.BLACK);
			g.drawString( "Press any key to return to menu", cx - 170, cy + 30);
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
