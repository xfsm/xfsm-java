package com.mabook.xfsm;

import java.util.HashMap;

/**
 * Created by sng2c on 15. 7. 28..
 */
public class RuleSet {

	public static class InitialStateNotSetException extends RuntimeException {
		public InitialStateNotSetException() {
			super(String.format("An initial state is not set."));
		}
	}

	public static class Builder {
		RuleSet ruleSet = new RuleSet();
		boolean initialStateSet = false;

		public Builder state(String stateName, String onEnterAction, String onExitAction) {
			ruleSet.registerState(stateName, onEnterAction, onExitAction);
			return this;
		}

		public Builder event(String event, String fromStateName, String toStateName, String action) {
			ruleSet.registerEvent(event, fromStateName, toStateName, action);
			return this;
		}

		public Builder initialState(String stateName) {
			initialStateSet = true;
			ruleSet.setInitialStateName(stateName);
			return this;
		}

		public RuleSet build() {
			if (!initialStateSet) {
				throw new InitialStateNotSetException();
			}
			return ruleSet;
		}
	}

	HashMap<String, XFSM.State> stateRegistry = new HashMap<>();
	HashMap<String, XFSM.Transition> eventRegistry = new HashMap<>();
	final String initEvent;

	private RuleSet() {
		initEvent = "__init__" + this.hashCode();
	}

	private RuleSet registerState(String stateName, String onEnterAction, String onExitAction) {
		stateRegistry.put(stateName, new XFSM.State(stateName, onEnterAction, onExitAction));
		return this;
	}

	private RuleSet registerEvent(String event, String fromStateName, String toStateName, String action) {
		XFSM.State from = stateRegistry.get(fromStateName);
		XFSM.State to = stateRegistry.get(toStateName);
		if (from == null) {
			throw new XFSM.StateNotFoundException(fromStateName);
		}
		if (to == null) {
			throw new XFSM.StateNotFoundException(toStateName);
		}
		eventRegistry.put(event + "@" + fromStateName, new XFSM.Transition(event, from, to, action));
		return this;
	}

	private RuleSet setInitialStateName(String initialStateName) {
		XFSM.State to = stateRegistry.get(initialStateName);
		if (to == null) {
			throw new XFSM.StateNotFoundException(initialStateName);
		}
		eventRegistry.put(initEvent, new XFSM.Transition(initEvent, null, to, null));
		return this;
	}

	public XFSM.Transition getTransition(XFSM.State state, String event) {
		XFSM.Transition found = null;
		String stateName = null;
		if (state == null) {
			found = eventRegistry.get(initEvent);
		} else {
			found = eventRegistry.get(event + "@" + state.name);
		}
		return found;
	}

	public XFSM.State getState(String stateName) {
		return stateRegistry.get(stateName);
	}
}
