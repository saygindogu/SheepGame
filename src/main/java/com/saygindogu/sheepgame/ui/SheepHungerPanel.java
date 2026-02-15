package com.saygindogu.sheepgame.ui;

import com.saygindogu.sheepgame.SheepGame;
import com.saygindogu.sheepgame.model.Sheep;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

public class SheepHungerPanel extends JPanel implements SheepGameView {

	SheepGame game;
	JLabel label;
	JProgressBar hunger;
	JProgressBar thirst;

	public SheepHungerPanel( SheepGame game){
		label = new JLabel( "Hunger:\tThirst");
		hunger = new JProgressBar( SwingConstants.VERTICAL, 0, Sheep.MAX_HUNGER );
		thirst = new JProgressBar( SwingConstants.VERTICAL, 0, Sheep.MAX_THIRST );

		setLayout( new BorderLayout() );

		add( label, BorderLayout.NORTH);
		add( hunger, BorderLayout.WEST);
		add( thirst, BorderLayout.EAST);

		update( game);
	}

	@Override
	public void update(SheepGame game) {
		this.game = game;

		hunger.setValue( Sheep.MAX_HUNGER - game.getSheep().getHunger());
		thirst.setValue( Sheep.MAX_THIRST - game.getSheep().getThirst());
		repaint();
	}


}
