package tst;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import yabacaga.model.Card;
import yabacaga.model.Player;

public class PlayerTest {
	
	private Card card1 = null;
	private Card card2 = null;
	private Card card3 = null;
	List<Card> correctDeck = null;
	private Player p1 = null;
	
	@BeforeEach
	void init() {
		card1 = new Card(0, "c1");
		card2 = new Card(1, "c2");
		card3 = new Card(2, "c3");
		
		correctDeck = new ArrayList<>();
		
		correctDeck.add(card1);
		correctDeck.add(card2);
		correctDeck.add(card3);
		
		p1 = new Player("p1", correctDeck);
	}
	
	@Test
	void playTestNominal() {
		p1.play(card1);
		
		assertTrue(p1.getPlayedCards().contains(card1));
	}
	
	@Test
	void playTestNotPartOfDeck() {
		Card intrus = new Card(0, "Test");
		boolean exception = false;
		try {
			p1.play(intrus);
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		
		assertTrue(exception);
	}
}
