package com.saygindogu.sheepgame.ui;

import com.saygindogu.sheepgame.SheepGame;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class SheepGameKeyListener implements KeyListener{

	SheepGame game;
	Runnable onReturnToMenu;

	public SheepGameKeyListener( SheepGame game, Runnable onReturnToMenu){
		super();
		this.game = game;
		this.onReturnToMenu = onReturnToMenu;
	}

		@Override
		public void keyPressed(KeyEvent e) {
			if( game.isGameOver() ){
				onReturnToMenu.run();
				return;
			}

			int key = e.getKeyCode();

			if( key == KeyEvent.VK_UP || key == KeyEvent.VK_W ){
				game.getSheep().goUp();
			}
			else if( key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S ){
				game.getSheep().goDown();
			}
			else if( key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A ){
				game.getSheep().goLeft();
			}
			else if( key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D ){
				game.getSheep().goRight();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			int key = e.getKeyCode();

			if( key == KeyEvent.VK_UP || key == KeyEvent.VK_W ){
				game.getSheep().stopUp();
			}
			else if( key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S ){
				game.getSheep().stopDown();
			}
			else if( key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A ){
				game.getSheep().stopLeft();
			}
			else if( key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D ){
				game.getSheep().stopRight();
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}

	}
