package com.saygindogu.sheepgame.ui;

import com.saygindogu.sheepgame.SheepGame;
import com.saygindogu.sheepgame.model.Sheep;
import com.saygindogu.sheepgame.model.Wolf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

public class SheepHungerPanel extends JPanel implements SheepGameView {

	SheepGame game;
	JLabel label;
	JProgressBar hunger;
	JProgressBar thirst;
	JLabel wolfWarning;

	public SheepHungerPanel( SheepGame game){
		label = new JLabel( "Hunger:\tThirst");
		hunger = new JProgressBar( SwingConstants.VERTICAL, 0, Sheep.MAX_HUNGER );
		thirst = new JProgressBar( SwingConstants.VERTICAL, 0, Sheep.MAX_THIRST );
		wolfWarning = new JLabel();
		wolfWarning.setFont( new Font( "SansSerif", Font.BOLD, 14));
		wolfWarning.setHorizontalAlignment( SwingConstants.CENTER);
		wolfWarning.setVisible( false);

		setLayout( new BorderLayout() );

		add( label, BorderLayout.NORTH);
		add( hunger, BorderLayout.WEST);
		add( thirst, BorderLayout.EAST);
		add( wolfWarning, BorderLayout.SOUTH);

		update( game);
	}

	@Override
	public void update(SheepGame game) {
		this.game = game;

		hunger.setValue( Sheep.MAX_HUNGER - game.getSheep().getHunger());
		thirst.setValue( Sheep.MAX_THIRST - game.getSheep().getThirst());

		// Wolf proximity warning
		double nearest = Double.MAX_VALUE;
		int sheepX = game.getSheep().getLocationX();
		int sheepY = game.getSheep().getLocationY();
		for( Wolf w : game.getWolves() ){
			double dist = Math.hypot( w.getLocationX() - sheepX, w.getLocationY() - sheepY);
			if( dist < nearest ){
				nearest = dist;
			}
		}
		if( nearest <= 150 ){
			wolfWarning.setText( "WOLF! " + (int) nearest + "px");
			wolfWarning.setForeground( Color.RED);
			wolfWarning.setVisible( true);
		} else if( nearest <= 400 ){
			wolfWarning.setText( "WOLF! " + (int) nearest + "px");
			wolfWarning.setForeground( Color.ORANGE);
			wolfWarning.setVisible( true);
		} else {
			wolfWarning.setVisible( false);
		}

		repaint();
	}


}
