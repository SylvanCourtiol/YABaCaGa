package yabacaga.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9055118500736937484L;

	private int id;
	
	private String name;
	
	private int runes = GameMaster.DEFAULT_RUNES;
	
	private int healthPoints = GameMaster.DEFAULT_HEALTH_POINTS;
	
	private List<Card> deck = new ArrayList<Card>();
	
	private List<Integer> played = new ArrayList<>();

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

	public List<Card> getDeck() {
		return deck;
	}
	
	public void play(Card card) {
		if (!deck.contains(card)) {
			throw new IllegalArgumentException();
		}
		played.add(deck.indexOf(card));
		played.sort((a, b) ->  a - b);
	}
	
	public List<Card> getPlayedCards() {
		List<Card> result = new ArrayList<Card>();
		for (int i : played) {
			result.add(deck.get(i));
		}
		return result;
	}
	
}
