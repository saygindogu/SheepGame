package com.sheepgame;

public abstract class LocatableShape implements Shape, Locatable {

	public boolean overlaps( LocatableShape other ){
		int x, otherX;
		int y, otherY;
		int h, otherH;
		int w, otherW;
		
		x = getLocationX();
		y = getLocationY();
		h = getHeigth();
		w = getWidth();
		
		otherX = other.getLocationX();
		otherY = other.getLocationY();
		otherH = other.getHeigth();
		otherW = other.getWidth();
		
		if( x > otherX ){
			if( x - otherX > otherW )
				return false;
		}
		else{
			if( otherX - x > w )
				return false;
		}
		
		if( y > otherY){
			if( y - otherY > otherH )
				return false;
		}
		else{
			if( otherY - y > h)
				return false;
		}
		
		return true;
	}
}
