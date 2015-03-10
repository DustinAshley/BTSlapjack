package com.dubstin.btslapjack;

import java.util.ArrayList;

public class Hand {

    private ArrayList<Card> cards = new ArrayList<Card>();
    private int numberOfJokers = 0;

    Hand() {

    }

    public void addCards(ArrayList<Card> newCards) {
        for (int i = 0; i < newCards.size(); i++) {
            cards.add(newCards.get(i));
            if (newCards.get(i).getSuit() == 4) {
                numberOfJokers++;
            }
        }
    }

    public int getNumberOfJokers() {
        return numberOfJokers;
    }

    public void clearHand() {
        cards.clear();
        numberOfJokers = 0;
    }

    public int getCount() {
        return cards.size();
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

}
