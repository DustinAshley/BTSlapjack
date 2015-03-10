package com.dubstin.btslapjack;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Deck implements Parcelable {

    private boolean usesJokers = false;
    private List<Card> cards = new ArrayList<Card>();

    public Deck(boolean shouldShuffle, boolean shouldUseJokers) {
        initiateDeck(shouldUseJokers);
        if (shouldShuffle) {
            shuffle();
        }
    }

    public void initiateDeck(boolean shouldUseJokers){
        int suit = -1;
        for (int i = 0; i < 52; i++) {
            if (i % 13 == 0) {
                suit++;
            }
            cards.add(new Card(suit, (i % 13) + 1));
        }
        if (shouldUseJokers) {
            cards.add(new Card(4, 0));
            cards.add(new Card(4, 0));
        }
    }

    public void resetDeck() {
        cards.clear();
        initiateDeck(usesJokers);
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public void shuffle(Random rnd) {
        Collections.shuffle(cards, rnd);
    }

    public List<Card> getCards() {
        return cards;
    }

    public int getCardCount() {
        return cards.size();
    }

    public Card dealCard() {
        Card topCard = cards.get(0); // assign from card on top of list instead
        cards.remove(0); // MAKE SURE ARRAYLIST IS ZERO BASED
        return topCard;
    }

    public int describeContents() {
        return 0;
    }

    /** save object in parcel */
    public void writeToParcel(Parcel out, int flags) {
        out.writeByte((byte) (usesJokers ? 1 : 0));
        out.writeList(cards);
    }

    public static final Creator<Deck> CREATOR
            = new Creator<Deck>() {
        public Deck createFromParcel(Parcel in) {
            return new Deck(in);
        }

        public Deck[] newArray(int size) {
            return new Deck[size];
        }
    };

    /** recreate object from parcel */
    private Deck(Parcel in) {
        usesJokers = in.readByte() != 0;
        cards = in.readArrayList(Card.class.getClassLoader());
    }
}
