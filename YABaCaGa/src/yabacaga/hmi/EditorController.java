package yabacaga.hmi;

import java.util.List;

import client.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import yabacaga.model.Card;
import yabacaga.model.GameMaster;
import yabacaga.model.Player;
import yabacaga.model.Skill;

/**
 * Controller of the editor interface.
 * 
 * @author Mattéo Camin
 * @author Sylvan Courtiol
 */
public class EditorController {
	/** First card modified by this */
	private Card card = new Card(0, "first card");

	/** Second card modified by this */
	private Card card1 = new Card(1, "second card");

	/** Third card modified by this */
	private Card card2 = new Card(2, "third card");

	private boolean lockUpdate = false;

	@FXML
	private Text costLabel;

	@FXML
	private Text damageValueLabel;

	@FXML
	private Text damageValueLabel1;

	@FXML
	private Text damageValueLabel2;

	@FXML
	private Button decrementDamageButton;

	@FXML
	private Button decrementDamageButton1;

	@FXML
	private Button decrementDamageButton2;

	@FXML
	private Button decrementPowerButton;

	@FXML
	private Button decrementPowerButton1;

	@FXML
	private Button decrementPowerButton2;

	@FXML
	private Button incrementDamageButton;

	@FXML
	private Button incrementDamageButton1;

	@FXML
	private Button incrementDamageButton2;

	@FXML
	private Button incrementPowerButton;

	@FXML
	private Button incrementPowerButton1;

	@FXML
	private Button incrementPowerButton2;

	@FXML
	private TextField nameField;

	@FXML
	private TextField nameField1;

	@FXML
	private TextField nameField2;

	@FXML
	private TextField playerName;

	@FXML
	private Text powerValueLabel;

	@FXML
	private Text powerValueLabel1;

	@FXML
	private Text powerValueLabel2;

	@FXML
	private ComboBox<Skill> skillComboBox;

	@FXML
	private ComboBox<Skill> skillComboBox1;

	@FXML
	private ComboBox<Skill> skillComboBox2;

	@FXML
	private Button playButton;

	@FXML
	private void initialize() {
		ObservableList<Skill> skills = FXCollections.observableArrayList(Skill.getSkills());
		skillComboBox.setItems(skills);
		skillComboBox1.setItems(skills);
		skillComboBox2.setItems(skills);

		playButton.setDisable(true);

		update();
	}

	@FXML
	void decrementDamage(ActionEvent event) {
		try {
			Object source = event.getSource();
			if (source == decrementDamageButton) {
				card.setDamage(card.getDamage() - 1);
			} else if (source == decrementDamageButton1) {
				card1.setDamage(card1.getDamage() - 1);
			} else if (source == decrementDamageButton2) {
				card2.setDamage(card2.getDamage() - 1);
			}
			update();
		} catch (IllegalArgumentException e) {
			// empty body
		}
	}

	@FXML
	void decrementPower(ActionEvent event) {
		try {
			Object source = event.getSource();
			if (source == decrementPowerButton) {
				card.setPower(card.getPower() - 1);
			} else if (source == decrementPowerButton1) {
				card1.setPower(card1.getPower() - 1);
			} else if (source == decrementPowerButton2) {
				card2.setPower(card2.getPower() - 1);
			}
			update();
		} catch (IllegalArgumentException e) {
			// empty body
		}
	}

	@FXML
	void incrementDamage(ActionEvent event) {
		if (getDeckCost() >= GameMaster.MAX_DECK_COST) {
			return; // ignore
		}
		Object source = event.getSource();
		if (source == incrementDamageButton) {
			card.setDamage(card.getDamage() + 1);
		} else if (source == incrementDamageButton1) {
			card1.setDamage(card1.getDamage() + 1);
		} else if (source == incrementDamageButton2) {
			card2.setDamage(card2.getDamage() + 1);
		}
		update();
	}

	@FXML
	void incrementPower(ActionEvent event) {
		if (getDeckCost() >= GameMaster.MAX_DECK_COST) {
			return; // ignore
		}
		Object source = event.getSource();
		if (source == incrementPowerButton) {
			card.setPower(card.getPower() + 1);
		} else if (source == incrementPowerButton1) {
			card1.setPower(card1.getPower() + 1);
		} else if (source == incrementPowerButton2) {
			card2.setPower(card2.getPower() + 1);
		}
		update();
	}

	@FXML
	void playerNameChanged(ActionEvent event) {
		update();
	}

	@FXML
	void skillChosen(ActionEvent event) {
		ComboBox<Skill> source = (ComboBox<Skill>) event.getSource();
		Card relevantCard = null;
		Skill chosenSkill = null;
		if (source == skillComboBox) {
			relevantCard = card;
			chosenSkill = skillComboBox.getValue();
		} else if (source == skillComboBox1) {
			relevantCard = card1;
			chosenSkill = skillComboBox1.getValue();
		} else if (source == skillComboBox2) {
			relevantCard = card2;
			chosenSkill = skillComboBox2.getValue();
		}

		if (getDeckCost() + (chosenSkill != null ? chosenSkill.getCost() : 0)
				- (relevantCard.getSkill() != null ? relevantCard.getSkill().getCost()
						: 0) <= GameMaster.MAX_DECK_COST) {
			relevantCard.setSkill(chosenSkill);
		} else {
			source.setValue(relevantCard.getSkill());
		}

		update();
	}

	@FXML
	void playButtonPressed(ActionEvent event) {
		// TODO envoyer le joueur au serveur
		Player newPlayer = new Player(playerName.getText(), List.of(card, card1, card2));
		Client.getClient().enterPlayer(newPlayer, this);
	}

	/**
	 * Update the editor border panel.
	 */
	void update() {
		if (!lockUpdate) {
			nameField.setText(card.getName());
			nameField1.setText(card1.getName());
			nameField2.setText(card2.getName());

			powerValueLabel.setText(Integer.toString(card.getPower()));
			powerValueLabel1.setText(Integer.toString(card1.getPower()));
			powerValueLabel2.setText(Integer.toString(card2.getPower()));

			damageValueLabel.setText(Integer.toString(card.getDamage()));
			damageValueLabel1.setText(Integer.toString(card1.getDamage()));
			damageValueLabel2.setText(Integer.toString(card2.getDamage()));

			boolean deckIsValid = getDeckCost() <= GameMaster.MAX_DECK_COST && !playerName.getText().isBlank()
					&& !nameField.getText().isBlank() && !nameField1.getText().isBlank()
					&& !nameField2.getText().isBlank() && skillComboBox.getValue() != null
					&& skillComboBox1.getValue() != null && skillComboBox2.getValue() != null;
			playButton.setDisable(!deckIsValid);

			updateCostInformation();
		}
	}

	/**
	 * Update cost information displayed with costLabel.
	 */
	void updateCostInformation() {
		costLabel.setText(String.format("Deck cost: %d/%d (remaining points: %d)", getDeckCost(),
				GameMaster.MAX_DECK_COST, GameMaster.MAX_DECK_COST - getDeckCost()));
	}

	/**
	 * @return the cost of the edited deck.
	 */
	int getDeckCost() {
		return card.getCost() + card1.getCost() + card2.getCost();
	}

	public void lockOptions() {
		playButton.setDisable(true);
		this.lockUpdate = true;

	}
	
	public void unlockOptions() {
		this.lockUpdate = false;

	}
}
