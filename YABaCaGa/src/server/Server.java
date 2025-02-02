package server;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingescape.*;
import callbacks.*;
import util.Blobizer;
import yabacaga.model.Bet;
import yabacaga.model.Card;
import yabacaga.model.GameMaster;
import yabacaga.model.GameMaster.State;
import yabacaga.model.Player;

/**
 * Le serveur, gère le modèle GameMaster et communique avec les clients et le whiteboard (possède l'agent Ingescape).
 * 
 * @author Mattéo Camin
 * @author Sylvan Courtiol
 */
public class Server implements AgentEventListener, WebSocketEventListener, PropertyChangeListener {

	private static Logger _logger = LoggerFactory.getLogger(Server.class);

	/**
	 * Modèle du jeu
	 */
	private GameMaster model;
	
	/**
	 * Joueurs et leurs agents
	 */
	private Map<Integer, String> playersAgentName = new HashMap<Integer, String>();
	
	/**
	 * Agent du serveur
	 */
	private Agent agent;

	public Server(GameMaster model) {
		this.model = model;
		model.addPropertyChangeListener(this);

	}

	/*
	 * Contrôleur d'Event reçu du modèle pour savoir comment mettre à jour le
	 * Whiteboard
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case "player": {
			Player newPlayer = (Player) evt.getNewValue();
			agent.outputSetString("chatMessage", newPlayer.getName() + " entered the game.");
			updateWhiteboard();
			break;
		}
		case "game": {
			Player firstPlayer = (Player) evt.getNewValue();
			agent.outputSetString("chatMessage",
					firstPlayer.getName() + " is the first player. Turn : " + this.model.getTurn());
			break;
		}
		case "bet": {
			Bet bet = (Bet) evt.getNewValue();
			Player player = this.model.getPlayers().get(bet.getPlayerId());
			Card card = player.getDeck().get(bet.getCardId());
			agent.outputSetString("chatMessage", player.getName() + " bet " + bet.getRunes() + " runes with card "
					+ card.getName() + (bet.isRage() ? ". He's enraged !" : ".") + ". Turn : " + this.model.getTurn());

			updateWhiteboard();
			break;
		}
		case "battle": {
			int code = (int) evt.getNewValue();
			if (code != GameMaster.BATTLE_TIE) {
				Player winner = this.model.getPlayers().get(code);
				agent.outputSetString("chatMessage",
						winner.getName() + " won the duel of turn : " + this.model.getTurn());
			} else {
				agent.outputSetString("chatMessage", "The duel of turn " + this.model.getTurn() + " is a tie !");
			}
			break;
		}
		case "winner": {
			int code = (int) evt.getNewValue();
			if (code == GameMaster.GAME_TIE) {
				agent.outputSetString("chatMessage", "It's a tie.");
			} else {
				Player player = this.model.getPlayers().get(code);
				agent.outputSetString("chatMessage", player.getName() + " won the match.");
			}
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + evt.getPropertyName());
		}
	}

	/**
	 * MAJ du Whiteboard en affichant les joueurs et leurs cartes.
	 */
	public void updateWhiteboard() {
		for (Player player : model.getPlayers().values()) {
			agent.outputSetString("chatMessage",
					player.getName() + " Runes: " + player.getRunes() + " Health : " + player.getHealthPoints());
			String deck = "";
			for (Card card : player.getDeck()) {
				deck += card.getName() + " Power: " + card.getPower() + " Damage : " + card.getDamage() + " Skills : "
						+ card.getSkill().toString() + "\n";
			}
			agent.outputSetString("chatMessage", deck);
		}
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	@Override
	public void handleAgentEvent(Agent agent, AgentEvent event, String uuid, String name, Object eventData) {
		_logger.debug("**received agent event for {} ({}): {} with data {}", name, uuid, event, eventData);

		if (playersAgentName.containsValue(name) && event == AgentEvent.IGS_AGENT_EXITED) {
			for (String s : playersAgentName.values()) {
				if (!s.equals(name)) {
					List<Object> args = new ArrayList<Object>();
					args.add(true);
					this.agent.serviceCall(s, "opponentLeft", args, "");
				}
			}
			this.model.finishGame();
			this.playersAgentName.clear();
		}
	}

	@Override
	public void handleWebSocketEvent(WebSocketEvent event, Throwable t) {
		if (t != null) {
			_logger.error("**received web socket event {} with exception {}", event, t.toString());
		} else {
			_logger.debug("**received web socket event {}", event);
		}
	}

	/**
	 * Un joueur veut rentrer dans la partie. On envoie au joueur le résultat de sa
	 * tentative.
	 * 
	 * @param agent  l'agent du serveur
	 * @param caller le nom de l'agent du joueur
	 * @param player le joueur
	 */
	public void enterPlayer(Agent agent, String caller, Player player) {
		int newId = model.enterPlayer(player);
		List<Object> args = new ArrayList<>();
		args.add(newId);
		if (newId > 0) {
			playersAgentName.put(newId, caller);
		}
		agent.serviceCall(caller, "acceptPlayer", args, "");
		this.beginGame(agent, caller, player);
	}

	/**
	 * Un joueur a envoyé un pari. Si le pari est refusé on lui envoie le refus. Si
	 * le pari est accepté :
	 *   - Si c'est le premier pari du tour, on envoit au joueur l'acceptation du pari
	 *   et à son opposant la carte qui a été joué.
	 *   - Si c'est le deuxième pari du tour, on envoit aux deux joueurs le résultat du duel.
	 *   - Si le match a été fini avec ce pari : on envoit au deux joueurs le résultat de la partie.
	 * 
	 * @param agent agent du serveur
	 * @param caller nom de l'agent du joueur pariant
	 * @param playerId id du joueur
	 * @param cardId id de la carte du joueur
	 * @param runes nombre de runes parié
	 * @param rage si le joueur a utilisé une rage
	 */
	public void receiveBet(Agent agent, String caller, int playerId, int cardId, int runes, boolean rage) {
		if (playersAgentName.containsKey(playerId) && cardId >= 0 && cardId < GameMaster.DECK_SIZE) {
			int returnCode = model.receiveBet(playerId, cardId, runes, rage);
			if (returnCode != GameMaster.NOT_A_PLAYER_BET_ERROR && returnCode != GameMaster.NOT_PLAYER_TURN_ERROR
					&& returnCode != GameMaster.NOT_A_CORRECT_BET_ERROR) {
				List<Object> args = new ArrayList<>();
				if (this.model.getState() == State.WAITING_SECOND_BET) {
					args.add(returnCode);
					agent.serviceCall(caller, "acceptBet", args, "");

					for (Entry<Integer, String> e : playersAgentName.entrySet()) {
						args.clear();
						if (e.getKey() != playerId) {
							args.add(cardId);
							agent.serviceCall(e.getValue(), "receiveOpponentBet", args, "");
						}
					}
					args.clear();
				} else {
					for (Entry<Integer, String> e : playersAgentName.entrySet()) {
						args.add(returnCode);
						Object[] playerList = model.getPlayers().values().toArray();
						try {
							args.add(Blobizer.toString(playerList));
							agent.serviceCall(e.getValue(), "receiveDuelResult", args, "");
							if (this.model.getState() == State.MATCH_OVER) {
								args.clear();
								args.add(this.model.getWinningPlayer());
								agent.serviceCall(e.getValue(), "receiveGameResult", args, "");
							}
							args.clear();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					if (this.model.getState() == State.MATCH_OVER) {
						this.model.finishGame();
						this.playersAgentName.clear();
					}
				}
			} else {
				List<Object> args = new ArrayList<>();
				args.add(returnCode);
				agent.serviceCall(caller, "acceptBet", args, "");
			}
		}

	}

	/**
	 * Si le nombre de joueurs et les joueurs sont correct (dans la partie et avec un nom d'agent connu),
	 * alors on les fait entrer dans le jeu. On envoit à chaque joueur toutes les infos de la partie :
	 * -  Les autres joueurs.
	 * - S'ils seront les premier à jouer.
	 * 
	 * 
	 * @param agent agent du serveur
	 * @param caller nom de l'agent du joueur
	 * @param player le joueur
	 */
	private void beginGame(Agent agent, String caller, Player player) {
		if (model.getPlayers().size() == 2 && model.getPlayers().keySet().equals(playersAgentName.keySet())
				&& model.getPlayers().containsValue(player)) {
			try {
				int firstPlayer = model.beginGame();
				for (Entry<Integer, String> e : playersAgentName.entrySet()) {
					List<Object> args = new ArrayList<>();
					args.add(e.getKey() == firstPlayer);
					Object[] playerList = model.getPlayers().values().toArray();
					args.add(Blobizer.toString(playerList));
					agent.serviceCall(e.getValue(), "receiveGameInfo", args, "");
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
	}

	public static void main(String[] args) throws InterruptedException {

		_logger.info("Start Java app 'Server'");
		_logger.info("is DEBUG enabled ? {}", _logger.isDebugEnabled());

		Global globalContext = new Global("ws://127.0.0.1:8080");

		GameMaster model = new GameMaster();
		Server Server = new Server(model);

		ServerServiceCallback serverCB = new ServerServiceCallback(Server);

		globalContext.observeWebSocketEvents(Server);

		Agent agent = globalContext.agentCreate("YABaCaGaServer");
		agent.observeAgentEvents(Server);

		agent.definition.setClass("ServerYABaCaGa");

		agent.serviceInit("enterPlayer", serverCB);
		agent.serviceArgAdd("enterPlayer", "player", IopType.IGS_DATA_T);

		agent.serviceInit("receiveBet", serverCB);
		agent.serviceArgAdd("receiveBet", "playerId", IopType.IGS_INTEGER_T);
		agent.serviceArgAdd("receiveBet", "cardId", IopType.IGS_INTEGER_T);
		agent.serviceArgAdd("receiveBet", "runes", IopType.IGS_INTEGER_T);
		agent.serviceArgAdd("receiveBet", "rage", IopType.IGS_BOOL_T);

		agent.definition.outputCreate("title", IopType.IGS_STRING_T);
		agent.definition.outputCreate("chatMessage", IopType.IGS_STRING_T);
		agent.definition.outputCreate("clear", IopType.IGS_IMPULSION_T);
		agent.definition.outputCreate("ui_command", IopType.IGS_STRING_T);
		agent.start();

		Server.setAgent(agent);
		Thread.sleep(2000);
		agent.outputSetString("title", "YABaCaGa !");
		agent.outputSetString("chatMessage", "Server connected.");

		System.out.println("Press Enter to stop the agent");
		Scanner scanner = new Scanner(System.in);
		try {
			scanner.nextLine();
		} catch (IllegalStateException | NoSuchElementException e) {
			// System.in has been closed
			System.out.println("System.in was closed; exiting");
		}
		scanner.close();
		agent.stop();
	}

}
