package yabacaga.model;

public class Bet {

  public static final int RAGE_COST = 3;
	public static final int RAGE_BONUS = 2;
	
	private int playerId;
	private int cardId;
	private int runes;
	private boolean rage;
	
	public Bet(int playerId, int cardId, int runes, boolean rage) {
		super();
		this.playerId = playerId;
		this.cardId = cardId;
		this.runes = runes;
		this.rage = rage;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public int getCardId() {
		return cardId;
	}

	public void setCardId(int cardId) {
		this.cardId = cardId;
	}

	public int getRunes() {
		return runes;
	}

	public void setRunes(int runes) {
		this.runes = runes;
	}

	public boolean isRage() {
		return rage;
	}

	public void setRage(boolean rage) {
		this.rage = rage;
	}
	
	public int getCost() {
		return this.runes + (this.rage ? RAGE_COST : 0);
	}

}
