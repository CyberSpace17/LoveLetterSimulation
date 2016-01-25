package org.loveletter;

/**
 * One card in the game. There are 16 cards with 8 different values.
 */
public class Card {
    public static final int PRINCESS = 8;
    public static final int COUNTESS = 7;
    public static final int KING     = 6;
    public static final int PRINCE   = 5;
    public static final int MAID     = 4;
    public static final int BARON    = 3;
    public static final int PRIEST   = 2;
    public static final int GUARD    = 1;
    
    public final static int[] NumCardsOfValue = {
        5, 2, 2, 2, 2, 1, 1, 1      
    };
    public static final String[] Name_EN = {
        "GUARD", "PRIEST", "BARON", "MAID", "PRINCE", "KING", "COUNTESS", "PRINCESS"
    };
    public static final String[] Name_DE = {
        "Wächter", "Priester", "Baron", "Zofe", "Prinz", "König", "Gräfin", "Prinzessin"
    };

    int value;   // card's value as printed on the card. (1-8)
    String name;
    
    public Card(int value, String name) {
        this.value = value;
        this.name  = name;
    }

    public String toString() {
        return this.value+this.name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + value;
        return result;
    }

    /**
     * cards are equal if they have the same value. (Name doesn't matter!)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Card other = (Card) obj;
        if (value != other.value)
            return false;
        return true;
    }
    
    
}