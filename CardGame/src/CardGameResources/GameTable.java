package CardGameResources;

import java.util.ArrayList;

public class GameTable {
	private Card cardinPlay;
	private int drawAmount = 0;
	private ArrayList<Integer> playerCardCounts = new ArrayList<Integer>();
	private boolean clockwise = true;
	private boolean skipTurn = false;
	private boolean openSuit = false;
	
	public GameTable(){}
	public GameTable(int count){	//Gives the array, holding player hand counts, the correct dimension corresponding to the number of players
		for(int j=0; j<count; j++)
			playerCardCounts.add(0);
	}
	
	public Card getCardinPlay() { return cardinPlay; }
	public Card setCardinPlay(Card newCard){
		if(!openSuit){
			Card hold = cardinPlay;		//Holds the previous card in play to be moved to the discard pile
			cardinPlay = newCard;
			return hold;
		}
		else{
			cardinPlay = newCard;		//Simply updating the card for a crazy 8 suit change
			openSuit = false;
			return newCard;
		}
	}
	
	public void updateCardCount(int index, int count){		//Updates the number of cards in a player's hand
		playerCardCounts.set(index, count);
	}
	public ArrayList<Integer> getCardCounts(){ return playerCardCounts; }
	
	public boolean getSkip(){ return skipTurn; }
	public void setSkip(boolean s){
		skipTurn = s;
	}
	
	public boolean getOpenSuit(){ return openSuit; }
	public void setOpenSuit(boolean o){
		openSuit = o;
	}
	
	public boolean getPlayDirection(){ return clockwise; }
	public void changePlayDirection(){	//Reverses the direction of play
		if(clockwise)
			clockwise = false;
		else
			clockwise = true;
	}
	
	public void addtoDrawAmount(int d){ 
		drawAmount += d;
	}
	public void resetDrawAmount(){
		drawAmount = 0;
	}
	public int getDrawAmount(){ return drawAmount; }
	
}
