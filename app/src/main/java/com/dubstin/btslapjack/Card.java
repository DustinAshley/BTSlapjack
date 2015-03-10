package com.dubstin.btslapjack;

import android.os.Parcel;
import android.os.Parcelable;
import java.security.InvalidParameterException;

public class Card implements Parcelable {

    public final static int SPADES = 0;
    public final static int HEARTS = 1;
    public final static int DIAMONDS = 2;
    public final static int CLUBS = 3;
    public final static int JOKER = 4;

    public final static int ACE = 1;
    public final static int JACK = 11;
    public final static int QUEEN = 12;
    public final static int KING = 13;

    private final int suit, value;

    public Card() {
        suit = JOKER;
        value = 1;
    }

    public Card(int cardSuit, int cardValue) {
        if (    cardSuit == SPADES ||
                cardSuit == HEARTS ||
                cardSuit == DIAMONDS ||
                cardSuit == CLUBS ) {
            if (cardValue >= 1 || cardValue <= 13) {
                value = cardValue;
            } else {
                throw new InvalidParameterException("Invalid card value: " + Integer.toString(cardValue));
            }
        } else if (cardSuit == JOKER) {
            value = 0;
        } else {
            throw new IllegalArgumentException("Invalid card suit");
        }
        suit = cardSuit;
    }

    public Card(Card c) {
        suit = c.getSuit();
        value = c.getValue();
    }

    public int getSuit() {
        return suit;
    }

    public int getValue() {
        return value;
    }

    public String suitToString() {
        switch ( suit ) {
            case SPADES:
                return "Spades";
            case HEARTS:
                return "Hearts";
            case DIAMONDS:
                return "Diamonds";
            case CLUBS:
                return "Clubs";
            default:
                return "Joker";
        }
    }

    public String valueToString() {
            switch ( value ) {
                case 1:
                    return "Ace";
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                    return Integer.toString(value);
                case 11:
                    return "Jack";
                case 12:
                    return "Queen";
                default:
                    return "King";
            }
        }

    public String toString() {
        if (suit == JOKER) {
                return "Joker";
        } else {
            return valueToString() + " of " + suitToString();
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(suit);
        out.writeInt(value);
    }

    public static final Creator<Card> CREATOR
            = new Creator<Card>() {
        public Card createFromParcel(Parcel in) {
            return new Card(in);
        }

        public Card[] newArray(int size) {
            return new Card[size];
        }
    };

    private Card(Parcel in) {
        suit = in.readInt();
        value = in.readInt();
    }
}
