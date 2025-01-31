package yabacaga.hmi;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import client.Client;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import yabacaga.model.Bet;
import yabacaga.model.Card;
import yabacaga.model.Player;
import yabacaga.model.Skill;

public class ArenaController {
	
	private enum State {
		INIT, BET_1, WAIT_END_BET_1, BET_2, WAIT_END_BET_2, BET_3, WAIT_END_BET_3, END
	}
	
	private Player player = null;
	private Player opponent = null;
	private boolean firstPlayer = false;
	
	private State state = State.INIT;
	private State lastState = State.INIT;
	
	private Bet bet = new Bet(0, 0, 0, false);

	public void setPlayer(Player player) {
		this.player = player;
		bet.setPlayerId(player.getId());
		update();
	}

	public void setOpponent(Player opponent) {
		this.opponent = opponent;
		update();
	}
	
	public void setFirstPlayer(boolean firstPlayer) {
		this.firstPlayer = firstPlayer;
		update();
	}

	@FXML
    private Text betValueLabel;

    @FXML
    private Rectangle card1;

    @FXML
    private Rectangle card11;

    @FXML
    private Rectangle card111;

    @FXML
    private Rectangle card1111;

    @FXML
    private ComboBox<Card> cardComboBox;

    @FXML
    private Text cardO1NameLabel;

    @FXML
    private Text cardO1SkillLabel;

    @FXML
    private Text cardO2NameLabel;

    @FXML
    private Text cardO2SkillLabel;

    @FXML
    private Text cardO3NameLabel;

    @FXML
    private Text cardO3SkillLabel;

    @FXML
    private Text cardP1NameLabel;

    @FXML
    private Text cardP1SkillLabel;

    @FXML
    private Text cardP2NameLabel;

    @FXML
    private Text cardP2SkillLabel;

    @FXML
    private Text cardP3NameLabel;

    @FXML
    private Text cardP3SkillLabel;

    @FXML
    private Text damageValueO1Label;

    @FXML
    private Text damageValueO2Label;

    @FXML
    private Text damageValueO3Label;

    @FXML
    private Text damageValueP1Label;

    @FXML
    private Text damageValueP2Label;

    @FXML
    private Text damageValueP3Label;

    @FXML
    private Button decrementBetButton;

    @FXML
    private Button incrementBetButton;

    @FXML
    private Text middleTextLabel;

    @FXML
    private Text opponentHPLabel;

    @FXML
    private Text opponentNameLabel;

    @FXML
    private Text opponentRunesLabel;

    @FXML
    private Text playerHPLabel;

    @FXML
    private Text playerNameLabel;

    @FXML
    private Text playerRunesLabel;

    @FXML
    private Text powerValueO2Label;

    @FXML
    private Text powerValueO3Label;

    @FXML
    private Text powerValueP1Label;

    @FXML
    private Text powerValueP2Label;

    @FXML
    private Text powerValueP3Label;

    @FXML
    private Text powerValueO1Label;

    @FXML
    private CheckBox rageCheckbox;

    @FXML
    private Button sendBetButton;

    @FXML
    void cardSelected(ActionEvent event) {
    	int cardId = cardComboBox.getValue() != null ? player.getDeck().indexOf(cardComboBox.getValue()) : 0;
    	bet.setCardId(cardId);
    	update();
    }

    @FXML
    void decrementBet(ActionEvent event) {
    	try {
    		bet.setRunes(bet.getRunes() - 1);
    		betValueLabel.setText(Integer.toString(bet.getRunes()));
    		update();
    	} catch (IllegalArgumentException e) {
			// empty body
		}
    }

    @FXML
    void incrementBet(ActionEvent event) {
    	if (player.getRunes() - bet.getRunes() - 1 - (rageCheckbox.isSelected() ? Bet.RAGE_COST : 0) < 0) {
    		return;
    	}
    	bet.setRunes(bet.getRunes() + 1);
		betValueLabel.setText(Integer.toString(bet.getRunes()));
		update();
    }

    @FXML
    void rageSelection(ActionEvent event) {
    	if (player.getRunes() - bet.getRunes() - (rageCheckbox.isSelected() ? Bet.RAGE_COST : 0) < 0) {
    		rageCheckbox.setSelected(false);
    		return;
    	}
    	bet.setRage(rageCheckbox.isSelected());
    	update();
    }

    @FXML
    void sendBet(ActionEvent event) {
    	Bet toSend = bet;
    	state = state == State.BET_1 ? State.WAIT_END_BET_1 : state == State.BET_2 ? State.WAIT_END_BET_2 : state == State.BET_3 ? State.WAIT_END_BET_3 : state;
    	update();
    	// TODO enlever apres tests
    	ArenaController arena = this;
    	Platform.runLater(() -> {
    		try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	player.setHealthPoints(player.getHealthPoints() - 1);
	    	opponent.setRunes(opponent.getRunes() - 2);
	    	opponent.setHealthPoints(0);
	        arena.nextState(player, opponent);
    	});
    }
    
    @FXML
    private void initialize() {
    	state = State.INIT;
    	
    	this.player = Client.getClient().getPlayer();
    	this.opponent = Client.getClient().getOpponent();
    	this.firstPlayer = Client.getClient().getFirstPlayer();
    	
    	update();
    	
    	nextState(this.player, this.opponent);
    }
    
    void update() {
    	if (player == null || opponent == null) {
    		return;
    	}
    	updateCards();
    	
    	updateBet();
    	
    	// update player stats
    	playerNameLabel.setText(player.getName());
    	playerHPLabel.setText("HP: " + player.getHealthPoints());
    	int betted = bet.getRunes() + (bet.isRage() ? Bet.RAGE_COST : 0);
    	playerRunesLabel.setText(String.format("Runes: %d (bet: %d, remains: %d)", player.getRunes(), betted, player.getRunes() - betted));
    	
    	// update opponent stats
    	opponentNameLabel.setText(opponent.getName());
    	opponentHPLabel.setText("HP: " + opponent.getHealthPoints());
    	opponentRunesLabel.setText("Runes: " + opponent.getRunes());
    	
    	updateMiddleText();
    	
    	if (this.firstPlayer && (this.state == State.WAIT_END_BET_1 || this.state == State.BET_2 || this.state == State.WAIT_END_BET_3) ||
    			!this.firstPlayer && (this.state == State.BET_1 || this.state == State.WAIT_END_BET_2 || this.state == State.BET_3) || this.state == State.INIT) {
    		this.lockSend();
    	} else {
    		this.unlockSend();
    	}
    	
    	lastState = state;
    }
    
    void updateMiddleText() {
    	String text;
    	Player firstPlayer = this.firstPlayer ? this.player : this.opponent;
    	Player secondPlayer = this.firstPlayer ? this.opponent : this.player;
    	switch (state) {
    	case INIT :
    		text = "YABaCaGa will start soon!";
    		break;
    	case BET_1 : 
    		text = "Turn 1: " +  firstPlayer.getName() + " make your bet";
    		break;
    	case WAIT_END_BET_1:
    		text = "Turn 1: " +  secondPlayer.getName() + " make your bet";
    		break;
    	case BET_2:
    		text = "Turn 2: " +  secondPlayer.getName() + " make your bet";
    		break;
    	case WAIT_END_BET_2: 
    		text = "Turn 2: " +  firstPlayer.getName() + " make your bet";
    		break;
    	case BET_3:
    		text = "Turn 3: " +  firstPlayer.getName() + " make your bet";
    		break;
    	case WAIT_END_BET_3:
    		text = "Turn 3: " +  secondPlayer.getName() + " make your bet";
    	case END:
    		text = "The end! " + (player.getHealthPoints() >= opponent.getHealthPoints() ? "The victory is yours." : "No victory for you today.");
    		break;
    	default:
    		text = "Oops, not yet implemented.";
    	}
    	middleTextLabel.setText(text);
    }
    
    void updateBet() {
    	// Update when last bet is replace by a new
    	if (state != lastState && state != State.WAIT_END_BET_1 && state != State.WAIT_END_BET_2 && state != State.WAIT_END_BET_3) {
    		rageCheckbox.setSelected(false);
    		cardComboBox.setValue(null);
    		
    		bet.setCardId(0);
    		bet.setRage(false);
    		bet.setRunes(0);
    		
        	// update card combo box
        	List<Card> played = new ArrayList<>(player.getDeck());
        	played.removeAll(player.getPlayedCards());
        	ObservableList<Card> chosable = FXCollections.observableArrayList(played);
        	cardComboBox.setItems(chosable);
    	}
    	
    	
    	// bet runes value
    	betValueLabel.setText(Integer.toString(bet.getRunes()));
    	
    	// update rage
    	rageCheckbox.setText(String.format("Rage (cost: %d runes)", Bet.RAGE_COST));
    	
    	boolean isBettingOk = (state == State.BET_1 || state == State.BET_2 || state == State.BET_3);
    	cardComboBox.setDisable(!isBettingOk);
    	incrementBetButton.setDisable(!isBettingOk);
    	decrementBetButton.setDisable(!isBettingOk);
    	sendBetButton.setDisable(!isBettingOk || cardComboBox.getValue() == null);
    	rageCheckbox.setDisable(!isBettingOk);
    	

    }
    
    void updateCards() {
    	String playedMessage = " (played)";
    	String message = "";
    	List<Card> playerDeck = player.getDeck();
    	List<Card> playerPlayedCards = player.getPlayedCards();
    	List<Card> opponentDeck = opponent.getDeck();
    	List<Card> opponentPlayedCards = opponent.getPlayedCards();
    	
    	// P1
    	message = playerPlayedCards.contains(playerDeck.get(0)) ? playedMessage : "";
    	cardP1NameLabel.setText(playerDeck.get(0).getName() + message);
    	powerValueP1Label.setText(powerValueString(playerDeck.get(0)));
    	damageValueP1Label.setText(damageValueString(playerDeck.get(0)));
    	cardP1SkillLabel.setText(playerDeck.get(0).getSkill().toString());
    	
    	
    	// P2
    	message = playerPlayedCards.contains(playerDeck.get(1)) ? playedMessage : "";
    	cardP2NameLabel.setText(playerDeck.get(1).getName() + message);
    	powerValueP2Label.setText(powerValueString(playerDeck.get(1)));
    	damageValueP2Label.setText(damageValueString(playerDeck.get(1)));
    	cardP2SkillLabel.setText(playerDeck.get(1).getSkill().toString());
    	
    	// P3
    	message = playerPlayedCards.contains(playerDeck.get(2)) ? playedMessage : "";
    	cardP3NameLabel.setText(playerDeck.get(2).getName() + message);
    	powerValueP3Label.setText(powerValueString(playerDeck.get(2)));
    	damageValueP3Label.setText(damageValueString(playerDeck.get(2)));
    	cardP3SkillLabel.setText(playerDeck.get(2).getSkill().toString());
    	
    	// O1
    	message = opponentPlayedCards.contains(opponentDeck.get(0)) ? playedMessage : "";
    	cardO1NameLabel.setText(opponentDeck.get(0).getName() + message);
    	powerValueO1Label.setText(powerValueString(opponentDeck.get(0)));
    	damageValueO1Label.setText(damageValueString(opponentDeck.get(0)));
    	cardO1SkillLabel.setText(opponentDeck.get(0).getSkill().toString());
    	
    	// O2
    	message = opponentPlayedCards.contains(opponentDeck.get(1)) ? playedMessage : "";
    	cardO2NameLabel.setText(opponentDeck.get(1).getName() + message);
    	powerValueO2Label.setText(powerValueString(opponentDeck.get(1)));
    	damageValueO2Label.setText(damageValueString(opponentDeck.get(1)));
    	cardO2SkillLabel.setText(opponentDeck.get(1).getSkill().toString());
    	
    	// P3
    	message = opponentPlayedCards.contains(opponentDeck.get(2)) ? playedMessage : "";
    	cardO3NameLabel.setText(opponentDeck.get(2).getName() + message);
    	powerValueO3Label.setText(powerValueString(opponentDeck.get(2)));
    	damageValueO3Label.setText(damageValueString(opponentDeck.get(2)));
    	cardO3SkillLabel.setText(opponentDeck.get(2).getSkill().toString());
    }
    
    String powerValueString(Card c) {
    	return String.format("%d (base: %d)", (int)((c.getPower() + c.getSkill().getPowerBonus()) * c.getSkill().getPowerModifier()), c.getPower());
    }
    
    String damageValueString(Card c) {
    	return String.format("%d (base: %d)", (int)((c.getDamage() + c.getSkill().getDamageBonus()) * c.getSkill().getDamageModifier()), c.getDamage());
    }
    
    public void nextState(Player player, Player opponent) {
    	state = State.values()[(state.ordinal()+1)%State.values().length];
    	if (player.getHealthPoints() <= 0 || opponent.getHealthPoints() <= 0) {
    		state = State.END;
    	}
    	this.player = this.opponent = null;
    	setPlayer(player);
    	setOpponent(opponent);
    }
    
    public void lockSend() {
    	this.sendBetButton.setDisable(true);
    	this.incrementBetButton.setDisable(true);
    	this.decrementBetButton.setDisable(true);
    	this.rageCheckbox.setDisable(true);
    	this.cardComboBox.setDisable(true);
    }
    
    public void unlockSend() {
    	this.sendBetButton.setDisable(false);
    	this.incrementBetButton.setDisable(false);
    	this.decrementBetButton.setDisable(false);
    	this.rageCheckbox.setDisable(false);
    	this.cardComboBox.setDisable(false);
    }

}
