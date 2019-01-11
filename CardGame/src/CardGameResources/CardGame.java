package CardGameResources;
import java.util.ArrayList;

public abstract class CardGame {
	private int numStartCards = 5;
	private int numPlayers = 4;
	
	public int getnumStartCards(){ return numStartCards; }
	public int getnumPlayers(){ return numPlayers; }
	public abstract boolean validate(Card checkCard, Card faceCard, boolean offensive);
	public abstract boolean endCondition(Player currentPlayer);
	public abstract void updateScores(ArrayList<Player> allPlayers);
}
