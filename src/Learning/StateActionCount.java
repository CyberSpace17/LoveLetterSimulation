package Learning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class StateActionCount {
	public String stateAction;
	public Double value;
	public ArrayList<String> children;

	public StateActionCount() {
		this.children = new ArrayList<String>();
		this.value = 0.0;
	}

	public StateActionCount(String stateAction) {
		this.stateAction = stateAction;
		this.value = 0.0;
		this.children = new ArrayList<String>();
	}

	public StateActionCount(String stateAction, Double value) {
		this.stateAction = stateAction;
		this.value = value;
		this.children = new ArrayList<String>();
	}

	public StateActionCount(String stateAction, Double value, ArrayList<String> children) {
		this.stateAction = stateAction;
		this.value = value;
		this.children = children;
	}
	
	public String getAction() {
		return this.stateAction.substring(this.stateAction.length()-1);
	}

	public String toString() {
		return this.stateAction + " " + String.valueOf(this.value) +" "+ this.children.toString().replace(", ", ":");
	}

	public boolean matchStateAction(StateActionCount sac) {
		return this.stateAction.equals(sac.stateAction);
	}
	
	/**
	 * @param child
	 * adds a child to this state if is not already a child
	 */
	public void addChild(String child) {
		if(!this.children.contains(child)) {
			this.children.add(child);
		}
	}

	public static StateActionCount convertToSAC(HashMap<String, Double> sa) {
		return new StateActionCount(sa.keySet().iterator().next(), sa.values().iterator().next());
	}

	public static class sortByStateAction implements Comparator<StateActionCount> {
		@Override
		public int compare(StateActionCount o1, StateActionCount o2) {
			return o1.stateAction.compareTo(o2.stateAction);
		}
	}

	public static class sortByValue implements Comparator<StateActionCount> {
		@Override
		public int compare(StateActionCount o1, StateActionCount o2) {
			return o1.value.compareTo(o2.value);
		}
	}
}
