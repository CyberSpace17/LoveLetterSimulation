package org.loveletter.Players;

import java.util.Set;

import org.loveletter.Card;
import org.loveletter.Player;

/**
 * A player that always play's a random card.
 */
public class True_Random extends Player {
    
    /** random play, and may play princess */
    @Override
    public Card chooseCardtoPlay() {
    	assert(card1 != null && card2 != null);
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
        return 2+rand.nextInt(7);      // card value (2-8)
    }
}
