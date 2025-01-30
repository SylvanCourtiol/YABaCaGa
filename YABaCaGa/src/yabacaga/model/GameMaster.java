package yabacaga.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameMaster {

	public enum State {
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
	private int turn = 0;
	private int winningPlayer = -1;

	public GameMaster() {
		this.support = new PropertyChangeSupport(this);
		this.state = State.WAITING_PLAYERS;
	}

	public Map<Integer, Player> getPlayers() {
		return this.players;
	}

	public int getFirstPlayer() {
		return this.firstPlayer;
	}

	public State getState() {
		return this.state;
	}
	
	public int getTurn() {
		return this.turn;
	}
	
	public int getWinningPlayer() {
		return this.winningPlayer;
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
					int newId = (int) random.nextInt(1, Integer.MAX_VALUE);
					while (this.players.containsKey(newId)) {
						newId = (int) random.nextInt(1, Integer.MAX_VALUE);
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
			support.firePropertyChange("game", null, this.players.get(this.firstPlayer));
			returnCode = firstPlayer;
		}
		return returnCode;
	}

	public int receiveBet(int playerId, int cardId, int runes, boolean rage) {
		int returnCode = -1;
		if (this.players.containsKey(playerId)) {
			if ((this.state == State.WAITING_FIRST_BET && playerId == this.firstPlayer)
					|| (this.state == State.WAITING_SECOND_BET && playerId != this.firstPlayer && firstBet != null)) {
				Bet bet = new Bet(playerId, cardId, runes, rage);
				Player p = this.players.get(playerId);
				if (bet.getCost() <= this.players.get(playerId).getRunes() || !this.playedCard.contains(p.getDeck().get(cardId))) {
					returnCode = 0;
					p.setRunes(p.getRunes() - bet.getCost());
					switch (this.state) {
					case State.WAITING_FIRST_BET: {
						this.firstBet = bet;
						support.firePropertyChange("bet", null, bet);
						this.state = State.WAITING_SECOND_BET;
						break;
					}
					case State.WAITING_SECOND_BET: {
						support.firePropertyChange("bet", null, bet);
						int turnWinner = battle(bet, playerId);
						support.firePropertyChange("battle", null, turnWinner);
						this.turn++;
						int winner = checkBattleState(playerId);
						if (winner == -1) {
							returnCode = turnWinner;
							this.firstPlayer = playerId;
							this.state = State.WAITING_FIRST_BET;
							this.firstBet = null;
						}
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
	
	public int finishGame() {
		this.turn = 1;
		this.state = State.WAITING_PLAYERS;
		this.players.clear();
		this.playedCard.clear();
		this.firstBet = null;
		this.firstPlayer = -1;
		
		return this.winningPlayer;
	}

	private int checkBattleState(int secondPlayerId) {
		int returnCode = -1;
		Player firstPlayer = this.players.get(this.firstPlayer);
		Player secondPlayer = this.players.get(secondPlayerId);
		if (this.turn > DECK_SIZE || secondPlayer.getHealthPoints() == 0 || firstPlayer.getHealthPoints() == 0 ) {
			if (firstPlayer.getHealthPoints() != secondPlayer.getHealthPoints()) {
				returnCode = firstPlayer.getHealthPoints() > secondPlayer.getHealthPoints() ? firstPlayer.getId() : secondPlayer.getId();
			} else {
				returnCode = -999;
			}
			support.firePropertyChange("winner", null, returnCode);
			this.state = State.MATCH_OVER;
			this.winningPlayer = returnCode;
		}
		return returnCode;
	}

	private int battle(Bet secondBet, int secondPlayerId) {
		int returnCode = -333;
		
		Player firstPlayer = this.players.get(this.firstPlayer);
		Player secondPlayer = this.players.get(secondPlayerId);
		Card firstCard = firstPlayer.getDeck().get(this.firstBet.getCardId());
		Card secondCard = secondPlayer.getDeck().get(secondBet.getCardId());
		this.playedCard.add(firstCard);
		this.playedCard.add(secondCard);
		
		Skill firstSkill = firstCard.getSkill();
		int firstCardAttack = computeAttack(this.firstBet.getRunes(), firstSkill, firstCard.getPower());
		Skill secondSkill = secondCard.getSkill();
		int secondCardAttack = computeAttack(secondBet.getRunes(), secondSkill, secondCard.getPower());
		
		if (secondCardAttack != firstCardAttack) {
			Player winningPlayer = firstCardAttack > secondCardAttack ? firstPlayer : secondPlayer;
			Player losingPlayer = firstCardAttack > secondCardAttack ? secondPlayer : firstPlayer;
			Skill winningSkill = firstCardAttack > secondCardAttack ? firstSkill : secondSkill;
			Skill losingSkill = firstCardAttack > secondCardAttack ? secondSkill : firstSkill;
			Card winningCard = firstCardAttack > secondCardAttack ? firstCard : secondCard;
			boolean winnerRage = firstCardAttack > secondCardAttack ? this.firstBet.isRage() : secondBet.isRage();
			
			int damageInflicted = computeDamage(winnerRage, winningSkill, winningCard.getDamage());
			losingPlayer.setHealthPoints(losingPlayer.getHealthPoints() - damageInflicted);
			
			winningPlayer.setHealthPoints(winningPlayer.getHealthPoints() - losingSkill.getDamageRevenge());
			
			winningPlayer.setRunes(winningPlayer.getRunes() - losingSkill.getRunePoison());
			
			returnCode = winningPlayer.getId();
		}
		firstPlayer.setRunes(firstPlayer.getRunes() + firstSkill.getRunePayback());
		secondPlayer.setRunes(secondPlayer.getRunes() + secondSkill.getRunePayback());
		return returnCode;
	}
	
	private int computeAttack(int runes, Skill skill, int basePower) {
		float totalPower = (basePower + skill.getPowerBonus()) * skill.getPowerModifier();
		return (int)Math.floor(( totalPower * runes + skill.getAttackBonus()) * skill.getAttackModifier());
	}
	
	private int computeDamage(boolean rage, Skill skill, int baseDamage) {
		return (int)Math.floor((baseDamage + skill.getDamageBonus() + (rage ? Bet.RAGE_BONUS : 0)) * skill.getDamageModifier());
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
