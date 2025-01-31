package yabacaga.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

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
public class Skill implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5842303676007863491L;

	/**
	 * Value of the neutral element for a modifier.
	 */
	public static final float NEUTRAL_MODIFIER = 1.0f;
	
	/**
	 * Value of the neutral element for a bonus.
	 */
	public static final int NEUTRAL_BONUS = 0;
	
	/**
	 * Modifier amount corresponding to 1 point of cost.
	 */
	public static final float MODIFIER_COST_UNIT = 0.5f;
	
	/**
	 * The power modifier for this skill.
	 */
	private float powerModifier = NEUTRAL_MODIFIER;
	
	/**
	 * The power bonus for this skill.
	 */
	private int powerBonus = NEUTRAL_BONUS;
	
	/**
	 * The attack modifier for this skill.
	 */
	private float attackModifier = NEUTRAL_MODIFIER;
	
	/**
	 * The attack bonus for this skill.
	 */
	private int attackBonus = NEUTRAL_BONUS;
	
	/**
	 * The damage modifier for this skill.
	 */
	private float damageModifier = NEUTRAL_MODIFIER;
	
	/**
	 * The damage bonus for this skill.
	 */
	private int damageBonus = NEUTRAL_BONUS;
	
	/**
	 * The damage revenge for this skill. This is guaranteed damage to the opponent.
	 */
	private int damageRevenge = NEUTRAL_BONUS;
	
	/**
	 * The rune payback for this skill. This is a number of run you gain back after a fight.
	 */
	private int runePayback = NEUTRAL_BONUS;
	
	/**
	 * The rune poison for this skill. This provokes a loss of runes for the opponent.
	 */
	private int runePoison = NEUTRAL_BONUS;
	
	/**
	 * @return the list of skills of the game.
	 */
	public static List<Skill> getSkills() {
		List<Skill> skills = new ArrayList<>();
		
		Skill s = new Skill();
		s.setPowerModifier(2.0f);
		skills.add(s);
		
		s = new Skill();
		s.setPowerModifier(1.5f);
		s.setDamageModifier(1.5f);
		skills.add(s);
		
		s = new Skill();
		s.setRunePayback(1);
		skills.add(s);
		
		s = new Skill(); //Neutral skill
		skills.add(s);
		
		return skills;
	}
	
	/**
	 * The cost of a skill is calculated from its effects. It is used for deck building.
	 * @return the cost of this skill.
	 */
	public int getCost() {
		return powerBonus + attackBonus + damageBonus + runePayback + runePoison 
				+ costFromModifier(powerModifier) + costFromModifier(attackModifier) + costFromModifier(damageModifier);
	}
	
	/**
	 * Calculate the cost of a modifier (multiplication).
	 * @param modifier the modifier from which is calculated the cost.
	 * @return the cost for this modifier.
	 */
	private int costFromModifier(float modifier) {
		float MARGIN = 0.001f;
		return (int) Math.ceil((Math.abs(modifier - NEUTRAL_MODIFIER) - MARGIN) / MODIFIER_COST_UNIT);
	}
	
	/**
	 * @return The power modifier of this skill.
	 */
	public float getPowerModifier() {
		return powerModifier;
	}

	/**
	 * Modify the power modifier of this skill.
	 * @param powerModifier the new power modifier.
	 */
	public void setPowerModifier(float powerModifier) {
		this.powerModifier = powerModifier;
	}

	/**
	 * @return the power bonus of this skill.
	 */
	public int getPowerBonus() {
		return powerBonus;
	}

	/**
	 * Modify the power bonus of this skill.
	 * @param powerBonus the new power bonus.
	 */
	public void setPowerBonus(int powerBonus) {
		this.powerBonus = powerBonus;
	}

	/**
	 * @return the attack modifier of this skill.
	 */
	public float getAttackModifier() {
		return attackModifier;
	}

	/**
	 * Modify the attack modifier of this skill.
	 * @param attackModifier the new attack modifier.
	 */
	public void setAttackModifier(float attackModifier) {
		this.attackModifier = attackModifier;
	}

	/**
	 * @return the attack bonus of this skill.
	 */
	public int getAttackBonus() {
		return attackBonus;
	}

	/**
	 * Modify the attack bonus of this skill.
	 * @param attackBonus the new attack bonus.
	 */
	public void setAttackBonus(int attackBonus) {
		this.attackBonus = attackBonus;
	}

	/**
	 * @return the damage modifier of this skill.
	 */
	public float getDamageModifier() {
		return damageModifier;
	}

	/**
	 * Modify the damage modifier of this skill.
	 * @param damageModifier the new damage modifier.
	 */
	public void setDamageModifier(float damageModifier) {
		this.damageModifier = damageModifier;
	}

	/**
	 * @return the damage bonus of this skill.
	 */
	public int getDamageBonus() {
		return damageBonus;
	}

	/**
	 * Modify the damage bonus of this skill.
	 * @param damageBonus the new damage bonus.
	 */
	public void setDamageBonus(int damageBonus) {
		this.damageBonus = damageBonus;
	}

	/**
	 * The damage revenge is the amount of damage your opponent take even if you loose the fight.
	 * @return the damage revenge of this skill.
	 */
	public int getDamageRevenge() {
		return damageRevenge;
	}

	/**
	 * Modify the damage revenge of this skill.
	 * @param damageRevenge the new damage revenge value.
	 */
	public void setDamageRevenge(int damageRevenge) {
		this.damageRevenge = damageRevenge;
	}

	/**
	 * The rune payback is the amount of rune you gain back after the round with a card with that skill.
	 * @return the rune payback of this skill.
	 */
	public int getRunePayback() {
		return runePayback;
	}

	/**
	 * Modify the rune payback of this skill.
	 * @param runePayback the new rune payback value.
	 */
	public void setRunePayback(int runePayback) {
		this.runePayback = runePayback;
	}

	/**
	 * The rune poison is the amount of run your opponent will automatically loose.
	 * @return the rune poison of this skill.
	 */
	public int getRunePoison() {
		return runePoison;
	}

	/**
	 * Modify the rune poison of this skill.
	 * @param runePoison the new rune poison value.
	 */
	public void setRunePoison(int runePoison) {
		this.runePoison = runePoison;
	}

	/* no javadoc - @see Object#toString() */
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
		if (result.length() == 0) {
			result.append("No skill\n");
		}
		result.append(String.format("Cost: %d\n", getCost()));
		return result.toString();
	}
	
	
	
}
