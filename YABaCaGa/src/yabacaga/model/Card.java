package yabacaga.model;

/**
 * Card of the game of YABaCaGa.
 * 
 * @author Mattéo Camin
 * @author Sylvan Courtiol
 */
public class Card {
	
	/**
	 * MIN_VALUE for the card's stats.
	 */
	public static final int MIN_STAT = 1;
	
	/**
	 * Id of this card (should be unique).
	 */
	private int id;
	
	/**
	 * Display name of this card.
	 */
	private String name;
	
	/**
	 * Power statistic of this card.
	 */
	private int power = MIN_STAT;
	
	/**
	 * Damage statistic of this card.
	 */
	private int damage = MIN_STAT;
	
	/**
	 * Skill of this card.
	 */
	private Skill skill = new Skill(); 
	
	@Override
	public String toString() {
		return name;
	}

	/**
	 * A battle card of id id and name name.
	 * @param id id of the card.
	 * @param name display name of the card.
	 */
	public Card(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	/**
	 * Cost of this card for deck building.
	 * @return the cost of this card.
	 */
	public int getCost() {
		return (skill != null ? skill.getCost() : 0)  + power + damage - 2;
	}
	
	/**
	 * @return the display name of this card.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Modify the name of this card.
	 * @param name the new name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the power statistic of this card.
	 */
	public int getPower() {
		return power;
	}

	/**
	 * Modify the power statistic of this card.
	 * @param power the new power statistic.
	 * @throws IllegalArgumentException if damage < MIN_STAT
	 */
	public void setPower(int power) {
		if (power < MIN_STAT) {
			throw new IllegalArgumentException();
		}
		this.power = power;
	}

	/**
	 * @return the damage statistic of this card.
	 */
	public int getDamage() {
		return damage;
	}

	/**
	 * Modify the damage statistic of this card.
	 * @param damage the new damage statistic.
	 * @throws IllegalArgumentException if damage < MIN_STAT
	 */
	public void setDamage(int damage) {
		if (damage < MIN_STAT) {
			throw new IllegalArgumentException();
		}
		this.damage = damage;
	}

	/**
	 * @return the skill of this card or null if no skill.
	 */
	public Skill getSkill() {
		return skill;
	}

	/**
	 * Modify the skill of this card.
	 * @param skill the new skill of this card, null if no skill.
	 */
	public void setSkill(Skill skill) {
		this.skill = skill;
	}

	/**
	 * @return the id of this card.
	 */
	public int getId() {
		return id;
	}
	
}
