package yabacaga.model;

import java.util.ArrayList;
import java.util.List;

public class Player {
	
	private int id;
	
	private String name;
	
	private int runes = GameMaster.DEFAULT_RUNES;
	
	private int healthPoints = GameMaster.DEFAULT_HEALTH_POINTS;
	
	private List<Card> deck = new ArrayList<Card>();

	public Player(String name, List<Card> deck) {
		super();
		this.name = name;
		this.deck = deck;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRunes() {
		return runes;
	}

	public void setRunes(int runes) {
		this.runes = runes;
	}

	public int getHealthPoints() {
		return healthPoints;
	}

	public void setHealthPoints(int healthPoints) {
		this.healthPoints = healthPoints;
	}
	
}
