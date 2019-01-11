package CardGameResources;
import java.util.ArrayList;
import java.util.Random;

public class Deck {
	private ArrayList<Card> deckCards = new ArrayList<Card>();
	private ArrayList<Card> discardPile = new ArrayList<Card>();
	
	public Deck(){		//Creates a standard deck, shuffling not required as pulls are random
		for(int j=0; j< 52; j++){
			if(j < 13)
				deckCards.add(new Card(Card.Suits.CLUB, j+1));
			else if(j < 26)
				deckCards.add(new Card(Card.Suits.DIAMOND, (j%13)+1 ));
			else if(j < 39)
				deckCards.add(new Card(Card.Suits.HEART, (j%13)+1 ));
			else
				deckCards.add(new Card(Card.Suits.SPADE, (j%13)+1 ));
		}
	}
	
	public ArrayList<Card> getDeckCards(){ return deckCards; }
	public ArrayList<Card> getDiscardPile(){ return discardPile; }
	
	public boolean refreshDeck(){	//Move the discard pile back into the deck
		if(discardPile.isEmpty())
			return false;
		else{
			for(Card eachCard : discardPile){
				deckCards.add(eachCard);
			}
			discardPile.clear();
			
			return true;
		}
	}
	
	public void addtoDiscard(Card discarded){		//Move the given card to the discard pile
		discardPile.add(discarded);
	}
	
	public Card pullCard(){		//Pull a random card from the deck
		Random rand = new Random();
		return deckCards.remove( rand.nextInt(deckCards.size()));
	}
}
