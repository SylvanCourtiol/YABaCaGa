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

public class Client implements AgentEventListener, WebSocketEventListener {

	private static Logger _logger = LoggerFactory.getLogger(Client.class);

	private static final String SERVER_AGENT_NAME = "YABaCaGaServer";

	private static Client CLIENT_INSTANCE = null;

	private Agent agent;

	private Stage primaryStage;

	private Player player = null;

	private EditorController editor = null;
	
	private ArenaController arena = null;

	private Player opponent = null;

	private boolean firstPlayer = false;

	private boolean serverOk = false;

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

	public void acceptPlayer(int newId) {
		if (this.editor != null) {
			if (newId > 0) {
				this.player.setId(newId);
				openDialog("Your request to play has been accepted ! Waiting for other player...");
			} else {
				String cause = newId == GameMaster.TOO_EXPENSIVE_ERROR ? "Deck too expensive"
						: newId == GameMaster.ALREADY_ENTERED_ERROR ? "You entered the game already"
								: newId == GameMaster.TOO_MANY_PLAYER_ERROR ? "Too many players already in"
										: "Unknown cause";
				this.editor.unlockOptions();
				openDialog("Your request has been denied ! Cause : " + cause);
			}
		}
	}

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
	
	public void acceptBet(int returnCode) {
		if (this.arena != null) {
			if (returnCode >= 0) {
				this.arena.acceptBet();
			}
		} else {
			String cause = returnCode == GameMaster.NOT_PLAYER_TURN_ERROR ? "This is not your turn"
					: returnCode == GameMaster.NOT_A_PLAYER_BET_ERROR ? "You're not part of the game (how did you get here ?)"
							: returnCode == GameMaster.NOT_A_CORRECT_BET_ERROR ? "This bet is not correct"
									: "Unknown cause";
			openDialog("Your bet has been denied ! Cause : " + cause);
		}
	}
	
	public void receiveOpponentBet(int cardId) {
		if (this.arena != null && this.opponent != null && this.player != null) {
			System.out.println("Receive Bet ok");
			if (cardId >= 0 && cardId < GameMaster.DECK_SIZE) {
				Player opponent = this.arena.getOpponent();
				opponent.play(opponent.getDeck().get(cardId));
				Player player = this.arena.getPlayer();
				this.arena.nextState(player, opponent);
			}
		}
	}

	public void openDialog(String message) {
		Platform.runLater(() -> {
			final Stage dialog = new Stage();
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.initOwner(primaryStage);
			VBox dialogVbox = new VBox(20);
			dialogVbox.getChildren().add(new Text(message));
			Scene dialogScene = new Scene(dialogVbox, 300, 200);
			dialog.setScene(dialogScene);
			dialog.show();
		});
	}

	public static Client getClient(String[] args, Stage primaryStage) {
		String port = args[0];
		String name = args[1];

		if (Client.CLIENT_INSTANCE == null) {
			Client.CLIENT_INSTANCE = new Client(port, name, primaryStage);
		}
		return Client.CLIENT_INSTANCE;
	}

	public static Client getClient() {
		return Client.CLIENT_INSTANCE;
	}

}
