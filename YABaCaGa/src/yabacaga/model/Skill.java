package yabacaga.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Power of a card that changes some statistics.
 * The statistics changed can be : 
 * - powerModifier
 * - powerBonus
 * - attackModifier
 * - attackBonus
 * - damageModifier
 * - damageBonus
 * - damageRevenge
 * - runePayback
 * - runePoison
 * 
 * @author Mattéo Camin
 * @author Sylvan Courtiol
 */
public class Skill {
	
	public static final float NEUTRAL_MODIFIER = 1.0f;
	public static final int NEUTRAL_BONUS = 0;
	public static final float MODIFIER_COST_UNIT = 0.1f;
	
	private float powerModifier = NEUTRAL_MODIFIER;
	
	private int powerBonus = NEUTRAL_BONUS;
	
	private float attackModifier = NEUTRAL_MODIFIER;
	
	private int attackBonus = NEUTRAL_BONUS;
	
	private float damageModifier = NEUTRAL_MODIFIER;
	
	private int damageBonus = NEUTRAL_BONUS;
	
	private int damageRevenge = NEUTRAL_BONUS;
	
	private int runePayback = NEUTRAL_BONUS;
	
	private int runePoison = NEUTRAL_BONUS;
	
	public static List<Skill> getSkills() {
		List<Skill> skills = new ArrayList<>();
		
		Skill s = new Skill();
		s.setPowerModifier(1.2f);
		skills.add(s);
		
		s = new Skill();
		s.setPowerModifier(1.1f);
		s.setDamageModifier(1.1f);
		skills.add(s);
		
		s = new Skill();
		s.setRunePayback(1);
		skills.add(s);
		
		return skills;
	}
	
	public int getCost() {
		return powerBonus + attackBonus + damageBonus + runePayback + runePoison 
				+ costFromModifier(powerModifier) + costFromModifier(attackModifier) + costFromModifier(damageModifier);
	}
	
	private int costFromModifier(float modifier) {
		float MARGIN = 0.001f;
		return (int) Math.ceil((Math.abs(modifier - NEUTRAL_MODIFIER) - MARGIN) / MODIFIER_COST_UNIT);
	}
	
	public float getPowerModifier() {
		return powerModifier;
	}

	public void setPowerModifier(float powerModifier) {
		this.powerModifier = powerModifier;
	}

	public int getPowerBonus() {
		return powerBonus;
	}

	public void setPowerBonus(int powerBonus) {
		this.powerBonus = powerBonus;
	}

	public float getAttackModifier() {
		return attackModifier;
	}

	public void setAttackModifier(float attackModifier) {
		this.attackModifier = attackModifier;
	}

	public int getAttackBonus() {
		return attackBonus;
	}

	public void setAttackBonus(int attackBonus) {
		this.attackBonus = attackBonus;
	}

	public float getDamageModifier() {
		return damageModifier;
	}

	public void setDamageModifier(float damageModifier) {
		this.damageModifier = damageModifier;
	}

	public int getDamageBonus() {
		return damageBonus;
	}

	public void setDamageBonus(int damageBonus) {
		this.damageBonus = damageBonus;
	}

	public int getDamageRevenge() {
		return damageRevenge;
	}

	public void setDamageRevenge(int damageRevenge) {
		this.damageRevenge = damageRevenge;
	}

	public int getRunePayback() {
		return runePayback;
	}

	public void setRunePayback(int runePayback) {
		this.runePayback = runePayback;
	}

	public int getRunePoison() {
		return runePoison;
	}

	public void setRunePoison(int runePoison) {
		this.runePoison = runePoison;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		
        if (powerModifier != NEUTRAL_MODIFIER) {
        	result.append(String.format("power * %3.2f\n", powerModifier));
        }
        if (powerBonus != NEUTRAL_BONUS) {
        	result.append(String.format("power + %d\n", powerBonus));
        }
        if (attackModifier != NEUTRAL_MODIFIER) {
        	result.append(String.format("attack * %3.2f\n", attackModifier));
        }
        if (attackBonus != NEUTRAL_BONUS) {
        	result.append(String.format("attack + %d\n", attackBonus));
        }
        if (damageModifier != NEUTRAL_MODIFIER) {
        	result.append(String.format("damage * %3.2f\n", damageModifier));
        }
        if (damageBonus != NEUTRAL_BONUS) {
        	result.append(String.format("damage + %d\n", damageBonus));
        }
        if (damageRevenge != NEUTRAL_BONUS) {
        	result.append(String.format("damage revenge: %d\n", damageRevenge));
        }
        if (runePayback != NEUTRAL_BONUS) {
        	result.append(String.format("rune Payback: %d\n", runePayback));
        }
        if (runePoison != NEUTRAL_BONUS) {
        	result.append(String.format("rune poison: %d\n", runePoison));
        }
		result.append(String.format("Cost: %d\n", getCost()));
		
		return result.toString();
	}
	
	
	
}
