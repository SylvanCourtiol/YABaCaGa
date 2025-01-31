package yabacaga.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameMaster {

	private enum State {
		WAITING_PLAYERS, WAITING_FIRST_BET, WAITING_SECOND_BET, MATCH_OVER
	}

	public static final int DEFAULT_RUNES = 20;
	public static final int DEFAULT_HEALTH_POINTS = 20;

	public static final int MAX_DECK_COST = 20;
	public static final int DECK_SIZE = 3;

	private final PropertyChangeSupport support;
	private Map<Integer, Player> players = new HashMap<Integer, Player>();
	private List<Card> playedCard = new ArrayList<Card>();
	private int firstPlayer = -1;
	private State state = null;
	private Bet firstBet = null;
	private int turn;

	public GameMaster() {
		this.support = new PropertyChangeSupport(this);
		this.state = State.WAITING_PLAYERS;
	}

	public Map<Integer, Player> getPlayers() {
		return players;
	}

	public int getFirstPlayer() {
		return firstPlayer;
	}

	public State getState() {
		return state;
	}
	
	public int getTurn() {
		return turn;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	/**
	 * A player tries to enter the game. If the player is accepted, they will be
	 * given a new id. A player is accepted if : the game is not full or started,
	 * they are not already in, their deck is correct.
	 * 
	 * @param player : Player that want to enter
	 * @return int : if the return is positive it's the new player id, an error code
	 *         otherwise
	 */
	public int enterPlayer(Player player) {
		int returnCode = -1; // -20 : deck too expensive, -2 : player already entered (network dupe), -1 :
								// too many players/game started
		if (this.state == State.WAITING_PLAYERS || this.players.size() < 2) {
			if (!this.players.containsValue(player)) {
				if (isDeckOk(player)) {
					Random random = new Random();
					int newId = (int) random.nextInt() % Integer.MAX_VALUE;
					while (this.players.containsKey(newId)) {
						newId = (int) random.nextInt() % Integer.MAX_VALUE;
					}
					player.setId(newId);
					this.players.put(newId, player);
					support.firePropertyChange("player", null, player);
					returnCode = newId;
				} else {
					returnCode = -20;
				}
			} else {
				returnCode = -2;
			}
		}
		return returnCode;
	}

	/**
	 * Start of the game if there are enough players and no game going.
	 * 
	 * @return int : first player id or error code
	 */
	public int beginGame() {
		int returnCode = -1;
		if (this.state == State.WAITING_PLAYERS && this.players.size() == 2) {
			ArrayList<Integer> keys = new ArrayList<Integer>(this.players.keySet());
			this.firstPlayer = keys.get(new Random().nextInt(keys.size()));
			this.state = State.WAITING_FIRST_BET;
			this.turn = 1;
			support.firePropertyChange("game", this.firstPlayer, this.firstPlayer);
			returnCode = firstPlayer;
		}
		return returnCode;
	}

	public int receiveBet(int playerId, int cardId, int runes, boolean rage) {
		int returnCode = -1;
		if (this.players.containsKey(playerId)) {
			if (this.state == State.WAITING_FIRST_BET && playerId == this.firstPlayer
					|| this.state == State.WAITING_SECOND_BET && firstBet != null) {
				Bet bet = new Bet(playerId, cardId, runes, rage);
				if (bet.getCost() <= this.players.get(playerId).getRunes()) {
					returnCode = 0;
					switch (this.state) {
					case State.WAITING_FIRST_BET: {
						this.firstBet = bet;
						break;
					}
					case State.WAITING_SECOND_BET: {
						battle(bet, playerId);
						checkBattleState(playerId);
						firstPlayer = playerId;
						break;
					}
					default:
						throw new IllegalArgumentException("Unexpected value: " + this.state);
					}
				} else {
					returnCode = -3;
				}
			} else {
				returnCode = -2;
			}
		}

		return returnCode;

	}

	private int checkBattleState(int secondPlayerId) {
		int returnCode = -1;
		Player firstPlayer = this.players.get(this.firstPlayer);
		Player secondPlayer = this.players.get(secondPlayerId);
		if (this.turn == 3 || secondPlayer.getHealthPoints() == 0 || firstPlayer.getHealthPoints() == 0 ) {
			
		}
		return returnCode;
	}

	private int battle(Bet secondBet, int secondPlayerId) {
		int returnCode = -1;
		this.turn++;
		
		Player firstPlayer = this.players.get(this.firstPlayer);
		Player secondPlayer = this.players.get(secondPlayerId);
		Card firstCard = firstPlayer.getDeck().get(this.firstBet.getCardId());
		Card secondCard = secondPlayer.getDeck().get(secondBet.getCardId());
		this.playedCard.add(firstCard);
		this.playedCard.add(secondCard);
		
		Skill firstSkill = firstCard.getSkill();
		int firstCardAttack = (int)Math.floor(((firstCard.getPower() + firstSkill.getPowerBonus()) * firstSkill.getPowerModifier() + firstSkill.getAttackBonus()) * firstSkill.getAttackModifier());
		Skill secondSkill = secondCard.getSkill();
		int secondCardAttack = (int)Math.floor(((secondCard.getPower() + secondSkill.getPowerBonus()) * secondSkill.getPowerModifier() + secondSkill.getAttackBonus()) * secondSkill.getAttackModifier());
		
		if (secondCardAttack != firstCardAttack) {
			Player winningPlayer = firstCardAttack > secondCardAttack ? firstPlayer : secondPlayer;
			Player losingPlayer = firstCardAttack > secondCardAttack ? secondPlayer : firstPlayer;
			Skill winningSkill = firstCardAttack > secondCardAttack ? firstSkill : secondSkill;
			Skill losingSkill = firstCardAttack > secondCardAttack ? secondSkill : firstSkill;
			Card winningCard = firstCardAttack > secondCardAttack ? firstCard : secondCard;
			
			int damageInflicted = (int)Math.floor((winningCard.getDamage() + winningSkill.getDamageBonus()) * winningSkill.getDamageModifier());
			losingPlayer.setHealthPoints(losingPlayer.getHealthPoints() - damageInflicted);
			
			winningPlayer.setHealthPoints(winningPlayer.getHealthPoints() - losingSkill.getDamageRevenge());
			
			winningPlayer.setRunes(winningPlayer.getRunes() - losingSkill.getRunePoison());
			
			returnCode = winningPlayer.getId();
		}
		firstPlayer.setRunes(firstPlayer.getRunes() - firstBet.getCost() + firstSkill.getRunePayback());
		secondPlayer.setRunes(secondPlayer.getRunes() - secondBet.getCost() + secondSkill.getRunePayback());
		return returnCode;
	}

	/**
	 * Check if a player's deck is correct (no too many cards and not too
	 * expensive).
	 * 
	 * @param player : Player deck to check
	 * @return boolean : true if correct, false otherwise
	 */
	private boolean isDeckOk(Player player) {
		int deckCost = 0;

		for (Card card : player.getDeck()) {
			deckCost += card.getCost();
		}

		return deckCost <= MAX_DECK_COST && player.getDeck().size() == DECK_SIZE;
	}

}
