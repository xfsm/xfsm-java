package com.mabook.xfsm;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class XFSM {
	public enum When {INIT, ENTER, TRANSITION, EXIT, SHUTDOWN}

	;

	public interface ActionListener {
		void onAction(XFSM context, When when, String action);
	}

	public static class RuleSet {
		HashMap<String, State> stateRegistry = new HashMap<>();
		HashMap<String, Transition> eventRegistry = new HashMap<>();
		String initialStateName;

		public RuleSet registerState(String stateName, String onEnterAction, String onExitAction) {
			stateRegistry.put(stateName, new State(stateName, onEnterAction, onExitAction));
			return this;
		}

		public RuleSet registerEvent(String event, String fromStateName, String toStateName, String action) {
			State from = stateRegistry.get(fromStateName);
			State to = stateRegistry.get(toStateName);
			eventRegistry.put(event + "@" + fromStateName, new Transition(event, from, to, action));
			return this;
		}

		public State getInitialState() {
			return stateRegistry.get(initialStateName);
		}

		public RuleSet setInitialStateName(String initialStateName) {
			this.initialStateName = initialStateName;
			return this;
		}

		public Transition getTransition(State state, String event) {
			return eventRegistry.get(event + "@" + state.name);
		}
	}

	public static class State {
		public String name;
		public String onEnterAction;
		public String onExitAction;

		public State(String name, String onEnterAction, String onExitAction) {
			this.name = name;
			this.onEnterAction = onEnterAction;
			this.onExitAction = onExitAction;
		}
	}

	public static class Transition {
		public String event;
		public State fromState;
		public State toState;
		public String onTransitAction;

		public Transition(String event, State fromState, State toState, String onTransitAction) {
			this.event = event;
			this.fromState = fromState;
			this.toState = toState;
			this.onTransitAction = onTransitAction;
		}
	}

	State currentState;
	ActionListener actionListener;

	final RuleSet ruleSet;

	public XFSM(RuleSet ruleSet) {
		this(ruleSet, null);
	}

	public XFSM(RuleSet ruleSet, ActionListener actionListener) {
		this.ruleSet = ruleSet;
		this.actionListener = actionListener;
	}

	public State getCurrentState() {
		return currentState;
	}

	public ActionListener getActionListener() {
		return actionListener;
	}

	public void setActionListener(ActionListener actionListener) {
		this.actionListener = actionListener;
	}

	public synchronized String shutdown() {
		if (currentState == null) return null;

		if (currentState.onEnterAction != null) {
			if (actionListener != null) {
				actionListener.onAction(this, When.SHUTDOWN, currentState.onExitAction);
			}
		}
		String exitAction = currentState.onExitAction;
		currentState = null;
		return exitAction;
	}

	public synchronized String init() {
		if (currentState != null) return null;

		this.currentState = ruleSet.getInitialState();

		if (currentState.onEnterAction != null) {
			if (actionListener != null) {
				actionListener.onAction(this, When.INIT, currentState.onEnterAction);
			}
		}

		return currentState.onEnterAction;
	}

	boolean inTask = false;
	public synchronized List<String> emit(String event) {
		if( inTask ) return null;

		inTask = true;
		ArrayList<String> actions = new ArrayList<>();
		try {
			if (currentState != null) {
				Transition transition = ruleSet.getTransition(currentState, event);
				if (transition != null) {

					if (currentState.onExitAction != null) {
						actions.add(currentState.onExitAction);
						if (actionListener != null) {
							actionListener.onAction(this, When.EXIT, currentState.onExitAction);
						}
					}

					if (transition.onTransitAction != null) {
						actions.add(transition.onTransitAction);
						if (actionListener != null) {
							actionListener.onAction(this, When.TRANSITION, transition.onTransitAction);
						}
					}

					currentState = transition.toState;

					if (currentState.onEnterAction != null) {
						actions.add(currentState.onEnterAction);
						if (actionListener != null) {
							actionListener.onAction(this, When.ENTER, currentState.onEnterAction);
						}
					}
				}
			}
		}
		finally {
			inTask = false;
		}
		return actions;
	}

}
