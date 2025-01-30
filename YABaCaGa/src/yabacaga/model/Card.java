package yabacaga.model;

/**
 * Card of the game of YABaCaGa.
 * 
 * @author Mattéo Camin
 * @author Sylvan Courtiol
 */
public class Card {
	
	public static final int MIN_STAT = 1;
	
	private int id;
	
	private String name;
	
	private int power = MIN_STAT;
	
	private int damage = MIN_STAT;
	
	private Skill skill = null; 
	
	
	public Card(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public int getCost() {
		return (skill != null ? skill.getCost() : 0)  + power + damage - 2;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		if (power < MIN_STAT) {
			throw new IllegalArgumentException();
		}
		this.power = power;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		if (damage < MIN_STAT) {
			throw new IllegalArgumentException();
		}
		this.damage = damage;
	}

	public Skill getSkill() {
		return skill;
	}

	public void setSkill(Skill skill) {
		this.skill = skill;
	}

	public int getId() {
		return id;
	}
	
}
