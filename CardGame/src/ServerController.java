import CardGameResources.Card;
import CardGameResources.Card.Suits;
import CardGameResources.Crazy8s;
import CardGameResources.GameController;
import CardGameResources.GameTable;
import CardGameResources.Hand;
import CardGameResources.Messages;
import CardGameResources.Player;

public class ServerController {
	private GameController gameController = new GameController();
	private Crazy8s cardGame = new Crazy8s();
	private GameTable gameTable = new GameTable(4);		//Needs to get size from cardGame
	

	public boolean startMessage(Messages newMsg){
		if(newMsg.clientConnected){
			 System.out.println("Server: A client connected.");
        	 Player newPlayer = new Player();	//Create a new player object for the client
        	 newPlayer.setID(gameController.getAllPlayers().size());
        	 gameController.addPlayer(newPlayer);	//Add that player to the list of all players
        	 
        	 return true;
		}
		else
			return false;
	}
	
	public void startGame(){	
		Card firstFaceCard = gameController.setUpGame(cardGame.getnumStartCards());		//Deal cards to each player and the face card
		gameTable.setCardinPlay(firstFaceCard);		//Set the face card
	}
	
	public boolean isGameOver(){	//Checks end condition set by rules, in this case if the player's hand is empty
		return cardGame.endCondition(gameController.getCurrentPlayer());
	}
	public boolean allPlayersConnected(){	//Checks if the correct number of players have been created
		if(cardGame.getnumPlayers() > gameController.getAllPlayers().size())
			return false;
		else
			return true;
	}
	public int currentPlayerIndex(){	//Gets the current player index from GameController
		return gameController.getCurrentPlayerIndex();
	}
	public GameTable getGameTable(){		//Updates the player card counts and then returns the game table
		for(Player eachPlayer : gameController.getAllPlayers()){
			gameTable.updateCardCount(eachPlayer.getID(), eachPlayer.getHand().cards.size());
		}
				
		return this.gameTable;
	}
	public Hand getPlayerHand(int playerIndex){		//Gets a specific players hand from GameController
		return gameController.getAllPlayers().get(playerIndex).getHand();
	}
	
	public boolean checkPlayerCard(Card newCard){	//Handles validation and updating the game
		
		if(cardGame.validate(newCard, gameTable.getCardinPlay(), (gameTable.getDrawAmount() == 0 ? false : true)  )){
			
			gameController.updatePlayerCard(gameController.getCurrentPlayerIndex(), newCard);	//Update Player's played card
	        gameController.getCurrentPlayer().removeCardfromHand(newCard);	//Remove played card from players hand
	        Card movetoDiscard = gameTable.setCardinPlay(newCard);	//Change face up card on card table
	        gameController.discardCard(movetoDiscard); 				//Move the old card to the discard pile
	        
	        cardGame.specialtyCard(newCard, gameTable);		//Handles specialty card effects
	        
	        gameController.getCurrentPlayer().setValidMove(true);	//Player has made a valid move
	        return true;
		}
		else{
	        gameController.getCurrentPlayer().setValidMove(false);	//Player has made an invalid move
			
			return false;
		}
	}
	
	public Messages playerTurn(){
		Messages turnMsg = new Messages();
	
		int drawAmount = possibleValidMove();
		
		if(gameTable.getSkip()){		//This player was skipped
			turnMsg.skipped = true;
			gameTable.setSkip(false);
		}
		else if(gameTable.getOpenSuit()){		//Player needs to choose a suit for previously places wild card
			turnMsg.chooseSuit = true;
		}
		else if(drawAmount == 0){		//Player has a valid move, denoted by 0 cards needing to be drawn
			turnMsg.playCard = true;
		}
		else{		//Player does not have a valid move
			turnMsg.drawCards = true;
			if(drawAmount == 1)
				turnMsg.drawReason = "No moves available. Draw 1 card and forfeit turn.";
			else
				turnMsg.drawReason = "Draw cards due to played offensive cards.";
		}
			
		return turnMsg;
	}
	
	public Hand playerDraw(){	//Sends drawn cards and then resets the number of cards needing to be drawn
		int drawAmount = gameTable.getDrawAmount();
		gameTable.resetDrawAmount();
		return gameController.dealCards(gameController.getCurrentPlayer(), drawAmount);
		
	}
	
	public void setCrazy8(Suits choice){	//Updates the player and table wildcard suit
		Card crazy8 = gameController.getCurrentPlayer().getCardPlayed();
		crazy8.setSuit(choice);
		
		gameController.getCurrentPlayer().setCardPlayed(crazy8);
		gameTable.setCardinPlay(crazy8);
		
		gameController.getCurrentPlayer().setValidMove(true);
	}
	
	public int possibleValidMove(){
		if(gameTable.getDrawAmount() > 0){		//Offensive draw cards have been played, changing which cards the player can play
			if(cardGame.mustDraw(gameController.getCurrentPlayer().getHand(), gameTable.getCardinPlay()))		//Can the player avoid drawing
				return gameTable.getDrawAmount();		//Player must draw cards, totaling the number of offensive effects stacked
			else		//The player can avoid drawing cards
				return 0;
		}
		else{
			for(Card eachCard : gameController.getCurrentPlayer().getHand().cards){
				if(cardGame.validate(eachCard, gameTable.getCardinPlay(), false))
					return 0;
			}
			
			gameTable.addtoDrawAmount(1);
			return 1;	//Player has no valid move and must draw a card
		}

	}
	
	public boolean validMove(){	//Checks if current player made a valid move, if so then reset that value
		if(gameController.getCurrentPlayer().getValidMove()){
			gameController.getCurrentPlayer().setValidMove(false);
			return true;
		}
		else
			return false;
	}
	
	public void nextPlayer(){
		if(!gameTable.getOpenSuit()){		//Do not move to next player if this player still needs to set wild card suit
			if(gameTable.getPlayDirection())
				gameController.nextPlayer(1);
			else
				gameController.nextPlayer(-1);
		}
		
	}
}
