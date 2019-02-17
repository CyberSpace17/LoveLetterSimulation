package org.loveletter.Players;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.loveletter.Card;
import org.loveletter.Player;

import Learning.Experience;
import Learning.StateActionCount;

/**
 * A player that tries to learn how to play better by remembering every state is
 * has seen and updating it's experience at the end of every game. Instantiate
 * in learning mode to generate new experience data. Instantiate in Testing mode
 * to test the players current skill. The learners does not "learn" in testing
 * mode. 
 * 
 * @author ben holley
 *
 */
public class LearnerState extends Player {
	final String fileNameExp = "prev_exp.txt";
	final String[] chooseCardToPlayActions = { "1", "2" };
	final String[] guessCardValues = { "2", "3", "4", "5", "6", "7", "8" };
	final String[] emptyActions = { "" };
	boolean doPrintLearner = true;
	String currentPath;

	Experience experience;
	Stack<StateActionCount> episode;
	String[] remainingPlayers = {};

	double episilon = .01;
	int gamesPlayed = 0;
	int saveFreq = 100; // decrease to save more often
	int winValue = 1;
	int loseValue = 0;
	Mode mode;

	public LearnerState(Mode mode) {
		this.mode = mode;
		this.episode = new Stack<StateActionCount>();
		this.experience = new Experience();
		try {
			this.currentPath = (new java.io.File(".").getCanonicalPath()) + "\\" + this.fileNameExp;
		} catch (Exception e) {
			System.out.println("SETTING CURRENT PATH FAILED!");
			e.printStackTrace();
		}
		if (mode.equals(Mode.Learning) || mode.equals(Mode.Testing)) {
			try {
				loadExperience();
				this.experience.sortExperienceIfNeeded();
			} catch (Exception e) {
				System.out.println("LOAD EXPERIENCE FAILED!");
				e.printStackTrace();
			}
		} else if (mode.equals(Mode.Debug)) {
			// test something here
		}
	}

	@Override
	public Card chooseCardtoPlay() {
		ArrayList<String> stateActions = buildStateActions(getPlayerKnowledge(-1, null, null), chooseCardToPlayActions);
		addChildrenSorted(stateActions);
		this.episode.push(getAction(stateActions));
		if (this.episode.peek().getAction().equals("1"))
			return playCard1();
		else
			return playCard2();
	}

	@Override
	public Player getPlayerFor(int cardValue, Set<Player> availablePlayers) {
		ArrayList<String> stateActions = buildStateActions(getPlayerKnowledge(cardValue, null, availablePlayers),
				getPlayerForActions(availablePlayers));
		addChildrenSorted(stateActions);
		this.episode.push(getAction(stateActions));
		for (Player p : availablePlayers) {
			if (this.episode.peek().getAction().equals(String.valueOf(p.id))) {
				return p;
			}
		}
		System.out.println("THIS SHOULD NOT HAPPEN getPlayerFor");
		return availablePlayers.iterator().next();
	}

	@Override
	public int guessCardValue(Player p) {
		ArrayList<String> stateActions = buildStateActions(getPlayerKnowledge(-1, p, null), guessCardValues);
		addChildrenSorted(stateActions);
		this.episode.push(getAction(stateActions));
		return Integer.valueOf(this.episode.peek().getAction());
	}

	private StateActionCount getAction(ArrayList<String> stateActions) {
		StateActionCount chosen;
		ArrayList<StateActionCount> actions = new ArrayList<StateActionCount>();
		for (String sa : stateActions) {
			actions.add(this.experience.getStateSorted(sa));
		}
		boolean isGreedy = this.mode.equals(Mode.Testing) || rand.nextDouble() > this.episilon;
		if (isGreedy) {
			chosen = getGreedyAction(stateActions);
		} else {
			chosen = getRandomAction(stateActions);
		}
		return chosen;
	}

	private StateActionCount getGreedyAction(ArrayList<String> stateActions) {
		StateActionCount chosen = new StateActionCount();
		StateActionCount temp;
		for (String sa : stateActions) {
			temp = this.experience.getStateSorted(sa);
			temp = temp == null ? new StateActionCount(sa) : temp;// only needed in testing mode
			if (chosen.value <= temp.value) {
				chosen = temp;
			}
		}
		return chosen;
	}

	private StateActionCount getRandomAction(ArrayList<String> stateActions) {
		StateActionCount chosen = new StateActionCount(stateActions.get(rand.nextInt(stateActions.size())));
		return chosen;
	}

	private String[] getPlayerForActions(Set<Player> availablePlayers) {
		ArrayList<String> players = new ArrayList<String>();
		for (Player p : availablePlayers) {
			players.add(String.valueOf(p.id));
		}
		return players.toArray(remainingPlayers);
	}

	private ArrayList<String> buildStateActions(String state, String[] actions) {
		ArrayList<String> temp = new ArrayList<String>();
		for (String a : actions) {
			temp.add(state + a);
		}
		return temp;
	}

	private void addChildrenSorted(ArrayList<String> children) {
		// add children to previous state
		if (!this.episode.empty()) {
			for (String child : children) {
				this.episode.peek().addChild(child);
			}
		}
		if (this.mode.equals(Mode.Learning)) {// only modify experience if in learn mode
			for (String child : children) {
				this.experience.addInSortedPosition(new StateActionCount(child));
			}
		}
	}

	private String getPlayerKnowledge(int card, Player player, Set<Player> players) {
		String boardKnow = getBoardKnowledge();
		if (card != -1)
			boardKnow += card;
		if (player != null)
			boardKnow += player.id;
		if (players != null) {
			for (Player p : players) {
				boardKnow += p.id;
			}
		}
		return boardKnow;
	}

	private String getBoardKnowledge() {
		StringBuffer know = new StringBuffer();
		know.append(this.id);
		if (this.card1 != null)
			know.append(this.card1.value);
		if (this.card2 != null)
			know.append(this.card2.value);
		know.append(getIsGuarded());
		know.append(this.getCardsLeft(Card.BARON));
		know.append(this.getCardsLeft(Card.COUNTESS));
		know.append(this.getCardsLeft(Card.GUARD));
		know.append(this.getCardsLeft(Card.KING));
		know.append(this.getCardsLeft(Card.MAID));
		know.append(this.getCardsLeft(Card.PRIEST));
		know.append(this.getCardsLeft(Card.PRINCE));
		know.append(this.getCardsLeft(Card.PRINCESS));
		know.append(getKnownCards());
//		System.out.println("BOARD KNOWLEDGE: " + know.toString());
		return know.toString();
	}

	private String getKnownCards() {
		String s = "";
		for (Player p : this.board.players) {
			if (this != p) {
				if (this.knownCards.get(p) == null) {
					s += "N";
				} else {
					s += this.knownCards.get(p).value;
				}
			}
		}
		return s;
	}

	private String getIsGuarded() {
		String s = "";
		for (Player p : this.board.players) {
			s += this.board.players.get(p.id).isGuarded ? 'T' : 'F';
		}
		return s;
	}

	public void saveExperience() {
		System.out.println("SAVING...");
		long start = System.currentTimeMillis();
		try {
			FileWriter writer = new FileWriter(currentPath);
			writer.write(this.experience.toString());
			writer.flush();
			writer.close();
			System.out.println("SAVED in " + (System.currentTimeMillis() - start) / 1000.0 + " seconds");
		} catch (Exception e) {
			System.out.println("Failed to Save Experience!");
			e.printStackTrace();
		}
	}

	public boolean isTimeToSave() {
		return this.mode.equals(Mode.Learning) && gamesPlayed % saveFreq == 0;
	}

	/**
	 * @return returns this learners experience
	 */
	public Experience getExperience() {
		return this.experience;
	}

	/**
	 * collects all the experience from the learner players
	 * 
	 * @param players
	 */
	public void collectExperience(List<Player> players) {
		for (Player p : players) {
			if (p.getClass().equals(LearnerState.class)) {
				if (p.id != this.id) {
					LearnerState temp = (LearnerState) p;
					for (StateActionCount sac : temp.getExperience().getExperience()) {
						this.experience.addInSortedPosition(sac);
					}
				}
			}
		}
	}

	private void loadExperience() throws Exception {
		System.out.println("LOADING...");
		long memSize = getExperienceFileOrMakeNew().length();
		List<String> exp = Files.readAllLines(getExperienceFileOrMakeNew().toPath());
		if (memSize != 0l)
			runNewParseExperience(exp.size() > 0 ? exp.get(0) : "");
	}

	private void runNewParseExperience(String exp) throws InterruptedException, ExecutionException {
		long start1 = System.currentTimeMillis();
		String[] states = exp.split(",");
		int poolSize = Runtime.getRuntime().availableProcessors() + 1;
		int chunkSize = states.length / poolSize == 0 ? 1 : states.length / poolSize;
		ExecutorService pool = Executors.newFixedThreadPool(poolSize);
		int loops = states.length % chunkSize == 0 ? states.length / chunkSize : states.length / chunkSize + 1;
		int offset = 0;
		int limit = 0;
		ArrayList<Future<List<StateActionCount>>> futures = new ArrayList<Future<List<StateActionCount>>>();
		for (int i = 0; i < loops; i++) {
			offset = i * chunkSize;
			limit = offset + chunkSize;
			limit = limit >= states.length ? states.length : limit;
			String[] subset = Arrays.copyOfRange(states, offset, limit);
			futures.add(pool.submit(new parseExperience(subset)));
		}
		for (Future<List<StateActionCount>> future : futures) {
			for (StateActionCount sac : future.get()) {
				this.experience.addExperience(sac);
			}
		}
		pool.shutdown();
		System.out.println("experice size: " + experience.getExperience().size() + " parsed in: "
				+ (System.currentTimeMillis() - start1) / 1000.0 + " seconds");
	}

	/**
	 * deletes experience file at default location. use with caution.
	 * 
	 * @throws Exception
	 */
	public void resetExperience() throws Exception {
		File file = getExperienceFileOrMakeNew();
		file.delete();
	}

	private void updateExperience(int result) {

		// result should be 1 for a win and 0 otherwise
		double futureDiscount = .9;
		double learnRate = .9;

		StateActionCount termState = this.episode.pop();
		termState.value = (double) result;
		termState.children = new ArrayList<String>();
		this.experience.addInSortedPosition(termState);

		StateActionCount child = termState;

		while (!this.episode.empty()) {
			StateActionCount parent = this.episode.pop();
			this.experience.addInSortedPosition(parent);

			StateActionCount prevParent = this.experience.getExperience()
					.get(this.experience.getStateIndexSorted(parent));
			prevParent.addChild(child.stateAction);
			for (String c : prevParent.children) {
				this.experience.addInSortedPosition(new StateActionCount(c));
			}
			Double maxVal = this.experience.getMaxValueOfChildren(prevParent);
			Double newValue = prevParent.value + learnRate * (futureDiscount * maxVal - prevParent.value);
			prevParent.value = newValue;

			this.experience.updateSortedExperience(prevParent);
			child = prevParent;
		}
	}

	private void addTerminalState(String state) {
		this.episode.push(new StateActionCount(buildStateActions(state, emptyActions).get(0)));
	}

	/**
	 * if the learner is in test mode only resets the player.
	 * 
	 * if the learner is in learn mode then updates the experience of the player and
	 * resets the player.
	 * 
	 * @param result
	 * @throws Exception
	 * 
	 */
	public void updateAtEnd(boolean didWin) {
		if (this.mode.equals(Mode.Learning)) {
			addTerminalState(getPlayerKnowledge(-1, null, null));
			updateExperience(didWin ? this.winValue : this.loseValue);
			this.gamesPlayed++;
		}
		if (this.mode.equals(Mode.Testing)) {
			this.episode = new Stack<StateActionCount>();
		}
	}

	private File getExperienceFileOrMakeNew() throws Exception {
		File expFile = new File(currentPath);
		if (expFile.isFile()) {
			return expFile;
		} else {
			System.out.println("Path: " + expFile.getPath());
			expFile.createNewFile();
			return expFile;
		}
	}

	public static enum Mode {
		Debug, Learning, Testing;
	}
}

class parseExperience implements Callable<List<StateActionCount>> {
	String[] exp;

	@Override
	public List<StateActionCount> call() throws Exception {
		List<StateActionCount> experience = new ArrayList<StateActionCount>();
		for (String e : exp) {
			e = e.replace("[", "");
			e = e.replace("]", "");
			e = e.trim();
			String[] parts = e.split(" ");
			if (parts.length > 2) {
				experience.add(new StateActionCount(parts[0], Double.parseDouble(parts[1]), getChildren(parts[2])));
			} else {
				experience.add(new StateActionCount(parts[0], Double.parseDouble(parts[1])));
			}
		}
		return experience;
	}

	private ArrayList<String> getChildren(String str) {
		return new ArrayList<String>(Arrays.asList(str.replace(",", "").split(":")));
	}

	parseExperience(String[] raw) {
		this.exp = raw;
	}
}