package tst;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import yabacaga.model.Card;

public class CardTest {
	Card c = null;

	@BeforeEach
	void init() {
		c = new Card(0, "CardName");
	}

	@Test
	void setPowerTestNominal() {
		int expected = 10;
		c.setPower(expected);
		assertEquals(expected, c.getPower());
	}

	@Test
	void setDamageTestNominal() {
		int expected = 10;
		c.setDamage(expected);
		assertEquals(expected, c.getDamage());
	}

	@Test
	void setPowerTestNegative() {
		boolean exception = false;
		try {
			c.setPower(-1);
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);
	}

	@Test
	void setDamageTestNegative() {
		boolean exception = false;
		try {
			c.setDamage(-1);
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);
	}
	
	@Test
	void setPowerTestZero() {
		boolean exception = false;
		try {
			c.setPower(0);
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);
	}

	@Test
	void setDamageTestZero() {
		boolean exception = false;
		try {
			c.setDamage(0);
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);
	}

}
