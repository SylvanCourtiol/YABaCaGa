package yabacaga.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Le modèle du jeu, gère l'évolution correcte de l'état du jeu
 * 
 * @author Mattéo Camin
 * @author Sylvan Courtiol
 */
public class GameMaster {

	public enum State {
		WAITING_PLAYERS, WAITING_FIRST_BET, WAITING_SECOND_BET, MATCH_OVER
	}

	public static final int DEFAULT_RUNES = 20;
	public static final int DEFAULT_HEALTH_POINTS = 20;

	public static final int MAX_DECK_COST = 20;
	public static final int DECK_SIZE = 3;

	public static final int INCORRECT_DECK_ERROR = -20;
	public static final int TOO_MANY_PLAYER_ERROR = -21;
	public static final int ALREADY_ENTERED_ERROR = -22;

	public static final int NOT_A_PLAYER_BET_ERROR = -1;
	public static final int NOT_PLAYER_TURN_ERROR = -2;
	public static final int NOT_A_CORRECT_BET_ERROR = -3;

	public static final int BATTLE_TIE = -333;
	public static final int GAME_TIE = -999;

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
	 * Un joueur essaye d'entrer dans la partie. Si le joueur est accepté, un id
	 * leur sera attribué. Un joueur est accepté si : la partie n'est pas pleine, le
	 * joueur n'est pas déjà dedans, son deck est correct.
	 * 
	 * @param player Joueur souhaitant entrer.
	 * @return int : si le code est positif c'est un Id, sinon c'est un code
	 *         d'erreur
	 */
	public int enterPlayer(Player player) {
		int returnCode = TOO_MANY_PLAYER_ERROR; // -20 : deck too expensive, -2 : player already entered (network dupe),
												// -1 :
		// too many players/game started
		if (this.state == State.WAITING_PLAYERS && this.players.size() < 2) {
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
					returnCode = INCORRECT_DECK_ERROR;
				}
			} else {
				returnCode = ALREADY_ENTERED_ERROR;
			}
		}
		return returnCode;
	}

	/**
	 * Démarre le jeu s'il y a assez de joueur.
	 * 
	 * @return int : l'id du premier joueur ou -1 si le jeu ne peut pas commencer
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

	/**
	 * Recevoir un pari. Si le pari est correct (joueur dans le jeu, carte dans le
	 * deck et pas déjà joué, nombre de runes <= à celles possédées par le joueur)
	 * alors le pari est accepté est enregistré. Si un premier pari pour ce tour
	 * était déjà enregistré alors on fait fait les affrontements. On retourne le
	 * résultat du pari : 0 si c'est un premier pari accepté, l'id du gagnant de
	 * l'affrontement si c'est un deuxième pari accepté et un code erreur sinon.
	 * 
	 * @param playerId l'id du joueur pariant
	 * @param cardId   l'id de la carte dans le deck du pariant (entre 0 et 3
	 *                 exclus)
	 * @param runes    le nombre de runes du pari
	 * @param rage     si le joueur a utilisé la rage
	 * @return int : code d'erreur, 0 pour un premier pari de tour accepté et le
	 *         résultat du duel sinon
	 */
	public int receiveBet(int playerId, int cardId, int runes, boolean rage) {
		int returnCode = NOT_A_PLAYER_BET_ERROR;
		if (this.players.containsKey(playerId)) {
			if ((this.state == State.WAITING_FIRST_BET && playerId == this.firstPlayer)
					|| (this.state == State.WAITING_SECOND_BET && playerId != this.firstPlayer && firstBet != null)) {
				Bet bet = new Bet(playerId, cardId, runes, rage);
				Player p = this.players.get(playerId);
				if (bet.getCost() <= this.players.get(playerId).getRunes()
						&& !this.playedCard.contains(p.getDeck().get(cardId))) {
					returnCode = 0;
					p.setRunes(p.getRunes() - bet.getCost());
					p.play(p.getDeck().get(cardId));
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
						returnCode = turnWinner;
						if (winner == -1) {
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
					returnCode = NOT_A_CORRECT_BET_ERROR;
				}
			} else {
				returnCode = NOT_PLAYER_TURN_ERROR;
			}
		}

		return returnCode;

	}

	/**
	 * Fini un partie et réinitialise l'état du jeu : aucun joueurs, aucun pari.
	 * Retourne l'id d'un éventuel joueur qui aurait gagné.
	 * 
	 * @return int : id du joueur gagnant ou -1.
	 */
	public int finishGame() {
		int winner = this.winningPlayer;
		this.winningPlayer = -1;
		this.turn = 1;
		this.state = State.WAITING_PLAYERS;
		this.players.clear();
		this.playedCard.clear();
		this.firstBet = null;
		this.firstPlayer = -1;

		return winner;
	}

	/**
	 * Vérifie si la partie est finie : une partie est finie si un des joueurs n'a
	 * plus de vie ou qu'on a dépassé le tour 3. On retourne l'id du gagnant, un
	 * code en cas d'égalité ou -1 si ce n'est pas fini.
	 * 
	 * @param secondPlayerId id du deuxième joueur (dans le sens de l'ordre de jeu)
	 * @return int : id du gagnant, code d'égalité ou -1.
	 */
	private int checkBattleState(int secondPlayerId) {
		int returnCode = -1;
		Player firstPlayer = this.players.get(this.firstPlayer);
		Player secondPlayer = this.players.get(secondPlayerId);
		if (this.turn > DECK_SIZE || secondPlayer.getHealthPoints() == 0 || firstPlayer.getHealthPoints() == 0) {
			if (firstPlayer.getHealthPoints() != secondPlayer.getHealthPoints()) {
				returnCode = firstPlayer.getHealthPoints() > secondPlayer.getHealthPoints() ? firstPlayer.getId()
						: secondPlayer.getId();
			} else {
				returnCode = GAME_TIE;
			}
			support.firePropertyChange("winner", null, returnCode);
			this.state = State.MATCH_OVER;
			this.winningPlayer = returnCode;
		}
		return returnCode;
	}

	/**
	 * Réalise l'affrontement entre 2 paris. On calcule d'abord l'attaque de chaque
	 * pari en fonction du power de la carte parié, des runes et des skills de la
	 * carte. Si un joueur a une attaque plus grande, on calcule les damages du pari
	 * à l'aide des damages de la carte, de la rage et des effets de la carte. Puis
	 * on applique ces damages aux points de vie du perdant. Enfin on applique les
	 * dernier effets possibles : - revenge damage : dégâts infligés au gagnant -
	 * rune payback : récupération de rune - rune poison : suppression des runes du
	 * gagnant On retourne l'id du gagnant ou un code d'égalité.
	 * 
	 * @param secondBet      pari du deuxième joueur du tour
	 * @param secondPlayerId id du deuxième joueur du tour
	 * @return int : id du gagnant ou code d'égalité
	 */
	private int battle(Bet secondBet, int secondPlayerId) {
		int returnCode = BATTLE_TIE;

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

	/**
	 * Calcul l'attaque d'un pari avec un skill, un nombre de runes et une stat de
	 * power.
	 * 
	 * @param runes     runes parié
	 * @param skill     le skill
	 * @param basePower la stat de power
	 * @return int : l'attaque
	 */
	private int computeAttack(int runes, Skill skill, int basePower) {
		float totalPower = (basePower + skill.getPowerBonus()) * skill.getPowerModifier();
		return (int) Math.floor((totalPower * (runes + 1) + skill.getAttackBonus()) * skill.getAttackModifier());
	}

	/**
	 * Calcul des damages d'un pari avec la rage, un skill et une stat de damage.
	 * 
	 * @param rage       savoir si on applique le bonus de rage
	 * @param skill      le skill à appliquer
	 * @param baseDamage la state de damage
	 * @return int : les damages totaux
	 */
	private int computeDamage(boolean rage, Skill skill, int baseDamage) {
		return (int) Math
				.floor((baseDamage + skill.getDamageBonus() + (rage ? Bet.RAGE_BONUS : 0)) * skill.getDamageModifier());
	}

	/**
	 * Vérifie qu'un deck d'un joueur est correct : coût inférieur ou égal au coût
	 * total autorisé, et nombre de carte correct.
	 * 
	 * @param player : Joueur dont il faut vérifier le deck
	 * @return boolean : true si correct, false sinon
	 */
	private boolean isDeckOk(Player player) {
		int deckCost = 0;

		for (Card card : player.getDeck()) {
			deckCost += card.getCost();
		}

		return deckCost <= MAX_DECK_COST && player.getDeck().size() == DECK_SIZE;
	}

}
