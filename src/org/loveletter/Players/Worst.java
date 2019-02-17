package org.loveletter.Players;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.loveletter.Card;
import org.loveletter.Log;
import org.loveletter.Player;

/**
 * Player that knows the best move but does not play it.
 */
public class Worst extends Player {

	private static Player best = new Best();

	/**
	 * returns the card that is not the best
	 */
	@Override
	public Card chooseCardtoPlay() {
		best.reset(this.board, this.id, card1);
		best.drawCard(card2);
		return card1.equals(best.chooseCardtoPlay()) ? playCard2() : playCard1();
	}

	/**
	 * chooses not the best player to choose unless there is only one option
	 */
	@Override
	public Player getPlayerFor(int cardValue, Set<Player> availablePlayers) {
		if (availablePlayers.size() == 1)
			return availablePlayers.iterator().next();
		best.reset(this.board, this.id, card1);
		availablePlayers.remove(best.getPlayerFor(cardValue, availablePlayers));
		return (Player) availablePlayers.toArray()[rand.nextInt(availablePlayers.size())];
	}

	/**
	 * removes possibly the best option and then plays randomly
	 */
	@Override
	public int guessCardValue(Player p) {
		best.reset(this.board, this.id, card1);
		ArrayList<Integer> cardValues = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 6, 7, 8));
		cardValues.remove(Math.max(0, best.guessCardValue(p) - 2));
		return cardValues.get(rand.nextInt(cardValues.size()));
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (Log.logTRACE) {
			buf.append("[");
			buf.append(card1);
			buf.append(",");
			for (Card card : knownCards.values()) {
				buf.append(card.value);
			}
			buf.append("]");
		}
		return super.toString() + buf.toString();
	}
}