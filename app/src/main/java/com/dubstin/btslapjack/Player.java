package com.dubstin.btslapjack;

import java.util.ArrayList;

public class Player {
    private Hand hand = new Hand();
    private String name;

    Player() {
        name = "Anonymous";
    }

    Player(String n) {
        name = n;
    }

    public void setName(String n) {
        name = n;
    }

    public void setCards(ArrayList<Card> cards) {
        hand.addCards(cards);
    }

    public void grabPile(ArrayList<Card> pile) {
        hand.addCards(pile);
    }

    public int getNumberOfJokers() {
        return hand.getNumberOfJokers();
    }

    public String getName() {
        return name;
    }

    public ArrayList<Card> getCards() {
        return hand.getCards();
    }

    public int getHandCount() {
        return hand.getCount();
    }
}
