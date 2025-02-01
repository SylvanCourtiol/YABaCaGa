package tst;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import yabacaga.model.Skill;

public class SkillTest {
	
	float epsilon = 0.000001f;
	
	Skill s = null;
	
	@BeforeEach
	void init() {
		s = new Skill();
	}
	
	@Test
	void setDamageBonusTestNominal() {
		int expected = 10;
		s.setDamageBonus(expected);
		assertEquals(expected, s.getDamageBonus());
	}
	
	@Test
	void setPowerBonusTestNominal() {
		int expected = 10;
		s.setPowerBonus(expected);
		assertEquals(expected, s.getPowerBonus());
	}
	
	@Test
	void setAttackBonusTestNominal() {
		int expected = 10;
		s.setAttackBonus(expected);
		assertEquals(expected, s.getAttackBonus());
	}
	
	@Test
	void setRunePaybackTestNominal() {
		int expected = 10;
		s.setRunePayback(expected);
		assertEquals(expected, s.getRunePayback());
	}
	
	@Test
	void setDamageRevengeTestNominal() {
		int expected = 10;
		s.setDamageRevenge(expected);
		assertEquals(expected, s.getDamageRevenge());
	}
	
	@Test
	void setRunePoisonTestNominal() {
		int expected = 10;
		s.setRunePoison(expected);
		assertEquals(expected, s.getRunePoison());
	}
	
	@Test
	void setDamageModifierTestNominal() {
		float expected = 2.0f;
		s.setDamageModifier(expected);
		assertTrue(epsilon > Math.abs(s.getDamageModifier()-expected));
	}
	
	@Test
	void setPowerModifierTestNominal() {
		float expected = 2.0f;
		s.setPowerModifier(expected);
		assertTrue(epsilon > Math.abs(s.getPowerModifier()-expected));
	}
	
	@Test
	void setAttackModifierTestNominal() {
		float expected = 2.0f;
		s.setAttackModifier(expected);
		assertTrue(epsilon > Math.abs(s.getAttackModifier()-expected));
	}

	@Test
	void setDamageBonusTestNegative() {
		boolean exception = false;
		try {
			s.setDamageBonus(-1);
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);
	}
	
	@Test
	void setPowerBonusTestNegative() {
		boolean exception = false;
		try {
			s.setPowerBonus(-1);
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);
	}
	
	@Test
	void setAttackBonusTestNegative() {
		boolean exception = false;
		try {
			s.setAttackBonus(-1);
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);
	}
	
	@Test
	void setRunePaybackTestNegative() {
		boolean exception = false;
		try {
			s.setRunePayback(-1);
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);
	}
	
	@Test
	void setRunePoisonTestNegative() {
		boolean exception = false;
		try {
			s.setRunePoison(-1);
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);
	}
	
	@Test
	void setDamageRevengeTestNegative() {
		boolean exception = false;
		try {
			s.setDamageRevenge(-1);
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);
	}
	
	@Test
	void setDamageModifierTestNegative() {
		boolean exception = false;
		try {
			s.setDamageModifier(0.9f);
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);
	}
	
	@Test
	void setPowerModifierTestNegative() {
		boolean exception = false;
		try {
			s.setPowerModifier(0.9f);
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);
	}
	
	@Test
	void setAttackModifierTestNegative() {
		boolean exception = false;
		try {
			s.setAttackModifier(0.9f);
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(exception);
	}

}
