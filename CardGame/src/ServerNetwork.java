import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import CardGameResources.Card;
import CardGameResources.Card.Suits;
import CardGameResources.GameTable;
import CardGameResources.Hand;
import CardGameResources.Messages;

public class ServerNetwork{
	private ServerController serverController = new ServerController();
	
	private boolean endProgram = false;
	private int TCPport = 30001, UDPport = 30001;	
	
	public void main(){
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

		
		
		//Listener to get client connections
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
				
		for(int j=0; j < clientConnections.size(); j++){
			clientConnections.get(j).sendTCP(serverController.getPlayerHand(j));	//Send each client their hand
			clientConnections.get(j).sendTCP(serverController.getGameTable());		//Send each client the game table
			
			
			startGameMsg.idNumber = j;
			clientConnections.get(j).sendTCP(startGameMsg);		//Send the client the start message along with their id number
		}
		
		System.out.println("Server: Game started");
		
		Messages validMsg = new Messages();
		validMsg.validMove = true;
		
		//Listener to get choices from the clients
		Listener getPlayerCard = new Listener() { 
	        public void received (Connection connection, Object object) {
	        	if(object instanceof Card.Suits){
	        		//Gets the suit choice from user
	            	  System.out.println("New suit");
	            	  Suits choice = (Suits) object;
	            	  serverController.setCrazy8(choice);
	              }
	        	
	        	else if (object instanceof Card) { 
	        		//Get the card choice from the client and validate it
	            	 System.out.println("Server: Received Card");
	                 Card playedCard = (Card)object;
	                 
	                 //Send back either a valid or invalid message
	                 if(serverController.checkPlayerCard(playedCard)){	//Card played was valid
	                	 clientConnections.get(serverController.currentPlayerIndex()).sendTCP(validMsg); 
	                 }
	                 else{	//Card was invalid              	 
	                	 Messages invMsg = new Messages();
	                	 invMsg.invalidMove = true;
	                	 clientConnections.get(serverController.currentPlayerIndex()).sendTCP(invMsg);
	                 }
	                 
	              }
	              
	           } 
	        };
	    
		
	    //Create message to send to client
	    Messages playerMsg;
		
		server.addListener(getPlayerCard);	//Start the listener for client choices
		while(!endProgram){
			server.sendToAllTCP(serverController.getGameTable());		//Send updated game table to all clients
			
			playerMsg = serverController.playerTurn();		//Determines what the player needs to do this turn
			
			if(playerMsg.skipped){		//If the player has been skipped, send that message to the player
				clientConnections.get(serverController.currentPlayerIndex()).sendTCP(playerMsg);
			}
			else if(playerMsg.chooseSuit || playerMsg.playCard){
				clientConnections.get(serverController.currentPlayerIndex()).sendTCP(playerMsg);		//Send the message to play a card to the client
				
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
			else if(playerMsg.drawCards){		//If a player needs to draw a card for some reason
				clientConnections.get(serverController.currentPlayerIndex()).sendTCP(playerMsg);		//Send the message to the player
				clientConnections.get(serverController.currentPlayerIndex()).sendTCP(serverController.playerDraw());		//Send the hand of drawn cards to the player
				
			}

				
			if(serverController.isGameOver()){	//If the game is over, send that message to the client and end this
				endProgram = true;
				System.out.println("Game Over. Server closing.");
				
				Messages endMessage = new Messages();
				endMessage.endGame = true;
				server.sendToAllTCP(endMessage);
			}
			
			serverController.nextPlayer();		//Move to the next player
						
		}
		
		server.stop();
	}
}
