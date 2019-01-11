package CardGameResources;

public class Player {
	private Hand myHand = new Hand();
	private Card cardPlayed = null;
	private boolean validMove = false;
	private int id = 0;
	
	public int getID(){ return id; }
	public void setID(int i){
		id = i;
	}
	public Hand getHand(){return myHand;}
	public void setHand(Hand h){ myHand = h; }
	public Card getCardPlayed(){ return cardPlayed; }
	public void setCardPlayed(Card c){ cardPlayed = c; }
	
	public void addCardtoHand(Card newCard){
		this.myHand.cards.add(newCard);
	}
	public void removeCardfromHand(Card removedCard){
		myHand.cards.remove(removedCard);
	}
	public boolean getValidMove() {
		return validMove;
	}
	public void setValidMove(boolean validMove) {
		this.validMove = validMove;
	}
}