package tst;

import static org.junit.jupiter.api.Assertions.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import yabacaga.model.Card;
import yabacaga.model.GameMaster;
import yabacaga.model.GameMaster.State;
import yabacaga.model.Player;

class GameMasterTest implements PropertyChangeListener {

	private GameMaster gm = null;
	private Card card1 = null;
	private Card card2 = null;
	private Card card3 = null;
	List<Card> correctDeck = null;
	private Player p1 = null;
	private Player p2 = null;
	private int firstPlayer = -1;

	@BeforeEach
	void init() {
		gm = new GameMaster();
		card1 = new Card(0, "c1");
		card2 = new Card(1, "c2");
		card3 = new Card(2, "c3");
		
		correctDeck = new ArrayList<>();
		
		correctDeck.add(card1);
		correctDeck.add(card2);
		correctDeck.add(card3);
		
		p1 = new Player("p1", correctDeck);
		p2 = new Player("p2", correctDeck);
	}

	@Test
	void enterPlayerTestNominal() {
		assertTrue(gm.enterPlayer(p1) > 0);
		assertTrue(gm.enterPlayer(p2) > 0);
	}

	@Test
	void enterPlayerTestAlreadyEntered() {
		assertTrue(gm.enterPlayer(p1) > 0);
		assertEquals(GameMaster.ALREADY_ENTERED_ERROR, gm.enterPlayer(p1));
		assertTrue(gm.enterPlayer(p2) > 0);
	}

	@Test
	void enterPlayerTestTooMuchPlayer() {
		Player intrus = new Player("pIntrus", correctDeck);

		assertTrue(gm.enterPlayer(p1) > 0);
		assertTrue(gm.enterPlayer(p2) > 0);
		assertEquals(GameMaster.TOO_MANY_PLAYER_ERROR, gm.enterPlayer(intrus));
	}

	@Test
	void enterPlayerTestTooFew() {
		List<Card> tooFewDeck = new ArrayList<>();
		tooFewDeck.add(card1);
		tooFewDeck.add(card2);

		Player pTooFew = new Player("pTooFew", tooFewDeck);
		assertEquals(GameMaster.INCORRECT_DECK_ERROR, gm.enterPlayer(pTooFew));
	}

	@Test
	void enterPlayerTestTooMuch() {
		Card cardTooMuch = new Card(3, "cTooMuch");
		List<Card> tooMuchDeck = new ArrayList<>();
		
		tooMuchDeck.add(card1);
		tooMuchDeck.add(card2);
		tooMuchDeck.add(card3);
		tooMuchDeck.add(cardTooMuch);

		Player pTooMuch = new Player("pTooMuch", tooMuchDeck);
		assertEquals(GameMaster.INCORRECT_DECK_ERROR, gm.enterPlayer(pTooMuch));
	}

	@Test
	void enterPlayerTestExpensive() {
		Card cardTooExpensive = new Card(2, "cTooExpensive");
		cardTooExpensive.setDamage(100);

		List<Card> tooExpensiveDeck = new ArrayList<>();
		tooExpensiveDeck.add(card1);
		tooExpensiveDeck.add(card2);
		tooExpensiveDeck.add(cardTooExpensive);

		Player pTooExpensive = new Player("pooExpensive", tooExpensiveDeck);
		assertEquals(GameMaster.INCORRECT_DECK_ERROR, gm.enterPlayer(pTooExpensive));
	}
	
	int nominalGameBeginning() {
		p1.setId(gm.enterPlayer(p1));
		p2.setId(gm.enterPlayer(p2));
		
		return gm.beginGame();
	}

	@Test
	void beginGameTestNominal() {
		int returnCode = nominalGameBeginning();
		assertTrue(returnCode == p1.getId() || returnCode == p2.getId());

	}

	@Test
	void beginGameTestError() {
		p1.setId(gm.enterPlayer(p1));
		
		
		int returnCode = gm.beginGame();
		assertTrue(returnCode == -1);

		p2.setId(gm.enterPlayer(p2));
		returnCode = gm.beginGame();
		assertTrue(returnCode == p1.getId() || returnCode == p2.getId());
		

		returnCode = gm.beginGame();
		assertTrue(returnCode == -1);
	}
	
	int nominalFirstBet() {
		firstPlayer = nominalGameBeginning();
		int returnCode;
		
		if (firstPlayer == p1.getId()) {
			returnCode = gm.receiveBet(p1.getId(), 0, 1, false);
		} else {
			returnCode = gm.receiveBet(p2.getId(), 0, 1, false);
		}
		 return returnCode;
	}
	
	@Test
	void receiveBetTestFirstPlayerNominal() {
		int returnCode = nominalFirstBet();
		assertEquals(0, returnCode);
	}
	
	@Test
	void receiveBetTestFirstPlayerTooExpensive() {
		int firstPlayer = nominalGameBeginning();
		int returnCode;
		
		if (firstPlayer == p1.getId()) {
			returnCode = gm.receiveBet(p1.getId(), 0, GameMaster.DEFAULT_RUNES + 1, false);
		} else {
			returnCode = gm.receiveBet(p2.getId(), 0, GameMaster.DEFAULT_RUNES + 1, false);
		}
		assertEquals(GameMaster.NOT_A_CORRECT_BET_ERROR, returnCode);
		
		if (firstPlayer == p1.getId()) {
			returnCode = gm.receiveBet(p1.getId(), 0, GameMaster.DEFAULT_RUNES - 1, true);
		} else {
			returnCode = gm.receiveBet(p2.getId(), 0, GameMaster.DEFAULT_RUNES - 1, true);
		}
		assertEquals(GameMaster.NOT_A_CORRECT_BET_ERROR, returnCode);
	}
	
	@Test
	void receiveBetTestFirstPlayerNotRightPlayer() {
		int firstPlayer = nominalGameBeginning();
		int returnCode;
		if (firstPlayer == p1.getId()) {
			returnCode = gm.receiveBet(p2.getId(), 0, 1, false);
		} else {
			returnCode = gm.receiveBet(p1.getId(), 0, 1, false);
		}
		assertEquals(GameMaster.NOT_PLAYER_TURN_ERROR, returnCode);
	}
	
	@Test
	void receiveBetTestFirstPlayerNotPlayer() {
		nominalGameBeginning();
		Player intrus = new Player("Intrus", correctDeck);
		
		int returnCode = gm.receiveBet(intrus.getId(), 0, 1, false);
		assertEquals(GameMaster.NOT_A_PLAYER_BET_ERROR, returnCode);
	}
	
	int nominalSecondBet() {
		nominalFirstBet();
		int returnCode;
		if (firstPlayer == p1.getId()) {
			returnCode = gm.receiveBet(p2.getId(), 0, 1, false);
		} else {
			returnCode = gm.receiveBet(p1.getId(), 0, 1, false);
		}
		return returnCode;
	}
	
	@Test
	void receiveBetTestSecondPlayerNominal() {
		nominalFirstBet();
		int returnCode = nominalSecondBet();
		assertEquals(0, returnCode);
	}


	@Override
	public void propertyChange(PropertyChangeEvent evt) {

	}

}
