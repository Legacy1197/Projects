import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import CardGameResources.Card;
import CardGameResources.Card.Suits;
import CardGameResources.GameTable;
import CardGameResources.Hand;
import CardGameResources.Messages;
import CardGameResources.Player;

public class ClientNetwork{
	private GameTable currentGame = new GameTable();
	private Player myInfo = new Player();
	private boolean endProgram = false;
	private int TCPport = 30001, UDPport = 30001;
	
	public void main(){
		Client client = new Client(); 
		client.start(); 
		try {
			client.connect(5000, "127.0.0.1", TCPport, UDPport);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		System.out.println("Client: Connected");
		
		       
		Kryo kryoClient = client.getKryo(); 	//Setup classes to be sent and received
		kryoClient.register(GameTable.class); 
		kryoClient.register(Messages.class);
		kryoClient.register(Card.class);
		kryoClient.register(Hand.class);
		kryoClient.register(ArrayList.class);
		kryoClient.register(Card.Suits.class);
		kryoClient.register(int[].class);
		
		Scanner in = new Scanner(System.in);	//Will be used for user input until UI is running
		
		Listener getServerMsg = new Listener() { 			//Wait for a response from the server
			public void received (Connection connection, Object object) { 
			
			if(object instanceof GameTable){ 
				 //Update the game table with the recieved object from the server
				 //System.out.println("Client: Updating Game Table");
				 GameTable updatedGame = (GameTable) object;
				 currentGame = updatedGame;
				 
				 System.out.println("Current Face Card: " + currentGame.getCardinPlay().getValue() + " " + currentGame.getCardinPlay().getSuit());
			 
			
			}
			if(object instanceof Hand){
				  //System.out.println("Client: Received hand");
				  //Update hand with the recieved hand from the server
				  Hand newHand = (Hand) object;
				  for(Card eachCard : newHand.cards)
					  myInfo.addCardtoHand(eachCard);
				  
			}
			if(object instanceof Card){
				  //Add the card from the server to the hand
				  Card newCard = (Card) object;
				  myInfo.addCardtoHand(newCard);
			}
			if(object instanceof Messages){
				  connection.setTimeout(0);		//Removes timeout on connection to allow player time to make a choice.
				  Messages serverMsg = (Messages) object;
				  
				  
				  
				  if(serverMsg.invalidMove || serverMsg.playCard){		//If a move needs to be made either first or again due to previous error

					  if(serverMsg.invalidMove)	//Print out the reason for the invalid move and prompt to try again
						  System.out.println(serverMsg.invalidMessage + " Please try again.");
					  
					  
					  //Prints out the current status of the game
					  ArrayList<Integer> cardCounts = currentGame.getCardCounts();	//Gets the card counts of each player, stored for each of use
					  for(int j =0; j< cardCounts.size(); j++){
						  if(j != myInfo.getID())
							  System.out.println("Player " + j + " has " + cardCounts.get(j) + " cards.");
					  }
					  System.out.println("Face Card: " + currentGame.getCardinPlay().getValue() + " " + currentGame.getCardinPlay().getSuit());
					  System.out.println("Your Hand:");
					  for(int j = 0; j < myInfo.getHand().cards.size(); j++){
						  Card eachCard = myInfo.getHand().cards.get(j);
						  System.out.println(j + ". " + eachCard.getValue() + " " + eachCard.getSuit());
					  }
					  
					  //Prompt the user to choose a card
					  System.out.println("Client: Please choose a card");
					  boolean invalid;
					  int choice = -1;
					  
					  do{
						  invalid = false;
						  try{
								  choice = in.nextInt();
								  if(choice < 0 || choice >= myInfo.getHand().cards.size())
								  {
									  invalid = true;
									  System.out.println("Client: Please choose from your hand");
								  }
						  }catch(InputMismatchException e){
							  System.out.println("Client: Please choose from your hand");
							  invalid = true;
						  }
						  
						  in.nextLine();	//Clears newline character
					  }while(invalid);
					  
					  //Turn the choice into a card from the players hand
					  Card chosenCard = myInfo.getHand().cards.get(choice);
					  myInfo.setCardPlayed(chosenCard);
					 
					  //Send the chosen card object back to the server
					  connection.sendTCP(chosenCard);
				  }
				  if(serverMsg.validMove){
            		  myInfo.removeCardfromHand(myInfo.getCardPlayed());		//Remove the card played from hand			            		  
            	  }
				  if(serverMsg.startGame){
					  //Use UI object to display the starting setup of the game (player's hand)
					  
					  myInfo.setID(serverMsg.idNumber);		//Set the id number
					  System.out.println("Starting game. Please wait for your turn.");
				  }
				  if(serverMsg.endGame){
					  endProgram = true;
				  	  System.out.println("Game Over.");
				  	  if(myInfo.getHand().cards.size() == 0)
				  		  System.out.println("WINNER!");
				  	  else
				  		  System.out.println("YOU LOSE!");
				  }
				  if(serverMsg.skipped){
					  System.out.println("Your turn was skipped.");
				  }
				  if(serverMsg.chooseSuit){
					  //Get suit choice to change suit of wildcard played
					  System.out.println("Client: Please choose a suit.");
					  System.out.println("1. Diamond");
					  System.out.println("2. Heart");
					  System.out.println("3. Spade");
					  System.out.println("4. Club");
					  
					  boolean invalid;
					  int choice = -1;
					  
					  do{
						  invalid = false;
						  try{
								  choice = in.nextInt();
								  if(choice < 1 || choice > 4)
								  {
									  invalid = true;
									  System.out.println("Client: Please choose a suit");
								  }
						  }catch(InputMismatchException e){
							  System.out.println("Client: Please choose a suit");
							  invalid = true;
						  }
						  
						  in.nextLine();	//Clears newline character
					  }while(invalid);
					  
					  Suits suitChoice = Suits.DIAMOND;
					  switch(choice){
					  
					  case 1: suitChoice = Suits.DIAMOND;
					  	break;
					  case 2: suitChoice = Suits.HEART;
			  			break;
					  case 3: suitChoice = Suits.SPADE;
			  			break;
					  case 4: suitChoice = Suits.CLUB;
			  			break;
					  }
					  
					  Card suitChange = myInfo.getCardPlayed();		//Change card played in own hand to reflect chosen suit change
					  suitChange.setSuit(suitChoice);
					  myInfo.setCardPlayed(suitChange);
					  connection.sendTCP(suitChoice);		//Send the suit change choice to the server	  
					  
				  }
				  if(serverMsg.drawCards){
					  System.out.println(serverMsg.drawReason);
				  }
				}
			} 
		};
		
		client.addListener(getServerMsg);
		
		Messages startConnection = new Messages();		//Create a message that this client is connected to the server
		startConnection.clientConnected = true;
		client.sendTCP(startConnection); 				//Send message to the server
		//System.out.println("Client: Connected msg to Server");
		
		while(!endProgram){
			//Client will continually listen for messages from server until eventually the endGame message is received
			//and this loop breaks
		}
		in.close();
		client.stop();
	}
}
