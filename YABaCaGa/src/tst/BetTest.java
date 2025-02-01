package tst;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import yabacaga.model.Bet;

public class BetTest {
	private Bet bet;

	@BeforeEach
	void init() {
		bet = new Bet(0, 0, 0, false);
	}

	@Test
	void setRunesTestNominal() {
		int expected = 10;
		bet.setRunes(expected);
		assertEquals(expected, bet.getRunes());
	}

	@Test
	void setRunesTestNegative() {
		boolean exception = false;
		try {
		bet.setRunes(-1);
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);
	}
}
