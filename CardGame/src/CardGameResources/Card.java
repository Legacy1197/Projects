package CardGameResources;

public class Card {
	public enum Suits { SPADE, CLUB, HEART, DIAMOND }
	private Suits suit;
	private int value;
	
	public Card(){
		this.suit = null;
		this.value = 0;
	}
	public Card(Suits s, int v){
		suit = s;
		value = v;
	}
	public Suits getSuit(){ return suit;}
	public int getValue(){return value;}
	
	public boolean setSuit(Suits s){
		if(value == 8){
			suit = s;
			return true;
		}
		else
			return false;
	}

	@Override 
	public boolean equals(Object o){
		if(this == o)
			return true;
		if(! (o instanceof Card))
			return false;
		
		Card checkCard = (Card) o;
		
		return ( (this.getSuit() == checkCard.getSuit()) && (this.getValue() == checkCard.getValue()) );
	}
}
