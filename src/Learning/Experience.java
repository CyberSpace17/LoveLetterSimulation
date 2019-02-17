package Learning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class Experience {

	private ArrayList<StateActionCount> experience;

	public Experience() {
		experience = new ArrayList<StateActionCount>();
	}

	public ArrayList<StateActionCount> getExperience() {
		return this.experience;
	}

	/**
	 * checks the order of the experience by StateAction returns true if the
	 * experience is in sorted order and false otherwise
	 * 
	 * @return
	 */
	boolean needSort() {
		if (this.experience.size() < 2)
			return false;
		Iterator<StateActionCount> iter = this.experience.iterator();
		StateActionCount sac1 = iter.next();
		StateActionCount sac2 = null;
		Comparator<StateActionCount> sacComp = new StateActionCount.sortByStateAction();
		while (iter.hasNext()) {
			sac2 = iter.next();
			if (sacComp.compare(sac1, sac2) > 0)
				return true;
			sac1 = sac2;
		}
		return false;
	}

	public void sortExperienceIfNeeded() {
		if (needSort()) {
			Collections.sort(this.experience, new StateActionCount.sortByStateAction());
			return;
		}
	}

	/**
	 * assumes the experience is sorted by stateAction and adds the parameter in
	 * sorted position
	 * 
	 * @param sac
	 */
	public void addInSortedPosition(StateActionCount sac) {
		int index = Collections.binarySearch(this.experience, sac, new StateActionCount.sortByStateAction());
		if (index < 0) {
			index = Math.abs(index) - 1;
			this.experience.add(index, sac);
		}
	}

	/**
	 * @param sac adds a StateActionCount to the Experience only if it is a new
	 *            StateAction
	 */
	public void addExperience(StateActionCount sac) {
		if (getStateSorted(sac.stateAction) == null) {
			this.experience.add(sac);
//			System.out.println("added "+sac.toString()); // may cause a lot of output
		}
	}

	public void updateExperience(StateActionCount sac) {
		StateActionCount oldSac = getStateSorted(sac.stateAction);
		if (oldSac != null) {
			this.experience.remove(this.experience.indexOf(oldSac));
			addExperience(sac);
		}
	}

	public void updateSortedExperience(StateActionCount sac) {
		int index = getStateIndexSorted(sac);
		if (index > -1) {
			this.experience.remove(index);
			this.experience.add(index, sac);
		}
	}

	public void prettyPrintExperience() throws Exception {
		int count = 0;
		System.out.println("prettyPrintExperience");
		for (StateActionCount sac : this.experience) {
			System.out.println(count + " " + sac);
			count++;
		}
	}

	void checkForDup() throws Exception {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		for (StateActionCount sac1 : this.experience) {
			for (int i = 0; i < this.experience.size(); i++) {
				if (this.experience.get(i).matchStateAction(sac1)) {
					indexes.add(i);
				}
			}
			if (indexes.size() > 1) {
				System.out.println("DUPLICATE STATE: " + sac1 + "\n at indexes: " + indexes.toString());
				throw (new Exception("DUPLICATE!"));
			}
			indexes = new ArrayList<Integer>();
		}
	}

	/**
	 * @param state
	 * @return StateActionCount with the same state argument or null if not found
	 */
	public StateActionCount getState(String state) {
		for (StateActionCount sac : this.experience) {
			if (sac.stateAction.equals(state)) {
				return sac;
			}
		}
		return null;
	}

	/**
	 * @param state
	 * @return StateActionCount with the same state argument or null if not found
	 *         assumes the experience is in sorted order
	 */
	public StateActionCount getStateSorted(String state) {
		int index = Collections.binarySearch(this.experience, new StateActionCount(state),
				new StateActionCount.sortByStateAction());
		return index < 0 ? null : this.experience.get(index);
	}

	/**
	 * @param state
	 * @return int index of sac with the same stateaction argument or -1 if not
	 *         found
	 */
	public int getStateIndexSorted(StateActionCount sac) {
		int index = Collections.binarySearch(this.experience, sac, new StateActionCount.sortByStateAction());
		if (index < 0)
			return -1;
		return index;
	}

	/**
	 * @param sac
	 * @return the maximum value of children of this state
	 * @throws Exception
	 */
	public Double getMaxValueOfChildren(StateActionCount sac) {
//		checkForDup();
		if (sac.children.size() > 0) {
			Double max = getStateSorted(sac.children.get(0)).value;
			for (String child : sac.children) {
				max = Double.max(max, getStateSorted(child).value);
			}
			return max;
		}
		return null;
	}

	@Override
	public String toString() {
		return this.experience.toString();
	}
}
