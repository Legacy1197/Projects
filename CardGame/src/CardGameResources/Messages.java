package CardGameResources;

public class Messages {
	//Turn prompt messages
	public boolean playCard = false;
	public boolean chooseSuit = false;
	public boolean skipped = false;
	public boolean drawCards = false;
	public String drawReason = "";
	
	
	//Game mechanics messages
	public boolean endGame = false;
	public boolean clientConnected = false;
	public boolean startGame = false;
	public int idNumber = -1;
	
	//Move validation messages
	public boolean invalidMove = false;
	public boolean validMove = false;
	public String invalidMessage = "Card must match suit and/or value.";
	
	
}
