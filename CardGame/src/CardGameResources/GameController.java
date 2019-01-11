package CardGameResources;
import java.util.ArrayList;

public class GameController {
	private Deck deck = new Deck();
	private ArrayList<Player> allPlayers = new ArrayList<Player>();
	private int currentPlayerIndex = 0;
	
	public Card setUpGame(int numStartCards){
		for(Player eachPlayer : allPlayers){
			dealCards(eachPlayer, numStartCards);
		}
		
		return deck.pullCard();
	}
	public Hand dealCards(Player player, int numCards){
		
		Hand drawnCards = new Hand();		//New cards add to hand in this method, for return purposes
		for(int j=0; j < numCards; j++){
			if(deck.getDeckCards().isEmpty()){		//Deck is empty
				if(!deck.refreshDeck()){			//Discard pile is empty
					return drawnCards;				//Return number of cards dealt so far (only cards left)
				}
			}
			
			Card newCard = deck.pullCard();		//Pull a card from the deck
			drawnCards.cards.add(newCard);		//Add card to hand of the new cards drawn
			player.addCardtoHand(newCard);		//Add card to player's hand
		}
		
		return drawnCards;
	}
	
	public void updatePlayerCard(int playerIndex, Card newCard){
		allPlayers.get(playerIndex).setCardPlayed(newCard);	//Record what card the player played
	}
	public void discardCard(Card discarded){
		deck.addtoDiscard(discarded);
	}
	
	public int getCurrentPlayerIndex(){
		return currentPlayerIndex;
	}
	public Player getCurrentPlayer(){
		return allPlayers.get(currentPlayerIndex);
	}
	public int nextPlayer(int direction){		//Moves to the next player in the specified direction		
		currentPlayerIndex += direction;
		
		if(currentPlayerIndex == allPlayers.size())
			currentPlayerIndex = 0;
		else if(currentPlayerIndex == -1)
			currentPlayerIndex = allPlayers.size()-1;
				
		return currentPlayerIndex;
	}
	public void addPlayer(Player newPlayer){
		allPlayers.add(newPlayer);
	}
	public ArrayList<Player> getAllPlayers(){ return allPlayers; }
}
