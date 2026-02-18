package com.saygindogu.sheepgame.ui;

import com.saygindogu.sheepgame.SheepGame;
import com.saygindogu.sheepgame.model.Sheep;
import com.saygindogu.sheepgame.model.Wolf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

public class SheepHungerPanel extends JPanel implements SheepGameView {

	SheepGame game;
	JLabel label;
	JProgressBar hunger;
	JProgressBar thirst;
	JProgressBar stamina;
	JLabel wolfWarning;

	public SheepHungerPanel( SheepGame game){
		label = new JLabel( "Hunger  Thirst  Energy");
		hunger = new JProgressBar( SwingConstants.VERTICAL, 0, Sheep.MAX_HUNGER );
		thirst = new JProgressBar( SwingConstants.VERTICAL, 0, Sheep.MAX_THIRST );
		stamina = new JProgressBar( SwingConstants.VERTICAL, 0, Sheep.MAX_FATIGUE );
		wolfWarning = new JLabel();
		wolfWarning.setFont( new Font( "SansSerif", Font.BOLD, 14));
		wolfWarning.setHorizontalAlignment( SwingConstants.CENTER);
		wolfWarning.setVisible( false);

		setLayout( new BorderLayout());

		JPanel barsPanel = new JPanel( new GridLayout( 1, 3));
		barsPanel.add( hunger);
		barsPanel.add( thirst);
		barsPanel.add( stamina);

		add( label, BorderLayout.NORTH);
		add( barsPanel, BorderLayout.CENTER);
		add( wolfWarning, BorderLayout.SOUTH);

		update( game);
	}

	@Override
	public void update(SheepGame game) {
		this.game = game;

		hunger.setValue( Sheep.MAX_HUNGER - game.getSheep().getHunger());
		thirst.setValue( Sheep.MAX_THIRST - game.getSheep().getThirst());

		// Stamina bar: full = fresh, empty = exhausted
		double fatigue = game.getSheep().getFatigue();
		stamina.setValue( Sheep.MAX_FATIGUE - (int) fatigue);

		// Stamina bar color based on fatigue level
		if( fatigue > 90 ){
			stamina.setForeground( Color.RED);
		} else if( fatigue > 70 ){
			stamina.setForeground( Color.ORANGE);
		} else {
			stamina.setForeground( new Color( 0x33, 0x99, 0x33)); // default green
		}

		// Wolf proximity warning — thresholds derived from each wolf's detection radius
		double nearest = Double.MAX_VALUE;
		double nearestDetectRadius = 0;
		int sheepX = game.getSheep().getLocationX();
		int sheepY = game.getSheep().getLocationY();
		for( Wolf w : game.getWolves() ){
			double dist = Math.hypot( w.getLocationX() - sheepX, w.getLocationY() - sheepY);
			if( dist < nearest ){
				nearest = dist;
				nearestDetectRadius = w.getDetectionRadius();
			}
		}
		// Red when inside detection radius (wolf is chasing), orange when within 1.5x radius
		if( nearest <= nearestDetectRadius ){
			wolfWarning.setText( "WOLF! " + (int) nearest + "px");
			wolfWarning.setForeground( Color.RED);
			wolfWarning.setVisible( true);
		} else if( nearest <= nearestDetectRadius * 1.5 ){
			wolfWarning.setText( "WOLF! " + (int) nearest + "px");
			wolfWarning.setForeground( Color.ORANGE);
			wolfWarning.setVisible( true);
		} else {
			wolfWarning.setVisible( false);
		}

		repaint();
	}


}
