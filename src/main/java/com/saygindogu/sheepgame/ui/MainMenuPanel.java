package com.saygindogu.sheepgame.ui;

import com.saygindogu.sheepgame.SheepGame;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

public class MainMenuPanel extends JPanel {

	public MainMenuPanel( Consumer<Integer> onStart ){
		setPreferredSize( new Dimension( SheepGame.GAME_SIZE_X, SheepGame.GAME_SIZE_Y));
		setLayout( new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.insets = new Insets( 10, 10, 10, 10);

		JLabel title = new JLabel( "Sheep Game", SwingConstants.CENTER);
		title.setFont( new Font( "SansSerif", Font.BOLD, 48));
		gbc.gridy = 0;
		add( title, gbc);

		JLabel description = new JLabel( "As a sheep, roam around to find grass and water not to die!");
		description.setFont( new Font( "SansSerif", Font.ITALIC, 16));
		gbc.gridy = 1;
		add( description, gbc);

		JLabel diffLabel = new JLabel( "Difficulty: 5");
		diffLabel.setFont( new Font( "SansSerif", Font.PLAIN, 18));
		gbc.gridy = 2;
		add( diffLabel, gbc);

		JSlider slider = new JSlider( 1, 10, 5);
		slider.setMajorTickSpacing( 1);
		slider.setPaintTicks( true);
		slider.setPaintLabels( true);
		slider.setSnapToTicks( true);
		slider.addChangeListener( e -> diffLabel.setText( "Difficulty: " + slider.getValue()));
		gbc.gridy = 3;
		add( slider, gbc);

		JButton startButton = new JButton( "Start Game");
		startButton.setFont( new Font( "SansSerif", Font.PLAIN, 24));
		startButton.addActionListener( e -> onStart.accept( slider.getValue()));
		gbc.gridy = 4;
		add( startButton, gbc);
	}

}
