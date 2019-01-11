import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import CardGameResources.Card;
import CardGameResources.GameTable;
import CardGameResources.Hand;
import CardGameResources.Messages;
import CardGameResources.Player;
import CardGameResources.Card.Suits;


public class Test_ServerClient {
	
	private static ServerController serverController = new ServerController();
	private static boolean endProgram = false;
	private static int TCPport = 30003, UDPport = 30003;	
	
	/////////
	private static GameTable currentGame = new GameTable();
	private static Player myInfo = new Player();
	private static Player myInfo2 = new Player();
	/////////
	
	public static void main(String []args) throws IOException, InterruptedException{

			Server server = new Server(); 
			server.start();
			
			try{
				server.bind(TCPport, UDPport);		//If server program is ended improperly, new port numbers may be necessary as old server was not disconnected properly
				System.out.println("Server: Connected");
			}catch(BindException e){
				System.out.println("Selected port already in use.");
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
			
			Kryo kryo = server.getKryo(); 
			kryo.register(GameTable.class); 
			kryo.register(Messages.class);
			kryo.register(Card.class);
			kryo.register(Hand.class);
			kryo.register(ArrayList.class);
			kryo.register(Card.Suits.class);
			kryo.register(int[].class);
			
			
			
			//////////////////////////////////////////////////
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
			/////////////////////////////////////////////////
			//////////////////////////////////////////////////
			Client client2 = new Client(); 
			client2.start(); 
			try {
			client2.connect(5000, "127.0.0.1", TCPport, UDPport);
			} catch (IOException e) {
			e.printStackTrace();
			} 
			System.out.println("Client2: Connected");
			
			
			Kryo kryoClient2 = client2.getKryo(); 	//Setup classes to be sent and received
			kryoClient2.register(GameTable.class); 
			kryoClient2.register(Messages.class);
			kryoClient2.register(Card.class);
			kryoClient2.register(Hand.class);
			kryoClient2.register(ArrayList.class);
			kryoClient2.register(Card.Suits.class);
			kryoClient2.register(int[].class);
			/////////////////////////////////////////////////
			
			
			
			
			
			ArrayList<Connection> clientConnections = new ArrayList<Connection>();
			Listener newClient = new Listener() { 
		        public void received (Connection connection, Object object) { 
		              if (object instanceof Messages) { 
		                 Messages clientMsg = (Messages)object;
		                 
		                 if(serverController.startMessage(clientMsg))
		                	 clientConnections.add(connection);
		          
		              }
		           } 
		        };
			
			server.addListener(newClient);
			
			//////////////////////////
			Messages startConnection = new Messages();		//Create a message that this client is connected to the server
			startConnection.clientConnected = true;
			client.sendTCP(startConnection); 				//Send message to the server
			System.out.println("Client: Connected msg to Server");
			//////////////////////////
			//////////////////////////
			client2.sendTCP(startConnection); 				//Send message to the server
			System.out.println("Client2: Connected msg to Server");
			//////////////////////////
			
			
			while(!serverController.allPlayersConnected()){
				//Program holds while clients connect to server, resumes once set number of clients have connected
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			server.removeListener(newClient);	//The server stops listening for more clients
		
			System.out.println("Server: All clients connected");
			
			//Deal cards to Players then send those hands to each client and tell them the game is starting
			serverController.startGame();
			
			Messages startGameMsg = new Messages();
			startGameMsg.startGame = true;
			
			
			/////////////////////////////////////
			Scanner in = new Scanner(System.in);	//Will be used for user input until UI is running
			
			Listener getServerMsg = new Listener() { 			//Wait for a response from the server
				public void received (Connection connection, Object object) { 
				
				if(object instanceof GameTable){ 
					 GameTable updatedGame = (GameTable) object;
					 currentGame = updatedGame;
				 
				 
				 //Use UI object to display updated GameTable, currentGame
				}
				if(object instanceof Hand){
					  
					  Hand newHand = (Hand) object;
					  System.out.println("Client: Received hand. " + newHand.cards.size());
					  for(Card eachCard : newHand.cards)
						  myInfo.addCardtoHand(eachCard);
					  
				}
				if(object instanceof Card){
					  Card newCard = (Card) object;
					  myInfo.addCardtoHand(newCard);
				}
				if(object instanceof Messages){
					  connection.setTimeout(0);		//Removes timeout on connection to allow player time to make a choice.
					  Messages serverMsg = (Messages) object;
					  
					  
					  
					  if(serverMsg.invalidMove || serverMsg.playCard){		//If either a move needs to be made first or again due to previous error

						  if(serverMsg.invalidMove)	//Print out the reason for the invalid move and prompt to try again
							  System.out.println(serverMsg.invalidMessage + " Please try again.");
						  
						  ArrayList<Integer> cardCounts = currentGame.getCardCounts();
						  for(int j =0; j< cardCounts.size(); j++){
							  if(j != myInfo.getID())
								  System.out.println("Player " + j + " has " + cardCounts.get(j) + " cards.");
						  }
						  
						  System.out.println("Game Direction: " + (currentGame.getPlayDirection() ? "Clockwise." : "Counterclockwise."));
						  System.out.println("Face Card: " + currentGame.getCardinPlay().getValue() + " " + currentGame.getCardinPlay().getSuit());
						  System.out.println("Player " + myInfo.getID() + " Hand:");
						  for(int j = 0; j < myInfo.getHand().cards.size(); j++){
							  Card eachCard = myInfo.getHand().cards.get(j);
							  System.out.println(j + ". " + eachCard.getValue() + " " + eachCard.getSuit());
						  }
						  
						  //Use UI object to prompt the user to make a move choice
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
						  myInfo.setID(serverMsg.idNumber);
						  System.out.println("Client: Starting game");
					  }
					  if(serverMsg.endGame){
						  endProgram = true;
					  	  System.out.println("Game Over. Client closing.");
					  }
					  if(serverMsg.skipped){
						  System.out.println("Your turn was skipped.");
					  }
					  if(serverMsg.chooseSuit){
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
			Listener getServerMsg2 = new Listener() { 			//Wait for a response from the server
				public void received (Connection connection, Object object) { 
				
				if(object instanceof GameTable){ 
					 GameTable updatedGame = (GameTable) object;
					 currentGame = updatedGame;
				 
					 //Use UI object to display updated GameTable, currentGame
				}
				if(object instanceof Hand){
					  
					  Hand newHand = (Hand) object;
					  System.out.println("Client2: Received hand. " + newHand.cards.size());
					  for(Card eachCard : newHand.cards)
						  myInfo2.addCardtoHand(eachCard);
					  
				}
				if(object instanceof Card){
					  Card newCard = (Card) object;
					  myInfo2.addCardtoHand(newCard);
				}
				if(object instanceof Messages){
					  connection.setTimeout(0);		//Removes timeout on connection to allow player time to make a choice.
					  Messages serverMsg = (Messages) object;
					  
					  
					  
					  if(serverMsg.invalidMove || serverMsg.playCard){		//If either a move needs to be made first or again due to previous error

						  if(serverMsg.invalidMove)	//Print out the reason for the invalid move and prompt to try again
							  System.out.println(serverMsg.invalidMessage + " Please try again.");
						  
						  ArrayList<Integer> cardCounts = currentGame.getCardCounts();
						  for(int j =0; j< cardCounts.size(); j++){
							  if(j != myInfo2.getID())
								  System.out.println("Player " + j + " has " + cardCounts.get(j) + " cards.");
						  }
						  
						  System.out.println("Game Direction: " + (currentGame.getPlayDirection() ? "Clockwise." : "Counterclockwise."));
						  System.out.println("Face Card: " + currentGame.getCardinPlay().getValue() + " " + currentGame.getCardinPlay().getSuit());
						  System.out.println("Player " + myInfo2.getID() + " Hand:");
						  for(int j = 0; j < myInfo2.getHand().cards.size(); j++){
							  Card eachCard = myInfo2.getHand().cards.get(j);
							  System.out.println(j + ". " + eachCard.getValue() + " " + eachCard.getSuit());
						  }
						  
						  //Use UI object to prompt the user to make a move choice
						  System.out.println("Client2: Please choose a card");
						  boolean invalid;
						  int choice = -1;
						  
						  do{
							  invalid = false;
							  try{
									  choice = in.nextInt();
									  if(choice < 0 || choice >= myInfo2.getHand().cards.size())
									  {
										  invalid = true;
										  System.out.println("Client2: Please choose from your hand");
									  }
							  }catch(InputMismatchException e){
								  System.out.println("Client2: Please choose from your hand");
								  invalid = true;
							  }
							  
							  in.nextLine();	//Clears newline character
						  }while(invalid);
						  
					
						  Card chosenCard = myInfo2.getHand().cards.get(choice);
						  myInfo2.setCardPlayed(chosenCard);
						 
						  //Send the chosen card object back to the server
						  
						  connection.sendTCP(chosenCard);
					  }
					  if(serverMsg.validMove){
	            		  myInfo2.removeCardfromHand(myInfo2.getCardPlayed());		//Remove the card played from hand			            		  
	            	  }
					  if(serverMsg.startGame){
						  //Use UI object to display the starting setup of the game (player's hand)
						  myInfo2.setID(serverMsg.idNumber);
						  System.out.println("Client2: Starting game");
					  }
					  if(serverMsg.endGame){
						  endProgram = true;
					  	  System.out.println("Game Over. Client closing.");
					  }
					  if(serverMsg.skipped){
						  System.out.println("Your turn was skipped.");
					  }
					  if(serverMsg.chooseSuit){
						  System.out.println("Client2: Please choose a suit.");
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
										  System.out.println("Client2: Please choose a suit");
									  }
							  }catch(InputMismatchException e){
								  System.out.println("Client2: Please choose a suit");
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
						  
						  Card suitChange = myInfo2.getCardPlayed();		//Change card played in own hand to reflect chosen suit change
						  suitChange.setSuit(suitChoice);
						  myInfo2.setCardPlayed(suitChange);
						  
						  connection.sendTCP(suitChoice);		//Send the suit change choice to the server	  
						  
					  }
					  if(serverMsg.drawCards){
						  System.out.println(serverMsg.drawReason);
					  }
					}
				} 
			};
			client.addListener(getServerMsg);
			client2.addListener(getServerMsg2);
			////////////////////////////////////
			
			
			
			
			for(int j=0; j < clientConnections.size(); j++){
				clientConnections.get(j).sendTCP(serverController.getPlayerHand(j));	//Send each client their player info
				clientConnections.get(j).sendTCP(serverController.getGameTable());
				
				startGameMsg.idNumber = j;
				clientConnections.get(j).sendTCP(startGameMsg);
			}
			
			System.out.println("Server: Game started");
			
			Messages validMsg = new Messages();
			validMsg.validMove = true;
			
			Listener getPlayerCard = new Listener() { 
		        public void received (Connection connection, Object object) {
		        	
		              if (object instanceof Card) { 
		            	 System.out.println("Server: Received Card");
		                 Card playedCard = (Card)object;
		                 
		                 if(serverController.checkPlayerCard(playedCard)){	//Card played was valid
		                	 clientConnections.get(serverController.currentPlayerIndex()).sendTCP(validMsg); 
		                 }
		                 else{	//Card was invalid              	 
		                	 Messages invMsg = new Messages();
		                	 invMsg.invalidMove = true;
		                	 clientConnections.get(serverController.currentPlayerIndex()).sendTCP(invMsg);
		                 }
		                 
		              }
		              
		              if(object instanceof Card.Suits){
		            	  Suits choice = (Suits) object;
		            	  serverController.setCrazy8(choice);
		              }
		           } 
		        };
		    
			
		      //Create message to send to client
			    Messages playerMsg;
				
				server.addListener(getPlayerCard);
				while(!endProgram){
					server.sendToAllTCP(serverController.getGameTable());		//Send updated game table to all clients
					
					playerMsg = serverController.playerTurn();
					
					if(playerMsg.skipped){
						clientConnections.get(serverController.currentPlayerIndex()).sendTCP(playerMsg);
					}
					else if(playerMsg.chooseSuit || playerMsg.playCard){
						clientConnections.get(serverController.currentPlayerIndex()).sendTCP(playerMsg);		//Send the message to play a card to the client
						//System.out.println("Server: Choose msg sent to Client " + serverController.currentPlayerIndex());
						
						clientConnections.get(serverController.currentPlayerIndex()).setTimeout(0);	//Removes timeout to allow player time to make choice
						while(true){ 
							//Holds the program until the server receives a valid move from the current player 
							if(serverController.validMove())
								break;
							else
								try {
									TimeUnit.SECONDS.sleep(1);		//Gives the listener a chance to update the player's valid move flag
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
						}
						
						
						
					}
					else if(playerMsg.drawCards){
						clientConnections.get(serverController.currentPlayerIndex()).sendTCP(playerMsg);
						clientConnections.get(serverController.currentPlayerIndex()).sendTCP(serverController.playerDraw());
					}

						
					if(serverController.isGameOver()){
						endProgram = true;
						System.out.println("Game Over. Server closing.");
						
						Messages endMessage = new Messages();
						endMessage.endGame = true;
						server.sendToAllTCP(endMessage);
					}
					
					serverController.nextPlayer();
								
				}
			
			server.stop();
			in.close();//Only for client testing code
			client.stop();//Only for client testing code
			client2.stop();
		}
}
