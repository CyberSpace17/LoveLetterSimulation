package org.loveletter.Players;

import java.util.Set;

import org.loveletter.Card;
import org.loveletter.Player;

/**
 * A player that always play's a random card and guesses the highest card left.
 */
public class Random_High extends Player {
    
    /** random play, but will never play princess */
    @Override
    public Card chooseCardtoPlay() {
        assert(card1 != null && card2 != null);
        if (card1.value == Card.PRINCESS) return playCard2();
        if (card2.value == Card.PRINCESS) return playCard1();
        return rand.nextBoolean() ? playCard1() : playCard2();
    }
    
    /**
     * always returns a random other player
     */
    @Override
    public Player getPlayerFor(int cardValue, Set<Player> availablePlayers) {
        return getRandomPlayer(availablePlayers);
    }

    /**
     * Guard: guess another player's card
     * @return value to guess (2-8). Guessing a Guard is not allowed
     */
    @Override
    public int guessCardValue(Player p) {
    	for (int i = Card.PRINCESS; i > Card.GUARD; i--) {
    		if (getCardsLeft(i) > 0)
    				return i;
    	}
    	
    	//It could be the case that only GUARDS are left -> guess nothing in this case.
    	return -1;
    }
}
