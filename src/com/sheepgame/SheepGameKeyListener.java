package com.sheepgame;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class SheepGameKeyListener implements KeyListener{

	SheepGame game;
	
	public SheepGameKeyListener( SheepGame game){
		super();
		this.game = game;
		
	}
		@Override
		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();
			
			if( key == KeyEvent.VK_UP ){
				game.getSheep().goUp();
			}
			else if( key == KeyEvent.VK_DOWN ){
				game.getSheep().goDown();
			}
			else if( key == KeyEvent.VK_LEFT ){
				game.getSheep().goLeft();
			}
			else if( key == KeyEvent.VK_RIGHT ){
				game.getSheep().goRight();
			}
			
			game.updateViews();
			System.out.println( "lan!?");
			
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}