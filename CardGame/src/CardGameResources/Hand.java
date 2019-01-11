package CardGameResources;
import java.util.ArrayList;

import CardGameResources.Card.Suits;

public class Hand {
	public ArrayList<Card> cards = new ArrayList<Card>();
	
	public boolean hasCard(int searchValue, Suits searchSuit){
		for(Card eachCard : cards){
			if(eachCard.getValue() == searchValue && eachCard.getSuit() == searchSuit)
				return true;
		}
		return false;
	}
	public boolean hasCardValue(int searchValue){
		for(Card eachCard : cards){
			if(eachCard.getValue() == searchValue)
				return true;
		}
		return false;
	}
}
