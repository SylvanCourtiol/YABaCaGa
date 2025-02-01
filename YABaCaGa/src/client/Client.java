package client;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ingescape.*;
import callbacks.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import util.Blobizer;
import yabacaga.hmi.ArenaController;
import yabacaga.hmi.EditorController;
import yabacaga.model.Bet;
import yabacaga.model.GameMaster;
import yabacaga.model.Player;

/**
 * Le client, possède l'agent pour communiquer avec le serveur, ainsi que les
 * contrôleurs des différentes IHM. Le client est un singleton
 * 
 * @author Mattéo Camin
 * @author Sylvan Courtiol
 */
public class Client implements AgentEventListener, WebSocketEventListener {

	private static Logger _logger = LoggerFactory.getLogger(Client.class);

	/**
	 * Nom de l'agent du serveur
	 */
	private static final String SERVER_AGENT_NAME = "YABaCaGaServer";

	/**
	 * Instance du singleton CLient
	 */
	private static Client CLIENT_INSTANCE = null;

	/**
	 * Agent du client
	 */
	private Agent agent;

	/**
	 * Fenêtre JavaFX
	 */
	private Stage primaryStage;

	/**
	 * Modèle du joueur du client
	 */
	private Player player = null;

	/**
	 * Contrôleur de la fenêtre d'édition
	 */
	private EditorController editor = null;

	/**
	 * Contrôleur de la fenêtre d'arène
	 */
	private ArenaController arena = null;

	/**
	 * Modèle du joueur opposant
	 */
	private Player opponent = null;

	/**
	 * Si le joueur du client est le premier dans l'ordre de jeu
	 */
	private boolean firstPlayer = false;

	/**
	 * Si le serveur est up
	 */
	private boolean serverOk = false;

	/**
	 * Créer un Client, et démarre son agent. Un client est un singleton.
	 * 
	 * @param port         sur lequel démarrer l'agent
	 * @param name         nom de l'agent
	 * @param primaryStage stage pour l'affichage JavaFX
	 */
	private Client(String port, String name, Stage primaryStage) {
		this.primaryStage = primaryStage;
		_logger.info("Start Java app 'Client'");
		_logger.info("is DEBUG enabled ? {}", _logger.isDebugEnabled());

		Global globalContext = new Global("ws://127.0.0.1:" + port);
		ClientServiceCallback clientCB = new ClientServiceCallback(this);

		globalContext.observeWebSocketEvents(this);

		Agent agent = globalContext.agentCreate("Player_" + name);
		agent.observeAgentEvents(this);
		this.agent = agent;

		agent.definition.setClass("ClientYABaCaGa");

		agent.serviceInit("acceptPlayer", clientCB);
		agent.serviceArgAdd("acceptPlayer", "playerID", IopType.IGS_INTEGER_T);

		agent.serviceInit("receiveGameInfo", clientCB);
		agent.serviceArgAdd("receiveGameInfo", "firstPlayer", IopType.IGS_BOOL_T);
		agent.serviceArgAdd("receiveGameInfo", "players", IopType.IGS_DATA_T);

		agent.serviceInit("acceptBet", clientCB);
		agent.serviceArgAdd("acceptBet", "returnCode", IopType.IGS_INTEGER_T);

		agent.serviceInit("receiveOpponentBet", clientCB);
		agent.serviceArgAdd("receiveOpponentBet", "runes", IopType.IGS_INTEGER_T);

		agent.serviceInit("receiveDuelResult", clientCB);
		agent.serviceArgAdd("receiveDuelResult", "returnCode", IopType.IGS_INTEGER_T);
		agent.serviceArgAdd("receiveDuelResult", "players", IopType.IGS_DATA_T);

		agent.serviceInit("receiveGameResult", clientCB);
		agent.serviceArgAdd("receiveGameResult", "winner", IopType.IGS_DATA_T);

		agent.serviceInit("opponentLeft", clientCB);
		agent.serviceArgAdd("opponentLeft", "hasLeft", IopType.IGS_BOOL_T);

		agent.start();

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
	 * On affiche un dialogue quand le serveur est down ou up.
	 */
	@Override
	public void handleAgentEvent(Agent agent, AgentEvent event, String uuid, String name, Object eventData) {
		_logger.debug("**received agent event for {} ({}): {} with data {}", name, uuid, event, eventData);
		if (name.equals(Client.SERVER_AGENT_NAME) && event == AgentEvent.IGS_AGENT_ENTERED) {
			this.serverOk = true;
			this.openDialog("Server Up");
		} else if (name.equals(Client.SERVER_AGENT_NAME) && event == AgentEvent.IGS_AGENT_EXITED) {
			this.serverOk = false;
			this.openDialog("Server Down");
		}
	}

	public Agent getAgent() {
		return this.agent;
	}

	public Player getPlayer() {
		return this.player;
	}

	public Player getOpponent() {
		return this.opponent;
	}

	public boolean getFirstPlayer() {
		return this.firstPlayer;
	}

	public void setArena(ArenaController arena) {
		this.arena = arena;
	}

	/**
	 * Le joueur veut entrer dans le jeu. On envoie alors les infos du joueur au
	 * serveur et on bloque les intéractions de l'éditeur. Si le serveur n'est pas
	 * dispo on affiche un message.
	 * 
	 * @param player Infos du joueur à envoyer
	 * @param editor Le contrôleur de l'éditeur
	 */
	public void enterPlayer(Player player, EditorController editor) {
		if (this.serverOk) {
			List<Object> args = new ArrayList<Object>();
			try {
				args.add(Blobizer.toString(player));
				this.player = player;
				this.editor = editor;
				editor.lockOptions();
				this.agent.serviceCall(SERVER_AGENT_NAME, "enterPlayer", args, "");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			this.openDialog("Server Down");
		}
	}

	/**
	 * Recoit une acceptation de la tentative d'entrer dans une partie. Si le joueur
	 * est accepté il reçoit un id. Sinon un popup apparaît indiquant la cause du
	 * rejet.
	 * 
	 * @param newId
	 */
	public void acceptPlayer(int newId) {
		if (this.editor != null) {
			if (newId > 0) {
				this.player.setId(newId);
				openDialog("Your request to play has been accepted ! Waiting for other player...");
			} else {
				String cause = newId == GameMaster.INCORRECT_DECK_ERROR ? "Deck too expensive"
						: newId == GameMaster.ALREADY_ENTERED_ERROR ? "You entered the game already"
								: newId == GameMaster.TOO_MANY_PLAYER_ERROR ? "Too many players already in"
										: "Unknown cause";
				this.editor.unlockOptions();
				openDialog("Your request has been denied ! Cause : " + cause);
			}
		}
	}

	/**
	 * Reçoit les infos de la partie quand la partie commence. Les infos sont : - le
	 * joueur, l'opposant et si le joueur est celui qui commence. On lance alors
	 * l'interface de combat (arena).
	 * 
	 * @param firstPlayer
	 * @param player
	 * @param opponent
	 */
	public void receiveGameInfo(Boolean firstPlayer, Player player, Player opponent) {
		this.player = player;
		this.opponent = opponent;
		this.firstPlayer = firstPlayer;
		Platform.runLater(() -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/yabacaga/hmi/arena.fxml"));
				BorderPane root = (BorderPane) loader.load();
				Scene scene = new Scene(root, 1083, 1078);
				scene.getStylesheets().add(getClass().getResource("/yabacaga/hmi/application.css").toExternalForm());
				this.primaryStage.setTitle("YABaCaGa");
				this.primaryStage.setScene(scene);
				this.primaryStage.show();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * On envoie un pari fait par le joueur au serveur. On bloque les interactions.
	 * 
	 * @param bet   le pari à envoyer
	 * @param arena le controlleur de l'interface de jeu
	 */
	public void sendBet(Bet bet, ArenaController arena) {
		if (this.serverOk) {
			this.arena = arena;
			this.arena.lockSend();
			List<Object> args = new ArrayList<>();
			args.add(bet.getPlayerId());
			args.add(bet.getCardId());
			args.add(bet.getRunes());
			args.add(bet.isRage());

			this.agent.serviceCall(SERVER_AGENT_NAME, "receiveBet", args, "");
		}
	}

	/**
	 * Information reçu de la part du serveur sur notre pari. Si le code est
	 * supérieur à 0, le pari est valide, on peut alors l'afficher. Sinon, on
	 * affiche une popup avec la raison du rejet.
	 * 
	 * @param returnCode code reçu du serveur
	 */
	public void acceptBet(int returnCode) {
		if (this.arena != null) {
			if (returnCode >= 0) {
				this.arena.acceptBet();
			}
		} else {
			String cause = returnCode == GameMaster.NOT_PLAYER_TURN_ERROR ? "This is not your turn"
					: returnCode == GameMaster.NOT_A_PLAYER_BET_ERROR
							? "You're not part of the game (how did you get here ?)"
							: returnCode == GameMaster.NOT_A_CORRECT_BET_ERROR ? "This bet is not correct"
									: "Unknown cause";
			openDialog("Your bet has been denied ! Cause : " + cause);
		}
	}

	/**
	 * On reçoit du serveur une carte pariée par un opposant. On met-à-jour
	 * l'interface pour la prendre en compte
	 * 
	 * @param cardId carte jouée
	 */
	public void receiveOpponentBet(int cardId) {
		if (this.arena != null && this.opponent != null && this.player != null) {
			if (cardId >= 0 && cardId < GameMaster.DECK_SIZE) {
				Player opponent = this.arena.getOpponent();
				opponent.play(opponent.getDeck().get(cardId));
				Player player = this.arena.getPlayer();
				Platform.runLater(() -> {
					this.arena.nextState(player, opponent);
				});
			}
		}
	}

	/**
	 * On reçoit les résultats d'un duel (un duel intervient quand tous les joueurs
	 * ont parié pour le tour). On affiche alors le résultat dans un dialogue.
	 * 
	 * @param result   le résultat (id du gagnant ou code d'égalité)
	 * @param player   le joueur
	 * @param opponent l'opposant
	 */
	public void receiveDuelResult(Integer result, Player player, Player opponent) {
		if (this.arena != null && this.opponent != null && this.player != null) {
			String message = result == GameMaster.BATTLE_TIE ? "This duel is a tie !"
					: result == player.getId() ? "You won this duel !"
							: result == opponent.getId() ? "Your opponent won this duel !" : "Unknown duel result";
			openDialog(message);
			Platform.runLater(() -> {
				if (this.arena != null) {
					this.arena.nextState(player, opponent);
				}
			});
		}
	}

	/**
	 * On reçoit les résultats de la partie de la part du serveur. On affiche les
	 * résultats dans dialogue puis on retourne à l'éditeur.
	 * 
	 * @param winnerId id du vainqueur
	 */
	public void receiveGameResult(int winnerId) {
		if (this.arena != null && this.opponent != null && this.player != null) {
			String message = winnerId == GameMaster.GAME_TIE ? "The game is a tie"
					: winnerId == player.getId() ? "You won the game !"
							: winnerId == opponent.getId() ? "Your opponent won this game !" : "Unknown game result";
			openDialog(message);

		}
	}

	/**
	 * On reçoit du serveur l'information que l'adversaire a quitté la partie. On
	 * affiche un dialogue et on retourne à l'éditeur.
	 * 
	 * @param opponentLeft
	 */
	public void opponentLeft(boolean opponentLeft) {
		if (this.player != null && this.opponent != null && opponentLeft) {
			this.openDialog("Opponent left the game");
			returnToEditor();
		}

	}

	/**
	 * Retourne à l'interface de l'éditeur après avoir réinitialisé les champs
	 * important.
	 */
	private void returnToEditor() {
		player = null;
		editor = null;
		arena = null;
		opponent = null;
		firstPlayer = false;
		Platform.runLater(() -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/yabacaga/hmi/editor.fxml"));
				// loader.setController(new EditorController());
				BorderPane root;
				root = (BorderPane) loader.load();
				Scene scene = new Scene(root, 1100, 680);
				scene.getStylesheets().add(getClass().getResource("/yabacaga/hmi/application.css").toExternalForm());
				primaryStage.setTitle("YABaCaGa");
				primaryStage.setScene(scene);
				primaryStage.show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	/**
	 * Ouvre un dialogue bloquant dans une fenêtre avec un message.
	 * 
	 * @param message message à afficher
	 */
	public void openDialog(String message) {
		Platform.runLater(() -> {
			final Stage dialog = new Stage();
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.initOwner(primaryStage);
			VBox dialogVbox = new VBox(20);
			dialogVbox.getChildren().add(new Text(message));
			Scene dialogScene = new Scene(dialogVbox, 500, 200);
			dialog.setScene(dialogScene);
			dialog.show();
		});
	}

	/**
	 * Permet de reçevoir l'instance de client (ou la créer si elle n'existe pas).
	 * 
	 * @param args         argument à fournir au client
	 * @param primaryStage interface JavaFX
	 * @return Client : le client
	 */
	public static Client getClient(String[] args, Stage primaryStage) {
		String port = args[0];
		String name = args[1];

		if (Client.CLIENT_INSTANCE == null) {
			Client.CLIENT_INSTANCE = new Client(port, name, primaryStage);
		}
		return Client.CLIENT_INSTANCE;
	}

	/**
	 * Permet de recevoir l'instance de client ou null s'il n'y en a pas.
	 * 
	 * @return Client : le client ou null
	 */
	public static Client getClient() {
		return Client.CLIENT_INSTANCE;
	}

}
