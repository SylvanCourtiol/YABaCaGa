package yabacaga.model;

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
	
	private float powerModifier = NEUTRAL_MODIFIER;
	
	private int powerBonus = NEUTRAL_BONUS;
	
	private float attackModifier = NEUTRAL_MODIFIER;
	
	private int attackBonus = NEUTRAL_BONUS;
	
	private float damageModifier = NEUTRAL_MODIFIER;
	
	private int damageBonus = NEUTRAL_BONUS;
	
	private int damageRevenge = NEUTRAL_BONUS;
	
	private int runePayback = NEUTRAL_BONUS;
	
	private int runePoison = NEUTRAL_BONUS;
	
	public int getCost() {
		return 1; // TODO stub
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
	
}
