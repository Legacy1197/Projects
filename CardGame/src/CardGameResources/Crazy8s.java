package CardGameResources;

import java.util.ArrayList;

public class Crazy8s extends CardGame{
	@Override
	public boolean endCondition(Player currentPlayer) {		//End condition is a player sheds their hand
		return currentPlayer.getHand().cards.isEmpty();
	}

	@Override
	public void updateScores(ArrayList<Player> allPlayers) {
		// Not required for this shedding game
	}
	
	@Override
	public boolean validate(Card checkCard, Card faceCard, boolean offensive) {
		if(offensive){	//An offensive card is the face card and has not been resolved by a previous player
			if(checkCard.getValue() == 1)		//Played a 1
				return true;
			if(faceCard.getValue() == 2 && checkCard.getValue() == 2)	//Played a 2 and face card is a 2
				return true;
			if(faceCard.getValue() == 12 && (checkCard.getValue() == 12 || (checkCard.getValue() == 2 && checkCard.getSuit() == faceCard.getSuit())))	//2 is same suit as 12 or face card is a 2
				return true;
			else
				return false;
		}
		
		if(checkCard.getValue() == 1 || checkCard.getValue() == 8)	//Wild cards can be played on anything
			return true;
		if( (checkCard.getSuit() != faceCard.getSuit()) && (checkCard.getValue() != faceCard.getValue()))	//Either suit or value must match face card
			return false;
		return true;
	}
	
	public boolean mustDraw(Hand playerHand, Card faceCard){
		if(playerHand.hasCardValue(1))		//Player has an ace to block offensive draw
			return false;
		if(playerHand.hasCardValue(2))		//Player has a 2
			if(faceCard.getValue() == 2 || (faceCard.getValue() == 12 && playerHand.hasCard(2, faceCard.getSuit())))	//2 is same suit as 12 or face card is a 2
				return false;
		if(faceCard.getValue() == 12 && playerHand.hasCardValue(12)){		//Draw card played was a queen and player has a queen to stack
			return false;
		}
		
		return true;			//Offensive draw cards were played and the player has no defense/stackable card to avoid drawing
	}
	
	public void specialtyCard(Card newCard, GameTable gameTable){
		//Updates the game state, if a special card was played, based on the card's effect 
		if(newCard.getValue() == 1){
			gameTable.resetDrawAmount();
		}
		else if(newCard.getValue() == 2){
			gameTable.addtoDrawAmount(2);
		}
		else if(newCard.getValue() == 12){
			gameTable.addtoDrawAmount(4);
		}
		else if(newCard.getValue() == 7){
			gameTable.setSkip(true);
		}
		else if(newCard.getValue() == 8){
			gameTable.setOpenSuit(true);
		}
		else if(newCard.getValue() == 11){
			gameTable.changePlayDirection();
		}
	}
	
}
